/* Copyright (C) 2011 Josh Ventura <joshv@zoominternet.net>
 * 
 * This file is part of JoshEdit. JoshEdit is free software.
 * You can use, modify, and distribute it under the terms of
 * the GNU General Public License, version 3 or later. 
 */

package org.edit;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.edit.GenericHighlighter.SchemeInfo.SchemeType;
import org.edit.JoshText.LineChangeListener;
import org.edit.Line.LINE_ATTRIBS;

public class GenericHighlighter implements Highlighter, LineChangeListener
{
	private JoshText jt; // The owning JoshText
	private int line_count; // The number of lines last time we parsed--used to determine change type
	private int invalid_line = 0; // The index of the first invalid line, or -1 for all-clear

	// The index of the next valid line after an invalid line is determined
	// while parsing--the first line that matches up with the tags from the
	// previous line is considered valid.

	class BlockDescriptor
	{
		String name; // Human-readable name for this scheme.
		Pattern begin, end; // Begin and end are not regex. If end is null, EOL is used.
		boolean multiline; // True if this block type is allowed to span multiple lines. Can be true only if end is non-null.
		boolean escapeend; // True if we can escape the ending character. If end is null, this allows us to span multiple lines.
		char escapeChar; // The character used to escape things, or null if no escape is allowed.
		Color color; // The color with which this will be rendered, or NULL to use the default
		int fontStyle; // Font attributes (Font.BOLD, etc)

		public BlockDescriptor(String block_name, String begin_regex, String end_regex,
				boolean allow_multiline, boolean escaping_newlines, char escape_char,
				Color highlight_color, int font_style)
		{
			name = block_name;
			begin = Pattern.compile(begin_regex);
			end = Pattern.compile(end_regex);
			multiline = allow_multiline;
			escapeend = escaping_newlines;
			escapeChar = escape_char;
			color = highlight_color;
			fontStyle = font_style;
		}
	}

	ArrayList<BlockDescriptor> schemes = new ArrayList<BlockDescriptor>();

	class KeywordSet
	{
		String name; // The name of this group of keywords
		Set<String> words; // A set of words highlighted according to this rule
		Color color; // The color with which this will be rendered, or NULL to use the default
		int fontStyle; // Font attributes (Font.BOLD, etc)

		public KeywordSet(String group_name, Color highlight_color, int font_style)
		{
			name = group_name;
			color = highlight_color;
			fontStyle = font_style;
			words = new HashSet<String>();
		}
	}

	ArrayList<KeywordSet> hlKeywords = new ArrayList<KeywordSet>();

	class CharSymbolSet
	{
		String name; // The name of this group of characters
		Set<Character> chars; // A set of characters highlighted according to this rule
		Color color; // The color with which this will be rendered, or NULL to use the default
		int fontStyle; // Font attributes (Font.BOLD, etc)

		public CharSymbolSet(String group_name, Color highlight_color, int font_style)
		{
			name = group_name;
			color = highlight_color;
			fontStyle = font_style;
			chars = new HashSet<Character>();
		}
	}

	ArrayList<CharSymbolSet> hlChars = new ArrayList<CharSymbolSet>();

	int numberFontStyle;
	Color numberColor;

	static final class SchemeInfo
	{
		enum SchemeType
		{
			NOTHING,NUMBER,BLOCK,KEYWORD,SYMBOL;
		}

		SchemeType type;
		int id;

		public SchemeInfo()
		{
			type = SchemeType.NOTHING;
			id = 0;
		}

		public SchemeInfo(SchemeType scheme_type, int scheme_id)
		{
			type = scheme_type;
			id = scheme_id;
		}
	}

