/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DimensionPropertiesEditor.java
 *
 * Created on 8-jun-2011, 20:56:18
 */
package org.pepsoft.worldpainter;

import org.jnbt.Tag;
import org.pepsoft.minecraft.*;
import org.pepsoft.util.DesktopUtils;
import org.pepsoft.worldpainter.Dimension.Anchor;
import org.pepsoft.worldpainter.Dimension.LayerAnchor;
import org.pepsoft.worldpainter.biomeschemes.CustomBiomeManager;
import org.pepsoft.worldpainter.exporting.ExportSettings;
import org.pepsoft.worldpainter.exporting.ExportSettingsEditor;
import org.pepsoft.worldpainter.layers.*;
import org.pepsoft.worldpainter.layers.exporters.AbstractCavesExporter.CaveDecorationSettings;
import org.pepsoft.worldpainter.layers.exporters.AnnotationsExporter.AnnotationsSettings;
import org.pepsoft.worldpainter.layers.exporters.CavernsExporter.CavernsSettings;
import org.pepsoft.worldpainter.layers.exporters.CavesExporter.CavesSettings;
import org.pepsoft.worldpainter.layers.exporters.ChasmsExporter.ChasmsSettings;
import org.pepsoft.worldpainter.layers.exporters.FrostExporter.FrostSettings;
import org.pepsoft.worldpainter.layers.exporters.ResourcesExporter.ResourcesExporterSettings;
import org.pepsoft.worldpainter.layers.exporters.TreesExporter.TreeLayerSettings;
import org.pepsoft.worldpainter.plugins.PlatformManager;
import org.pepsoft.worldpainter.plugins.PlatformProvider;
import org.pepsoft.worldpainter.superflat.EditSuperflatPresetDialog;
import org.pepsoft.worldpainter.themes.SimpleTheme;
import org.pepsoft.worldpainter.themes.TerrainListCellRenderer;
import org.pepsoft.worldpainter.tools.Eyedropper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.JSpinner.NumberEditor;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static javax.swing.JOptionPane.YES_NO_OPTION;
import static javax.swing.JOptionPane.YES_OPTION;
import static org.pepsoft.minecraft.Material.*;
import static org.pepsoft.util.AwtUtils.doLaterOnEventThread;
import static org.pepsoft.util.CollectionUtils.listOf;
import static org.pepsoft.util.GUIUtils.scaleToUI;
import static org.pepsoft.util.MathUtils.clamp;
import static org.pepsoft.util.swing.MessageUtils.beepAndShowError;
import static org.pepsoft.util.swing.MessageUtils.showInfo;
import static org.pepsoft.worldpainter.Constants.V_1_17;
import static org.pepsoft.worldpainter.CustomLayersTableModel.COLUMN_EXPORT;
import static org.pepsoft.worldpainter.DefaultPlugin.ATTRIBUTE_MC_VERSION;
import static org.pepsoft.worldpainter.Dimension.Role.*;
import static org.pepsoft.worldpainter.DimensionPropertiesEditor.Mode.DEFAULT_SETTINGS;
import static org.pepsoft.worldpainter.Generator.CUSTOM;
import static org.pepsoft.worldpainter.Generator.FLAT;
import static org.pepsoft.worldpainter.Platform.Capability.*;
import static org.pepsoft.worldpainter.layers.exporters.AbstractCavesExporter.CaveDecorationSettings.Decoration.BROWN_MUSHROOM;
import static org.pepsoft.worldpainter.layers.exporters.AbstractCavesExporter.CaveDecorationSettings.Decoration.*;
import static org.pepsoft.worldpainter.tools.Eyedropper.PaintType.LAYER;
import static org.pepsoft.worldpainter.util.BiomeUtils.getAllBiomes;

/**
 * @author pepijn
 */
