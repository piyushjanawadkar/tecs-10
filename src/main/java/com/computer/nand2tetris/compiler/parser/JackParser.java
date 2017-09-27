package com.computer.nand2tetris.compiler.parser;

import com.computer.nand2tetris.compiler.Context;
import com.computer.nand2tetris.compiler.JackElementVisitor;
import com.computer.nand2tetris.compiler.JackToken;
import com.computer.nand2tetris.compiler.JackToken.TokenType;
import com.computer.nand2tetris.compiler.LookAheadStream;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

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

  private JackElementVisitor visitor;
  private Optional<Context> context;
  private ImmutableMap<String, Runnable> statementParserByLookahead =
      ImmutableMap.of(
          "let", this::parseLetStatement,
          "if", this::parseIfStatement,
          "while", this::parseWhileStatement,
          "do", this::parseDoStatement,
          "return", this::parseReturnStatement);

  public void parse(
      ImmutableList<JackToken> tokenList,
      Optional<Context> context,
      JackElementVisitor visitor) {
    tokens = new LookAheadStream<>(tokenList);
    this.context = context;
    this.visitor = visitor;
    parseClass();
    Preconditions.checkArgument(tokens.isEmpty(), "Unexpected trailing tokens: %s", tokens);
  }

  private void parseClass() {
    visitor.visitNonTerminalBeginElement("class");
    match("class");
    parseClassName();
    match("{");
    parseClassVarDecs();
    parseSubroutineDecs();
    match("}");
    visitor.visitNonTerminalEndElement("class");
  }

  private void match(String tokenText) {
    match(ImmutableSet.of(tokenText));
  }

  private void match(
      ImmutableSet<String> tokenTexts) {
    expect(tokenTexts.toString());
    JackToken token = tokens.extract().get();
    Preconditions.checkArgument(
        tokenTexts.contains(token.tokenText()),
        "Expected %s but found %s.", tokenTexts, token.toString());
    visitor.visitTerminal(token);
  }

  private void parseClassName() {
    expect("class name");
    parseIdentifier();
  }

  private void parseClassVarDecs() {
    while(hasLookaheadTextIn(CLASS_VAR_DEC_LOOKAHEAD_TOKENS)) {
      parseClassVarDec();
    }
  }

  private void parseClassVarDec() {
    expect("class variable declarations");
    visitor.visitNonTerminalBeginElement("classVarDec");
    match(CLASS_VAR_DEC_LOOKAHEAD_TOKENS);
    parseType();
    parseVarName();
    while (hasLookaheadText(",")) {
      match(",");
      parseVarName();
    }
    match(";");
    visitor.visitNonTerminalEndElement("classVarDec");
  }

  private void parseSubroutineDecs() {
    while (hasLookaheadTextIn(SUBROUTINE_DEC_LOOK_AHEAD_TOKENS)) {
      parseSubroutineDec();
    }
  }

  private void parseSubroutineDec() {
    visitor.visitNonTerminalBeginElement("subroutineDec");
    match(SUBROUTINE_DEC_LOOK_AHEAD_TOKENS);
    parseSubroutineReturnType();
    parseSubroutineName();
    match("(");
    parseSubroutineParameterList();
    match(")");
    parseSubroutineBody();
    visitor.visitNonTerminalEndElement("subroutineDec");
  }

  private void parseSubroutineReturnType() {
    expect("subroutine return type");
    if (hasTypeLookaheadToken()) {
      parseType();
    } else {
      match("void");
    }
  }

  private void parseSubroutineName() {
    expect("subroutine name");
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
    expect("subroutine body");
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
    expect("variable declaration");
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
        visitor.visitNonTerminalBeginElement("statements");
      }
      parseStatement();
    }
    if (shouldWriteNonTerminalTag) {
      visitor.visitNonTerminalEndElement("statements");
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
    Runnable parser = statementParserByLookahead.get(getPeekedToken("statement").tokenText());
    return parser != null ? Optional.of(parser) : Optional.absent();
  }

  private boolean hasStatementLookaheadToken() {
    return !tokens.isEmpty()
        && statementParserByLookahead.keySet().contains(getPeekedToken("statement").tokenText());
  }

  private void parseIdentifier() {
    expect("identifier");
    visitor.visitTerminal(extractIdentifier());
  }

  private JackToken extractIdentifier() {
    JackToken token = tokens.extract().get();
    Preconditions.checkArgument(
        token.tokenType().equals(TokenType.IDENTIFIER),
        "Expected %s but found %s.", TokenType.IDENTIFIER, token.toString());
    return token;
  }

  private void parseType() {
    expect("type");
    if (hasLookaheadType(TokenType.IDENTIFIER)) {
      parseIdentifier();
    } else {
      match(PRIMITIVE_TYPE_TOKENS);
    }
  }

  private void parseLetStatement() {
    expect("let statement");
    visitor.visitNonTerminalBeginElement("letStatement");
    match("let");
    parseVarName();
    if (getPeekedToken("array open").tokenText().equals("[")) {
      match("[");
      parseExpression();
      match("]");
    }
    match("=");
    parseExpression();
    match(";");
    visitor.visitNonTerminalEndElement("letStatement");
  }

  private void parseIfStatement() {
    expect("if statement");
    visitor.visitNonTerminalBeginElement("ifStatement");

    match("if");
    match("(");
    parseExpression();
    match(")");

    match("{");
    parseStatements();
    match("}");

    if (getPeekedToken("else").tokenText().equals("else")) {
      match("else");
      match("{");
      parseStatements();
      match("}");
    }

    visitor.visitNonTerminalEndElement("ifStatement");
  }

  private void parseWhileStatement() {
    expect("while statement");
    visitor.visitNonTerminalBeginElement("whileStatement");
    match("while");
    match("(");
    parseExpression();
    match(")");
    match("{");
    parseStatements();
    match("}");
    visitor.visitNonTerminalEndElement("whileStatement");
  }

  private void parseDoStatement() {
    expect("do statement");
    visitor.visitNonTerminalBeginElement("doStatement");
    match("do");
    parseSubroutineCall();
    match(";");
    visitor.visitNonTerminalEndElement("doStatement");
  }

  private void parseSubroutineCall() {
    expect("subroutine call");
    JackToken token = tokens.extract().get();
    if (hasLookaheadText(".")) {
      tokens.putBack(token);
      if (hasClassNameLookahead()) {
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

  private void parseReturnStatement() {
    expect("return statement");
    visitor.visitNonTerminalBeginElement("returnStatement");
    match("return");
    parseExpression();
    match(";");
    visitor.visitNonTerminalEndElement("returnStatement");
  }

  private void parseExpressionList() {
  }

  private void parseExpression() {
    // TODO
    match("0");
  }

  private void parseTypedVarName() {
    expect("typed followed by variable name");
    parseType();
    parseVarName();
  }

  private void parseVarName() {
    expect("variable name");
    parseIdentifier();
  }

  private boolean hasTypeLookaheadToken() {
    return hasLookaheadTextIn(PRIMITIVE_TYPE_TOKENS) || hasLookaheadType(TokenType.IDENTIFIER);
  }

  private boolean hasLookaheadTextIn(ImmutableSet<String> expectedTokenTexts) {
    return !tokens.isEmpty() && expectedTokenTexts
        .contains(getPeekedToken(expectedTokenTexts.toString()).tokenText());
  }

  private boolean hasLookaheadText(String expectedText) {
    return hasLookaheadTextIn(ImmutableSet.of(expectedText));
  }

  private boolean hasLookaheadType(TokenType tokenType) {
    return !tokens.isEmpty() && getPeekedToken(tokenType.toString()).tokenType().equals(tokenType);
  }

  private boolean hasClassNameLookahead() {
    return !tokens.isEmpty() && context.isPresent() && context.get()
        .isClassNameToken(getPeekedToken("class name"));
  }

  private JackToken getPeekedToken(String expectedTokenDescription) {
    expect(expectedTokenDescription);
    return tokens.peek().get();
  }

  private void expect(String tokenDescription) {
    Preconditions
        .checkArgument(!tokens.isEmpty(), "No further tokens. Expected %s", tokenDescription);
  }
}
