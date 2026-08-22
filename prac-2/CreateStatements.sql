-- =============================================================================
-- COS326 Practical 2 (2026) - Aegis Digital Forensics 5G Incident Response
-- CreateStatements.sql
--
-- All DDL for domains, enums, UDTs, sequences, inherited tables, auxiliary
-- tables, PL/pgSQL utility/validation functions, and triggers.
--
-- Run this script first, against a fresh PostgreSQL 18 database.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. Domains, Enums, UDTs & Sequences
-- -----------------------------------------------------------------------------

-- NodeIPType: a domain constraining text to a valid dotted-decimal IPv4 address
-- (also bounded to 15 characters, the longest possible IPv4 literal).
CREATE DOMAIN NodeIPType AS VARCHAR(15)
    CHECK (
        VALUE ~ '^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)){3}$'
    );

-- SeverityType: enumerated incident severity levels.
CREATE TYPE SeverityType AS ENUM ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL');

-- InvestigatorName: composite UDT for a structured investigator name.
CREATE TYPE InvestigatorName AS (
    title      VARCHAR(10),
    first_name VARCHAR(50),
    last_name  VARCHAR(50)
);

-- Sequences driving the base Incident PK and the EvidenceCatalog PK.
CREATE SEQUENCE incident_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE evidence_seq START WITH 1 INCREMENT BY 1;

-- -----------------------------------------------------------------------------
-- 2. Table Inheritance Hierarchy
-- -----------------------------------------------------------------------------

