package com.computer.nand2tetris.compiler.parser;

import com.computer.nand2tetris.compiler.JackToken;
import java.io.BufferedWriter;
import java.io.IOException;

public class CompiledXmlWriter implements CompiledCodeWriter {

  private static final String INDENTATION_UNIT = "  ";  // 2 spaces

  private final BufferedWriter writer;
  StringBuilder indentation = new StringBuilder();

  public CompiledXmlWriter(BufferedWriter writer) {
    this.writer = writer;
  }

  @Override
  public void writeOpenNonTerminalTag(String terminalText) {
    indentAndWrite(createTag(terminalText));
    increaseIndentation();
    writeNewline();
  }

  private void increaseIndentation() {
    indentation.append(INDENTATION_UNIT);
  }

  private void indent() {
    write(indentation.toString());
  }

  private static String createTag(String tagText) {
    return "<" + tagText + ">";
  }

  @Override
  public void writeClosingNonTerminalTag(String terminalText) {
    decreaseIndentation();
    indentAndWrite(createClosingTag(terminalText));
    writeNewline();
  }

  @Override
  public void writeTerminal(JackToken token) {
    indentAndWrite(createTag(token.tokenType().toString()));
    write(token.tokenText());
    write(createClosingTag(token.tokenType().toString()));
    writeNewline();
  }

  private void decreaseIndentation() {
    indentation.delete(indentation.length() - INDENTATION_UNIT.length(), indentation.length());
  }

  private String createClosingTag(String tagText) {
    return createTag("/" + tagText);
  }

  private void write(String text) {
    try {
      writer.write(text);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void writeNewline() {
    try {
      writer.newLine();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void indentAndWrite(String text) {
    indent();
    write(text);
  }
}
