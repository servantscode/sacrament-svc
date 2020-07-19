package org.servantscode.sacrament.db;

import org.servantscode.commons.search.FieldTransformer;
import org.servantscode.commons.search.QueryBuilder;
import org.servantscode.commons.search.UpdateBuilder;
import org.servantscode.commons.security.OrganizationContext;
import org.servantscode.sacrament.Mass;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class MassDB extends AbstractSacramentDB<Mass> {

    private static FieldTransformer FIELD_MAP = new FieldTransformer();
    static{
        FIELD_MAP.put("eventName", "e.name");
    }


    public MassDB() {
        super(Mass.class, "e.name", FIELD_MAP);
    }

    private QueryBuilder dataFields() {
        return select("m.*");
    }

    private QueryBuilder select(QueryBuilder selection) {
        return selection.from("masses m").leftJoin("events e ON m.id=e.id").inOrg();
    }

    public int getCount(String search) {
        return getCount(select(count()).search(searchParser.parse(search)));
    }

    public List<Mass> getPage(String search, String sort, int start, int count) {
        return get(select(dataFields()).search(searchParser.parse(search)).page(sort, start, count));
    }

    public Mass get(int id) {
        return getOne(selectAll().from("masses").withId(id).inOrg());
    }

    public Mass create(Mass mass) {
        create(insertInto("masses")
                .value("id", mass.getId())
                .value("presider_name", mass.getPresider().getName())
                .value("presider_id", mass.getPresider().getId())
                .value("org_id", OrganizationContext.orgId()));
        return mass;
    }

    public Mass update(Mass mass) {
        UpdateBuilder cmd = update("masses")
                .value("presider_name", mass.getPresider().getName())
                .value("presider_id", mass.getPresider().getId())
                .withId(mass.getId()).inOrg();

        if(!update(cmd))
            throw new RuntimeException("Could not update mass: " + mass.getId());

        return mass;
    }

    public boolean delete(Mass mass) {
        return delete(deleteFrom("events").withId(mass.getId()).inOrg());
    }

    // ----- Private -----
    @Override
    protected Mass processRow(ResultSet rs) throws SQLException {
        Mass mass = new Mass();
        mass.setId(rs.getInt("id"));
        mass.setPresider(getIdentity(rs, "name", "person_id"));
        return mass;
    }
}
