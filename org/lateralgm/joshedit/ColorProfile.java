package org.lateralgm.joshedit;

import java.awt.Color;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Class storing a syntax highlighting color profile.
 */
public class ColorProfile implements Iterable<ColorProfile.ColorProfileEntry> {

  /** The name as it appears to the user. This is considered pre-internationalized. */
  final String name;
  final Map<String, ColorProfileEntry> colors;

  /** Construct with a name and contents. A profile is immutable. */
  public ColorProfile(String name, Map<String, ColorProfileEntry> colors) {
    this.name = name;
    this.colors = Collections.unmodifiableMap(colors);
  }

  /** An entry in a {@link ColorProfile}. */
  public static class ColorProfileEntry {
    /** The name as it appears to the user. This should be internationalized. */
    public String nlsName;
    /** The color of the font used to depict members of this entry. */
    public Color color;
    /** The font style, such as Font.BOLD or Font.ITALICS. */
    public int fontStyle;

    /** Construct completely. */
    public ColorProfileEntry(String nlsName, Color color, int transform) {
      super();
      this.nlsName = nlsName;
      this.color = color;
      this.fontStyle = transform;
    }

  }

  /**
   * Create an entry from basic required information in its rawest form.
   * 
   * @param nlsKey
   *        The internationalization key of this entry, to be looked up in the HighlightBlocks
   *        namespace. Example: COMMENT -> HighlightBlocks.COMMENT -> Comment.
   * @param r
   *        The red channel of the color.
   * @param g
   *        The green channel of the color.
   * @param b
   *        The blue channel of the color.
   * @param transform
   *        Font transformation and styling to apply, such as Font.BOLD or Font.ITALICS.
   */
  public static ColorProfileEntry makeEntry(String nlsKey, int r, int g, int b, int transform) {
    return new ColorProfileEntry(translate(nlsKey), new Color(r, g, b), transform);
  }

  /**
   * Create an entry from basic required information in its (nearly) rawest form.
   * 
   * @param nlsKey
   *        The internationalization key of this entry, to be looked up in the HighlightBlocks
   *        namespace. Example: COMMENT -> HighlightBlocks.COMMENT -> Comment.
   * @param color
   *        The color used to depict members of the entry.
   * @param transform
   *        Font transformation and styling to apply, such as Font.BOLD or Font.ITALICS.
   */
  public static ColorProfileEntry makeEntry(String nlsKey, Color color, int transform) {
    return new ColorProfileEntry(translate(nlsKey), color, transform);
  }

  /** Look up an entry by its key name. */
  public ColorProfileEntry get(String key) {
    return colors.get(key);
  }

  /** Retrieve a set of all entry key names. */
  public Set<String> keySet() {
    return colors.keySet();
  }

  /** Retrieve a set of all entries. */
  public Collection<ColorProfileEntry> values() {
    return colors.values();
  }

  /** Retrieve a set of map entries to all color entries by their key name. */
  public Set<Entry<String, ColorProfileEntry>> entrySet() {
    return colors.entrySet();
  }

  @Override
  public Iterator<ColorProfileEntry> iterator() {
    return colors.values().iterator();
  }

  // Private helpers

  private static String translate(String key) {
    return Runner.editorInterface.getString("HighlightBlocks." + key); //$NON-NLS-1$
  }
}
