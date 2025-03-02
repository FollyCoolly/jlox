package com.zhsu.lox;

public class AstPrinter implements Expr.Visitor<String>, Stmt.Visitor<String> {

    String print(Expr expr) {
        if (expr == null) {
            return "[null expression]";
        }
        return expr.accept(this);
    }

    String print(Stmt stmt) {
        if (stmt == null) {
            return "[null expression]";
        }
        return stmt.accept(this);
    }

    @Override
    public String visitExpressionStmt(Stmt.Expression stmt) {
        return "ExpressionStmt " + stmt.expression.accept(this);
    }

    @Override
    public String visitPrintStmt(Stmt.Print stmt) {
        return "PrintStmt " + stmt.expression.accept(this);
    }

    @Override
    public String visitVarStmt(Stmt.Var stmt) {
        StringBuilder builder = new StringBuilder();
        builder.append("VarStmt ").append(stmt.name.toString());
        if (stmt.initializer != null) {
            builder.append("=");
            builder.append(stmt.initializer.accept(this));
        }
        return builder.toString();
    }

    @Override
    public String visitAssignExpr(Expr.Assign expr) {
        return "(" + expr.name.toString() + "=" + expr.value.toString() + ")";
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return parenthesize(expr.operator.lexeme,
                expr.left, expr.right);
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return parenthesize("group", expr.expression);
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        if (expr.value == null) {
            return "nil";
        }
        return expr.value.toString();
    }

    @Override
    public String visitVariableExpr(Expr.Variable expr) {
        return expr.name.toString();
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return parenthesize(expr.operator.lexeme, expr.right);
    }

    @Override
    public String visitConditionalExpr(Expr.Conditional expr) {
        return parenthesize("?:", expr.condition, expr.trueValue, expr.FalseValue);
    }

    private String parenthesize(String name, Expr... exprs) {
        StringBuilder builder = new StringBuilder();

        builder.append("(").append(name);
        for (Expr expr : exprs) {
            builder.append(" ");
            builder.append(expr.accept(this));
        }
        builder.append(")");

        return builder.toString();
    }

}
