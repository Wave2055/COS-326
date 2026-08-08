import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import model.Researcher;
import model.Equipment;
import model.Booking;
import model.BookingStatus;
import model.EquipmentStatus;


import java.time.*;
import java.util.HashMap;
import java.util.Map;

public class Main {

    public static void main(String[] args) {

        String dbPath = System.getProperty("user.dir") + "/practical1.odb";

        Map<String, String> overrides = new HashMap<>();
        overrides.put("javax.persistence.jdbc.url", "jdbc:objectdb:" + dbPath);

        EntityManagerFactory emf =
                Persistence.createEntityManagerFactory("practical1PU", overrides);

        testPersistRetrieve(emf);

        emf.close();

        System.out.println("Opening database: " + dbPath);
    }

   private static void testPersistRetrieve(EntityManagerFactory emf) {

    System.out.println("=== PERSIST / RETRIEVE TEST ===\n");

    EntityManager em = emf.createEntityManager();

    try {
        // =========================================================
        // CREATE OBJECTS
        // =========================================================

        Researcher researcher = new Researcher();
        researcher.setFullName("Dr. Alice");
        researcher.setDepartment("Computer Science");
        researcher.setEmail("alice@up.ac.za");

        Equipment equipment = new Equipment(
                "Dell Precision Laptop",
                "Computer",
                LocalDate.of(2025, 3, 15),
                35000.00,
                EquipmentStatus.AVAILABLE
        );

        Booking booking = new Booking(
                LocalDate.of(2026, 8, 10),
                LocalTime.of(10, 0),
                LocalTime.of(12, 0),
                "Testing machine learning algorithms",
                BookingStatus.ACTIVE,
                researcher,
                equipment
        );

        // =========================================================
        // PERSIST
        // =========================================================

        em.getTransaction().begin();

        em.persist(researcher);
        em.persist(equipment);
        em.persist(booking);

        em.getTransaction().commit();

        System.out.println("Objects persisted successfully.\n");

        // =========================================================
        // RETRIEVE
        // =========================================================

        em.clear();

        System.out.println("=== RESEARCHER ===");

        Researcher foundResearcher = em.createQuery(
                "SELECT r FROM Researcher r " +
                "WHERE r.email = 'alice@up.ac.za'",
                Researcher.class
        ).getSingleResult();

        System.out.println("ID:         " + foundResearcher.getResearcherId());
        System.out.println("Name:       " + foundResearcher.getFullName());
        System.out.println("Department: " + foundResearcher.getDepartment());
        System.out.println("Email:      " + foundResearcher.getEmail());


        System.out.println("\n=== EQUIPMENT ===");

        Equipment foundEquipment = em.createQuery(
                "SELECT e FROM Equipment e " +
                "WHERE e.equipmentName = 'Dell Precision Laptop'",
                Equipment.class
        ).getSingleResult();

        System.out.println("ID:            " + foundEquipment.getEquipmentId());
        System.out.println("Name:          " + foundEquipment.getEquipmentName());
        System.out.println("Category:      " + foundEquipment.getEquipmentCategory());
        System.out.println("Purchase Date: " + foundEquipment.getPurchaseDate());
        System.out.println("Replacement:   R" + foundEquipment.getReplacementCost());
        System.out.println("Status:        " + foundEquipment.getStatus());


        System.out.println("\n=== BOOKING ===");

        Booking foundBooking = em.createQuery(
                "SELECT b FROM Booking b " +
                "WHERE b.researchPurpose = " +
                "'Testing machine learning algorithms'",
                Booking.class
        ).getSingleResult();

        System.out.println("ID:              " + foundBooking.getBookingId());
        System.out.println("Date:            " + foundBooking.getBookingDate());
        System.out.println("Start Time:      " + foundBooking.getStartTime());
        System.out.println("End Time:        " + foundBooking.getEndTime());
        System.out.println("Research Purpose:" + foundBooking.getResearchPurpose());
        System.out.println("Status:          " + foundBooking.getStatus());


        System.out.println("\n=== RELATIONSHIPS ===");

        System.out.println(
                "Booking researcher: " +
                foundBooking.getResearcher().getFullName()
        );

        System.out.println(
                "Booking equipment: " +
                foundBooking.getEquipment().getEquipmentName()
        );


        // =========================================================
        // BASIC TESTS
        // =========================================================

        if (foundResearcher.getResearcherId() <= 0)
            throw new RuntimeException("Researcher ID was not generated.");

        if (foundEquipment.getEquipmentId() <= 0)
            throw new RuntimeException("Equipment ID was not generated.");

        if (foundBooking.getBookingId() <= 0)
            throw new RuntimeException("Booking ID was not generated.");

        if (!foundBooking.getResearcher()
                .getEmail()
                .equals("alice@up.ac.za")) {

            throw new RuntimeException(
                    "Booking -> Researcher relationship failed."
            );
        }

        if (!foundBooking.getEquipment()
                .getEquipmentName()
                .equals("Dell Precision Laptop")) {

            throw new RuntimeException(
                    "Booking -> Equipment relationship failed."
            );
        }

        System.out.println("\n=== ALL TESTS PASSED ===");

    } catch (Exception e) {

        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }

        System.err.println("\n=== TEST FAILED ===");
        System.err.println(e.getMessage());
        e.printStackTrace();

    } finally {
        em.close();
    }
}
}