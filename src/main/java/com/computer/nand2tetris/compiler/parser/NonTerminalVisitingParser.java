package com.computer.nand2tetris.compiler.parser;

import com.computer.nand2tetris.compiler.JackElementVisitor;
import com.computer.nand2tetris.compiler.JackToken;
import com.computer.nand2tetris.compiler.LookAheadStream;

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

  public void parse(Runnable parser) {
    parser.run();
    visitor.endNonTerminalVisit(tokenText);
  }
}
