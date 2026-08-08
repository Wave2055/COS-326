package dao;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class JPAUtil {

    private static final String PERSISTENCE_UNIT_NAME = "practical1PU";

    private static EntityManagerFactory emf;
    private static EntityManager em;

    private JPAUtil(){

    }

    public static EntityManager geEntityManager() {
        if (em == null || !em.isOpen()) {
            if (emf == null || !emf.isOpen()) {
                emf = createFactory();
            }
            em = emf.createEntityManager();
        }
        return em;
    }
 
    private static EntityManagerFactory createFactory() {
        String dbPath = System.getProperty("user.dir") + "/practical1.odb";
        Map<String, String> overrides = new HashMap<>();
        overrides.put("javax.persistence.jdbc.url", "jdbc:objectdb:" + dbPath);
        return Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, overrides);
    }


    public static void close()
    {
        if (em != null && em.isOpen())
        {
            em.close();
        }

        if (emf != null && emf.isOpen())
        {
            emf.close();
        }
    }
    
}
