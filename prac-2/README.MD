# COS326 Practical 2 (2026) — Aegis Digital Forensics 5G Incident Response Platform

A PostgreSQL 18 object-relational schema for Aegis Digital Forensics: table
inheritance modeling incident specialization, custom domains/enums/composite
types, PL/pgSQL validation functions, and triggers enforcing evidence
integrity and an immutable deletion audit log. A small Java/JDBC console app
is included as an optional demo harness for running and observing the
scripts without needing a `psql` session.

## Tech Stack

- PostgreSQL 18 (domains, ENUM, composite UDTs, sequences, table inheritance,
  PL/pgSQL functions and triggers)
- Java (JDK 17+, developed against 21) — optional JDBC demo harness only
- Maven (build tool for the optional Java harness)

## Submission Deliverables

| File | Purpose |
|---|---|
| `EERD.pdf` | Enhanced Entity-Relationship Diagram of the schema |
| `CreateStatements.sql` | All DDL: domains, enum, UDT, sequences, inherited tables, auxiliary/audit tables, PL/pgSQL functions, trigger functions and triggers |
| `InsertQueries.sql` | Sample data population |
| `SelectQueries.sql` | Analytical report queries |
| `TestTriggerQueries.sql` | DML proving the triggers enforce integrity and capture audit logs (TC-1 – TC-4) |

These four `.sql` files are self-contained and run correctly with `psql`
alone — the Java harness below is an optional convenience, not a
requirement of the schema.

## Project Structure

```
prac-2/
├── EERD.pdf                   # Task 1 deliverable
├── EERD.svg                   # Single-file SVG source the EERD.pdf was rendered from
├── CreateStatements.sql       # Task 2 deliverable
├── InsertQueries.sql          # Task 3 deliverable
├── SelectQueries.sql          # Task 4 deliverable
├── TestTriggerQueries.sql     # Task 5 deliverable
├── pom.xml                    # Maven project for the optional Java demo harness
└── src/
    └── main/
        └── java/
            ├── Main.java            # Entry point: runs the .sql scripts over JDBC
            └── db/
                ├── DbConfig.java       # Reads connection settings from PG* env vars
                └── SqlScriptRunner.java # Splits/executes a .sql file, prints results
```

## Prerequisites

- **PostgreSQL 18** server and the `psql` client
- **JDK 17 or later** (only needed to build/run the optional Java harness)
- **Maven** (only needed to build/run the optional Java harness)

### Windows

1. Install PostgreSQL 18 from https://www.postgresql.org/download/windows/
   (or `winget install PostgreSQL.PostgreSQL.18`). Remember the password you
   set for the `postgres` superuser and make sure `psql` is on your `PATH`
   (the installer adds `C:\Program Files\PostgreSQL\18\bin`).
2. Install a JDK 17+, e.g. `winget install EclipseAdoptium.Temurin.21.JDK`.
3. Maven isn't published on winget, so install it one of these ways:
   - **Chocolatey** (if installed): `choco install maven`
   - **Scoop** (if installed): `scoop install maven`
   - **Manual**: download the binary zip from
     https://maven.apache.org/download.cgi and extract it (e.g. to
     `C:\Program Files\Apache\maven`). The zip contains a version-numbered
     folder, so the actual `bin` directory ends up nested one level deeper,
     e.g. `C:\Program Files\Apache\maven\apache-maven-3.9.16\bin` — add
     *that* path (System Properties &gt; Environment Variables &gt; edit your
     user `Path` variable, or in PowerShell:
     `[Environment]::SetEnvironmentVariable("PATH", $env:PATH + ";C:\Program Files\Apache\maven\apache-maven-3.9.16\bin", "User")`).
     Restart your terminal/IDE afterwards for the change to take effect.
4. Verify:
   ```powershell
   psql --version
   java -version
   mvn -v
   ```

### Fedora

1. Install PostgreSQL 18 and initialize the cluster:
   ```bash
   sudo dnf install -y postgresql-server postgresql-contrib
   sudo postgresql-setup --initdb
   sudo systemctl enable --now postgresql
   ```
