package org.pepsoft.worldpainter.layers;

import com.google.common.collect.ImmutableSet;
import org.pepsoft.util.DesktopUtils;
import org.pepsoft.util.IconUtils;
import org.pepsoft.util.swing.BetterJPopupMenu;
import org.pepsoft.worldpainter.App;
import org.pepsoft.worldpainter.ColourScheme;
import org.pepsoft.worldpainter.Platform;
import org.pepsoft.worldpainter.World2;
import org.pepsoft.worldpainter.biomeschemes.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.*;
import java.util.function.Consumer;

import static java.lang.Boolean.TRUE;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.emptySet;
import static java.util.EnumSet.noneOf;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;
import static javax.swing.BoxLayout.PAGE_AXIS;
import static org.pepsoft.worldpainter.App.KEY_PAINT_ID;
import static org.pepsoft.worldpainter.Platform.Capability.*;
import static org.pepsoft.worldpainter.biomeschemes.Minecraft1_21Biomes.*;
import static org.pepsoft.worldpainter.layers.BiomesPanel.BiomeOption.*;
import static org.pepsoft.worldpainter.painting.PaintFactory.createDiscreteLayerPaintId;

/**
 * Created by pepijn on 27-05-15.
 */
public class BiomesPanel extends JPanel implements CustomBiomeManager.CustomBiomeListener {
    public BiomesPanel(CustomBiomeManager customBiomeManager, Listener listener, ButtonGroup buttonGroup) {
        this.customBiomeManager = customBiomeManager;
        this.listener = listener;
        this.buttonGroup = buttonGroup;

        initComponents();

        customBiomeManager.addListener(this);
    }

    public void loadBiomes(Platform platform, ColourScheme colourScheme) {
        biomeHelper = new BiomeHelper(colourScheme, customBiomeManager, platform);
        BiomesSet desiredSet;
        // TODO move this stuff to BiomeScheme/PlatformProvider
        if (platform.capabilities.contains(NAMED_BIOMES)) {
            desiredSet = MINECRAFT_1_20_BIOMES;
            showIds = false;
        } else if (platform.capabilities.contains(BIOMES) || platform.capabilities.contains(BIOMES_3D)) {
            desiredSet = MINECRAFT_1_17_BIOMES;
            showIds = true;
        } else {
            desiredSet = null;
        }
        if (biomesSet != desiredSet) {
            loadBiomes(desiredSet, colourScheme);
        }
    }

    public void selectBiome(int biomeId) {
        if (selectedBiome == biomeId) {
            // Biome already selected
            selectCurrentBaseBiomeButton();
            notifyListener();
        } else {
            final BiomeDescriptor descriptor = findBiomeDescriptor(biomeId);
            if (descriptor.baseId == selectedBaseBiome) {
                // The corresponding base biome is already selected; just update the options if necessary
                selectCurrentBaseBiomeButton();
                forEveryOption(checkBox -> {
                    if (descriptor.options.contains(checkBox.getClientProperty(KEY_BIOME_OPTION))) {
                        // Checkbox should be checked
                        if (! checkBox.isSelected()) {
                            checkBox.setSelected(true);
                        }
                    } else {
                        // Checkbox should be unchecked
                        if (checkBox.isSelected()) {
                            checkBox.setSelected(false);
                        }
                    }
                });
                updateOptions();
                updateLabels();
            } else {
                // Base biome also needs to be updated
                selectedBaseBiome = descriptor.baseId;
                selectCurrentBaseBiomeButton();
                resetOptions();
                forEveryOption(checkBox -> {
                    if (descriptor.options.contains(checkBox.getClientProperty(KEY_BIOME_OPTION))) {
                        checkBox.setSelected(true);
                    }
                });
                updateOptions();
                updateLabels();
            }
            notifyListener();
        }
    }

    // CustomBiomeListener

    @Override
    public void customBiomeAdded(CustomBiome customBiome) {
        addButton(customBiome);
    }

    @Override
    public void customBiomeChanged(CustomBiome customBiome) {
        for (Component component: grid.getComponents()) {
            if ((component instanceof JToggleButton) && (((Integer) ((JToggleButton) component).getClientProperty(KEY_BIOME)) == customBiome.getId())) {
                final JToggleButton button = (JToggleButton) component;
                final BufferedImage pattern = customBiome.getPattern();
                button.setIcon((pattern != null) ? new ImageIcon(pattern) : IconUtils.createScaledColourIcon(customBiome.getColour()));
                button.setToolTipText(customBiome.getName());
                return;
            }
        }
    }

    @Override
    public void customBiomeRemoved(CustomBiome customBiome) {
        for (Component component: grid.getComponents()) {
            if ((component instanceof JToggleButton) && (((Integer) ((JToggleButton) component).getClientProperty(KEY_BIOME)) == customBiome.getId())) {
                JToggleButton button = (JToggleButton) component;
                if (button.isSelected()) {
                    button.setSelected(false);
                    selectedBiome = BIOME_PLAINS;
                    notifyListener();
                }
                grid.remove(component);
                forceRepaint();
                return;
            }
        }
    }

