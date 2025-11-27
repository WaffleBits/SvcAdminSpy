# SvcAdminSpy

Simple Voice Chat admin tools — hear everyone (spy) and broadcast globally (broadcast).

Features
- Toggle hear-only spy mode with `/vcspy` (admins hear all players, do not broadcast).
- Toggle global broadcast mode with `/vcbroadcast` (admins' voice is broadcast to all SVC-connected players).

Installation
- Place `SvcAdminSpy-1.0.2.jar` into your server `plugins/` folder.
- Ensure `voicechat` (Simple Voice Chat) is installed in `plugins/`.
- Start or restart the server.

Commands & Permissions
- `/vcspy` — Toggle hear-only spy mode. Permission: `svcspy.use` (default: OP).
- `/vcbroadcast` — Toggle broadcast mode. Permission: `svcspy.use` (default: OP).

Compatibility
- Declared `api-version: 1.20` for compatibility with Purpur/Spigot 1.20.x and loads on 1.21.x as well. Test on your target server.
- Built against `voicechat-api:2.6.0` (server may run compatible 2.6.x releases).

Building
Requirements: JDK 17+, Gradle (or use the Gradle wrapper).

From project root:
```
gradle clean build
```

Output
- Jar: `build/libs/SvcAdminSpy-1.0.2.jar`

Releases
- For Modrinth/Distributions: upload the built jar as a Release asset rather than committing large distribution files to the repo.

Notes
- Do not commit `gradle-*-bin.zip`, `build/` or heavy dependencies; use `.gitignore` and GitHub Releases or Git LFS for large assets.