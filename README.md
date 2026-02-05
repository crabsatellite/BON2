BON2 - Forked with Java 17+ and Offline Support
====

A rewrite for Immibis's bearded-octo-nemesis for ForgeGradle.

This fork includes modifications to make BON2 work with modern Java (17+) and to support offline operation when mcpbot.bspk.rs is unavailable.

## Changes in this Fork

### 1. Gradle 8.5 Compatibility
- Upgraded from Gradle 2.9 to Gradle 8.5
- Updated `build.gradle` syntax (`compile` → `implementation`, etc.)
- Updated deprecated API calls

### 2. Offline mcpbot.bspk.rs Fallback
- The original mcpbot.bspk.rs server is no longer available
- Added hardcoded fallback data for MCP mappings
- Supported versions: 1.7.10, 1.8, 1.8.8, 1.8.9, 1.9, 1.9.4, 1.10.2, 1.11, 1.11.2, 1.12/1.12.2

### 3. Fixed Duplicate JAR Entry Handling
- Added Set tracking to prevent "duplicate entry" errors when processing JARs with duplicate entries

## Supported Mappings (Offline Mode)

| Minecraft Version | Stable | Snapshot |
|-------------------|--------|----------|
| 1.12/1.12.2       | 39     | 20171003 |
| 1.11.2            | 32     | 20161220 |
| 1.11              | 30, 31 | Multiple |
| 1.10.2            | 29     | 20160518 |
| 1.9.4             | 26     | 20160501 |
| 1.9               | 24     | Multiple |
| 1.8.9             | 22     | Multiple |
| 1.8.8             | 20     | 20150913 |
| 1.8               | 18     | Multiple |
| 1.7.10            | 12     | 20140925 |

## Building

```bash
./gradlew fatJar
```

The output JAR will be in `build/libs/BON-2.4.0.CUSTOM-all.jar`

## Usage

### GUI Mode
```bash
java -jar BON-2.4.0.CUSTOM-all.jar
```

### CLI Mode
```bash
java -jar BON-2.4.0.CUSTOM-all.jar --input input.jar --output output.jar --mapping stable_39
```

For 1.12.2 deobfuscation, use `stable_39` or `snapshot_20171003`.

## Original Project

Old releases available in releases tab. Newer releases available on [Jenkins](http://ci.tterrag.com/job/BON2/) (may be unavailable).
