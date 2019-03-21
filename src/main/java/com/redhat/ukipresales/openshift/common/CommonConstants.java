package com.redhat.ukipresales.openshift.common;

public interface CommonConstants {

    String KUBERNETES_HOST_ENV = "KUBERNETES_SERVICE_HOST";
    String KUBERNETES_PORT_ENV = "KUBERNETES_SERVICE_PORT";

    String OPENSHIFT_API_ENDPOINT = "/api/v1/namespaces/%s/pods/%s";

    String DEFAULT_TOKEN_PATH = "/run/secrets/kubernetes.io/serviceaccount/token";
}