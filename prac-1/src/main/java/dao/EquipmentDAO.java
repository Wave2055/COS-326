package dao;

import model.Equipment;
import model.EquipmentStatus;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import java.util.List;

public class EquipmentDAO {

    private final EntityManager em = JPAUtil.geEntityManager();

    public Equipment create(Equipment equipment) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(equipment);
            tx.commit();
            return equipment;
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }

    public Equipment findById(long equipmentId) {
        return em.find(Equipment.class, equipmentId);
    }

    public List<Equipment> findAll() {
        TypedQuery<Equipment> query = em.createQuery(
                "SELECT e FROM Equipment e ORDER BY e.equipmentName", Equipment.class);
        return query.getResultList();
    }

    public List<Equipment> findByStatus(EquipmentStatus status) {
        TypedQuery<Equipment> query = em.createQuery(
                "SELECT e FROM Equipment e WHERE e.status = :status ORDER BY e.equipmentName",
                Equipment.class);
        query.setParameter("status", status);
        return query.getResultList();
    }

    public List<Equipment> findByCategory(String category) {
        TypedQuery<Equipment> query = em.createQuery(
                "SELECT e FROM Equipment e WHERE e.equipmentCategory = :category ORDER BY e.equipmentName",
                Equipment.class);
        query.setParameter("category", category);
        return query.getResultList();
    }

    public List<Equipment> searchByName(String namePart) {
        TypedQuery<Equipment> query = em.createQuery(
                "SELECT e FROM Equipment e WHERE LOWER(e.equipmentName) LIKE LOWER(:namePart) ORDER BY e.equipmentName",
                Equipment.class);
        query.setParameter("namePart", "%" + namePart + "%");
        return query.getResultList();
    }

    /** Spec (c)(viii): total replacement cost of all equipment currently stored. */
    public double getTotalReplacementCost() {
        TypedQuery<Double> query = em.createQuery(
                "SELECT SUM(e.replacementCost) FROM Equipment e", Double.class);
        Double total = query.getSingleResult();
        return total != null ? total : 0.0;
    }

    /** Spec (d)(3): equipment that has never been booked. */
    public List<Equipment> findNeverBooked() {
        // ObjectDB's JPQL doesn't accept "IS EMPTY" on this mapped-by collection,
        // so find equipment that HAS bookings and subtract it in Java instead.
        TypedQuery<Equipment> bookedQuery = em.createQuery(
                "SELECT DISTINCT b.equipment FROM Booking b", Equipment.class);
        List<Equipment> bookedEquipment = bookedQuery.getResultList();

        List<Equipment> neverBooked = findAll();
        neverBooked.removeAll(bookedEquipment);
        return neverBooked;
    }

    public Equipment update(Equipment equipment) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Equipment merged = em.merge(equipment);
            tx.commit();
            return merged;
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }

    public boolean delete(long equipmentId) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Equipment equipment = em.find(Equipment.class, equipmentId);
            if (equipment == null) {
                tx.rollback();
                return false;
            }
            if (equipment.getBookings() != null && !equipment.getBookings().isEmpty()) {
                // Business rule: equipment with existing bookings cannot be removed.
                tx.rollback();
                throw new IllegalStateException(
                        "Cannot delete equipment " + equipment.getEquipmentName()
                                + ": equipment has existing bookings");
            }
            em.remove(equipment);
            tx.commit();
            return true;
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }
}