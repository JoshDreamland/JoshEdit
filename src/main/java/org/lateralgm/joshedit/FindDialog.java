/*
 * Copyright (C) 2011 Josh Ventura <JoshV10@gmail.com>
 * Copyright (C) 2011 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2013, Robert B. Colton
 *
 * This file is part of JoshEdit. JoshEdit is free software.
 * You can use, modify, and distribute it under the terms of
 * the GNU General Public License, version 3 or later.
 */

package org.lateralgm.joshedit;

import java.awt.Component;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;

/**
 * @author Josh Ventura
 *         Class to display a full set of find and replace options.
 */
public class FindDialog extends JDialog
    implements WindowListener, ActionListener, ComponentListener {
  /** Cram it, ECJ. */
  private static final long serialVersionUID = 1L;

  /** The Find combobox (a text field with drop-down history). */
  public static JComboBox<String> tFind;
  /** The Replace combobox (a text field with drop-down history). */
  public static JComboBox<String> tReplace;

  /** The "whole word only" checkbox. */
  public static JCheckBox whole;
  /** The "search from start" checkbox. */
  public static JCheckBox start;
  /** The "case sensitive" checkbox. */
  public static JCheckBox sens;
  /** The checkbox. */
  public static JCheckBox esc;
  /** The "regular expression" checkbox. */
  public static JCheckBox regex;
  /** The "wrap search at EOF" checkbox. */
  public static JCheckBox wrap;
  /** The "search backwards" checkbox. */
  public static JCheckBox backward;

  /** The game scope radio. */
  public static JRadioButton scGame;
  /** The object scope radio. */
  public static JRadioButton scObject;
  /** The event scope radio. */
  public static JRadioButton scEvent;
  /** The code scope radio. */
  public static JRadioButton scCode;
  /** The selection scope radio. */
  public static JRadioButton scSel;

  /** The Find button. */
  public static JButton bFind;
  /** The Replace button. */
  public static JButton bReplace;
  /** The Replace All button. */
  public static JButton bRepAll;

  /** The static FindDialog isntance (one per program). */
  protected static FindDialog INSTANCE;
  /** A collection of action listeners. */
  protected Set<ActionListener> listenerList = new HashSet<ActionListener>();
  /** A collection of permanent action listeners. */
  protected static Set<ActionListener> permListenerList = new HashSet<ActionListener>();

  /** The currently active JoshText. */
  public JoshText selectedJoshText = null;
  /** Whether we've ever been repositioned by the user. */
  private boolean userPositioned = false;
  /** The last assigned position of this widget. */
  private Point previousLocation;

  /** Listener to be informed when the find dialog is created. */
  public interface FindDialogCreationListener {
    /** Invoked upon the creation of a FindDialog. */
    void dialogCreated(FindDialog findDialog);
  }

  /** Add a global {@link FindDialogCreationListener}. */
  public static void addDialogCreationListener(FindDialogCreationListener listener) {
    createListeners.add(listener);
  }

  private static List<FindDialogCreationListener> createListeners = new ArrayList<>();

  /** Construct, creating everything. */
  private FindDialog() {
    super((Frame) null, Runner.editorInterface.getString("FindDialog.TITLE")); //$NON-NLS-1$
    applyLayout();
    addWindowListener(this);
    addComponentListener(this);
    pack();
    setMinimumSize(getSize());
    setIconImage(Runner.editorInterface.getIconForKey("JoshText.FIND").getImage()); //$NON-NLS-1$
  }

  /** @return Returns the static FindDialog instance. */
  public static FindDialog getInstance() {
    if (INSTANCE == null) {
      fireCreationEvent(INSTANCE = new FindDialog());
    }
    return INSTANCE;
  }

  private static void fireCreationEvent(FindDialog findDialog) {
    for (FindDialogCreationListener listener : createListeners) {
      listener.dialogCreated(findDialog);
    }
  }

  /**
   * Present this component, centering it if it's off in space.
   *
   * @param selectedEditor
   *        The JoshText which will be searched.
   */
  public void present(JoshText selectedEditor) {
    this.selectedJoshText = selectedEditor;
    if (!isVisible()) {
      if (!userPositioned) {
        Component centeringComponent =
            selectedJoshText == null? null : SwingUtilities.getWindowAncestor(selectedJoshText);
        setLocationRelativeTo(centeringComponent);
        getLocation(previousLocation);
      }
      setVisible(true);
    } else {
      requestFocus();
    }
  };

  /**
   * @author Josh Ventura
   *         Class to listen for <Enter> presses on find/replace fields and act on them.
   */
  class EnterListener implements ActionListener {
    /** Simulate a find press. */
    @Override
    public void actionPerformed(ActionEvent e) {
      bFind.doClick();
    }
  }

  /**
   * @author Josh Ventura
   *         An interface by which find/replace navigators can be invoked to handle search queries.
   */
  public interface FindNavigator {
    /**
     * @param find
     *        The new string to find.
     * @param replace
     *        The new string with which to replace found items.
     */
    void updateParameters(String find, String replace);

    /** Show yourself if hidden and grab focus. */
    void present();

    /** Find the next match. */
    void findNext();

    /** Find the previous match. */
    void findPrevious();

    /** Find and replace the next match. */
    void replaceNext();

    /** Replaces all the matches and returns the count. */
    int replaceAll();

    /** Find and replace the previous match. */
    void replacePrevious();

    /** Performs the replace action. */
    void doReplace();
  }

  /** Create the form layout. */
  private void applyLayout() {
    GroupLayout gl = new GroupLayout(getContentPane());
    gl.setAutoCreateContainerGaps(true);
    gl.setAutoCreateGaps(true);
    setLayout(gl);

    JLabel lFind = new JLabel(Runner.editorInterface.getString("FindDialog.FIND_LABEL")); //$NON-NLS-1$
    tFind = new JComboBox<String>();
    tFind.setEditable(true);
    tFind.getEditor().addActionListener(new EnterListener());

    JLabel lReplace = new JLabel(Runner.editorInterface.getString("FindDialog.REPLACE_LABEL")); //$NON-NLS-1$
    tReplace = new JComboBox<String>();
    tReplace.setEditable(true);
    tReplace.getEditor().addActionListener(new EnterListener());

    JPanel options = new JPanel();
    options.setLayout(new BoxLayout(options, BoxLayout.PAGE_AXIS));
    options.setBorder(BorderFactory.createTitledBorder(Runner.editorInterface.getString("FindDialog.OPTIONS"))); //$NON-NLS-1$
    options.add(whole = new JCheckBox(Runner.editorInterface.getString("FindDialog.WHOLE_WORD"))); //$NON-NLS-1$
    options.add(start = new JCheckBox(Runner.editorInterface.getString("FindDialog.WORD_START"))); //$NON-NLS-1$
    options.add(wrap = new JCheckBox(Runner.editorInterface.getString("FindDialog.WRAP_EOF"))); //$NON-NLS-1$
    wrap.setSelected(true);
    options.add(sens = new JCheckBox(Runner.editorInterface.getString("FindDialog.CASE_SENSITIVE"))); //$NON-NLS-1$
    options.add(esc = new JCheckBox(Runner.editorInterface.getString("FindDialog.ESCAPE_SEQS"))); //$NON-NLS-1$
    options.add(regex = new JCheckBox(Runner.editorInterface.getString("FindDialog.REGEX"))); //$NON-NLS-1$
    options.add(backward = new JCheckBox(Runner.editorInterface.getString("FindDialog.BACKWARDS"))); //$NON-NLS-1$

    new ButGroup(whole, start);
    new ButGroup(esc, regex);
    regex.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        boolean b = regex.isSelected();
        backward.setEnabled(!b);
      }
    });

    JPanel scope = new JPanel();
    scope.setLayout(new BoxLayout(scope, BoxLayout.PAGE_AXIS));
    scope.setBorder(BorderFactory.createTitledBorder(Runner.editorInterface.getString("FindDialog.SCOPE"))); //$NON-NLS-1$

    ButtonGroup bg = new ButtonGroup();

    // TODO: Modularize to avoid ties to LGM.
    bg.add(scGame = new JRadioButton(Runner.editorInterface.getString("FindDialog.GAME"))); //$NON-NLS-1$
    scope.add(scGame);
    bg.add(scObject = new JRadioButton(Runner.editorInterface.getString("FindDialog.OBJECT"))); //$NON-NLS-1$
    scope.add(scObject);
    bg.add(scEvent = new JRadioButton(Runner.editorInterface.getString("FindDialog.EVENT"))); //$NON-NLS-1$
    scope.add(scEvent);
    bg.add(scCode = new JRadioButton(Runner.editorInterface.getString("FindDialog.CODE"), true)); //$NON-NLS-1$
    scope.add(scCode);
    bg.add(scSel = new JRadioButton(Runner.editorInterface.getString("FindDialog.SELECTION"))); //$NON-NLS-1$
    scope.add(scSel);

    bFind = new JButton(Runner.editorInterface.getString("FindDialog.FIND")); //$NON-NLS-1$
    bFind.addActionListener(this);
    getRootPane().setDefaultButton(bFind);
    bReplace = new JButton(Runner.editorInterface.getString("FindDialog.REPLACE")); //$NON-NLS-1$
    bReplace.addActionListener(this);
    bRepAll = new JButton(Runner.editorInterface.getString("FindDialog.REPLACE_ALL")); //$NON-NLS-1$
    bRepAll.addActionListener(this);
    JButton bClose = new JButton(Runner.editorInterface.getString("FindDialog.CLOSE")); //$NON-NLS-1$

    bFind.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (selectedJoshText == null) {
          System.err.println("No text editor selected. Ever."); //$NON-NLS-1$
          return;
        }
        setVisible(false);
        selectedJoshText.finder.updateParameters((String) tFind.getEditor().getItem(),
            (String) tReplace.getEditor().getItem());
        selectedJoshText.finder.present();
        if (backward.isSelected()) {
          selectedJoshText.finder.findPrevious();
        } else {
          selectedJoshText.finder.findNext();
        }
      }
    });
    bReplace.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (selectedJoshText == null) {
          System.err.println("No text editor selected. Ever."); //$NON-NLS-1$
          return;
        }
        setVisible(false);
        selectedJoshText.finder.updateParameters((String) tFind.getEditor().getItem(),
            (String) tReplace.getEditor().getItem());
        selectedJoshText.finder.present();
        if (backward.isSelected()) {
          selectedJoshText.finder.replacePrevious();
        } else {
          selectedJoshText.finder.replaceNext();
        }
      }
    });

    bRepAll.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (selectedJoshText == null) {
          System.err.println("No text editor selected. Ever."); //$NON-NLS-1$
          return;
        }
        setVisible(false);
        selectedJoshText.finder.updateParameters((String) tFind.getEditor().getItem(),
            (String) tReplace.getEditor().getItem());
        int results = selectedJoshText.finder.replaceAll();
        JOptionPane.showMessageDialog(null,
            String.format(Runner.editorInterface.getString("FindDialog.OCCURRENCES_REPL"), //$NON-NLS-1$
                results));
      }
    });

    bClose.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        dispose();
      }
    });

    int pref = GroupLayout.PREFERRED_SIZE;

    gl.setHorizontalGroup(gl.createParallelGroup()
    /**/.addGroup(gl.createSequentialGroup()
    /*	*/.addGroup(gl.createParallelGroup()
    /*		*/.addComponent(lFind)
    /*		*/.addComponent(lReplace))
    /*	*/.addGroup(gl.createParallelGroup()
    /*		*/.addComponent(tFind)
    /*		*/.addComponent(tReplace)))
    /**/.addGroup(gl.createSequentialGroup()
    /*	*/.addComponent(options, pref, pref, Integer.MAX_VALUE)
    /*	*/.addComponent(scope, pref, pref, Integer.MAX_VALUE))
    /**/.addGroup(gl.createSequentialGroup()
    /*	*/.addComponent(bFind, pref, pref, Integer.MAX_VALUE)
    /*	*/.addComponent(bReplace, pref, pref, Integer.MAX_VALUE)
    /*	*/.addComponent(bRepAll, pref, pref, Integer.MAX_VALUE)
    /*	*/.addComponent(bClose, pref, pref, Integer.MAX_VALUE)));

    gl.setVerticalGroup(gl.createSequentialGroup()
    /**/.addGroup(gl.createParallelGroup()
    /*	*/.addComponent(lFind)
    /*	*/.addComponent(tFind, pref, pref, pref))
    /**/.addGroup(gl.createParallelGroup()
    /*	*/.addComponent(lReplace)
    /*	*/.addComponent(tReplace, pref, pref, pref))
    /**/.addGroup(gl.createParallelGroup()
    /*	*/.addComponent(options, pref, pref, Integer.MAX_VALUE)
    /*	*/.addComponent(scope, pref, pref, Integer.MAX_VALUE))
    /**/.addGroup(gl.createParallelGroup()
    /*	*/.addComponent(bFind)
    /*	*/.addComponent(bReplace)
    /*	*/.addComponent(bRepAll)
    /*	*/.addComponent(bClose)));
  }

  /**
   * Adds an action listener to this dialog for when one of the buttons gets pressed.
   * Note that the ActionEvent.getSource() will be the respective button, and not this dialog.
   * <p>Also note that this listener automatically unregisters when the dialog closes.
   * For a more permanent listener that exists beyond dialog closes, use the static
   * <code>addPermanentActionListener</code>.
   *
   * @param l
   *        The <code>ActionListener</code> to be added.
   */
  public void addActionListener(ActionListener l) {
    listenerList.add(l);
  }

  /**
   * Adds an action listener to this dialog that exists beyond dialog closes.
   * Note that the ActionEvent.getSource() will be the respective button, and not this dialog.
   * <p>For projects with multiple places that you can find/replace in, it becomes undesirable
   * for one Find/replace to trigger the old listener of another location, so it would be
   * preferable to remove the listener after a dialog close. The non-static
   * <code>addActionListener</code> handles this for you automatically.
   *
   * @param l
   *        The <code>ActionListener</code> to be added.
   */
  public static void addPermanentActionListener(ActionListener l) {
    permListenerList.add(l);
  }

  /**
   * @param l
   *        The listener to remove.
   */
  public static void removePermanentActionListener(ActionListener l) {
    permListenerList.remove(l);
  }

  /** Dispatch performed actions to own listers. */
  @Override
  public void actionPerformed(ActionEvent e) {
    for (ActionListener l : listenerList) {
      l.actionPerformed(e);
    }
    for (ActionListener l : permListenerList) {
      l.actionPerformed(e);
    }
  }

  /** Clean up when window is closed. */
  @Override
  public void windowClosed(WindowEvent e) {
    listenerList.clear();
  }

  /** Clean up when window is closed. */
  @Override
  public void windowClosing(WindowEvent e) {
    listenerList.clear();
  }

  /** Unused. */
  @Override
  public void windowActivated(WindowEvent e) { // unused
  }

  /** Unused. */
  @Override
  public void windowDeactivated(WindowEvent e) { // unused
  }

  /** Unused. */
  @Override
  public void windowDeiconified(WindowEvent e) { // unused
  }

  /** Unused. */
  @Override
  public void windowIconified(WindowEvent e) { // unused
  }

  /** Unused. */
  @Override
  public void windowOpened(WindowEvent e) { // unused
  }

  @Override
  public void componentMoved(ComponentEvent e) {
    if (userPositioned) {
      return;
    }
    if (previousLocation == null) {
      previousLocation = getLocation();
      return;
    }
    if (!getLocation().equals(previousLocation)) {
      userPositioned = true;
    }
  }

  /** Unused. */
  @Override
  public void componentHidden(ComponentEvent e) { // unused
  }

  /** Unused. */
  @Override
  public void componentResized(ComponentEvent e) { // unused
  }

  /** Unused. */
  @Override
  public void componentShown(ComponentEvent e) { // unused

  }
}
