package com.computer.nand2tetris.compiler.parser;

import com.computer.nand2tetris.compiler.JackElementVisitor;
import com.computer.nand2tetris.compiler.JackToken;
import com.computer.nand2tetris.compiler.LookAheadStream;
import com.google.auto.value.AutoValue;

@AutoValue
abstract class TokensVisitor {

  static TokensVisitor create(LookAheadStream<JackToken> tokens, JackElementVisitor visitor) {
    return new AutoValue_TokensVisitor(tokens, visitor);
  }

  abstract LookAheadStream<JackToken> tokens();
  abstract JackElementVisitor visitor();

  NonTerminalVisitingParser nonTerminalParserOf(String tokenText, String tokenDescription) {
    return NonTerminalVisitingParser.of(tokenText, tokenDescription, tokens(), visitor());
  }

  NonTerminalVisitingParser nonTerminalParserOf(String tokenText) {
    return nonTerminalParserOf(tokenText, tokenText);
  }
}
