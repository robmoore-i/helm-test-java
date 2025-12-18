# Snapshot release process

Load secrets:
```
source private/release/set-env.sh
```

Run publish Gradle task:
```
./gradlew :library:publishToMavenCentral
```

Clear secrets:
```
source private/release/clear-env.sh
```

See `example/` for an example of a project that uses a published snapshot. 
