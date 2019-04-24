package org.servantscode.sacrament.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.servantscode.commons.rest.SCServiceBase;
import org.servantscode.sacrament.Baptism;
import org.servantscode.sacrament.Marriage;
import org.servantscode.sacrament.db.BaptismDB;
import org.servantscode.sacrament.db.MarriageDB;
import org.servantscode.sacrament.util.ObjectComparator;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Set;

import static java.util.Collections.emptyList;
import static org.servantscode.commons.StringUtils.isEmpty;

@Path("/sacrament/marriage")
public class MarriageSvc extends SCServiceBase {
    private static final Logger LOG = LogManager.getLogger(MarriageSvc.class);

    private MarriageDB db;
    private BaptismDB baptismDb;

    public MarriageSvc() {
        this.db = new MarriageDB();
        this.baptismDb = new BaptismDB();
    }

    @GET @Path("/{id}") @Produces(MediaType.APPLICATION_JSON)
    public Marriage getMarriageRecord(@PathParam("id") int id) {
        verifyUserAccess("sacrament.marriage.read");
        if(id <= 0)
            throw new NotFoundException();

        try {
            Marriage marriage = db.getMarriage(id);
            if (marriage == null)
                throw new NotFoundException();
            return marriage;
        } catch(Throwable t) {
            LOG.error("Could not retrieve marriage record.", t);
            throw new WebApplicationException("Could not retrieve marriage record.", t);
        }
    }

    @GET @Path("/person/{id}") @Produces(MediaType.APPLICATION_JSON)
    public Marriage getMarriageRecordForPerson(@PathParam("id") int personId) {
        verifyUserAccess("sacrament.marriage.read");
        if(personId <= 0)
            throw new NotFoundException();

        try {
            Marriage marriage;
            marriage = db.getMarriageByPerson(personId);
            if (marriage == null)
                throw new NotFoundException();
            return marriage;
        } catch(WebApplicationException we) {
            throw we;
        } catch(Throwable t) {
            LOG.error("Could not retrieve marriage record.", t);
            throw new WebApplicationException("Could not retrieve marriage record.", t);
        }
    }

    @POST @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
    public Marriage createMarriageRecord(Marriage marriage) {
        verifyUserAccess("sacrament.marriage.create");
        if(marriage.getGroom() == null || isEmpty(marriage.getGroom().getName()))
            throw new BadRequestException();

        if(marriage.getBride() == null || isEmpty(marriage.getBride().getName()))
            throw new BadRequestException();

        if(marriage.getNotations() == null)
            marriage.setNotations(emptyList());

        int groomBaptismId = marriage.getGroomBaptismId();
        if(groomBaptismId > 0) {
            Baptism b = baptismDb.getBaptism(groomBaptismId );
            if(b == null)
                throw new BadRequestException();

            marriage.setGroomBaptismDate(b.getBaptismDate());
            marriage.setGroomBaptismLocation(b.getBaptismLocation());
        }

        int brideBaptismId = marriage.getBrideBaptismId();
        if(brideBaptismId > 0) {
            Baptism b = baptismDb.getBaptism(brideBaptismId );
            if(b == null)
                throw new BadRequestException();

            marriage.setBrideBaptismDate(b.getBaptismDate());
            marriage.setBrideBaptismLocation(b.getBaptismLocation());
        }

        try {
            db.createMarriageRecord(marriage);
            LOG.info(String.format("Stored marriage record for: %s and %s", marriage.getGroom().getName(), marriage.getBride().getName()));
            return marriage;
        } catch(WebApplicationException we) {
            throw we;
        } catch(Throwable t) {
            LOG.error("Could not create marriage record.", t);
            throw new WebApplicationException("Could not create marriage record.", t);
        }
    }

    @PUT @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
    public Marriage updateMarriageRecord(Marriage marriage) {
        verifyUserAccess("sacrament.marriage.update");
        if(marriage.getId() <= 0)
            throw new NotFoundException();

        if(marriage.getNotations() == null)
            marriage.setNotations(emptyList());

        try {
            Marriage dbRecord = db.getMarriage(marriage.getId());
            if(dbRecord == null)
                throw new NotFoundException();

            if(changeRequiresAdmin(dbRecord, marriage))
                verifyUserAccess("admin.marriage.update");

            db.updateMarriageRecord(marriage);

            LOG.info(String.format("Updated marriage record for: %s and %s", dbRecord.getGroom().getName(), dbRecord.getBride().getName()));
            return marriage;
        } catch(WebApplicationException we) {
            throw we;
        } catch(Throwable t) {
            LOG.error("Could not update marriage record.", t);
            throw new WebApplicationException("Could not update marriage record.", t);
        }
    }

    @POST @Path("/{id}/notation") @Consumes(MediaType.APPLICATION_JSON)
    public void addNotation(@PathParam("id") int id,
                            String notation) {
        verifyUserAccess("sacrament.marriage.update");
        if(id <= 0)
            throw new NotFoundException();

        if(isEmpty(notation))
            throw new BadRequestException();

        try {
            Marriage dbRecord = db.getMarriage(id);
            if(dbRecord == null)
                throw new NotFoundException();

            dbRecord.getNotations().add(notation);
            db.updateMarriageRecord(dbRecord);
            LOG.info(String.format("Added notification to marriage record for: %s and %s", dbRecord.getGroom().getName(), dbRecord.getBride().getName()));
        } catch(WebApplicationException we) {
            throw we;
        } catch(Throwable t) {
            LOG.error("Could not update marriage record.", t);
            throw new WebApplicationException("Could not update marriage record.", t);
        }
    }

    @DELETE @Path("/{id}") @Produces(MediaType.APPLICATION_JSON)
    public void deleteMarriageRecord(@PathParam("id") int id) {
        verifyUserAccess("admin.marriage.delete");
        if(id <= 0)
            throw new NotFoundException();

        try {
            Marriage marriage = db.getMarriage(id);
            if (marriage == null)
                throw new NotFoundException();

            db.delete(marriage);
        } catch(WebApplicationException we) {
            throw we;
        } catch(Throwable t) {
            LOG.error("Could not delete marriage record.", t);
            throw new WebApplicationException("Could not delete marriage record.", t);
        }
    }

    // ----- Private -----
    private boolean changeRequiresAdmin(Marriage dbRecord, Marriage marriage) {
        Set<String> differences = ObjectComparator.getFieldDifferences(dbRecord, marriage);

        //Notations are the only change allowable without admin permissions
        differences.remove("getNotations");
        return differences.size()>0;
    }


}
