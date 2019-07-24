package org.servantscode.sacrament.db;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.servantscode.sacrament.Baptism;
import org.servantscode.sacrament.Confirmation;
import org.servantscode.sacrament.Identity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class ConfirmationDBTest {

    @Mock
    private Connection c;

    @Mock
    private PreparedStatement stmt;

    @Mock
    private ResultSet rs;

    @Mock
    private ConnectionFactoryMocking factory;

    private ConfirmationDB confirmationDB;

//                b.setId(rs.getInt("id"));
//                b.setPerson(getIdentity(rs, "name", "person_id"));
//                b.setFather(getIdentity(rs, "father_name", "father_id"));
//                b.setMother(getIdentity(rs, "mother_name", "mother_id"));
//                b.setBaptismId(rs.getInt("baptism_id"));
//                b.setBaptismDate(convert(rs.getDate("baptism_date")));
//                b.setBaptismLocation(rs.getString("baptism_location"));
//                b.setSponsor(getIdentity(rs, "sponsor_name", "sponsor_id"));
//                b.setConfirmationDate(convert(rs.getDate("confirmation_date")));
//                b.setConfirmationLocation(rs.getString("confirmation_location"));
//                b.setMinister(getIdentity(rs, "minister_name", "minister_id"));
//                b.setNotations(convertNotations(rs.getString("notations")));

    @Before
    public void initialise() throws Exception {
        confirmationDB = new ConfirmationDB();
        when(stmt.executeQuery()).thenReturn(rs);
        when(c.prepareStatement(any(String.class))).thenReturn(stmt);
        when(factory.getConnection()).thenReturn(c);

        //TODO update to have confirmation values
        when(rs.getInt("id")).thenReturn(13);
        when(rs.getString("name")).thenReturn("person1");
        when(rs.getInt("person_id")).thenReturn(1);
        when(rs.getString("father_name")).thenReturn("person3");
        when(rs.getInt("father_id")).thenReturn(2);
        when(rs.getString("mother_name")).thenReturn("person4");
        when(rs.getInt("mother_id")).thenReturn(3);
        when(rs.getString("minister_name")).thenReturn("person2");
        when(rs.getInt("minister_id")).thenReturn(2);
        when(rs.getString("sponsor_name")).thenReturn("person5");
        when(rs.getInt("sponsor_id")).thenReturn(5);

        when(rs.getBoolean("conditional")).thenReturn(false);
        when(rs.getBoolean("reception")).thenReturn(true);
        when(rs.getString("notations")).thenReturn("Notation");

        ConfirmationDB.setConnectionFactory(factory);
    }

    @Test
    public void getConfirmationTest() {
        Confirmation confirmation = getTestConfirmation();
        Confirmation other = confirmationDB.getConfirmation(confirmation.getId());
        checkEqual(confirmation, other);
    }

    @Test
    public void getConfirmationByPersonTest() {
        Confirmation confirmation = getTestConfirmation();
        Confirmation other = confirmationDB.getConfirmationByPerson(confirmation.getPerson().getId());
        checkEqual(confirmation, other);
    }

    private Confirmation getTestConfirmation() {
        Confirmation confirmation = new Confirmation();
        confirmation.setId(13);
        Identity[] people = new Identity[5];
        for (int i = 0; i < people.length; i++) {
            people[i] = new Identity("person" + (i + 1), i + 1);
        }
        confirmation.setPerson(people[0]);
        confirmation.setFather(people[1]);
        confirmation.setMother(people[2]);
        confirmation.setSponsor(people[3]);
        confirmation.setMinister(people[4]);
        confirmation.setBaptismId(43201);
        confirmation.setBaptismDate(LocalDate.of(2005, 4, 18));
        confirmation.setBaptismLocation("Somewhere");
        confirmation.setConfirmationDate(LocalDate.of(2009, 5, 17));
        confirmation.setConfirmationLocation("Somewhere, maybe elsewhere?");
        confirmation.setNotations(new ArrayList<>());
        confirmation.getNotations().add("Notation");
        confirmation.getNotations().add("Another Notation");
        confirmation.getNotations().add("A third Notation");
        confirmation.setVolume("Spring, 2009");
        confirmation.setPage(43789210);
        confirmation.setEntry(0);
        return confirmation;
    }

    private void checkEqual(Confirmation a, Confirmation b) {
        assertEquals(a.getId(), b.getId());
        assertEquals(a.getPerson().getId(), b.getPerson().getId());
        assertEquals(a.getPerson().getName(), b.getPerson().getName());
        assertEquals(a.getFather().getId(), b.getFather().getId());
        assertEquals(a.getFather().getName(), b.getFather().getName());
        assertEquals(a.getMother().getId(), b.getMother().getId());
        assertEquals(a.getMother().getName(), b.getMother().getName());
        assertEquals(a.getSponsor().getId(), b.getSponsor().getId());
        assertEquals(a.getSponsor().getName(), b.getSponsor().getName());
        assertEquals(a.getMinister().getId(), b.getMinister().getId());
        assertEquals(a.getMinister().getName(), b.getMinister().getName());
        assertEquals(a.getBaptismId(), b.getBaptismId());
        assertTrue(a.getBaptismDate().isEqual(b.getBaptismDate()));
        assertEquals(a.getBaptismLocation(), b.getBaptismLocation());
        assertTrue(a.getConfirmationDate().isEqual(b.getConfirmationDate()));
        assertEquals(a.getConfirmationLocation(), b.getConfirmationLocation());
        assertEquals(a.getNotations().size(), b.getNotations().size());
        for (int i = 0; i < a.getNotations().size(); i++) {
            assertEquals(a.getNotations().get(i), b.getNotations().get(i));
        }
        assertEquals(a.getVolume(), b.getVolume());
        assertEquals(a.getPage(), b.getPage());
        assertEquals(a.getEntry(), b.getEntry());
    }

}
