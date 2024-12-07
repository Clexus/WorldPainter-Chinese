/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pepsoft.worldpainter.operations;

import org.pepsoft.worldpainter.Dimension;
import org.pepsoft.worldpainter.WorldPainter;
import org.pepsoft.worldpainter.layers.FloodWithLava;
import org.pepsoft.worldpainter.painting.GeneralQueueLinearFloodFiller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

import static org.pepsoft.util.swing.MessageUtils.showWarning;
import static org.pepsoft.worldpainter.Constants.TILE_SIZE;

/**
 *
 * @author pepijn
 */
public class Flood extends MouseOrTabletOperation {
    public Flood(WorldPainter view, boolean floodWithLava) {
        super(floodWithLava ? "Lava" : "Flood", (floodWithLava ? "\u5CA9\u6D46" : "\u6C34")+"\u586B\u5145\u5DE5\u5177-\u7528" + (floodWithLava ? "\u5CA9\u6D46" : "\u6C34") +"\u586B\u5145\u5730\u5F62\uFF0C\u4E0D\u66FF\u6362\u8868\u9762\u65B9\u5757",
                view,
                "operation.flood." + (floodWithLava ? "lava" : "water"),
                floodWithLava ? "flood_with_lava" : "flood");
        this.floodWithLava = floodWithLava;
        optionsPanel = floodWithLava
                ? new StandardOptionsPanel("\u5CA9\u6D46\u586B\u5145\u5DE5\u5177", "<ul><li>\u5DE6\u952E\u70B9\u51FB\u65E0\u6DB2\u4F53\u5730\u5F62\u4F7F\u7528\u5CA9\u6D46\u586B\u5145\u8BE5\u533A\u57DF\n" +
                "<li>\u5DE6\u952E\u70B9\u51FB\u5CA9\u6D46\u63D0\u9AD8\u5176\u9AD8\u5EA6\u4E00\u683C\n" +
                "<li>\u53F3\u952E\u70B9\u51FB\u5CA9\u6D46\u964D\u4F4E\u5176\u9AD8\u5EA6\u4E00\u683C\n" +
                "<li>\u70B9\u51FB\u6C34\u4F7F\u5176\u53D8\u4E3A\u5CA9\u6D46\n" +
                "</ul>")
                : new StandardOptionsPanel("\u6C34\u586B\u5145\u5DE5\u5177", "<ul><li>\u5DE6\u952E\u70B9\u51FB\u65E0\u6DB2\u4F53\u5730\u5F62\u4F7F\u7528\u6C34\u586B\u5145\u8BE5\u533A\u57DF\n" +
                "<li>\u5DE6\u952E\u70B9\u51FB\u6C34\u63D0\u9AD8\u5176\u9AD8\u5EA6\u4E00\u683C\n" +
                "<li>\u53F3\u952E\u70B9\u51FB\u6C34\u964D\u4F4E\u5176\u9AD8\u5EA6\u4E00\u683C\n" +
                "<li>\u70B9\u51FB\u5CA9\u6D46\u4F7F\u5176\u53D8\u4E3A\u6C34\n" +
                "</ul>");
    }

