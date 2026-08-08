import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.EntityManager;
import model.Researcher;


import java.util.HashMap;
import java.util.Map;

public class Main {

    public static void main (String [] args){
        String dbPath = System.getProperty("user.dir") + "/practical1.odb";

        Map<String, String> overrides = new HashMap<>();
        overrides.put("jakarta.persistence.jdbc.url", dbPath);

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("practical1PU", overrides);
        
        testPersistRetrieve(emf);

        emf.close();

        System.out.println("Opening database: " + dbPath);
    
    }

    private static void testPersistRetrieve(EntityManagerFactory emf) {
    System.out.println("=== PERSIST / RETRIEVE TEST ===\n");

    EntityManager em = emf.createEntityManager();
    try {
        // ---- Persist ----
        em.getTransaction().begin();

        Researcher researcher = new Researcher();
        researcher.setFullName("Dr. Alice");
        researcher.setDepartment("Computer Science");
        researcher.setEmail("alice@up.ac.za");
        em.persist(researcher);

        em.getTransaction().commit();

        // ---- Retrieve ----
        em.clear(); // clear persistence context to force a fresh read

        var found = em.createQuery("SELECT r FROM Researcher r WHERE r.fullName = 'Dr. Alice'", Researcher.class)
                      .getSingleResult();

        System.out.println("Retrieved researcher:");
        System.out.println("  ID: " + found.getResearcherId());
        System.out.println("  Full Name: " + found.getFullName());
        System.out.println("  Department: " + found.getDepartment());
        System.out.println("  Email: " + found.getEmail());

    } catch (Exception e) {
        System.err.println("Test failed: " + e.getMessage());
        e.printStackTrace();
    } finally {
        em.close();
    }
}
}
