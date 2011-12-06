/* Copyright (C) 2011 Josh Ventura <joshv@zoominternet.net>
 * 
 * This file is part of JoshEdit. JoshEdit is free software.
 * You can use, modify, and distribute it under the terms of
 * the GNU General Public License, version 3 or later. 
 */

package org.lateralgm.joshedit;

import java.awt.Color;

public interface Highlighter
{
	void set_owner(JoshText jt); // Affiliate a JoshEdit with us

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

	HighlighterInfo getStyle(int lineNum, int ind); // Return the color expected at a given line

	void formatCode(); // Format the code according to some specification in some grammar
}