    private void initComponents() {
        setLayout(new BoxLayout(this, PAGE_AXIS));

        label1.setHorizontalTextPosition(JLabel.LEADING);
        label1.setAlignmentX(0.0f);
        add(label1);
        label2.setAlignmentX(0.0f);
        add(label2);

        JButton addCustomBiomeButton = new JButton(IconUtils.loadScaledIcon("org/pepsoft/worldpainter/icons/plus.png"));
        addCustomBiomeButton.putClientProperty(KEY_ADD_BUTTON, TRUE);
        addCustomBiomeButton.setMargin(App.BUTTON_INSETS);
        addCustomBiomeButton.setToolTipText("\u6DFB\u52A0\u4E00\u4E2A\u81EA\u5B9A\u4E49\u7FA4\u7CFB");
        addCustomBiomeButton.addActionListener(e -> {
            final World2 world = App.getInstance().getWorld();
            if (world == null) {
                DesktopUtils.beep();
                return;
            }
            final Window parent = SwingUtilities.getWindowAncestor(BiomesPanel.this);
            final int id = customBiomeManager.getNextId();
            if (id == -1) {
                JOptionPane.showMessageDialog(parent, "\u5DF2\u8FBE\u5230\u81EA\u5B9A\u4E49\u7FA4\u7CFB\u6570\u91CF\u7684\u6700\u5927\u503C", "\u8FBE\u5230\u6700\u5927\u503C", JOptionPane.ERROR_MESSAGE);
                return;
            }
            final Platform platform = world.getPlatform();
            CustomBiome customBiome = new CustomBiome(platform.capabilities.contains(NAMED_BIOMES) ? "namespace:biome" : "\u81EA\u5B9A\u4E49", id, Color.ORANGE.getRGB());
            CustomBiomeDialog dialog = new CustomBiomeDialog(parent, customBiome, true, platform);
            dialog.setVisible(true);
            if (! dialog.isCancelled()) {
                customBiomeManager.addCustomBiome(parent, customBiome);
            }
        });
        grid.add(addCustomBiomeButton);
        grid.setAlignmentX(0.0f);
        add(grid);

        optionsPanel.setLayout(new BoxLayout(optionsPanel, PAGE_AXIS));
        add(optionsPanel);
    }

    private void loadBiomes(BiomesSet biomesSet, ColourScheme colourScheme) {
        if (this.biomesSet != null) {
            while ((((JComponent) grid.getComponent(0)).getClientProperty(KEY_ADD_BUTTON) == null)
                    && (((JComponent) grid.getComponent(0)).getClientProperty(KEY_CUSTOM_BIOME) == null)) {
                // The first component is not the "add custom biome" button or a custom biome; keep removing components until it is
                grid.remove(0);
            }
        }
        this.biomesSet = biomesSet;
        if (biomesSet != null) {
            int index = 0;
            for (final int biome: biomesSet.biomeOrder) {
                if (biome != -1) {
                    final JToggleButton button = new JToggleButton(new ImageIcon(BiomeSchemeManager.createImage(StaticBiomeInfo.INSTANCE, biome, colourScheme)));
                    button.putClientProperty(KEY_BIOME, biome);
                    button.setMargin(App.BUTTON_INSETS);
                    StringBuilder tooltip = new StringBuilder();
                    tooltip.append(biomesSet.displayNames[biome]);
                    if (showIds) {
                        tooltip.append(" (");
                        List<Integer> variantIds = findVariants(biome);
                        tooltip.append(variantIds.stream().map(i -> Integer.toString(i)).collect(joining(", ")));
                        tooltip.append(')');
                    } else {
                        Set<BiomeOption> options = findVariantOptions(biome);
                        if (! options.isEmpty()) {
                            tooltip.append(" (\u9009\u9879: ");
                            tooltip.append(options.stream().map(this::createOptionName).collect(joining(", ")));
                            tooltip.append(')');
                        }
                    }
                    button.setToolTipText(tooltip.toString());
                    buttonGroup.add(button);
                    if (biome == selectedBiome) {
                        button.setSelected(true);
                    }
                    button.addActionListener(e -> {
                        if (button.isSelected()) {
                            selectBaseBiome(biome);
                        }
                    });
                    grid.add(button, index++);
                } else {
                    grid.add(Box.createGlue(), index++);
                }
            }
        }
        resetOptions();
        forceRepaint();
    }

    private void selectBaseBiome(int biome) {
        selectedBaseBiome = biome;
        selectedBiome = biome;
        notifyListener();
        resetOptions();
        updateLabels();
    }

    private void selectCurrentBaseBiomeButton() {
        for (Component component: grid.getComponents()) {
            if (component instanceof JToggleButton) {
                final JToggleButton button = (JToggleButton) component;
                if ((int) button.getClientProperty(KEY_BIOME) == selectedBaseBiome) {
                    button.setSelected(true);
                    return;
                }
            }
        }
        throw new IllegalArgumentException("No button found for currently selected base biome ID " + selectedBaseBiome);
    }

    private void resetOptions() {
        Set<BiomeOption> availableOptions = findAvailableOptions(selectedBaseBiome);
        optionsPanel.removeAll();
        optionsPanel.add(new JLabel("\u53D8\u79CD:"));
        for (BiomeOption option: availableOptions) {
            JCheckBox checkBox = new JCheckBox(createOptionName(option));
            checkBox.addActionListener(event -> updateOptions());
            checkBox.putClientProperty(KEY_BIOME_OPTION, option);
            checkBox.setEnabled(findBiome(selectedBaseBiome, EnumSet.of(option)) != -1);
            optionsPanel.add(checkBox);
        }
    }

    private String createOptionName(BiomeOption option) {
        String string = stream(option.name().split("_"))
                .map(s -> s.charAt(0) + s.substring(1).toLowerCase())
                .collect(joining(" "));
        switch (string) {
            case "Frozen":
                string = "\u51B0\u51BB\u53D8\u79CD";
                break;
            case "Deep":
                string = "\u6DF1\u6D77\u53D8\u79CD";
                break;
            case "Warm":
                string = "\u6696\u6C34\u53D8\u79CD";
                break;
            case "Lukewarm":
                string = "\u6E29\u6C34\u53D8\u79CD";
                break;
            case "Cold":
                string = "\u51B7\u6C34\u53D8\u79CD";
                break;
            case "Flowers":
                string = "\u7E41\u82B1\u53D8\u79CD";
                break;
            case "Windswept":
                string = "\u98CE\u88AD\u53D8\u79CD";
                break;
            case "Sparse":
                string = "\u7A00\u758F\u53D8\u79CD";
                break;
            case "Old Growth":
                string = "\u539F\u59CB\u53D8\u79CD";
                break;
            case "Gravelly":
                string = "\u7802\u783E\u53D8\u79CD";
                break;
            case "Plateau":
                string = "\u9AD8\u539F\u53D8\u79CD";
                break;
            case "Wooded":
                string = "\u7E41\u8302\u53D8\u79CD";
                break;
            case "Eroded":
                string = "\u4FB5\u8680\u53D8\u79CD";
                break;
            case "Snowy":
                string = "\u79EF\u96EA\u53D8\u79CD";
                break;
        }
        return string;
    }

