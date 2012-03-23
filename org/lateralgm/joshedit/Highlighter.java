/* Copyright (C) 2011 Josh Ventura <joshv@zoominternet.net>
 * 
 * This file is part of JoshEdit. JoshEdit is free software.
 * You can use, modify, and distribute it under the terms of
 * the GNU General Public License, version 3 or later. 
 */

package org.lateralgm.joshedit;

import java.awt.Color;
import java.util.ArrayList;

import org.lateralgm.joshedit.JoshText.LineChangeListener;

public interface Highlighter extends LineChangeListener
{
	class HighlighterInfo
	{
		int fontStyle;
		Color color;

		public HighlighterInfo(int fs, Color col)
		{
			fontStyle = fs;
			color = col;
		}
	}

	class HighlighterInfoEx extends HighlighterInfo
	{
		public int endPos;
		public int startPos;
		public int blockHash; /// A unique hash for open block types.

		public HighlighterInfoEx(int fs, Color col, int start, int end, int hash)
		{
			super(fs,col);
			startPos = start;
			endPos = end;
			blockHash = hash;
		}
	}

	HighlighterInfo getStyle(Line line, int ind); // Return the color expected at a given line

	ArrayList<HighlighterInfoEx> getStyles(Line jline); // Return an array of line highlight colors with their positions

	void formatCode(); // Format the code according to some specification in some grammar
}
