/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * PreferencesDialog.java
 *
 * Created on Apr 26, 2012, 3:18:17 PM
 */
package org.pepsoft.worldpainter;

import org.pepsoft.minecraft.*;
import org.pepsoft.util.GUIUtils;
import org.pepsoft.util.ProgressReceiver;
import org.pepsoft.worldpainter.TileRenderer.LightOrigin;
import org.pepsoft.worldpainter.exporting.ExportSettings;
import org.pepsoft.worldpainter.exporting.ExportSettingsEditor;
import org.pepsoft.worldpainter.plugins.PlatformManager;
import org.pepsoft.worldpainter.plugins.PlatformProvider;
import org.pepsoft.worldpainter.themes.SimpleTheme;
import org.pepsoft.worldpainter.themes.TerrainListCellRenderer;
import org.pepsoft.worldpainter.util.BackupUtils;
import org.pepsoft.worldpainter.util.EnumListCellRenderer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.SortedMap;

import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.SOUTH;
import static java.awt.Color.BLUE;
import static java.awt.Color.GRAY;
import static java.awt.Cursor.HAND_CURSOR;
import static java.awt.FlowLayout.RIGHT;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.pepsoft.minecraft.Constants.DEFAULT_WATER_LEVEL;
import static org.pepsoft.util.swing.MessageUtils.showInfo;
import static org.pepsoft.util.swing.SpinnerUtils.setMaximum;
import static org.pepsoft.util.swing.SpinnerUtils.setMinimum;
import static org.pepsoft.worldpainter.DefaultPlugin.*;
import static org.pepsoft.worldpainter.Dimension.Anchor.NORMAL_DETAIL;
import static org.pepsoft.worldpainter.ExceptionHandler.handleException;
import static org.pepsoft.worldpainter.Generator.*;
import static org.pepsoft.worldpainter.HeightTransform.IDENTITY;
import static org.pepsoft.worldpainter.Platform.Capability.BLOCK_BASED;
import static org.pepsoft.worldpainter.Platform.Capability.NAME_BASED;
import static org.pepsoft.worldpainter.Terrain.GRASS;
import static org.pepsoft.worldpainter.World2.DEFAULT_OCEAN_SEED;
import static org.pepsoft.worldpainter.util.WorldUtils.resizeDimension;

/**
 *
 * @author pepijn
 */
@SuppressWarnings({"unchecked", "rawtypes", "Convert2Lambda", "Anonymous2MethodRef", "unused", "FieldCanBeLocal"}) // Managed by NetBeans
public class PreferencesDialog extends WorldPainterDialog {
    /** Creates new form PreferencesDialog */
    public PreferencesDialog(java.awt.Frame parent, ColourScheme colourScheme) {
        super(parent);
        this.colourScheme = colourScheme;
        
        initComponents();
        
        comboBoxSurfaceMaterial.setModel(new DefaultComboBoxModel(Terrain.PICK_LIST));
        comboBoxSurfaceMaterial.setRenderer(new TerrainListCellRenderer(colourScheme));
        comboBoxMode.setRenderer(new EnumListCellRenderer());
        comboBoxWorldType.setRenderer(new EnumListCellRenderer());

        List<AccelerationType> accelTypes = AccelerationType.getForThisOS();
        radioButtonAccelDefault.setEnabled(accelTypes.contains(AccelerationType.DEFAULT));
        radioButtonAccelDirect3D.setEnabled(accelTypes.contains(AccelerationType.DIRECT3D));
        radioButtonAccelOpenGL.setEnabled(accelTypes.contains(AccelerationType.OPENGL));
        radioButtonAccelQuartz.setEnabled(accelTypes.contains(AccelerationType.QUARTZ));
        radioButtonAccelUnaccelerated.setEnabled(accelTypes.contains(AccelerationType.UNACCELERATED));
        radioButtonAccelXRender.setEnabled(accelTypes.contains(AccelerationType.XRENDER));

        comboBoxPlatform.setModel(new DefaultComboBoxModel<>(PlatformManager.getInstance().getAllPlatforms().toArray(new Platform[0])));
        comboBoxPlatform.setRenderer(new PlatformListCellRenderer());

        final int logicalProcessorCount = Runtime.getRuntime().availableProcessors();
        ((SpinnerNumberModel) spinnerManualThreadCount.getModel()).setMaximum(logicalProcessorCount);
        spinnerManualThreadCount.setValue(logicalProcessorCount);

        loadSettings();
        
        rootPane.setDefaultButton(buttonOK);
        scaleToUI();
        pack();
        setLocationRelativeTo(parent);
    }
    
    public void ok() {
        saveSettings();
        super.ok();
    }
    
    private void loadSettings() {
        programmaticChange = true;
        try {
            Configuration config = Configuration.getInstance();
            if (Main.privateContext == null) {
                checkBoxPing.setSelected(false);
                checkBoxPing.setEnabled(false);
                pingNotSet = true;
            } else if (config.getPingAllowed() != null) {
                checkBoxPing.setSelected(config.getPingAllowed());
            } else {
                checkBoxPing.setSelected(false);
                pingNotSet = true;
            }
            if ((Main.privateContext == null)
                    || "true".equals(System.getProperty("org.pepsoft.worldpainter.devMode"))
                    || "true".equals(System.getProperty("org.pepsoft.worldpainter.disableUpdateCheck"))) {
                checkBoxCheckForUpdates.setSelected(false);
                checkBoxCheckForUpdates.setEnabled(false);
            } else {
                checkBoxCheckForUpdates.setSelected(config.isCheckForUpdates());
            }
            if ("true".equals(System.getProperty("org.pepsoft.worldpainter.disableUndo"))) {
                checkBoxUndo.setSelected(false);
                checkBoxUndo.setEnabled(false);
                spinnerUndoLevels.setEnabled(false);
            } else {
                checkBoxUndo.setSelected(config.isUndoEnabled());
                spinnerUndoLevels.setValue(config.getUndoLevels());
            }

            checkBoxGrid.setSelected(config.isDefaultGridEnabled());
            spinnerGrid.setValue(config.getDefaultGridSize());
            checkBoxContours.setSelected(config.isDefaultContoursEnabled());
            spinnerContours.setValue(config.getDefaultContourSeparation());
            checkBoxViewDistance.setSelected(config.isDefaultViewDistanceEnabled());
            checkBoxWalkingDistance.setSelected(config.isDefaultWalkingDistanceEnabled());
            comboBoxLightDirection.setSelectedItem(config.getDefaultLightOrigin());
            checkBoxCircular.setSelected(config.isDefaultCircularWorld());
            spinnerBrushSize.setValue(config.getMaximumBrushSize());

            defaultTerrainAndLayerSettings = config.getDefaultTerrainAndLayerSettings(); // TODO this should be cloned too
            defaultExportSettings = (config.getDefaultExportSettings() != null) ? config.getDefaultExportSettings().clone() : null;
            checkBoxResourcesEverywhere.setSelected(config.getDefaultResourcesMinimumLevel() > 0);

            spinnerWidth.setValue(config.getDefaultWidth() * 128);
            spinnerHeight.setValue(config.getDefaultHeight() * 128);
            previousPlatform = config.getDefaultPlatform();
            comboBoxPlatform.setSelectedItem(previousPlatform);
            setControlStatesForPlatform();
            comboBoxHeight.setSelectedItem(config.getDefaultMaxHeight());
            if (config.isHilly()) {
                radioButtonHilly.setSelected(true);
            } else {
                radioButtonFlat.setSelected(true);
                spinnerRange.setEnabled(false);
                spinnerScale.setEnabled(false);
            }
            spinnerRange.setValue(Math.round(config.getDefaultRange()));
            spinnerScale.setValue((int) Math.round(config.getDefaultScale() * 100));
            spinnerGroundLevel.setValue(config.getLevel());
            spinnerWaterLevel.setValue(config.getWaterLevel());
            checkBoxLava.setSelected(config.isLava());
            checkBoxBeaches.setSelected(config.isBeaches());
            comboBoxSurfaceMaterial.setSelectedItem(config.getSurface());
            spinnerWorldBackups.setValue(config.getWorldFileBackups());
            checkBoxExtendedBlockIds.setSelected(config.isDefaultExtendedBlockIds());

            // Export settings
            checkBoxChestOfGoodies.setSelected(config.isDefaultCreateGoodiesChest());
            comboBoxWorldType.setSelectedItem(config.getDefaultGenerator().getType());
            generatorOptions = (config.getDefaultGenerator() instanceof CustomGenerator) ? ((CustomGenerator) config.getDefaultGenerator()).getName() : null;
            checkBoxStructures.setSelected(config.isDefaultMapFeatures());
            comboBoxMode.setSelectedItem(config.getDefaultGameType());
            checkBoxCheats.setSelected(config.isDefaultAllowCheats());

            previousMaxHeight = config.getDefaultMaxHeight();

            comboBoxLookAndFeel.setSelectedIndex(config.getLookAndFeel() != null ? config.getLookAndFeel().ordinal() : 0);
            if (config.getUiScale() == 0.0f) {
                radioButtonUIScaleAuto.setSelected(true);
                sliderUIScale.setValue((int) (GUIUtils.SYSTEM_UI_SCALE_FLOAT * 100));
            } else {
                radioButtonUIScaleManual.setSelected(true);
                sliderUIScale.setValue((int) (config.getUiScale() * 100));
            }
            updateLabelUIScale();

            switch (config.getAccelerationType()) {
                case DEFAULT:
                    radioButtonAccelDefault.setSelected(true);
                    break;
                case DIRECT3D:
                    radioButtonAccelDirect3D.setSelected(true);
                    break;
                case OPENGL:
                    radioButtonAccelOpenGL.setSelected(true);
                    break;
                case QUARTZ:
                    radioButtonAccelQuartz.setSelected(true);
                    break;
                case UNACCELERATED:
                    radioButtonAccelUnaccelerated.setSelected(true);
                    break;
                case XRENDER:
                    radioButtonAccelXRender.setSelected(true);
                    break;
            }

            switch (config.getOverlayType()) {
                case OPTIMISE_ON_LOAD:
                    radioButtonOverlayOptimiseOnLoad.setSelected(true);
                    break;
                case SCALE_ON_LOAD:
                    radioButtonOverlayScaleOnLoad.setSelected(true);
                    break;
                case SCALE_ON_PAINT:
                    radioButtonOverlayScaleOnPaint.setSelected(true);
                    break;
            }

            checkBoxAutoSave.setSelected(config.isAutosaveEnabled());
            spinnerAutoSaveGuardTime.setValue(config.getAutosaveDelay() / 1000);
            spinnerAutoSaveInterval.setValue(config.getAutosaveInterval() / 1000);
            spinnerFreeSpaceForMaps.setValue(config.getMinimumFreeSpaceForMaps());
            checkBoxAutoDeleteBackups.setSelected(config.isAutoDeleteBackups());

            final String sysProp = System.getProperty("org.pepsoft.worldpainter.threads");
            final boolean maxThreadCountFixed = sysProp != null;
            if (maxThreadCountFixed) {
                radioButtonThreadCountManual.setSelected(true);
                spinnerManualThreadCount.setValue(Integer.parseInt(sysProp));
                radioButtonThreadCountAuto.setToolTipText("Overridden by org.pepsoft.worldpainter.threads advanced setting");
                radioButtonThreadCountManual.setToolTipText("Overridden by org.pepsoft.worldpainter.threads advanced setting");
                spinnerManualThreadCount.setToolTipText("Overridden by org.pepsoft.worldpainter.threads advanced setting");
            } else if (config.getMaxThreadCount() != null) {
                radioButtonThreadCountManual.setSelected(true);
                spinnerManualThreadCount.setValue(config.getMaxThreadCount());
            } else {
                radioButtonThreadCountAuto.setSelected(true);
            }
            radioButtonThreadCountAuto.setEnabled(! maxThreadCountFixed);
            radioButtonThreadCountManual.setEnabled(! maxThreadCountFixed);

            setControlStates();
        } finally {
            programmaticChange = false;
        }
    }
    
