package com.computer.nand2tetris.compiler;

import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import java.util.List;
import java.util.stream.Stream;

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
    return "lookAhead: " + (peek().isPresent() ? peek().get() : "nil")
        + ", stream: " + restItems.toString();
  }

  public boolean isEmpty() {
    return !peek().isPresent();
  }

  public void putBack(T token) {
    if (!isEmpty()) {
      restItems = concat(lookAhead.get(), restItems);
    }
    lookAhead = Optional.of(token);
  }

  private List<T> concat(T lookAhead, List<T> restItems) {
    return Streams.concat(Stream.of(lookAhead), restItems.stream()).collect(toImmutableList());
  }
}
