package com.computer.nand2tetris.compiler;

import com.computer.nand2tetris.compiler.tokenizer.JackTokenizer;
import com.google.common.collect.ImmutableList;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

final class JackAnalyzer {

  private final JackTokenizer tokenizer;
  private final JackParser parser;

  JackAnalyzer(JackTokenizer tokenizer, JackParser parser) {
    this.tokenizer = tokenizer;
    this.parser = parser;
  }

  private void analyze(IOPaths ioPaths) throws IOException {
    BufferedWriter writer = createWriter(ioPaths.outputFilePath());
    ioPaths.inputFilePaths().stream()
        .forEachOrdered(f -> compile(f, writer));
    writer.close();
  }

  private void compile(String filePath, BufferedWriter writer) {
    try {
      BufferedReader reader = createReader(filePath);
      ImmutableList<JackToken> tokens = tokenizer.tokenize(reader);
      parser.parse(tokens, writer);
      reader.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static BufferedReader createReader(String filePath) throws FileNotFoundException {
    return new BufferedReader(new FileReader(filePath));
  }

  private static BufferedWriter createWriter(String filePath) throws IOException {
    return new BufferedWriter(new FileWriter(filePath));
  }

  public static void main(String[] args) throws IOException {
    IOPaths ioPaths = IOPaths.create(args);
    JackAnalyzer analyzer = new JackAnalyzer(new JackTokenizer(), new JackParser());
    analyzer.analyze(ioPaths);
  }
}
