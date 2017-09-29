package com.computer.nand2tetris.compiler;

import com.google.common.collect.ImmutableSet;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Stack;

public class CompiledXmlWriter extends StackBasedJackElementVisitor {

  private static final String INDENTATION_UNIT = "  ";  // 2 spaces

  private final BufferedWriter writer;
  private StringBuilder indentation = new StringBuilder();

  ImmutableSet<String> NON_TERMINALS_TO_WRITE =
      ImmutableSet.of(
          // program structure
          "class",
          "classVarDec",
          "subroutineDec",
          "parameterList",
          "subroutineBody",
          "varDec",
          // statements
          "statements",
          "whileSatement",
          "ifStatement",
          "returnStatement",
          "letStatement",
          "doStatement",
          // expressions
          "expression",
          "term",
          "expressionList");

  CompiledXmlWriter(BufferedWriter writer) {
    this.writer = writer;
  }

  @Override
  protected void beginVisitForNonTerminal(String nonTerminalText) {
    if (NON_TERMINALS_TO_WRITE.contains(nonTerminalText)) {
      indentAndWrite(createTag(nonTerminalText));
      increaseIndentation();
      writeNewline();
    }
  }

  @Override
  protected void endVisitForNonTerminal(String nonTerminalText) {
    if (NON_TERMINALS_TO_WRITE.contains(nonTerminalText)) {
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
