package com.computer.nand2tetris.compiler;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.util.Arrays;

@AutoValue
abstract class IOPaths {

  static IOPaths create(String[] args) {
    return new AutoValue_IOPaths(ImmutableList.of("/tmp/input.txt"), "/tmp/output.txt");
    /*String location = extractLocation(args);
    if (isJackFile(location)) {
      return createFromJackFile(location);
    }

    if (isDirectory(location)) {
      createFromJackFileDirectory(location);
    }

    throw new RuntimeException("Either a .jack file or a directory expected. Found " + location);*/
  }

  private static void createFromJackFileDirectory(String location) {
  }

  private static boolean isDirectory(String location) {
    return false;
  }

  private static IOPaths createFromJackFile(String location) {
    return null;
  }

  private static boolean isJackFile(String location) {
    return false;
  }

  private static String extractLocation(String[] args) {
    return Iterables.getOnlyElement(Arrays.asList(args));
  }

  abstract ImmutableList<String> inputFilePaths();

  abstract String outputFilePath();
}
