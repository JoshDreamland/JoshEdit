/**
 * @file ShaderHighlightingSchemes.java
 * @brief Class implementing an HLSL keyword container.
 *
 * @section License
 *
 *          Copyright (C) 2013-2014 Robert B. Colton
 *          Copyright (C) 2014 Josh Ventura
 *          This file is a part of the LateralGM IDE.
 *
 *          This program is free software: you can redistribute it and/or modify
 *          it under the terms of the GNU General Public License as published by
 *          the Free Software Foundation, either version 3 of the License, or
 *          (at your option) any later version.
 *
 *          This program is distributed in the hope that it will be useful,
 *          but WITHOUT ANY WARRANTY; without even the implied warranty of
 *          MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *          GNU General Public License for more details.
 *
 *          You should have received a copy of the GNU General Public License
 *          along with this program. If not, see <http://www.gnu.org/licenses/>.
 **/

package org.lateralgm.joshedit.lexers;

import static org.lateralgm.joshedit.ColorProfile.makeEntry;

import java.awt.Color;
import java.awt.Font;
import java.util.Collections;

import org.lateralgm.joshedit.ColorProfile;
import org.lateralgm.joshedit.DefaultKeywords.Constant;
import org.lateralgm.joshedit.DefaultKeywords.Construct;
import org.lateralgm.joshedit.DefaultKeywords.Function;
import org.lateralgm.joshedit.DefaultKeywords.Operator;
import org.lateralgm.joshedit.DefaultKeywords.Variable;
import org.lateralgm.joshedit.DefaultTokenMarker;
import org.lateralgm.joshedit.DefaultTokenMarker.BlockDescriptor;
import org.lateralgm.joshedit.DefaultTokenMarker.CharSymbolSet;
import org.lateralgm.joshedit.DefaultTokenMarker.KeywordSet;
import org.lateralgm.joshedit.DefaultTokenMarker.SimpleToken;

/**
 * Class declaring syntax highlighting schemes for use with shaders.
 */

public class ShaderHighlightingSchemes {

  private static final String S_HEX_LITERAL = "HEX_LITERAL"; //$NON-NLS-1$
  private static final String S_NUMERIC_LITERAL = "NUMERIC_LITERAL"; //$NON-NLS-1$
  private static final String S_SINGLEQ_STRING = "SINGLEQ_STRING"; //$NON-NLS-1$
  private static final String S_DOUBLEQ_STRING = "DOUBLEQ_STRING"; //$NON-NLS-1$
  private static final String S_LINE_COMMENT = "LINE_COMMENT"; //$NON-NLS-1$
  private static final String S_DOC_LINE_COMMENT = "FORMAL_LINE_COMMENT"; //$NON-NLS-1$
  private static final String S_BLOCK_COMMENT = "BLOCK_COMMENT"; //$NON-NLS-1$
  private static final String S_DOC_COMMENT = "FORMAL_COMMENT"; //$NON-NLS-1$
  private static final String S_OPS_AND_SEPS = "OPS_AND_SEPS"; //$NON-NLS-1$

  /** Pre-defined group name for variables */
  public static final String S_VARIABLES = "VARIABLES"; //$NON-NLS-1$
  /** Pre-defined group name for constants */
  public static final String S_CONSTANTS = "CONSTANTS"; //$NON-NLS-1$
  /** Pre-defined group name for operators */
  public static final String S_OPERATORS = "OPERATORS"; //$NON-NLS-1$
  /** Pre-defined group name for constructs */
  public static final String S_CONSTRUCTS = "CONSTRUCTS"; //$NON-NLS-1$
  /** Pre-defined group name for functions */
  public static final String S_FUNCTIONS = "FUNCTIONS"; //$NON-NLS-1$

