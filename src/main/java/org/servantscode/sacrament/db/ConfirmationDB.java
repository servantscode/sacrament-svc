package org.servantscode.sacrament.db;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.servantscode.commons.db.DBAccess;
import org.servantscode.commons.search.QueryBuilder;
import org.servantscode.commons.security.OrganizationContext;
import org.servantscode.sacrament.Confirmation;
import org.servantscode.sacrament.Identity;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.servantscode.commons.StringUtils.isEmpty;

public class ConfirmationDB extends AbstractSacramentDB {
//    private static final String[] FIELDS = new String[]{
//            "name", "person_id", "father_name", "father_id",
//            "mother_name", "mother_id", "baptism_id", "baptism_date",
//            "baptism_location", "sponsor_name", "sponsor_id",
//            "confirmation_date", "confirmation_location", "minister_name",
//            "minister_id", "notations", "volume", "page", "entry", "org_id"};

    public Confirmation getConfirmation(int id) {
        QueryBuilder query = selectAll().from("confirmations").withId(id).inOrg();
        try(Connection conn = getConnection();
            PreparedStatement stmt = query.prepareStatement(conn)) {

            return firstOrNull(processResults(stmt));
        } catch (SQLException e) {
            throw new RuntimeException("Could not retrieve confirmation: " + id, e);
        }
    }

    public Confirmation getConfirmationByPerson(int personId) {
        QueryBuilder query = selectAll().from("confirmations").where("person_id=?", personId).inOrg();
        try(Connection conn = getConnection();
            PreparedStatement stmt = query.prepareStatement(conn)) {

            return firstOrNull(processResults(stmt));
        } catch (SQLException e) {
            throw new RuntimeException("Could not retrieve confirmation for person: " + personId, e);
        }
    }

    public void createConfirmationRecord(Confirmation confirmation) {
        String sql = "INSERT INTO confirmations (name, person_id, father_name, father_id, mother_name, mother_id, baptism_id, baptism_date, baptism_location, sponsor_name, sponsor_id, confirmation_date, confirmation_location, minister_name, minister_id, notations, volume, page, entry, org_id) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
//        String sql = "INSERT INTO confirmations (" + String.join(", ", FIELDS) + ") values (" + String.join(",", Collections.nCopies(FIELDS.length, "?")) + ")";
        try(Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            setIdentity(confirmation.getPerson(), stmt, 1, 2);
            setIdentity(confirmation.getFather(), stmt, 3, 4);
            setIdentity(confirmation.getMother(), stmt, 5, 6);
            if(confirmation.getBaptismId() > 0)
                stmt.setInt(7, confirmation.getBaptismId());
            else
                stmt.setNull(7, Types.INTEGER);
            stmt.setDate(8, convert(confirmation.getBaptismDate()));
            stmt.setString(9, confirmation.getBaptismLocation());
            setIdentity(confirmation.getSponsor(), stmt, 10, 11);
            stmt.setDate(12, convert(confirmation.getConfirmationDate()));
            stmt.setString(13, confirmation.getConfirmationLocation());
            setIdentity(confirmation.getMinister(), stmt, 14, 15);
            stmt.setString(16, convertNotations(confirmation.getNotations()));
            stmt.setInt(17, OrganizationContext.orgId());

            if(stmt.executeUpdate() == 0)
                throw new RuntimeException("Could not create confirmation record: " + confirmation.getPerson().getName());

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next())
                    confirmation.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not create confirmation record: " + confirmation.getPerson().getName(), e);
        }
    }

    public void updateConfirmationRecord(Confirmation confirmation) {
        String sql = "UPDATE confirmations SET name=?, person_id=?, " +
                "father_name=?, father_id=?, " +
                "mother_name=?, mother_id=?, " +
                "baptism_id=?, baptism_date=?, baptism_location=?, " +
                "sponsor_name=?, sponsor_id=?,  " +
                "confirmation_date=?, confirmation_location=?, " +
                "minister_name=?, minister_id=?, " +
                "notations=?, volume=?, page=?, entry=? " +
                "WHERE id=? AND org_id=?";
//        String sql = "UPDATE confirmations SET " + String.join("=?, ", Arrays.copyOfRange(FIELDS, 0, FIELDS.length - 1)) + "=? WHERE id=? AND org_id=?";
        try(Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            setIdentity(confirmation.getPerson(), stmt, 1, 2);
            setIdentity(confirmation.getFather(), stmt, 3, 4);
            setIdentity(confirmation.getMother(), stmt, 5, 6);
            if(confirmation.getBaptismId() > 0)
                stmt.setInt(7, confirmation.getBaptismId());
            else
                stmt.setNull(7, Types.INTEGER);
            stmt.setDate(8, convert(confirmation.getBaptismDate()));
            stmt.setString(9, confirmation.getBaptismLocation());
            setIdentity(confirmation.getSponsor(), stmt, 10, 11);
            stmt.setDate(12, convert(confirmation.getConfirmationDate()));
            stmt.setString(13, confirmation.getConfirmationLocation());
            setIdentity(confirmation.getMinister(), stmt, 14, 15);
            stmt.setString(16, convertNotations(confirmation.getNotations()));
            stmt.setInt(17, confirmation.getId());
            stmt.setInt(18, OrganizationContext.orgId());

            if(stmt.executeUpdate() == 0)
                throw new RuntimeException("Could not update confirmation record: " + confirmation.getPerson().getName());
        } catch (SQLException e) {
            throw new RuntimeException("Could not update confirmation record: " + confirmation.getPerson().getName(), e);
        }
    }

    public boolean delete(Confirmation confirmation) {
        try ( Connection conn = getConnection();
              PreparedStatement stmt = conn.prepareStatement("DELETE FROM confirmations WHERE id=? AND org_id=?")
        ){
            stmt.setInt(1, confirmation.getId());
            stmt.setInt(2, OrganizationContext.orgId());

            return stmt.executeUpdate() != 0;
        } catch (SQLException e) {
            throw new RuntimeException("Could not delete confirmation record: " + confirmation.getPerson().getName(), e);
        }
    }

    // ----- Private -----
    private List<Confirmation> processResults(PreparedStatement stmt) throws SQLException {
        try(ResultSet rs = stmt.executeQuery()) {
            List<Confirmation> results = new LinkedList<>();
            while(rs.next()) {
                Confirmation b = new Confirmation();
                b.setId(rs.getInt("id"));
                b.setPerson(getIdentity(rs, "name", "person_id"));
                b.setFather(getIdentity(rs, "father_name", "father_id"));
                b.setMother(getIdentity(rs, "mother_name", "mother_id"));
                b.setBaptismId(rs.getInt("baptism_id"));
                b.setBaptismDate(convert(rs.getDate("baptism_date")));
                b.setBaptismLocation(rs.getString("baptism_location"));
                b.setSponsor(getIdentity(rs, "sponsor_name", "sponsor_id"));
                b.setConfirmationDate(convert(rs.getDate("confirmation_date")));
                b.setConfirmationLocation(rs.getString("confirmation_location"));
                b.setMinister(getIdentity(rs, "minister_name", "minister_id"));
                b.setNotations(convertNotations(rs.getString("notations")));
                results.add(b);
            }
            return results;
        }
    }
}
