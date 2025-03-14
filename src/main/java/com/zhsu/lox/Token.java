package com.zhsu.lox;

class Token {

    final TokenType type;
    final String lexeme;
    final Object literal;
    final int line;

    Token(TokenType type, String lexeme, Object literal, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    @Override
    public String toString() {
        if (literal == null) {
            return type + " " + lexeme;
        }
        return type + " " + lexeme + " " + literal;
    }
}
