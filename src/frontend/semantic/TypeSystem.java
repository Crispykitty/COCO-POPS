package frontend.semantic;

import java.util.*;

/**
 * Type System for SPL
 * Defines all types and type checking rules according to SPL_Types.pdf
 * FIXED: Corrected > symbol (was incorrectly "<")
 */
public class TypeSystem {
    
    /**
     * SPL Type enumeration
     */
    public enum SPLType {
        NUMERIC,      // For variables, numbers, numeric operations
        BOOLEAN,      // For logical operations and conditions
        COMPARISON,   // For comparison operators (eq, >)
        TYPELESS,     // For procedure/function names
        STRING,       // For string literals
        UNKNOWN       // Error state
    }
    
    /**
     * Get the type of a unary operator
     */
    public SPLType getUnaryOperatorType(String operator) {
        switch (operator) {
            case "neg":
                return SPLType.NUMERIC;
            case "not":
                return SPLType.BOOLEAN;
            default:
                return SPLType.UNKNOWN;
        }
    }
    
    /**
     * Get the type of a binary operator
     * FIXED: Changed "<" to ">" to match SPL grammar
     */
    public SPLType getBinaryOperatorType(String operator) {
        switch (operator) {
            // Numeric operators
            case "plus":
            case "minus":
            case "mult":
            case "div":
                return SPLType.NUMERIC;
            
            // Boolean operators
            case "or":
            case "and":
                return SPLType.BOOLEAN;
            
            // Comparison operators
            case "eq":
            case ">":  // FIXED: Was "<", now correct ">" symbol
                return SPLType.COMPARISON;
            
            default:
                return SPLType.UNKNOWN;
        }
    }
    
    /**
     * Check if a TERM with UNOP is correctly typed
     * Rules from SPL_Types.pdf:
     * - TERM (lhs) is "numeric" if UNOP is "numeric" and TERM (rhs) is "numeric"
     * - TERM (lhs) is "boolean" if UNOP is "boolean" and TERM (rhs) is "boolean"
     */
    public SPLType inferUnaryTermType(String operator, SPLType operandType) {
        SPLType opType = getUnaryOperatorType(operator);
        
        if (opType == SPLType.NUMERIC && operandType == SPLType.NUMERIC) {
            return SPLType.NUMERIC;
        } else if (opType == SPLType.BOOLEAN && operandType == SPLType.BOOLEAN) {
            return SPLType.BOOLEAN;
        }
        
        return SPLType.UNKNOWN;
    }
    
    /**
     * Check if a TERM with BINOP is correctly typed
     * Rules from SPL_Types.pdf:
     * - TERM (lhs) is "numeric" if BINOP is "numeric" and both TERM (rhs) are "numeric"
     * - TERM (lhs) is "boolean" if BINOP is "boolean" and both TERM (rhs) are "boolean"
     * - TERM (lhs) is "boolean" if BINOP is "comparison" and both TERM (rhs) are "numeric"
     */
    public SPLType inferBinaryTermType(String operator, SPLType leftType, SPLType rightType) {
        SPLType opType = getBinaryOperatorType(operator);
        
        // Numeric operations: both operands must be numeric, result is numeric
        if (opType == SPLType.NUMERIC) {
            if (leftType == SPLType.NUMERIC && rightType == SPLType.NUMERIC) {
                return SPLType.NUMERIC;
            }
        }
        
        // Boolean operations: both operands must be boolean, result is boolean
        else if (opType == SPLType.BOOLEAN) {
            if (leftType == SPLType.BOOLEAN && rightType == SPLType.BOOLEAN) {
                return SPLType.BOOLEAN;
            }
        }
        
        // Comparison operations: both operands must be numeric, result is boolean
        else if (opType == SPLType.COMPARISON) {
            if (leftType == SPLType.NUMERIC && rightType == SPLType.NUMERIC) {
                return SPLType.BOOLEAN;
            }
        }
        
        return SPLType.UNKNOWN;
    }
    
    /**
     * Check if types are compatible for assignment
     */
    public boolean areTypesCompatible(SPLType expected, SPLType actual) {
        return expected == actual;
    }
    
    /**
     * Convert type to string for error messages
     */
    public String typeToString(SPLType type) {
        switch (type) {
            case NUMERIC: return "numeric";
            case BOOLEAN: return "boolean";
            case COMPARISON: return "comparison";
            case TYPELESS: return "type-less";
            case STRING: return "string";
            case UNKNOWN: return "unknown";
            default: return "undefined";
        }
    }
}