    private void saveSettings() {
        Configuration config = Configuration.getInstance();
        if (! pingNotSet) {
            config.setPingAllowed(checkBoxPing.isSelected());
        }
        if ((! "true".equals(System.getProperty("org.pepsoft.worldpainter.devMode")))
                && (! "true".equals(System.getProperty("org.pepsoft.worldpainter.disableUpdateCheck")))) {
            config.setCheckForUpdates(checkBoxCheckForUpdates.isSelected());
        }
        if (! "true".equals(System.getProperty("org.pepsoft.worldpainter.disableUndo"))) {
            config.setUndoEnabled(checkBoxUndo.isSelected());
            config.setUndoLevels(((Number) spinnerUndoLevels.getValue()).intValue());
        }
        config.setDefaultGridEnabled(checkBoxGrid.isSelected());
        config.setDefaultGridSize((Integer) spinnerGrid.getValue());
        config.setDefaultContoursEnabled(checkBoxContours.isSelected());
        config.setDefaultContourSeparation((Integer) spinnerContours.getValue());
        config.setDefaultViewDistanceEnabled(checkBoxViewDistance.isSelected());
        config.setDefaultWalkingDistanceEnabled(checkBoxWalkingDistance.isSelected());
        config.setDefaultLightOrigin((LightOrigin) comboBoxLightDirection.getSelectedItem());
        config.setDefaultWidth(((Integer) spinnerWidth.getValue()) / 128);
        // Set defaultCircularWorld *before* defaultHeight, otherwise defaultHeight might not take
        config.setDefaultCircularWorld(checkBoxCircular.isSelected());
        config.setDefaultHeight(((Integer) spinnerHeight.getValue()) / 128);
        final Platform platform = (Platform) comboBoxPlatform.getSelectedItem();
        config.setDefaultPlatform(platform);
        config.setDefaultMaxHeight((Integer) comboBoxHeight.getSelectedItem());
        config.setHilly(radioButtonHilly.isSelected());
        config.setDefaultRange(((Number) spinnerRange.getValue()).floatValue());
        config.setDefaultScale((Integer) spinnerScale.getValue() / 100.0);
        config.setLevel((Integer) spinnerGroundLevel.getValue());
        config.setWaterLevel((Integer) spinnerWaterLevel.getValue());
        config.setLava(checkBoxLava.isSelected());
        config.setBeaches(checkBoxBeaches.isSelected());
        config.setSurface((Terrain) comboBoxSurfaceMaterial.getSelectedItem());
        config.setWorldFileBackups((Integer) spinnerWorldBackups.getValue());
        config.setMaximumBrushSize((Integer) spinnerBrushSize.getValue());
        config.setDefaultExtendedBlockIds(checkBoxExtendedBlockIds.isSelected());
        
        // Export settings
        config.setDefaultCreateGoodiesChest(checkBoxChestOfGoodies.isSelected());
        final MapGenerator defaultGenerator;
        final Generator generatorType = (Generator) comboBoxWorldType.getSelectedItem();
        switch (generatorType) {
            case DEFAULT:
            case LARGE_BIOMES:
            case AMPLIFIED:
            case NETHER:
            case END:
                defaultGenerator = new SeededGenerator(generatorType, DEFAULT_OCEAN_SEED);
                break;
            case FLAT:
                defaultGenerator = new SuperflatGenerator(SuperflatPreset.defaultPreset(platform));
                break;
            default:
                throw new InternalError("Generator type " + generatorType + " not supported");
        }
        config.setDefaultGenerator(defaultGenerator);
        config.setDefaultMapFeatures(checkBoxStructures.isSelected());
        config.setDefaultGameType((GameType) comboBoxMode.getSelectedItem());
        config.setDefaultAllowCheats(checkBoxCheats.isSelected());

        config.setLookAndFeel(Configuration.LookAndFeel.values()[comboBoxLookAndFeel.getSelectedIndex()]);
        if (radioButtonUIScaleAuto.isSelected()) {
            config.setUiScale(0.0f);
        } else {
            config.setUiScale(sliderUIScale.getValue() / 100.0f);
        }
        
        if (radioButtonAccelDefault.isSelected()) {
            config.setAccelerationType(AccelerationType.DEFAULT);
        } else if (radioButtonAccelDirect3D.isSelected()) {
            config.setAccelerationType(AccelerationType.DIRECT3D);
        } else if (radioButtonAccelOpenGL.isSelected()) {
            config.setAccelerationType(AccelerationType.OPENGL);
        } else if (radioButtonAccelQuartz.isSelected()) {
            config.setAccelerationType(AccelerationType.QUARTZ);
        } else if (radioButtonAccelUnaccelerated.isSelected()) {
            config.setAccelerationType(AccelerationType.UNACCELERATED);
        } else if (radioButtonAccelXRender.isSelected()) {
            config.setAccelerationType(AccelerationType.XRENDER);
        }
        
        if (radioButtonOverlayOptimiseOnLoad.isSelected()) {
            config.setOverlayType(Configuration.OverlayType.OPTIMISE_ON_LOAD);
        } else if (radioButtonOverlayScaleOnLoad.isSelected()) {
            config.setOverlayType(Configuration.OverlayType.SCALE_ON_LOAD);
        } else if (radioButtonOverlayScaleOnPaint.isSelected()) {
            config.setOverlayType(Configuration.OverlayType.SCALE_ON_PAINT);
        }
        
        config.setAutosaveEnabled(checkBoxAutoSave.isSelected());
        config.setAutosaveDelay(((Integer) spinnerAutoSaveGuardTime.getValue()) * 1000);
        config.setAutosaveInterval(((Integer) spinnerAutoSaveInterval.getValue()) * 1000);
        config.setMinimumFreeSpaceForMaps((Integer) spinnerFreeSpaceForMaps.getValue());
        config.setAutoDeleteBackups(checkBoxAutoDeleteBackups.isSelected());

        config.setDefaultExportSettings(defaultExportSettings);
        config.setDefaultResourcesMinimumLevel(checkBoxResourcesEverywhere.isSelected() ? 8 : 0);

        if (radioButtonThreadCountManual.isEnabled()) {
            if (radioButtonThreadCountAuto.isSelected()) {
                config.setMaxThreadCount(null);
            } else {
                config.setMaxThreadCount((int) spinnerManualThreadCount.getValue());
            }
        }

        try {
            config.save();
        } catch (IOException e) {
            handleException(e, this);
        }
    }
    
    private void setControlStates() {
        spinnerUndoLevels.setEnabled(checkBoxUndo.isSelected());
        final boolean hilly = radioButtonHilly.isSelected();
        spinnerRange.setEnabled(hilly);
        spinnerScale.setEnabled(hilly);
        spinnerHeight.setEnabled(! checkBoxCircular.isSelected());
        buttonModePreset.setEnabled(comboBoxWorldType.getSelectedItem() == FLAT);
        final boolean autosaveEnabled = checkBoxAutoSave.isSelected();
        final boolean autosaveInhibited = Configuration.getInstance().isAutosaveInhibited();
        checkBoxAutoSave.setEnabled(! autosaveInhibited);
        spinnerAutoSaveGuardTime.setEnabled(autosaveEnabled && (! autosaveInhibited));
        spinnerAutoSaveInterval.setEnabled(autosaveEnabled && (! autosaveInhibited));
        sliderUIScale.setEnabled(radioButtonUIScaleManual.isSelected());
        spinnerManualThreadCount.setEnabled(radioButtonThreadCountManual.isSelected() && radioButtonThreadCountManual.isEnabled());
    }

    private void updateLabelUIScale() {
        if (radioButtonUIScaleAuto.isSelected()) {
            labelUIScale.setText((int) (GUIUtils.SYSTEM_UI_SCALE_FLOAT * 100) + "%");
        } else {
            labelUIScale.setText(sliderUIScale.getValue() + "%");
        }
    }
    
