COS326 - Database Systems - Practical 5 2026
u23621372 - Moodally J

Social network investigation: a Neo4j graph database built from the supplied
CSV datasets, plus a Java Swing application for investigators.

Section 2 is the demo script. Work through it top to bottom and every question
in the mark sheet is shown in order. The sections after it explain each step.

=====================================================================
1. FILES
=====================================================================

  Task1Queries.txt   All Cypher queries for Tasks 1 - 3
  pom.xml, src/      Task 4 Java application
  persons.csv        Supplied dataset (8828 accounts)
  follows.csv        Supplied dataset (4970 follow relationships)
  import/            Copies of the two CSVs, mounted into the Neo4j
                     import directory

=====================================================================
2. DEMO COMMAND LIST (IN ORDER)
=====================================================================

Every command for the demo, top to bottom. Expected results are given so each
step can be checked as it runs.

--- Terminal: start the server and the application -------------------

  cd ~/Documents/COS-326/pracs/prac-5
  podman start neo4j-prac5                   # skip if already Up
  podman ps --filter name=neo4j-prac5        # expect: Up ...
  mvn clean package                          # expect: BUILD SUCCESS
  java -jar target/u23621372-Moodally-J.jar &

  Open http://localhost:7474 and connect as neo4j / prac5pass.

--- Terminal: reset, only to demonstrate the import live --------------

  podman exec neo4j-prac5 cypher-shell -u neo4j -p prac5pass -d system \
    "DROP DATABASE COS326Prac5 IF EXISTS WAIT; CREATE DATABASE COS326Prac5 WAIT;"

  Skip this to demo against the already-populated database.

--- Neo4j Browser: select the database --------------------------------

  :use cos326prac5

--- Neo4j Browser: Task 1 ---------------------------------------------

  Copy each query from Task1Queries.txt in order.

  Constraint and index   must run before the FOLLOWS import
  Q1.1a  import persons  expect 8828 nodes created
  Q1.1b  import follows  expect 4902 relationships created
  Q1.2a  counts          expect people 8828, follows 4902
  Q1.2b  sample of five  click the graph icon for the visualisation

--- Neo4j Browser: Task 2 ---------------------------------------------

  Run the :param line at the top of the Task 2 section first.

  :param account => '@mileycyrus';

  Q2.1a  node labels        expect Person, Post
  Q2.1b  first 20 names     alphabetical
  Q2.1c  relationship types expect FOLLOWS, LIKED, POSTED, REPOSTED
  Q2.1d  followers of       expect 30 rows
  Q2.2a  top 5 followers    expect @mileycyrus 30, @tommcfly 21, @ddlovato 17,
                            @Kal_Penn 16, @Schofe 14
  Q2.2b  top 5 following    expect @Hollywood_Trey 8, @loris_sl 6
  Q2.3a  create 3 posts     P001, P002, P003 with POSTED relationships
  Q2.3b  REPOSTED, LIKED    nodes matched first, only relationships created
  Q2.3c  reposted posts     expect @mileycyrus / @tommcfly / #Security

--- Neo4j Browser: Task 3 ---------------------------------------------

  Run the :param block at the top of the Task 3 section first.

  :param from    => '@_Brittany_';
  :param to      => '@EmmsAngel';
  :param account => '@mileycyrus';

  Q3.1a  reachable in 1-2 hops  expect 2 at length 1, 4 at length 2
  Q3.1b  path with nodes+rels   expect Path found, 2 FOLLOWS relationships
  Q3.1c  shortest path          expect Path found, length 2
                                click the graph icon for the visualisation

  Then show the no-path branch, which Q3.1b and Q3.1c must both handle:

  :param to => '@ Disney';

  Q3.1b  expect No path found, empty lists
  Q3.1c  expect No path found, pathLength null

  Q3.2a  posted + reposted flag  expect @mileycyrus TRUE
  Q3.2b  post totals             expect 3 total, 1 reposted, 1 liked
  Q3.2c  direct connections      expect 30 rows, all incoming; @mileycyrus
                                 follows nobody, so no outgoing row appears

