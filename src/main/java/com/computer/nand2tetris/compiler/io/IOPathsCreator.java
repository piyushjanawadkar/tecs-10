package com.computer.nand2tetris.compiler.io;

import com.google.common.collect.ImmutableList;

public class IOPathsCreator {

  public static ImmutableList<IOPaths> createPaths(String[] args) {
    return ImmutableList.of(
        IOPaths.create(
            "/tmp/inputclass.jack",
            "/tmp/outputT.xml",
            "/tmp/output.xml"));
  }
}
