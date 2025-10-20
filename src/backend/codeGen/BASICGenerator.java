package backend.codeGen;

import java.util.*;
import java.util.regex.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

/**
 * Converts intermediate code to executable BASIC with line numbers
 * Resolves GOTO and THEN labels to line numbers
 */
public class BASICGenerator {
    private static final int LINE_INCREMENT = 10; // Traditional BASIC: 10, 20, 30...
    
    /**
     * Convert intermediate code to executable BASIC
     */
    public String generateBASIC(String intermediateCode) {
        String[] lines = intermediateCode.split("\n");
        
        // Step 1: Build label map (label name -> line number)
        Map<String, Integer> labelMap = buildLabelMap(lines);
        
        // Step 2: Generate BASIC code with line numbers and resolved labels
        return generateNumberedCode(lines, labelMap);
    }
    
    /**
     * Build a map of labels to their line numbers
     */
    private Map<String, Integer> buildLabelMap(String[] lines) {
        Map<String, Integer> labelMap = new HashMap<>();
        int lineNumber = LINE_INCREMENT;
        
        for (String line : lines) {
            String trimmed = line.trim();
            
            // Check if this line is a label: REM Lx
            if (trimmed.startsWith("REM ")) {
                String label = extractLabel(trimmed);
                if (label != null) {
                    labelMap.put(label, lineNumber);
                }
            }
            
            lineNumber += LINE_INCREMENT;
        }
        
        return labelMap;
    }
    
    /**
     * Extract label name from REM statement
     */
    private String extractLabel(String line) {
        // REM L1, REM T2, REM Exit3, etc.
        String[] parts = line.split("\\s+", 2);
        if (parts.length >= 2) {
            return parts[1].trim();
        }
        return null;
    }
    
    /**
     * Generate line-numbered BASIC code with resolved labels
     */
    private String generateNumberedCode(String[] lines, Map<String, Integer> labelMap) {
        StringBuilder basic = new StringBuilder();
        int lineNumber = LINE_INCREMENT;
        
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            
            // Add line number
            basic.append(lineNumber).append(" ");
            
            // Process line and resolve labels
            String processedLine = resolveLabels(trimmed, labelMap);
            basic.append(processedLine).append("\n");
            
            lineNumber += LINE_INCREMENT;
        }
        
        return basic.toString();
    }
    
    /**
     * Resolve GOTO Lx and THEN Lx to actual line numbers
     */
    private String resolveLabels(String line, Map<String, Integer> labelMap) {
        // Pattern 1: GOTO Lx -> GOTO lineNumber
        line = resolveGotoLabels(line, labelMap);
        
        // Pattern 2: THEN Lx -> THEN lineNumber
        line = resolveThenLabels(line, labelMap);
        
        return line;
    }
    
    /**
     * Resolve GOTO labels: GOTO L1 -> GOTO 20
     */
    private String resolveGotoLabels(String line, Map<String, Integer> labelMap) {
        Pattern gotoPattern = Pattern.compile("GOTO\\s+(\\w+)");
        Matcher matcher = gotoPattern.matcher(line);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String label = matcher.group(1);
            Integer lineNumber = labelMap.get(label);
            
            if (lineNumber != null) {
                matcher.appendReplacement(result, "GOTO " + lineNumber);
            } else {
                // Label not found - keep original
                matcher.appendReplacement(result, matcher.group(0));
            }
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
    
    /**
     * Resolve THEN labels: THEN L1 -> THEN 20
     */
    private String resolveThenLabels(String line, Map<String, Integer> labelMap) {
        Pattern thenPattern = Pattern.compile("THEN\\s+(\\w+)");
        Matcher matcher = thenPattern.matcher(line);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String label = matcher.group(1);
            Integer lineNumber = labelMap.get(label);
            
            if (lineNumber != null) {
                matcher.appendReplacement(result, "THEN " + lineNumber);
            } else {
                // Label not found - keep original
                matcher.appendReplacement(result, matcher.group(0));
            }
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
    
    // ========== FILE I/O METHODS ==========
    
    /**
     * Save BASIC code to a text file
     * @param basicCode The BASIC code to save
     * @param filename Output filename (e.g., "output.bas")
     */
    public void saveToFile(String basicCode, String filename) throws IOException {
        Files.writeString(Paths.get(filename), basicCode);
        System.out.println("✓ BASIC code saved to: " + filename);
    }
    
    /**
     * Save intermediate code to a text file (for debugging)
     * @param intermediateCode The intermediate code to save
     * @param filename Output filename (e.g., "output_intermediate.txt")
     */
    public void saveIntermediateToFile(String intermediateCode, String filename) throws IOException {
        Files.writeString(Paths.get(filename), intermediateCode);
        System.out.println("✓ Intermediate code saved to: " + filename);
    }
    
    /**
     * Save inlined code to a text file (for debugging)
     * @param inlinedCode The inlined code to save
     * @param filename Output filename (e.g., "output_inlined.txt")
     */
    public void saveInlinedToFile(String inlinedCode, String filename) throws IOException {
        Files.writeString(Paths.get(filename), inlinedCode);
        System.out.println("✓ Inlined code saved to: " + filename);
    }
    
    /**
     * Read SPL source code from a text file
     * @param filename Input filename (e.g., "program.txt")
     * @return The SPL source code as a string
     */
    public static String readFromFile(String filename) throws IOException {
        return Files.readString(Paths.get(filename));
    }
}