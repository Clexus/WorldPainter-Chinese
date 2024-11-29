/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.tools;

import org.pepsoft.minecraft.Constants;
import org.pepsoft.minecraft.JavaLevel;
import org.pepsoft.minecraft.SeededGenerator;
import org.pepsoft.util.DesktopUtils;
import org.pepsoft.util.ProgressReceiver;
import org.pepsoft.util.ProgressReceiver.OperationCancelled;
import org.pepsoft.util.swing.ProgressDialog;
import org.pepsoft.util.swing.ProgressTask;
import org.pepsoft.worldpainter.Dimension;
import org.pepsoft.worldpainter.*;
import org.pepsoft.worldpainter.biomeschemes.*;
import org.pepsoft.worldpainter.util.MinecraftUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.pepsoft.util.GUIUtils.scaleToUI;
import static org.pepsoft.worldpainter.Constants.*;
import static org.pepsoft.worldpainter.Dimension.Anchor.NORMAL_DETAIL;
import static org.pepsoft.worldpainter.Generator.DEFAULT;
import static org.pepsoft.worldpainter.Generator.LARGE_BIOMES;

/**
 *
 * @author pepijn
 */
public class BiomesViewerFrame extends JFrame {
    public BiomesViewerFrame(long seed, int preferredBiomeAlgorithm, ColourScheme colourScheme, SeedListener seedListener) throws HeadlessException {
        this(seed, null, preferredBiomeAlgorithm, colourScheme, seedListener);
    }
    
