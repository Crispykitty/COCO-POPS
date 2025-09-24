package frontend.parser;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;
import frontend.parser.antlr.SPLParser;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Visual Syntax Tree Extractor for SPL
 * Displays the ANTLR parse tree in a readable, indented format
 */
public class VisualSyntaxTreeExtractor {

    /**
     * Extract and display the syntax tree structure with visual indentation
     */
    public static void displayTree(ParseTree tree, SPLParser parser) {
        System.out.println("=== SPL SYNTAX TREE STRUCTURE ===");
        printTreeStructure(tree, 0, parser);
        System.out.println("=== END TREE STRUCTURE ===\n");
    }

    /**
     * Recursively print tree structure with proper indentation
     */
    private static void printTreeStructure(ParseTree tree, int depth, SPLParser parser) {
        String indent = "│  ".repeat(depth);
        String prefix = depth > 0 ? "├─ " : "";
        
        if (tree instanceof TerminalNode) {
            // Terminal nodes (actual tokens)
            System.out.println(indent + prefix + "TOKEN: \"" + tree.getText() + "\"");
        } else if (tree instanceof ParserRuleContext) {
            // Rule nodes
            ParserRuleContext ruleContext = (ParserRuleContext) tree;
            String ruleName = parser.getRuleNames()[ruleContext.getRuleIndex()];
            System.out.println(indent + prefix + "RULE: " + ruleName.toUpperCase());
            
            // Print all children
            for (int i = 0; i < tree.getChildCount(); i++) {
                printTreeStructure(tree.getChild(i), depth + 1, parser);
            }
        }
    }

    /**
     * Extract tree with node IDs and positions for symbol table linking
     */
    public static void displayTreeWithNodeIds(ParseTree tree, SPLParser parser) {
        System.out.println("=== SPL SYNTAX TREE WITH NODE IDS ===");
        NodeIdCounter counter = new NodeIdCounter();
        printTreeWithIds(tree, 0, parser, counter);
        System.out.println("=== END TREE WITH IDS ===\n");
        System.out.println("Total nodes processed: " + counter.getCount());
    }

    private static void printTreeWithIds(ParseTree tree, int depth, SPLParser parser, NodeIdCounter counter) {
        String indent = "│  ".repeat(depth);
        String prefix = depth > 0 ? "├─ " : "";
        int nodeId = counter.getNextId();
        
        if (tree instanceof TerminalNode) {
            System.out.println(indent + prefix + "[ID:" + nodeId + "] TOKEN: \"" + tree.getText() + "\"");
        } else if (tree instanceof ParserRuleContext) {
            ParserRuleContext ruleContext = (ParserRuleContext) tree;
            String ruleName = parser.getRuleNames()[ruleContext.getRuleIndex()];
            System.out.println(indent + prefix + "[ID:" + nodeId + "] RULE: " + ruleName.toUpperCase());
            
            // Recursively process children
            for (int i = 0; i < tree.getChildCount(); i++) {
                printTreeWithIds(tree.getChild(i), depth + 1, parser, counter);
            }
        }
    }

    /**
     * Extract tree as a hierarchical text representation
     */
    public static String extractTreeAsText(ParseTree tree, SPLParser parser) {
        StringBuilder sb = new StringBuilder();
        buildTextTree(tree, sb, 0, parser);
        return sb.toString();
    }

    private static void buildTextTree(ParseTree tree, StringBuilder sb, int depth, SPLParser parser) {
        String indent = "  ".repeat(depth);
        
        if (tree instanceof TerminalNode) {
            sb.append(indent).append("\"").append(tree.getText()).append("\"\n");
        } else if (tree instanceof ParserRuleContext) {
            ParserRuleContext ruleContext = (ParserRuleContext) tree;
            String ruleName = parser.getRuleNames()[ruleContext.getRuleIndex()];
            sb.append(indent).append(ruleName).append("\n");
            
            for (int i = 0; i < tree.getChildCount(); i++) {
                buildTextTree(tree.getChild(i), sb, depth + 1, parser);
            }
        }
    }

    /**
     * Extract tree with Node IDs (needed for symbol table)
     * Note: For actual symbol table implementation, you'll need to create
     * your own mapping from nodes to IDs since ANTLR contexts don't
     * directly support user objects in this version.
     */
    public static void assignNodeIds(ParseTree tree) {
        // This method demonstrates the concept but doesn't modify the tree
        // For symbol table work, create a HashMap<ParseTree, Integer> mapping
        System.out.println("Node ID assignment completed conceptually.");
        System.out.println("For symbol table work, create: Map<ParseTree, Integer> nodeIds = new HashMap<>();");
    }

    /**
     * Helper class for node ID generation
     */
    private static class NodeIdCounter {
        private int count = 1;
        
        public int getNextId() {
            return count++;
        }
        
        public int getCount() {
            return count - 1;
        }
    }
}