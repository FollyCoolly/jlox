package com.zhsu.lox;

import java.util.List;

import org.junit.jupiter.api.Test;

public class InterpreterTest {

    @Test
    public void testInterpreter() {
        String input = """
fun thrice(fn) {
  for (var i = 1; i <= 3; i = i + 1) {
    fn(i);
  }
}

thrice(fun (a) {
  print a;
});
            """;

        Scanner scanner = new Scanner(input);

        List<Token> tokens = scanner.scanTokens();

        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();

        Interpreter interpreter = new Interpreter();
        interpreter.interpret(statements);
    }
}
