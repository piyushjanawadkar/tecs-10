package com.computer.nand2tetris.compiler;

import com.google.common.collect.ImmutableSet;

public class SymbolTokenExtractor implements JackTokenExtractor {

  ImmutableSet<Character> VALID_SYMBOLS = ImmutableSet.of(
      '{', '}', '(', ')', '[', ']', '.',
      ',', ';', '+', '-', '*', '/', '&',
      '|', '<', '>', '=', '~');

  @Override
  public JackToken extractToken(LookAheadStream lookAheadStream) {
    Character symbol = lookAheadStream.extract().get();
    return JackToken.create(JackToken.TokenType.SYMBOL, symbol.toString());
  }

  @Override
  public boolean matches(Character lookAhead) {
    return VALID_SYMBOLS.contains(lookAhead);
  }
}
