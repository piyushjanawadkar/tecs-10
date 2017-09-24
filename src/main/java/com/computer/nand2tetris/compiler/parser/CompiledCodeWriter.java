package com.computer.nand2tetris.compiler.parser;

import com.computer.nand2tetris.compiler.JackToken;

interface CompiledCodeWriter {

  void writeOpeningNonTerminalTag(String terminalText);

  void writeClosingNonTerminalTag(String terminalText);

  void writeTerminal(JackToken token);
}
