# Release process

## Library

```
bash private/release/library/publish-snapshot-release.sh
```

Alternatively,

```
bash private/release/library/publish-promoted-release.sh
```

## Plugin

```
bash private/release/plugin/publish.sh
```

## Post-release

- Edit and finalize the release notes in the `release-notes` directory, move them to `release-notes/.../released`, and then create a new release notes file for the next release.
- Update the version numbers in `gradle.properties`.
- Update the version numbers in `gradle/plugins/gradle-plugin/build.gradle.kts`.
- Use the released version numbers in the examples in `README.md`.
