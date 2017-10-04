package com.computer.nand2tetris.compiler;

public interface JackElementVisitor {

  void beginNonTerminalVisit(String nonTerminalText);

  void endNonTerminalVisit(String nonTerminalText);

  void visitTerminal(JackToken token);
}
