package com.zhsu.lox;

import java.util.List;

class LoxFunction implements LoxCallable {

    private final List<Token> params;
    private final List<Stmt> body;
    private final Environment closure;
    private final String name;

    LoxFunction(Stmt.Function declaration, Environment closure) {
        this.name = declaration.name.lexeme;
        this.params = declaration.params;
        this.body = declaration.body;
        this.closure = closure;
    }

    LoxFunction(Expr.Lambda lambda, Environment closure) {
        this.name = null;
        this.params = lambda.params;
        this.body = lambda.body;
        this.closure = closure;
    }

    @Override
    public int arity() {
        return params.size();
    }

    @Override
    public Object call(Interpreter interpreter,
            List<Object> arguments) {
        Environment environment = new Environment(closure);
        for (int i = 0; i < params.size(); i++) {
            environment.define(params.get(i).lexeme,
                    arguments.get(i));
        }

        try {
            interpreter.executeBlock(body, environment);
        } catch (Return returnValue) {
            return returnValue.value;
        }
        return null;
    }

    @Override
    public String toString() {
        return "<fn " + name + ">";
    }

}
