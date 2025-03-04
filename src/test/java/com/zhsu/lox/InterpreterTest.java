package com.zhsu.lox;

import java.util.List;

import org.junit.jupiter.api.Test;

public class InterpreterTest {

    @Test
    public void testInterpreter() {
        String input = """
                       var a = "global a";\r
                       var b = "global b";\r
                       var c = "global c";\r
                       {\r
                         var a = "outer a";\r
                         var b = "outer b";\r
                         {\r
                           var a = "inner a";\r
                           print a;\r
                           print b;\r
                           print c;\r
                         }\r
                         print a;\r
                         print b;\r
                         print c;\r
                       }\r
                       print a;\r
                       print b;\r
                       print c;""";

        Scanner scanner = new Scanner(input);

        List<Token> tokens = scanner.scanTokens();

        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();

        Interpreter interpreter = new Interpreter();
        interpreter.interpret(statements);
    }
}
