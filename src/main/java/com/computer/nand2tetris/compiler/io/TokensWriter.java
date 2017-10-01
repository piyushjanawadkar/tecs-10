package com.computer.nand2tetris.compiler.io;

import com.computer.nand2tetris.compiler.JackToken;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.io.BufferedWriter;

public class TokensWriter {

  public void writeTokens(ImmutableList<JackToken> tokens, BufferedWriter writer) {
    ParsedXmlWriter xmlWriter = new ParsedXmlWriter(writer, ImmutableSet.of("tokens"));
    xmlWriter.beginNonTerminalVisit("tokens");
    tokens.stream().forEachOrdered(xmlWriter::visitTerminal);
    xmlWriter.endNonTerminalVisit();
  }
}
