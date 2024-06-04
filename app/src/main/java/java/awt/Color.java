/*
 * This program is part of the OpenLMIS logistics management information
 * system platform software.
 *
 * Copyright © 2015 ThoughtWorks, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should
 * have received a copy of the GNU Affero General Public License along with
 * this program. If not, see http://www.gnu.org/licenses. For additional
 * information contact info@OpenLMIS.org
 */

package java.awt;

public class Color implements java.io.Serializable {

  /**
   * The color white.  In the default sRGB space.
   */
  public static final Color white = new Color(255, 255, 255);

  /**
   * The color white.  In the default sRGB space.
   *
   * @since 1.4
   */
  public static final Color WHITE = white;

  /**
   * The color light gray.  In the default sRGB space.
   */
  public static final Color lightGray = new Color(192, 192, 192);

  /**
   * The color light gray.  In the default sRGB space.
   *
   * @since 1.4
   */
  public static final Color LIGHT_GRAY = lightGray;

  /**
   * The color gray.  In the default sRGB space.
   */
  public static final Color gray = new Color(128, 128, 128);

  /**
   * The color gray.  In the default sRGB space.
   *
   * @since 1.4
   */
  public static final Color GRAY = gray;

  /**
   * The color dark gray.  In the default sRGB space.
   */
  public static final Color darkGray = new Color(64, 64, 64);

  /**
   * The color dark gray.  In the default sRGB space.
   *
   * @since 1.4
   */
  public static final Color DARK_GRAY = darkGray;

  /**
   * The color black.  In the default sRGB space.
   */
  public static final Color black = new Color(0, 0, 0);

  /**
   * The color black.  In the default sRGB space.
   *
   * @since 1.4
   */
  public static final Color BLACK = black;

  /**
   * The color red.  In the default sRGB space.
   */
  public static final Color red = new Color(255, 0, 0);

  /**
   * The color red.  In the default sRGB space.
   *
   * @since 1.4
   */
  public static final Color RED = red;

  /**
   * The color pink.  In the default sRGB space.
   */
  public static final Color pink = new Color(255, 175, 175);

  /**
   * The color pink.  In the default sRGB space.
   *
   * @since 1.4
   */
  public static final Color PINK = pink;

  /**
   * The color orange.  In the default sRGB space.
   */
  public static final Color orange = new Color(255, 200, 0);

  /**
   * The color orange.  In the default sRGB space.
   *
   * @since 1.4
   */
  public static final Color ORANGE = orange;

  /**
   * The color yellow.  In the default sRGB space.
   */
  public static final Color yellow = new Color(255, 255, 0);

  /**
   * The color yellow.  In the default sRGB space.
   *
   * @since 1.4
   */
  public static final Color YELLOW = yellow;

  /**
   * The color green.  In the default sRGB space.
   */
  public static final Color green = new Color(0, 255, 0);

  /**
   * The color green.  In the default sRGB space.
   *
   * @since 1.4
   */
  public static final Color GREEN = green;

  /**
   * The color magenta.  In the default sRGB space.
   */
  public static final Color magenta = new Color(255, 0, 255);

  /**
   * The color magenta.  In the default sRGB space.
   *
   * @since 1.4
   */
  public static final Color MAGENTA = magenta;

  /**
   * The color cyan.  In the default sRGB space.
   */
  public static final Color cyan = new Color(0, 255, 255);

  /**
   * The color cyan.  In the default sRGB space.
   *
   * @since 1.4
   */
  public static final Color CYAN = cyan;

  /**
   * The color blue.  In the default sRGB space.
   */
  public static final Color blue = new Color(0, 0, 255);

  /**
   * The color blue.  In the default sRGB space.
   *
   * @since 1.4
   */
  public static final Color BLUE = blue;

  /**
   * The color value.
   *
   * @serial
   * @see #getRGB
   */
  int value;

  public Color(int r, int g, int b) {
    this(r, g, b, 255);
  }

  public Color(int r, int g, int b, int a) {
    value = ((a & 0xFF) << 24)
        | ((r & 0xFF) << 16)
        | ((g & 0xFF) << 8)
        | ((b & 0xFF) << 0);
    testColorValueRange(r, g, b, a);
  }

  private static void testColorValueRange(int r, int g, int b, int a) {
    boolean rangeError = false;
    String badComponentString = "";

    if (a < 0 || a > 255) {
      rangeError = true;
      badComponentString = badComponentString + " Alpha";
    }
    if (r < 0 || r > 255) {
      rangeError = true;
      badComponentString = badComponentString + " Red";
    }
    if (g < 0 || g > 255) {
      rangeError = true;
      badComponentString = badComponentString + " Green";
    }
    if (b < 0 || b > 255) {
      rangeError = true;
      badComponentString = badComponentString + " Blue";
    }
    if (rangeError == true) {
      throw new IllegalArgumentException("Color parameter outside of expected range:"
          + badComponentString);
    }
  }

  public Color(int rgb) {
    value = 0xff000000 | rgb;
  }

  public int getRed() {
    return (getRGB() >> 16) & 0xFF;
  }

  public int getGreen() {
    return (getRGB() >> 8) & 0xFF;
  }

  public int getBlue() {
    return (getRGB() >> 0) & 0xFF;
  }

  public int getAlpha() {
    return (getRGB() >> 24) & 0xff;
  }

  public int getRGB() {
    return value;
  }

  public int hashCode() {
    return value;
  }

  public boolean equals(Object obj) {
    return obj instanceof Color && ((Color) obj).getRGB() == this.getRGB();
  }

  public String toString() {
    return getClass().getName() + "[r=" + getRed() + ",g=" + getGreen() + ",b=" + getBlue() + "]";
  }

}
