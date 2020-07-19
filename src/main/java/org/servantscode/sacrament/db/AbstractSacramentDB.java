package org.servantscode.sacrament.db;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.servantscode.commons.Identity;
import org.servantscode.commons.db.DBAccess;
import org.servantscode.commons.db.EasyDB;
import org.servantscode.commons.search.FieldTransformer;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;

import static org.servantscode.commons.StringUtils.isEmpty;

public abstract class AbstractSacramentDB<T> extends EasyDB<T> {
    protected static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    public AbstractSacramentDB(Class<T> clazz, String defaultField) {
        super(clazz, defaultField);
    }

    public AbstractSacramentDB(Class<T> clazz, String defaultField, Map<String, String> fieldMap) {
        super(clazz, defaultField, fieldMap);
    }

    public AbstractSacramentDB(Class<T> clazz, String defaultField, FieldTransformer transformer) {
        super(clazz, defaultField, transformer);
    }


    protected List<String> convertNotations(String notations) {
        try {
            return JSON_MAPPER.readValue(notations, new TypeReference<List<String>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse notations", e);
        }
    }

    protected String convertNotations(List<String> notations) {
        try {
           return JSON_MAPPER.writeValueAsString(notations);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse notations", e);
        }
    }

    protected Identity getIdentity(ResultSet rs, String nameField, String idField) throws SQLException {
        String name = rs.getString(nameField);
        if(isEmpty(name))
            return null;
        return new Identity(name, rs.getInt(idField));
    }

    protected void setIdentity(Identity i, PreparedStatement stmt, int nameFieldNum, int idFieldNum) throws SQLException {
        if (i != null) {
            stmt.setString(nameFieldNum, i.getName());
            if(i.getId() <= 0)
                stmt.setNull(idFieldNum, Types.INTEGER);
            else
                stmt.setInt(idFieldNum, i.getId());
        } else {
            stmt.setNull(nameFieldNum, Types.VARCHAR);
            stmt.setNull(idFieldNum, Types.INTEGER);
        }
    }
}
