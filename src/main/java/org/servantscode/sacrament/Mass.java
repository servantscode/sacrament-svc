package org.servantscode.sacrament;

import org.servantscode.commons.Identity;
import org.servantscode.sacrament.MassIntention;

public class Mass {
    private int id;
    private MassIntention intention;
    private Identity presider;

    // ----- Accessors -----
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public MassIntention getIntention() { return intention; }
    public void setIntention(MassIntention intention) { this.intention = intention; }

    public Identity getPresider() { return presider; }
    public void setPresider(Identity presider) { this.presider = presider; }
}
