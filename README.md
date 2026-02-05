# BON2 - Forked with Java 17+ and Offline Support

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

### 4. Fixed Version Parsing for Gradle Cache Format

- Fixed `getVersionFor()` to correctly parse `39-1.12` folder format (extracts `39`, not `1.12`)

### 5. Mapping Download and Management (NEW!)

- Added `--download` command to download MCP mappings from Forge Maven
- Added `--list` command to list all available mappings
- Added `--mappingsDir` to use custom mapping directory
- Mappings are downloaded to `mappings/` folder next to the JAR
- Users can add custom mapping files for unsupported versions

## Available Mappings for Download

| Minecraft Version | Mapping Key           |
| ----------------- | --------------------- |
| 1.12.2            | 1.12.2-stable_39      |
| 1.12.2            | 1.12.2-snapshot_20171003 |
| 1.11.2            | 1.11.2-stable_32      |
| 1.10.2            | 1.10.2-stable_29      |
| 1.9.4             | 1.9.4-stable_26       |
| 1.8.9             | 1.8.9-stable_22       |
| 1.7.10            | 1.7.10-stable_12      |

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

### Download Mappings First (Recommended for new users)

```bash
# Download all available mappings
java -jar BON-2.4.0.CUSTOM-all.jar --download

# Or download specific version
java -jar BON-2.4.0.CUSTOM-all.jar --download --mappingsVer 1.12.2-stable_39

# List available mappings
java -jar BON-2.4.0.CUSTOM-all.jar --list
```

### CLI Mode - Deobfuscate a Mod

```bash
# Using downloaded mappings (auto-detected)
java -jar BON-2.4.0.CUSTOM-all.jar --inputJar input.jar --outputJar output.jar --mappingsVer 1.12.2

# Using Gradle cache mappings
java -jar BON-2.4.0.CUSTOM-all.jar --inputJar input.jar --outputJar output.jar --mappingsVer stable_39

# Using custom mapping directory
java -jar BON-2.4.0.CUSTOM-all.jar --inputJar input.jar --outputJar output.jar --mappingsDir ./my-mappings --mappingsVer custom
```

## Custom Mappings

You can add custom mapping files for versions not available for download:

1. Create a folder in `mappings/` directory (e.g., `mappings/1.6.4/`)
2. Add `fields.csv` and `methods.csv` files
3. Use with `--mappingsDir mappings/1.6.4`

### Mapping File Format

**fields.csv:**
```csv
searge,name,side,desc
field_70170_p,worldObj,2,
field_70165_t,posX,2,
```

**methods.csv:**
```csv
searge,name,side,desc
func_70003_b,shouldExecute,2,
func_70037_a,readFromNBT,2,
```

## Original Project

- Original BON2 by [Parker8283](https://github.com/Parker8283/BON2)
- Old releases available in releases tab
- Jenkins builds at [ci.tterrag.com](http://ci.tterrag.com/job/BON2/) (may be unavailable)

## License

This project is licensed under the same terms as the original BON2 project.
