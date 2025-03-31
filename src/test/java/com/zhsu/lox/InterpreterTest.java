package com.zhsu.lox;

import java.util.List;

import org.junit.jupiter.api.Test;

public class InterpreterTest {

  @Test
  public void testInterpreter() {
    String input = """
        class Doughnut {
          cook() {
            print "Fry until golden brown.";
          }
        }

        class BostonCream < Doughnut {}

        BostonCream().cook();
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
