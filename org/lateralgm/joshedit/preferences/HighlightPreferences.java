package org.lateralgm.joshedit.preferences;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.LayoutManager;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.ScrollPaneLayout;
import javax.swing.Spring;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;

import org.lateralgm.joshedit.ColorProfile;
import org.lateralgm.joshedit.ColorProfile.ColorProfileEntry;
import org.lateralgm.joshedit.Runner;
import org.lateralgm.joshedit.TokenMarker.LanguageDescription;
import org.lateralgm.joshedit.swing.JEColorButton;

/**
 * Syntax highlighting preferences panel.
 *
 * @author Josh Ventura
 */
public class HighlightPreferences extends JTabbedPane {
  /** Shut. Up. ECJ. Gomz. */
  private static final long serialVersionUID = 1L;

  private static final class ProfileItem {
    public final ColorProfile prof;

    @Override
    public String toString() {
      return prof.getName();
    }

    public ProfileItem(ColorProfile prof) {
      this.prof = prof;
    }
  }

  private static final class LanguagePanel extends JPanel implements ItemListener {
    /** holy i dont even stfu ecj */
    private static final long serialVersionUID = 1L;

    Map<String, StylesPanel> stylePanels = new HashMap<>();

    /**/JPanel boxPanel;
    /*  */JComboBox<ProfileItem> languagePicker = new JComboBox<>();
    /*  */JButton addLangButton = new JButton(Runner.editorInterface.getString("LangPanel.NEW")); //$NON-NLS-1$
    /*  */JButton delLangButton = new JButton(Runner.editorInterface.getString("LangPanel.DELETE")); //$NON-NLS-1$
    /**/JScrollPane scrollPane = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

    public LanguagePanel(LanguageDescription lang) {
      setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

      languagePicker.setEditable(false);
      add(buildBoxPanel());
      displayCard(lang.defaultProfiles().iterator().next());

      for (ColorProfile profile : lang.defaultProfiles()) {
        languagePicker.addItem(new ProfileItem(profile));
      }

      languagePicker.addItemListener(this);
    }

    private Component buildBoxPanel() {
      final int padding = 4;
      boxPanel = new JPanel();
      boxPanel.setLayout(new BoxLayout(boxPanel, BoxLayout.LINE_AXIS));
      boxPanel.add(languagePicker);
      boxPanel.add(Box.createRigidArea(new Dimension(padding, padding)));
      boxPanel.add(addLangButton);
      boxPanel.add(Box.createRigidArea(new Dimension(padding, padding)));
      boxPanel.add(delLangButton);
      boxPanel.setMaximumSize(new Dimension(boxPanel.getMaximumSize().width,
          languagePicker.getPreferredSize().height + 2 * padding));
      boxPanel.setBorder(BorderFactory.createEmptyBorder(padding, padding, padding, padding));
      return boxPanel;
    }

    @Override
    public void itemStateChanged(ItemEvent arg0) {
      displayCard(((ProfileItem) arg0.getItem()).prof);
    }

    private void displayCard(ColorProfile prof) {
      // ////scrollPane.removeAll();
      scrollPane.add(new StylesPanel(prof), 0, 0);
      scrollPane.setLayout(new ScrollPaneLayout());
      scrollPane.setSize(new Dimension(64, 64));
      add(scrollPane);
    }
  }

  private static final class StylesPanel extends JPanel {
    /** WRAAAAAAAAAAAAAAAAAAAAAH */
    private static final long serialVersionUID = 1L;

    SpringLayout springLayout = new SpringLayout();

    StylesPanel(ColorProfile prof) {
      setLayout(springLayout);
      for (ColorProfileEntry e : prof.values()) {
        add(new JLabel(e.nlsName));
        add(new JCheckBox("Bold", (e.fontStyle & Font.BOLD) != 0));
        add(new JCheckBox("Italic", (e.fontStyle & Font.ITALIC) != 0));
        add(new JEColorButton(e.color, "Pick a color"));
      }
      packLayout(4, prof.values().size(), 4);
    }

    private void packLayout(int cols, int rows, int padding) {
      int totalWidth = 0, totalHeight = 0;
      // Align all cells in each column and make them the same width.
      Spring x = Spring.constant(padding);
      for (int c = 0; c < cols; c++) {
        Spring width = Spring.constant(0);
        for (int r = 0; r < rows; r++) {
          width = Spring.max(width, getConstraintsForCell(r, c, cols).getWidth());
        }
        totalWidth += width.getValue();
        for (int r = 0; r < rows; r++) {
          SpringLayout.Constraints constraints = getConstraintsForCell(r, c, cols);
          constraints.setX(x);
          constraints.setWidth(width);
        }
        x = Spring.sum(x, Spring.sum(width, Spring.constant(padding)));
      }

      // Align all cells in each row and make them the same height.
      Spring y = Spring.constant(padding);
      for (int r = 0; r < rows; r++) {
        Spring height = Spring.constant(0);
        for (int c = 0; c < cols; c++) {
          height = Spring.max(height, getConstraintsForCell(r, c, cols).getHeight());
        }
        totalHeight += height.getValue();
        for (int c = 0; c < cols; c++) {
          SpringLayout.Constraints constraints = getConstraintsForCell(r, c, cols);
          constraints.setY(y);
          constraints.setHeight(height);
        }
        y = Spring.sum(y, Spring.sum(height, Spring.constant(padding)));
      }

      // Set the parent's size.
      SpringLayout.Constraints pCons = springLayout.getConstraints(this);
      pCons.setConstraint(SpringLayout.SOUTH, y);
      pCons.setConstraint(SpringLayout.EAST, x);

      // setPreferredSize(new Dimension(totalWidth, totalHeight));
      setSize(new Dimension(520, totalHeight));
    }

    /* Used by makeCompactGrid. */
    private SpringLayout.Constraints getConstraintsForCell(int row, int col, int cols) {
      return springLayout.getConstraints(getComponent(row * cols + col));
    }
  }

  /** Construct, populating window */
  public HighlightPreferences(LanguageDescription[][] languages) {
    setTabPlacement(SwingConstants.RIGHT);
    for (LanguageDescription[] langs : languages) {
      for (LanguageDescription lang : langs) {
        addTab(lang.getName(), new LanguagePanel(lang));
      }
    }
  }

}
