package org.servantscode.sacrament.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.omg.CORBA.portable.ApplicationException;
import org.servantscode.commons.rest.SCServiceBase;
import org.servantscode.sacrament.Baptism;
import org.servantscode.sacrament.db.BaptismDB;
import org.servantscode.sacrament.util.ObjectComparator;

import javax.print.attribute.standard.Media;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.servantscode.commons.StringUtils.isEmpty;

@Path("/sacrament/baptism")
public class BaptismSvc extends SCServiceBase {
    private static final Logger LOG = LogManager.getLogger(BaptismSvc.class);

    private BaptismDB db;

    public BaptismSvc() {
        this.db = new BaptismDB();
    }

    @GET @Path("/{id}") @Produces(MediaType.APPLICATION_JSON)
    public Baptism getBaptismalRecord(@PathParam("id") int id) {
        verifyUserAccess("sacrament.baptism.read");
        if(id <= 0)
            throw new NotFoundException();

        try {
            Baptism baptism = db.getBaptism(id);
            if (baptism == null)
                throw new NotFoundException();
            return baptism;
        } catch(Throwable t) {
            LOG.error("Could not retrieve baptismal record.", t);
            throw new WebApplicationException("Could not retrieve baptismal record.", t);
        }
    }

    @GET @Path("/person/{id}") @Produces(MediaType.APPLICATION_JSON)
    public Baptism getBaptismalRecordForPerson(@PathParam("id") int personId) {
        verifyUserAccess("sacrament.baptism.read");
        if(personId <= 0)
            throw new NotFoundException();

        try {
            Baptism baptism;
            baptism = db.getBaptismByPerson(personId);
            if (baptism == null)
                throw new NotFoundException();
            return baptism;
        } catch(WebApplicationException we) {
            throw we;
        } catch(Throwable t) {
            LOG.error("Could not retrieve baptismal record.", t);
            throw new WebApplicationException("Could not retrieve baptismal record.", t);
        }
    }

    @POST @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
    public Baptism createBaptismalRecord(Baptism baptism) {
        verifyUserAccess("sacrament.baptism.create");
        if(baptism.getPerson() == null || isEmpty(baptism.getPerson().getName()))
            throw new BadRequestException();

        if(baptism.getNotations() == null)
            baptism.setNotations(emptyList());

        try {
            db.createBaptismalRecord(baptism);
            LOG.info("Stored baptismal record for: " + baptism.getPerson().getName());
            return baptism;
        } catch(WebApplicationException we) {
            throw we;
        } catch(Throwable t) {
            LOG.error("Could not create baptismal record.", t);
            throw new WebApplicationException("Could not create baptismal record.", t);
        }
    }

    @PUT @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
    public Baptism updateBaptismalRecord(Baptism baptism) {
        verifyUserAccess("sacrament.baptism.update");
        if(baptism.getId() <= 0)
            throw new NotFoundException();

        if(baptism.getNotations() == null)
            baptism.setNotations(emptyList());

        try {
            Baptism dbRecord = db.getBaptism(baptism.getId());
            if(dbRecord == null)
                throw new NotFoundException();

            if(changeRequiresAdmin(dbRecord, baptism))
                verifyUserAccess("admin.baptism.update");

            db.updateBaptismalRecord(baptism);

            LOG.info("Updated baptismal record for: " + dbRecord.getPerson().getName());
            return baptism;
        } catch(WebApplicationException we) {
            throw we;
        } catch(Throwable t) {
            LOG.error("Could not update baptismal record.", t);
            throw new WebApplicationException("Could not update baptismal record.", t);
        }
    }

    @POST @Path("/{id}/notation") @Consumes(MediaType.APPLICATION_JSON)
    public void addNotation(@PathParam("id") int id,
                            String notation) {
        verifyUserAccess("sacrament.baptism.update");
        if(id <= 0)
            throw new NotFoundException();

        if(isEmpty(notation))
            throw new BadRequestException();

        try {
            Baptism dbRecord = db.getBaptism(id);
            if(dbRecord == null)
                throw new NotFoundException();

            dbRecord.getNotations().add(notation);
            db.updateBaptismalRecord(dbRecord);
            LOG.info("Added notification to baptismal record for: " + dbRecord.getPerson().getName());
        } catch(WebApplicationException we) {
            throw we;
        } catch(Throwable t) {
            LOG.error("Could not update baptismal record.", t);
            throw new WebApplicationException("Could not update baptismal record.", t);
        }
    }

    @DELETE @Path("/{id}") @Produces(MediaType.APPLICATION_JSON)
    public void deleteBaptismalRecord(@PathParam("id") int id) {
        verifyUserAccess("admin.baptism.delete");
        if(id <= 0)
            throw new NotFoundException();

        try {
            Baptism baptism = db.getBaptism(id);
            if (baptism == null)
                throw new NotFoundException();

            db.delete(baptism);
        } catch(WebApplicationException we) {
            throw we;
        } catch(Throwable t) {
            LOG.error("Could not delete baptismal record.", t);
            throw new WebApplicationException("Could not delete baptismal record.", t);
        }
    }

    // ----- Private -----
    private boolean changeRequiresAdmin(Baptism dbRecord, Baptism baptism) {
        Set<String> differences = ObjectComparator.getFieldDifferences(dbRecord, baptism);

        //Notations are the only change allowable without admin permissions
        differences.remove("getNotations");
        return differences.size()>0;
    }
}
