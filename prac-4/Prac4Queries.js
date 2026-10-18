// COS326 - Database Systems - Practical 4 2026
// u23621372 - Moodally J
//
// Task 2 - the MongoDB queries in their plain shell form.
// The same queries are wrapped as reusable functions in .mongoshrc.js
//
// Setup:
//   mongoimport --db cybershield --collection securityEvents \
//               --jsonArray --drop --file security_events.json
//   mongosh cybershield

// ---------------------------------------------------------------------------
// Question 2.1 - Find Events by Severity
// All High and Critical events, newest first.
// ---------------------------------------------------------------------------

db.securityEvents.find(
    { severity: { $in: ["High", "Critical"] } },
    { _id: 0, eventId: 1, timestamp: 1, sourceIP: 1, eventType: 1, severity: 1 }
).sort({ timestamp: -1 });

// ---------------------------------------------------------------------------
// Question 2.2 - Query an Array Field
// Events whose tags array contains "brute-force".
// ---------------------------------------------------------------------------

db.securityEvents.find(
    { tags: "brute-force" },
    { _id: 0, eventId: 1, eventType: 1, severity: 1, tags: 1 }
);

// ---------------------------------------------------------------------------
// Question 2.3 - Query Embedded Documents
// Events with at least one response action still marked Pending.
// ---------------------------------------------------------------------------

db.securityEvents.find(
    { responseActions: { $elemMatch: { status: "Pending" } } },
    { _id: 0, eventId: 1, eventType: 1, severity: 1, responseActions: 1 }
);

// ---------------------------------------------------------------------------
// Question 2.4 - Aggregation: Events by Severity
// Number of events at each severity level, highest count first.
// ---------------------------------------------------------------------------

db.securityEvents.aggregate([
    { $group: { _id: "$severity", numberOfEvents: { $sum: 1 } } },
    { $project: { _id: 0, severity: "$_id", numberOfEvents: 1 } },
    { $sort: { numberOfEvents: -1 } }
]);

// ---------------------------------------------------------------------------
// Question 2.5 - Text Search
// Text index on description, then search for "authentication".
// ---------------------------------------------------------------------------

db.securityEvents.createIndex({ description: "text" });

db.securityEvents.find(
    { $text: { $search: "authentication" } },
    { _id: 0, eventId: 1, eventType: 1, severity: 1, description: 1 }
);
