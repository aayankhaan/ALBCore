package com.aayan.albcore.effect.registry;

import java.util.Map;

public final class EffectAction {

    private final String type;
    private final Map<String, Object> params;

    public EffectAction(String type, Map<String, Object> params) {
        this.type = type.toUpperCase();
        this.params = params;
    }

    public String getType() { return type; }
    public Map<String, Object> getParams() { return params; }

    public String getString(String key, String def) {
        Object val = params.get(key);
        return val != null ? val.toString() : def;
    }

    public double getDouble(String key, double def) {
        Object val = params.get(key);
        if (val instanceof Number n) return n.doubleValue();
        if (val instanceof String s) {
            try { return Double.parseDouble(s); } catch (Exception e) { return def; }
        }
        return def;
    }

    public int getInt(String key, int def) {
        Object val = params.get(key);
        if (val instanceof Number n) return n.intValue();
        if (val instanceof String s) {
            try { return Integer.parseInt(s); } catch (Exception e) { return def; }
        }
        return def;
    }

    public float getFloat(String key, float def) {
        Object val = params.get(key);
        if (val instanceof Number n) return n.floatValue();
        if (val instanceof String s) {
            try { return Float.parseFloat(s); } catch (Exception e) { return def; }
        }
        return def;
    }

    public boolean getBool(String key, boolean def) {
        Object val = params.get(key);
        if (val instanceof Boolean b) return b;
        if (val instanceof String s) return Boolean.parseBoolean(s);
        return def;
    }

    /**
     * Evaluate a math expression that may contain "level".
     * Supports simple expressions like "8 * level", "level + 5", "10".
     */
    public double evaluateExpression(String key, int level, double def) {
        Object val = params.get(key);
        if (val == null) return def;
        String expr = val.toString().trim().replace("level", String.valueOf(level));
        try {
            // Simple eval: supports +, -, *, /
            return evalSimple(expr);
        } catch (Exception e) {
            return def;
        }
    }

    private double evalSimple(String expr) {
        expr = expr.replaceAll("\\s+", "");

        // Handle addition/subtraction (lowest precedence)
        int lastPlus = -1, lastMinus = -1;
        int depth = 0;
        for (int i = expr.length() - 1; i >= 0; i--) {
            char c = expr.charAt(i);
            if (c == ')') depth++;
            else if (c == '(') depth--;
            else if (depth == 0) {
                if (c == '+' && i > 0) lastPlus = i;
                else if (c == '-' && i > 0) lastMinus = i;
            }
        }
        if (lastPlus > 0) return evalSimple(expr.substring(0, lastPlus)) + evalSimple(expr.substring(lastPlus + 1));
        if (lastMinus > 0) return evalSimple(expr.substring(0, lastMinus)) - evalSimple(expr.substring(lastMinus + 1));

        // Handle multiplication/division
        int lastMul = -1, lastDiv = -1;
        depth = 0;
        for (int i = expr.length() - 1; i >= 0; i--) {
            char c = expr.charAt(i);
            if (c == ')') depth++;
            else if (c == '(') depth--;
            else if (depth == 0) {
                if (c == '*') lastMul = i;
                else if (c == '/') lastDiv = i;
            }
        }
        if (lastMul > 0) return evalSimple(expr.substring(0, lastMul)) * evalSimple(expr.substring(lastMul + 1));
        if (lastDiv > 0) return evalSimple(expr.substring(0, lastDiv)) / evalSimple(expr.substring(lastDiv + 1));

        // Parentheses
        if (expr.startsWith("(") && expr.endsWith(")")) return evalSimple(expr.substring(1, expr.length() - 1));

        return Double.parseDouble(expr);
    }
}