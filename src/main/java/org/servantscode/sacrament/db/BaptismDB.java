package org.servantscode.sacrament.db;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.servantscode.commons.db.DBAccess;
import org.servantscode.commons.search.QueryBuilder;
import org.servantscode.commons.security.OrganizationContext;
import org.servantscode.sacrament.Baptism;
import org.servantscode.sacrament.Identity;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.servantscode.commons.StringUtils.isEmpty;

public class BaptismDB extends AbstractSacramentDB {
//    private static final String[] FIELDS = new String[]{
//            "name", "person_id", "father_name", "father_id",
//            "mother_name", "mother_id", "baptism_date",
//            "baptism_location", "birth_date", "birth_location",
//            "minister_name", "minister_id", "godfather_name",
//            "godfather_id", "godmother_name", "godmother_id",
//            "witness_name", "witness_id", "conditional", "reception",
//            "notations", "volume", "page", "entry", "org_id"};

    public Baptism getBaptism(int id) {
        QueryBuilder query = selectAll().from("baptisms").withId(id).inOrg();
        try(Connection conn = getConnection();
            PreparedStatement stmt = query.prepareStatement(conn)) {

            return firstOrNull(processResults(stmt));
        } catch (SQLException e) {
            throw new RuntimeException("Could not retrieve baptism: " + id, e);
        }
    }

    public Baptism getBaptismByPerson(int personId) {
        QueryBuilder query = selectAll().from("baptisms").where("person_id=?", personId).inOrg();
        try(Connection conn = getConnection();
            PreparedStatement stmt = query.prepareStatement(conn)) {

            return firstOrNull(processResults(stmt));
        } catch (SQLException e) {
            throw new RuntimeException("Could not retrieve baptism for person: " + personId, e);
        }
    }

    public void createBaptismalRecord(Baptism baptism) {
        String sql = "INSERT INTO baptisms (name, person_id, father_name, father_id, mother_name, mother_id, baptism_date, baptism_location, birth_date, birth_location, minister_name, minister_id, godfather_name, godfather_id, godmother_name, godmother_id, witness_name, witness_id, conditional, reception, notations, volume, page, entry, org_id) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
//        String sql = "INSERT INTO baptisms (" + String.join(", ", FIELDS) + ") values (" + String.join(",", Collections.nCopies(FIELDS.length, "?")) + ")";
        try(Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            setIdentity(baptism.getPerson(), stmt, 1, 2);
            setIdentity(baptism.getFather(), stmt, 3, 4);
            setIdentity(baptism.getMother(), stmt, 5, 6);
            stmt.setDate(7, convert(baptism.getBaptismDate()));
            stmt.setString(8, baptism.getBaptismLocation());
            stmt.setDate(9, convert(baptism.getBirthDate()));
            stmt.setString(10, baptism.getBirthLocation());
            setIdentity(baptism.getMinister(), stmt, 11, 12);
            setIdentity(baptism.getGodfather(), stmt, 13, 14);
            setIdentity(baptism.getGodmother(), stmt, 15, 16);
            setIdentity(baptism.getWitness(), stmt, 17, 18);
            stmt.setBoolean(19, baptism.isConditional());
            stmt.setBoolean(20, baptism.isReception());
            stmt.setString(21, convertNotations(baptism.getNotations()));
            stmt.setInt(22, OrganizationContext.orgId());

            if(stmt.executeUpdate() == 0)
                throw new RuntimeException("Could not create baptismal record: " + baptism.getPerson().getName());

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next())
                    baptism.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not create baptismal record: " + baptism.getPerson().getName(), e);
        }
    }

    public void updateBaptismalRecord(Baptism baptism) {
        String sql = "UPDATE baptisms SET name=?, person_id=?, " +
                "father_name=?, father_id=?, " +
                "mother_name=?, mother_id=?, " +
                "baptism_date=?, baptism_location=?, " +
                "birth_date=?, birth_location=?, " +
                "minister_name=?, minister_id=?, " +
                "godfather_name=?, godfather_id=?, " +
                "godmother_name=?, godmother_id=?, " +
                "witness_name=?, witness_id=?, " +
                "conditional=?, reception=?, notations=? WHERE id=? AND org_id=?";
//        String sql = "UPDATE baptisms SET " + String.join("=?, ", Arrays.copyOfRange(FIELDS, 0, FIELDS.length - 1)) + "=? WHERE id=? AND org_id=?";
        try(Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {


            setIdentity(baptism.getPerson(), stmt, 1, 2);
            setIdentity(baptism.getFather(), stmt, 3, 4);
            setIdentity(baptism.getMother(), stmt, 5, 6);
            stmt.setDate(7, convert(baptism.getBaptismDate()));
            stmt.setString(8, baptism.getBaptismLocation());
            stmt.setDate(9, convert(baptism.getBirthDate()));
            stmt.setString(10, baptism.getBirthLocation());
            setIdentity(baptism.getMinister(), stmt, 11, 12);
            setIdentity(baptism.getGodfather(), stmt, 13, 14);
            setIdentity(baptism.getGodmother(), stmt, 15, 16);
            setIdentity(baptism.getWitness(), stmt, 17, 18);
            stmt.setBoolean(19, baptism.isConditional());
            stmt.setBoolean(20, baptism.isReception());
            stmt.setString(21, convertNotations(baptism.getNotations()));
            stmt.setInt(22, baptism.getId());
            stmt.setInt(23, OrganizationContext.orgId());

            if(stmt.executeUpdate() == 0)
                throw new RuntimeException("Could not update baptismal record: " + baptism.getPerson().getName());
        } catch (SQLException e) {
            throw new RuntimeException("Could not update baptismal record: " + baptism.getPerson().getName(), e);
        }
    }

    public boolean delete(Baptism baptism) {
        try ( Connection conn = getConnection();
              PreparedStatement stmt = conn.prepareStatement("DELETE FROM baptisms WHERE id=? AND org_id=?")
        ){
            stmt.setInt(1, baptism.getId());
            stmt.setInt(2, OrganizationContext.orgId());

            return stmt.executeUpdate() != 0;
        } catch (SQLException e) {
            throw new RuntimeException("Could not delete baptismal record: " + baptism.getPerson().getName(), e);
        }
    }

    // ----- Private -----
    private List<Baptism> processResults(PreparedStatement stmt) throws SQLException {
        try(ResultSet rs = stmt.executeQuery()) {
            List<Baptism> results = new LinkedList<>();
            while(rs.next()) {
                Baptism b = new Baptism();
                b.setId(rs.getInt("id"));
                b.setPerson(getIdentity(rs, "name", "person_id"));
                b.setFather(getIdentity(rs, "father_name", "father_id"));
                b.setMother(getIdentity(rs, "mother_name", "mother_id"));
                b.setBaptismDate(convert(rs.getDate("baptism_date")));
                b.setBaptismLocation(rs.getString("baptism_location"));
                b.setBirthDate(convert(rs.getDate("birth_date")));
                b.setBirthLocation(rs.getString("birth_location"));
                b.setMinister(getIdentity(rs, "minister_name", "minister_id"));
                b.setGodfather(getIdentity(rs, "godfather_name", "godfather_id"));
                b.setGodmother(getIdentity(rs, "godmother_name", "godmother_id"));
                b.setWitness(getIdentity(rs, "witness_name", "witness_id"));
                b.setConditional(rs.getBoolean("conditional"));
                b.setReception(rs.getBoolean("reception"));
                b.setNotations(convertNotations(rs.getString("notations")));
                results.add(b);
            }
            return results;
        }
    }
}
