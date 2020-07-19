package org.servantscode.sacrament.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.servantscode.commons.rest.PaginatedResponse;
import org.servantscode.commons.rest.SCServiceBase;
import org.servantscode.sacrament.Mass;
import org.servantscode.sacrament.MassIntention;
import org.servantscode.sacrament.db.MassDB;
import org.servantscode.sacrament.db.MassIntentionDB;

import javax.ws.rs.*;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/sacrament/mass")
public class MassSvc extends SCServiceBase {
    private static final Logger LOG = LogManager.getLogger(MassSvc.class);

    private MassDB db;
    private MassIntentionDB intentionDB;

    public MassSvc() {
        this.db = new MassDB();
        this.intentionDB = new MassIntentionDB();
    }

    @GET @Produces(APPLICATION_JSON)
    public PaginatedResponse<Mass> getMassAvailability(@QueryParam("start") @DefaultValue("0") int start,
                                                       @QueryParam("count") @DefaultValue("10") int count,
                                                       @QueryParam("sort_field") @DefaultValue("eventName") String sortField,
                                                       @QueryParam("search") @DefaultValue("") String search) {

        return processRequest(() -> {
            verifyUserAccess("sacrament.mass.list");

            LOG.trace(String.format("Retrieving mass details (%s, %s, page: %d; %d)", search, sortField, start, count));
            int totalAvailability = db.getCount(search);

            List<Mass> results = db.getPage(search, sortField, start, count);

            return new PaginatedResponse<>(start, results.size(), totalAvailability, results);
        });
    }

    @GET @Path("/{id}") @Produces(APPLICATION_JSON)
    public Mass getMassTime(@PathParam("id") int id) {
        return processRequest(() -> {
            verifyUserAccess("sacrament.mass.read");
            if(id <= 0)
                throw new NotFoundException();

            Mass mass = db.get(id);
            if (mass == null)
                throw new NotFoundException();
            return mass;
        });
    }

    @POST @Consumes(APPLICATION_JSON) @Produces(APPLICATION_JSON)
    public Mass createMass(Mass mass) {
        return processRequest(() -> {
            Mass existing = db.get(mass.getId());
            if(existing != null)
                throw new BadRequestException();

            return db.create(mass);
        });
    }

    @PUT @Consumes(APPLICATION_JSON) @Produces(APPLICATION_JSON)
    public Mass updateMass(Mass mass) {
        return processRequest(() -> {
            Mass existing = db.get(mass.getId());
            if(existing == null)
                throw new NotFoundException();

            return db.update(mass);
        });
    }

    @DELETE @Path("/{id}") @Produces(APPLICATION_JSON)
    public void deleteMassIntention(@PathParam("id") int id) {
        processRequest(() -> {
            verifyUserAccess("sacrament.mass.delete");
            if(id <= 0)
                throw new NotFoundException();

            Mass existing = db.get(id);
            if (existing == null)
                throw new NotFoundException();

            db.delete(existing);
        });
    }
}
