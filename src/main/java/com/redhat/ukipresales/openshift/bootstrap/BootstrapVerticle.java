package com.redhat.ukipresales.openshift.bootstrap;


import com.redhat.ukipresales.openshift.api.ServiceVerticle;
import io.vertx.config.ConfigRetriever;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class BootstrapVerticle extends AbstractVerticle {

    private static final Logger log = LoggerFactory.getLogger("BootstrapVerticle");

    private static final String LOG_DEP = "Deployed %s";
    private static final String LOG_NEW_CONF = "New configuration retrieved: %n%s";

    private ConfigRetriever conf;

    private JsonObject config;

    @Override
    public void start() {
        conf = ConfigRetriever.create(vertx);

        this.deployVerticles();

        // It should use the retrieve.listen method, however it does not catch the deletion of the config map.
        // https://github.com/vert-x3/vertx-config/issues/7
        vertx.setPeriodic(5000, l -> conf.getConfig(ar -> {
            if (ar.succeeded()) {
                if (config == null || !config.encode().equals(ar.result().encode())) {
                    config = ar.result();
                    log.info(String.format(LOG_NEW_CONF, ar.result()));
                }
            }
        }));
    }

    private void deployVerticles() {
        conf.getConfig(ar -> vertx.deployVerticle(ServiceVerticle.class.getName(), new DeploymentOptions().setConfig(ar.result()), res -> {
            if (res.failed()) {
                log.error("Initialisation failed", res.cause());
            } else {
                log.info(String.format(LOG_DEP, ServiceVerticle.class.getName()));
            }
        }));
    }
}