    @Override
    protected void tick(int centreX, int centreY, boolean inverse, boolean first, float dynamicLevel) {
        final Dimension dimension = getDimension();
        if (dimension == null) {
            // Probably some kind of race condition
            return;
        }

        // We have seen in the wild that this sometimes gets called recursively (perhaps someone clicks to flood more
        // than once and then it takes more than two seconds so it is continued in the background and event queue
        // processing is resumed?), which causes errors, so just ignore it if we are already flooding.
        if (alreadyFlooding) {
            logger.debug("Flood operation already in progress; ignoring repeated invocation");
            return;
        }
        alreadyFlooding = true;
        try {
            final Rectangle dimensionBounds = new Rectangle(dimension.getLowestX() * TILE_SIZE, dimension.getLowestY() * TILE_SIZE, dimension.getWidth() * TILE_SIZE, dimension.getHeight() * TILE_SIZE);
            final int terrainHeight = dimension.getIntHeightAt(centreX, centreY);
            if (terrainHeight == Integer.MIN_VALUE) {
                // Not on a tile
                return;
            }
            final int waterLevel = dimension.getWaterLevelAt(centreX, centreY);
            final boolean fluidPresent = waterLevel > terrainHeight;
            if (inverse && (! fluidPresent)) {
                // No point lowering the water level if there is no water...
                return;
            }
            final GeneralQueueLinearFloodFiller.FillMethod fillMethod;
            if (fluidPresent && (floodWithLava != dimension.getBitLayerValueAt(FloodWithLava.INSTANCE, centreX, centreY))) {
                // There is fluid present of a different type; don't change the
                // height, just change the type
                if (floodWithLava) {
                    fillMethod = new FloodFillMethod("\u5C06\u6C34\u8F6C\u6362\u4E3A\u5CA9\u6D46", dimensionBounds) {
                        @Override public boolean isBoundary(int x, int y) {
                            final int height = dimension.getIntHeightAt(x, y);
                            return (height == Integer.MIN_VALUE) // Not on a tile
                                    || (dimension.getWaterLevelAt(x, y) <= height) // Not flooded
                                    || dimension.getBitLayerValueAt(FloodWithLava.INSTANCE, x, y); // Not water
                        }

                        @Override public void fill(int x, int y) {
                            dimension.setBitLayerValueAt(FloodWithLava.INSTANCE, x, y, true);
                        }
                    };
                } else {
                    fillMethod = new FloodFillMethod("\u5C06\u5CA9\u6D46\u8F6C\u6362\u4E3A\u6C34r", dimensionBounds) {
                        @Override public boolean isBoundary(int x, int y) {
                            final int height = dimension.getIntHeightAt(x, y);
                            return (height == Integer.MIN_VALUE) // Not on a tile
                                    || (dimension.getWaterLevelAt(x, y) <= height) // Not flooded
                                    || (! dimension.getBitLayerValueAt(FloodWithLava.INSTANCE, x, y)); // Not lava
                        }

                        @Override public void fill(int x, int y) {
                            dimension.setBitLayerValueAt(FloodWithLava.INSTANCE, x, y, false);
                        }
                    };
                }
            } else {
                final int height = Math.max(terrainHeight, waterLevel);
                if (inverse ? (height <= dimension.getMinHeight()) : (height >= (dimension.getMaxHeight() - 1))) {
                    // Already at the lowest or highest possible point
                    return;
                }
                final int floodToHeight = inverse ? (height - 1): (height + 1);
                if (inverse) {
                    if (floodWithLava) {
                        fillMethod = new FloodFillMethod("\u964D\u4F4E\u5CA9\u6D46\u9AD8\u5EA6", dimensionBounds) {
                            @Override public boolean isBoundary(int x, int y) {
                                final int height = dimension.getIntHeightAt(x, y);
                                return (height == Integer.MIN_VALUE) // Not on a tile
                                        || (dimension.getWaterLevelAt(x, y) <= height) // Not flooded
                                        || (dimension.getWaterLevelAt(x, y) <= floodToHeight); // Already at the required level or lower
                            }

                            @Override public void fill(int x, int y) {
                                dimension.setWaterLevelAt(x, y, floodToHeight);
                                dimension.setBitLayerValueAt(FloodWithLava.INSTANCE, x, y, true);
                            }
                        };
                    } else {
                        fillMethod = new FloodFillMethod("\u964D\u4F4E\u6C34\u9AD8\u5EA6", dimensionBounds) {
                            @Override public boolean isBoundary(int x, int y) {
                                final int height = dimension.getIntHeightAt(x, y);
                                return (height == Integer.MIN_VALUE) // Not on a tile
                                        || (dimension.getWaterLevelAt(x, y) <= height) // Not flooded
                                        || (dimension.getWaterLevelAt(x, y) <= floodToHeight); // Already at the required level or lower
                            }

                            @Override public void fill(int x, int y) {
                                dimension.setWaterLevelAt(x, y, floodToHeight);
                                dimension.setBitLayerValueAt(FloodWithLava.INSTANCE, x, y, false);
                            }
                        };
                    }
                } else {
                    if (floodWithLava) {
                        fillMethod = new FloodFillMethod(fluidPresent ? "\u63D0\u9AD8\u5CA9\u6D46\u9AD8\u5EA6" : "\u4F7F\u7528\u5CA9\u6D46\u586B\u5145", dimensionBounds) {
                            @Override public boolean isBoundary(int x, int y) {
                                final int height = dimension.getIntHeightAt(x, y), waterLevel = dimension.getWaterLevelAt(x, y);
                                return (height == Integer.MIN_VALUE) // Not on a tile
                                        || (height >= floodToHeight) // Higher land encountered
                                        || (waterLevel >= floodToHeight); // Already at the required level or lower
                            }

                            @Override public void fill(int x, int y) {
                                dimension.setWaterLevelAt(x, y, floodToHeight);
                                dimension.setBitLayerValueAt(FloodWithLava.INSTANCE, x, y, true);
                            }
                        };
                    } else {
                        fillMethod = new FloodFillMethod(fluidPresent ? "\u63D0\u9AD8\u6C34\u9AD8\u5EA6" : "\u4F7F\u7528\u6C34\u586B\u5145", dimensionBounds) {
                            @Override public boolean isBoundary(int x, int y) {
                                final int height = dimension.getIntHeightAt(x, y), waterLevel = dimension.getWaterLevelAt(x, y);
                                return (height == Integer.MIN_VALUE) // Not on a tile
                                        || (height >= floodToHeight) // Higher land encountered
                                        || (waterLevel >= floodToHeight); // Already at the required level or higher
                            }

                            @Override public void fill(int x, int y) {
                                dimension.setWaterLevelAt(x, y, floodToHeight);
                                dimension.setBitLayerValueAt(FloodWithLava.INSTANCE, x, y, false);
                            }
                        };
                    }
                }
            }
            synchronized (dimension) {
                dimension.setEventsInhibited(true);
            }
            try {
                synchronized (dimension) {
                    dimension.rememberChanges();
                }
                final GeneralQueueLinearFloodFiller flooder = new GeneralQueueLinearFloodFiller(fillMethod);
                try {
                    if (! flooder.floodFill(centreX, centreY, SwingUtilities.getWindowAncestor(getView()))) {
                        // Cancelled by user
                        synchronized (dimension) {
                            if (dimension.undoChanges()) {
                                dimension.clearRedo();
                                dimension.armSavePoint();
                            }
                        }
                        return;
                    }
                    if (flooder.isBoundsHit()) {
                        showWarning(getView(), "\u9700\u8981\u586B\u5145\u7684\u533A\u57DF\u8FC7\u5927\uFF0C\u53EF\u80FD\u65E0\u6CD5\u88AB\u5B8C\u5168\u586B\u5145.", "\u533A\u57DF\u8FC7\u5927");
                    }
                } catch (IndexOutOfBoundsException e) {
                    // This most likely indicates that the area being flooded was too large
                    synchronized (dimension) {
                        if (dimension.undoChanges()) {
                            dimension.clearRedo();
                            dimension.armSavePoint();
                        }
                    }
                    JOptionPane.showMessageDialog(getView(), "\u9700\u8981\u586B\u5145\u7684\u533A\u57DF\u8FC7\u5927\u6216\u8FC7\u4E8E\u590D\u6742\uFF0C\u8BF7\u5C1D\u8BD5\u4E00\u4E2A\u66F4\u5C0F\u7684\u533A\u57DF.", "\u533A\u57DF\u8FC7\u5927", JOptionPane.ERROR_MESSAGE);
                }
            } finally {
                synchronized (dimension) {
                    dimension.setEventsInhibited(false);
                }
            }
        } finally {
            alreadyFlooding = false;
        }
    }

    @Override
    public JPanel getOptionsPanel() {
        return optionsPanel;
    }

    private boolean alreadyFlooding;

    private final boolean floodWithLava;
    private final StandardOptionsPanel optionsPanel;

    private static final Logger logger = LoggerFactory.getLogger(Flood.class);

    static abstract class FloodFillMethod implements GeneralQueueLinearFloodFiller.FillMethod {
        protected FloodFillMethod(String description, Rectangle bounds) {
            this.description = description;
            this.bounds = bounds;
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public Rectangle getBounds() {
            return bounds;
        }

        private final String description;
        private final Rectangle bounds;
    }
}