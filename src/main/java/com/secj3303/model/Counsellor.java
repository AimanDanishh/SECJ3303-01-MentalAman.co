package com.secj3303.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "COUNSELLOR")
public class Counsellor implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 100)
    private String specialty;

    @Column(length = 50, unique = true)
    private String code;

    @OneToMany(mappedBy = "counsellor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CounsellingSession> sessions = new ArrayList<>();

    public Counsellor() {}

    public Counsellor(String name, String specialty, String code) {
        this.name = name;
        this.specialty = specialty;
        this.code = code;
    }

    // Getters & Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

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
}