/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pepsoft.worldpainter.layers.bo2;

import org.pepsoft.minecraft.Material;
import org.pepsoft.util.DesktopUtils;
import org.pepsoft.worldpainter.App;
import org.pepsoft.worldpainter.ColourScheme;
import org.pepsoft.worldpainter.Configuration;
import org.pepsoft.worldpainter.Platform;
import org.pepsoft.worldpainter.layers.AbstractLayerEditor;
import org.pepsoft.worldpainter.layers.Bo2Layer;
import org.pepsoft.worldpainter.layers.exporters.ExporterSettings;
import org.pepsoft.worldpainter.objects.WPObject;
import org.pepsoft.worldpainter.plugins.CustomObjectManager;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.vecmath.Point3i;
import java.awt.*;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.List;
import java.util.*;

import static java.lang.Math.round;
import static java.lang.String.format;
import static org.pepsoft.minecraft.Material.PERSISTENT;
import static org.pepsoft.util.swing.MessageUtils.*;
import static org.pepsoft.worldpainter.ExceptionHandler.doWithoutExceptionReporting;
import static org.pepsoft.worldpainter.Platform.Capability.NAME_BASED;
import static org.pepsoft.worldpainter.objects.WPObject.*;

/**
 *
 * @author Pepijn Schmitz
 */
@SuppressWarnings({"unused", "FieldCanBeLocal"}) // Managed by NetBeans
public class Bo2LayerEditor extends AbstractLayerEditor<Bo2Layer> implements ListSelectionListener, DocumentListener {
    /**
     * Creates new form Bo2LayerEditor
     */
    public Bo2LayerEditor() {
        initComponents();
        
        listModel = new DefaultListModel<>();
        listObjects.setModel(listModel);
        listObjects.setCellRenderer(new WPObjectListCellRenderer());
        
        listObjects.getSelectionModel().addListSelectionListener(this);
        fieldName.getDocument().addDocumentListener(this);

        updateBlocksPerAttempt();
    }

    // LayerEditor
    
    @Override
    public Bo2Layer createLayer() {
        return new Bo2Layer(new Bo2ObjectTube("\u6211\u7684\u81EA\u5B9A\u4E49\u5BF9\u8C61", Collections.emptyList()), "\u81EA\u5B9A\u4E49\u5BF9\u8C61 (\u4F8B\u5982 bo2, bo3, nbt, schem \u548C/\u6216 schematic \u5BF9\u8C61)", Color.ORANGE);
    }

    @Override
    public void setLayer(Bo2Layer layer) {
        super.setLayer(layer);
        reset();
    }

    @Override
    public void commit() {
        if (! isCommitAvailable()) {
            throw new IllegalStateException("Settings invalid or incomplete");
        }
        saveSettings(layer);
    }

    @Override
    public void reset() {
        List<WPObject> objects = new ArrayList<>();
        fieldName.setText(layer.getName());
        paintPicker1.setPaint(layer.getPaint());
        paintPicker1.setOpacity(layer.getOpacity());
        List<File> files = layer.getFiles();
        if (files != null) {
            if (files.isEmpty()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Existing layer contains new style objects");
                }
                // New layer; files stored in object attributes
                objects.addAll(layer.getObjectProvider().getAllObjects());
            } else {
                // Old layer; files stored separately
                int missingFiles = 0;
                CustomObjectManager customObjectManager = CustomObjectManager.getInstance();
                if ((files.size() == 1) && files.get(0).isDirectory()) {
                    logger.info("Existing custom object layer contains old style directory; migrating to new style");
                    File[] filesInDir = files.get(0).listFiles((FilenameFilter) CustomObjectManager.getInstance().getFileFilter());
                    //noinspection ConstantConditions // Cannot happen as we already checked that files.get(0) is an extant directory
                    for (File file: filesInDir) {
                        try {
                            objects.add(customObjectManager.loadObject(file));
                        } catch (IOException e) {
                            logger.error("I/O error while trying to load custom object " + file, e);
                            missingFiles++;
                        }
                    }
                } else {
                    logger.info("Existing custom object layer contains old style file list; migrating to new style");
                    for (File file: files) {
                        if (file.exists()) {
                            try {
                                objects.add(customObjectManager.loadObject(file));
                            } catch (IOException e) {
                                logger.error("I/O error while trying to load custom object " + file, e);
                                missingFiles++;
                            }
                        } else {
                            missingFiles++;
                        }
                    }
                }
                if (missingFiles > 0) {
                    showWarning(this, "\u8BE5\u8986\u76d6\u5c42\u4E3A\u65E7\u7684\u81EA\u5B9A\u4E49\u5BF9\u8C61\u8986\u76d6\u5c42\u5E76\u4E14\u6709 " + missingFiles + " \u4E2A\u5BF9\u8C61\u56E0\u4E22\u5931\u6216\u8BFB\u5199\u9519\u8BEF\u65E0\u6CD5\u88AB\u6062\u590D.\n\n\u4F60\u9700\u8981\u5728\u4FDD\u5B58\u8BBE\u7F6E\u524D\u91CD\u65B0\u6DFB\u52A0\u8FD9\u4E9B\u5BF9\u8C61\uFF0C\u5426\u5219\u73B0\u6709\u7684\u5BF9\u8C61\u6570\u636E\u4E5F\u4F1A\u4E22\u5931\n\u4F60\u4E5F\u53EF\u4EE5\u5728\u4E0D\u5F71\u54CD\u5BF9\u8C61\u6570\u636E\u7684\u60C5\u51B5\u4E0B\u5173\u95ED\u8BE5\u7A97\u53E3.", "\u7F3A\u5931\u6587\u4EF6");
                }
            }
        } else {
            logger.info("Existing custom object layer contains very old style objects with no file information; migrating to new style");
            // Very old layer; no file information at all
            objects.addAll(layer.getObjectProvider().getAllObjects());
        }
        listModel.clear();
        for (WPObject object: objects) {
            listModel.addElement(object.clone());
        }
        spinnerBlocksPerAttempt.setValue(layer.getDensity());
        spinnerGrid.setValue(layer.getGridX());
        spinnerRandomOffset.setValue(layer.getRandomDisplacement());

