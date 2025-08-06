package com.example.hackathon.config;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
// import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.example.hackathon.rules.CategorizationRule;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
public class RuleLoader {
    private static final String DEFAULT_RULES_PATH = "/rules.json";
    
    public static List<CategorizationRule> loadRules(String path) {
        try {
            // Try loading from classpath first
            InputStream is = RuleLoader.class.getResourceAsStream(path);
            if (is == null) {
                // Fall back to absolute path
                File file = new File(path);
                if (!file.exists()) {
                    // Try default location
                    is = RuleLoader.class.getResourceAsStream(DEFAULT_RULES_PATH);
                    if (is == null) {
                        // logger.warn("Rules file not found at: {}", path);
                        return Collections.emptyList();
                    }
                } else {
                    String json = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
                    return parseRules(json);
                }
            }
            return parseRules(IOUtils.toString(is, StandardCharsets.UTF_8));
        } catch (Exception e) {
            // logger.error("Failed to load rules", e);
            return Collections.emptyList();
        }
    }
    
    private static List<CategorizationRule> parseRules(String json) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, new TypeReference<List<CategorizationRule>>(){});
    }
}

