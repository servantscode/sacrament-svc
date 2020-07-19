package org.servantscode.sacrament.db;

import org.servantscode.commons.search.QueryBuilder;
import org.servantscode.commons.security.OrganizationContext;
import org.servantscode.sacrament.Marriage;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;

public class MarriageDB extends AbstractSacramentDB<Marriage> {

    public MarriageDB() {
        super(Marriage.class, "groom_name");
    }
    public Marriage getMarriage(int id) {
        QueryBuilder query = selectAll().from("marriages").withId(id).inOrg();
        try(Connection conn = getConnection();
            PreparedStatement stmt = query.prepareStatement(conn)) {

            return firstOrNull(processResults(stmt));
        } catch (SQLException e) {
            throw new RuntimeException("Could not retrieve marriage: " + id, e);
        }
    }

    public Marriage getMarriageByPerson(int personId) {
        QueryBuilder query = selectAll().from("marriages").where("(groom_id=? OR bride_id=?)", personId, personId).inOrg();
        try(Connection conn = getConnection();
            PreparedStatement stmt = query.prepareStatement(conn)) {

            return firstOrNull(processResults(stmt));
        } catch (SQLException e) {
            throw new RuntimeException("Could not retrieve marriage for person: " + personId, e);
        }
    }

    public void createMarriageRecord(Marriage marriage) {
        String sql = "INSERT INTO marriages (groom_name, groom_id, groom_father_name, groom_father_id, groom_mother_name, groom_mother_id, groom_baptism_id, groom_baptism_date, groom_baptism_location, bride_name, bride_id, bride_father_name, bride_father_id, bride_mother_name, bride_mother_id, bride_baptism_id, bride_baptism_date, bride_baptism_location, wedding_date, wedding_location, minister_name, minister_id, witness_1_name, witness_1_id, witness_2_name, witness_2_id, notations, org_id) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try(Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            setIdentity(marriage.getGroom(), stmt, 1, 2);
            setIdentity(marriage.getGroomFather(), stmt, 3, 4);
            setIdentity(marriage.getGroomMother(), stmt, 5, 6);
            if(marriage.getGroomBaptismId() > 0)
                stmt.setInt(7, marriage.getGroomBaptismId());
            else
                stmt.setNull(7, Types.INTEGER);
            stmt.setDate(8, convert(marriage.getGroomBaptismDate()));
            stmt.setString(9, marriage.getGroomBaptismLocation());
            setIdentity(marriage.getBride(), stmt, 10, 11);
            setIdentity(marriage.getBrideFather(), stmt, 12, 13);
            setIdentity(marriage.getBrideMother(), stmt, 14, 15);
            if(marriage.getBrideBaptismId() > 0)
                stmt.setInt(16, marriage.getBrideBaptismId());
            else
                stmt.setNull(16, Types.INTEGER);
            stmt.setDate(17, convert(marriage.getBrideBaptismDate()));
            stmt.setString(18, marriage.getBrideBaptismLocation());
            stmt.setDate(19, convert(marriage.getMarriageDate()));
            stmt.setString(20, marriage.getMarriageLocation());
            setIdentity(marriage.getMinister(), stmt, 21, 22);
            setIdentity(marriage.getWitness1(), stmt, 23, 24);
            setIdentity(marriage.getWitness2(), stmt, 25, 26);
            stmt.setString(27, convertNotations(marriage.getNotations()));
            stmt.setInt(28, OrganizationContext.orgId());

            if(stmt.executeUpdate() == 0)
                throw new RuntimeException(String.format("Could not create marriage record: %s and %s",
                        marriage.getGroom().getName(), marriage.getBride().getName()));

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next())
                    marriage.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException(String.format("Could not create marriage record: %s and %s",
                    marriage.getGroom().getName(), marriage.getBride().getName()), e);
        }
    }

    public void updateMarriageRecord(Marriage marriage) {
        String sql = "UPDATE marriages SET groom_name=?, groom_id=?, " +
                "groom_father_name=?, groom_father_id=?, " +
                "groom_mother_name=?, groom_mother_id=?, " +
                "groom_baptism_id=?, groom_baptism_date=?, groom_baptism_location=?, " +
                "bride_name=?, bride_id=?, " +
                "bride_father_name=?, bride_father_id=?, " +
                "bride_mother_name=?, bride_mother_id=?, " +
                "bride_baptism_id=?, bride_baptism_date=?, bride_baptism_location=?, " +
                "wedding_date=?, wedding_location=?, " +
                "minister_name=?, minister_id=?, " +
                "witness_1_name=?, witness_1_id=?, " +
                "witness_2_name=?, witness_2_id=?, notations=? WHERE id=? AND org_id=?";
        try(Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            setIdentity(marriage.getGroom(), stmt, 1, 2);
            setIdentity(marriage.getGroomFather(), stmt, 3, 4);
            setIdentity(marriage.getGroomMother(), stmt, 5, 6);
            if(marriage.getGroomBaptismId() > 0)
                stmt.setInt(7, marriage.getGroomBaptismId());
            else
                stmt.setNull(7, Types.INTEGER);
            stmt.setDate(8, convert(marriage.getGroomBaptismDate()));
            stmt.setString(9, marriage.getGroomBaptismLocation());
            setIdentity(marriage.getBride(), stmt, 10, 11);
            setIdentity(marriage.getBrideFather(), stmt, 12, 13);
            setIdentity(marriage.getBrideMother(), stmt, 14, 15);
            if(marriage.getBrideBaptismId() > 0)
                stmt.setInt(16, marriage.getBrideBaptismId());
            else
                stmt.setNull(16, Types.INTEGER);
            stmt.setDate(17, convert(marriage.getBrideBaptismDate()));
            stmt.setString(18, marriage.getBrideBaptismLocation());
            stmt.setDate(19, convert(marriage.getMarriageDate()));
            stmt.setString(20, marriage.getMarriageLocation());
            setIdentity(marriage.getMinister(), stmt, 21, 22);
            setIdentity(marriage.getWitness1(), stmt, 23, 24);
            setIdentity(marriage.getWitness2(), stmt, 25, 26);
            stmt.setString(27, convertNotations(marriage.getNotations()));
            stmt.setInt(28, marriage.getId());
            stmt.setInt(29, OrganizationContext.orgId());

            if(stmt.executeUpdate() == 0)
                throw new RuntimeException(String.format("Could not update marriage record: %s and %s",
                        marriage.getGroom().getName(), marriage.getBride().getName()));
        } catch (SQLException e) {
            throw new RuntimeException(String.format("Could not update marriage record: %s and %s",
                    marriage.getGroom().getName(), marriage.getBride().getName()), e);
        }
    }

    public boolean delete(Marriage marriage) {
        try ( Connection conn = getConnection();
              PreparedStatement stmt = conn.prepareStatement("DELETE FROM marriages WHERE id=? AND org_id=?")
        ){
            stmt.setInt(1, marriage.getId());
            stmt.setInt(2, OrganizationContext.orgId());

            return stmt.executeUpdate() != 0;
        } catch (SQLException e) {
            throw new RuntimeException(String.format("Could not delete marriage record: %s and %s",
                    marriage.getGroom().getName(), marriage.getBride().getName()), e);
        }
    }

    // ----- Private -----
    @Override
    protected Marriage processRow(ResultSet rs) throws SQLException {
        Marriage m = new Marriage();
        m.setId(rs.getInt("id"));
        m.setGroom(getIdentity(rs, "groom_name", "groom_id"));
        m.setGroomFather(getIdentity(rs, "groom_father_name", "groom_father_id"));
        m.setGroomMother(getIdentity(rs, "groom_mother_name", "groom_mother_id"));
        m.setGroomBaptismId(rs.getInt("groom_baptism_id"));
        m.setGroomBaptismDate(convert(rs.getDate("groom_baptism_date")));
        m.setGroomBaptismLocation(rs.getString("groom_baptism_location"));
        m.setBride(getIdentity(rs, "bride_name", "bride_id"));
        m.setBrideFather(getIdentity(rs, "bride_father_name", "bride_father_id"));
        m.setBrideMother(getIdentity(rs, "bride_mother_name", "bride_mother_id"));
        m.setBrideBaptismId(rs.getInt("bride_baptism_id"));
        m.setBrideBaptismDate(convert(rs.getDate("bride_baptism_date")));
        m.setBrideBaptismLocation(rs.getString("bride_baptism_location"));
        m.setMarriageDate(convert(rs.getDate("wedding_date")));
        m.setMarriageLocation(rs.getString("wedding_location"));
        m.setMinister(getIdentity(rs, "minister_name", "minister_id"));
        m.setWitness1(getIdentity(rs, "witness_1_name", "witness_1_id"));
        m.setWitness2(getIdentity(rs, "witness_2_name", "witness_2_id"));
        m.setNotations(convertNotations(rs.getString("notations")));
        return m;
    }
}
