package com.computer.nand2tetris.compiler;

import java.io.BufferedWriter;
import java.io.IOException;

public class CompiledXmlWriter implements JackElementVisitor {

  private static final String INDENTATION_UNIT = "  ";  // 2 spaces

  private final BufferedWriter writer;
  private StringBuilder indentation = new StringBuilder();

  CompiledXmlWriter(BufferedWriter writer) {
    this.writer = writer;
  }

  @Override
  public void visitNonTerminalBeginElement(String nonTerminalText) {
    indentAndWrite(createTag(nonTerminalText));
    increaseIndentation();
    writeNewline();
  }

  @Override
  public void visitNonTerminalEndElement(String nonTerminalText) {
    decreaseIndentation();
    indentAndWrite(createClosingTag(nonTerminalText));
    writeNewline();
  }

  @Override
  public void visitTerminal(JackToken token) {
    indentAndWrite(createTag(token.tokenType().toString()));
    write(token.tokenText());
    write(createClosingTag(token.tokenType().toString()));
    writeNewline();
  }

  private void increaseIndentation() {
    indentation.append(INDENTATION_UNIT);
  }

  private void decreaseIndentation() {
    indentation.delete(indentation.length() - INDENTATION_UNIT.length(), indentation.length());
  }

  private void indent() {
    write(indentation.toString());
  }

  private void indentAndWrite(String text) {
    indent();
    write(text);
  }

  private static String createTag(String tagText) {
    return String.format("<%s>", tagText);
  }

  private String createClosingTag(String tagText) {
    return createTag(String.format("/%s", tagText));
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
}
