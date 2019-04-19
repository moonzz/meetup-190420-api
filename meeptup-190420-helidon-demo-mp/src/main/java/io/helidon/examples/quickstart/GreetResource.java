/*
 * Copyright (c) 2018, 2019 Oracle and/or its affiliates. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.helidon.examples.quickstart;

import java.util.Collections;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * A simple JAX-RS resource to g 
 * Get default greeting message:
 * curl -X GET http://localhost: 
 * Get greeting message for Joe:
 * curl -X GET htt 
 * 
 * Change greeting
 * curl -X PUT -H "Content-Type: application/json" -d '{"greeting" : "Howdy"}' http://localhost:8080/greet/greeting
 *
 * The message is returned as a JSON object.
 */
@Path("/greet")
@RequestScoped
public class GreetResource {

    private static final JsonBuilderFactory JSON = Json.createBuilderFactory(Collections.emptyMap());

    private static final Logger logger = Logger.getLogger("io.helidon.microprofile.helloworld.level");

    /**
     * The greeting message provider.
     */
    private final GreetingProvider greetingProvider;
    private final String appName;
    private FaultTolerance faultTolerance;
    /**
     * Using constructor injection to get a configuration property.
     * By default this gets the value from META-INF/microprofile-config
     *
     * @param greetingConfig the configured greeting message
     */
    @Inject
    public GreetResource(GreetingProvider greetingConfig, FaultTolerance faultTolerance, @ConfigProperty(name = "app.name") String appName) {
        this.greetingProvider = greetingConfig;
        this.appName = appName;
        this.faultTolerance = faultTolerance;
    }

    /**
     * Return a wordly greeting message.
     *
     * @return {@link JsonObject}
     */
    @SuppressWarnings("checkstyle:designforextension")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getDefaultMessage() {
        logger.info("appName : " + appName);
        return createResponse("World");
    }

    /**
     * Return a greeting message using the name that was provided.
     *
     * @param name the name to greet
     * @return {@link JsonObject}
     */
    @SuppressWarnings("checkstyle:designforextension")
    @Path("/{name}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getMessage(@PathParam("name") String name) {
        return createResponse(name);
    }

    /**
     * Set the greeting to use in future messages.
     *
     * @param jsonObject JSON containing the new greeting
     * @return {@link Response}
     */
    @SuppressWarnings("checkstyle:designforextension")
    @Path("/greeting")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateGreeting(JsonObject jsonObject) {

        if (!jsonObject.containsKey("greeting")) {
            JsonObject entity = JSON.createObjectBuilder()
                    .add("error", "No greeting provided")
                    .build();
            return Response.status(Response.Status.BAD_REQUEST).entity(entity).build();
        }

        String newGreeting = jsonObject.getString("greeting");

        greetingProvider.setMessage(newGreeting);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @SuppressWarnings("checkstyle:designforextension")
    @Path("/fault-tolerance")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject faultTolerance() {

        String message = faultTolerance.faultTolerance();

        JsonObject jsonObject = JSON.createObjectBuilder().add("message", message).build();

        return jsonObject;
    }
    
    private JsonObject createResponse(String who) {
        String msg = String.format("%s %s!", greetingProvider.getMessage(), who);

        return JSON.createObjectBuilder()
                .add("message", msg)
                .build();
    }
}
