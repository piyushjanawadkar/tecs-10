package com.computer.nand2tetris.compiler.io;

import com.computer.nand2tetris.compiler.JackToken;
import com.computer.nand2tetris.compiler.StackBasedJackElementVisitor;
import com.google.common.collect.ImmutableSet;
import java.io.BufferedWriter;
import java.io.IOException;

public class ParsedXmlWriter extends StackBasedJackElementVisitor {

  private static final String INDENTATION_UNIT = "  ";  // 2 spaces

  private final BufferedWriter writer;
  private StringBuilder indentation = new StringBuilder();
  private final ImmutableSet<String> nonTerminalsToParse;

  public ParsedXmlWriter(BufferedWriter writer, ImmutableSet<String> nonTerminalsToParse) {
    this.writer = writer;
    this.nonTerminalsToParse = nonTerminalsToParse;
  }

  @Override
  protected void beginVisitForNonTerminal(String nonTerminalText) {
    if (nonTerminalsToParse.contains(nonTerminalText)) {
      indentAndWrite(createTag(nonTerminalText));
      increaseIndentation();
      writeNewline();
    }
  }

  @Override
  protected void endVisitForNonTerminal(String nonTerminalText) {
    if (nonTerminalsToParse.contains(nonTerminalText)) {
      decreaseIndentation();
      indentAndWrite(createClosingTag(nonTerminalText));
      writeNewline();
    }
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
      writer.flush();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