--- Application: Task 4 -----------------------------------------------

  Network Overview tab
    click Refresh              expect People 8828, FOLLOWS 4902
                               then the top five accounts

  Connection Investigation tab
    @_Brittany_ / @EmmsAngel   expect path via @_EmilyYoung
    @mileycyrus / @ Disney     expect "No FOLLOWS path exists"
    @nonsense / @mileycyrus    expect a validation dialog, application stays up

  Content Investigation tab
    @mileycyrus                expect P001 Reposted Yes, P002 and P003 No

  Close the window to show the driver being shut down cleanly.

=====================================================================
3. DATABASE SETUP
=====================================================================

Neo4j 5.26 Enterprise runs in a container. Enterprise is required because
CREATE DATABASE (Question 1.1) is not available in Community Edition.

Start the server:

  podman run -d --name neo4j-prac5 \
    -p 7474:7474 -p 7687:7687 \
    -v "$PWD/import:/var/lib/neo4j/import:z" \
    -e NEO4J_AUTH=neo4j/prac5pass \
    -e NEO4J_ACCEPT_LICENSE_AGREEMENT=eval \
    docker.io/library/neo4j:5.26-enterprise

Connection settings used by the application (db/Neo4jConnection.java):

  URI       bolt://localhost:7687
  Username  neo4j
  Password  prac5pass
  Database  COS326Prac5

Neo4j Browser is available at http://localhost:7474 for the screenshots.

Open a shell:

  podman exec -it neo4j-prac5 cypher-shell -u neo4j -p prac5pass

=====================================================================
4. IMPORTING THE DATA
=====================================================================

Create the database (against the system database):

  CREATE DATABASE COS326Prac5 IF NOT EXISTS;

Then run the Question 1.1 queries from Task1Queries.txt against COS326Prac5.
The constraint and index must be created before the FOLLOWS import, otherwise
each of the 4970 rows triggers a full scan.

Task1Queries.txt holds every query for Tasks 1 to 3 in order and runs top to
bottom against an empty COS326Prac5 without edits. The Task 2 and Task 3
sections each begin with the :param lines they need, so the investigator
changes only those lines to query a different account.

Two quirks in the supplied data are worth knowing about:

  - 60 account names appear in follows.csv but not in persons.csv. Those rows
    match no Person node and so create no relationship, which leaves 4902
    FOLLOWS relationships out of 4970 rows. The import uses MATCH rather than
    MERGE on purpose, so that no account is invented that persons.csv does
    not list.

  - 5 rows in persons.csv are quoted incorrectly, so their names import with
    stray quote characters around them. They are loaded as they appear in the
    supplied file rather than silently corrected. None of the five is
    referenced by follows.csv.

=====================================================================
5. RUNNING THE APPLICATION
=====================================================================

  mvn clean package
  java -jar target/u23621372-Moodally-J.jar

The application verifies the connection before opening the window, so a
server that is not running is reported in a dialog rather than a stack trace.

Direction note: the Task 3.1 queries follow the direction of FOLLOWS, as
Question 3.1 requires, and so report "no path" for most pairs in this sparse
graph. The application's Connection Investigation treats a follow as a plain
connection instead, because Question 4.3c asks for the shortest available
path between two accounts rather than a chain of follows. The difference is
deliberate and is explained in Task1Queries.txt.

Tabs map to the Question 4.3 functions:

  Network Overview          4.3a total people and FOLLOWS relationships
                            4.3b five accounts with the most followers
  Connection Investigation  4.3c shortest FOLLOWS path between two accounts
                                 (undirected - see the note below)
  Content Investigation     4.3d posts for an account, with reposted status

=====================================================================
6. REQUIREMENTS
=====================================================================

  JDK 21 or later        (built and tested on OpenJDK 25)
  Maven 3.9              (downloads the Neo4j Java Driver 5.28.15)
  Podman or Docker       (to run the Neo4j 5.26 Enterprise server)

=====================================================================
7. TROUBLESHOOTING
=====================================================================

The container entrypoint chowns its import directory on startup. Under
rootless Podman that makes import/ unreadable from the host, because the
container's neo4j user maps to a subordinate UID. To take the directory
back without stopping the server:

  podman unshare chown -R 0:0 import
  chmod 755 import && chmod 644 import/*.csv

Both the host and the container can read the files afterwards.

If the server is restarted and the import files are needed again, the chown
happens once more and the two commands above can simply be repeated.
