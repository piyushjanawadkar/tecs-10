package com.computer.nand2tetris.compiler.tokenizer;

import com.computer.nand2tetris.compiler.JackToken;
import com.computer.nand2tetris.compiler.JackToken.TokenType;

public class WhitespaceTokenExtractor implements JackTokenExtractor {

  @Override
  public JackToken extractToken(LookAheadStream<Character> lookAheadStream) {
    while (lookAheadStream.peek().isPresent() && matches(lookAheadStream.peek().get())) {
      lookAheadStream.extract();
    }
    return JackToken.create(TokenType.WHITESPACE, "");
  }

  @Override
  public boolean matches(Character lookAhead) {
    return Character.isWhitespace(lookAhead);
  }
}
