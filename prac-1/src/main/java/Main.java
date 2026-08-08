import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import model.Researcher;

import java.util.HashMap;
import java.util.Map;

public class Main {

    public static void main (String [] args){
        String dbPath = System.getProperty("user.dir") + "/practical1.odb";

        Map<String, String> overrides = new HashMap<>();
        overrides.put("jakarta.persistence.jdbc.url", dbPath);

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("practical1PU", overrides);
    
    }

private static void testModels(EntityManagerFactory emf) {

    // ----- 1. Inspect the Metamodel (prints all entities and their fields) -----
    System.out.println("=== Metamodel inspection ===");
    var metamodel = emf.getMetamodel();
    for (var entity : metamodel.getEntities()) {
        System.out.println("Entity: " + entity.getName());
        for (var attr : entity.getAttributes()) {
            System.out.println("  - " + attr.getName() + " : " 
                + attr.getJavaType().getSimpleName()
                + " (PersistentType: " + attr.getPersistentAttributeType() + ")");
        }
    }

    // ----- 2. Perform a simple persist-and-retrieve test -----
    // This validates that the database can actually store and load objects.
    var em = emf.createEntityManager();
    try {
        em.getTransaction().begin();

        // Create a sample Researcher
        Researcher researcher = new Researcher();
        researcher.setFullName("Dr. Alice");
        em.persist(researcher);

        // Flush to force writing to disk (ObjectDB will generate the schema now)
        em.flush();

        em.getTransaction().commit();
    } catch (Exception e) {
        em.getTransaction().rollback();
        throw e;
    } finally {
        em.close();
    }

    // Now read it back
    em = emf.createEntityManager();
    try {
        var found = em.createQuery("SELECT r FROM Researcher r WHERE r.name = 'Dr. Alice'", Researcher.class)
                      .getSingleResult();
        System.out.println("\n=== Retrieve test ===");
        System.out.println("Found researcher: " + found.getFullName());
        System.out.println("ID: " + found.getResearcherId()); // if you have an @Id field
    } finally {
        em.close();
    }
}
}
