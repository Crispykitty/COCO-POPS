package frontend.parser;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private Map<Integer, SymbolEntry> table = new HashMap<>();

    public static class SymbolEntry {
        public int nodeId;
        public String name;
        public String scope; // e.g., "Global", "Local", "Main", "Everywhere"
        public String type; // e.g., "variable", "procedure", "function", "parameter"
        public int paramCount; // For procedures/functions

        public SymbolEntry(int nodeId, String name, String scope, String type, int paramCount) {
            this.nodeId = nodeId;
            this.name = name;
            this.scope = scope;
            this.type = type;
            this.paramCount = paramCount;
        }

        @Override
        public String toString() {
            return String.format("ID: %d, Name: %s, Scope: %s, Type: %s, Params: %d",
                    nodeId, name, scope, type, paramCount);
        }
    }

    public void addEntry(int nodeId, String name, String scope, String type, int paramCount) {
        table.put(nodeId, new SymbolEntry(nodeId, name, scope, type, paramCount));
    }

    public SymbolEntry getEntry(int nodeId) {
        return table.get(nodeId);
    }

    public Map<Integer, SymbolEntry> getTable() {
        return new HashMap<>(table);
    }
}