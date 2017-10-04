package com.computer.nand2tetris.compiler.parser;

import com.computer.nand2tetris.compiler.JackElementVisitor;
import com.computer.nand2tetris.compiler.JackToken;
import com.computer.nand2tetris.compiler.LookAheadStream;

public class TokensVisitor {

  private final LookAheadStream<JackToken> tokens;
  private final JackElementVisitor visitor;

  public TokensVisitor(LookAheadStream<JackToken> tokens, JackElementVisitor visitor) {
    this.tokens = tokens;
    this.visitor = visitor;
  }

  public NonTerminalVisitingParser nonTerminalParserOf(String tokenText, String tokenDescription) {
    return NonTerminalVisitingParser.of(tokenText, tokenDescription, tokens, visitor);
  }

  public NonTerminalVisitingParser nonTerminalParserOf(String tokenText) {
    return nonTerminalParserOf(tokenText, tokenText);
  }
}
