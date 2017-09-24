package com.computer.nand2tetris.compiler.parser;

import com.computer.nand2tetris.compiler.JackToken;
import com.computer.nand2tetris.compiler.JackToken.TokenType;
import com.computer.nand2tetris.compiler.LookAheadStream;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.io.BufferedWriter;

public class JackParser {

  public void parse(ImmutableList<JackToken> tokens, BufferedWriter bufferedWriter) {
    LookAheadStream<JackToken> tokenStream = new LookAheadStream<>(tokens);
    CompiledCodeWriter writer = new CompiledXmlWriter(bufferedWriter);
    parseClass(tokenStream, writer);
  }

  private void parseClass(LookAheadStream<JackToken> tokens, CompiledCodeWriter writer) {
    writer.writeOpenNonTerminalTag("class");
    match("class", tokens, writer);
    parseClassName(tokens, writer);
    match("{", tokens, writer);
    parseClassVarDecs(tokens, writer);
    parseSubroutineDecs(tokens, writer);
    match("}", tokens, writer);
    writer.writeClosingNonTerminalTag("class");
  }

  private void parseClassVarDecs(LookAheadStream<JackToken> tokens, CompiledCodeWriter writer) {
    // TODO
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

  private void match(String tokenText, LookAheadStream<JackToken> tokens,
      CompiledCodeWriter writer) {
    Preconditions.checkArgument(
        tokens.peek().isPresent(),
        "No further tokens. Expected %s.", tokenText);
    JackToken token = tokens.extract().get();
    Preconditions.checkArgument(
        token.tokenText().equals(tokenText),
        "Expected %s but found %s.", tokenText, token.toString());
    writer.writeTerminal(token);
  }
}