  private static final Color BLUE = new Color(0, 0, 255);
  private static final Color LIGHT_BLUE = new Color(128, 128, 255);
  private static final Color SLIGHTLY_LESS_LIGHT_BLUE = new Color(100, 100, 255);
  private static final Color BLUE_BLACK = new Color(20, 50, 90);
  private static final Color FOREST = new Color(13, 135, 13);
  private static final Color DARK_RED = new Color(200, 0, 0);
  private static final Color HALFASS_TURQUOIS = new Color(0, 100, 150);
  private static final Color BLACK = Color.BLACK;

  /** Robert's shader syntax highlighting sheme. */
  public static final ColorProfile ASS_BLASTERS_DX;

  static {
    //@formatter:off (It's amazing how stupid Eclipse's formatter is)
    ASS_BLASTERS_DX = ColorProfile.newBuilder("Ass Blasters DX") //$NON-NLS-1$
        .add(S_DOC_COMMENT, makeEntry(S_DOC_COMMENT, LIGHT_BLUE, Font.BOLD))
        .add(S_BLOCK_COMMENT, makeEntry(S_BLOCK_COMMENT, FOREST, Font.ITALIC))
        .add(S_DOC_LINE_COMMENT, makeEntry(S_DOC_LINE_COMMENT, LIGHT_BLUE, Font.BOLD))
        .add(S_LINE_COMMENT, makeEntry(S_LINE_COMMENT, FOREST, Font.ITALIC))
        .add(S_DOUBLEQ_STRING, makeEntry(S_DOUBLEQ_STRING, BLUE, Font.PLAIN))
        .add(S_SINGLEQ_STRING, makeEntry(S_SINGLEQ_STRING, BLUE, Font.PLAIN))
        .add(S_FUNCTIONS, makeEntry(S_FUNCTIONS, HALFASS_TURQUOIS, Font.PLAIN))
        .add(S_CONSTRUCTS, makeEntry(S_CONSTRUCTS, HALFASS_TURQUOIS, Font.BOLD))
        .add(S_OPERATORS, makeEntry(S_OPERATORS, BLACK, Font.BOLD))
        .add(S_CONSTANTS, makeEntry(S_CONSTANTS, DARK_RED, Font.PLAIN))
        .add(S_VARIABLES, makeEntry(S_VARIABLES, BLUE, Font.ITALIC))
        .add(S_OPS_AND_SEPS, makeEntry(S_OPS_AND_SEPS, DARK_RED, Font.PLAIN))
        .add(S_NUMERIC_LITERAL, makeEntry(S_NUMERIC_LITERAL, BLUE_BLACK, Font.PLAIN))
        .add(S_HEX_LITERAL, makeEntry(S_HEX_LITERAL, SLIGHTLY_LESS_LIGHT_BLUE, Font.PLAIN))
        .build();
    //@formatter:on

  }

  /** Return a set of block descriptor schemes for the given color profile. */
  public static BlockDescriptor[] cannedSchemes(ColorProfile profile) {
    return new BlockDescriptor[] {
        new BlockDescriptor(S_DOC_COMMENT, "/\\*(?=\\*)", "\\*/", profile), //$NON-NLS-1$ //$NON-NLS-2$
        new BlockDescriptor(S_BLOCK_COMMENT, "/(?=\\*)", "\\*/", profile), //$NON-NLS-1$ //$NON-NLS-2$
        new BlockDescriptor(S_DOC_LINE_COMMENT, "///", "$", profile), //$NON-NLS-1$ //$NON-NLS-2$
        new BlockDescriptor(S_LINE_COMMENT, "//", "$", profile), //$NON-NLS-1$ //$NON-NLS-2$
        new BlockDescriptor(S_DOUBLEQ_STRING, "\"", "\"", profile), //$NON-NLS-1$ //$NON-NLS-2$
        new BlockDescriptor(S_SINGLEQ_STRING, "'", "'", profile) //$NON-NLS-1$ //$NON-NLS-2$
    };
  }

