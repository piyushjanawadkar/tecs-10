package com.computer.nand2tetris.compiler;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import java.util.List;

public final class LookAheadStream<T> {

  Optional<T> lookAhead = Optional.absent();
  List<T> restItems = ImmutableList.of();

  public LookAheadStream(ImmutableList<T> items) {
    resetStateFromList(items);
  }

  public Optional<T> peek() {
    return lookAhead;
  }

  public Optional<T> extract() {
    Optional<T> extractedLookAhead = peek();
    resetStateFromList(restItems);
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
    return "lookAhead: " + (peek().isPresent() ? peek().get() : "nil") + ", stream: " + restItems
        .toString();
  }
}
