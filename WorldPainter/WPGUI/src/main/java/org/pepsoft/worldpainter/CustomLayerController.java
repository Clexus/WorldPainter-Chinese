package org.pepsoft.worldpainter;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AtomicDouble;
import com.jidesoft.docking.DockContext;
import com.jidesoft.docking.DockableFrame;
import org.jetbrains.annotations.NotNull;
import org.pepsoft.minecraft.Material;
import org.pepsoft.util.DesktopUtils;
import org.pepsoft.util.IconUtils;
import org.pepsoft.util.MathUtils;
import org.pepsoft.util.swing.BetterJPopupMenu;
import org.pepsoft.worldpainter.importing.CustomItemsTreeModel;
import org.pepsoft.worldpainter.layers.*;
import org.pepsoft.worldpainter.layers.annotation.CustomAnnotationLayerDialog;
import org.pepsoft.worldpainter.layers.groundcover.GroundCoverLayer;
import org.pepsoft.worldpainter.layers.plants.PlantLayer;
import org.pepsoft.worldpainter.layers.pockets.UndergroundPocketsDialog;
import org.pepsoft.worldpainter.layers.pockets.UndergroundPocketsLayer;
import org.pepsoft.worldpainter.layers.tunnel.FloatingLayerDialog;
import org.pepsoft.worldpainter.layers.tunnel.TunnelLayer;
import org.pepsoft.worldpainter.layers.tunnel.TunnelLayerDialog;
import org.pepsoft.worldpainter.operations.PaintOperation;
import org.pepsoft.worldpainter.painting.LayerPaint;
import org.pepsoft.worldpainter.palettes.Palette;
import org.pepsoft.worldpainter.palettes.PaletteManager;
import org.pepsoft.worldpainter.plugins.CustomLayerProvider;
import org.pepsoft.worldpainter.plugins.WPPluginManager;
import org.pepsoft.worldpainter.util.FileFilter;
import org.pepsoft.worldpainter.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.text.MessageFormat;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.zip.GZIPOutputStream;

import static java.awt.Color.*;
import static java.util.Arrays.asList;
import static java.util.Comparator.comparing;
import static javax.swing.JOptionPane.*;
import static org.pepsoft.minecraft.Constants.DEFAULT_WATER_LEVEL;
import static org.pepsoft.util.AwtUtils.doLaterOnEventThread;
import static org.pepsoft.util.DesktopUtils.beep;
import static org.pepsoft.util.swing.MessageUtils.*;
import static org.pepsoft.worldpainter.App.COMMAND_KEY_NAME;
import static org.pepsoft.worldpainter.Constants.TILE_SIZE;
import static org.pepsoft.worldpainter.Constants.TILE_SIZE_BITS;
import static org.pepsoft.worldpainter.Dimension.Role.CAVE_FLOOR;
import static org.pepsoft.worldpainter.Dimension.Role.FLOATING_FLOOR;
import static org.pepsoft.worldpainter.layers.tunnel.TunnelLayer.LayerMode.CAVE;
import static org.pepsoft.worldpainter.layers.tunnel.TunnelLayer.LayerMode.FLOATING;
import static org.pepsoft.worldpainter.layers.tunnel.TunnelLayer.Mode.FIXED_HEIGHT_ABOVE_FLOOR;
import static org.pepsoft.worldpainter.panels.DefaultFilter.buildForDimension;

public class CustomLayerController implements PropertyChangeListener {
    CustomLayerController(App app) {
        this.app = app;
    }

