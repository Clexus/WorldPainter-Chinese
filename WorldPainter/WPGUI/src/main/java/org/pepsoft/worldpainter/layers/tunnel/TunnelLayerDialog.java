/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.layers.tunnel;

import org.pepsoft.util.DesktopUtils;
import org.pepsoft.util.swing.BetterJPopupMenu;
import org.pepsoft.worldpainter.Dimension;
import org.pepsoft.worldpainter.*;
import org.pepsoft.worldpainter.Dimension.Anchor;
import org.pepsoft.worldpainter.biomeschemes.CustomBiomeManager;
import org.pepsoft.worldpainter.exporting.IncidentalLayerExporter;
import org.pepsoft.worldpainter.heightMaps.ConstantHeightMap;
import org.pepsoft.worldpainter.heightMaps.NoiseHeightMap;
import org.pepsoft.worldpainter.layers.*;
import org.pepsoft.worldpainter.layers.groundcover.GroundCoverLayer;
import org.pepsoft.worldpainter.layers.plants.PlantLayer;
import org.pepsoft.worldpainter.layers.tunnel.TunnelLayer.FillMode;
import org.pepsoft.worldpainter.layers.tunnel.TunnelLayer.Mode;
import org.pepsoft.worldpainter.themes.JSpinnerTableCellEditor;
import org.pepsoft.worldpainter.themes.SimpleTheme;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static javax.swing.JOptionPane.*;
import static org.pepsoft.util.AwtUtils.doLaterOnEventThread;
import static org.pepsoft.util.CollectionUtils.listOf;
import static org.pepsoft.worldpainter.Dimension.Role.CAVE_FLOOR;
import static org.pepsoft.worldpainter.Dimension.Role.DETAIL;
import static org.pepsoft.worldpainter.Platform.Capability.*;
import static org.pepsoft.worldpainter.layers.tunnel.TunnelLayer.FillMode.*;
import static org.pepsoft.worldpainter.layers.tunnel.TunnelLayer.LayerMode.CAVE;
import static org.pepsoft.worldpainter.layers.tunnel.TunnelLayersTableModel.*;
import static org.pepsoft.worldpainter.themes.Filter.EVERYWHERE;
import static org.pepsoft.worldpainter.util.BiomeUtils.getAllBiomes;

/**
 *
 * @author SchmitzP
 */
@SuppressWarnings({"unused", "FieldCanBeLocal"}) // Managed by NetBeans
public class TunnelLayerDialog extends AbstractEditLayerDialog<TunnelLayer> implements ChangeListener, ListSelectionListener {
    public TunnelLayerDialog(Window parent, Platform platform, TunnelLayer layer, Dimension dimension, boolean extendedBlockIds, ColourScheme colourScheme, CustomBiomeManager customBiomeManager, int minHeight, int maxHeight, int baseHeight, int waterLevel) {
        this(parent, platform, layer, dimension, extendedBlockIds, colourScheme, customBiomeManager, minHeight, maxHeight, baseHeight, waterLevel, true);
    }

    protected TunnelLayerDialog(Window parent, Platform platform, TunnelLayer layer, Dimension dimension, boolean extendedBlockIds, ColourScheme colourScheme, CustomBiomeManager customBiomeManager, int minHeight, int maxHeight, int baseHeight, int waterLevel, boolean init) {
        super(parent);
        this.platform = platform;
        this.layer = layer;
        this.dimension = dimension;
        this.colourScheme = colourScheme;
        this.customBiomeManager = customBiomeManager;
        this.baseHeight = baseHeight;
        this.waterLevel = waterLevel;
        this.minHeight = minHeight;
        this.maxHeight = maxHeight;

        if (! init) {
            return;
        }
        
        initComponents();
        programmaticChange = true;
        try {
            tableFloorLayers.getSelectionModel().addListSelectionListener(this);
            tableRoofLayers.getSelectionModel().addListSelectionListener(this);
            mixedMaterialChooserFloor.setPlatform(platform);
            mixedMaterialChooserFloor.setExtendedBlockIds(extendedBlockIds);
            mixedMaterialChooserFloor.setColourScheme(colourScheme);
            mixedMaterialChooserRoof.setPlatform(platform);
            mixedMaterialChooserRoof.setExtendedBlockIds(extendedBlockIds);
            mixedMaterialChooserRoof.setColourScheme(colourScheme);
            mixedMaterialChooserWall.setPlatform(platform);
            mixedMaterialChooserWall.setExtendedBlockIds(extendedBlockIds);
            mixedMaterialChooserWall.setColourScheme(colourScheme);
            mixedMaterialChooserFill.setPlatform(platform);
            mixedMaterialChooserFill.setExtendedBlockIds(extendedBlockIds);
            mixedMaterialChooserFill.setColourScheme(colourScheme);
            labelPreview.setPreferredSize(new java.awt.Dimension(128, 0));
            ((SpinnerNumberModel) spinnerFloorLevel.getModel()).setMinimum(minHeight);
            ((SpinnerNumberModel) spinnerFloorLevel.getModel()).setMaximum(maxHeight - 1);
            ((SpinnerNumberModel) spinnerRoofLevel.getModel()).setMinimum(minHeight);
            ((SpinnerNumberModel) spinnerRoofLevel.getModel()).setMaximum(maxHeight - 1);
            ((SpinnerNumberModel) spinnerFloorMin.getModel()).setMinimum(minHeight);
            ((SpinnerNumberModel) spinnerFloorMin.getModel()).setMaximum(maxHeight - 1);
            ((SpinnerNumberModel) spinnerFloorMax.getModel()).setMinimum(minHeight);
            ((SpinnerNumberModel) spinnerFloorMax.getModel()).setMaximum(maxHeight - 1);
            ((SpinnerNumberModel) spinnerRoofMin.getModel()).setMinimum(minHeight);
            ((SpinnerNumberModel) spinnerRoofMin.getModel()).setMaximum(maxHeight - 1);
            ((SpinnerNumberModel) spinnerRoofMax.getModel()).setMinimum(minHeight);
            ((SpinnerNumberModel) spinnerRoofMax.getModel()).setMaximum(maxHeight - 1);
            ((SpinnerNumberModel) spinnerFloodLevel.getModel()).setMinimum(minHeight);
            ((SpinnerNumberModel) spinnerFloodLevel.getModel()).setMaximum(maxHeight - 1);
            comboBoxBiome.setRenderer(new BiomeListCellRenderer(colourScheme, customBiomeManager, "None", platform));
            if (platform.capabilities.contains(BIOMES_3D) || platform.capabilities.contains(NAMED_BIOMES)) {
                comboBoxBiome.setModel(new DefaultComboBoxModel<>(listOf(singletonList(null), getAllBiomes(platform, customBiomeManager)).toArray(new Integer[0])));
            } else {
                comboBoxBiome.setModel(new DefaultComboBoxModel<>(new Integer[] {null}));
                comboBoxBiome.setEnabled(false);
            }
            radioButtonFloorCustomDimension.setEnabled(dimension.getAnchor().role == DETAIL);
        } finally {
            programmaticChange = false;
        }
        
        loadSettings();

        getRootPane().setDefaultButton(buttonOK);
        
        noiseSettingsEditorFloor.addChangeListener(this);
        noiseSettingsEditorRoof.addChangeListener(this);

        scaleToUI();
        pack();
        scaleWindowToUI();
        setLocationRelativeTo(parent);

        // TODO this causes the preview only to become bigger, never smaller:
//        addComponentListener(new ComponentAdapter() {
//            @Override
//            public void componentResized(ComponentEvent e) {
//                updatePreview();
//            }
//        });
        doLaterOnEventThread(this::updatePreview);
    }

    // AbstractEditLayerDialog

    @Override
    public TunnelLayer getLayer() {
        return layer;
    }

    // ListSelectionListener

    @Override
    public void valueChanged(ListSelectionEvent e) {
        setControlStates();
    }
    
    // ChangeListener
    
    @Override
    public void stateChanged(ChangeEvent e) {
        generatePreview();
    }

