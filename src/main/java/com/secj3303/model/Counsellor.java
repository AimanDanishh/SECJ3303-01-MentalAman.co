package com.secj3303.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "COUNSELLOR")
public class Counsellor implements Serializable {

    @Id
    @Column(name = "counsellor_id", nullable = false, length = 50)
    private String id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 100)
    private String specialty;

    @Column(length = 50, unique = true)
    private String code;

    @OneToMany(mappedBy = "counsellor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CounsellingSession> sessions = new ArrayList<>();

    // Set of titles to ignore when generating initials
    private static final Set<String> IGNORED_TITLES = new HashSet<>();
    
    static {
        IGNORED_TITLES.add("DR");
        IGNORED_TITLES.add("DR.");
        IGNORED_TITLES.add("MR");
        IGNORED_TITLES.add("MR.");
        IGNORED_TITLES.add("MRS");
        IGNORED_TITLES.add("MRS.");
        IGNORED_TITLES.add("MS");
        IGNORED_TITLES.add("MS.");
        IGNORED_TITLES.add("PROF");
        IGNORED_TITLES.add("PROF.");
        IGNORED_TITLES.add("PROFESSOR");
    }

    public Counsellor() {}

    // Constructor with explicit ID
    public Counsellor(String id, String name, String specialty, String code) {
        this.id = id;
        this.name = name;
        this.specialty = specialty;
        this.code = code;
    }

    // Constructor that generates ID from name
    public Counsellor(String name, String specialty, String code) {
        this.name = name;
        this.specialty = specialty;
        this.code = code;
        this.id = generateIdFromName(name);
    }

    // Method to generate ID from name while ignoring titles
    private String generateIdFromName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        
        String[] nameParts = name.trim().split("\\s+");
        StringBuilder initial = new StringBuilder();
        
        for (String part : nameParts) {
            if (!part.isEmpty()) {
                String normalizedPart = part.toUpperCase().replace(".", "");
                // Skip if this normalized part is a title to ignore
                if (IGNORED_TITLES.contains(normalizedPart)) {
                    continue;
                }
                // Take the first character of valid name parts
                initial.append(part.charAt(0));
            }
        }
        
        String generatedId = initial.toString().toUpperCase();
        
        // Ensure we have at least one character
        if (generatedId.isEmpty()) {
            throw new IllegalArgumentException("Could not generate ID from name. Name may contain only titles.");
        }
        
        return generatedId;
    }

    // Static utility method that can be used elsewhere
    public static String extractInitialsFromName(String name) {
        return new Counsellor().generateIdFromName(name);
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { 
        this.name = name;
    }

    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public List<CounsellingSession> getSessions() { return sessions; }
    public void setSessions(List<CounsellingSession> sessions) { this.sessions = sessions; }

    // Helper methods for bidirectional relationship
    public void addSession(CounsellingSession session) {
        sessions.add(session);
        session.setCounsellor(this);
    }

    public void removeSession(CounsellingSession session) {
        sessions.remove(session);
        session.setCounsellor(null);
    }

    @Override
    public String toString() {
        return "Counsellor{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", specialty='" + specialty + '\'' +
                ", code='" + code + '\'' +
                '}';
    }
}