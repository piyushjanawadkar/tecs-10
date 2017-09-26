package com.computer.nand2tetris.compiler;

public interface JackElementVisitor {

  void visitNonTerminalBeginElement(String nonTerminalText);

  void visitNonTerminalEndElement(String nonTerminalText);

  void visitTerminal(JackToken token);
}
