/**
 * @file DefaultKeywords.java
 * @brief Class implementing abstract keyword classes.
 *
 * @section License
 *
 *          Copyright (C) 2007, 2008 Quadduc <quadduc@gmail.com>
 *          Copyright (C) 2009, 2010 IsmAvatar <IsmAvatar@gmail.com>
 *          Copyright (C) 2014 Robert B. Colton
 *          This file is a part of the LateralGM IDE.
 *
 *          This program is free software: you can redistribute it and/or modify
 *          it under the terms of the GNU General Public License as published by
 *          the Free Software Foundation, either version 3 of the License, or
 *          (at your option) any later version.
 *
 *          This program is distributed in the hope that it will be useful,
 *          but WITHOUT ANY WARRANTY; without even the implied warranty of
 *          MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *          GNU General Public License for more details.
 *
 *          You should have received a copy of the GNU General Public License
 *          along with this program. If not, see <http://www.gnu.org/licenses/>.
 **/

package org.lateralgm.joshedit;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Class defining a basic keyword code-completion interface. */
public class DefaultKeywords {

  /** Basic keyword class. */
  public abstract static class Keyword {
    protected String name;

    /** Fetch the name of this keyword. */
    public String getName() {
      return name;
    }
  }

  /** Interface used to retrieve keywords from a TokenMarker (or other entity). */
  public interface HasKeywords {
    /** Retrieve an array of keyword groups used by this marker. */
    Keyword[][] getKeywords();
  }

  /** Class used to store language construct keywords, such as "if". */
  public static class Construct extends Keyword {
    /** Create with a construct name, eg, "if". */
    public Construct(String input) {
      name = input;
    }
  }

  /** Class used to store operator keywords, such as "and". */
  public static class Operator extends Keyword {
    /** Construct with an operator name, eg, "if". */
    public Operator(String input) {
      name = input;
    }
  }

  /** Class used to store language global variables, such as "current_time". */
  public static class Variable extends Keyword {
    /** True if this variable cannot be assigned (note: even readOnly variables change). */
    public final boolean readOnly;
    /** The size of this variable if it is an array. */
    public final int arraySize;

    /** Construct with a variable name, eg, "current_time". */
    public Variable(String input) {
      Matcher m = Pattern.compile("(\\w+)(\\[(\\d+)])?(\\*)?").matcher(input); //$NON-NLS-1$
      if (!m.matches()) {
        System.err.println("Invalid variable: " + input); //$NON-NLS-1$
      }
      name = m.group(1);
      String s = m.group(3);
      arraySize = s != null? Integer.valueOf(m.group(3)) : 0;
      readOnly = "*".equals(m.group(4)); //$NON-NLS-1$
    }
  }

  /** Class used to store language constants, such as "M_PI". */
  public static class Constant extends Keyword {
    /** Construct with a constant name, eg, "M_PI". */
    public Constant(String input) {
      name = input;
    }
  }

  /** Class to store information about functions, such as printf. */
  public static class Function extends Keyword {
    /** The human-friendly description of this function, internationalized. */
    public final String description;
    /** The names of the arguments to this function. */
    public final String[] arguments;
    /** The index at which arguments become dynamic; that is, the index of the first dynamic arg. */
    public final int dynArgIndex;
    /** The minimum number of dynamic arguments. */
    public final int dynArgMin;
    /** The maximum number of dynamic arguments. */
    public final int dynArgMax;

    /** Construct with a function prototype; parse prototype for other metrics. */
    public Function(String input) {
      // @formatter:off
      //    1   1  23    3 245   5  6   6 7   7 8        84 9   9
      // /(\w+)\(((\w+,)*)((\w+)\{(\d+),(\d+)}((?=\))|,))?(\w+)?\)/
      //   fun  (  arg,     arg  { 0   , 9   }        ,    arg   )
      // @formatter:on
      String re = "(\\w+)\\(((\\w+,)*)((\\w+)\\{(\\d+),(\\d+)}((?=\\))|,))?(\\w+)?\\)"; //$NON-NLS-1$
      Matcher m = Pattern.compile(re).matcher(input);
      if (!m.matches()) {
        System.err.println("Invalid function: " + input); //$NON-NLS-1$
      }
      name = m.group(1); // the function name
      String a1 = m.group(2); // plain arguments with commas
      String da = m.group(5); // argument with range
      String daMin = m.group(6); // range min
      String daMax = m.group(7); // range max
      String a2 = m.group(9); // last argument
      String[] aa1 = a1.length() > 0? a1.split(",") : new String[0]; //$NON-NLS-1$
      arguments = new String[aa1.length + (da != null? 1 : 0) + (a2 != null? 1 : 0)];
      System.arraycopy(aa1, 0, arguments, 0, aa1.length);
      if (da == null) {
        dynArgIndex = -1;
        dynArgMin = 0;
        dynArgMax = 0;
      } else {
        dynArgIndex = aa1.length;
        dynArgMin = Integer.parseInt(daMin);
        dynArgMax = Integer.parseInt(daMax);
        arguments[aa1.length] = da;
      }
      if (a2 != null) {
        arguments[arguments.length - 1] = a2;
      }
      description = ""; //$NON-NLS-1$
    }

    /**
     * Construct with most metrics.
     *
     * @param func
     *        The name of the function.
     * @param args
     *        The parameters accepted by this function. These will be parsed crudely using a
     *        comma-delimited string split.
     * @param desc
     *        The internationalized, human-readable description of this function.
     */
    public Function(String func, String args, String desc) {
      name = func;
      arguments = args.split(","); //$NON-NLS-1$
      description = desc;

      dynArgIndex = -1;
      dynArgMin = 0;
      dynArgMax = 0;
    }

  }

}
