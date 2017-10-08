package com.computer.nand2tetris.compiler.tokenizer;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

class JackPreprocessor {

  private static final String SINGLE_LINE_COMMENT_DELIM = "//";
  private static final String MULTI_LINE_COMMENT_BEGIN_DELIM = "/*";
  private static final String MULTI_LINE_COMMENT_END_DELIM = "*/";
  private boolean inComment = false;

  String preprocess(String line) {
    StringBuilder builder = new StringBuilder();
    stripComment(line, 0, builder);
    Preconditions.checkArgument(!inComment, "unterminated comment.");
    return builder.toString();
  }

  private void stripComment(String line, int index, StringBuilder builder) {
    // initialize state as if we are in the middle of a multiline comment that was begun in a
    // previous line.
    Optional<Integer> commentBeginIndex = Optional.of(index);
    String commentBeginDelim = "";

    if (!inComment) {
      commentBeginIndex = findCommentBeginIndex(line, index);
      if (!commentBeginIndex.isPresent()) {
        // append the entire substring from position index.
        builder.append(line, index, line.length());
        return;
      }

      // append all characters until the beginning of the comment.
      builder.append(line, index, commentBeginIndex.get());

      commentBeginDelim = getCommentBeginDelim(line, commentBeginIndex.get());
      if (SINGLE_LINE_COMMENT_DELIM.equals(commentBeginDelim)) {
        // Ignore the rest of the line as we found a '//'
        return;
      }

      inComment = true;  // a new multiline comment has begun.
    }

    Optional<Integer> commentEndIndex =
        findCommentDelimIndex(
            line,
            commentBeginIndex.get() + commentBeginDelim.length(),
            MULTI_LINE_COMMENT_END_DELIM);
    if (!commentEndIndex.isPresent()) {
      // Skip the rest of the line as no closing multiline comment delimiter was found.
      return;
    }

    inComment = false;
    // recursively strip the substring following the closing multiline comment delimiter.
    stripComment(line, commentEndIndex.get() + MULTI_LINE_COMMENT_END_DELIM.length(), builder);
  }

  private Optional<Integer> findCommentDelimIndex(String line, int index, String delim) {
    int delimIndex = line.indexOf(delim, index);
    return delimIndex >= 0 ? Optional.of(delimIndex) : Optional.absent();
  }

  private String getCommentBeginDelim(String line, int index) {
    return line.startsWith(SINGLE_LINE_COMMENT_DELIM, index) ? SINGLE_LINE_COMMENT_DELIM
        : MULTI_LINE_COMMENT_BEGIN_DELIM;
  }

  private Optional<Integer> findCommentBeginIndex(String line, int index) {
    Optional<Integer> singleLineCommentIndex = findCommentDelimIndex(line, index,
        SINGLE_LINE_COMMENT_DELIM);
    Optional<Integer> multiLineCommentIndex = findCommentDelimIndex(line, index,
        MULTI_LINE_COMMENT_BEGIN_DELIM);

    if (!singleLineCommentIndex.isPresent() && !multiLineCommentIndex.isPresent()) {
      return Optional.absent();
    }

    if (singleLineCommentIndex.isPresent()) {
      return
          multiLineCommentIndex.isPresent() ?
              Optional.of(Math.min(singleLineCommentIndex.get(), multiLineCommentIndex.get()))
              : singleLineCommentIndex;
    }

    return multiLineCommentIndex;
  }
}
