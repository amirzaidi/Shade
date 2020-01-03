/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.android.launcher3.uioverrides.dynamicui;

import android.graphics.Color;
import android.util.Log;

import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

/**
 * Helper class to process legacy (Holo) notifications to make them look like material notifications.
 */
public class ContrastColorUtil {
    private static final String TAG = "ContrastColorUtil";

    public static double calculateContrast(int foregroundColor, int backgroundColor) {
        return ColorUtilsFromCompat.calculateContrast(foregroundColor, backgroundColor);
    }

    /**
     * Framework copy of functions needed from android.support.v4.graphics.ColorUtils.
     */
    private static class ColorUtilsFromCompat {
        private static final ThreadLocal<double[]> TEMP_ARRAY = new ThreadLocal<>();

        private ColorUtilsFromCompat() {}

        /**
         * Composite two potentially translucent colors over each other and returns the result.
         */
        public static int compositeColors(@ColorInt int foreground, @ColorInt int background) {
            int bgAlpha = Color.alpha(background);
            int fgAlpha = Color.alpha(foreground);
            int a = compositeAlpha(fgAlpha, bgAlpha);

            int r = compositeComponent(Color.red(foreground), fgAlpha,
                    Color.red(background), bgAlpha, a);
            int g = compositeComponent(Color.green(foreground), fgAlpha,
                    Color.green(background), bgAlpha, a);
            int b = compositeComponent(Color.blue(foreground), fgAlpha,
                    Color.blue(background), bgAlpha, a);

            return Color.argb(a, r, g, b);
        }

        private static int compositeAlpha(int foregroundAlpha, int backgroundAlpha) {
            return 0xFF - (((0xFF - backgroundAlpha) * (0xFF - foregroundAlpha)) / 0xFF);
        }

        private static int compositeComponent(int fgC, int fgA, int bgC, int bgA, int a) {
            if (a == 0) return 0;
            return ((0xFF * fgC * fgA) + (bgC * bgA * (0xFF - fgA))) / (a * 0xFF);
        }

        /**
         * Returns the luminance of a color as a float between {@code 0.0} and {@code 1.0}.
         * <p>Defined as the Y component in the XYZ representation of {@code color}.</p>
         */
        @FloatRange(from = 0.0, to = 1.0)
        public static double calculateLuminance(@ColorInt int color) {
            final double[] result = getTempDouble3Array();
            colorToXYZ(color, result);
            // Luminance is the Y component
            return result[1] / 100;
        }

        /**
         * Returns the contrast ratio between {@code foreground} and {@code background}.
         * {@code background} must be opaque.
         * <p>
         * Formula defined
         * <a href="http://www.w3.org/TR/2008/REC-WCAG20-20081211/#contrast-ratiodef">here</a>.
         */
        public static double calculateContrast(@ColorInt int foreground, @ColorInt int background) {
            if (Color.alpha(background) != 255) {
                Log.wtf(TAG, "background can not be translucent: #"
                        + Integer.toHexString(background));
            }
            if (Color.alpha(foreground) < 255) {
                // If the foreground is translucent, composite the foreground over the background
                foreground = compositeColors(foreground, background);
            }

            final double luminance1 = calculateLuminance(foreground) + 0.05;
            final double luminance2 = calculateLuminance(background) + 0.05;

            // Now return the lighter luminance divided by the darker luminance
            return Math.max(luminance1, luminance2) / Math.min(luminance1, luminance2);
        }

        /**
         * Convert the ARGB color to it's CIE XYZ representative components.
         *
         * <p>The resulting XYZ representation will use the D65 illuminant and the CIE
         * 2° Standard Observer (1931).</p>
         *
         * <ul>
         * <li>outXyz[0] is X [0 ...95.047)</li>
         * <li>outXyz[1] is Y [0...100)</li>
         * <li>outXyz[2] is Z [0...108.883)</li>
         * </ul>
         *
         * @param color  the ARGB color to convert. The alpha component is ignored
         * @param outXyz 3-element array which holds the resulting LAB components
         */
        public static void colorToXYZ(@ColorInt int color, @NonNull double[] outXyz) {
            RGBToXYZ(Color.red(color), Color.green(color), Color.blue(color), outXyz);
        }

        /**
         * Convert RGB components to it's CIE XYZ representative components.
         *
         * <p>The resulting XYZ representation will use the D65 illuminant and the CIE
         * 2° Standard Observer (1931).</p>
         *
         * <ul>
         * <li>outXyz[0] is X [0 ...95.047)</li>
         * <li>outXyz[1] is Y [0...100)</li>
         * <li>outXyz[2] is Z [0...108.883)</li>
         * </ul>
         *
         * @param r      red component value [0..255]
         * @param g      green component value [0..255]
         * @param b      blue component value [0..255]
         * @param outXyz 3-element array which holds the resulting XYZ components
         */
        public static void RGBToXYZ(@IntRange(from = 0x0, to = 0xFF) int r,
                                    @IntRange(from = 0x0, to = 0xFF) int g, @IntRange(from = 0x0, to = 0xFF) int b,
                                    @NonNull double[] outXyz) {
            if (outXyz.length != 3) {
                throw new IllegalArgumentException("outXyz must have a length of 3.");
            }

            double sr = r / 255.0;
            sr = sr < 0.04045 ? sr / 12.92 : Math.pow((sr + 0.055) / 1.055, 2.4);
            double sg = g / 255.0;
            sg = sg < 0.04045 ? sg / 12.92 : Math.pow((sg + 0.055) / 1.055, 2.4);
            double sb = b / 255.0;
            sb = sb < 0.04045 ? sb / 12.92 : Math.pow((sb + 0.055) / 1.055, 2.4);

            outXyz[0] = 100 * (sr * 0.4124 + sg * 0.3576 + sb * 0.1805);
            outXyz[1] = 100 * (sr * 0.2126 + sg * 0.7152 + sb * 0.0722);
            outXyz[2] = 100 * (sr * 0.0193 + sg * 0.1192 + sb * 0.9505);
        }

        public static double[] getTempDouble3Array() {
            double[] result = TEMP_ARRAY.get();
            if (result == null) {
                result = new double[3];
                TEMP_ARRAY.set(result);
            }
            return result;
        }
    }
}
