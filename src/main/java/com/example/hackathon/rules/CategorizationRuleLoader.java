package com.example.hackathon.rules;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

public class CategorizationRuleLoader {

    public static List<CategorizationRule> loadRulesFromJson() {
        try (InputStream is = CategorizationRuleLoader.class.getResourceAsStream("/rules.json")) {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(is, new TypeReference<>() {});
        } catch (Exception e) {
            System.out.println("❌ Failed to load rules.json: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}
