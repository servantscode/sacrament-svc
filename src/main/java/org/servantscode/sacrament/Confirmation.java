package org.servantscode.sacrament;

import java.time.LocalDate;
import java.util.List;

public class Confirmation {
    private int id;
    private Identity person;
    private Identity father;
    private Identity mother;
    private int baptismId;
    private LocalDate baptismDate;
    private String baptismLocation;
    private Identity sponsor;
    private LocalDate confirmationDate;
    private String confirmationLocation;
    private Identity minister;
    private List<String> notations;
    private String volume;
    private int page;
    private int entry;

    public Confirmation() {};

    // ----- Accessors -----
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Identity getPerson() { return person; }
    public void setPerson(Identity person) { this.person = person; }

    public Identity getFather() { return father; }
    public void setFather(Identity father) { this.father = father; }

    public Identity getMother() { return mother; }
    public void setMother(Identity mother) { this.mother = mother; }

    public int getBaptismId() { return baptismId; }
    public void setBaptismId(int baptismId) { this.baptismId = baptismId; }

    public LocalDate getBaptismDate() { return baptismDate; }
    public void setBaptismDate(LocalDate baptismDate) { this.baptismDate = baptismDate; }

    public String getBaptismLocation() { return baptismLocation; }
    public void setBaptismLocation(String baptismLocation) { this.baptismLocation = baptismLocation; }

    public Identity getSponsor() { return sponsor; }
    public void setSponsor(Identity sponsor) { this.sponsor = sponsor; }

    public LocalDate getConfirmationDate() { return confirmationDate; }
    public void setConfirmationDate(LocalDate confirmationDate) { this.confirmationDate = confirmationDate; }

    public String getConfirmationLocation() { return confirmationLocation; }
    public void setConfirmationLocation(String confirmationLocation) { this.confirmationLocation = confirmationLocation; }

    public Identity getMinister() { return minister; }
    public void setMinister(Identity minister) { this.minister = minister; }

    public List<String> getNotations() { return notations; }
    public void setNotations(List<String> notations) { this.notations = notations; }

    public String getVolume() { return volume; }
    public void setVolume(String volume) { this.volume = volume; }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public int getEntry() { return entry; }
    public void setEntry(int entry) { this.entry = entry; }
}
