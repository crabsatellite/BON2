package com.github.parker8283.bon2;

import java.awt.EventQueue;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import javax.swing.UIManager;

import com.github.parker8283.bon2.cli.CLIErrorHandler;
import com.github.parker8283.bon2.cli.CLIProgressListener;
import com.github.parker8283.bon2.data.BONFiles;
import com.github.parker8283.bon2.data.IErrorHandler;
import com.github.parker8283.bon2.data.MappingManager;
import com.github.parker8283.bon2.data.MappingVersion;
import com.github.parker8283.bon2.data.VersionLookup;
import com.github.parker8283.bon2.exception.InvalidMappingsVersionException;
import com.github.parker8283.bon2.util.BONUtils;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class BON2 {
    public static final String VERSION = "Bearded Octo Nemesis v${DEV} by Parker8283. BON v1 by immibis.";

    public static void main(String[] args) throws Exception {
        if(args.length > 0) {
            parseArgs(args);
        } else {
            launchGui();
        }
    }

    private static void parseArgs(String[] args) throws Exception {
        OptionParser parser = new OptionParser();
        parser.accepts("help", "Prints this help menu").forHelp();
        parser.accepts("version", "Prints the version string").forHelp();
        parser.accepts("inputJar", "The jar file to deobfuscate").withRequiredArg();
        parser.accepts("outputJar", "The location and name of the output jar. Defaults to same dir and appends \"-deobf\"").withRequiredArg();
        parser.accepts("mappingsVer", "The version of the mappings to use. Must exist in Gradle cache or bundled mappings. Format: \"stable_39\" or \"1.12.2\"").withRequiredArg();
        parser.accepts("mappingsDir", "Custom directory containing mapping files (fields.csv, methods.csv)").withRequiredArg();
        parser.accepts("download", "Download mappings. Use with --mappingsVer or 'all' to download all available mappings");
        parser.accepts("list", "List all available mappings (bundled + Gradle cache)");

        try {
            OptionSet options = parser.parse(args);
            if(options.has("help")) {
                System.out.println(VERSION);
                System.out.println();
                System.out.println("Available mappings for download:");
                for (String key : MappingManager.getAvailableDownloads()) {
                    System.out.println("  - " + key);
                }
                System.out.println();
                parser.printHelpOn(System.out);
                System.exit(0);
            }
            if(options.has("version")) {
                System.out.println(VERSION);
                System.exit(0);
            }
            
            MappingManager mappingManager = new MappingManager();
            
            // Handle --list command
            if(options.has("list")) {
                System.out.println("Available mappings:");
                System.out.println();
                System.out.println("[Bundled/Downloaded]");
                for (MappingManager.MappingInfo info : mappingManager.listAvailableMappings()) {
                    if (info.getSource() != MappingManager.MappingSource.GRADLE_CACHE) {
                        System.out.println("  " + info);
                    }
                }
                System.out.println();
                System.out.println("[Gradle Cache]");
                for (MappingManager.MappingInfo info : mappingManager.listAvailableMappings()) {
                    if (info.getSource() == MappingManager.MappingSource.GRADLE_CACHE) {
                        System.out.println("  " + info);
                    }
                }
                System.out.println();
                System.out.println("[Available for download]");
                for (String key : MappingManager.getAvailableDownloads()) {
                    System.out.println("  " + key);
                }
                System.exit(0);
            }
            
            // Handle --download command
            if(options.has("download")) {
                if (options.has("mappingsVer")) {
                    String ver = (String) options.valueOf("mappingsVer");
                    if ("all".equalsIgnoreCase(ver)) {
                        mappingManager.downloadAllMappings();
                    } else {
                        String key = null;
                        for (String k : MappingManager.getAvailableDownloads()) {
                            if (k.contains(ver)) {
                                key = k;
                                break;
                            }
                        }
                        if (key != null) {
                            File targetDir = new File(mappingManager.getMappingsDir(), key.substring(0, key.indexOf("-")));
                            mappingManager.downloadMappings(key, targetDir);
                        } else {
                            System.err.println("Unknown mapping version: " + ver);
                            System.err.println("Available: " + MappingManager.getAvailableDownloads());
                        }
                    }
                } else {
                    mappingManager.downloadAllMappings();
                }
                System.exit(0);
            }
            
            // Normal deobfuscation mode - require inputJar and mappingsVer
            if (!options.has("inputJar") || !options.has("mappingsVer")) {
                System.err.println("Error: --inputJar and --mappingsVer are required for deobfuscation");
                parser.printHelpOn(System.err);
                System.exit(1);
            }

            String inputJar = (String)options.valueOf("inputJar");
            String outputJar = options.has("outputJar") ? (String)options.valueOf("outputJar") : inputJar.replace(".jar", "-deobf.jar");
            String mappingsVer = (String)options.valueOf("mappingsVer");

            if(!new File(inputJar).exists()) {
                System.err.println("The provided inputJar does not exist");
                new FileNotFoundException(inputJar).printStackTrace();
                System.exit(1);
            }
            
            MappingVersion mapping = null;
            
            // Check for custom mappings directory
            if (options.has("mappingsDir")) {
                File customDir = new File((String) options.valueOf("mappingsDir"));
                if (customDir.exists() && new File(customDir, "fields.csv").exists()) {
                    mapping = new MappingVersion("custom", customDir);
                    System.out.println("[BON2] Using custom mappings from: " + customDir);
                } else {
                    System.err.println("Custom mappings directory is invalid or missing files");
                    System.exit(1);
                }
            }
            
            // Try bundled/downloaded mappings first
            if (mapping == null) {
                File bundledDir = mappingManager.getMappingDir(mappingsVer);
                if (bundledDir != null) {
                    mapping = new MappingVersion(mappingsVer, bundledDir);
                }
            }
            
            // Fall back to Gradle cache
            if (mapping == null) {
                VersionLookup.INSTANCE.refresh();
                List<MappingVersion> mappings = BONUtils.buildValidMappings();
                for (MappingVersion m : mappings) {
                    if (m.getVersion().contains(mappingsVer)) {
                        mapping = m;
                        break;
                    }
                }
            }
            
            if (mapping == null) {
                System.err.println("The provided mappingsVer are invalid.");
                System.err.println("Options:");
                System.err.println("  1. Use --download to download mappings first");
                System.err.println("  2. Place mapping files in: " + mappingManager.getMappingsDir());
                System.err.println("  3. Use --mappingsDir to specify custom mapping directory");
                System.err.println("  4. Ensure mappings exist in Gradle cache");
                new InvalidMappingsVersionException(mappingsVer).printStackTrace();
                System.exit(1);
            }
            
            IErrorHandler errorHandler = new CLIErrorHandler();

            log(VERSION);
            log("Input JAR:       " + inputJar);
            log("Output JAR:      " + outputJar);
            log("Mappings:        " + mapping.getVersion());
            log("Mappings Dir:    " + mapping.getSrgs());

            try {
                BON2Impl.remap(new File(inputJar), new File(outputJar), mapping, errorHandler, new CLIProgressListener());
            } catch(Exception e) {
                logErr(e.getMessage(), e);
                System.exit(1);
            }
        } catch(OptionException e) {
            e.printStackTrace();
            parser.printHelpOn(System.err);
        }
    }

    private static void log(String message) {
        System.out.println(message);
    }

    private static void logErr(String message, Throwable t) {
        System.err.println(message);
        t.printStackTrace();
    }

    private static void launchGui() {
        log(VERSION);
        log("No arguments passed. Launching gui...");
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    BON2Gui frame = new BON2Gui();
                    frame.setVisible(true);
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
