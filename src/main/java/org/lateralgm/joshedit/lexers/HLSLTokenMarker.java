/**
 * @file HLSLTokenMarker.java
 * @brief Class implementing an HLSL lexer for syntax highlighting.
 *
 * @section License
 *
 *          Copyright (C) 2013-2014 Robert B. Colton
 *          Copyright (C) 2014 Josh Ventura <JoshV10@gmail.com>
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

import java.util.Arrays;
import java.util.Collection;

import org.lateralgm.joshedit.ColorProfile;
import org.lateralgm.joshedit.DefaultKeywords;
import org.lateralgm.joshedit.DefaultKeywords.HasKeywords;
import org.lateralgm.joshedit.DefaultKeywords.Keyword;
import org.lateralgm.joshedit.DefaultTokenMarker;

/**
 * Sample HLSL token marker class based on the default token marker.
 */
public class HLSLTokenMarker extends DefaultTokenMarker implements HasKeywords {

  private static class HLSLDescription implements LanguageDescription {
    @Override
    public String getName() {
      return "HLSL"; //$NON-NLS-1$
    }

    @Override
    public String getUnixName() {
      return "hlsl"; //$NON-NLS-1$
    }

    @Override
    public Collection<ColorProfile> defaultProfiles() {
      return Arrays.asList(new ColorProfile[] { ShaderHighlightingSchemes.ASS_BLASTERS_DX });
    }
  }

  /** Retrieve information about the languages supported by this TokenMarker. */
  public static LanguageDescription[] getLanguageDescriptions() {
    return new LanguageDescription[] { new HLSLDescription() };
  }

  private final ColorProfile profile = ShaderHighlightingSchemes.ASS_BLASTERS_DX;

  static KeywordSet resNames, scrNames, constructs, functions, operators, constants, variables;

  /** Construct, populating language data. */
  public HLSLTokenMarker() {
    super();

    for (BlockDescriptor blockDescriptor : ShaderHighlightingSchemes.cannedSchemes(profile)) {
      schemes.add(blockDescriptor);
    }

    functions = ShaderHighlightingSchemes.putFunctionSet(HLSLKeywords.FUNCTIONS, profile, this);
    constructs = ShaderHighlightingSchemes.putConstructSet(HLSLKeywords.CONSTRUCTS, profile, this);
    operators = ShaderHighlightingSchemes.putOperatorSet(HLSLKeywords.OPERATORS, profile, this);
    constants = ShaderHighlightingSchemes.putConstantSet(HLSLKeywords.CONSTANTS, profile, this);
    variables = ShaderHighlightingSchemes.putVariableSet(HLSLKeywords.VARIABLES, profile, this);

    tmKeywords.add(functions);
    tmKeywords.add(constructs);
    tmKeywords.add(operators);
    tmKeywords.add(constants);
    tmKeywords.add(variables);

    tmChars.add(ShaderHighlightingSchemes.cannedCharSymbols(profile));

    for (SimpleToken tok : ShaderHighlightingSchemes.cannedTokens(profile)) {
      otherTokens.add(tok);
    }
  }

  @Override
  public Keyword[][] getKeywords() {
    DefaultKeywords.Keyword[][] HLSL_KEYWORDS =
        { HLSLKeywords.CONSTRUCTS, HLSLKeywords.FUNCTIONS, HLSLKeywords.VARIABLES,
            HLSLKeywords.OPERATORS, HLSLKeywords.CONSTANTS };
    return HLSL_KEYWORDS;
  }
}
