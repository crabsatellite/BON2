package com.github.parker8283.bon2.data;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Manages MCP mappings - supports bundled mappings, local cache, and downloading from archives.
 * 
 * Bundled mappings are included in the JAR for offline use.
 * Users can also place custom mapping files in the mappings directory.
 * 
 * Supported versions: 1.7.10 - 1.16.5 (all versions with MCP mappings before Mojang official mappings)
 */
public class MappingManager {
    
    // Known MCP mapping download URLs from Forge Maven
    // Covers all Minecraft versions from 1.7.10 to 1.16.5 (before Mojang official mappings)
    private static final Map<String, String> MAPPING_URLS = new LinkedHashMap<>();
    
    static {
        // === 1.16.x (snapshot only - last MCP versions before full Mojmap adoption) ===
        MAPPING_URLS.put("1.16.5-snapshot_20210309", "https://maven.minecraftforge.net/de/oceanlabs/mcp/mcp_snapshot/20210309-1.16.5/mcp_snapshot-20210309-1.16.5.zip");
        MAPPING_URLS.put("1.16.3-snapshot_20201028", "https://maven.minecraftforge.net/de/oceanlabs/mcp/mcp_snapshot/20201028-1.16.3/mcp_snapshot-20201028-1.16.3.zip");
        MAPPING_URLS.put("1.16.2-snapshot_20200916", "https://maven.minecraftforge.net/de/oceanlabs/mcp/mcp_snapshot/20200916-1.16.2/mcp_snapshot-20200916-1.16.2.zip");
        MAPPING_URLS.put("1.16.1-snapshot_20200723", "https://maven.minecraftforge.net/de/oceanlabs/mcp/mcp_snapshot/20200723-1.16.1/mcp_snapshot-20200723-1.16.1.zip");
        MAPPING_URLS.put("1.16-snapshot_20200514", "https://maven.minecraftforge.net/de/oceanlabs/mcp/mcp_snapshot/20200514-1.16/mcp_snapshot-20200514-1.16.zip");
        
        // === 1.15.x (snapshot only) ===
        MAPPING_URLS.put("1.15.1-snapshot_20200220", "https://maven.minecraftforge.net/de/oceanlabs/mcp/mcp_snapshot/20200220-1.15.1/mcp_snapshot-20200220-1.15.1.zip");
        
        // === 1.14.x (snapshot only) ===
        MAPPING_URLS.put("1.14.3-snapshot_20190719", "https://maven.minecraftforge.net/de/oceanlabs/mcp/mcp_snapshot/20190719-1.14.3/mcp_snapshot-20190719-1.14.3.zip");
        MAPPING_URLS.put("1.14.2-snapshot_20190608", "https://maven.minecraftforge.net/de/oceanlabs/mcp/mcp_snapshot/20190608-1.14.2/mcp_snapshot-20190608-1.14.2.zip");
        
        // === 1.13.x (snapshot only) ===
        MAPPING_URLS.put("1.13-snapshot_20180921", "https://maven.minecraftforge.net/de/oceanlabs/mcp/mcp_snapshot/20180921-1.13/mcp_snapshot-20180921-1.13.zip");
        MAPPING_URLS.put("1.13-snapshot_20180815", "https://maven.minecraftforge.net/de/oceanlabs/mcp/mcp_snapshot/20180815-1.13/mcp_snapshot-20180815-1.13.zip");
        
        // === 1.12.x (stable + snapshot) ===
        MAPPING_URLS.put("1.12.2-stable_39", "https://maven.minecraftforge.net/de/oceanlabs/mcp/mcp_stable/39-1.12/mcp_stable-39-1.12.zip");
        MAPPING_URLS.put("1.12.1-stable_39", "https://maven.minecraftforge.net/de/oceanlabs/mcp/mcp_stable/39-1.12/mcp_stable-39-1.12.zip");
        MAPPING_URLS.put("1.12-stable_39", "https://maven.minecraftforge.net/de/oceanlabs/mcp/mcp_stable/39-1.12/mcp_stable-39-1.12.zip");
        MAPPING_URLS.put("1.12-snapshot_20180814", "https://maven.minecraftforge.net/de/oceanlabs/mcp/mcp_snapshot/20180814-1.12/mcp_snapshot-20180814-1.12.zip");
        MAPPING_URLS.put("1.12-snapshot_20171003", "https://maven.minecraftforge.net/de/oceanlabs/mcp/mcp_snapshot/20171003-1.12/mcp_snapshot-20171003-1.12.zip");
        
        // === 1.11.x (stable) ===
        MAPPING_URLS.put("1.11.2-stable_32", "https://maven.minecraftforge.net/de/oceanlabs/mcp/mcp_stable/32-1.11/mcp_stable-32-1.11.zip");
        MAPPING_URLS.put("1.11-stable_32", "https://maven.minecraftforge.net/de/oceanlabs/mcp/mcp_stable/32-1.11/mcp_stable-32-1.11.zip");
        
        // === 1.10.x (stable) ===
        MAPPING_URLS.put("1.10.2-stable_29", "https://maven.minecraftforge.net/de/oceanlabs/mcp/mcp_stable/29-1.10.2/mcp_stable-29-1.10.2.zip");
        MAPPING_URLS.put("1.10-stable_29", "https://maven.minecraftforge.net/de/oceanlabs/mcp/mcp_stable/29-1.10.2/mcp_stable-29-1.10.2.zip");
        
        // === 1.9.x (stable) ===
        MAPPING_URLS.put("1.9.4-stable_26", "https://maven.minecraftforge.net/de/oceanlabs/mcp/mcp_stable/26-1.9.4/mcp_stable-26-1.9.4.zip");
        MAPPING_URLS.put("1.9-stable_24", "https://maven.minecraftforge.net/de/oceanlabs/mcp/mcp_stable/24-1.9/mcp_stable-24-1.9.zip");
        
        // === 1.8.x (stable) ===
        MAPPING_URLS.put("1.8.9-stable_22", "https://maven.minecraftforge.net/de/oceanlabs/mcp/mcp_stable/22-1.8.9/mcp_stable-22-1.8.9.zip");
        MAPPING_URLS.put("1.8.8-stable_20", "https://maven.minecraftforge.net/de/oceanlabs/mcp/mcp_stable/20-1.8.8/mcp_stable-20-1.8.8.zip");
        MAPPING_URLS.put("1.8-stable_18", "https://maven.minecraftforge.net/de/oceanlabs/mcp/mcp_stable/18-1.8/mcp_stable-18-1.8.zip");
        
        // === 1.7.x (stable - oldest supported) ===
        MAPPING_URLS.put("1.7.10-stable_12", "https://maven.minecraftforge.net/de/oceanlabs/mcp/mcp_stable/12-1.7.10/mcp_stable-12-1.7.10.zip");
    }
    
