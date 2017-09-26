package com.computer.nand2tetris.compiler;

import com.computer.nand2tetris.compiler.JackToken.TokenType;
import com.google.common.base.Optional;
import java.util.TreeSet;

public class Context implements JackElementVisitor {

  private TreeSet<String> classNames = new TreeSet<>();

  boolean inClassNonTerminal = false;
  Optional<String> currentClassName = Optional.absent();

  @Override
  public void visitNonTerminalBeginElement(String nonTerminalText) {
    inClassNonTerminal = nonTerminalText.equals("class");
  }

  @Override
  public void visitNonTerminalEndElement(String nonTerminalText) {
    if (inClassNonTerminal && nonTerminalText.equals("class")) {
      inClassNonTerminal = false;
    }
  }

  @Override
  public void visitTerminal(JackToken token) {
    if (token.tokenType().equals(TokenType.IDENTIFIER) && !currentClassName.isPresent()) {
      currentClassName = Optional.of(token.tokenText());
      classNames.add(currentClassName.get());
    }
  }

  public boolean isClassNameToken(JackToken token) {
    return classNames.contains(token.tokenText());
  }
}
