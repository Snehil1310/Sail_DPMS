package com.sail.dpms.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@Table(name = "sail_units")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class SailUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String location;

    @Column(name = "short_code", unique = true)
    private String shortCode;

    private String description;

    @Column(name = "daily_capacity")
    private String dailyCapacity;

    public SailUnit() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getShortCode() {
        return shortCode;
    }

    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDailyCapacity() {
        return dailyCapacity;
    }

    public void setDailyCapacity(String dailyCapacity) {
        this.dailyCapacity = dailyCapacity;
    }
}
