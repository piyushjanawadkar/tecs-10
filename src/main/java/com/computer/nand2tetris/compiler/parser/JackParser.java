package com.computer.nand2tetris.compiler.parser;

import com.computer.nand2tetris.compiler.JackToken;
import com.computer.nand2tetris.compiler.JackToken.TokenType;
import com.computer.nand2tetris.compiler.LookAheadStream;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
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
  private ImmutableMap<String, Runnable> statementParserByLookahead =
      ImmutableMap.of(
          "let", this::parseLetStatement,
          "if", this::parseIfStatement,
          "while", this::parseWhileStatement,
          "do", this::parseDoStatement,
          "return", this::parseReturnStatement);

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

  private JackToken extractClassName() {
    return extractIdentifier();
  }

  private void parseClassVarDecs() {
    while (!tokens.isEmpty()
        && CLASS_VAR_DEC_LOOKAHEAD_TOKENS.contains(getPeekedToken().tokenText())) {
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
    while (SUBROUTINE_DEC_LOOK_AHEAD_TOKENS.contains(getPeekedToken().tokenText())) {
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
    while (hasLookaheadText("var")) {
      parseVarDec();
    }
  }

  private void parseVarDec() {
    match("var");
    parseType();
    parseVarName();
    while (hasLookaheadText(",")) {
      match(",");
      parseVarName();
    }
    match(";");
  }

  private void parseStatements() {
    boolean shouldWriteNonTerminalTag = false;
    while (hasStatementLookaheadToken()) {
      if (!shouldWriteNonTerminalTag) {
        shouldWriteNonTerminalTag = true;
        writer.writeOpeningNonTerminalTag("statements");
      }
      parseStatement();
    }
    if (shouldWriteNonTerminalTag) {
      writer.writeClosingNonTerminalTag("statements");
    }
  }

  private void parseStatement() {
    Optional<Runnable> statementParser = getStatementParser();
    Preconditions.checkArgument(
        statementParser.isPresent(),
        "No parser found for statement beginning at %s", tokens);
    statementParser.get().run();
  }

  private Optional<Runnable> getStatementParser() {
    Preconditions.checkArgument(!tokens.isEmpty(), "No more tokens. Statement expected.");
    Runnable parser = statementParserByLookahead.get(getPeekedToken().tokenText());
    return parser != null ? Optional.of(parser) : Optional.absent();
  }

  private boolean hasStatementLookaheadToken() {
    return !tokens.isEmpty()
        && statementParserByLookahead.keySet().contains(getPeekedToken().tokenText());
  }

  private void parseIdentifier() {
    writer.writeTerminal(extractIdentifier());
  }

  private JackToken extractIdentifier() {
    Preconditions
        .checkArgument(!tokens.isEmpty(), "No further tokens. Expected identifier.");
    JackToken token = tokens.extract().get();
    Preconditions.checkArgument(
        token.tokenType().equals(TokenType.IDENTIFIER),
        "Expected %s but found %s.", TokenType.IDENTIFIER, token.toString());
    return token;
  }

  private void parseType() {
    if (getPeekedToken().tokenType().equals(TokenType.IDENTIFIER)) {
      parseIdentifier();
    } else {
      match(PRIMITIVE_TYPE_TOKENS);
    }
  }

  private void parseLetStatement() {
    writer.writeOpeningNonTerminalTag("letStatement");
    match("let");
    parseVarName();
    if (getPeekedToken().tokenText().equals("[")) {
      match("[");
      parseExpression();
      match("]");
    }
    match("=");
    parseExpression();
    match(";");
    writer.writeClosingNonTerminalTag("letStatement");
  }

  private void parseIfStatement() {
    writer.writeOpeningNonTerminalTag("ifStatement");

    match("if");
    match("(");
    parseExpression();
    match(")");

    match("{");
    parseStatements();
    match("}");

    if (getPeekedToken().tokenText().equals("else")) {
      match("else");
      match("{");
      parseStatements();
      match("}");
    }

    writer.writeClosingNonTerminalTag("ifStatement");
  }

  private void parseWhileStatement() {
    writer.writeOpeningNonTerminalTag("whileStatement");
    match("while");
    match("(");
    parseExpression();
    match(")");
    match("{");
    parseStatements();
    match("}");
    writer.writeClosingNonTerminalTag("whileStatement");
  }

  private void parseDoStatement() {
    writer.writeOpeningNonTerminalTag("doStatement");
    match("do");
    parseSubroutineCall();
    match(";");
    writer.writeClosingNonTerminalTag("doStatement");
  }

  private void parseSubroutineCall() {
    Preconditions.checkArgument(!tokens.isEmpty(), "No more tokens. Expected subroutine call.");
    JackToken token = tokens.extract().get();
    if (getPeekedToken().tokenText().equals(".")) {
      tokens.putBack(token);
      if (isClassNameToken(token)) {
        parseClassName();
      } else {
        parseIdentifier();
      }
      match(".");
    } else {
      tokens.putBack(token);
    }
    parseSubroutineName();
    match("(");
    parseExpressionList();
    match(")");
  }

  private boolean isClassNameToken(JackToken token) {
    // TODO: consult symbol table during code generation.
    return false;
  }

  private void parseReturnStatement() {
    match("return");
    // parseExpression();
    match(";");
  }

  private void parseExpressionList() {
  }

  private void parseExpression() {
    // TODO
    match("0");
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
    return expectedTokenTexts.contains(getPeekedToken().tokenText());
  }

  private boolean hasLookaheadText(String expectedText) {
    return hasLookaheadText(ImmutableSet.of(expectedText));
  }

  private boolean hasLookaheadType(TokenType tokenType) {
    return getPeekedToken().tokenType().equals(tokenType);
  }

  private JackToken getPeekedToken() {
    return tokens.peek().get();
  }
}
