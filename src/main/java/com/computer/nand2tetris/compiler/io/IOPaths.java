package com.computer.nand2tetris.compiler.io;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class IOPaths {

  static IOPaths create(String inputPath, String tokenizerOutputPath, String parserOutputPath) {
    return new AutoValue_IOPaths(inputPath, tokenizerOutputPath, parserOutputPath);
  }

  public abstract String inputFilePath();

  public abstract String tokenizerOutputPath();

  public abstract String parserOutputPath();
}
