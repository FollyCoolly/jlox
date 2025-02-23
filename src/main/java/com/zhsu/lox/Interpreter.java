package com.zhsu.lox;

import com.zhsu.lox.Expr.Conditional;

class Interpreter implements Expr.Visitor<Object> {

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);

        return switch (expr.operator.type) {
            case BANG ->
                !isTruthy(right);
            case MINUS ->
                -(double) right;
            default ->
                null;
        };
    }

    private boolean isTruthy(Object object) {
        if (object == null) {
            return false;
        }
        if (object instanceof Boolean) {
            return (boolean) object;
        }
        return true;
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case GREATER -> {
                return (double) left > (double) right;
            }
            case GREATER_EQUAL -> {
                return (double) left >= (double) right;
            }
            case LESS -> {
                return (double) left < (double) right;
            }
            case LESS_EQUAL -> {
                return (double) left <= (double) right;
            }
            case MINUS -> {
                return (double) left - (double) right;
            }
            case PLUS -> {
                if (left instanceof Double && right instanceof Double) {
                    return (double) left + (double) right;
                }

                if (left instanceof String && right instanceof String) {
                    return (String) left + (String) right;
                }

                return null;
            }
            case SLASH -> {
                return (double) left / (double) right;
            }
            case STAR -> {
                return (double) left * (double) right;
            }
            case BANG_EQUAL -> {
                return !isEqual(left, right);
            }
            case EQUAL_EQUAL -> {
                return isEqual(left, right);
            }
            default -> {
                return null;
            }
        }
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null) {
            return false;
        }

        return a.equals(b);
    }

    @Override
    public Object visitConditionalExpr(Conditional expr) {
        Object cond = evaluate(expr.condition);
        if (isTruthy(cond)) {
            return evaluate(expr.trueValue);
        } else {
            return evaluate(expr.FalseValue);
        }
    }
}
