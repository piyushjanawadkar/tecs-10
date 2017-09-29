package com.computer.nand2tetris.compiler;

import com.computer.nand2tetris.compiler.JackToken.TokenType;
import com.google.common.base.Optional;
import java.util.Set;
import java.util.TreeSet;

public class Context extends StackBasedJackElementVisitor {

  private Set<String> classNames = new TreeSet<>();

  boolean inClassNonTerminal = false;
  Optional<String> currentClassName = Optional.absent();

  @Override
  protected void beginVisitForNonTerminal(String nonTerminalText) {
    inClassNonTerminal = nonTerminalText.equals("class");
  }

  @Override
  protected void endVisitForNonTerminal(String nonTerminalText) {
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
