package com.example.hackathon.service;

import java.io.File;
import java.io.IOException;
import java.util.*;
// import java.util.stream.Collectors;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;

import com.example.hackathon.helper.GetfileExtension;
import com.example.hackathon.rules.CategorizationRule;
import com.example.hackathon.rules.CategorizationRuleLoader;
// import com.example.hackathon.service.Logwritter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class DuplicateScannerService {

    private final Map<String, List<File>> hashMap = new HashMap<>();
    private final GetfileExtension getFileExtension = new GetfileExtension();
    private List<CategorizationRule> metadataRules = new ArrayList<>();

    public DuplicateScannerService() {
        loadCategorizationRules(); // ✅ Load rules from JSON file at startup
    }

    private void loadCategorizationRules() {
        try {
            File jsonFile = new File("metadata_rules.json"); // ✅ JSON must be in root dir or provide full path
            if (jsonFile.exists()) {
                ObjectMapper objectMapper = new ObjectMapper();
                metadataRules = objectMapper.readValue(jsonFile, new TypeReference<List<CategorizationRule>>() {});
                System.out.println("✅ Metadata categorization rules loaded: " + metadataRules.size());
            } else {
                System.out.println("⚠️ metadata_rules.json not found. Skipping metadata rules.");
            }
        } catch (IOException e) {
            System.out.println("❌ Error loading metadata rules: " + e.getMessage());
        }
    }

    public void scanDirectory(String path, boolean recursive) {
        hashMap.clear(); // Reset before every new scan

        Collection<File> files = FileUtils.listFiles(new File(path), null, recursive);
        for (File file : files) {
            try {
                String fileHash = DigestUtils.sha256Hex(FileUtils.readFileToByteArray(file));
                hashMap.computeIfAbsent(fileHash, k -> new ArrayList<>()).add(file);
            } catch (IOException e) {
                System.out.println("❌ Failed to read file: " + file.getAbsolutePath());
            }
        }

        deleteDuplicates();
        categorizeApplications(path);
    }

    public void deleteDuplicatesOnly(String path, boolean recursive) {
        hashMap.clear();

        Collection<File> files = FileUtils.listFiles(new File(path), null, recursive);
        for (File file : files) {
            try {
                String fileHash = DigestUtils.sha256Hex(FileUtils.readFileToByteArray(file));
                hashMap.computeIfAbsent(fileHash, k -> new ArrayList<>()).add(file);
            } catch (IOException e) {
                System.out.println("❌ Failed to read file: " + file.getAbsolutePath());
            }
        }

        deleteDuplicates();
    }

    public void categorizeOnly(String path, boolean recursive) {
        categorizeApplications(path);
    }

    private void deleteDuplicates() {
        Scanner scanner = new Scanner(System.in);
    
        int groupIndex = 1;
    
        for (List<File> duplicates : hashMap.values()) {
            if (duplicates.size() > 1) {
                System.out.println("\n🔁 Duplicate Group " + groupIndex++);
                for (int i = 0; i < duplicates.size(); i++) {
                    System.out.println("[" + i + "] " + duplicates.get(i).getAbsolutePath());
                }
    
                System.out.print("➡️ Enter index of file to KEEP (e.g., 0): ");
                String input = scanner.nextLine();
    
                int keepIndex;
                try {
                    keepIndex = Integer.parseInt(input);
                    if (keepIndex < 0 || keepIndex >= duplicates.size()) {
                        System.out.println("⚠️ Invalid index. Skipping this group.");
                        continue;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("⚠️ Invalid input. Skipping this group.");
                    continue;
                }
    
                for (int i = 0; i < duplicates.size(); i++) {
                    if (i != keepIndex) {
                        File file = duplicates.get(i);
                        if (file.delete()) {
                            System.out.println("🗑️ Deleted: " + file.getAbsolutePath());
                            Logwritter.write("🗑️ Deleted: " + file.getAbsolutePath());
                        } else {
                            System.out.println("❌ Failed to delete: " + file.getAbsolutePath());
                        }
                    }
                }
            }
        }
    
        System.out.println("\n✅ Duplicate deletion process completed.");
    }
    
    private String categorizeFile(File file, LoggerService logger) {
    String name = file.getName().toLowerCase();
    String extension = new GetfileExtension().getFileExtension(file);
    String path = file.getAbsolutePath();
    List<CategorizationRule> rules = CategorizationRuleLoader.loadRulesFromJson();

    // 1. Rule from JSON
    for (CategorizationRule rule : rules) {
        if (path.matches(rule.getMatch()) || name.matches(rule.getMatch())) {
            logger.log("📁 Categorized by JSON Rule: " + rule.getMatch() + " → " + rule.getCategory());
            return rule.getCategory();
        }
    }

    // 2. Manifest-based Categorization (for .jar, .apk)
    if (extension.equals("jar")) {
        try (JarFile jar = new JarFile(file)) {
            Manifest manifest = jar.getManifest();
            if (manifest != null) {
                String appName = manifest.getMainAttributes().getValue("Implementation-Title");
                if (appName != null && appName.toLowerCase().contains("game")) {
                    logger.log("🕹️ Categorized by JAR Manifest → Games");
                    return "Games";
                }
            }
        } catch (Exception e) {
            logger.log("❌ Manifest parse error: " + e.getMessage());
        }
    }

    // 3. Content Inspection (README, txt)
    if (extension.equals("txt") || name.toLowerCase().contains("readme")) {
        try {
            String content = FileUtils.readFileToString(file, "UTF-8");
            if (content.toLowerCase().contains("install") || content.toLowerCase().contains("setup")) {
                logger.log("📖 Categorized by Content → Setup Guide");
                return "Setup Guide";
            }
        } catch (IOException e) {
            logger.log("❌ Failed to read file content: " + e.getMessage());
        }
    }

    // 4. Path-based categorization
    if (path.toLowerCase().contains("/games/") || path.toLowerCase().contains("\\games\\")) {
        logger.log("🗂️ Categorized by Path → Games");
        return "Games";
    }

    // 5. Fallback to extension
    switch (extension) {
        case "exe":
        case "msi":
        case "apk":
            logger.log("📦 Extension match → Installers");
            return "Installers";
        case "pdf":
        case "docx":
            logger.log("📄 Extension match → Documents");
            return "Documents";
        default:
            try {
            Tika tika = new Tika();
            String detectedType = tika.detect(file);
            if (detectedType.contains("image")) {
                logger.log("🖼️ Tika match → Images");
                return "Images";
            } else if (detectedType.contains("video")) {
                logger.log("🎞️ Tika match → Videos");
                return "Videos";
            } else if (detectedType.contains("text")) {
                logger.log("📜 Tika match → TextFiles");
                return "TextFiles";
            }
        } catch (Exception e) {
            logger.log("❌ Tika error: " + e.getMessage());
        }

        logger.log("📁 Uncategorized → Others");
            return "Others";
    }
}



    private void categorizeApplications(String basePath) {
        Collection<File> files = FileUtils.listFiles(new File(basePath), null, true);

        for (File file : files) {
            String extension = getFileExtension.getFileExtension(file);
            String category = getCategoryFromRules(file, extension);

            if (category != null && !category.isEmpty()) {
                File targetDir = new File(basePath + File.separator + category);
                if (!targetDir.exists()) {
                    targetDir.mkdirs();
                }

                File dest = new File(targetDir, file.getName());
                try {
                    FileUtils.moveFile(file, dest);
                    System.out.println("➡️ Moved: " + file.getName() + " → " + category + "/");
                    Logwritter.write("➡️ Moved: " + file.getName() + " → " + category + "/");
                } catch (IOException e) {
                    System.out.println("❌ Failed to move: " + file.getAbsolutePath());
                }
            }
        }
    }

    private String getCategoryFromRules(File file, String extension) {
        // ✅ First try metadata rules
        for (CategorizationRule rule : metadataRules) {
            if (file.getName().toLowerCase().contains(rule.getMatch().toLowerCase())) {
                return rule.getCategory();
            }
        }

        // ✅ Then fall back to extension-based
        Map<String, String> CATEGORY_RULES = Map.of(
            "exe", "Executables",
            "pdf", "Documents",
            "docx", "Documents",
            "txt", "TextFiles",
            "png", "Images",
            "jpg", "Images",
            "jpeg", "Images",
            "mp4", "Videos",
            "mkv", "Videos"
        );

        return CATEGORY_RULES.getOrDefault(extension, "Others");
    }
}
