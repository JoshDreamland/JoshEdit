/*
 * Copyright (C) 2010 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2008 Quadduc <quadduc@gmail.com>
 * Copyright (C) 2013, 2014, Robert B. Colton
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.joshedit;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.UIManager;

/**
 * Class to handle code completion.
 *
 * @author IsmAvatar
 */
// TODO: doCodeSize(true); <- call that when the auto completion is confirmed
// otherwise it doesnt update the client area, i propose making a system
// wherein all insertions/deletions of text will invalidate a client area resizing
public class CompletionMenu {
  /** The text area in which to handle code completion. */
  protected JoshText area;
  /** The scroll pane to house completion results. */
  private final JScrollPane scroll;
  /** Array of available completions. */
  private final Completion[] completions;
  /** Completion options from which the user can select. */
  private Completion[] options;
  private String word;
  private final JList<Completion> completionList;
  private final KeyHandler keyHandler;
  // protected int wordOffset;
  // protected int wordPos;
  // protected int wordLength;

  protected int row, wordStart, wordEnd, caret;

  protected PopupHandler ph;
  protected Point loc;

  // FIXME: For some reason this completion menu can not accept focus allowing VK_TAB to not be
  // dispatched.

  /**
   * @param owner
   *        The owning Frame, for focus handling.
   * @param a
   *        The JoshText we'll be completing code in.
   * @param y
   *        The row we're being created on.
   * @param x1
   *        The index of the first character of the current word to complete.
   * @param x2
   *        The index of the last character of the current word to complete.
   * @param caret
   *        The current caret position on this row.
   * @param c
   *        The set of completions to choose between.
   */
  public CompletionMenu(Frame owner, JoshText a, int y, int x1, int x2, int caret, Completion[] c) {
    area = a;
    row = y;
    wordStart = x1;
    wordEnd = x2;
    this.caret = caret;
    completions = c;

    keyHandler = new KeyHandler();
    completionList = new JList<Completion>();
    completionList.setFixedCellHeight(12);
    completionList.setFont(new Font("Monospace", Font.PLAIN, 10)); //$NON-NLS-1$
    completionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    completionList.addKeyListener(keyHandler);
    completionList.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (apply()) {
          e.consume();
        } else {
          dispose();
        }
      }
    });
    scroll = new JScrollPane(completionList);
    scroll.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    ph = new PopupHandler(owner, scroll);
    ph.addHideListener(new PopupHandler.HideListener() {
      @Override
      public void hidePerformed(boolean wasVisible) {
        dispose();
      }
    });

    reset();
  }

  /** Show our completion pop-up. */
  public void show() {
    ph.show(loc.x, loc.y);
  }

  /** Dispose our completion pop-up. */
  public void dispose() {
    ph.dispose();
    area.requestFocusInWindow();
  }

  /** Update the location of our completion pop-up on-screen. */
  public void setLocation() {
    Point p = area.getLocationOnScreen();
    int y = (row + 1) * area.metrics.lineHeight();
    int x = area.metrics.lineWidth(row, wordEnd);
    // adding this breaks it, but without it shows at the correct position
    // ffs wtf?
    /*
     * if (area.getParent() instanceof JViewport)
     * {
     * Point vp = ((JViewport) area.getParent()).getViewPosition();
     * x -= vp.x;
     * y -= vp.y;
     * }
     */
    p.x += Math.min(area.getWidth(), Math.max(0, x));
    p.y += Math.min(area.getHeight(), Math.max(0, y)) + 3;
    loc = p;
    // pm.setLocation(p);
    // setLocation(p);
  }

  public void reset() {
    // if (area.getSelectionStart() != wordOffset + wordPos)
    // area.setCaretPosition(wordOffset + wordPos);
    // String w = area.getText(wordOffset,wordPos);
    String w = area.code.getsb(row).toString().substring(wordStart, wordEnd);
    if (w.isEmpty()) {
      options = completions;
    } else if ((options != null) && (word != null) && (w.startsWith(word))) {
      ArrayList<Completion> l = new ArrayList<Completion>();
      for (Completion c : options) {
        if (c.match(w)) {
          l.add(c);
        }
      }
      options = l.toArray(new Completion[l.size()]);
    } else {
      ArrayList<Completion> l = new ArrayList<Completion>();
      for (Completion c : completions) {
        if (c.match(w)) {
          l.add(c);
        }
      }
      options = l.toArray(new Completion[l.size()]);
    }
    if (options.length <= 0) {
      dispose();
      return;
    }
    word = w;
    completionList.setListData(options);
    completionList.setVisibleRowCount(Math.min(options.length, 8));
    // pack();
    setLocation();
    select(0);

    show();
    // requestFocus();
    completionList.requestFocusInWindow();
  }

  /** Set the current completion selection in the completions list. */
  public void select(int n) {
    completionList.setSelectedIndex(n);
    completionList.ensureIndexIsVisible(n);
  }

  /** Set the current completion selection in the completions list, relative to the current index. */
  public void selectRelative(int n) {
    int s = completionList.getModel().getSize();
    if (s <= 1) {
      return;
    }
    int i = completionList.getSelectedIndex();
    select((s + ((i + n) % s)) % s);
  }

  /** Apply the currently selected code completion option to the code. */
  public boolean apply() {
    return apply('\0');
  }

  public boolean apply(char input) {
    Object o = completionList.getSelectedValue();
    if (o instanceof Completion) {
      Completion c = (Completion) o;
      dispose();
      if (input == '\n') {
        input = '\0';
      }
      return c.apply(area, input, row, wordStart, wordEnd);
    }
    return false;
  }

  /** Replace the current text selection with a given string. */
  public void setSelectedText(String s) {
    area.sel.insert(s);
  }

  /**
   * The PopupHandler class maintains a popup container that can popup on demand,
   * and hide at the expected time (e.g. loss of focus). Please be sure to dispose of
   * it when you are done, as this will free up the global listeners it registers.
   * A HideListener can be registered to listen for whenever the popup is hidden
   * (e.g. loss of focus causes it to hide itself - as well as user-invoked hides).
   * <p>
   * A PopupHandler is intended for custom popup components where a JPopupMenu is insufficient.
   */
  public static class PopupHandler implements AWTEventListener, WindowListener, ComponentListener {
    protected Popup pop;
    protected Window invoker;
    protected Component contents;
    protected int lastX, lastY;

    /** Construct with an invoker and content. */
    public PopupHandler(Window invoker, Component contents) {
      this.invoker = invoker;
      this.contents = contents;
      install();
    }

    /** Set current content. */
    public void setContents(Component contents) {
      this.contents = contents;
      show(lastX, lastY);
    }

    protected void install() {
      long mask = AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_WHEEL_EVENT_MASK;
      Toolkit.getDefaultToolkit().addAWTEventListener(this, mask);
      invoker.addWindowListener(this);
      invoker.addComponentListener(this);
    }

    /** Called at termination. */
    public void dispose() {
      if (pop != null) {
        pop.hide();
      }
      pop = null;
      uninstall();
    }

    protected void uninstall() {
      Toolkit.getDefaultToolkit().removeAWTEventListener(this);
      invoker.removeWindowListener(this);
      invoker.removeComponentListener(this);
    }

    /** Display the completion menu at the given coordinates. */
    public void show(int x, int y) {
      lastX = x;
      lastY = y;
      if (pop != null) {
        pop.hide();
      }
      pop = PopupFactory.getSharedInstance().getPopup(invoker, contents, x, y);
      pop.show();
    }

    /** Hide this pop-up. */
    public void hide() {
      if (pop != null) {
        pop.hide();
      }
      fireHide(pop != null);
      pop = null;
    }

    /** Listener interface to observe when this pop-up is hidden. */
    public static interface HideListener extends EventListener {
      /** Invoked when a {@link PopupHandler#hide()} is called on our pop-up. */
      void hidePerformed(boolean wasVisible);
    }

    protected List<HideListener> hll = new ArrayList<HideListener>();

    /** Add a {@link HideListener} to our pop-up. */
    public void addHideListener(HideListener e) {
      hll.add(e);
    }

    /** Remove a {@link HideListener} from our pop-up. */
    public void removeHideListener(HideListener e) {
      hll.remove(e);
    }

    protected void fireHide(boolean wasVisible) {
      for (HideListener hl : hll) {
        hl.hidePerformed(wasVisible);
      }
    }

    protected boolean isInPopup(Component src) {
      for (Component c = src; c != null; c = c.getParent()) {
        if (c == contents) {
          return true;
        } else if (c instanceof java.applet.Applet || c instanceof Window) {
          return false;
        }
      }
      return false;
    }

    // events
    @Override
    public void eventDispatched(AWTEvent ev) {
      // We are interested in MouseEvents only
      if (!(ev instanceof MouseEvent)) {
        return;
      }
      MouseEvent me = (MouseEvent) ev;
      Component src = me.getComponent();
      switch (me.getID()) {
        case MouseEvent.MOUSE_PRESSED:
          if (isInPopup(src)) {
            return;
          }
          hide();
          // Ask UIManager about should we consume event that closes
          // popup. This made to match native apps behaviour.
          // Consume the event so that normal processing stops.
          if (UIManager.getBoolean("PopupMenu.consumeEventOnClose")) { //$NON-NLS-1$
            me.consume();
          }
          break;
        case MouseEvent.MOUSE_WHEEL:
          if (isInPopup(src)) {
            return;
          }
          hide();
          break;
      }
    }

    /** Just hide the window. */
    @Override
    public void componentResized(ComponentEvent e) {
      hide();
    }

    /** Just hide the window. */
    @Override
    public void componentMoved(ComponentEvent e) {
      hide();
    }

    /** Just hide the window. */
    @Override
    public void componentShown(ComponentEvent e) {
      hide();
    }

    /** Just hide the window. */
    @Override
    public void componentHidden(ComponentEvent e) {
      hide();
    }

    /** Just hide the window. */
    @Override
    public void windowClosing(WindowEvent e) {
      hide();
    }

    /** Just hide the window. */
    @Override
    public void windowClosed(WindowEvent e) {
      hide();
    }

    /** Just hide the window. */
    @Override
    public void windowIconified(WindowEvent e) {
      hide();
    }

    /** Just hide the window. */
    @Override
    public void windowDeactivated(WindowEvent e) {
      hide();
    }

    /** Unused */
    // Unused
    @Override
    public void windowOpened(WindowEvent e) { // Unused
    }

    /** Unused */
    @Override
    public void windowDeiconified(WindowEvent e) { // Unused
    }

    /** Unused */
    @Override
    public void windowActivated(WindowEvent e) { // Unused
    }
  }

  /** An abstract class for anything the code completion menu can help to complete. */
  public abstract static class Completion {
    protected String name;

    /**
     * Check if this code completion is applicable for the current code fragment.
     *
     * @param start
     *        The current input; the word so far.
     */
    public boolean match(String start) {
      return match(start, name) >= 0;
    }

    /**
     * Apply this completion to an editor.
     *
     * @param a
     *        The editor to which to apply this completion.
     * @param input
     *        The current input word fragment.
     * @param row
     *        The editor row the caret is currently on.
     * @param wordStart
     *        The index of the start of the word on this row.
     * @param wordEnd
     *        The index of the end of the word on this row.
     * @return Return whether this completion was applied successfully.
     */
    public abstract boolean apply(JoshText a, char input, int row, int wordStart, int wordEnd);

    /**
     * Convenience method to replace a section of text in the editor with new text.
     *
     * @param d
     *        The editor to which to apply this completion.
     * @param row
     *        The editor row the caret is currently on.
     * @param start
     *        The index of the start of the section of text to replace.
     * @param end
     *        The index of the end of the section of text to replace.
     * @param text
     *        The text with which to replace the given region.
     * @return Return whether this completion was applied successfully.
     */
    public static boolean replace(JoshText d, int row, int start, int end, String text) {
      // d.sel.insert(text);
      d.code.getsb(row).replace(start, end, text);
      d.code.fireLinesChanged();
      d.fireLineChange(row, row);
      d.repaint();

      // try
      // {
      // d.replace(offset,length,text,null);
      // }
      // catch (BadLocationException ble) {}
      return true;
    }

    /**
     * Given the full completion name, check if this completion is a match for the current input
     * fragment.
     *
     * @param input
     *        The current input; the word so far.
     * @param name
     *        The full name of this entry when completed, as known.
     */
    public static int match(String input, String name) {
      if (input.equals(name)) {
        return 0;
      }
      if (name.startsWith(input)) {
        return 1;
      }
      String il = input.toLowerCase();
      String nl = name.toLowerCase();
      if (il.equals(nl)) {
        return 2;
      }
      if (nl.startsWith(il)) {
        return 3;
      }
      String re = "(?<!(^|_))" + (name.matches("[A-Z_]+")? "." : "[a-z_]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
      String ns = name.replaceAll(re, "").toLowerCase(); //$NON-NLS-1$
      if (il.equals(ns)) {
        return 4;
      }
      if (ns.startsWith(il)) {
        return 5;
      }
      return -1;
    }

    @Override
    public String toString() {
      return name;
    }
  }

  /** Class for completing a simple keyword. */
  public static class WordCompletion extends Completion {
    /** Construct with the word to be completed. */
    public WordCompletion(String w) {
      name = w;
    }

    @Override
    public boolean apply(JoshText a, char input, int row, int wordStart, int wordEnd) {
      String s = name + (input != '\0'? String.valueOf(input) : new String());
      // int l = input != '\0' ? pos : length;
      if (!replace(a, row, wordStart, wordEnd, s)) {
        return false;
      }
      a.caret.row = row;
      a.caret.col = wordStart + s.length();
      a.caret.positionChanged();
      // a.setCaretPosition(offset + s.length());
      return true;
    }
  }

  /**
   * Class to handle key presses in the code completion pane.
   *
   * @author IsmAvatar
   */
  private class KeyHandler extends KeyAdapter {
    /** Invoke default super constructor. */
    public KeyHandler() {
      super();
    }

    /** Handle key press. */
    @Override
    public void keyPressed(KeyEvent e) {
      switch (e.getKeyCode()) {
        case KeyEvent.VK_BACK_SPACE:
          if (area.sel.isEmpty()) {
            if (caret <= 0) {
              dispose();
            } else {
              // try
              // {
              // area.getDocument().remove(wordOffset + wordPos - 1,1);
              caret -= 1;
            }
            wordEnd -= 1;
            // }
            // catch (BadLocationException ble)
            // {
            // dispose();
            // }
          } else {
            setSelectedText(new String());
          }
          e.consume();
          reset();
          break;
        case KeyEvent.VK_LEFT:
          if (caret <= 0) {
            dispose();
          } else {
            caret -= 1;
          }
          e.consume();
          reset();
          break;
        case KeyEvent.VK_RIGHT:
          if (caret >= wordEnd) {
            dispose();
          } else {
            caret += 1;
          }
          e.consume();
          reset();
          break;
        case KeyEvent.VK_ESCAPE:
          dispose();
          e.consume();
          break;
        case KeyEvent.VK_UP:
          selectRelative(-1);
          e.consume();
          break;
        case KeyEvent.VK_DOWN:
          selectRelative(1);
          e.consume();
          break;
      }
    }

    /** Handle key type. */
    @Override
    public void keyTyped(KeyEvent e) {
      if ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0) {
        return;
      }
      char c = e.getKeyChar();
      if (c == KeyEvent.VK_BACK_SPACE) {
        return;
      }
      // TODO: This statement used to check \\v and \\t as well, but it was causing VK_ENTER and
      // VK_TAB not to be accepted
      // as completing the menu which resulted in a painting exception. VK_TAB and VK_ENTER are
      // standard for completing
      // an autocompletion menu, see Eclipse and Scintilla/CodeBlocks.
      if (c == KeyEvent.VK_ENTER || c == KeyEvent.VK_TAB || c == KeyEvent.VK_SPACE) {
        apply(c);
        e.consume();
        dispose();
        return;
      }
      setSelectedText(String.valueOf(c));
      e.consume();
      reset();
    }
  }
}
