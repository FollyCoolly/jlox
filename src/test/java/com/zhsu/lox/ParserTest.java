package com.zhsu.lox;

import java.util.List;

import org.junit.jupiter.api.Test;

public class ParserTest {

    @Test
    public void testScanner() {
        String input = "8 == 1 * 5 + 1";

        Scanner scanner = new Scanner(input);

        List<Token> tokens = scanner.scanTokens();

        Parser parser = new Parser(tokens);
        Expr expression = parser.parse();

        System.out.println(new AstPrinter().print(expression));
    }
}
