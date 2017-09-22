package com.computer.nand2tetris.compiler.tokenizer;

import com.google.common.base.Optional;

final class LookAheadStream {

  private String line;
  private int lookAheadIndex;

  LookAheadStream(String line) {
    this.line = line;
    lookAheadIndex = 0;
  }

  Optional<Character> peek() {
    return hasLookAhead() ? Optional.of(getLookAhead()) : Optional.absent();
  }

  Optional<Character> extract() {
    Optional<Character> extractedLookAhead = peek();
    lookAheadIndex++;
    return extractedLookAhead;
  }

  private boolean hasLookAhead() {
    return lookAheadIndex < line.length();
  }

  private char getLookAhead() {
    return line.charAt(lookAheadIndex);
  }

  @Override
  public String toString() {
    return "lookAhead: " + peek() + ", stream: " + getRemainingStream();
  }

  private String getRemainingStream() {
    return line.substring(lookAheadIndex);
  }
}
