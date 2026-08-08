package model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

@Entity
public class Booking {
    
    @Id
    private String bookingId;

    private LocalDate bookingDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String researchPurpose;

    @Enumerated
    private BookingStatus status;

    @ManyToOne
    @JoinColumn(name = "researcherId")
    private Researcher researcher;

    @ManyToMany
    @JoinColumn(name = "equipment_id")
    private Equipment equipment;

    public Booking(){

    }

    public Booking(
        String bookingId, 
        LocalDate bookingDate, 
        LocalTime startTime, 
        LocalTime endTime,
        String researchPurpose, 
        BookingStatus status,
        Researcher researcher, 
        Equipment equipment){
    
        this.bookingId = bookingId;
        this.bookingDate = bookingDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.researchPurpose = researchPurpose;
        this.status = status;
        this.researcher = researcher;
        this.equipment = equipment;
    }

        public String getBookingId() {
        return bookingId;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public String getResearchPurpose() {
        return researchPurpose;
    }

    public void setResearchPurpose(String researchPurpose) {
        this.researchPurpose = researchPurpose;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public Researcher getResearcher() {
        return researcher;
    }

    public void setResearcher(Researcher researcher) {
        this.researcher = researcher;
    }

    public Equipment getEquipment() {
        return equipment;
    }

    public void setEquipment(Equipment equipment) {
        this.equipment = equipment;
    }

        @Override
    public boolean equals (Object o){
        if (this == o) return true; // same address

        if (!(o instanceof Booking)) return false; //if not Booking not same

        Booking other = (Booking) o;// can only be Booking when we get here

        return Objects.equals(bookingId, other.bookingId);// if same  id
    }

    @Override
    public String toString() {
        return "Booking{" + bookingId + ", " + bookingDate + " " + startTime + "-" + endTime + ", " + status + "}";
    }
}
