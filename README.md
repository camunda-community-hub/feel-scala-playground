[![Community Extension](https://img.shields.io/badge/Community%20Extension-An%20open%20source%20community%20maintained%20project-FF4700)](https://github.com/camunda-community-hub/community)
![Compatible with: Camunda Platform 8](https://img.shields.io/badge/Compatible%20with-Camunda%20Platform%208-0072Ce)
[![](https://img.shields.io/badge/Lifecycle-Incubating-blue)](https://github.com/Camunda-Community-Hub/community/blob/main/extension-lifecycle.md#incubating-)

# FEEL Tutorial Backend

This repository contains a Camunda 8 Java Spring Boot application that provides a Rest API backend
for an interactive FEEL Tutorial.

After starting the app, you should be able to access the swagger ui here:

http://<host>:<port>/swagger-ui/index.html#/process-controller/startProcessInstance

http://feel.upgradingdave.com/swagger-ui/index.html#/process-controller/startProcessInstance

# (Re) Create Docker Image

```shell
docker buildx create --use
docker buildx build --platform linux/amd64,linux/arm64 --push -t upgradingdave/feel-tutorial:main .

docker run -d -t -i -e ZEEBE_BASE_URL='zeebe.ultrawombat.com' \
-e ZEEBE_AUTH_URL='https://login.cloud.ultrawombat.com/oauth/token' \
-e ZEEBE_REGION=bru-2 \
-e ZEEBE_CLUSTER_ID='xxx' \
-e ZEEBE_CLIENT_ID='xxx' \
-e ZEEBE_CLIENT_SECRET='xxx' \
-p 8080:8080 \
--name feel-tutorial upgradingdave/feel-tutorial:main

```

# Test in Docker Compose

```shell
docker-compose -f ./docker-compose.feel-tutorial.yml up -d --build
```

# Deploy to Kubernetes Cluster

The following commands do some setup and should only be needed as a "one time" setup

```shell
cd k8s

kubectl create namespace feel

kubectl create secret generic feel-tutorial \
--from-literal=zeebe.client.cloud.base-url=zeebe.ultrawombat.com \
--from-literal=zeebe.client.cloud.auth-url=https://login.cloud.ultrawombat.com/oauth/token \
--from-literal=zeebe.client.cloud.region=bru-2 \
--from-literal=zeebe.client.cloud.clusterId=xxx \
--from-literal=zeebe.client.cloud.clientId=xxx \
--from-literal=zeebe.client.cloud.clientSecret=xxx \
--namespace feel

kubectl apply -f service.yaml -n feel
```

To deploy a new version or the rest api, edit `deployment.yaml` and set `spec.template.spec.containers[0].img` to point
to the latest docker image. Then run the following:

```shell
kubectl apply -f deployment.yaml -n feel
```

At this point, you should be able to port forward to the service. Run this:

    kubectl port-forward svc/feel-tutorial-service 8080:8080 -n feel

Then try accessing the service here: http://localhost:8080/swagger-ui/index.html#/process-controller/startProcessInstance

To setup load balancing, we need a static ip address. Here's the gcloud command to create an ip address.

Note that there are subtle differences between regional vs global ip addresses that I still don't fully understand!

First, check if the ip address already exists:

    gcloud compute addresses describe feel-tutorial-ip --global
    # 34.111.164.116

If needed, create a new static ip like so:

    gcloud compute addresses create feel-tutorial-ip --global

To setup ssl, first edit `managedCert.yaml` and update the domain names you'd like to use, then create a managed
certificate object by running this:

    kubectl apply -f managedCert.yaml -n feel

Finally, create the ingress. The ingress is configured to use the static ip and managed certificate.

    kubectl apply -f ingress.yaml -n feel

Note: currently the app responds with a 404 at the root url `/`. Google Load balancer will think the app is not
responding. So, after the ingress starts, go to the google console, search for "Load Balancers", and click on the load balancer
for this feel-tutorial. Click on the Health Check and edit the path to be `/actuator/health`.

Check on status of ingress and ssl certificate:

    kubectl describe ingress feel-tutorial-ingress -n feel
    kubectl describe managedcertificate managed-cert -n feel

# Clean up

To delete the pod (before redeploy):
```shell
kubectl delete pod feel-tutorial-pod -n feel
```

To completely clear out all kubernetes objects related to feel:
```shell
kubectl delete namespace feel
```