    private void updateOptions() {
        final Set<BiomeOption> selectedOptions = getSelectedOptions();
        selectedBiome = findBiome(selectedBaseBiome, selectedOptions);
        if (selectedBiome == -1) {
            // This means the new combination of selected options is no longer valid. This can happen when an option
            // becomes available only after selecting another option and then the other option is deselected. Just
            // deselect everything when this happens
            forEveryOption(checkBox -> {
                if (checkBox.isSelected()) {
                    checkBox.setSelected(false);
                }
            });
            selectedOptions.clear();
            selectedBiome = selectedBaseBiome;
        }
        notifyListener();
        forEveryOption(checkBox -> {
            BiomeOption biomeOption = (BiomeOption) checkBox.getClientProperty(KEY_BIOME_OPTION);
            if (selectedOptions.contains(biomeOption)) {
                checkBox.setEnabled(true);
            } else {
                EnumSet<BiomeOption> optionsCopy = EnumSet.copyOf(selectedOptions);
                optionsCopy.add(biomeOption);
                checkBox.setEnabled(findBiome(selectedBaseBiome, optionsCopy) != -1);
            }
        });
        updateLabels();
    }

    private Set<BiomeOption> getSelectedOptions() {
        final Set<BiomeOption> selectedOptions = noneOf(BiomeOption.class);
        forEveryOption(checkBox -> {
            if (checkBox.isSelected()) {
                selectedOptions.add((BiomeOption) checkBox.getClientProperty(KEY_BIOME_OPTION));
            }
        });
        return selectedOptions;
    }

    /**
     * Find the actual biome ID for a specific base biome and a set of selected
     * options.
     *
     * @param baseId The base ID of the biome.
     * @param options The selected options.
     * @return The actual biome ID for the specified base biome and options, or
     * -1 if the specified base ID or options are invalid or don't specify an
     * existing actual biome.
     */
    private int findBiome(int baseId, Set<BiomeOption> options) {
        for (BiomeDescriptor descriptor: biomesSet.descriptors) {
            if ((descriptor.getBaseId() == baseId) && descriptor.getOptions().equals(options)) {
                return descriptor.getId();
            }
        }
        return -1;
    }

    private void updateLabels() {
        label1.setText("\u9009\u62E9\u7684\u7FA4\u7CFB: " + (showIds ? selectedBiome : ""));
        label1.setIcon(biomeHelper.getBiomeIcon(selectedBiome));
        label2.setText(biomeHelper.getBiomeNameWithoutId(selectedBiome));
    }

