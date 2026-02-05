package com.github.parker8283.bon2.data;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

/**
 * Manages common library downloads for mod decompilation/compilation.
 * 
 * Libraries are downloaded from Maven Central to the libs/ directory.
 * Users can edit libs.txt to add custom library coordinates.
 */
public class LibraryManager {
    
    // Common libraries used in Minecraft modding (Maven coordinates: groupId:artifactId:version)
    private static final Map<String, String> BUILTIN_LIBS = new LinkedHashMap<>();
    
    static {
        // JSON libraries
        BUILTIN_LIBS.put("gson", "com.google.code.gson:gson:2.8.0");
        BUILTIN_LIBS.put("json-simple", "com.googlecode.json-simple:json-simple:1.1.1");
        
        // Jackson (JSON serialization - used by many mods)
        BUILTIN_LIBS.put("jackson-core", "com.fasterxml.jackson.core:jackson-core:2.9.9");
        BUILTIN_LIBS.put("jackson-databind", "com.fasterxml.jackson.core:jackson-databind:2.9.9");
        BUILTIN_LIBS.put("jackson-annotations", "com.fasterxml.jackson.core:jackson-annotations:2.9.9");
        
        // Google libraries
        BUILTIN_LIBS.put("guava", "com.google.guava:guava:21.0");
        
        // Apache Commons
        BUILTIN_LIBS.put("commons-io", "commons-io:commons-io:2.5");
        BUILTIN_LIBS.put("commons-lang3", "org.apache.commons:commons-lang3:3.5");
        BUILTIN_LIBS.put("commons-codec", "commons-codec:commons-codec:1.10");
        BUILTIN_LIBS.put("commons-compress", "org.apache.commons:commons-compress:1.8.1");
        
        // Apache HTTP (used by some mods for web requests)
        BUILTIN_LIBS.put("httpclient", "org.apache.httpcomponents:httpclient:4.5.2");
        BUILTIN_LIBS.put("httpcore", "org.apache.httpcomponents:httpcore:4.4.4");
        
        // Logging
        BUILTIN_LIBS.put("log4j-api", "org.apache.logging.log4j:log4j-api:2.8.1");
        BUILTIN_LIBS.put("log4j-core", "org.apache.logging.log4j:log4j-core:2.8.1");
        BUILTIN_LIBS.put("slf4j-api", "org.slf4j:slf4j-api:1.7.25");
        
        // Networking
        BUILTIN_LIBS.put("netty-all", "io.netty:netty-all:4.1.9.Final");
        
        // Annotations
        BUILTIN_LIBS.put("jsr305", "com.google.code.findbugs:jsr305:3.0.1");
        BUILTIN_LIBS.put("javax.annotation-api", "javax.annotation:javax.annotation-api:1.3.2");
        BUILTIN_LIBS.put("jsr311-api", "javax.ws.rs:jsr311-api:1.1.1");
        
        // LWJGL (use stable Maven Central version)
        BUILTIN_LIBS.put("lwjgl", "org.lwjgl.lwjgl:lwjgl:2.9.3");
        BUILTIN_LIBS.put("lwjgl_util", "org.lwjgl.lwjgl:lwjgl_util:2.9.3");
        
        // JInput (input handling)
        BUILTIN_LIBS.put("jinput", "net.java.jinput:jinput:2.0.5");
        
        // JUtils (used by JInput)
        BUILTIN_LIBS.put("jutils", "net.java.jutils:jutils:1.0.0");
        
        // Trove (collections)
        BUILTIN_LIBS.put("trove4j", "net.sf.trove4j:trove4j:3.0.3");
        
        // Vecmath (use javax groupId)
        BUILTIN_LIBS.put("vecmath", "javax.vecmath:vecmath:1.5.2");
        
        // JOML (modern math library - used by newer mods)
        BUILTIN_LIBS.put("joml", "org.joml:joml:1.9.25");
        
        // ICU4J (internationalization)
        BUILTIN_LIBS.put("icu4j", "com.ibm.icu:icu4j:60.2");
        
        // FastUtil (collections)
        BUILTIN_LIBS.put("fastutil", "it.unimi.dsi:fastutil:7.1.0");
        
        // ASM (bytecode manipulation)
        BUILTIN_LIBS.put("asm", "org.ow2.asm:asm:5.2");
        BUILTIN_LIBS.put("asm-commons", "org.ow2.asm:asm-commons:5.2");
        BUILTIN_LIBS.put("asm-tree", "org.ow2.asm:asm-tree:5.2");
        BUILTIN_LIBS.put("asm-analysis", "org.ow2.asm:asm-analysis:5.2");
        BUILTIN_LIBS.put("asm-util", "org.ow2.asm:asm-util:5.2");
        
        // LZMA (compression)
        BUILTIN_LIBS.put("lzma", "com.github.jponge:lzma-java:1.3");
        
        // JNA (native access)
        BUILTIN_LIBS.put("jna", "net.java.dev.jna:jna:4.4.0");
        BUILTIN_LIBS.put("jna-platform", "net.java.dev.jna:jna-platform:4.4.0");
        
        // OshiCore (system info)
        BUILTIN_LIBS.put("oshi-core", "com.github.oshi:oshi-core:3.4.0");
        
        // Bouncy Castle (cryptography)
        BUILTIN_LIBS.put("bcprov-jdk15on", "org.bouncycastle:bcprov-jdk15on:1.58");
        
        // JOpt Simple (command line parsing)
        BUILTIN_LIBS.put("jopt-simple", "net.sf.jopt-simple:jopt-simple:5.0.3");
        
        // Java-ObjC-Bridge (macOS)
        BUILTIN_LIBS.put("java-objc-bridge", "ca.weblite:java-objc-bridge:1.0.0");
        
        // Note: authlib, patchy, text-io are not on Maven Central
        // They can be found in Minecraft libraries folder or added manually
    }
    