    @Override
    protected void ok() {
        if (tableFloorLayers.isEditing()) {
            tableFloorLayers.getCellEditor().stopCellEditing();
        }
        if (tableRoofLayers.isEditing()) {
            tableRoofLayers.getCellEditor().stopCellEditing();
        }
        if ((! radioButtonFloorCustomDimension.isSelected()) && (layer.getFloorDimensionId() != null)) {
            DesktopUtils.beep();
            if (JOptionPane.showConfirmDialog(this, "\u8BE5\u5730\u9762\u7EF4\u5EA6\u5C06\u88AB\u5220\u9664.\n\u8BE5\u64CD\u4F5C\u4E0D\u53EF\u64A4\u9500! \u786E\u8BA4\u7EE7\u7EED\u5417?", "\u786E\u8BA4\u5220\u9664", YES_NO_OPTION, WARNING_MESSAGE) != YES_OPTION) {
                return;
            }
            final App app = App.getInstance();
            final Dimension currentDimension = app.getDimension();
            final int dim = currentDimension.getAnchor().dim;
            final World2 world = app.getWorld();
            world.removeDimension(new Anchor(dim, CAVE_FLOOR, false, layer.getFloorDimensionId()));
            layer.setFloorDimensionId(null);
        } else if (radioButtonFloorCustomDimension.isSelected() && (layer.getFloorDimensionId() == null)) {
            final Dimension floorDimension = createFloorDimension();
            if (floorDimension != null) {
                layer.setFloorDimensionId(floorDimension.getAnchor().id);
                final Configuration config = Configuration.getInstance();
                if (! config.isMessageDisplayedCountAtLeast(PAINT_TUNNEL_LAYER_KEY, 3)) {
                    doLaterOnEventThread(() -> JOptionPane.showMessageDialog(App.getInstance(),
                            "\u4F7F\u7528\u753B\u7B14\u5DE5\u5177\u5C06\u81EA\u5B9A\u4E49\u6D1E\u7A74/\u96A7\u9053\u8986\u76d6\u5c42\u6D82\u7ED8\u6210\u6240\u9700\u5F62\u72B6\u3002\n"+
                            "\u7136\u540E\u5728[" + layer.getPalette() + "]\u9762\u677F\u4E2D\uFF0C\u53F3\u952E\u70B9\u51FB[" + layer.getName() + "]\u6309\u94AE\uFF0C\n"+
                            "\u9009\u62E9\u201C\u7F16\u8F91\u5730\u9762\u7EF4\u5EA6\u201D\u6765\u7ED8\u5236\u5E76\u5782\u76F4\u5851\u9020\u6D1E\u7A74\u5730\u9762\u3002"));
                    config.setMessageDisplayed(PAINT_TUNNEL_LAYER_KEY);
                }
            } else {
                // Cancelled by user
                return;
            }
        }
        saveSettingsTo(layer, true);
        dismiss();
    }

    protected final void dismiss() {
        super.ok();
    }

    protected final void updatePreview() {
//        if ((radioButtonFloorFixedLevel.isSelected() && radioButtonRoofFixedLevel.isSelected())
//                || (radioButtonFloorInverse.isSelected() && radioButtonRoofInverse.isSelected())) {
//            labelTunnelHeight.setText("(tunnel height: " + Math.max(((Integer) spinnerRoofLevel.getValue() - (Integer) spinnerFloorLevel.getValue()), 0) + ")");
//        } else if (radioButtonFloorFixedDepth.isSelected() && radioButtonRoofFixedDepth.isSelected()) {
//            labelTunnelHeight.setText("(tunnel height: " + Math.max(((Integer) spinnerFloorLevel.getValue() - (Integer) spinnerRoofLevel.getValue()), 0) + ")");
//        } else {
//            labelTunnelHeight.setText("(tunnel height: variable)");
//        }
        generatePreview();
    }

    protected void generatePreview() {
        final TunnelLayer layer = new TunnelLayer("tmp", CAVE, null, platform);
        saveSettingsTo(layer, false);
        final Insets insets = labelPreview.getInsets();
        final int width = labelPreview.getWidth() - insets.left - insets.right;
        final int height = labelPreview.getHeight() - insets.top - insets.bottom;
        if ((width > 0) && (height > 0)) {
            final BufferedImage preview = TunnelLayerExporter.generatePreview(layer, width, height, waterLevel, minHeight, baseHeight, Math.min(maxHeight - baseHeight, height - baseHeight + minHeight));
            labelPreview.setIcon(new ImageIcon(preview));
        } else {
            labelPreview.setIcon(null);
        }
    }

    private void loadSettings() {
        programmaticChange = true;
        try {
            spinnerFloorLevel.setValue(layer.getFloorLevel());
            spinnerFloorMin.setValue(Math.max(layer.getFloorMin(), minHeight));
            spinnerFloorMax.setValue(Math.min(layer.getFloorMax(), maxHeight - 1));
            mixedMaterialChooserFloor.setMaterial(layer.getFloorMaterial());
            switch (layer.getFloorMode()) {
                case CONSTANT_DEPTH:
                    radioButtonFloorFixedDepth.setSelected(true);
                    break;
                case FIXED_HEIGHT:
                    radioButtonFloorFixedLevel.setSelected(true);
                    break;
                case INVERTED_DEPTH:
                    radioButtonFloorInverse.setSelected(true);
                    break;
                case CUSTOM_DIMENSION:
                    radioButtonFloorCustomDimension.setSelected(true);
                    break;
            }
            NoiseSettings floorNoise = layer.getFloorNoise();
            if (floorNoise == null) {
                floorNoise = new NoiseSettings();
            }
            noiseSettingsEditorFloor.setNoiseSettings(floorNoise);
            spinnerRoofLevel.setValue(layer.getRoofLevel());
            spinnerRoofMin.setValue(Math.max(layer.getRoofMin(), minHeight));
            spinnerRoofMax.setValue(Math.min(layer.getRoofMax(), maxHeight - 1));
            mixedMaterialChooserRoof.setMaterial(layer.getRoofMaterial());
            switch (layer.getRoofMode()) {
                case CONSTANT_DEPTH:
                    radioButtonRoofFixedDepth.setSelected(true);
                    break;
                case FIXED_HEIGHT:
                    radioButtonRoofFixedLevel.setSelected(true);
                    break;
                case INVERTED_DEPTH:
                    radioButtonRoofInverse.setSelected(true);
                    break;
                case FIXED_HEIGHT_ABOVE_FLOOR:
                    radioButtonRoofFixedHeight.setSelected(true);
                    break;
            }
            NoiseSettings roofNoise = layer.getRoofNoise();
            if (roofNoise == null) {
                roofNoise = new NoiseSettings();
            }
            noiseSettingsEditorRoof.setNoiseSettings(roofNoise);
            spinnerWallFloorDepth.setValue(layer.getFloorWallDepth());
            spinnerWallRoofDepth.setValue(layer.getRoofWallDepth());
            mixedMaterialChooserWall.setMaterial(layer.getWallMaterial());
            textFieldName.setText(layer.getName());
            paintPicker1.setPaint(layer.getPaint());
            paintPicker1.setOpacity(layer.getOpacity());
            checkBoxRemoveWater.setSelected(layer.isRemoveWater());
            checkBoxFlood.setSelected(layer.getFloodLevel() != Integer.MIN_VALUE);
            spinnerFloodLevel.setValue((layer.getFloodLevel() != Integer.MIN_VALUE) ? layer.getFloodLevel() : waterLevel);
            checkBoxFloodWithLava.setSelected(layer.isFloodWithLava());

            List<TunnelLayer.LayerSettings> floorLayers = layer.getFloorLayers();
            floorLayersTableModel = new TunnelLayersTableModel(floorLayers, minHeight, maxHeight);
            tableFloorLayers.setModel(floorLayersTableModel);
            tableFloorLayers.getColumnModel().getColumn(COLUMN_NAME).setCellRenderer(new LayerTableCellRenderer());
            SpinnerModel spinnerModel = new SpinnerNumberModel(50, 0, 100, 1);
            tableFloorLayers.getColumnModel().getColumn(COLUMN_INTENSITY).setCellEditor(new JSpinnerTableCellEditor(spinnerModel));
            tableFloorLayers.getColumnModel().getColumn(COLUMN_VARIATION).setCellRenderer(new NoiseSettingsTableCellRenderer());
            spinnerModel = new SpinnerNumberModel(minHeight, minHeight, maxHeight - 1, 1);
            tableFloorLayers.getColumnModel().getColumn(COLUMN_MIN_LEVEL).setCellEditor(new JSpinnerTableCellEditor(spinnerModel));
            spinnerModel = new SpinnerNumberModel(maxHeight - 1, minHeight, maxHeight - 1, 1);
            tableFloorLayers.getColumnModel().getColumn(COLUMN_MAX_LEVEL).setCellEditor(new JSpinnerTableCellEditor(spinnerModel));

            List<TunnelLayer.LayerSettings> roofLayers = layer.getRoofLayers();
            roofLayersTableModel = new TunnelLayersTableModel(roofLayers, minHeight, maxHeight);
            tableRoofLayers.setModel(roofLayersTableModel);
            tableRoofLayers.getColumnModel().getColumn(COLUMN_NAME).setCellRenderer(new LayerTableCellRenderer());
            spinnerModel = new SpinnerNumberModel(50, 0, 100, 1);
            tableRoofLayers.getColumnModel().getColumn(COLUMN_INTENSITY).setCellEditor(new JSpinnerTableCellEditor(spinnerModel));
            tableRoofLayers.getColumnModel().getColumn(COLUMN_VARIATION).setCellRenderer(new NoiseSettingsTableCellRenderer());
            spinnerModel = new SpinnerNumberModel(minHeight, minHeight, maxHeight - 1, 1);
            tableRoofLayers.getColumnModel().getColumn(COLUMN_MIN_LEVEL).setCellEditor(new JSpinnerTableCellEditor(spinnerModel));
            spinnerModel = new SpinnerNumberModel(maxHeight - 1, minHeight, maxHeight - 1, 1);
            tableRoofLayers.getColumnModel().getColumn(COLUMN_MAX_LEVEL).setCellEditor(new JSpinnerTableCellEditor(spinnerModel));

            if (platform.capabilities.contains(BIOMES_3D) || platform.capabilities.contains(NAMED_BIOMES)) {
                comboBoxBiome.setSelectedItem(layer.getTunnelBiome());
            }

            switch (layer.getFillMode()) {
                case CAVE_AIR:
                    radioButtonFillWithCaveAir.setSelected(true);
                    break;
                case AIR:
                    radioButtonFillWithAir.setSelected(true);
                    break;
                case LIGHT:
                    radioButtonFillWithLight.setSelected(true);
                    break;
                case MIXED_MATERIAL:
                    radioButtonFillWithCustom.setSelected(true);
                    mixedMaterialChooserFill.setMaterial(layer.getFillMaterial());
                    break;
            }
        } finally {
            programmaticChange = false;
        }

        setControlStates();
    }