    public BiomesViewerFrame(long seed, final Point marker, int preferredBiomeAlgorithm, ColourScheme colourScheme, SeedListener seedListener) throws HeadlessException {
        super("WorldPainter - \u7FA4\u7CFB\u67E5\u770B\u5668");
        this.colourScheme = colourScheme;
        this.seedListener = seedListener;
        standAloneMode = App.getInstanceIfExists() == null;
        imageViewer = new BiomesViewer(standAloneMode, true);
        if (marker != null) {
            imageViewer.setMarkerCoords(marker);
            imageViewer.moveToMarker();
        }
        imageViewer.addMouseWheelListener(e -> {
            int rotation = e.getWheelRotation();
            int zoom = imageViewer.getZoom();
            if (rotation < 0) {
                zoom = Math.min(zoom + -rotation, 0);
            } else {
                zoom = Math.max(zoom - rotation, -4);
            }
            imageViewer.setZoom(zoom);
        });

        if (! standAloneMode) {
            Controller controller = new Controller();
            imageViewer.addMouseListener(controller);
            imageViewer.addMouseMotionListener(controller);
        }

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().add(imageViewer, BorderLayout.CENTER);

        JToolBar toolBar = new JToolBar();
        toolBar.add(new JLabel("\u751F\u7269\u7FA4\u7CFB\u65B9\u6848:"));
        List<Integer> availableAlgorithms = BiomeSchemeManager.getAvailableBiomeAlgorithms();
        //noinspection unchecked // NetBeans visual designer
        schemeChooser = new JComboBox(availableAlgorithms.toArray());
        schemeChooser.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Integer) {
                    switch ((Integer) value) {
                        case BIOME_ALGORITHM_1_1:
                            setText("Minecraft 1.1");
                            break;
                        case BIOME_ALGORITHM_1_2_AND_1_3_DEFAULT:
                            setText("Minecraft 1.2 - 1.6 \u9ED8\u8BA4");
                            break;
                        case BIOME_ALGORITHM_1_3_LARGE:
                            setText("Minecraft 1.3 - 1.6 \u5927\u578B");
                            break;
                        case BIOME_ALGORITHM_1_7_DEFAULT:
                            setText("Minecraft 1.7 - 1.12 \u9ED8\u8BA4");
                            break;
                        case BIOME_ALGORITHM_1_7_LARGE:
                            setText("Minecraft 1.7 - 1.12 \u5927\u578B");
                            break;
                    }
                }
                return this;
            }
        });
        seedSpinner = new JSpinner(new SpinnerNumberModel(Long.valueOf(seed), Long.valueOf(Long.MIN_VALUE), Long.valueOf(Long.MAX_VALUE), Long.valueOf(1L)));
        if (availableAlgorithms.contains(preferredBiomeAlgorithm)) {
            schemeChooser.setSelectedItem(preferredBiomeAlgorithm);
        } else {
            schemeChooser.setSelectedIndex(0);
        }
        BiomeScheme biomeScheme = BiomeSchemeManager.getNewBiomeScheme((Integer) schemeChooser.getSelectedItem());
        if (biomeScheme != null) {
            this.biomeScheme = biomeScheme;
            this.biomeScheme.setSeed(seed);
            imageViewer.setTileProvider(new BiomesTileProvider(BiomesViewerFrame.this.biomeScheme, BiomesViewerFrame.this.colourScheme, imageViewer.getZoom(), false));
        }
        schemeChooser.addItemListener(e -> {
            BiomeScheme biomeScheme1 = BiomeSchemeManager.getNewBiomeScheme((Integer) schemeChooser.getSelectedItem());
            if (biomeScheme1 != null) {
                BiomesViewerFrame.this.biomeScheme = biomeScheme1;
                BiomesViewerFrame.this.biomeScheme.setSeed(((Number) seedSpinner.getValue()).longValue());
                imageViewer.setTileProvider(new BiomesTileProvider(BiomesViewerFrame.this.biomeScheme, BiomesViewerFrame.this.colourScheme, imageViewer.getZoom(), false));
            }
        });
        toolBar.add(schemeChooser);
        toolBar.add(Box.createHorizontalStrut(5));
        toolBar.add(new JLabel("Seed:"));
        seedSpinner.setEditor(new JSpinner.NumberEditor(seedSpinner, "0"));
        seedSpinner.addChangeListener(e -> {
            if (BiomesViewerFrame.this.biomeScheme != null) {
                BiomesViewerFrame.this.biomeScheme.setSeed(((Number) seedSpinner.getValue()).longValue());
                imageViewer.setTileProvider(new BiomesTileProvider(BiomesViewerFrame.this.biomeScheme, BiomesViewerFrame.this.colourScheme, imageViewer.getZoom(), false));
            }
        });
        toolBar.add(seedSpinner);
        toolBar.add(Box.createHorizontalGlue());
        getContentPane().add(toolBar, BorderLayout.NORTH);

        toolBar = new JToolBar();
        JButton button = new JButton("-");
        button.addActionListener(e -> {
            int zoom = imageViewer.getZoom();
            zoom = Math.max(zoom - 1, -4);
            imageViewer.setZoom(zoom);
        });
        toolBar.add(button);

        button = new JButton("+");
        button.addActionListener(e -> {
            int zoom = imageViewer.getZoom();
            zoom = Math.min(zoom + 1, 0);
            imageViewer.setZoom(zoom);
        });
        toolBar.add(button);

        toolBar.add(Box.createHorizontalStrut(5));
        createWorldButton = new JButton("\u521B\u5EFA\u4E16\u754C");
        if (! standAloneMode) {
            createWorldButton.setToolTipText("\u7531\u9009\u4E2D\u7684\u5206\u533A\u521B\u5EFA\u65B0\u7684WorldPainter\u4E16\u754C");
            createWorldButton.addActionListener(e -> createWorld());
        }
        createWorldButton.setEnabled(false);
        toolBar.add(createWorldButton);

        toolBar.add(Box.createHorizontalStrut(5));
        button = new JButton("\u91CD\u7F6E");
        button.addActionListener(e -> {
            imageViewer.setZoom(-2);
            if (marker != null) {
                imageViewer.moveToMarker();
            } else {
                imageViewer.moveToOrigin();
            }
        });
        toolBar.add(button);

        if (seedListener != null) {
            toolBar.add(Box.createHorizontalStrut(5));
            button = new JButton("\u590D\u5236\u79CD\u5B50\u5230\u4E16\u754C");
            button.setToolTipText("\u5C06\u5F53\u524D\u79CD\u5B50\u590D\u5236\u5230\u6B63\u5728\u7F16\u8F91\u7684\u4E16\u754C");
            button.addActionListener(e -> BiomesViewerFrame.this.seedListener.setSeed(((Number) seedSpinner.getValue()).longValue(), ((schemeChooser.getSelectedIndex() == 1) || (schemeChooser.getSelectedIndex() == 3)) ? Generator.LARGE_BIOMES : Generator.DEFAULT));
            toolBar.add(button);
        }

        toolBar.add(Box.createHorizontalStrut(5));
        button = new JButton("\u5728\u6B64\u4E16\u754C\u73A9\u751F\u5B58\u6A21\u5F0F");
        button.setToolTipText("\u5728\u6B64\u5904\u4EE5\u8BE5\u79CD\u5B50\u521B\u5EFA\u4E00\u4E2AMinecraft\u751F\u5B58\u6A21\u5F0F\u4E16\u754C");
        button.addActionListener(event -> playHere(false));
        toolBar.add(button);

        toolBar.add(Box.createHorizontalStrut(5));
        button = new JButton("\u5728\u6B64\u4E16\u754C\u73A9\u521B\u9020\u6A21\u5F0F");
        button.setToolTipText("\u5728\u6B64\u5904\u4EE5\u8BE5\u79CD\u5B50\u521B\u5EFA\u4E00\u4E2AMinecraft\u521B\u9020\u6A21\u5F0F\u4E16\u754C");
        button.addActionListener(event -> playHere(true));
        toolBar.add(button);

        toolBar.add(Box.createHorizontalGlue());
        getContentPane().add(toolBar, BorderLayout.SOUTH);

        scaleToUI(this);
        setSize(800, 600);
    }

    public void destroy() {
        imageViewer.removeAllTileProviders();
    }

    private void playHere(boolean creativeMode) {
        String name = JOptionPane.showInputDialog(BiomesViewerFrame.this, "\u4E3A\u5730\u56FE\u547D\u540D:", "\u5730\u56FE\u540D", JOptionPane.QUESTION_MESSAGE);
        if ((name == null) || (name.trim().length() == 0)) {
            return;
        }
        name = name.trim();
        File savesDir;
        boolean minecraftDirUsed = false;
        File minecraftDir = MinecraftUtil.findMinecraftDir();
        if (minecraftDir != null) {
            savesDir = new File(minecraftDir, "saves");
            minecraftDirUsed = true;
        } else {
            savesDir = DesktopUtils.getDocumentsFolder();
        }
        File worldDir = new File(savesDir, name);
        int ordinal = 1;
        while (worldDir.isDirectory()) {
            worldDir = new File(savesDir, name + ordinal);
            ordinal++;
        }
        if (! worldDir.mkdirs()) {
            throw new RuntimeException("Could not create " + worldDir);
        }
        BiomeScheme biomeScheme1 = BiomesViewerFrame.this.biomeScheme;
        final Platform platform = (biomeScheme1 instanceof Minecraft1_1BiomeScheme) ? DefaultPlugin.JAVA_MCREGION : DefaultPlugin.JAVA_ANVIL;
        JavaLevel level = JavaLevel.create(platform, platform.minZ, platform.standardMaxHeight);
        final long seed = ((Number) seedSpinner.getValue()).longValue();
        if (! (biomeScheme1 instanceof Minecraft1_1BiomeScheme)) {
            final Generator generatorType = ((biomeScheme1 instanceof Minecraft1_3LargeBiomeScheme) || (biomeScheme1 instanceof Minecraft1_7LargeBiomeScheme) || (biomeScheme1 instanceof Minecraft1_8LargeBiomeScheme) || (biomeScheme1 instanceof Minecraft1_12LargeBiomeScheme)) ? Generator.LARGE_BIOMES : Generator.DEFAULT;
            level.setGenerator(DIM_NORMAL, new SeededGenerator(generatorType, seed));
        }
        if (creativeMode) {
            level.setGameType(Constants.GAME_TYPE_CREATIVE);
        } else {
            level.setGameType(Constants.GAME_TYPE_SURVIVAL);
        }
        level.setMapFeatures(true);
        level.setName(name);
        level.setSeed(seed);
        Point worldCoords = imageViewer.getViewLocation();
        level.setSpawnX(worldCoords.x);
        level.setSpawnZ(worldCoords.y);
        level.setSpawnY(64);
        try {
            level.save(worldDir);
        } catch (IOException e) {
            throw new RuntimeException("I/O error writing level.dat file", e);
        }
        if (minecraftDirUsed) {
            JOptionPane.showMessageDialog(BiomesViewerFrame.this, "\u5730\u56FE\u5DF2\u4FDD\u5B58! \u4F60\u53EF\u4EE5\u5728\u5355\u4EBA\u6A21\u5F0F\u4E2D\u627E\u5230\u4F60\u7684\u5730\u56FE.");
        } else {
            JOptionPane.showMessageDialog(BiomesViewerFrame.this, "\u5730\u56FE\u5DF2\u4FDD\u5B58\u5230 " + worldDir + ".\n\u5C06\u5176\u79FB\u52A8\u5230\u4F60\u7684\u5B58\u6863\u6587\u4EF6\u5939\u4E2D\u6765\u6E38\u73A9.");
        }
    }

    private void createWorld() {
        App app = App.getInstanceIfExists();
        if (! app.saveIfNecessary()) {
            return;
        }
        final NewWorldDialog dialog = new NewWorldDialog(
            app,
            app.getColourScheme(),
            "\u751F\u6210\u4E16\u754C",
            ((Number) seedSpinner.getValue()).longValue(),
            ((Integer) schemeChooser.getSelectedItem() == BIOME_ALGORITHM_1_1) ? DefaultPlugin.JAVA_MCREGION : DefaultPlugin.JAVA_ANVIL,
            NORMAL_DETAIL,
            0,
            Configuration.getInstance().getDefaultMaxHeight(),
            null,
            imageViewer.getSelectedTiles());
        dialog.setVisible(true);
        if (! dialog.isCancelled()) {
            app.clearWorld();
            if (! dialog.checkMemoryRequirements(this)) {
                return;
            }
            World2 newWorld = ProgressDialog.executeTask(this, new ProgressTask<World2>() {
                @Override
                public String getName() {
                    return "\u521B\u5EFA\u65B0\u4E16\u754C";
                }
                
                @Override
                public World2 execute(ProgressReceiver progressReceiver) throws OperationCancelled {
                    return dialog.getSelectedWorld(progressReceiver);
                }
            });
            if (newWorld != null) {
                final Dimension dimension = newWorld.getDimension(NORMAL_DETAIL);
                switch ((Integer) schemeChooser.getSelectedItem()) {
                    case BIOME_ALGORITHM_1_1:
                    case BIOME_ALGORITHM_1_2_AND_1_3_DEFAULT:
                    case BIOME_ALGORITHM_1_7_DEFAULT:
                        dimension.setGenerator(new SeededGenerator(DEFAULT, dimension.getMinecraftSeed()));
                        break;
                    case BIOME_ALGORITHM_1_3_LARGE:
                    case BIOME_ALGORITHM_1_7_LARGE:
                        dimension.setGenerator(new SeededGenerator(LARGE_BIOMES, dimension.getMinecraftSeed()));
                        break;
                }
                app.setWorld(newWorld, true);
            }
        }
    }
    
    private void setControlStates() {
        createWorldButton.setEnabled((! standAloneMode) && (! imageViewer.getSelectedTiles().isEmpty()));
    }

    private final WPTileSelectionViewer imageViewer;
    private final SeedListener seedListener;
    private final JButton createWorldButton;
    private final JSpinner seedSpinner;
    private final JComboBox schemeChooser;
    private final ColourScheme colourScheme;
    private final boolean standAloneMode;
    private BiomeScheme biomeScheme;
    
    private static final long serialVersionUID = 1L;
    
    public interface SeedListener {
        void setSeed(long seed, Generator generator);
    }
    
    class Controller implements MouseListener, MouseMotionListener {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getButton() != MouseEvent.BUTTON1) {
                return;
            }
            Point tileLocation = getTileLocation(e.getX(), e.getY());
            if (imageViewer.isSelectedTile(tileLocation)) {
                imageViewer.removeSelectedTile(tileLocation);
            } else {
                imageViewer.addSelectedTile(tileLocation);
            }
            imageViewer.setSelectedRectangleCorner1(null);
            imageViewer.setSelectedRectangleCorner2(null);
            setControlStates();
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (e.getButton() != MouseEvent.BUTTON1) {
                return;
            }
            selecting = true;
            selectionCorner1 = getTileLocation(e.getX(), e.getY());
            selectionCorner2 = null;
            imageViewer.setSelectedRectangleCorner1(null);
            imageViewer.setSelectedRectangleCorner2(null);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.getButton() != MouseEvent.BUTTON1) {
                return;
            }
            if ((selectionCorner1 != null) && (selectionCorner2 != null)) {
                int tileX1 = Math.min(selectionCorner1.x, selectionCorner2.x);
                int tileX2 = Math.max(selectionCorner1.x, selectionCorner2.x);
                int tileY1 = Math.min(selectionCorner1.y, selectionCorner2.y);
                int tileY2 = Math.max(selectionCorner1.y, selectionCorner2.y);
                for (int x = tileX1; x <= tileX2; x++) {
                    for (int y = tileY1; y <= tileY2; y++) {
                        Point tileLocation = new Point(x, y);
                        if (imageViewer.isSelectedTile(tileLocation)) {
                            imageViewer.removeSelectedTile(tileLocation);
                        } else {
                            imageViewer.addSelectedTile(tileLocation);
                        }
                    }
                }
                setControlStates();
            }
            imageViewer.setSelectedRectangleCorner1(null);
            imageViewer.setSelectedRectangleCorner2(null);
            selecting = false;
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            imageViewer.setHighlightedTileLocation(getTileLocation(e.getX(), e.getY()));
        }

        @Override
        public void mouseExited(MouseEvent e) {
            imageViewer.setHighlightedTileLocation(null);
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            imageViewer.setHighlightedTileLocation(getTileLocation(e.getX(), e.getY()));
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            imageViewer.setHighlightedTileLocation(getTileLocation(e.getX(), e.getY()));
            if (selecting) {
                selectionCorner2 = getTileLocation(e.getX(), e.getY());
                imageViewer.setSelectedRectangleCorner1(selectionCorner1);
                imageViewer.setSelectedRectangleCorner2(selectionCorner2);
            }
        }

        private Point getTileLocation(int x, int y) {
            Point coords = imageViewer.viewToWorld(x, y);
            return new Point(coords.x >> TILE_SIZE_BITS, coords.y >> TILE_SIZE_BITS);
        }

        private boolean selecting;
        private Point selectionCorner1, selectionCorner2;
    }
}