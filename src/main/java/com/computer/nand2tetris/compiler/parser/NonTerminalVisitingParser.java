package com.computer.nand2tetris.compiler.parser;

import com.computer.nand2tetris.compiler.JackElementVisitor;
import com.computer.nand2tetris.compiler.JackToken;
import com.computer.nand2tetris.compiler.LookAheadStream;
import com.google.common.base.Preconditions;

final class NonTerminalVisitingParser {

  private final String tokenText;
  private final JackElementVisitor visitor;

  private NonTerminalVisitingParser(String tokenText, JackElementVisitor visitor) {
    this.tokenText = tokenText;
    this.visitor = visitor;
  }

  public static NonTerminalVisitingParser of(
      String tokenText,
      String tokenDescription,
      LookAheadStream<JackToken> tokens,
      JackElementVisitor visitor) {
    tokens.expect(tokenDescription);
    visitor.beginNonTerminalVisit(tokenText);
    return new NonTerminalVisitingParser(tokenText, visitor);
  }

  private static void expect(String tokenDescription, LookAheadStream<JackToken> tokens) {
    Preconditions
        .checkArgument(!tokens.isEmpty(), "No further tokens. Expected %s", tokenDescription);
  }

  public void parse(Runnable parser) {
    parser.run();
    visitor.endNonTerminalVisit();
  }
}
