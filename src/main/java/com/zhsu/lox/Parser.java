package com.zhsu.lox;

import java.util.List;
import java.util.Stack;
import java.util.function.Supplier;

import static com.zhsu.lox.TokenType.BANG;
import static com.zhsu.lox.TokenType.BANG_EQUAL;
import static com.zhsu.lox.TokenType.CLASS;
import static com.zhsu.lox.TokenType.COLON;
import static com.zhsu.lox.TokenType.COMMA;
import static com.zhsu.lox.TokenType.EOF;
import static com.zhsu.lox.TokenType.EQUAL_EQUAL;
import static com.zhsu.lox.TokenType.FALSE;
import static com.zhsu.lox.TokenType.FOR;
import static com.zhsu.lox.TokenType.FUN;
import static com.zhsu.lox.TokenType.GREATER;
import static com.zhsu.lox.TokenType.GREATER_EQUAL;
import static com.zhsu.lox.TokenType.IF;
import static com.zhsu.lox.TokenType.LEFT_PAREN;
import static com.zhsu.lox.TokenType.LESS;
import static com.zhsu.lox.TokenType.LESS_EQUAL;
import static com.zhsu.lox.TokenType.MINUS;
import static com.zhsu.lox.TokenType.NIL;
import static com.zhsu.lox.TokenType.NUMBER;
import static com.zhsu.lox.TokenType.PLUS;
import static com.zhsu.lox.TokenType.PRINT;
import static com.zhsu.lox.TokenType.QUESTION;
import static com.zhsu.lox.TokenType.RETURN;
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

    Expr parse() {
        try {
            return expression();
        } catch (ParseError error) {
            return null;
        }
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
        return parseLeftAssociativeBinary(this::conditional, COMMA);
    }

    private Expr conditional() {
        Expr expr = equality();

        Stack<Expr> exprsStack = new Stack<>();
        exprsStack.push(expr);
        Stack<Token> tokenStack = new Stack<>(); // just for tracking error token
        while (match(QUESTION, COLON)) {
            Token token = previous();
            expr = equality();

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

        return primary();
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

        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Expect expression.");
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
