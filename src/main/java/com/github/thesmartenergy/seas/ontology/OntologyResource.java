/*
 * Copyright 2016 ITEA 12004 SEAS Project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.thesmartenergy.seas.ontology;

import com.github.thesmartenergy.seas.App;
import com.github.thesmartenergy.seas.entities.OntologyVersion;
import java.io.FileInputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import static org.apache.jena.vocabulary.RDF.uri;

/**
 *
 * @author maxime.lefrancois
 */
@Path("")
public class OntologyResource {

    private static final Logger LOG = Logger.getLogger(OntologyResource.class.getSimpleName());

    @Inject
    HttpServletRequest request;

    @Inject
    App app; 

    @GET
    @Produces("text/html; qs=0.9")
    @Path("{ontoName : [a-zA-Z]*}/{major: [0-9]+}.{minor: [0-9]+}")
    public Response getAsHtml(@PathParam("ontoName") String ontoName, @PathParam("major") int major, @PathParam("minor") int minor ) {
        OntologyVersion version = app.getVersion(ontoName, major, minor);
        if (version == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        String requestedUri = app.getBase() + ontoName;
        try {
            return Response.seeOther(new URI("http://vowl.visualdataweb.org/webvowl/#iri=" + requestedUri)).build();
        } catch (URISyntaxException ex) {
            Logger.getLogger(OntologyResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Produces("text/turtle")
    @Path("{ontoName : [a-zA-Z]*}/{major: [0-9]+}.{minor: [0-9]+}")
    public Response getAsTurtle(@PathParam("ontoName") String ontoName, @PathParam("major") int major, @PathParam("minor") int minor ) {
        OntologyVersion version = app.getVersion(ontoName, major, minor);
        if (version == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        try {
            Response.ResponseBuilder res = Response.ok(IOUtils.toString(new FileInputStream(version.getFile())), "text/turtle");
            String filename = ontoName + "-" + major + "." + minor + ".ttl;";
            if(!ontoName.equals("seas")) {
                filename = "seas-" + filename;
            }
            res.header("Content-Disposition", "filename= "+filename);
            return res.build(); 
        } catch (Exception ex) {
            Logger.getLogger(OntologyResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Produces("application/rdf+xml")
    @Path("{ontoName : [a-zA-Z]*}/{major: [0-9]+}.{minor: [0-9]+}")
    public Response getAsXML(@PathParam("ontoName") String ontoName, @PathParam("major") int major, @PathParam("minor") int minor ) {
        OntologyVersion version = app.getVersion(ontoName, major, minor);
        if (version == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        try {
            Model model = ModelFactory.createDefaultModel().read(new FileInputStream(version.getFile()), uri, "TTL");

            StringWriter sw = new StringWriter();
            model.write(sw);
            Response.ResponseBuilder res = Response.ok(sw.toString(), "application/rdf+xml");
            String filename = ontoName + "-" + major + "." + minor + ".rdf;";
            if(!ontoName.equals("seas")) {
                filename = "seas-" + filename;
            }
            res.header("Content-Disposition", "filename= "+filename);
            return res.build();
        } catch (Exception ex) {
            Logger.getLogger(OntologyResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } 
    }

}
