/* Copyright (C) 2011 Josh Ventura <joshv@zoominternet.net>
 * Copyright (C) 2011 IsmAvatar <IsmAvatar@gmail.com>
 * 
 * This file is part of JoshEdit. JoshEdit is free software.
 * You can use, modify, and distribute it under the terms of
 * the GNU General Public License, version 3 or later. 
 */

package org.edit;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

public class QuickFind extends JToolBar
{
	private static final long serialVersionUID = 1L;

	public JButton close, prev, next, settings;
	JToggleButton highlight;
	public JTextField tFind;
	public JoshText joshText;

	public QuickFind(JoshText text)
	{
		super();
		setFloatable(false);
		add(close = new JButton(Runner.findIcon("x.gif")));
		close.setMaximumSize(new Dimension(12,12));
		close.setPreferredSize(new Dimension(12,12));
		JLabel l;
		add(l = new JLabel("Find: "));
		add(tFind = new JTextField());
		add(prev = new JButton(Runner.findIcon("la.gif")));
		add(next = new JButton(Runner.findIcon("ra.gif")));
		add(highlight = new JToggleButton("Highlight All",Runner.findIcon("hl.gif")));
		add(settings = new JButton(Runner.findIcon("set.gif")));
		highlight.setFont(new Font("Sans",0,12));
		l.setFont(new Font("Sans",0,12));

		setMaximumSize(new Dimension(Integer.MAX_VALUE,20));
		setPreferredSize(new Dimension(320,24));
		setBorder(null);

		setVisible(false);
		joshText = text;

		settings.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				FindDialog.tFind.setSelectedItem(tFind.getText());
				FindDialog.getInstance().selectedJoshText = joshText;
				FindDialog.getInstance().setVisible(true);
			}
		});
		close.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				setVisible(false);
			}
		});
		prev.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				findPrevious();
			}
		});
		next.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				findNext();
			}
		});
		tFind.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if ((e.getModifiers() & InputEvent.SHIFT_DOWN_MASK) != 0)
				  findPrevious();
				else
					findNext();
			}
		});
	}
	
	private void selectFind(int x, int y, int l)
	{
		selectFind(x,y,x + l,y);
		joshText.repaint();
	}
	private void selectFind(int x, int y, int x2, int y2)
	{
		joshText.caret.row = y;
		joshText.caret.col = x;
		joshText.sel.row = y2;
		joshText.sel.col = x2;
		joshText.repaint();
	}

	void findNext()
	{
		// TODO: I have no idea how multiline regexp search will be handled.
		String ftext = tFind.getText();
		if (ftext.length() == 0)
			return;
		if (FindDialog.regex.isSelected())
		{
			Pattern p;
			try {
			  p = Pattern.compile(ftext,Pattern.CASE_INSENSITIVE);
			} catch (PatternSyntaxException pse) {
				System.err.println("Shit man, your expression sucks");
				return;
			}
			for (int y = joshText.caret.row; y < joshText.code.size(); y++)
			{
				Matcher m = p.matcher(joshText.code.getsb(y).toString());
				int si = y==joshText.caret.row?joshText.caret.col+(joshText.sel.isEmpty()?0:1):0;
				System.out.println(si);
				if (m.find(si)) {
					selectFind(m.start(),y,m.end()-m.start());
					return;
				}
			}
			return;
		}
		String[] findme = ftext.split("\r?\n");
		System.out.println("Find next instance of `" + findme[0] + "'");
		
		findMain: 
		for (int y = joshText.caret.row; y < joshText.code.size(); y++)
		{
			int io = joshText.code.getsb(y).indexOf(findme[0],y==joshText.caret.row?joshText.caret.col+(joshText.sel.isEmpty()?0:1):0);
			if (io == -1) continue;
			if (findme.length == 1) {
				selectFind(io,y,findme[0].length());
				return;
			}
			int intermediate;
			for (intermediate = 1; intermediate < findme.length-1; intermediate++) {
				if (!findme[intermediate].equals(joshText.code.getsb(y+intermediate)))
				  continue findMain;
			}
			if (joshText.code.getsb(y+intermediate).length() >= findme[intermediate].length()
					&& joshText.code.getsb(y+intermediate).substring(0,findme[intermediate].length()).equals(findme[intermediate])) {
				selectFind(io,y,findme[intermediate].length(),y+intermediate);
				return;
			}
		}
	}

	public void findPrevious()
	{
		/* FIXME: I'm not sure what to do with this, yet. This method only works right
		          for one instance per line, and regexps can't be traversed backward. */
		String ftext = tFind.getText();
		if (ftext.length() == 0)
			return;
		if (FindDialog.regex.isSelected())
		{
			return;/*
			Pattern p;
			try {
			  p = Pattern.compile(ftext,Pattern.CASE_INSENSITIVE);
			} catch (PatternSyntaxException pse) {
				System.err.println("Shit man, your expression sucks");
				return;
			}
			for (int y = joshText.caret.row; y < joshText.code.size(); y++)
			{
				Matcher m = p.matcher(joshText.code.getsb(y).toString());
				int si = y==joshText.caret.row?joshText.caret.col+(joshText.sel.isEmpty()?0:1):0;
				System.out.println(si);
				if (m.find(si)) {
					selectFind(m.start(),y,m.end()-m.start());
					return;
				}
			}
			return;/**/
		}
		String[] findme = ftext.split("\r?\n");
		System.out.println("Find previous instance of `" + findme[0] + "'");
		for (int y = joshText.caret.row; y >= 0; y--)
		{
			if (findme.length == 1)
			{
				int io = joshText.code.getsb(y).indexOf(findme[0],y==joshText.caret.row?joshText.caret.col+(joshText.sel.isEmpty()?0:1):0);
				if (io != -1) {
					selectFind(io,y,findme[0].length());
					return;
				}
			}
		}
	}
}