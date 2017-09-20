package com.computer.nand2tetris.compiler.tokenizer;

import com.computer.nand2tetris.compiler.JackToken;
import com.google.common.base.Preconditions;

class StringConstantTokenExtractor implements JackTokenExtractor {

  @Override
  public JackToken extractToken(LookAheadStream lookAheadStream) {
    StringBuilder builder = new StringBuilder();

    lookAheadStream.extract(); // consume the leading double quote.
    while (lookAheadStream.peek().isPresent() && !isDoubleQuote(lookAheadStream.peek().get())) {
      builder.append(lookAheadStream.extract().get());
    }

    Preconditions.checkArgument(lookAheadStream.peek().isPresent() && isDoubleQuote(
        lookAheadStream.extract().get()), "Double quote expected.");

    return JackToken.create(JackToken.TokenType.STRING_CONSTANT, builder.toString());
  }

  private static boolean isDoubleQuote(Character c) {
    return c == '"';
  }

  @Override
  public boolean matches(Character lookAhead) {
    return isDoubleQuote(lookAhead);
  }
}
