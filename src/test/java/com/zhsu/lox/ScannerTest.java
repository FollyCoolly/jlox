package com.zhsu.lox;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class ScannerTest {

    @Test
    public void testScanner() {
        String input = "a = b * 5 + 1";

        Scanner scanner = new Scanner(input);

        List<Token> tokens = scanner.scanTokens();

        for (Token token : tokens) {
            System.out.println(token);
        }
    }
}
