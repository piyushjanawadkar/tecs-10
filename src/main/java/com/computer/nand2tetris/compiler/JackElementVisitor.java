package com.computer.nand2tetris.compiler;

public interface JackElementVisitor {

  void beginNonTerminalVisit(String nonTerminalText);

  void endNonTerminalVisit();

  void visitTerminal(JackToken token);
}
