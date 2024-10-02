# Sanford UI

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

## How to run on Cloud Foundry

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

TBD