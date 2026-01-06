package com.secj3303.dao;

import java.util.List;

import com.secj3303.model.Person;

public interface PersonDao {
    List<Person> findAll();          // R - Read all
    Person findById(int id);         // R - Read by ID
    Person findByEmail(String email);// R - Read by Email
    int insert(Person person);       // C - Create
    void update(Person person);      // U - Update
    int delete(int id);              // D - Delete
}