@SuppressWarnings({"unused", "rawtypes", "Convert2Lambda", "Anonymous2MethodRef", "ConstantConditions", "FieldCanBeLocal"}) // Managed by NetBeans
public class DimensionPropertiesEditor extends javax.swing.JPanel {
    /**
     * Creates new form DimensionPropertiesEditor
     */
    public DimensionPropertiesEditor() {
        initComponents();

        if ((Configuration.getInstance() != null) && Configuration.getInstance().isEasyMode()) {
            jLabel4.setVisible(false);
            radioButtonNoBorder.setVisible(false);
            radioButtonVoidBorder.setVisible(false);
            radioButtonWaterBorder.setVisible(false);
            jLabel5.setVisible(false);
            spinnerBorderLevel.setVisible(false);
            jLabel44.setVisible(false);
            radioButtonLavaBorder.setVisible(false);
            jLabel8.setVisible(false);
            spinnerBorderSize.setVisible(false);
            jLabel9.setVisible(false);
            checkBoxWall.setVisible(false);
            jLabel7.setVisible(false);
            spinnerMinecraftSeed.setVisible(false);

            checkBoxPopulate.setVisible(false);
            jLabel47.setVisible(false);
        }

        configureSpinners(spinnerGoldChance, spinnerGoldMinLevel, spinnerGoldMaxLevel);
        configureSpinners(spinnerIronChance, spinnerIronMinLevel, spinnerIronMaxLevel);
        configureSpinners(spinnerCoalChance, spinnerCoalMinLevel, spinnerCoalMaxLevel);
        configureSpinners(spinnerLapisChance, spinnerLapisMinLevel, spinnerLapisMaxLevel);
        configureSpinners(spinnerDiamondChance, spinnerDiamondMinLevel, spinnerDiamondMaxLevel);
        configureSpinners(spinnerEmeraldChance, spinnerEmeraldMinLevel, spinnerEmeraldMaxLevel);
        configureSpinners(spinnerWaterChance, spinnerWaterMinLevel, spinnerWaterMaxLevel);
        configureSpinners(spinnerLavaChance, spinnerLavaMinLevel, spinnerLavaMaxLevel);
        configureSpinners(spinnerDirtChance, spinnerDirtMinLevel, spinnerDirtMaxLevel);
        configureSpinners(spinnerGravelChance, spinnerGravelMinLevel, spinnerGravelMaxLevel);
        configureSpinners(spinnerRedstoneChance, spinnerRedstoneMinLevel, spinnerRedstoneMaxLevel);
        configureSpinners(spinnerQuartzChance, spinnerQuartzMinLevel, spinnerQuartzMaxLevel);
        configureSpinners(spinnerCopperChance, spinnerCopperMinLevel, spinnerCopperMaxLevel);
        configureSpinners(spinnerAncientDebrisChance, spinnerAncientDebrisMinLevel, spinnerAncientDebrisMaxLevel);

        configureSpinners(null, spinnerCavesMinLevel, spinnerCavesMaxLevel);
        configureSpinners(null, spinnerCavernsMinLevel, spinnerCavernsMaxLevel);
        configureSpinners(null, spinnerChasmsMinLevel, spinnerChasmsMaxLevel);

        tableCustomLayers.setDefaultRenderer(CustomLayer.class, new CustomLayersTableCellRenderer());
        tableCustomLayers.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
            if (! programmaticChange) {
                setControlStates();
            }
        });
        comboBoxGenerator.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value != null) {
                    setText(((Generator) value).getDisplayName());
                }
                return this;
            }
        });

        scaleToUI(this);
    }

    public void init(ColourScheme colourScheme, CustomBiomeManager customBiomeManager, Dimension dimension, Mode mode) {
        this.customBiomeManager = customBiomeManager;
        comboBoxSubsurfaceMaterial.setRenderer(new TerrainListCellRenderer(colourScheme));
        themeEditor.setColourScheme(colourScheme);
        comboBoxSubsurfaceBiome.setRenderer(new BiomeListCellRenderer(colourScheme, customBiomeManager, "\u4E0E\u5730\u8868\u76F8\u540C", dimension.getWorld().getPlatform()));
        setDimension(dimension);
        setMode(mode);
    }

    public ColourScheme getColourScheme() {
        return themeEditor.getColourScheme();
    }

    private void setMode(Mode mode) {
        requireNonNull(mode);
        if (this.mode != null) {
            throw new IllegalStateException("Mode already set");
        } else if (dimension == null) {
            throw new IllegalStateException("Mode must be set after dimension");
        }
        this.mode = mode;
        final Anchor anchor = dimension.getAnchor();
        switch (mode) {
            case EXPORT:
                if (anchor.role == MASTER) {
                    jTabbedPane1.remove(TAB_OTHER_LAYERS);
                }
                if ((anchor.role == CAVE_FLOOR) || (anchor.role == FLOATING_FLOOR) || (anchor.role == MASTER)) { // TODO support resources caves in floating dimensions
                    jTabbedPane1.remove(TAB_RESOURCES);
                    jTabbedPane1.remove(TAB_CAVES);
                }
                jTabbedPane1.remove(TAB_THEME);
                if ((anchor.role == DETAIL) && (! anchor.invert)) {
                    initialisePostProcessingTab();
                }
                break;
            case DEFAULT_SETTINGS:
                spinnerMinecraftSeed.setEnabled(false);
                jTabbedPane1.remove(TAB_CUSTOM_LAYERS);
                jTabbedPane1.remove(TAB_RESOURCES);
                themeEditor.setAllowCustomItems(false);
                break;
            case EDITOR:
                jTabbedPane1.remove(TAB_CUSTOM_LAYERS);
                if (anchor.role == MASTER) {
                    jTabbedPane1.remove(TAB_OTHER_LAYERS);
                }
                if ((anchor.role == CAVE_FLOOR) || (anchor.role == FLOATING_FLOOR) || (anchor.role == MASTER)) { // TODO support resources caves in floating dimensions
                    jTabbedPane1.remove(TAB_RESOURCES);
                    jTabbedPane1.remove(TAB_CAVES);
                }
                break;
            default:
                throw new IllegalArgumentException("mode " + mode);
        }
        loadSettings();
    }

    public Dimension getDimension() {
        return dimension;
    }

    private void setDimension(Dimension dimension) {
        requireNonNull(dimension);
        if (this.dimension != null) {
            throw new IllegalStateException("Dimension already set");
        }
        this.dimension = dimension;
        final Anchor anchor = dimension.getAnchor();
        if (! ((anchor.role == DETAIL) && (! anchor.invert))) {
            // If we do this straight away it doesn't work due to some bizarre Swing bug, even though we're on the event
            // thread already
            doLaterOnEventThread(() -> {
                panelGeneral.remove(panelMinecraftSettings);
                panelGeneral.remove(panelBorderWallRoof);
            });
        }
        setPlatform(dimension.getWorld().getPlatform());
    }

    public Platform getPlatform() {
        return platform;
    }

    private void setPlatform(Platform platform) {
        this.platform = platform;
        platformProvider = PlatformManager.getInstance().getPlatformProvider(platform);
        if (platform.capabilities.contains(POPULATE)) {
            checkBoxPopulate.setSelected(dimension.isPopulate());
        } else {
            checkBoxPopulate.setSelected(false);
            checkBoxPopulate.setToolTipText(platform+"\u683C\u5F0F\u4E0D\u652F\u6301\u81EA\u52A8\u586B\u5145");
        }
        Generator generator = (Generator) comboBoxGenerator.getSelectedItem();
        comboBoxGenerator.setModel(new DefaultComboBoxModel<>(platform.supportedGenerators.toArray(new Generator[platform.supportedGenerators.size()])));
        if (platform.supportedGenerators.contains(generator)) {
            comboBoxGenerator.setSelectedItem(generator);
        } else {
            comboBoxGenerator.setSelectedItem(platform.supportedGenerators.get(0));
        }
        comboBoxGenerator.setEnabled(platform.supportedGenerators.size() > 1);
        if (platform.capabilities.contains(BIOMES_3D)) {
            comboBoxSubsurfaceBiome.setEnabled(true);
        }
        setControlStates();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        comboBoxSubsurfaceMaterial.setEnabled(enabled);
        checkBoxCavernsEverywhere.setEnabled(enabled);
        checkBoxChasmsEverywhere.setEnabled(enabled);
        checkBoxDeciduousEverywhere.setEnabled(enabled);
        checkBoxFrostEverywhere.setEnabled(enabled);
        checkBoxFloodCaverns.setEnabled(enabled);
        checkBoxCavernsBreakSurface.setEnabled(enabled);
        checkBoxChasmsBreakSurface.setEnabled(enabled);
        checkBoxPineEverywhere.setEnabled(enabled);
        checkBoxJungleEverywhere.setEnabled(enabled);
        checkBoxSwamplandEverywhere.setEnabled(enabled);
        jCheckBox8.setEnabled(enabled);
        checkBoxSmoothSnow.setEnabled(enabled);
        jTabbedPane1.setEnabled(enabled);
        spinnerQuartzChance.setEnabled(enabled);
        spinnerGoldChance.setEnabled(enabled);
        spinnerGoldMaxLevel.setEnabled(enabled);
        spinnerIronChance.setEnabled(enabled);
        spinnerIronMaxLevel.setEnabled(enabled);
        spinnerCoalChance.setEnabled(enabled);
        spinnerCoalMaxLevel.setEnabled(enabled);
        spinnerLapisChance.setEnabled(enabled);
        spinnerLapisMaxLevel.setEnabled(enabled);
        spinnerDiamondChance.setEnabled(enabled);
        spinnerDiamondMaxLevel.setEnabled(enabled);
        spinnerRedstoneChance.setEnabled(enabled);
        spinnerRedstoneMaxLevel.setEnabled(enabled);
        spinnerWaterChance.setEnabled(enabled);
        spinnerWaterMaxLevel.setEnabled(enabled);
        spinnerLavaChance.setEnabled(enabled);
        spinnerDirtChance.setEnabled(enabled);
        spinnerLavaMaxLevel.setEnabled(enabled);
        spinnerDirtMaxLevel.setEnabled(enabled);
        spinnerGravelChance.setEnabled(enabled);
        spinnerGravelMaxLevel.setEnabled(enabled);
        spinnerEmeraldChance.setEnabled(enabled);
        spinnerEmeraldMaxLevel.setEnabled(enabled);
        spinnerGoldMinLevel.setEnabled(enabled);
        spinnerIronMinLevel.setEnabled(enabled);
        spinnerCoalMinLevel.setEnabled(enabled);
        spinnerLapisMinLevel.setEnabled(enabled);
        spinnerDiamondMinLevel.setEnabled(enabled);
        spinnerEmeraldMinLevel.setEnabled(enabled);
        spinnerWaterMinLevel.setEnabled(enabled);
        spinnerLavaMinLevel.setEnabled(enabled);
        spinnerDirtMinLevel.setEnabled(enabled);
        spinnerGravelMinLevel.setEnabled(enabled);
        spinnerRedstoneMinLevel.setEnabled(enabled);
        spinnerQuartzMinLevel.setEnabled(enabled);
        spinnerQuartzMaxLevel.setEnabled(enabled);
        themeEditor.setEnabled(enabled);
        spinnerMinSurfaceDepth.setEnabled(enabled);
        spinnerMaxSurfaceDepth.setEnabled(enabled);
        checkBoxBottomless.setEnabled(enabled);
        spinnerCavernsMinLevel.setEnabled(enabled);
        spinnerCavernsMaxLevel.setEnabled(enabled);
        spinnerChasmsMinLevel.setEnabled(enabled);
        spinnerChasmsMaxLevel.setEnabled(enabled);
        checkBoxCoverSteepTerrain.setEnabled(enabled);
        checkBoxExportAnnotations.setEnabled(enabled);
        checkBoxSnowUnderTrees.setEnabled(enabled);
        comboBoxSurfaceLayerAnchor.setEnabled(enabled);
        setControlStates();
    }

    public boolean saveSettings() {
        final int minHeight = dimension.getMinHeight(), maxHeight = dimension.getMaxHeight() - 1;

        // sanity checks
        if ((comboBoxGenerator.getSelectedItem() == CUSTOM) && ((generatorName == null) || generatorName.trim().isEmpty())) {
            buttonGeneratorOptions.requestFocusInWindow();
            beepAndShowError(this, "\u81EA\u5B9A\u4E49\u4E16\u754C\u751F\u6210\u5668\u6682\u672A\u914D\u7F6E.\n\u8BF7\u4F7F\u7528 [...] \u6309\u94AE\u6765\u8BBE\u7F6E.", "\u9519\u8BEF");
            return false;
        }

        // terrain ranges
        if ((mode != Mode.EXPORT) && (! themeEditor.save())) {
            jTabbedPane1.setSelectedIndex(1);
            return false;
        }

        // general
        int topLayerMinDepth = (Integer) spinnerMinSurfaceDepth.getValue();
        dimension.setTopLayerMinDepth(topLayerMinDepth);
        dimension.setTopLayerVariation((Integer) spinnerMaxSurfaceDepth.getValue() - topLayerMinDepth);
        dimension.setTopLayerAnchor(Dimension.LayerAnchor.values()[comboBoxSurfaceLayerAnchor.getSelectedIndex()]);
        dimension.setSubsurfaceMaterial((Terrain) comboBoxSubsurfaceMaterial.getSelectedItem());
        dimension.setSubsurfaceLayerAnchor(subsurfaceLayerAnchor);
        dimension.setBorder(getSelectedBorder());
        dimension.setBorderLevel((Integer) spinnerBorderLevel.getValue());
        dimension.setBorderSize((Integer) spinnerBorderSize.getValue() / 128);
        dimension.setWallType((checkBoxWall.isSelected() && (! endlessBorder)) ? (radioButtonBedrockWall.isSelected() ? Dimension.WallType.BEDROCK : Dimension.WallType.BARIER) : null);
        dimension.setRoofType(checkBoxRoof.isSelected() ? (radioButtonBedrockRoof.isSelected() ? Dimension.WallType.BEDROCK : Dimension.WallType.BARIER) : null);
        long previousSeed = dimension.getMinecraftSeed();
        long newSeed = ((Number) spinnerMinecraftSeed.getValue()).longValue();
        if (newSeed != previousSeed) {
            dimension.setMinecraftSeed(newSeed);
            if (dimension.getGenerator() instanceof SeededGenerator) {
                ((SeededGenerator) dimension.getGenerator()).setSeed(newSeed);
            }
        }
        dimension.setBottomless(checkBoxBottomless.isSelected());
        dimension.setCoverSteepTerrain(checkBoxCoverSteepTerrain.isSelected());
        dimension.setCeilingHeight((Integer) spinnerCeilingHeight.getValue());
        dimension.setUndergroundBiome((Integer) comboBoxSubsurfaceBiome.getSelectedItem());

        // caves
        CavesSettings cavesSettings = (CavesSettings) dimension.getLayerSettings(Caves.INSTANCE);
        if (cavesSettings == null) {
            cavesSettings = new CavesSettings();
        }
        if (checkBoxCavesEverywhere.isSelected()) {
            int cavesEverywhereLevel = sliderCavesEverywhereLevel.getValue();
            cavesSettings.setCavesEverywhereLevel(cavesEverywhereLevel);
        } else {
            cavesSettings.setCavesEverywhereLevel(0);
        }
        cavesSettings.setSurfaceBreaking(checkBoxCavesBreakSurface.isSelected());
        cavesSettings.setMinimumLevel((Integer) spinnerCavesMinLevel.getValue());
        cavesSettings.setMaximumLevel((Integer) spinnerCavesMaxLevel.getValue());
        // Other settings copied from Caverns layer
        if (checkBoxFloodCaverns.isSelected()) {
            cavesSettings.setWaterLevel((Integer) spinnerCavernsFloodLevel.getValue());
        } else {
            cavesSettings.setWaterLevel(Integer.MIN_VALUE);
        }
        cavesSettings.setFloodWithLava(checkBoxCavernsFloodWithLava.isSelected());
        cavesSettings.setLeaveWater(! checkBoxCavernsRemoveWater.isSelected());
        final boolean anyDecorationsSelected = checkBoxDecorationBrownMushrooms.isSelected()
                || checkBoxDecorationGlowLichen.isSelected()
                || checkBoxDecorationLushCaves.isSelected()
                || checkBoxDecorationDripstoneCaves.isSelected();
        final CaveDecorationSettings decorationSettings = anyDecorationsSelected
                ? new CaveDecorationSettings(checkBoxDecorationBrownMushrooms.isSelected(),
                    checkBoxDecorationGlowLichen.isSelected(),
                    checkBoxDecorationLushCaves.isSelected(),
                    checkBoxDecorationDripstoneCaves.isSelected())
                : null;
        cavesSettings.setCaveDecorationSettings(checkBoxDecorateCaves.isSelected() ? decorationSettings : null);
        dimension.setLayerSettings(Caves.INSTANCE, cavesSettings);

        // caverns
        CavernsSettings cavernsSettings = (CavernsSettings) dimension.getLayerSettings(Caverns.INSTANCE);
        if (cavernsSettings == null) {
            cavernsSettings = new CavernsSettings();
        }
        if (checkBoxCavernsEverywhere.isSelected()) {
            int cavernsEverywhereLevel = sliderCavernsEverywhereLevel.getValue();
            cavernsSettings.setCavernsEverywhereLevel(cavernsEverywhereLevel);
        } else {
            cavernsSettings.setCavernsEverywhereLevel(0);
        }
        if (checkBoxFloodCaverns.isSelected()) {
            cavernsSettings.setWaterLevel((Integer) spinnerCavernsFloodLevel.getValue());
        } else {
            cavernsSettings.setWaterLevel(Integer.MIN_VALUE);
        }
        cavernsSettings.setFloodWithLava(checkBoxCavernsFloodWithLava.isSelected());
        cavernsSettings.setSurfaceBreaking(checkBoxCavernsBreakSurface.isSelected());
        cavernsSettings.setLeaveWater(! checkBoxCavernsRemoveWater.isSelected());
        cavernsSettings.setMinimumLevel((Integer) spinnerCavernsMinLevel.getValue());
        int cavernsMaxLevel = (Integer) spinnerCavernsMaxLevel.getValue();
        cavernsSettings.setMaximumLevel((cavernsMaxLevel >= maxHeight) ? Integer.MAX_VALUE : cavernsMaxLevel);
        cavernsSettings.setCaveDecorationSettings(checkBoxDecorateCaverns.isSelected() ? decorationSettings : null);
        dimension.setLayerSettings(Caverns.INSTANCE, cavernsSettings);

        // chasms
        ChasmsSettings chasmsSettings = (ChasmsSettings) dimension.getLayerSettings(Chasms.INSTANCE);
        if (chasmsSettings == null) {
            chasmsSettings = new ChasmsSettings();
        }
        if (checkBoxChasmsEverywhere.isSelected()) {
            int chasmsEverywhereLevel = sliderChasmsEverywhereLevel.getValue();
            chasmsSettings.setChasmsEverywhereLevel(chasmsEverywhereLevel);
        } else {
            chasmsSettings.setChasmsEverywhereLevel(0);
        }
        chasmsSettings.setSurfaceBreaking(checkBoxChasmsBreakSurface.isSelected());
        chasmsSettings.setMinimumLevel((Integer) spinnerChasmsMinLevel.getValue());
        chasmsSettings.setMaximumLevel((Integer) spinnerChasmsMaxLevel.getValue());
        // Other settings copied from Caverns layer
        if (checkBoxFloodCaverns.isSelected()) {
            chasmsSettings.setWaterLevel((Integer) spinnerCavernsFloodLevel.getValue());
        } else {
            chasmsSettings.setWaterLevel(Integer.MIN_VALUE);
        }
        chasmsSettings.setFloodWithLava(checkBoxCavernsFloodWithLava.isSelected());
        chasmsSettings.setLeaveWater(! checkBoxCavernsRemoveWater.isSelected());
        chasmsSettings.setCaveDecorationSettings(checkBoxDecorateChasms.isSelected() ? decorationSettings : null);
        dimension.setLayerSettings(Chasms.INSTANCE, chasmsSettings);

        // populate
        dimension.setPopulate(checkBoxPopulate.isSelected());

        // deciduous
        TreeLayerSettings<DeciduousForest> deciduousSettings = (TreeLayerSettings<DeciduousForest>) dimension.getLayerSettings(DeciduousForest.INSTANCE);
        if (deciduousSettings == null) {
            deciduousSettings = new TreeLayerSettings<>(DeciduousForest.INSTANCE);
        }
        if (checkBoxDeciduousEverywhere.isSelected()) {
            int minimumLevel = sliderDeciduousLevel.getValue();
            deciduousSettings.setMinimumLevel(minimumLevel);
        } else {
            deciduousSettings.setMinimumLevel(0);
        }
        dimension.setLayerSettings(DeciduousForest.INSTANCE, deciduousSettings);

        // pine
        TreeLayerSettings<PineForest> pineSettings = (TreeLayerSettings<PineForest>) dimension.getLayerSettings(PineForest.INSTANCE);
        if (pineSettings == null) {
            pineSettings = new TreeLayerSettings<>(PineForest.INSTANCE);
        }
        if (checkBoxPineEverywhere.isSelected()) {
            int minimumLevel = sliderPineLevel.getValue();
            pineSettings.setMinimumLevel(minimumLevel);
        } else {
            pineSettings.setMinimumLevel(0);
        }
        dimension.setLayerSettings(PineForest.INSTANCE, pineSettings);

        // jungle
        TreeLayerSettings<Jungle> jungleSettings = (TreeLayerSettings<Jungle>) dimension.getLayerSettings(Jungle.INSTANCE);
        if (jungleSettings == null) {
            jungleSettings = new TreeLayerSettings<>(Jungle.INSTANCE);
        }
        if (checkBoxJungleEverywhere.isSelected()) {
            int minimumLevel = sliderJungleLevel.getValue();
            jungleSettings.setMinimumLevel(minimumLevel);
        } else {
            jungleSettings.setMinimumLevel(0);
        }
        dimension.setLayerSettings(Jungle.INSTANCE, jungleSettings);

        // swampland
        TreeLayerSettings<SwampLand> swampLandSettings = (TreeLayerSettings<SwampLand>) dimension.getLayerSettings(SwampLand.INSTANCE);
        if (swampLandSettings == null) {
            swampLandSettings = new TreeLayerSettings<>(SwampLand.INSTANCE);
        }
        if (checkBoxSwamplandEverywhere.isSelected()) {
            int minimumLevel = jSlider6.getValue();
            swampLandSettings.setMinimumLevel(minimumLevel);
        } else {
            swampLandSettings.setMinimumLevel(0);
        }
        dimension.setLayerSettings(SwampLand.INSTANCE, swampLandSettings);

        // frost
        FrostSettings frostSettings = (FrostSettings) dimension.getLayerSettings(Frost.INSTANCE);
        if (frostSettings == null) {
            frostSettings = new FrostSettings();
        }
        frostSettings.setFrostEverywhere(checkBoxFrostEverywhere.isSelected());
        if ((! checkBoxSmoothSnow.isSelected()) || (frostSettings.getMode() != 3)) {
            frostSettings.setMode(checkBoxSmoothSnow.isSelected() ? 2 : 0);
        }
        frostSettings.setSnowUnderTrees(checkBoxSnowUnderTrees.isSelected());
        dimension.setLayerSettings(Frost.INSTANCE, frostSettings);

        if (mode != DEFAULT_SETTINGS) {
            // resources
            ResourcesExporterSettings resourcesSettings = (ResourcesExporterSettings) dimension.getLayerSettings(Resources.INSTANCE);
            if (resourcesSettings == null) {
                resourcesSettings = ResourcesExporterSettings.defaultSettings(platform, dimension.getAnchor(), dimension.getMinHeight(), dimension.getMaxHeight());
            }
            if (jCheckBox8.isSelected()) {
                int minimumLevel = jSlider4.getValue();
                resourcesSettings.setMinimumLevel(minimumLevel);
            } else {
                resourcesSettings.setMinimumLevel(0);
            }
            resourcesSettings.setChance(GOLD_ORE, (Integer) spinnerGoldChance.getValue());
            resourcesSettings.setMinLevel(GOLD_ORE, (Integer) spinnerGoldMinLevel.getValue());
            resourcesSettings.setMaxLevel(GOLD_ORE, (Integer) spinnerGoldMaxLevel.getValue());
            resourcesSettings.setChance(IRON_ORE, (Integer) spinnerIronChance.getValue());
            resourcesSettings.setMinLevel(IRON_ORE, (Integer) spinnerIronMinLevel.getValue());
            resourcesSettings.setMaxLevel(IRON_ORE, (Integer) spinnerIronMaxLevel.getValue());
            resourcesSettings.setChance(COAL, (Integer) spinnerCoalChance.getValue());
            resourcesSettings.setMinLevel(COAL, (Integer) spinnerCoalMinLevel.getValue());
            resourcesSettings.setMaxLevel(COAL, (Integer) spinnerCoalMaxLevel.getValue());
            resourcesSettings.setChance(LAPIS_LAZULI_ORE, (Integer) spinnerLapisChance.getValue());
            resourcesSettings.setMinLevel(LAPIS_LAZULI_ORE, (Integer) spinnerLapisMinLevel.getValue());
            resourcesSettings.setMaxLevel(LAPIS_LAZULI_ORE, (Integer) spinnerLapisMaxLevel.getValue());
            resourcesSettings.setChance(DIAMOND_ORE, (Integer) spinnerDiamondChance.getValue());
            resourcesSettings.setMinLevel(DIAMOND_ORE, (Integer) spinnerDiamondMinLevel.getValue());
            resourcesSettings.setMaxLevel(DIAMOND_ORE, (Integer) spinnerDiamondMaxLevel.getValue());
            resourcesSettings.setChance(REDSTONE_ORE, (Integer) spinnerRedstoneChance.getValue());
            resourcesSettings.setMinLevel(REDSTONE_ORE, (Integer) spinnerRedstoneMinLevel.getValue());
            resourcesSettings.setMaxLevel(REDSTONE_ORE, (Integer) spinnerRedstoneMaxLevel.getValue());
            resourcesSettings.setChance(STATIONARY_WATER, (Integer) spinnerWaterChance.getValue());
            resourcesSettings.setMinLevel(STATIONARY_WATER, (Integer) spinnerWaterMinLevel.getValue());
            resourcesSettings.setMaxLevel(STATIONARY_WATER, (Integer) spinnerWaterMaxLevel.getValue());
            resourcesSettings.setChance(STATIONARY_LAVA, (Integer) spinnerLavaChance.getValue());
            resourcesSettings.setMinLevel(STATIONARY_LAVA, (Integer) spinnerLavaMinLevel.getValue());
            resourcesSettings.setMaxLevel(STATIONARY_LAVA, (Integer) spinnerLavaMaxLevel.getValue());
            resourcesSettings.setChance(DIRT, (Integer) spinnerDirtChance.getValue());
            resourcesSettings.setMinLevel(DIRT, (Integer) spinnerDirtMinLevel.getValue());
            resourcesSettings.setMaxLevel(DIRT, (Integer) spinnerDirtMaxLevel.getValue());
            resourcesSettings.setChance(GRAVEL, (Integer) spinnerGravelChance.getValue());
            resourcesSettings.setMinLevel(GRAVEL, (Integer) spinnerGravelMinLevel.getValue());
            resourcesSettings.setMaxLevel(GRAVEL, (Integer) spinnerGravelMaxLevel.getValue());
            resourcesSettings.setChance(EMERALD_ORE, (Integer) spinnerEmeraldChance.getValue());
            resourcesSettings.setMinLevel(EMERALD_ORE, (Integer) spinnerEmeraldMinLevel.getValue());
            resourcesSettings.setMaxLevel(EMERALD_ORE, (Integer) spinnerEmeraldMaxLevel.getValue());
            resourcesSettings.setChance(QUARTZ_ORE, (Integer) spinnerQuartzChance.getValue());
            resourcesSettings.setMinLevel(QUARTZ_ORE, (Integer) spinnerQuartzMinLevel.getValue());
            resourcesSettings.setMaxLevel(QUARTZ_ORE, (Integer) spinnerQuartzMaxLevel.getValue());
            resourcesSettings.setChance(COPPER_ORE, (Integer) spinnerCopperChance.getValue());
            resourcesSettings.setMinLevel(COPPER_ORE, (Integer) spinnerCopperMinLevel.getValue());
            resourcesSettings.setMaxLevel(COPPER_ORE, (Integer) spinnerCopperMaxLevel.getValue());
            resourcesSettings.setChance(ANCIENT_DEBRIS, (Integer) spinnerAncientDebrisChance.getValue());
            resourcesSettings.setMinLevel(ANCIENT_DEBRIS, (Integer) spinnerAncientDebrisMinLevel.getValue());
            resourcesSettings.setMaxLevel(ANCIENT_DEBRIS, (Integer) spinnerAncientDebrisMaxLevel.getValue());
            dimension.setLayerSettings(Resources.INSTANCE, resourcesSettings);
        }

        // annotations
        AnnotationsSettings annotationsSettings = (AnnotationsSettings) dimension.getLayerSettings(Annotations.INSTANCE);
        if (annotationsSettings == null) {
            annotationsSettings = new AnnotationsSettings();
        }
        annotationsSettings.setExport(checkBoxExportAnnotations.isSelected());
        dimension.setLayerSettings(Annotations.INSTANCE, annotationsSettings);

        // custom layers
        if ((mode == Mode.EXPORT) && (customLayersTableModel != null) && (! customLayersTableModel.isPristine())) {
            customLayersTableModel.save();
            dimension.changed();
        }

        // export settings
        for (Component component: jTabbedPane1.getComponents()) {
            if (component instanceof ExportSettingsEditor) {
                ExportSettingsEditor editor = (ExportSettingsEditor) component;
                dimension.setExportSettings(editor.getExportSettings());
            }
        }

        // world generation settings
        if (! endlessBorder) {
            final Generator generatorType = (Generator) comboBoxGenerator.getSelectedItem();
            if (generatorType != null) {
                switch (generatorType) {
                    case FLAT:
                        dimension.setGenerator(new SuperflatGenerator(superflatPreset));
                        break;
                    case DEFAULT:
                    case LARGE_BIOMES:
                    case BUFFET:
                    case CUSTOMIZED:
                    case NETHER:
                    case END:
                    case AMPLIFIED:
                        dimension.setGenerator(new SeededGenerator(generatorType, dimension.getMinecraftSeed()));
                        break;
                    case CUSTOM:
                        dimension.setGenerator(new CustomGenerator(generatorName.trim(), customGeneratorSettings));
                        break;
                    case UNKNOWN:
                        // Do nothing
                        break;
                }
            }
        }

        return true;
    }

    public boolean isPopulateSelected() {
        return checkBoxPopulate.isSelected();
    }

    Generator getSelectedGeneratorType() {
        return (Generator) comboBoxGenerator.getSelectedItem();
    }

    void setSelectedGeneratorType(Generator generatorType) {
        comboBoxGenerator.setSelectedItem(generatorType);
    }

    private void initialisePostProcessingTab() {
        if (mode == Mode.EXPORT) {
            ExportSettings exportSettings = dimension.getExportSettings();
            if ((exportSettings == null) && (platformProvider != null)) {
                exportSettings = platformProvider.getDefaultExportSettings(platform);
            }
            if (exportSettings != null) {
                try {
                    ExportSettingsEditor editor = platformProvider.getExportSettingsEditor(platform);
                    editor.setExportSettings(exportSettings);
                    jTabbedPane1.addTab("\u540E\u5904\u7406", editor);
                } catch (RuntimeException e) {
                    logger.warn("Could not initialise post processing tab", e);
                }
            }
        }
    }

    private Dimension.Border getSelectedBorder() {
        if (radioButtonLavaBorder.isSelected()) {
            return radioButtonEndlessBorder.isSelected() ? Dimension.Border.ENDLESS_LAVA : Dimension.Border.LAVA;
        } else if (radioButtonNoBorder.isSelected()) {
            return null;
        } else if (radioButtonVoidBorder.isSelected()) {
            return radioButtonEndlessBorder.isSelected() ? Dimension.Border.ENDLESS_VOID : Dimension.Border.VOID;
        } else if (radioButtonWaterBorder.isSelected()){
            return radioButtonEndlessBorder.isSelected() ? Dimension.Border.ENDLESS_WATER : Dimension.Border.WATER;
        } else {
            return radioButtonEndlessBorder.isSelected() ? Dimension.Border.ENDLESS_BARRIER : Dimension.Border.BARRIER;
        }
    }

    private void loadSettings() {
        final int minHeight = dimension.getMinHeight(), maxHeight = dimension.getMaxHeight() - 1;

        // general
        ((SpinnerNumberModel) spinnerMinSurfaceDepth.getModel()).setMaximum(maxHeight);
        spinnerMinSurfaceDepth.setValue(dimension.getTopLayerMinDepth());
        ((SpinnerNumberModel) spinnerMaxSurfaceDepth.getModel()).setMaximum(maxHeight);
        spinnerMaxSurfaceDepth.setValue(dimension.getTopLayerMinDepth() + dimension.getTopLayerVariation());
        comboBoxSurfaceLayerAnchor.setSelectedIndex(dimension.getTopLayerAnchor().ordinal());
        Terrain subsurfaceTerrain = dimension.getSubsurfaceMaterial();
        subsurfaceLayerAnchor = dimension.getSubsurfaceLayerAnchor();
        if ((subsurfaceTerrain != null) && subsurfaceTerrain.isCustom()) {
            MixedMaterial material = Terrain.getCustomMaterial(subsurfaceTerrain.getCustomTerrainIndex());
            if (material.getMode() == MixedMaterial.Mode.LAYERED) {
                comboBoxUndergroundLayerAnchor.setSelectedIndex(subsurfaceLayerAnchor.ordinal());
            } else {
                comboBoxUndergroundLayerAnchor.setSelectedItem(null);
            }
        } else {
            comboBoxUndergroundLayerAnchor.setSelectedItem(null);
        }

        if (dimension.getBorder() != null) {
            switch (dimension.getBorder()) {
                case LAVA:
                    radioButtonLavaBorder.setSelected(true);
                    radioButtonFixedBorder.setSelected(true);
                    break;
                case WATER:
                    radioButtonWaterBorder.setSelected(true);
                    radioButtonFixedBorder.setSelected(true);
                    break;
                case VOID:
                    radioButtonVoidBorder.setSelected(true);
                    radioButtonFixedBorder.setSelected(true);
                    break;
                case BARRIER:
                    radioButtonBarrierBorder.setSelected(true);
                    radioButtonFixedBorder.setSelected(true);
                    break;
                case ENDLESS_LAVA:
                    radioButtonLavaBorder.setSelected(true);
                    radioButtonEndlessBorder.setSelected(true);
                    break;
                case ENDLESS_WATER:
                    radioButtonWaterBorder.setSelected(true);
                    radioButtonEndlessBorder.setSelected(true);
                    break;
                case ENDLESS_VOID:
                    radioButtonVoidBorder.setSelected(true);
                    radioButtonEndlessBorder.setSelected(true);
                    break;
                case ENDLESS_BARRIER:
                    radioButtonBarrierBorder.setSelected(true);
                    radioButtonEndlessBorder.setSelected(true);
                    break;
                default:
                    throw new InternalError();
            }
        } else {
            radioButtonNoBorder.setSelected(true);
        }
        ((SpinnerNumberModel) spinnerBorderLevel.getModel()).setMinimum(minHeight);
        ((SpinnerNumberModel) spinnerBorderLevel.getModel()).setMaximum(maxHeight);
        spinnerBorderLevel.setValue(dimension.getBorderLevel());
        spinnerBorderSize.setValue(dimension.getBorderSize() * 128);
        checkBoxWall.setSelected(dimension.getWallType() != null);
        radioButtonBedrockWall.setSelected(dimension.getWallType() == Dimension.WallType.BEDROCK);
        radioButtonBarrierWall.setSelected(dimension.getWallType() == Dimension.WallType.BARIER);
        checkBoxRoof.setSelected(dimension.getRoofType() != null);
        radioButtonBedrockRoof.setSelected(dimension.getRoofType() == Dimension.WallType.BEDROCK);
        radioButtonBarrierRoof.setSelected(dimension.getRoofType() == Dimension.WallType.BARIER);
        spinnerMinecraftSeed.setValue(dimension.getMinecraftSeed());
        checkBoxBottomless.setSelected(dimension.isBottomless());
        checkBoxCoverSteepTerrain.setSelected(dimension.isCoverSteepTerrain());
        ((SpinnerNumberModel) spinnerCeilingHeight.getModel()).setMinimum(minHeight + 1);
        ((SpinnerNumberModel) spinnerCeilingHeight.getModel()).setMaximum(maxHeight + 1);
        spinnerCeilingHeight.setValue(dimension.getCeilingHeight());

        List<Terrain> materialList = new ArrayList<>(Arrays.asList(Terrain.VALUES));
        for (Iterator<Terrain> i = materialList.iterator(); i.hasNext(); ) {
            Terrain terrain = i.next();
            if ((terrain.isCustom() && (! terrain.isConfigured())) || (terrain == Terrain.GRASS) || (terrain == Terrain.DESERT) || (terrain == Terrain.RED_DESERT)) {
                i.remove();
            }
        }
        comboBoxSubsurfaceMaterial.setModel(new DefaultComboBoxModel<>(materialList.toArray(new Terrain[materialList.size()])));
        comboBoxSubsurfaceMaterial.setSelectedItem(dimension.getSubsurfaceMaterial());

        final List<Integer> allBiomes = listOf(singletonList(null), getAllBiomes(platform, customBiomeManager));
        comboBoxSubsurfaceBiome.setModel(new DefaultComboBoxModel<>(allBiomes.toArray(new Integer[allBiomes.size()])));
        comboBoxSubsurfaceBiome.setSelectedItem(dimension.getUndergroundBiome());

        // caves
        CavesSettings cavesSettings = (CavesSettings) dimension.getLayerSettings(Caves.INSTANCE);
        if (cavesSettings == null) {
            cavesSettings = new CavesSettings();
        }
        if (cavesSettings.getCavesEverywhereLevel() > 0) {
            checkBoxCavesEverywhere.setSelected(true);
            sliderCavesEverywhereLevel.setValue(cavesSettings.getCavesEverywhereLevel());
        } else {
            checkBoxCavesEverywhere.setSelected(false);
            sliderCavesEverywhereLevel.setValue(8);
        }
        checkBoxCavesBreakSurface.setSelected(cavesSettings.isSurfaceBreaking());
        ((SpinnerNumberModel) spinnerCavesMinLevel.getModel()).setMinimum(minHeight);
        ((SpinnerNumberModel) spinnerCavesMinLevel.getModel()).setMaximum(maxHeight);
        spinnerCavesMinLevel.setValue((cavesSettings.getMinimumLevel() == Integer.MIN_VALUE) ? (minHeight + 8) : Math.max(cavesSettings.getMinimumLevel(), minHeight));
        ((SpinnerNumberModel) spinnerCavesMaxLevel.getModel()).setMinimum(minHeight);
        ((SpinnerNumberModel) spinnerCavesMaxLevel.getModel()).setMaximum(maxHeight);
        spinnerCavesMaxLevel.setValue(Math.min(cavesSettings.getMaximumLevel(), maxHeight));
        CaveDecorationSettings decorationSettings = cavesSettings.getCaveDecorationSettings();
        checkBoxDecorateCaves.setSelected(decorationSettings != null);

        // caverns
        CavernsSettings cavernsSettings = (CavernsSettings) dimension.getLayerSettings(Caverns.INSTANCE);
        if (cavernsSettings == null) {
            cavernsSettings = new CavernsSettings();
        }
        if (cavernsSettings.getCavernsEverywhereLevel() > 0) {
            checkBoxCavernsEverywhere.setSelected(true);
            sliderCavernsEverywhereLevel.setValue(cavernsSettings.getCavernsEverywhereLevel());
        } else {
            checkBoxCavernsEverywhere.setSelected(false);
            sliderCavernsEverywhereLevel.setValue(8);
        }
        ((SpinnerNumberModel) spinnerCavernsFloodLevel.getModel()).setMinimum(minHeight);
        ((SpinnerNumberModel) spinnerCavernsFloodLevel.getModel()).setMaximum(maxHeight);
        if (cavernsSettings.getWaterLevel() >= minHeight) {
            checkBoxFloodCaverns.setSelected(true);
            spinnerCavernsFloodLevel.setValue(cavernsSettings.getWaterLevel());
        } else {
            checkBoxFloodCaverns.setSelected(false);
            spinnerCavernsFloodLevel.setValue(minHeight + 8);
        }
        checkBoxCavernsFloodWithLava.setSelected(cavernsSettings.isFloodWithLava());
        checkBoxCavernsBreakSurface.setSelected(cavernsSettings.isSurfaceBreaking());
        checkBoxCavernsRemoveWater.setSelected(! cavernsSettings.isLeaveWater());
        ((SpinnerNumberModel) spinnerCavernsMinLevel.getModel()).setMinimum(minHeight);
        ((SpinnerNumberModel) spinnerCavernsMinLevel.getModel()).setMaximum(maxHeight);
        spinnerCavernsMinLevel.setValue(Math.max(cavernsSettings.getMinimumLevel(), minHeight));
        ((SpinnerNumberModel) spinnerCavernsMaxLevel.getModel()).setMinimum(minHeight);
        ((SpinnerNumberModel) spinnerCavernsMaxLevel.getModel()).setMaximum(maxHeight);
        spinnerCavernsMaxLevel.setValue(Math.min(cavernsSettings.getMaximumLevel(), maxHeight));
        CaveDecorationSettings cavernDecorationSettings = cavernsSettings.getCaveDecorationSettings();
        if (checkBoxDecorateCaverns.isEnabled()) {
            checkBoxDecorateCaverns.setSelected(cavernDecorationSettings != null);
        }
        if (decorationSettings == null) {
            decorationSettings = cavernDecorationSettings;
        }

        // chasms
        ChasmsSettings chasmsSettings = (ChasmsSettings) dimension.getLayerSettings(Chasms.INSTANCE);
        if (chasmsSettings == null) {
            chasmsSettings = new ChasmsSettings();
        }
        if (chasmsSettings.getChasmsEverywhereLevel() > 0) {
            checkBoxChasmsEverywhere.setSelected(true);
            sliderChasmsEverywhereLevel.setValue(chasmsSettings.getChasmsEverywhereLevel());
        } else {
            checkBoxChasmsEverywhere.setSelected(false);
            sliderChasmsEverywhereLevel.setValue(8);
        }
        checkBoxChasmsBreakSurface.setSelected(chasmsSettings.isSurfaceBreaking());
        ((SpinnerNumberModel) spinnerChasmsMinLevel.getModel()).setMinimum(minHeight);
        ((SpinnerNumberModel) spinnerChasmsMinLevel.getModel()).setMaximum(maxHeight);
        spinnerChasmsMinLevel.setValue(Math.max(chasmsSettings.getMinimumLevel(), minHeight));
        ((SpinnerNumberModel) spinnerChasmsMaxLevel.getModel()).setMinimum(minHeight);
        ((SpinnerNumberModel) spinnerChasmsMaxLevel.getModel()).setMaximum(maxHeight);
        spinnerChasmsMaxLevel.setValue(Math.min(chasmsSettings.getMaximumLevel(), maxHeight));
        CaveDecorationSettings chasmDecorationSettings = chasmsSettings.getCaveDecorationSettings();
        if (checkBoxDecorateChasms.isEnabled()) {
            checkBoxDecorateChasms.setSelected(chasmDecorationSettings != null);
        }
        if (decorationSettings == null) {
            decorationSettings = chasmDecorationSettings;
        }

        // cave decoration settings
        if (decorationSettings == null) {
            decorationSettings = new CaveDecorationSettings();
        }
        checkBoxDecorationBrownMushrooms.setSelected(decorationSettings.isEnabled(BROWN_MUSHROOM));
        checkBoxDecorationGlowLichen.setSelected(decorationSettings.isEnabled(GLOW_LICHEN));
        checkBoxDecorationLushCaves.setSelected(decorationSettings.isEnabled(LUSH_CAVE_PATCHES));
        checkBoxDecorationDripstoneCaves.setSelected(decorationSettings.isEnabled(DRIPSTONE_CAVE_PATCHES));

        // populate
        checkBoxPopulate.setSelected(dimension.isPopulate());

        // deciduous
        TreeLayerSettings<DeciduousForest> deciduousSettings = (TreeLayerSettings<DeciduousForest>) dimension.getLayerSettings(DeciduousForest.INSTANCE);
        if (deciduousSettings == null) {
            deciduousSettings = new TreeLayerSettings<>(DeciduousForest.INSTANCE);
        }
        if (deciduousSettings.getMinimumLevel() > 0) {
            checkBoxDeciduousEverywhere.setSelected(true);
            sliderDeciduousLevel.setValue(deciduousSettings.getMinimumLevel());
        } else {
            checkBoxDeciduousEverywhere.setSelected(false);
            sliderDeciduousLevel.setValue(8);
        }

        // pine
        TreeLayerSettings<PineForest> pineSettings = (TreeLayerSettings<PineForest>) dimension.getLayerSettings(PineForest.INSTANCE);
        if (pineSettings == null) {
            pineSettings = new TreeLayerSettings<>(PineForest.INSTANCE);
        }
        if (pineSettings.getMinimumLevel() > 0) {
            checkBoxPineEverywhere.setSelected(true);
            sliderPineLevel.setValue(pineSettings.getMinimumLevel());
        } else {
            checkBoxPineEverywhere.setSelected(false);
            sliderPineLevel.setValue(8);
        }

        // jungle
        TreeLayerSettings<Jungle> jungleSettings = (TreeLayerSettings<Jungle>) dimension.getLayerSettings(Jungle.INSTANCE);
        if (jungleSettings == null) {
            jungleSettings = new TreeLayerSettings<>(Jungle.INSTANCE);
        }
        if (jungleSettings.getMinimumLevel() > 0) {
            checkBoxJungleEverywhere.setSelected(true);
            sliderJungleLevel.setValue(jungleSettings.getMinimumLevel());
        } else {
            checkBoxJungleEverywhere.setSelected(false);
            sliderJungleLevel.setValue(8);
        }

        // swamp
        TreeLayerSettings<SwampLand> swampLandSettings = (TreeLayerSettings<SwampLand>) dimension.getLayerSettings(SwampLand.INSTANCE);
        if (swampLandSettings == null) {
            swampLandSettings = new TreeLayerSettings<>(SwampLand.INSTANCE);
        }
        if (swampLandSettings.getMinimumLevel() > 0) {
            checkBoxSwamplandEverywhere.setSelected(true);
            jSlider6.setValue(swampLandSettings.getMinimumLevel());
        } else {
            checkBoxSwamplandEverywhere.setSelected(false);
            jSlider6.setValue(8);
        }

        // frost
        FrostSettings frostSettings = (FrostSettings) dimension.getLayerSettings(Frost.INSTANCE);
        if (frostSettings == null) {
            frostSettings = new FrostSettings();
        }
        checkBoxFrostEverywhere.setSelected(frostSettings.isFrostEverywhere());
        checkBoxSmoothSnow.setSelected((frostSettings.getMode() == 2) || (frostSettings.getMode() == 3));
        checkBoxSnowUnderTrees.setSelected(frostSettings.isSnowUnderTrees());

        if (mode != DEFAULT_SETTINGS) {
            // resources
            ResourcesExporterSettings resourcesSettings = (ResourcesExporterSettings) dimension.getLayerSettings(Resources.INSTANCE);
            if (resourcesSettings == null) {
                resourcesSettings = ResourcesExporterSettings.defaultSettings(platform, dimension.getAnchor(), dimension.getMinHeight(), dimension.getMaxHeight());
                resourcesSettings.setMinimumLevel(0);
            }
            jCheckBox8.setSelected(resourcesSettings.isApplyEverywhere());
            if (resourcesSettings.isApplyEverywhere()) {
                jSlider4.setValue(resourcesSettings.getMinimumLevel());
            } else {
                jSlider4.setValue(8);
            }

            // TODOMC118: encode minHeight as Integer.MIN_VALUE and maxHeight as Integer.MAX_VALUE so they adjust automatically
            //  (Or maybe only do this for maxHeight, otherwise _everything_ with minHeight 0 in 1.15 will be extended down for 1.18, which is not correct? Or just accept that?)

            spinnerGoldChance.setValue(resourcesSettings.getChance(GOLD_ORE));
            ((SpinnerNumberModel) spinnerGoldMinLevel.getModel()).setMinimum(minHeight);
            ((SpinnerNumberModel) spinnerGoldMinLevel.getModel()).setMaximum(maxHeight);
            spinnerGoldMinLevel.setValue(clamp(minHeight, resourcesSettings.getMinLevel(GOLD_ORE), maxHeight));
            ((SpinnerNumberModel) spinnerGoldMaxLevel.getModel()).setMinimum(minHeight);
            ((SpinnerNumberModel) spinnerGoldMaxLevel.getModel()).setMaximum(maxHeight);
            spinnerGoldMaxLevel.setValue(clamp(minHeight, resourcesSettings.getMaxLevel(GOLD_ORE), maxHeight));
            spinnerIronChance.setValue(resourcesSettings.getChance(IRON_ORE));
            ((SpinnerNumberModel) spinnerIronMinLevel.getModel()).setMinimum(minHeight);
            ((SpinnerNumberModel) spinnerIronMinLevel.getModel()).setMaximum(maxHeight);
            spinnerIronMinLevel.setValue(clamp(minHeight, resourcesSettings.getMinLevel(IRON_ORE), maxHeight));
            ((SpinnerNumberModel) spinnerIronMaxLevel.getModel()).setMinimum(minHeight);
            ((SpinnerNumberModel) spinnerIronMaxLevel.getModel()).setMaximum(maxHeight);
            spinnerIronMaxLevel.setValue(clamp(minHeight, resourcesSettings.getMaxLevel(IRON_ORE), maxHeight));
            spinnerCoalChance.setValue(resourcesSettings.getChance(COAL));
            ((SpinnerNumberModel) spinnerCoalMinLevel.getModel()).setMinimum(minHeight);
            ((SpinnerNumberModel) spinnerCoalMinLevel.getModel()).setMaximum(maxHeight);
            spinnerCoalMinLevel.setValue(clamp(minHeight, resourcesSettings.getMinLevel(COAL), maxHeight));
            ((SpinnerNumberModel) spinnerCoalMaxLevel.getModel()).setMinimum(minHeight);
            ((SpinnerNumberModel) spinnerCoalMaxLevel.getModel()).setMaximum(maxHeight);
            spinnerCoalMaxLevel.setValue(clamp(minHeight, resourcesSettings.getMaxLevel(COAL), maxHeight));
            spinnerLapisChance.setValue(resourcesSettings.getChance(LAPIS_LAZULI_ORE));
            ((SpinnerNumberModel) spinnerLapisMinLevel.getModel()).setMinimum(minHeight);
            ((SpinnerNumberModel) spinnerLapisMinLevel.getModel()).setMaximum(maxHeight);
            spinnerLapisMinLevel.setValue(clamp(minHeight, resourcesSettings.getMinLevel(LAPIS_LAZULI_ORE), maxHeight));
            ((SpinnerNumberModel) spinnerLapisMaxLevel.getModel()).setMinimum(minHeight);
            ((SpinnerNumberModel) spinnerLapisMaxLevel.getModel()).setMaximum(maxHeight);
            spinnerLapisMaxLevel.setValue(clamp(minHeight, resourcesSettings.getMaxLevel(LAPIS_LAZULI_ORE), maxHeight));
            spinnerDiamondChance.setValue(resourcesSettings.getChance(DIAMOND_ORE));
            ((SpinnerNumberModel) spinnerDiamondMinLevel.getModel()).setMinimum(minHeight);
            ((SpinnerNumberModel) spinnerDiamondMinLevel.getModel()).setMaximum(maxHeight);
            spinnerDiamondMinLevel.setValue(clamp(minHeight, resourcesSettings.getMinLevel(DIAMOND_ORE), maxHeight));
            ((SpinnerNumberModel) spinnerDiamondMaxLevel.getModel()).setMinimum(minHeight);
            ((SpinnerNumberModel) spinnerDiamondMaxLevel.getModel()).setMaximum(maxHeight);
            spinnerDiamondMaxLevel.setValue(clamp(minHeight, resourcesSettings.getMaxLevel(DIAMOND_ORE), maxHeight));
            spinnerRedstoneChance.setValue(resourcesSettings.getChance(REDSTONE_ORE));
            ((SpinnerNumberModel) spinnerRedstoneMinLevel.getModel()).setMinimum(minHeight);
            ((SpinnerNumberModel) spinnerRedstoneMinLevel.getModel()).setMaximum(maxHeight);
            spinnerRedstoneMinLevel.setValue(clamp(minHeight, resourcesSettings.getMinLevel(REDSTONE_ORE), maxHeight));
            ((SpinnerNumberModel) spinnerRedstoneMaxLevel.getModel()).setMinimum(minHeight);
            ((SpinnerNumberModel) spinnerRedstoneMaxLevel.getModel()).setMaximum(maxHeight);
            spinnerRedstoneMaxLevel.setValue(clamp(minHeight, resourcesSettings.getMaxLevel(REDSTONE_ORE), maxHeight));
            spinnerWaterChance.setValue(resourcesSettings.getChance(STATIONARY_WATER));
            ((SpinnerNumberModel) spinnerWaterMinLevel.getModel()).setMinimum(minHeight);
            ((SpinnerNumberModel) spinnerWaterMinLevel.getModel()).setMaximum(maxHeight);
            spinnerWaterMinLevel.setValue(clamp(minHeight, resourcesSettings.getMinLevel(STATIONARY_WATER), maxHeight));
            ((SpinnerNumberModel) spinnerWaterMaxLevel.getModel()).setMinimum(minHeight);
            ((SpinnerNumberModel) spinnerWaterMaxLevel.getModel()).setMaximum(maxHeight);
            spinnerWaterMaxLevel.setValue(clamp(minHeight, resourcesSettings.getMaxLevel(STATIONARY_WATER), maxHeight));
            spinnerLavaChance.setValue(resourcesSettings.getChance(STATIONARY_LAVA));
            ((SpinnerNumberModel) spinnerLavaMinLevel.getModel()).setMinimum(minHeight);
            ((SpinnerNumberModel) spinnerLavaMinLevel.getModel()).setMaximum(maxHeight);
            spinnerLavaMinLevel.setValue(clamp(minHeight, resourcesSettings.getMinLevel(STATIONARY_LAVA), maxHeight));
            ((SpinnerNumberModel) spinnerLavaMaxLevel.getModel()).setMinimum(minHeight);
            ((SpinnerNumberModel) spinnerLavaMaxLevel.getModel()).setMaximum(maxHeight);
            spinnerLavaMaxLevel.setValue(clamp(minHeight, resourcesSettings.getMaxLevel(STATIONARY_LAVA), maxHeight));
            spinnerDirtChance.setValue(resourcesSettings.getChance(DIRT));
            ((SpinnerNumberModel) spinnerDirtMinLevel.getModel()).setMinimum(minHeight);
            ((SpinnerNumberModel) spinnerDirtMinLevel.getModel()).setMaximum(maxHeight);
            spinnerDirtMinLevel.setValue(clamp(minHeight, resourcesSettings.getMinLevel(DIRT), maxHeight));
            ((SpinnerNumberModel) spinnerDirtMaxLevel.getModel()).setMinimum(minHeight);
            ((SpinnerNumberModel) spinnerDirtMaxLevel.getModel()).setMaximum(maxHeight);
            spinnerDirtMaxLevel.setValue(clamp(minHeight, resourcesSettings.getMaxLevel(DIRT), maxHeight));
            spinnerGravelChance.setValue(resourcesSettings.getChance(GRAVEL));
            ((SpinnerNumberModel) spinnerGravelMinLevel.getModel()).setMinimum(minHeight);
            ((SpinnerNumberModel) spinnerGravelMinLevel.getModel()).setMaximum(maxHeight);
            spinnerGravelMinLevel.setValue(clamp(minHeight, resourcesSettings.getMinLevel(GRAVEL), maxHeight));
            ((SpinnerNumberModel) spinnerGravelMaxLevel.getModel()).setMinimum(minHeight);
            ((SpinnerNumberModel) spinnerGravelMaxLevel.getModel()).setMaximum(maxHeight);
            spinnerGravelMaxLevel.setValue(clamp(minHeight, resourcesSettings.getMaxLevel(GRAVEL), maxHeight));
            spinnerEmeraldChance.setValue(resourcesSettings.getChance(EMERALD_ORE));
            ((SpinnerNumberModel) spinnerEmeraldMinLevel.getModel()).setMinimum(minHeight);
            ((SpinnerNumberModel) spinnerEmeraldMinLevel.getModel()).setMaximum(maxHeight);
            spinnerEmeraldMinLevel.setValue(clamp(minHeight, resourcesSettings.getMinLevel(EMERALD_ORE), maxHeight));
            ((SpinnerNumberModel) spinnerEmeraldMaxLevel.getModel()).setMinimum(minHeight);
            ((SpinnerNumberModel) spinnerEmeraldMaxLevel.getModel()).setMaximum(maxHeight);
            spinnerEmeraldMaxLevel.setValue(clamp(minHeight, resourcesSettings.getMaxLevel(EMERALD_ORE), maxHeight));
            spinnerQuartzChance.setValue(resourcesSettings.getChance(QUARTZ_ORE));
            ((SpinnerNumberModel) spinnerQuartzMinLevel.getModel()).setMinimum(minHeight);
            ((SpinnerNumberModel) spinnerQuartzMinLevel.getModel()).setMaximum(maxHeight);
            spinnerQuartzMinLevel.setValue(clamp(minHeight, resourcesSettings.getMinLevel(QUARTZ_ORE), maxHeight));
            ((SpinnerNumberModel) spinnerQuartzMaxLevel.getModel()).setMinimum(minHeight);
            ((SpinnerNumberModel) spinnerQuartzMaxLevel.getModel()).setMaximum(maxHeight);
            spinnerQuartzMaxLevel.setValue(clamp(minHeight, resourcesSettings.getMaxLevel(QUARTZ_ORE), maxHeight));
            spinnerCopperChance.setValue(resourcesSettings.getChance(COPPER_ORE));
            ((SpinnerNumberModel) spinnerCopperMinLevel.getModel()).setMinimum(minHeight);
            ((SpinnerNumberModel) spinnerCopperMinLevel.getModel()).setMaximum(maxHeight);
            spinnerCopperMinLevel.setValue(clamp(minHeight, resourcesSettings.getMinLevel(COPPER_ORE), maxHeight));
            ((SpinnerNumberModel) spinnerCopperMaxLevel.getModel()).setMinimum(minHeight);
            ((SpinnerNumberModel) spinnerCopperMaxLevel.getModel()).setMaximum(maxHeight);
            spinnerCopperMaxLevel.setValue(clamp(minHeight, resourcesSettings.getMaxLevel(COPPER_ORE), maxHeight));
            spinnerAncientDebrisChance.setValue(resourcesSettings.getChance(ANCIENT_DEBRIS));
            ((SpinnerNumberModel) spinnerAncientDebrisMinLevel.getModel()).setMinimum(minHeight);
            ((SpinnerNumberModel) spinnerAncientDebrisMinLevel.getModel()).setMaximum(maxHeight);
            spinnerAncientDebrisMinLevel.setValue(clamp(minHeight, resourcesSettings.getMinLevel(ANCIENT_DEBRIS), maxHeight));
            ((SpinnerNumberModel) spinnerAncientDebrisMaxLevel.getModel()).setMinimum(minHeight);
            ((SpinnerNumberModel) spinnerAncientDebrisMaxLevel.getModel()).setMaximum(maxHeight);
            spinnerAncientDebrisMaxLevel.setValue(clamp(minHeight, resourcesSettings.getMaxLevel(ANCIENT_DEBRIS), maxHeight));
        }

        // terrain ranges
        if ((mode != Mode.EXPORT)
                && (dimension.getTileFactory() instanceof HeightMapTileFactory)
                && (((HeightMapTileFactory) dimension.getTileFactory()).getTheme() instanceof SimpleTheme)
                && (((SimpleTheme) ((HeightMapTileFactory) dimension.getTileFactory()).getTheme()).getTerrainRanges() != null)) {
            themeEditor.setTheme((SimpleTheme) ((HeightMapTileFactory) dimension.getTileFactory()).getTheme());
        }

        // annotations
        AnnotationsSettings annotationsSettings = (AnnotationsSettings) dimension.getLayerSettings(Annotations.INSTANCE);
        if (annotationsSettings == null) {
            annotationsSettings = new AnnotationsSettings();
        }
        checkBoxExportAnnotations.setSelected(annotationsSettings.isExport());

        // custom layers
        if (mode == Mode.EXPORT) {
            final List<CustomLayer> customLayers = dimension.getCustomLayers(true);
            // Don't show layers which cannot be exported, such as custom annotations:
            customLayers.removeIf(layer -> layer.getExporterType() == null);
            if (! customLayers.isEmpty()) {
                customLayersTableModel = new CustomLayersTableModel(customLayers);
                tableCustomLayers.setModel(customLayersTableModel);
                orderPristine = customLayers.stream().noneMatch(layer -> layer.getExportIndex() != null);
            }
        }

        // world generation settings
        endlessBorder = (dimension.getBorder() != null) && dimension.getBorder().isEndless();
        final MapGenerator generator = dimension.getGenerator();
        comboBoxGenerator.setSelectedItem(endlessBorder ? FLAT : ((generator != null) ? generator.getType() : null));
        if (generator != null) {
            savedGeneratorType = generator.getType();
            generatorName = (generator instanceof CustomGenerator) ? ((CustomGenerator) generator).getName() : null;
            customGeneratorSettings = (generator instanceof CustomGenerator) ? ((CustomGenerator) generator).getSettings() : null;
            superflatPreset = (generator instanceof SuperflatGenerator) ? ((SuperflatGenerator) generator).getSettings() : null;
        }

        setControlStates();
    }

    private void setControlStates() {
        final boolean enabled = isEnabled();
        final Anchor anchor = (dimension != null) ? dimension.getAnchor() : null;
        final boolean dim0 = (anchor != null) && (anchor.dim == Constants.DIM_NORMAL) && (anchor.role == DETAIL) && (! anchor.invert);
        final boolean ceiling = (anchor != null) && anchor.invert;
        final boolean caveFloor = (anchor != null) && (anchor.role == CAVE_FLOOR);
        final boolean floatingFloor = (anchor != null) && (anchor.role == FLOATING_FLOOR);
        final boolean floorDimension = caveFloor || floatingFloor;
        final boolean master = (anchor != null) && (anchor.role == MASTER);
        final boolean decorations = checkBoxDecorateCaverns.isSelected() || checkBoxDecorateCaves.isSelected() || checkBoxDecorateChasms.isSelected();
        setEnabled(radioButtonLavaBorder, enabled && (! ceiling) && (! floorDimension) && (! master));
        setEnabled(radioButtonNoBorder, enabled && (! ceiling) && (! floorDimension) && (! master));
        setEnabled(radioButtonVoidBorder, enabled && (! ceiling) && (! floorDimension) && (! master));
        setEnabled(radioButtonWaterBorder, enabled && (! ceiling) && (! floorDimension) && (! master));
        setEnabled(radioButtonBarrierBorder, enabled && (! ceiling) && (! floorDimension) && (! master));
        setEnabled(spinnerBorderLevel, enabled && (! ceiling) && (! floorDimension) && (! master) && (radioButtonLavaBorder.isSelected() || radioButtonWaterBorder.isSelected()));
        setEnabled(radioButtonFixedBorder, enabled && (! ceiling) && (! floorDimension) && (! master) && (! radioButtonNoBorder.isSelected()));
        setEnabled(radioButtonEndlessBorder, enabled && (platform.capabilities.contains(GENERATOR_PER_DIMENSION) || dim0) && (! ceiling) && (! floorDimension) && (! master) && (! radioButtonNoBorder.isSelected()));
        setEnabled(spinnerBorderSize, enabled && (! ceiling) && (! floorDimension) && (! master) && (! radioButtonNoBorder.isSelected()) && radioButtonFixedBorder.isSelected());
        setEnabled(sliderCavesEverywhereLevel, enabled && checkBoxCavesEverywhere.isSelected());
        setEnabled(sliderCavernsEverywhereLevel, enabled && checkBoxCavernsEverywhere.isSelected());
        setEnabled(sliderChasmsEverywhereLevel, enabled && checkBoxChasmsEverywhere.isSelected());
        setEnabled(spinnerCavernsFloodLevel, enabled && checkBoxFloodCaverns.isSelected());
        setEnabled(checkBoxCavernsFloodWithLava, enabled && checkBoxFloodCaverns.isSelected());
        setEnabled(sliderDeciduousLevel, enabled && checkBoxDeciduousEverywhere.isSelected());
        setEnabled(sliderPineLevel, enabled && checkBoxPineEverywhere.isSelected());
        setEnabled(sliderJungleLevel, enabled && checkBoxJungleEverywhere.isSelected());
        setEnabled(jSlider6, enabled && checkBoxSwamplandEverywhere.isSelected());
        setEnabled(jSlider4, enabled && jCheckBox8.isSelected());
        setEnabled(spinnerMinecraftSeed, (mode != Mode.DEFAULT_SETTINGS) && enabled && dim0);
        setEnabled(checkBoxPopulate, ((platform == null) || platform.capabilities.contains(POPULATE)) && enabled && dim0);
        setEnabled(checkBoxCavernsRemoveWater, enabled && (checkBoxCavesBreakSurface.isSelected() || checkBoxCavernsBreakSurface.isSelected() || checkBoxChasmsBreakSurface.isSelected()));
        setEnabled(spinnerCeilingHeight, enabled && ceiling);
        final int[] selectedRows = tableCustomLayers.getSelectedRows();
        boolean headerIncluded = false, disabledLayersFound = false, enabledLayersFound = false;
        if (selectedRows.length > 0) {
            for (int row = selectedRows[0]; row <= selectedRows[selectedRows.length - 1]; row++) {
                if (customLayersTableModel.isHeaderRow(row)) {
                    headerIncluded = true;
                } else if ((boolean) customLayersTableModel.getValueAt(row, COLUMN_EXPORT)) {
                    enabledLayersFound = true;
                } else {
                    disabledLayersFound = true;
                }
            }
        }
        setEnabled(buttonSelectPaint, enabled && (customLayersTableModel != null));
        setEnabled(buttonCustomLayerUp, enabled && (selectedRows.length > 0) && (! headerIncluded) && (! customLayersTableModel.isHeaderRow(selectedRows[0])) && (selectedRows[0] > 0) && (! customLayersTableModel.isHeaderRow(selectedRows[0] - 1)));
        setEnabled(buttonCustomLayerTop, buttonCustomLayerUp.isEnabled());
        setEnabled(buttonCustomLayerDown, enabled && (selectedRows.length > 0) && (! headerIncluded) && (! customLayersTableModel.isHeaderRow(selectedRows[selectedRows.length - 1])) && (selectedRows[selectedRows.length - 1] < (tableCustomLayers.getRowCount() - 1)) && (! customLayersTableModel.isHeaderRow(selectedRows[selectedRows.length - 1] + 1)));
        setEnabled(buttonCustomLayerBottom, buttonCustomLayerDown.isEnabled());
        setEnabled(buttonDisableLayers, enabled && enabledLayersFound);
        setEnabled(buttonEnableLayers, enabled && disabledLayersFound);
        setEnabled(buttonReset, enabled && (! orderPristine));
        if (! enabled) {
            setEnabled(comboBoxUndergroundLayerAnchor, false);
        } else {
            Terrain subsurfaceTerrain = (Terrain) comboBoxSubsurfaceMaterial.getSelectedItem();
            if ((subsurfaceTerrain != null) && subsurfaceTerrain.isCustom()) {
                MixedMaterial material = Terrain.getCustomMaterial(subsurfaceTerrain.getCustomTerrainIndex());
                setEnabled(comboBoxUndergroundLayerAnchor, material.getMode() == MixedMaterial.Mode.LAYERED);
            } else {
                setEnabled(comboBoxUndergroundLayerAnchor, false);
            }
        }
        setEnabled(comboBoxGenerator, enabled && (! endlessBorder) && (! ceiling) && (! floorDimension) && (! master));
        setEnabled(buttonGeneratorOptions, enabled && (! endlessBorder) && (! ceiling) && (! floorDimension)
                && (! master) && ((comboBoxGenerator.getSelectedItem() == Generator.FLAT)
                || ((comboBoxGenerator.getSelectedItem() == CUSTOM) && (customGeneratorSettings == null))));
        setEnabled(checkBoxWall, enabled && (! endlessBorder) && (! ceiling) && (! floorDimension) && (! master));
        setEnabled(radioButtonBedrockWall, enabled && (! endlessBorder) && checkBoxWall.isSelected());
        setEnabled(radioButtonBarrierWall, enabled && (! endlessBorder) && checkBoxWall.isSelected());
        setEnabled(checkBoxRoof, enabled && (! ceiling) && (! floorDimension) && (! master));
        setEnabled(radioButtonBedrockRoof, enabled && checkBoxRoof.isSelected());
        setEnabled(radioButtonBarrierRoof, enabled && checkBoxRoof.isSelected());
        setEnabled(checkBoxDecorateCaverns, enabled);
        setEnabled(checkBoxDecorateCaves, enabled);
        setEnabled(checkBoxDecorateChasms, enabled);
        setEnabled(checkBoxDecorationBrownMushrooms, enabled && decorations);
        final boolean mcVersionAtLeast1_17 = platform.getAttribute(ATTRIBUTE_MC_VERSION).isAtLeast(V_1_17);
        setEnabled(checkBoxDecorationGlowLichen, enabled && decorations && mcVersionAtLeast1_17);
        setEnabled(checkBoxDecorationLushCaves, enabled && decorations && mcVersionAtLeast1_17);
        setEnabled(checkBoxDecorationDripstoneCaves, enabled && decorations && mcVersionAtLeast1_17);
        setEnabled(checkBoxCoverSteepTerrain, enabled && (! caveFloor) && (! master)); // TODO make it possible for this to be different for the master dimension
        setEnabled(comboBoxSurfaceLayerAnchor, enabled);
        setEnabled(comboBoxSubsurfaceMaterial, enabled && (! caveFloor) && (! master)); // TODO make it possible for this to be different for the master dimension
        setEnabled(comboBoxUndergroundLayerAnchor, enabled && (! caveFloor) && (! master)); // TODO make it possible for this to be different for the master dimension
        setEnabled(checkBoxBottomless, enabled && (! floorDimension) && (! master)); // TODO make it possible for this to be different for the master dimension
        setEnabled(comboBoxSubsurfaceBiome, enabled && (! caveFloor));
    }

    private void setEnabled(Component component, boolean enabled) {
        if (component.isEnabled() != enabled) {
            component.setEnabled(enabled);
        }
    }

    private void configureSpinners(final JSpinner chanceSpinner, final JSpinner minSpinner, final JSpinner maxSpinner) {
        if (chanceSpinner != null) {
            chanceSpinner.setEditor(new NumberEditor(chanceSpinner, "0"));
            minSpinner.setEnabled((int) chanceSpinner.getValue() != 0);
            maxSpinner.setEnabled((int) chanceSpinner.getValue() != 0);
            chanceSpinner.addChangeListener(e -> {
                minSpinner.setEnabled((int) chanceSpinner.getValue() != 0);
                maxSpinner.setEnabled((int) chanceSpinner.getValue() != 0);
            });
        }
        minSpinner.setEditor(new NumberEditor(minSpinner, "0"));
        minSpinner.addChangeListener(e -> {
            int newMinValue = (Integer) minSpinner.getValue();
            int currentMaxValue = (Integer) maxSpinner.getValue();
            if (newMinValue > currentMaxValue) {
                maxSpinner.setValue(newMinValue);
            }
        });
        maxSpinner.setEditor(new NumberEditor(maxSpinner, "0"));
        maxSpinner.addChangeListener(e -> {
            int newMaxValue = (Integer) maxSpinner.getValue();
            int currentMinValue = (Integer) minSpinner.getValue();
            if (newMaxValue < currentMinValue) {
                minSpinner.setValue(newMaxValue);
            }
        });
    }

    private void borderChanged() {
        final boolean previousEndlessBorder = endlessBorder;
        Dimension.Border border = getSelectedBorder();
        endlessBorder = (border != null) && border.isEndless();
        if (endlessBorder != previousEndlessBorder) {
            programmaticChange = true;
            try {
                if (endlessBorder) {
                    savedGeneratorType = (Generator) comboBoxGenerator.getSelectedItem();
                    comboBoxGenerator.setSelectedItem(FLAT);
                } else {
                    comboBoxGenerator.setSelectedItem(savedGeneratorType);
                }
            } finally {
                programmaticChange = false;
            }
        }
    }

    private void updateGeneratorButtonTooltip() {
        if (comboBoxGenerator.getSelectedItem() != null) {
            switch ((Generator) comboBoxGenerator.getSelectedItem()) {
                case FLAT:
                    buttonGeneratorOptions.setToolTipText("\u7F16\u8F91\u8D85\u5E73\u5766\u6A21\u5F0F\u9884\u8BBE");
                    break;
                case CUSTOM:
                    buttonGeneratorOptions.setToolTipText("\u8BBE\u7F6E\u81EA\u5B9A\u4E49\u4E16\u754C\u751F\u6210\u5668\u540D\u79F0");
                    break;
                default:
                    buttonGeneratorOptions.setToolTipText(null);
                    break;
            }
        } else {
            buttonGeneratorOptions.setToolTipText(null);
        }
    }

    private void disableSelectedLayers() {
        for (int row: tableCustomLayers.getSelectedRows()) {
            if (! customLayersTableModel.isHeaderRow(row)) {
                customLayersTableModel.setExport(row, false);
            }
        }
        setControlStates();
    }

    private void enableSelectedLayers() {
        for (int row: tableCustomLayers.getSelectedRows()) {
            if (! customLayersTableModel.isHeaderRow(row)) {
                customLayersTableModel.setExport(row, true);
            }
        }
        setControlStates();
    }

    private void selectPaintOnMap() {
        Container parent = getParent();
        while ((parent != null) && (! (parent instanceof WPDialogWithPaintSelection))) {
            parent = parent.getParent();
        }
        if (parent != null) {
            ((WPDialogWithPaintSelection) parent).selectFromMap(singleton(LAYER), new Eyedropper.SelectionListener() {
                @Override
                public void layerSelected(Layer layer, int value) {
                    if (layer instanceof CustomLayer) {
                        final int index = customLayersTableModel.getLayerIndex((CustomLayer) layer);
                        if (index != -1) {
                            tableCustomLayers.getSelectionModel().setSelectionInterval(index, index);
                            tableCustomLayers.scrollRectToVisible(tableCustomLayers.getCellRect(index, 0, true));
                        } else {
                            beepAndShowError(DimensionPropertiesEditor.this, "\u8986\u76D6\u5C42 " + layer.getName() + " \u4E0D\u5728\u5217\u8868\u4E2D", "\u4E0D\u5728\u5217\u8868\u4E2D");
                        }
                    } else {
                        beepAndShowError(DimensionPropertiesEditor.this, "\u8986\u76D6\u5C42 " + layer.getName() + " \u4E0D\u662F\u81EA\u5B9A\u4E49\u8986\u76D6\u5C42", "\u4E0D\u662F\u81EA\u5B9A\u4E49\u8986\u76D6\u5C42");
                    }
                }

                @Override public void terrainSelected(Terrain terrain) {}
                @Override public void selectionCancelled(boolean byUser) {}
            });
        } else {
            DesktopUtils.beep();
        }
    }

    private void resetOrder() {
        if (JOptionPane.showConfirmDialog(this, "\u4F60\u60F3\u8981\u91CD\u7F6E\u6240\u6709\u8986\u76D6\u5C42\u7684\u987A\u5E8F\u4E3A\u9ED8\u8BA4\u503C\u5417?\n\u8BE5\u64CD\u4F5C\u4E0D\u53EF\u64A4\u9500!", "\u786E\u8BA4\u987A\u5E8F\u91CD\u7F6E", YES_NO_OPTION) != YES_OPTION) {
            return;
        }
        final List<CustomLayer> customLayers = dimension.getCustomLayers(true);
        // Don't show layers which cannot be exported, such as custom annotations:
        customLayers.removeIf(layer -> layer.getExporterType() == null);
        customLayers.forEach(layer -> layer.setExportIndex(null));
        customLayersTableModel = new CustomLayersTableModel(customLayers);
        tableCustomLayers.setModel(customLayersTableModel);
        orderPristine = true;
        setControlStates();
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup3 = new javax.swing.ButtonGroup();
        buttonGroup5 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        panelGeneral = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        comboBoxSubsurfaceMaterial = new javax.swing.JComboBox<>();
        jLabel65 = new javax.swing.JLabel();
        spinnerMinSurfaceDepth = new javax.swing.JSpinner();
        jLabel66 = new javax.swing.JLabel();
        spinnerMaxSurfaceDepth = new javax.swing.JSpinner();
        checkBoxBottomless = new javax.swing.JCheckBox();
        jLabel67 = new javax.swing.JLabel();
        checkBoxCoverSteepTerrain = new javax.swing.JCheckBox();
        jLabel78 = new javax.swing.JLabel();
        spinnerCeilingHeight = new javax.swing.JSpinner();
        comboBoxSurfaceLayerAnchor = new javax.swing.JComboBox<>();
        jLabel83 = new javax.swing.JLabel();
        jLabel84 = new javax.swing.JLabel();
        comboBoxUndergroundLayerAnchor = new javax.swing.JComboBox<>();
        panelBorderWallRoof = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        checkBoxWall = new javax.swing.JCheckBox();
        spinnerBorderLevel = new javax.swing.JSpinner();
        radioButtonWaterBorder = new javax.swing.JRadioButton();
        radioButtonEndlessBorder = new javax.swing.JRadioButton();
        jLabel8 = new javax.swing.JLabel();
        spinnerBorderSize = new javax.swing.JSpinner();
        radioButtonBedrockRoof = new javax.swing.JRadioButton();
        radioButtonLavaBorder = new javax.swing.JRadioButton();
        jLabel5 = new javax.swing.JLabel();
        radioButtonNoBorder = new javax.swing.JRadioButton();
        radioButtonBarrierRoof = new javax.swing.JRadioButton();
        checkBoxRoof = new javax.swing.JCheckBox();
        jLabel9 = new javax.swing.JLabel();
        radioButtonFixedBorder = new javax.swing.JRadioButton();
        radioButtonBedrockWall = new javax.swing.JRadioButton();
        jLabel44 = new javax.swing.JLabel();
        radioButtonVoidBorder = new javax.swing.JRadioButton();
        radioButtonBarrierWall = new javax.swing.JRadioButton();
        radioButtonBarrierBorder = new javax.swing.JRadioButton();
        panelMinecraftSettings = new javax.swing.JPanel();
        buttonGeneratorOptions = new javax.swing.JButton();
        jLabel95 = new javax.swing.JLabel();
        jLabel94 = new javax.swing.JLabel();
        spinnerMinecraftSeed = new javax.swing.JSpinner();
        jLabel7 = new javax.swing.JLabel();
        comboBoxGenerator = new javax.swing.JComboBox<>();
        jLabel79 = new javax.swing.JLabel();
        comboBoxSubsurfaceBiome = new javax.swing.JComboBox<>();
        jPanel5 = new javax.swing.JPanel();
        themeEditor = new org.pepsoft.worldpainter.themes.impl.simple.SimpleThemeEditor();
        jLabel45 = new javax.swing.JLabel();
        jLabel46 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        checkBoxCavesEverywhere = new javax.swing.JCheckBox();
        sliderCavesEverywhereLevel = new javax.swing.JSlider();
        jPanel10 = new javax.swing.JPanel();
        checkBoxCavernsEverywhere = new javax.swing.JCheckBox();
        sliderCavernsEverywhereLevel = new javax.swing.JSlider();
        jPanel11 = new javax.swing.JPanel();
        checkBoxChasmsEverywhere = new javax.swing.JCheckBox();
        sliderChasmsEverywhereLevel = new javax.swing.JSlider();
        jPanel16 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jPanel13 = new javax.swing.JPanel();
        jLabel87 = new javax.swing.JLabel();
        spinnerCavesMinLevel = new javax.swing.JSpinner();
        jLabel88 = new javax.swing.JLabel();
        spinnerCavesMaxLevel = new javax.swing.JSpinner();
        jPanel12 = new javax.swing.JPanel();
        jLabel70 = new javax.swing.JLabel();
        spinnerCavernsMinLevel = new javax.swing.JSpinner();
        jLabel71 = new javax.swing.JLabel();
        spinnerCavernsMaxLevel = new javax.swing.JSpinner();
        jPanel14 = new javax.swing.JPanel();
        jLabel73 = new javax.swing.JLabel();
        spinnerChasmsMinLevel = new javax.swing.JSpinner();
        jLabel72 = new javax.swing.JLabel();
        spinnerChasmsMaxLevel = new javax.swing.JSpinner();
        jPanel8 = new javax.swing.JPanel();
        checkBoxFloodCaverns = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        spinnerCavernsFloodLevel = new javax.swing.JSpinner();
        checkBoxCavernsFloodWithLava = new javax.swing.JCheckBox();
        jPanel17 = new javax.swing.JPanel();
        checkBoxCavesBreakSurface = new javax.swing.JCheckBox();
        jPanel18 = new javax.swing.JPanel();
        checkBoxCavernsBreakSurface = new javax.swing.JCheckBox();
        jPanel19 = new javax.swing.JPanel();
        checkBoxChasmsBreakSurface = new javax.swing.JCheckBox();
        checkBoxCavernsRemoveWater = new javax.swing.JCheckBox();
        jPanel15 = new javax.swing.JPanel();
        jPanel23 = new javax.swing.JPanel();
        jSeparator5 = new javax.swing.JSeparator();
        jPanel24 = new javax.swing.JPanel();
        jLabel96 = new javax.swing.JLabel();
        checkBoxDecorationBrownMushrooms = new javax.swing.JCheckBox();
        checkBoxDecorationGlowLichen = new javax.swing.JCheckBox();
        checkBoxDecorationLushCaves = new javax.swing.JCheckBox();
        checkBoxDecorationDripstoneCaves = new javax.swing.JCheckBox();
        jPanel25 = new javax.swing.JPanel();
        checkBoxDecorateCaves = new javax.swing.JCheckBox();
        jPanel26 = new javax.swing.JPanel();
        checkBoxDecorateCaverns = new javax.swing.JCheckBox();
        jPanel27 = new javax.swing.JPanel();
        checkBoxDecorateChasms = new javax.swing.JCheckBox();
        jPanel4 = new javax.swing.JPanel();
        jCheckBox8 = new javax.swing.JCheckBox();
        jSlider4 = new javax.swing.JSlider();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel10 = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        jSeparator4 = new javax.swing.JSeparator();
        jPanel20 = new javax.swing.JPanel();
        spinnerIronMaxLevel = new javax.swing.JSpinner();
        jLabel35 = new javax.swing.JLabel();
        spinnerWaterChance = new javax.swing.JSpinner();
        jLabel29 = new javax.swing.JLabel();
        spinnerWaterMinLevel = new javax.swing.JSpinner();
        jLabel14 = new javax.swing.JLabel();
        spinnerEmeraldMinLevel = new javax.swing.JSpinner();
        jLabel54 = new javax.swing.JLabel();
        spinnerCoalChance = new javax.swing.JSpinner();
        jLabel20 = new javax.swing.JLabel();
        jLabel53 = new javax.swing.JLabel();
        spinnerCoalMaxLevel = new javax.swing.JSpinner();
        spinnerCoalMinLevel = new javax.swing.JSpinner();
        jLabel13 = new javax.swing.JLabel();
        jLabel34 = new javax.swing.JLabel();
        jLabel55 = new javax.swing.JLabel();
        jLabel37 = new javax.swing.JLabel();
        spinnerEmeraldMaxLevel = new javax.swing.JSpinner();
        spinnerLapisChance = new javax.swing.JSpinner();
        jLabel25 = new javax.swing.JLabel();
        spinnerIronChance = new javax.swing.JSpinner();
        jLabel51 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        spinnerGoldChance = new javax.swing.JSpinner();
        spinnerLapisMinLevel = new javax.swing.JSpinner();
        spinnerGoldMaxLevel = new javax.swing.JSpinner();
        spinnerDiamondMinLevel = new javax.swing.JSpinner();
        jLabel56 = new javax.swing.JLabel();
        spinnerWaterMaxLevel = new javax.swing.JSpinner();
        jLabel26 = new javax.swing.JLabel();
        jLabel59 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        spinnerLapisMaxLevel = new javax.swing.JSpinner();
        jLabel40 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        spinnerDiamondMaxLevel = new javax.swing.JSpinner();
        jLabel21 = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
        spinnerLavaMinLevel = new javax.swing.JSpinner();
        jLabel23 = new javax.swing.JLabel();
        jLabel60 = new javax.swing.JLabel();
        spinnerLavaMaxLevel = new javax.swing.JSpinner();
        jLabel24 = new javax.swing.JLabel();
        jLabel61 = new javax.swing.JLabel();
        jLabel52 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        jLabel57 = new javax.swing.JLabel();
        jLabel58 = new javax.swing.JLabel();
        spinnerIronMinLevel = new javax.swing.JSpinner();
        spinnerGoldMinLevel = new javax.swing.JSpinner();
        jLabel38 = new javax.swing.JLabel();
        spinnerLavaChance = new javax.swing.JSpinner();
        jLabel41 = new javax.swing.JLabel();
        spinnerEmeraldChance = new javax.swing.JSpinner();
        jLabel12 = new javax.swing.JLabel();
        spinnerDiamondChance = new javax.swing.JSpinner();
        jPanel21 = new javax.swing.JPanel();
        jLabel63 = new javax.swing.JLabel();
        jLabel92 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        spinnerCopperMinLevel = new javax.swing.JSpinner();
        spinnerGravelChance = new javax.swing.JSpinner();
        spinnerRedstoneMinLevel = new javax.swing.JSpinner();
        jLabel62 = new javax.swing.JLabel();
        jLabel39 = new javax.swing.JLabel();
        jLabel64 = new javax.swing.JLabel();
        jLabel43 = new javax.swing.JLabel();
        spinnerCopperChance = new javax.swing.JSpinner();
        spinnerDirtChance = new javax.swing.JSpinner();
        jLabel32 = new javax.swing.JLabel();
        jLabel93 = new javax.swing.JLabel();
        jLabel30 = new javax.swing.JLabel();
        jLabel33 = new javax.swing.JLabel();
        spinnerRedstoneChance = new javax.swing.JSpinner();
        jLabel19 = new javax.swing.JLabel();
        spinnerGravelMaxLevel = new javax.swing.JSpinner();
        spinnerCopperMaxLevel = new javax.swing.JSpinner();
        jLabel27 = new javax.swing.JLabel();
        spinnerGravelMinLevel = new javax.swing.JSpinner();
        jLabel31 = new javax.swing.JLabel();
        spinnerDirtMinLevel = new javax.swing.JSpinner();
        spinnerDirtMaxLevel = new javax.swing.JSpinner();
        jLabel11 = new javax.swing.JLabel();
        jLabel91 = new javax.swing.JLabel();
        jLabel42 = new javax.swing.JLabel();
        jLabel89 = new javax.swing.JLabel();
        spinnerRedstoneMaxLevel = new javax.swing.JSpinner();
        jPanel22 = new javax.swing.JPanel();
        jLabel101 = new javax.swing.JLabel();
        jLabel100 = new javax.swing.JLabel();
        spinnerQuartzChance = new javax.swing.JSpinner();
        jLabel74 = new javax.swing.JLabel();
        jLabel76 = new javax.swing.JLabel();
        jLabel98 = new javax.swing.JLabel();
        spinnerQuartzMaxLevel = new javax.swing.JSpinner();
        spinnerAncientDebrisMaxLevel = new javax.swing.JSpinner();
        spinnerAncientDebrisMinLevel = new javax.swing.JSpinner();
        jLabel75 = new javax.swing.JLabel();
        jLabel99 = new javax.swing.JLabel();
        spinnerAncientDebrisChance = new javax.swing.JSpinner();
        spinnerQuartzMinLevel = new javax.swing.JSpinner();
        jLabel77 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        checkBoxPopulate = new javax.swing.JCheckBox();
        checkBoxDeciduousEverywhere = new javax.swing.JCheckBox();
        sliderDeciduousLevel = new javax.swing.JSlider();
        checkBoxFrostEverywhere = new javax.swing.JCheckBox();
        checkBoxPineEverywhere = new javax.swing.JCheckBox();
        sliderPineLevel = new javax.swing.JSlider();
        checkBoxSmoothSnow = new javax.swing.JCheckBox();
        jLabel47 = new javax.swing.JLabel();
        jLabel48 = new javax.swing.JLabel();
        jLabel49 = new javax.swing.JLabel();
        jLabel50 = new javax.swing.JLabel();
        checkBoxJungleEverywhere = new javax.swing.JCheckBox();
        jLabel68 = new javax.swing.JLabel();
        sliderJungleLevel = new javax.swing.JSlider();
        checkBoxSwamplandEverywhere = new javax.swing.JCheckBox();
        jLabel69 = new javax.swing.JLabel();
        jSlider6 = new javax.swing.JSlider();
        checkBoxSnowUnderTrees = new javax.swing.JCheckBox();
        checkBoxExportAnnotations = new javax.swing.JCheckBox();
        jPanel7 = new javax.swing.JPanel();
        jLabel82 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tableCustomLayers = new javax.swing.JTable();
        buttonCustomLayerUp = new javax.swing.JButton();
        buttonCustomLayerDown = new javax.swing.JButton();
        buttonCustomLayerTop = new javax.swing.JButton();
        buttonCustomLayerBottom = new javax.swing.JButton();
        buttonDisableLayers = new javax.swing.JButton();
        buttonEnableLayers = new javax.swing.JButton();
        buttonSelectPaint = new javax.swing.JButton();
        buttonReset = new javax.swing.JButton();

        jLabel6.setText("\u5730\u4E0B\u90E8\u5206\u6750\u8D28:");

        comboBoxSubsurfaceMaterial.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboBoxSubsurfaceMaterialActionPerformed(evt);
            }
        });

        jLabel65.setText("\u9876\u90E8\u8986\u76D6\u5C42\u6700\u5C0F\u6DF1\u5EA6:");

        spinnerMinSurfaceDepth.setModel(new javax.swing.SpinnerNumberModel(3, 1, 255, 1));
        spinnerMinSurfaceDepth.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerMinSurfaceDepthStateChanged(evt);
            }
        });

        jLabel66.setText(", \u6700\u5927\u6DF1\u5EA6:");

        spinnerMaxSurfaceDepth.setModel(new javax.swing.SpinnerNumberModel(7, 1, 255, 1));
        spinnerMaxSurfaceDepth.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerMaxSurfaceDepthStateChanged(evt);
            }
        });

        checkBoxBottomless.setText("\u65E0\u5E95\u4E16\u754C");
        checkBoxBottomless.setToolTipText("<html>\u751F\u6210\u4E00\u4E2A\u65E0\u5E95\u7684\u4E16\u754C:\n<ul><li>\u5730\u56FE\u5E95\u90E8\u6CA1\u6709\u57FA\u5CA9\n<li>\u6D1E\u7A74\u548C\u5CE1\u8C37\u76F4\u901A\u865A\u7A7A</html>");

        jLabel67.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/pepsoft/worldpainter/icons/information.png"))); // NOI18N
        jLabel67.setText(" ");
        jLabel67.setToolTipText("<html>\u751F\u6210\u4E00\u4E2A\u65E0\u5E95\u7684\u4E16\u754C:\n<ul><li>\u5730\u56FE\u5E95\u90E8\u6CA1\u6709\u57FA\u5CA9\n<li>\u6D1E\u7A74\u548C\u5CE1\u8C37\u76F4\u901A\u865A\u7A7A</html>");

        checkBoxCoverSteepTerrain.setText("\u8986\u76D6\u9661\u5CED\u7684\u5730\u5F62");
        checkBoxCoverSteepTerrain.setToolTipText("<html>\u542F\u7528\u6B64\u529F\u80FD\u53EF\u4EE5\u5728\u9661\u5CED\u7684\u5730\u5F62\uFF08\u5982\u60AC\u5D16\uFF09\u4E0A\u5411\u4E0B\u5EF6\u4F38\u9876\u90E8\u8986\u76D6\u5C42<br>\n\u8FD9\u6837\u5730\u4E0B\u90E8\u5206\u7684\u6750\u8D28\u5C31\u6C38\u8FDC\u4E0D\u4F1A\u66B4\u9732\u51FA\u6765.</html>");

        jLabel78.setText("\u9876\u5C42\u7EF4\u5EA6\u9AD8\u5EA6:");

        spinnerCeilingHeight.setModel(new javax.swing.SpinnerNumberModel(256, 1, 256, 1));

        comboBoxSurfaceLayerAnchor.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "\u57FA\u5CA9\u5C42", "\u5730\u9762" }));

        jLabel83.setText("\u76F8\u5BF9\u4E8E:");

        jLabel84.setText("\u76F8\u5BF9\u4E8E:");

        comboBoxUndergroundLayerAnchor.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "\u57FA\u5CA9\u5C42", "\u5730\u9762" }));
        comboBoxUndergroundLayerAnchor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboBoxUndergroundLayerAnchorActionPerformed(evt);
            }
        });

        jLabel4.setText("\u8FB9\u754C:");

        checkBoxWall.setText("\u5899\u58C1:");
        checkBoxWall.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxWallActionPerformed(evt);
            }
        });

        spinnerBorderLevel.setModel(new javax.swing.SpinnerNumberModel(62, 0, 127, 1));
        spinnerBorderLevel.setEnabled(false);

        buttonGroup1.add(radioButtonWaterBorder);
        radioButtonWaterBorder.setText("\u6C34");
        radioButtonWaterBorder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonWaterBorderActionPerformed(evt);
            }
        });

        buttonGroup3.add(radioButtonEndlessBorder);
        radioButtonEndlessBorder.setText("\u65E0\u5C3D");
        radioButtonEndlessBorder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonEndlessBorderActionPerformed(evt);
            }
        });

        jLabel8.setLabelFor(spinnerBorderSize);
        jLabel8.setText("\u8FB9\u754C\u5C3A\u5BF8:");

        spinnerBorderSize.setModel(new javax.swing.SpinnerNumberModel(256, 128, null, 128));
        spinnerBorderSize.setEnabled(false);
        spinnerBorderSize.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerBorderSizeStateChanged(evt);
            }
        });

        buttonGroup5.add(radioButtonBedrockRoof);
        radioButtonBedrockRoof.setSelected(true);
        radioButtonBedrockRoof.setText("\u57FA\u5CA9");
        radioButtonBedrockRoof.setEnabled(false);
        radioButtonBedrockRoof.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonBedrockRoofActionPerformed(evt);
            }
        });

        buttonGroup1.add(radioButtonLavaBorder);
        radioButtonLavaBorder.setText("\u5CA9\u6D46");
        radioButtonLavaBorder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonLavaBorderActionPerformed(evt);
            }
        });

        jLabel5.setText("\u6C34\u6216\u5CA9\u6D46\u5E73\u9762\u9AD8\u5EA6:");

        buttonGroup1.add(radioButtonNoBorder);
        radioButtonNoBorder.setText("\u4E0D\u914D\u7F6E\u8FB9\u754C\uFF0C\u5730\u56FE\u5916\u90E8\u5206\u7531Minecraft\u81EA\u884C\u751F\u6210");
        radioButtonNoBorder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonNoBorderActionPerformed(evt);
            }
        });

        buttonGroup5.add(radioButtonBarrierRoof);
        radioButtonBarrierRoof.setText("\u5C4F\u969C");
        radioButtonBarrierRoof.setEnabled(false);
        radioButtonBarrierRoof.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonBarrierRoofActionPerformed(evt);
            }
        });

        checkBoxRoof.setText("\u5929\u82B1\u677F:");
        checkBoxRoof.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxRoofActionPerformed(evt);
            }
        });

        jLabel9.setText("\u683C (128\u7684\u6574\u6570\u500D)");

        buttonGroup3.add(radioButtonFixedBorder);
        radioButtonFixedBorder.setSelected(true);
        radioButtonFixedBorder.setText("\u56FA\u5B9A:");
        radioButtonFixedBorder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonFixedBorderActionPerformed(evt);
            }
        });

        buttonGroup2.add(radioButtonBedrockWall);
        radioButtonBedrockWall.setSelected(true);
        radioButtonBedrockWall.setText("\u57FA\u5CA9");
        radioButtonBedrockWall.setEnabled(false);
        radioButtonBedrockWall.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonBedrockWallActionPerformed(evt);
            }
        });

        jLabel44.setText("(Minecraft \u9ED8\u8BA4\u503C: 62)");

        buttonGroup1.add(radioButtonVoidBorder);
        radioButtonVoidBorder.setText("\u865A\u7A7A");
        radioButtonVoidBorder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonVoidBorderActionPerformed(evt);
            }
        });

        buttonGroup2.add(radioButtonBarrierWall);
        radioButtonBarrierWall.setText("\u5C4F\u969C");
        radioButtonBarrierWall.setEnabled(false);
        radioButtonBarrierWall.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonBarrierWallActionPerformed(evt);
            }
        });

        buttonGroup1.add(radioButtonBarrierBorder);
        radioButtonBarrierBorder.setText("\u5C4F\u969C");
        radioButtonBarrierBorder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonBarrierBorderActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelBorderWallRoofLayout = new javax.swing.GroupLayout(panelBorderWallRoof);
        panelBorderWallRoof.setLayout(panelBorderWallRoofLayout);
        panelBorderWallRoofLayout.setHorizontalGroup(
            panelBorderWallRoofLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBorderWallRoofLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(panelBorderWallRoofLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(radioButtonNoBorder)
                    .addComponent(radioButtonVoidBorder)
                    .addComponent(radioButtonLavaBorder)
                    .addGroup(panelBorderWallRoofLayout.createSequentialGroup()
                        .addGroup(panelBorderWallRoofLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(radioButtonWaterBorder)
                            .addComponent(jLabel8))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelBorderWallRoofLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(radioButtonEndlessBorder)
                            .addGroup(panelBorderWallRoofLayout.createSequentialGroup()
                                .addComponent(radioButtonFixedBorder)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(panelBorderWallRoofLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(panelBorderWallRoofLayout.createSequentialGroup()
                                        .addComponent(spinnerBorderSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, 0)
                                        .addComponent(jLabel9))
                                    .addGroup(panelBorderWallRoofLayout.createSequentialGroup()
                                        .addComponent(jLabel5)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(spinnerBorderLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(jLabel44)))))
                    .addComponent(radioButtonBarrierBorder)
                    .addGroup(panelBorderWallRoofLayout.createSequentialGroup()
                        .addComponent(checkBoxWall)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(radioButtonBedrockWall)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(radioButtonBarrierWall)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(checkBoxRoof)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(radioButtonBedrockRoof)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(radioButtonBarrierRoof)))
                .addGap(0, 0, 0))
        );
        panelBorderWallRoofLayout.setVerticalGroup(
            panelBorderWallRoofLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBorderWallRoofLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(radioButtonNoBorder)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(radioButtonVoidBorder)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelBorderWallRoofLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(radioButtonWaterBorder)
                    .addComponent(jLabel5)
                    .addComponent(spinnerBorderLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelBorderWallRoofLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(radioButtonLavaBorder)
                    .addComponent(jLabel44))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(radioButtonBarrierBorder)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelBorderWallRoofLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(spinnerBorderSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9)
                    .addComponent(radioButtonFixedBorder))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(radioButtonEndlessBorder)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelBorderWallRoofLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(checkBoxWall)
                    .addComponent(checkBoxRoof)
                    .addComponent(radioButtonBedrockRoof)
                    .addComponent(radioButtonBarrierRoof)
                    .addComponent(radioButtonBarrierWall)
                    .addComponent(radioButtonBedrockWall))
                .addGap(0, 0, 0))
        );

        buttonGeneratorOptions.setText("...");
        buttonGeneratorOptions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonGeneratorOptionsActionPerformed(evt);
            }
        });

        jLabel95.setText("\u79CD\u5B50:");

        jLabel94.setText("\u4E16\u754C\u7C7B\u578B:");

        spinnerMinecraftSeed.setModel(new javax.swing.SpinnerNumberModel(-9223372036854775808L, null, null, 1L));
        spinnerMinecraftSeed.setEditor(new javax.swing.JSpinner.NumberEditor(spinnerMinecraftSeed, "0"));

        jLabel7.setText("Minecraft\u8BBE\u7F6E:");

        comboBoxGenerator.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboBoxGeneratorActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelMinecraftSettingsLayout = new javax.swing.GroupLayout(panelMinecraftSettings);
        panelMinecraftSettings.setLayout(panelMinecraftSettingsLayout);
        panelMinecraftSettingsLayout.setHorizontalGroup(
            panelMinecraftSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMinecraftSettingsLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel94)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(comboBoxGenerator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonGeneratorOptions)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel95)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(spinnerMinecraftSeed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );
        panelMinecraftSettingsLayout.setVerticalGroup(
            panelMinecraftSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMinecraftSettingsLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(panelMinecraftSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(spinnerMinecraftSeed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(comboBoxGenerator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel94)
                    .addComponent(jLabel95)
                    .addComponent(buttonGeneratorOptions))
                .addGap(0, 0, 0))
        );

        jLabel79.setText("\u5730\u4E0B\u90E8\u5206\u7FA4\u7CFB:");

        javax.swing.GroupLayout panelGeneralLayout = new javax.swing.GroupLayout(panelGeneral);
        panelGeneral.setLayout(panelGeneralLayout);
        panelGeneralLayout.setHorizontalGroup(
            panelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelGeneralLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelBorderWallRoof, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panelGeneralLayout.createSequentialGroup()
                        .addComponent(jLabel65)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spinnerMinSurfaceDepth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(jLabel66)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spinnerMaxSurfaceDepth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(checkBoxCoverSteepTerrain)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel83)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(comboBoxSurfaceLayerAnchor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelGeneralLayout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(comboBoxSubsurfaceMaterial, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel84)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(comboBoxUndergroundLayerAnchor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel79)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(comboBoxSubsurfaceBiome, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelGeneralLayout.createSequentialGroup()
                        .addComponent(checkBoxBottomless)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel67)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel78)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spinnerCeilingHeight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(panelMinecraftSettings, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelGeneralLayout.setVerticalGroup(
            panelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelGeneralLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel65)
                    .addComponent(spinnerMinSurfaceDepth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel66)
                    .addComponent(spinnerMaxSurfaceDepth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(checkBoxCoverSteepTerrain)
                    .addComponent(comboBoxSurfaceLayerAnchor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel83))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(comboBoxSubsurfaceMaterial, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel84)
                    .addComponent(comboBoxUndergroundLayerAnchor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel79)
                    .addComponent(comboBoxSubsurfaceBiome, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(checkBoxBottomless)
                    .addComponent(jLabel67, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel78)
                    .addComponent(spinnerCeilingHeight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(19, 19, 19)
                .addComponent(panelBorderWallRoof, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(panelMinecraftSettings, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(67, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("\u4E00\u822C\u8BBE\u7F6E", panelGeneral);

        jLabel45.setText("\u8FD9\u4E9B\u662F\u9020\u5C71\u5DE5\u5177\u4EE5\u53CA\u5DF2\u9009\u62E9\u65B9\u5757\u65F6\u53F3\u952E\u4E00\u4E2A\u65B9\u5757");

        jLabel46.setText("\u5C06\u5176\u91CD\u7F6E\u4E3A\u9ED8\u8BA4\u503C\u65F6\u4F7F\u7528\u7684\u65B9\u5757\u548C\u8986\u76D6\u5C42\u7C7B\u578B:");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(themeEditor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jLabel46, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel45, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel45)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel46)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(themeEditor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("\u4E3B\u9898", jPanel5);

        jPanel3.setLayout(new java.awt.GridBagLayout());

        checkBoxCavesEverywhere.setText("\u968F\u5904\u751F\u6210\u6D1E\u7A74");
        checkBoxCavesEverywhere.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxCavesEverywhereActionPerformed(evt);
            }
        });

        sliderCavesEverywhereLevel.setMajorTickSpacing(1);
        sliderCavesEverywhereLevel.setMaximum(15);
        sliderCavesEverywhereLevel.setMinimum(1);
        sliderCavesEverywhereLevel.setPaintTicks(true);
        sliderCavesEverywhereLevel.setSnapToTicks(true);
        sliderCavesEverywhereLevel.setValue(8);

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addGap(19, 19, 19)
                        .addComponent(sliderCavesEverywhereLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(checkBoxCavesEverywhere))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(checkBoxCavesEverywhere)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sliderCavesEverywhereLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        jPanel3.add(jPanel9, gridBagConstraints);

        checkBoxCavernsEverywhere.setText("\u968F\u5904\u751F\u6210\u6D1E\u7A9F");
        checkBoxCavernsEverywhere.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxCavernsEverywhereActionPerformed(evt);
            }
        });

        sliderCavernsEverywhereLevel.setMajorTickSpacing(1);
        sliderCavernsEverywhereLevel.setMaximum(15);
        sliderCavernsEverywhereLevel.setMinimum(1);
        sliderCavernsEverywhereLevel.setPaintTicks(true);
        sliderCavernsEverywhereLevel.setSnapToTicks(true);
        sliderCavernsEverywhereLevel.setValue(8);

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addGap(19, 19, 19)
                        .addComponent(sliderCavernsEverywhereLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(checkBoxCavernsEverywhere))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(checkBoxCavernsEverywhere)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sliderCavernsEverywhereLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        jPanel3.add(jPanel10, gridBagConstraints);

        checkBoxChasmsEverywhere.setText("\u968F\u5904\u751F\u6210\u5CE1\u8C37");
        checkBoxChasmsEverywhere.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxChasmsEverywhereActionPerformed(evt);
            }
        });

        sliderChasmsEverywhereLevel.setMajorTickSpacing(1);
        sliderChasmsEverywhereLevel.setMaximum(15);
        sliderChasmsEverywhereLevel.setMinimum(1);
        sliderChasmsEverywhereLevel.setPaintTicks(true);
        sliderChasmsEverywhereLevel.setSnapToTicks(true);
        sliderChasmsEverywhereLevel.setValue(8);

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel11Layout.createSequentialGroup()
                        .addGap(19, 19, 19)
                        .addComponent(sliderChasmsEverywhereLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(checkBoxChasmsEverywhere))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(checkBoxChasmsEverywhere)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sliderChasmsEverywhereLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        jPanel3.add(jPanel11, gridBagConstraints);

        jLabel2.setText("\u6D1E\u7A74\u3001\u6D1E\u7A9F\u548C\u5CE1\u8C37\u8986\u76D6\u5C42\u7684\u8BBE\u7F6E\uFF08\u8FD9\u4E9B\u8BBE\u7F6E\u4E5F\u9002\u7528\u4E8E\u624B\u7ED8\u6D1E\u7A74\u3001\u6D1E\u7A9F\u548C\u5CE1\u8C37\uFF09:");

        javax.swing.GroupLayout jPanel16Layout = new javax.swing.GroupLayout(jPanel16);
        jPanel16.setLayout(jPanel16Layout);
        jPanel16Layout.setHorizontalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel16Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel16Layout.setVerticalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel16Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addContainerGap())
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        jPanel3.add(jPanel16, gridBagConstraints);

        jLabel87.setText("\u6D1E\u7A74\u6700\u4F4E\u9AD8\u5EA6:");

        spinnerCavesMinLevel.setModel(new javax.swing.SpinnerNumberModel(0, 0, 255, 1));

        jLabel88.setText("\u6D1E\u7A74\u6700\u9AD8\u9AD8\u5EA6:");

        spinnerCavesMaxLevel.setModel(new javax.swing.SpinnerNumberModel(0, 0, 255, 1));

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel13Layout.createSequentialGroup()
                        .addComponent(jLabel87)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spinnerCavesMinLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel13Layout.createSequentialGroup()
                        .addComponent(jLabel88)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spinnerCavesMaxLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel87)
                    .addComponent(spinnerCavesMinLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel88)
                    .addComponent(spinnerCavesMaxLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel3.add(jPanel13, gridBagConstraints);

        jLabel70.setText("\u6D1E\u7A9F\u6700\u4F4E\u9AD8\u5EA6:");

        spinnerCavernsMinLevel.setModel(new javax.swing.SpinnerNumberModel(0, 0, 255, 1));

        jLabel71.setText("\u6D1E\u7A9F\u6700\u9AD8\u9AD8\u5EA6:");

        spinnerCavernsMaxLevel.setModel(new javax.swing.SpinnerNumberModel(255, 0, 255, 1));

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel12Layout.createSequentialGroup()
                        .addComponent(jLabel70)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spinnerCavernsMinLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel12Layout.createSequentialGroup()
                        .addComponent(jLabel71)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spinnerCavernsMaxLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel70)
                    .addComponent(spinnerCavernsMinLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel71)
                    .addComponent(spinnerCavernsMaxLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel3.add(jPanel12, gridBagConstraints);

        jLabel73.setText("\u5CE1\u8C37\u6700\u4F4E\u9AD8\u5EA6:");

        spinnerChasmsMinLevel.setModel(new javax.swing.SpinnerNumberModel(0, 0, 255, 1));

        jLabel72.setText("\u5CE1\u8C37\u6700\u9AD8\u9AD8\u5EA6:");

        spinnerChasmsMaxLevel.setModel(new javax.swing.SpinnerNumberModel(255, 0, 255, 1));

        javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
        jPanel14.setLayout(jPanel14Layout);
        jPanel14Layout.setHorizontalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel14Layout.createSequentialGroup()
                        .addComponent(jLabel73)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spinnerChasmsMinLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel14Layout.createSequentialGroup()
                        .addComponent(jLabel72)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spinnerChasmsMaxLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        jPanel14Layout.setVerticalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel73)
                    .addComponent(spinnerChasmsMinLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel72)
                    .addComponent(spinnerChasmsMaxLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel3.add(jPanel14, gridBagConstraints);

        checkBoxFloodCaverns.setText("\u6DB2\u4F53\u586B\u5145");
        checkBoxFloodCaverns.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxFloodCavernsActionPerformed(evt);
            }
        });

        jLabel1.setText("\u9AD8\u5EA6:");

        spinnerCavernsFloodLevel.setModel(new javax.swing.SpinnerNumberModel(16, 1, 127, 1));

        checkBoxCavernsFloodWithLava.setText("\u7528\u5CA9\u6D46\u66FF\u6362\u6C34:");
        checkBoxCavernsFloodWithLava.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(checkBoxFloodCaverns)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGap(19, 19, 19)
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(checkBoxCavernsFloodWithLava)
                            .addGroup(jPanel8Layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spinnerCavernsFloodLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(checkBoxFloodCaverns)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(spinnerCavernsFloodLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(checkBoxCavernsFloodWithLava)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        jPanel3.add(jPanel8, gridBagConstraints);

        checkBoxCavesBreakSurface.setText("\u6D1E\u7A74\u7A81\u7834\u5730\u8868");
        checkBoxCavesBreakSurface.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxCavesBreakSurfaceActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel17Layout = new javax.swing.GroupLayout(jPanel17);
        jPanel17.setLayout(jPanel17Layout);
        jPanel17Layout.setHorizontalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel17Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(checkBoxCavesBreakSurface)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel17Layout.setVerticalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel17Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(checkBoxCavesBreakSurface)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        jPanel3.add(jPanel17, gridBagConstraints);

        checkBoxCavernsBreakSurface.setText("\u6D1E\u7A9F\u7A81\u7834\u5730\u8868");
        checkBoxCavernsBreakSurface.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxCavernsBreakSurfaceActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel18Layout = new javax.swing.GroupLayout(jPanel18);
        jPanel18.setLayout(jPanel18Layout);
        jPanel18Layout.setHorizontalGroup(
            jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel18Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(checkBoxCavernsBreakSurface)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel18Layout.setVerticalGroup(
            jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel18Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(checkBoxCavernsBreakSurface)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        jPanel3.add(jPanel18, gridBagConstraints);

        checkBoxChasmsBreakSurface.setText("\u5CE1\u8C37\u7A81\u7834\u5730\u8868");
        checkBoxChasmsBreakSurface.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxChasmsBreakSurfaceActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel19Layout = new javax.swing.GroupLayout(jPanel19);
        jPanel19.setLayout(jPanel19Layout);
        jPanel19Layout.setHorizontalGroup(
            jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel19Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(checkBoxChasmsBreakSurface)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel19Layout.setVerticalGroup(
            jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel19Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(checkBoxChasmsBreakSurface)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        jPanel3.add(jPanel19, gridBagConstraints);

        checkBoxCavernsRemoveWater.setSelected(true);
        checkBoxCavernsRemoveWater.setText("\u79FB\u9664\u5F00\u653E\u533A\u57DF\u4E4B\u4E0A\u7684\u6C34\u548C\u5CA9\u6D46");
        checkBoxCavernsRemoveWater.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 19, 0, 0);
        jPanel3.add(checkBoxCavernsRemoveWater, gridBagConstraints);

        javax.swing.GroupLayout jPanel15Layout = new javax.swing.GroupLayout(jPanel15);
        jPanel15.setLayout(jPanel15Layout);
        jPanel15Layout.setHorizontalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel15Layout.setVerticalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel3.add(jPanel15, gridBagConstraints);

        javax.swing.GroupLayout jPanel23Layout = new javax.swing.GroupLayout(jPanel23);
        jPanel23.setLayout(jPanel23Layout);
        jPanel23Layout.setHorizontalGroup(
            jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel23Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSeparator5, javax.swing.GroupLayout.DEFAULT_SIZE, 789, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel23Layout.setVerticalGroup(
            jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel23Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jSeparator5, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel3.add(jPanel23, gridBagConstraints);

        jLabel96.setText("\u88C5\u9970\u7269:");

        checkBoxDecorationBrownMushrooms.setText("\u68D5\u8272\u8611\u83C7");

        checkBoxDecorationGlowLichen.setText("\u53D1\u5149\u5730\u8863");

        checkBoxDecorationLushCaves.setText("\u7E41\u8302\u6D1E\u7A74\u88C5\u9970");

        checkBoxDecorationDripstoneCaves.setText("\u6EF4\u6C34\u6D1E\u7A74\u88C5\u9970");

        javax.swing.GroupLayout jPanel24Layout = new javax.swing.GroupLayout(jPanel24);
        jPanel24.setLayout(jPanel24Layout);
        jPanel24Layout.setHorizontalGroup(
            jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel24Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel96)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(checkBoxDecorationDripstoneCaves)
                    .addComponent(checkBoxDecorationLushCaves)
                    .addComponent(checkBoxDecorationGlowLichen)
                    .addComponent(checkBoxDecorationBrownMushrooms))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel24Layout.setVerticalGroup(
            jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel24Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel96)
                    .addComponent(checkBoxDecorationBrownMushrooms))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(checkBoxDecorationGlowLichen)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(checkBoxDecorationLushCaves)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(checkBoxDecorationDripstoneCaves)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        jPanel3.add(jPanel24, gridBagConstraints);

        checkBoxDecorateCaves.setText("\u88C5\u9970");
        checkBoxDecorateCaves.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxDecorateCavesActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel25Layout = new javax.swing.GroupLayout(jPanel25);
        jPanel25.setLayout(jPanel25Layout);
        jPanel25Layout.setHorizontalGroup(
            jPanel25Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel25Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(checkBoxDecorateCaves)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel25Layout.setVerticalGroup(
            jPanel25Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel25Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(checkBoxDecorateCaves)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        jPanel3.add(jPanel25, gridBagConstraints);

        checkBoxDecorateCaverns.setText("\u88C5\u9970");
        checkBoxDecorateCaverns.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxDecorateCavernsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel26Layout = new javax.swing.GroupLayout(jPanel26);
        jPanel26.setLayout(jPanel26Layout);
        jPanel26Layout.setHorizontalGroup(
            jPanel26Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel26Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(checkBoxDecorateCaverns)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel26Layout.setVerticalGroup(
            jPanel26Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel26Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(checkBoxDecorateCaverns)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        jPanel3.add(jPanel26, gridBagConstraints);

        checkBoxDecorateChasms.setText("\u88C5\u9970");
        checkBoxDecorateChasms.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxDecorateChasmsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel27Layout = new javax.swing.GroupLayout(jPanel27);
        jPanel27.setLayout(jPanel27Layout);
        jPanel27Layout.setHorizontalGroup(
            jPanel27Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel27Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(checkBoxDecorateChasms)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel27Layout.setVerticalGroup(
            jPanel27Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel27Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(checkBoxDecorateChasms)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        jPanel3.add(jPanel27, gridBagConstraints);

        jTabbedPane1.addTab("\u6D1E\u7A74\u3001\u6D1E\u7A9F\u548C\u5CE1\u8C37", new javax.swing.ImageIcon(getClass().getResource("/org/pepsoft/worldpainter/icons/caverns.png")), jPanel3); // NOI18N

        jCheckBox8.setText("\u968F\u5904\u751F\u6210\u8D44\u6E90");
        jCheckBox8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox8ActionPerformed(evt);
            }
        });

        jSlider4.setMajorTickSpacing(1);
        jSlider4.setMaximum(15);
        jSlider4.setMinimum(1);
        jSlider4.setPaintTicks(true);
        jSlider4.setSnapToTicks(true);
        jSlider4.setValue(8);

        jLabel10.setText("\u8D44\u6E90\u8986\u76D6\u5C42\u5728 50% \u786C\u5EA6\u4E0B\u7684\u8BBE\u7F6E(\u8FD9\u4E9B\u8BBE\u7F6E\u540C\u6837\u9002\u7528\u4E8E\u624B\u52A8\u7ED8\u5236\u7684\u8D44\u6E90\u8986\u76D6\u5C42)\uFF1A");

        jSeparator3.setOrientation(javax.swing.SwingConstants.VERTICAL);

        jPanel20.setLayout(new java.awt.GridBagLayout());

        spinnerIronMaxLevel.setModel(new javax.swing.SpinnerNumberModel(1, 0, 127, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel20.add(spinnerIronMaxLevel, gridBagConstraints);

        jLabel35.setText("\u683C");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel20.add(jLabel35, gridBagConstraints);

        spinnerWaterChance.setModel(new javax.swing.SpinnerNumberModel(0, 0, 1000, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel20.add(spinnerWaterChance, gridBagConstraints);

        jLabel29.setText("\u2030");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel20.add(jLabel29, gridBagConstraints);

        spinnerWaterMinLevel.setModel(new javax.swing.SpinnerNumberModel(0, 0, 127, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel20.add(spinnerWaterMinLevel, gridBagConstraints);

        jLabel14.setText("\u9AD8\u5EA6:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel20.add(jLabel14, gridBagConstraints);

        spinnerEmeraldMinLevel.setModel(new javax.swing.SpinnerNumberModel(0, 0, 127, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel20.add(spinnerEmeraldMinLevel, gridBagConstraints);

        jLabel54.setText("-");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel20.add(jLabel54, gridBagConstraints);

        spinnerCoalChance.setModel(new javax.swing.SpinnerNumberModel(0, 0, 1000, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel20.add(spinnerCoalChance, gridBagConstraints);

        jLabel20.setText("\u6C34:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel20.add(jLabel20, gridBagConstraints);

        jLabel53.setText("\u683C");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel20.add(jLabel53, gridBagConstraints);

        spinnerCoalMaxLevel.setModel(new javax.swing.SpinnerNumberModel(1, 0, 127, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel20.add(spinnerCoalMaxLevel, gridBagConstraints);

        spinnerCoalMinLevel.setModel(new javax.swing.SpinnerNumberModel(0, 0, 127, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel20.add(spinnerCoalMinLevel, gridBagConstraints);

        jLabel13.setText("\u751F\u6210\u7387:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel20.add(jLabel13, gridBagConstraints);

        jLabel34.setText("\u683C");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel20.add(jLabel34, gridBagConstraints);

        jLabel55.setText("-");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel20.add(jLabel55, gridBagConstraints);

        jLabel37.setText("\u683C");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel20.add(jLabel37, gridBagConstraints);

        spinnerEmeraldMaxLevel.setModel(new javax.swing.SpinnerNumberModel(1, 0, 127, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel20.add(spinnerEmeraldMaxLevel, gridBagConstraints);

        spinnerLapisChance.setModel(new javax.swing.SpinnerNumberModel(0, 0, 1000, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel20.add(spinnerLapisChance, gridBagConstraints);

        jLabel25.setText("\u2030");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel20.add(jLabel25, gridBagConstraints);

        spinnerIronChance.setModel(new javax.swing.SpinnerNumberModel(0, 0, 1000, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel20.add(spinnerIronChance, gridBagConstraints);

        jLabel51.setText("\u7EFF\u5B9D\u77F3:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel20.add(jLabel51, gridBagConstraints);

        jLabel22.setText("\u2030");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel20.add(jLabel22, gridBagConstraints);

        spinnerGoldChance.setModel(new javax.swing.SpinnerNumberModel(0, 0, 1000, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel20.add(spinnerGoldChance, gridBagConstraints);

        spinnerLapisMinLevel.setModel(new javax.swing.SpinnerNumberModel(0, 0, 127, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel20.add(spinnerLapisMinLevel, gridBagConstraints);

        spinnerGoldMaxLevel.setModel(new javax.swing.SpinnerNumberModel(1, 0, 127, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel20.add(spinnerGoldMaxLevel, gridBagConstraints);

        spinnerDiamondMinLevel.setModel(new javax.swing.SpinnerNumberModel(0, 0, 127, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel20.add(spinnerDiamondMinLevel, gridBagConstraints);

        jLabel56.setText("-");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel20.add(jLabel56, gridBagConstraints);

        spinnerWaterMaxLevel.setModel(new javax.swing.SpinnerNumberModel(1, 0, 127, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel20.add(spinnerWaterMaxLevel, gridBagConstraints);

        jLabel26.setText("\u2030");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel20.add(jLabel26, gridBagConstraints);

        jLabel59.setText("-");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel20.add(jLabel59, gridBagConstraints);

        jLabel16.setText("\u7164\u70AD:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel20.add(jLabel16, gridBagConstraints);

        jLabel15.setText("\u94C1:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel20.add(jLabel15, gridBagConstraints);

        jLabel17.setText("\u9752\u91D1\u77F3:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel20.add(jLabel17, gridBagConstraints);

        spinnerLapisMaxLevel.setModel(new javax.swing.SpinnerNumberModel(1, 0, 127, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel20.add(spinnerLapisMaxLevel, gridBagConstraints);

        jLabel40.setText("\u683C");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel20.add(jLabel40, gridBagConstraints);

        jLabel18.setText("\u94BB\u77F3:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel20.add(jLabel18, gridBagConstraints);

        spinnerDiamondMaxLevel.setModel(new javax.swing.SpinnerNumberModel(1, 0, 127, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel20.add(spinnerDiamondMaxLevel, gridBagConstraints);

        jLabel21.setText("\u5CA9\u6D46:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel20.add(jLabel21, gridBagConstraints);

        jLabel36.setText("\u683C");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel20.add(jLabel36, gridBagConstraints);

        spinnerLavaMinLevel.setModel(new javax.swing.SpinnerNumberModel(0, 0, 127, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel20.add(spinnerLavaMinLevel, gridBagConstraints);

        jLabel23.setText("\u2030");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel20.add(jLabel23, gridBagConstraints);

        jLabel60.setText("-");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel20.add(jLabel60, gridBagConstraints);

        spinnerLavaMaxLevel.setModel(new javax.swing.SpinnerNumberModel(1, 0, 127, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel20.add(spinnerLavaMaxLevel, gridBagConstraints);

        jLabel24.setText("\u2030");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel20.add(jLabel24, gridBagConstraints);

        jLabel61.setText("-");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel20.add(jLabel61, gridBagConstraints);

        jLabel52.setText("\u2030");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel20.add(jLabel52, gridBagConstraints);

        jLabel28.setText("\u2030");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel20.add(jLabel28, gridBagConstraints);

        jLabel57.setText("-");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel20.add(jLabel57, gridBagConstraints);

        jLabel58.setText("-");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel20.add(jLabel58, gridBagConstraints);

        spinnerIronMinLevel.setModel(new javax.swing.SpinnerNumberModel(0, 0, 127, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel20.add(spinnerIronMinLevel, gridBagConstraints);

        spinnerGoldMinLevel.setModel(new javax.swing.SpinnerNumberModel(0, 0, 127, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel20.add(spinnerGoldMinLevel, gridBagConstraints);

        jLabel38.setText("\u683C");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel20.add(jLabel38, gridBagConstraints);

        spinnerLavaChance.setModel(new javax.swing.SpinnerNumberModel(0, 0, 1000, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel20.add(spinnerLavaChance, gridBagConstraints);

        jLabel41.setText("\u683C");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel20.add(jLabel41, gridBagConstraints);

        spinnerEmeraldChance.setModel(new javax.swing.SpinnerNumberModel(0, 0, 1000, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel20.add(spinnerEmeraldChance, gridBagConstraints);

        jLabel12.setText("\u91D1:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel20.add(jLabel12, gridBagConstraints);

        spinnerDiamondChance.setModel(new javax.swing.SpinnerNumberModel(0, 0, 1000, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel20.add(spinnerDiamondChance, gridBagConstraints);

        jPanel21.setLayout(new java.awt.GridBagLayout());

        jLabel63.setText("-");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel21.add(jLabel63, gridBagConstraints);

        jLabel92.setText("-");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel21.add(jLabel92, gridBagConstraints);

        jLabel3.setText("\u751F\u6210\u7387:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel21.add(jLabel3, gridBagConstraints);

        spinnerCopperMinLevel.setModel(new javax.swing.SpinnerNumberModel(0, 0, 127, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel21.add(spinnerCopperMinLevel, gridBagConstraints);

        spinnerGravelChance.setModel(new javax.swing.SpinnerNumberModel(0, 0, 1000, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel21.add(spinnerGravelChance, gridBagConstraints);

        spinnerRedstoneMinLevel.setModel(new javax.swing.SpinnerNumberModel(0, 0, 127, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel21.add(spinnerRedstoneMinLevel, gridBagConstraints);

        jLabel62.setText("-");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel21.add(jLabel62, gridBagConstraints);

        jLabel39.setText("\u683C");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel21.add(jLabel39, gridBagConstraints);

        jLabel64.setText("-");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel21.add(jLabel64, gridBagConstraints);

        jLabel43.setText("\u683C");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel21.add(jLabel43, gridBagConstraints);

        spinnerCopperChance.setModel(new javax.swing.SpinnerNumberModel(0, 0, 1000, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel21.add(spinnerCopperChance, gridBagConstraints);

        spinnerDirtChance.setModel(new javax.swing.SpinnerNumberModel(0, 0, 1000, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel21.add(spinnerDirtChance, gridBagConstraints);

        jLabel32.setText("\u7802\u783E:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel21.add(jLabel32, gridBagConstraints);

        jLabel93.setText("\u683C");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel21.add(jLabel93, gridBagConstraints);

        jLabel30.setText("\u6CE5\u571F:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel21.add(jLabel30, gridBagConstraints);

        jLabel33.setText("\u2030");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel21.add(jLabel33, gridBagConstraints);

        spinnerRedstoneChance.setModel(new javax.swing.SpinnerNumberModel(0, 0, 1000, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel21.add(spinnerRedstoneChance, gridBagConstraints);

        jLabel19.setText("\u7EA2\u77F3:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel21.add(jLabel19, gridBagConstraints);

        spinnerGravelMaxLevel.setModel(new javax.swing.SpinnerNumberModel(1, 0, 127, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel21.add(spinnerGravelMaxLevel, gridBagConstraints);

        spinnerCopperMaxLevel.setModel(new javax.swing.SpinnerNumberModel(1, 0, 127, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel21.add(spinnerCopperMaxLevel, gridBagConstraints);

        jLabel27.setText("\u2030");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel21.add(jLabel27, gridBagConstraints);

        spinnerGravelMinLevel.setModel(new javax.swing.SpinnerNumberModel(0, 0, 127, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel21.add(spinnerGravelMinLevel, gridBagConstraints);

        jLabel31.setText("\u2030");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel21.add(jLabel31, gridBagConstraints);

        spinnerDirtMinLevel.setModel(new javax.swing.SpinnerNumberModel(0, 0, 127, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel21.add(spinnerDirtMinLevel, gridBagConstraints);

        spinnerDirtMaxLevel.setModel(new javax.swing.SpinnerNumberModel(1, 0, 127, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel21.add(spinnerDirtMaxLevel, gridBagConstraints);

        jLabel11.setText("\u9AD8\u5EA6:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel21.add(jLabel11, gridBagConstraints);

        jLabel91.setText("\u94DC:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel21.add(jLabel91, gridBagConstraints);

        jLabel42.setText("\u683C");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel21.add(jLabel42, gridBagConstraints);

        jLabel89.setText("\u2030");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel21.add(jLabel89, gridBagConstraints);

        spinnerRedstoneMaxLevel.setModel(new javax.swing.SpinnerNumberModel(1, 0, 127, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel21.add(spinnerRedstoneMaxLevel, gridBagConstraints);

        jPanel22.setLayout(new java.awt.GridBagLayout());

        jLabel101.setText("\u2030");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel22.add(jLabel101, gridBagConstraints);

        jLabel100.setText("-");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel22.add(jLabel100, gridBagConstraints);

        spinnerQuartzChance.setModel(new javax.swing.SpinnerNumberModel(0, 0, 1000, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel22.add(spinnerQuartzChance, gridBagConstraints);

        jLabel74.setText("\u4E0B\u754C\u77F3\u82F1:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel22.add(jLabel74, gridBagConstraints);

        jLabel76.setText("\u683C");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel22.add(jLabel76, gridBagConstraints);

        jLabel98.setText("\u683C");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel22.add(jLabel98, gridBagConstraints);

        spinnerQuartzMaxLevel.setModel(new javax.swing.SpinnerNumberModel(1, 0, 127, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel22.add(spinnerQuartzMaxLevel, gridBagConstraints);

        spinnerAncientDebrisMaxLevel.setModel(new javax.swing.SpinnerNumberModel(1, 0, 127, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel22.add(spinnerAncientDebrisMaxLevel, gridBagConstraints);

        spinnerAncientDebrisMinLevel.setModel(new javax.swing.SpinnerNumberModel(0, 0, 127, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel22.add(spinnerAncientDebrisMinLevel, gridBagConstraints);

        jLabel75.setText("\u2030");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel22.add(jLabel75, gridBagConstraints);

        jLabel99.setText("\u8FDC\u53E4\u6B8B\u9AB8:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel22.add(jLabel99, gridBagConstraints);

        spinnerAncientDebrisChance.setModel(new javax.swing.SpinnerNumberModel(0, 0, 1000, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel22.add(spinnerAncientDebrisChance, gridBagConstraints);

        spinnerQuartzMinLevel.setModel(new javax.swing.SpinnerNumberModel(0, 0, 127, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel22.add(spinnerQuartzMinLevel, gridBagConstraints);

        jLabel77.setText("-");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel22.add(jLabel77, gridBagConstraints);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(jSlider4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jCheckBox8)
                    .addComponent(jLabel10)
                    .addComponent(jSeparator2)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jPanel20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, 333, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel21, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel22, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jCheckBox8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSlider4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jSeparator3)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jPanel21, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, 11, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel22, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel20, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("\u8D44\u6E90", new javax.swing.ImageIcon(getClass().getResource("/org/pepsoft/worldpainter/icons/resources.png")), jPanel4); // NOI18N

        checkBoxPopulate.setText("\u5141\u8BB8Minecraft\u586B\u5145\u6574\u4E2A\u5730\u5F62");
        checkBoxPopulate.setToolTipText("<html>\u8FD9\u5C06\u6807\u8BB0\u6574\u4E2A\u5730\u5F62\u4E3A\u672A\u88AB\u586B\u5145\uFF0C\u5BFC\u81F4 Minecraft \u5728\u52A0\u8F7D\u533A\u5757\u65F6\u751F\u6210\u6811\u6728\u3001<br/>\u6C34\u6C60\u3001\u5CA9\u6D46\u6C60\u3001\u6CE5\u571F\u3001\u783E\u77F3\u3001\u7164\u77FF\u3001\u77FF\u77F3\u7B49\uFF08\u4F46\u4E0D\u5305\u62EC\u6D1E\u7A74\uFF09\u3002<br/>\u8FD9\u5C06\u51CF\u6162\u6BCF\u4E2A\u533A\u5757\u7684\u521D\u59CB\u52A0\u8F7D\u901F\u5EA6\uFF0C\u5E76\u4E14\u4F60\u65E0\u6CD5\u63A7\u5236\u6811\u6728\u3001\u96EA\u7B49\u7684\u751F\u6210\u4F4D\u7F6E\u3002</html>");
        checkBoxPopulate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxPopulateActionPerformed(evt);
            }
        });

        checkBoxDeciduousEverywhere.setText("\u968F\u5904\u751F\u6210\u843D\u53F6\u6797");
        checkBoxDeciduousEverywhere.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxDeciduousEverywhereActionPerformed(evt);
            }
        });

        sliderDeciduousLevel.setMajorTickSpacing(1);
        sliderDeciduousLevel.setMaximum(15);
        sliderDeciduousLevel.setMinimum(1);
        sliderDeciduousLevel.setPaintTicks(true);
        sliderDeciduousLevel.setSnapToTicks(true);
        sliderDeciduousLevel.setValue(8);

        checkBoxFrostEverywhere.setText("\u968F\u5904\u751F\u6210\u971C\u51BB");

        checkBoxPineEverywhere.setText("\u968F\u5904\u751F\u6210\u9488\u53F6\u6797\u6797");
        checkBoxPineEverywhere.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxPineEverywhereActionPerformed(evt);
            }
        });

        sliderPineLevel.setMajorTickSpacing(1);
        sliderPineLevel.setMaximum(15);
        sliderPineLevel.setMinimum(1);
        sliderPineLevel.setPaintTicks(true);
        sliderPineLevel.setSnapToTicks(true);
        sliderPineLevel.setValue(8);

        checkBoxSmoothSnow.setText("\u4F7F\u96EA\u66F4\u5E73\u6ED1 (\u4E5F\u9002\u7528\u4E8E\u624B\u7ED8\u7684\u971C\u51BB\u8986\u76D6\u5C42)");

        jLabel47.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/pepsoft/worldpainter/icons/populate.png"))); // NOI18N

        jLabel48.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/pepsoft/worldpainter/icons/deciduousforest.png"))); // NOI18N

        jLabel49.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/pepsoft/worldpainter/icons/pineforest.png"))); // NOI18N

        jLabel50.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/pepsoft/worldpainter/icons/frost.png"))); // NOI18N

        checkBoxJungleEverywhere.setText("\u968F\u5904\u751F\u6210\u4E1B\u6797");
        checkBoxJungleEverywhere.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxJungleEverywhereActionPerformed(evt);
            }
        });

        jLabel68.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/pepsoft/worldpainter/icons/jungle.png"))); // NOI18N

        sliderJungleLevel.setMajorTickSpacing(1);
        sliderJungleLevel.setMaximum(15);
        sliderJungleLevel.setMinimum(1);
        sliderJungleLevel.setPaintTicks(true);
        sliderJungleLevel.setSnapToTicks(true);
        sliderJungleLevel.setValue(8);

        checkBoxSwamplandEverywhere.setText("\u968F\u5904\u751F\u6210\u6CBC\u6CFD");
        checkBoxSwamplandEverywhere.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxSwamplandEverywhereActionPerformed(evt);
            }
        });

        jLabel69.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/pepsoft/worldpainter/icons/swampland.png"))); // NOI18N

        jSlider6.setMajorTickSpacing(1);
        jSlider6.setMaximum(15);
        jSlider6.setMinimum(1);
        jSlider6.setPaintTicks(true);
        jSlider6.setSnapToTicks(true);
        jSlider6.setValue(8);

        checkBoxSnowUnderTrees.setText("\u4F7F\u6811\u4E0B\u4E5F\u80FD\u88AB\u971C\u51BB (\u4E5F\u9002\u7528\u4E8E\u624B\u7ED8\u7684\u971C\u51BB\u8986\u76D6\u5C42)");

        checkBoxExportAnnotations.setText("\u4EE5\u67D3\u8272\u7F8A\u6BDB\u7684\u5F62\u5F0F\u5BFC\u51FA\u6807\u6CE8");
        checkBoxExportAnnotations.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxExportAnnotationsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(checkBoxPopulate)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel47))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(checkBoxFrostEverywhere)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel50))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(checkBoxSmoothSnow)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(sliderDeciduousLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(sliderPineLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(67, 67, 67)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addComponent(checkBoxJungleEverywhere)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel68))
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addComponent(checkBoxSwamplandEverywhere)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel69))
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addGap(12, 12, 12)
                                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jSlider6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(sliderJungleLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                            .addComponent(checkBoxSnowUnderTrees)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(checkBoxDeciduousEverywhere)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(checkBoxPineEverywhere)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel49)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel48))
                    .addComponent(checkBoxExportAnnotations))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(checkBoxPopulate)
                            .addComponent(jLabel47))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(checkBoxDeciduousEverywhere)
                                    .addComponent(jLabel48))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(sliderDeciduousLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(checkBoxJungleEverywhere)
                                    .addComponent(jLabel68))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(sliderJungleLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(checkBoxPineEverywhere)
                                    .addComponent(jLabel49)))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addComponent(jLabel69))))
                    .addComponent(checkBoxSwamplandEverywhere, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(sliderPineLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jSlider6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(checkBoxFrostEverywhere)
                    .addComponent(jLabel50))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(checkBoxSmoothSnow)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(checkBoxSnowUnderTrees)
                .addGap(18, 18, 18)
                .addComponent(checkBoxExportAnnotations)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("\u5176\u4ED6\u8986\u76D6\u5C42", jPanel2);

        jLabel82.setText("<html>\u5728\u6B64\u9875\u9762\u4E0A\uFF0C\u60A8\u53EF\u4EE5\u914D\u7F6E\u81EA\u5B9A\u4E49\u8986\u76D6\u5C42\u7684\u5BFC\u51FA\u987A\u5E8F\uFF0C\u4EE5\u53CA\u963B\u6B62\u67D0\u4E9B\u8986\u76D6\u5C42\u5BFC\u51FA.<br>\n\u4F4D\u4E8E\u5217\u8868\u4E0A\u5C42\u7684\u8986\u76D6\u5C42<em>\u5148\u4E8E</em>\u4E0B\u65B9\u8986\u76D6\u5C42\u5BFC\u51FA. \u8986\u76D6\u5C42\u65E0\u6CD5\u5728\u7B2C\u4E00\u5C42\u548C\u7B2C\u4E8C\u5C42\u4E4B\u95F4\u79FB\u52A8\u3002.<br>\n<strong>\u6CE8\u610F:</strong> \u4E00\u65E6\u4F60\u5728\u6B64\u9875\u9762\u4E0A\u81EA\u5B9A\u4E49\u4E86\u8986\u76D6\u5C42\u987A\u5E8F\uFF0C\u65B0\u589E\u8986\u76D6\u5C42\u5C06\u88AB\u6DFB\u52A0\u5230\u5217\u8868\u7684\u672B\u5C3E</html>");

        tableCustomLayers.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        jScrollPane1.setViewportView(tableCustomLayers);

        buttonCustomLayerUp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/pepsoft/worldpainter/icons/up.png"))); // NOI18N
        buttonCustomLayerUp.setText("\u4E0A\u79FB");
        buttonCustomLayerUp.setToolTipText("\u4E0A\u79FB\u8986\u76D6\u5C42");
        buttonCustomLayerUp.setHorizontalAlignment(javax.swing.SwingConstants.LEADING);
        buttonCustomLayerUp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonCustomLayerUpActionPerformed(evt);
            }
        });

        buttonCustomLayerDown.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/pepsoft/worldpainter/icons/down.png"))); // NOI18N
        buttonCustomLayerDown.setText("\u4E0B\u79FB");
        buttonCustomLayerDown.setToolTipText("\u4E0B\u79FB\u8986\u76D6\u5C42");
        buttonCustomLayerDown.setHorizontalAlignment(javax.swing.SwingConstants.LEADING);
        buttonCustomLayerDown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonCustomLayerDownActionPerformed(evt);
            }
        });

        buttonCustomLayerTop.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/pepsoft/worldpainter/icons/top.png"))); // NOI18N
        buttonCustomLayerTop.setText("\u7F6E\u9876");
        buttonCustomLayerTop.setToolTipText("\u5C06\u8986\u76D6\u5C42\u79FB\u81F3\u5217\u8868\u9876\u90E8");
        buttonCustomLayerTop.setHorizontalAlignment(javax.swing.SwingConstants.LEADING);
        buttonCustomLayerTop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonCustomLayerTopActionPerformed(evt);
            }
        });

        buttonCustomLayerBottom.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/pepsoft/worldpainter/icons/bottom.png"))); // NOI18N
        buttonCustomLayerBottom.setText("\u7F6E\u5E95");
        buttonCustomLayerBottom.setToolTipText("\u5C06\u8986\u76D6\u5C42\u79FB\u81F3\u5217\u8868\u5E95\u90E8");
        buttonCustomLayerBottom.setHorizontalAlignment(javax.swing.SwingConstants.LEADING);
        buttonCustomLayerBottom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonCustomLayerBottomActionPerformed(evt);
            }
        });

        buttonDisableLayers.setText("\u5173\u95ED");
        buttonDisableLayers.setToolTipText("\u5173\u95ED\u6240\u9009\u8986\u76D6\u5C42");
        buttonDisableLayers.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonDisableLayersActionPerformed(evt);
            }
        });

        buttonEnableLayers.setText("\u5F00\u542F");
        buttonEnableLayers.setToolTipText("\u5F00\u542F\u6240\u9009\u8986\u76D6\u5C42");
        buttonEnableLayers.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonEnableLayersActionPerformed(evt);
            }
        });

        buttonSelectPaint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/pepsoft/worldpainter/icons/eyedropper.png"))); // NOI18N
        buttonSelectPaint.setText("\u9009\u62E9");
        buttonSelectPaint.setHorizontalAlignment(javax.swing.SwingConstants.LEADING);
        buttonSelectPaint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSelectPaintActionPerformed(evt);
            }
        });

        buttonReset.setText("\u91CD\u7F6E\u987A\u5E8F");
        buttonReset.setToolTipText("\u5F00\u542F\u9009\u4E2D\u7684\u8986\u76D6\u5C42");
        buttonReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonResetActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(jLabel82, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(jScrollPane1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(buttonCustomLayerBottom, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(buttonCustomLayerDown, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(buttonCustomLayerUp, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(buttonDisableLayers, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(buttonEnableLayers, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(buttonCustomLayerTop, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(buttonSelectPaint, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(buttonReset, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel82, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 371, Short.MAX_VALUE)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(buttonSelectPaint)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonCustomLayerTop)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonCustomLayerUp)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonCustomLayerDown)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonCustomLayerBottom)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonDisableLayers)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonEnableLayers)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonReset)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        jTabbedPane1.addTab("\u81EA\u5B9A\u4E49\u8986\u76D6\u5C42", jPanel7);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jTabbedPane1))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void radioButtonWaterBorderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonWaterBorderActionPerformed
        borderChanged();
        setControlStates();
        if ((Integer) spinnerBorderLevel.getValue() == (dimension.getMaxHeight() - 1)) {
            spinnerBorderLevel.setValue(dimension.getBorderLevel());
        }
    }//GEN-LAST:event_radioButtonWaterBorderActionPerformed

    private void radioButtonNoBorderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonNoBorderActionPerformed
        borderChanged();
        setControlStates();
    }//GEN-LAST:event_radioButtonNoBorderActionPerformed

    private void checkBoxPopulateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxPopulateActionPerformed
        if (checkBoxPopulate.isSelected() && jCheckBox8.isSelected() && (! endlessBorder)) {
            jCheckBox8.setSelected(false);
            setControlStates();
            showInfo(this, "\u5DF2\u5173\u95ED\u4F4D\u4E8E\u8D44\u6E90\u6807\u7B7E\u9875\u7684 \"\u968F\u5904\u751F\u6210\u8D44\u6E90\" \u9009\u9879\u4EE5\u9632\u6B62\u8D44\u6E90\u91CD\u590D\u751F\u6210. \u4F60\u53EF\u4EE5\u624B\u52A8\u5F00\u542F.", "\u968F\u5904\u751F\u6210\u8D44\u6E90\u5DF2\u5173\u95ED");
        }
    }//GEN-LAST:event_checkBoxPopulateActionPerformed

    private void radioButtonVoidBorderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonVoidBorderActionPerformed
        borderChanged();
        setControlStates();
    }//GEN-LAST:event_radioButtonVoidBorderActionPerformed

    private void radioButtonLavaBorderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonLavaBorderActionPerformed
        borderChanged();
        setControlStates();
        if ((Integer) spinnerBorderLevel.getValue() == (dimension.getMaxHeight() - 1)) {
            spinnerBorderLevel.setValue(dimension.getBorderLevel());
        }
    }//GEN-LAST:event_radioButtonLavaBorderActionPerformed

    private void comboBoxSubsurfaceMaterialActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboBoxSubsurfaceMaterialActionPerformed
        Terrain terrain = (Terrain) comboBoxSubsurfaceMaterial.getSelectedItem();
        if ((terrain != null) && terrain.isCustom()) {
            MixedMaterial material = Terrain.getCustomMaterial(terrain.getCustomTerrainIndex());
            if (material.getMode() == MixedMaterial.Mode.LAYERED) {
                comboBoxUndergroundLayerAnchor.setSelectedIndex(subsurfaceLayerAnchor.ordinal());
            } else {
                comboBoxUndergroundLayerAnchor.setSelectedItem(null);
            }
        } else {
            comboBoxUndergroundLayerAnchor.setSelectedItem(null);
        }
        setControlStates();
    }//GEN-LAST:event_comboBoxSubsurfaceMaterialActionPerformed

    private void checkBoxDeciduousEverywhereActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxDeciduousEverywhereActionPerformed
        setControlStates();
    }//GEN-LAST:event_checkBoxDeciduousEverywhereActionPerformed

    private void checkBoxPineEverywhereActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxPineEverywhereActionPerformed
        setControlStates();
    }//GEN-LAST:event_checkBoxPineEverywhereActionPerformed

    private void spinnerBorderSizeStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerBorderSizeStateChanged
        int value = (Integer) spinnerBorderSize.getValue();
        value = Math.round(value / 128f) * 128;
        if (value < 128) {
            value = 128;
        }
        spinnerBorderSize.setValue(value);
    }//GEN-LAST:event_spinnerBorderSizeStateChanged

    private void jCheckBox8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox8ActionPerformed
        setControlStates();
    }//GEN-LAST:event_jCheckBox8ActionPerformed

    private void spinnerMinSurfaceDepthStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerMinSurfaceDepthStateChanged
        int topLevelMinDepth = (Integer) spinnerMinSurfaceDepth.getValue();
        int topLevelMaxDepth = (Integer) spinnerMaxSurfaceDepth.getValue();
        if (topLevelMinDepth > topLevelMaxDepth) {
            spinnerMaxSurfaceDepth.setValue(topLevelMinDepth);
        }
    }//GEN-LAST:event_spinnerMinSurfaceDepthStateChanged

    private void spinnerMaxSurfaceDepthStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerMaxSurfaceDepthStateChanged
        int topLevelMinDepth = (Integer) spinnerMinSurfaceDepth.getValue();
        int topLevelMaxDepth = (Integer) spinnerMaxSurfaceDepth.getValue();
        if (topLevelMaxDepth < topLevelMinDepth) {
            spinnerMinSurfaceDepth.setValue(topLevelMaxDepth);
        }
    }//GEN-LAST:event_spinnerMaxSurfaceDepthStateChanged

    private void checkBoxJungleEverywhereActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxJungleEverywhereActionPerformed
        setControlStates();
    }//GEN-LAST:event_checkBoxJungleEverywhereActionPerformed

    private void checkBoxSwamplandEverywhereActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxSwamplandEverywhereActionPerformed
        setControlStates();
    }//GEN-LAST:event_checkBoxSwamplandEverywhereActionPerformed

    private void checkBoxExportAnnotationsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxExportAnnotationsActionPerformed
        setControlStates();
    }//GEN-LAST:event_checkBoxExportAnnotationsActionPerformed

    private void buttonCustomLayerUpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonCustomLayerUpActionPerformed
        moveSelectedRowsUp(1);
    }//GEN-LAST:event_buttonCustomLayerUpActionPerformed

    private void moveSelectedRowsUp(int noOfRows) {
        programmaticChange = true;
        try {
            for (int i = 0; i < noOfRows; i++) {
                int[] rowIndices = tableCustomLayers.getSelectedRows();
                tableCustomLayers.clearSelection();
                for (int rowIndex: rowIndices) {
                    customLayersTableModel.swap(rowIndex - 1, rowIndex);
                    tableCustomLayers.getSelectionModel().addSelectionInterval(rowIndex - 1, rowIndex - 1);
                }
            }
        } finally {
            programmaticChange = false;
        }
        tableCustomLayers.scrollRectToVisible(tableCustomLayers.getCellRect(tableCustomLayers.getSelectedRows()[0], 0, true));
        orderPristine = false;
        setControlStates();
    }

    private void buttonCustomLayerDownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonCustomLayerDownActionPerformed
        moveSelectedRowsDown(1);
    }//GEN-LAST:event_buttonCustomLayerDownActionPerformed

    private void moveSelectedRowsDown(int noOfRows) {
        programmaticChange = true;
        try {
            for (int i = 0; i < noOfRows; i++) {
                int[] rowIndices = tableCustomLayers.getSelectedRows();
                tableCustomLayers.clearSelection();
                for (int j = rowIndices.length - 1; j >= 0; j--) {
                    customLayersTableModel.swap(rowIndices[j] + 1, rowIndices[j]);
                    tableCustomLayers.getSelectionModel().addSelectionInterval(rowIndices[j] + 1, rowIndices[j] + 1);
                }
            }
        } finally {
            programmaticChange = false;
        }
        tableCustomLayers.scrollRectToVisible(tableCustomLayers.getCellRect(tableCustomLayers.getSelectedRows()[tableCustomLayers.getSelectedRowCount() - 1], 0, true));
        orderPristine = false;
        setControlStates();
    }

    private void radioButtonFixedBorderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonFixedBorderActionPerformed
        borderChanged();
        setControlStates();
    }//GEN-LAST:event_radioButtonFixedBorderActionPerformed

    private void radioButtonEndlessBorderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonEndlessBorderActionPerformed
        borderChanged();
        setControlStates();
    }//GEN-LAST:event_radioButtonEndlessBorderActionPerformed

    private void comboBoxUndergroundLayerAnchorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboBoxUndergroundLayerAnchorActionPerformed
        int selectedIndex = comboBoxUndergroundLayerAnchor.getSelectedIndex();
        if (selectedIndex >= 0) {
            subsurfaceLayerAnchor = LayerAnchor.values()[selectedIndex];
        }
    }//GEN-LAST:event_comboBoxUndergroundLayerAnchorActionPerformed

    private void checkBoxCavernsEverywhereActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxCavernsEverywhereActionPerformed
        setControlStates();
    }//GEN-LAST:event_checkBoxCavernsEverywhereActionPerformed

    private void checkBoxCavernsBreakSurfaceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxCavernsBreakSurfaceActionPerformed
        setControlStates();
    }//GEN-LAST:event_checkBoxCavernsBreakSurfaceActionPerformed

    private void checkBoxChasmsBreakSurfaceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxChasmsBreakSurfaceActionPerformed
        setControlStates();
    }//GEN-LAST:event_checkBoxChasmsBreakSurfaceActionPerformed

    private void checkBoxFloodCavernsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxFloodCavernsActionPerformed
        setControlStates();
    }//GEN-LAST:event_checkBoxFloodCavernsActionPerformed

    private void checkBoxChasmsEverywhereActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxChasmsEverywhereActionPerformed
        setControlStates();
    }//GEN-LAST:event_checkBoxChasmsEverywhereActionPerformed

    private void checkBoxCavesEverywhereActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxCavesEverywhereActionPerformed
        setControlStates();
    }//GEN-LAST:event_checkBoxCavesEverywhereActionPerformed

    private void checkBoxCavesBreakSurfaceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxCavesBreakSurfaceActionPerformed
        setControlStates();
    }//GEN-LAST:event_checkBoxCavesBreakSurfaceActionPerformed

    private void buttonGeneratorOptionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonGeneratorOptionsActionPerformed
        if (comboBoxGenerator.getSelectedItem() == CUSTOM) {
            String editedGeneratorOptions = JOptionPane.showInputDialog(this, "\u7F16\u8F91\u81EA\u5B9A\u4E49\u4E16\u754C\u751F\u6210\u5668\u7684\u540D\u79F0:", generatorName);
            if (editedGeneratorOptions != null) {
                generatorName = editedGeneratorOptions;
            }
        } else {
            if (generatorName != null) {
                String editedGeneratorOptions = JOptionPane.showInputDialog(this, "\u7F16\u8F91\u8D85\u5E73\u5766\u6A21\u5F0F\u9884\u8BBE:", generatorName);
                if (editedGeneratorOptions != null) {
                    generatorName = editedGeneratorOptions;
                }
            } else {
                SuperflatPreset mySuperflatPreset = (superflatPreset != null)
                        ? superflatPreset
                        : SuperflatPreset.defaultPreset(platform);
                EditSuperflatPresetDialog dialog = new EditSuperflatPresetDialog(SwingUtilities.windowForComponent(this), platform, mySuperflatPreset);
                dialog.setVisible(true);
                if (! dialog.isCancelled()) {
                    superflatPreset = mySuperflatPreset;
                }
            }
        }
    }//GEN-LAST:event_buttonGeneratorOptionsActionPerformed

    private void comboBoxGeneratorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboBoxGeneratorActionPerformed
        updateGeneratorButtonTooltip();
        setControlStates();
    }//GEN-LAST:event_comboBoxGeneratorActionPerformed

    private void buttonCustomLayerTopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonCustomLayerTopActionPerformed
        int[] selectedRows = tableCustomLayers.getSelectedRows();
        int topSelectedRow = selectedRows[0];
        int headerRow = -1;
        for (int row = topSelectedRow - 1; row >= 0; row--) {
            if (customLayersTableModel.isHeaderRow(row)) {
                headerRow = row;
                break;
            }
        }
        if (headerRow == -1) {
            throw new IllegalStateException("No header row found above selected rows");
        }
        moveSelectedRowsUp(topSelectedRow - headerRow - 1);
    }//GEN-LAST:event_buttonCustomLayerTopActionPerformed

    private void buttonCustomLayerBottomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonCustomLayerBottomActionPerformed
        final int[] selectedRows = tableCustomLayers.getSelectedRows();
        final int bottomSelectedRow = selectedRows[selectedRows.length - 1];
        final int rowCount = tableCustomLayers.getRowCount();
        int headerRow = -1;
        for (int row = bottomSelectedRow + 1; row < rowCount; row++) {
            if (customLayersTableModel.isHeaderRow(row)) {
                headerRow = row;
                break;
            }
        }
        if (headerRow == -1) {
            // "Virtual" header at the end of the table
            headerRow = rowCount;
        }
        moveSelectedRowsDown(headerRow - bottomSelectedRow - 1);
    }//GEN-LAST:event_buttonCustomLayerBottomActionPerformed

    private void checkBoxRoofActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxRoofActionPerformed
        setControlStates();
    }//GEN-LAST:event_checkBoxRoofActionPerformed

    private void checkBoxWallActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxWallActionPerformed
        setControlStates();
    }//GEN-LAST:event_checkBoxWallActionPerformed

    private void radioButtonBedrockRoofActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonBedrockRoofActionPerformed
        setControlStates();
    }//GEN-LAST:event_radioButtonBedrockRoofActionPerformed

    private void radioButtonBarrierRoofActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonBarrierRoofActionPerformed
        setControlStates();
    }//GEN-LAST:event_radioButtonBarrierRoofActionPerformed

    private void radioButtonBarrierBorderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonBarrierBorderActionPerformed
        borderChanged();
        setControlStates();
    }//GEN-LAST:event_radioButtonBarrierBorderActionPerformed

    private void radioButtonBedrockWallActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonBedrockWallActionPerformed
        setControlStates();
    }//GEN-LAST:event_radioButtonBedrockWallActionPerformed

    private void radioButtonBarrierWallActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonBarrierWallActionPerformed
        setControlStates();
    }//GEN-LAST:event_radioButtonBarrierWallActionPerformed

    private void checkBoxDecorateCavesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxDecorateCavesActionPerformed
        setControlStates();
    }//GEN-LAST:event_checkBoxDecorateCavesActionPerformed

    private void checkBoxDecorateCavernsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxDecorateCavernsActionPerformed
        setControlStates();
    }//GEN-LAST:event_checkBoxDecorateCavernsActionPerformed

    private void checkBoxDecorateChasmsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxDecorateChasmsActionPerformed
        setControlStates();
    }//GEN-LAST:event_checkBoxDecorateChasmsActionPerformed

    private void buttonDisableLayersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonDisableLayersActionPerformed
        disableSelectedLayers();
    }//GEN-LAST:event_buttonDisableLayersActionPerformed

    private void buttonEnableLayersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonEnableLayersActionPerformed
        enableSelectedLayers();
    }//GEN-LAST:event_buttonEnableLayersActionPerformed

    private void buttonSelectPaintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSelectPaintActionPerformed
        selectPaintOnMap();
    }//GEN-LAST:event_buttonSelectPaintActionPerformed

    private void buttonResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonResetActionPerformed
        resetOrder();
    }//GEN-LAST:event_buttonResetActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonCustomLayerBottom;
    private javax.swing.JButton buttonCustomLayerDown;
    private javax.swing.JButton buttonCustomLayerTop;
    private javax.swing.JButton buttonCustomLayerUp;
    private javax.swing.JButton buttonDisableLayers;
    private javax.swing.JButton buttonEnableLayers;
    private javax.swing.JButton buttonGeneratorOptions;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup3;
    private javax.swing.ButtonGroup buttonGroup5;
    private javax.swing.JButton buttonReset;
    private javax.swing.JButton buttonSelectPaint;
    private javax.swing.JCheckBox checkBoxBottomless;
    private javax.swing.JCheckBox checkBoxCavernsBreakSurface;
    private javax.swing.JCheckBox checkBoxCavernsEverywhere;
    private javax.swing.JCheckBox checkBoxCavernsFloodWithLava;
    private javax.swing.JCheckBox checkBoxCavernsRemoveWater;
    private javax.swing.JCheckBox checkBoxCavesBreakSurface;
    private javax.swing.JCheckBox checkBoxCavesEverywhere;
    private javax.swing.JCheckBox checkBoxChasmsBreakSurface;
    private javax.swing.JCheckBox checkBoxChasmsEverywhere;
    private javax.swing.JCheckBox checkBoxCoverSteepTerrain;
    private javax.swing.JCheckBox checkBoxDeciduousEverywhere;
    private javax.swing.JCheckBox checkBoxDecorateCaverns;
    private javax.swing.JCheckBox checkBoxDecorateCaves;
    private javax.swing.JCheckBox checkBoxDecorateChasms;
    private javax.swing.JCheckBox checkBoxDecorationBrownMushrooms;
    private javax.swing.JCheckBox checkBoxDecorationDripstoneCaves;
    private javax.swing.JCheckBox checkBoxDecorationGlowLichen;
    private javax.swing.JCheckBox checkBoxDecorationLushCaves;
    private javax.swing.JCheckBox checkBoxExportAnnotations;
    private javax.swing.JCheckBox checkBoxFloodCaverns;
    private javax.swing.JCheckBox checkBoxFrostEverywhere;
    private javax.swing.JCheckBox checkBoxJungleEverywhere;
    private javax.swing.JCheckBox checkBoxPineEverywhere;
    private javax.swing.JCheckBox checkBoxPopulate;
    private javax.swing.JCheckBox checkBoxRoof;
    private javax.swing.JCheckBox checkBoxSmoothSnow;
    private javax.swing.JCheckBox checkBoxSnowUnderTrees;
    private javax.swing.JCheckBox checkBoxSwamplandEverywhere;
    private javax.swing.JCheckBox checkBoxWall;
    private javax.swing.JComboBox<Generator> comboBoxGenerator;
    private javax.swing.JComboBox<Integer> comboBoxSubsurfaceBiome;
    private javax.swing.JComboBox<Terrain> comboBoxSubsurfaceMaterial;
    private javax.swing.JComboBox<String> comboBoxSurfaceLayerAnchor;
    private javax.swing.JComboBox<String> comboBoxUndergroundLayerAnchor;
    private javax.swing.JCheckBox jCheckBox8;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel100;
    private javax.swing.JLabel jLabel101;
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
    private javax.swing.JLabel jLabel55;
    private javax.swing.JLabel jLabel56;
    private javax.swing.JLabel jLabel57;
    private javax.swing.JLabel jLabel58;
    private javax.swing.JLabel jLabel59;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel60;
    private javax.swing.JLabel jLabel61;
    private javax.swing.JLabel jLabel62;
    private javax.swing.JLabel jLabel63;
    private javax.swing.JLabel jLabel64;
    private javax.swing.JLabel jLabel65;
    private javax.swing.JLabel jLabel66;
    private javax.swing.JLabel jLabel67;
    private javax.swing.JLabel jLabel68;
    private javax.swing.JLabel jLabel69;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel70;
    private javax.swing.JLabel jLabel71;
    private javax.swing.JLabel jLabel72;
    private javax.swing.JLabel jLabel73;
    private javax.swing.JLabel jLabel74;
    private javax.swing.JLabel jLabel75;
    private javax.swing.JLabel jLabel76;
    private javax.swing.JLabel jLabel77;
    private javax.swing.JLabel jLabel78;
    private javax.swing.JLabel jLabel79;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel82;
    private javax.swing.JLabel jLabel83;
    private javax.swing.JLabel jLabel84;
    private javax.swing.JLabel jLabel87;
    private javax.swing.JLabel jLabel88;
    private javax.swing.JLabel jLabel89;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabel91;
    private javax.swing.JLabel jLabel92;
    private javax.swing.JLabel jLabel93;
    private javax.swing.JLabel jLabel94;
    private javax.swing.JLabel jLabel95;
    private javax.swing.JLabel jLabel96;
    private javax.swing.JLabel jLabel98;
    private javax.swing.JLabel jLabel99;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel18;
    private javax.swing.JPanel jPanel19;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel20;
    private javax.swing.JPanel jPanel21;
    private javax.swing.JPanel jPanel22;
    private javax.swing.JPanel jPanel23;
    private javax.swing.JPanel jPanel24;
    private javax.swing.JPanel jPanel25;
    private javax.swing.JPanel jPanel26;
    private javax.swing.JPanel jPanel27;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSlider jSlider4;
    private javax.swing.JSlider jSlider6;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JPanel panelBorderWallRoof;
    private javax.swing.JPanel panelGeneral;
    private javax.swing.JPanel panelMinecraftSettings;
    private javax.swing.JRadioButton radioButtonBarrierBorder;
    private javax.swing.JRadioButton radioButtonBarrierRoof;
    private javax.swing.JRadioButton radioButtonBarrierWall;
    private javax.swing.JRadioButton radioButtonBedrockRoof;
    private javax.swing.JRadioButton radioButtonBedrockWall;
    private javax.swing.JRadioButton radioButtonEndlessBorder;
    private javax.swing.JRadioButton radioButtonFixedBorder;
    private javax.swing.JRadioButton radioButtonLavaBorder;
    private javax.swing.JRadioButton radioButtonNoBorder;
    private javax.swing.JRadioButton radioButtonVoidBorder;
    private javax.swing.JRadioButton radioButtonWaterBorder;
    private javax.swing.JSlider sliderCavernsEverywhereLevel;
    private javax.swing.JSlider sliderCavesEverywhereLevel;
    private javax.swing.JSlider sliderChasmsEverywhereLevel;
    private javax.swing.JSlider sliderDeciduousLevel;
    private javax.swing.JSlider sliderJungleLevel;
    private javax.swing.JSlider sliderPineLevel;
    private javax.swing.JSpinner spinnerAncientDebrisChance;
    private javax.swing.JSpinner spinnerAncientDebrisMaxLevel;
    private javax.swing.JSpinner spinnerAncientDebrisMinLevel;
    private javax.swing.JSpinner spinnerBorderLevel;
    private javax.swing.JSpinner spinnerBorderSize;
    private javax.swing.JSpinner spinnerCavernsFloodLevel;
    private javax.swing.JSpinner spinnerCavernsMaxLevel;
    private javax.swing.JSpinner spinnerCavernsMinLevel;
    private javax.swing.JSpinner spinnerCavesMaxLevel;
    private javax.swing.JSpinner spinnerCavesMinLevel;
    private javax.swing.JSpinner spinnerCeilingHeight;
    private javax.swing.JSpinner spinnerChasmsMaxLevel;
    private javax.swing.JSpinner spinnerChasmsMinLevel;
    private javax.swing.JSpinner spinnerCoalChance;
    private javax.swing.JSpinner spinnerCoalMaxLevel;
    private javax.swing.JSpinner spinnerCoalMinLevel;
    private javax.swing.JSpinner spinnerCopperChance;
    private javax.swing.JSpinner spinnerCopperMaxLevel;
    private javax.swing.JSpinner spinnerCopperMinLevel;
    private javax.swing.JSpinner spinnerDiamondChance;
    private javax.swing.JSpinner spinnerDiamondMaxLevel;
    private javax.swing.JSpinner spinnerDiamondMinLevel;
    private javax.swing.JSpinner spinnerDirtChance;
    private javax.swing.JSpinner spinnerDirtMaxLevel;
    private javax.swing.JSpinner spinnerDirtMinLevel;
    private javax.swing.JSpinner spinnerEmeraldChance;
    private javax.swing.JSpinner spinnerEmeraldMaxLevel;
    private javax.swing.JSpinner spinnerEmeraldMinLevel;
    private javax.swing.JSpinner spinnerGoldChance;
    private javax.swing.JSpinner spinnerGoldMaxLevel;
    private javax.swing.JSpinner spinnerGoldMinLevel;
    private javax.swing.JSpinner spinnerGravelChance;
    private javax.swing.JSpinner spinnerGravelMaxLevel;
    private javax.swing.JSpinner spinnerGravelMinLevel;
    private javax.swing.JSpinner spinnerIronChance;
    private javax.swing.JSpinner spinnerIronMaxLevel;
    private javax.swing.JSpinner spinnerIronMinLevel;
    private javax.swing.JSpinner spinnerLapisChance;
    private javax.swing.JSpinner spinnerLapisMaxLevel;
    private javax.swing.JSpinner spinnerLapisMinLevel;
    private javax.swing.JSpinner spinnerLavaChance;
    private javax.swing.JSpinner spinnerLavaMaxLevel;
    private javax.swing.JSpinner spinnerLavaMinLevel;
    private javax.swing.JSpinner spinnerMaxSurfaceDepth;
    private javax.swing.JSpinner spinnerMinSurfaceDepth;
    private javax.swing.JSpinner spinnerMinecraftSeed;
    private javax.swing.JSpinner spinnerQuartzChance;
    private javax.swing.JSpinner spinnerQuartzMaxLevel;
    private javax.swing.JSpinner spinnerQuartzMinLevel;
    private javax.swing.JSpinner spinnerRedstoneChance;
    private javax.swing.JSpinner spinnerRedstoneMaxLevel;
    private javax.swing.JSpinner spinnerRedstoneMinLevel;
    private javax.swing.JSpinner spinnerWaterChance;
    private javax.swing.JSpinner spinnerWaterMaxLevel;
    private javax.swing.JSpinner spinnerWaterMinLevel;
    private javax.swing.JTable tableCustomLayers;
    private org.pepsoft.worldpainter.themes.impl.simple.SimpleThemeEditor themeEditor;
    // End of variables declaration//GEN-END:variables

    private Dimension dimension;
    private Platform platform;
    private PlatformProvider platformProvider;
    private CustomLayersTableModel customLayersTableModel;
    private Mode mode;
    private Dimension.LayerAnchor subsurfaceLayerAnchor;
    private String generatorName;
    private SuperflatPreset superflatPreset;
    private boolean endlessBorder, programmaticChange, orderPristine = true;
    private Tag customGeneratorSettings;
    private Generator savedGeneratorType;
    private Eyedropper.SelectionListener selectionListener;
    private CustomBiomeManager customBiomeManager;

    private static final int TAB_GENERAL       = 0;
    private static final int TAB_THEME         = 1;
    private static final int TAB_CAVES         = 2;
    private static final int TAB_RESOURCES     = 3;
    private static final int TAB_OTHER_LAYERS  = 4;
    private static final int TAB_CUSTOM_LAYERS = 5;

    private static final Logger logger = LoggerFactory.getLogger(DimensionPropertiesEditor.class);
    private static final long serialVersionUID = 1L;

    public enum Mode {EXPORT, DEFAULT_SETTINGS, EDITOR}
}