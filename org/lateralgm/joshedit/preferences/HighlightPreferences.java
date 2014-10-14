package org.lateralgm.joshedit.preferences;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.Spring;
import javax.swing.SpringLayout;
import javax.swing.SpringLayout.Constraints;
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

    Map<String, JScrollPane> stylePanels = new HashMap<>();

    /**/JPanel boxPanel;
    /*  */JComboBox<ProfileItem> languagePicker = new JComboBox<>();
    /*  */JButton addLangButton = new JButton(Runner.editorInterface.getString("LangPanel.NEW")); //$NON-NLS-1$
    /*  */JButton delLangButton = new JButton(Runner.editorInterface.getString("LangPanel.DELETE")); //$NON-NLS-1$
    /**/JScrollPane scrollPane = null;

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
      if (scrollPane != null) {
        remove(scrollPane);
      }
      if (stylePanels.containsKey(prof.getName())) {
        scrollPane = stylePanels.get(prof.getName());
      } else {
        scrollPane = new JScrollPane(new StylesPanel(prof));
        stylePanels.put(prof.getName(), scrollPane);
      }
      add(scrollPane);
    }
  }

  private static final class StylesPanel extends JPanel {
    /** WRAAAAAAAAAAAAAAAAAAAAAH */
    private static final long serialVersionUID = 1L;
    List<UiRow> rows = new ArrayList<>();

    SpringLayout springLayout = new SpringLayout();

    StylesPanel(ColorProfile prof) {
      setLayout(springLayout);
      for (ColorProfileEntry e : prof.values()) {
        rows.add(new UiRow(e));
      }
      packLayout(3);
    }

    private final class UiRow {
      private final String S_BOLD = Runner.editorInterface.getString("LangPanel.BOLD"); //$NON-NLS-1$
      private final String S_ITAL = Runner.editorInterface.getString("LangPanel.ITALIC"); //$NON-NLS-1$
      private final String S_PICK_COLOR = Runner.editorInterface.getString("LangPanel.PICK_COLOR"); //$NON-NLS-1$

      JLabel label;
      JCheckBox chkBold;
      JCheckBox chkItal;
      JEColorButton colorButton;

      UiRow(ColorProfileEntry e) {
        StylesPanel.this.add(label = new JLabel(e.nlsName));
        StylesPanel.this.add(chkBold = new JCheckBox(S_BOLD, (e.fontStyle & Font.BOLD) != 0));
        StylesPanel.this.add(chkItal = new JCheckBox(S_ITAL, (e.fontStyle & Font.ITALIC) != 0));
        StylesPanel.this.add(colorButton = new JEColorButton(e.color, S_PICK_COLOR));
        label.setMaximumSize(new Dimension(Integer.MAX_VALUE, label.getPreferredSize().height));
      }

      int componentCount() {
        return 4;
      }

      public Constraints[] getConstraints() {
        return new Constraints[] { springLayout.getConstraints(label),
            springLayout.getConstraints(chkBold), springLayout.getConstraints(chkItal),
            springLayout.getConstraints(colorButton) };
      }
    }

    private void packLayout(int padding) {
      // Prepare to stash maxima
      Spring[] maxWidths = new Spring[rows.get(0).componentCount()];
      Spring[] maxHeights = new Spring[rows.size()];
      for (int i = 0; i < maxWidths.length; ++i) {
        maxWidths[i] = Spring.constant(0);
      }

      final Spring padSpring = Spring.constant(padding);
      Constraints parentConstraints = springLayout.getConstraints(this);

      // Compute maximum widths of columns and heights of rows
      int i = 0;
      for (UiRow row : rows) {
        maxHeights[i] = Spring.constant(0);
        int j = 0;
        for (Constraints c : row.getConstraints()) {
          maxWidths[j] = Spring.max(maxWidths[j], c.getWidth());
          maxHeights[i] = Spring.max(maxHeights[i], c.getHeight());
          ++j;
        }
        ++i;
      }

      // Compute column coordinates
      Spring x = padSpring;
      Spring[] xs = new Spring[maxWidths.length];
      for (i = 0; i < xs.length; ++i) {
        xs[i] = x;
        x = Spring.sum(padSpring, Spring.sum(x, maxWidths[i]));
      }

      // Apply computed coordinate springs to each component
      i = 0;
      Spring y = padSpring;
      for (UiRow row : rows) {
        int j = 0;
        for (Constraints c : row.getConstraints()) {
          c.setX(xs[j]);
          c.setY(y);
          c.setWidth(maxWidths[j]);
          c.setHeight(maxHeights[i]);
          ++j;
        }
        y = Spring.sum(padSpring, Spring.sum(y, maxHeights[i]));
        ++i;
      }

      // Set our size to our sums
      parentConstraints.setConstraint(SpringLayout.SOUTH, y);
      parentConstraints.setConstraint(SpringLayout.EAST, x);
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
