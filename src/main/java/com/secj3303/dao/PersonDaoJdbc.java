package com.secj3303.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.secj3303.model.Person;

@Repository
public class PersonDaoJdbc implements PersonDao {

    private final DataSource dataSource;

    @Autowired
    public PersonDaoJdbc(DataSource dataSource) {
        this.dataSource = dataSource;
        System.out.println("PersonDaoJdbc initialized with DataSource: " + dataSource);
    }

    @Override
    public List<Person> findAll() {
        System.out.println("DEBUG: findAll() called");
        List<Person> list = new ArrayList<>();
        String sql = "SELECT * FROM person";

        try (Connection conn = dataSource.getConnection()) {
            System.out.println("DEBUG: Connection obtained: " + conn);
            
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                System.out.println("DEBUG: PreparedStatement created");
                
                try (ResultSet rs = ps.executeQuery()) {
                    System.out.println("DEBUG: Query executed");
                    
                    while (rs.next()) {
                        Person p = new Person();
                        p.setId(rs.getInt("id"));
                        p.setName(rs.getString("name"));
                        
                        int yob = rs.getInt("yob");
                        p.setYob(rs.wasNull() ? null : yob);
                        
                        double weight = rs.getDouble("weight");
                        p.setWeight(rs.wasNull() ? null : weight);
                        
                        double height = rs.getDouble("height");
                        p.setHeight(rs.wasNull() ? null : height);
                        
                        double bmi = rs.getDouble("bmi");
                        p.setBmi(rs.wasNull() ? null : bmi);
                        
                        p.setCategory(rs.getString("category"));
                        
                        list.add(p);
                        System.out.println("DEBUG: Found person: " + p.getName());
                    }
                }
            }
            
            System.out.println("DEBUG: findAll() found " + list.size() + " records");
            return list;

        } catch (SQLException e) {
            System.err.println("ERROR in findAll(): " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error in findAll(): " + e.getMessage(), e);
        }
    }

    @Override
    public Person findById(int id) {
        System.out.println("DEBUG: findById(" + id + ") called");
        String sql = "SELECT * FROM person WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, id);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Person p = new Person();
                    p.setId(rs.getInt("id"));
                    p.setName(rs.getString("name"));
                    
                    int yob = rs.getInt("yob");
                    p.setYob(rs.wasNull() ? null : yob);
                    
                    double weight = rs.getDouble("weight");
                    p.setWeight(rs.wasNull() ? null : weight);
                    
                    double height = rs.getDouble("height");
                    p.setHeight(rs.wasNull() ? null : height);
                    
                    double bmi = rs.getDouble("bmi");
                    p.setBmi(rs.wasNull() ? null : bmi);
                    
                    p.setCategory(rs.getString("category"));
                    
                    System.out.println("DEBUG: findById() found: " + p.getName());
                    return p;
                }
            }
            System.out.println("DEBUG: findById() no record found for id=" + id);
            return null;
            
        } catch (SQLException e) {
            System.err.println("ERROR in findById(): " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error in findById(): " + e.getMessage(), e);
        }
    }

    @Override
    public int insert(Person person) {
        String sql = "INSERT INTO person (name, yob, age, weight, height, bmi, category) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setString(1, person.getName());
            
            if (person.getYob() != null) {
                ps.setInt(2, person.getYob());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            
            if (person.getAge() != null) {
                ps.setInt(3, person.getAge());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            
            if (person.getWeight() != null) {
                ps.setDouble(4, person.getWeight());
            } else {
                ps.setNull(4, Types.DOUBLE);
            }
            
            if (person.getHeight() != null) {
                ps.setDouble(5, person.getHeight());
            } else {
                ps.setNull(5, Types.DOUBLE);
            }
            
            if (person.getBmi() != null) {
                ps.setDouble(6, person.getBmi());
            } else {
                ps.setNull(6, Types.DOUBLE);
            }
            
            ps.setString(7, person.getCategory() != null ? person.getCategory() : "Unknown");

            int rows = ps.executeUpdate();
            System.out.println("insert(): inserted rows = " + rows);
            
            // Get generated ID
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    person.setId(generatedId);
                    System.out.println("Generated ID: " + generatedId);
                }
            }
            
            return rows;

        } catch (SQLException e) {
            System.err.println("ERROR in insert(): " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error in insert(): " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Person person) {
        String sql = "UPDATE person SET name=?, yob=?, age=?, weight=?, height=?, bmi=?, category=? WHERE id=?";

        try (Connection conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, person.getName());
            
            if (person.getYob() != null) {
                ps.setInt(2, person.getYob());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            
            if (person.getAge() != null) {
                ps.setInt(3, person.getAge());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            
            if (person.getWeight() != null) {
                ps.setDouble(4, person.getWeight());
            } else {
                ps.setNull(4, Types.DOUBLE);
            }
            
            if (person.getHeight() != null) {
                ps.setDouble(5, person.getHeight());
            } else {
                ps.setNull(5, Types.DOUBLE);
            }
            
            if (person.getBmi() != null) {
                ps.setDouble(6, person.getBmi());
            } else {
                ps.setNull(6, Types.DOUBLE);
            }
            
            ps.setString(7, person.getCategory() != null ? person.getCategory() : "Unknown");
            ps.setInt(8, person.getId());

            int rows = ps.executeUpdate();
            System.out.println("update(): updated rows = " + rows);

        } catch (SQLException e) {
            System.err.println("ERROR in update(): " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error in update(): " + e.getMessage(), e);
        }
    }

    @Override
    public int delete(int id) {
        System.out.println("DEBUG: delete() called for id: " + id);
        String sql = "DELETE FROM person WHERE id=?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            System.out.println("DEBUG: delete() deleted rows = " + rows);
            return rows;

        } catch (SQLException e) {
            System.err.println("ERROR in delete(): " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error in delete(): " + e.getMessage(), e);
        }
    }
}