    private static final String MAVEN_CENTRAL = "https://repo1.maven.org/maven2/";
    private static final String LIBS_FILE = "libs.txt";
    
    private final File libsDir;
    private final File configFile;
    private final Map<String, String> customLibs = new LinkedHashMap<>();
    
    public LibraryManager() {
        this.libsDir = getLibsDirectory();
        this.configFile = new File(libsDir, LIBS_FILE);
        loadCustomLibs();
    }
    
    public LibraryManager(File libsDir) {
        this.libsDir = libsDir;
        this.configFile = new File(libsDir, LIBS_FILE);
        loadCustomLibs();
    }
    
    private static File getLibsDirectory() {
        try {
            String jarPath = LibraryManager.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            File jarFile = new File(jarPath);
            File libsDir = new File(jarFile.getParentFile(), "libs");
            libsDir.mkdirs();
            return libsDir;
        } catch (Exception e) {
            File homeDir = new File(System.getProperty("user.home"), ".bon2");
            File libsDir = new File(homeDir, "libs");
            libsDir.mkdirs();
            return libsDir;
        }
    }
    
    /**
     * Load custom library definitions from libs.txt
     */
    private void loadCustomLibs() {
        if (!configFile.exists()) {
            saveDefaultConfig();
            return;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    customLibs.put(parts[0].trim(), parts[1].trim());
                }
            }
        } catch (IOException e) {
            System.err.println("[BON2] Warning: Could not load libs.txt: " + e.getMessage());
        }
    }
    
    /**
     * Save default config with all builtin libraries
     */
    private void saveDefaultConfig() {
        libsDir.mkdirs();
        try (PrintWriter writer = new PrintWriter(new FileWriter(configFile))) {
            writer.println("# BON2 Library Configuration");
            writer.println("# Format: name=groupId:artifactId:version");
            writer.println("# Lines starting with # are comments");
            writer.println("# Add your own libraries below or modify versions");
            writer.println();
            writer.println("# === Built-in Libraries ===");
            writer.println();
            writer.println("# JSON");
            writer.println("gson=com.google.code.gson:gson:2.8.0");
            writer.println("json-simple=com.googlecode.json-simple:json-simple:1.1.1");
            writer.println();
            writer.println("# Google");
            writer.println("guava=com.google.guava:guava:21.0");
            writer.println();
            writer.println("# Apache Commons");
            writer.println("commons-io=commons-io:commons-io:2.5");
            writer.println("commons-lang3=org.apache.commons:commons-lang3:3.5");
            writer.println("commons-codec=commons-codec:commons-codec:1.10");
            writer.println();
            writer.println("# Logging");
            writer.println("log4j-api=org.apache.logging.log4j:log4j-api:2.8.1");
            writer.println("log4j-core=org.apache.logging.log4j:log4j-core:2.8.1");
            writer.println("slf4j-api=org.slf4j:slf4j-api:1.7.25");
            writer.println();
            writer.println("# Networking");
            writer.println("netty-all=io.netty:netty-all:4.1.9.Final");
            writer.println();
            writer.println("# Annotations");
            writer.println("jsr305=com.google.code.findbugs:jsr305:3.0.1");
            writer.println();
            writer.println("# Collections");
            writer.println("trove4j=net.sf.trove4j:trove4j:3.0.3");
            writer.println("fastutil=it.unimi.dsi:fastutil:7.1.0");
            writer.println();
            writer.println("# LWJGL");
            writer.println("lwjgl=org.lwjgl.lwjgl:lwjgl:2.9.3");
            writer.println("lwjgl_util=org.lwjgl.lwjgl:lwjgl_util:2.9.3");
            writer.println();
            writer.println("# Math");
            writer.println("vecmath=javax.vecmath:vecmath:1.5.2");
            writer.println();
            writer.println("# Bytecode");
            writer.println("asm=org.ow2.asm:asm:5.2");
            writer.println("asm-commons=org.ow2.asm:asm-commons:5.2");
            writer.println("asm-tree=org.ow2.asm:asm-tree:5.2");
            writer.println();
            writer.println("# === Custom Libraries ===");
            writer.println("# Add your own libraries below:");
            writer.println();
        } catch (IOException e) {
            System.err.println("[BON2] Warning: Could not save libs.txt: " + e.getMessage());
        }
    }
    
    /**
     * Get all available libraries (builtin + custom)
     */
    public Map<String, String> getAllLibraries() {
        Map<String, String> all = new LinkedHashMap<>(BUILTIN_LIBS);
        all.putAll(customLibs); // Custom overrides builtin
        return all;
    }
    
    /**
     * List all available libraries
     */
    public void listLibraries() {
        System.out.println("Available libraries:");
        System.out.println();
        
        Map<String, String> all = getAllLibraries();
        for (Map.Entry<String, String> entry : all.entrySet()) {
            String name = entry.getKey();
            String coord = entry.getValue();
            File jarFile = getJarFile(coord);
            String status = jarFile.exists() ? "[downloaded]" : "[not downloaded]";
            System.out.println("  " + name + " = " + coord + " " + status);
        }
        
        System.out.println();
        System.out.println("Config file: " + configFile.getAbsolutePath());
        System.out.println("Libs directory: " + libsDir.getAbsolutePath());
    }
    
    /**
     * Download a specific library by name
     */
    public boolean downloadLibrary(String name) {
        Map<String, String> all = getAllLibraries();
        String coord = all.get(name);
        
        if (coord == null) {
            System.err.println("[BON2] Unknown library: " + name);
            System.err.println("[BON2] Use --list-libs to see available libraries");
            return false;
        }
        
        return downloadByCoordinate(coord);
    }
    
    /**
     * Download a library by Maven coordinate (groupId:artifactId:version)
     */
    public boolean downloadByCoordinate(String coordinate) {
        String[] parts = coordinate.split(":");
        if (parts.length != 3) {
            System.err.println("[BON2] Invalid coordinate format: " + coordinate);
            System.err.println("[BON2] Expected: groupId:artifactId:version");
            return false;
        }
        
        String groupId = parts[0];
        String artifactId = parts[1];
        String version = parts[2];
        
        String path = groupId.replace('.', '/') + "/" + artifactId + "/" + version + "/" + artifactId + "-" + version + ".jar";
        String url = MAVEN_CENTRAL + path;
        
        File outputFile = new File(libsDir, artifactId + "-" + version + ".jar");
        
        if (outputFile.exists()) {
            System.out.println("[BON2] Already downloaded: " + outputFile.getName());
            return true;
        }
        
        System.out.println("[BON2] Downloading: " + coordinate);
        System.out.println("[BON2] URL: " + url);
        
        try {
            libsDir.mkdirs();
            
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(30000);
            conn.setRequestProperty("User-Agent", "BON2/2.4.0");
            
            if (conn.getResponseCode() != 200) {
                System.err.println("[BON2] Download failed: HTTP " + conn.getResponseCode());
                return false;
            }
            
            try (InputStream in = conn.getInputStream();
                 FileOutputStream out = new FileOutputStream(outputFile)) {
                byte[] buffer = new byte[8192];
                int len;
                long total = 0;
                while ((len = in.read(buffer)) > 0) {
                    out.write(buffer, 0, len);
                    total += len;
                }
                System.out.println("[BON2] Downloaded: " + outputFile.getName() + " (" + total / 1024 + " KB)");
            }
            
            return true;
            
        } catch (Exception e) {
            System.err.println("[BON2] Download failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Download all libraries
     */
    public void downloadAllLibraries() {
        System.out.println("[BON2] Downloading all libraries...");
        Map<String, String> all = getAllLibraries();
        int success = 0;
        int failed = 0;
        
        for (Map.Entry<String, String> entry : all.entrySet()) {
            if (downloadByCoordinate(entry.getValue())) {
                success++;
            } else {
                failed++;
            }
        }
        
        System.out.println();
        System.out.println("[BON2] Download complete: " + success + " succeeded, " + failed + " failed");
        System.out.println("[BON2] Libraries saved to: " + libsDir.getAbsolutePath());
    }
    
    /**
     * Get the JAR file path for a coordinate
     */
    private File getJarFile(String coordinate) {
        String[] parts = coordinate.split(":");
        if (parts.length != 3) return new File(libsDir, "invalid.jar");
        return new File(libsDir, parts[1] + "-" + parts[2] + ".jar");
    }
    
    public File getLibsDir() {
        return libsDir;
    }
    
    public File getConfigFile() {
        return configFile;
    }
    
    /**
     * Get builtin libraries (static)
     */
    public static Map<String, String> getBuiltinLibraries() {
        return Collections.unmodifiableMap(BUILTIN_LIBS);
    }
}
