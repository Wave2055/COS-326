package model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class Researcher {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String researcherId;

    private String fullName;
    private String department;
    private String email;

    @OneToMany(mappedBy = "researcher", cascade = CascadeType.PERSIST, orphanRemoval = false)
    private List<Booking> bookings = new ArrayList<>();

    public Researcher() {

    }

    public Researcher(String fullName, String department, String email){
        this.department = department;
        this.email = email;
        this.fullName = fullName;
    }

        public String getResearcherId() {
        return researcherId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<Booking> getBookings() {
        return this.bookings;
    }

    @Override
    public String toString() {
        return "Researcher{" + researcherId + ", " + fullName + ", " + department + "}";
    }

    @Override
    public boolean equals (Object o){
        if (this == o) return true; // same address

        if (!(o instanceof Researcher)) return false; //if not researcher not same

        Researcher other = (Researcher) o;// can only be researcher when we get here

        return Objects.equals(researcherId, other.researcherId);// if same researcher id
    }
}
