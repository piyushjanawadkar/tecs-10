package com.computer.nand2tetris.compiler;

import com.google.common.base.Preconditions;
import java.util.Stack;

public abstract class StackBasedJackElementVisitor implements JackElementVisitor {

  private Stack<String> nonTerminalsBeingVisited = new Stack<>();

  protected abstract void beginVisitForNonTerminal(String nonTerminalText);
  protected abstract void endVisitForNonTerminal(String nonTerminalText);

  @Override
  public final void beginNonTerminalVisit(String nonTerminalText) {
    nonTerminalsBeingVisited.push(nonTerminalText);
    System.err.println("pushed " + nonTerminalText + ", stack: " + nonTerminalsBeingVisited);
    beginVisitForNonTerminal(nonTerminalText);
  }

  @Override
  public final void endNonTerminalVisit() {
    Preconditions.checkArgument(!nonTerminalsBeingVisited.isEmpty(),
        "No non terminals being visited. Can not end visit.");
    System.err.println("popping " + nonTerminalsBeingVisited.peek() + ", stack: " + nonTerminalsBeingVisited);
    endVisitForNonTerminal(nonTerminalsBeingVisited.pop());
  }
}
