/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pepsoft.worldpainter.brushes;

import org.pepsoft.util.ObjectUtils;

/**
 * An object which calculates brush strengths for simple mathematical brushes,
 * which must be symmetric in both the x and y axes (so that only one quadrant
 * of the brush needs to be calculated).
 *
 * @author pepijn
 */
public abstract class SymmetricBrush extends AbstractBrush {
    public SymmetricBrush(String name, BrushShape brushShape, boolean rotationallySymmetric) {
        super(name);
        this.brushShape = brushShape;
        this.rotationallySymmetric = rotationallySymmetric;
        cacheStrengths();
    }

    @Override
    public BrushShape getBrushShape() {
        return brushShape;
    }

    @Override
    public int getRadius() {
        return radius;
    }

    @Override
    public void setRadius(int radius) {
        if (radius != this.radius) {
            this.radius = radius;
            cacheStrengths();
        }
    }

    @Override
    public float getLevel() {
        return level;
    }

    @Override
    public void setLevel(float level) {
        if (level != this.level) {
            this.level = level;
            cacheStrengths();
        }
    }

    public final boolean isRotationallySymmetric() {
        return rotationallySymmetric;
    }

    @Override
    public final float getStrength(int dx, int dy) {
        return strengthCache[Math.abs(dx)][Math.abs(dy)];
    }
    
    @Override
    public final float getFullStrength(int dx, int dy) {
        return fullStrengthCache[Math.abs(dx)][Math.abs(dy)];
    }
    
    @Override
    public String toString() {
        return getName() + " (\u534A\u5F84=" + radius + ", \u7B14\u5237\u5F62\u72B6=" + brushShape + ", \u7EA7\u522B=" + level + ')';
    }

    @Override
    public SymmetricBrush clone() {
        SymmetricBrush clone = (SymmetricBrush) super.clone();
        if (strengthCache != null) {
            clone.strengthCache = ObjectUtils.clone(strengthCache);
        }
        if (fullStrengthCache != null) {
            if (fullStrengthCache == strengthCache) {
                clone.fullStrengthCache = clone.strengthCache;
            } else {
                clone.fullStrengthCache = ObjectUtils.clone(fullStrengthCache);
            }
        }
        return clone;
    }

    protected abstract float calcStrength(int dx, int dy);

    private void cacheStrengths() {
        if ((radius != cachedRadius) || (level != cachedLevel)) {
            // No need to allocate a new array if there is one already and the
            // data will fit in it:
            if ((strengthCache == null) || (strengthCache.length < (radius + 1))) {
                strengthCache = new float[radius + 1][radius + 1];
            }
            // Only update the full strength cache if necessary
            if (radius != cachedRadius) {
                // No need to allocate a new array if there is one already and the
                // data will fit in it:
                if ((fullStrengthCache == null) || (fullStrengthCache.length < (radius + 1))) {
                    fullStrengthCache = new float[radius + 1][radius + 1];
                }
                for (int dx = 0; dx <= radius; dx++) {
                    for (int dy = 0; dy <= radius; dy++) {
                        float strength = calcStrength(dx, dy);
                        strengthCache[dx][dy] = strength * level;
                        fullStrengthCache[dx][dy] = strength;
                    }
                }
            } else {
                for (int dx = 0; dx <= radius; dx++) {
                    for (int dy = 0; dy <= radius; dy++) {
                        strengthCache[dx][dy] = calcStrength(dx, dy) * level;
                    }
                }
            }
            cachedRadius = radius;
            cachedLevel = level;
        }
    }

    protected final boolean rotationallySymmetric;
    protected final BrushShape brushShape;

    private int radius, cachedRadius = -1;
    private float[][] strengthCache, fullStrengthCache;
    private float level = 1.0f, cachedLevel = -1.0f;

    public static final SymmetricBrush CONSTANT_CIRCLE = new RadialBrush("\u5E38\u91CF\u5706", BrushShape.CIRCLE, true) {
        @Override
        protected float calcStrength(float dr) {
            return 1.0f;
        }
    };

    public static final SymmetricBrush CONSTANT_SQUARE = new RadialBrush("\u5E38\u91CF\u65B9\u5F62", BrushShape.SQUARE, false) {
        @Override
        protected float calcStrength(float dr) {
            return 1.0f;
        }
    };

    public static final SymmetricBrush LINEAR_CIRCLE = new RadialBrush("\u7EBF\u6027\u5706", BrushShape.CIRCLE, true) {
        @Override
        protected float calcStrength(float dr) {
            return 1.0f - dr;
        }
    };

    public static final SymmetricBrush LINEAR_SQUARE = new RadialBrush("\u7EBF\u6027\u65B9\u5F62", BrushShape.SQUARE, false) {
        @Override
        protected float calcStrength(float dr) {
            return 1.0f - dr;
        }
    };

    public static final SymmetricBrush COSINE_CIRCLE = new RadialBrush("\u6B63\u5F26\u5706", BrushShape.CIRCLE, true) {
        @Override
        protected float calcStrength(float dr) {
            return (float) Math.cos(dr * Math.PI) / 2 + 0.5f;
        }
    };

    public static final SymmetricBrush COSINE_SQUARE = new RadialBrush("\u6B63\u5F26\u65B9\u5F62", BrushShape.SQUARE, false) {
        @Override
        protected float calcStrength(float dr) {
            return (float) Math.cos(dr * Math.PI) / 2 + 0.5f;
        }
    };

    public static final SymmetricBrush PLATEAU_CIRCLE = new RadialBrush("\u666E\u62C9\u6258\u5706", BrushShape.CIRCLE, true) {
        @Override
        protected float calcStrength(float dr) {
            if (dr <= 0.5f) {
                return 1.0f;
            } else {
                return (float) Math.cos((dr - 0.5f) * TWO_PI) / 2 + 0.5f;
            }
        }
    };

    public static final SymmetricBrush PLATEAU_SQUARE = new RadialBrush("\u666E\u62C9\u6258\u65B9\u5F62", BrushShape.SQUARE, false) {
        @Override
        protected float calcStrength(float dr) {
            if (dr <= 0.5f) {
                return 1.0f;
            } else {
                return (float) Math.cos((dr - 0.5f) * TWO_PI) / 2 + 0.5f;
            }
        }
    };

    public static final SymmetricBrush SPIKE_CIRCLE = new RadialBrush("\u5C16\u523A\u5706", BrushShape.CIRCLE, true) {
        @Override
        protected float calcStrength(float dr) {
            return (1.0f - dr) * (1.0f - dr);
        }
    };

    public static final SymmetricBrush SPIKE_SQUARE = new RadialBrush("\u5C16\u523A\u65B9\u5F62", BrushShape.SQUARE, false) {
        @Override
        protected float calcStrength(float dr) {
            return (1.0f - dr) * (1.0f - dr);
        }
    };

    public static final SymmetricBrush DOME_CIRCLE = new RadialBrush("\u7A79\u9876\u5706", BrushShape.CIRCLE, true) {
        @Override
        protected float calcStrength(float dr) {
            return (float) Math.sqrt(1 - dr * dr);
        }
    };

    public static final SymmetricBrush DOME_SQUARE = new RadialBrush("\u7A79\u9876\u65B9\u5F62", BrushShape.SQUARE, false) {
        @Override
        protected float calcStrength(float dr) {
            return (float) Math.sqrt(1 - dr * dr);
        }
    };
    
    private static final float TWO_PI = (float) (Math.PI * 2);
}