    private void addButton(CustomBiome customBiome) {
        final int biome = customBiome.getId();
        final BufferedImage pattern = customBiome.getPattern();
        final JToggleButton button = new JToggleButton((pattern != null) ? new ImageIcon(pattern) : IconUtils.createScaledColourIcon(customBiome.getColour()));
        button.putClientProperty(KEY_BIOME, biome);
        button.putClientProperty(KEY_PAINT_ID, createDiscreteLayerPaintId(Biome.INSTANCE, biome));
        button.putClientProperty(KEY_CUSTOM_BIOME, TRUE);
        button.setMargin(App.BUTTON_INSETS);
        button.setToolTipText(customBiome.getName() + " (" + biome + "); \u53F3\u952E\u70B9\u51FB\u67E5\u770B\u9009\u9879");
        button.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        showPopupMenu(e);
                    }
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        showPopupMenu(e);
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        showPopupMenu(e);
                    }
                }

                private void showPopupMenu(MouseEvent e) {
                    JPopupMenu popup = new BetterJPopupMenu();

                    JMenuItem item = new JMenuItem("\u7F16\u8F91...");
                    item.addActionListener(actionEvent -> {
                        CustomBiomeDialog dialog = new CustomBiomeDialog(SwingUtilities.getWindowAncestor(button), customBiome, false, App.getInstance().getWorld().getPlatform());
                        dialog.setVisible(true);
                        if (! dialog.isCancelled()) {
                            customBiomeManager.editCustomBiome(customBiome);
                        }
                    });
                    popup.add(item);

                    item = new JMenuItem("\u79FB\u9664...");
                    item.addActionListener(actionEvent -> {
                        if (JOptionPane.showConfirmDialog(button, "Are you sure you want to remove custom biome \"" + customBiome.getName() + "\" (ID: " + customBiome.getId() + ")?\nAny occurrences will be replaced with Automatic Biomes", "Confirm Removal", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                            customBiomeManager.removeCustomBiome(customBiome);
                        }
                    });
                    popup.add(item);

                    popup.show(button, e.getX(), e.getY());
                }
            });
        buttonGroup.add(button);
        button.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                selectBaseBiome(biome);
            }
        });
        grid.add(button, grid.getComponentCount() - 1);
        forceRepaint();
    }

    private void forceRepaint() {
        // Not sure why this is necessary. Swing bug?
        Window parent = SwingUtilities.getWindowAncestor(this);
        if (parent != null) {
            parent.validate();
        }
    }

    /**
     * Find the available biome options given a particular base biome.
     *
     * @param baseId The ID of the base biome.
     * @return The total available options for the specified base biome. May be
     * empty, but not {@code null}.
     */
    private Set<BiomeOption> findAvailableOptions(int baseId) {
        if ((biomesSet != null) && (baseId < biomesSet.displayNames.length) && (biomesSet.displayNames[baseId] != null)) {
            Set<BiomeOption> availableOptions = noneOf(BiomeOption.class);
            for (BiomeDescriptor descriptor: biomesSet.descriptors) {
                if (descriptor.getBaseId() == baseId) {
                    availableOptions.addAll(descriptor.getOptions());
                }
            }
            return availableOptions;
        } else {
            return emptySet();
        }
    }

    /**
     * Find the IDs of all variants of the specified base biome.
     *
     * @param baseId The ID of the base biome.
     * @return The IDs of all variants of the specified base biome (including
     *     the base biome itself).
     */
    private List<Integer> findVariants(int baseId) {
        List<Integer> variants = new ArrayList<>();
        for (BiomeDescriptor descriptor: biomesSet.descriptors) {
            if (descriptor.getBaseId() == baseId) {
                variants.add(descriptor.getId());
            }
        }
        return variants;
    }

    /**
     * Find the options of all variants of the specified base biome.
     *
     * @param baseId The ID of the base biome.
     * @return The options of all variants of the specified base biome (including the base biome itself).
     */
    private Set<BiomeOption> findVariantOptions(int baseId) {
        Set<BiomeOption> options = noneOf(BiomeOption.class);
        for (BiomeDescriptor descriptor: biomesSet.descriptors) {
            if (descriptor.getBaseId() == baseId) {
                options.addAll(descriptor.getOptions());
            }
        }
        return options;
    }

    private BiomeDescriptor findBiomeDescriptor(int biomeId) {
        for (BiomeDescriptor descriptor: biomesSet.descriptors) {
            if (descriptor.id == biomeId) {
                return descriptor;
            }
        }
        throw new IllegalArgumentException("Biome ID not found in current biomes set: " + biomeId);
    }

    private void notifyListener() {
        listener.biomeSelected(selectedBiome);
    }

    private void forEveryOption(Consumer<JCheckBox> consumer) {
        stream(optionsPanel.getComponents())
                .filter(c -> c instanceof JCheckBox)
                .forEach(c -> consumer.accept((JCheckBox) c));
    }

    private final JPanel grid = new JPanel(new GridLayout(0, 5)), optionsPanel = new JPanel();
    private final ButtonGroup buttonGroup;
    private final JLabel label1 = new JLabel("\u9009\u62E9\u7684\u7FA4\u7CFB: 1"), label2 = new JLabel("\u5E73\u539F");

    private final CustomBiomeManager customBiomeManager;
    private final Listener listener;
    private BiomeHelper biomeHelper;
    private BiomesSet biomesSet;
    private int selectedBiome = BIOME_PLAINS, selectedBaseBiome = BIOME_PLAINS;
    private boolean showIds;

    // TODO move this stuff to BiomeScheme/PlatformProvider

    /**
     * Display order of the biomes on the biomes panel. This only contains the base biomes that get their own button;
     * all other biomes are presumed to be variants of the base biomes.
     */
    private static final int[] MC_117_BIOME_ORDER = {
            BIOME_PLAINS, BIOME_FOREST, BIOME_SWAMP, BIOME_JUNGLE, BIOME_BAMBOO_JUNGLE,
            BIOME_BIRCH_FOREST, BIOME_DARK_FOREST, BIOME_TAIGA, BIOME_GIANT_TREE_TAIGA, BIOME_GIANT_SPRUCE_TAIGA,
            BIOME_MOUNTAINS, BIOME_MUSHROOM_FIELDS, BIOME_DESERT, BIOME_SAVANNA, BIOME_BADLANDS,
            BIOME_SNOWY_TUNDRA, BIOME_ICE_SPIKES, BIOME_OCEAN, BIOME_RIVER, BIOME_BEACH,
            BIOME_STONE_SHORE, BIOME_DRIPSTONE_CAVES, BIOME_LUSH_CAVES, BIOME_THE_END, BIOME_THE_VOID,
            BIOME_NETHER_WASTES, BIOME_SOUL_SAND_VALLEY, BIOME_CRIMSON_FOREST, BIOME_WARPED_FOREST, BIOME_BASALT_DELTAS,
    };

    /**
     * Display order of the biomes on the biomes panel. This only contains the base biomes that get their own button;
     * all other biomes are presumed to be variants of the base biomes.
     */
    private static final int[] MC_121_BIOME_ORDER = {
            BIOME_PLAINS, BIOME_FOREST, BIOME_SWAMP, BIOME_JUNGLE, BIOME_BAMBOO_JUNGLE,
            BIOME_BIRCH_FOREST, BIOME_DARK_FOREST, BIOME_MANGROVE_SWAMP, BIOME_OLD_GROWTH_PINE_TAIGA, BIOME_OLD_GROWTH_SPRUCE_TAIGA,
            BIOME_WINDSWEPT_HILLS, BIOME_DESERT, BIOME_SAVANNA, BIOME_BADLANDS, BIOME_TAIGA,
            BIOME_SNOWY_PLAINS, BIOME_ICE_SPIKES, BIOME_OCEAN, BIOME_RIVER, BIOME_BEACH,
            BIOME_DRIPSTONE_CAVES, BIOME_MEADOW, BIOME_STONY_PEAKS, BIOME_JAGGED_PEAKS, BIOME_STONY_SHORE,
            BIOME_LUSH_CAVES, BIOME_MUSHROOM_FIELDS, BIOME_SNOWY_SLOPES, BIOME_GROVE, BIOME_FROZEN_PEAKS,
            BIOME_DEEP_DARK, BIOME_THE_VOID, BIOME_CHERRY_GROVE, BIOME_PALE_GARDEN, -1,
            BIOME_THE_END, BIOME_END_BARRENS, BIOME_END_HIGHLANDS, BIOME_END_MIDLANDS, BIOME_SMALL_END_ISLANDS,
            BIOME_NETHER_WASTES, BIOME_SOUL_SAND_VALLEY, BIOME_CRIMSON_FOREST, BIOME_WARPED_FOREST, BIOME_BASALT_DELTAS
    };

    private static final String KEY_BIOME = BiomesPanel.class.getName() + ".biome";
    private static final String KEY_BIOME_OPTION = BiomesPanel.class.getName() + ".biomeOption";
    private static final String KEY_ADD_BUTTON = BiomesPanel.class.getName() + ".addButton";
    private static final String KEY_CUSTOM_BIOME = BiomesPanel.class.getName() + ".customBiome";

    private static final Set<BiomeDescriptor> MC_117_DESCRIPTORS = ImmutableSet.of(
        new BiomeDescriptor(BIOME_OCEAN),
        new BiomeDescriptor(BIOME_PLAINS),
        new BiomeDescriptor(BIOME_DESERT),
        new BiomeDescriptor(BIOME_MOUNTAINS),
        new BiomeDescriptor(BIOME_FOREST),
        new BiomeDescriptor(BIOME_TAIGA),
        new BiomeDescriptor(BIOME_SWAMP),
        new BiomeDescriptor(BIOME_RIVER),
        new BiomeDescriptor(BIOME_NETHER_WASTES),
        new BiomeDescriptor(BIOME_THE_END),

        new BiomeDescriptor(BIOME_FROZEN_OCEAN, 0, FROZEN),
        new BiomeDescriptor(BIOME_FROZEN_RIVER, 7, FROZEN),
        new BiomeDescriptor(BIOME_SNOWY_TUNDRA),
        new BiomeDescriptor(BIOME_SNOWY_MOUNTAINS, 3, SNOWY), // Double
        new BiomeDescriptor(BIOME_MUSHROOM_FIELDS),
        new BiomeDescriptor(BIOME_MUSHROOM_FIELD_SHORE, 14, SHORE), // Double
        new BiomeDescriptor(BIOME_BEACH),
        new BiomeDescriptor(BIOME_DESERT_HILLS, 2, HILLS),
        new BiomeDescriptor(BIOME_WOODED_HILLS, 4, HILLS),
        new BiomeDescriptor(BIOME_TAIGA_HILLS, 5, HILLS),

        new BiomeDescriptor(BIOME_MOUNTAIN_EDGE, 3, EDGE),
        new BiomeDescriptor(BIOME_JUNGLE),
        new BiomeDescriptor(BIOME_JUNGLE_HILLS, 21, HILLS),
        new BiomeDescriptor(BIOME_JUNGLE_EDGE, 21, EDGE),
        new BiomeDescriptor(BIOME_DEEP_OCEAN, 0, DEEP),
        new BiomeDescriptor(BIOME_STONE_SHORE),
        new BiomeDescriptor(BIOME_SNOWY_BEACH, 16, SNOWY),
        new BiomeDescriptor(BIOME_BIRCH_FOREST),
        new BiomeDescriptor(BIOME_BIRCH_FOREST_HILLS, 27, HILLS),
        new BiomeDescriptor(BIOME_DARK_FOREST),

        new BiomeDescriptor(BIOME_SNOWY_TAIGA, 5, SNOWY),
        new BiomeDescriptor(BIOME_SNOWY_TAIGA_HILLS, 5, SNOWY, HILLS),
        new BiomeDescriptor(BIOME_GIANT_TREE_TAIGA),
        new BiomeDescriptor(BIOME_GIANT_TREE_TAIGA_HILLS, 32, HILLS),
        new BiomeDescriptor(BIOME_WOODED_MOUNTAINS, 3, WOODED),
        new BiomeDescriptor(BIOME_SAVANNA),
        new BiomeDescriptor(BIOME_SAVANNA_PLATEAU, 35, PLATEAU),
        new BiomeDescriptor(BIOME_BADLANDS),
        new BiomeDescriptor(BIOME_WOODED_BADLANDS_PLATEAU, 37, WOODED, PLATEAU),
        new BiomeDescriptor(BIOME_BADLANDS_PLATEAU, 37, PLATEAU),

        new BiomeDescriptor(BIOME_SMALL_END_ISLANDS, 9, SMALL_ISLANDS),
        new BiomeDescriptor(BIOME_END_MIDLANDS, 9, MIDLANDS),
        new BiomeDescriptor(BIOME_END_HIGHLANDS, 9, HIGHLANDS),
        new BiomeDescriptor(BIOME_END_BARRENS, 9, BARRENS),
        new BiomeDescriptor(BIOME_WARM_OCEAN, 0, WARM),
        new BiomeDescriptor(BIOME_LUKEWARM_OCEAN, 0, LUKEWARM),
        new BiomeDescriptor(BIOME_COLD_OCEAN, 0, COLD),
        new BiomeDescriptor(BIOME_DEEP_WARM_OCEAN, 0, DEEP, WARM),
        new BiomeDescriptor(BIOME_DEEP_LUKEWARM_OCEAN, 0, DEEP, LUKEWARM),
        new BiomeDescriptor(BIOME_DEEP_COLD_OCEAN, 0, DEEP, COLD),

        new BiomeDescriptor(BIOME_DEEP_FROZEN_OCEAN, 0, DEEP, FROZEN),

        new BiomeDescriptor(BIOME_THE_VOID),
        new BiomeDescriptor(BIOME_SUNFLOWER_PLAINS, 1, FLOWERS),

        new BiomeDescriptor(BIOME_DESERT_LAKES, 2, LAKES),
        new BiomeDescriptor(BIOME_GRAVELLY_MOUNTAINS, 3, GRAVELLY),
        new BiomeDescriptor(BIOME_FLOWER_FOREST, 4, FLOWERS),
        new BiomeDescriptor(BIOME_TAIGA_MOUNTAINS, 5, MOUNTAINOUS),
        new BiomeDescriptor(BIOME_SWAMP_HILLS, 6, HILLS),

        new BiomeDescriptor(BIOME_ICE_SPIKES),
        new BiomeDescriptor(BIOME_MODIFIED_JUNGLE, 21, MODIFIED),

        new BiomeDescriptor(BIOME_MODIFIED_JUNGLE_EDGE, 21, MODIFIED, EDGE),
        new BiomeDescriptor(BIOME_TALL_BIRCH_FOREST, 27, TALL),
        new BiomeDescriptor(BIOME_TALL_BIRCH_HILLS, 27, HILLS, TALL),
        new BiomeDescriptor(BIOME_DARK_FOREST_HILLS, 29, HILLS),
        new BiomeDescriptor(BIOME_SNOWY_TAIGA_MOUNTAINS, 5, SNOWY, MOUNTAINOUS),

        new BiomeDescriptor(BIOME_GIANT_SPRUCE_TAIGA),
        new BiomeDescriptor(BIOME_GIANT_SPRUCE_TAIGA_HILLS, 160, HILLS),
        new BiomeDescriptor(BIOME_MODIFIED_GRAVELLY_MOUNTAINS, 3, GRAVELLY, VARIANT),
        new BiomeDescriptor(BIOME_SHATTERED_SAVANNA, 35, SHATTERED),
        new BiomeDescriptor(BIOME_SHATTERED_SAVANNA_PLATEAU, 35, SHATTERED, PLATEAU),
        new BiomeDescriptor(BIOME_ERODED_BADLANDS, 37, ERODED),
        new BiomeDescriptor(BIOME_MODIFIED_WOODED_BADLANDS_PLATEAU, 37, MODIFIED, WOODED, PLATEAU),
        new BiomeDescriptor(BIOME_MODIFIED_BADLANDS_PLATEAU, 37, MODIFIED, PLATEAU),
        new BiomeDescriptor(BIOME_BAMBOO_JUNGLE),
        new BiomeDescriptor(BIOME_BAMBOO_JUNGLE_HILLS, 168, HILLS),

        new BiomeDescriptor(BIOME_SOUL_SAND_VALLEY),
        new BiomeDescriptor(BIOME_CRIMSON_FOREST),
        new BiomeDescriptor(BIOME_WARPED_FOREST),
        new BiomeDescriptor(BIOME_BASALT_DELTAS),
        new BiomeDescriptor(BIOME_DRIPSTONE_CAVES),
        new BiomeDescriptor(BIOME_LUSH_CAVES)
    );

    private static final List<BiomeDescriptor> MC_121_DESCRIPTORS = asList(
        new BiomeDescriptor(BIOME_OCEAN),
        new BiomeDescriptor(BIOME_PLAINS),
        new BiomeDescriptor(BIOME_DESERT),
        new BiomeDescriptor(BIOME_WINDSWEPT_HILLS),
        new BiomeDescriptor(BIOME_FOREST),
        new BiomeDescriptor(BIOME_TAIGA),
        new BiomeDescriptor(BIOME_SWAMP),
        new BiomeDescriptor(BIOME_RIVER),
        new BiomeDescriptor(BIOME_NETHER_WASTES),
        new BiomeDescriptor(BIOME_THE_END),

        new BiomeDescriptor(BIOME_FROZEN_OCEAN, BIOME_OCEAN, FROZEN),
        new BiomeDescriptor(BIOME_FROZEN_RIVER, BIOME_RIVER, FROZEN),
        new BiomeDescriptor(BIOME_SNOWY_PLAINS),
        // BIOME_SNOWY_MOUNTAINS no longer exists separately in Minecraft 1.18+
        new BiomeDescriptor(BIOME_MUSHROOM_FIELDS),
        // BIOME_MUSHROOM_FIELD_SHORE no longer exists separately in Minecraft 1.18+
        new BiomeDescriptor(BIOME_BEACH),
        // BIOME_DESERT_HILLS no longer exists separately in Minecraft 1.18+
        // BIOME_WOODED_HILLS no longer exists separately in Minecraft 1.18+
        // BIOME_TAIGA_HILLS no longer exists separately in Minecraft 1.18+

        // BIOME_MOUNTAIN_EDGE no longer exists separately in Minecraft 1.18+
        new BiomeDescriptor(BIOME_JUNGLE),
        // BIOME_JUNGLE_HILLS no longer exists separately in Minecraft 1.18+
        new BiomeDescriptor(BIOME_SPARSE_JUNGLE, BIOME_JUNGLE, SPARSE),
        new BiomeDescriptor(BIOME_DEEP_OCEAN, BIOME_OCEAN, DEEP),
        new BiomeDescriptor(BIOME_STONY_SHORE),
        new BiomeDescriptor(BIOME_SNOWY_BEACH, BIOME_BEACH, SNOWY),
        new BiomeDescriptor(BIOME_BIRCH_FOREST),
        // BIOME_BIRCH_FOREST_HILLS no longer exists separately in Minecraft 1.18+
        new BiomeDescriptor(BIOME_DARK_FOREST),

        new BiomeDescriptor(BIOME_SNOWY_TAIGA, BIOME_TAIGA, SNOWY),
        // BIOME_SNOWY_TAIGA_HILLS no longer exists separately in Minecraft 1.18+
        new BiomeDescriptor(BIOME_OLD_GROWTH_PINE_TAIGA),
        // BIOME_GIANT_TREE_TAIGA_HILLS no longer exists separately in Minecraft 1.18+
        new BiomeDescriptor(BIOME_WINDSWEPT_FOREST, BIOME_FOREST, WINDSWEPT),
        new BiomeDescriptor(BIOME_SAVANNA),
        new BiomeDescriptor(BIOME_SAVANNA_PLATEAU, BIOME_SAVANNA, PLATEAU),
        new BiomeDescriptor(BIOME_BADLANDS),
        new BiomeDescriptor(BIOME_WOODED_BADLANDS, BIOME_BADLANDS, WOODED),
        // BIOME_BADLANDS_PLATEAU no longer exists separately in Minecraft 1.18+

        new BiomeDescriptor(BIOME_SMALL_END_ISLANDS),
        new BiomeDescriptor(BIOME_END_MIDLANDS),
        new BiomeDescriptor(BIOME_END_HIGHLANDS),
        new BiomeDescriptor(BIOME_END_BARRENS),
        new BiomeDescriptor(BIOME_WARM_OCEAN, BIOME_OCEAN, WARM),
        new BiomeDescriptor(BIOME_LUKEWARM_OCEAN, BIOME_OCEAN, LUKEWARM),
        new BiomeDescriptor(BIOME_COLD_OCEAN, BIOME_OCEAN, COLD),
        // BIOME_DEEP_WARM_OCEAN no longer exists separately in Minecraft 1.18+
        new BiomeDescriptor(BIOME_DEEP_LUKEWARM_OCEAN, BIOME_OCEAN, DEEP, LUKEWARM),
        new BiomeDescriptor(BIOME_DEEP_COLD_OCEAN, BIOME_OCEAN, DEEP, COLD),

        new BiomeDescriptor(BIOME_DEEP_FROZEN_OCEAN, BIOME_OCEAN, DEEP, FROZEN),

        new BiomeDescriptor(BIOME_THE_VOID),
        new BiomeDescriptor(BIOME_SUNFLOWER_PLAINS, BIOME_PLAINS, FLOWERS),

        // BIOME_DESERT_LAKES no longer exists separately in Minecraft 1.18+
        new BiomeDescriptor(BIOME_WINDSWEPT_GRAVELLY_HILLS, BIOME_WINDSWEPT_HILLS, GRAVELLY),
        new BiomeDescriptor(BIOME_FLOWER_FOREST, BIOME_FOREST, FLOWERS),
        // BIOME_TAIGA_MOUNTAINS no longer exists separately in Minecraft 1.18+
        // BIOME_SWAMP_HILLS no longer exists separately in Minecraft 1.18+

        new BiomeDescriptor(BIOME_ICE_SPIKES),
        // BIOME_MODIFIED_JUNGLE no longer exists separately in Minecraft 1.18+

        // BIOME_MODIFIED_JUNGLE_EDGE no longer exists separately in Minecraft 1.18+
        new BiomeDescriptor(BIOME_OLD_GROWTH_BIRCH_FOREST, BIOME_BIRCH_FOREST, OLD_GROWTH),
        // BIOME_TALL_BIRCH_HILLS no longer exists separately in Minecraft 1.18+
        // BIOME_DARK_FOREST_HILLS no longer exists separately in Minecraft 1.18+
        // BIOME_SNOWY_TAIGA_MOUNTAINS no longer exists separately in Minecraft 1.18+

        new BiomeDescriptor(BIOME_OLD_GROWTH_SPRUCE_TAIGA),
        // BIOME_GIANT_SPRUCE_TAIGA_HILLS no longer exists separately in Minecraft 1.18+
        // BIOME_MODIFIED_GRAVELLY_MOUNTAINS no longer exists separately in Minecraft 1.18+
        new BiomeDescriptor(BIOME_WINDSWEPT_SAVANNA, BIOME_SAVANNA, WINDSWEPT),
        // BIOME_SHATTERED_SAVANNA_PLATEAU no longer exists separately in Minecraft 1.18+
        new BiomeDescriptor(BIOME_ERODED_BADLANDS, BIOME_BADLANDS, ERODED),
        // BIOME_MODIFIED_WOODED_BADLANDS_PLATEAU no longer exists separately in Minecraft 1.18+
        // BIOME_MODIFIED_BADLANDS_PLATEAU no longer exists separately in Minecraft 1.18+
        new BiomeDescriptor(BIOME_BAMBOO_JUNGLE),
        // BIOME_BAMBOO_JUNGLE_HILLS no longer exists separately in Minecraft 1.18+

        new BiomeDescriptor(BIOME_SOUL_SAND_VALLEY),
        new BiomeDescriptor(BIOME_CRIMSON_FOREST),
        new BiomeDescriptor(BIOME_WARPED_FOREST),
        new BiomeDescriptor(BIOME_BASALT_DELTAS),
        new BiomeDescriptor(BIOME_DRIPSTONE_CAVES),
        new BiomeDescriptor(BIOME_LUSH_CAVES),

        // Minecraft 1.18+ biomes, with synthetic numerical ID's that do not correspond to pre-1.18 biome ID's:
        new BiomeDescriptor(BIOME_PALE_GARDEN),
        new BiomeDescriptor(BIOME_CHERRY_GROVE),
        new BiomeDescriptor(BIOME_MANGROVE_SWAMP),
        new BiomeDescriptor(BIOME_DEEP_DARK),
        new BiomeDescriptor(BIOME_FROZEN_PEAKS),

        new BiomeDescriptor(BIOME_GROVE),
        new BiomeDescriptor(BIOME_JAGGED_PEAKS),
        new BiomeDescriptor(BIOME_MEADOW),
        new BiomeDescriptor(BIOME_SNOWY_SLOPES),
        new BiomeDescriptor(BIOME_STONY_PEAKS)
    );

    static {
        // Sanity checks
        final Set<String> modernIdsEncountered = new HashSet<>();
        for (int biomeId = 0; biomeId <= HIGHEST_BIOME_ID; biomeId++) {
            if ((biomeId < Minecraft1_17Biomes.BIOME_NAMES.length) && (Minecraft1_17Biomes.BIOME_NAMES[biomeId] != null)) {
                boolean found = false;
                for (BiomeDescriptor descriptor: MC_117_DESCRIPTORS) {
                    if (descriptor.id == biomeId) {
                        found = true;
                        // Check that it is reachable
                        if ((descriptor.baseId != biomeId) && MC_117_DESCRIPTORS.stream().noneMatch(d -> d.baseId == d.id && d.id == descriptor.baseId)) {
                            throw new IllegalArgumentException("Biome " + biomeId + " (" + Minecraft1_17Biomes.BIOME_NAMES[biomeId] + ") is not reachable in MC_117_DESCRIPTORS");
                        }
                        break;
                    }
                }
                if (! found) {
                    throw new IllegalArgumentException("MC_117_DESCRIPTORS is missing biome " + biomeId + " (" + Minecraft1_17Biomes.BIOME_NAMES[biomeId] + ")");
                }
            }
            if (Minecraft1_21Biomes.BIOME_NAMES[biomeId] != null) {
                if (modernIdsEncountered.contains(MODERN_IDS[biomeId])) {
                    continue;
                } else {
                    modernIdsEncountered.add(MODERN_IDS[biomeId]);
                }
                boolean found = false;
                for (BiomeDescriptor descriptor: MC_121_DESCRIPTORS) {
                    if (descriptor.id == biomeId) {
                        found = true;
                        // Check that it is reachable
                        if ((descriptor.baseId != biomeId) && MC_121_DESCRIPTORS.stream().noneMatch(d -> d.baseId == d.id && d.id == descriptor.baseId)) {
                            throw new IllegalArgumentException("Biome " + biomeId + " (" + Minecraft1_21Biomes.BIOME_NAMES[biomeId] + ") is not reachable in MC_120_DESCRIPTORS");
                        }
                        break;
                    }
                }
                if (! found) {
                    throw new IllegalArgumentException("MC_120_DESCRIPTORS is missing biome " + biomeId + " (" + Minecraft1_21Biomes.BIOME_NAMES[biomeId] + ")");
                }
            }
        }
        MC_117_DESCRIPTORS.forEach(desc -> {
            if (Minecraft1_17Biomes.BIOME_NAMES[desc.id] == null) {
                throw new IllegalArgumentException("MC_117_DESCRIPTORS contains non-existent biome ID " + desc.id);
            }
        });
        modernIdsEncountered.clear();
        MC_121_DESCRIPTORS.forEach(desc -> {
            if (Minecraft1_21Biomes.BIOME_NAMES[desc.id] == null) {
                throw new IllegalArgumentException("MC_120_DESCRIPTORS contains non-existent biome ID " + desc.id);
            }
            if (modernIdsEncountered.contains(MODERN_IDS[desc.id])) {
                System.err.println("MC_120_DESCRIPTORS contains duplicate descriptor for biome ID " + desc.id + " (" + Minecraft1_21Biomes.BIOME_NAMES[desc.id] + ") for modern ID " + MODERN_IDS[desc.id]);
            } else {
                modernIdsEncountered.add(MODERN_IDS[desc.id]);
            }
        });
        Set<Integer> uniqueIds = stream(MC_117_BIOME_ORDER).filter(i -> i != -1).boxed().collect(toSet());
        if (uniqueIds.size() != stream(MC_117_BIOME_ORDER).filter(i -> i != -1).count()) {
            throw new IllegalArgumentException("MC_117_BIOME_ORDER has duplicate IDs");
        }
        uniqueIds = MC_117_DESCRIPTORS.stream().map(d -> d.id).collect(toSet());
        if (uniqueIds.size() != MC_117_DESCRIPTORS.size()) {
            throw new IllegalArgumentException("MC_117_DESCRIPTORS has duplicate IDs");
        }
        uniqueIds = stream(MC_121_BIOME_ORDER).filter(i -> i != -1).boxed().collect(toSet());
        if (uniqueIds.size() != stream(MC_121_BIOME_ORDER).filter(i -> i != -1).count()) {
            throw new IllegalArgumentException("MC_120_BIOME_ORDER has duplicate IDs");
        }
        uniqueIds = MC_121_DESCRIPTORS.stream().map(d -> d.id).collect(toSet());
        if (uniqueIds.size() != MC_121_DESCRIPTORS.size()) {
            throw new IllegalArgumentException("MC_120_DESCRIPTORS has duplicate IDs");
        }
    }

    private static final BiomesSet MINECRAFT_1_17_BIOMES = new BiomesSet(MC_117_BIOME_ORDER, MC_117_DESCRIPTORS, Minecraft1_17Biomes.BIOME_NAMES);
    private static final BiomesSet MINECRAFT_1_20_BIOMES = new BiomesSet(MC_121_BIOME_ORDER, new HashSet<>(MC_121_DESCRIPTORS), Minecraft1_21Biomes.BIOME_NAMES);

    public enum BiomeOption {HILLS, SHORE, EDGE, PLATEAU, MOUNTAINOUS, VARIANT, FROZEN, SNOWY, DEEP, WOODED, WARM,
        LUKEWARM, COLD, TALL, FLOWERS, LAKES, GRAVELLY, SHATTERED, SMALL_ISLANDS, MIDLANDS, HIGHLANDS, BARRENS,
        MODIFIED, ERODED, WINDSWEPT, SPARSE, OLD_GROWTH}

    public static class BiomeDescriptor {
        public BiomeDescriptor(int id) {
            this(id, id);
        }

        public BiomeDescriptor(int id, int baseId, BiomeOption... options) {
            this.id = id;
            this.baseId = baseId;
            this.options = ((options != null) && (options.length > 0)) ? EnumSet.copyOf(asList(options)) : emptySet();
        }

        public int getId() {
            return id;
        }

        public int getBaseId() {
            return baseId;
        }

        public Set<BiomeOption> getOptions() {
            return options;
        }

        private final int id, baseId;
        private final Set<BiomeOption> options;
    }

    public interface Listener {
        void biomeSelected(int biomeId);
    }

    static class BiomesSet {
        BiomesSet(int[] biomeOrder, Set<BiomeDescriptor> descriptors, String[] displayNames) {
            this.biomeOrder = biomeOrder;
            this.descriptors = descriptors;
            this.displayNames = displayNames;
        }

        final int[] biomeOrder;
        final Set<BiomeDescriptor> descriptors;
        final String[] displayNames;
    }
}