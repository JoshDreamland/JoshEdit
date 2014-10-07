/*
 * Copyright (C) 2011 Josh Ventura <JoshV10@gmail.com>
 * Copyright (C) 2011 IsmAvatar <IsmAvatar@gmail.com>
 *
 * This file is part of JoshEdit. JoshEdit is free software.
 * You can use, modify, and distribute it under the terms of
 * the GNU General Public License, version 3 or later.
 */

package org.lateralgm.joshedit.preferences;

import java.awt.Component;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.AbstractCellEditor;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;

import org.lateralgm.joshedit.JoshText;
import org.lateralgm.joshedit.Runner;
import org.lateralgm.joshedit.preferences.KeybindingsPanel.KeystrokeTableModel.Row;

/**
 * A panel class containing keybinding options.
 */
public class KeybindingsPanel extends JPanel {
  /** Can it, ECJ. */
  private static final long serialVersionUID = 1L;

  private static final String EMPTY_STRING = ""; //$NON-NLS-1$

  /** The default mappings file. */
  private static final ResourceBundle DEFAULTS =
      ResourceBundle.getBundle("org.lateralgm.joshedit.defaults"); //$NON-NLS-1$
  /** The translation file. */
  private static final ResourceBundle TRANSLATE =
      ResourceBundle.getBundle("org.lateralgm.joshedit.translate"); //$NON-NLS-1$
  /** The preferences file. */
  public static final Preferences PREFS = Preferences.userRoot().node("/org/lateralgm/joshedit"); //$NON-NLS-1$

  /** Our list of bindings. */
  JTable list;

  /** The TableModel for our keystroke manipulation table. */
  class KeystrokeTableModel extends AbstractTableModel {
    /** Bottle it, ECJ. */
    private static final long serialVersionUID = 1L;

    /** A row containing a description and a keystroke. */
    class Row {
      /** The description of the shortcut. */
      String desc;
      /** The associated keystroke. */
      String key;

      /**
       * @param d
       *        The human-readable description.
       * @param k
       *        The keystroke.
       */
      Row(String d, String k) {
        desc = d;
        key = k;
      }
    }

    /** Rows in this table. */
    ArrayList<Row> rows;

    /** Construct, do allocation. */
    public KeystrokeTableModel() {
      rows = new ArrayList<Row>();
    }

    /**
     * @param desc
     *        The human-readable description of the shortcut.
     * @param key
     *        The keystroke to which it is mapped.
     */
    public void addRow(String desc, String key) {
      rows.add(new Row(desc, key));
    }

    /** We always have two columns. */
    @Override
    public int getColumnCount() {
      return 2;
    }

    /**
     * Our column names are fixed.
     *
     * @return The name of the column with the given index (max = 1).
     */
    @Override
    public String getColumnName(int columnIndex) {
      return columnIndex == 0? Runner.editorInterface.getString("Bindings.DESCRIPTION") //$NON-NLS-1$
          : Runner.editorInterface.getString("Bindings.KEYSTROKES"); //$NON-NLS-1$
    }

    /** @return Return the number of rows (or possible keybindings). */
    @Override
    public int getRowCount() {
      return rows.size();
    }

    /** @see javax.swing.table.TableModel#getValueAt(int, int) */
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
      return columnIndex == 0? rows.get(rowIndex).desc : rows.get(rowIndex).key;
    }

