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

  public static final ImmutableSet<String> NON_TERMINALS_TO_PARSE =
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
          "whileStatement",
          "ifStatement",
          "returnStatement",
          "letStatement",
          "doStatement",
          // expressions
          "expression",
          "term",
          "expressionList");

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
          "&", "|"
      );

  private Optional<Context> context;

  private TokensVisitor tokensVisitor;

  private LookAheadStream<JackToken> tokens;

  private ImmutableMap<String, Runnable> statementParserByLookahead =
      ImmutableMap.of(
          "let", this::parseLetStatement,
          "if", this::parseIfStatement,
          "while", this::parseWhileStatement,
          "do", this::parseDoStatement,
          "return", this::parseReturnStatement);

  private ImmutableList<TermParser> TERM_PARSERS = ImmutableList.of(
      TermParser.of(
          () -> hasLookaheadType(TokenType.INTEGER_CONSTANT),
          this::parseIntegerConstant),
      TermParser.of(
          () -> hasLookaheadType(TokenType.STRING_CONSTANT),
          this::parseStringConstant),
      TermParser.of(
          () -> hasLookaheadTextIn(KEYWORD_CONSTANT_TOKENS),
          this::parseKeywordConstant),
      TermParser.of(
          () -> hasLookaheadText("("),
          this::parseParenthesizedExpression),
      TermParser.of(
          () -> hasLookaheadTextIn(UNARY_OP_TOKENS),
          this::parseTermWithPrecedingUnaryop),
      TermParser.of(
          () -> hasLookaheadType(TokenType.IDENTIFIER),
          this::parseVariableOrArrayOrSubroutineCall)
  );

  public void parse(
      ImmutableList<JackToken> tokenList,
      Optional<Context> context,
      JackElementVisitor visitor) {
    this.context = context;
    this.tokens = new LookAheadStream<>(tokenList);
    this.tokensVisitor = TokensVisitor.create(tokens, visitor);
    parseClass();
    Preconditions.checkArgument(tokens.isEmpty(), "Unexpected trailing tokens: %s", tokens);
  }

  private void parseClass() {
    tokensVisitor.nonTerminalParserOf("class", "keyword class")
        .parse(
            () -> {
              match("class");
              parseClassName();
              match("{");
              parseClassVarDecs();
              parseSubroutineDecs();
              match("}");
            });
  }

  private void parseClassName() {
    tokensVisitor.nonTerminalParserOf("className", "class name")
        .parse(this::parseIdentifier);
  }

  private void parseClassVarDecs() {
    while (hasLookaheadTextIn(CLASS_VAR_DEC_LOOKAHEAD_TOKENS)) {
      parseClassVarDec();
    }
  }

  private void parseClassVarDec() {
    tokensVisitor.nonTerminalParserOf("classVarDec", "class variable declarations")
        .parse(
            () -> {
              matchOneOf(CLASS_VAR_DEC_LOOKAHEAD_TOKENS);
              parseType();
              parseVarName();
              while (hasLookaheadText(",")) {
                match(",");
                parseVarName();
              }
              match(";");
            });
  }

  // Subroutine non terminal parsing

  private void parseSubroutineDecs() {
    while (hasLookaheadTextIn(SUBROUTINE_DEC_LOOK_AHEAD_TOKENS)) {
      parseSubroutineDec();
    }
  }

  private void parseSubroutineDec() {
    tokensVisitor.nonTerminalParserOf("subroutineDec", "subroutine declaration")
        .parse(
            () -> {
              matchOneOf(SUBROUTINE_DEC_LOOK_AHEAD_TOKENS);
              parseSubroutineReturnType();
              parseSubroutineName();
              match("(");
              parseSubroutineParameterList();
              match(")");
              parseSubroutineBody();
            }
        );
  }

  private void parseSubroutineReturnType() {
    tokensVisitor.nonTerminalParserOf("subroutineReturnType", "subroutine return type")
        .parse(
            () -> {
              if (hasTypeLookaheadToken()) {
                parseType();
              } else {
                match("void");
              }
            });
  }

  private void parseSubroutineName() {
    tokensVisitor.nonTerminalParserOf("subroutineName", "subroutine name")
        .parse(this::parseIdentifier);
  }

  private void parseSubroutineParameterList() {
    tokensVisitor.nonTerminalParserOf("parameterList", "subroutine parameter list")
        .parse(
            () -> {
              if (hasTypeLookaheadToken()) {
                parseTypedVarName();
                while (hasLookaheadText(",")) {
                  match(",");
                  parseTypedVarName();
                }
              }
            });
  }

  private void parseSubroutineBody() {
    tokensVisitor.nonTerminalParserOf("subroutineBody", "subroutine body")
        .parse(
            () -> {
              match("{");
              parseVarDecs();
              parseStatements();
              match("}");
            });
  }

  private void parseVarDecs() {
    while (hasLookaheadText("var")) {
      parseVarDec();
    }
  }

  private void parseVarDec() {
    tokensVisitor.nonTerminalParserOf("varDec", "variable declaration")
        .parse(
            () -> {
              match("var");
              parseType();
              parseVarName();
              while (hasLookaheadText(",")) {
                match(",");
                parseVarName();
              }
              match(";");
            });
  }

  // Statement parsing

  private void parseStatements() {
    if (!hasStatementLookaheadToken()) {
      return;
    }

    tokensVisitor.nonTerminalParserOf("statements", "list of statements")
        .parse(
            () -> {
              while (hasStatementLookaheadToken()) {
                parseStatement();
              }
            });
  }

  private void parseStatement() {
    tokensVisitor.nonTerminalParserOf("statement")
        .parse(
            () -> {
              Optional<Runnable> statementParser = getStatementParser();
              Preconditions.checkArgument(
                  statementParser.isPresent(),
                  "No parser found for statement beginning at %s", tokens);
              statementParser.get().run();
            });
  }

  private Optional<Runnable> getStatementParser() {
    Runnable parser = statementParserByLookahead
        .get(getPeekedTokenExpecting("statement").tokenText());
    return parser != null ? Optional.of(parser) : Optional.absent();
  }

  private void parseLetStatement() {
    tokensVisitor.nonTerminalParserOf("letStatement", "let statement")
        .parse(
            () -> {
              match("let");
              tokens.expect("token");
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
            });
  }

  private void parseIfStatement() {
    tokensVisitor.nonTerminalParserOf("ifStatement", "if statement")
        .parse(
            () -> {
              match("if");
              match("(");
              parseExpression();
              match(")");

              match("{");
              parseStatements();
              match("}");

              if (getPeekedTokenExpecting("else").tokenText().equals("else")) {
                match("else");
                match("{");
                parseStatements();
                match("}");
              }
            });
  }

  private void parseWhileStatement() {
    tokensVisitor.nonTerminalParserOf("whileStatement", "while statement")
        .parse(
            () -> {
              match("while");
              match("(");
              parseExpression();
              match(")");
              match("{");
              parseStatements();
              match("}");
            });
  }

  private void parseDoStatement() {
    tokensVisitor.nonTerminalParserOf("doStatement", "do statement")
        .parse(
            () -> {
              match("do");
              parseSubroutineCall();
              match(";");
            });
  }

  private void parseSubroutineCall() {
    tokensVisitor.nonTerminalParserOf("subroutineCall", "subroutine call")
        .parse(
            () -> {
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
            });
  }

  private void parseReturnStatement() {
    tokensVisitor.nonTerminalParserOf("returnStatement", "return statement")
        .parse(
            () -> {
              match("return");
              if (!hasLookaheadText(";")) {
                parseExpression();
              }
              match(";");
            });
  }

  // Expressions

  private void parseExpressionList() {
    tokensVisitor.nonTerminalParserOf("expressionList", "list of expressions")
        .parse(
            () -> {
              if (hasExpressionLookaheadToken()) {
                parseExpression();
                while (hasLookaheadText(",")) {
                  match(",");
                  parseExpression();
                }
              }
            });
  }

  private void parseExpression() {
    tokensVisitor.nonTerminalParserOf("expression")
        .parse(
            () -> {
              parseTerm();
              while (hasLookaheadTextIn(BINARY_OP_TOKENS)) {
                matchOneOf(BINARY_OP_TOKENS);
                parseTerm();
              }
            });
  }

  private void parseParenthesizedExpression() {
    tokensVisitor.nonTerminalParserOf("parenthesized expression")
        .parse(
            () -> {
              match("(");
              parseExpression();
              match(")");
            });
  }

  private void parseTerm() {
    tokensVisitor.nonTerminalParserOf("term")
        .parse(
            () -> {
              Runnable parser = getTermParser();
              parser.run();
            });
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
    tokensVisitor.nonTerminalParserOf("arrayExpression", "array expression")
        .parse(
            () -> {
              parseVarName();
              match("[");
              parseExpression();
              match("]");
            });
  }

  private void parseTermWithPrecedingUnaryop() {
    tokensVisitor
        .nonTerminalParserOf("termWithPrecedingUnaryOp", "term preceded by a unary operator")
        .parse(
            () -> {
              matchOneOf(UNARY_OP_TOKENS);
              parseTerm();
            });
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
    tokensVisitor.nonTerminalParserOf("typedVarName", "type followed by variable name")
        .parse(
            () -> {
              parseType();
              parseVarName();
            });
  }

  private void parseType() {
    tokensVisitor.nonTerminalParserOf("type")
        .parse(
            () -> {
              if (hasLookaheadType(TokenType.IDENTIFIER)) {
                parseIdentifier();
              } else {
                matchOneOf(PRIMITIVE_TYPE_TOKENS);
              }
            });
  }

  private void parseVarName() {
    tokensVisitor.nonTerminalParserOf("varName", "variable name")
        .parse(this::parseIdentifier);
  }

  // Parsing terminals

  private void parseKeywordConstant() {
    tokens.expect(KEYWORD_CONSTANT_TOKENS.toString());
    JackToken token = extractToken(TokenType.KEYWORD);
    Preconditions.checkArgument(KEYWORD_CONSTANT_TOKENS.contains(token.tokenText()));
    tokensVisitor.visitor().visitTerminal(token);
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
    tokens.expect(tokenDescription);
    tokensVisitor.visitor().visitTerminal(extractToken(tokenType));
  }

  // Look ahead functions

  private boolean hasStatementLookaheadToken() {
    return !tokens.isEmpty()
        && statementParserByLookahead
        .keySet()
        .contains(
            getPeekedTokenExpecting("statement").tokenText());
  }

  private boolean hasExpressionLookaheadToken() {
    return hasTermLookaheadToken();
  }

  private boolean hasTermLookaheadToken() {
    return findMatchingTermParser().isPresent();
  }

  private boolean hasTypeLookaheadToken() {
    return hasLookaheadTextIn(PRIMITIVE_TYPE_TOKENS)
        || hasLookaheadType(TokenType.IDENTIFIER);
  }

  private boolean hasLookaheadTextIn(ImmutableSet<String> expectedTokenTexts) {
    return !tokens.isEmpty() && expectedTokenTexts
        .contains(getPeekedTokenExpecting(expectedTokenTexts.toString()).tokenText());
  }

  private boolean hasLookaheadText(String expectedText) {
    return hasLookaheadTextIn(ImmutableSet.of(expectedText));
  }

  private boolean hasLookaheadType(TokenType tokenType) {
    return !tokens.isEmpty() && getPeekedTokenExpecting(tokenType.toString()).tokenType()
        .equals(tokenType);
  }

  private boolean hasClassNameLookahead() {
    return !tokens.isEmpty() && context.isPresent() && context.get()
        .isClassNameToken(getPeekedTokenExpecting("class name"));
  }

  private void match(String tokenText) {
    matchOneOf(ImmutableSet.of(tokenText));
  }

  private void matchOneOf(
      ImmutableSet<String> tokenTexts) {
    tokens.expect(tokenTexts.toString());
    JackToken token = tokens.extract().get();
    Preconditions.checkArgument(
        tokenTexts.contains(token.tokenText()),
        "Expected %s but found %s.", tokenTexts, token.toString());
    tokensVisitor.visitor().visitTerminal(token);
  }

  private JackToken getPeekedTokenExpecting(String expectedTokenDescription) {
    tokens.expect(expectedTokenDescription);
    return tokens.peek().get();
  }

  private JackToken extractToken(TokenType tokenType) {
    tokens.expect(tokenType.toString());
    JackToken token = tokens.extract().get();
    Preconditions.checkArgument(
        token.tokenType().equals(tokenType),
        "Expected %s but found %s.", tokenType, token.toString());
    return token;
  }
}
