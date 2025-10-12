package frontend.semantic;

import java.util.ArrayList;
import java.util.List;

/**
 * Semantic Exception for SPL
 * Thrown when semantic errors are detected
 */
public class SemanticException extends Exception {
    
    private List<String> errors;
    
    public SemanticException(String message) {
        super(message);
        this.errors = new ArrayList<>();
        this.errors.add(message);
    }
    
    public SemanticException(List<String> errors) {
        super("Semantic errors detected: " + errors.size());
        this.errors = new ArrayList<>(errors);
    }
    
    public List<String> getErrors() {
        return new ArrayList<>(errors);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SemanticException: ").append(errors.size()).append(" error(s)\n");
        for (int i = 0; i < errors.size(); i++) {
            sb.append("  ").append(i + 1).append(". ").append(errors.get(i)).append("\n");
        }
        return sb.toString();
    }
}