    /**
     * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
     */
    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
      if (columnIndex == 0) {
        rows.get(rowIndex).desc = aValue.toString();
      } else {
        rows.get(rowIndex).key = aValue.toString();
      }
    }

    /**
     * Only our second column (index 1) is editable.
     *
     * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
     */
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
      return columnIndex == 1;
    }
  }

  /**
   * @param key2
   *        The key to describe in human language.
   * @return The keystroke's human-language description.
   */
  private static String keyToEnglish(String key2) {
    String pieces[] = key2.split("\\+"); //$NON-NLS-1$
    if (pieces.length == 0) {
      return null;
    }
    int mods = 0;
    if (pieces.length > 1) {
      if (pieces[0].contains("C")) { //$NON-NLS-1$
        mods |= InputEvent.CTRL_DOWN_MASK;
      }
      if (pieces[0].contains("S")) { //$NON-NLS-1$
        mods |= InputEvent.SHIFT_DOWN_MASK;
      }
      if (pieces[0].contains("A")) { //$NON-NLS-1$
        mods |= InputEvent.ALT_DOWN_MASK;
      }
      if (pieces[0].contains("M")) { //$NON-NLS-1$
        mods |= InputEvent.META_DOWN_MASK;
      }
      if (pieces[0].contains("G")) { //$NON-NLS-1$
        mods |= InputEvent.ALT_GRAPH_DOWN_MASK;
      }
    }
    String lastPiece = pieces[pieces.length - 1];
    if (mods == 0) {
      return lastPiece;
    }
    return InputEvent.getModifiersExText(mods) + " + " + lastPiece; //$NON-NLS-1$
  }

  /** Renderer that writes human-language depictions of keystrokes. */
  class KeystrokeRenderer extends DefaultTableCellRenderer {
    /** Shut up, ECJ. */
    private static final long serialVersionUID = 1L;

    /**
     * Set the text to display to a translation of the given value.
     *
     * @param value
     *        The keystroke value to display.
     */
    @Override
    public void setValue(Object value) {
      super.setValue((value == null)? EMPTY_STRING
          : value instanceof String? keyToEnglish((String) value)
          : value);
    }
  }

  /** A keystroke selection box to prompt for a key combination. */
  class KeystrokeSelector extends JTextField {

    /** Shut up, ECJ. */
    private static final long serialVersionUID = 1L;

    /** The internal representation of the keystroke. */
    public String coreValue = EMPTY_STRING;
    /** The initial internal representation of the keystroke. */
    private String beginValue = EMPTY_STRING;
    /** The table in which we are nested. */
    private JTable myTable;
    /** The row on which we are placed. */
    private int myRow;

    /** Construct with specific JTextField properties. */
    KeystrokeSelector() {
      setFocusTraversalKeysEnabled(false);
      setDragEnabled(false);
      setEditable(false);
    }

    /** @see javax.swing.JComponent#processKeyEvent(java.awt.event.KeyEvent) */
    @Override
    public void processKeyEvent(KeyEvent e) {
      if (e.getID() != KeyEvent.KEY_PRESSED) {
        return;
      }

      if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
        coreValue = beginValue;
        super.setText(keyToEnglish(coreValue));
        return;
      }
      if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
        coreValue = EMPTY_STRING;
        super.setText(Runner.editorInterface.getString("Bindings.2")); //$NON-NLS-1$
        return;
      }

      System.out.println(e.getModifiersEx() + " " + e.getKeyCode()); //$NON-NLS-1$

      // Modifiers by themselves invalid
      if (e.getKeyCode() == KeyEvent.VK_CONTROL || e.getKeyCode() == KeyEvent.VK_ALT
          || e.getKeyCode() == KeyEvent.VK_SHIFT || e.getKeyCode() == 0) {
        return;
      }

      // Typable characters invalid
      if (e.getModifiersEx() == InputEvent.SHIFT_DOWN_MASK // and nothing else
          || e.getModifiersEx() == 0 && !e.isActionKey()) {
        return;
      }

      // Generate name
      String mods = EMPTY_STRING;
      if (e.isControlDown()) {
        mods += "C"; //$NON-NLS-1$
      }
      if (e.isAltDown()) {
        mods += "A"; //$NON-NLS-1$
      }
      if (e.isShiftDown()) {
        mods += "S"; //$NON-NLS-1$
      }
      if (e.isMetaDown()) {
        mods += "M"; //$NON-NLS-1$
      }
      if (e.isAltGraphDown()) {
        mods += "G"; //$NON-NLS-1$
      }
      if (mods.length() > 0) {
        mods += "+"; //$NON-NLS-1$
      }

      String name = mods + KeyEvent.getKeyText(e.getKeyCode());

      if (!resolveCollision(name)) {
        return;
      }

      setText(name);
      fireActionPerformed();
    }

    /**
     * Check for keystroke collisions.
     *
     * @param name
     *        The keystroke to check for collisions against.
     * @return Whether or not there is already a shortcut to that keystroke.
     */
    private boolean resolveCollision(String name) {
      int row = 0;
      for (Row i : ((KeystrokeTableModel) myTable.getModel()).rows) {
        if (i.key.equals(name) && row != myRow) {
          boolean ret =
              JOptionPane.showConfirmDialog(null,
                  String.format(Runner.editorInterface.getString("Bindings.ALREADY_SET"), i.desc), //$NON-NLS-1$
                  Runner.editorInterface.getString("Bindings.CONFLICT_CAP"), //$NON-NLS-1$
                  JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
          if (ret) {
            i.key = EMPTY_STRING;
            myTable.repaint();
          }
          return ret;
        }
        row++;
      }
      return true;
    }

    /**
     * Translate keystroke garbage before setting it as text.
     *
     * @see javax.swing.text.JTextComponent#setText(java.lang.String)
     */
    @Override
    public void setText(String text) {
      coreValue = text;
      super.setText(keyToEnglish(text));
    }

    /**
     * @see javax.swing.text.JTextComponent#getText()
     */
    @Override
    public String getText() {
      return super.getText();
    }

    /**
     * @return Returns the English text.
     */
    public String getValue() {
      return getText();
    }

    /**
     * @param row
     *        The row we represent.
     * @param table
     *        The table we are nested in.
     * @param initialKeyStroke
     *        Our initial keystroke value.
     */
    public void init(int row, JTable table, String initialKeyStroke) {
      myRow = row;
      myTable = table;
      setText(beginValue = initialKeyStroke);
    }
  }

  /**
   * A cell editor implementation that uses our {@link KeystrokeSelector} class.
   */
  class KeystrokeEditor extends AbstractCellEditor implements TableCellEditor {
    /** Shut up, ECJ. */
    private static final long serialVersionUID = 1L;

    /** The keystroke selector we will use to prompt for a key combination. */
    private final KeystrokeSelector comp;

    /** Construct, creating a keystroke selector. */
    public KeystrokeEditor() {
      comp = new KeystrokeSelector();
    }

    /** @see javax.swing.CellEditor#getCellEditorValue() */
    @Override
    public Object getCellEditorValue() {
      return comp.coreValue;
    }

    /**
     * @see javax.swing.table.TableCellEditor#getTableCellEditorComponent(javax.swing.JTable,
     *      java.lang.Object, boolean, int, int)
     */
    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,
        int row, int column) {
      comp.init(row, table, value.toString());
      return comp;
    }
  }

  /** Construct and set up. */
  public KeybindingsPanel() {
    super();
    setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

    add(new JLabel(Runner.editorInterface.getString("Bindings.LIST_EXPLANATION"))); //$NON-NLS-1$

    KeystrokeTableModel model = new KeystrokeTableModel();
    // DefaultTableColumnModel colmodel = new DefaultTableColumnModel();
    list = new JTable(model);
    add(new JScrollPane(list));
    TableColumn c = list.getColumnModel().getColumn(1);
    c.setCellRenderer(new KeystrokeRenderer());
    c.setCellEditor(new KeystrokeEditor());
    // c.setCellEditor(new DefaultCellEditor(new KeystrokeSelector()));

    System.out.println("Populate bindings"); //$NON-NLS-1$
    populateBindings(model);

    /*
     * TableColumn descc = new TableColumn();
     * descc.setHeaderRenderer(new DefaultTableCellRenderer());
     * colmodel.addColumn(descc);
     * TableColumn keyc = new TableColumn();
     * keyc.setHeaderRenderer(new DefaultTableCellRenderer());
     * colmodel.addColumn(keyc);
     */
    // model.addRow("Description","Key");
    // list.updateUI();

  }

  /**
   * @param model
   *        The custom table model into which bindings are read.
   */
  static void populateBindings(KeystrokeTableModel model) {
    TreeMap<String, ArrayList<String>> items = new TreeMap<String, ArrayList<String>>();
    Preferences bindings = PREFS.node("bindings"); //$NON-NLS-1$
    try {
      for (String key : bindings.keys()) {
        String act = bindings.get(key, null);
        if (act != null) {
          ArrayList<String> a = items.get(act);
          if (a == null) {
            items.put(act, a = new ArrayList<String>());
          }
          a.add(key);
        }
      }
    } catch (BackingStoreException e) {
      System.err.println("Failed to read JoshEdit keybindings!"); //$NON-NLS-1$
    }
    if (model.getRowCount() == 0) {
      for (String key : DEFAULTS.keySet()) {
        String act = DEFAULTS.getString(key);
        ArrayList<String> a = items.get(act);
        if (a == null) {
          items.put(act, a = new ArrayList<String>());
        }
        a.add(key);
      }
    }
    for (Entry<String, ArrayList<String>> i : items.entrySet()) {
      String act = i.getKey();
      for (String key : i.getValue()) {
        model.addRow(getString("bindings." + act, act), key); //$NON-NLS-1$
        act = EMPTY_STRING;
      }
    }
  }

  /**
   * @param key
   *        The translation to look up.
   * @param def
   *        The default text when no translation is found, or null if it should be found.
   * @return The translation if it exists, the default string if it doesn't, or an error string if
   *         neither is non-null.
   */
  public static String getString(String key, String def) {
    String r;
    try {
      r = TRANSLATE.getString(key);
    } catch (MissingResourceException e) {
      r = def == null? '!' + key + '!' : def;
    }
    return PREFS.get(key, r);
  }

  /**
   * @param im
   *        The input map into which mappings are read.
   */
  public static void readMappings(InputMap im) {
    Preferences bindings = PREFS.node("bindings"); //$NON-NLS-1$
    try {
      boolean changed = false;
      for (String key : bindings.keys()) {
        String act = bindings.get(key, null);
        if (act != null) {
          changed = true;
          im.put(JoshText.key(key), act);
        }
      }
      if (changed) {
        return;
      }
    } catch (BackingStoreException e) {
      System.err.println("Failed to read JoshEdit keybindings!"); //$NON-NLS-1$
    }

    for (String key : DEFAULTS.keySet()) {
      String act = DEFAULTS.getString(key);
      im.put(JoshText.key(key), act);
    }
  }
}
