package com.computer.nand2tetris.compiler;

import com.google.common.collect.Streams;
import java.util.stream.Collectors;

public final class ErrorMessageGenerator {
  private ErrorMessageGenerator() {}

  public static <T> String generateClassNamesCsv(Iterable<T> instances) {
    return Streams.stream(instances).map(x -> x.getClass().getSimpleName())
        .collect(Collectors.joining(", "));
  }
}
