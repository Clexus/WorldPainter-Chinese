/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.threedeeview;

import org.pepsoft.minecraft.Direction;
import org.pepsoft.util.IconUtils;
import org.pepsoft.util.ProgressReceiver;
import org.pepsoft.util.ProgressReceiver.OperationCancelled;
import org.pepsoft.util.swing.ProgressDialog;
import org.pepsoft.util.swing.ProgressTask;
import org.pepsoft.worldpainter.App;
import org.pepsoft.worldpainter.ColourScheme;
import org.pepsoft.worldpainter.Dimension;
import org.pepsoft.worldpainter.Tile;
import org.pepsoft.worldpainter.biomeschemes.CustomBiomeManager;
import org.pepsoft.worldpainter.layers.Layer;
import org.pepsoft.worldpainter.threedeeview.Tile3DRenderer.LayerVisibilityMode;
import org.pepsoft.worldpainter.util.BetterAction;
import org.pepsoft.worldpainter.util.ImageUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.Set;

import static org.pepsoft.util.GUIUtils.scaleToUI;
import static org.pepsoft.util.swing.MessageUtils.beepAndShowError;
import static org.pepsoft.worldpainter.App.INT_NUMBER_FORMAT;
import static org.pepsoft.worldpainter.Constants.DIM_NORMAL;
import static org.pepsoft.worldpainter.Dimension.Anchor.NORMAL_DETAIL;
import static org.pepsoft.worldpainter.threedeeview.Tile3DRenderer.LayerVisibilityMode.*;
import static org.pepsoft.worldpainter.util.LayoutUtils.setDefaultSizeAndLocation;

/**
 *
 * @author pepijn
 */
public class ThreeDeeFrame extends JFrame implements WindowListener {
    public ThreeDeeFrame(Dimension dimension, ColourScheme colourScheme, CustomBiomeManager customBiomeManager, Point initialCoords) throws HeadlessException {
        super("WorldPainter - 3D \u89C6\u56FE");
        setIconImage(App.ICON);
        this.colourScheme = colourScheme;
        this.customBiomeManager = customBiomeManager;
        this.coords = initialCoords;

        scrollPane = new JScrollPane();

        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                previousX = e.getX();
                previousY = e.getY();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                int dx = e.getX() - previousX;
                int dy = e.getY() - previousY;
                previousX = e.getX();
                previousY = e.getY();
                JScrollBar scrollBar = scrollPane.getHorizontalScrollBar();
                scrollBar.setValue(scrollBar.getValue() - dx);
                scrollBar = scrollPane.getVerticalScrollBar();
                scrollBar.setValue(scrollBar.getValue() - dy);
            }

            @Override public void mouseClicked(MouseEvent e) {}
            @Override public void mouseReleased(MouseEvent e) {}
            @Override public void mouseEntered(MouseEvent e) {}
            @Override public void mouseExited(MouseEvent e) {}
            @Override public void mouseMoved(MouseEvent e) {}

