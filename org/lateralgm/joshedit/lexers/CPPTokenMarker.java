/*
 * Copyright (C) 2011, 2014 Josh Ventura <JoshV10@gmail.com>
 *
 * This file is part of JoshEdit. JoshEdit is free software.
 * You can use, modify, and distribute it under the terms of
 * the GNU General Public License, version 3 or later.
 */

package org.lateralgm.joshedit.lexers;

import static org.lateralgm.joshedit.ColorProfile.makeEntry;

import java.awt.Color;
import java.awt.Font;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.lateralgm.joshedit.ColorProfile;
import org.lateralgm.joshedit.ColorProfile.ColorProfileEntry;
import org.lateralgm.joshedit.DefaultTokenMarker;

/**
 * Sample C++ token marker class based on the default token marker.
 */
public class CPPTokenMarker extends DefaultTokenMarker {

  private static class CPPDescription implements LanguageDescription {
    @Override
    public String getName() {
      return "C++"; //$NON-NLS-1$
    }

    @Override
    public Collection<ColorProfile> defaultProfiles() {
      return Arrays.asList(new ColorProfile[] { PROFILE_CODE_BLOCKS });
    }
  }

  /** Retrieve information about the languages supported by this TokenMarker. */
  public static LanguageDescription[] getLanguageDescriptions() {
    return new LanguageDescription[] { new CPPDescription() };
  }

  private static final String S_PREPROCESSOR = "PREPROCESSOR"; //$NON-NLS-1$
  private static final String S_DOUBLEQ_STRING = "DOUBLEQ_STRING"; //$NON-NLS-1$
  private static final String S_CHARACTER_LIT = "CHARACTER_LIT"; //$NON-NLS-1$
  private static final String S_LINE_COMMENT = "LINE_COMMENT"; //$NON-NLS-1$
  private static final String S_LAZY_LINE_COMMENT = "LAZY_FORMAL_LINE_COMMENT"; //$NON-NLS-1$
  private static final String S_DOC_LINE_COMMENT = "FORMAL_LINE_COMMENT"; //$NON-NLS-1$
  private static final String S_BLOCK_COMMENT = "BLOCK_COMMENT"; //$NON-NLS-1$
  private static final String S_LAZY_COMMENT = "LAZY_FORMAL_COMMENT"; //$NON-NLS-1$
  private static final String S_FORMAL_COMMENT = "FORMAL_COMMENT"; //$NON-NLS-1$
  private static final String S_CONSTRUCTS = "CONSTRUCTS"; //$NON-NLS-1$
  private static final String S_OPS_AND_SEPS = "OPS_AND_SEPS"; //$NON-NLS-1$
  private static final String S_NUMERIC_LITERAL = "NUMERIC_LITERAL"; //$NON-NLS-1$
  private static final String S_HEX_LITERAL = "HEX_LITERAL"; //$NON-NLS-1$

  private static final Color RED = new Color(255, 0, 0);
  private static final Color MAGENTA = new Color(255, 0, 255);
  private static final Color LIGHT_RED = new Color(255, 100, 100);
  private static final Color CYAN = new Color(0, 255, 255);
  private static final Color NAVY = new Color(0, 0, 128);
  private static final Color ORANGE = new Color(255, 128, 0);
  private static final Color BLUE = new Color(0, 0, 255);
  private static final Color FOREST = new Color(13, 165, 13);
  private static final Color LIGHT_BLUE = new Color(128, 128, 255);