    private final File mappingsDir;
    
    public MappingManager() {
        // Default to mappings directory next to the JAR
        this.mappingsDir = getMappingsDirectory();
    }
    
    public MappingManager(File mappingsDir) {
        this.mappingsDir = mappingsDir;
    }
    
    /**
     * Get the default mappings directory.
     * Priority:
     * 1. "mappings" folder next to the JAR
     * 2. User home directory/.bon2/mappings
     */
    private static File getMappingsDirectory() {
        // Try to find mappings folder relative to JAR location
        try {
            String jarPath = MappingManager.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            File jarFile = new File(jarPath);
            File mappingsDir = new File(jarFile.getParentFile(), "mappings");
            if (mappingsDir.exists() || mappingsDir.mkdirs()) {
                return mappingsDir;
            }
        } catch (Exception e) {
            // Fall through to default
        }
        
        // Default to user home
        File homeDir = new File(System.getProperty("user.home"), ".bon2");
        File mappingsDir = new File(homeDir, "mappings");
        mappingsDir.mkdirs();
        return mappingsDir;
    }
    
    /**
     * List all available mappings (bundled + downloaded + from Gradle cache).
     */
    public List<MappingInfo> listAvailableMappings() {
        List<MappingInfo> mappings = new ArrayList<>();
        
        // Check bundled/local mappings
        if (mappingsDir.exists()) {
            for (File versionDir : mappingsDir.listFiles(File::isDirectory)) {
                File fieldsFile = new File(versionDir, "fields.csv");
                File methodsFile = new File(versionDir, "methods.csv");
                if (fieldsFile.exists() && methodsFile.exists()) {
                    mappings.add(new MappingInfo(versionDir.getName(), versionDir, MappingSource.BUNDLED));
                }
            }
        }
        
        // Check Gradle cache
        File mcpFolder = BONFiles.OCEANLABS_MCP_FOLDER;
        if (mcpFolder.exists()) {
            for (File typeFolder : mcpFolder.listFiles(f -> f.isDirectory() && f.getName().startsWith("mcp_"))) {
                for (File versionFolder : typeFolder.listFiles(File::isDirectory)) {
                    File fieldsFile = new File(versionFolder, "fields.csv");
                    File methodsFile = new File(versionFolder, "methods.csv");
                    if (fieldsFile.exists() && methodsFile.exists()) {
                        String type = typeFolder.getName().substring(4); // Remove "mcp_"
                        String name = versionFolder.getName();
                        mappings.add(new MappingInfo(type + "_" + name, versionFolder, MappingSource.GRADLE_CACHE));
                    }
                }
            }
        }
        
        return mappings;
    }
    
