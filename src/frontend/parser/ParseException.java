package frontend.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom exception for parsing errors
 */
public class ParseException extends Exception {
    private List<String> errors;

    public ParseException(String message) {
        super(message);
        this.errors = new ArrayList<>();
    }

    public ParseException(String message, List<String> errors) {
        super(message);
        this.errors = new ArrayList<>(errors);
    }

    public ParseException(String message, Throwable cause) {
        super(message, cause);
        this.errors = new ArrayList<>();
    }

    public List<String> getErrors() {
        return errors;
    }
}