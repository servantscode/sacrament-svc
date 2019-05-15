package org.servantscode.sacrament.db;

import org.servantscode.commons.db.ReportStreamingOutput;
import org.servantscode.commons.search.QueryBuilder;
import org.servantscode.commons.search.SearchParser;
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

import static org.servantscode.commons.StringUtils.isSet;

public class MassIntentionDB extends AbstractSacramentDB {

    private static final Map<String, String> FIELD_MAP = new HashMap<>(8);
    static {
        FIELD_MAP.put("person.name", "personName");
        FIELD_MAP.put("person.id", "personId");
        FIELD_MAP.put("requester.name", "requesterName");
        FIELD_MAP.put("requester.id", "requesterId");
    }

    private final SearchParser<MassIntention> searchParser;

    public MassIntentionDB() {
        searchParser = new SearchParser<>(MassIntention.class, "person.name", FIELD_MAP);
    }

    public int getCount(String search) {
        QueryBuilder query = count().from("mass_intentions")
                .search(searchParser.parse(search));
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
                .where("i.eventId = e.id");
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

    public List<MassIntention> getMassIntentions(String search, String sortField, int start, int count) {
        QueryBuilder query = dataQuery().search(searchParser.parse(search))
                .sort(sortField).limit(count).offset(start);
        try ( Connection conn = getConnection();
              PreparedStatement stmt = query.prepareStatement(conn)
        ) {

            return processResults(stmt);
        } catch (SQLException e) {
            throw new RuntimeException("Could not get mass intentions: " + search, e);
        }
    }

    public MassIntention getMassIntention(int id) {
        QueryBuilder query = dataQuery().where("i.id = ?", id);
        try(Connection conn = getConnection();
            PreparedStatement stmt = query.prepareStatement(conn)) {

            List<MassIntention> results = processResults(stmt);

            return results.isEmpty()? null: results.get(0);
        } catch (SQLException e) {
            throw new RuntimeException("Could not retrieve intention: " + id, e);
        }
    }

    public void createMassIntention(MassIntention intention) {
        String sql = "INSERT INTO mass_intentions (eventId, personName, personId, intentionType, requesterName, requesterId, requesterPhone) values (?,?,?,?,?,?,?)";

        try(Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, intention.getEventId());
            setIdentity(intention.getPerson(), stmt, 2, 3);
            stmt.setString(4, intention.getIntentionType().toString());
            setIdentity(intention.getRequester(), stmt, 5, 6);
            stmt.setString(7, intention.getRequesterPhone());

            if(stmt.executeUpdate() == 0)
                throw new RuntimeException("Could not create mass intention: " + intention.getPerson().getName());

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next())
                    intention.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not create mass intention: " + intention.getPerson().getName(), e);
        }
    }

    public void updateMassIntention(MassIntention intention) {
        String sql = "UPDATE mass_intentions SET eventId=?, " +
                "personName=?, personId=?, intentionType=?, " +
                "requesterName=?, requesterId=?, requesterPhone=? " +
                "WHERE id=?";
        try(Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {


            stmt.setInt(1, intention.getEventId());
            setIdentity(intention.getPerson(), stmt, 2, 3);
            stmt.setString(4, intention.getIntentionType().toString());
            setIdentity(intention.getRequester(), stmt, 5, 6);
            stmt.setString(7, intention.getRequesterPhone());
            stmt.setInt(8, intention.getId());

            if(stmt.executeUpdate() == 0)
                throw new RuntimeException("Could not update Mass intention: " + intention.getPerson().getName());
        } catch (SQLException e) {
            throw new RuntimeException("Could not update Mass intention: " + intention.getPerson().getName(), e);
        }
    }

    public boolean delete(MassIntention intention) {
        try ( Connection conn = getConnection();
              PreparedStatement stmt = conn.prepareStatement("DELETE FROM mass_intentions WHERE id=?")
        ){
            stmt.setInt(1, intention.getId());

            return stmt.executeUpdate() != 0;
        } catch (SQLException e) {
            throw new RuntimeException("Could not delete intentional record: " + intention.getPerson().getName(), e);
        }
    }

    // ----- Private -----
    private List<MassIntention> processResults(PreparedStatement stmt) throws SQLException {
        try(ResultSet rs = stmt.executeQuery()) {
            List<MassIntention> results = new LinkedList<>();
            while(rs.next()) {
                MassIntention b = new MassIntention();
                b.setId(rs.getInt("id"));
                b.setMassTime(convert(rs.getTimestamp("massTime")));
                b.setEventId(rs.getInt("eventId"));
                b.setPerson(getIdentity(rs, "personName", "personId"));
                b.setIntentionType(IntentionType.valueOf(rs.getString("intentionType")));
                b.setRequester(getIdentity(rs, "requesterName", "requesterId"));
                b.setRequesterPhone(rs.getString("requesterPhone"));
                results.add(b);
            }
            return results;
        }
    }

    private String optionalWhereClause(String search) {
        String selectors = searchParser.parse(search).getDBQueryString();
        return isSet(selectors)? " WHERE " + selectors: "";
    }
}
