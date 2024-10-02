# Sanford UI

## How to Build

```bash
./gradlew clean build
```

This builds a version of the user-interface that is optimized for iterative development.


### Alternatives

By default Vaadin compiles in developer mode.  If we want to package the application in production mode, you will need to execute:

```bash
./gradlew clean build -Dvaadin.productionMode=true
```