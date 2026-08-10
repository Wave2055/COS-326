package dao;

import model.Booking;
import model.BookingStatus;
import model.Equipment;
import model.EquipmentStatus;
import model.Researcher;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class BookingDAO {

    private static final int MAX_ACTIVE_BOOKINGS_PER_RESEARCHER = 3;

    private final EntityManager em = JPAUtil.geEntityManager();

    public Booking create(Booking booking) {
        validate(booking, null);
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

    public Booking update(Booking booking) {
        validate(booking, booking.getBookingId());
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

    public List<Researcher> findResearchersWithMostBookings() {
        TypedQuery<Object[]> countQuery = em.createQuery(
                "SELECT b.researcher, COUNT(b) FROM Booking b GROUP BY b.researcher", Object[].class);
        List<Object[]> results = countQuery.getResultList();
        long max = 0;
        for (Object[] row : results) {
            long count = (Long) row[1];
            if (count > max) max = count;
        }
        List<Researcher> topResearchers = new java.util.ArrayList<>();
        for (Object[] row : results) {
            if ((Long) row[1] == max) {
                topResearchers.add((Researcher) row[0]);
            }
        }
        return topResearchers;
    }

    // ---- Business rule validation ----

    private void validate(Booking booking, Long excludeBookingId) {
        Equipment equipment = booking.getEquipment();
        Researcher researcher = booking.getResearcher();
        LocalDate date = booking.getBookingDate();
        LocalTime start = booking.getStartTime();
        LocalTime end = booking.getEndTime();

        if (equipment == null || researcher == null) {
            throw new IllegalArgumentException("A booking must reference an existing researcher and equipment");
        }
        if (date == null || start == null || end == null) {
            throw new IllegalArgumentException("A booking must have a date, start time and end time");
        }
        // valid start/end time
        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("Start time must be before end time");
        }
        // today or future date only
        if (date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Bookings may only be created for the current date or a future date");
        }
        // equipment must not be out of service
        if (equipment.getStatus() == EquipmentStatus.OUT_OF_SERVICE) {
            throw new IllegalStateException("Equipment marked as Out of Service may not be booked");
        }
        // max 3 active bookings per researcher
        if (booking.getStatus() == BookingStatus.ACTIVE) {
            long activeCount = countActiveBookings(researcher, excludeBookingId);
            if (activeCount >= MAX_ACTIVE_BOOKINGS_PER_RESEARCHER) {
                throw new IllegalStateException(
                        "Researcher " + researcher.getFullName()
                                + " already has the maximum of " + MAX_ACTIVE_BOOKINGS_PER_RESEARCHER
                                + " active bookings");
            }
        }
        // no conflicting (overlapping) bookings for the same equipment
        if (hasOverlap(equipment, date, start, end, excludeBookingId)) {
            throw new IllegalStateException("Equipment is already booked for an overlapping time slot on this date");
        }
        // researcher may not book the same equipment twice on the same date
        if (hasSameResearcherEquipmentDate(researcher, equipment, date, excludeBookingId)) {
            throw new IllegalStateException(
                    "This researcher already has a booking for this equipment on this date");
        }
    }

    private long countActiveBookings(Researcher researcher, Long excludeBookingId) {
        String jpql = "SELECT COUNT(b) FROM Booking b WHERE b.researcher = :researcher AND b.status = :status";
        if (excludeBookingId != null) {
            jpql += " AND b.bookingId <> :excludeId";
        }
        TypedQuery<Long> query = em.createQuery(jpql, Long.class);
        query.setParameter("researcher", researcher);
        query.setParameter("status", BookingStatus.ACTIVE);
        if (excludeBookingId != null) {
            query.setParameter("excludeId", excludeBookingId);
        }
        return query.getSingleResult();
    }

    private boolean hasOverlap(Equipment equipment, LocalDate bookingDate,
                                LocalTime startTime, LocalTime endTime, Long excludeBookingId) {
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

    private boolean hasSameResearcherEquipmentDate(Researcher researcher, Equipment equipment,
                                                     LocalDate bookingDate, Long excludeBookingId) {
        String jpql = "SELECT b FROM Booking b WHERE b.researcher = :researcher "
                + "AND b.equipment = :equipment AND b.bookingDate = :bookingDate "
                + "AND b.status = :status";
        if (excludeBookingId != null) {
            jpql += " AND b.bookingId <> :excludeId";
        }
        TypedQuery<Booking> query = em.createQuery(jpql, Booking.class);
        query.setParameter("researcher", researcher);
        query.setParameter("equipment", equipment);
        query.setParameter("bookingDate", bookingDate);
        query.setParameter("status", BookingStatus.ACTIVE);
        if (excludeBookingId != null) {
            query.setParameter("excludeId", excludeBookingId);
        }
        return !query.getResultList().isEmpty();
    }
}