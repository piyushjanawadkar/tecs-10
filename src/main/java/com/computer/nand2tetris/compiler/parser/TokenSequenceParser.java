package com.computer.nand2tetris.compiler.parser;

import com.computer.nand2tetris.compiler.JackToken;
import com.computer.nand2tetris.compiler.LookAheadStream;
import java.io.BufferedWriter;

public interface TokenSequenceParser {
  void parse(LookAheadStream<JackToken> tokenStream, BufferedWriter writer);

  boolean matches(JackToken token);
}
