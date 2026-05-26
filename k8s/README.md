# K8s setup

## Install 

The following notes describe how to deploy to a Google Cloud Kubernetes (GKE) cluster.

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

To deploy the rest api, edit `deployment.yaml` and set `spec.template.spec.containers[0].image` to point
to the latest docker image. Then run the following:

```shell
kubectl apply -f deployment.yaml -n feel
```

At this point, you should be able to port forward to the service. Run this:

    kubectl port-forward svc/feel-scala-playground-service 8080:8080 -n feel

## Expose the API to the internet with TLS

We use Traefik as the ingress controller and cert-manager + Let's Encrypt for automatic TLS certificates.
The public entry point is `https://feel-playground.camunda.com`.

### 1. Reserve a regional static IP

A `Service: LoadBalancer` on GKE provisions a regional Network Load Balancer, which requires a
**regional** external IP (a global IP cannot be attached). Create one in the cluster's region:

    gcloud compute addresses create feel-playground-regional-ip --region=us-east4

    gcloud compute addresses describe feel-playground-regional-ip --region=us-east4 \
      --format='value(address)'

### 2. Point DNS at the IP

Create / update an `A` record for `feel-playground.camunda.com` pointing at the reserved IP.
Wait for DNS to propagate before applying the ingress, otherwise the Let's Encrypt HTTP01
challenge will fail (cert-manager will retry, so it's not fatal — just slower).

### 3. Install cert-manager

If cert-manager isn't already on the cluster:

```shell
helm repo add jetstack https://charts.jetstack.io
helm repo update
helm install cert-manager jetstack/cert-manager \
  --namespace cert-manager --create-namespace \
  --set installCRDs=true
```

### 4. Create the Let's Encrypt ClusterIssuer

```shell
kubectl apply -f - <<'EOF'
apiVersion: cert-manager.io/v1
kind: ClusterIssuer
metadata:
  name: letsencrypt
spec:
  acme:
    email: your-email@camunda.com
    server: https://acme-v02.api.letsencrypt.org/directory
    privateKeySecretRef:
      name: letsencrypt-account-key
    solvers:
      - http01:
          ingress:
            class: traefik
EOF
```

Verify it became ready:

    kubectl get clusterissuer letsencrypt

### 5. Install Traefik

`k8s/traefik-values.yaml` is preconfigured with HTTP→HTTPS redirect, the `traefik` ingress class,
and `loadBalancerIP` pinned to the regional static IP. Update the IP if you created a different
one, then:

```shell
helm repo add traefik https://traefik.github.io/charts
helm repo update
kubectl create namespace traefik
helm install traefik traefik/traefik -n traefik -f k8s/traefik-values.yaml
```

Confirm the Service got the right external IP:

    kubectl get svc -n traefik traefik

### 6. Apply the ingress

`k8s/ingress.yaml` references the `traefik` ingress class and the `letsencrypt` ClusterIssuer,
and requests a cert for `feel-playground.camunda.com`.

    kubectl apply -f ingress.yaml -n feel

### 7. Verify

cert-manager will create a `Certificate`, solve the HTTP01 challenge via Traefik, and store the
result in the `feel-playground-tls` secret. Watch:

    kubectl get certificate -n feel -w
    kubectl describe certificate feel-playground-tls -n feel
    kubectl describe ingress feel-scala-playground-ingress -n feel

Once `READY=True`, hit the service:

    curl https://feel-playground.camunda.com/actuator/health

## Clean up

To delete the pod (before redeploy):
```shell
kubectl delete pod -l app=feel-scala-playground -n feel
```

To completely clear out all kubernetes objects related to feel:
```shell
kubectl delete namespace feel
```