    /**
     * Get mapping directory for a specific version.
     * Will download if not available locally.
     */
    public File getMappingDir(String version) throws IOException {
        // First check bundled mappings
        File bundledDir = new File(mappingsDir, version);
        if (hasMappingFiles(bundledDir)) {
            System.out.println("[BON2] Using bundled mappings: " + version);
            return bundledDir;
        }
        
        // Check if version matches a downloadable mapping
        String downloadKey = findDownloadKey(version);
        if (downloadKey != null) {
            File downloadDir = new File(mappingsDir, extractVersionFromKey(downloadKey));
            if (hasMappingFiles(downloadDir)) {
                System.out.println("[BON2] Using cached mappings: " + downloadKey);
                return downloadDir;
            }
            
            // Try to download
            System.out.println("[BON2] Downloading mappings: " + downloadKey);
            if (downloadMappings(downloadKey, downloadDir)) {
                return downloadDir;
            }
        }
        
        // Fall back to Gradle cache (original behavior)
        return null;
    }
    
    private boolean hasMappingFiles(File dir) {
        if (!dir.exists()) return false;
        return new File(dir, "fields.csv").exists() && new File(dir, "methods.csv").exists();
    }
    
    private String findDownloadKey(String version) {
        // Try exact match first
        for (String key : MAPPING_URLS.keySet()) {
            if (key.contains(version) || version.contains(key.split("-")[1])) {
                return key;
            }
        }
        
        // Try partial match (e.g., "stable_39" -> "1.12.2-stable_39")
        for (String key : MAPPING_URLS.keySet()) {
            String mappingPart = key.substring(key.indexOf("-") + 1);
            if (mappingPart.equals(version) || version.contains(mappingPart.split("_")[1])) {
                return key;
            }
        }
        
        return null;
    }
    
    private String extractVersionFromKey(String key) {
        return key.substring(0, key.indexOf("-"));
    }
    
    /**
     * Download mappings from the archive.
     */
    public boolean downloadMappings(String key, File targetDir) {
        String urlStr = MAPPING_URLS.get(key);
        if (urlStr == null) {
            System.err.println("[BON2] No download URL for: " + key);
            return false;
        }
        
        try {
            targetDir.mkdirs();
            
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(30000);
            conn.setRequestProperty("User-Agent", "BON2/2.4.0");
            
            if (conn.getResponseCode() != 200) {
                System.err.println("[BON2] Failed to download: HTTP " + conn.getResponseCode());
                return false;
            }
            
            // Download and extract ZIP
            try (ZipInputStream zis = new ZipInputStream(conn.getInputStream())) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    String name = entry.getName();
                    if (name.equals("fields.csv") || name.equals("methods.csv") || name.equals("params.csv")) {
                        File outFile = new File(targetDir, name);
                        try (FileOutputStream fos = new FileOutputStream(outFile)) {
                            byte[] buffer = new byte[8192];
                            int len;
                            while ((len = zis.read(buffer)) > 0) {
                                fos.write(buffer, 0, len);
                            }
                        }
                        System.out.println("[BON2] Extracted: " + name);
                    }
                    zis.closeEntry();
                }
            }
            
            return hasMappingFiles(targetDir);
            
        } catch (Exception e) {
            System.err.println("[BON2] Download failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * List all available download keys.
     */
    public static Set<String> getAvailableDownloads() {
        return MAPPING_URLS.keySet();
    }
    
    /**
     * Download all available mappings.
     */
    public void downloadAllMappings() {
        System.out.println("[BON2] Downloading all available mappings...");
        for (String key : MAPPING_URLS.keySet()) {
            File targetDir = new File(mappingsDir, extractVersionFromKey(key));
            if (!hasMappingFiles(targetDir)) {
                downloadMappings(key, targetDir);
            } else {
                System.out.println("[BON2] Already have: " + key);
            }
        }
        System.out.println("[BON2] Done downloading mappings.");
    }
    
    public File getMappingsDir() {
        return mappingsDir;
    }
    
    /**
     * Information about an available mapping.
     */
    public static class MappingInfo {
        private final String name;
        private final File directory;
        private final MappingSource source;
        
        public MappingInfo(String name, File directory, MappingSource source) {
            this.name = name;
            this.directory = directory;
            this.source = source;
        }
        
        public String getName() { return name; }
        public File getDirectory() { return directory; }
        public MappingSource getSource() { return source; }
        
        @Override
        public String toString() {
            return name + " [" + source + "]";
        }
    }
    
    public enum MappingSource {
        BUNDLED,      // Included with BON2
        GRADLE_CACHE, // From Gradle cache
        DOWNLOADED    // Downloaded from archive
    }
}
