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
    public String visitFunctionStmt(Stmt.Function stmt) {
        return "FunctionStmt" + stmt.toString();
    }

    @Override
    public String visitIfStmt(Stmt.If stmt) {
        StringBuilder builder = new StringBuilder();
        builder.append("(if ");
        builder.append(stmt.condition.accept(this));
        builder.append("then ");
        builder.append(stmt.thenBranch.accept(this));
        if (stmt.elseBranch != null) {
            builder.append("else");
            builder.append(stmt.elseBranch.accept(this));
        }
        return builder.toString();
    }

    @Override
    public String visitPrintStmt(Stmt.Print stmt) {
        return "PrintStmt " + stmt.expression.accept(this);
    }

    @Override
    public String visitReturnStmt(Stmt.Return stmt) {
        return "Return" + stmt.value.accept(this);
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
    public String visitClassStmt(Stmt.Class stmt) {
        return "Class " + stmt.toString();
    }

    @Override
    public String visitWhileStmt(Stmt.While stmt) {
        StringBuilder builder = new StringBuilder();
        builder.append("while");
        builder.append(" (").append(stmt.condition.accept(this)).append(")");
        builder.append(" {").append(stmt.body.accept(this)).append("}");
        return builder.toString();
    }

    @Override
    public String visitBlockStmt(Stmt.Block stmt) {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        for (Stmt innerStmt : stmt.statements) {
            builder.append(innerStmt.accept(this));
        }
        builder.append("}\n");
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
    public String visitLogicalExpr(Expr.Logical expr) {
        return parenthesize(expr.operator.lexeme,
                expr.left, expr.right);
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

    @Override
    public String visitCallExpr(Expr.Call expr) {
        return parenthesize(expr.callee.accept(this), expr.arguments.toArray(new Expr[0]));
    }

    @Override
    public String visitGetExpr(Expr.Get expr) {
        return expr.object.accept(this) + "." + expr.name.lexeme;
    }

    @Override
    public String visitSetExpr(Expr.Set expr) {
        return "set " + expr.object.accept(this) + "." + expr.name.lexeme;
    }

    @Override
    public String visitSuperExpr(Expr.Super expr) {
        return "super";
    }

    @Override
    public String visitThisExpr(Expr.This expr) {
        return "this";
    }

    @Override
    public String visitLambdaExpr(Expr.Lambda expr) {
        return "<anonymous fn>";
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