            private int previousX, previousY;
        };
        scrollPane.addMouseListener(mouseAdapter);
        scrollPane.addMouseMotionListener(mouseAdapter);
        scrollPane.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.getWheelRotation() < 0) {
                    if (zoom < MAX_ZOOM) {
                        ZOOM_IN_ACTION.actionPerformed(new ActionEvent(e.getSource(), ActionEvent.ACTION_PERFORMED, null, e.getWhen(), e.getModifiers()));
                    }
                } else {
                    if (zoom > MIN_ZOOM) {
                        ZOOM_OUT_ACTION.actionPerformed(new ActionEvent(e.getSource(), ActionEvent.ACTION_PERFORMED, null, e.getWhen(), e.getModifiers()));
                    }
                }
            }
        });

        getContentPane().add(scrollPane, BorderLayout.CENTER);

        alwaysOnTopButton.setToolTipText("\u8BBE\u7F6E3D\u89C6\u56FE\u603B\u662F\u4F4D\u4E8E\u7A97\u53E3\u6700\u4E0A\u5C42");
        alwaysOnTopButton.addActionListener(e -> {
            if (alwaysOnTopButton.isSelected()) {
                ThreeDeeFrame.this.setAlwaysOnTop(true);
            } else {
                ThreeDeeFrame.this.setAlwaysOnTop(false);
            }
        });

        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.add(alwaysOnTopButton);
        toolBar.addSeparator();
        toolBar.add(ROTATE_LEFT_ACTION);
        toolBar.add(ROTATE_RIGHT_ACTION);
        toolBar.addSeparator();
        toolBar.add(ZOOM_OUT_ACTION);
        toolBar.add(RESET_ZOOM_ACTION);
        toolBar.add(ZOOM_IN_ACTION);
        toolBar.addSeparator();
        toolBar.add(EXPORT_IMAGE_ACTION);
        toolBar.addSeparator();
        toolBar.add(MOVE_TO_SPAWN_ACTION);
        toolBar.add(MOVE_TO_ORIGIN_ACTION);
        toolBar.addSeparator();
        toolBar.add(new JLabel("\u53EF\u89C1\u8986\u76D6\u5C42:"));
        final JRadioButton radioButtonLayersNone = new JRadioButton(NO_LAYERS_ACTION);
        layerVisibilityButtonGroup.add(radioButtonLayersNone);
        toolBar.add(radioButtonLayersNone);
        final JRadioButton radioButtonLayersSync = new JRadioButton(SYNC_LAYERS_ACTION);
        layerVisibilityButtonGroup.add(radioButtonLayersSync);
        toolBar.add(radioButtonLayersSync);
        final JRadioButton radioButtonLayersAll = new JRadioButton(SURFACE_LAYERS_ACTION);
        layerVisibilityButtonGroup.add(radioButtonLayersAll);
        toolBar.add(radioButtonLayersAll);
        getContentPane().add(toolBar, BorderLayout.NORTH);

        glassPane = new GlassPane();
        setGlassPane(glassPane);
        getGlassPane().setVisible(true);

        ActionMap actionMap = rootPane.getActionMap();
        actionMap.put("rotateLeft", ROTATE_LEFT_ACTION);
        actionMap.put("rotateRight", ROTATE_RIGHT_ACTION);
        actionMap.put("zoomIn", ZOOM_IN_ACTION);
        actionMap.put("resetZoom", RESET_ZOOM_ACTION);
        actionMap.put("zoomOut", ZOOM_OUT_ACTION);

        InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(KeyStroke.getKeyStroke('l'), "rotateLeft");
        inputMap.put(KeyStroke.getKeyStroke('r'), "rotateRight");
        inputMap.put(KeyStroke.getKeyStroke('-'), "zoomOut");
        inputMap.put(KeyStroke.getKeyStroke('0'), "resetZoom");
        inputMap.put(KeyStroke.getKeyStroke('+'), "zoomIn");

        setSize(800, 600);
        scaleToUI(this);
        setDefaultSizeAndLocation(this, 60);

        setDimension(dimension);

        addWindowListener(this);
    }

    public final Dimension getDimension() {
        return dimension;
    }

    public final void setDimension(Dimension dimension) {
        this.dimension = dimension;
        if (dimension != null) {
            threeDeeView = new ThreeDeeView(dimension, colourScheme, customBiomeManager, rotation, zoom);
            threeDeeView.setLayerVisibility(layerVisibility);
            threeDeeView.setHiddenLayers(hiddenLayers);
            scrollPane.setViewportView(threeDeeView);
            MOVE_TO_SPAWN_ACTION.setEnabled(dimension.getAnchor().equals((dimension.getWorld().getSpawnPointDimension() == null) ? NORMAL_DETAIL : dimension.getWorld().getSpawnPointDimension()));
            glassPane.setRotation(DIRECTIONS[rotation], dimension.getAnchor().invert);
        }
    }

    public void setHiddenLayers(Set<Layer> hiddenLayers) {
        this.hiddenLayers = hiddenLayers;
        if (threeDeeView != null) {
            threeDeeView.setHiddenLayers(hiddenLayers);
        }
    }

    public void resetAlwaysOnTop() {
        if (isAlwaysOnTop()) {
            setAlwaysOnTop(false);
            alwaysOnTopButton.setSelected(false);
        }
    }

    public void moveTo(Point coords) {
        this.coords = coords;
        threeDeeView.moveTo(coords.x, coords.y);
    }

    public void refresh(boolean clear) {
        if (threeDeeView != null) {
            threeDeeView.refresh(clear);
        }
    }

    private boolean imageFitsInJavaArray(Rectangle imageBounds) {
        final long area = (long) imageBounds.width * imageBounds.height;
        return (area >= 0L) && (area <= Integer.MAX_VALUE);
    }

    private void setLayerVisibility(LayerVisibilityMode layerVisibility) {
        this.layerVisibility = layerVisibility;
        if (threeDeeView != null) {
            threeDeeView.setLayerVisibility(layerVisibility);
        }
    }

    // WindowListener

    @Override
    public void windowOpened(WindowEvent e) {
        moveTo(coords);
    }

    @Override public void windowClosing(WindowEvent e) {}
    @Override public void windowClosed(WindowEvent e) {}
    @Override public void windowIconified(WindowEvent e) {}
    @Override public void windowDeiconified(WindowEvent e) {}
    @Override public void windowActivated(WindowEvent e) {}
    @Override public void windowDeactivated(WindowEvent e) {}

    private final Action ROTATE_LEFT_ACTION = new BetterAction("rotate3DViewLeft", "\u5DE6\u8F6C", ICON_ROTATE_LEFT) {
        {
            setShortDescription("\u5C06\u89C6\u56FE\u9006\u65F6\u9488 90 \u5EA6\u65CB\u8F6C");
        }

        @Override
        public void performAction(ActionEvent e) {
            rotation--;
            if (rotation < 0) {
                rotation = 3;
            }
            final Tile centreMostTile = threeDeeView.getCentreMostTile();
            if (centreMostTile != null) {
                threeDeeView = new ThreeDeeView(dimension, colourScheme, customBiomeManager, rotation, zoom);
                threeDeeView.setLayerVisibility(layerVisibility);
                threeDeeView.setHiddenLayers(hiddenLayers);
                scrollPane.setViewportView(threeDeeView);
//                scrollPane.getViewport().setViewPosition(new Point((threeDeeView.getWidth() - scrollPane.getWidth()) / 2, (threeDeeView.getHeight() - scrollPane.getHeight()) / 2));
                threeDeeView.moveToTile(centreMostTile);
                glassPane.setRotation(DIRECTIONS[rotation], dimension.getAnchor().invert);
            }
        }

        private static final long serialVersionUID = 1L;
    };

    private final Action ROTATE_RIGHT_ACTION = new BetterAction("rotate3DViewRight", "\u53F3\u8F6C", ICON_ROTATE_RIGHT) {
        {
            setShortDescription("\u5C06\u89C6\u56FE\u987A\u65F6\u9488 90 \u5EA6\u65CB\u8F6C");
        }

        @Override
        public void performAction(ActionEvent e) {
            rotation++;
            if (rotation > 3) {
                rotation = 0;
            }
            final Tile centreMostTile = threeDeeView.getCentreMostTile();
            if (centreMostTile != null) {
                threeDeeView = new ThreeDeeView(dimension, colourScheme, customBiomeManager, rotation, zoom);
                threeDeeView.setLayerVisibility(layerVisibility);
                threeDeeView.setHiddenLayers(hiddenLayers);
                scrollPane.setViewportView(threeDeeView);
//                scrollPane.getViewport().setViewPosition(new Point((threeDeeView.getWidth() - scrollPane.getWidth()) / 2, (threeDeeView.getHeight() - scrollPane.getHeight()) / 2));
                threeDeeView.moveToTile(centreMostTile);
                glassPane.setRotation(DIRECTIONS[rotation], dimension.getAnchor().invert);
            }
        }

        private static final long serialVersionUID = 1L;
    };

    private final Action EXPORT_IMAGE_ACTION = new BetterAction("export3DViewImage", "\u5BFC\u51FA\u56FE\u7247", ICON_EXPORT_IMAGE) {
        {
            setShortDescription("\u5BFC\u51FA\u4E00\u4E2A\u56FE\u7247\u6587\u4EF6");
        }

        @Override
        public void performAction(ActionEvent e) {
            final Rectangle imageBounds = threeDeeView.getImageBounds();
            if (! imageFitsInJavaArray(imageBounds)) {
                beepAndShowError(ThreeDeeFrame.this, "\u8BE53D\u56FE\u8D85\u51FA\u4E86\u53EF\u5BFC\u51FA\u56FE\u7247\u7684\u5927\u5C0F\u9650\u5236.\n\u56FE\u50CF\u5927\u5C0F(\u957Fx\u5BBD)\u4E0D\u80FD\u5927\u4E8E" + INT_NUMBER_FORMAT.format(Integer.MAX_VALUE), "3D\u56FE\u50CF\u8FC7\u5927");
                return;
            }
            final String defaultname = dimension.getWorld().getName().replaceAll("\\s", "").toLowerCase() + ((dimension.getAnchor().dim == DIM_NORMAL) ? "" : ("_" + dimension.getName().toLowerCase())) + "_3d.png";
            File selectedFile = ImageUtils.selectImageForSave(ThreeDeeFrame.this, "image file", new File(defaultname));
            if (selectedFile != null) {
                final String type;
                int p = selectedFile.getName().lastIndexOf('.');
                if (p != -1) {
                    type = selectedFile.getName().substring(p + 1).toUpperCase();
                } else {
                    type = "PNG";
                    selectedFile = new File(selectedFile.getParentFile(), selectedFile.getName() + ".png");
                }
                if (selectedFile.exists()) {
                    if (JOptionPane.showConfirmDialog(ThreeDeeFrame.this, "\u8BE5\u6587\u4EF6\u5DF2\u7ECF\u5B58\u5728\u4E86!\n\u4F60\u8981\u8986\u76D6\u5B83\u5417?", "\u8986\u76D6\u6587\u4EF6?", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                        return;
                    }
                }
                final File file = selectedFile;
                Boolean result = ProgressDialog.executeTask(ThreeDeeFrame.this, new ProgressTask<Boolean>() {
                        @Override
                        public String getName() {
                            return "\u5BFC\u51FA\u56FE\u50CF...";
                        }

                        @Override
                        public Boolean execute(ProgressReceiver progressReceiver) throws OperationCancelled {
                            try {
                                return ImageIO.write(threeDeeView.getImage(imageBounds, progressReceiver), type, file);
                            } catch (IOException e) {
                                throw new RuntimeException("I/O error while exporting image", e);
                            }
                        }
                    });
                if ((result != null) && result.equals(Boolean.FALSE)) {
                    JOptionPane.showMessageDialog(ThreeDeeFrame.this, "\u683C\u5F0F " + type + " \u4E0D\u652F\u6301!");
                }
            }
        }

        private static final long serialVersionUID = 1L;
    };

    private final Action MOVE_TO_SPAWN_ACTION = new BetterAction("move3DViewToSpawn", "\u79FB\u52A8\u5230\u51FA\u751F\u70B9", ICON_MOVE_TO_SPAWN) {
        {
            setShortDescription("\u5C06\u89C6\u56FE\u79FB\u52A8\u5230\u51FA\u751F\u70B9");
        }

        @Override
        public void performAction(ActionEvent e) {
            if (dimension.getAnchor().dim == DIM_NORMAL) {
                Point spawn = dimension.getWorld().getSpawnPoint();
                threeDeeView.moveTo(spawn.x, spawn.y);
            }
        }

        private static final long serialVersionUID = 1L;
    };

    private final Action MOVE_TO_ORIGIN_ACTION = new BetterAction("move3DViewToOrigin", "\u79FB\u52A8\u5230\u539F\u70B9", ICON_MOVE_TO_ORIGIN) {
        {
            setShortDescription("\u5C06\u89C6\u56FE\u79FB\u52A8\u5230\u539F\u70B9 (\u5750\u6807 0,0)");
        }

        @Override
        public void performAction(ActionEvent e) {
            threeDeeView.moveTo(0, 0);
        }

        private static final long serialVersionUID = 1L;
    };

    private final Action ZOOM_IN_ACTION = new BetterAction("zoom3DViewIn", "\u653E\u5927", ICON_ZOOM_IN) {
        {
            setShortDescription("\u653E\u5927");
        }

        @Override
        public void performAction(ActionEvent e) {
            final Rectangle visibleRect = threeDeeView.getVisibleRect();
            zoom++;
            threeDeeView.setZoom(zoom);
            visibleRect.x *= 2;
            visibleRect.y *= 2;
            visibleRect.x += visibleRect.width / 2;
            visibleRect.y += visibleRect.height / 2;
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    threeDeeView.scrollRectToVisible(visibleRect);
                }
            });
            if (zoom >= MAX_ZOOM) {
                setEnabled(false);
            }
            ZOOM_OUT_ACTION.setEnabled(true);
            RESET_ZOOM_ACTION.setEnabled(zoom != 1);
        }

        private static final long serialVersionUID = 1L;
    };

    private final Action RESET_ZOOM_ACTION = new BetterAction("reset3DViewZoom", "\u91CD\u7F6E\u653E\u7F29", ICON_RESET_ZOOM) {
        {
            setShortDescription("\u5C06\u89C6\u56FE\u7F29\u653E\u91CD\u7F6E\u4E3A 1:1");
            setEnabled(false);
        }

        @Override
        public void performAction(ActionEvent e) {
            final Rectangle visibleRect = threeDeeView.getVisibleRect();
            if (zoom < 1) {
                while (zoom < 1) {
                    zoom++;
                    visibleRect.x *= 2;
                    visibleRect.y *= 2;
                    visibleRect.x += visibleRect.width / 2;
                    visibleRect.y += visibleRect.height / 2;
                }
                threeDeeView.setZoom(zoom);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        threeDeeView.scrollRectToVisible(visibleRect);
                    }
                });
            } else if (zoom > 1) {
                while (zoom > 1) {
                    zoom--;
                    visibleRect.x /= 2;
                    visibleRect.y /= 2;
                    visibleRect.x -= visibleRect.width / 4;
                    visibleRect.y -= visibleRect.height / 4;
                }
                threeDeeView.setZoom(zoom);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        threeDeeView.scrollRectToVisible(visibleRect);
                    }
                });
            }
            ZOOM_IN_ACTION.setEnabled(true);
            ZOOM_OUT_ACTION.setEnabled(true);
            setEnabled(false);
        }

        private static final long serialVersionUID = 1L;
    };

    private final Action ZOOM_OUT_ACTION = new BetterAction("zoom3DViewOut", "\u7F29\u5C0F", ICON_ZOOM_OUT) {
        {
            setShortDescription("\u7F29\u5C0F");
        }

        @Override
        public void performAction(ActionEvent e) {
            final Rectangle visibleRect = threeDeeView.getVisibleRect();
            zoom--;
            threeDeeView.setZoom(zoom);
            visibleRect.x /= 2;
            visibleRect.y /= 2;
            visibleRect.x -= visibleRect.width / 4;
            visibleRect.y -= visibleRect.height / 4;
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    threeDeeView.scrollRectToVisible(visibleRect);
                }
            });
            if (zoom <= MIN_ZOOM) {
                setEnabled(false);
            }
            ZOOM_IN_ACTION.setEnabled(true);
            RESET_ZOOM_ACTION.setEnabled(zoom != 1);
        }

        private static final long serialVersionUID = 1L;
    };

    private final Action NO_LAYERS_ACTION = new BetterAction("layerVisibilityNone", "\u65E0") {
        {
            setShortDescription("\u4E0D\u663E\u793A\u8986\u76D6\u5C42");
        }

        @Override
        protected void performAction(ActionEvent e) {
            setLayerVisibility(NONE);
        }
    };

    private final Action SYNC_LAYERS_ACTION = new BetterAction("layerVisibilitySync", "\u540C\u6B65") {
        {
            setShortDescription("\u4E0E\u7F16\u8F91\u5668\u540C\u6B65\u8986\u76D6\u5C42\u53EF\u89C1\u6027");
        }

        @Override
        protected void performAction(ActionEvent e) {
            setLayerVisibility(SYNC);
        }
    };

    private final Action SURFACE_LAYERS_ACTION = new BetterAction("layerVisibilitySurface", "\u8868\u5C42") {
        {
            setShortDescription("\u663E\u793A\u6240\u6709\u5730\u8868\u8986\u76D6\u5C42");
            setSelected(true);
        }

        @Override
        protected void performAction(ActionEvent e) {
            setLayerVisibility(SURFACE);
        }
    };

    private final JScrollPane scrollPane;
    private final GlassPane glassPane;
    private final CustomBiomeManager customBiomeManager;
    private final ButtonGroup layerVisibilityButtonGroup = new ButtonGroup();
    final JToggleButton alwaysOnTopButton = new JToggleButton(ICON_ALWAYS_ON_TOP);
    private Dimension dimension;
    private ThreeDeeView threeDeeView;
    private ColourScheme colourScheme;
    private int rotation = 3, zoom = 1;
    private Point coords;
    private LayerVisibilityMode layerVisibility = SURFACE;
    private Set<Layer> hiddenLayers;
    
    private static final Direction[] DIRECTIONS = {Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.NORTH};
    
    private static final Icon ICON_ROTATE_LEFT    = IconUtils.loadScaledIcon("org/pepsoft/worldpainter/icons/arrow_rotate_anticlockwise.png");
    private static final Icon ICON_ROTATE_RIGHT   = IconUtils.loadScaledIcon("org/pepsoft/worldpainter/icons/arrow_rotate_clockwise.png");
    private static final Icon ICON_EXPORT_IMAGE   = IconUtils.loadScaledIcon("org/pepsoft/worldpainter/icons/picture_save.png");
    private static final Icon ICON_MOVE_TO_SPAWN  = IconUtils.loadScaledIcon("org/pepsoft/worldpainter/icons/spawn_red.png");
    private static final Icon ICON_MOVE_TO_ORIGIN = IconUtils.loadScaledIcon("org/pepsoft/worldpainter/icons/arrow_in.png");
    private static final Icon ICON_ALWAYS_ON_TOP  = IconUtils.loadScaledIcon("org/pepsoft/worldpainter/icons/lock.png");
    private static final Icon ICON_ZOOM_IN        = IconUtils.loadScaledIcon("org/pepsoft/worldpainter/icons/magnifier_zoom_in.png");
    private static final Icon ICON_RESET_ZOOM     = IconUtils.loadScaledIcon("org/pepsoft/worldpainter/icons/magnifier.png");
    private static final Icon ICON_ZOOM_OUT       = IconUtils.loadScaledIcon("org/pepsoft/worldpainter/icons/magnifier_zoom_out.png");
    
    private static final int MIN_ZOOM = -2;
    private static final int MAX_ZOOM = 4;
    
    private static final long serialVersionUID = 1L;
}