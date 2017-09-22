package com.computer.nand2tetris.compiler.parser;

import static com.google.common.collect.ImmutableSet.toImmutableSet;

import com.computer.nand2tetris.compiler.ErrorMessageGenerator;
import com.computer.nand2tetris.compiler.JackToken;
import com.computer.nand2tetris.compiler.LookAheadStream;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import java.io.BufferedWriter;

public class JackParser {

  private static final ImmutableSet<TokenSequenceParser> TOKEN_SEQUENCE_PARSERS = ImmutableSet.of(
      new TokenWriter()
  );

  public void parse(ImmutableList<JackToken> tokens, BufferedWriter writer) {
    LookAheadStream<JackToken> tokenStream = new LookAheadStream(tokens);
    while (tokenStream.peek().isPresent()) {
      TokenSequenceParser tokenSequenceParser = getOnlyTokenSequenceParser(
          tokenStream.peek().get());
      tokenSequenceParser.parse(tokenStream, writer);
    }
  }

  private TokenSequenceParser getOnlyTokenSequenceParser(JackToken token) {
    ImmutableSet<TokenSequenceParser> parsers = TOKEN_SEQUENCE_PARSERS.stream()
        .filter(p -> p.matches(token)).collect(toImmutableSet());
    Preconditions.checkArgument(parsers.size() == 1,
        "Exactly 1 token sequence parser expected for token: %s. Found [%s]", token.toString(),
        ErrorMessageGenerator.generateClassNamesCsv(parsers));
    return Iterables.getOnlyElement(parsers);
  }
}
