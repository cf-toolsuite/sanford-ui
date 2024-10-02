# Sanford UI

## How to Run with Gradle

Open a terminal shell and execute

```bash
./gradlew build bootRun -Dspring.profiles.active=default,local -Dvaadin.productionMode=true
```
> Will run the application and automatically open the user interface in your favorite browser on port 8081.  If you want to run this application on the default port, 8080, then you can remove `-Dspring.profiles.active=default,local` above.

### targeting an instance of Sanford

By default the application is configured to interact with an instance of Sanford running on http://localhost:8080.

If you want to override this default, then execute with this additional command-line argument

```bash
./gradlew build bootRun -DsanfordUrl=https://sanford.apps.dhaka.cf-app.com
```
> Replace the value after `-DsanfordUrl=` above with the URL where your Sanford application instance is hosted.
