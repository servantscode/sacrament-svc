package org.servantscode.sacrament.db;

import org.junit.Before;
import org.junit.Test;
import org.servantscode.commons.db.DBAccess;
import org.servantscode.sacrament.Baptism;
import org.servantscode.sacrament.Identity;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


// TODO mock the database so that
public class BaptismDBTest {
    private BaptismDB bapt;
    private Connection conn;

    @Before
    public void initialise() {
        bapt = new BaptismDB();
    }

    @Test
    public void getBaptismTestNoConnection() {
        Baptism b = bapt.getBaptism(0);
        assertNull("Returned non-null value", b);
    }

    //Mock the database?
    @Test
    public void getBaptismTest() {
        Baptism b = getTestBaptism();
        bapt.createBaptismalRecord(b);
        Baptism c = bapt.getBaptism(b.getId());
        assertEquals(b, c);
    }

    private Baptism getTestBaptism() {
        Baptism ans = new Baptism();
        ans.setId(1);
        Identity[] people = new Identity[7];
        for (int i = 0; i < people.length; i++) {
            Identity person = new Identity("person" + (i + 1), i + 100);

            people[i] = new Identity("person" + (i + 1), i + 1);
        }
        ans.setPerson(people[0]);
        ans.setBaptismLocation("TheLocation");
        ans.setBaptismDate(LocalDate.of(2019, 7, 9));
        ans.setBirthLocation(null);
        ans.setBirthDate(LocalDate.of(2015, 1, 1));
        ans.setMinister(people[1]);
        ans.setFather(people[2]);
        ans.setMother(people[3]);
        ans.setGodfather(people[4]);
        ans.setGodmother(people[5]);
        ans.setWitness(people[6]);
        ans.setConditional(false);
        ans.setReception(true);
        ans.setNotations(new ArrayList<>());
        ans.getNotations().add("Notation");
        ans.setVolume("Summer, 2019");
        ans.setPage(314159);
        ans.setEntry(-7);
        return ans;
    }
}
