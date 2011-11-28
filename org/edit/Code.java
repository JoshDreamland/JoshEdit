/* Copyright (C) 2011 IsmAvatar <IsmAvatar@gmail.com>
 * 
 * This file is part of JoshEdit. JoshEdit is free software.
 * You can use, modify, and distribute it under the terms of
 * the GNU General Public License, version 3 or later. 
 */

package org.edit;

import java.awt.AWTEvent;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.LinkedList;
import java.util.List;

public class Code extends ArrayList<Line>
{
	private static final long serialVersionUID = 1L;

	public void add(int index, StringBuilder sb)
	{
		super.add(index,new Line(sb));
		fireLinesChanged();
	}

	public void add(int index, String substring)
	{
		add(index,new StringBuilder(substring));
	}

	public boolean add(StringBuilder sb)
	{
		boolean r = super.add(new Line(sb));
		fireLinesChanged();
		return r;
	}

	public boolean add(String s)
	{
		return add(new StringBuilder(s));
	}

	public Line remove(int index)
	{
		Line r = super.remove(index);
		fireLinesChanged();
		return r;
	}
	
	public StringBuilder getsb(int index)
	{
		return super.get(index).sbuild;
	}

	/**
	 * A CodeListener listens for lines being added/removed.
	 * Use a JoshText.LineListener for individual characters.
	 */
	public static interface CodeListener extends EventListener
	{
		public void codeChanged(Code.CodeEvent e);
	}

	public List<Code.CodeListener> listenerList = new LinkedList<Code.CodeListener>();

	public void addCodeListener(Code.CodeListener l)
	{
		listenerList.add(l);
	}

	public void removeCodeListener(Code.CodeListener l)
	{
		listenerList.remove(l);
	}

	protected void fireLinesChanged()
	{
		for (Code.CodeListener l : listenerList)
			l.codeChanged(new CodeEvent(this,CodeEvent.LINES_CHANGED));
	}
	
	public static class CodeEvent extends AWTEvent
	{
		private static final long serialVersionUID = 1L;
		public static final int LINES_CHANGED = 0;

		public CodeEvent(Object source, int id)
		{
			super(source,id);
		}

	}
}
