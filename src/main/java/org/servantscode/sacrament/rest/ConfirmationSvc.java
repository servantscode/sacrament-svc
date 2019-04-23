package org.servantscode.sacrament.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.servantscode.commons.rest.SCServiceBase;
import org.servantscode.sacrament.Baptism;
import org.servantscode.sacrament.Confirmation;
import org.servantscode.sacrament.db.BaptismDB;
import org.servantscode.sacrament.db.ConfirmationDB;
import org.servantscode.sacrament.util.ObjectComparator;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Set;

import static java.util.Collections.emptyList;
import static org.servantscode.commons.StringUtils.isEmpty;

@Path("/sacrament/confirmation")
public class ConfirmationSvc extends SCServiceBase {
    private static final Logger LOG = LogManager.getLogger(ConfirmationSvc.class);

    private ConfirmationDB db;
    private BaptismDB baptismDb;

    public ConfirmationSvc() {
        this.db = new ConfirmationDB();
        this.baptismDb = new BaptismDB();
    }

    @GET @Path("/{id}") @Produces(MediaType.APPLICATION_JSON)
    public Confirmation getConfirmationRecord(@PathParam("id") int id) {
        verifyUserAccess("sacrament.confirmation.read");
        if(id <= 0)
            throw new NotFoundException();

        try {
            Confirmation confirmation = db.getConfirmation(id);
            if (confirmation == null)
                throw new NotFoundException();
            return confirmation;
        } catch(Throwable t) {
            LOG.error("Could not retrieve confirmation record.", t);
            throw new WebApplicationException("Could not retrieve confirmation record.", t);
        }
    }

    @GET @Path("/person/{id}") @Produces(MediaType.APPLICATION_JSON)
    public Confirmation getConfirmationRecordForPerson(@PathParam("id") int personId) {
        verifyUserAccess("sacrament.confirmation.read");
        if(personId <= 0)
            throw new NotFoundException();

        try {
            Confirmation confirmation;
            confirmation = db.getConfirmationByPerson(personId);
            if (confirmation == null)
                throw new NotFoundException();
            return confirmation;
        } catch(WebApplicationException we) {
            throw we;
        } catch(Throwable t) {
            LOG.error("Could not retrieve confirmation record.", t);
            throw new WebApplicationException("Could not retrieve confirmation record.", t);
        }
    }

    @POST @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
    public Confirmation createConfirmationRecord(Confirmation confirmation) {
        verifyUserAccess("sacrament.confirmation.create");
        if(confirmation.getPerson() == null || isEmpty(confirmation.getPerson().getName()))
            throw new BadRequestException();

        int baptismId = confirmation.getBaptismId();
        if(baptismId <= 0 && confirmation.getBaptismDate() == null)
            throw new BadRequestException();

        if(confirmation.getNotations() == null)
            confirmation.setNotations(emptyList());

        if(baptismId > 0) {
            Baptism b = baptismDb.getBaptism(baptismId);
            if(b == null)
                throw new BadRequestException();

            confirmation.setBaptismDate(b.getBaptismDate());
            confirmation.setBaptismLocation(b.getBaptismLocation());
        }

        try {
            db.createConfirmationRecord(confirmation);
            LOG.info("Stored confirmation record for: " + confirmation.getPerson().getName());
            return confirmation;
        } catch(WebApplicationException we) {
            throw we;
        } catch(Throwable t) {
            LOG.error("Could not create confirmation record.", t);
            throw new WebApplicationException("Could not create confirmation record.", t);
        }
    }

    @PUT @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
    public Confirmation updateConfirmationRecord(Confirmation confirmation) {
        verifyUserAccess("sacrament.confirmation.update");
        if(confirmation.getId() <= 0)
            throw new NotFoundException();

        if(confirmation.getNotations() == null)
            confirmation.setNotations(emptyList());

        try {
            Confirmation dbRecord = db.getConfirmation(confirmation.getId());
            if(dbRecord == null)
                throw new NotFoundException();

            if(changeRequiresAdmin(dbRecord, confirmation))
                verifyUserAccess("admin.confirmation.update");

            db.updateConfirmationRecord(confirmation);

            LOG.info("Updated confirmation record for: " + dbRecord.getPerson().getName());
            return confirmation;
        } catch(WebApplicationException we) {
            throw we;
        } catch(Throwable t) {
            LOG.error("Could not update confirmation record.", t);
            throw new WebApplicationException("Could not update confirmation record.", t);
        }
    }

    @POST @Path("/{id}/notation") @Consumes(MediaType.APPLICATION_JSON)
    public void addNotation(@PathParam("id") int id,
                            String notation) {
        verifyUserAccess("sacrament.confirmation.update");
        if(id <= 0)
            throw new NotFoundException();

        if(isEmpty(notation))
            throw new BadRequestException();

        try {
            Confirmation dbRecord = db.getConfirmation(id);
            if(dbRecord == null)
                throw new NotFoundException();

            dbRecord.getNotations().add(notation);
            db.updateConfirmationRecord(dbRecord);
            LOG.info("Added notification to confirmation record for: " + dbRecord.getPerson().getName());
        } catch(WebApplicationException we) {
            throw we;
        } catch(Throwable t) {
            LOG.error("Could not update confirmation record.", t);
            throw new WebApplicationException("Could not update confirmation record.", t);
        }
    }

    @DELETE @Path("/{id}") @Produces(MediaType.APPLICATION_JSON)
    public void deleteConfirmationRecord(@PathParam("id") int id) {
        verifyUserAccess("admin.confirmation.delete");
        if(id <= 0)
            throw new NotFoundException();

        try {
            Confirmation confirmation = db.getConfirmation(id);
            if (confirmation == null)
                throw new NotFoundException();

            db.delete(confirmation);
        } catch(WebApplicationException we) {
            throw we;
        } catch(Throwable t) {
            LOG.error("Could not delete confirmation record.", t);
            throw new WebApplicationException("Could not delete confirmation record.", t);
        }
    }

    // ----- Private -----
    private boolean changeRequiresAdmin(Confirmation dbRecord, Confirmation confirmation) {
        Set<String> differences = ObjectComparator.getFieldDifferences(dbRecord, confirmation);

        //Notations are the only change allowable without admin permissions
        differences.remove("getNotations");
        return differences.size()>0;
    }


}
