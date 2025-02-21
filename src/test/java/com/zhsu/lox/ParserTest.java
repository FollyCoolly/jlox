package com.zhsu.lox;

import java.util.List;

import org.junit.jupiter.api.Test;

public class ParserTest {

    @Test
    public void testScanner() {
        String input = "1 ? 2 ? 3: 4: 5, 2 * 3";

        Scanner scanner = new Scanner(input);

        List<Token> tokens = scanner.scanTokens();

        Parser parser = new Parser(tokens);
        Expr expression = parser.parse();

        System.out.println(new AstPrinter().print(expression));
    }
}
