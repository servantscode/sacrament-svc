package org.servantscode.sacrament.db;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.servantscode.commons.db.AbstractDBUpgrade;

import java.sql.SQLException;

public class DBUpgrade extends AbstractDBUpgrade {
    private static final Logger LOG = LogManager.getLogger(DBUpgrade.class);

    @Override
    public void doUpgrade() throws SQLException {
        LOG.info("Verifying database structures.");

        if(!tableExists("baptisms")) {
            LOG.info("-- Creating baptisms table");
            runSql("CREATE TABLE baptisms (id SERIAL PRIMARY KEY, " +
                                          "name TEXT NOT NULL, " +
                                          "person_id INTEGER, " +
                                          "father_name TEXT, " +
                                          "father_id INTEGER, " +
                                          "mother_name TEXT, " +
                                          "mother_id INTEGER, " +
                                          "baptism_date DATE NOT NULL, " +
                                          "baptism_location TEXT, " +
                                          "birth_date DATE, " +
                                          "birth_location TEXT, " +
                                          "minister_name TEXT NOT NULL, " +
                                          "minister_id INTEGER, " +
                                          "godfather_name TEXT, " +
                                          "godfather_id INTEGER, " +
                                          "godmother_name TEXT, " +
                                          "godmother_id INTEGER, " +
                                          "witness_name TEXT, " +
                                          "witness_id INTEGER, " +
                                          "conditional BOOLEAN DEFAULT FALSE, " +
                                          "reception BOOLEAN DEFAULT FALSE, " +
                                          "notations TEXT, " +
                                          "org_id INTEGER references organizations(id) ON DELETE CASCADE)");
        }

        if(!tableExists("confirmations")) {
            LOG.info("-- Creating confirmations table");
            runSql("CREATE TABLE confirmations (id SERIAL PRIMARY KEY, " +
                                               "name TEXT NOT NULL, " +
                                               "person_id INTEGER, " +
                                               "father_name TEXT, " +
                                               "father_id INTEGER, " +
                                               "mother_name TEXT, " +
                                               "mother_id INTEGER, " +
                                               "baptism_id INTEGER REFERENCES baptisms(id), " +
                                               "baptism_date DATE NOT NULL, " +
                                               "baptism_location TEXT, " +
                                               "sponsor_name TEXT NOT NULL, " +
                                               "sponsor_id INTEGER, " +
                                               "confirmation_date DATE NOT NULL, " +
                                               "confirmation_location TEXT NOT NULL, " +
                                               "minister_name TEXT NOT NULL, " +
                                               "minister_id INTEGER, " +
                                               "notations TEXT, " +
                                               "org_id INTEGER references organizations(id) ON DELETE CASCADE)");
        }

        if(!tableExists("marriages")) {
            LOG.info("-- Creating marriages table");
            runSql("CREATE TABLE marriages (id SERIAL PRIMARY KEY, " +
                                           "groom_name TEXT NOT NULL, " +
                                           "groom_id INTEGER, " +
                                           "groom_father_name TEXT, " +
                                           "groom_father_id INTEGER, " +
                                           "groom_mother_name TEXT, " +
                                           "groom_mother_id INTEGER, " +
                                           "groom_baptism_id INTEGER REFERENCES baptisms(id), " +
                                           "groom_baptism_date DATE, " +
                                           "groom_baptism_location TEXT, " +
                                           "bride_name TEXT NOT NULL, " +
                                           "bride_id INTEGER, " +
                                           "bride_father_name TEXT, " +
                                           "bride_father_id INTEGER, " +
                                           "bride_mother_name TEXT, " +
                                           "bride_mother_id INTEGER, " +
                                           "bride_baptism_id INTEGER REFERENCES baptisms(id), " +
                                           "bride_baptism_date DATE, " +
                                           "bride_baptism_location TEXT, " +
                                           "wedding_date DATE NOT NULL, " +
                                           "wedding_location TEXT, " +
                                           "minister_name TEXT NOT NULL, " +
                                           "minister_id INTEGER, " +
                                           "witness_1_name TEXT NOT NULL, " +
                                           "witness_1_id INTEGER, " +
                                           "witness_2_name TEXT NOT NULL, " +
                                           "witness_2_id INTEGER, " +
                                           "notations TEXT, " +
                                           "org_id INTEGER references organizations(id) ON DELETE CASCADE)");
        }

        if(!tableExists("mass_intentions")) {
            LOG.info("-- Creating mass_intentions table");
            runSql("CREATE TABLE mass_intentions (id SERIAL PRIMARY KEY, " +
                                                 "eventId INTEGER REFERENCES events(id) NOT NULL, " +
                                                 "personName TEXT, " +
                                                 "personId INTEGER REFERENCES people(id), " +
                                                 "intentionType TEXT, " +
                                                 "requesterName TEXT, " +
                                                 "requesterId INTEGER REFERENCES people(id), " +
                                                 "requesterPhone TEXT, " +
                                                 "stipend FLOAT, " +
                                                 "org_id INTEGER references organizations(id) ON DELETE CASCADE)");
        }

        if(!tableExists("masses")) {
            LOG.info("-- Creating masses table");
            runSql("CREATE TABLE masses (id INTEGER PRIMARY KEY REFERENCES events(id) ON DELETE CASCADE, " +
                                        "presider_name TEXT, presider_id INTEGER REFERENCES people(id), " +
                                        "org_id INTEGER REFERENCES organizations(id) ON DELETE CASCADE)");
        }

        ensureColumn("mass_intentions", "stipend", "FLOAT");
    }
}
