package com.zhsu.lox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.function.Supplier;

import static com.zhsu.lox.TokenType.AND;
import static com.zhsu.lox.TokenType.BANG;
import static com.zhsu.lox.TokenType.BANG_EQUAL;
import static com.zhsu.lox.TokenType.CLASS;
import static com.zhsu.lox.TokenType.COLON;
import static com.zhsu.lox.TokenType.COMMA;
import static com.zhsu.lox.TokenType.DOT;
import static com.zhsu.lox.TokenType.ELSE;
import static com.zhsu.lox.TokenType.EOF;
import static com.zhsu.lox.TokenType.EQUAL;
import static com.zhsu.lox.TokenType.EQUAL_EQUAL;
import static com.zhsu.lox.TokenType.FALSE;
import static com.zhsu.lox.TokenType.FOR;
import static com.zhsu.lox.TokenType.FUN;
import static com.zhsu.lox.TokenType.GREATER;
import static com.zhsu.lox.TokenType.GREATER_EQUAL;
import static com.zhsu.lox.TokenType.IDENTIFIER;
import static com.zhsu.lox.TokenType.IF;
import static com.zhsu.lox.TokenType.LEFT_BRACE;
import static com.zhsu.lox.TokenType.LEFT_PAREN;
import static com.zhsu.lox.TokenType.LESS;
import static com.zhsu.lox.TokenType.LESS_EQUAL;
import static com.zhsu.lox.TokenType.MINUS;
import static com.zhsu.lox.TokenType.NIL;
import static com.zhsu.lox.TokenType.NUMBER;
import static com.zhsu.lox.TokenType.OR;
import static com.zhsu.lox.TokenType.PLUS;
import static com.zhsu.lox.TokenType.PRINT;
import static com.zhsu.lox.TokenType.QUESTION;
import static com.zhsu.lox.TokenType.RETURN;
import static com.zhsu.lox.TokenType.RIGHT_BRACE;
import static com.zhsu.lox.TokenType.RIGHT_PAREN;
import static com.zhsu.lox.TokenType.SEMICOLON;
import static com.zhsu.lox.TokenType.SLASH;
import static com.zhsu.lox.TokenType.STAR;
import static com.zhsu.lox.TokenType.STRING;
import static com.zhsu.lox.TokenType.TRUE;
import static com.zhsu.lox.TokenType.VAR;
import static com.zhsu.lox.TokenType.WHILE;

class Parser {

