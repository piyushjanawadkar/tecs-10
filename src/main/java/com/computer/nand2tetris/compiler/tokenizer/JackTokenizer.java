package com.computer.nand2tetris.compiler.tokenizer;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;

import com.computer.nand2tetris.compiler.ErrorMessageGenerator;
import com.computer.nand2tetris.compiler.JackToken;
import com.computer.nand2tetris.compiler.JackToken.TokenType;
import com.computer.nand2tetris.compiler.LookAheadStream;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.io.BufferedReader;
import java.util.stream.Stream;

public class JackTokenizer {

  private static final ImmutableSet<JackTokenExtractor> TOKEN_EXTRACTORS =
      ImmutableSet.of(
          new WhitespaceTokenExtractor(),
          new SymbolTokenExtractor(),
          new IntegerConstantTokenExtractor(),
          new StringConstantTokenExtractor(),
          new IdentifierOrKeywordTokenExtractor());

  public ImmutableList<JackToken> tokenize(BufferedReader reader) {
    JackPreprocessor preprocessor = new JackPreprocessor();
    ImmutableList<JackToken> tokens =
        reader
            .lines()
            .map(preprocessor::preprocess)
            .flatMap(l -> tokenizeLine(l))
            .collect(toImmutableList());
    preprocessor.done();
    return tokens;
  }

  private static Stream<JackToken> tokenizeLine(String line) {
    LookAheadStream<Character> lookAheadStream = new LookAheadStream(Lists.charactersOf(line));
    ImmutableList.Builder<JackToken> builder = ImmutableList.builder();
    while (lookAheadStream.peek().isPresent()) {
      JackTokenExtractor tokenExtractor = getOnlyTokenExtractorForLookAhead(
          lookAheadStream.peek().get());
      JackToken token = tokenExtractor.extractToken(lookAheadStream);
      if (!token.tokenType().equals(TokenType.WHITESPACE)) {
        builder.add(token);
      }
    }
    return builder.build().stream();
  }

  private static JackTokenExtractor getOnlyTokenExtractorForLookAhead(Character lookAhead) {
    ImmutableSet<JackTokenExtractor> tokenExtractorsMatchingLookAhead = TOKEN_EXTRACTORS.stream()
        .filter(x -> x.matches(lookAhead)).collect(toImmutableSet());
    Preconditions.checkArgument(tokenExtractorsMatchingLookAhead.size() == 1,
        "Exactly one token extractor expected for lookahead *%s*. Found [%s]",
        lookAhead,
        ErrorMessageGenerator.generateClassNamesCsv(tokenExtractorsMatchingLookAhead));
    return Iterables.getOnlyElement(tokenExtractorsMatchingLookAhead);
  }
}
