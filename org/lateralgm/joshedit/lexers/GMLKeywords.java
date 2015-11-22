/*
 * Copyright (C) 2007, 2008 Quadduc <quadduc@gmail.com>
 * Copyright (C) 2009, 2010 IsmAvatar <IsmAvatar@gmail.com>
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.joshedit.lexers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Properties;

import org.lateralgm.joshedit.DefaultKeywords;
import org.lateralgm.joshedit.DefaultKeywords.Constant;
import org.lateralgm.joshedit.DefaultKeywords.Construct;
import org.lateralgm.joshedit.DefaultKeywords.Function;
import org.lateralgm.joshedit.DefaultKeywords.Operator;
import org.lateralgm.joshedit.DefaultKeywords.Variable;

/**
 * Class to load and serve GML keywords.
 */
public final class GMLKeywords {
  /** GML syntax construct keywords */
  public static DefaultKeywords.Construct[] CONSTRUCTS;
  /** GML operator keywords */
  public static DefaultKeywords.Operator[] OPERATORS;
  /** GML global variable names */
  public static DefaultKeywords.Variable[] VARIABLES;
  /** GML constant names */
  public static DefaultKeywords.Constant[] CONSTANTS;
  /** GML function names */
  public static DefaultKeywords.Function[] FUNCTIONS;

  static {
    InputStream is = GMLKeywords.class.getResourceAsStream("gmlkeywords.properties"); //$NON-NLS-1$
    Properties p = new Properties();
    try {
      p.load(is);
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        is.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    String[] s = p.getProperty("CONSTRUCTS").split("\\s+"); //$NON-NLS-1$ //$NON-NLS-2$
    CONSTRUCTS = new Construct[s.length];
    for (int i = 0; i < s.length; i++) {
      CONSTRUCTS[i] = new Construct(s[i]);
    }
    s = p.getProperty("OPERATORS").split("\\s+"); //$NON-NLS-1$ //$NON-NLS-2$
    OPERATORS = new Operator[s.length];
    for (int i = 0; i < s.length; i++) {
      OPERATORS[i] = new Operator(s[i]);
    }
    s = p.getProperty("VARIABLES").split("\\s+"); //$NON-NLS-1$ //$NON-NLS-2$
    VARIABLES = new Variable[s.length];
    for (int i = 0; i < s.length; i++) {
      VARIABLES[i] = new Variable(s[i]);
    }
    s = p.getProperty("CONSTANTS").split("\\s+"); //$NON-NLS-1$ //$NON-NLS-2$
    CONSTANTS = new Constant[s.length];
    for (int i = 0; i < s.length; i++) {
      CONSTANTS[i] = new Constant(s[i]);
    }
    p.clear();

    // read functions
    InputStream is2 = GMLKeywords.class.getResourceAsStream("gmlfunctions.txt"); //$NON-NLS-1$
    BufferedReader br2 = new BufferedReader(new InputStreamReader(is2));
    ArrayList<Function> funcList = new ArrayList<Function>();

    try {
      String func;
      while ((func = br2.readLine()) != null) {
        String args = br2.readLine();
        String desc = br2.readLine();
        funcList.add(new Function(func, args, desc));
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        br2.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    FUNCTIONS = funcList.toArray(new Function[funcList.size()]);
  }

  private GMLKeywords() {
  }

}
