package com.github.parker8283.bon2;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Test to validate all download URLs are accessible.
 * Run with: java -cp BON-2.4.0.CUSTOM-all.jar com.github.parker8283.bon2.UrlValidationTest
 */
public class UrlValidationTest {
    
    // MCP Mapping URLs
    private static final Map<String, String> MAPPING_URLS = new LinkedHashMap<>();
    
    // Library Maven coordinates
    private static final Map<String, String> LIBRARY_COORDS = new LinkedHashMap<>();
    
    private static final String MAVEN_CENTRAL = "https://repo1.maven.org/maven2/";
    
    static {
        // MCP Mappings
        MAPPING_URLS.put("1.12.2-stable_39", "https://maven.minecraftforge.net/de/oceanlabs/mcp/mcp_stable/39-1.12/mcp_stable-39-1.12.zip");
        MAPPING_URLS.put("1.12.2-snapshot_20171003", "https://maven.minecraftforge.net/de/oceanlabs/mcp/mcp_snapshot/20171003-1.12/mcp_snapshot-20171003-1.12.zip");
        MAPPING_URLS.put("1.11.2-stable_32", "https://maven.minecraftforge.net/de/oceanlabs/mcp/mcp_stable/32-1.11/mcp_stable-32-1.11.zip");
        MAPPING_URLS.put("1.10.2-stable_29", "https://maven.minecraftforge.net/de/oceanlabs/mcp/mcp_stable/29-1.10.2/mcp_stable-29-1.10.2.zip");
        MAPPING_URLS.put("1.9.4-stable_26", "https://maven.minecraftforge.net/de/oceanlabs/mcp/mcp_stable/26-1.9.4/mcp_stable-26-1.9.4.zip");
        MAPPING_URLS.put("1.8.9-stable_22", "https://maven.minecraftforge.net/de/oceanlabs/mcp/mcp_stable/22-1.8.9/mcp_stable-22-1.8.9.zip");
        MAPPING_URLS.put("1.7.10-stable_12", "https://maven.minecraftforge.net/de/oceanlabs/mcp/mcp_stable/12-1.7.10/mcp_stable-12-1.7.10.zip");
        
        // Common Libraries (Maven coordinates)
        LIBRARY_COORDS.put("gson", "com.google.code.gson:gson:2.8.0");
        LIBRARY_COORDS.put("json-simple", "com.googlecode.json-simple:json-simple:1.1.1");
        LIBRARY_COORDS.put("guava", "com.google.guava:guava:21.0");
        LIBRARY_COORDS.put("commons-io", "commons-io:commons-io:2.5");
        LIBRARY_COORDS.put("commons-lang3", "org.apache.commons:commons-lang3:3.5");
        LIBRARY_COORDS.put("commons-codec", "commons-codec:commons-codec:1.10");
        LIBRARY_COORDS.put("log4j-api", "org.apache.logging.log4j:log4j-api:2.8.1");
        LIBRARY_COORDS.put("log4j-core", "org.apache.logging.log4j:log4j-core:2.8.1");
        LIBRARY_COORDS.put("slf4j-api", "org.slf4j:slf4j-api:1.7.25");
        LIBRARY_COORDS.put("netty-all", "io.netty:netty-all:4.1.9.Final");
        LIBRARY_COORDS.put("jsr305", "com.google.code.findbugs:jsr305:3.0.1");
        LIBRARY_COORDS.put("javax.annotation-api", "javax.annotation:javax.annotation-api:1.3.2");
        LIBRARY_COORDS.put("lwjgl", "org.lwjgl.lwjgl:lwjgl:2.9.3");
        LIBRARY_COORDS.put("lwjgl_util", "org.lwjgl.lwjgl:lwjgl_util:2.9.3");
        LIBRARY_COORDS.put("trove4j", "net.sf.trove4j:trove4j:3.0.3");
        LIBRARY_COORDS.put("vecmath", "javax.vecmath:vecmath:1.5.2");
        LIBRARY_COORDS.put("icu4j", "com.ibm.icu:icu4j:60.2");
        LIBRARY_COORDS.put("fastutil", "it.unimi.dsi:fastutil:7.1.0");
        LIBRARY_COORDS.put("asm", "org.ow2.asm:asm:5.2");
        LIBRARY_COORDS.put("asm-commons", "org.ow2.asm:asm-commons:5.2");
        LIBRARY_COORDS.put("asm-tree", "org.ow2.asm:asm-tree:5.2");
    }
    
    public static void main(String[] args) {
        System.out.println("=".repeat(60));
        System.out.println("BON2 URL Validation Test");
        System.out.println("=".repeat(60));
        System.out.println();
        
        List<String> failed = new ArrayList<>();
        int passed = 0;
        
        // Test MCP Mappings
        System.out.println("[MCP Mappings]");
        System.out.println("-".repeat(60));
        for (Map.Entry<String, String> entry : MAPPING_URLS.entrySet()) {
            String name = entry.getKey();
            String url = entry.getValue();
            boolean ok = testUrl(url);
            String status = ok ? "OK" : "FAILED";
            System.out.printf("  %-30s %s%n", name, status);
            if (ok) passed++; else failed.add("mapping:" + name);
        }
        System.out.println();
        
        // Test Libraries
        System.out.println("[Libraries (Maven Central)]");
        System.out.println("-".repeat(60));
        for (Map.Entry<String, String> entry : LIBRARY_COORDS.entrySet()) {
            String name = entry.getKey();
            String coord = entry.getValue();
            String url = coordToUrl(coord);
            boolean ok = testUrl(url);
            String status = ok ? "OK" : "FAILED";
            System.out.printf("  %-30s %s%n", name, status);
            if (ok) passed++; else failed.add("library:" + name);
        }
        System.out.println();
        
        // Summary
        System.out.println("=".repeat(60));
        int total = MAPPING_URLS.size() + LIBRARY_COORDS.size();
        System.out.printf("Results: %d/%d passed%n", passed, total);
        
        if (!failed.isEmpty()) {
            System.out.println();
            System.out.println("Failed URLs:");
            for (String f : failed) {
                System.out.println("  - " + f);
            }
            System.exit(1);
        } else {
            System.out.println("All URLs are valid!");
            System.exit(0);
        }
    }
    
    /**
     * Convert Maven coordinate to URL
     */
    private static String coordToUrl(String coordinate) {
        String[] parts = coordinate.split(":");
        if (parts.length != 3) return "";
        String groupId = parts[0];
        String artifactId = parts[1];
        String version = parts[2];
        String path = groupId.replace('.', '/') + "/" + artifactId + "/" + version + "/" + artifactId + "-" + version + ".jar";
        return MAVEN_CENTRAL + path;
    }
    
    /**
     * Test if URL is accessible (HEAD request)
     */
    private static boolean testUrl(String urlStr) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("HEAD");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.setRequestProperty("User-Agent", "BON2-Test/1.0");
            conn.setInstanceFollowRedirects(true);
            
            int code = conn.getResponseCode();
            conn.disconnect();
            
            return code >= 200 && code < 400;
        } catch (Exception e) {
            return false;
        }
    }
}
