package com.computer.nand2tetris.compiler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.stream.Stream;

public class JackAnalyzer {

  private final JackTokenizer tokenizer;
  private final JackParser parser;

  JackAnalyzer(JackTokenizer tokenizer, JackParser parser) {
    this.tokenizer = tokenizer;
    this.parser = parser;
  }

  private void analyze(IOLocations ioLocations) throws IOException {
    BufferedWriter writer = createWriter(ioLocations.outputFilePath());
    ioLocations.inputFilePaths().stream()
        .forEachOrdered(f -> compile(f, writer));
    writer.close();
  }

  private void compile(String filePath, BufferedWriter writer) {
    try {
      BufferedReader reader = createReader(filePath);
      Stream<JackToken> tokenStream = tokenizer.tokenize(reader);
      parser.parse(tokenStream, writer);
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
    IOLocations ioLocations = IOLocations.create(args);
    JackAnalyzer analyzer = new JackAnalyzer(new JackTokenizer(), new JackParser());
    analyzer.analyze(ioLocations);
  }
}
