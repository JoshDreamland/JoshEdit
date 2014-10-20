package org.lateralgm.joshedit.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JColorChooser;

/**
 * Button for displaying and selecting a color.
 *
 * @author Josh Ventura
 */
public class JEColorButton extends JButton {
  /** ... */
  private static final long serialVersionUID = 1L;

  private int paddingH = 6;
  private int paddingV = 4;

  private Color color = Color.WHITE;
  private String caption;

  private static BufferedImage nullColorPattern = null;

  private static void generateNullColorPattern() {
    nullColorPattern = new BufferedImage(5, 5, BufferedImage.TYPE_4BYTE_ABGR);
    Graphics g = nullColorPattern.getGraphics();
    g.setColor(Color.BLACK);
    g.drawLine(4, 0, 0, 4);
    g.dispose();
  }

  /** Default constructor. */
  public JEColorButton() {
    addActionListener(new ClickListener());
  }

  /**
   * Construct with a color.
   *
   * @param c
   *        The initial color.
   */
  public JEColorButton(Color c) {
    this();
    color = c;
  }

  /**
   * Construct with a color.
   *
   * @param c
   *        The initial color.
   * @param cap
   *        The color picker window caption.
   */
  public JEColorButton(Color c, String cap) {
    this(c);
    caption = cap;
  }

  /** Fetch the currently selected color. */
  public Color getColor() {
    return color;
  }

  /**
   * Set the padding around the color rectangle rendered on this button.
   *
   * @param paddingH
   *        The horizontal padding.
   * @param paddingV
   *        The vertical padding.
   */
  public void setPadding(int paddingH, int paddingV) {
    this.paddingH = paddingH;
    this.paddingV = paddingV;
  }

  @Override
  public Dimension getMinimumSize() {
    return new Dimension(48, 22);
  }

  @Override
  public Dimension getPreferredSize() {
    return new Dimension(48, 22);
  }

  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    Dimension size = getSize();
    size.width -= paddingH << 1;
    size.height -= paddingV << 1;

    Rectangle myRect = new Rectangle(paddingH, paddingV, size.width, size.height);
    if (color != null) {
      g.setColor(color);
      g.fillRect(paddingH, paddingV, size.width, size.height);
    } else {
      if (nullColorPattern == null) {
        generateNullColorPattern();
      }
      Graphics2D g2 = (Graphics2D) g;
      g2.setPaint(new TexturePaint(nullColorPattern, new Rectangle(0, 0, 5, 5)));
      g2.fill(myRect);
    }
    g.setColor(Color.BLACK);
    g.drawRect(paddingH, paddingV, size.width, size.height);
  }

  /** Event fired when the current color changes. */
  public static final class ColorChangeEvent {
    private final Color oldColor;
    private final Color newColor;
    private final JEColorButton sender;

    /** Full constructor. */
    public ColorChangeEvent(Color oldColor, Color newColor, JEColorButton sender) {
      this.oldColor = oldColor;
      this.newColor = newColor;
      this.sender = sender;
    }

    /** Get the color which was previously selected. */
    public Color getOldColor() {
      return oldColor;
    }

    /** Get the newly-selected color. */
    public Color getNewColor() {
      return newColor;
    }

    /** Get the button which generated this event. */
    public JEColorButton getSender() {
      return sender;
    }
  }

  /** Fire off a color change event to all listeners. */
  public void fireColorChange(ColorChangeEvent event) {
    for (ColorListener listener : colorListeners) {
      listener.colorChanged(event);
    }
  }

  /** Interface to listen for changes to the selected color. */
  public interface ColorListener {
    /** Invoked when a new color is chosen. */
    void colorChanged(ColorChangeEvent event);
  }

  private final List<ColorListener> colorListeners = new ArrayList<>();

  /** Add a {@link ColorListener} to this color button. */
  public void addColorListener(ColorListener colorListener) {
    colorListeners.add(colorListener);
  }

  private final class ClickListener implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
      Color nColor = JColorChooser.showDialog(JEColorButton.this, caption, color);
      if (nColor == null) {
        return;
      }
      if (!nColor.equals(color)) {
        Color oColor = color;
        color = nColor;
        fireColorChange(new ColorChangeEvent(oColor, color, JEColorButton.this));
      }
    }
  }
}
