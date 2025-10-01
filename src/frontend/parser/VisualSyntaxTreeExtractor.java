package frontend.parser;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;
import frontend.parser.antlr.SPLParser;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * Visual Syntax Tree Extractor for SPL
 * Displays the ANTLR parse tree in a readable, indented format
 */
public class VisualSyntaxTreeExtractor {

    public static class NodeIdMapping {
        private Map<ParseTree, Integer> nodeIds = new HashMap<>();
        private int count = 1;

        public int getNextId() {
            return count++;
        }

        public void assignId(ParseTree node) {
            nodeIds.put(node, getNextId());
        }

        public Integer getId(ParseTree node) {
            return nodeIds.get(node);
        }

        public Map<ParseTree, Integer> getNodeIds() {
            return new HashMap<>(nodeIds);
        }

        public int getCount() {
            return count - 1;
        }
    }

    public static NodeIdMapping assignNodeIds(ParseTree tree, SPLParser parser) {
        NodeIdMapping mapping = new NodeIdMapping();
        assignNodeIdsRecursive(tree, mapping);
        System.out.println("=== SPL SYNTAX TREE WITH NODE IDS ===");
        printTreeWithIds(tree, 0, parser, mapping);
        System.out.println("=== END TREE WITH IDS ===\n");
        System.out.println("Total nodes processed: " + mapping.getCount());
        return mapping;
    }

    private static void assignNodeIdsRecursive(ParseTree tree, NodeIdMapping mapping) {
        mapping.assignId(tree);
        for (int i = 0; i < tree.getChildCount(); i++) {
            assignNodeIdsRecursive(tree.getChild(i), mapping);
        }
    }

    // FIX: Use the ID from the mapping instead of calling getNextId()
    private static void printTreeWithIds(ParseTree tree, int depth, SPLParser parser, NodeIdMapping mapping) {
        String indent = "│  ".repeat(depth);
        String prefix = depth > 0 ? "├─ " : "";
        Integer nodeId = mapping.getId(tree);  // <-- USE THE EXISTING ID!
        
        if (nodeId == null) {
            throw new IllegalStateException("Node ID not found for tree node: " + tree.getText());
        }
        
        if (tree instanceof TerminalNode) {
            System.out.println(indent + prefix + "[ID:" + nodeId + "] TOKEN: \"" + tree.getText() + "\"");
        } else if (tree instanceof ParserRuleContext) {
            ParserRuleContext ruleContext = (ParserRuleContext) tree;
            String ruleName = parser.getRuleNames()[ruleContext.getRuleIndex()];
            System.out.println(indent + prefix + "[ID:" + nodeId + "] RULE: " + ruleName.toUpperCase());
            
            // Recursively process children
            for (int i = 0; i < tree.getChildCount(); i++) {
                printTreeWithIds(tree.getChild(i), depth + 1, parser, mapping);
            }
        }
    }

    // Keep other methods unchanged
    public static void displayTree(ParseTree tree, SPLParser parser) {
        System.out.println("=== SPL SYNTAX TREE STRUCTURE ===");
        printTreeStructure(tree, 0, parser);
        System.out.println("=== END TREE STRUCTURE ===\n");
    }

    private static void printTreeStructure(ParseTree tree, int depth, SPLParser parser) {
        String indent = "│  ".repeat(depth);
        String prefix = depth > 0 ? "├─ " : "";
        
        if (tree instanceof TerminalNode) {
            System.out.println(indent + prefix + "TOKEN: \"" + tree.getText() + "\"");
        } else if (tree instanceof ParserRuleContext) {
            ParserRuleContext ruleContext = (ParserRuleContext) tree;
            String ruleName = parser.getRuleNames()[ruleContext.getRuleIndex()];
            System.out.println(indent + prefix + "RULE: " + ruleName.toUpperCase());
            
            for (int i = 0; i < tree.getChildCount(); i++) {
                printTreeStructure(tree.getChild(i), depth + 1, parser);
            }
        }
    }

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
}