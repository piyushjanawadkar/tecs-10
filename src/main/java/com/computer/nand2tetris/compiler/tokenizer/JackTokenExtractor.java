package com.computer.nand2tetris.compiler.tokenizer;

import com.computer.nand2tetris.compiler.JackToken;

interface JackTokenExtractor {

  JackToken extractToken(LookAheadStream lookAheadStream);

  boolean matches(Character lookAhead);
}
