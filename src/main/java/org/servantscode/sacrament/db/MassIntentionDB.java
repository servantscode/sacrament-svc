package org.servantscode.sacrament.db;

import org.servantscode.commons.db.ReportStreamingOutput;
import org.servantscode.commons.search.InsertBuilder;
import org.servantscode.commons.search.QueryBuilder;
import org.servantscode.commons.search.SearchParser;
import org.servantscode.commons.search.UpdateBuilder;
import org.servantscode.commons.security.OrganizationContext;
import org.servantscode.sacrament.MassIntention;
import org.servantscode.sacrament.MassIntention.IntentionType;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MassIntentionDB extends AbstractSacramentDB<MassIntention> {

    private static final Map<String, String> FIELD_MAP = new HashMap<>(8);
    static {
        FIELD_MAP.put("person.name", "personName");
        FIELD_MAP.put("person.id", "personId");
        FIELD_MAP.put("requester.name", "requesterName");
        FIELD_MAP.put("requester.id", "requesterId");
        FIELD_MAP.put("massTime", "e.start_time");
    }

    public MassIntentionDB() {
        super(MassIntention.class, "person.name", FIELD_MAP);
    }

    public int getCount(String search) {
        QueryBuilder query = count().from("mass_intentions i", "events e")
                .where("i.eventId = e.id").inOrg("i.org_id").inOrg("e.org_id");
        try (Connection conn = getConnection();
             PreparedStatement stmt = query.prepareStatement(conn);
             ResultSet rs = stmt.executeQuery() ){

            return rs.next()? rs.getInt(1): 0;
        } catch (SQLException e) {
            throw new RuntimeException("Could not get mass intentions: " + search, e);
        }
    }

    private QueryBuilder dataQuery() {
        return select("i.*", "e.start_time AS massTime").from("mass_intentions i", "events e")
                .where("i.eventId = e.id").inOrg("i.org_id").inOrg("e.org_id");
    }

    public StreamingOutput getReportReader(String search, final List<String> fields) {
        QueryBuilder query = dataQuery().search(searchParser.parse(search));

        return new ReportStreamingOutput(fields) {
            @Override
            public void write(OutputStream output) throws WebApplicationException {
                try ( Connection conn = getConnection();
                      PreparedStatement stmt = query.prepareStatement(conn);
                      ResultSet rs = stmt.executeQuery()) {

                    writeCsv(output, rs);
                } catch (SQLException | IOException e) {
                    throw new RuntimeException("Could not retrieve mass intentions '" + search + "'", e);
                }
            }
        };
    }

    public List<MassIntention> getMassIntentions(String search, String sort, int start, int count) {
        return get(dataQuery().search(searchParser.parse(search)).page(sort, start, count));
    }

    public MassIntention getMassIntention(int id) {
        return getOne(dataQuery().with("i.id", id));
    }

    public MassIntention createMassIntention(MassIntention intention) {
        InsertBuilder cmd = insertInto("mass_intentions")
                .value("eventId", intention.getEventId())
                .value("personName", intention.getPerson().getName())
                .value("personId", intention.getPerson().getId())
                .value("intentionType", intention.getIntentionType())
                .value("requesterName", intention.getRequester().getName())
                .value("requesterId", intention.getRequester().getId())
                .value("requesterPhone", intention.getRequesterPhone())
                .value("stipend", intention.getStipend())
                .value("org_id", OrganizationContext.orgId());
        intention.setId(createAndReturnKey(cmd));
        return intention;
    }

    public MassIntention updateMassIntention(MassIntention intention) {
        UpdateBuilder cmd = update("mass_intentions")
                .value("eventId", intention.getEventId())
                .value("personName", intention.getPerson().getName())
                .value("personId", intention.getPerson().getId())
                .value("intentionType", intention.getIntentionType())
                .value("requesterName", intention.getRequester().getName())
                .value("requesterId", intention.getRequester().getId())
                .value("requesterPhone", intention.getRequesterPhone())
                .value("stipend", intention.getStipend())
                .withId(intention.getId()).inOrg();
        if(!update(cmd))
            throw new RuntimeException("Could not update Mass intention: " + intention.getPerson().getName());

        return intention;
    }

    public boolean delete(MassIntention intention) {
        return delete(deleteFrom("mass_intentions").withId(intention.getId()).inOrg());
    }

    // ----- Private -----
    @Override
    protected MassIntention processRow(ResultSet rs) throws SQLException {
        MassIntention intention = new MassIntention();
        intention.setId(rs.getInt("id"));
        intention.setMassTime(convert(rs.getTimestamp("massTime")));
        intention.setEventId(rs.getInt("eventId"));
        intention.setPerson(getIdentity(rs, "personName", "personId"));
        intention.setIntentionType(IntentionType.valueOf(rs.getString("intentionType")));
        intention.setRequester(getIdentity(rs, "requesterName", "requesterId"));
        intention.setRequesterPhone(rs.getString("requesterPhone"));
        intention.setStipend(rs.getFloat("stipend"));
        return intention;
    }
}
