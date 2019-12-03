package io.bdrc.libraries;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;

import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapStd;
import org.apache.jena.shared.PrefixMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Prefixes {

    public final static Logger log = LoggerFactory.getLogger(Prefixes.class);
    private final static PrefixMap pMap = new PrefixMapStd();
    private final static HashMap<String, String> map = new HashMap<>();
    private static String prefixesString;
    private final static PrefixMapping PREFIXES_MAP = PrefixMapping.Factory.create();

    static {
        try {
            loadPrefixes();
        } catch (IOException ex) {
            log.error("Prefixes initialization error", ex);
        }
    }

    public static String getPrefixesString() {
        return prefixesString;
    }

    public static void loadPrefixes() throws IOException {
        URL url = new URL("https://raw.githubusercontent.com/buda-base/lds-queries/master/public/prefixes.txt");
        BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
        final StringBuilder sb = new StringBuilder();
        PREFIXES_MAP.clearNsPrefixMap();
        pMap.clear();
        String line = "";
        while ((line = br.readLine()) != null) {
            sb.append(line);
            if (line.length() < 10 || line.startsWith("#"))
                continue;
            final String uri = line.substring(line.indexOf('<') + 1, line.indexOf('>'));
            final String prefix = line.substring(7, line.indexOf(':')).trim();
            map.put(prefix, uri);
            pMap.add(prefix, uri);
            PREFIXES_MAP.setNsPrefix(prefix, uri);
        }
        prefixesString = sb.toString();
        br.close();
    }

    public static void loadPrefixes(final String filePath) throws IOException {

        log.info("reading prefixes from {}", filePath);
        final File file = new File(filePath);
        final BufferedReader br = new BufferedReader(new FileReader(file));
        final StringBuilder sb = new StringBuilder();
        PREFIXES_MAP.clearNsPrefixMap();
        pMap.clear();
        String line = "";
        while ((line = br.readLine()) != null) {
            sb.append(line);
            if (line.length() < 10 || line.startsWith("#"))
                continue;
            final String uri = line.substring(line.indexOf('<') + 1, line.indexOf('>'));
            final String prefix = line.substring(7, line.indexOf(':')).trim();
            map.put(prefix, uri);
            pMap.add(prefix, uri);
            PREFIXES_MAP.setNsPrefix(prefix, uri);
        }
        prefixesString = sb.toString();
        br.close();
    }

    public static PrefixMap getPrefixMap() {
        return pMap;
    }

    public static HashMap<String, String> getMap() {
        return map;
    }

    public static PrefixMapping getPrefixMapping() {
        return PREFIXES_MAP;
    }

    public static String getFullIRI(String prefix) {
        if (prefix != null) {
            return PREFIXES_MAP.getNsPrefixURI(prefix);
        }
        return null;
    }

    public static String getPrefix(String IRI) {
        if (IRI != null) {
            return PREFIXES_MAP.getNsURIPrefix(IRI);
        }
        return "";
    }

    public static String getPrefixedIRI(String IRI) {
        if (IRI != null) {
            return PREFIXES_MAP.shortForm(IRI);
        }
        return "";
    }

}
