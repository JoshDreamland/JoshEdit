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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
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

      for (ColorProfile profile : lang.defaultProfiles()) {
        languagePicker.addItem(new ProfileItem(profile, null));
      }
      try {
        for (String scheme : prefs.childrenNames()) {
          Preferences schemeNode = prefs.node(scheme);
          ColorProfile prof = propertiesToColorProfile(prefsReadProperties(schemeNode));
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
      ProfileItem item = (ProfileItem) languagePicker.getSelectedItem();
      if (item.prefsEntry != null) {
        languagePicker.removeItem(item);
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
        scrollPane = new JScrollPane(new StylesPanel(prof, item.prefsEntry));
        stylePanels.put(prof.getName(), scrollPane);
      }
      add(scrollPane);
      scrollPane.repaint();
    }
  }

  private static final class StylesPanel extends JPanel {
    /** WRAAAAAAAAAAAAAAAAAAAAAH */
    private static final long serialVersionUID = 1L;
    List<UiRow> rows = new ArrayList<>();

    SpringLayout springLayout = new SpringLayout();
    private final Preferences prefsNode;

    StylesPanel(ColorProfile prof, Preferences prefsNode) {
      this.prefsNode = prefsNode;

      setLayout(springLayout);

      boolean builtIn = prefsNode == null;
      for (Entry<String, ColorProfileEntry> e : prof.entrySet()) {
        rows.add(new UiRow(e.getKey(), e.getValue(), !builtIn));
      }
      packLayout(3);
    }

    private final class UiRow {
      private final String S_BOLD = Runner.editorInterface.getString("LangPanel.BOLD"); //$NON-NLS-1$
      private final String S_ITAL = Runner.editorInterface.getString("LangPanel.ITALIC"); //$NON-NLS-1$
      private final String S_PICK_COLOR = Runner.editorInterface.getString("LangPanel.PICK_COLOR"); //$NON-NLS-1$

      private final JLabel label;
      private final JCheckBox chkBold;
      private final JCheckBox chkItal;
      private final JEColorButton colorButton;

      private final String prefsKey;

      UiRow(String key, ColorProfileEntry e, boolean editable) {
        this.prefsKey = key;

        StylesPanel.this.add(label = new JLabel(e.nlsName));
        StylesPanel.this.add(chkBold = new JCheckBox(S_BOLD, (e.fontStyle & Font.BOLD) != 0));
        StylesPanel.this.add(chkItal = new JCheckBox(S_ITAL, (e.fontStyle & Font.ITALIC) != 0));
        StylesPanel.this.add(colorButton = new JEColorButton(e.color, S_PICK_COLOR));

        chkBold.setEnabled(editable);
        chkItal.setEnabled(editable);
        colorButton.setEnabled(editable);
        if (editable) {
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
          colorButton.addColorListener(new ColorListener() {
            @Override
            public void colorChanged(ColorChangeEvent event) {
              updatePrefs();
            }
          });
        }
        label.setMaximumSize(new Dimension(Integer.MAX_VALUE, label.getPreferredSize().height));
      }

      protected void updatePrefs() {
        Color color = colorButton.getColor();
        boolean bold = chkBold.isSelected();
        boolean italic = chkItal.isSelected();
        prefsNode.put(BLOCK_NAMESPACE + prefsKey, encode(color, bold, italic));
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
      if (rows.size() < 1) {
        return;
      }

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
   * @throws IOException
   *         If the file cannot be read properly.
   * @throws FileNotFoundException
   *         If the given file doesn't exist.
   */
  public static ColorProfile readProfile(File properties) throws FileNotFoundException, IOException {
    Properties props = new Properties();
    try (InputStream is = new FileInputStream(properties)) {
      props.load(is);
    }
    return propertiesToColorProfile(props);
  }

  /** Parse a {@link Properties} object into a {@link ColorProfile}. */
  public static ColorProfile propertiesToColorProfile(Properties props) {
    String blockPrefix = BLOCK_NAMESPACE.toUpperCase();
    String editorPrefix = EDITOR_NAMESPACE.toUpperCase();
    ColorProfile.Builder builder = ColorProfile.newBuilder();

    for (Entry<Object, Object> entry : props.entrySet()) {
      String key = ((String) entry.getKey()).toUpperCase();
      String value = (String) entry.getValue();
      if (key.equals("NAME")) { //$NON-NLS-1$
        builder.setName(value);
        continue;
      }
      if (!key.startsWith(blockPrefix)) {
        if (key.startsWith(editorPrefix)) {
          System.out.println("FIXME: Ignoring editor attributes");
          continue;
        }
        System.err.println(String.format("Property \"%s\" not recognized", (String) entry.getKey())); //$NON-NLS-1$
        continue;
      }
      key = key.substring(BLOCK_NAMESPACE.length());
      builder.addProfileEntry(key, decode(key, value));
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

    props.setProperty("Name", profile.getName()); //$NON-NLS-1$
    props.setProperty("Name", profile.getName()); //$NON-NLS-1$
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

    Matcher m = RGB_PATTERN.matcher(value);
    Color color = Color.BLACK;
    if (m.find()) {
      int r = Integer.parseInt(m.group(1));
      int g = Integer.parseInt(m.group(2));
      int b = Integer.parseInt(m.group(3));
      color = new Color(r, g, b);
    }
    return ColorProfile.makeEntry(key, color, transform);
  }

  private static String encode(Color color, boolean bold, boolean ital) {
    int r = color.getRed();
    int g = color.getGreen();
    int b = color.getBlue();
    String res = String.format("rgb(%d, %d, %d)", r, g, b); //$NON-NLS-1$
    if (bold) {
      res += " bold"; //$NON-NLS-1$
    }
    if (ital) {
      res += " italic";  //$NON-NLS-1$
    }
    return res;
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
