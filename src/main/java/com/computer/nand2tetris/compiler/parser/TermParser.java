package com.computer.nand2tetris.compiler.parser;

import com.google.auto.value.AutoValue;
import java.util.concurrent.Callable;

@AutoValue
abstract class TermParser {

  static TermParser of(Callable<Boolean> matcher, Runnable parser) {
    return new AutoValue_TermParser(matcher, parser);
  }

  abstract Callable<Boolean> matcher();

  abstract Runnable parser();
}
