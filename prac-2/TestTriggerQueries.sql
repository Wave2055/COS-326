-- =============================================================================
-- COS326 Practical 2 (2026) - Aegis Digital Forensics 5G Incident Response
-- TestTriggerQueries.sql
--
-- DML operations proving that trg_check_evidence_integrity and
-- trg_audit_deleted_network_incident / trg_audit_deleted_node_breach_incident
-- behave as required. Run after CreateStatements.sql and InsertQueries.sql.
--
-- Run with autocommit ON (psql default): each statement below commits or
-- fails independently, so an expected failure in TC-1/TC-2 does not block
-- the tests that follow it.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- TC-1: INSERT INTO NetworkIncident with an unregistered evidence hash.
-- Expected: trg_check_evidence_integrity raises an exception and the
-- transaction aborts - no row is inserted.
-- -----------------------------------------------------------------------------
INSERT INTO NetworkIncident (incident_code, target_ip, severity, detected_at, slice_id, evidence_hashes)
VALUES ('NET-TC1', '10.20.50.1', 'HIGH', CURRENT_TIMESTAMP, 'slice-test-01', ARRAY['hash_999_unregistered']);

-- Verify no row was inserted:
SELECT * FROM NetworkIncident WHERE incident_code = 'NET-TC1';

-- -----------------------------------------------------------------------------
-- TC-2: INSERT / UPDATE on NetworkIncident with a duplicate evidence hash.
-- Expected: trg_check_evidence_integrity raises an exception in both cases.
-- -----------------------------------------------------------------------------
INSERT INTO NetworkIncident (incident_code, target_ip, severity, detected_at, slice_id, evidence_hashes)
VALUES ('NET-TC2', '10.20.50.2', 'HIGH', CURRENT_TIMESTAMP, 'slice-test-01', ARRAY['hash_101', 'hash_101']);

UPDATE NetworkIncident
SET evidence_hashes = ARRAY['hash_102', 'hash_102']
WHERE incident_code = 'NET-001';

-- Verify the insert was rejected and the existing row was left unmodified:
SELECT * FROM NetworkIncident WHERE incident_code = 'NET-TC2';
SELECT incident_code, evidence_hashes FROM NetworkIncident WHERE incident_code = 'NET-001';

-- -----------------------------------------------------------------------------
-- TC-3: DELETE FROM NetworkIncident.
-- Expected: row is deleted and trg_audit_deleted_network_incident copies it
-- into DeletedIncidentAudit with incident_type = 'NETWORK'.
-- -----------------------------------------------------------------------------
DELETE FROM NetworkIncident WHERE incident_code = 'NET-003';

-- Verify audit capture:
SELECT * FROM DeletedIncidentAudit WHERE incident_code = 'NET-003';

-- -----------------------------------------------------------------------------
-- TC-4: DELETE FROM NodeBreachIncident.
-- Expected: row is deleted and trg_audit_deleted_node_breach_incident copies
-- it into DeletedIncidentAudit with incident_type = 'NODE_BREACH'.
-- -----------------------------------------------------------------------------
DELETE FROM NodeBreachIncident WHERE incident_code = 'NBI-002';

-- Verify audit capture:
SELECT * FROM DeletedIncidentAudit WHERE incident_code = 'NBI-002';