    private void editTerrainAndLayerSettings() {
        Configuration config = Configuration.getInstance();
        defaultTerrainAndLayerSettings.getWorld().setPlatform((Platform) comboBoxPlatform.getSelectedItem());
        DimensionPropertiesDialog dialog = new DimensionPropertiesDialog(this, defaultTerrainAndLayerSettings, colourScheme, null, true);
        dialog.setVisible(true);
        TileFactory tileFactory = defaultTerrainAndLayerSettings.getTileFactory();
        if ((tileFactory instanceof HeightMapTileFactory)
                && (((HeightMapTileFactory) tileFactory).getTheme() instanceof SimpleTheme)) {
            HeightMapTileFactory heightMapTileFactory = (HeightMapTileFactory) tileFactory;
            SimpleTheme theme = (SimpleTheme) ((HeightMapTileFactory) tileFactory).getTheme();
            checkBoxBeaches.setSelected(theme.isBeaches());
            int waterLevel = heightMapTileFactory.getWaterHeight();
            spinnerWaterLevel.setValue(waterLevel);
            defaultTerrainAndLayerSettings.setBorderLevel(heightMapTileFactory.getWaterHeight());
            SortedMap<Integer, Terrain> terrainRanges = theme.getTerrainRanges();
            comboBoxSurfaceMaterial.setSelectedItem(terrainRanges.get(terrainRanges.headMap(waterLevel + 3).lastKey()));
        }
    }
    
    private void cleanUpBackupsNow() {
        Configuration config = Configuration.getInstance();
        int oldMinimumFreeSpaceForMaps = config.getMinimumFreeSpaceForMaps();
        config.setMinimumFreeSpaceForMaps((Integer) spinnerFreeSpaceForMaps.getValue());
        try {
            BackupUtils.cleanUpBackups(null, this);
        } catch (IOException e) {
            throw new RuntimeException("I/O error while cleaning backups", e);
        } finally {
            config.setMinimumFreeSpaceForMaps(oldMinimumFreeSpaceForMaps);
        }
    }

    private void platformSelected() {
        if (programmaticChange) {
            return;
        }
        programmaticChange = true;
        try {
            final Platform platform = (Platform) comboBoxPlatform.getSelectedItem();
            final Generator currentGenerator = (Generator) comboBoxWorldType.getSelectedItem();
            final List<Generator> supportedGenerators = new ArrayList<>(platform.supportedGenerators);
            supportedGenerators.retainAll(asList(DEFAULT, LARGE_BIOMES, AMPLIFIED, NETHER, END, FLAT));
            comboBoxWorldType.setModel(new DefaultComboBoxModel<>(supportedGenerators.toArray(new Generator[0])));
            if ((currentGenerator != null) && supportedGenerators.contains(currentGenerator)) {
                comboBoxWorldType.setSelectedItem(currentGenerator);
            }
            final Integer currentMaxHeight = (Integer) comboBoxHeight.getSelectedItem();
            final boolean useStandardMaxHeight = (currentMaxHeight == null) || (currentMaxHeight == previousPlatform.standardMaxHeight);
            final List<Integer> supportedMaxHeights = stream(platform.maxHeights).boxed().collect(toList());
            comboBoxHeight.setModel(new DefaultComboBoxModel<>(supportedMaxHeights.toArray(new Integer[0])));
            final int newMinHeight = platform.minZ /* TODO: make configurable */, newMaxHeight;
            if ((! useStandardMaxHeight) && supportedMaxHeights.contains(currentMaxHeight)) {
                newMaxHeight = currentMaxHeight;
            } else {
                newMaxHeight = platform.standardMaxHeight;
            }
            comboBoxHeight.setSelectedItem(newMaxHeight);
            setMinimum(spinnerGroundLevel, newMinHeight);
            setMaximum(spinnerGroundLevel, newMaxHeight - 1);
            setMinimum(spinnerWaterLevel, newMinHeight);
            setMaximum(spinnerWaterLevel, newMaxHeight - 1);
            setMinimum(spinnerRange, newMinHeight);
            setMaximum(spinnerRange, newMaxHeight - 1);
            final GameType currentGameType = (GameType) comboBoxMode.getSelectedItem();
            final List<GameType> supportedGameTypes = platform.supportedGameTypes;
            comboBoxMode.setModel(new DefaultComboBoxModel<>(supportedGameTypes.toArray(new GameType[0])));
            if ((currentGameType != null) && supportedGameTypes.contains(currentGameType)) {
                comboBoxMode.setSelectedItem(currentGameType);
            }
            checkBoxChestOfGoodies.setEnabled((platform != JAVA_ANVIL_1_15) && (platform != JAVA_ANVIL_1_17));
            checkBoxExtendedBlockIds.setEnabled(platform.capabilities.contains(BLOCK_BASED) && (!platform.capabilities.contains(NAME_BASED)) && (platform != JAVA_MCREGION));
            try {
                resizeDimension(defaultTerrainAndLayerSettings, newMinHeight, newMaxHeight, IDENTITY, true, null);
            } catch (ProgressReceiver.OperationCancelled e) {
                throw new InternalError(); // Can't happen since we don't pass in a ProgressReceiver
            }

            // Check whether this platform supports the current default export settings (or any export settings)
            final PlatformProvider platformProvider = PlatformManager.getInstance().getPlatformProvider(platform);
            final ExportSettings platformDefaultExportSettings = platformProvider.getDefaultExportSettings(platform);
            if (platformDefaultExportSettings != null) {
                labelEditExportSettingsLink.setForeground(BLUE);
                labelEditExportSettingsLink.setCursor(new Cursor(HAND_CURSOR));
                if ((defaultExportSettings != null) && (platformDefaultExportSettings.getClass() != defaultExportSettings.getClass())) {
                    defaultExportSettings = null;
                }
            } else {
                defaultExportSettings = null;
                labelEditExportSettingsLink.setForeground(GRAY);
                labelEditExportSettingsLink.setCursor(null);
            }

            previousPlatform = platform;
        } finally {
            programmaticChange = false;
        }
    }

    private void setControlStatesForPlatform() {
        final Configuration config = Configuration.getInstance();
        final Platform platform = config.getDefaultPlatform();
        final List<Generator> supportedGenerators = new ArrayList<>(platform.supportedGenerators);
        supportedGenerators.retainAll(asList(DEFAULT, LARGE_BIOMES, AMPLIFIED, NETHER, END, FLAT));
        comboBoxWorldType.setModel(new DefaultComboBoxModel<>(supportedGenerators.toArray(new Generator[0])));
        final List<Integer> supportedMaxHeights = stream(platform.maxHeights).boxed().collect(toList());
        comboBoxHeight.setModel(new DefaultComboBoxModel<>(supportedMaxHeights.toArray(new Integer[0])));
        final List<GameType> supportedGameTypes = platform.supportedGameTypes;
        comboBoxMode.setModel(new DefaultComboBoxModel<>(supportedGameTypes.toArray(new GameType[0])));
        checkBoxChestOfGoodies.setEnabled((platform != JAVA_ANVIL_1_15) && (platform != JAVA_ANVIL_1_17));
        checkBoxExtendedBlockIds.setEnabled(platform.capabilities.contains(BLOCK_BASED) && (!platform.capabilities.contains(NAME_BASED)) && (platform != JAVA_MCREGION));

        // Check whether this platform supports the current default export settings (or any export settings)
        final PlatformProvider platformProvider = PlatformManager.getInstance().getPlatformProvider(platform);
        final ExportSettings platformDefaultExportSettings = platformProvider.getDefaultExportSettings(platform);
        if (platformDefaultExportSettings != null) {
            labelEditExportSettingsLink.setForeground(BLUE);
            labelEditExportSettingsLink.setCursor(new Cursor(HAND_CURSOR));
        } else {
            labelEditExportSettingsLink.setForeground(GRAY);
            labelEditExportSettingsLink.setCursor(null);
        }

        final int maxHeight = config.getDefaultMaxHeight();
        ((SpinnerNumberModel) spinnerGroundLevel.getModel()).setMaximum(maxHeight - 1);
        ((SpinnerNumberModel) spinnerWaterLevel.getModel()).setMaximum(maxHeight - 1);
        ((SpinnerNumberModel) spinnerRange.getModel()).setMaximum(maxHeight - 1);
    }

