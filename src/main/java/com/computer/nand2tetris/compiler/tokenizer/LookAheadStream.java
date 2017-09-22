package com.computer.nand2tetris.compiler.tokenizer;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import java.util.List;

final class LookAheadStream<T> {

  Optional<T> lookAhead = Optional.absent();
  List<T> restItems = ImmutableList.of();

  LookAheadStream(ImmutableList<T> items) {
    resetStateFromList(items);
  }

  private void resetStateFromList(List<T> items) {
    lookAhead = Optional.absent();
    if (!items.isEmpty()) {
      lookAhead = Optional.of(items.get(0));
      restItems = items.subList(1, items.size());
    }
  }

  Optional<T> peek() {
    return lookAhead;
  }

  Optional<T> extract() {
    Optional<T> extractedLookAhead = peek();
    resetStateFromList(restItems);
    return extractedLookAhead;
  }

  @Override
  public String toString() {
    return "lookAhead: " + peek() + ", stream: " + restItems.toString();
  }
}
