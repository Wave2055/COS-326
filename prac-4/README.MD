# COS326 Practical 4 2026 - CyberShield SOC

u23621372 - Moodally J

MongoDB-based security event management prototype: reusable mongosh functions,
analysis queries, and a Java Swing application for SOC analysts.

This README is the demo script. Work through it top to bottom and every
question in the mark sheet gets shown in order.

---

## Files

| Path | Contents |
| --- | --- |
| `.mongoshrc.js` | Task 1 functions (Q1.2 - Q1.5) and Task 2 functions (Q2.1 - Q2.5) |
| `Prac4Queries.js` | Task 2 queries in plain shell form |
| `pom.xml`, `src/` | Task 3 Java application |
| `security_events.json` | Supplied dataset (32 events) |

---

## Before the demo

Run these once. Everything after this assumes they are done.

**1. MongoDB is running.**

```bash
systemctl status mongod        # should say: active (running)
sudo systemctl start mongod    # if it is not
```

**2. The functions are on the shell's path.**

`mongosh` reads `.mongoshrc.js` from your home directory, so link it there:

```bash
ln -s "$PWD/.mongoshrc.js" ~/.mongoshrc.js
```

Check it worked:

```bash
echo 'print(typeof insertSecurityEvents)' | mongosh --quiet
```

Expect `function`. If you get `undefined`, the link is missing or wrong.

> The rc file only loads in an **interactive** session. `mongosh --eval "..."`
> skips rc files entirely, so always demo from inside the shell, not with `--eval`.

**3. The Java application builds.**

```bash
mvn clean package
```

Expect `BUILD SUCCESS` and `target/u23621372-Moodally-J.jar`.

---

## Reset to a clean dataset

Do this immediately before Task 1, and **again before Task 2** (Task 1 changes
the data; the practical requires Task 2 to run on the original dataset):

```bash
mongoimport --db cybershield --collection securityEvents \
            --jsonArray --drop --file security_events.json
```

Expect `32 document(s) imported successfully`.

---

## Task 1 - shell functions [18 marks]

Reset the dataset, then open the shell:

```bash
mongosh cybershield
```

### Q1.1 - Database and collection

```javascript
show dbs
db.securityEvents.countDocuments()
db.securityEvents.findOne()
```

Shows `cybershield` exists, holds 32 documents, and the document structure
(including the `tags`, `affectedSystems` and `responseActions` arrays).

### Q1.2 - Insert security events

```javascript
insertSecurityEvents("cybershield", "securityEvents", 5)
```

> `5 security event document(s) were inserted into cybershield.securityEvents.`

Show the generated documents and their unique IDs:

```javascript
db.securityEvents.find({ eventId: { $gte: "EVT10033" } }, { _id: 0 })
db.securityEvents.countDocuments()      // 37
```

Then show the input is validated:

```javascript
insertSecurityEvents("cybershield", "securityEvents", 0)
```

> `The number of events to insert must be a positive whole number.`

### Q1.3 - Find security events

```javascript
findEventsByIP("cybershield", "securityEvents", "192.168.10.25")
```

> `1 security event(s) found for source IP 192.168.10.25:` followed by the full
> document.

Every source IP in the dataset is unique, so one hit is the expected result.
Now show the not-found path:

```javascript
findEventsByIP("cybershield", "securityEvents", "1.2.3.4")
```

> `No security events were found for source IP 1.2.3.4.`

### Q1.4 - Update event severity

```javascript
updateSeverity("cybershield", "securityEvents", "Brute Force Attack", "Critical")
```

> `3 security event document(s) of type "Brute Force Attack" were updated to severity Critical.`

**Expect to be asked why this says 3 when there are 4 brute-force events.**
There are four (`EVT10001`, `EVT10008`, `EVT10017`, `EVT10027`), but `EVT10017`
is already `Critical`, so MongoDB matched 4 and modified 3. The function
deliberately reports `modifiedCount` - the number of documents actually
changed. Show it:

```javascript
db.securityEvents.find({ eventType: "Brute Force Attack" }, { _id: 0, eventId: 1, severity: 1 })
```

Then the two guard paths:

```javascript
updateSeverity("cybershield", "securityEvents", "Nonexistent Type", "Critical")
updateSeverity("cybershield", "securityEvents", "Port Scan", "Urgent")
```

> `No security events of type "Nonexistent Type" were found.`
> `Severity must be one of: Low, Medium, High, Critical.`

### Q1.5 - Remove low-severity events

Count first so the number has something to be checked against:

```javascript
db.securityEvents.countDocuments({ severity: "Low" })
removeLowSeverityEvents("cybershield", "securityEvents")
db.securityEvents.countDocuments({ severity: "Low" })      // 0
```

The count will be **4 plus however many Low events Q1.2 generated** (severities
are random), so it is normally 4-9. Quote the number you just counted, not a
memorised one.

---

## Task 2 - queries and aggregation [17 marks]

**Reset the dataset first** - Task 1 inserted, updated and deleted documents:

```bash
mongoimport --db cybershield --collection securityEvents \
            --jsonArray --drop --file security_events.json
mongosh cybershield
```

Each question is wrapped as a function in `.mongoshrc.js`; the plain query form
is in `Prac4Queries.js`. Run the function, and have the file open to show the
query itself if asked.

