/*
 * Copyright (C) 2011 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2013, 2014, Robert B. Colton
 *
 * This file is part of JoshEdit. JoshEdit is free software.
 * You can use, modify, and distribute it under the terms of
 * the GNU General Public License, version 3 or later.
 */

package org.lateralgm.joshedit;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JPanel;

// import org.lateralgm.resources.Font;

/** Panel to display line numbers. */
public class LineNumberPanel extends JPanel {
  /** The default foreground color for light themes. */
  public static final Color DEFAULT_LIGHT_BACKGROUND = new Color(220, 220, 220);
  /** The default background color for light themes. */
  public static final Color DEFAULT_LIGHT_FOREGROUND = new Color(170, 170, 170);

  /** Shut up, ECJ. */
  private static final long serialVersionUID = 1L;

  /**
   * The FontMetrics of the textarea that this is watching, so we know the font height for spacing
   * line numbers
   */
  protected FontMetrics metrics;
  /** The number of lines to number. */
  protected int lines;
  /** Indicates whether line numbering starts at 0 */
  protected boolean startZero;

  /** The foreground (font) color of this panel. */
  private Color fgColor = DEFAULT_LIGHT_FOREGROUND;
  /** The background color of this panel. */
  private Color bgColor = DEFAULT_LIGHT_BACKGROUND;

  /**
   * @param metrics
   *        The font metrics to use to paint numbers at the correct position.
   * @param lines
   *        The number of lines in the code.
   * @param startZero
   *        True if the first line should be given index 0, false if it should be given index 1.
   */
  public LineNumberPanel(FontMetrics metrics, int lines, boolean startZero) {
    this.setFont(new Font("Monospace", Font.PLAIN, metrics.getFont().getSize())); //$NON-NLS-1$
    this.metrics = metrics;
    this.lines = lines;
    this.startZero = startZero;
    resize();
  }

  /**
   * @param textarea
   *        The text area component to draw next to.
   * @param lines
   *        The number of lines in the text area component.
   * @param startZero
   *        True if the first line should be given index 0, false if it should be given index 1.
   */
  public LineNumberPanel(JComponent textarea, int lines, boolean startZero) {
    this(textarea.getFontMetrics(textarea.getFont()), lines, startZero);
  }

  /**
   * Set the number of lines to be numbered.
   *
   * @param lines
   *        The number of lines.
   */
  public void setLines(int lines) {
    this.lines = lines + (startZero? 0 : 1);
    resize();
    repaint();
  }

  /** Set the colors used to render this panel from a {@link ColorProfile}. */
  public void setColorProfile(ColorProfile prof) {
    if (prof.getLineNumberColor() != null) {
      fgColor = prof.getLineNumberColor();
    }
    if (prof.getLineNumberPanelColor() != null) {
      bgColor = prof.getLineNumberPanelColor();
    }
  }

  /**
   * Get the width of a line number panel
   * @param maxline
   *        the maximum line number to determine how many characters must be fit
   * @return appropriate width for the line number component or to print one
   */
  public int getLineNumberWidth(int maxline) {
    // find the advance of the widest number
    int[] widths = getFontMetrics(getFont()).getWidths();
    int maxAdvance = 0;
    for (int i = '0'; i <= '9'; i++) {
      if (widths[i] > maxAdvance) {
        maxAdvance = widths[i];
      }
    }
    return maxAdvance * (int) Math.max(Math.log10(maxline - (startZero? 1 : 0)) + 2, 2);
  }

  /**
   * Call upon resize to repaint.
   */
  public void resize() {
    // multiply by max number of digits
    int width = getLineNumberWidth(lines);

    // get line height, multiply by number of lines. + 1 line since the end seems to have a little
    // extra
    int height = metrics.getHeight() * (lines + 1);

    setPreferredSize(new Dimension(width + 3, height));
    revalidate();

    // this particular line appears to be necessary to allow these changes to take effect.
    // note that we can't swap out with validate() or revalidate() for some reason.
    // getParent().doLayout();
  }

  /**
   * Paints the line number panel with special consideration for printing.
   * @param g
   *        graphics object to use for painting
   * @param start
   *        the first line to start on
   * @param count
   *        the number of lines to paint from the start
   * @param width
   *        the width of the line number area, ensures all pages have the same line number width
   */
  public void printLineNumbers(Graphics g, int start, int count, int width) {
    Object map = Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints"); //$NON-NLS-1$
    if (map != null) {
      ((Graphics2D) g).addRenderingHints((Map<?, ?>) map);
    }

    final int insetY = metrics.getLeading() + metrics.getAscent();
    final int gh = metrics.getHeight();
    Dimension size = new Dimension(width, count * gh);
    int lineNum = start;
    final int end = size.height;

    // we don't want to waste the users ink, most IDE's do not print the background
    g.setColor(bgColor);
    g.fillRect(0, 0, size.width, size.height);
    g.setColor(fgColor);

    g.setFont(this.getFont());

    for (int y = insetY; lineNum < lines && y <= end; lineNum++, y += gh) {
      String str = Integer.toString(lineNum);
      int strw = (int) g.getFontMetrics().getStringBounds(str, g).getWidth();
      g.drawString(str, size.width - strw - 3, y);
    }
  }

  /**
   * @param g
   *        The graphics object to which to paint.
   */
  @Override
  public void paintComponent(Graphics g) {
    Object map = Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints"); //$NON-NLS-1$
    if (map != null) {
      ((Graphics2D) g).addRenderingHints((Map<?, ?>) map);
    }

    Rectangle clip = this.getVisibleRect();
    // line numbering is always there regardless of horizontal scroll
    // if you don't make the width static and drag the code editor
    // so that line numbers are outside the mdi area, the numbers smudge
    clip.setSize(this.getWidth(), clip.height);
    final int insetY = metrics.getLeading() + metrics.getAscent();
    final int gh = metrics.getHeight();
    int lineNum = clip.y / gh;
    final int start = lineNum * gh + insetY;
    final int end = clip.y + clip.height + gh;
    if (!startZero) {
      lineNum++;
    }

    g.setColor(bgColor);
    // g.fillRect(clip.x,clip.y,clip.width,clip.height);
    g.fillRect(0, 0, getWidth(), getHeight());
    g.setColor(fgColor);

    g.setFont(this.getFont());

    for (int y = start; lineNum < lines && y <= end; lineNum++, y += gh) {
      String str = Integer.toString(lineNum);
      int strw = (int) g.getFontMetrics().getStringBounds(str, g).getWidth();
      g.drawString(str, clip.width - strw - 3, y);
    }
  }
}