    private void saveSettingsTo(TunnelLayer layer, boolean registerMaterials) {
        layer.setFloorLevel((Integer) spinnerFloorLevel.getValue());
        layer.setFloorMin(((Integer) spinnerFloorMin.getValue() <= minHeight) ? Integer.MIN_VALUE : ((Integer) spinnerFloorMin.getValue()));
        layer.setFloorMax(((Integer) spinnerFloorMax.getValue() >= (maxHeight - 1)) ? Integer.MAX_VALUE : ((Integer) spinnerFloorMax.getValue()));
        MixedMaterial floorMaterial = mixedMaterialChooserFloor.getMaterial();
        if ((floorMaterial != null) && registerMaterials) {
            // Make sure the material is registered, in case it's new
            floorMaterial = MixedMaterialManager.getInstance().register(floorMaterial);
        }
        layer.setFloorMaterial(floorMaterial);
        if (radioButtonFloorFixedDepth.isSelected()) {
            layer.setFloorMode(Mode.CONSTANT_DEPTH);
        } else if (radioButtonFloorFixedLevel.isSelected()) {
            layer.setFloorMode(Mode.FIXED_HEIGHT);
        } else if (radioButtonFloorCustomDimension.isSelected()) {
            layer.setFloorMode(Mode.CUSTOM_DIMENSION);
        } else {
            layer.setFloorMode(Mode.INVERTED_DEPTH);
        }
        NoiseSettings floorNoiseSettings = noiseSettingsEditorFloor.getNoiseSettings();
        if (floorNoiseSettings.getRange() == 0) {
            layer.setFloorNoise(null);
        } else {
            layer.setFloorNoise(floorNoiseSettings);
        }
        layer.setRoofLevel((Integer) spinnerRoofLevel.getValue());
        layer.setRoofMin(((Integer) spinnerRoofMin.getValue() <= minHeight) ? Integer.MIN_VALUE : ((Integer) spinnerRoofMin.getValue()));
        layer.setRoofMax(((Integer) spinnerRoofMax.getValue() >= (maxHeight - 1)) ? Integer.MAX_VALUE : ((Integer) spinnerRoofMax.getValue()));
        MixedMaterial roofMaterial = mixedMaterialChooserRoof.getMaterial();
        if ((roofMaterial != null) && registerMaterials) {
            // Make sure the material is registered, in case it's new
            roofMaterial = MixedMaterialManager.getInstance().register(roofMaterial);
        }
        layer.setRoofMaterial(roofMaterial);
        if (radioButtonRoofFixedDepth.isSelected()) {
            layer.setRoofMode(Mode.CONSTANT_DEPTH);
        } else if (radioButtonRoofFixedLevel.isSelected()) {
            layer.setRoofMode(Mode.FIXED_HEIGHT);
        } else if (radioButtonRoofFixedHeight.isSelected()) {
            layer.setRoofMode(Mode.FIXED_HEIGHT_ABOVE_FLOOR);
        } else {
            layer.setRoofMode(Mode.INVERTED_DEPTH);
        }
        NoiseSettings roofNoiseSettings = noiseSettingsEditorRoof.getNoiseSettings();
        if (roofNoiseSettings.getRange() == 0) {
            layer.setRoofNoise(null);
        } else {
            layer.setRoofNoise(roofNoiseSettings);
        }
        layer.setFloorWallDepth((Integer) spinnerWallFloorDepth.getValue());
        layer.setRoofWallDepth((Integer) spinnerWallRoofDepth.getValue());
        MixedMaterial wallMaterial = mixedMaterialChooserWall.getMaterial();
        if ((wallMaterial != null) && registerMaterials) {
            // Make sure the material is registered, in case it's new
            wallMaterial = MixedMaterialManager.getInstance().register(wallMaterial);
        }
        layer.setWallMaterial(wallMaterial);
        layer.setName(textFieldName.getText().trim());
        layer.setPaint(paintPicker1.getPaint());
        layer.setOpacity(paintPicker1.getOpacity());
        layer.setRemoveWater(checkBoxRemoveWater.isSelected());
        layer.setFloodLevel(checkBoxFlood.isSelected() ? (Integer) spinnerFloodLevel.getValue() : Integer.MIN_VALUE);
        layer.setFloodWithLava(checkBoxFloodWithLava.isSelected());
        if (platform.capabilities.contains(BIOMES_3D) || platform.capabilities.contains(NAMED_BIOMES)) {
            layer.setTunnelBiome((Integer) comboBoxBiome.getSelectedItem());
        }

        List<TunnelLayer.LayerSettings> floorLayers = floorLayersTableModel.getLayers();
        layer.setFloorLayers(((floorLayers != null) && (! floorLayers.isEmpty())) ? floorLayers : null);

        List<TunnelLayer.LayerSettings> roofLayers = roofLayersTableModel.getLayers();
        layer.setRoofLayers(((roofLayers != null) && (! roofLayers.isEmpty())) ? roofLayers : null);

        layer.setFillLightLevel((int) spinnerFillLightLevel.getValue());
        if (radioButtonFillWithCaveAir.isSelected()) {
            layer.setFillMode(CAVE_AIR);
            layer.setFillMaterial(null);
        } else if (radioButtonFillWithAir.isSelected()) {
            layer.setFillMode(AIR);
            layer.setFillMaterial(null);
        } else if (radioButtonFillWithLight.isSelected()) {
            layer.setFillMode(FillMode.LIGHT);
            layer.setFillMaterial(null);
        } else {
            layer.setFillMode(MIXED_MATERIAL);
            layer.setFillMaterial(mixedMaterialChooserFill.getMaterial());
        }
    }

    protected void setControlStates() {
        spinnerFloorLevel.setEnabled(! radioButtonFloorCustomDimension.isSelected());
        spinnerFloorMin.setEnabled((! radioButtonFloorFixedLevel.isSelected()) && (! radioButtonFloorCustomDimension.isSelected()));
        spinnerFloorMax.setEnabled((! radioButtonFloorFixedLevel.isSelected()) && (! radioButtonFloorCustomDimension.isSelected()));
        noiseSettingsEditorFloor.setEnabled(! radioButtonFloorCustomDimension.isSelected());
        mixedMaterialChooserFloor.setEnabled(! radioButtonFloorCustomDimension.isSelected());
        jTabbedPane1.setEnabledAt(1, ! radioButtonFloorCustomDimension.isSelected());

        spinnerRoofMin.setEnabled(! radioButtonRoofFixedLevel.isSelected());
        spinnerRoofMax.setEnabled(! radioButtonRoofFixedLevel.isSelected());
        checkBoxFlood.setEnabled(! radioButtonFloorCustomDimension.isSelected());
        checkBoxFloodWithLava.setEnabled(checkBoxFlood.isSelected() && (! radioButtonFloorCustomDimension.isSelected()));
        spinnerFloodLevel.setEnabled(checkBoxFlood.isSelected() && (! radioButtonFloorCustomDimension.isSelected()));

        int selectedFloorRowCount = tableFloorLayers.getSelectedRowCount();
        buttonRemoveFloorLayer.setEnabled(selectedFloorRowCount > 0);
        buttonEditFloorLayer.setEnabled((selectedFloorRowCount == 1) && (floorLayersTableModel.getLayer(tableFloorLayers.getSelectedRow()) instanceof CustomLayer));

        int selectedRoofRowCount = tableRoofLayers.getSelectedRowCount();
        buttonRemoveRoofLayer.setEnabled(selectedRoofRowCount > 0);
        buttonEditRoofLayer.setEnabled((selectedRoofRowCount == 1) && (roofLayersTableModel.getLayer(tableRoofLayers.getSelectedRow()) instanceof CustomLayer));

        radioButtonFillWithCaveAir.setEnabled(platform.capabilities.contains(NAME_BASED));
        radioButtonFillWithLight.setEnabled(platform.capabilities.contains(BLOCK_MINECRAFT_LIGHT));
        spinnerFillLightLevel.setEnabled(radioButtonFillWithLight.isSelected());
        mixedMaterialChooserFill.setEnabled(radioButtonFillWithCustom.isSelected());
    }