| Run this | Expected result |
| --- | --- |
| `findHighAndCriticalEvents("cybershield", "securityEvents")` | 19 events (13 High + 6 Critical), newest timestamp first |
| `findBruteForceTaggedEvents("cybershield", "securityEvents")` | 4 events tagged `brute-force` |
| `findPendingResponseEvents("cybershield", "securityEvents")` | 21 events with a pending response action |
| `securityEventSummary("cybershield", "securityEvents")` | High 13, Medium 9, Critical 6, Low 4 |
| `searchEventDescriptions("cybershield", "securityEvents", "authentication")` | 5 matching events |

Two things worth pointing out as you go:

- **Q2.1** sorts on `timestamp`, which is stored as an ISO-8601 **string**.
  Sorting works because that format sorts identically as text and as a date.
- **Q2.5** creates the text index before searching. Show it exists:

  ```javascript
  db.securityEvents.getIndexes()
  ```

  The search matches `Authentication` with a capital A as well - text search is
  case-insensitive and matches word stems, which a plain `find` on the string
  would not do.

Also show the no-match path:

```javascript
searchEventDescriptions("cybershield", "securityEvents", "zzzznotathing")
```

---

## Task 3 - Java application [15 marks]

Leave the dataset in its freshly imported state and run:

```bash
java -jar target/u23621372-Moodally-J.jar
```

### Q3.1 - MongoDB connection

The window only opens once the connection has been verified with a ping, and
the status bar along the bottom shows `mongodb://localhost:27017 - database:
cybershield`. The table loads all 32 events on startup, which proves the
connection is live.

**To demo the failure path**, stop the database first, then launch:

```bash
sudo systemctl stop mongod
java -jar target/u23621372-Moodally-J.jar
```

An error dialog appears within about 5 seconds saying the connection could not
be established, and the application exits instead of opening a broken window.
Then start it again:

```bash
sudo systemctl start mongod
```

### Q3.2 - SOC analyst GUI

The three buttons down the left switch between the screens: **Search Events**,
**Update Event**, **Security Summary**.

### Q3.3 - Search security events

On **Search Events**, enter `192.168.10.25` and click *Search Events*.

One row is returned, with its tags and response actions shown in the last two
columns. Scroll right to show the full document is being read, not just a few
fields.

Then search `1.2.3.4` - a dialog reports that no events were found, and the
status line under the table repeats it.

*Show All Events* reloads all 32 from the database.

### Q3.4 - Update security event

On **Update Event**, enter `EVT10001`, pick `Critical`, click *Update Severity*.

> `Security event EVT10001 was updated to severity Critical.`

Prove it actually changed the database - go back to **Search Events**, search
`192.168.10.25`, and the severity column now reads `Critical`. Better still,
confirm it from the shell in a second terminal:

```javascript
db.securityEvents.find({ eventId: "EVT10001" }, { _id: 0, eventId: 1, severity: 1 })
```

Two more cases worth showing:

- Enter `EVT99999` - reports that no such event exists.
- Click *Update Severity* twice with the same value - the second time reports
  the event already has that severity, because MongoDB modified 0 documents.

### Q3.5 - Security summary

On **Security Summary**, click the *Security Summary* button.

| Severity | Number of events |
| --- | --- |
| High | 13 |
| Medium | 9 |
| Critical | 6 |
| Low | 4 |

If you did Q3.4 just before this, Critical will read 7 and High 12 - the
summary reflects the update you made, which is the point. The numbers come from
an aggregation pipeline (`$group` / `$project` / `$sort`) in
`dao/SecurityEventDAO.java`, not from anything stored in the application.

To make that unmissable, change some data from the shell while the application
is open, then click the button again:

```javascript
updateSeverity("cybershield", "securityEvents", "Port Scan", "Critical")
```

The Critical count goes up the moment you click.

---

## Questions you should be ready for

**Where do the functions live and how does the shell find them?**
`.mongoshrc.js` in the home directory, loaded automatically when `mongosh`
starts an interactive session.

**Why `$elemMatch` in Q2.3?** `responseActions` is an array of embedded
documents. `$elemMatch` requires a **single** array element to satisfy the
condition. With dot notation (`"responseActions.status": "Pending"`) the match
can be spread across different elements, which is not what was asked for.

**Why `$project` after `$group` in the aggregation?** `$group` always names its
key `_id`. The output has to be `severity` and `numberOfEvents`, so `$project`
renames `_id` to `severity` and drops `_id`.

**How does the Java app avoid hard-coding?** Every panel calls
`SecurityEventDAO`, which queries the collection each time. Nothing is cached -
changing the data in the shell and re-clicking a button changes what is shown.

**Why is the connection checked before the GUI opens?** `MongoConnection.connect()`
runs a `ping` command and throws if the server is unreachable, so the failure is
reported as a dialog rather than surfacing as a broken window or an error on the
first query.

---

## Troubleshooting

| Symptom | Fix |
| --- | --- |
| `typeof insertSecurityEvents` is `undefined` | `~/.mongoshrc.js` link missing, or you used `--eval` (rc files do not load with `--eval`) |
| `MongoNetworkError` / connection refused | `sudo systemctl start mongod` |
| Counts do not match this README | Re-run the `mongoimport` reset command |
| `mvn package` fails on a fresh machine | It needs internet once to download the MongoDB driver into `~/.m2` |

---

## Structure

```
src/main/java/
  Main.java                        application entry point, connection check
  db/MongoConnection.java          connection handling (Q3.1)
  dao/SecurityEventDAO.java        all queries, updates and the aggregation
  model/SecurityEvent.java         one security event document
  model/SeveritySummaryRow.java    one summary row
  ui/                              Swing interface (Q3.2 - Q3.5)
```
