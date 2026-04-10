package com.aayan.albcore.condition;

public enum Operator {
    EQUALS,
    NOT_EQUALS,
    GREATER_THAN,
    LESS_THAN,
    GREATER_THAN_OR_EQUAL,
    LESS_THAN_OR_EQUAL;

    public boolean evaluate(double a, double b) {
        return switch (this) {
            case EQUALS                -> a == b;
            case NOT_EQUALS            -> a != b;
            case GREATER_THAN          -> a > b;
            case LESS_THAN             -> a < b;
            case GREATER_THAN_OR_EQUAL -> a >= b;
            case LESS_THAN_OR_EQUAL    -> a <= b;
        };
    }

    public boolean evaluate(String a, String b) {
        return switch (this) {
            case EQUALS     -> a.equals(b);
            case NOT_EQUALS -> !a.equals(b);
            default -> {
                java.util.logging.Logger.getLogger("ALBCore")
                        .warning("[ALBCore | Operator] Operator " + this
                                + " not supported for string comparison — returning false");
                yield false;
            }
        };
    }
}