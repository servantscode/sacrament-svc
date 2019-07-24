package org.servantscode.sacrament.db;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.servantscode.sacrament.Baptism;
import org.servantscode.sacrament.Identity;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BaptismDBTest {

    @Mock
    private Connection c;

    @Mock
    private PreparedStatement stmt;

    @Mock
    private ResultSet rs;

    @Mock
    private ConnectionFactoryMocking factory;

    private BaptismDB bapt;

    @Before
    public void initialise() throws Exception {
        bapt = new BaptismDB();
        when(stmt.executeQuery()).thenReturn(rs);
        when(c.prepareStatement(any(String.class))).thenReturn(stmt);
        when(factory.getConnection()).thenReturn(c);

        when(rs.getInt("id")).thenReturn(1);
        when(rs.getString("name")).thenReturn("person1");
        when(rs.getInt("person_id")).thenReturn(1);
        when(rs.getString("father_name")).thenReturn("person3");
        when(rs.getInt("father_id")).thenReturn(3);
        when(rs.getString("mother_name")).thenReturn("person4");
        when(rs.getInt("mother_id")).thenReturn(4);
        when(rs.getString("minister_name")).thenReturn("person2");
        when(rs.getInt("minister_id")).thenReturn(2);
        when(rs.getString("godfather_name")).thenReturn("person5");
        when(rs.getInt("godfather_id")).thenReturn(5);
        when(rs.getString("godmother_name")).thenReturn("person6");
        when(rs.getInt("godmother_id")).thenReturn(6);
        when(rs.getString("witness_name")).thenReturn("person7");
        when(rs.getInt("witness_id")).thenReturn(7);
        when(rs.getDate("baptism_date")).thenReturn(Date.valueOf(LocalDate.of(2019, 7, 9)));
        when(rs.getString("baptism_location")).thenReturn("TheLocation");
        when(rs.getDate("birth_date")).thenReturn(Date.valueOf(LocalDate.of(2015, 1, 1)));
        when(rs.getString("birth_location")).thenReturn(null);
        when(rs.getBoolean("conditional")).thenReturn(false);
        when(rs.getBoolean("reception")).thenReturn(true);
        when(rs.getString("notations")).thenReturn("Notation");

        BaptismDB.setConnectionFactory(factory);
    }


    //TODO fix null pointer exception for these tests
    @Test
    public void getBaptismTest() {
        Baptism b = getTestBaptism();
        Baptism c = bapt.getBaptism(b.getId());
        checkEqual(b, c);
    }

    @Test
    public void getBaptismByPersonTest() {
        Baptism b = getTestBaptism();
        Baptism c = bapt.getBaptismByPerson(b.getPerson().getId());
        checkEqual(b, c);
    }

    //TODO add more tests

    private Baptism getTestBaptism() {
        Baptism ans = new Baptism();
        ans.setId(1);
        Identity[] people = new Identity[7];
        for (int i = 0; i < people.length; i++) {
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

    private void checkEqual(Baptism a, Baptism b) {
        assertEquals(a.getPerson().getId(), b.getPerson().getId());
        assertEquals(a.getPerson().getName(), b.getPerson().getName());
        assertEquals(a.getBaptismLocation(), b.getBaptismLocation());
        assertTrue(a.getBaptismDate().isEqual(b.getBaptismDate()));
        assertEquals(a.getBirthLocation(), b.getBirthLocation());
        assertTrue(a.getBirthDate().isEqual(b.getBirthDate()));
        assertEquals(a.getPerson().getId(), b.getPerson().getId());
        assertEquals(a.getPerson().getName(), b.getPerson().getName());
        assertEquals(a.getMinister().getId(), b.getMinister().getId());
        assertEquals(a.getMinister().getName(), b.getMinister().getName());
        assertEquals(a.getMother().getId(), b.getMother().getId());
        assertEquals(a.getMother().getName(), b.getMother().getName());
        assertEquals(a.getGodfather().getId(), b.getGodfather().getId());
        assertEquals(a.getGodfather().getName(), b.getGodfather().getName());
        assertEquals(a.getGodmother().getId(), b.getGodmother().getId());
        assertEquals(a.getGodmother().getName(), b.getGodmother().getName());
        assertEquals(a.getWitness().getId(), b.getWitness().getId());
        assertEquals(a.getWitness().getName(), b.getWitness().getName());
        assertEquals(a.isConditional(), b.isConditional());
        assertEquals(a.isReception(), b.isReception());
        assertEquals(a.getNotations().size(), b.getNotations().size());
        for (int i = 0; i < a.getNotations().size(); i++) {
            assertEquals(a.getNotations().get(i), b.getNotations().get(i));
        }
        assertEquals(a.getVolume(), b.getVolume());
        assertEquals(a.getPage(), b.getPage());
        assertEquals(a.getEntry(), b.getEntry());
    }
}
