package org.servantscode.sacrament;

import java.time.ZonedDateTime;

public class MassAvailability {
    private int id;
    private ZonedDateTime massTime;
    private String description;

    // ----- Private -----
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public ZonedDateTime getMassTime() { return massTime; }
    public void setMassTime(ZonedDateTime massTime) { this.massTime = massTime; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
