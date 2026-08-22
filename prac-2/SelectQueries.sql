-- =============================================================================
-- COS326 Practical 2 (2026) - Aegis Digital Forensics 5G Incident Response
-- SelectQueries.sql
--
-- Analytical report queries. Run after CreateStatements.sql and
-- InsertQueries.sql.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. Investigator Assignment Summary
-- Incident code, target IP, containment status, and formatted investigator
-- name for every breach incident.
-- -----------------------------------------------------------------------------
SELECT
    incident_code,
    target_ip,
    is_contained,
    format_investigator_name(assigned_investigator) AS investigator_name
FROM NodeBreachIncident
ORDER BY incident_code;

-- -----------------------------------------------------------------------------
-- 2. Evidence Hash Lookup Query
-- All NetworkIncident records whose evidence_hashes array contains a given
-- hash (example hash: 'hash_101').
-- -----------------------------------------------------------------------------
SELECT
    incident_id,
    incident_code,
    slice_id,
    evidence_hashes
FROM NetworkIncident
WHERE 'hash_101' = ANY (evidence_hashes)
ORDER BY incident_code;

-- -----------------------------------------------------------------------------
-- 3. High-Risk Network Slices Report
-- Number of CRITICAL/HIGH severity incidents per network slice.
-- -----------------------------------------------------------------------------
SELECT
    slice_id,
    COUNT(*) AS high_risk_incident_count
FROM NetworkIncident
WHERE severity IN ('CRITICAL', 'HIGH')
GROUP BY slice_id
ORDER BY high_risk_incident_count DESC;
