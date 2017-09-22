package com.computer.nand2tetris.compiler.tokenizer;

import com.computer.nand2tetris.compiler.JackToken;
import com.computer.nand2tetris.compiler.JackToken.TokenType;
import com.google.common.collect.ImmutableSet;

class IdentifierOrKeywordTokenExtractor implements JackTokenExtractor {

  private static final ImmutableSet<String> KEYWORDS = ImmutableSet.of(
      "class", "constructor", "function",
      "method", "field", "static", "var",
      "int", "char", "boolean", "void", "true",
      "false", "null", "this", "let", "do",
      "if", "else", "while", "return");

  private static boolean isKeyword(String tokenText) {
    return KEYWORDS.contains(tokenText);
  }

  private static boolean isLeadingIdentifierCharacter(Character lookAhead) {
    return Character.isLetter(lookAhead) || lookAhead.charValue() == '_';
  }

  private static boolean isIdentifierCharacter(Character lookAhead) {
    return isLeadingIdentifierCharacter(lookAhead) || Character.isDigit(lookAhead);
  }

  @Override
  public JackToken extractToken(LookAheadStream<Character> lookAheadStream) {
    StringBuilder builder = new StringBuilder();

    while (lookAheadStream.peek().isPresent() && isIdentifierCharacter(
        lookAheadStream.peek().get())) {
      builder.append(lookAheadStream.extract().get());
    }

    String tokenText = builder.toString();
    TokenType tokenType = isKeyword(tokenText) ? TokenType.KEYWORD : TokenType.IDENTIFIER;
    return JackToken.create(tokenType, tokenText);
  }

  @Override
  public boolean matches(Character lookAhead) {
    return isLeadingIdentifierCharacter(lookAhead);
  }
}
