# egress-locator

## About
Simple query service to locate the appropriate egress router for your Pod. Created by the Cloud App Generator

## Usage
* Create an application configuration file in YAML format which describes the nodes in your cluster, and their mapping to an egress router:

```yaml
---
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

* 


