package dao;

import db.Neo4jConnection;
import model.InfluentialAccount;
import model.NetworkOverview;
import model.PostSummary;
import org.neo4j.driver.Session;
import org.neo4j.driver.Values;

import java.util.ArrayList;
import java.util.List;

/**
 * Question 4.3 - the investigation functions.
 *
 * Every method runs its Cypher inside a read transaction (Question 4.4a) and
 * passes account names as parameters rather than building query strings
 * (Question 4.3 implementation requirements).
 */
public final class SocialNetworkDAO {

    /** Question 4.3a - total people and total FOLLOWS relationships. */
    public NetworkOverview getNetworkOverview() {
        try (Session session = Neo4jConnection.openSession()) {
            return session.executeRead(tx -> {
                long people = tx.run("MATCH (p:Person) RETURN count(p) AS total")
                        .single().get("total").asLong();
                long follows = tx.run("MATCH ()-[f:FOLLOWS]->() RETURN count(f) AS total")
                        .single().get("total").asLong();
                return new NetworkOverview(people, follows);
            });
        }
    }

    /** Question 4.3b - the five accounts with the most incoming FOLLOWS. */
    public List<InfluentialAccount> getInfluentialAccounts(int limit) {
        try (Session session = Neo4jConnection.openSession()) {
            return session.executeRead(tx -> tx.run("""
                            MATCH (p:Person)<-[:FOLLOWS]-(follower:Person)
                            RETURN p.name AS name, count(follower) AS followerCount
                            ORDER BY followerCount DESC
                            LIMIT $limit
                            """, Values.parameters("limit", limit))
                    .list(r -> new InfluentialAccount(
                            r.get("name").asString(),
                            r.get("followerCount").asLong())));
        }
    }

    /**
     * Question 4.3c - the shortest FOLLOWS path between two accounts.
     *
     * @return the account names along the path, or an empty list when the two
     *         accounts are not connected.
     */
    public List<String> findShortestPath(String fromName, String toName) {
        try (Session session = Neo4jConnection.openSession()) {
            return session.executeRead(tx -> {
                var result = tx.run("""
                        MATCH (a:Person {name: $from}), (b:Person {name: $to})
                        MATCH path = shortestPath((a)-[:FOLLOWS*]-(b))
                        RETURN [n IN nodes(path) | n.name] AS names
                        """, Values.parameters("from", fromName, "to", toName));

                if (!result.hasNext()) {
                    return new ArrayList<String>();
                }
                return new ArrayList<>(result.single().get("names").asList(v -> v.asString()));
            });
        }
    }

    /** Returns true when an account with this exact name exists. */
    public boolean accountExists(String name) {
        try (Session session = Neo4jConnection.openSession()) {
            return session.executeRead(tx -> tx.run(
                            "MATCH (p:Person {name: $name}) RETURN count(p) > 0 AS found",
                            Values.parameters("name", name))
                    .single().get("found").asBoolean());
        }
    }

    /** Question 4.3d - the posts of one account, with a reposted flag for each. */
    public List<PostSummary> getPostsForAccount(String accountName) {
        try (Session session = Neo4jConnection.openSession()) {
            return session.executeRead(tx -> tx.run("""
                            MATCH (p:Person {name: $name})-[:POSTED]->(post:Post)
                            RETURN post.id AS id,
                                   post.hashtag AS hashtag,
                                   post.message AS message,
                                   EXISTS { (post)<-[:REPOSTED]-() } AS reposted
                            ORDER BY post.id
                            """, Values.parameters("name", accountName))
                    .list(r -> new PostSummary(
                            r.get("id").asString(),
                            r.get("hashtag").asString(),
                            r.get("message").asString(),
                            r.get("reposted").asBoolean())));
        }
    }
}
