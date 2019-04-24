package org.servantscode.sacrament;

import java.time.LocalDate;
import java.util.List;

public class Marriage {
    private int id;
    private Identity groom;
    private Identity groomFather;
    private Identity groomMother;
    private int groomBaptismId;
    private LocalDate groomBaptismDate;
    private String groomBaptismLocation;
    private Identity bride;
    private Identity brideFather;
    private Identity brideMother;
    private int brideBaptismId;
    private LocalDate brideBaptismDate;
    private String brideBaptismLocation;
    private LocalDate marriageDate;
    private String marriageLocation;
    private Identity minister;
    private Identity witness1;
    private Identity witness2;
    private List<String> notations;

    public Marriage() {}

    // ----- Accessors -----
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Identity getGroom() { return groom; }
    public void setGroom(Identity groom) { this.groom = groom; }

    public Identity getGroomFather() { return groomFather; }
    public void setGroomFather(Identity groomFather) { this.groomFather = groomFather; }

    public Identity getGroomMother() { return groomMother; }
    public void setGroomMother(Identity groomMother) { this.groomMother = groomMother; }

    public int getGroomBaptismId() { return groomBaptismId; }
    public void setGroomBaptismId(int groomBaptismId) { this.groomBaptismId = groomBaptismId; }

    public LocalDate getGroomBaptismDate() { return groomBaptismDate; }
    public void setGroomBaptismDate(LocalDate groomBaptismDate) { this.groomBaptismDate = groomBaptismDate; }

    public String getGroomBaptismLocation() { return groomBaptismLocation; }
    public void setGroomBaptismLocation(String groomBaptismLocation) { this.groomBaptismLocation = groomBaptismLocation; }

    public Identity getBride() { return bride; }
    public void setBride(Identity bride) { this.bride = bride; }

    public Identity getBrideFather() { return brideFather; }
    public void setBrideFather(Identity brideFather) { this.brideFather = brideFather; }

    public Identity getBrideMother() { return brideMother; }
    public void setBrideMother(Identity brideMother) { this.brideMother = brideMother; }

    public int getBrideBaptismId() { return brideBaptismId; }
    public void setBrideBaptismId(int brideBaptismId) { this.brideBaptismId = brideBaptismId; }

    public LocalDate getBrideBaptismDate() { return brideBaptismDate; }
    public void setBrideBaptismDate(LocalDate brideBaptismDate) { this.brideBaptismDate = brideBaptismDate; }

    public String getBrideBaptismLocation() { return brideBaptismLocation; }
    public void setBrideBaptismLocation(String brideBaptismLocation) { this.brideBaptismLocation = brideBaptismLocation; }

    public LocalDate getMarriageDate() { return marriageDate; }
    public void setMarriageDate(LocalDate marriageDate) { this.marriageDate = marriageDate; }

    public String getMarriageLocation() { return marriageLocation; }
    public void setMarriageLocation(String marriageLocation) { this.marriageLocation = marriageLocation; }

    public Identity getMinister() { return minister; }
    public void setMinister(Identity minister) { this.minister = minister; }

    public Identity getWitness1() { return witness1; }
    public void setWitness1(Identity witness1) { this.witness1 = witness1; }

    public Identity getWitness2() { return witness2; }
    public void setWitness2(Identity witness2) { this.witness2 = witness2; }

    public List<String> getNotations() { return notations; }
    public void setNotations(List<String> notations) { this.notations = notations; }
}
