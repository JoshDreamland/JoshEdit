/* Copyright (C) 2011 Josh Ventura <joshv@zoominternet.net>
 * Copyright (C) 2011 IsmAvatar <IsmAvatar@gmail.com>
 * 
 * This file is part of JoshEdit. JoshEdit is free software.
 * You can use, modify, and distribute it under the terms of
 * the GNU General Public License, version 3 or later. 
 */

package org.edit;

import java.awt.BorderLayout;
import java.net.URL;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.edit.Code.CodeEvent;
import org.edit.Code.CodeListener;

public class Runner
{
	public static void createAndShowGUI()
	{
		showCodeWindow(true);
		//showBindingsWindow(false);
	}

	public static void showCodeWindow(boolean closeExit)
	{
		final JoshText text = new JoshText();
		final LineNumberPanel lines = new LineNumberPanel(text.getFont(),text.code.size());

		text.code.addCodeListener(new CodeListener()
		{
			@Override
			public void codeChanged(CodeEvent e)
			{
				lines.setLines(text.code.size());
			}
		});

		JScrollPane scroller = new JScrollPane(text);
		scroller.setRowHeaderView(lines);
		InputMap im = scroller.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		im.put(KeyStroke.getKeyStroke("UP"),"none");
		im.put(KeyStroke.getKeyStroke("DOWN"),"none");
		im.put(KeyStroke.getKeyStroke("LEFT"),"none");
		im.put(KeyStroke.getKeyStroke("RIGHT"),"none");

		JFrame f = new JFrame("Title");
		f.setLayout(new BorderLayout());
		f.add(scroller,BorderLayout.CENTER);
		f.add(text.quickFind = new QuickFind(text), BorderLayout.SOUTH);

		//f.setSize(480,320);
		f.pack();
		f.setLocationRelativeTo(null);
		if (closeExit) f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
	}

	public static void showBindingsWindow(boolean closeExit)
	{
		JFrame f = new JFrame();
		f.getContentPane().setLayout(new BoxLayout(f.getContentPane(),BoxLayout.PAGE_AXIS));

		JTabbedPane tabs = new JTabbedPane();
		tabs.addTab("Bindings",new Bindings());
		f.add(tabs);

		JPanel repanel = new JPanel();
		repanel.setLayout(new BoxLayout(repanel,BoxLayout.LINE_AXIS));
		repanel.add(new JButton("OK"));
		repanel.add(new JButton("Cancel"));
		f.add(repanel);

		f.pack();
		f.setLocationRelativeTo(null);
		if (closeExit) f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
	}

	public static void showFindWindow(boolean closeExit)
	{
		JFrame f = new JFrame();
		f.getContentPane().setLayout(new BoxLayout(f.getContentPane(),BoxLayout.PAGE_AXIS));

	}

	public static ImageIcon findIcon(String filename)
	{
		String location = "org/edit/icons/" + filename; //$NON-NLS-1$
		ImageIcon ico = new ImageIcon(location);
		if (ico.getIconWidth() == -1)
		{
			URL url = Runner.class.getClassLoader().getResource(location);
			if (url != null) ico = new ImageIcon(url);
		}
		return ico;
	}

	public static void main(String[] args)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				createAndShowGUI();
			}
		});
	}
}
