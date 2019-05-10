package org.servantscode.sacrament.db;

import org.servantscode.commons.db.ReportStreamingOutput;
import org.servantscode.commons.search.SearchParser;
import org.servantscode.sacrament.MassIntention;
import org.servantscode.sacrament.MassIntention.IntentionType;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;

import static java.lang.String.format;
import static org.servantscode.commons.StringUtils.isSet;

public class MassIntentionDB extends AbstractSacramentDB {

    public int getCount(String search) {
        String sql = format("Select count(1) from mass_intentions%s", optionalWhereClause(search));
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery() ){

            return rs.next()? rs.getInt(1): 0;
        } catch (SQLException e) {
            throw new RuntimeException("Could not get mass intentions: " + search, e);
        }
    }

    public StreamingOutput getReportReader(String search, final List<String> fields) {
        final String sql = format("SELECT * FROM mass_intentions%s", optionalWhereClause(search));

        return new ReportStreamingOutput(fields) {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                try ( Connection conn = getConnection();
                      PreparedStatement stmt = conn.prepareStatement(sql);
                      ResultSet rs = stmt.executeQuery()) {

                    writeCsv(output, rs);
                } catch (SQLException | IOException e) {
                    throw new RuntimeException("Could not retrieve mass intentions '" + search + "'", e);
                }
            }
        };
    }

    public List<MassIntention> getMassIntentions(String search, String sortField, int start, int count) {
        String sql = format("SELECT * FROM mass_intentions%s ORDER BY %s LIMIT ? OFFSET ?", optionalWhereClause(search), sortField);
        try ( Connection conn = getConnection();
              PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, count);
            stmt.setInt(2, start);

            return processResults(stmt);
        } catch (SQLException e) {
            throw new RuntimeException("Could not get mass intentions: " + search, e);
        }
    }

    private String optionalWhereClause(String search) {
        String selectors = new SearchParser(MassIntention.class, "person_name").parse(search).getDBQueryString();
        return isSet(selectors)? " WHERE " + selectors: "";

    }

    public MassIntention getMassIntention(int id) {
        try(Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM mass_intentions WHERE id=?")) {

            stmt.setInt(1, id);

            List<MassIntention> results = processResults(stmt);

            return results.isEmpty()? null: results.get(0);
        } catch (SQLException e) {
            throw new RuntimeException("Could not retrieve intention: " + id, e);
        }
    }

    public void createMassIntention(MassIntention intention) {
        String sql = "INSERT INTO mass_intentions (event_id, person_name, person_id, intention_type, requester_name, requester_id, requester_phone) values (?,?,?,?,?,?,?)";

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
        String sql = "UPDATE mass_intentions SET event_id=?, " +
                "person_name=?, person_id=?, intention_type=?, " +
                "requester_name=?, requester_id=?, requester_phone=? " +
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
                b.setEventId(rs.getInt("event_id"));
                b.setPerson(getIdentity(rs, "person_name", "person_id"));
                b.setIntentionType(IntentionType.valueOf(rs.getString("intention_type")));
                b.setRequester(getIdentity(rs, "requester_name", "requester_id"));
                b.setRequesterPhone(rs.getString("requester_phone"));
                results.add(b);
            }
            return results;
        }
    }
}
