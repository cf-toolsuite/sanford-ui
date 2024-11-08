# Sanford UI

* [How to Run with Gradle](#how-to-run-with-gradle)
  * [targeting an instance of Sanford](#targeting-an-instance-of-sanford)
* [How to run on Tanzu Platform for Cloud Foundry](#how-to-run-on-tanzu-platform-for-cloud-foundry)
  * [Target a foundation](#target-a-foundation)
  * [Authenticate](#authenticate)
  * [Target space](#target-space)
  * [Clone and build the app](#clone-and-build-the-app)
  * [Deploy](#deploy)
* [How to run on Kubernetes](#how-to-run-on-kubernetes)
  * [Build](#build)
  * [(Optional) Authenticate to a container image registry](#optional-authenticate-to-a-container-image-registry)
  * [(Optional) Push image to a container registry](#optional-push-image-to-a-container-registry)
  * [Target a cluster](#target-a-cluster)
  * [Prepare](#prepare)
  * [Apply](#apply)
  * [Setup port forwarding](#setup-port-forwarding)
  * [Teardown](#teardown)
* [How to run on Tanzu Platform for Kubernetes](#how-to-run-on-tanzu-platform-for-kubernetes)
  * [Clone this repo](#clone-this-repo)
  * [Initialize](#initialize)
    * [Configuring daemon builds](#configuring-daemon-builds)
    * [Configuring platform builds](#configuring-platform-builds)
    * [Validating build configuration](#validating-build-configuration)
  * [Deploy application](#deploy-application)
  * [Establish a domain binding](#establish-a-domain-binding)
  * [Destroy the app](#destroy-the-app)

## How to Run with Gradle

Open a terminal shell and execute

```bash
./gradlew build bootRun -Dspring.profiles.active=default,local -Dvaadin.productionMode=true
```
> Will run the application and automatically open the user interface in your favorite browser on port 8081.  If you want to run this application on the default port, 8080, then you can remove `-Dspring.profiles.active=default,local` above.

### targeting an instance of Sanford

By default the application is configured to interact with an instance of Sanford running on http://localhost:8080.

If you want to override this default, then execute with these additional environment variables, e.g.,

```bash
export DOCUMENT_SERVICE_SCHEME=https
export DOCUMENT_SERVICE_HOST=sanford.dev.jitterbug.io
export DOCUMENT_SERVICE_PORT=443
./gradlew build bootRun
```

## How to run on Tanzu Platform for Cloud Foundry

### Target a foundation

```bash
cf api {cloud_foundry_foundation_api_endpoint}
```

> Replace `{cloud_foundry_foundation_api_endpoint}` above with an API endppint

Sample interaction

```bash
cf api api.sys.dhaka.cf-app.com
```

### Authenticate

Interactively

```bash
cf login
```

With single sign-on

```bash
cf login --sso
```

With a username and password

```bash
cf login -u {username} -p "{password}"
```

> Replace `{username}` and `{password}` above respectively with your account's username and password.

### Target space

If your user account has `OrgManager` and `SpaceManager` permissions, then you can create your own organization and space with

```bash
cf create-org {organization_name}
cf create-space -o {organization_name} {space_name}
```

> Replace `{organization_name}` and `{space_name}` above with names of your design

To target a space

```bash
cf target -o {organization_name} -s {space_name}
```

> Replace `{organization_name}` and `{space_name}` above with an existing organization and space your account has access to

Sample interaction

```bash
cf create-org zoolabs
cf create-space -o zoolabs dev
cf target -o zoolabs -s dev
```

### Clone and build the app

```bash
gh repo clone cf-toolsuite/sanford-ui
cd sanford-ui
gradle build -Dvaadin.productionMode=true
```

### Deploy

Take a look at the deployment script

```bash
cat deploy-on-tp4cf.sh
```

> Make any required edits to the environment variables.

Execute the deployment script

```bash
./deploy-on-tp4cf.sh setup
```

To teardown, execute

```bash
./deploy-on-tp4cf.sh teardown
```

## How to run on Kubernetes

We're going to make use of the [Eclipse JKube Gradle plugin](https://eclipse.dev/jkube/docs/kubernetes-gradle-plugin/#getting-started)

### Build

To build a container image with Spring Boot, set the container image version, and assemble the required Kubernetes manifests for deployment, execute:

```bash
❯ gradle clean setVersion build bootBuildImage k8sResource -PnewVersion=$(date +"%Y.%m.%d") -Dvaadin.productionMode=true --stacktrace
```

This will build and tag a container image using [Paketo Buildpacks](https://paketo.io/docs/concepts/buildpacks/) and produce a collection of manifests in `build/classes/META-INF/jkube`.

### (Optional) Authenticate to a container image registry

If you are a contributor with an account that has permissions to push updates to the container image, you will need to authenticate with the container image registry.

For [DockerHub](https://hub.docker.com/), you could execute:

```bash
echo "REPLACE_ME" | docker login docker.io -u cftoolsuite --password-stdin
```

For [GitHub CR](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-container-registry#authenticating-to-the-container-registry), you could execute:

```bash
echo "REPLACE_ME" | docker login ghcr.io -u cf-toolsuite --password-stdin
```

> Replace the password value `REPLACE_ME` above with a valid personal access token to DockerHub or GitHub CR.

### (Optional) Push image to a container registry

Here's how to push an update:

```bash
gradle k8sPush
```

### Target a cluster

You will need to establish a connection context to a cluster you have access to.

```bash
export KUBECONFIG=/path/to/.kube/config
```
> Replace `/path/to/.kube/config` above with a valid path

### Prepare

Consult GitHub CR for the latest available tagged image, [here](https://github.com/cf-toolsuite/sanford-ui/pkgs/container/sanford-ui).

Edit the `build/classes/java/main/META-INF/jkube/kubernetes/sanford-ui-deployment.yml` and `build/classes/java/main/META-INF/jkube/kubernetes/sanford-ui-service.yml` files

You should replace occurrences of `YYYY.MM.DD` (e.g., 2024.10.28) with the latest available tag, and save your changes.

> Note: the image tag in Github CR will have a slightly different format (e.g., 20241106.1853.9926)

### Apply

Finally, we can deploy the application and dependent runtime services to our Kubernetes cluster.

Do so, with:

```bash
gradle k8sApply
```

or

```bash
kubectl apply -f build/classes/java/main/META-INF/jkube/kubernetes.yml
```

### Setup port forwarding

At this point you'd probably like to interact with sanford-ui, huh?  We need to setup port-forwarding, so execute:

```bash
kubectl port-forward service/sanford-ui 8080:8080
```

Then visit `http://localhost:8080/` in your favorite browser.

When you're done, revisit the terminal where you started port-forwarding and press `Ctrl+C`.

> Yeah, this only gets you so far.  For a more production-ready footprint, there's quite a bit more work involved.  But this suffices for an inner-loop development experience.

### Teardown

```bash
gradle k8sUndeploy
```

or

```bash
kubectl delete -f build/classes/java/main/META-INF/jkube/kubernetes.yml
```

## How to run on Tanzu Platform for Kubernetes

Consider this a Quick Start guide to getting `sanford-ui` deployed using the [tanzu](https://docs.vmware.com/en/VMware-Tanzu-Application-Platform/1.12/tap/install-tanzu-cli.html) CLI.

We'll be focused on a subset of commands to get the job done.  That said, you will likely need to work with a Platform Engineer within your enterprise to pre-provision an environment for your use.

This [Github gist](https://gist.github.com/pacphi/b9a7bb0f9538db1d11d1671d8a2b5566) should give you sense of how to get started with infrastructure provisioning and operational concerns, before attempting to deploy.

### Clone this repo

```bash
gh repo clone cf-toolsuite/sanford-ui
```

### Initialize

> We're going to assume that your account is a member of an organization and has appropriate access-level permissions to work with an existing project and space(s).

Login, set a project and space.

```bash
tanzu login
tanzu project list
tanzu project use AMER-West
tanzu space list
tanzu space use cphillipson-sbx
```

> You will set a different `project` and `space`.  The above is just illustrative of what you'll need to do to target where you'll deploy your own instance of this application.

Set the context for `kubectl`, just in case you need to inspect resources.

```bash
tanzu context current
```

**Sample interaction**

```bash
❯ tanzu context current
  Name:            sa-tanzu-platform
  Type:            tanzu
  Organization:    sa-tanzu-platform (77aee83b-308f-4c8e-b9c4-3f7a6f19ba75)
  Project:         AMER-West (3b65ba5e-52a4-4666-ad29-4eefab93127b)
  Space:           cphillipson-sbx
  Kube Config:     /home/cphillipson/.config/tanzu/kube/config
  Kube Context:    tanzu-cli-sa-tanzu-platform:AMER-West:cphillipson-sbx
```

Then

```bash
# Use the value after "Kube Config:"
# Likely this will work consistently for you
export KUBECONFIG=$HOME/.config/tanzu/kube/config
```

Now, let's jump into the root-level directory of the Git repository's project we cloned earlier, create a new branch, and freshly initialize Tanzu application configuration.

```bash
cd sanford-ui
git checkout -b tp4k8s-experiment
tanzu app init -y
```

We'll also need to remove any large files or sensitive configuration files.

```
du -sh * -c
./prune.sh
```

Edit the file `.tanzu/config/sanford-ui.yml`.

It should look like this after editing.  Save your work.

```yaml
apiVersion: apps.tanzu.vmware.com/v1
kind: ContainerApp
metadata:
  name: sanford-ui
spec:
  nonSecretEnv:
    - name: JAVA_TOOL_OPTIONS
      value: "-Djava.security.egd=file:///dev/urandom -XX:+UseZGC -XX:+UseStringDeduplication"
    - name: SPRING_PROFILES_ACTIVE
      value: "default,cloud"
    - name: DOCUMENT_SERVICE_SCHEME
      value: "https"
    - name: DOCUMENT_SERVICE_HOST
      value: "REPLACE_ME"
    - name: DOCUMENT_SERVICE_PORT
      value: "443"
  build:
    nonSecretEnv:
    - name: BP_JVM_VERSION
      value: "21"
    - name: BP_GRADLE_BUILD_ARGUMENTS
      value: "-Dvaadin.production.mode=true"
    buildpacks: {}
    path: ../..
  contact:
    team: cftoolsuite
  ports:
  - name: main
    port: 8080
  probes:
    liveness:
      httpGet:
        path: /actuator/health/liveness
        port: 8080
        scheme: HTTP
    readiness:
      httpGet:
        path: /actuator/health/readiness
        port: 8080
        scheme: HTTP
    startup:
      failureThreshold: 120
      httpGet:
        path: /actuator/health/readiness
        port: 8080
        scheme: HTTP
      initialDelaySeconds: 2
      periodSeconds: 2
```

> Replace the value of `REPLACE_ME` for `DOCUMENT_SERVICE_HOST` above with the URL to an instance of `sanford`.

#### Configuring daemon builds

```bash
tanzu build config \
  --build-plan-source-type ucp \
  --containerapp-registry docker.io/{contact.team}/{name} \
  --build-plan-source custom-build-plan-ingressv2  \
  --build-engine=daemon
```

Builds will be performed locally.  (Docker must be installed).  We are targeting Dockerhub as the container image registry.  If you wish to target another registry provider you would have to change the prefix value of `docker.io` above to something else.  Pay attention to `{contact.team}`.  In the `ContainerApp` resource definition above, you will have to change `cftoolsuite` to an existing repository name in your registry.

You will also have to authenticate with the registry, e.g.

```bash
export REGISTRY_USERNAME=cftoolsuite
export REGISTRY_PASSWORD=xxx
export REGISTRY_HOST=docker.io
echo $REGISTRY_PASSWORD | docker login $REGISTRY_HOST -u $REGISTRY_USERNAME --password-stdin
```

> Replace the values of `REGISTRY_` environment variables above as appropriate.

By the way, whatever container image registry provider you choose, make sure to restrict access to the repository.  If you're using DockerHub, set the visibility of your repository to private.

> If your app will work with a private registry, then your Platform Engineer will have to have had to configure the [Registry Credentials Pull Only Installer](https://www.platform.tanzu.broadcom.com/hub/application-engine/capabilities/registry-pull-only-credentials-installer.tanzu.vmware.com/details).

<details>

<summary>Working with a container registry hosted on Github</summary>

Alternatively, if you intend to use [Github](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-container-registry#authenticating-to-the-container-registry) as a container image registry provider for your repository, you could authenticate to the registry with

```bash
export REGISTRY_USERNAME=cf-toolsuite
export REGISTRY_PASSWORD=xxx
export REGISTRY_HOST=ghcr.io
echo $REGISTRY_PASSWORD | docker login $REGISTRY_HOST -u $REGISTRY_USERNAME --password-stdin
```

then update the build configuration to be

```bash
tanzu build config \
  --build-plan-source-type ucp \
  --containerapp-registry ghcr.io/{contact.team}/{name} \
  --build-plan-source custom-build-plan-ingressv2  \
  --build-engine=daemon
```

and finally, make sure that `contact.name` in the `ContainerApp` is updated to be `cf-toolsuite` which matches the organization name for the Github repository.

> The first time you build and publish the container image, if you do not want to have to configure `Registry Credentials Pull Only Installer`, you will need to visit the `Package settings`, then set the visibility of the package to `Public`.

</details>

#### Configuring platform builds

```bash
tanzu build config \
  --build-plan-source-type ucp \
  --containerapp-registry us-west1-docker.pkg.dev/fe-cpage/west-sa-build-registry/{contact.team}/{name} \
  --build-plan-source custom-build-plan-ingressv2 \
  --build-engine=platform
```

A benefit of platform builds is that they occur on-platform.  (Therefore, Docker does not need to be installed).  We will assume that a Platform Engineer has set this up on our behalf.

> You will likely need to change `us-west1-docker.pkg.dev/fe-cpage/west-sa-build-registry` above to an appropriate prefix targeting a shared container image registry.

#### Validating build configuration

For daemon builds, e.g.

```bash
❯ tanzu build config view
Using config file: /home/cphillipson/.config/tanzu/build/config.yaml
Success: Getting config
buildengine: daemon
buildPlanSource: custom-build-plan-ingressv2
buildPlanSourceType: ucp
containerAppRegistry: docker.io/{contact.team}/{name}
experimentalFeatures: false
```

### Create and publish package to container image registry repository

```bash
tanzu build -o .tanzu/build
```

### Deploy application

```bash
tanzu deploy --from-build .tanzu/build -y
```

Here's a few optional commands you could run afterwards to check on the state of deployment

```bash
tanzu app get sanford-ui
tanzu app logs sanford-ui --lines -1
```

### Establish a domain binding

```bash
tanzu domain-binding create sanford-ui --domain sanford-ui.sbx.tpk8s.cloudmonk.me --entrypoint main --port 443
```

> Replace the portion of the value of `--domain` before the application name above with your own sub-domain (or with one your Platform Engineer setup on your behalf).

### Destroy the app

```bash
tanzu app delete sanford-ui -y
```
