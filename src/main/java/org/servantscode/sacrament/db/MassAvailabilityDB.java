package org.servantscode.sacrament.db;

import org.servantscode.commons.db.DBAccess;
import org.servantscode.commons.search.QueryBuilder;
import org.servantscode.commons.search.SearchParser;
import org.servantscode.sacrament.MassAvailability;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MassAvailabilityDB extends DBAccess {

    private SearchParser<MassAvailability> searchParser;

    private static final Map<String, String> FIELD_MAP = new HashMap<>(4);

    static {
        FIELD_MAP.put("massTime", "start_time");
    }

    public MassAvailabilityDB() {
        this.searchParser = new SearchParser<>(MassAvailability.class, "description", FIELD_MAP);
    }

    public int getCount(String search) {
        QueryBuilder query = count().from("events e")
                .where("start_time > now()")
                .where("start_time < now() + interval '1 year'")
                .search(searchParser.parse(search))
                .whereIdNotIn("id", select("eventId").from("mass_intentions"));
        try (Connection conn = getConnection();
             PreparedStatement stmt = query.prepareStatement(conn);
             ResultSet rs = stmt.executeQuery() ){

            return rs.next()? rs.getInt(1): 0;
        } catch (SQLException e) {
            throw new RuntimeException("Could not get available mass times: " + search, e);
        }
    }

    public List<MassAvailability> getAvailableMasses(String search, String sortField, int start, int count) {
        QueryBuilder query = select("id", "start_time AS massTime", "description").from("events e")
                .where("start_time > now()")
                .where("start_time < now() + interval '1 year'")
                .search(searchParser.parse(search))
                .whereIdNotIn("id", select("eventId").from("mass_intentions"))
                .sort(sortField).limit(count).offset(start);

        List<MassAvailability> results = new LinkedList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = query.prepareStatement(conn);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                MassAvailability avail = new MassAvailability();
                avail.setId(rs.getInt("id"));
                avail.setMassTime(convert(rs.getTimestamp("massTime")));
                avail.setDescription(rs.getString("description"));
                results.add(avail);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not retrieve available Mass times: " + search, e);
        }
        return results;
    }
}
