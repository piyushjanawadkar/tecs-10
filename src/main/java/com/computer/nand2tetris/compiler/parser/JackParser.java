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
          "boolean"
      );

  private static final ImmutableSet<String> CLASS_VAR_DEC_LOOKAHEAD_TOKENS =
      ImmutableSet.of(
          "static",
          "field"
      );
  private static final ImmutableSet<String> SUBROUTINE_DEC_LOOK_AHEAD_TOKENS =
      ImmutableSet.of(
          "constructor",
          "function",
          "method"
      );

  private static void parseClass(LookAheadStream<JackToken> tokens, CompiledCodeWriter writer) {
    writer.writeOpeningNonTerminalTag("class");
    match("class", tokens, writer);
    parseClassName(tokens, writer);
    match("{", tokens, writer);
    parseClassVarDecs(tokens, writer);
    parseSubroutineDecs(tokens, writer);
    match("}", tokens, writer);
    writer.writeClosingNonTerminalTag("class");
  }

  private static void parseClassVarDecs(LookAheadStream<JackToken> tokens,
      CompiledCodeWriter writer) {
    while (tokens.peek().isPresent()
        && CLASS_VAR_DEC_LOOKAHEAD_TOKENS.contains(tokens.peek().get().tokenText())) {
      parseClassVarDec(tokens, writer);
    }
  }

  private static void parseClassVarDec(LookAheadStream<JackToken> tokens,
      CompiledCodeWriter writer) {
    writer.writeOpeningNonTerminalTag("classVarDec");
    match(CLASS_VAR_DEC_LOOKAHEAD_TOKENS, tokens, writer);
    parseType(tokens, writer);
    parseVarName(tokens, writer);
    while (tokens.peek().get().tokenText().equals(",")) {
      match(",", tokens, writer);
      parseVarName(tokens, writer);
    }
    match(";", tokens, writer);
    writer.writeClosingNonTerminalTag("classVarDec");
  }

  private static void parseVarName(LookAheadStream<JackToken> tokens, CompiledCodeWriter writer) {
    parseIdentifier(tokens, writer);
  }

  private static void parseType(LookAheadStream<JackToken> tokens, CompiledCodeWriter writer) {
    if (tokens.peek().get().tokenType().equals(TokenType.IDENTIFIER)) {
      parseIdentifier(tokens, writer);
    } else {
      match(PRIMITIVE_TYPE_TOKENS, tokens, writer);
    }
  }

  private static void parseSubroutineDecs(LookAheadStream<JackToken> tokens,
      CompiledCodeWriter writer) {
    while (SUBROUTINE_DEC_LOOK_AHEAD_TOKENS.contains(tokens.peek().get().tokenText())) {
      parseSubroutineDec(tokens, writer);
    }
  }

  private static void parseSubroutineDec(
      LookAheadStream<JackToken> tokens,
      CompiledCodeWriter writer) {
    writer.writeOpeningNonTerminalTag("subroutineDec");
    match(SUBROUTINE_DEC_LOOK_AHEAD_TOKENS, tokens, writer);
    parseSubroutineReturnType(tokens, writer);
    parseSubroutineName(tokens, writer);
    match("(", tokens, writer);
    parseSubroutineParameterList(tokens, writer);
    match(")", tokens, writer);
    parseSubroutineBody(tokens, writer);
    writer.writeClosingNonTerminalTag("subroutineDec");
  }

  private static void parseSubroutineBody(
      LookAheadStream<JackToken> tokens,
      CompiledCodeWriter writer) {
    match("{", tokens, writer);
    parseVarDecs(tokens, writer);
    parseStatements();
    match("}", tokens, writer);
  }

  private static void parseStatements() {
  }

  private static void parseVarDecs(LookAheadStream<JackToken> tokens, CompiledCodeWriter writer) {

  }

  private static void parseSubroutineParameterList(
      LookAheadStream<JackToken> tokens,
      CompiledCodeWriter writer) {
    if (!hasTypeLookaheadToken(tokens)) {
      return;
    }
    parseTypedVarName(tokens, writer);
    while (hasLookaheadText(",", tokens)) {
      match(",", tokens, writer);
      parseTypedVarName(tokens, writer);
    }
  }

  private static void parseTypedVarName(
      LookAheadStream<JackToken> tokens,
      CompiledCodeWriter writer) {
    parseType(tokens, writer);
    parseVarName(tokens, writer);
  }

  private static boolean hasLookaheadText(String expectedText, LookAheadStream<JackToken> tokens) {
    return hasLookaheadText(ImmutableSet.of(expectedText), tokens);
  }

  private static boolean hasTypeLookaheadToken(LookAheadStream<JackToken> tokens) {
    return hasLookaheadText(PRIMITIVE_TYPE_TOKENS, tokens)
        || hasLookaheadType(TokenType.IDENTIFIER, tokens);
  }

  private static boolean hasLookaheadType(TokenType tokenType, LookAheadStream<JackToken> tokens) {
    return getPeekedToken(tokens).tokenType().equals(tokenType);
  }

  private static boolean hasLookaheadText(
      ImmutableSet<String> expectedTokenTexts,
      LookAheadStream<JackToken> tokens) {
    return expectedTokenTexts.contains(getPeekedToken(tokens).tokenText());
  }

  private static JackToken getPeekedToken(LookAheadStream<JackToken> tokens) {
    return tokens.peek().get();
  }

  private static void parseSubroutineName(
      LookAheadStream<JackToken> tokens,
      CompiledCodeWriter writer) {
    parseIdentifier(tokens, writer);
  }

  private static void parseSubroutineReturnType(
      LookAheadStream<JackToken> tokens,
      CompiledCodeWriter writer) {
    if (hasTypeLookaheadToken(tokens)) {
      parseType(tokens, writer);
    } else {
      match("void", tokens, writer);
    }
  }

  private static void parseClassName(LookAheadStream<JackToken> tokens, CompiledCodeWriter writer) {
    parseIdentifier(tokens, writer);
  }

  private static void parseIdentifier(LookAheadStream<JackToken> tokens,
      CompiledCodeWriter writer) {
    Preconditions
        .checkArgument(tokens.peek().isPresent(), "No further tokens. Expected identifier.");
    JackToken token = tokens.extract().get();
    Preconditions.checkArgument(
        token.tokenType().equals(TokenType.IDENTIFIER),
        "Expected %s but found %s.", TokenType.IDENTIFIER, token.toString());
    writer.writeTerminal(token);
  }

  private static void match(
      String tokenText,
      LookAheadStream<JackToken> tokens,
      CompiledCodeWriter writer) {
    match(ImmutableSet.of(tokenText), tokens, writer);
  }

  private static void match(
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

  public void parse(ImmutableList<JackToken> tokens, BufferedWriter bufferedWriter) {
    LookAheadStream<JackToken> tokenStream = new LookAheadStream<>(tokens);
    CompiledCodeWriter writer = new CompiledXmlWriter(bufferedWriter);
    parseClass(tokenStream, writer);
  }
}
