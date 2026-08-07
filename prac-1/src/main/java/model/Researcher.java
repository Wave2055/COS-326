package model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class Researcher {
    
    @Id
    private String researcherId;

    private String fullName;
    private String department;
    private String email;

    @OneToMany(mappedBy = "researcher", cascade = CascadeType.PERSIST, orphanRemoval = false)
    private List<Booking> bookings = new ArrayList<>();

    protected Researcher() {

    }

    public Researcher(String researcherId, String fullName, String department, String email){
        this.researcherId = researcherId;
        this.department = department;
        this.email = email;
        this.fullName = fullName;
    }
}
