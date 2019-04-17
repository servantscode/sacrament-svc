package org.servantscode.sacrament.db;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.servantscode.commons.db.DBAccess;
import org.servantscode.sacrament.Baptism;
import org.servantscode.sacrament.Identity;

import java.io.IOException;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;

import static org.servantscode.commons.StringUtils.isEmpty;

public class BaptismDB extends DBAccess {
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    public Baptism getBaptism(int id) {
        try(Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM baptisms WHERE id=?")) {

            stmt.setInt(1, id);

            List<Baptism> results = processResults(stmt);

            return results.isEmpty()? null: results.get(0);
        } catch (SQLException e) {
            throw new RuntimeException("Could not retrieve baptism: " + id, e);
        }
    }

    public void createBaptismalRecord(Baptism baptism) {
        String sql = "INSERT INTO baptisms (name, person_id, father_name, father_id, mother_name, mother_id, baptism_date, baptism_location, birth_date, birth_location, minister_name, minister_id, godfather_name, godfather_id, godmother_name, godmother_id, withness_name, witness_id, conditional, reception, notations) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try(Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, baptism.getPerson().getName());
            stmt.setInt(2, baptism.getPerson().getId());
            stmt.setString(3, baptism.getFather().getName());
            stmt.setInt(4, baptism.getFather().getId());
            stmt.setString(5, baptism.getMother().getName());
            stmt.setInt(6, baptism.getMother().getId());
            stmt.setDate(7, Date.valueOf(baptism.getBaptismDate()));
            stmt.setString(8, baptism.getBaptismLocation());
            stmt.setDate(9, Date.valueOf(baptism.getBirthDate()));
            stmt.setString(10, baptism.getBirthLocation());
            stmt.setString(11, baptism.getMinister().getName());
            stmt.setInt(12, baptism.getMinister().getId());
            stmt.setString(13, baptism.getGodfather().getName());
            stmt.setInt(14, baptism.getGodfather().getId());
            stmt.setString(15, baptism.getGodmother().getName());
            stmt.setInt(16, baptism.getGodmother().getId());
            stmt.setString(17, baptism.getWitness().getName());
            stmt.setInt(18, baptism.getWitness().getId());
            stmt.setBoolean(19, baptism.isConditional());
            stmt.setBoolean(20, baptism.isReception());
            stmt.setString(21, convertNotations(baptism.getNotations()));

            if(stmt.executeUpdate() == 0)
                throw new RuntimeException("Could not create baptismal record: " + baptism.getPerson().getName());

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next())
                    baptism.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not create baptismal record: " + baptism.getPerson().getName());
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
                "withness_name=?, witness_id=?, " +
                "conditional=?, reception=?, notations=? WHERE id=?";
        try(Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, baptism.getPerson().getName());
            stmt.setInt(2, baptism.getPerson().getId());
            stmt.setString(3, baptism.getFather().getName());
            stmt.setInt(4, baptism.getFather().getId());
            stmt.setString(5, baptism.getMother().getName());
            stmt.setInt(6, baptism.getMother().getId());
            stmt.setDate(7, Date.valueOf(baptism.getBaptismDate()));
            stmt.setString(8, baptism.getBaptismLocation());
            stmt.setDate(9, Date.valueOf(baptism.getBirthDate()));
            stmt.setString(10, baptism.getBirthLocation());
            stmt.setString(11, baptism.getMinister().getName());
            stmt.setInt(12, baptism.getMinister().getId());
            stmt.setString(13, baptism.getGodfather().getName());
            stmt.setInt(14, baptism.getGodfather().getId());
            stmt.setString(15, baptism.getGodmother().getName());
            stmt.setInt(16, baptism.getGodmother().getId());
            stmt.setString(17, baptism.getWitness().getName());
            stmt.setInt(18, baptism.getWitness().getId());
            stmt.setBoolean(19, baptism.isConditional());
            stmt.setBoolean(20, baptism.isReception());
            stmt.setString(21, convertNotations(baptism.getNotations()));
            stmt.setInt(22, baptism.getId());

            if(stmt.executeUpdate() == 0)
                throw new RuntimeException("Could not update baptismal record: " + baptism.getPerson().getName());
        } catch (SQLException e) {
            throw new RuntimeException("Could not update baptismal record: " + baptism.getPerson().getName());
        }
    }

    public boolean delete(Baptism baptism) {
        try ( Connection conn = getConnection();
              PreparedStatement stmt = conn.prepareStatement("DELETE FROM baptisms WHERE id=?")
        ){
            stmt.setInt(1, baptism.getId());

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
                b.setBaptismDate(rs.getDate("baptism_date").toLocalDate());
                b.setBaptismLocation(rs.getString("baptism_location"));
                b.setBirthDate(rs.getDate("birth_date").toLocalDate());
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

    private List<String> convertNotations(String notations) {
        try {
            return JSON_MAPPER.readValue(notations, new TypeReference<List<String>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse notations", e);
        }
    }

    private String convertNotations(List<String> notations) {
        try {
            return JSON_MAPPER.writeValueAsString(notations);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse notations", e);
        }
    }

    private Identity getIdentity(ResultSet rs, String nameField, String idField) throws SQLException {
        String name = rs.getString(nameField);
        if(isEmpty(name))
            return null;
        return new Identity(name, rs.getInt(idField));
    }
}
