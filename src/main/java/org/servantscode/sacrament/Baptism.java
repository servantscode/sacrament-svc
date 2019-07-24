package org.servantscode.sacrament;

import java.time.LocalDate;
import java.util.List;

public class Baptism {
    private int id;
    private Identity person;
    private String baptismLocation;
    private LocalDate baptismDate;
    private String birthLocation;
    private LocalDate birthDate;
    private Identity minister;
    private Identity father;
    private Identity mother;
    private Identity godfather;
    private Identity godmother;
    private Identity witness;
    private boolean conditional;
    private boolean reception;
    private List<String> notations;
    private String volume;
    private int page;
    private int entry;

    public Baptism() {}

    // ----- Accessors -----
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Identity getPerson() { return person; }
    public void setPerson(Identity person) { this.person = person; }

    public String getBaptismLocation() { return baptismLocation; }
    public void setBaptismLocation(String baptismLocation) { this.baptismLocation = baptismLocation; }

    public LocalDate getBaptismDate() { return baptismDate; }
    public void setBaptismDate(LocalDate baptismDate) { this.baptismDate = baptismDate; }

    public String getBirthLocation() { return birthLocation; }
    public void setBirthLocation(String birthLocation) { this.birthLocation = birthLocation; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    public Identity getMinister() { return minister; }
    public void setMinister(Identity minister) { this.minister = minister; }

    public Identity getFather() { return father; }
    public void setFather(Identity father) { this.father = father; }

    public Identity getMother() { return mother; }
    public void setMother(Identity mother) { this.mother = mother; }

    public Identity getGodfather() { return godfather; }
    public void setGodfather(Identity godfather) { this.godfather = godfather; }

    public Identity getGodmother() { return godmother; }
    public void setGodmother(Identity godmother) { this.godmother = godmother; }

    public Identity getWitness() { return witness; }
    public void setWitness(Identity witness) { this.witness = witness; }

    public boolean isConditional() { return conditional; }
    public void setConditional(boolean conditional) { this.conditional = conditional; }

    public boolean isReception() { return reception; }
    public void setReception(boolean reception) { this.reception = reception; }

    public List<String> getNotations() { return notations; }
    public void setNotations(List<String> notations) { this.notations = notations; }

    public String getVolume() { return volume; }
    public void setVolume(String volume) { this.volume = volume; }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public int getEntry() { return entry; }
    public void setEntry(int entry) { this.entry = entry; }
}
