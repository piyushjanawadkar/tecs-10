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
  private static final ImmutableSet<String> KEYWORD_CONSTANT_TOKENS =
      ImmutableSet.of(
          "true",
          "false",
          "null",
          "this"
      );
  private static final ImmutableSet<String> UNARY_OP_TOKENS =
      ImmutableSet.of(
          "-",
          "~"
      );
  private static final ImmutableSet<String> BINARY_OP_TOKENS =
      ImmutableSet.of(
          "+", "-", "*", "/",
          "<", ">", "=",
          "&", ","
      );

  public static ImmutableSet<String> NON_TERMINALS_TO_PARSE =
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

  private ImmutableList<TermParser> TERM_PARSERS = ImmutableList.of(
      TermParser.of(() -> hasLookaheadType(TokenType.INTEGER_CONSTANT), this::parseIntegerConstant),
      TermParser.of(() -> hasLookaheadType(TokenType.STRING_CONSTANT), this::parseStringConstant),
      TermParser.of(() -> hasLookaheadTextIn(KEYWORD_CONSTANT_TOKENS), this::parseKeywordConstant),
      TermParser.of(() -> hasLookaheadText("("), this::parseParenthesizedExpression),
      TermParser.of(() -> hasLookaheadTextIn(UNARY_OP_TOKENS), this::parseTermWithPrecedingUnaryop),
      TermParser.of(() -> hasLookaheadType(TokenType.IDENTIFIER),
          this::parseVariableOrArrayOrSubroutineCall)
  );

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
    expectAndVisitNonTerminal("class", "keyword class");
    match("class");
    parseClassName();
    match("{");
    parseClassVarDecs();
    parseSubroutineDecs();
    match("}");
    endNonTerminalVisit();
  }

  private void parseClassName() {
    expectAndVisitNonTerminal("className", "class name");
    parseIdentifier();
    endNonTerminalVisit();
  }

  private void parseClassVarDecs() {
    while (hasLookaheadTextIn(CLASS_VAR_DEC_LOOKAHEAD_TOKENS)) {
      parseClassVarDec();
    }
  }

  private void parseClassVarDec() {
    expectAndVisitNonTerminal("classVarDec", "class variable declarations");
    matchOneOf(CLASS_VAR_DEC_LOOKAHEAD_TOKENS);
    parseType();
    parseVarName();
    while (hasLookaheadText(",")) {
      match(",");
      parseVarName();
    }
    match(";");
    endNonTerminalVisit();
  }

  // Subroutine non terminal parsing

  private void parseSubroutineDecs() {
    while (hasLookaheadTextIn(SUBROUTINE_DEC_LOOK_AHEAD_TOKENS)) {
      parseSubroutineDec();
    }
  }

  private void parseSubroutineDec() {
    expectAndVisitNonTerminal("subroutineDec", "subroutine declaration");
    matchOneOf(SUBROUTINE_DEC_LOOK_AHEAD_TOKENS);
    parseSubroutineReturnType();
    parseSubroutineName();
    match("(");
    parseSubroutineParameterList();
    match(")");
    parseSubroutineBody();
    endNonTerminalVisit();
  }

  private void parseSubroutineReturnType() {
    expectAndVisitNonTerminal("subroutineReturnType", "subroutine return type");
    if (hasTypeLookaheadToken()) {
      parseType();
    } else {
      match("void");
    }
    endNonTerminalVisit();
  }

  private void parseSubroutineName() {
    expectAndVisitNonTerminal("subroutineName", "subroutine name");
    parseIdentifier();
    endNonTerminalVisit();
  }

  private void parseSubroutineParameterList() {
    if (!hasTypeLookaheadToken()) {
      return;
    }
    expectAndVisitNonTerminal("parameterList", "subroutine parameter list");
    parseTypedVarName();
    while (hasLookaheadText(",")) {
      match(",");
      parseTypedVarName();
    }
    endNonTerminalVisit();
  }

  private void parseSubroutineBody() {
    expectAndVisitNonTerminal("subroutineBody", "subroutine body");
    match("{");
    parseVarDecs();
    parseStatements();
    match("}");
    endNonTerminalVisit();
  }

  private void parseVarDecs() {
    while (hasLookaheadText("var")) {
      parseVarDec();
    }
  }

  private void parseVarDec() {
    expectAndVisitNonTerminal("varDec", "variable declaration");
    match("var");
    parseType();
    parseVarName();
    while (hasLookaheadText(",")) {
      match(",");
      parseVarName();
    }
    match(";");
    endNonTerminalVisit();
  }

  // Statement parsing

  private void parseStatements() {
    boolean isFirstStatementVisited = false;
    while (hasStatementLookaheadToken()) {
      if (!isFirstStatementVisited) {
        isFirstStatementVisited = true;
        expectAndVisitNonTerminal("statements", "list of statements");
      }
      parseStatement();
    }
    if (isFirstStatementVisited) {
      endNonTerminalVisit();
    }
  }

  private void parseStatement() {
    expectAndVisitNonTerminal("statement");
    Optional<Runnable> statementParser = getStatementParser();
    Preconditions.checkArgument(
        statementParser.isPresent(),
        "No parser found for statement beginning at %s", tokens);
    statementParser.get().run();
    endNonTerminalVisit();
  }

  private Optional<Runnable> getStatementParser() {
    Runnable parser = statementParserByLookahead.get(getPeekedToken("statement").tokenText());
    return parser != null ? Optional.of(parser) : Optional.absent();
  }

  private void parseLetStatement() {
    expectAndVisitNonTerminal("letStatement", "let statement");
    match("let");
    expect("token");
    JackToken token = tokens.extract().get();
    if (hasLookaheadText("[")) {
      tokens.putBack(token);
      parseArrayExpression();
    } else {
      tokens.putBack(token);
      parseVarName();
    }
    match("=");
    parseExpression();
    match(";");
    endNonTerminalVisit();
  }

  private void parseIfStatement() {
    expectAndVisitNonTerminal("ifStatement", "if statement");

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

    endNonTerminalVisit();
  }

  private void parseWhileStatement() {
    expectAndVisitNonTerminal("whileStatement", "while statement");
    match("while");
    match("(");
    parseExpression();
    match(")");
    match("{");
    parseStatements();
    match("}");
    endNonTerminalVisit();
  }

  private void parseDoStatement() {
    expectAndVisitNonTerminal("doStatement", "do statement");
    match("do");
    parseSubroutineCall();
    match(";");
    endNonTerminalVisit();
  }

  private void parseSubroutineCall() {
    expectAndVisitNonTerminal("subroutineCall", "subroutine call");
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
    endNonTerminalVisit();
  }

  private void parseReturnStatement() {
    expectAndVisitNonTerminal("returnStatement", "return statement");
    match("return");
    if (!hasLookaheadText(";")) {
      parseExpression();
    }
    match(";");
    endNonTerminalVisit();
  }

  // Expressions

  private void parseExpressionList() {
    if (!hasExpressionLookaheadToken()) {
      return;
    }

    expectAndVisitNonTerminal("expressionList", "list of expressions");
    parseExpression();
    while (hasLookaheadText(",")) {
      match(",");
      parseExpression();
    }
    endNonTerminalVisit();
  }

  private void parseExpression() {
    expectAndVisitNonTerminal("expression");
    parseTerm();
    while (hasLookaheadTextIn(BINARY_OP_TOKENS)) {
      matchOneOf(BINARY_OP_TOKENS);
      parseTerm();
    }
    endNonTerminalVisit();
  }

  private void parseParenthesizedExpression() {
    expectAndVisitNonTerminal("parenthesized expression");
    match("(");
    parseExpression();
    match(")");
    endNonTerminalVisit();
  }

  private void parseTerm() {
    expectAndVisitNonTerminal("term");
    Runnable parser = getTermParser();
    parser.run();
    endNonTerminalVisit();
  }

  private void parseVariableOrArrayOrSubroutineCall() {
    JackToken token = tokens.extract().get();
    if (hasLookaheadText("[")) {
      tokens.putBack(token);
      parseArrayExpression();
      return;
    }

    if (hasLookaheadTextIn(ImmutableSet.of("(", "."))) {
      tokens.putBack(token);
      parseSubroutineCall();
      return;
    }

    tokens.putBack(token);
    parseVarName();
  }

  private void parseArrayExpression() {
    expectAndVisitNonTerminal("arrayExpression", "array expression");
    parseVarName();
    match("[");
    parseExpression();
    match("]");
    endNonTerminalVisit();
  }

  private void parseTermWithPrecedingUnaryop() {
    expectAndVisitNonTerminal("term");
    matchOneOf(UNARY_OP_TOKENS);
    parseTerm();
    endNonTerminalVisit();
  }

  private Runnable getTermParser() {
    Optional<TermParser> termParser = findMatchingTermParser();
    Preconditions.checkArgument(termParser.isPresent(), "No parser found for %s.", tokens);
    return termParser.get().parser();
  }

  private Optional<TermParser> findMatchingTermParser() {
    return Optional.fromJavaUtil(
        TERM_PARSERS
            .stream()
            .filter(p -> {
              try {
                return p.matcher().call();
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
            })
            .findFirst());
  }

  // Types parsing

  private void parseTypedVarName() {
    expectAndVisitNonTerminal("typedVarName", "type followed by variable name");
    parseType();
    parseVarName();
    endNonTerminalVisit();
  }

  private void parseType() {
    expectAndVisitNonTerminal("type");
    if (hasLookaheadType(TokenType.IDENTIFIER)) {
      parseIdentifier();
    } else {
      matchOneOf(PRIMITIVE_TYPE_TOKENS);
    }
    endNonTerminalVisit();
  }

  private void parseVarName() {
    expectAndVisitNonTerminal("varName", "variable name");
    parseIdentifier();
    endNonTerminalVisit();
  }

  // Parsing terminals

  private void parseKeywordConstant() {
    expect(KEYWORD_CONSTANT_TOKENS.toString());
    JackToken token = extractToken(TokenType.KEYWORD);
    Preconditions.checkArgument(KEYWORD_CONSTANT_TOKENS.contains(token.tokenText()));
    visitor.visitTerminal(token);
  }

  private void parseStringConstant() {
    parseToken("string constant", TokenType.STRING_CONSTANT);
  }

  private void parseIdentifier() {
    parseToken("identifier", TokenType.IDENTIFIER);
  }

  private void parseIntegerConstant() {
    parseToken("integer constant", TokenType.INTEGER_CONSTANT);
  }

  private void parseToken(String tokenDescription, TokenType tokenType) {
    expect(tokenDescription);
    visitor.visitTerminal(extractToken(tokenType));
  }

  // Look ahead functions

  private boolean hasStatementLookaheadToken() {
    return !tokens.isEmpty()
        && statementParserByLookahead.keySet().contains(getPeekedToken("statement").tokenText());
  }

  private boolean hasExpressionLookaheadToken() {
    return hasTermLookaheadToken();
  }

  private boolean hasTermLookaheadToken() {
    return findMatchingTermParser().isPresent();
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

  private void match(String tokenText) {
    matchOneOf(ImmutableSet.of(tokenText));
  }

  private void matchOneOf(
      ImmutableSet<String> tokenTexts) {
    expect(tokenTexts.toString());
    JackToken token = tokens.extract().get();
    Preconditions.checkArgument(
        tokenTexts.contains(token.tokenText()),
        "Expected %s but found %s.", tokenTexts, token.toString());
    visitor.visitTerminal(token);
  }

  private JackToken getPeekedToken(String expectedTokenDescription) {
    expect(expectedTokenDescription);
    return tokens.peek().get();
  }

  private JackToken extractToken(TokenType tokenType) {
    expect(tokenType.toString());
    JackToken token = tokens.extract().get();
    Preconditions.checkArgument(
        token.tokenType().equals(tokenType),
        "Expected %s but found %s.", tokenType, token.toString());
    return token;
  }

  // Errors and visitation.

  private void expect(String tokenDescription) {
    Preconditions
        .checkArgument(!tokens.isEmpty(), "No further tokens. Expected %s", tokenDescription);
  }

  private void expectAndVisitNonTerminal(String nonTerminalText, String nonTerminalDesc) {
    expect(nonTerminalDesc);
    visitor.beginNonTerminalVisit(nonTerminalText);
  }

  private void expectAndVisitNonTerminal(String nonTerminalText) {
    expectAndVisitNonTerminal(nonTerminalText, nonTerminalText);
  }

  private void endNonTerminalVisit() {
    visitor.endNonTerminalVisit();
  }
}
