package com.computer.nand2tetris.compiler;

import com.google.common.base.Optional;
import java.io.BufferedReader;
import java.util.stream.Stream;

class JackTokenizer {

  Stream<JackToken> tokenize(BufferedReader reader) {
    JackPreprocessor preprocessor = new JackPreprocessor();
    return reader.lines().map(preprocessor::preprocess)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .flatMap(l -> tokenizeLine(l));
  }

  private static Stream<JackToken> tokenizeLine(String line) {
    return null;
  }
}
