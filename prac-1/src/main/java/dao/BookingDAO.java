package dao;

import model.Booking;
import model.BookingStatus;
import model.Equipment;
import model.Researcher;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import java.time.LocalDate;
import java.util.List;

public class BookingDAO {

    private final EntityManager em = JPAUtil.geEntityManager();

    /**
     * Creates a booking after checking that the equipment is not already
     * booked for an overlapping time slot on the same date.
     */
    public Booking create(Booking booking) {
        if (hasOverlap(booking.getEquipment(), booking.getBookingDate(),
                booking.getStartTime(), booking.getEndTime(), null)) {
            throw new IllegalStateException(
                    "Cannot create booking: equipment is already booked for an overlapping time slot");
        }
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(booking);
            tx.commit();
            return booking;
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }

    public Booking findById(long bookingId) {
        return em.find(Booking.class, bookingId);
    }

    public List<Booking> findAll() {
        TypedQuery<Booking> query = em.createQuery(
                "SELECT b FROM Booking b ORDER BY b.bookingDate, b.startTime", Booking.class);
        return query.getResultList();
    }

    public List<Booking> findByResearcher(Researcher researcher) {
        TypedQuery<Booking> query = em.createQuery(
                "SELECT b FROM Booking b WHERE b.researcher = :researcher ORDER BY b.bookingDate, b.startTime",
                Booking.class);
        query.setParameter("researcher", researcher);
        return query.getResultList();
    }

    public List<Booking> findByEquipment(Equipment equipment) {
        TypedQuery<Booking> query = em.createQuery(
                "SELECT b FROM Booking b WHERE b.equipment = :equipment ORDER BY b.bookingDate, b.startTime",
                Booking.class);
        query.setParameter("equipment", equipment);
        return query.getResultList();
    }

    public List<Booking> findByStatus(BookingStatus status) {
        TypedQuery<Booking> query = em.createQuery(
                "SELECT b FROM Booking b WHERE b.status = :status ORDER BY b.bookingDate, b.startTime",
                Booking.class);
        query.setParameter("status", status);
        return query.getResultList();
    }

    public List<Booking> findByDateRange(LocalDate from, LocalDate to) {
        TypedQuery<Booking> query = em.createQuery(
                "SELECT b FROM Booking b WHERE b.bookingDate BETWEEN :from AND :to "
                        + "ORDER BY b.bookingDate, b.startTime",
                Booking.class);
        query.setParameter("from", from);
        query.setParameter("to", to);
        return query.getResultList();
    }

    /**
     * Re-validates the overlap rule (ignoring the booking's own id) before
     * persisting changes, so edits can't introduce a clash either.
     */
    public Booking update(Booking booking) {
        if (hasOverlap(booking.getEquipment(), booking.getBookingDate(),
                booking.getStartTime(), booking.getEndTime(), booking.getBookingId())) {
            throw new IllegalStateException(
                    "Cannot update booking: equipment is already booked for an overlapping time slot");
        }
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Booking merged = em.merge(booking);
            tx.commit();
            return merged;
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }

    public boolean delete(long bookingId) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Booking booking = em.find(Booking.class, bookingId);
            if (booking == null) {
                tx.rollback();
                return false;
            }
            em.remove(booking);
            tx.commit();
            return true;
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }

    /**
     * Checks whether the given equipment already has an ACTIVE booking on
     * bookingDate whose time range overlaps [startTime, endTime).
     * excludeBookingId lets update() ignore the booking being edited.
     */
    private boolean hasOverlap(Equipment equipment, LocalDate bookingDate,
                                java.time.LocalTime startTime, java.time.LocalTime endTime,
                                Long excludeBookingId) {
        if (equipment == null || bookingDate == null || startTime == null || endTime == null) {
            return false;
        }
        String jpql = "SELECT b FROM Booking b WHERE b.equipment = :equipment "
                + "AND b.bookingDate = :bookingDate AND b.status = :status "
                + "AND b.startTime < :endTime AND b.endTime > :startTime";
        if (excludeBookingId != null) {
            jpql += " AND b.bookingId <> :excludeId";
        }
        TypedQuery<Booking> query = em.createQuery(jpql, Booking.class);
        query.setParameter("equipment", equipment);
        query.setParameter("bookingDate", bookingDate);
        query.setParameter("status", BookingStatus.ACTIVE);
        query.setParameter("startTime", startTime);
        query.setParameter("endTime", endTime);
        if (excludeBookingId != null) {
            query.setParameter("excludeId", excludeBookingId);
        }
        return !query.getResultList().isEmpty();
    }
}