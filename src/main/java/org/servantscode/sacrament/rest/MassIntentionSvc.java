package org.servantscode.sacrament.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.servantscode.commons.EnumUtils;
import org.servantscode.commons.rest.PaginatedResponse;
import org.servantscode.commons.rest.SCServiceBase;
import org.servantscode.sacrament.Baptism;
import org.servantscode.sacrament.MassIntention;
import org.servantscode.sacrament.db.BaptismDB;
import org.servantscode.sacrament.db.MassIntentionDB;
import org.servantscode.sacrament.util.ObjectComparator;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Set;

import static java.util.Collections.emptyList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.servantscode.commons.StringUtils.isEmpty;

@Path("/sacrament/mass/intention")
public class MassIntentionSvc extends SCServiceBase {
    private static final Logger LOG = LogManager.getLogger(MassIntentionSvc.class);

    private MassIntentionDB db;

    public MassIntentionSvc() {
        this.db = new MassIntentionDB();
    }

    @GET @Produces(APPLICATION_JSON)
    public PaginatedResponse<MassIntention> getMassIntentions(@QueryParam("start") @DefaultValue("0") int start,
                                                              @QueryParam("count") @DefaultValue("10") int count,
                                                              @QueryParam("sort_field") @DefaultValue("id") String sortField,
                                                              @QueryParam("partial_name") @DefaultValue("") String search) {

        verifyUserAccess("sacrament.mass.intention.list");

        try {
            LOG.trace(String.format("Retrieving mass intentions (%s, %s, page: %d; %d)", search, sortField, start, count));
            int totalPeople = db.getCount(search);

            List<MassIntention> results = db.getMassIntentions(search, sortField, start, count);

            return new PaginatedResponse<>(start, results.size(), totalPeople, results);
        } catch (Throwable t) {
            LOG.error("Retrieving mass intentions failed:", t);
            throw t;
        }
    }

    @GET @Path("/{id}") @Produces(APPLICATION_JSON)
    public MassIntention getMassIntention(@PathParam("id") int id) {
        verifyUserAccess("sacrament.mass.intention.read");
        if(id <= 0)
            throw new NotFoundException();

        try {
            MassIntention intention = db.getMassIntention(id);
            if (intention == null)
                throw new NotFoundException();
            return intention;
        } catch(Throwable t) {
            LOG.error("Could not retrieve mass intention.", t);
            throw new WebApplicationException("Could not retrieve mass intention.", t);
        }
    }

    @POST @Consumes(APPLICATION_JSON) @Produces(APPLICATION_JSON)
    public MassIntention createMassIntention(MassIntention intention) {
        verifyUserAccess("sacrament.mass.intention.create");
        if(intention.getPerson() == null || isEmpty(intention.getPerson().getName()))
            throw new BadRequestException();

        if(intention.getRequester() == null || isEmpty(intention.getRequester().getName()))
            throw new BadRequestException();

        if(intention.getEventId() <= 0 || intention.getIntentionType() == null)
            throw new BadRequestException();

        try {
            db.createMassIntention(intention);
            LOG.info(String.format("Stored mass intention for: %s requeted by: %s", intention.getPerson().getName(), intention.getRequester().getName()));
            return intention;
        } catch(WebApplicationException we) {
            throw we;
        } catch(Throwable t) {
            LOG.error("Could not create mass intention.", t);
            throw new WebApplicationException("Could not create mass intention.", t);
        }
    }

    @PUT @Consumes(APPLICATION_JSON) @Produces(APPLICATION_JSON)
    public MassIntention updateMassIntention(MassIntention intention) {
        verifyUserAccess("sacrament.mass.intention.update");
        if(intention.getId() <= 0)
            throw new NotFoundException();

        if(intention.getPerson() == null || isEmpty(intention.getPerson().getName()))
            throw new BadRequestException();

        if(intention.getRequester() == null || isEmpty(intention.getRequester().getName()))
            throw new BadRequestException();

        if(intention.getEventId() <= 0 || intention.getIntentionType() == null)
            throw new BadRequestException();

        try {
            MassIntention dbIntention = db.getMassIntention(intention.getId());
            if(dbIntention == null)
                throw new NotFoundException();

            db.updateMassIntention(intention);

            LOG.info(String.format("Updated mass intention for: %s requeted by %s", dbIntention.getPerson().getName(), dbIntention.getRequester().getName()));
            return intention;
        } catch(WebApplicationException we) {
            throw we;
        } catch(Throwable t) {
            LOG.error("Could not update mass intention.", t);
            throw new WebApplicationException("Could not update mass intention.", t);
        }
    }

    @DELETE @Path("/{id}") @Produces(APPLICATION_JSON)
    public void deleteMassIntention(@PathParam("id") int id) {
        verifyUserAccess("admin.sacrament.mass.intention.delete");
        if(id <= 0)
            throw new NotFoundException();

        try {
            MassIntention intention = db.getMassIntention(id);
            if (intention == null)
                throw new NotFoundException();

            db.delete(intention);
        } catch(WebApplicationException we) {
            throw we;
        } catch(Throwable t) {
            LOG.error("Could not delete mass intention.", t);
            throw new WebApplicationException("Could not delete mass intention.", t);
        }
    }

    @GET @Path("/types") @Produces(APPLICATION_JSON)
    public List<String> getIntentionTypes() {
        return EnumUtils.listValues(MassIntention.IntentionType.class);
    }
}
