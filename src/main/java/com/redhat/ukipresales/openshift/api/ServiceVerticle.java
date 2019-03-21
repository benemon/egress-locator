package com.redhat.ukipresales.openshift.api;

import io.vertx.config.ConfigRetriever;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.healthchecks.Status;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.util.Optional;

/**
 *
 */
public class ServiceVerticle extends AbstractVerticle {

    private static final Logger log = LoggerFactory.getLogger("ServiceVerticle");

    private boolean online = false;
    private JsonObject config;


    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);
    }

    @Override
    public void start(Future<Void> future) {
        config = config();
        ConfigRetriever conf = ConfigRetriever.create(vertx);

        conf.listen(updates -> {
            config = updates.getNewConfiguration();
        });

        HealthCheckHandler healthCheckHandler = HealthCheckHandler.create(vertx)
                .register("server-online", fut -> fut.complete(online ? Status.OK() : Status.KO()));

        Router router = Router.router(vertx);

        router.get("/api/health/readiness").handler(rc -> rc.response().end("OK"));
        router.get("/api/health/liveness").handler(healthCheckHandler);
        router.get("/api/egress/:nodeName/").handler(this::egressLocationRequestByNodeName);

        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(
                        config().getInteger("http.port", 8080), ar -> {
                            online = ar.succeeded();
                            future.handle(ar.mapEmpty());
                        });
    }


    /**
     * Determine egress location without using OpenShift API
     *
     * @param rc
     */
    private void egressLocationRequestByNodeName(RoutingContext rc) {
        String nodeName = rc.request().getParam("nodeName");
        rc.response().setStatusCode(200).end(this.getEgressForNode(nodeName));
    }

    /**
     * Based on the mapping in the process configuration, return an egress point for a given node
     *
     * @param nodeName
     * @return egress service
     */
    private String getEgressForNode(String nodeName) {
        JsonArray nodeGroups = config.getJsonArray("nodeGroups");
        Optional<JsonObject> nodeGroup = nodeGroups.stream().filter(
                ng -> ng instanceof JsonObject).map(ng -> (JsonObject) ng).filter(
                ng -> ng.getJsonArray("hosts").getList().stream().map(h -> h).anyMatch(h -> ((String) h).equalsIgnoreCase(nodeName))).findFirst();


        return nodeGroup.map(ng -> ng.getString("egress")).orElseThrow(RuntimeException::new);
    }


}