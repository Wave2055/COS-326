# COS326 Practical 1 (2026) — Research Equipment Booking System

A Java Swing desktop application, backed by ObjectDB, for managing researchers,
laboratory equipment, and equipment bookings for the Faculty of Engineering and
the Built Environment.

## Tech Stack

- Java (JDK 17+ recommended)
- Swing (GUI)
- JPA (`javax.persistence`) with **ObjectDB** as the persistence provider
- Maven (build tool)

## Project Structure

```
practical1/
├── pom.xml
├── practical1.odb                 # ObjectDB database file (created on first run)
├── src/
│   └── main/
│       ├── java/
│       │   ├── Main.java              # App entry point — launches the Swing UI
│       │   ├── PersistenceSmokeTest.java  # Standalone DB connectivity test (optional, run manually)
│       │   ├── dao/
│       │   │   ├── JpaUtil.java        # EntityManagerFactory/EntityManager singleton
│       │   │   ├── ResearcherDao.java
│       │   │   ├── EquipmentDao.java
│       │   │   └── BookingDao.java
│       │   ├── model/
│       │   │   ├── Researcher.java
│       │   │   ├── Equipment.java
│       │   │   ├── EquipmentStatus.java
│       │   │   ├── Booking.java
│       │   │   └── BookingStatus.java
│       │   └── ui/
│       │       ├── MainFrame.java          # Side nav + CardLayout content area
│       │       ├── RegisterResearcherPanel.java
│       │       ├── RegisterEquipmentPanel.java
│       │       ├── CreateBookingPanel.java
│       │       ├── SearchResearcherPanel.java
│       │       ├── SearchEquipmentPanel.java
│       │       ├── ManageBookingsPanel.java
│       │       ├── ReportsPanel.java
│       │       ├── BookingTableModel.java
│       │       ├── EquipmentTableModel.java
│       │       └── UiUtils.java
│       └── resources/
│           └── META-INF/
│               └── persistence.xml
```

## Prerequisites

- JDK 17 or later installed and on your `PATH`
- Maven installed and on your `PATH`
- The ObjectDB JPA provider dependency declared in `pom.xml`
- `persistence.xml` with a persistence unit named **`practical1PU`** (must match
  the constant in `dao/JpaUtil.java`)

## Setup

1. Clone or copy the project into its own folder (kept isolated from other
   COS326 practicals).
2. Confirm the ObjectDB dependency is present in `pom.xml`.
3. Confirm `src/main/resources/META-INF/persistence.xml` declares a
   persistence unit named `practical1PU`.
4. The database file `practical1.odb` is created automatically at
   `<project working directory>/practical1.odb` the first time the app runs —
   no manual setup needed.

## Build & Run

### Using Maven (command line)

Build the project:

```bash
mvn clean compile
```

Run the application (launches the Swing UI):

```bash
mvn exec:java -Dexec.mainClass="Main"
```

Run the standalone persistence smoke test (optional — sanity-checks the
ObjectDB connection by persisting and retrieving a sample Researcher,
Equipment, and Booking):

```bash
mvn exec:java -Dexec.mainClass="PersistenceSmokeTest"
```

> If `mvn exec:java` isn't recognised, make sure the `exec-maven-plugin` is
> configured in `pom.xml`:
>
> ```xml
> <plugin>
>     <groupId>org.codehaus.mojo</groupId>
>     <artifactId>exec-maven-plugin</artifactId>
>     <version>3.6.3</version>
>     <configuration>
>         <mainClass>Main</mainClass>
>     </configuration>
> </plugin>
> ```

### Using an IDE (NetBeans / IntelliJ / VS Code)

1. Open the project as a Maven project.
2. Set `Main.java` as the main class to run.
3. Clean and build the project (important after any dependency or class
   renames, to clear stale compiled classes).
4. Run the project.

### Packaging a runnable jar (optional)

```bash
mvn clean package
java -jar target/practical1-<version>.jar
```

(Adjust the jar name to whatever your `pom.xml` produces, and make sure it's
built with a manifest `Main-Class` or the shade/assembly plugin if you want a
standalone jar with dependencies bundled.)

## Features

- **Register Researcher** — capture and store researcher details
- **Register Equipment** — capture and store equipment details
- **Create Booking** — book equipment for a researcher (equipment/researcher
  selected from existing records only)
- **Search Researcher** — look up a researcher by ID, view their bookings
- **Search Equipment** — look up equipment by ID, view its booking history
- **Manage Bookings** — update a booking's status or cancel it
- **Reports** — total equipment replacement cost, available equipment,
  equipment never booked, researcher(s) with the most bookings, and bookings
  for a specified researcher

## Business Rules Enforced

1. Equipment marked **Out of Service** may not be booked.
2. A booking must have a valid start time before its end time.
3. A researcher may not have more than **three Active bookings** at once.
4. Duplicate Researcher IDs are not permitted.
5. Duplicate Equipment IDs are not permitted.
6. Duplicate Booking IDs are not permitted.
7. Conflicting (overlapping) bookings for the same equipment are not permitted.
8. A researcher may not book the same equipment twice on the same date.
9. Bookings may only be created for the current date or a future date.