    private void editDefaultExportSettings() {
        final Platform platform = (Platform) comboBoxPlatform.getSelectedItem();
        final PlatformProvider platformProvider = PlatformManager.getInstance().getPlatformProvider(platform);
        final ExportSettings platformDefaultExportSettings = platformProvider.getDefaultExportSettings(platform);
        if (platformDefaultExportSettings != null) {
            final ExportSettingsEditor editor = platformProvider.getExportSettingsEditor(platform);
            if ((defaultExportSettings != null) && (defaultExportSettings.getClass() == platformDefaultExportSettings.getClass())) {
                editor.setExportSettings(defaultExportSettings);
            } else {
                editor.setExportSettings(platformDefaultExportSettings);
            }
            final WorldPainterDialog dialog = new WorldPainterDialog(this);
            dialog.setTitle("\u914D\u7F6E\u9ED8\u8BA4\u540E\u5904\u7406\u8BBE\u7F6E");
            dialog.getContentPane().add(editor, CENTER);
            final JPanel panel = new JPanel(new FlowLayout(RIGHT));
            final boolean[] reset = { false };
            panel.add(new JButton(new AbstractAction("\u91CD\u7F6E") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    reset[0] = true;
                    dialog.ok();
                }
            }));
            final JButton okButton = new JButton(new AbstractAction("\u786E\u8BA4") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    dialog.ok();
                }
            });
            panel.add(okButton);
            panel.add(new JButton(new AbstractAction("\u53d6\u6d88") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    dialog.cancel();
                }
            }));
            dialog.getContentPane().add(panel, SOUTH);
            dialog.getRootPane().setDefaultButton(okButton);
            dialog.pack();
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);
            if (! dialog.isCancelled()) {
                if (reset[0]) {
                    defaultExportSettings = null;
                    showInfo(this, "\u9ED8\u8BA4\u540E\u5904\u7406\u8BBE\u7F6E\u5DF2\u88AB\u91CD\u7F6E\u4E3A\u9ED8\u8BA4\u503C.", "\u9ED8\u8BA4\u540E\u5904\u7406\u8BBE\u7F6E\u91CD\u7F6E");
                } else {
                    defaultExportSettings = editor.getExportSettings();
                }
            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        buttonGroup3 = new javax.swing.ButtonGroup();
        buttonGroup4 = new javax.swing.ButtonGroup();
        buttonGroup5 = new javax.swing.ButtonGroup();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        checkBoxPing = new javax.swing.JCheckBox();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        checkBoxCheckForUpdates = new javax.swing.JCheckBox();
        jLabel20 = new javax.swing.JLabel();
        spinnerWorldBackups = new javax.swing.JSpinner();
        jLabel30 = new javax.swing.JLabel();
        comboBoxLookAndFeel = new javax.swing.JComboBox();
        jLabel32 = new javax.swing.JLabel();
        checkBoxAutoSave = new javax.swing.JCheckBox();
        jLabel45 = new javax.swing.JLabel();
        spinnerAutoSaveGuardTime = new javax.swing.JSpinner();
        jLabel46 = new javax.swing.JLabel();
        spinnerAutoSaveInterval = new javax.swing.JSpinner();
        jLabel47 = new javax.swing.JLabel();
        jLabel48 = new javax.swing.JLabel();
        checkBoxUndo = new javax.swing.JCheckBox();
        jLabel5 = new javax.swing.JLabel();
        spinnerUndoLevels = new javax.swing.JSpinner();
        jLabel2 = new javax.swing.JLabel();
        sliderUIScale = new javax.swing.JSlider();
        labelUIScale = new javax.swing.JLabel();
        radioButtonUIScaleAuto = new javax.swing.JRadioButton();
        radioButtonUIScaleManual = new javax.swing.JRadioButton();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel49 = new javax.swing.JLabel();
        jLabel50 = new javax.swing.JLabel();
        spinnerFreeSpaceForMaps = new javax.swing.JSpinner();
        jLabel51 = new javax.swing.JLabel();
        checkBoxAutoDeleteBackups = new javax.swing.JCheckBox();
        buttonCleanUpBackupsNow = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        checkBoxGrid = new javax.swing.JCheckBox();
        checkBoxContours = new javax.swing.JCheckBox();
        checkBoxViewDistance = new javax.swing.JCheckBox();
        jLabel22 = new javax.swing.JLabel();
        comboBoxLightDirection = new javax.swing.JComboBox();
        jLabel7 = new javax.swing.JLabel();
        spinnerGrid = new javax.swing.JSpinner();
        jLabel8 = new javax.swing.JLabel();
        spinnerContours = new javax.swing.JSpinner();
        checkBoxWalkingDistance = new javax.swing.JCheckBox();
        jLabel26 = new javax.swing.JLabel();
        spinnerBrushSize = new javax.swing.JSpinner();
        jLabel21 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        jLabel10 = new javax.swing.JLabel();
        spinnerWidth = new javax.swing.JSpinner();
        jLabel11 = new javax.swing.JLabel();
        spinnerHeight = new javax.swing.JSpinner();
        jLabel19 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        comboBoxHeight = new javax.swing.JComboBox<>();
        jLabel13 = new javax.swing.JLabel();
        radioButtonHilly = new javax.swing.JRadioButton();
        jLabel23 = new javax.swing.JLabel();
        spinnerRange = new javax.swing.JSpinner();
        jLabel24 = new javax.swing.JLabel();
        spinnerScale = new javax.swing.JSpinner();
        jLabel25 = new javax.swing.JLabel();
        radioButtonFlat = new javax.swing.JRadioButton();
        checkBoxCircular = new javax.swing.JCheckBox();
        jLabel14 = new javax.swing.JLabel();
        spinnerGroundLevel = new javax.swing.JSpinner();
        jLabel15 = new javax.swing.JLabel();
        spinnerWaterLevel = new javax.swing.JSpinner();
        checkBoxLava = new javax.swing.JCheckBox();
        checkBoxBeaches = new javax.swing.JCheckBox();
        jLabel16 = new javax.swing.JLabel();
        comboBoxSurfaceMaterial = new javax.swing.JComboBox();
        checkBoxExtendedBlockIds = new javax.swing.JCheckBox();
        buttonReset = new javax.swing.JButton();
        labelTerrainAndLayerSettings = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jSeparator5 = new javax.swing.JSeparator();
        checkBoxChestOfGoodies = new javax.swing.JCheckBox();
        jLabel28 = new javax.swing.JLabel();
        comboBoxWorldType = new javax.swing.JComboBox<>();
        buttonModePreset = new javax.swing.JButton();
        checkBoxStructures = new javax.swing.JCheckBox();
        jLabel29 = new javax.swing.JLabel();
        comboBoxMode = new javax.swing.JComboBox<>();
        checkBoxCheats = new javax.swing.JCheckBox();
        jLabel52 = new javax.swing.JLabel();
        comboBoxPlatform = new javax.swing.JComboBox<>();
        labelEditExportSettingsLink = new javax.swing.JLabel();
        checkBoxResourcesEverywhere = new javax.swing.JCheckBox();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel31 = new javax.swing.JLabel();
        radioButtonAccelDefault = new javax.swing.JRadioButton();
        radioButtonAccelDirect3D = new javax.swing.JRadioButton();
        radioButtonAccelOpenGL = new javax.swing.JRadioButton();
        radioButtonAccelQuartz = new javax.swing.JRadioButton();
        radioButtonAccelXRender = new javax.swing.JRadioButton();
        radioButtonAccelUnaccelerated = new javax.swing.JRadioButton();
        jLabel33 = new javax.swing.JLabel();
        jLabel34 = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
        jLabel37 = new javax.swing.JLabel();
        jLabel38 = new javax.swing.JLabel();
        jLabel39 = new javax.swing.JLabel();
        jLabel35 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel40 = new javax.swing.JLabel();
        jLabel41 = new javax.swing.JLabel();
        radioButtonOverlayScaleOnLoad = new javax.swing.JRadioButton();
        radioButtonOverlayOptimiseOnLoad = new javax.swing.JRadioButton();
        radioButtonOverlayScaleOnPaint = new javax.swing.JRadioButton();
        jLabel42 = new javax.swing.JLabel();
        jLabel43 = new javax.swing.JLabel();
        jLabel44 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jLabel53 = new javax.swing.JLabel();
        radioButtonThreadCountAuto = new javax.swing.JRadioButton();
        radioButtonThreadCountManual = new javax.swing.JRadioButton();
        spinnerManualThreadCount = new javax.swing.JSpinner();
        jLabel54 = new javax.swing.JLabel();
        buttonCancel = new javax.swing.JButton();
        buttonOK = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("\u504F\u597D\u8BBE\u7F6E");

        checkBoxPing.setSelected(true);
        checkBoxPing.setText("\u5C06\u4F7F\u7528\u4FE1\u606F\u53D1\u9001\u7ED9\u5F00\u53D1\u8005");
        checkBoxPing.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxPingActionPerformed(evt);
            }
        });

        jLabel3.setFont(jLabel3.getFont().deriveFont((jLabel3.getFont().getStyle() | java.awt.Font.ITALIC)));
        jLabel3.setText("\u4FE1\u606F\u4E0D\u5305\u542B\u80FD\u8BC6\u522B\u4E2A\u4EBA\u7684\u4FE1\u606F");

        jLabel4.setFont(jLabel4.getFont().deriveFont((jLabel4.getFont().getStyle() | java.awt.Font.ITALIC)));
        jLabel4.setText("\u4E5F\u7EDD\u4E0D\u4F1A\u51FA\u552E\u6216\u8F6C\u8BA9\u7ED9\u7B2C\u4E09\u65B9. ");

        checkBoxCheckForUpdates.setSelected(true);
        checkBoxCheckForUpdates.setText("\u542F\u52A8\u65F6\u68C0\u67E5\u66F4\u65B0");

        jLabel20.setText("\u8981\u4FDD\u5B58\u7684 .world \u6587\u4EF6\u6570\u91CF:");

        spinnerWorldBackups.setModel(new javax.swing.SpinnerNumberModel(3, 0, null, 1));

        jLabel30.setText("\u89C6\u89C9\u4E3B\u9898:");

        comboBoxLookAndFeel.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "\u7CFB\u7EDF", "\u91D1\u5C5E", "\u5149\u73AF", "\u6697\u91D1\u5C5E", "\u6697\u5149\u73AF" }));
        comboBoxLookAndFeel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboBoxLookAndFeelActionPerformed(evt);
            }
        });

        jLabel32.setText("<html><em>\u4E0B\u6B21\u542F\u52A8\u751F\u6548  </em></html>");

        checkBoxAutoSave.setSelected(true);
        checkBoxAutoSave.setText("\u5F00\u542F\u81EA\u52A8\u4FDD\u5B58");
        checkBoxAutoSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxAutoSaveActionPerformed(evt);
            }
        });

        jLabel45.setText("\u4FDD\u62A4\u95F4\u9694:");

        spinnerAutoSaveGuardTime.setModel(new javax.swing.SpinnerNumberModel(10, 1, 999, 1));

        jLabel46.setText("\u81EA\u52A8\u4FDD\u5B58\u95F4\u9694:");

        spinnerAutoSaveInterval.setModel(new javax.swing.SpinnerNumberModel(300, 1, 9999, 1));

        jLabel47.setText("\u79D2");

        jLabel48.setText("\u79D2");

        checkBoxUndo.setSelected(true);
        checkBoxUndo.setText("\u5F00\u542F\u64A4\u9500");
        checkBoxUndo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxUndoActionPerformed(evt);
            }
        });

        jLabel5.setLabelFor(spinnerUndoLevels);
        jLabel5.setText("\u53EF\u64A4\u9500\u6B21\u6570:");

        spinnerUndoLevels.setModel(new javax.swing.SpinnerNumberModel(25, 1, 999, 1));

        jLabel2.setLabelFor(sliderUIScale);
        jLabel2.setText("UI\u6BD4\u4F8B:");

        sliderUIScale.setMajorTickSpacing(25);
        sliderUIScale.setMaximum(400);
        sliderUIScale.setMinimum(25);
        sliderUIScale.setSnapToTicks(true);
        sliderUIScale.setValue(100);
        sliderUIScale.setEnabled(false);
        sliderUIScale.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sliderUIScaleStateChanged(evt);
            }
        });

        labelUIScale.setText("100 %");

        buttonGroup4.add(radioButtonUIScaleAuto);
        radioButtonUIScaleAuto.setText("\u81EA\u52A8:");
        radioButtonUIScaleAuto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonUIScaleAutoActionPerformed(evt);
            }
        });

        buttonGroup4.add(radioButtonUIScaleManual);
        radioButtonUIScaleManual.setText("\u624B\u52A8:");
        radioButtonUIScaleManual.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonUIScaleManualActionPerformed(evt);
            }
        });

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);

        jLabel49.setText("\u5907\u4EFD\u8BBE\u7F6E");

        jLabel50.setText("\u9A71\u52A8\u5668\u4E0A\u7684\u6700\u5C0F\u53EF\u7528\u7A7A\u95F4:");

        spinnerFreeSpaceForMaps.setModel(new javax.swing.SpinnerNumberModel(2, 1, 999, 1));

        jLabel51.setText("GB");

        checkBoxAutoDeleteBackups.setSelected(true);
        checkBoxAutoDeleteBackups.setText("\u6839\u636E\u9700\u8981\uFF0C\u5728\u5BFC\u51FA\u548C\u5408\u5E76\u65F6\u5220\u9664\u65E7\u5730\u56FE\u5907\u4EFD");
        checkBoxAutoDeleteBackups.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxAutoDeleteBackupsActionPerformed(evt);
            }
        });

        buttonCleanUpBackupsNow.setText("\u73B0\u5728\u5C31\u6E05\u9664\u5907\u4EFD\u6587\u4EF6");
        buttonCleanUpBackupsNow.setToolTipText("\u5220\u9664\u5907\u4EFD\uFF0C\u5148\u5220\u9664\u6700\u65E7\u7684\u5907\u4EFD\uFF0C\u76F4\u5230\u81F3\u5C11\u6709\u6307\u5B9A\u7684\u53EF\u7528\u7A7A\u95F4");
        buttonCleanUpBackupsNow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonCleanUpBackupsNowActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(checkBoxPing)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(checkBoxCheckForUpdates)
                            .addComponent(checkBoxUndo)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel30)
                                    .addComponent(jLabel2))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(comboBoxLookAndFeel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel32, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(radioButtonUIScaleManual)
                                            .addComponent(radioButtonUIScaleAuto))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(labelUIScale)
                                            .addComponent(sliderUIScale, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(12, 12, 12)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel4)
                                    .addComponent(jLabel3)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel5)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(spinnerUndoLevels, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                        .addGap(22, 22, 22)
                        .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel49)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel20)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spinnerWorldBackups, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(checkBoxAutoSave)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(19, 19, 19)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel45)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spinnerAutoSaveGuardTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, 0)
                                .addComponent(jLabel47))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel46)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spinnerAutoSaveInterval, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, 0)
                                .addComponent(jLabel48))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel50)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spinnerFreeSpaceForMaps, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel51))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(buttonCleanUpBackupsNow)
                            .addComponent(checkBoxAutoDeleteBackups))))
                .addContainerGap(50, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator1)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(checkBoxPing)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel4)
                                .addGap(18, 18, 18)
                                .addComponent(checkBoxCheckForUpdates)
                                .addGap(18, 18, 18)
                                .addComponent(checkBoxUndo)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel5)
                                    .addComponent(spinnerUndoLevels, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel30)
                                    .addComponent(comboBoxLookAndFeel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(radioButtonUIScaleAuto)
                                    .addComponent(jLabel2)
                                    .addComponent(labelUIScale))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(radioButtonUIScaleManual)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel32, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(sliderUIScale, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel49)
                                .addGap(18, 18, 18)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel20)
                                    .addComponent(spinnerWorldBackups, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addComponent(checkBoxAutoSave)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel45)
                                    .addComponent(spinnerAutoSaveGuardTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel47))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel46)
                                    .addComponent(spinnerAutoSaveInterval, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel48))
                                .addGap(18, 18, 18)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel50)
                                    .addComponent(spinnerFreeSpaceForMaps, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel51))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(checkBoxAutoDeleteBackups)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(buttonCleanUpBackupsNow)))
                        .addGap(0, 166, Short.MAX_VALUE)))
                .addContainerGap())
        );

        jTabbedPane1.addTab("\u5168\u5C40\u8BBE\u7F6E", jPanel1);

        jLabel1.setText("\u5728\u6B64\u754C\u9762\u7F16\u8F91\u4F60\u7684\u9ED8\u8BA4\u8BBE\u7F6E:");

        jLabel18.setFont(jLabel18.getFont().deriveFont((jLabel18.getFont().getStyle() | java.awt.Font.ITALIC)));
        jLabel18.setText("(\u8BF7\u6CE8\u610F\u8FD9\u4E9B\u66F4\u6539\u53EA\u4F1A\u5728\u4F60\u521B\u5EFA\u4E0B\u4E00\u4E2A\u4E16\u754C\u65F6\u624D\u5F00\u59CB\u751F\u6548.) ");

        jLabel6.setFont(jLabel6.getFont().deriveFont((jLabel6.getFont().getStyle() | java.awt.Font.ITALIC)));
        jLabel6.setText("\u9ED8\u8BA4\u89C6\u56FE\u8BBE\u7F6E ");

        checkBoxGrid.setText("\u5F00\u542F\u7F51\u683C");

        checkBoxContours.setSelected(true);
        checkBoxContours.setText("\u5F00\u542F\u7B49\u9AD8\u7EBF");

        checkBoxViewDistance.setText("\u5F00\u542F\u89C6\u8DDD");

        jLabel22.setText("\u5149\u7167\u65B9\u5411:");

        comboBoxLightDirection.setModel(new DefaultComboBoxModel(LightOrigin.values()));
        comboBoxLightDirection.setRenderer(new EnumListCellRenderer());

        jLabel7.setLabelFor(spinnerGrid);
        jLabel7.setText("\u7F51\u683C\u5C3A\u5BF8:");

        spinnerGrid.setModel(new javax.swing.SpinnerNumberModel(128, 2, 999, 1));

        jLabel8.setText("\u5206\u5272\u95F4\u9694:");

        spinnerContours.setModel(new javax.swing.SpinnerNumberModel(10, 2, 999, 1));

        checkBoxWalkingDistance.setText("\u5F00\u542F\u884C\u8D70\u8DDD\u79BB");

        jLabel26.setText("\u6700\u5927\u7B14\u5237\u5927\u5C0F:");

        spinnerBrushSize.setModel(new javax.swing.SpinnerNumberModel(300, 100, null, 10));

        jLabel21.setText(" ");

        jLabel27.setFont(jLabel27.getFont().deriveFont((jLabel27.getFont().getStyle() | java.awt.Font.ITALIC)));
        jLabel27.setText("\u8B66\u544A: \u8FC7\u5927\u7684\u7B14\u5237\u5927\u5C0F\u53EF\u80FD\u4F1A\u8BA9\u4F60\u7684\u7535\u8111\u5361\u987F! ");

        jLabel9.setFont(jLabel9.getFont().deriveFont((jLabel9.getFont().getStyle() | java.awt.Font.ITALIC)));
        jLabel9.setText("\u9ED8\u8BA4\u4E16\u754C\u8BBE\u7F6E ");

        jLabel10.setLabelFor(spinnerWidth);
        jLabel10.setText("\u7EF4\u5EA6:");

        spinnerWidth.setModel(new javax.swing.SpinnerNumberModel(640, 128, null, 128));
        spinnerWidth.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerWidthStateChanged(evt);
            }
        });

        jLabel11.setText("x");

        spinnerHeight.setModel(new javax.swing.SpinnerNumberModel(640, 128, null, 128));
        spinnerHeight.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerHeightStateChanged(evt);
            }
        });

        jLabel19.setText("\u683C");

        jLabel12.setLabelFor(comboBoxHeight);
        jLabel12.setText("\u9AD8\u5EA6:");

        comboBoxHeight.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboBoxHeightActionPerformed(evt);
            }
        });

        jLabel13.setText("\u5730\u5F62:");

        buttonGroup1.add(radioButtonHilly);
        radioButtonHilly.setSelected(true);
        radioButtonHilly.setText("\u8D77\u4F0F");
        radioButtonHilly.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonHillyActionPerformed(evt);
            }
        });

        jLabel23.setText("(\u9AD8\u5EA6:");

        spinnerRange.setModel(new javax.swing.SpinnerNumberModel(20, 1, 255, 1));

        jLabel24.setText("\u7F29\u653E:");

        spinnerScale.setModel(new javax.swing.SpinnerNumberModel(100, 1, 999, 1));

        jLabel25.setText("%)");

        buttonGroup1.add(radioButtonFlat);
        radioButtonFlat.setText("\u5E73\u5766");
        radioButtonFlat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonFlatActionPerformed(evt);
            }
        });

        checkBoxCircular.setText("\u5706\u5F62\u4E16\u754C");
        checkBoxCircular.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxCircularActionPerformed(evt);
            }
        });

        jLabel14.setLabelFor(spinnerGroundLevel);
        jLabel14.setText("\u9AD8\u5EA6:");

        spinnerGroundLevel.setModel(new javax.swing.SpinnerNumberModel(58, 1, 255, 1));

        jLabel15.setText("\u6C34\u5E73\u9762\u9AD8\u5EA6:");

        spinnerWaterLevel.setModel(new javax.swing.SpinnerNumberModel(62, 0, 255, 1));
        spinnerWaterLevel.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerWaterLevelStateChanged(evt);
            }
        });

        checkBoxLava.setText("\u7528\u5ca9\u6d46\u66ff\u6362\u6c34");

        checkBoxBeaches.setSelected(true);
        checkBoxBeaches.setText("\u6D77\u6EE9");
        checkBoxBeaches.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxBeachesActionPerformed(evt);
            }
        });

        jLabel16.setText("\u8868\u9762\u6750\u8D28:");

        comboBoxSurfaceMaterial.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        comboBoxSurfaceMaterial.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboBoxSurfaceMaterialActionPerformed(evt);
            }
        });

        checkBoxExtendedBlockIds.setText("\u6269\u5C55\u65B9\u5757ID");

        buttonReset.setText("\u91CD\u7F6E...");
        buttonReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonResetActionPerformed(evt);
            }
        });

        labelTerrainAndLayerSettings.setForeground(java.awt.Color.blue);
        labelTerrainAndLayerSettings.setText("<html><u>\u914D\u7F6E\u9ED8\u8BA4\u8FB9\u754C, \u65B9\u5757\u548C\u8986\u76D6\u5C42\u8BBE\u7F6E</u></html>");
        labelTerrainAndLayerSettings.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        labelTerrainAndLayerSettings.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                labelTerrainAndLayerSettingsMouseClicked(evt);
            }
        });

        jLabel17.setFont(jLabel17.getFont().deriveFont((jLabel17.getFont().getStyle() | java.awt.Font.ITALIC)));
        jLabel17.setText("\u9ED8\u8BA4\u5BFC\u51FA\u8BBE\u7F6E");

        checkBoxChestOfGoodies.setText("\u5305\u542B\u5956\u52B1\u7BB1");

        jLabel28.setText("\u4E16\u754C\u7C7B\u578B:");

        comboBoxWorldType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboBoxWorldTypeActionPerformed(evt);
            }
        });

        buttonModePreset.setText("...");
        buttonModePreset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonModePresetActionPerformed(evt);
            }
        });

        checkBoxStructures.setText("\u7ED3\u6784");

        jLabel29.setText("\u6E38\u620F\u6A21\u5F0F:");

        comboBoxMode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboBoxModeActionPerformed(evt);
            }
        });

        checkBoxCheats.setText("\u5141\u8BB8\u4F5C\u5F0A");

        jLabel52.setText("\u5730\u56FE\u683C\u5F0F:");

        comboBoxPlatform.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboBoxPlatformActionPerformed(evt);
            }
        });

        labelEditExportSettingsLink.setForeground(new java.awt.Color(0, 0, 255));
        labelEditExportSettingsLink.setText("<html><u>\u914D\u7F6E\u9ED8\u8BA4\u540E\u5904\u7406\u8BBE\u7F6E</u></html>");
        labelEditExportSettingsLink.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        labelEditExportSettingsLink.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                labelEditExportSettingsLinkMouseClicked(evt);
            }
        });

        checkBoxResourcesEverywhere.setSelected(true);
        checkBoxResourcesEverywhere.setText("\u968F\u5904\u751F\u6210\u8D44\u6E90");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator3)
                    .addComponent(jSeparator2)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(labelTerrainAndLayerSettings, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(buttonReset))
                    .addComponent(jSeparator5)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel18)
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addComponent(checkBoxChestOfGoodies)
                                .addGap(18, 18, 18)
                                .addComponent(checkBoxStructures)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel29)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(comboBoxMode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(checkBoxCheats)
                                .addGap(18, 18, 18)
                                .addComponent(labelEditExportSettingsLink, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addComponent(jLabel10)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spinnerWidth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel11)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spinnerHeight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, 0)
                                .addComponent(jLabel19)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel12)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(comboBoxHeight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel13)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(radioButtonHilly)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel23)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spinnerRange, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel24)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spinnerScale, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, 0)
                                .addComponent(jLabel25)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(radioButtonFlat))
                            .addComponent(jLabel6)
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel5Layout.createSequentialGroup()
                                        .addGap(12, 12, 12)
                                        .addComponent(jLabel7)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(spinnerGrid, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(checkBoxGrid))
                                .addGap(18, 18, 18)
                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(checkBoxContours)
                                    .addGroup(jPanel5Layout.createSequentialGroup()
                                        .addGap(12, 12, 12)
                                        .addComponent(jLabel8)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(spinnerContours, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(18, 18, 18)
                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(checkBoxWalkingDistance)
                                    .addComponent(checkBoxViewDistance))
                                .addGap(18, 18, 18)
                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel5Layout.createSequentialGroup()
                                        .addComponent(jLabel26)
                                        .addGap(6, 6, 6)
                                        .addComponent(spinnerBrushSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel21))
                                    .addGroup(jPanel5Layout.createSequentialGroup()
                                        .addComponent(jLabel22)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(comboBoxLightDirection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(jLabel27)))
                            .addComponent(jLabel9)
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addComponent(checkBoxCircular)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel14)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spinnerGroundLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel15)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spinnerWaterLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(checkBoxLava)
                                .addGap(18, 18, 18)
                                .addComponent(checkBoxBeaches)
                                .addGap(18, 18, 18)
                                .addComponent(checkBoxResourcesEverywhere))
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addComponent(jLabel16)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(comboBoxSurfaceMaterial, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(checkBoxExtendedBlockIds))
                            .addComponent(jLabel17)
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addComponent(jLabel52)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(comboBoxPlatform, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel28)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(comboBoxWorldType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(buttonModePreset)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel18)
                .addGap(18, 18, 18)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(checkBoxGrid)
                    .addComponent(checkBoxContours)
                    .addComponent(checkBoxViewDistance)
                    .addComponent(jLabel22)
                    .addComponent(comboBoxLightDirection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(spinnerGrid, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8)
                    .addComponent(spinnerContours, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(checkBoxWalkingDistance)
                    .addComponent(jLabel21)
                    .addComponent(jLabel26)
                    .addComponent(spinnerBrushSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel27)
                .addGap(18, 18, 18)
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel28)
                        .addComponent(comboBoxWorldType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(buttonModePreset))
                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel52)
                        .addComponent(comboBoxPlatform, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(spinnerWidth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11)
                    .addComponent(spinnerHeight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12)
                    .addComponent(comboBoxHeight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13)
                    .addComponent(radioButtonHilly)
                    .addComponent(radioButtonFlat)
                    .addComponent(jLabel19)
                    .addComponent(jLabel23)
                    .addComponent(spinnerRange, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel24)
                    .addComponent(spinnerScale, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel25))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(spinnerGroundLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel15)
                    .addComponent(spinnerWaterLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(checkBoxLava)
                    .addComponent(checkBoxBeaches)
                    .addComponent(checkBoxCircular)
                    .addComponent(checkBoxResourcesEverywhere))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel16)
                    .addComponent(comboBoxSurfaceMaterial, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(checkBoxExtendedBlockIds))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelTerrainAndLayerSettings, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonReset))
                .addGap(18, 18, 18)
                .addComponent(jLabel17)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator5, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(checkBoxChestOfGoodies)
                    .addComponent(checkBoxStructures)
                    .addComponent(jLabel29)
                    .addComponent(comboBoxMode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(checkBoxCheats)
                    .addComponent(labelEditExportSettingsLink, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("\u9ED8\u8BA4\u503C", jPanel5);

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("\u786C\u4EF6\u52A0\u901F"));

        jLabel31.setText("<html>\u9009\u62E9\u4E00\u79CD\u786C\u4EF6\u52A0\u901F\u65B9\u6CD5. \u5C1D\u8BD5\u4E00\u4E0B\uFF0C\u4EE5\u63D0\u9AD8\u901A\u7528\u7F16\u8F91\u5668\u7684\u6027\u80FD\u6216\u89E3\u51B3\u89C6\u89C9\u95EE\u9898:</html>");

        buttonGroup2.add(radioButtonAccelDefault);
        radioButtonAccelDefault.setText("\u9ED8\u8BA4");

        buttonGroup2.add(radioButtonAccelDirect3D);
        radioButtonAccelDirect3D.setText("Direct3D");

        buttonGroup2.add(radioButtonAccelOpenGL);
        radioButtonAccelOpenGL.setText("OpenGL");

        buttonGroup2.add(radioButtonAccelQuartz);
        radioButtonAccelQuartz.setText("Quartz");

        buttonGroup2.add(radioButtonAccelXRender);
        radioButtonAccelXRender.setText("XRender");

        buttonGroup2.add(radioButtonAccelUnaccelerated);
        radioButtonAccelUnaccelerated.setText("\u4E0D\u52A0\u901F");

        jLabel33.setText("\u5173\u95ED\u6240\u6709\u786C\u4EF6\u52A0\u901F");

        jLabel34.setText("\u5728Linux\u4E0A\u4F7F\u7528 XRender X11 \u6269\u5C55");

        jLabel36.setText("<html><em>\u91CD\u542F\u540E\u751F\u6548  </em></html>");

        jLabel37.setText("\u4F7F\u7528 OpenGL \u6E32\u67D3\u7CFB\u7EDF ");

        jLabel38.setText("\u5728 Windows \u4F7F\u7528 Direct3D \u6E32\u67D3\u7CFB\u7EDF");

        jLabel39.setText("\u4F7F\u7528 Java \u9ED8\u8BA4\u6E32\u67D3\u8BBE\u7F6E");

        jLabel35.setText("\u5728 Mac OS X \u4E0A\u4F7F\u7528\u82F9\u679C\u7684 Quartz \u6E32\u67D3\u7CFB\u7EDF");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel36, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(radioButtonAccelUnaccelerated)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel34)
                            .addComponent(jLabel33)
                            .addComponent(jLabel35)
                            .addComponent(jLabel37)
                            .addComponent(jLabel38)
                            .addComponent(jLabel39)))
                    .addComponent(radioButtonAccelDirect3D)
                    .addComponent(radioButtonAccelDefault)
                    .addComponent(radioButtonAccelXRender)
                    .addComponent(radioButtonAccelQuartz)
                    .addComponent(radioButtonAccelOpenGL)
                    .addComponent(jLabel31))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel31, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel36, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(radioButtonAccelDefault)
                    .addComponent(jLabel39))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(radioButtonAccelDirect3D)
                    .addComponent(jLabel38))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(radioButtonAccelOpenGL)
                    .addComponent(jLabel37))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(radioButtonAccelQuartz)
                    .addComponent(jLabel35))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(radioButtonAccelXRender)
                    .addComponent(jLabel34))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(radioButtonAccelUnaccelerated)
                    .addComponent(jLabel33))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("\u906E\u7F69\u56FE\u7F29\u653E\u548C\u7ED8\u5236"));

        jLabel40.setText("<html>\u9009\u62E9\u4E00\u79CD\u906E\u7F69\u56FE\u7F29\u653E\u548C\u7ED8\u5236\u7684\u65B9\u6CD5. \u5C1D\u8BD5\u9009\u62E9\u4EE5\u63D0\u9AD8\u906E\u7F69\u56FE\u6027\u80FD:</html>");

        jLabel41.setText("<html><em>\u91CD\u542F\u540E\u751F\u6548  </em></html>");

        buttonGroup3.add(radioButtonOverlayScaleOnLoad);
        radioButtonOverlayScaleOnLoad.setText("\u52A0\u8F7D\u65F6\u7F29\u653E");

        buttonGroup3.add(radioButtonOverlayOptimiseOnLoad);
        radioButtonOverlayOptimiseOnLoad.setText("<html>\u52A0\u8F7D\u65F6\u4F18\u5316,<br>\u7ED8\u5236\u65F6\u7F29\u653E</html>");
        radioButtonOverlayOptimiseOnLoad.setVerticalTextPosition(javax.swing.SwingConstants.TOP);

        buttonGroup3.add(radioButtonOverlayScaleOnPaint);
        radioButtonOverlayScaleOnPaint.setText("\u7ED8\u5236\u65F6\u7F29\u653E");

        jLabel42.setText("<html>\u5728\u9996\u6B21\u52A0\u8F7D\u56FE\u50CF\u65F6\u5BF9\u5176\u8FDB\u884C\u4F18\u5316\uFF0C<br>\u4F46\u5728\u7ED8\u753B\u65F6\u5BF9\u5176\u7F29\u653E\u3002\u4F7F\u7528\u66F4\u5C11\u7684\u5185\u5B58.</html>");

        jLabel43.setText("<html>\u9996\u6B21\u52A0\u8F7D\u65F6\uFF0C\u7F29\u653E\u548C\u4F18\u5316\u5185\u5B58\u4E2D\u7684\u56FE\u50CF<br>\u3002\u4F7F\u7528\u5927\u91CF\u5185\u5B58.</html>");

        jLabel44.setText("<html>\u6839\u672C\u4E0D\u4F18\u5316\u56FE\u50CF\uFF0C\u5E76\u4E14\u5728\u7ED8\u753B\u65F6\u5BF9\u5176\u8FDB\u884C\u7F29\u653E\u3002\u4F7F\u7528\u6700\u5C11\u7684\u5185\u5B58.</html>");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel40, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel41, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(radioButtonOverlayOptimiseOnLoad, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(radioButtonOverlayScaleOnLoad)
                            .addComponent(radioButtonOverlayScaleOnPaint))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel44, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel43, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel42, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel40, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel41, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(radioButtonOverlayScaleOnLoad)
                    .addComponent(jLabel43, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(radioButtonOverlayOptimiseOnLoad, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel42, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(radioButtonOverlayScaleOnPaint)
                    .addComponent(jLabel44, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder("\u5BFC\u51FA"));

        jLabel53.setText("\u6700\u5927\u7EBF\u7A0B\u6570\u91CF:");

        buttonGroup5.add(radioButtonThreadCountAuto);
        radioButtonThreadCountAuto.setText("\u81EA\u52A8\u7BA1\u7406");
        radioButtonThreadCountAuto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonThreadCountAutoActionPerformed(evt);
            }
        });

        buttonGroup5.add(radioButtonThreadCountManual);
        radioButtonThreadCountManual.setText("\u624B\u52A8\u8BBE\u7F6E:");
        radioButtonThreadCountManual.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonThreadCountManualActionPerformed(evt);
            }
        });

        spinnerManualThreadCount.setModel(new javax.swing.SpinnerNumberModel(1, 1, 99, 1));

        jLabel54.setText("<html><i>\u4E0B\u6B21\u5BFC\u51FA\u65F6\u751F\u6548 </i></html>");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel53)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addComponent(radioButtonThreadCountManual)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spinnerManualThreadCount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(radioButtonThreadCountAuto)))
                    .addComponent(jLabel54, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel53)
                    .addComponent(radioButtonThreadCountAuto))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(radioButtonThreadCountManual)
                    .addComponent(spinnerManualThreadCount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel54, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(77, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("\u6027\u80FD", jPanel2);

        buttonCancel.setText("\u53d6\u6d88");
        buttonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonCancelActionPerformed(evt);
            }
        });

        buttonOK.setText("\u786e\u8ba4");
        buttonOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonOKActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTabbedPane1)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(buttonOK)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonCancel)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonCancel)
                    .addComponent(buttonOK))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void buttonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonCancelActionPerformed
        cancel();
    }//GEN-LAST:event_buttonCancelActionPerformed

    private void buttonOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonOKActionPerformed
        ok();
    }//GEN-LAST:event_buttonOKActionPerformed

    private void checkBoxUndoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxUndoActionPerformed
        setControlStates();
    }//GEN-LAST:event_checkBoxUndoActionPerformed

    private void labelTerrainAndLayerSettingsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_labelTerrainAndLayerSettingsMouseClicked
        editTerrainAndLayerSettings();
    }//GEN-LAST:event_labelTerrainAndLayerSettingsMouseClicked

    private void comboBoxHeightActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboBoxHeightActionPerformed
        if (programmaticChange) {
            return;
        }
        programmaticChange = true;
        try {
            int maxHeight = (Integer) comboBoxHeight.getSelectedItem();
            if (maxHeight != previousMaxHeight) {
                previousMaxHeight = maxHeight;

                int terrainLevel = (Integer) spinnerGroundLevel.getValue();
                int waterLevel = (Integer) spinnerWaterLevel.getValue();
                if (terrainLevel >= maxHeight) {
                    spinnerGroundLevel.setValue(maxHeight - 1);
                }
                if (waterLevel >= maxHeight) {
                    spinnerWaterLevel.setValue(maxHeight - 1);
                }
                ((SpinnerNumberModel) spinnerGroundLevel.getModel()).setMaximum(maxHeight - 1);
                ((SpinnerNumberModel) spinnerWaterLevel.getModel()).setMaximum(maxHeight - 1);

                int range = (Integer) spinnerRange.getValue();
                if (range >= maxHeight) {
                    spinnerRange.setValue(maxHeight - 1);
                }
                ((SpinnerNumberModel) spinnerRange.getModel()).setMaximum(maxHeight - 1);

                try {
                    resizeDimension(defaultTerrainAndLayerSettings, defaultTerrainAndLayerSettings.getMinHeight(), maxHeight, IDENTITY, true, null);
                } catch (ProgressReceiver.OperationCancelled e) {
                    // Can never happen since we don't pass in a ProgressReceiver
                    throw new InternalError(e);
                }

                setControlStates();
            }
        } finally {
            programmaticChange = false;
        }
    }//GEN-LAST:event_comboBoxHeightActionPerformed

    private void spinnerWaterLevelStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerWaterLevelStateChanged
        int waterLevel = ((Number) spinnerWaterLevel.getValue()).intValue();
        defaultTerrainAndLayerSettings.setBorderLevel(waterLevel);
        TileFactory tileFactory = defaultTerrainAndLayerSettings.getTileFactory();
        if (tileFactory instanceof HeightMapTileFactory) {
            ((HeightMapTileFactory) tileFactory).setWaterHeight(waterLevel);
        }
    }//GEN-LAST:event_spinnerWaterLevelStateChanged

    private void checkBoxBeachesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxBeachesActionPerformed
        TileFactory tileFactory = defaultTerrainAndLayerSettings.getTileFactory();
        if (tileFactory instanceof HeightMapTileFactory) {
            ((HeightMapTileFactory) tileFactory).setBeaches(checkBoxBeaches.isSelected());
        }
    }//GEN-LAST:event_checkBoxBeachesActionPerformed

    private void comboBoxSurfaceMaterialActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboBoxSurfaceMaterialActionPerformed
        // Update the terrain ranges map to conform to the surface material
        // setting
        Configuration config = Configuration.getInstance();
        TileFactory tileFactory = defaultTerrainAndLayerSettings.getTileFactory();
        if ((tileFactory instanceof HeightMapTileFactory)
                && (((HeightMapTileFactory) tileFactory).getTheme() instanceof SimpleTheme)) {
            SortedMap<Integer, Terrain> defaultTerrainRanges = ((SimpleTheme) ((HeightMapTileFactory) tileFactory).getTheme()).getTerrainRanges();
            // Find what is probably meant to be the surface material. With the
            // default settings this should be -1, but if someone configured a
            // default underwater material, try not to change that
            int waterLevel = (Integer) spinnerWaterLevel.getValue();
            int surfaceLevel = defaultTerrainRanges.headMap(waterLevel + 3).lastKey();
            defaultTerrainRanges.put(surfaceLevel, (Terrain) comboBoxSurfaceMaterial.getSelectedItem());
        }
    }//GEN-LAST:event_comboBoxSurfaceMaterialActionPerformed

    private void buttonResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonResetActionPerformed
        if (JOptionPane.showConfirmDialog(this, "Are you sure you want to reset all default world settings,\nincluding the border, terrain and layer settings, to the defaults?", "Confirm Reset", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            final Configuration config = Configuration.getInstance();
            spinnerWidth.setValue(640);
            spinnerHeight.setValue(640);
            final Platform defaultPlatform = config.getDefaultPlatform();
            comboBoxPlatform.setSelectedItem(defaultPlatform);
            radioButtonHilly.setSelected(true);
            spinnerGroundLevel.setValue(58);
            spinnerWaterLevel.setValue(DEFAULT_WATER_LEVEL);
            checkBoxLava.setSelected(false);
            checkBoxBeaches.setSelected(true);
            comboBoxSurfaceMaterial.setSelectedItem(GRASS);
            checkBoxResourcesEverywhere.setSelected(true);
            defaultTerrainAndLayerSettings = new World2(defaultPlatform, World2.DEFAULT_OCEAN_SEED, TileFactoryFactory.createNoiseTileFactory(new Random().nextLong(), GRASS, defaultPlatform.minZ, defaultPlatform.standardMaxHeight, 58, DEFAULT_WATER_LEVEL, false, true, 20, 1.0)).getDimension(NORMAL_DETAIL);
            config.setDefaultTerrainAndLayerSettings(defaultTerrainAndLayerSettings);
            defaultExportSettings = null;
        }
    }//GEN-LAST:event_buttonResetActionPerformed

    private void spinnerWidthStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerWidthStateChanged
        int value = (Integer) spinnerWidth.getValue();
        value = Math.round(value / 128f) * 128;
        if (value < 128) {
            value = 128;
        }
        spinnerWidth.setValue(value);
    }//GEN-LAST:event_spinnerWidthStateChanged

    private void spinnerHeightStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerHeightStateChanged
        int value = (Integer) spinnerHeight.getValue();
        value = Math.round(value / 128f) * 128;
        if (value < 128) {
            value = 128;
        }
        spinnerHeight.setValue(value);
    }//GEN-LAST:event_spinnerHeightStateChanged

    private void radioButtonHillyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonHillyActionPerformed
        setControlStates();
    }//GEN-LAST:event_radioButtonHillyActionPerformed

    private void radioButtonFlatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonFlatActionPerformed
        setControlStates();
    }//GEN-LAST:event_radioButtonFlatActionPerformed

    private void checkBoxCircularActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxCircularActionPerformed
        setControlStates();
    }//GEN-LAST:event_checkBoxCircularActionPerformed

    private void buttonModePresetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonModePresetActionPerformed
        String editedGeneratorOptions = JOptionPane.showInputDialog(this, "Edit the Superflat mode preset:", generatorOptions);
        if (editedGeneratorOptions != null) {
            generatorOptions = editedGeneratorOptions;
        }
    }//GEN-LAST:event_buttonModePresetActionPerformed

    private void comboBoxWorldTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboBoxWorldTypeActionPerformed
        setControlStates();
    }//GEN-LAST:event_comboBoxWorldTypeActionPerformed

    private void comboBoxModeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboBoxModeActionPerformed
        if (comboBoxMode.getSelectedItem() == GameType.CREATIVE) {
            checkBoxCheats.setSelected(true);
        }
    }//GEN-LAST:event_comboBoxModeActionPerformed

    private void checkBoxPingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxPingActionPerformed
        pingNotSet = false;
    }//GEN-LAST:event_checkBoxPingActionPerformed

    private void checkBoxAutoSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxAutoSaveActionPerformed
        setControlStates();
    }//GEN-LAST:event_checkBoxAutoSaveActionPerformed

    private void radioButtonUIScaleAutoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonUIScaleAutoActionPerformed
        setControlStates();
        sliderUIScale.setValue((int) (GUIUtils.SYSTEM_UI_SCALE_FLOAT * 100));
        updateLabelUIScale();
    }//GEN-LAST:event_radioButtonUIScaleAutoActionPerformed

    private void radioButtonUIScaleManualActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonUIScaleManualActionPerformed
        setControlStates();
        updateLabelUIScale();
    }//GEN-LAST:event_radioButtonUIScaleManualActionPerformed

    private void sliderUIScaleStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sliderUIScaleStateChanged
        updateLabelUIScale();
    }//GEN-LAST:event_sliderUIScaleStateChanged

    private void comboBoxLookAndFeelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboBoxLookAndFeelActionPerformed
        // Do nothing
    }//GEN-LAST:event_comboBoxLookAndFeelActionPerformed

    private void checkBoxAutoDeleteBackupsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxAutoDeleteBackupsActionPerformed
        setControlStates();
    }//GEN-LAST:event_checkBoxAutoDeleteBackupsActionPerformed

    private void buttonCleanUpBackupsNowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonCleanUpBackupsNowActionPerformed
        cleanUpBackupsNow();
    }//GEN-LAST:event_buttonCleanUpBackupsNowActionPerformed

    private void comboBoxPlatformActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboBoxPlatformActionPerformed
        platformSelected();
    }//GEN-LAST:event_comboBoxPlatformActionPerformed

    private void labelEditExportSettingsLinkMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_labelEditExportSettingsLinkMouseClicked
        editDefaultExportSettings();
    }//GEN-LAST:event_labelEditExportSettingsLinkMouseClicked

    private void radioButtonThreadCountAutoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonThreadCountAutoActionPerformed
        setControlStates();
    }//GEN-LAST:event_radioButtonThreadCountAutoActionPerformed

    private void radioButtonThreadCountManualActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonThreadCountManualActionPerformed
        setControlStates();
    }//GEN-LAST:event_radioButtonThreadCountManualActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonCancel;
    private javax.swing.JButton buttonCleanUpBackupsNow;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup3;
    private javax.swing.ButtonGroup buttonGroup4;
    private javax.swing.ButtonGroup buttonGroup5;
    private javax.swing.JButton buttonModePreset;
    private javax.swing.JButton buttonOK;
    private javax.swing.JButton buttonReset;
    private javax.swing.JCheckBox checkBoxAutoDeleteBackups;
    private javax.swing.JCheckBox checkBoxAutoSave;
    private javax.swing.JCheckBox checkBoxBeaches;
    private javax.swing.JCheckBox checkBoxCheats;
    private javax.swing.JCheckBox checkBoxCheckForUpdates;
    private javax.swing.JCheckBox checkBoxChestOfGoodies;
    private javax.swing.JCheckBox checkBoxCircular;
    private javax.swing.JCheckBox checkBoxContours;
    private javax.swing.JCheckBox checkBoxExtendedBlockIds;
    private javax.swing.JCheckBox checkBoxGrid;
    private javax.swing.JCheckBox checkBoxLava;
    private javax.swing.JCheckBox checkBoxPing;
    private javax.swing.JCheckBox checkBoxResourcesEverywhere;
    private javax.swing.JCheckBox checkBoxStructures;
    private javax.swing.JCheckBox checkBoxUndo;
    private javax.swing.JCheckBox checkBoxViewDistance;
    private javax.swing.JCheckBox checkBoxWalkingDistance;
    private javax.swing.JComboBox<Integer> comboBoxHeight;
    private javax.swing.JComboBox comboBoxLightDirection;
    private javax.swing.JComboBox comboBoxLookAndFeel;
    private javax.swing.JComboBox<GameType> comboBoxMode;
    private javax.swing.JComboBox<Platform> comboBoxPlatform;
    private javax.swing.JComboBox comboBoxSurfaceMaterial;
    private javax.swing.JComboBox<Generator> comboBoxWorldType;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel42;
    private javax.swing.JLabel jLabel43;
    private javax.swing.JLabel jLabel44;
    private javax.swing.JLabel jLabel45;
    private javax.swing.JLabel jLabel46;
    private javax.swing.JLabel jLabel47;
    private javax.swing.JLabel jLabel48;
    private javax.swing.JLabel jLabel49;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel50;
    private javax.swing.JLabel jLabel51;
    private javax.swing.JLabel jLabel52;
    private javax.swing.JLabel jLabel53;
    private javax.swing.JLabel jLabel54;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel labelEditExportSettingsLink;
    private javax.swing.JLabel labelTerrainAndLayerSettings;
    private javax.swing.JLabel labelUIScale;
    private javax.swing.JRadioButton radioButtonAccelDefault;
    private javax.swing.JRadioButton radioButtonAccelDirect3D;
    private javax.swing.JRadioButton radioButtonAccelOpenGL;
    private javax.swing.JRadioButton radioButtonAccelQuartz;
    private javax.swing.JRadioButton radioButtonAccelUnaccelerated;
    private javax.swing.JRadioButton radioButtonAccelXRender;
    private javax.swing.JRadioButton radioButtonFlat;
    private javax.swing.JRadioButton radioButtonHilly;
    private javax.swing.JRadioButton radioButtonOverlayOptimiseOnLoad;
    private javax.swing.JRadioButton radioButtonOverlayScaleOnLoad;
    private javax.swing.JRadioButton radioButtonOverlayScaleOnPaint;
    private javax.swing.JRadioButton radioButtonThreadCountAuto;
    private javax.swing.JRadioButton radioButtonThreadCountManual;
    private javax.swing.JRadioButton radioButtonUIScaleAuto;
    private javax.swing.JRadioButton radioButtonUIScaleManual;
    private javax.swing.JSlider sliderUIScale;
    private javax.swing.JSpinner spinnerAutoSaveGuardTime;
    private javax.swing.JSpinner spinnerAutoSaveInterval;
    private javax.swing.JSpinner spinnerBrushSize;
    private javax.swing.JSpinner spinnerContours;
    private javax.swing.JSpinner spinnerFreeSpaceForMaps;
    private javax.swing.JSpinner spinnerGrid;
    private javax.swing.JSpinner spinnerGroundLevel;
    private javax.swing.JSpinner spinnerHeight;
    private javax.swing.JSpinner spinnerManualThreadCount;
    private javax.swing.JSpinner spinnerRange;
    private javax.swing.JSpinner spinnerScale;
    private javax.swing.JSpinner spinnerUndoLevels;
    private javax.swing.JSpinner spinnerWaterLevel;
    private javax.swing.JSpinner spinnerWidth;
    private javax.swing.JSpinner spinnerWorldBackups;
    // End of variables declaration//GEN-END:variables

    private final ColourScheme colourScheme;
    private boolean pingNotSet, programmaticChange;
    private int previousMaxHeight;
    private String generatorOptions; // TODOMC118 this needs to become a SuperflatPreset
    private Dimension defaultTerrainAndLayerSettings;
    private ExportSettings defaultExportSettings;
    private Platform previousPlatform;
    
    private static final long serialVersionUID = 1L;
}