  /** Return a set of all character symbols with highlighting according to the given color profile. */
  public static CharSymbolSet cannedCharSymbols(ColorProfile profile) {
    CharSymbolSet css = new CharSymbolSet(S_OPS_AND_SEPS, profile);
    char[] ca = "{[()]}!@%^&*-/+=?:~<>.,;".toCharArray(); //$NON-NLS-1$
    for (int i = 0; i < ca.length; i++) {
      css.chars.add(ca[i]);
    }
    return css;
  }

  /**
   * Put a given group of functions into a DefaultTokenMarker and return the keyword set added.
   *
   * @param functionSet
   *        Array of Functions to add.
   * @param profile
   *        Profile to use for coloring.
   * @param who
   *        The object that receives this set.
   */
  public static KeywordSet putFunctionSet(Function[] functionSet, ColorProfile profile,
      DefaultTokenMarker who) {
    KeywordSet functions = who.addKeywordSet(ShaderHighlightingSchemes.S_FUNCTIONS, profile);
    for (Function f : functionSet) {
      Collections.addAll(functions.words, f.getName());
    }
    return functions;
  }

  /**
   * Put a given group of constructs into a DefaultTokenMarker and return the keyword set added.
   *
   * @param constructSet
   *        Array of Functions to add.
   * @param profile
   *        Profile to use for coloring.
   * @param who
   *        The object that receives this set.
   */
  public static KeywordSet putConstructSet(Construct[] constructSet, ColorProfile profile,
      DefaultTokenMarker who) {
    KeywordSet constructs = who.addKeywordSet(ShaderHighlightingSchemes.S_CONSTRUCTS, profile);
    for (Construct c : constructSet) {
      Collections.addAll(constructs.words, c.getName());
    }
    return constructs;
  }

  /**
   * Put a given group of operators into a DefaultTokenMarker and return the keyword set added.
   *
   * @param operatorSet
   *        Array of Functions to add.
   * @param profile
   *        Profile to use for coloring.
   * @param who
   *        The object that receives this set.
   */
  public static KeywordSet putOperatorSet(Operator[] operatorSet, ColorProfile profile,
      DefaultTokenMarker who) {
    KeywordSet operators = who.addKeywordSet(ShaderHighlightingSchemes.S_OPERATORS, profile);
    for (Operator o : operatorSet) {
      Collections.addAll(operators.words, o.getName());
    }
    return operators;
  }

  /**
   * Put a given group of constants into a DefaultTokenMarker and return the keyword set added.
   *
   * @param constantSet
   *        Array of Functions to add.
   * @param profile
   *        Profile to use for coloring.
   * @param who
   *        The object that receives this set.
   */
  public static KeywordSet putConstantSet(Constant[] constantSet, ColorProfile profile,
      DefaultTokenMarker who) {
    KeywordSet constants = who.addKeywordSet(ShaderHighlightingSchemes.S_CONSTANTS, profile);
    for (Constant c : constantSet) {
      Collections.addAll(constants.words, c.getName());
    }
    return constants;
  }

  /**
   * Put a given group of variables into a DefaultTokenMarker and return the keyword set added.
   *
   * @param variableSet
   *        Array of Functions to add.
   * @param profile
   *        Profile to use for coloring.
   * @param who
   *        The object that receives this set.
   */
  public static KeywordSet putVariableSet(Variable[] variableSet, ColorProfile profile,
      DefaultTokenMarker who) {
    KeywordSet variables = who.addKeywordSet(ShaderHighlightingSchemes.S_VARIABLES, profile);
    for (Variable v : variableSet) {
      Collections.addAll(variables.words, v.getName());
    }
    return variables;
  }

  /** Get basic shader simple token markers for a given color profile. */
  public static SimpleToken[] cannedTokens(ColorProfile profile) {
    return new SimpleToken[] { new SimpleToken(S_NUMERIC_LITERAL, "[0-9]+", profile), //$NON-NLS-1$
        new SimpleToken(S_HEX_LITERAL, "\\$[0-9A-Fa-f]+", profile) //$NON-NLS-1$
    };
  }

  private ShaderHighlightingSchemes() {
  }
}
