package com.computer.nand2tetris.compiler.io;

import com.computer.nand2tetris.compiler.JackElementVisitor;
import com.computer.nand2tetris.compiler.JackToken;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.io.BufferedWriter;
import java.io.IOException;

public class ParsedXmlWriter implements JackElementVisitor {

  private static final String INDENTATION_UNIT = "  ";  // 2 spaces
  private static final ImmutableMap<String, String> BROWSABLE_STRINGS =
      ImmutableMap.of(
          "<", "&lt;",
          ">", "&gt;",
          "\"", "&quot;",
          "&", "&amp;");

  private final BufferedWriter writer;
  private final ImmutableSet<String> nonTerminalsToParse;
  private StringBuilder indentation = new StringBuilder();

  public ParsedXmlWriter(BufferedWriter writer, ImmutableSet<String> nonTerminalsToParse) {
    this.writer = writer;
    this.nonTerminalsToParse = nonTerminalsToParse;
  }

  private static String getTokenText(JackToken token) {
    String tokenText = token.tokenText();
    String browsableText = BROWSABLE_STRINGS.get(tokenText);
    return browsableText == null ? tokenText : browsableText;
  }

  private static String createTag(String tagText) {
    return String.format("<%s>", tagText);
  }

  private static String createClosingTag(String tagText) {
    return createTag(String.format("/%s", tagText));
  }

  @Override
  public void beginNonTerminalVisit(String nonTerminalText) {
    if (nonTerminalsToParse.contains(nonTerminalText)) {
      indentAndWrite(createTag(nonTerminalText));
      increaseIndentation();
      writeNewline();
    }
  }

  @Override
  public void endNonTerminalVisit(String nonTerminalText) {
    if (nonTerminalsToParse.contains(nonTerminalText)) {
      decreaseIndentation();
      indentAndWrite(createClosingTag(nonTerminalText));
      writeNewline();
    }
  }

  @Override
  public void visitTerminal(JackToken token) {
    String tokenTypeText = token.tokenType().toString();
    indentAndWrite(createTag(tokenTypeText));
    write(String.format(" %s ", getTokenText(token)));
    write(createClosingTag(tokenTypeText));
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
