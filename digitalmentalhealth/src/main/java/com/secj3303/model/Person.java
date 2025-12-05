package com.secj3303.model;

import java.text.DecimalFormat;
import java.time.Year;

public class Person {
    private Integer id;
    private String name;
    private Integer yob;
    private Integer age;
    private Double weight;
    private Double height;
    private Double bmi;
    private String category;
    
    // Constructors
    public Person() {}
    
    public Person(String name, Integer yob, Double weight, Double height) {
        this.name = name;
        this.yob = yob;
        this.weight = weight;
        this.height = height;
        calculateAgeAndBMI(); // Auto-calculate
    }
    
    // Calculate age from year of birth and BMI from height/weight
    public void calculateAgeAndBMI() {
        // Calculate age
        if (yob != null) {
            this.age = Year.now().getValue() - yob;
        }
        
        // Calculate BMI
        if (height != null && height > 0 && weight != null) {
            double rawBmi = weight / (height * height);
            DecimalFormat df = new DecimalFormat("#.#");
            this.bmi = Double.valueOf(df.format(rawBmi));   // Round to 1 decimal place
            determineCategory();
        }
    }
    
    // Determine BMI category
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
    
    // Getters and Setters - with auto-calculation
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public Integer getYob() { return yob; }
    public void setYob(Integer yob) { 
        this.yob = yob; 
        calculateAgeAndBMI();
    }
    
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    
    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { 
        this.weight = weight; 
        calculateAgeAndBMI();
    }
    
    public Double getHeight() { return height; }
    public void setHeight(Double height) { 
        this.height = height; 
        calculateAgeAndBMI();
    }
    
    public Double getBmi() { return bmi; }
    public void setBmi(Double bmi) { 
        this.bmi = bmi; 
        determineCategory();
    }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    @Override
    public String toString() {
        return "Person [id=" + id + ", name=" + name + ", yob=" + yob + 
               ", age=" + age + ", weight=" + weight + ", height=" + height + 
               ", bmi=" + bmi + ", category=" + category + "]";
    }
}