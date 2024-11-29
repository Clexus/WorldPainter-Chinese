/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pepsoft.worldpainter.operations;

import org.pepsoft.util.PerlinNoise;
import org.pepsoft.worldpainter.Dimension;
import org.pepsoft.worldpainter.WorldPainter;
import org.pepsoft.worldpainter.brushes.Brush;

import javax.swing.*;

import static org.pepsoft.worldpainter.Constants.MEDIUM_BLOBS;

/**
 *
 * @author pepijn
 */
public class RaiseMountain extends RadiusOperation {
    public RaiseMountain(WorldPainter view) {
        super("Raise Mountain", "\u5347\u5C71\u5DE5\u5177-\u7531\u5730\u8868\u5347\u8D77\u4E00\u5EA7\u5C71", view, 100, "operation.raiseMountain", "mountain");
        options = new TerrainShapingOptions<>();
        options.setApplyTheme(true); // This has historically been the default for this operation
        optionsPanel = new TerrainShapingOptionsPanel("\u5347\u5C71\u5DE5\u5177", "<ul><li>\u5DE6\u952E\u70B9\u51FB\u4EE5\u6309\u7167\u753B\u7B14\u7684\u5F62\u72B6\u5347\u8D77\u4E00\u5EA7\u5C71\uFF0C\u5C71\u7684\u5E95\u90E8\u4F4D\u4E8E\u57FA\u5CA9\u5C42\u3002<li>\u53F3\u952E\u70B9\u51FB\u4EE5\u6316\u6398\u4E00\u4E2A\u5F62\u72B6\u4E0E\u753B\u7B14\u76F8\u540C\u7684\u5751\u6D1E\uFF0C\u5751\u6D1E\u7684\u5E95\u90E8\u4F4D\u4E8E\u5EFA\u9020\u9AD8\u5EA6\u3002</ul>", options);
    }

    @Override
    public JPanel getOptionsPanel() {
        return optionsPanel;
    }

    @Override
    protected void tick(int centreX, int centreY, boolean inverse, boolean first, float dynamicLevel) {
        final Dimension dimension = getDimension();
        if (dimension == null) {
            // Probably some kind of race condition
            return;
        }
        final float adjustment = (float) Math.pow(getLevel() * dynamicLevel * 2, 2.0);
        final int minZ = dimension.getMinHeight(), maxRange = dimension.getMaxHeight() - 1 - minZ;
        float peakHeight = dimension.getHeightAt(centreX + peakDX, centreY + peakDY) - minZ + (inverse ? -adjustment : adjustment);
        if (peakHeight < 0) {
            peakHeight = 0;
        } else if (peakHeight > maxRange) {
            peakHeight = maxRange;
        }
        dimension.setEventsInhibited(true);
        try {
            final int radius = getEffectiveRadius();
            final boolean applyTheme = options.isApplyTheme();
            for (int x = centreX - radius; x <= centreX + radius; x++) {
                for (int y = centreY - radius; y <= centreY + radius; y++) {
                    final float currentHeight = dimension.getHeightAt(x, y);
                    final float targetHeight = getTargetHeight(minZ, maxRange, centreX, centreY, x, y, peakHeight, inverse);
                    if (inverse ? (targetHeight < currentHeight) : (targetHeight > currentHeight)) {
//                        float strength = calcStrength(centerX, centerY, x, y);
//                        float newHeight = strength * targetHeight  + (1f - strength) * currentHeight;
                        dimension.setHeightAt(x, y, targetHeight);
                        if (applyTheme) {
                            dimension.applyTheme(x, y);
                        }
                    }
                }
            }
        } finally {
            dimension.setEventsInhibited(false);
        }
    }
    
    @Override
    protected final void brushChanged(Brush brush) {
        super.brushChanged(brush);
        if (brush == null) {
            return;
        }

        // Some calculations to support brushes where the centre point is not
        // the brightest point and/or where the brightest point is less than 1.0
        final int radius = getEffectiveRadius();
        float strength = brush.getFullStrength(0, 0);
        if (strength == 1.0f) {
            peakDX = 0;
            peakDY = 0;
            peakFactor = 1.0f;
//            System.out.println("Peak: 1.0 @ " + peakDX + ", " + peakDY);
            return;
        }
        float highestStrength = 0.0f;
        for (int r = 1; r <= radius; r++) {
            for (int i = -r + 1; i <= r; i++) {
                strength = brush.getFullStrength(i, -r);
                if (strength > highestStrength) {
                    peakDX = i;
                    peakDY = -r;
                    peakFactor = 1.0f / strength;
                    highestStrength = strength;
                    if (strength == 1.0f) {
//                        System.out.println("Peak: 1.0 @ " + peakDX + ", " + peakDY);
                        return;
                    }
                }
                strength = brush.getFullStrength(r, i);
                if (strength > highestStrength) {
                    peakDX = r;
                    peakDY = i;
                    peakFactor = 1.0f / strength;
                    highestStrength = strength;
                    if (strength == 1.0f) {
//                        System.out.println("Peak: 1.0 @ " + peakDX + ", " + peakDY);
                        return;
                    }
                }
                strength = brush.getFullStrength(-i, r);
                if (strength > highestStrength) {
                    peakDX = -i;
                    peakDY = r;
                    peakFactor = 1.0f / strength;
                    highestStrength = strength;
                    if (strength == 1.0f) {
//                        System.out.println("Peak: 1.0 @ " + peakDX + ", " + peakDY);
                        return;
                    }
                }
                strength = brush.getFullStrength(-r, -i);
                if (strength > highestStrength) {
                    peakDX = -r;
                    peakDY = -i;
                    peakFactor = 1.0f / strength;
                    highestStrength = strength;
                    if (strength == 1.0f) {
//                        System.out.println("Peak: 1.0 @ " + peakDX + ", " + peakDY);
                        return;
                    }
                }
            }
        }
//        System.out.println("Peak: " + highestStrength + " @ " + peakDX + ", " + peakDY);
    }

    /**
     * Calculate the target height for the mountain at a particular location. Note that {@code peakHeight} is the
     * absolute height above bedrock (not above z == 0) of the peak.
     */
    private float getTargetHeight(int minZ, int maxRange, int centerX, int centerY, int x, int y, float peakHeight, boolean undo) {
        return (undo
            ? Math.max(maxRange - (maxRange - peakHeight) * peakFactor * getNoisyStrength(x, y, getBrush().getFullStrength(x - centerX, y - centerY)), 0)
            : Math.min(peakHeight * peakFactor * getNoisyStrength(x, y, getBrush().getFullStrength(x - centerX, y - centerY)), maxRange)) + minZ;
    }
    
    private float getNoisyStrength(int x, int y, float strength) {
        float allowableNoiseRange = (0.5f - Math.abs(strength - 0.5f)) / 5;
        float noise = perlinNoise.getPerlinNoise(x / MEDIUM_BLOBS, y / MEDIUM_BLOBS);
        strength = strength + noise * allowableNoiseRange * strength;
        if (strength < 0.0) {
            return 0.0f;
        } else if (strength > 1.0) {
            return 1.0f;
        } else {
            return strength;
        }
    }
    
    private final PerlinNoise perlinNoise = new PerlinNoise(67);
    private final TerrainShapingOptions<RaiseMountain> options;
    private final TerrainShapingOptionsPanel optionsPanel;
    private int peakDX, peakDY;
    private float peakFactor;
}