-- Incident: base table shared by every incident subtype.
CREATE TABLE Incident (
    incident_id   INTEGER PRIMARY KEY DEFAULT nextval('incident_seq'),
    incident_code VARCHAR(10) UNIQUE NOT NULL,
    target_ip     NodeIPType,
    severity      SeverityType NOT NULL,
    detected_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER SEQUENCE incident_seq OWNED BY Incident.incident_id;

-- NetworkIncident: specialization capturing 5G network-slice evidence.
CREATE TABLE NetworkIncident (
    slice_id        VARCHAR(20) NOT NULL,
    evidence_hashes TEXT[] NOT NULL DEFAULT '{}'
) INHERITS (Incident);

-- PostgreSQL table inheritance does not propagate PRIMARY KEY/UNIQUE
-- constraints to child tables, so they must be declared explicitly here.
ALTER TABLE NetworkIncident ADD PRIMARY KEY (incident_id);
ALTER TABLE NetworkIncident ADD UNIQUE (incident_code);

-- NodeBreachIncident: specialization capturing edge-node breach evidence.
CREATE TABLE NodeBreachIncident (
    node_id               VARCHAR(20) NOT NULL,
    assigned_investigator InvestigatorName,
    is_contained          BOOLEAN NOT NULL DEFAULT FALSE
) INHERITS (Incident);

ALTER TABLE NodeBreachIncident ADD PRIMARY KEY (incident_id);
ALTER TABLE NodeBreachIncident ADD UNIQUE (incident_code);

-- -----------------------------------------------------------------------------
-- 3. Auxiliary Lookup & Audit Tables
-- -----------------------------------------------------------------------------

-- EvidenceCatalog: register of valid SHA-256 evidence hashes.
CREATE TABLE EvidenceCatalog (
    hash_id       INTEGER PRIMARY KEY DEFAULT nextval('evidence_seq'),
    evidence_hash TEXT UNIQUE NOT NULL,
    file_type     VARCHAR(20)
);

ALTER SEQUENCE evidence_seq OWNED BY EvidenceCatalog.hash_id;

-- DeletedIncidentAudit: immutable log of deleted NetworkIncident and
-- NodeBreachIncident rows. incident_type discriminates which subtype the
-- deleted row came from; columns specific to the other subtype are left NULL.
CREATE TABLE DeletedIncidentAudit (
    audit_id              SERIAL PRIMARY KEY,
    incident_id           INTEGER NOT NULL,
    incident_code         VARCHAR(10) NOT NULL,
    target_ip             NodeIPType,
    severity               SeverityType NOT NULL,
    detected_at            TIMESTAMP NOT NULL,
    incident_type          VARCHAR(20) NOT NULL CHECK (incident_type IN ('NETWORK', 'NODE_BREACH')),

    -- NetworkIncident-specific attributes (NULL for NODE_BREACH rows)
    slice_id               VARCHAR(20),
    evidence_hashes        TEXT[],

    -- NodeBreachIncident-specific attributes (NULL for NETWORK rows)
    node_id                VARCHAR(20),
    assigned_investigator  InvestigatorName,
    is_contained           BOOLEAN,

    deleted_at             TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_by              VARCHAR(50) NOT NULL
);

-- -----------------------------------------------------------------------------
-- 4. PL/pgSQL Utility & Validation Functions
-- -----------------------------------------------------------------------------

-- format_investigator_name: formats an InvestigatorName UDT into a single
-- display string, e.g. 'Dr. Alex Mercer'.
CREATE OR REPLACE FUNCTION format_investigator_name(investigator InvestigatorName)
RETURNS TEXT AS $$
BEGIN
    IF investigator IS NULL OR investigator.first_name IS NULL THEN
        RETURN NULL;
    END IF;

    RETURN TRIM(
        COALESCE(investigator.title || ' ', '') ||
        investigator.first_name || ' ' ||
        investigator.last_name
    );
END;
$$ LANGUAGE plpgsql IMMUTABLE;

-- is_hash_registered: TRUE if the given hash exists in EvidenceCatalog.
CREATE OR REPLACE FUNCTION is_hash_registered(hash TEXT)
RETURNS BOOLEAN AS $$
DECLARE
    hash_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO hash_count
    FROM EvidenceCatalog
    WHERE evidence_hash = hash;

    RETURN hash_count > 0;
END;
$$ LANGUAGE plpgsql STABLE;

-- has_duplicate_hashes: TRUE if the given array contains any hash string more
-- than once. Implemented with a nested FOREACH loop as required by the spec.
CREATE OR REPLACE FUNCTION has_duplicate_hashes(hashes TEXT[])
RETURNS BOOLEAN AS $$
DECLARE
    outer_hash  TEXT;
    inner_hash  TEXT;
    outer_index INTEGER := 0;
    inner_index INTEGER;
BEGIN
    FOREACH outer_hash IN ARRAY hashes LOOP
        outer_index := outer_index + 1;
        inner_index := 0;

        FOREACH inner_hash IN ARRAY hashes LOOP
            inner_index := inner_index + 1;

            IF outer_index <> inner_index AND outer_hash = inner_hash THEN
                RETURN TRUE;
            END IF;
        END LOOP;
    END LOOP;

    RETURN FALSE;
END;
$$ LANGUAGE plpgsql IMMUTABLE;

-- validate_evidence_array: TRUE only if every hash in the array is registered
-- in EvidenceCatalog and no duplicates are present.
CREATE OR REPLACE FUNCTION validate_evidence_array(hashes TEXT[])
RETURNS BOOLEAN AS $$
DECLARE
    hash_value TEXT;
BEGIN
    IF hashes IS NULL OR array_length(hashes, 1) IS NULL THEN
        RETURN TRUE;
    END IF;

    IF has_duplicate_hashes(hashes) THEN
        RETURN FALSE;
    END IF;

    FOREACH hash_value IN ARRAY hashes LOOP
        IF NOT is_hash_registered(hash_value) THEN
            RETURN FALSE;
        END IF;
    END LOOP;

    RETURN TRUE;
END;
$$ LANGUAGE plpgsql STABLE;

-- -----------------------------------------------------------------------------
-- 5. Trigger Functions & Triggers
-- -----------------------------------------------------------------------------

-- check_evidence_integrity: rejects an INSERT/UPDATE on NetworkIncident if
-- evidence_hashes contains an unregistered or duplicate hash.
CREATE OR REPLACE FUNCTION check_evidence_integrity()
RETURNS TRIGGER AS $$
BEGIN
    IF NOT validate_evidence_array(NEW.evidence_hashes) THEN
        RAISE EXCEPTION
            'Evidence hash validation failed for incident %: evidence_hashes must contain only registered, non-duplicate hashes',
            NEW.incident_code;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_check_evidence_integrity
    BEFORE INSERT OR UPDATE ON NetworkIncident
    FOR EACH ROW
    EXECUTE FUNCTION check_evidence_integrity();

-- audit_deleted_incident: copies a deleted NetworkIncident/NodeBreachIncident
-- row into DeletedIncidentAudit, stamping the deletion time and acting user.
-- TG_TABLE_NAME distinguishes which child table fired the trigger.
CREATE OR REPLACE FUNCTION audit_deleted_incident()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_TABLE_NAME = 'networkincident' THEN
        INSERT INTO DeletedIncidentAudit (
            incident_id, incident_code, target_ip, severity, detected_at,
            incident_type, slice_id, evidence_hashes, deleted_by
        ) VALUES (
            OLD.incident_id, OLD.incident_code, OLD.target_ip, OLD.severity, OLD.detected_at,
            'NETWORK', OLD.slice_id, OLD.evidence_hashes, CURRENT_USER
        );
    ELSIF TG_TABLE_NAME = 'nodebreachincident' THEN
        INSERT INTO DeletedIncidentAudit (
            incident_id, incident_code, target_ip, severity, detected_at,
            incident_type, node_id, assigned_investigator, is_contained, deleted_by
        ) VALUES (
            OLD.incident_id, OLD.incident_code, OLD.target_ip, OLD.severity, OLD.detected_at,
            'NODE_BREACH', OLD.node_id, OLD.assigned_investigator, OLD.is_contained, CURRENT_USER
        );
    END IF;

    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_audit_deleted_network_incident
    AFTER DELETE ON NetworkIncident
    FOR EACH ROW
    EXECUTE FUNCTION audit_deleted_incident();

CREATE TRIGGER trg_audit_deleted_node_breach_incident
    AFTER DELETE ON NodeBreachIncident
    FOR EACH ROW
    EXECUTE FUNCTION audit_deleted_incident();
