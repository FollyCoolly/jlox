package com.zhsu.lox;

abstract class Expr {

    abstract <R> R accept(Visitor<R> visitor);

    interface Visitor<R> {

        R visitBinaryExpr(Binary expr);

        R visitGroupingExpr(Grouping expr);

        R visitLiteralExpr(Literal expr);

        R visitUnaryExpr(Unary expr);

        R visitConditionalExpr(Conditional expr);
    }

    static class Binary extends Expr {

        Binary(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitBinaryExpr(this);
        }

        final Expr left;
        final Token operator;
        final Expr right;
    }

    static class Grouping extends Expr {

        Grouping(Expr expression) {
            this.expression = expression;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitGroupingExpr(this);
        }

        final Expr expression;
    }

    static class Literal extends Expr {

        Literal(Object value) {
            this.value = value;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitLiteralExpr(this);
        }

        final Object value;
    }

    static class Unary extends Expr {

        Unary(Token operator, Expr right) {
            this.operator = operator;
            this.right = right;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitUnaryExpr(this);
        }

        final Token operator;
        final Expr right;
    }

    static class Conditional extends Expr {

        Conditional(Expr condition, Expr trueValue, Expr FalseValue) {
            this.condition = condition;
            this.trueValue = trueValue;
            this.FalseValue = FalseValue;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitConditionalExpr(this);
        }

        final Expr condition;
        final Expr trueValue;
        final Expr FalseValue;
    }

}