    // PropertyChangeListener

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ((evt.getSource() instanceof Palette) && (evt.getPropertyName().equals("show") || evt.getPropertyName().equals("solo"))) {
            if (evt.getPropertyName().equals("solo") && evt.getNewValue() == Boolean.TRUE) {
                for (Palette palette: paletteManager.getPalettes()) {
                    if ((palette != evt.getSource()) && palette.isSolo()) {
                        palette.setSolo(false);
                    }
                }
            }
            app.updateLayerVisibility();
        }
    }

    public List<Component> createCustomLayerButton(final CustomLayer layer) {
        final List<Component> buttonComponents = app.createLayerButton(layer, '\0');
        final JToggleButton button = (JToggleButton) buttonComponents.get(2);
        button.setToolTipText(button.getToolTipText() + "; \u53F3\u952E\u67E5\u770B\u9009\u9879");
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopup(e);
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopup(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopup(e);
                }
            }

            private void showPopup(MouseEvent e) {
                final JPopupMenu popup = new BetterJPopupMenu();

                JMenuItem menuItem = new JMenuItem(strings.getString("edit") + "...");
                menuItem.addActionListener(e1 -> editCustomLayer(layer));
                popup.add(menuItem);

                final Dimension dimension = app.getDimension();
                if (layer instanceof TunnelLayer) {
                    final TunnelLayer tunnelLayer = (TunnelLayer) layer;
                    final Integer floorDimensionId = tunnelLayer.getFloorDimensionId();
                    if (floorDimensionId != null) {
                        final String shortName, longName;
                        switch (tunnelLayer.getLayerMode()) {
                            case CAVE:
                                shortName = "\u5E95\u5C42\u7EF4\u5EA6";
                                longName = "\u81EA\u5B9A\u4E49\u6D1E\u7A74/\u901A\u9053\u8986\u76D6\u5C42\u5E95\u5C42\u7EF4\u5EA6";
                                break;
                            case FLOATING:
                                shortName = "\u60AC\u6D6E\u7EF4\u5EA6";
                                longName = "\u60AC\u6D6E\u7EF4\u5EA6";
                                break;
                            default:
                                throw new InternalError("Unknown layer mode " + tunnelLayer.getLayerMode());
                        }
                        menuItem = new JMenuItem("\u7F16\u8F91 " + shortName);
                        if (dimension.containsOneOf(tunnelLayer)) {
                            menuItem.addActionListener(e1 -> {
                                final Point viewPosition = app.view.getViewCentreInWorldCoords();
                                final Dimension floorDimension = tunnelLayer.updateFloorDimension(dimension, null);
                                app.setDimension(floorDimension);

                                // Initially we move to the same location as we were on the surface. Then we check
                                // whether the floor dimension is actually visible then. If not, we try to find the
                                // middle and move there
                                // TODO: this might not be helpful if the layer is painted in multiple discontiguous areas
                                app.view.moveTo(viewPosition);
                                int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE, minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
                                final Rectangle visibleArea = app.view.getVisibleArea();
                                boolean floorTileVisible = false;
                                for (Tile tile: floorDimension.getTiles()) {
                                    // Check if the tile is entirely visible. If so, we're done and will
                                    if (visibleArea.contains(tile.getX() << TILE_SIZE_BITS, tile.getY() << TILE_SIZE_BITS)
                                            && visibleArea.contains((tile.getX() << TILE_SIZE_BITS) + TILE_SIZE - 1, (tile.getY() << TILE_SIZE_BITS) + TILE_SIZE - 1)) {
                                        floorTileVisible = true;
                                        break;
                                    }

                                    // Record the extents of the tiles of the floor dimension
                                    if (tile.getX() < minX) {
                                        minX = tile.getX();
                                    }
                                    if (tile.getX() > maxX) {
                                        maxX = tile.getX();
                                    }
                                    if (tile.getY() < minY) {
                                        minY = tile.getY();
                                    }
                                    if (tile.getY() > maxY) {
                                        maxY = tile.getY();
                                    }
                                };
                                if ((! floorTileVisible) && (minX != Integer.MAX_VALUE)) {
                                    app.view.moveTo((((maxX + minX) / 2) << TILE_SIZE_BITS) + (TILE_SIZE / 2),
                                            (((maxY + minY) / 2) << TILE_SIZE_BITS) + (TILE_SIZE / 2));
                                }

                                final Configuration config = Configuration.getInstance();
                                if (! config.isMessageDisplayedCountAtLeast(EDITING_FLOOR_DIMENSION_KEY, 3)) {
                                    doLaterOnEventThread(() -> JOptionPane.showMessageDialog(app,
                                            "\u6309Esc\u7ED3\u675F " + longName + " \u7684\u7F16\u8F91,\n" +
                                                    "\u6216\u4ECE\u201C\u89C6\u56FE\u201D\u83DC\u5355\u4E2D(\u6216\u4F7F\u7528 " + COMMAND_KEY_NAME + "+U \u5FEB\u6377\u952E)\u5207\u6362\u5230\u4E3B\u4E16\u754C\u89C6\u56FE", "\u7F16\u8F91" + longName, JOptionPane.INFORMATION_MESSAGE));
                                    config.setMessageDisplayed(EDITING_FLOOR_DIMENSION_KEY);
                                }

                                final JLabel label = new JLabel("<html><font size='+1'>\u6309Esc\u653E\u5F03" + longName + "\u7684\u7F16\u8F91.</font></html>");
                                label.setBorder(new CompoundBorder(new LineBorder(BLACK), new EmptyBorder(5, 5, 5, 5)));
                                app.pushGlassPaneComponent(label);
                            });
                        } else {
                            menuItem.setEnabled(false);
                        }
                        popup.add(menuItem);
                    }
                }

                menuItem = new JMenuItem("\u67E5\u627E");
                menuItem.addActionListener(e1 -> findLayer(layer));
                popup.add(menuItem);

                menuItem = new JMenuItem("\u590D\u5236...");
                if (layer.isExportableToFile()) {
                    menuItem.addActionListener(e1 -> duplicate());
                } else {
                    menuItem.setEnabled(false);
                    menuItem.setToolTipText("\u8BE5\u8986\u76D6\u5C42\u4E0D\u53EF\u88AB\u590D\u5236.");
                }
                popup.add(menuItem);

                menuItem = new JMenuItem(strings.getString("remove") + "...");
                menuItem.addActionListener(e1 -> remove());
                popup.add(menuItem);

                menuItem = new JMenuItem("\u5BFC\u51FA\u5230\u6587\u4EF6...");
                if (layer.isExportableToFile()) {
                    menuItem.addActionListener(e1 -> exportLayer(layer));
                } else {
                    menuItem.setEnabled(false);
                    menuItem.setToolTipText("\u8BE5\u8986\u76D6\u5C42\u4E0D\u53EF\u5BFC\u51FA\u4E3A\u6587\u4EF6.");
                }
                popup.add(menuItem);

                JMenu paletteMenu = new JMenu("\u79FB\u52A8\u81F3\u753B\u677F");

                for (final Palette palette: paletteManager.getPalettes()) {
                    menuItem = new JMenuItem(palette.getName());
                    menuItem.addActionListener(e1 -> moveLayerToPalette(layer, palette));
                    if (palette.contains(layer)) {
                        menuItem.setEnabled(false);
                    }
                    paletteMenu.add(menuItem);
                }

                menuItem = new JMenuItem("\u65B0\u5EFA\u753B\u677F...");
                menuItem.addActionListener(e1 -> createNewLayerPalette(layer));
                paletteMenu.add(menuItem);

                popup.add(paletteMenu);

                List<Action> actions = layer.getActions();
                if (actions != null) {
                    for (Action action : actions) {
                        action.putValue(CustomLayer.KEY_DIMENSION, dimension);
                        popup.add(new JMenuItem(action));
                    }
                }

                popup.show(button, e.getX(), e.getY());
            }

            private void duplicate() {
                final CustomLayer duplicate = layer.clone();
                duplicate.setName(layer.getName()+"\u7684\u590D\u5236");
                final Object paint = layer.getPaint();
                if (paint instanceof Color) {
                    Color colour = (Color) paint;
                    final float[] hsb = Color.RGBtoHSB(colour.getRed(), colour.getGreen(), colour.getBlue(), null);
                    hsb[0] += 1f / 12;
                    if (hsb[0] > 1f) {
                        hsb[0] -= 1f;
                    }
                    colour = Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
                    duplicate.setPaint(colour);
                }
                AbstractEditLayerDialog<CustomLayer> dialog;
                dialog = createEditLayerDialog(duplicate);
                dialog.setVisible(() -> registerCustomLayer(dialog.getLayer(), true));
            }

            private void remove() {
                if (showConfirmDialog(app, MessageFormat.format(strings.getString("are.you.sure.you.want.to.remove.the.0.layer"), layer.getName()), MessageFormat.format(strings.getString("confirm.0.removal"), layer.getName()), YES_NO_OPTION) == YES_OPTION) {
                    deleteCustomLayer(layer);
                    app.validate(); // Doesn't happen automatically for some reason; Swing bug?
                }
            }
        });
        return buttonComponents;
    }

    public void layerRemoved(CustomLayer layer) {
        app.layerRemoved(layer);
    }

    public List<Component> createPopupMenuButton() {
        final JButton addLayerButton = new JButton(ADD_CUSTOM_LAYER_BUTTON_ICON);
        addLayerButton.setToolTipText(strings.getString("add.a.custom.layer"));
        addLayerButton.setMargin(new Insets(2, 2, 2, 2));
        addLayerButton.addActionListener(e -> {
            if (app.getDimension() == null) {
                beep();
                return;
            }
            // Find out which palette the button is on
            Container parent = addLayerButton.getParent();
            while ((parent != null) && (! (parent instanceof DockableFrame))) {
                parent = parent.getParent();
            }
            if (parent != null) {
                final String nameKey = ((DockableFrame) parent).getKey();
                final String paletteName = nameKey.substring(nameKey.indexOf('.') + 1);
                final JPopupMenu customLayerMenu = createCustomLayerMenu(paletteName);
                customLayerMenu.show(addLayerButton, addLayerButton.getWidth(), 0);
            } else {
                logger.error("Could not find palette add layer button is on");
                beep();
            }
        });
        final List<Component> addLayerButtonPanel = new ArrayList<>(3);
        addLayerButtonPanel.add(new JPanel());
        addLayerButtonPanel.add(new JPanel());
        addLayerButtonPanel.add(addLayerButton);

        return addLayerButtonPanel;
    }

    void registerCustomLayer(final CustomLayer layer, boolean activate) {
        // Add to palette, creating it if necessary
        Palette palette = paletteManager.register(layer);

        // Show the palette if it is not showing yet
        if (palette != null) {
            app.dockingManager.addFrame(palette.getDockableFrame());
            app.dockingManager.dockFrame(palette.getDockableFrame().getKey(), DockContext.DOCK_SIDE_WEST, 3);
            if (activate) {
                app.dockingManager.activateFrame(palette.getDockableFrame().getKey());
            }
            palette.addPropertyChangeListener(this);
        } else {
            app.validate();
        }

        if (activate) {
            paletteManager.activate(layer);
        }
    }

    void unregisterCustomLayer(final CustomLayer layer) {
        // Remove from palette
        Palette palette = paletteManager.unregister(layer);

        // Remove tracked GUI components
        app.layerSoloCheckBoxes.remove(layer);

        // If the palette is now empty, remove it too
        if (palette.isEmpty()) {
            palette.removePropertyChangeListener(this);
            paletteManager.delete(palette);
            app.dockingManager.removeFrame(palette.getDockableFrame().getKey());
        }
    }

    JPopupMenu createCustomLayerMenu(final String paletteName) {
        final World2 world = app.getWorld();

        JPopupMenu customLayerMenu = new BetterJPopupMenu();
        JMenuItem menuItem = new JMenuItem(strings.getString("add.a.custom.object.layer") + "...");
        menuItem.addActionListener(e -> {
            EditLayerDialog<Bo2Layer> dialog = new EditLayerDialog<>(app, world.getPlatform(), Bo2Layer.class);
            dialog.setVisible(() -> {
                Bo2Layer layer = dialog.getLayer();
                if (paletteName != null) {
                    layer.setPalette(paletteName);
                }
                registerCustomLayer(layer, true);
            });
        });
        customLayerMenu.add(menuItem);

        menuItem = new JMenuItem(strings.getString("add.a.custom.ground.cover.layer") + "...");
        menuItem.addActionListener(e -> {
            EditLayerDialog<GroundCoverLayer> dialog = new EditLayerDialog<>(app, world.getPlatform(), GroundCoverLayer.class);
            dialog.setVisible(() -> {
                GroundCoverLayer layer = dialog.getLayer();
                if (paletteName != null) {
                    layer.setPalette(paletteName);
                }
                registerCustomLayer(layer, true);
            });
        });
        customLayerMenu.add(menuItem);

        final Dimension dimension = app.getDimension();
        final Dimension.Anchor anchor = dimension.getAnchor();
        menuItem = new JMenuItem(strings.getString("add.a.custom.underground.pockets.layer") + "...");
        menuItem.addActionListener(e -> {
            UndergroundPocketsDialog dialog = new UndergroundPocketsDialog(app, world.getPlatform(), MixedMaterial.create(world.getPlatform(), Material.IRON_BLOCK), app.getColourScheme(), dimension.getMinHeight(), dimension.getMaxHeight(), world.isExtendedBlockIds());
            dialog.setVisible(() -> {
                UndergroundPocketsLayer layer = dialog.getLayer();
                if (paletteName != null) {
                    layer.setPalette(paletteName);
                }
                registerCustomLayer(layer, true);
            });
        });
        if ((anchor.role == CAVE_FLOOR) || (anchor.role == FLOATING_FLOOR)) { // TODO support more layers in floating dimensions
            menuItem.setEnabled(false);
        }
        customLayerMenu.add(menuItem);

        menuItem = new JMenuItem("\u6DFB\u52A0\u4E00\u4E2A\u81EA\u5B9A\u4E49\u7684\u6D1E\u7A74/\u901A\u9053\u8986\u76D6\u5C42...");
        menuItem.addActionListener(e -> {
            final TunnelLayer layer = new TunnelLayer("\u901A\u9053", CAVE, BLACK, world.getPlatform());
            final int baseHeight, waterLevel;
            final TileFactory tileFactory = dimension.getTileFactory();
            if (tileFactory instanceof HeightMapTileFactory) {
                baseHeight = (int) ((HeightMapTileFactory) tileFactory).getBaseHeight();
                waterLevel = ((HeightMapTileFactory) tileFactory).getWaterHeight();
                layer.setFloodWithLava(((HeightMapTileFactory) tileFactory).isFloodWithLava());
            } else {
                baseHeight = 58;
                waterLevel = DEFAULT_WATER_LEVEL;
            }
            // TODO passing in dimension here is a crude mechanism. It is supposed to be the dimension on which this
            //  layer will be used, but that is impossible to enforce. In practice this will usually be right though
            final TunnelLayerDialog dialog = new TunnelLayerDialog(app, world.getPlatform(), layer, dimension, world.isExtendedBlockIds(), app.getColourScheme(), app.getCustomBiomeManager(), dimension.getMinHeight(), dimension.getMaxHeight(), baseHeight, waterLevel);
            dialog.setVisible(() -> {
                if (paletteName != null) {
                    layer.setPalette(paletteName);
                }
                registerCustomLayer(layer, true);
            });
        });
        if ((anchor.role == CAVE_FLOOR) || (anchor.role == FLOATING_FLOOR)) { // TODO support more layers in floating dimensions
            menuItem.setEnabled(false);
        }
        customLayerMenu.add(menuItem);

        menuItem = new JMenuItem("\u6DFB\u52A0\u4E00\u4E2A\u81EA\u5B9A\u4E49\u7684\u690D\u88AB\u8986\u76D6\u5C42...");
        menuItem.addActionListener(e -> {
            final EditLayerDialog<PlantLayer> dialog = new EditLayerDialog<>(app, world.getPlatform(), PlantLayer.class);
            dialog.setVisible(() -> {
                final PlantLayer layer = dialog.getLayer();
                if (paletteName != null) {
                    layer.setPalette(paletteName);
                }
                registerCustomLayer(layer, true);
            });
        });
        customLayerMenu.add(menuItem);

        menuItem = new JMenuItem("\u6DFB\u52A0\u4E00\u4E2A\u6DF7\u5408\u8986\u76D6\u5C42...");
        menuItem.addActionListener(e -> {
            final EditLayerDialog<CombinedLayer> dialog = new EditLayerDialog<>(app, world.getPlatform(), CombinedLayer.class);
            dialog.setVisible(() -> {
                // TODO: get saved layer
                final CombinedLayer layer = dialog.getLayer();
                if (paletteName != null) {
                    layer.setPalette(paletteName);
                }
                registerCustomLayer(layer, true);
            });
        });
        customLayerMenu.add(menuItem);

        menuItem = new JMenuItem("\u6DFB\u52A0\u4E00\u4E2A\u81EA\u5B9A\u4E49\u7684\u8499\u7248\u8986\u76D6\u5C42...");
        menuItem.addActionListener(e -> {
            final CustomAnnotationLayerDialog dialog = new CustomAnnotationLayerDialog(app, new CustomAnnotationLayer("\u6211\u7684\u81EA\u5B9A\u4E49\u8499\u7248", "\u81EA\u5B9A\u4E49\u8499\u7248\u8986\u76D6\u5C42", YELLOW));
            dialog.setVisible(() -> {
                final CustomAnnotationLayer layer = dialog.getLayer();
                if (paletteName != null) {
                    layer.setPalette(paletteName);
                }
                registerCustomLayer(layer, true);
            });
        });
        customLayerMenu.add(menuItem);

        menuItem = new JMenuItem("[\u9884\u89C8\u529F\u80FD]\u6DFB\u52A0\u4E00\u4E2A\u60AC\u6D6E\u7EF4\u5EA6...");
        menuItem.addActionListener(e -> {
            final TunnelLayer layer = new TunnelLayer("\u60AC\u6D6E\u7EF4\u5EA6", FLOATING, CYAN, world.getPlatform());
            layer.setFloorMode(FIXED_HEIGHT_ABOVE_FLOOR);
            layer.setFloorLevel(16);
            final int baseHeight, waterLevel;
            final TileFactory tileFactory = dimension.getTileFactory();
            if (tileFactory instanceof HeightMapTileFactory) {
                baseHeight = (int) ((HeightMapTileFactory) tileFactory).getBaseHeight();
                waterLevel = ((HeightMapTileFactory) tileFactory).getWaterHeight();
                layer.setFloodWithLava(((HeightMapTileFactory) tileFactory).isFloodWithLava());
            } else {
                baseHeight = 58;
                waterLevel = DEFAULT_WATER_LEVEL;
            }
            // TODO passing in dimension here is a crude mechanism. It is supposed to be the dimension on which this
            //  layer will be used, but that is impossible to enforce. In practice this will usually be right though
            final FloatingLayerDialog dialog = new FloatingLayerDialog(app, world.getPlatform(), layer, dimension, world.isExtendedBlockIds(), app.getColourScheme(), app.getCustomBiomeManager(), dimension.getMinHeight(), dimension.getMaxHeight(), baseHeight, waterLevel);
            dialog.setVisible(() -> {
                if (paletteName != null) {
                    layer.setPalette(paletteName);
                }
                registerCustomLayer(layer, true);
            });
        });
        if ((anchor.role == CAVE_FLOOR) || (anchor.role == FLOATING_FLOOR)) { // TODO support more layers in floating dimensions
            menuItem.setEnabled(false);
        }
        customLayerMenu.add(menuItem);

        List<Class<? extends CustomLayer>> allPluginLayers = new ArrayList<>();
        for (CustomLayerProvider layerProvider: WPPluginManager.getInstance().getPlugins(CustomLayerProvider.class)) {
            allPluginLayers.addAll(layerProvider.getCustomLayers());
        }
        if (! allPluginLayers.isEmpty()) {
            customLayerMenu.addSeparator();

            for (Class<? extends CustomLayer> customLayerClass: allPluginLayers) {
                menuItem = new JMenuItem("\u6DFB\u52A0\u4E00\u4E2A" + customLayerClass.getSimpleName() + "\u8986\u76D6\u5C42..."); // TODO: introduce a proper display name for custom layers
                menuItem.addActionListener(e -> {
                    final EditLayerDialog<CustomLayer> dialog = new EditLayerDialog<>(app, world.getPlatform(), (Class<CustomLayer>) customLayerClass);
                    dialog.setVisible(() -> {
                        // TODO: get saved layer
                        CustomLayer layer = dialog.getLayer();
                        if (paletteName != null) {
                            layer.setPalette(paletteName);
                        }
                        registerCustomLayer(layer, true);
                    });
                });
                customLayerMenu.add(menuItem);
            }

            customLayerMenu.addSeparator();
        }

        menuItem = new JMenu("\u4ECE\u53E6\u4E00\u4E2A\u7EF4\u5EA6\u590D\u5236\u8986\u76D6\u5C42");
        menuItem.setToolTipText("\u8FD9\u5C06\u4F1A\u590D\u5236\u4E00\u4E2A\u8986\u76D6\u5C42\uFF0C\u5B83\u4F1A\u6709\u81EA\u5DF1\u7684ID\u548C\u72EC\u7ACB\u7684\u8BBE\u7F6E\u9879");
        final Function<Layer, Boolean> filter = app.getLayerFilterForCurrentDimension();
        List<JMenuItem> copyMenuItems = getCopyLayerMenuItems((paletteName != null) ? paletteName : "\u81EA\u5B9A\u4E49\u8986\u76D6\u5C42", filter);
        if (! copyMenuItems.isEmpty()) {
            for (JMenuItem copyMenuItem: copyMenuItems) {
                ((JMenu) menuItem).add(copyMenuItem);
            }
        } else {
            menuItem.setEnabled(false);
        }
        customLayerMenu.add(menuItem);

        menuItem = new JMenuItem("\u7531\u6587\u4EF6\u5BFC\u5165\u81EA\u5B9A\u4E49\u8986\u76D6\u5C42...");
        menuItem.addActionListener(e -> app.importLayers(paletteName, filter));
        customLayerMenu.add(menuItem);

        menuItem = new JMenuItem("\u7531\u5176\u4ED6\u4E16\u754C\u5BFC\u5165\u81EA\u5B9A\u4E49\u8986\u76D6\u5C42...");
        menuItem.addActionListener(e -> app.importCustomItemsFromWorld(CustomItemsTreeModel.ItemType.LAYER, filter));
        customLayerMenu.add(menuItem);

        return customLayerMenu;
    }

    void editCustomLayer(CustomLayer layer, Runnable callback) {
        final Object previousPaint = layer.getPaint();
        final float previousOpacity = layer.getOpacity();
        final BufferedImage previousIcon = layer.getIcon();
        final AbstractEditLayerDialog<CustomLayer> dialog = createEditLayerDialog(layer);
        dialog.setVisible(() -> {
            final App.LayerControls layerControls = app.layerControls.get(layer);
            final JComponent control = (layerControls != null) ? layerControls.control : null;
            if (control != null) {
                if (control instanceof AbstractButton) {
                    ((AbstractButton) control).setText(layer.getName());
                }
                control.setToolTipText(layer.getName() + ": " + layer.getDescription() + "; \u53F3\u952E\u67E5\u770B\u9009\u9879");
            }
            final Object newPaint = layer.getPaint();
            final float newOpacity = layer.getOpacity();
            final BufferedImage newIcon = layer.getIcon();
            boolean viewRefreshed = false;
            if ((! Objects.equals(newIcon, previousIcon)) && (control instanceof AbstractButton)) {
                ((AbstractButton) control).setIcon(new ImageIcon(layer.getIcon()));
            }
            if ((! Objects.equals(newPaint, previousPaint)) || (newOpacity != previousOpacity)) {
                app.view.refreshTilesForLayer(layer, false);
                viewRefreshed = true;
            }
            app.getDimension().changed();
            if (layer instanceof CombinedLayer) {
                updateHiddenLayers();
            }
            if ((layer instanceof TunnelLayer) && (! viewRefreshed)) {
                app.view.refreshTilesForLayer(layer, false);
            }
            if (callback != null) {
                callback.run();
            }
        });
    }

    void deleteCustomLayer(CustomLayer layer) {
        final Dimension dimension = app.getDimension();
        if ((app.getActiveOperation() instanceof PaintOperation) && (app.paint instanceof LayerPaint) && (((LayerPaint) app.paint).getLayer() == layer)) {
            app.deselectPaint();
        }
        dimension.setEventsInhibited(true);
        try {
            dimension.clearLayerData(layer);
            if ((layer instanceof TunnelLayer) && (((TunnelLayer) layer).getFloorDimensionId() != null)) {
                final Dimension.Anchor anchor = dimension.getAnchor();
                app.getWorld().removeDimension(new Dimension.Anchor(anchor.dim, (((TunnelLayer) layer).getLayerMode() == CAVE) ? CAVE_FLOOR : FLOATING_FLOOR, anchor.invert, ((TunnelLayer) layer).getFloorDimensionId()));
            }
            dimension.clearUndo();
        } finally {
            dimension.setEventsInhibited(false);
        }
        unregisterCustomLayer(layer);

        boolean visibleLayersChanged = false;
        if (app.getHiddenLayers().contains(layer)) {
            app.hiddenLayers.remove(layer);
            visibleLayersChanged = true;
        }
        if (layer.equals(app.soloLayer)) {
            app.soloLayer = null;
            visibleLayersChanged = true;
        }
        if (layer instanceof LayerContainer) {
            boolean layersUnhidden = false;
            for (Layer subLayer : ((LayerContainer) layer).getLayers()) {
                if ((subLayer instanceof CustomLayer) && ((CustomLayer) subLayer).isHide()) {
                    ((CustomLayer) subLayer).setHide(false);
                    layersUnhidden = true;
                }
            }
            if (layersUnhidden) {
                updateHiddenLayers();
                visibleLayersChanged = false;
            }
        }
        if (visibleLayersChanged) {
            app.updateLayerVisibility();
        }
    }

    void deleteUnusedLayers() {
        final Dimension dimension = app.getDimension();
        if (dimension == null) {
            beep();
            return;
        }
        final List<CustomLayer> unusedLayers = getCustomLayers();
        final Set<Layer> layersInUse = dimension.getAllLayers(true);
        unusedLayers.removeAll(layersInUse);
        if (unusedLayers.isEmpty()) {
            showInfo(app, "\u8BE5\u4E16\u754C\u6CA1\u6709\u672A\u4F7F\u7528\u7684\u8986\u76D6\u5C42.", "\u65E0\u672A\u4F7F\u7528\u8986\u76D6\u5C42");
        } else {
            final DeleteLayersDialog dialog = new DeleteLayersDialog(app, unusedLayers);
            dialog.setVisible(true);
            if (! dialog.isCancelled()) {
                showInfo(app, "\u9009\u4E2D\u7684\u8986\u76D6\u5C42\u5DF2\u88AB\u5220\u9664.", "\u8986\u76D6\u5C42\u5DF2\u5220\u9664");
            }
        }
    }

    /**
     * Gets all currently loaded custom layers, including hidden ones (from the
     * panel or the view), regardless of whether they are used on the map.
     */
    List<CustomLayer> getCustomLayers() {
        final List<CustomLayer> customLayers = new ArrayList<>(256);
        customLayers.addAll(paletteManager.getLayers());
        customLayers.addAll(app.layersWithNoButton);
        customLayers.sort(comparing(CustomLayer::getName));
        return customLayers;
    }

    /**
     * Gets all currently loaded custom layers, including hidden ones (from the
     * panel or the view), regardless of whether they are used on the map, by
     * palette (which will be {@code null} for hidden layers). For the
     * visible layers the collections will be in the order they are displayed on
     * the palette.
     */
    Map<String, Collection<CustomLayer>> getCustomLayersByPalette() {
        Map<String, Collection<CustomLayer>> customLayers = paletteManager.getLayersByPalette();
        if (! app.layersWithNoButton.isEmpty()) {
            customLayers.put(null, app.layersWithNoButton);
        }
        return customLayers;
    }

    /**
     * Import the custom layer, and if it is a combined layer, also the contained layers, terrain, etc.
     *
     * @param layer The layer to import.
     * @return {@code true} if new custom terrains were imported.
     */
    boolean importCustomLayer(CustomLayer layer) {
        boolean customTerrainButtonsAdded = false;
        layer.setExportIndex(null);
        registerCustomLayer(layer, true);
        if (layer instanceof CombinedLayer) {
            final CombinedLayer combinedLayer = (CombinedLayer) layer;
            importLayersFromCombinedLayer(combinedLayer);
            if (! combinedLayer.restoreCustomTerrain()) {
                showWarning(app, "\u8BE5\u8986\u76D6\u5C42\u5305\u542B\u4E86\u4E00\u4E2A\u4E0D\u53EF\u88AB\u6062\u590D\u7684\u81EA\u5B9A\u4E49\u65B9\u5757\uFF0C\u65B9\u5757\u5DF2\u91CD\u7F6E.", "\u81EA\u5B9A\u4E49\u65B9\u5757\u672A\u88AB\u6062\u590D");
            } else {
                // Check for a custom terrain type and if necessary make sure it has a button
                final Terrain terrain = combinedLayer.getTerrain();
                if ((terrain != null) && terrain.isCustom()) {
                    if (app.customMaterialButtons[terrain.getCustomTerrainIndex()] == null) {
                        customTerrainButtonsAdded = true;
                        app.addButtonForNewCustomTerrain(terrain.getCustomTerrainIndex(), Terrain.getCustomMaterial(terrain.getCustomTerrainIndex()), false);
                    }
                }
            }
        }
        return customTerrainButtonsAdded;
    }

    private void updateHiddenLayers() {
        // Hide newly hidden layers
        paletteManager.getLayers().stream().filter(CustomLayer::isHide).forEach(layer -> {
            if ((app.getActiveOperation() instanceof PaintOperation) && (app.paint instanceof LayerPaint) && (((LayerPaint) app.paint).getLayer().equals(layer))) {
                app.deselectPaint();
            }
            unregisterCustomLayer(layer);
            app.hiddenLayers.remove(layer);
            if (layer.equals(app.soloLayer)) {
                app.soloLayer = null;
            }
            app.layersWithNoButton.add(layer);
        });
        // Show newly unhidden layers
        for (Iterator<CustomLayer> i = app.layersWithNoButton.iterator(); i.hasNext(); ) {
            CustomLayer layer = i.next();
            if (! layer.isHide()) {
                i.remove();
                registerCustomLayer(layer, false);
            }
        }
        app.updateLayerVisibility();
    }

    private void importLayersFromCombinedLayer(CombinedLayer combinedLayer) {
        combinedLayer.getLayers().stream().filter(layer -> (layer instanceof CustomLayer) && (! paletteManager.contains(layer)) && (! app.layersWithNoButton.contains(layer))).forEach(layer -> {
            final CustomLayer customLayer = (CustomLayer) layer;
            customLayer.setExportIndex(null);
            if (customLayer.isHide()) {
                app.layersWithNoButton.add(customLayer);
            } else {
                registerCustomLayer(customLayer, false);
            }
            if (layer instanceof CombinedLayer) {
                importLayersFromCombinedLayer((CombinedLayer) customLayer);
            }
        });
    }

    private void findLayer(Layer layer) {
        final WorldPainter view = app.view;
        final Dimension dimension = app.getDimension();
        final int tileX = view.getViewX() >> TILE_SIZE_BITS, tileY = view.getViewY() >> TILE_SIZE_BITS;
        final AtomicInteger closestTileX = new AtomicInteger(), closestTileY = new AtomicInteger();
        final AtomicDouble closestDistance = new AtomicDouble(Double.MAX_VALUE);
        dimension.visitTiles()
                .forFilter(buildForDimension(dimension).onlyOn(layer).build())
                .andDo(tile -> {
                    if (! isLayerSet(tile, layer)) {
                        return;
                    }
                    final float distance = MathUtils.getDistance(tile.getX() - tileX, tile.getY() - tileY);
                    if (distance < closestDistance.floatValue()) {
                        closestDistance.set(distance);
                        closestTileX.set(tile.getX());
                        closestTileY.set(tile.getY());
                    }
                    // TODO stop iterating if the current location already contains the layer
                });
        if (closestDistance.get() == Double.MAX_VALUE) {
            beep();
            showInfo(view, "\u8986\u76D6\u5C42" + layer + "\u5728\u5F53\u524D\u7EF4\u5EA6\u672A\u88AB\u4F7F\u7528", "\u8986\u76D6\u5C42\u672A\u4F7F\u7528");
        } else {
            view.moveTo((closestTileX.get() << TILE_SIZE_BITS) + (TILE_SIZE / 2), (closestTileY.get() << TILE_SIZE_BITS) + (TILE_SIZE / 2));
        }
    }

    /**
     * Check whether the layer is set anywhere in the tile.
     */
    private boolean isLayerSet(Tile tile, Layer layer) {
        switch (layer.dataSize) {
            case BIT:
            case BIT_PER_CHUNK:
                final boolean defaultBitValue = layer.getDefaultValue() != 0;
                for (int x = 0; x < TILE_SIZE; x++) {
                    for (int y = 0; y < TILE_SIZE; y++) {
                        if (tile.getBitLayerValue(layer, x, y) != defaultBitValue) {
                            return true;
                        }
                    }
                }
                return false;
            case NIBBLE:
            case BYTE:
                final int defaultValue = layer.getDefaultValue();
                for (int x = 0; x < TILE_SIZE; x++) {
                    for (int y = 0; y < TILE_SIZE; y++) {
                        if (tile.getLayerValue(layer, x, y) != defaultValue) {
                            return true;
                        }
                    }
                }
                return false;
            default:
                throw new IllegalArgumentException("\u65E0\u6548\u7684\u6570\u636E\u5927\u5C0F" + layer.dataSize);
        }
    }

    private void editCustomLayer(CustomLayer layer) {
        editCustomLayer(layer, null);
    }

    private void exportLayer(CustomLayer layer) {
        Configuration config = Configuration.getInstance();
        File layerDirectory = config.getLayerDirectory();
        if ((layerDirectory == null) || (! layerDirectory.isDirectory())) {
            layerDirectory = DesktopUtils.getDocumentsFolder();
        }
        File selectedFile = FileUtils.selectFileForSave(app, "\u5BFC\u51FAWorldPainter\u8986\u76D6\u5C42\u6587\u4EF6", new File(layerDirectory, org.pepsoft.util.FileUtils.sanitiseName(layer.getName()) + ".layer"), new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".layer");
            }

            @Override
            public String getDescription() {
                return "WorldPainter\u81EA\u5B9A\u4E49\u8986\u76D6\u5C42 (*.layer)";
            }

            @Override
            public String getExtensions() {
                return "*.layer";
            }
        });
        if (selectedFile != null) {
            if (!selectedFile.getName().toLowerCase().endsWith(".layer")) {
                selectedFile = new File(selectedFile.getPath() + ".layer");
            }
            if (selectedFile.isFile() && (showConfirmDialog(app, "\u6587\u4EF6" + selectedFile.getName() + "\u5DF2\u7ECF\u5B58\u5728.\n\u4F60\u8981\u8986\u76D6\u5B83\u5417?", "\u8986\u76D6\u6587\u4EF6", YES_NO_OPTION) == NO_OPTION)) {
                return;
            }
            try {
                try (ObjectOutputStream out = new ObjectOutputStream(new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(selectedFile))))) {
                    out.writeObject(layer);
                }
            } catch (IOException e) {
                throw new RuntimeException("I/O error while trying to write " + selectedFile, e);
            }
            config.setLayerDirectory(selectedFile.getParentFile());
            showInfo(app, "\u8986\u76D6\u5C42" + layer.getName() + "\u6210\u529F\u5BFC\u51FA", "\u6210\u529F");
        }
    }

    private void moveLayerToPalette(CustomLayer layer, Palette destPalette) {
        Palette srcPalette = paletteManager.move(layer, destPalette);
        if (srcPalette.isEmpty()) {
            app.dockingManager.removeFrame(srcPalette.getDockableFrame().getKey());
            srcPalette.removePropertyChangeListener(this);
            paletteManager.delete(srcPalette);
        }
        app.validate();
    }

    private void createNewLayerPalette(CustomLayer layer) {
        String name;
        if ((name = showInputDialog(app, "\u4E3A\u8BE5\u753B\u677F\u8F93\u5165\u4E00\u4E2A\u72EC\u7279\u7684\u547D\u540D:", "\u65B0\u5EFA\u753B\u677F", QUESTION_MESSAGE)) != null) {
            name = name.trim();
            if (name.isEmpty()) {
                beepAndShowError(app, "\u753B\u677F\u540D\u79F0\u4E0D\u80FD\u4E3A\u7A7A", "\u65E0\u6548\u540D\u79F0");
                return;
            }
            if (paletteManager.getPalette(name) != null) {
                showMessageDialog(app, "\u5DF2\u7ECF\u5B58\u5728\u6B64\u547D\u540D\u7684\u753B\u677F\u4E86!", "\u91CD\u590D\u540D\u79F0", ERROR_MESSAGE);
                return;
            }
            Palette destPalette = paletteManager.create(name);
            app.dockingManager.addFrame(destPalette.getDockableFrame());
            app.dockingManager.dockFrame(destPalette.getDockableFrame().getKey(), DockContext.DOCK_SIDE_WEST, 3);
            moveLayerToPalette(layer, destPalette);
            app.dockingManager.activateFrame(destPalette.getDockableFrame().getKey());
            destPalette.addPropertyChangeListener(this);
        }
    }

    @SuppressWarnings("unchecked") // Guaranteed by code
    @NotNull
    private <L extends CustomLayer> AbstractEditLayerDialog<L> createEditLayerDialog(L layer) {
        final World2 world = app.getWorld();
        final Dimension dimension = app.getDimension();
        final AbstractEditLayerDialog<L> dialog;
        if (layer instanceof UndergroundPocketsLayer) {
            dialog = (AbstractEditLayerDialog<L>) new UndergroundPocketsDialog(app, world.getPlatform(), (UndergroundPocketsLayer) layer, app.getColourScheme(), dimension.getMinHeight(), dimension.getMaxHeight(), world.isExtendedBlockIds());
        } else if (layer instanceof TunnelLayer) {
            final int baseHeight, waterLevel;
            final TileFactory tileFactory = dimension.getTileFactory();
            if (tileFactory instanceof HeightMapTileFactory) {
                baseHeight = (int) ((HeightMapTileFactory) tileFactory).getBaseHeight();
                waterLevel = ((HeightMapTileFactory) tileFactory).getWaterHeight();
            } else {
                baseHeight = 58;
                waterLevel = DEFAULT_WATER_LEVEL;
            }
            // TODO passing in dimension here is a crude mechanism. It is supposed to be the dimension on which this
            //  layer is being used, but that is a lot of work to determine. In practice this will usually be right
            //  though
            dialog = (AbstractEditLayerDialog<L>) ((((TunnelLayer) layer).getLayerMode() == CAVE)
                ? new TunnelLayerDialog(app, world.getPlatform(), (TunnelLayer) layer, dimension, world.isExtendedBlockIds(), app.getColourScheme(), app.getCustomBiomeManager(), dimension.getMinHeight(), dimension.getMaxHeight(), baseHeight, waterLevel)
                : new FloatingLayerDialog(app, world.getPlatform(), (TunnelLayer) layer, dimension, world.isExtendedBlockIds(), app.getColourScheme(), app.getCustomBiomeManager(), dimension.getMinHeight(), dimension.getMaxHeight(), baseHeight, waterLevel));
        } else if (layer instanceof CustomAnnotationLayer) {
            dialog = (AbstractEditLayerDialog<L>) new CustomAnnotationLayerDialog(app, (CustomAnnotationLayer) layer);
        } else {
            dialog = new EditLayerDialog<>(app, world.getPlatform(), layer);
        }
        return dialog;
    }

    private List<JMenuItem> getCopyLayerMenuItems(String targetPaletteName, Function<Layer, Boolean> filter) {
        if (targetPaletteName == null) {
            throw new NullPointerException("targetPaletteName");
        }
        final List<JMenuItem> menuItems = new ArrayList<>();
        final Dimension currentDimension = app.getDimension();
        for (Dimension dimension: app.getWorld().getDimensions()) {
            if (dimension == currentDimension) {
                continue;
            }
            final Map<String, JMenu> menusForDimension = new HashMap<>();
            for (CustomLayer layer: dimension.getCustomLayers()) {
                if ((! layer.isExportableToFile()) || ((filter != null) && (! filter.apply(layer)))) {
                    continue;
                }
                final String palette = layer.getPalette();
                final JMenuItem menuForPalette = menusForDimension.computeIfAbsent(palette, k -> new JMenu(palette));
                final JMenuItem menuItem = new JMenuItem(layer.getName(), new ImageIcon(layer.getIcon()));
                menuItem.addActionListener(event -> copyLayerToPalette(layer, targetPaletteName));
                menuForPalette.add(menuItem);
            }
            final JMenu menuForDimension;
            if (menusForDimension.size() == 1) {
                menuForDimension = menusForDimension.values().iterator().next();
                menuForDimension.setText(dimension.getName());
            } else {
                menuForDimension = new JMenu(dimension.getName());
                for (JMenu menu: menusForDimension.values()) {
                    menuForDimension.add(menu);
                }
            }
            if (menuForDimension.getItemCount() > 0) {
                menuItems.add(menuForDimension);
            }
        }
        if (menuItems.size() == 1) {
            return Lists.transform(asList(((JMenu) menuItems.get(0)).getMenuComponents()), e -> (JMenuItem) e);
        } else {
            return menuItems;
        }
    }

    private void copyLayerToPalette(CustomLayer layer, String paletteName) {
        final CustomLayer copy = layer.clone();
        copy.setPalette(paletteName);
        registerCustomLayer(copy, true);
    }

    private final App app;

    final PaletteManager paletteManager = new PaletteManager(this);

    private static final ResourceBundle strings = ResourceBundle.getBundle("org.pepsoft.worldpainter.resources.strings"); // NOI18N
    private static final String EDITING_FLOOR_DIMENSION_KEY = "org.pepsoft.worldpainter.TunnelLayer.editingFloorDimension";
    private static final Icon ADD_CUSTOM_LAYER_BUTTON_ICON = IconUtils.loadScaledIcon("org/pepsoft/worldpainter/icons/plus.png");
    private static final Logger logger = LoggerFactory.getLogger(CustomLayerController.class);
}