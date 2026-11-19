# COS326 Practical 5 2026 - Social Network Investigation

u23621372 - Moodally J

A Neo4j graph database built from the supplied CSV datasets, plus a Java Swing
application that lets an investigator query it without writing Cypher.

Section 2 is the demo script. Work through it top to bottom and every question
in the mark sheet is shown in order. The sections after it explain each step.

---

## 1. Files

| Path | Contents |
| --- | --- |
| `Task1Queries.txt` | All Cypher queries for Tasks 1 - 3 |
| `pom.xml`, `src/` | Task 4 Java application |
| `screenshots/` | Evidence of the application's main functions |
| `persons.csv` | Supplied dataset (8828 accounts) |
| `follows.csv` | Supplied dataset (4970 follow relationships) |
| `import/` | Copies of the two CSVs, mounted into the Neo4j import directory |

---

## 2. Demo command list (in order)

Every command for the demo, top to bottom. Expected results are given so each
step can be checked as it runs.

**Terminal - start the server and the application**

```bash
cd ~/Documents/COS-326/pracs/prac-5
podman start neo4j-prac5                   # skip if already Up
podman ps --filter name=neo4j-prac5        # expect: Up ...
mvn clean package                          # expect: BUILD SUCCESS
java -jar target/u23621372-Moodally-J.jar &
```

Open <http://localhost:7474> and connect as `neo4j` / `prac5pass`.

**Terminal - confirm the graph is populated before starting**

```bash
podman exec neo4j-prac5 cypher-shell -u neo4j -p prac5pass -d COS326Prac5 \
  "MATCH (p:Person) WITH count(p) AS people
   MATCH ()-[f:FOLLOWS]->() WITH people, count(f) AS follows
   MATCH (po:Post) RETURN people, follows, count(po) AS posts;"
```

Expect `8828, 4902, 3`. If the database is empty, restore it:

```bash
grep -v '^CREATE DATABASE' Task1Queries.txt > /tmp/restore.cypher
podman exec -i neo4j-prac5 cypher-shell -u neo4j -p prac5pass \
  -d COS326Prac5 < /tmp/restore.cypher
```

**Terminal - reset, only to demonstrate the import live**

```bash
podman exec neo4j-prac5 cypher-shell -u neo4j -p prac5pass -d system \
  "DROP DATABASE COS326Prac5 IF EXISTS WAIT; CREATE DATABASE COS326Prac5 WAIT;"
```

Skip this to demo against the already-populated database. If it is run, the
Task 1 queries below must be run afterwards or the database stays empty.

**Neo4j Browser - select the database**

```cypher
:use cos326prac5
```

**Neo4j Browser - Task 1**

Copy each query from `Task1Queries.txt` in order.

| Query | Expected |
| --- | --- |
| Constraint and index | must run before the FOLLOWS import |
| Q1.1a import persons | 8828 nodes created |
| Q1.1b import follows | 4902 relationships created |
| Q1.2a counts | people 8828, follows 4902 |
| Q1.2b sample of five | click the graph icon for the visualisation |

**Neo4j Browser - Task 2**

Run the parameter line at the top of the Task 2 section first.

```cypher
:param account => '@mileycyrus';
```

| Query | Expected |
| --- | --- |
| Q2.1a node labels | `Person`, `Post` |
| Q2.1b first 20 names | alphabetical |
| Q2.1c relationship types | `FOLLOWS`, `LIKED`, `POSTED`, `REPOSTED` |
| Q2.1d followers of the account | 30 rows |
| Q2.2a top 5 followers | @mileycyrus 30, @tommcfly 21, @ddlovato 17, @Kal_Penn 16, @Schofe 14 |
| Q2.2b top 5 following | @Hollywood_Trey 8, @loris_sl 6 |
| Q2.3a create 3 posts | P001, P002, P003 with POSTED relationships |
| Q2.3b REPOSTED and LIKED | nodes matched first, only relationships created |
| Q2.3c reposted posts | @mileycyrus / @tommcfly / #Security |

**Neo4j Browser - Task 3**

Run the parameter block at the top of the Task 3 section first.

```cypher
:param from    => '@_Brittany_';
:param to      => '@EmmsAngel';
:param account => '@mileycyrus';
```

| Query | Expected |
| --- | --- |
| Q3.1a reachable in 1-2 hops | 2 accounts at length 1, 4 at length 2 |
| Q3.1b path with nodes and relationships | Path found, 2 FOLLOWS relationships |
| Q3.1c shortest path | Path found, length 2 - click the graph icon |

Then show the no-path branch, which Q3.1b and Q3.1c must both handle:

```cypher
:param to => '@ Disney';
```

| Query | Expected |
| --- | --- |
| Q3.1b | No path found, empty lists |
| Q3.1c | No path found, pathLength null |
| Q3.2a posted and reposted flag | @mileycyrus TRUE |
| Q3.2b post totals | 3 total, 1 reposted, 1 liked |
| Q3.2c direct connections | 30 rows, all incoming; @mileycyrus follows nobody, so no outgoing row appears |

