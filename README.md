# egress-locator

## About
Simple query service to locate the appropriate egress router for your Pod. Created by the Cloud App Generator

## Usage

It is best to run this service in a global project, so that you can use internal DNS to resolve it. This reduces load on the HAProxy instances, and prevents requests hairpinning out of the cluster only to come back in again.

* Create an application configuration file in YAML format which describes the nodes in your cluster, and their mapping to an egress router:

```yaml
nodeGroups:
  -
    egress: fake-egress-apps-a.example.com
    hosts:
      - node-apps-1.example.com
      - node-apps-3.example.com
    nodeGroup: siteA
  -
    egress: fake-egress-apps-b.example.com
    hosts:
      - node-apps-2.example.com
      - node-apps-4.example.com
    nodeGroup: siteB

```

* Create a configmap from this YAML file:

`oc create configmap egress-locator-conf --from-file=conf/application.yml`

* Do an S2I build of the codebase:

`oc new-build redhat-openjdk18-openshift:1.4~https://github.com/benemon/egress-locator`

`oc start-build egress-locator`

* Create the application from the resulting build:

`oc new-app egress-locator:latest`

* Expose the Service if necessary.

`oc expose svc/egress-locator`
 
* add the ConfigMap to the resulting image's DeploymentConfig:

```yaml
        - env:
            - name: VERTX_CONFIG_PATH
              value: /deployments/conf/application.yml
        ...
          volumeMounts:
            - mountPath: /deployments/conf/
              name: volume-enprm
        ...
        - volumes:
            - configMap:
                defaultMode: 420
                items:
                    - key: application.yml
                      path: application.yml
                name: egress-locator-conf
              name: volume-enprm      
```

* Scale up to *n* replicas to provide resiliency in your environment

* Once deployed, the ConfigMap can be edited on the fly, and the changes will take effect without a Service restart. The refresh can take up to 30 seconds.

