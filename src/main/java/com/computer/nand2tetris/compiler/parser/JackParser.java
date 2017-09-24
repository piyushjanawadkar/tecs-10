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

  private LookAheadStream<JackToken> tokens;
  private CompiledXmlWriter writer;

  public void parse(ImmutableList<JackToken> tokenList, BufferedWriter bufferedWriter) {
    tokens = new LookAheadStream<>(tokenList);
    writer = new CompiledXmlWriter(bufferedWriter);
    parseClass();
    Preconditions.checkArgument(tokens.isEmpty(), "Unexpected trailing tokens: %s", tokens);
  }

  private void parseClass() {
    writer.writeOpeningNonTerminalTag("class");
    match("class");
    parseClassName();
    match("{");
    parseClassVarDecs();
    parseSubroutineDecs();
    match("}");
    writer.writeClosingNonTerminalTag("class");
  }

  private void match(String tokenText) {
    match(ImmutableSet.of(tokenText));
  }

  private void match(
      ImmutableSet<String> tokenTexts) {
    Preconditions.checkArgument(
        !tokens.isEmpty(),
        "No further tokens. Expected %s.", tokenTexts);
    JackToken token = tokens.extract().get();
    Preconditions.checkArgument(
        tokenTexts.contains(token.tokenText()),
        "Expected %s but found %s.", tokenTexts, token.toString());
    writer.writeTerminal(token);
  }

  private void parseClassName() {
    parseIdentifier();
  }

  private void parseClassVarDecs() {
    while (!tokens.isEmpty()
        && CLASS_VAR_DEC_LOOKAHEAD_TOKENS.contains(getPeekedToken(tokens).tokenText())) {
      parseClassVarDec();
    }
  }

  private void parseClassVarDec() {
    writer.writeOpeningNonTerminalTag("classVarDec");
    match(CLASS_VAR_DEC_LOOKAHEAD_TOKENS);
    parseType();
    parseVarName();
    while (hasLookaheadText(",")) {
      match(",");
      parseVarName();
    }
    match(";");
    writer.writeClosingNonTerminalTag("classVarDec");
  }


  private void parseSubroutineDecs() {
    while (SUBROUTINE_DEC_LOOK_AHEAD_TOKENS.contains(getPeekedToken(tokens).tokenText())) {
      parseSubroutineDec();
    }
  }

  private void parseSubroutineDec() {
    writer.writeOpeningNonTerminalTag("subroutineDec");
    match(SUBROUTINE_DEC_LOOK_AHEAD_TOKENS);
    parseSubroutineReturnType();
    parseSubroutineName();
    match("(");
    parseSubroutineParameterList();
    match(")");
    parseSubroutineBody();
    writer.writeClosingNonTerminalTag("subroutineDec");
  }

  private void parseSubroutineReturnType() {
    if (hasTypeLookaheadToken()) {
      parseType();
    } else {
      match("void");
    }
  }

  private void parseSubroutineName() {
    parseIdentifier();
  }

  private void parseSubroutineParameterList() {
    if (!hasTypeLookaheadToken()) {
      return;
    }
    parseTypedVarName();
    while (hasLookaheadText(",")) {
      match(",");
      parseTypedVarName();
    }
  }

  private void parseSubroutineBody() {
    match("{");
    parseVarDecs();
    parseStatements();
    match("}");
  }

  private void parseVarDecs() {
  }

  private void parseStatements() {
  }

  private void parseIdentifier() {
    Preconditions
        .checkArgument(tokens.peek().isPresent(), "No further tokens. Expected identifier.");
    JackToken token = tokens.extract().get();
    Preconditions.checkArgument(
        token.tokenType().equals(TokenType.IDENTIFIER),
        "Expected %s but found %s.", TokenType.IDENTIFIER, token.toString());
    writer.writeTerminal(token);
  }

  private void parseType() {
    if (getPeekedToken(tokens).tokenType().equals(TokenType.IDENTIFIER)) {
      parseIdentifier();
    } else {
      match(PRIMITIVE_TYPE_TOKENS);
    }
  }

  private boolean hasTypeLookaheadToken() {
    return hasLookaheadText(PRIMITIVE_TYPE_TOKENS) || hasLookaheadType(TokenType.IDENTIFIER);
  }

  private void parseTypedVarName() {
    parseType();
    parseVarName();
  }

  private void parseVarName() {
    parseIdentifier();
  }

  private boolean hasLookaheadText(ImmutableSet<String> expectedTokenTexts) {
    return expectedTokenTexts.contains(getPeekedToken(tokens).tokenText());
  }

  private boolean hasLookaheadText(String expectedText) {
    return hasLookaheadText(ImmutableSet.of(expectedText));
  }

  private boolean hasLookaheadType(TokenType tokenType) {
    return getPeekedToken(tokens).tokenType().equals(tokenType);
  }

  private static JackToken getPeekedToken(LookAheadStream<JackToken> tokens) {
    return tokens.peek().get();
  }
}
