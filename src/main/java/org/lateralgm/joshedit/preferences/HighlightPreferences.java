package org.lateralgm.joshedit.preferences;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.Spring;
import javax.swing.SpringLayout;
import javax.swing.SpringLayout.Constraints;
import javax.swing.SwingConstants;

import org.lateralgm.joshedit.ColorProfile;
import org.lateralgm.joshedit.ColorProfile.ColorProfileEntry;
import org.lateralgm.joshedit.Runner;
import org.lateralgm.joshedit.TokenMarker.LanguageDescription;
import org.lateralgm.joshedit.swing.JEColorButton;
import org.lateralgm.joshedit.swing.JEColorButton.ColorChangeEvent;
import org.lateralgm.joshedit.swing.JEColorButton.ColorListener;

/**
 * Syntax highlighting preferences panel.
 *
 * @author Josh Ventura
 */
public class HighlightPreferences extends JTabbedPane {
  /** Shut. Up. ECJ. Gomz. */
  private static final long serialVersionUID = 1L;

  private static final class ProfileItem {
    public final ColorProfile profile;
    public final Preferences prefsEntry;

    @Override
    public String toString() {
      return profile.getName();
    }

    public ProfileItem(ColorProfile prof, Preferences fileName) {
      this.profile = prof;
      this.prefsEntry = fileName;
    }
  }

  private static final class LanguagePanel extends JPanel implements ItemListener {
    /** holy i dont even stfu ecj */
    private static final long serialVersionUID = 1L;

    private static final int SCROLLPANE_HEIGHT = 320;

    Map<String, JScrollPane> stylePanels = new HashMap<>();

    /**/JPanel boxPanel;
    /*  */JComboBox<ProfileItem> languagePicker = new JComboBox<>();
    /*  */JButton addLangButton = new JButton(Runner.editorInterface.getString("LangPanel.NEW")); //$NON-NLS-1$
    /*  */JButton delLangButton = new JButton(Runner.editorInterface.getString("LangPanel.DELETE")); //$NON-NLS-1$
    /**/JScrollPane scrollPane = null;

    private final Preferences prefs;

