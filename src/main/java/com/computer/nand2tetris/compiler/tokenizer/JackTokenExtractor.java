package com.computer.nand2tetris.compiler.tokenizer;

import com.computer.nand2tetris.compiler.JackToken;
import com.computer.nand2tetris.compiler.LookAheadStream;

interface JackTokenExtractor {

  JackToken extractToken(LookAheadStream<Character> lookAheadStream);

  boolean matches(Character lookAhead);
}
