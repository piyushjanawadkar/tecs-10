package com.computer.nand2tetris.compiler;

import static com.google.common.collect.ImmutableList.toImmutableList;

import com.computer.nand2tetris.compiler.io.ParsedXmlWriter;
import com.computer.nand2tetris.compiler.io.IOPaths;
import com.computer.nand2tetris.compiler.io.IOPathsCreator;
import com.computer.nand2tetris.compiler.parser.JackParser;
import com.computer.nand2tetris.compiler.tokenizer.JackTokenizer;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.stream.Stream;

final class JackAnalyzer {

  private final JackTokenizer tokenizer;
  private final JackParser parser;

  JackAnalyzer(JackTokenizer tokenizer, JackParser parser) {
    this.tokenizer = tokenizer;
    this.parser = parser;
  }

  private void analyze(ImmutableList<IOPaths> ioPaths) throws IOException {
    Context context = buildContext(ioPaths);
    ioPaths.stream().forEachOrdered(p -> compile(p, context));
  }

  private Context buildContext(ImmutableList<IOPaths> ioPaths) {
    Context context = new Context();
    ImmutableList<BufferedReader> inputReaders = createInputReaders(getInputPaths(ioPaths));
    inputReaders
        .stream()
        .map(tokenizer::tokenize)
        .forEachOrdered(t -> parser.parse(t, Optional.absent(), context));
    closeInputReaders(inputReaders);
    return context;
  }

  private void closeInputReaders(ImmutableList<BufferedReader> readers) {
    readers.stream().forEachOrdered(r -> {
      try {
        r.close();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
  }

  private ImmutableList<BufferedReader> createInputReaders(Stream<String> inputPaths) {
    return inputPaths.map(JackAnalyzer::createReader).collect(toImmutableList());
  }

  private static Stream<String> getInputPaths(ImmutableList<IOPaths> ioPaths) {
    return ioPaths.stream().map(IOPaths::inputFilePath);
  }

  private void compile(IOPaths ioPaths, Context context) {
    try {
      BufferedReader reader = createReader(ioPaths.inputFilePath());
      BufferedWriter writer = createWriter(ioPaths.parserOutputPath());
      ImmutableList<JackToken> tokens = tokenizer.tokenize(reader);
      parser.parse(
          tokens,
          Optional.of(context),
          new ParsedXmlWriter(writer));
      reader.close();
      writer.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static BufferedReader createReader(String filePath) {
    try {
      return new BufferedReader(new FileReader(filePath));
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  private static BufferedWriter createWriter(String filePath) throws IOException {
    return new BufferedWriter(new FileWriter(filePath));
  }

  public static void main(String[] args) throws IOException {
    ImmutableList<IOPaths> ioPaths = IOPathsCreator.createPaths(args);
    JackAnalyzer analyzer = new JackAnalyzer(new JackTokenizer(), new JackParser());
    analyzer.analyze(ioPaths);
  }
}
