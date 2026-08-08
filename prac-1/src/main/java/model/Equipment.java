package model;


import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long equipmentId;

    private String equipmentName;
    private String equipmentCategory;
    private LocalDate purchaseDate;
    private double replacementCost;

    @Enumerated(EnumType.STRING)
    private EquipmentStatus status;

    @OneToMany(mappedBy = "equipment", cascade = CascadeType.PERSIST, orphanRemoval = false)
    private List<Booking> bookings = new ArrayList<>();

    public Equipment(){

    }

    public Equipment(String equipmentName, String equipmentCategory,
                      LocalDate purchaseDate, double replacementCost, EquipmentStatus status) {
        this.equipmentName = equipmentName;
        this.equipmentCategory = equipmentCategory;
        this.purchaseDate = purchaseDate;
        this.replacementCost = replacementCost;
        this.status = status;
    }

    public long getEquipmentId() {
        return equipmentId;
    }

    public String getEquipmentName() {
        return equipmentName;
    }

    public void setEquipmentName(String equipmentName) {
        this.equipmentName = equipmentName;
    }

    public String getEquipmentCategory() {
        return equipmentCategory;
    }

    public void setEquipmentCategory(String equipmentCategory) {
        this.equipmentCategory = equipmentCategory;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public double getReplacementCost() {
        return replacementCost;
    }

    public void setReplacementCost(double replacementCost) {
        this.replacementCost = replacementCost;
    }

    public EquipmentStatus getStatus() {
        return status;
    }

    public void setStatus(EquipmentStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals (Object o){
        if (this == o) return true; // same address

        if (!(o instanceof Equipment)) return false; //if not equipment not same

        Equipment other = (Equipment) o;// can only be equipment when we get here

        return Objects.equals(equipmentId, other.equipmentId);// if same id
    }

    @Override
    public String toString() {
        return "Equipment{" + equipmentId + ", " + equipmentName + ", " + status + "}";
    }
}
