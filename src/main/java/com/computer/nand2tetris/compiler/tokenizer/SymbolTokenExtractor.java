package com.computer.nand2tetris.compiler.tokenizer;

import com.computer.nand2tetris.compiler.JackToken;
import com.computer.nand2tetris.compiler.LookAheadStream;
import com.google.common.collect.ImmutableSet;

class SymbolTokenExtractor implements JackTokenExtractor {

  ImmutableSet<Character> VALID_SYMBOLS = ImmutableSet.of(
      '{', '}', '(', ')', '[', ']', '.',
      ',', ';', '+', '-', '*', '/', '&',
      '|', '<', '>', '=', '~');

  @Override
  public JackToken extractToken(LookAheadStream<Character> lookAheadStream) {
    Character symbol = lookAheadStream.extract().get();
    return JackToken.create(JackToken.TokenType.SYMBOL, symbol.toString());
  }

  @Override
  public boolean matches(Character lookAhead) {
    return VALID_SYMBOLS.contains(lookAhead);
  }
}
