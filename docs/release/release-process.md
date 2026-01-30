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
- Use the just-released version number in the examples in `README.md`.
- Use the just-released version number in the `example` project.
- Bump the version number in the project build script for the library and the plugin.
