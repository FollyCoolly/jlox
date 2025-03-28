package com.zhsu.lox;

import java.util.List;

import org.junit.jupiter.api.Test;

public class InterpreterTest {

  @Test
  public void testInterpreter() {
    String input = """
        class Math {
          class square(n) {
            return n * n;
          }
        }

        print Math.square(3);
                                    """;

    Scanner scanner = new Scanner(input);

    List<Token> tokens = scanner.scanTokens();

    Parser parser = new Parser(tokens);
    List<Stmt> statements = parser.parse();

    Interpreter interpreter = new Interpreter();

    Resolver resolver = new Resolver(interpreter);
    resolver.resolve(statements);

    interpreter.interpret(statements);
  }
}
