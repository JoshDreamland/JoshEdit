package org.lateralgm.joshedit.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JColorChooser;

/**
 * Button for displaying and selecting a color.
 *
 * @author Josh Ventura
 */
public class JEColorButton extends JButton implements ActionListener {
  /** ... */
  private static final long serialVersionUID = 1L;

  private int paddingH = 6;
  private int paddingV = 4;

  private Color color = Color.WHITE;
  private String caption;

  /** Default constructor. */
  public JEColorButton() {
    addActionListener(this);
  }

  /**
   * Construct with a color.
   *
   * @param c
   *        The initial color.
   */
  public JEColorButton(Color c) {
    color = c;
    addActionListener(this);
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
    color = c;
    caption = cap;
    addActionListener(this);
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
  public Dimension getPreferredSize() {
    return new Dimension(48, super.getPreferredSize().height);
  }

  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    Dimension size = getSize();
    size.width -= paddingH << 1;
    size.height -= paddingV << 1;

    g.setColor(color);
    g.fillRect(paddingH, paddingV, size.width, size.height);
    g.setColor(Color.BLACK);
    g.drawRect(paddingH, paddingV, size.width, size.height);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    color = JColorChooser.showDialog(this, caption, color);
  }
}
