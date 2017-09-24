package com.computer.nand2tetris.compiler.parser;

import com.computer.nand2tetris.compiler.JackToken;
import com.computer.nand2tetris.compiler.JackToken.TokenType;
import com.computer.nand2tetris.compiler.LookAheadStream;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.io.BufferedWriter;

public class JackParser {

  private static final ImmutableSet<String> PRIMITIVE_TYPE_TOKENS =
      ImmutableSet.of(
          "int",
          "char",
          "boolean");

  private static final ImmutableSet<String> CLASS_VAR_LOOKAHEAD_TOKENS =
      ImmutableSet.of(
          "static",
          "field");

  public void parse(ImmutableList<JackToken> tokens, BufferedWriter bufferedWriter) {
    LookAheadStream<JackToken> tokenStream = new LookAheadStream<>(tokens);
    CompiledCodeWriter writer = new CompiledXmlWriter(bufferedWriter);
    parseClass(tokenStream, writer);
  }

  private void parseClass(LookAheadStream<JackToken> tokens, CompiledCodeWriter writer) {
    writer.writeOpeningNonTerminalTag("class");
    match("class", tokens, writer);
    parseClassName(tokens, writer);
    match("{", tokens, writer);
    parseClassVarDecs(tokens, writer);
    parseSubroutineDecs(tokens, writer);
    match("}", tokens, writer);
    writer.writeClosingNonTerminalTag("class");
  }

  private void parseClassVarDecs(LookAheadStream<JackToken> tokens, CompiledCodeWriter writer) {
    while (tokens.peek().isPresent()
        && CLASS_VAR_LOOKAHEAD_TOKENS.contains(tokens.peek().get().tokenText())) {
      parseClassVarDec(tokens, writer);
    }
  }

  private void parseClassVarDec(LookAheadStream<JackToken> tokens, CompiledCodeWriter writer) {
    writer.writeOpeningNonTerminalTag("classVarDec");
    match(CLASS_VAR_LOOKAHEAD_TOKENS, tokens, writer);
    parseType(tokens, writer);
    parseVarName(tokens, writer);
    while (tokens.peek().get().tokenText().equals(",")) {
      match(",", tokens, writer);
      parseVarName(tokens, writer);
    }
    match(";", tokens, writer);
    writer.writeClosingNonTerminalTag("classVarDec");
  }

  private void parseVarName(LookAheadStream<JackToken> tokens, CompiledCodeWriter writer) {
    parseIdentifier(tokens, writer);
  }

  private void parseType(LookAheadStream<JackToken> tokens, CompiledCodeWriter writer) {
    if (tokens.peek().get().tokenType().equals(TokenType.IDENTIFIER)) {
      parseIdentifier(tokens, writer);
    } else {
      match(PRIMITIVE_TYPE_TOKENS, tokens, writer);
    }
  }

  private void parseSubroutineDecs(LookAheadStream<JackToken> tokens, CompiledCodeWriter writer) {
    // TODO
  }


  private void parseClassName(LookAheadStream<JackToken> tokens, CompiledCodeWriter writer) {
    parseIdentifier(tokens, writer);
  }

  private void parseIdentifier(LookAheadStream<JackToken> tokens, CompiledCodeWriter writer) {
    Preconditions
        .checkArgument(tokens.peek().isPresent(), "No further tokens. Expected identifier.");
    JackToken token = tokens.extract().get();
    Preconditions.checkArgument(
        token.tokenType().equals(TokenType.IDENTIFIER),
        "Expected %s but found %s.", TokenType.IDENTIFIER, token.toString());
    writer.writeTerminal(token);
  }

  private void match(
      String tokenText,
      LookAheadStream<JackToken> tokens,
      CompiledCodeWriter writer) {
    match(ImmutableSet.of(tokenText), tokens, writer);
  }

  private void match(
      ImmutableSet<String> tokenTexts,
      LookAheadStream<JackToken> tokens,
      CompiledCodeWriter writer) {
    Preconditions.checkArgument(
        tokens.peek().isPresent(),
        "No further tokens. Expected %s.", tokenTexts);
    JackToken token = tokens.extract().get();
    Preconditions.checkArgument(
        tokenTexts.contains(token.tokenText()),
        "Expected %s but found %s.", tokenTexts, token.toString());
    writer.writeTerminal(token);
  }
}
