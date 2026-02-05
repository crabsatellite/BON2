package com.github.parker8283.bon2;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

/**
 * Scans and validates all known MCP mapping URLs.
 * Run this to find all available mappings from Forge Maven.
 */
public class MappingUrlScanner {
    
    public static void main(String[] args) {
        System.out.println("============================================================");
        System.out.println("MCP Mappings URL Scanner");
        System.out.println("============================================================\n");
        
        List<String> validUrls = new ArrayList<>();
        
        // Test stable mappings
        System.out.println("[Stable Mappings]");
        System.out.println("------------------------------------------------------------");
        String[][] stableVersions = {
            // MC version, stable number, MCP version suffix
            {"1.12.2", "39", "1.12"},
            {"1.12.1", "39", "1.12"},
            {"1.12", "39", "1.12"},
            {"1.11.2", "32", "1.11"},
            {"1.11", "32", "1.11"},
            {"1.10.2", "29", "1.10.2"},
            {"1.10", "29", "1.10.2"},
            {"1.9.4", "26", "1.9.4"},
            {"1.9", "24", "1.9"},
            {"1.8.9", "22", "1.8.9"},
            {"1.8.8", "20", "1.8.8"},
            {"1.8", "18", "1.8"},
            {"1.7.10", "12", "1.7.10"},
            {"1.7.2", "10", "1.7"},
            {"1.6.4", "9", "1.6"},
            {"1.6.2", "8", "1.6"},
        };
        
        for (String[] ver : stableVersions) {
            String mcVer = ver[0];
            String stableNum = ver[1];
            String mcpSuffix = ver[2];
            String urlKey = mcVer + "-stable_" + stableNum;
            String url = String.format(
                "https://maven.minecraftforge.net/de/oceanlabs/mcp/mcp_stable/%s-%s/mcp_stable-%s-%s.zip",
                stableNum, mcpSuffix, stableNum, mcpSuffix);
            
            boolean valid = testUrl(url);
            System.out.printf("  %-30s %s%n", urlKey, valid ? "OK" : "FAIL");
            if (valid) {
                validUrls.add("STABLE|" + urlKey + "|" + url);
            }
        }
        
        // Test snapshot mappings for 1.13+
        System.out.println("\n[Snapshot Mappings (1.13+)]");
        System.out.println("------------------------------------------------------------");
        String[][] snapshotVersions = {
            // date, MC version
            {"20210309", "1.16.5"},
            {"20201028", "1.16.3"},
            {"20200916", "1.16.2"},
            {"20200514", "1.16"},
            {"20200515", "1.15.2"},
            {"20200220", "1.15.1"},
            {"20190719", "1.14.3"},
            {"20190726", "1.14.4"},
            {"20180921", "1.13"},
            {"20190213", "1.13.2"},
        };
        
        for (String[] ver : snapshotVersions) {
            String date = ver[0];
            String mcVer = ver[1];
            String urlKey = mcVer + "-snapshot_" + date;
            String url = String.format(
                "https://maven.minecraftforge.net/de/oceanlabs/mcp/mcp_snapshot/%s-%s/mcp_snapshot-%s-%s.zip",
                date, mcVer, date, mcVer);
            
            boolean valid = testUrl(url);
            System.out.printf("  %-30s %s%n", urlKey, valid ? "OK" : "FAIL");
            if (valid) {
                validUrls.add("SNAPSHOT|" + urlKey + "|" + url);
            }
        }
        
        // Additional common snapshot dates
        System.out.println("\n[Additional Snapshot Dates]");
        System.out.println("------------------------------------------------------------");
        String[][] additionalSnapshots = {
            {"20171003", "1.12"},
            {"20180814", "1.12"},
            {"20180815", "1.13"},
            {"20181130", "1.13.2"},
            {"20190608", "1.14.2"},
            {"20200119", "1.15.2"},
            {"20200723", "1.16.1"},
        };
        
        for (String[] ver : additionalSnapshots) {
            String date = ver[0];
            String mcVer = ver[1];
            String urlKey = mcVer + "-snapshot_" + date;
            String url = String.format(
                "https://maven.minecraftforge.net/de/oceanlabs/mcp/mcp_snapshot/%s-%s/mcp_snapshot-%s-%s.zip",
                date, mcVer, date, mcVer);
            
            boolean valid = testUrl(url);
            System.out.printf("  %-30s %s%n", urlKey, valid ? "OK" : "FAIL");
            if (valid) {
                validUrls.add("SNAPSHOT|" + urlKey + "|" + url);
            }
        }
        
        // Print summary
        System.out.println("\n============================================================");
        System.out.println("VALID MAPPINGS SUMMARY (" + validUrls.size() + " total)");
        System.out.println("============================================================");
        System.out.println("\n// Java code for MAPPING_URLS:");
        System.out.println("static {");
        for (String entry : validUrls) {
            String[] parts = entry.split("\\|");
            String type = parts[0];
            String key = parts[1];
            String url = parts[2];
            System.out.printf("    MAPPING_URLS.put(\"%s\", \"%s\");%n", key, url);
        }
        System.out.println("}");
    }
    
    private static boolean testUrl(String url) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("HEAD");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            int code = conn.getResponseCode();
            conn.disconnect();
            return code == 200;
        } catch (Exception e) {
            return false;
        }
    }
}
