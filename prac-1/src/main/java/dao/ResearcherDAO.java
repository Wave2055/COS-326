package dao;

import model.Researcher;

import javax.persistence.*;
import java.util.List;

public class ResearcherDAO{

    private final EntityManager em = JPAUtil.geEntityManager();

    public Researcher create(Researcher researcher)
    {
        EntityTransaction tx = em.getTransaction();
        try
        {
            tx.begin();

            em.persist(researcher);
            tx.commit();
            return researcher;
        }
        catch(RuntimeException e)
        {
            if (tx.isActive()) tx.rollback();

            throw e;
        }
    }

    public Researcher findById(long researcherId)
    {
        return em.find(Researcher.class, researcherId);
    }

    public List<Researcher> findByDepartment(String department)
    {
        TypedQuery<Researcher> query = em.createQuery(
            "SELECT r FROM RESEARCHER r WHERE r.department = :department ORDER BY r.fullName",
            Researcher.class
        );
        query.setParameter("department", department);

        return query.getResultList();
    }

    public Researcher findByEmail(String email)
    {
        TypedQuery<Researcher> query = em.createQuery(
            "SELECT r FROM RESEARCHER r WHERE r.email = :email ORDER BY r.fullName",
            Researcher.class
        );
        query.setParameter("email", email);
        List<Researcher> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    public List<Researcher> findByName(String nameSegment)
    {
        TypedQuery<Researcher> query = em.createQuery(
            "SELECT r FROM RESEARCHER r WHERE LOWER(r.fullName) LIKE LOWER(:nameSegment) ORDER BY r.fullName",
            Researcher.class
        );
        query.setParameter("nameSegment","%"+ nameSegment +"%");

        return query.getResultList();
    }

    public Researcher update (Researcher researcher)
    {
        EntityTransaction tx = em.getTransaction();

        try{
            tx.begin();
            Researcher merged = em.merge(researcher);
            tx.commit();
            return merged;
        }
        catch(RuntimeException e)
        {
            if (tx.isActive()) tx.rollback();

            throw e;
        }
    }

    public boolean delete(long researcherID)
    {
        EntityTransaction tx = em.getTransaction();

        try
        {
            tx.begin();
            Researcher researcher = em.find(Researcher.class, researcherID);
            
            if (researcher == null)
            {
                tx.rollback();
                return false;
            }

            if(researcher.getBookings() != null && !researcher.getBookings().isEmpty())
            {
                tx.rollback();
                throw new IllegalStateException("Cannot delete researcher " + researcher.getFullName() + " because they have existing bookings");
            }

            em.remove(researcher);
            tx.commit();
            return true;
        }
        catch(RuntimeException e)
        {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }

}