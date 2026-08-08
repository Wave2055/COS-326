package dao;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class JPAUtil {

    private static final String PERSISTENCE_UNIT_NAME = "practical1PU";

    private static EntityManagerFactory emf;
    private static EntityManager em;

    private JPAUtil(){

    }

    public static EntityManager geEntityManager(){

        if (em == null || !em.isOpen()){
            if (emf == null || !emf.isOpen()){
                emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
            }
            em = emf.createEntityManager();
        }
        return em;
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
