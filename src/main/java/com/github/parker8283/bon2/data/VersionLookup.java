package com.github.parker8283.bon2.data;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import com.github.parker8283.bon2.data.VersionJson.MappingsJson;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public enum VersionLookup {

    INSTANCE;

    private static final String VERSION_JSON = "http://export.mcpbot.bspk.rs/versions.json";
    private static final Gson GSON = new GsonBuilder().create();

    // Hardcoded fallback JSON for when mcpbot.bspk.rs is unavailable
    // Contains MCP mappings data for Minecraft 1.12.2 and other versions
    private static final String FALLBACK_JSON = "{" +
        "\"1.12\":{\"snapshot\":[20171003],\"stable\":[39]}," +
        "\"1.11.2\":{\"snapshot\":[20161220],\"stable\":[32]}," +
        "\"1.11\":{\"snapshot\":[20161115,20161111,20161104],\"stable\":[31,30]}," +
        "\"1.10.2\":{\"snapshot\":[20160518],\"stable\":[29]}," +
        "\"1.9.4\":{\"snapshot\":[20160501],\"stable\":[26]}," +
        "\"1.9\":{\"snapshot\":[20160320,20160312,20160305,20160301,20160228,20160227,20160226,20160225,20160224],\"stable\":[24]}," +
        "\"1.8.9\":{\"snapshot\":[20160301,20151216],\"stable\":[22]}," +
        "\"1.8.8\":{\"snapshot\":[20150913],\"stable\":[20]}," +
        "\"1.8\":{\"snapshot\":[20141130,20140925,20140903],\"stable\":[18]}," +
        "\"1.7.10\":{\"snapshot\":[20140925],\"stable\":[12]}" +
        "}";

    private VersionJson jsoncache;

    public String getVersionFor(String version) {
        if (jsoncache != null) {
            // Parse version string to extract the numeric part
            // Handles formats like:
            // - "stable_39" -> "39"
            // - "snapshot_20171003" -> "20171003"
            // - "39-1.12" -> "39"
            // - "20171003-1.12" -> "20171003"
            String numericVersion = version;
            
            // Handle underscore format like "stable_39" or "snapshot_20171003"
            if (version.contains("_")) {
                numericVersion = version.substring(version.indexOf("_") + 1);
            }
            // Handle hyphen format like "39-1.12" (Gradle cache folder format)
            // Take the part BEFORE the hyphen as it's the mapping number
            else if (version.contains("-")) {
                numericVersion = version.substring(0, version.indexOf("-"));
            }
            
            for (String s : jsoncache.getVersions()) {
                MappingsJson mappings = jsoncache.getMappings(s);
                if (mappings.hasSnapshot(numericVersion) || mappings.hasStable(numericVersion)) {
                    return s;
                }
            }
        }
        return null;
    }
    
    public VersionJson getVersions() {
        return jsoncache;
    }

    @SuppressWarnings("serial")
    public void refresh() throws IOException {
        Reader in = null;
        boolean usedFallback = false;
        
        try {
            URL url = new URL(VERSION_JSON);
            HttpURLConnection request = (HttpURLConnection) url.openConnection();
            request.setConnectTimeout(5000); // 5 second timeout
            request.setReadTimeout(5000);
            request.connect();
            in = new InputStreamReader(request.getInputStream());
        } catch (IOException e) {
            // mcpbot.bspk.rs is down, use fallback JSON
            System.out.println("[BON2] Warning: Could not connect to mcpbot.bspk.rs, using offline fallback data.");
            System.out.println("[BON2] Fallback supports: 1.7.10, 1.8, 1.8.8, 1.8.9, 1.9, 1.9.4, 1.10.2, 1.11, 1.11.2, 1.12/1.12.2");
            in = new StringReader(FALLBACK_JSON);
            usedFallback = true;
        }
        
        try {
            INSTANCE.jsoncache = new VersionJson(GSON.fromJson(in, new TypeToken<Map<String, MappingsJson>>() {}.getType()));
        } finally {
            in.close();
        }
        
        if (!usedFallback) {
            System.out.println("[BON2] Successfully loaded version data from mcpbot.bspk.rs");
        }
    }
}
