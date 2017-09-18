package com.computer.nand2tetris.compiler;

interface JackTokenExtractor {

  JackToken extractToken(LookAheadStream lookAheadStream);

  boolean matches(Character lookAhead);
}