2. Install a JDK 17+ and Maven:
   ```bash
   sudo dnf install -y java-21-openjdk-devel maven
   ```
3. Verify:
   ```bash
   psql --version
   java -version
   mvn -v
   ```

## Database Setup

Create a database and, if needed, a role with a password (adjust names to
taste — the defaults below match what the Java harness expects):

```bash
# Windows: run from an elevated or regular shell with psql on PATH
# Fedora:  run as the postgres OS user, e.g. `sudo -u postgres psql`
psql -U postgres -c "CREATE DATABASE aegis_forensics;"
```

## Running the SQL Scripts (required — via psql)

Run the four scripts in order against the new database:

```bash
psql -U postgres -d aegis_forensics -f CreateStatements.sql
psql -U postgres -d aegis_forensics -f InsertQueries.sql
psql -U postgres -d aegis_forensics -f SelectQueries.sql
psql -U postgres -d aegis_forensics -f TestTriggerQueries.sql
```

`TestTriggerQueries.sql` is *expected* to raise errors for TC-1 and TC-2 —
that's the trigger correctly rejecting bad data. `psql`'s default autocommit
behaviour means one failed statement doesn't block the statements after it.

## Running the Optional Java Demo Harness

The harness connects over JDBC and runs the same four scripts, printing each
statement's result to the console — useful for demoing the practical without
switching to a `psql` session. It reads connection details from the standard
libpq environment variables, so the same variable names work for both `psql`
and the harness.

Environment variables (all optional — shown with their defaults):

| Variable | Default |
|---|---|
| `PGHOST` | `localhost` |
| `PGPORT` | `5432` |
| `PGDATABASE` | `aegis_forensics` |
| `PGUSER` | `postgres` |
| `PGPASSWORD` | *(empty)* |

### Windows (PowerShell)

```powershell
cd prac-2
$env:PGPASSWORD = "your-postgres-password"
mvn clean package
java -jar target\u23621372-Moodally-J-Practical2.jar all
```

### Fedora (bash)

```bash
cd prac-2
export PGPASSWORD="your-postgres-password"
mvn clean package
java -jar target/u23621372-Moodally-J-Practical2.jar all
```

Run the jar from inside the `prac-2` directory — it looks for the `.sql`
files relative to the current working directory. Pass a single stage
instead of `all` to run just one script:

```bash
java -jar target/u23621372-Moodally-J-Practical2.jar create
java -jar target/u23621372-Moodally-J-Practical2.jar insert
java -jar target/u23621372-Moodally-J-Practical2.jar select
java -jar target/u23621372-Moodally-J-Practical2.jar test
```

## Schema Overview

- **Incident** (base table) — shared columns, PK generated by `incident_seq`.
- **NetworkIncident** / **NodeBreachIncident** — `INHERITS (Incident)`,
  each adding its own subtype-specific columns.
- **EvidenceCatalog** — the register of valid SHA-256 evidence hashes.
- **DeletedIncidentAudit** — immutable log populated only by the
  `AFTER DELETE` triggers, never written to directly.

## Business Rules Enforced

1. `NetworkIncident.evidence_hashes` may only contain hashes that are
   registered in `EvidenceCatalog` (`trg_check_evidence_integrity`,
   `BEFORE INSERT OR UPDATE`).
2. `NetworkIncident.evidence_hashes` may not contain duplicate hashes
   (same trigger, using `has_duplicate_hashes`).
3. Deleting a row from `NetworkIncident` or `NodeBreachIncident`
   automatically copies its data, the deletion timestamp, and
   `CURRENT_USER` into `DeletedIncidentAudit` (`AFTER DELETE`).
4. `target_ip` values must be valid dotted-decimal IPv4 addresses
   (`NodeIPType` domain `CHECK` constraint).
5. `incident_code` is unique per incident (enforced independently on each
   child table, since PostgreSQL inheritance does not propagate `UNIQUE`
   constraints to child tables).
