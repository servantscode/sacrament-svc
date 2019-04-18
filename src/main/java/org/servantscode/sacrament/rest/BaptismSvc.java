package org.servantscode.sacrament.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.omg.CORBA.portable.ApplicationException;
import org.servantscode.commons.rest.SCServiceBase;
import org.servantscode.sacrament.Baptism;
import org.servantscode.sacrament.db.BaptismDB;

import javax.print.attribute.standard.Media;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

@Path("/sacrament/baptism")
public class BaptismSvc extends SCServiceBase {
    private static final Logger LOG = LogManager.getLogger(BaptismSvc.class);

    private BaptismDB db;

    public BaptismSvc() {
        this.db = new BaptismDB();
    }

    @GET @Path("/{id}") @Produces(MediaType.APPLICATION_JSON)
    public Baptism getBaptismalRecord(@PathParam("id") int id) {
        verifyUserAccess("baptism.read");
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
        verifyUserAccess("baptism.read");
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
        verifyUserAccess("baptism.create");
        try {
            db.createBaptismalRecord(baptism);
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
        verifyUserAccess("baptism.update");
        if(baptism.getId() <= 0)
            throw new NotFoundException();

        try {
            Baptism dbRecord = db.getBaptism(baptism.getId());
            if(dbRecord == null)
                throw new NotFoundException();

            if(changeRequiresAdmin(dbRecord, baptism))
                verifyUserAccess("admin.baptism.update");

            db.updateBaptismalRecord(baptism);

            return baptism;
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
        Set<String> differences = Arrays.stream(Baptism.class.getMethods())
                .filter(method -> method.getName().startsWith("get") || method.getName().startsWith("is"))
                .filter(method -> {
                    try {
                        LOG.debug("Checking field: " + method.getName());
                        Object existing = method.invoke(dbRecord);
                        boolean diff = existing == null? method.invoke(baptism) == null: existing.equals(method.invoke(baptism));
                        if(diff) LOG.debug("Found change in field: " + method.getName());
                        return diff;
                    } catch (Exception e) {
                        throw new RuntimeException("Could not invoke method: " + method.getName(), e);
                    }
                })
                .map(Method::getName).collect(Collectors.toSet());

        //Notations are the only change allowable without admin permissions
        differences.remove("getNotations");
        return differences.size()>0;
    }
}
