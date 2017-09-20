package com.computer.nand2tetris.compiler;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import java.io.BufferedReader;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class JackTokenizer {

  private static final ImmutableSet<JackTokenExtractor> TOKEN_EXTRACTORS =
      ImmutableSet.of(
          new SymbolTokenExtractor(),
          new IntegerConstantTokenExtractor(),
          new StringConstantTokenExtractor(),
          new IdentifierOrKeywordTokenExtractor());

  ImmutableList<JackToken> tokenize(BufferedReader reader) {
    JackPreprocessor preprocessor = new JackPreprocessor();
    return reader.lines()
        .map(preprocessor::preprocess)
        .flatMap(l -> tokenizeLine(l))
        .collect(toImmutableList());
  }

  private static Stream<JackToken> tokenizeLine(String line) {
    LookAheadStream lookAheadStream = new LookAheadStream(line);
    ImmutableList.Builder<JackToken> builder = ImmutableList.builder();
    while (lookAheadStream.peek().isPresent()) {
      JackTokenExtractor tokenExtractor = getOnlyTokenExtractorForLookAhead(
          lookAheadStream.peek().get());
      JackToken token = tokenExtractor.extractToken(lookAheadStream);
      lookAheadStream.skipWhitespace();
      builder.add(token);
    }
    return builder.build().stream();
  }

  private static JackTokenExtractor getOnlyTokenExtractorForLookAhead(Character lookAhead) {
    ImmutableSet<JackTokenExtractor> tokenExtractorsMatchingLookAhead = TOKEN_EXTRACTORS.stream()
        .filter(x -> x.matches(lookAhead)).collect(toImmutableSet());
    Preconditions.checkArgument(tokenExtractorsMatchingLookAhead.size() == 1,
        "Exactly one token extractor expected for lookahead *%s*. Found [%s]",
        lookAhead,
        generateTokenExtractorsDebugString(tokenExtractorsMatchingLookAhead));
    return Iterables.getOnlyElement(tokenExtractorsMatchingLookAhead);
  }

  private static String generateTokenExtractorsDebugString(
      ImmutableSet<JackTokenExtractor> tokenExtractors) {
    return tokenExtractors.stream().map(x -> x.getClass().getSimpleName())
        .collect(Collectors.joining(", "));
  }
}