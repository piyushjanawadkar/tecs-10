package com.computer.nand2tetris.compiler;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class JackToken {

  public enum TokenType {
    WHITESPACE, SYMBOL, INTEGER_CONSTANT, STRING_CONSTANT, KEYWORD, IDENTIFIER,
  }

  public static JackToken create(TokenType tokenType, String tokenText) {
    return new AutoValue_JackToken(tokenType, tokenText);
  }

  public abstract TokenType tokenType();
  public abstract String tokenText();
}
