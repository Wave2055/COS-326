-- =============================================================================
-- COS326 Practical 2 (2026) - Aegis Digital Forensics 5G Incident Response
-- InsertQueries.sql
--
-- Sample data population. Run after CreateStatements.sql.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- EvidenceCatalog (>= 4 valid entries)
-- -----------------------------------------------------------------------------
INSERT INTO EvidenceCatalog (evidence_hash, file_type) VALUES
    ('hash_101', 'PCAP'),
    ('hash_102', 'PCAP'),
    ('hash_103', 'LOG'),
    ('hash_104', 'LOG'),
    ('hash_105', 'PCAP');

-- -----------------------------------------------------------------------------
-- NetworkIncident (>= 3 valid records)
-- evidence_hashes must reference only registered, non-duplicate hashes above,
-- otherwise trg_check_evidence_integrity will reject the insert.
-- -----------------------------------------------------------------------------
INSERT INTO NetworkIncident (incident_code, target_ip, severity, detected_at, slice_id, evidence_hashes) VALUES
    ('NET-001', '10.20.30.1', 'HIGH',     '2026-08-10 09:15:00', 'slice-alpha-01', ARRAY['hash_101', 'hash_102']),
    ('NET-002', '10.20.30.2', 'CRITICAL', '2026-08-11 14:42:00', 'slice-alpha-01', ARRAY['hash_103']),
    ('NET-003', '10.20.30.3', 'MEDIUM',   '2026-08-12 08:05:00', 'slice-beta-02',  ARRAY['hash_104', 'hash_105']);

-- -----------------------------------------------------------------------------
-- NodeBreachIncident (>= 2 valid records)
-- -----------------------------------------------------------------------------
INSERT INTO NodeBreachIncident (incident_code, target_ip, severity, detected_at, node_id, assigned_investigator, is_contained) VALUES
    ('NBI-001', '10.20.40.1', 'CRITICAL', '2026-08-13 11:30:00', 'edge-node-07', ROW('Dr.', 'Alex', 'Mercer'),    FALSE),
    ('NBI-002', '10.20.40.2', 'HIGH',     '2026-08-14 16:20:00', 'edge-node-12', ROW('Mr.', 'Sipho', 'Dlamini'), TRUE);