**Application - Task 4**

| Tab | Input | Expected |
| --- | --- | --- |
| Network Overview | click Refresh | People 8828, FOLLOWS 4902, then the top five accounts |
| Connection Investigation | `@_Brittany_` / `@EmmsAngel` | path via `@_EmilyYoung` |
| Connection Investigation | `@mileycyrus` / `@ Disney` | "No FOLLOWS path exists" |
| Connection Investigation | `@nonsense` / `@mileycyrus` | validation dialog, application stays up |
| Content Investigation | `@mileycyrus` | P001 Reposted Yes, P002 and P003 No |

Close the window to show the driver being shut down cleanly.

---

## 3. Database setup

Neo4j 5.26 Enterprise runs in a container. Enterprise is required because
`CREATE DATABASE` (Question 1.1) is not available in Community Edition.

```bash
podman run -d --name neo4j-prac5 \
  -p 7474:7474 -p 7687:7687 \
  -v "$PWD/import:/var/lib/neo4j/import:z" \
  -e NEO4J_AUTH=neo4j/prac5pass \
  -e NEO4J_ACCEPT_LICENSE_AGREEMENT=eval \
  docker.io/library/neo4j:5.26-enterprise
```

Connection settings used by the application (`db/Neo4jConnection.java`):

| Setting | Value |
| --- | --- |
| URI | `bolt://localhost:7687` |
| Username | `neo4j` |
| Password | `prac5pass` |
| Database | `COS326Prac5` |

Neo4j Browser is at <http://localhost:7474>. To open a shell:

```bash
podman exec -it neo4j-prac5 cypher-shell -u neo4j -p prac5pass
```

---

## 4. Importing the data

Create the database, against the `system` database:

```cypher
CREATE DATABASE COS326Prac5 IF NOT EXISTS;
```

Then run the Question 1.1 queries from `Task1Queries.txt` against
`COS326Prac5`. The constraint and index must be created before the FOLLOWS
import, otherwise each of the 4970 rows triggers a full scan.

`Task1Queries.txt` holds every query for Tasks 1 to 3 in order and runs top to
bottom against an empty `COS326Prac5` without edits. The Task 2 and Task 3
sections each begin with the `:param` lines they need, so the investigator
changes only those lines to query a different account.

Two quirks in the supplied data are worth knowing about:

- 60 account names appear in `follows.csv` but not in `persons.csv`. Those rows
  match no Person node and so create no relationship, which leaves 4902 FOLLOWS
  relationships out of 4970 rows. The import uses `MATCH` rather than `MERGE`
  on purpose, so that no account is invented that `persons.csv` does not list.
- 5 rows in `persons.csv` are quoted incorrectly, so their names import with
  stray quote characters around them. They are loaded as they appear in the
  supplied file rather than silently corrected. None of the five is referenced
  by `follows.csv`.

---

## 5. Running the application

```bash
mvn clean package
java -jar target/u23621372-Moodally-J.jar
```

The application verifies the connection before opening the window, so a server
that is not running is reported in a dialog rather than a stack trace.

Tabs map to the Question 4.3 functions:

| Tab | Question | Function |
| --- | --- | --- |
| Network Overview | 4.3a | total people and FOLLOWS relationships |
| Network Overview | 4.3b | five accounts with the most followers |
| Connection Investigation | 4.3c | shortest FOLLOWS path between two accounts |
| Content Investigation | 4.3d | posts for an account, with reposted status |

**Direction note.** The Task 3.1 queries follow the direction of FOLLOWS, as
Question 3.1 requires, and so report "no path" for most pairs in this sparse
graph. The application's Connection Investigation treats a follow as a plain
connection instead, because Question 4.3c asks for the shortest *available*
path between two accounts rather than a chain of follows. The difference is
deliberate and is explained in `Task1Queries.txt`.

---

## 6. Requirements

| Requirement | Notes |
| --- | --- |
| JDK 21 or later | built and tested on OpenJDK 25 |
| Maven 3.9 | downloads the Neo4j Java Driver 5.28.15 |
| Podman or Docker | to run the Neo4j 5.26 Enterprise server |

---

## 7. Troubleshooting

**`import/` is unreadable from the host.** The container entrypoint chowns its
import directory on startup. Under rootless Podman that makes `import/`
unreadable, because the container's neo4j user maps to a subordinate UID. To
take the directory back without stopping the server:

```bash
podman unshare chown -R 0:0 import
chmod 755 import && chmod 644 import/*.csv
```

Both the host and the container can read the files afterwards. If the server is
restarted and the import files are needed again, the chown happens once more
and the two commands can simply be repeated.

**The database is empty.** Most often this follows a `DROP DATABASE` reset that
was not followed by the Task 1 import. Restore it with the `grep` and
`cypher-shell` pair in section 2.
