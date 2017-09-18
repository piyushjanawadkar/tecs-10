package com.computer.nand2tetris.compiler;

public class IntegerConstantTokenExtractor implements JackTokenExtractor {

  private static int extractDigit(LookAheadStream lookAheadStream) {
    return Character.digit(lookAheadStream.extract().get(), 10);
  }

  @Override
  public JackToken extractToken(LookAheadStream lookAheadStream) {
    int value = 0;
    while (lookAheadStream.peek().isPresent() && Character.isDigit(lookAheadStream.peek().get())) {
      value = value * 10 + extractDigit(lookAheadStream);
    }
    return JackToken.create(JackToken.TokenType.INTEGER_CONSTANT, Integer.toString(value));
  }

  @Override
  public boolean matches(Character lookAhead) {
    return Character.isDigit(lookAhead);
  }
}