    private void removeFloorLayers() {
        if (tableFloorLayers.isEditing()) {
            tableFloorLayers.getCellEditor().stopCellEditing();
        }
        int[] selectedRows = tableFloorLayers.getSelectedRows();
        for (int i = selectedRows.length - 1; i >= 0; i--) {
            floorLayersTableModel.removeLayer(selectedRows[i]);
        }
    }

    private void removeRoofLayers() {
        if (tableRoofLayers.isEditing()) {
            tableRoofLayers.getCellEditor().stopCellEditing();
        }
        int[] selectedRows = tableRoofLayers.getSelectedRows();
        for (int i = selectedRows.length - 1; i >= 0; i--) {
            roofLayersTableModel.removeLayer(selectedRows[i]);
        }
    }

    private void editFloorLayer() {
        editLayer(tableFloorLayers, floorLayersTableModel);
    }

    private void editRoofLayer() {
        editLayer(tableRoofLayers, roofLayersTableModel);
    }

    protected final void editLayer(JTable table, TunnelLayersTableModel tableModel) {
        final int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            final Layer layer = tableModel.getLayer(selectedRow);
            if (layer instanceof CustomLayer) {
                final EditLayerDialog<Layer> dialog = new EditLayerDialog<>(this, platform, layer);
                dialog.setVisible(() -> tableModel.layerChanged(selectedRow));
            }
        }
    }

    private void addFloorLayer() {
        addLayer(buttonAddFloorLayer, floorLayersTableModel);
    }

    private void addRoofLayer() {
        addLayer(buttonAddRoofLayer, roofLayersTableModel);
    }

    protected final void addLayer(Component button, TunnelLayersTableModel tableModel) {
        JPopupMenu popupMenu = new BetterJPopupMenu();
        LayerManager.getInstance().getLayers().stream()
            .filter(l -> (l.getExporterType() != null) && IncidentalLayerExporter.class.isAssignableFrom(l.getExporterType()))
            .forEach(l -> {
                JMenuItem menuItem = new JMenuItem(l.getName(), new ImageIcon(l.getIcon()));
                menuItem.addActionListener(e -> tableModel.addLayer(l));
                popupMenu.add(menuItem);
            });
        App app = App.getInstance();
        Set<CustomLayer> customLayers = app.getCustomLayers().stream()
            .filter(l -> (l.getExporterType() != null) && IncidentalLayerExporter.class.isAssignableFrom(l.getExporterType()))
            .collect(Collectors.toSet());
        if (customLayers.size() > 15) {
            // If there are fifteen or more custom layers, split them by palette
            // and move them to separate submenus to try and conserve screen
            // space
            app.getCustomLayersByPalette().entrySet().stream()
                .map((entry) -> {
                    String palette = entry.getKey();
                    JMenu paletteMenu = new JMenu(palette != null ? palette : "\u9690\u85CF\u8986\u76d6\u5c42");
                    entry.getValue().stream()
                        .filter(l -> (l.getExporterType() != null) && IncidentalLayerExporter.class.isAssignableFrom(l.getExporterType()))
                        .forEach(l -> {
                            JMenuItem menuItem = new JMenuItem(l.getName(), new ImageIcon(l.getIcon()));
                            menuItem.addActionListener(e -> tableModel.addLayer(l));
                            paletteMenu.add(menuItem);
                        });
                    return paletteMenu;
                }).filter((paletteMenu) -> (paletteMenu.getItemCount() > 0))
                .forEach(popupMenu::add);
        } else {
            customLayers.forEach(l -> {
                JMenuItem menuItem = new JMenuItem(l.getName(), new ImageIcon(l.getIcon()));
                menuItem.addActionListener(e -> tableModel.addLayer(l));
                popupMenu.add(menuItem);
            });
        }
        popupMenu.show(button, button.getWidth(), 0);
    }

    private void newFloorLayer() {
        newLayer(buttonNewFloorLayer, floorLayersTableModel);
    }

    private void newRoofLayer() {
        newLayer(buttonNewRoofLayer, roofLayersTableModel);
    }

    protected final void newLayer(Component button, TunnelLayersTableModel tableModel) {
        JPopupMenu popupMenu = new BetterJPopupMenu();
        JMenuItem item = new JMenuItem("\u81EA\u5B9A\u4E49\u5BF9\u8C61\u8986\u76d6\u5c42");
        item.addActionListener(e -> {
            EditLayerDialog<Bo2Layer> dialog = new EditLayerDialog<>(TunnelLayerDialog.this, platform, Bo2Layer.class);
            dialog.setVisible(() -> {
                Bo2Layer newLayer = dialog.getLayer();
                newLayer.setHide(true);
                tableModel.addLayer(newLayer);
            });
        });
        popupMenu.add(item);
        item = new JMenuItem("\u81EA\u5B9A\u4E49\u5730\u9762\u8986\u76D6\u7269\u8986\u76d6\u5c42");
        item.addActionListener(e -> {
            EditLayerDialog<GroundCoverLayer> dialog = new EditLayerDialog<>(TunnelLayerDialog.this, platform, GroundCoverLayer.class);
            dialog.setVisible(() -> {
                GroundCoverLayer newLayer = dialog.getLayer();
                newLayer.setHide(true);
                tableModel.addLayer(newLayer);
            });
        });
        popupMenu.add(item);
        item = new JMenuItem("\u81EA\u5B9A\u4E49\u690D\u88AB\u8986\u76d6\u5c42");
        item.addActionListener(e -> {
            EditLayerDialog<PlantLayer> dialog = new EditLayerDialog<>(TunnelLayerDialog.this, platform, PlantLayer.class);
            dialog.setVisible(() -> {
                PlantLayer newLayer = dialog.getLayer();
                newLayer.setHide(true);
                tableModel.addLayer(newLayer);
            });
        });
        popupMenu.add(item);
        popupMenu.show(button, button.getWidth(), 0);
    }

    private void editFloorLayerVariation() {
        editLayerVariation(tableFloorLayers, floorLayersTableModel);
    }

    private void editRoofLayerVariation() {
        editLayerVariation(tableRoofLayers, roofLayersTableModel);
    }

    protected final void editLayerVariation(JTable table, TunnelLayersTableModel tableModel) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            NoiseSettings noiseSettings = (NoiseSettings) tableModel.getValueAt(selectedRow, COLUMN_VARIATION);
            NoiseSettingsDialog noiseSettingsDialog = new NoiseSettingsDialog(this, (noiseSettings != null) ? noiseSettings : new NoiseSettings(), (Integer) tableModel.getValueAt(selectedRow, COLUMN_INTENSITY));
            noiseSettingsDialog.setVisible(true);
            if (! noiseSettingsDialog.isCancelled()) {
                noiseSettings = noiseSettingsDialog.getNoiseSettings();
                if (noiseSettings.getRange() == 0) {
                    tableModel.setValueAt(null, selectedRow, COLUMN_VARIATION);
                } else {
                    tableModel.setValueAt(noiseSettings, selectedRow, COLUMN_VARIATION);
                }
                tableModel.layerChanged(selectedRow);
            }
        }
    }

    private Dimension createFloorDimension() {
        final int minHeight = dimension.getMinHeight();
        final int maxHeight = dimension.getMaxHeight();
        final FloorDimensionSettingsDialog dialog = new FloorDimensionSettingsDialog(this, colourScheme, platform,
                minHeight, maxHeight, (int) spinnerFloorLevel.getValue(), noiseSettingsEditorFloor.getNoiseSettings(),
                checkBoxFlood.isSelected() ? ((int) spinnerFloodLevel.getValue()) : minHeight,
                checkBoxFloodWithLava.isSelected(), dimension.getSubsurfaceMaterial());
        dialog.setVisible(true);
        if (! dialog.isCancelled()) {
            final int dim = dimension.getAnchor().dim;
            final boolean invert = dimension.getAnchor().invert;
            final World2 world = dimension.getWorld();
            final int id = findNextId(world, dim, CAVE_FLOOR, invert);
            layer.setFloorDimensionId(id);
            final long seed = dimension.getSeed() + id;
            HeightMap heightMap;
            heightMap = new ConstantHeightMap(dialog.getLevel());
            if (dialog.getVariation().getRange() != 0) {
                heightMap = heightMap.minus(dialog.getVariation().getRange()).plus(new NoiseHeightMap(dialog.getVariation()));
            }
            final SimpleTheme theme = SimpleTheme.createSingleTerrain(dialog.getTerrain(), minHeight, maxHeight, dialog.getWaterLevel());
            if (dialog.getBiome() != null) {
                theme.setLayerMap(singletonMap(EVERYWHERE, Biome.INSTANCE));
                theme.setDiscreteValues(singletonMap(Biome.INSTANCE, dialog.getBiome()));
            }
            final TileFactory tileFactory = new HeightMapTileFactory(seed, heightMap, minHeight, maxHeight, dialog.isFloodWithLava(), theme);
            final Dimension floorDimension = new Dimension(world, null, seed, tileFactory, new Anchor(dim, CAVE_FLOOR, invert, id));
            world.addDimension(floorDimension);
            layer.updateFloorDimension(dimension, textFieldName.getText() + "\u5730\u9762\u7EF4\u5EA6");

            // Also update the layer with the same settings, so that e.g. the preview works approximately right
            layer.setFloorLevel(dialog.getLevel());
            layer.setFloorNoise(dialog.getVariation());
            layer.setFloodLevel(dialog.getWaterLevel());
            layer.setFloodWithLava(dialog.isFloodWithLava());
            layer.setBiome((dialog.getBiome() != null) ? dialog.getBiome() : -1);

            return floorDimension;
        } else {
            return null;
        }
    }

    protected final int findNextId(World2 world, int dim, Dimension.Role role, boolean invert) {
        int layer = 0;
        for (Dimension dimension: world.getDimensions()) {
            final Anchor anchor = dimension.getAnchor();
            if ((anchor.dim == dim) && (anchor.role == role) && (anchor.invert == invert)) {
                layer = Math.max(layer, anchor.id + 1);
            }
        }
        return layer;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings({"Convert2Lambda", "Anonymous2MethodRef"}) // Managed by NetBeans
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup3 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        buttonCancel = new javax.swing.JButton();
        buttonOK = new javax.swing.JButton();
        buttonReset = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        textFieldName = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        radioButtonFloorFixedLevel = new javax.swing.JRadioButton();
        spinnerRoofLevel = new javax.swing.JSpinner();
        jLabel12 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        spinnerFloorLevel = new javax.swing.JSpinner();
        jLabel20 = new javax.swing.JLabel();
        radioButtonRoofFixedDepth = new javax.swing.JRadioButton();
        spinnerFloorMin = new javax.swing.JSpinner();
        noiseSettingsEditorFloor = new org.pepsoft.worldpainter.NoiseSettingsEditor();
        jLabel17 = new javax.swing.JLabel();
        spinnerRoofMin = new javax.swing.JSpinner();
        jLabel18 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        spinnerFloodLevel = new javax.swing.JSpinner();
        radioButtonFloorFixedDepth = new javax.swing.JRadioButton();
        jLabel14 = new javax.swing.JLabel();
        checkBoxFloodWithLava = new javax.swing.JCheckBox();
        spinnerRoofMax = new javax.swing.JSpinner();
        jLabel13 = new javax.swing.JLabel();
        noiseSettingsEditorRoof = new org.pepsoft.worldpainter.NoiseSettingsEditor();
        jLabel8 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        spinnerWallRoofDepth = new javax.swing.JSpinner();
        spinnerWallFloorDepth = new javax.swing.JSpinner();
        checkBoxRemoveWater = new javax.swing.JCheckBox();
        spinnerFloorMax = new javax.swing.JSpinner();
        jLabel19 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        radioButtonFloorInverse = new javax.swing.JRadioButton();
        checkBoxFlood = new javax.swing.JCheckBox();
        radioButtonRoofFixedLevel = new javax.swing.JRadioButton();
        jLabel3 = new javax.swing.JLabel();
        radioButtonRoofInverse = new javax.swing.JRadioButton();
        jLabel15 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        labelPreview = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        comboBoxBiome = new javax.swing.JComboBox<>();
        radioButtonFloorCustomDimension = new javax.swing.JRadioButton();
        radioButtonRoofFixedHeight = new javax.swing.JRadioButton();
        mixedMaterialChooserRoof = new MixedMaterialChooser(true);
        mixedMaterialChooserFloor = new MixedMaterialChooser(true);
        mixedMaterialChooserWall = new MixedMaterialChooser(true);
        jLabel26 = new javax.swing.JLabel();
        radioButtonFillWithAir = new javax.swing.JRadioButton();
        radioButtonFillWithLight = new javax.swing.JRadioButton();
        spinnerFillLightLevel = new javax.swing.JSpinner();
        radioButtonFillWithCustom = new javax.swing.JRadioButton();
        mixedMaterialChooserFill = new MixedMaterialChooser(true);
        radioButtonFillWithCaveAir = new javax.swing.JRadioButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel22 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tableFloorLayers = new javax.swing.JTable();
        buttonNewFloorLayer = new javax.swing.JButton();
        buttonAddFloorLayer = new javax.swing.JButton();
        buttonEditFloorLayer = new javax.swing.JButton();
        buttonRemoveFloorLayer = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel24 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tableRoofLayers = new javax.swing.JTable();
        buttonNewRoofLayer = new javax.swing.JButton();
        buttonAddRoofLayer = new javax.swing.JButton();
        buttonEditRoofLayer = new javax.swing.JButton();
        buttonRemoveRoofLayer = new javax.swing.JButton();
        paintPicker1 = new org.pepsoft.worldpainter.layers.renderers.PaintPicker();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("\u914D\u7F6E\u6D1E\u7A74/\u901A\u9053\u8986\u76D6\u5C42");

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

        buttonReset.setText("\u91CD\u7F6E");
        buttonReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonResetActionPerformed(evt);
            }
        });

        jLabel1.setText("\u4F7F\u7528\u4EE5\u4E0B\u5C5E\u6027\u914D\u7F6E\u6D1E\u7A74/\u901A\u9053\u8986\u76D6\u5C42:");

        jLabel4.setText("\u540D\u79F0:");

        textFieldName.setColumns(20);
        textFieldName.setText("jTextField1");

        jLabel11.setText("\u753b\u7b14");

        buttonGroup1.add(radioButtonFloorFixedLevel);
        radioButtonFloorFixedLevel.setSelected(true);
        radioButtonFloorFixedLevel.setText("\u56FA\u5B9A\u9AD8\u5EA6");
        radioButtonFloorFixedLevel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonFloorFixedLevelActionPerformed(evt);
            }
        });

        spinnerRoofLevel.setModel(new javax.swing.SpinnerNumberModel(0, 0, 255, 1));
        spinnerRoofLevel.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerRoofLevelStateChanged(evt);
            }
        });

        jLabel12.setText("\u5899\u58C1:");

        jLabel5.setText("\u53D8\u5316:");

        jLabel9.setText("\u53D8\u5316:");

        spinnerFloorLevel.setModel(new javax.swing.SpinnerNumberModel(0, 0, 255, 1));
        spinnerFloorLevel.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerFloorLevelStateChanged(evt);
            }
        });

        jLabel20.setText("\u9009\u9879:");

        buttonGroup3.add(radioButtonRoofFixedDepth);
        radioButtonRoofFixedDepth.setText("\u56FA\u5B9A\u6DF1\u5EA6");
        radioButtonRoofFixedDepth.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonRoofFixedDepthActionPerformed(evt);
            }
        });

        spinnerFloorMin.setModel(new javax.swing.SpinnerNumberModel(0, 0, 255, 1));
        spinnerFloorMin.setEnabled(false);
        spinnerFloorMin.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerFloorMinStateChanged(evt);
            }
        });

        jLabel17.setText(", \u6700\u9AD8\u9AD8\u5EA6:");

        spinnerRoofMin.setModel(new javax.swing.SpinnerNumberModel(0, 0, 255, 1));
        spinnerRoofMin.setEnabled(false);
        spinnerRoofMin.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerRoofMinStateChanged(evt);
            }
        });

        jLabel18.setText("\u7EDD\u5BF9\u6700\u4F4E\u9AD8\u5EA6:");

        jLabel2.setText("\u4E0B\u8868\u9762:");

        spinnerFloodLevel.setModel(new javax.swing.SpinnerNumberModel(1, 1, 255, 1));
        spinnerFloodLevel.setEnabled(false);
        spinnerFloodLevel.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerFloodLevelStateChanged(evt);
            }
        });

        buttonGroup1.add(radioButtonFloorFixedDepth);
        radioButtonFloorFixedDepth.setText("\u56FA\u5B9A\u6DF1\u5EA6");
        radioButtonFloorFixedDepth.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonFloorFixedDepthActionPerformed(evt);
            }
        });

        jLabel14.setText("\u6750\u8D28:");

        checkBoxFloodWithLava.setText("\u4F7F\u7528\u5CA9\u6D46\u586B\u5145:");
        checkBoxFloodWithLava.setEnabled(false);
        checkBoxFloodWithLava.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        checkBoxFloodWithLava.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxFloodWithLavaActionPerformed(evt);
            }
        });

        spinnerRoofMax.setModel(new javax.swing.SpinnerNumberModel(255, 0, 255, 1));
        spinnerRoofMax.setEnabled(false);
        spinnerRoofMax.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerRoofMaxStateChanged(evt);
            }
        });

        jLabel13.setText("\u5E95\u90E8\u5BBD\u5EA6:");

        jLabel8.setText("\u9AD8\u5EA6:");

        jLabel16.setText("\u7EDD\u5BF9\u6700\u5C0F\u9AD8\u5EA6:");

        spinnerWallRoofDepth.setModel(new javax.swing.SpinnerNumberModel(0, 0, 255, 1));
        spinnerWallRoofDepth.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerWallRoofDepthStateChanged(evt);
            }
        });

        spinnerWallFloorDepth.setModel(new javax.swing.SpinnerNumberModel(0, 0, 255, 1));
        spinnerWallFloorDepth.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerWallFloorDepthStateChanged(evt);
            }
        });

        checkBoxRemoveWater.setText("\u79FB\u9664\u6C34\u6216\u5CA9\u6D46:");
        checkBoxRemoveWater.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        checkBoxRemoveWater.setMargin(new java.awt.Insets(2, 0, 2, 2));
        checkBoxRemoveWater.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxRemoveWaterActionPerformed(evt);
            }
        });

        spinnerFloorMax.setModel(new javax.swing.SpinnerNumberModel(255, 0, 255, 1));
        spinnerFloorMax.setEnabled(false);
        spinnerFloorMax.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerFloorMaxStateChanged(evt);
            }
        });

        jLabel19.setText(", \u6700\u5927\u9AD8\u5EA6:");

        jLabel7.setText("\u6750\u8D28:");

        jLabel6.setText("\u4E0A\u8868\u9762:");

        buttonGroup1.add(radioButtonFloorInverse);
        radioButtonFloorInverse.setText("\u4E0E\u5730\u8868\u76F8\u53CD");
        radioButtonFloorInverse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonFloorInverseActionPerformed(evt);
            }
        });

        checkBoxFlood.setText("\u4F7F\u7528\u6DB2\u4F53\u586B\u5145:");
        checkBoxFlood.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        checkBoxFlood.setMargin(new java.awt.Insets(2, 0, 2, 2));
        checkBoxFlood.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxFloodActionPerformed(evt);
            }
        });

        buttonGroup3.add(radioButtonRoofFixedLevel);
        radioButtonRoofFixedLevel.setText("\u56FA\u5B9A\u9AD8\u5EA6");
        radioButtonRoofFixedLevel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonRoofFixedLevelActionPerformed(evt);
            }
        });

        jLabel3.setText("\u9AD8\u5EA6:");

        buttonGroup3.add(radioButtonRoofInverse);
        radioButtonRoofInverse.setText("\u4E0E\u5730\u8868\u76F8\u53CD");
        radioButtonRoofInverse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonRoofInverseActionPerformed(evt);
            }
        });

        jLabel15.setText("\u9876\u90E8\u5BBD\u5EA6:");

        jLabel10.setText("\u6750\u8D28:");

        labelPreview.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        jLabel21.setLabelFor(spinnerFloodLevel);
        jLabel21.setText("\u9AD8\u5EA6:");

        jLabel23.setText("\u751F\u7269\u7FA4\u7CFB:");

        buttonGroup1.add(radioButtonFloorCustomDimension);
        radioButtonFloorCustomDimension.setText("\u81EA\u5B9A\u4E49\u7EF4\u5EA6");
        radioButtonFloorCustomDimension.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonFloorCustomDimensionActionPerformed(evt);
            }
        });

        buttonGroup3.add(radioButtonRoofFixedHeight);
        radioButtonRoofFixedHeight.setSelected(true);
        radioButtonRoofFixedHeight.setText("\u56FA\u5B9A\u76F8\u5BF9\u5E95\u90E8\u9AD8\u5EA6");
        radioButtonRoofFixedHeight.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonRoofFixedHeightActionPerformed(evt);
            }
        });

        mixedMaterialChooserRoof.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                mixedMaterialChooserRoofPropertyChange(evt);
            }
        });

        mixedMaterialChooserFloor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                mixedMaterialChooserFloorPropertyChange(evt);
            }
        });

        mixedMaterialChooserWall.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                mixedMaterialChooserWallPropertyChange(evt);
            }
        });

        jLabel26.setText("\u4F7F\u7528...\u586B\u5145:");

        buttonGroup2.add(radioButtonFillWithAir);
        radioButtonFillWithAir.setText("\u7A7A\u6C14");
        radioButtonFillWithAir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonFillWithAirActionPerformed(evt);
            }
        });

        buttonGroup2.add(radioButtonFillWithLight);
        radioButtonFillWithLight.setText("\u5149\u6E90\u65B9\u5757:");
        radioButtonFillWithLight.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonFillWithLightActionPerformed(evt);
            }
        });

        spinnerFillLightLevel.setModel(new javax.swing.SpinnerNumberModel(0, 0, 15, 1));
        spinnerFillLightLevel.setEnabled(false);

        buttonGroup2.add(radioButtonFillWithCustom);
        radioButtonFillWithCustom.setText("\u81EA\u5B9A\u4E49:");
        radioButtonFillWithCustom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonFillWithCustomActionPerformed(evt);
            }
        });

        mixedMaterialChooserFill.setEnabled(false);
        mixedMaterialChooserFill.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                mixedMaterialChooserFillPropertyChange(evt);
            }
        });

        buttonGroup2.add(radioButtonFillWithCaveAir);
        radioButtonFillWithCaveAir.setSelected(true);
        radioButtonFillWithCaveAir.setText("\u6D1E\u7A74\u7A7A\u6C14");
        radioButtonFillWithCaveAir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonFillWithCaveAirActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel12)
                    .addComponent(jLabel6)
                    .addComponent(jLabel2)
                    .addComponent(jLabel20)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel13)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spinnerWallFloorDepth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel15)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spinnerWallRoofDepth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel14)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(mixedMaterialChooserWall, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(radioButtonRoofFixedHeight)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(radioButtonRoofFixedLevel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(radioButtonRoofFixedDepth)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(radioButtonRoofInverse))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel8)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spinnerRoofLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel16)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spinnerRoofMin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, 0)
                                .addComponent(jLabel17)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spinnerRoofMax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spinnerFloorLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel18)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spinnerFloorMin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, 0)
                                .addComponent(jLabel19)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spinnerFloorMax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(checkBoxFlood)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel21)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spinnerFloodLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(checkBoxFloodWithLava))
                            .addComponent(checkBoxRemoveWater)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel23)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(comboBoxBiome, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(radioButtonFloorFixedLevel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(radioButtonFloorFixedDepth)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(radioButtonFloorInverse)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(radioButtonFloorCustomDimension))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel9)
                                    .addComponent(jLabel10))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(mixedMaterialChooserRoof, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(noiseSettingsEditorRoof, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel5)
                                    .addComponent(jLabel7))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(mixedMaterialChooserFloor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(noiseSettingsEditorFloor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel26)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(radioButtonFillWithCaveAir)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(radioButtonFillWithAir)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(radioButtonFillWithLight)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spinnerFillLightLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(radioButtonFillWithCustom)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(mixedMaterialChooserFill, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addGap(18, 18, 18)
                .addComponent(labelPreview, javax.swing.GroupLayout.DEFAULT_SIZE, 128, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(radioButtonRoofFixedLevel)
                            .addComponent(radioButtonRoofFixedDepth)
                            .addComponent(radioButtonRoofInverse)
                            .addComponent(radioButtonRoofFixedHeight))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel8)
                            .addComponent(spinnerRoofLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel16)
                            .addComponent(spinnerRoofMin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel17)
                            .addComponent(spinnerRoofMax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel9)
                            .addComponent(noiseSettingsEditorRoof, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel10)
                            .addComponent(mixedMaterialChooserRoof, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(radioButtonFloorFixedLevel)
                            .addComponent(radioButtonFloorFixedDepth)
                            .addComponent(radioButtonFloorInverse)
                            .addComponent(radioButtonFloorCustomDimension))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel18)
                                .addComponent(spinnerFloorMin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel19)
                                .addComponent(spinnerFloorMax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel3)
                                .addComponent(spinnerFloorLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(noiseSettingsEditorFloor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel7)
                            .addComponent(mixedMaterialChooserFloor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(jLabel12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel13)
                            .addComponent(spinnerWallFloorDepth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel15)
                            .addComponent(spinnerWallRoofDepth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel14)
                            .addComponent(mixedMaterialChooserWall, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(jLabel20)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel23)
                            .addComponent(comboBoxBiome, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(checkBoxRemoveWater)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(checkBoxFlood)
                            .addComponent(jLabel21)
                            .addComponent(spinnerFloodLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(checkBoxFloodWithLava))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel26)
                            .addComponent(radioButtonFillWithAir)
                            .addComponent(radioButtonFillWithLight)
                            .addComponent(spinnerFillLightLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(radioButtonFillWithCustom)
                            .addComponent(mixedMaterialChooserFill, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(radioButtonFillWithCaveAir))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(labelPreview, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        jTabbedPane1.addTab("\u6D1E\u7A74/\u901A\u9053\u8BBE\u7F6E", jPanel1);

        jLabel22.setText("\u4F60\u53EF\u4EE5\u5728\u6B64\u5904\u6DFB\u52A0\u81EA\u5B9A\u4E49\u8986\u76D6\u5C42\uFF0C\u8FD9\u4E9B\u8986\u76D6\u5C42\u5C06\u5728\u6D1E\u7A74/\u96A7\u9053\u5E95\u90E8\u6E32\u67D3:");

        tableFloorLayers.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        tableFloorLayers.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tableFloorLayersMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tableFloorLayers);

        buttonNewFloorLayer.setText("\u65B0\u5EFA");
        buttonNewFloorLayer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonNewFloorLayerActionPerformed(evt);
            }
        });

        buttonAddFloorLayer.setText("\u590D\u5236\u73B0\u6709");
        buttonAddFloorLayer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonAddFloorLayerActionPerformed(evt);
            }
        });

        buttonEditFloorLayer.setText("\u7F16\u8F91");
        buttonEditFloorLayer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonEditFloorLayerActionPerformed(evt);
            }
        });

        buttonRemoveFloorLayer.setText("\u79FB\u9664");
        buttonRemoveFloorLayer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonRemoveFloorLayerActionPerformed(evt);
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
                        .addComponent(jLabel22)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 536, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(buttonAddFloorLayer, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(buttonNewFloorLayer, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(buttonEditFloorLayer, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(buttonRemoveFloorLayer, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel22)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 484, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(buttonNewFloorLayer)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonAddFloorLayer)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonEditFloorLayer)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonRemoveFloorLayer)))
                .addContainerGap())
        );

        jTabbedPane1.addTab("\u5E95\u90E8\u8986\u76D6\u5C42", jPanel2);

        jLabel24.setText("<html>\u4F60\u53EF\u4EE5\u5728\u6B64\u5904\u6DFB\u52A0\u81EA\u5B9A\u4E49\u8986\u76D6\u5C42\uFF0C\u8FD9\u4E9B\u8986\u76D6\u5C42\u5C06\u5728\u6D1E\u7A74/\u96A7\u9053\u9876\u90E8\u6E32\u67D3:<br>\n<strong>\u6CE8\u610F:</strong> \u8FD9\u4E9B\u8986\u76D6\u5C42\u5C06\u88AB\u7FFB\u8F6C\uFF0C\u5305\u62EC\u81EA\u5B9A\u4E49\u5BF9\u8C61.<br>\n\u4E4B\u540E\u7684\u7248\u672C\u4F1A\u5C06\u5176\u53D8\u4E3A\u53EF\u9009.</html>");

        tableRoofLayers.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        tableRoofLayers.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tableRoofLayersMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(tableRoofLayers);

        buttonNewRoofLayer.setText("\u65B0\u5EFA");
        buttonNewRoofLayer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonNewRoofLayerActionPerformed(evt);
            }
        });

        buttonAddRoofLayer.setText("\u590D\u5236\u73B0\u6709");
        buttonAddRoofLayer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonAddRoofLayerActionPerformed(evt);
            }
        });

        buttonEditRoofLayer.setText("\u7F16\u8F91");
        buttonEditRoofLayer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonEditRoofLayerActionPerformed(evt);
            }
        });

        buttonRemoveRoofLayer.setText("\u79FB\u9664");
        buttonRemoveRoofLayer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonRemoveRoofLayerActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel24, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 536, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(buttonAddRoofLayer, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(buttonNewRoofLayer, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(buttonEditRoofLayer, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(buttonRemoveRoofLayer, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel24, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 452, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(buttonNewRoofLayer)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonAddRoofLayer)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonEditRoofLayer)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonRemoveRoofLayer)))
                .addContainerGap())
        );

        jTabbedPane1.addTab("\u9876\u5C42\u8986\u76D6\u5C42", jPanel3);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(buttonReset)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(buttonOK)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonCancel)
                        .addGap(11, 11, 11))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTabbedPane1)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel1)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel4)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(textFieldName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jLabel11)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(paintPicker1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(textFieldName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11)
                    .addComponent(paintPicker1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonCancel)
                    .addComponent(buttonOK)
                    .addComponent(buttonReset))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void buttonOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonOKActionPerformed
        ok();
    }//GEN-LAST:event_buttonOKActionPerformed

    private void buttonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonCancelActionPerformed
        cancel();
    }//GEN-LAST:event_buttonCancelActionPerformed

    private void buttonResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonResetActionPerformed
        loadSettings();
        updatePreview();
    }//GEN-LAST:event_buttonResetActionPerformed

    private void radioButtonRoofInverseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonRoofInverseActionPerformed
        updatePreview();
        setControlStates();
    }//GEN-LAST:event_radioButtonRoofInverseActionPerformed

    private void radioButtonRoofFixedLevelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonRoofFixedLevelActionPerformed
        updatePreview();
        setControlStates();
    }//GEN-LAST:event_radioButtonRoofFixedLevelActionPerformed

    private void checkBoxFloodActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxFloodActionPerformed
        setControlStates();
        updatePreview();
    }//GEN-LAST:event_checkBoxFloodActionPerformed

    private void radioButtonFloorInverseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonFloorInverseActionPerformed
        updatePreview();
        setControlStates();
    }//GEN-LAST:event_radioButtonFloorInverseActionPerformed

    private void spinnerFloorMaxStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerFloorMaxStateChanged
        if (! programmaticChange) {
            if ((Integer) spinnerFloorMax.getValue() < (Integer) spinnerFloorMin.getValue()) {
                spinnerFloorMin.setValue(spinnerFloorMax.getValue());
            }
            updatePreview();
        }
    }//GEN-LAST:event_spinnerFloorMaxStateChanged

    private void checkBoxRemoveWaterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxRemoveWaterActionPerformed
        updatePreview();
    }//GEN-LAST:event_checkBoxRemoveWaterActionPerformed

    private void spinnerWallFloorDepthStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerWallFloorDepthStateChanged
        if (! programmaticChange) {
            updatePreview();
        }
    }//GEN-LAST:event_spinnerWallFloorDepthStateChanged

    private void spinnerWallRoofDepthStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerWallRoofDepthStateChanged
        if (! programmaticChange) {
            updatePreview();
        }
    }//GEN-LAST:event_spinnerWallRoofDepthStateChanged

    private void spinnerRoofMaxStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerRoofMaxStateChanged
        if (! programmaticChange) {
            if ((Integer) spinnerRoofMax.getValue() < (Integer) spinnerRoofMin.getValue()) {
                spinnerRoofMin.setValue(spinnerRoofMax.getValue());
            }
            updatePreview();
        }
    }//GEN-LAST:event_spinnerRoofMaxStateChanged

    private void checkBoxFloodWithLavaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxFloodWithLavaActionPerformed
        updatePreview();
    }//GEN-LAST:event_checkBoxFloodWithLavaActionPerformed

    private void radioButtonFloorFixedDepthActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonFloorFixedDepthActionPerformed
        if (radioButtonFloorFixedDepth.isSelected() && ((Integer) spinnerFloorLevel.getValue() < 0)) {
            spinnerFloorLevel.setValue(0);
        }
        updatePreview();
        setControlStates();
    }//GEN-LAST:event_radioButtonFloorFixedDepthActionPerformed

    private void spinnerFloodLevelStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerFloodLevelStateChanged
        if (! programmaticChange) {
            updatePreview();
        }
    }//GEN-LAST:event_spinnerFloodLevelStateChanged

    private void spinnerRoofMinStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerRoofMinStateChanged
        if (! programmaticChange) {
            if ((Integer) spinnerRoofMax.getValue() < (Integer) spinnerRoofMin.getValue()) {
                spinnerRoofMax.setValue(spinnerRoofMin.getValue());
            }
            updatePreview();
        }
    }//GEN-LAST:event_spinnerRoofMinStateChanged

    private void spinnerFloorMinStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerFloorMinStateChanged
        if (! programmaticChange) {
            if ((Integer) spinnerFloorMax.getValue() < (Integer) spinnerFloorMin.getValue()) {
                spinnerFloorMax.setValue(spinnerFloorMin.getValue());
            }
            updatePreview();
        }
    }//GEN-LAST:event_spinnerFloorMinStateChanged

    private void radioButtonRoofFixedDepthActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonRoofFixedDepthActionPerformed
        if (radioButtonRoofFixedDepth.isSelected() && ((Integer) spinnerRoofLevel.getValue() < 0)) {
            spinnerRoofLevel.setValue(0);
        }
        updatePreview();
        setControlStates();
    }//GEN-LAST:event_radioButtonRoofFixedDepthActionPerformed

    private void spinnerFloorLevelStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerFloorLevelStateChanged
        if (! programmaticChange) {
            updatePreview();
        }
    }//GEN-LAST:event_spinnerFloorLevelStateChanged

    private void spinnerRoofLevelStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerRoofLevelStateChanged
        if (! programmaticChange) {
            updatePreview();
        }
    }//GEN-LAST:event_spinnerRoofLevelStateChanged

    private void radioButtonFloorFixedLevelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonFloorFixedLevelActionPerformed
        updatePreview();
        setControlStates();
    }//GEN-LAST:event_radioButtonFloorFixedLevelActionPerformed

    private void buttonNewFloorLayerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonNewFloorLayerActionPerformed
        newFloorLayer();
    }//GEN-LAST:event_buttonNewFloorLayerActionPerformed

    private void buttonAddFloorLayerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonAddFloorLayerActionPerformed
        addFloorLayer();
    }//GEN-LAST:event_buttonAddFloorLayerActionPerformed

    private void buttonEditFloorLayerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonEditFloorLayerActionPerformed
        editFloorLayer();
    }//GEN-LAST:event_buttonEditFloorLayerActionPerformed

    private void buttonRemoveFloorLayerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonRemoveFloorLayerActionPerformed
        removeFloorLayers();
    }//GEN-LAST:event_buttonRemoveFloorLayerActionPerformed

    private void tableFloorLayersMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableFloorLayersMouseClicked
        if ((! evt.isPopupTrigger()) && (evt.getClickCount() == 2)) {
            int column = tableFloorLayers.columnAtPoint(evt.getPoint());
            if (column == COLUMN_NAME) {
                editFloorLayer();
            } else if (column == COLUMN_VARIATION) {
                editFloorLayerVariation();
            }
        }
    }//GEN-LAST:event_tableFloorLayersMouseClicked

    private void tableRoofLayersMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableRoofLayersMouseClicked
        if ((! evt.isPopupTrigger()) && (evt.getClickCount() == 2)) {
            int column = tableRoofLayers.columnAtPoint(evt.getPoint());
            if (column == COLUMN_NAME) {
                editRoofLayer();
            } else if (column == COLUMN_VARIATION) {
                editRoofLayerVariation();
            }
        }
    }//GEN-LAST:event_tableRoofLayersMouseClicked

    private void buttonNewRoofLayerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonNewRoofLayerActionPerformed
        newRoofLayer();
    }//GEN-LAST:event_buttonNewRoofLayerActionPerformed

    private void buttonAddRoofLayerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonAddRoofLayerActionPerformed
        addRoofLayer();
    }//GEN-LAST:event_buttonAddRoofLayerActionPerformed

    private void buttonEditRoofLayerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonEditRoofLayerActionPerformed
        editRoofLayer();
    }//GEN-LAST:event_buttonEditRoofLayerActionPerformed

    private void buttonRemoveRoofLayerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonRemoveRoofLayerActionPerformed
        removeRoofLayers();
    }//GEN-LAST:event_buttonRemoveRoofLayerActionPerformed

    private void radioButtonFloorCustomDimensionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonFloorCustomDimensionActionPerformed
        updatePreview();
        setControlStates();
    }//GEN-LAST:event_radioButtonFloorCustomDimensionActionPerformed

    private void radioButtonRoofFixedHeightActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonRoofFixedHeightActionPerformed
        updatePreview();
        setControlStates();
    }//GEN-LAST:event_radioButtonRoofFixedHeightActionPerformed

    private void mixedMaterialChooserRoofPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_mixedMaterialChooserRoofPropertyChange
        if (evt.getPropertyName().equals("material")) {
            // Refresh the other choosers, in case a new material has been added
            mixedMaterialChooserFloor.refresh();
            mixedMaterialChooserWall.refresh();
            mixedMaterialChooserFill.refresh();
        }
    }//GEN-LAST:event_mixedMaterialChooserRoofPropertyChange

    private void mixedMaterialChooserFloorPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_mixedMaterialChooserFloorPropertyChange
        if (evt.getPropertyName().equals("material")) {
            // Refresh the other choosers, in case a new material has been added
            mixedMaterialChooserRoof.refresh();
            mixedMaterialChooserWall.refresh();
            mixedMaterialChooserFill.refresh();
        }
    }//GEN-LAST:event_mixedMaterialChooserFloorPropertyChange

    private void mixedMaterialChooserWallPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_mixedMaterialChooserWallPropertyChange
        if (evt.getPropertyName().equals("material")) {
            // Refresh the other choosers, in case a new material has been added
            mixedMaterialChooserRoof.refresh();
            mixedMaterialChooserFloor.refresh();
            mixedMaterialChooserFill.refresh();
        }
    }//GEN-LAST:event_mixedMaterialChooserWallPropertyChange

    private void radioButtonFillWithAirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonFillWithAirActionPerformed
        setControlStates();
    }//GEN-LAST:event_radioButtonFillWithAirActionPerformed

    private void radioButtonFillWithLightActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonFillWithLightActionPerformed
        setControlStates();
    }//GEN-LAST:event_radioButtonFillWithLightActionPerformed

    private void radioButtonFillWithCustomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonFillWithCustomActionPerformed
        setControlStates();
    }//GEN-LAST:event_radioButtonFillWithCustomActionPerformed

    private void mixedMaterialChooserFillPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_mixedMaterialChooserFillPropertyChange
        if (evt.getPropertyName().equals("material")) {
            // Refresh the other choosers, in case a new material has been added
            mixedMaterialChooserFloor.refresh();
            mixedMaterialChooserWall.refresh();
            mixedMaterialChooserRoof.refresh();
        }
    }//GEN-LAST:event_mixedMaterialChooserFillPropertyChange

    private void radioButtonFillWithCaveAirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonFillWithCaveAirActionPerformed
        setControlStates();
    }//GEN-LAST:event_radioButtonFillWithCaveAirActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonAddFloorLayer;
    private javax.swing.JButton buttonAddRoofLayer;
    private javax.swing.JButton buttonCancel;
    private javax.swing.JButton buttonEditFloorLayer;
    private javax.swing.JButton buttonEditRoofLayer;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup3;
    private javax.swing.JButton buttonNewFloorLayer;
    private javax.swing.JButton buttonNewRoofLayer;
    private javax.swing.JButton buttonOK;
    private javax.swing.JButton buttonRemoveFloorLayer;
    private javax.swing.JButton buttonRemoveRoofLayer;
    private javax.swing.JButton buttonReset;
    private javax.swing.JCheckBox checkBoxFlood;
    private javax.swing.JCheckBox checkBoxFloodWithLava;
    private javax.swing.JCheckBox checkBoxRemoveWater;
    private javax.swing.JComboBox<Integer> comboBoxBiome;
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
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel labelPreview;
    private org.pepsoft.worldpainter.MixedMaterialChooser mixedMaterialChooserFill;
    private org.pepsoft.worldpainter.MixedMaterialChooser mixedMaterialChooserFloor;
    private org.pepsoft.worldpainter.MixedMaterialChooser mixedMaterialChooserRoof;
    private org.pepsoft.worldpainter.MixedMaterialChooser mixedMaterialChooserWall;
    private org.pepsoft.worldpainter.NoiseSettingsEditor noiseSettingsEditorFloor;
    private org.pepsoft.worldpainter.NoiseSettingsEditor noiseSettingsEditorRoof;
    private org.pepsoft.worldpainter.layers.renderers.PaintPicker paintPicker1;
    private javax.swing.JRadioButton radioButtonFillWithAir;
    private javax.swing.JRadioButton radioButtonFillWithCaveAir;
    private javax.swing.JRadioButton radioButtonFillWithCustom;
    private javax.swing.JRadioButton radioButtonFillWithLight;
    private javax.swing.JRadioButton radioButtonFloorCustomDimension;
    private javax.swing.JRadioButton radioButtonFloorFixedDepth;
    private javax.swing.JRadioButton radioButtonFloorFixedLevel;
    private javax.swing.JRadioButton radioButtonFloorInverse;
    private javax.swing.JRadioButton radioButtonRoofFixedDepth;
    private javax.swing.JRadioButton radioButtonRoofFixedHeight;
    private javax.swing.JRadioButton radioButtonRoofFixedLevel;
    private javax.swing.JRadioButton radioButtonRoofInverse;
    private javax.swing.JSpinner spinnerFillLightLevel;
    private javax.swing.JSpinner spinnerFloodLevel;
    private javax.swing.JSpinner spinnerFloorLevel;
    private javax.swing.JSpinner spinnerFloorMax;
    private javax.swing.JSpinner spinnerFloorMin;
    private javax.swing.JSpinner spinnerRoofLevel;
    private javax.swing.JSpinner spinnerRoofMax;
    private javax.swing.JSpinner spinnerRoofMin;
    private javax.swing.JSpinner spinnerWallFloorDepth;
    private javax.swing.JSpinner spinnerWallRoofDepth;
    private javax.swing.JTable tableFloorLayers;
    private javax.swing.JTable tableRoofLayers;
    private javax.swing.JTextField textFieldName;
    // End of variables declaration//GEN-END:variables

    protected final Platform platform;
    protected final TunnelLayer layer;
    protected final Dimension dimension;
    protected final int waterLevel, baseHeight, minHeight, maxHeight;
    protected final ColourScheme colourScheme;
    protected final CustomBiomeManager customBiomeManager;
    private TunnelLayersTableModel floorLayersTableModel;
    protected TunnelLayersTableModel roofLayersTableModel;
    protected boolean programmaticChange;

    private static final String PAINT_TUNNEL_LAYER_KEY = "org.pepsoft.worldpainter.TunnelLayer.paintLayer";
    private static final long serialVersionUID = 1L;
}