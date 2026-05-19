# Contributing guide

## Install on K8s

The following notes describe how to deploy to Google Cloud Kubernetes Cluster

First, do some setup (this should only be needed as a "one time" setup)

```shell
cd k8s

kubectl create namespace feel

kubectl apply -f service.yaml -n feel
```

To deploy the secrets, edit `secret.yaml` and set `mixpanel_project_token` to your Mixpanel project token. Then run the following:

```shell
kubectl apply -f secret.yaml -n feel
```

To deploy the rest api, edit `deployment.yaml` and set `spec.template.spec.containers[0].img` to point
to the latest docker image. Then run the following:

```shell
kubectl apply -f deployment.yaml -n feel
```

At this point, you should be able to port forward to the service. Run this:

    kubectl port-forward svc/feel-scala-playground-service 8080:8080 -n feel

To setup load balancing, we need a static ip address. Here's the gcloud command to create an ip address.

First, check if the ip address already exists:

    gcloud compute addresses describe feel-scala-playground-ip --global
    # 34.111.164.116

If needed, create a new static ip like so:

    gcloud compute addresses create feel-scala-playground-ip --global

To setup ssl, first edit `managedCert.yaml` and update the domain names you'd like to use, then create a managed
certificate object by running this:

    kubectl apply -f managedCert.yaml -n feel

Finally, create the ingress. The ingress is configured to use the static ip and managed certificate.

    kubectl apply -f ingress.yaml -n feel

Note: currently the app responds with a 404 at the root url `/`. Google Load balancer will think the app is not
responding. So, after the ingress starts, go to the google console, search for "Load Balancers", and click on the load balancer
for this feel-tutorial. Click on the Health Check and edit the path to be `/actuator/health`.

Check on status of ingress and ssl certificate:

    kubectl describe ingress feel-scala-playground-ingress -n feel
    kubectl describe managedcertificate feel-service-managed-cert -n feel

## Clean up

To delete the pod (before redeploy):
```shell
kubectl delete pod feel-scala-playground-pod -n feel
```

To completely clear out all kubernetes objects related to feel:
```shell
kubectl delete namespace feel
```
