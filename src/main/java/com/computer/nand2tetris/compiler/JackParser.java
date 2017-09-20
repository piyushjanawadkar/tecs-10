package com.computer.nand2tetris.compiler;

import com.google.common.collect.ImmutableList;
import java.io.BufferedWriter;
import java.io.IOException;

public class JackParser {

  public void parse(ImmutableList<JackToken> tokens, BufferedWriter writer) {

    tokens.stream().forEachOrdered(t -> write(t.toString(), writer));
  }

  private void write(String tokenString, BufferedWriter writer) {
    try {
      writer.write(tokenString);
      writer.newLine();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
