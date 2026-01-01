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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    private Integer yob;
    private Integer age;
    private Double weight;
    private Double height;
    private Double bmi;
    private String category;

    // =====================
    // Constructors
    // =====================
    public Person() {}

    public Person(String name, Integer yob, Double weight, Double height) {
        this.name = name;
        this.yob = yob;
        this.weight = weight;
        this.height = height;
        calculateAgeAndBMI();
    }

    // =====================
    // JPA Lifecycle Hooks
    // =====================
    @PrePersist
    @PreUpdate
    public void prePersistAndUpdate() {
        calculateAgeAndBMI();
    }

    // =====================
    // Business Logic
    // =====================
    public void calculateAgeAndBMI() {

        // Calculate age
        if (yob != null) {
            this.age = Year.now().getValue() - yob;
        }

        // Calculate BMI
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
    // Getters & Setters
    // =====================
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    // Hibernate manages this automatically
    private void setAge(Integer age) {
        this.age = age;
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

    // Hibernate sets this via lifecycle hook
    private void setBmi(Double bmi) {
        this.bmi = bmi;
    }

    public String getCategory() {
        return category;
    }

    private void setCategory(String category) {
        this.category = category;
    }

    // =====================
    // Debugging
    // =====================
    @Override
    public String toString() {
        return "Person [id=" + id + ", name=" + name + ", yob=" + yob +
               ", age=" + age + ", weight=" + weight + ", height=" + height +
               ", bmi=" + bmi + ", category=" + category + "]";
    }
}
