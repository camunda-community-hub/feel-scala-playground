[![Community Extension](https://img.shields.io/badge/Community%20Extension-An%20open%20source%20community%20maintained%20project-FF4700)](https://github.com/camunda-community-hub/community)
![Compatible with: Camunda Platform 8](https://img.shields.io/badge/Compatible%20with-Camunda%20Platform%208-0072Ce)
[![](https://img.shields.io/badge/Lifecycle-Incubating-blue)](https://github.com/Camunda-Community-Hub/community/blob/main/extension-lifecycle.md#incubating-)

# FEEL Tutorial Backend

This repository contains a Camunda 8 Java Spring Boot application that provides a Rest API backend
for an interactive FEEL Tutorial.

After starting the app, you should be able to access the swagger ui here:

http://<host>:<port>/swagger-ui/index.html#/process-controller/startProcessInstance

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

```shell
kubectl create namespace feel

kubectl create secret generic feel-tutorial \
--from-literal=zeebe.client.cloud.base-url=zeebe.ultrawombat.com \
--from-literal=zeebe.client.cloud.auth-url=https://login.cloud.ultrawombat.com/oauth/token \
--from-literal=zeebe.client.cloud.region=bru-2 \
--from-literal=zeebe.client.cloud.clusterId=xxx \
--from-literal=zeebe.client.cloud.clientId=xxx \
--from-literal=zeebe.client.cloud.clientSecret=xxx \
--namespace feel

kubectl apply -f app.yaml --namespace feel

kubectl port-forward svc/feel-tutorial-service 8080:8080 -n feel

```

# Clean up

To delete the pod (before redeploy):
```shell
kubectl delete pod feel-tutorial-pod -n feel
```

To completely clear out all kubernetes objects related to feel:
```shell
kubectl delete namespace feel
```





