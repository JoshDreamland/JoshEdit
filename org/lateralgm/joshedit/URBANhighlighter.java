package org.lateralgm.joshedit;

import java.awt.Color;
import java.awt.Font;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import assemblernator.Assembler;
import assemblernator.Instruction;

/**
 * @author Josh Ventura
 * @date Apr 8, 2012; 7:35:51 PM
 */
public class URBANhighlighter extends GenericHighlighter implements Highlighter {
	/**
	 * Construct and add syntax rules.
	 */
	public URBANhighlighter() {
		super();
		schemes.add(new BlockDescriptor("Comment", "(?<=;)", "[\r\n]", true, false,
				(char) 0, new Color(165, 165, 165), Font.ITALIC));
		schemes.add(new BlockDescriptor("String", "'", "'", true, true, '\\',
				new Color(0, 0, 255), 0));

		KeywordSet kws = new KeywordSet("Instructions", new Color(0, 0, 128),
				Font.BOLD, false);
		for (Entry<String, Instruction> i : Assembler.instructions.entrySet()) {
			kws.words.add(i.getKey().toLowerCase());
		}
		hlKeywords.add(kws);

		kws = new KeywordSet("OperandKeywords", new Color(0, 0, 255), 0, false);
		for (String i : Assembler.keyWords) {
			kws.words.add(i.toLowerCase());
		}
		hlKeywords.add(kws);

		CharSymbolSet css = new CharSymbolSet("Operators and Separators",
				new Color(255, 0, 0), 0);
		char[] ca = "{[()]}!%^&*-/+=?:~<>.,;".toCharArray();
		for (int i = 0; i < ca.length; i++)
			css.chars.add(ca[i]);
		hlChars.add(css);

		otherTokens.add(new SimpleToken("Numeric literal", "[0-9]+[FfUuLlDd]*",
				0, new Color(0, 225, 175)));

		default_kws = new KeywordSet("Label", new Color(128, 0, 128),
				Font.ITALIC);
		identifier_pattern = Pattern.compile("[a-z_A-Z]([^\\\\+\\\\-\\\\*/.,;:\\s']*)");
	}
}