    private static class ParseError extends RuntimeException {
    }

    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(declaration());
        }

        return statements;
    }

    private Stmt statement() {
        if (match(FOR)) {
            return forStatement();
        }
        if (match(IF)) {
            return ifStatement();
        }
        if (match(PRINT)) {
            return printStatement();
        }
        if (match(RETURN)) {
            return returnStatement();
        }
        if (match(WHILE)) {
            return whileStatement();
        }
        if (match(LEFT_BRACE)) {
            return new Stmt.Block(block());
        }

        return expressionStatement();
    }

    private Stmt forStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'for'.");

        Stmt initializer;
        if (match(SEMICOLON)) {
            initializer = null;
        } else if (match(VAR)) {
            initializer = varDeclaration();
        } else {
            initializer = expressionStatement();
        }

        Expr condition = null;
        if (!check(SEMICOLON)) {
            condition = expression();
        }
        consume(SEMICOLON, "Expect ';' after loop condition.");

        Expr increment = null;
        if (!check(RIGHT_PAREN)) {
            increment = expression();
        }
        consume(RIGHT_PAREN, "Expect ')' after for clauses.");
        Stmt body = statement();

        if (increment != null) {
            body = new Stmt.Block(
                    Arrays.asList(
                            body,
                            new Stmt.Expression(increment)));
        }

        if (condition == null) {
            condition = new Expr.Literal(true);
        }
        body = new Stmt.While(condition, body);

        if (initializer != null) {
            body = new Stmt.Block(Arrays.asList(initializer, body));
        }

        return body;
    }

    private Stmt declaration() {
        try {
            if (match(VAR)) {
                return varDeclaration();
            }
            if (match(CLASS)) {
                return classDeclaration();
            }
            if (match(FUN)) {
                return function("function");
            }

            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    private Stmt classDeclaration() {
        Token name = consume(IDENTIFIER, "Expect class name.");
        consume(LEFT_BRACE, "Expect '{' before class body.");

        List<Stmt.Function> methods = new ArrayList<>();
        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            methods.add(function("method"));
        }

        consume(RIGHT_BRACE, "Expect '}' after class body.");

        return new Stmt.Class(name, methods);
    }

    private Stmt varDeclaration() {
        Token name = consume(IDENTIFIER, "Expect variable name.");

        Expr initializer = null;
        if (match(EQUAL)) {
            initializer = expression();
        }

        consume(SEMICOLON, "Expect ';' after variable declaration.");
        return new Stmt.Var(name, initializer);
    }

    private Stmt whileStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'while'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after condition.");
        Stmt body = statement();

        return new Stmt.While(condition, body);
    }

    private Stmt ifStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'if'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after if condition.");

        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if (match(ELSE)) {
            elseBranch = statement();
        }

        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    private Stmt printStatement() {

        Expr value = expression();
        consume(SEMICOLON, "Expect ';' after value. for print stmt");
        return new Stmt.Print(value);
    }

    private Stmt returnStatement() {
        Token keyword = previous();
        Expr value = null;
        if (!check(SEMICOLON)) {
            value = expression();
        }

        consume(SEMICOLON, "Expect ';' after return value.");
        return new Stmt.Return(keyword, value);
    }

    private Stmt expressionStatement() {
        Expr expr = expression();
        if (match(SEMICOLON)) {
            return new Stmt.Expression(expr);
        }
        // consume(SEMICOLON, "Expect ';' after expression.");
        return new Stmt.Print(expr); // treat single expr as print stmt
    }

    private Stmt.Function function(String kind) {
        Token name = consume(IDENTIFIER, "Expect " + kind + " name.");
        consume(LEFT_PAREN, "Expect '(' after " + kind + " name.");
        List<Token> parameters = new ArrayList<>();
        if (!check(RIGHT_PAREN)) {
            do {
                if (parameters.size() >= 255) {
                    error(peek(), "Can't have more than 255 parameters.");
                }

                parameters.add(
                        consume(IDENTIFIER, "Expect parameter name."));
            } while (match(COMMA));
        }
        consume(RIGHT_PAREN, "Expect ')' after parameters.");

        // Consuming LEFT_BRACE here lets us report a more precise error message if the LEFT_BRACE isn’t found 
        consume(LEFT_BRACE, "Expect '{' before " + kind + " body.");
        List<Stmt> body = block();
        return new Stmt.Function(name, parameters, body);
    }

    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();

        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }

        consume(RIGHT_BRACE, "Expect '}' after block.");
        return statements;
    }

    private Expr expression() {
        return commaed();
    }

    private Expr parseLeftAssociativeBinary(Supplier<Expr> operandParser, TokenType... operators) {
        Expr expr = operandParser.get();

        while (match(operators)) {
            Token operator = previous();
            Expr right = operandParser.get();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr commaed() {
        return parseLeftAssociativeBinary(this::assignment, COMMA);
    }

    private Expr assignment() {
        Expr expr = conditional();

        if (match(EQUAL)) {
            Token equals = previous();
            Expr value = assignment(); // assignment is right-associative

            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable) expr).name;
                return new Expr.Assign(name, value);
            } else if (expr instanceof Expr.Get) {
                Expr.Get get = (Expr.Get) expr;
                return new Expr.Set(get.object, get.name, value);
            }

            //  we don’t throw it because the parser isn’t in a confused state where we need to go into panic mode and synchronize.
            error(equals, "Invalid assignment target.");
        }

        return expr;
    }

    private Expr conditional() {
        Expr expr = or();

        Stack<Expr> exprsStack = new Stack<>();
        exprsStack.push(expr);
        Stack<Token> tokenStack = new Stack<>(); // just for tracking error token
        while (match(QUESTION, COLON)) {
            Token token = previous();
            expr = or();

            if (token.type == QUESTION) {
                exprsStack.push(expr);
                tokenStack.push(token);
            } else {
                if (!tokenStack.empty()) {
                    Expr trueValue = exprsStack.pop();
                    tokenStack.pop();
                    Expr conditon = exprsStack.pop();
                    Expr conditonal = new Expr.Conditional(conditon, trueValue, expr);
                    exprsStack.push(conditonal);
                } else {
                    throw error(token, "can not find corresponding '?' for ':'");
                }
            }
        }

        if (!tokenStack.empty()) {
            throw error(tokenStack.pop(), "can not find corresponding ':' for '?'");
        }
        return exprsStack.pop();
    }

    private Expr or() {
        Expr expr = and();

        while (match(OR)) {
            Token operator = previous();
            Expr right = and();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr and() {
        Expr expr = equality();

        while (match(AND)) {
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr equality() {
        return parseLeftAssociativeBinary(this::comparison, BANG_EQUAL, EQUAL_EQUAL);
    }

    private Expr comparison() {
        return parseLeftAssociativeBinary(this::term, GREATER, GREATER_EQUAL, LESS, LESS_EQUAL);
    }

    private Expr term() {
        return parseLeftAssociativeBinary(this::factor, PLUS, MINUS);
    }

    private Expr factor() {
        return parseLeftAssociativeBinary(this::unary, STAR, SLASH);
    }

    private Expr unary() {
        if (match(BANG, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        return call();
    }

    private Expr call() {
        Expr expr = primary();

        while (true) {
            if (match(LEFT_PAREN)) {
                expr = finishCall(expr);
            } else if (match(DOT)) {
                Token name = consume(IDENTIFIER,
                        "Expect property name after '.'.");
                expr = new Expr.Get(expr, name);
            } else {
                break;
            }
        }

        return expr;
    }

    private Expr finishCall(Expr callee) {
        List<Expr> arguments = new ArrayList<>();
        if (!check(RIGHT_PAREN)) {
            do {
                if (arguments.size() >= 255) {
                    error(peek(), "Can't have more than 255 arguments.");
                }
                arguments.add(assignment()); // skip parsing commaed
            } while (match(COMMA));
        }
        Token paren = consume(RIGHT_PAREN,
                "Expect ')' after arguments.");

        return new Expr.Call(callee, paren, arguments);
    }

    private Expr primary() {
        if (match(FALSE)) {
            return new Expr.Literal(false);
        }
        if (match(TRUE)) {
            return new Expr.Literal(true);
        }
        if (match(NIL)) {
            return new Expr.Literal(null);
        }

        if (match(NUMBER, STRING)) {
            return new Expr.Literal(previous().literal);
        }

        if (match(IDENTIFIER)) {
            return new Expr.Variable(previous());
        }

        if (match(FUN)) {
            return lambda();
        }

        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Expect expression.");
    }

    private Expr lambda() {
        consume(LEFT_PAREN, "Expect '(' after 'fun'.");
        List<Token> parameters = new ArrayList<>();
        if (!check(RIGHT_PAREN)) {
            do {
                if (parameters.size() >= 255) {
                    error(peek(), "Can't have more than 255 parameters.");
                }
                parameters.add(consume(IDENTIFIER, "Expect parameter name."));
            } while (match(COMMA));
        }
        consume(RIGHT_PAREN, "Expect ')' after parameters.");
        consume(LEFT_BRACE, "Expect '{' before function body.");
        List<Stmt> body = block();

        return new Expr.Lambda(parameters, body);
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) {
            return advance();
        }

        throw error(peek(), message);
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) {
            return false;
        }
        return peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd()) {
            current++;
        }
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
    }

    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) {
                return;
            }

            switch (peek().type) {
                case CLASS, FUN, VAR, FOR, IF, WHILE, PRINT, RETURN -> {
                    return;
                }
                default -> {
                }
            }

            advance();
        }
    }

}