	/**
	 * Returns the scheme active at a given position, or a token type otherwise.
	 * 
	 * Requires 0 <= pos <= line.length. Returns the id of any active scheme,
	 * If no scheme is active at that point, one of the values in @c TOKEN_TYPES
	 * is returned instead in the low-order byte. The next two lowest-order bytes
	 * of the return value carry any extra information (namely an ID).
	 * 
	 * If pos >= line.length, the function will return only block schemes open after
	 * the end of the line.
	 * 
	 * You must shift this id yourself before ORing it into a flag set.
	 * 
	 * @param ischeme  Initial scheme; the scheme active at the beginning of the line. 
	 * @param line     The line to parse for schemes.
	 * @param pos      The position at which to stop parsing.
	 */
	private SchemeInfo get_scheme_at(int ischeme, StringBuilder line, int pos)
	{
		int i = 0; // The position from which we will parse this thing
		if (ischeme != 0)
		{
			Matcher m = schemes.get(ischeme).end.matcher(line.toString());
			if (!m.find() || m.end() > pos) return new SchemeInfo(SchemeType.BLOCK,ischeme);
			if (m.end() > line.length()) return new SchemeInfo();
			i = m.end();
		}
		for (;;)
		{
			int shm = 0; // Scheme Holding Minimum Match
			int mmin = pos + 1; // Minimum match position
			for (int si = 0; si < schemes.size(); si++)
			{
				Matcher m = schemes.get(si).begin.matcher(line.toString()).region(i,line.length()).useTransparentBounds(
						true);
				if (!m.find()) continue;
				if (m.start() < mmin)
				{ // If this one is closer to the beginning, it can potentially consume later ones. 
					mmin = m.start(); // So we have to pay attention to it first.
					shm = si;
				}
			}
			if (mmin <= pos)
			{ // If we actually found one
				Matcher mmatcher = schemes.get(shm).end.matcher(line.toString()).region(mmin + 1,
						line.length());
				if (!mmatcher.find() || mmatcher.end() > pos) // If there's no end in sight, or that end passed our position of interest
					return new SchemeInfo(SchemeType.BLOCK,shm); // Then our position is inside the block, so we return the block's scheme info.
				i = mmatcher.end();
			}
			else
				// Otherwise, checking again won't help anything. Leave.
				break;
		}

		// Okay, so we're not in any blocks. We might be at an important symbol,
		// or in a keyword or numeral or something.
		if (pos >= line.length()) // But if we're looking for our status after the line is over,
			return new SchemeInfo(); // Then we'd better just return nothing.

		while (i <= pos)
		{
			if (Character.isWhitespace(line.charAt(i)))
			{
				while (++i < line.length() && Character.isWhitespace(line.charAt(i)))
					;
				continue;
			}
			if (Character.isLetter(line.charAt(i))) // TODO: This should instead match some modifiable regex.
			{
				final int si = i;
				while (++i < line.length() && Character.isLetterOrDigit(line.charAt(i)))
					;
				if (i > pos)
				{
					String f = line.substring(si,i);
					for (int sn = 0; sn < hlKeywords.size(); sn++)
						if (hlKeywords.get(sn).words.contains(f)) return new SchemeInfo(SchemeType.KEYWORD,sn);
					return new SchemeInfo();
				}
				continue;
			}
			if (Character.isDigit(line.charAt(i))) // TODO: This should instead match some modifiable regex.
			{
				while (++i < line.length() && Character.isDigit(line.charAt(i)))
					;
				if (i > pos) return new SchemeInfo(SchemeType.NUMBER,0);
				continue;
			}
			if (i == pos)
			{
				char c = line.charAt(i);
				for (int sn = 0; sn < hlChars.size(); sn++)
					if (hlChars.get(sn).chars.contains(c)) return new SchemeInfo(SchemeType.SYMBOL,sn);
			}
			i++;
		}
		return new SchemeInfo();
	}

	private void highlight()
	{
		System.out.println("Invoked from " + invalid_line);
		while (jt.code.get(invalid_line).attr < 0)
		{
			if (invalid_line == 0)
				jt.code.get(invalid_line).attr = 0;
			else
				invalid_line--;
		}
		System.out.println("Highlight from " + invalid_line + " to " + line_count);
		System.out.println("while (" + invalid_line + " < " + (line_count-1));
		while (invalid_line < line_count - 1)
		{
			SchemeInfo a = get_scheme_at(
					(int) ((jt.code.get(invalid_line).attr & LINE_ATTRIBS.LA_SCHEMEBLOCK) >> LINE_ATTRIBS.LA_SCHEMEBITOFFSET),
					jt.code.get(invalid_line).sbuild,jt.code.get(invalid_line).sbuild.length());
			invalid_line++;
			if (jt.code.get(invalid_line).attr < 1)
				jt.code.get(invalid_line).attr = 0;
			else
				jt.code.get(invalid_line).attr &= ~LINE_ATTRIBS.LA_SCHEMEBLOCK; // Remove all scheme info
			System.out.println("Set attribs for line " + invalid_line + " to " + jt.code.get(invalid_line).attr);
			if (a.type == SchemeType.BLOCK) // If we're in a block scheme, note so.
				jt.code.get(invalid_line).attr |= a.id << LINE_ATTRIBS.LA_SCHEMEBITOFFSET;
		}
		invalid_line = -1;
	}

	public GenericHighlighter(JoshText joshText)
	{
		this();
		set_owner(joshText);
	}

	public GenericHighlighter()
	{
		// TODO Auto-generated constructor stub
	}

	@Override
	public void set_owner(JoshText jt)
	{
		this.jt = jt;
	}

	@Override
	public HighlighterInfo getStyle(int lineNum, int i)
	{
		Line line = jt.code.get(lineNum);
		if (line.attr < 0) {
			System.err.println("ERROR! That FUCKING highlight function didn't complete; line " + lineNum + " is invalid");
			jt.code.get(lineNum).attr = 0;
		}
		SchemeInfo si = get_scheme_at(
				(int) ((line.attr & LINE_ATTRIBS.LA_SCHEMEBLOCK) >> LINE_ATTRIBS.LA_SCHEMEBITOFFSET),
				line.sbuild,i);
		switch (si.type)
		{
			case BLOCK:
				return new HighlighterInfo(schemes.get(si.id).fontStyle,schemes.get(si.id).color);
			case KEYWORD:
				return new HighlighterInfo(hlKeywords.get(si.id).fontStyle,hlKeywords.get(si.id).color);
			case NOTHING:
				break;
			case NUMBER:
				return new HighlighterInfo(numberFontStyle,numberColor);
			case SYMBOL:
				return new HighlighterInfo(hlChars.get(si.id).fontStyle,hlChars.get(si.id).color);
		}
		return new HighlighterInfo(0,null);
	}

	@Override
	public void formatCode()
	{
		return; // We can't format the code; we're only pretending to know anything about it.
	}

	@Override
	public void linesChanged(int start, int end)
	{
		line_count = jt.code.size();
		if (start < invalid_line || invalid_line == -1) invalid_line = start;
		for (int i = start; i < end; i++)
			if (jt.code.get(i).attr > 0) jt.code.get(i).attr = -jt.code.get(i).attr;
			else if (jt.code.get(i).attr > 0) jt.code.get(i).attr = -1;
		highlight();
	}
}
