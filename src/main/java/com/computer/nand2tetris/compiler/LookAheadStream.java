package com.computer.nand2tetris.compiler;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.LinkedList;
import java.util.List;

public final class LookAheadStream<T> {

  Optional<T> lookAhead = Optional.absent();
  List<T> restItems = new LinkedList<>();

  public LookAheadStream(ImmutableList<T> items) {
    resetStateFromList(new LinkedList<T>(items));
  }

  public Optional<T> peek() {
    return lookAhead;
  }

  public Optional<T> extract() {
    Optional<T> extractedLookAhead = peek();
    resetStateFromList(restItems);
    //System.err.println(restItems);
    return extractedLookAhead;
  }

  private void resetStateFromList(List<T> items) {
    lookAhead = Optional.absent();
    if (!items.isEmpty()) {
      lookAhead = Optional.of(items.get(0));
      restItems = items.subList(1, items.size());
    }
  }

  @Override
  public String toString() {
    return "lookAhead: " + (peek().isPresent() ? peek().get() : "nil")
        + ", stream: " + restItems.toString();
  }

  public boolean isEmpty() {
    return !peek().isPresent();
  }

  public void putBack(T token) {
    if (!isEmpty()) {
      concat(lookAhead.get(), restItems);
    }
    lookAhead = Optional.of(token);
  }

  private void concat(T lookAhead, List<T> restItems) {
    restItems.add(0, lookAhead);
  }

  public void expect(String tokenDescription) {
    Preconditions.checkArgument(
        !isEmpty(), "No further tokens. Expected %s", tokenDescription);
  }
}
