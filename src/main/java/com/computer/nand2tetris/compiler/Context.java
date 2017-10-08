package com.computer.nand2tetris.compiler;

import com.computer.nand2tetris.compiler.JackToken.TokenType;
import com.google.common.base.Optional;
import java.util.Set;
import java.util.TreeSet;

public class Context implements JackElementVisitor {

  private Set<String> classNames = new TreeSet<>();

  boolean inClassNonTerminal = false;
  Optional<String> currentClassName = Optional.absent();

  @Override
  public void beginNonTerminalVisit(String nonTerminalText) {
    if (nonTerminalText.equals("class")) {
      inClassNonTerminal = true;
    }
  }

  @Override
  public void endNonTerminalVisit(String nonTerminalText) {
    if (inClassNonTerminal && nonTerminalText.equals("class")) {
      inClassNonTerminal = false;
      currentClassName = Optional.absent();
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

  public String toString() {
    return String.format(
        "current class name: %s, all class names: %s",
        currentClassName,
        classNames);
  }
}