  private static final ColorProfile PROFILE_CODE_BLOCKS;
  static {
    Map<String, ColorProfileEntry> colors = new HashMap<String, ColorProfile.ColorProfileEntry>();

    colors.put(S_FORMAL_COMMENT, makeEntry(S_FORMAL_COMMENT, LIGHT_BLUE, Font.BOLD));
    colors.put(S_LAZY_COMMENT, makeEntry(S_LAZY_COMMENT, LIGHT_BLUE, Font.BOLD));
    colors.put(S_BLOCK_COMMENT, makeEntry(S_BLOCK_COMMENT, FOREST, Font.ITALIC));
    colors.put(S_DOC_LINE_COMMENT, makeEntry(S_DOC_LINE_COMMENT, LIGHT_BLUE, Font.BOLD));
    colors.put(S_LAZY_LINE_COMMENT, makeEntry(S_LAZY_LINE_COMMENT, LIGHT_BLUE, Font.BOLD));
    colors.put(S_LINE_COMMENT, makeEntry(S_LINE_COMMENT, FOREST, Font.ITALIC));
    colors.put(S_DOUBLEQ_STRING, makeEntry(S_DOUBLEQ_STRING, BLUE, Font.PLAIN));
    colors.put(S_CHARACTER_LIT, makeEntry(S_CHARACTER_LIT, ORANGE, Font.PLAIN));
    colors.put(S_PREPROCESSOR, makeEntry(S_PREPROCESSOR, CYAN, Font.PLAIN));
    colors.put(S_CONSTRUCTS, makeEntry(S_CONSTRUCTS, NAVY, Font.BOLD));
    colors.put(S_OPS_AND_SEPS, makeEntry(S_OPS_AND_SEPS, RED, Font.PLAIN));
    colors.put(S_HEX_LITERAL, makeEntry(S_HEX_LITERAL, LIGHT_RED, Font.PLAIN));
    colors.put(S_NUMERIC_LITERAL, makeEntry(S_NUMERIC_LITERAL, MAGENTA, Font.PLAIN));

    PROFILE_CODE_BLOCKS = new ColorProfile("Code::Blocks", Collections.unmodifiableMap(colors)); //$NON-NLS-1$
  }

  private final ColorProfile profile = PROFILE_CODE_BLOCKS;

  /** Construct, populating language data. */
  public CPPTokenMarker() {
    super();

    schemes.add(new BlockDescriptor(S_FORMAL_COMMENT, "/\\*!", "\\*/", profile)); //$NON-NLS-1$ //$NON-NLS-2$
    schemes.add(new BlockDescriptor(S_LAZY_COMMENT, "/\\*(?=\\*)", "\\*/", profile)); //$NON-NLS-1$ //$NON-NLS-2$
    schemes.add(new BlockDescriptor(S_BLOCK_COMMENT, "/\\*", "\\*/", profile)); //$NON-NLS-1$ //$NON-NLS-2$
    schemes.add(new BlockDescriptor(S_DOC_LINE_COMMENT, "//!", "$", profile)); //$NON-NLS-1$ //$NON-NLS-2$
    schemes.add(new BlockDescriptor(S_LAZY_LINE_COMMENT, "///", "$", true, true, '\\', profile)); //$NON-NLS-1$ //$NON-NLS-2$
    schemes.add(new BlockDescriptor(S_LINE_COMMENT, "//", "($)", true, true, '\\', profile)); //$NON-NLS-1$ //$NON-NLS-2$
    schemes.add(new BlockDescriptor(S_DOUBLEQ_STRING, "\"", "($|\")", true, true, '\\', profile)); //$NON-NLS-1$ //$NON-NLS-2$
    schemes.add(new BlockDescriptor(S_CHARACTER_LIT, "'", "($|')", true, true, '\\', profile)); //$NON-NLS-1$ //$NON-NLS-2$
    schemes.add(new BlockDescriptor(S_PREPROCESSOR, "^(\\s*)#", "$", true, true, '\\', profile)); //$NON-NLS-1$ //$NON-NLS-2$

    KeywordSet kws = addKeywordSet(S_CONSTRUCTS, NAVY, Font.BOLD);
    String[] cppkws = { "if", "else", //$NON-NLS-1$ //$NON-NLS-2$
        "do", "while", "for", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        "new", "delete", "this", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        "and", "or", "not" };//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    for (int i = 0; i < cppkws.length; i++) {
      kws.words.add(cppkws[i]);
    }
    tmKeywords.add(kws);

    CharSymbolSet css = new CharSymbolSet(S_OPS_AND_SEPS, profile);
    char[] ca = "{[()]}!%^&*-/+=?:~<>.,;".toCharArray(); //$NON-NLS-1$
    for (int i = 0; i < ca.length; i++) {
      css.chars.add(ca[i]);
    }
    tmChars.add(css);

    otherTokens.add(new SimpleToken(S_HEX_LITERAL, "0[Xx][0-9A-Fa-f]+[FfUuLlDd]*", profile)); //$NON-NLS-1$
    otherTokens.add(new SimpleToken(S_NUMERIC_LITERAL, "[0-9]+[FfUuLlDd]*", profile)); //$NON-NLS-1$
  }
}
