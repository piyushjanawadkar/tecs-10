package com.computer.nand2tetris.compiler.io;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.MoreCollectors.onlyElement;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class IOPathsCreator {

  private static final String PARSER_OUTPUT_XML_SUFFIX = ".xml";
  private static final String TOKENIZER_OUTPUT_XML_SUFFIX = "T.xml";
  private static final String JACK_FILE_EXTENSION = ".jack";
  private static final String OUTPUT_SUBDIR = "parseroutput";

  public static ImmutableList<IOPaths> createPaths(String[] args) {
    String inputLocation = extractInputLocation(args);
    File file = new File(inputLocation);
    if (file.isDirectory()) {
      return createPathsFromDirectory(file);
    }

    if (file.isFile() && hasJackExtension(file.getPath())) {
      return ImmutableList.of(createPathsFromFile(file));
    }

    throw new RuntimeException(
        "Input location " + inputLocation + " is neither a .jack file nor a directory.");
  }

  private static ImmutableList<IOPaths> createPathsFromDirectory(File inputDirectory) {
    return Arrays.stream(
        inputDirectory.listFiles((dir, name) -> {
          return hasJackExtension(name);
        }))
        .map(IOPathsCreator::createPathsFromFile)
        .collect(toImmutableList());
  }


  private static boolean hasJackExtension(String path) {
    return path.endsWith(JACK_FILE_EXTENSION);
  }

  private static IOPaths createPathsFromFile(File inputFile) {
    String parentPath = inputFile.getParent();
    String outputSubdirectory = createSubdirectory(parentPath, OUTPUT_SUBDIR);
    return IOPaths.create(
        inputFile.getAbsolutePath(),
        createXmlOutputPath(outputSubdirectory, generateTokenizerOutputBasename(inputFile)),
        createXmlOutputPath(outputSubdirectory, generateParserOutputBasename(inputFile)));
  }

  private static String createSubdirectory(String parentPath, String outputSubdir) {
    Path outputSubdirPath = Paths.get(parentPath, outputSubdir);
    File file = new File(outputSubdirPath.toString());
    if (!file.exists()) {
      Preconditions.checkState(file.mkdir(), "output directory %s creation failed.",
          file.getAbsolutePath());
    }
    return outputSubdirPath.toString();
  }

  private static String generateTokenizerOutputBasename(File inputFile) {
    return replaceSuffix(inputFile.toPath().getFileName(), TOKENIZER_OUTPUT_XML_SUFFIX);
  }

  private static String createXmlOutputPath(String outputSubdirectory, String baseName) {
    return Paths.get(outputSubdirectory, baseName).toAbsolutePath().toString();
  }

  private static String generateParserOutputBasename(File inputFile) {
    return replaceSuffix(inputFile.toPath().getFileName(), PARSER_OUTPUT_XML_SUFFIX);
  }

  private static String replaceSuffix(Path fileName, String parserOutputXmlSuffix) {
    String stringFileName = fileName.toString();
    Preconditions.checkArgument(hasJackExtension(stringFileName));
    return fileName.toString().substring(0, stringFileName.length() - JACK_FILE_EXTENSION.length())
        + parserOutputXmlSuffix;
  }

  private static String extractInputLocation(String[] args) {
    return Arrays.stream(args).collect(onlyElement());
  }
}
