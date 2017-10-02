package com.computer.nand2tetris.compiler.io;

import com.google.common.base.Optional;

public class CamelCaseConverter {

  public static String convert(String text) {
    StringBuilder builder = new StringBuilder();
    convert(text, 0, builder);
    return builder.toString();
  }

  private static void convert(String text, int startIndex, StringBuilder builder) {
    if (startIndex >= text.length()) {
      return;
    }

    int delimIndex = findDelimPosition(text, startIndex);
    builder.append(text, startIndex, delimIndex);

    Optional<Character> upperCasedCharacter =
        getCamelCaseCharacterForTwoLetterSubstring(text, delimIndex);
    if (upperCasedCharacter.isPresent()) {
      builder.append(upperCasedCharacter.get());
    }
    convert(text, delimIndex + 2, builder);
  }

  private static Optional<Character> getCamelCaseCharacterForTwoLetterSubstring(
      String text,
      int delimIndex) {
    int delimFollowerIndex = delimIndex + 1;
    return delimFollowerIndex < text.length()
        ? Optional.of(Character.toUpperCase(text.charAt(delimFollowerIndex)))
        : Optional.absent();
  }

  private static int findDelimPosition(String text, int startIndex) {
    int delimIndex = text.indexOf("_", startIndex);
    return delimIndex >= 0 ? delimIndex : text.length();
  }
}
