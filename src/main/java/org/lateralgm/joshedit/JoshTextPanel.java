/*
 * Copyright (C) 2011 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2011 Josh Ventura <JoshV10@gmail.com>
 * Copyright (C) 2014 Robert B. Colton
 *
 * This file is part of JoshEdit. JoshEdit is free software.
 * You can use, modify, and distribute it under the terms of
 * the GNU General Public License, version 3 or later.
 */

package org.lateralgm.joshedit;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import javax.print.PrintService;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.CaretListener;

import org.lateralgm.joshedit.Code.CodeEvent;
import org.lateralgm.joshedit.Code.CodeListener;

public class JoshTextPanel extends JPanel implements Printable {
  private static final long serialVersionUID = 1L;

  public JScrollPane scroller;
  public JoshText text;
  public LineNumberPanel lines;
  public QuickFind find;

  public interface ScrollPaneProvider {
    JScrollPane get(JoshText text);
  }

  public static ScrollPaneProvider scrollPaneProvider = new ScrollPaneProvider() {
    @Override
    public JScrollPane get(JoshText text) {
      return new JScrollPane(text);
    }
  };

  public JoshTextPanel() {
    this((String[]) null, null);
  }

  public JoshTextPanel(String code) {
    this(Runner.splitLines(code), null);
  }
  
  public JoshTextPanel(String code, Font font) {
    this(Runner.splitLines(code), font);
  }

  public JoshTextPanel(String[] codeLines) {
    this(codeLines, null);
  }

  public JoshTextPanel(String[] codeLines, Font font) {
    this(codeLines, font, true);
  }

  public JoshTextPanel(String[] codeLines, Font font, boolean startZero) {
    super(new BorderLayout());

    text = new JoshText(codeLines, font);
    lines = new LineNumberPanel(text, text.code.size(), startZero);
    text.code.addCodeListener(new CodeListener() {
      @Override
      public void codeChanged(CodeEvent e) {
        lines.setLines(text.code.size());
      }
    });

    find = new QuickFind(text);
    text.finder = find;

    text.mapAction(actPrint);

    scroller = scrollPaneProvider.get(text);

    scroller.setRowHeaderView(lines);
    add(scroller, BorderLayout.CENTER);
    add(find, BorderLayout.SOUTH);
  }

  public int getCaretLine() {
    return text.caret.row;
  }

  public int getCaretColumn() {
    return text.caret.col;
  }

  public void setCaretPosition(int row, int col) {
    text.caret.row = row;
    text.caret.col = col;
    text.caret.colw = text.line_wid_at(row, col);
    text.sel.deselect(false);
    text.caret.positionChanged();
  }

  public void addCaretListener(CaretListener cl) {
    text.caret.addCaretListener(cl);
  }

  /** Convenience method that replaces newlines with \r\n for GM compatibility */
  public String getTextCompat() {
    StringBuilder res = new StringBuilder();
    for (int i = 0; i < text.code.size(); i++) {
      if (i != 0) {
        res.append("\r\n");
      }
      res.append(text.code.getsb(i));
    }
    return res.toString();
  }

  public boolean isChanged() {
    return text.isChanged();
  }

  public void setText(String s) {
    text.setText(s == null? null : s.split("\r?\n"));
  }

  public String getLineText(int line) {
    return text.code.getsb(line).toString();
  }

  public int getLineCount() {
    return text.code.size();
  }

  // TODO: Does not appear to actually be any warning here to suppress.
  @SuppressWarnings("static-method")
  public void setTabSize(int spaces) {
    JoshText.Settings.indentSizeInSpaces = spaces;
  }

  // TODO: This method is icky because CodeTextArea takes DefaultTokenMarker
  // and it is very easy to mix these two up when you are not aware that CodeTextArea
  // derives from this class JoshTextPanel.
  public void setTokenMarker(TokenMarker tm) {
    text.setTokenMarker(tm);
  }

  public void setSelection(int row, int col, int row2, int col2) {
    text.sel.row = row;
    text.sel.col = col;
    text.caret.row = row2;
    text.caret.col = col2;
    text.caret.colw = text.line_wid_at(row, col);
    text.caret.positionChanged();
    text.sel.selectionChanged();
  }

  /**
   * Convenience method for displaying a print dialog returns true if successful or false if the
   * user cancels or an exception otherwise occurs.
   */
  public boolean Print() throws PrinterException {
    //Step 1: Set up initial print settings.
    PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
    //Step 2: Obtain a print job.
    PrinterJob pj = PrinterJob.getPrinterJob();
    pj.setPrintable(this);
    //Step 3: Find print services.
    PrintService[] services = PrinterJob.lookupPrintServices();
    if (services.length > 0) {
      pj.setPrintService(services[0]);
      // Step 4: Update the settings made by the user in the dialogs.
      if (pj.printDialog(aset)) {
        // Step 5: Pass the final settings into the print request.
        pj.print(aset);
        return true;
      }
    }
    return false;
  }

  /** Print the given page of code with line numbers based on how many lines of code will fit in the printable area */
  @Override
  public int print(Graphics g, PageFormat pf, int pageIndex) throws PrinterException {
    return text.print(lines,g,pf,pageIndex);
  }

  /** Print action. */
  public AbstractAction actPrint = new AbstractAction("PRINT") { //$NON-NLS-1$
    private static final long serialVersionUID = 1L;

    @Override
    public void actionPerformed(ActionEvent e) {
      try {
        Print();
      } catch (PrinterException pe) {
        // TODO Auto-generated catch block
        pe.printStackTrace();
      }
    }
  };
}
