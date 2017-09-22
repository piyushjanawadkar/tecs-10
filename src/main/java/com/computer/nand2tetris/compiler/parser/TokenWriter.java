package com.computer.nand2tetris.compiler.parser;

import com.computer.nand2tetris.compiler.JackToken;
import com.computer.nand2tetris.compiler.LookAheadStream;
import java.io.BufferedWriter;
import java.io.IOException;

public class TokenWriter implements TokenSequenceParser {

  @Override
  public void parse(LookAheadStream<JackToken> tokenStream, BufferedWriter writer) {
    try {
      writer.write(tokenStream.extract().get().toString());
      writer.newLine();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean matches(JackToken token) {
    return true;
  }
}