        refreshLeafDecaySettings();

        settingsChanged();
    }

    @Override
    public ExporterSettings getSettings() {
        if (! isCommitAvailable()) {
            throw new IllegalStateException("Settings invalid or incomplete");
        }
        final Bo2Layer previewLayer = saveSettings(null);
        return new ExporterSettings() {
            @Override
            public boolean isApplyEverywhere() {
                return false;
            }

            @Override
            public Bo2Layer getLayer() {
                return previewLayer;
            }

            @Override
            public ExporterSettings clone() {
                throw new UnsupportedOperationException("Not supported");
            }
        };
    }

    @Override
    public boolean isCommitAvailable() {
        boolean filesSelected = listModel.getSize() > 0;
        boolean nameSpecified = fieldName.getText().trim().length() > 0;
        return filesSelected && nameSpecified;
    }

    @Override
    public void setContext(LayerEditorContext context) {
        super.setContext(context);
        colourScheme = context.getColourScheme();
    }

    // ListSelectionListener

    @Override
    public void valueChanged(ListSelectionEvent e) {
        settingsChanged();
    }

    // DocumentListener

    @Override
    public void insertUpdate(DocumentEvent e) {
        settingsChanged();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        settingsChanged();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        settingsChanged();
    }

    private Bo2Layer saveSettings(Bo2Layer layer) {
        String name = fieldName.getText();
        List<WPObject> objects = new ArrayList<>(listModel.getSize());
        for (int i = 0; i < listModel.getSize(); i++) {
            objects.add(listModel.getElementAt(i));
        }
        Bo2ObjectProvider objectProvider = new Bo2ObjectTube(name, objects);
        if (layer == null) {
            layer = new Bo2Layer(objectProvider, "\u81EA\u5B9A\u4E49\u5BF9\u8C61 (\u4F8B\u5982 bo2, bo3 \u548C/\u6216 schematic \u5BF9\u8C61)", paintPicker1.getPaint());
        } else {
            layer.setObjectProvider(objectProvider);
            layer.setPaint(paintPicker1.getPaint());
        }
        layer.setOpacity(paintPicker1.getOpacity());
        layer.setDensity((Integer) spinnerBlocksPerAttempt.getValue());
        layer.setGridX((Integer) spinnerGrid.getValue());
        layer.setGridY((Integer) spinnerGrid.getValue());
        layer.setRandomDisplacement((Integer) spinnerRandomOffset.getValue());
        return layer;
    }

    private void settingsChanged() {
        setControlStates();
        context.settingsChanged();
    }

    private void setControlStates() {
        boolean filesSelected = listModel.getSize() > 0;
        boolean objectsSelected = listObjects.getSelectedIndex() != -1;
        buttonRemoveFile.setEnabled(objectsSelected);
        buttonReloadAll.setEnabled(filesSelected);
        buttonEdit.setEnabled(objectsSelected);
    }

    private void addFilesOrDirectory() {
        // Can't use FileUtils.selectFilesForOpen() because it doesn't support
        // selecting directories, or adding custom components to the dialog
        JFileChooser fileChooser = new JFileChooser();
        Configuration config = Configuration.getInstance();
        if ((config.getCustomObjectsDirectory() != null) && config.getCustomObjectsDirectory().isDirectory()) {
            fileChooser.setCurrentDirectory(config.getCustomObjectsDirectory());
        }
        fileChooser.setDialogTitle("\u9009\u62E9\u6587\u4EF6\u6216\u76EE\u5F55");
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        CustomObjectManager.UniversalFileFilter fileFilter = CustomObjectManager.getInstance().getFileFilter();
        fileChooser.setFileFilter(fileFilter);
        WPObjectPreviewer previewer = new WPObjectPreviewer();
        previewer.setDimension(App.getInstance().getDimension());
        fileChooser.addPropertyChangeListener(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY, previewer);
        fileChooser.setAccessory(previewer);
        if (doWithoutExceptionReporting(() -> fileChooser.showOpenDialog(this)) == JFileChooser.APPROVE_OPTION) {
            File[] selectedFiles = fileChooser.getSelectedFiles();
            if (selectedFiles.length > 0) {
                Platform platform = context.getDimension().getWorld().getPlatform();
                boolean checkForNameOnlyMaterials = ! platform.capabilities.contains(NAME_BASED);
                Set<String> nameOnlyMaterialsNames = checkForNameOnlyMaterials ? new HashSet<>() : null;
                config.setCustomObjectsDirectory(selectedFiles[0].getParentFile());
                for (File selectedFile: selectedFiles) {
                    if (selectedFile.isDirectory()) {
                        if (fieldName.getText().isEmpty()) {
                            String name = selectedFiles[0].getName();
                            if (name.length() > 12) {
                                name = "..." + name.substring(name.length() - 10);
                            }
                            fieldName.setText(name);
                        }
                        File[] files = selectedFile.listFiles((FilenameFilter) fileFilter);
                        if (files == null) {
                            beepAndShowError(this, selectedFile.getName() + " \u4E0D\u662F\u76EE\u5F55\u6216\u65E0\u6CD5\u8BFB\u53D6.", "\u76EE\u5F55\u65E0\u6548");
                        } else if (files.length == 0) {
                            beepAndShowError(this, "\u76EE\u5F55 " + selectedFile.getName() + " \u4E0D\u5305\u542B\u4EFB\u4F55\u652F\u6301\u7684\u5BF9\u8C61\u7C7B\u578B.", "\u672A\u627E\u5230\u81EA\u5B9A\u4E49\u5BF9\u8C61\u6587\u4EF6");
                        } else {
                            for (File file: files) {
                                addFile(checkForNameOnlyMaterials, nameOnlyMaterialsNames, file);
                            }
                        }
                    } else {
                        if (fieldName.getText().isEmpty()) {
                            String name = selectedFile.getName();
                            int p = name.lastIndexOf('.');
                            if (p != -1) {
                                name = name.substring(0, p);
                            }
                            if (name.length() > 12) {
                                name = "..." + name.substring(name.length() - 10);
                            }
                            fieldName.setText(name);
                        }
                        addFile(checkForNameOnlyMaterials, nameOnlyMaterialsNames, selectedFile);
                    }
                }
                settingsChanged();
                refreshLeafDecaySettings();
                if (checkForNameOnlyMaterials && (! nameOnlyMaterialsNames.isEmpty())) {
                    String message;
                    if (nameOnlyMaterialsNames.size() > 4) {
                        message = format("\u4E00\u4E2A\u6216\u591A\u4E2A\u6DFB\u52A0\u7684\u5BF9\u8C61\u5305\u542B\u4E0E\u5F53\u524D\u5730\u56FE\u683C\u5F0F(%s)\u4E0D\u517C\u5BB9\u7684\u65B9\u5757: \n" +
                                "%s(\u8FD8\u6709%d\u4E2A)\n" +
                                "\u5982\u679C\u4F60\u4ECD\u7136\u4F7F\u7528\u8BE5\u8986\u76d6\u5c42\uFF0C\u4F60\u5C06\u65E0\u6CD5\u4EE5\u8BE5\u683C\u5F0F\u5BFC\u51FA\u672C\u4E16\u754C.",
                                platform.displayName, String.join(", ", new ArrayList<>(nameOnlyMaterialsNames).subList(0, 3)),
                                nameOnlyMaterialsNames.size() - 3);
                    } else {
                        message = format("\u4E00\u4E2A\u6216\u591A\u4E2A\u6DFB\u52A0\u7684\u5BF9\u8C61\u5305\u542B\u4E0E\u5F53\u524D\u5730\u56FE\u683C\u5F0F(%s)\u4E0D\u517C\u5BB9\u7684\u65B9\u5757: \n" +
                                        "%s\n" +
                                        "\u5982\u679C\u4F60\u4ECD\u7136\u4F7F\u7528\u8BE5\u8986\u76d6\u5c42\uFF0C\u4F60\u5C06\u65E0\u6CD5\u4EE5\u8BE5\u683C\u5F0F\u5BFC\u51FA\u672C\u4E16\u754C.",
                                platform.displayName, String.join(", ", nameOnlyMaterialsNames));
                    }
                    beepAndShowWarning(this, message, "\u5730\u56FE\u683C\u5F0F\u4E0D\u652F\u6301");
                }
            }
        }
    }

    private void addFile(boolean checkForNameOnlyMaterials, Set<String> nameOnlyMaterialsNames, File file) {
        try {
            WPObject object = CustomObjectManager.getInstance().loadObject(file);
            if (checkForNameOnlyMaterials) {
                Set<String> materialNamesEncountered = new HashSet<>();
                object.visitBlocks((o, x, y, z, material) -> {
                    if (! materialNamesEncountered.contains(material.name)) {
                        materialNamesEncountered.add(material.name);
                        if (material.blockType == -1) {
                            nameOnlyMaterialsNames.add(material.name);
                        }
                    }
                    return true;
                });
            }
            listModel.addElement(object);
        } catch (IllegalArgumentException e) {
            logger.error("IllegalArgumentException while trying to load custom object " + file, e);
            JOptionPane.showMessageDialog(this, e.getMessage() + " while loading " + file.getName() + "; it was not added", "Illegal Argument", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            logger.error("I/O error while trying to load custom object " + file, e);
            JOptionPane.showMessageDialog(this, "I/O error while loading " + file.getName() + "; it was not added", "I/O Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void removeFiles() {
        int[] selectedIndices = listObjects.getSelectedIndices();
        for (int i = selectedIndices.length - 1; i >= 0; i--) {
            listModel.removeElementAt(selectedIndices[i]);
        }
        settingsChanged();
        refreshLeafDecaySettings();
    }

    private void reloadObjects() {
        StringBuilder noFiles = new StringBuilder();
        StringBuilder notFound = new StringBuilder();
        StringBuilder errors = new StringBuilder();
        int[] indices;
        if (listObjects.getSelectedIndex() != -1) {
            indices = listObjects.getSelectedIndices();
        } else {
            indices = new int[listModel.getSize()];
            for (int i = 0; i < indices.length; i++) {
                indices[i] = i;
            }
        }
        CustomObjectManager customObjectManager = CustomObjectManager.getInstance();
        for (int index: indices) {
            WPObject object = listModel.getElementAt(index);
            File file = object.getAttribute(ATTRIBUTE_FILE);
            if (file != null) {
                if (file.isFile() && file.canRead()) {
                    try {
                        Map<String, Serializable> existingAttributes = object.getAttributes();
                        object = customObjectManager.loadObject(file);
                        if (existingAttributes != null) {
                            Map<String, Serializable> attributes = object.getAttributes();
                            if (attributes == null) {
                                attributes = new HashMap<>();
                            }
                            attributes.putAll(existingAttributes);
                            object.setAttributes(attributes);
                        }
                        listModel.setElementAt(object, index);
                    } catch (IOException e) {
                        logger.error("I/O error while reloading " + file, e);
                        errors.append(file.getPath()).append('\n');
                    }
                } else {
                    notFound.append(file.getPath()).append('\n');
                }
            } else {
                noFiles.append(object.getName()).append('\n');
            }
        }
        if ((noFiles.length() > 0) || (notFound.length() > 0) || (errors.length() > 0)) {
            StringBuilder message = new StringBuilder();
            message.append("\u6709\u6587\u4EF6\u65E0\u6CD5\u91CD\u8F7D!\n");
            if (noFiles.length() > 0) {
                message.append("\n\u4E0B\u65B9\u7684\u5BF9\u8C61\u6765\u81EA\u4E8E\u4E00\u4E2A\u8001\u7684\u8986\u76d6\u5c42\uFF0C\u4E14\u6CA1\u6709\u50A8\u5B58\u6587\u4EF6\u540D\u6570\u636E:\n");
                message.append(noFiles);
            }
            if (notFound.length() > 0) {
                message.append("\n\u4E0B\u65B9\u6587\u4EF6\u7F3A\u5931\u6216\u65E0\u6CD5\u8BBF\u95EE:\n");
                message.append(notFound);
            }
            if (errors.length() > 0) {
                message.append("\n\u4E0B\u65B9\u6587\u4EF6\u52A0\u8F7D\u65F6\u51FA\u73B0 I/O \u9519\u8BEF:\n");
                message.append(errors);
            }
            JOptionPane.showMessageDialog(this, message, "\u6709\u6587\u4EF6\u65E0\u6CD5\u91CD\u8F7D", JOptionPane.ERROR_MESSAGE);
        } else {
            showInfo(this, indices.length + " \u4E2A\u5BF9\u8C61\u6210\u529F\u91CD\u8F7D", "\u6210\u529F");
        }
        refreshLeafDecaySettings();
    }

    private void editObjects() {
        List<WPObject> selectedObjects = new ArrayList<>(listObjects.getSelectedIndices().length);
        int[] selectedIndices = listObjects.getSelectedIndices();
        for (int i = selectedIndices.length - 1; i >= 0; i--) {
            selectedObjects.add(listModel.getElementAt(selectedIndices[i]));
        }
        EditObjectAttributes dialog = new EditObjectAttributes(SwingUtilities.getWindowAncestor(this), selectedObjects, colourScheme);
        dialog.setVisible(true);
        if (! dialog.isCancelled()) {
            settingsChanged();
            refreshLeafDecaySettings();
        }
    }

    private void refreshLeafDecaySettings() {
        if (listModel.isEmpty()) {
            labelLeafDecayTitle.setEnabled(false);
            labelEffectiveLeafDecaySetting.setEnabled(false);
            labelEffectiveLeafDecaySetting.setText("\u4E0D\u9002\u7528");
            buttonSetDecay.setEnabled(false);
            buttonSetNoDecay.setEnabled(false);
            buttonReset.setEnabled(false);
            return;
        }
        boolean decayingLeavesFound = false;
        boolean nonDecayingLeavesFound = false;
        outer:
        for (Enumeration<WPObject> e = listModel.elements(); e.hasMoreElements(); ) {
            WPObject object = e.nextElement();
            int leafDecayMode = object.getAttribute(ATTRIBUTE_LEAF_DECAY_MODE);
            switch (leafDecayMode) {
                case LEAF_DECAY_NO_CHANGE:
                    // Leaf decay attribute not set (or set to "no change"); examine actual blocks
                    object.prepareForExport(context.getDimension());
                    Point3i dim = object.getDimensions();
                    for (int x = 0; x < dim.x; x++) {
                        for (int y = 0; y < dim.y; y++) {
                            for (int z = 0; z < dim.z; z++) {
                                if (object.getMask(x, y, z)) {
                                    final Material material = object.getMaterial(x, y, z);
                                    if (material.leafBlock) {
                                        if (material.is(PERSISTENT)) {
                                            // Non decaying leaf block
                                            nonDecayingLeavesFound = true;
                                            if (decayingLeavesFound) {
                                                // We have enough information; no reason to continue the examination
                                                break outer;
                                            }
                                        } else {
                                            // Decaying leaf block
                                            decayingLeavesFound = true;
                                            if (nonDecayingLeavesFound) {
                                                // We have enough information; no reason to continue the examination
                                                break outer;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    break;
                case LEAF_DECAY_OFF:
                    // Leaf decay attribute set to "off"; don't examine blocks for performance (even though this could
                    // lead to misleading information if the object doesn't contain any leaf blocks)
                    nonDecayingLeavesFound = true;
                    if (decayingLeavesFound) {
                        // We have enough information; no reason to continue the examination
                        break outer;
                    }
                    break;
                case LEAF_DECAY_ON:
                    // Leaf decay attribute set to "off"; don't examine blocks for performance (even though this could
                    // lead to misleading information if the object doesn't contain any leaf blocks)
                    decayingLeavesFound = true;
                    if (nonDecayingLeavesFound) {
                        // We have enough information; no reason to continue the examination
                        break outer;
                    }
                    break;
                default:
                    throw new InternalError();
            }
        }

        if (decayingLeavesFound) {
            labelLeafDecayTitle.setEnabled(true);
            labelEffectiveLeafDecaySetting.setEnabled(true);
            buttonSetNoDecay.setEnabled(true);
            buttonReset.setEnabled(true);
            if (nonDecayingLeavesFound) {
                // Both decaying and non decaying leaves found
                labelEffectiveLeafDecaySetting.setText("<html>\u51CB\u843D\u7684<i>\u4E0E</i>\u4E0D\u51CB\u843D\u7684\u6811\u53F6\u90FD\u5B58\u5728.</html>");
                buttonSetDecay.setEnabled(true);
            } else {
                // Only decaying leaves found
                labelEffectiveLeafDecaySetting.setText("<html><b>\u6240\u6709</b>\u6811\u53F6\u90FD\u4F1A\u51CB\u843D.</html>");
                buttonSetDecay.setEnabled(false);
            }
        } else {
            if (nonDecayingLeavesFound) {
                // Only non decaying leaves found
                labelLeafDecayTitle.setEnabled(true);
                labelEffectiveLeafDecaySetting.setEnabled(true);
                labelEffectiveLeafDecaySetting.setText("<html><b>\u6240\u6709</b>\u6811\u53F6\u90FD\u4E0D\u4F1A\u51CB\u843D.</html>");
                buttonSetDecay.setEnabled(true);
                buttonSetNoDecay.setEnabled(false);
                buttonReset.setEnabled(true);
            } else {
                // No leaf blocks encountered at all, so N/A
                labelLeafDecayTitle.setEnabled(false);
                labelEffectiveLeafDecaySetting.setEnabled(false);
                labelEffectiveLeafDecaySetting.setText("\u4E0D\u9002\u7528");
                buttonSetDecay.setEnabled(false);
                buttonSetNoDecay.setEnabled(false);
                buttonReset.setEnabled(false);
            }
        }
    }

    private void setLeavesDecay() {
        for (Enumeration<WPObject> e = listModel.elements(); e.hasMoreElements(); ) {
            WPObject object = e.nextElement();
            object.setAttribute(ATTRIBUTE_LEAF_DECAY_MODE, LEAF_DECAY_ON);
        }
        refreshLeafDecaySettings();
    }

    private void setLeavesNoDecay() {
        for (Enumeration<WPObject> e = listModel.elements(); e.hasMoreElements(); ) {
            WPObject object = e.nextElement();
            object.setAttribute(ATTRIBUTE_LEAF_DECAY_MODE, LEAF_DECAY_OFF);
        }
        refreshLeafDecaySettings();
    }

    private void resetLeafDecay() {
        for (Enumeration<WPObject> e = listModel.elements(); e.hasMoreElements(); ) {
            WPObject object = e.nextElement();
            object.getAttributes().remove(ATTRIBUTE_LEAF_DECAY_MODE.key);
        }
        refreshLeafDecaySettings();
    }

    private void updateBlocksPerAttempt() {
        final int grid = (Integer) spinnerGrid.getValue();
        final float blocksAt50 = (float) ((Integer) spinnerBlocksPerAttempt.getValue()) * grid * grid;
        final float blocksAt1 = blocksAt50 * 64, blocksAt100 = round(blocksAt50 / 3.515625f);
        labelBlocksPerAttempt.setText(format("\u6BCF %d \u4E2A\u65B9\u5757 1 \u4E2A (1%%\u7684\u6982\u7387)\uFF1B\u6BCF %d \u4E2A\u65B9\u5757 1 \u4E2A (50%%\u7684\u6982\u7387)\uFF1B\u6BCF %d \u4E2A\u65B9\u5757 1 \u4E2A (100%%\u7684\u6982\u7387)",
                round(blocksAt1),
                round(blocksAt50),
                round((blocksAt100 <= 1) ? 1 : blocksAt100)));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonReloadAll = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JSeparator();
        buttonEdit = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        labelLeafDecayTitle = new javax.swing.JLabel();
        labelEffectiveLeafDecaySetting = new javax.swing.JLabel();
        buttonSetDecay = new javax.swing.JButton();
        buttonSetNoDecay = new javax.swing.JButton();
        buttonReset = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        listObjects = new javax.swing.JList<>();
        jLabel6 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        fieldName = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        paintPicker1 = new org.pepsoft.worldpainter.layers.renderers.PaintPicker();
        jLabel2 = new javax.swing.JLabel();
        buttonAddFile = new javax.swing.JButton();
        buttonRemoveFile = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        spinnerBlocksPerAttempt = new javax.swing.JSpinner();
        labelBlocksPerAttempt = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        spinnerGrid = new javax.swing.JSpinner();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        spinnerRandomOffset = new javax.swing.JSpinner();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();

        buttonReloadAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/pepsoft/worldpainter/icons/arrow_rotate_clockwise.png"))); // NOI18N
        buttonReloadAll.setToolTipText("\u4ECE\u78C1\u76D8\u91CD\u8F7D\u6240\u6709\u6216\u9009\u4E2D\u5BF9\u8C61");
        buttonReloadAll.setEnabled(false);
        buttonReloadAll.setMargin(new java.awt.Insets(2, 2, 2, 2));
        buttonReloadAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonReloadAllActionPerformed(evt);
            }
        });

        jSeparator2.setOrientation(javax.swing.SwingConstants.VERTICAL);

        buttonEdit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/pepsoft/worldpainter/icons/brick_edit.png"))); // NOI18N
        buttonEdit.setToolTipText("\u7F16\u8F91\u9009\u62E9\u7684\u5BF9\u8C61\u9009\u9879");
        buttonEdit.setEnabled(false);
        buttonEdit.setMargin(new java.awt.Insets(2, 2, 2, 2));
        buttonEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonEditActionPerformed(evt);
            }
        });

        labelLeafDecayTitle.setText("\u8FD9\u4E9B\u5BF9\u8C61\u7684\u6811\u53F6\u51CB\u96F6\u8BBE\u7F6E:");

        labelEffectiveLeafDecaySetting.setText("<html>\u6811\u53F6<b>\u4E0D\u4F1A</b>\u51CB\u96F6.</html>");
        labelEffectiveLeafDecaySetting.setEnabled(false);

        buttonSetDecay.setText("\u8BBE\u7F6E\u6240\u6709\u6811\u53F6\u90FD\u4F1A\u51CB\u96F6");
        buttonSetDecay.setToolTipText("\u8BBE\u7F6E\u6240\u6709\u5BF9\u8C61\u7684\u6811\u53F6\u90FD\u4F1A\u51CB\u96F6");
        buttonSetDecay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSetDecayActionPerformed(evt);
            }
        });

        buttonSetNoDecay.setText("<html>\u8BBE\u7F6E\u6240\u6709\u6811\u53F6\u90FD<b>\u4E0D\u4F1A</b>\u51CB\u96F6</html>");
        buttonSetNoDecay.setToolTipText("\u8BBE\u7F6E\u6240\u6709\u5BF9\u8C61\u7684\u6811\u53F6\u90FD\u4E0D\u4F1A\u51CB\u96F6");
        buttonSetNoDecay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSetNoDecayActionPerformed(evt);
            }
        });

        buttonReset.setText("\u91CD\u7F6E");
        buttonReset.setToolTipText("\u5C06\u6811\u53F6\u51CB\u96F6\u72B6\u6001\u91CD\u7F6E\u4E3A\u5BF9\u8C61\u9ED8\u8BA4\u503C");
        buttonReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonResetActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labelEffectiveLeafDecaySetting, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labelLeafDecayTitle)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(buttonSetDecay)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonSetNoDecay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonReset)))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(labelLeafDecayTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelEffectiveLeafDecaySetting, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonSetDecay)
                    .addComponent(buttonSetNoDecay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonReset)))
        );

        listObjects.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                listObjectsMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(listObjects);

        jLabel6.setForeground(new java.awt.Color(0, 0, 255));
        jLabel6.setText("<html><u>\u83B7\u53D6\u81EA\u5B9A\u4E49\u5BF9\u8C61</u></html>");
        jLabel6.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel6.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel6MouseClicked(evt);
            }
        });

        jLabel1.setText("\u5728\u8BE5\u754C\u9762\u5B9A\u4E49\u4F60\u7684\u81EA\u5B9A\u4E49\u5BF9\u8C61\u8986\u76D6\u5C42.");

        jLabel3.setText("\u540D\u79F0:");

        fieldName.setColumns(15);

        jLabel4.setText("\u753B\u7B14:");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(paintPicker1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fieldName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(fieldName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(paintPicker1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jLabel2.setText("\u5BF9\u8C61:");

        buttonAddFile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/pepsoft/worldpainter/icons/brick_add.png"))); // NOI18N
        buttonAddFile.setToolTipText("\u6DFB\u52A0\u4E00\u4E2A\u6216\u66F4\u591A\u5BF9\u8C61");
        buttonAddFile.setMargin(new java.awt.Insets(2, 2, 2, 2));
        buttonAddFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonAddFileActionPerformed(evt);
            }
        });

        buttonRemoveFile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/pepsoft/worldpainter/icons/brick_delete.png"))); // NOI18N
        buttonRemoveFile.setToolTipText("\u79FB\u9664\u9009\u4E2D\u5BF9\u8C61");
        buttonRemoveFile.setEnabled(false);
        buttonRemoveFile.setMargin(new java.awt.Insets(2, 2, 2, 2));
        buttonRemoveFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonRemoveFileActionPerformed(evt);
            }
        });

        jLabel7.setText("\u751F\u6210\u51E0\u7387:");

        spinnerBlocksPerAttempt.setModel(new javax.swing.SpinnerNumberModel(20, 1, 99999, 1));
        spinnerBlocksPerAttempt.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerBlocksPerAttemptStateChanged(evt);
            }
        });

        labelBlocksPerAttempt.setText("\u6BCF x \u4E2A\u65B9\u5757 1%; y \u4E2A\u65B9\u5757 50%; z \u4E2A\u65B9\u5757 100%)");

        jLabel10.setText("1/");

        jLabel5.setText("\u7F51\u683C:");

        spinnerGrid.setModel(new javax.swing.SpinnerNumberModel(1, 1, 999, 1));
        spinnerGrid.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerGridStateChanged(evt);
            }
        });

        jLabel8.setText("(\u572850%\u51E0\u7387\u4E0B)");

        jLabel9.setText("\u968F\u673A\u504F\u79FB:");

        spinnerRandomOffset.setModel(new javax.swing.SpinnerNumberModel(0, 0, 999, 1));
        spinnerRandomOffset.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerRandomOffsetStateChanged(evt);
            }
        });

        jLabel11.setText("\u4E2A\u65B9\u5757");

        jLabel12.setText("\u6709\u6548\u5BC6\u5EA6:");

        jLabel13.setText("(\u5BF9\u8C61\u5728\u968F\u673A\u65B9\u5411\u4E0A\u4F4D\u79FB\u6700\u591A\u8FD9\u4E2A\u8DDD\u79BB)");

        jLabel14.setText("\u4E2A\u65B9\u5757");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(buttonAddFile, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(buttonRemoveFile, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(buttonEdit, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(buttonReloadAll, javax.swing.GroupLayout.Alignment.TRAILING)))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(jLabel1)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel7)
                            .addComponent(jLabel5)
                            .addComponent(jLabel9)
                            .addComponent(jLabel12))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(spinnerRandomOffset, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel11)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel13))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(spinnerGrid, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel14))
                            .addComponent(labelBlocksPerAttempt)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel10)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spinnerBlocksPerAttempt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel8)))))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(buttonAddFile)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonRemoveFile)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonEdit)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonReloadAll)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(spinnerGrid, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel14))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(spinnerBlocksPerAttempt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelBlocksPerAttempt)
                    .addComponent(jLabel12))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(spinnerRandomOffset, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11)
                    .addComponent(jLabel13))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jSeparator2)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void buttonReloadAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonReloadAllActionPerformed
        reloadObjects();
    }//GEN-LAST:event_buttonReloadAllActionPerformed

    private void buttonEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonEditActionPerformed
        editObjects();
    }//GEN-LAST:event_buttonEditActionPerformed

    private void buttonSetDecayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSetDecayActionPerformed
        setLeavesDecay();
    }//GEN-LAST:event_buttonSetDecayActionPerformed

    private void buttonSetNoDecayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSetNoDecayActionPerformed
        setLeavesNoDecay();
    }//GEN-LAST:event_buttonSetNoDecayActionPerformed

    private void buttonResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonResetActionPerformed
        resetLeafDecay();
    }//GEN-LAST:event_buttonResetActionPerformed

    private void listObjectsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_listObjectsMouseClicked
        if (evt.getClickCount() == 2) {
            int row = listObjects.getSelectedIndex();
            if (row != -1) {
                WPObject object = listModel.getElementAt(row);
                EditObjectAttributes dialog = new EditObjectAttributes(SwingUtilities.getWindowAncestor(this), object, colourScheme);
                dialog.setVisible(true);
                if (! dialog.isCancelled()) {
                    refreshLeafDecaySettings();
                }
            }
        }
    }//GEN-LAST:event_listObjectsMouseClicked

    private void jLabel6MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel6MouseClicked
        try {
            DesktopUtils.open(new URL("https://www.worldpainter.net/doc/legacy/customobjects"));
        } catch (MalformedURLException e) {
            throw new RuntimeException("Malformed URL exception while trying to open https://www.worldpainter.net/doc/legacy/customobjects", e);
        }
    }//GEN-LAST:event_jLabel6MouseClicked

    private void buttonAddFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonAddFileActionPerformed
        addFilesOrDirectory();
    }//GEN-LAST:event_buttonAddFileActionPerformed

    private void buttonRemoveFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonRemoveFileActionPerformed
        removeFiles();
    }//GEN-LAST:event_buttonRemoveFileActionPerformed

    private void spinnerBlocksPerAttemptStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerBlocksPerAttemptStateChanged
        updateBlocksPerAttempt();
        settingsChanged();
    }//GEN-LAST:event_spinnerBlocksPerAttemptStateChanged

    private void spinnerGridStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerGridStateChanged
        updateBlocksPerAttempt();
        settingsChanged();
    }//GEN-LAST:event_spinnerGridStateChanged

    private void spinnerRandomOffsetStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerRandomOffsetStateChanged
        settingsChanged();
    }//GEN-LAST:event_spinnerRandomOffsetStateChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonAddFile;
    private javax.swing.JButton buttonEdit;
    private javax.swing.JButton buttonReloadAll;
    private javax.swing.JButton buttonRemoveFile;
    private javax.swing.JButton buttonReset;
    private javax.swing.JButton buttonSetDecay;
    private javax.swing.JButton buttonSetNoDecay;
    private javax.swing.JTextField fieldName;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JLabel labelBlocksPerAttempt;
    private javax.swing.JLabel labelEffectiveLeafDecaySetting;
    private javax.swing.JLabel labelLeafDecayTitle;
    private javax.swing.JList<WPObject> listObjects;
    private org.pepsoft.worldpainter.layers.renderers.PaintPicker paintPicker1;
    private javax.swing.JSpinner spinnerBlocksPerAttempt;
    private javax.swing.JSpinner spinnerGrid;
    private javax.swing.JSpinner spinnerRandomOffset;
    // End of variables declaration//GEN-END:variables

    private final DefaultListModel<WPObject> listModel;
    private final NumberFormat numberFormat = NumberFormat.getInstance();
    private ColourScheme colourScheme;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Bo2LayerEditor.class);
    private static final long serialVersionUID = 1L;
}