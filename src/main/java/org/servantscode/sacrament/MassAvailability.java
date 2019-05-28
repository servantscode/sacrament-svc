package org.servantscode.sacrament;

import java.time.ZonedDateTime;

public class MassAvailability {
    private int id;
    private ZonedDateTime massTime;
    private String title;

    // ----- Private -----
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public ZonedDateTime getMassTime() { return massTime; }
    public void setMassTime(ZonedDateTime massTime) { this.massTime = massTime; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
}
