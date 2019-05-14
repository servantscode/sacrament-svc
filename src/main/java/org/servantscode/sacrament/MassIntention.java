package org.servantscode.sacrament;

import java.time.ZonedDateTime;

public class MassIntention {
    public enum IntentionType {DECEASED, SPECIAL};

    private int id;
    private int eventId;
    private ZonedDateTime massTime;
    private Identity person;
    private IntentionType intentionType;
    private Identity requester;
    private String requesterPhone;

    public MassIntention() {}

    // ----- Accessors -----
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEventId() { return eventId; }
    public void setEventId(int eventId) { this.eventId = eventId; }

    public ZonedDateTime getMassTime() { return massTime; }
    public void setMassTime(ZonedDateTime massTime) { this.massTime = massTime; }

    public Identity getPerson() { return person; }
    public void setPerson(Identity person) { this.person = person; }

    public IntentionType getIntentionType() { return intentionType; }
    public void setIntentionType(IntentionType intentionType) { this.intentionType = intentionType; }

    public Identity getRequester() { return requester; }
    public void setRequester(Identity requester) { this.requester = requester; }

    public String getRequesterPhone() { return requesterPhone; }
    public void setRequesterPhone(String requesterPhone) { this.requesterPhone = requesterPhone; }
}