    public LanguagePanel(LanguageDescription lang, Preferences prefs) {
      setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
      this.prefs = prefs;

      languagePicker.setEditable(false);
      add(buildBoxPanel());

      Set<String> correctOrder = new LinkedHashSet<>();
      for (ColorProfile profile : lang.defaultProfiles()) {
        languagePicker.addItem(new ProfileItem(profile, null));
        for (String n : profile.keySet()) {
          correctOrder.add(n);
        }
      }
      try {
        for (String scheme : prefs.childrenNames()) {
          Preferences schemeNode = prefs.node(scheme);
          ColorProfile prof =
              propertiesToColorProfile(prefsReadProperties(schemeNode), correctOrder);
          languagePicker.addItem(new ProfileItem(prof, schemeNode));
        }
      } catch (BackingStoreException e) {
        e.printStackTrace();
      }

      languagePicker.addItemListener(this);
      addLangButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent arg0) {
          addClicked();
        }
      });
      delLangButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent arg0) {
          deleteClicked();
        }
      });

      if (languagePicker.getItemCount() > 0) {
        displayCard((ProfileItem) languagePicker.getSelectedItem());
      }
    }

    private void addClicked() {
      ColorProfile curProfile = ((ProfileItem) languagePicker.getSelectedItem()).profile;
      String curName = curProfile.getName();
      String defNameFmt = Runner.editorInterface.getString("LangPanel.MY_COPY"); //$NON-NLS-1$
      String msg = Runner.editorInterface.getString("LangPanel.ENTER_NAME_PROMPT"); //$NON-NLS-1$
      String cap = Runner.editorInterface.getString("LangPanel.ENTER_NAME_CAP"); //$NON-NLS-1$
      String newName = String.format(defNameFmt, curName);

      int type = JOptionPane.QUESTION_MESSAGE;
      newName = (String) JOptionPane.showInputDialog(this, msg, cap, type, null, null, newName);
      if (newName == null) {
        return;
      }

      String newNixName = newName.replaceAll("\\W", "_"); //$NON-NLS-1$ //$NON-NLS-2$
      try {
        while (prefs.nodeExists(newNixName)) {
          newNixName += "_"; //$NON-NLS-1$
        }
      } catch (BackingStoreException e) {
        e.printStackTrace();
      }

      Preferences node = prefs.node(newNixName);
      ColorProfile nProf = new ColorProfile(newName, curProfile);
      prefsPutProperties(node, profileToProperties(nProf));
      ProfileItem profileItem = new ProfileItem(nProf, node);
      languagePicker.addItem(profileItem);
      languagePicker.setSelectedItem(profileItem);
    }

    private void deleteClicked() {
      String confMessage = Runner.editorInterface.getString("LangPanel.CONFIRM_DELETE"); //$NON-NLS-1$
      String deleteTitle = Runner.editorInterface.getString("LangPanel.DELETE_PROF_TITLE"); //$NON-NLS-1$
      int confirm =
          JOptionPane.showConfirmDialog(this, confMessage, deleteTitle, JOptionPane.WARNING_MESSAGE);
      if (confirm != JOptionPane.OK_OPTION)
        return;
      ProfileItem item = (ProfileItem) languagePicker.getSelectedItem();
      if (item.prefsEntry != null) {
        languagePicker.removeItem(item);
        stylePanels.remove(item.profile.getName());
        try {
          item.prefsEntry.removeNode();
        } catch (BackingStoreException e) {
          e.printStackTrace();
        }
      }
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
    public void itemStateChanged(ItemEvent event) {
      ProfileItem item = (ProfileItem) event.getItem();
      displayCard(item);
    }

    private void displayCard(ProfileItem item) {
      ColorProfile prof = item.profile;
      boolean builtIn = item.prefsEntry == null;
      delLangButton.setEnabled(!builtIn);
      if (scrollPane != null) {
        remove(scrollPane);
      }
      if (stylePanels.containsKey(prof.getName())) {
        scrollPane = stylePanels.get(prof.getName());
      } else {
        scrollPane =
            new JScrollPane(new StylesPanel(prof, item.prefsEntry),
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        Dimension preferred = scrollPane.getPreferredSize();
        if (preferred.height > SCROLLPANE_HEIGHT) {
          scrollPane.setPreferredSize(new Dimension(preferred.width, SCROLLPANE_HEIGHT));
        }
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        stylePanels.put(prof.getName(), scrollPane);
      }
      add(scrollPane);
      scrollPane.repaint();
    }
  }

  private static final class StylesPanel extends JPanel {
    /** WRAAAAAAAAAAAAAAAAAAAAAH */
    private static final long serialVersionUID = 1L;
    List<FormattingRow> rows = new ArrayList<>();

    SpringLayout springLayout = new SpringLayout();
    private final Preferences prefsNode;

    StylesPanel(ColorProfile prof, Preferences prefsNode) {
      this.prefsNode = prefsNode;

      setLayout(springLayout);

      boolean builtIn = prefsNode == null;
      for (Entry<String, ColorProfileEntry> e : prof.entrySet()) {
        rows.add(new FormattingRow(e.getKey(), e.getValue(), !builtIn));
      }
      for (Entry<String, Color> e : prof.colorProperties()) {
        rows.add(new FormattingRow(e.getKey(), e.getValue(), !builtIn));
      }
      packLayout(3);
    }

    private final class FormattingRow {
      private final String S_BOLD = Runner.editorInterface.getString("LangPanel.BOLD"); //$NON-NLS-1$
      private final String S_ITAL = Runner.editorInterface.getString("LangPanel.ITALIC"); //$NON-NLS-1$
      private final String S_PICK_COLOR = Runner.editorInterface.getString("LangPanel.PICK_COLOR"); //$NON-NLS-1$

      private final boolean colorOnly;
      private final JLabel label;
      private final JCheckBox chkBold;
      private final JCheckBox chkItal;
      private final JEColorButton colorButton;

      private final String prefsKey;

      FormattingRow(String key, ColorProfileEntry e, boolean editable) {
        colorOnly = false;
        this.prefsKey = key;

        StylesPanel.this.add(label = new JLabel(e.nlsName));
        StylesPanel.this.add(chkBold = new JCheckBox(S_BOLD, (e.fontStyle & Font.BOLD) != 0));
        StylesPanel.this.add(chkItal = new JCheckBox(S_ITAL, (e.fontStyle & Font.ITALIC) != 0));
        StylesPanel.this.add(colorButton = new JEColorButton(e.color, S_PICK_COLOR));

        chkBold.setEnabled(editable);
        chkItal.setEnabled(editable);
        colorButton.setEnabled(editable);

        if (editable) {
          addCheckListeners();
          addColorListener();
        }

        label.setMaximumSize(new Dimension(Integer.MAX_VALUE, label.getPreferredSize().height));
      }

      FormattingRow(String key, Color color, boolean editable) {
        colorOnly = true;
        this.prefsKey = key;

        StylesPanel.this.add(label = new JLabel(ColorProfile.getPropertyNlsName(key)));
        StylesPanel.this.add(colorButton = new JEColorButton(color, S_PICK_COLOR));
        chkBold = chkItal = null;

        colorButton.setEnabled(editable);

        if (editable) {
          addColorListener();
        }

        label.setMaximumSize(new Dimension(Integer.MAX_VALUE, label.getPreferredSize().height));
      }

      private void addColorListener() {
        colorButton.addColorListener(new ColorListener() {
          @Override
          public void colorChanged(ColorChangeEvent event) {
            updatePrefs();
          }
        });
      }

      private void addCheckListeners() {
        chkBold.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            updatePrefs();
          }
        });
        chkItal.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            updatePrefs();
          }
        });
      }

      protected void updatePrefs() {
        Color color = colorButton.getColor();
        if (colorOnly) {
          prefsNode.put(EDITOR_NAMESPACE + prefsKey, encodeColor(color));
          return;
        }
        boolean bold = chkBold.isSelected();
        boolean italic = chkItal.isSelected();
        prefsNode.put(BLOCK_NAMESPACE + prefsKey, encode(color, bold, italic));
      }

      /** The maximum height of elements of this row. */
      private Spring maxHeight;

      void computeMaxima(Spring[] maxWidths, Spring[] maxSpanWidths) {
        if (colorOnly) {
          Constraints c = springLayout.getConstraints(label);
          maxSpanWidths[1] = Spring.max(maxSpanWidths[1], c.getWidth());
          maxHeight = c.getHeight();
          c = springLayout.getConstraints(colorButton);
          maxWidths[3] = Spring.max(maxWidths[3], c.getWidth());
          maxHeight = Spring.max(maxHeight, c.getHeight());
          return;
        }
        int j = 0;
        maxHeight = Spring.constant(0);
        for (Constraints c : getConstraints()) {
          maxWidths[j] = Spring.max(maxWidths[j], c.getWidth());
          maxHeight = Spring.max(maxHeight, c.getHeight());
          ++j;
        }
      }

      private Spring applyMaxima(Spring[] maxWidths, Spring[] spanWidths, final Spring padSpring,
          Spring[] xs, Spring y) {
        if (colorOnly) {
          constrain(springLayout.getConstraints(label), xs[0], y, spanWidths[1], maxHeight);
          constrain(springLayout.getConstraints(colorButton), xs[3], y, maxWidths[3], maxHeight);
        } else {
          int j = 0;
          for (Constraints c : getConstraints()) {
            constrain(c, xs[j], y, maxWidths[j++], maxHeight);
          }
        }
        return Spring.sum(padSpring, Spring.sum(y, maxHeight));
      }

      private void constrain(Constraints c, Spring x, Spring y, Spring width, Spring height) {
        c.setX(x);
        c.setY(y);
        c.setWidth(width);
        c.setHeight(height);
      }

      private Constraints[] getConstraints() {
        return new Constraints[] { springLayout.getConstraints(label),
            springLayout.getConstraints(chkBold), springLayout.getConstraints(chkItal),
            springLayout.getConstraints(colorButton) };
      }

      int componentCount() {
        return 4;
      }
    }

    private void packLayout(int padding) {
      if (rows.size() < 1) {
        return;
      }

      // Prepare to stash maxima
      Spring[] maxWidths = new Spring[rows.get(0).componentCount()];
      Spring[] maxSpanWidths = new Spring[rows.get(1).componentCount() - 1];
      for (int i = 0; i < maxWidths.length; ++i) {
        maxWidths[i] = Spring.constant(0);
      }
      for (int i = 0; i < maxWidths.length - 1; ++i) {
        maxSpanWidths[i] = Spring.constant(0);
      }

      final Spring padSpring = Spring.constant(padding);
      Constraints parentConstraints = springLayout.getConstraints(this);

      // Compute maximum widths of columns and heights of rows
      for (FormattingRow row : rows) {
        row.computeMaxima(maxWidths, maxSpanWidths);
      }

      // Compute column coordinates
      Spring tWidth = maxWidths[0];
      final Spring dualPad = Spring.sum(padSpring, padSpring);
      Spring[] xs = new Spring[maxWidths.length];
      xs[0] = padSpring;

      for (int i = 1; i < xs.length; ++i) {
        // Set the current x coordinate to that maximum, plus the initial padding
        xs[i] = Spring.sum(dualPad, tWidth);
        // Compute the total width thus far as the sum of all column widths and the padding between
        tWidth = Spring.sum(padSpring, Spring.sum(tWidth, maxWidths[i]));
        // Use whichever is larger of the width of spanning columns and the current sum, for both
        tWidth = maxSpanWidths[i - 1] = Spring.max(tWidth, maxSpanWidths[i - 1]);
      }

      // Apply computed coordinate springs to each component
      Spring y = padSpring;
      for (FormattingRow row : rows) {
        y = row.applyMaxima(maxWidths, maxSpanWidths, padSpring, xs, y);
      }

      // Set our size to our sums
      parentConstraints.setConstraint(SpringLayout.SOUTH, y);
      parentConstraints.setConstraint(SpringLayout.EAST, Spring.sum(tWidth, dualPad));
    }
  }

  /** Construct, populating window */
  public HighlightPreferences(LanguageDescription[][] languages, Preferences prefs) {
    setTabPlacement(SwingConstants.RIGHT);
    for (LanguageDescription[] langs : languages) {
      for (LanguageDescription lang : langs) {
        addTab(lang.getName(), new LanguagePanel(lang, prefs.node(lang.getUnixName())));
      }
    }
  }

  private static final String BLOCK_NAMESPACE = "Block."; //$NON-NLS-1$
  private static final String EDITOR_NAMESPACE = "Editor."; //$NON-NLS-1$

  /**
   * Read a color profile from a properties file.
   *
   * @param properties
   *        The filename to write.
   * @param correctOrder
   *        A set representing the correct order of the complete set of profile entries, by key.
   *
   * @throws IOException
   *         If the file cannot be read properly.
   * @throws FileNotFoundException
   *         If the given file doesn't exist.
   */
  public static ColorProfile readProfile(File properties, Set<String> correctOrder)
      throws FileNotFoundException, IOException {
    Properties props = new Properties();
    try (InputStream is = new FileInputStream(properties)) {
      props.load(is);
    }
    return propertiesToColorProfile(props, correctOrder);
  }

  /**
   * Parse a {@link Properties} object into a {@link ColorProfile}.
   *
   * @param props
   *        The filename to write.
   * @param correctOrder
   *        A set representing the correct order of the complete set of profile entries, by key.
   *
   * @param correctOrder
   */
  public static ColorProfile propertiesToColorProfile(Properties props, Set<String> correctOrder) {
    String blockPrefix = BLOCK_NAMESPACE.toUpperCase();
    String editorPrefix = EDITOR_NAMESPACE.toUpperCase();
    ColorProfile.Builder builder = ColorProfile.newBuilder();

    Map<String, ColorProfileEntry> readEntries =
        correctOrder != null? new HashMap<String, ColorProfileEntry>() : null;
    for (Entry<Object, Object> entry : props.entrySet()) {
      String key = ((String) entry.getKey()).toUpperCase();
      String value = (String) entry.getValue();
      if (key.equals("NAME")) { //$NON-NLS-1$
        builder.setName(value);
        continue;
      }
      if (!key.startsWith(blockPrefix)) {
        if (key.startsWith(editorPrefix)) {
          Color color = decodeColor(value.toLowerCase());
          builder.setColorByProperty(key.substring(EDITOR_NAMESPACE.length()), color);
          continue;
        }
        System.err.println(String.format("Property \"%s\" not recognized", (String) entry.getKey())); //$NON-NLS-1$
        continue;
      }
      key = key.substring(BLOCK_NAMESPACE.length());
      if (readEntries != null) {
        readEntries.put(key, decode(key, value));
      } else {
        builder.addProfileEntry(key, decode(key, value));
      }
    }

    if (correctOrder != null && readEntries != null) { // No one said warning compiler was perfect
      for (String key : correctOrder) {
        if (readEntries.containsKey(key)) {
          builder.addProfileEntry(key, readEntries.get(key));
          readEntries.remove(key);
        } else {
          builder.add(key, ColorProfile.makeEntry(key, null, 0));
        }
      }
      for (Entry<String, ColorProfileEntry> entry : readEntries.entrySet()) {
        builder.add(entry.getKey(), entry.getValue());
      }
    }

    return builder.build();
  }

  /**
   * Write a {@link ColorProfile} to a properties file.
   *
   * @throws FileNotFoundException
   * @throws IOException
   */
  public static void writeProfile(ColorProfile profile, File properties)
      throws FileNotFoundException, IOException {
    String comments =
        String.format("Generated highlight theme \"%s\"", profile.getName(), new Date().toString()); //$NON-NLS-1$

    Properties props = profileToProperties(profile);
    try (OutputStream fos = new FileOutputStream(properties)) {
      props.store(fos, comments);
    }
  }

  /** Represent a {@link ColorProfile} as a {@link Properties} object. */
  public static Properties profileToProperties(ColorProfile profile) {
    Properties props = new Properties();
    for (Entry<String, ColorProfileEntry> ent : profile.entrySet()) {
      props.setProperty(BLOCK_NAMESPACE + ent.getKey(), encode(ent.getValue()));
    }
    for (Entry<String, Color> ent : profile.colorProperties()) {
      props.setProperty(EDITOR_NAMESPACE + ent.getKey(), encodeColor(ent.getValue()));
    }
    props.setProperty("Name", profile.getName()); //$NON-NLS-1$
    return props;
  }

  private static final Pattern RGB_PATTERN =
      Pattern.compile("rgb\\(\\s*([0-9]+)\\s*,\\s*([0-9]+)\\s*,\\s*([0-9]+)\\)"); //$NON-NLS-1$

  private static ColorProfileEntry decode(String key, String value) {
    value = value.toLowerCase();

    int transform = Font.PLAIN;
    if (value.contains("ital")) { //$NON-NLS-1$
      transform |= Font.ITALIC;
    }
    if (value.contains("bold")) { //$NON-NLS-1$
      transform |= Font.BOLD;
    }

    Color color = decodeColor(value);
    return ColorProfile.makeEntry(key, color, transform);
  }

  private static Color decodeColor(String value) {
    Matcher m = RGB_PATTERN.matcher(value);
    Color color = Color.BLACK;
    if (m.find()) {
      int r = Integer.parseInt(m.group(1));
      int g = Integer.parseInt(m.group(2));
      int b = Integer.parseInt(m.group(3));
      color = new Color(r, g, b);
    }
    return color;
  }

  private static String encode(Color color, boolean bold, boolean ital) {
    String res = encodeColor(color);
    if (bold) {
      res += " bold"; //$NON-NLS-1$
    }
    if (ital) {
      res += " italic";  //$NON-NLS-1$
    }
    return res;
  }

  private static String encodeColor(Color color) {
    int r = color.getRed();
    int g = color.getGreen();
    int b = color.getBlue();
    return String.format("rgb(%d, %d, %d)", r, g, b); //$NON-NLS-1$
  }

  private static String encode(ColorProfileEntry value) {
    return encode(value.color, (value.fontStyle & Font.BOLD) != 0,
        (value.fontStyle & Font.ITALIC) != 0);
  }

  private static void prefsPutProperties(Preferences prefs, Properties props) {
    for (Entry<Object, Object> e : props.entrySet()) {
      prefs.put((String) e.getKey(), (String) e.getValue());
    }
  }

  private static Properties prefsReadProperties(Preferences prefs) {
    Properties props = new Properties();
    try {
      for (String key : prefs.keys()) {
        props.put(key, prefs.get(key, null));
      }
    } catch (BackingStoreException e) {
      e.printStackTrace();
    }
    return props;
  }
}
