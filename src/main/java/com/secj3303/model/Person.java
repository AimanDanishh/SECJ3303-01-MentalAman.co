package com.secj3303.model;

import java.text.DecimalFormat;
import java.time.Year;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

@Entity
@Table(name = "person")
public class Person {

    // =====================
    // PRIMARY KEY
    // =====================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // =====================
    // AUTHENTICATION FIELDS
    // =====================
    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role; // ADMIN, MEMBER

    @Column(nullable = false)
    private boolean enabled = true;

    // =====================
    // PROFILE / BMI FIELDS
    // =====================
    @Column(nullable = false)
    private String name;

    private Integer yob;
    private Integer age;
    private Double weight;
    private Double height;
    private Double bmi;
    private String category;

    @Column(unique = true, length = 20)
    private String matrixId;

    // =====================
    // CONSTRUCTORS
    // =====================
    public Person() {}

    public Person(String email, String password, String role,
                  String name, Integer yob, Double weight, Double height) {
        this.email = email;
        this.password = password;
        this.role = role;
        this.name = name;
        this.yob = yob;
        this.weight = weight;
        this.height = height;
        calculateAgeAndBMI();
    }

    // =====================
    // JPA LIFECYCLE
    // =====================
    @PrePersist
    @PreUpdate
    public void prePersistAndUpdate() {
        calculateAgeAndBMI();
    }

    // =====================
    // BUSINESS LOGIC
    // =====================
    private void calculateAgeAndBMI() {

        if (yob != null) {
            this.age = Year.now().getValue() - yob;
        }

        if (height != null && height > 0 && weight != null) {
            double rawBmi = weight / (height * height);
            DecimalFormat df = new DecimalFormat("#.#");
            this.bmi = Double.valueOf(df.format(rawBmi));
            determineCategory();
        }
    }

    private void determineCategory() {
        if (bmi == null) {
            category = "Unknown";
        } else if (bmi < 18.5) {
            category = "Underweight";
        } else if (bmi < 25) {
            category = "Normal";
        } else if (bmi < 30) {
            category = "Overweight";
        } else {
            category = "Obese";
        }
    }

    // =====================
    // GETTERS & SETTERS
    // =====================
    public Integer getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getYob() {
        return yob;
    }

    public void setYob(Integer yob) {
        this.yob = yob;
        calculateAgeAndBMI();
    }

    public Integer getAge() {
        return age;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
        calculateAgeAndBMI();
    }

    public Double getHeight() {
        return height;
    }

    public void setHeight(Double height) {
        this.height = height;
        calculateAgeAndBMI();
    }

    public Double getBmi() {
        return bmi;
    }

    public String getCategory() {
        return category;
    }

    public String getMatrixId() {
        return matrixId;
    }

    public void setMatrixId(String matrixId) {
        this.matrixId = matrixId;
    }

    // =====================
    // DEBUG
    // =====================
    @Override
    public String toString() {
        return "Person [id=" + id +
               ", email=" + email +
               ", role=" + role +
               ", name=" + name +
               ", yob=" + yob +
               ", age=" + age +
               ", weight=" + weight +
               ", height=" + height +
               ", bmi=" + bmi +
               ", category=" + category + "]";
    }
}
