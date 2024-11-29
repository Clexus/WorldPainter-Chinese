package org.pepsoft.worldpainter.tools;

import org.pepsoft.worldpainter.*;
import org.pepsoft.worldpainter.layers.Layer;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

import static org.pepsoft.worldpainter.Constants.DIM_NORMAL;

/**
 * Created by Pepijn Schmitz on 21-09-15.
 */
public class DumpWorld {
    public static void main(String[] args) throws IOException, UnloadableWorldException {
        WorldIO worldIO = new WorldIO();
        worldIO.load(new FileInputStream(args[0]));
        World2 world = worldIO.getWorld();
        System.out.println("\u4E16\u754C\u540D: " + world.getName());
        if (world.getMetadata() != null) {
            world.getMetadata().entrySet().forEach(entry -> System.out.println("   " + entry.getKey() + ": " + entry.getValue()));
        }
        System.out.println("\u5386\u53F2:");
        world.getHistory().forEach(entry -> System.out.println("    " + entry.getText()));
        if (world.getPlatform() != null) {
            System.out.printf("\u4E0A\u6B21\u5BFC\u51FA\u7684\u5E73\u53F0: %s%n", world.getPlatform().displayName);
        }
        System.out.println("\u6E38\u620F\u6A21\u5F0F: " + world.getGameType());
        System.out.println("\u96BE\u5EA6: " + DIFFICULTIES[world.getDifficulty()]);
        if (world.getImportedFrom() != null) {
            System.out.println("\u5BFC\u5165\u81EA: " + world.getImportedFrom());
        }
        System.out.println("\u6700\u5927\u9AD8\u5EA6: " + world.getMaxHeight());
        System.out.println("\u51FA\u751F\u70B9: " + world.getSpawnPoint().y + "," + world.getSpawnPoint().y);
        System.out.println("\u4E0A\u65B9\u4E3A: " + world.getUpIs());

        boolean headerPrinted = false;
        for (int i = 0; i < Terrain.CUSTOM_TERRAIN_COUNT; i++) {
            MixedMaterial customTerrain = world.getMixedMaterial(i);
            if (customTerrain != null) {
                if (! headerPrinted) {
                    System.out.println("\u5B89\u88C5\u7684\u81EA\u5B9A\u4E49\u65B9\u5757:");
                    headerPrinted = true;
                }
                System.out.println("    \u81EA\u5B9A\u4E49\u65B9\u5757 " + (i + 1));
                System.out.println("        \u540D\u79F0: " + customTerrain.getName());
                System.out.println("        \u6A21\u5F0F: " + customTerrain.getMode());
                Arrays.stream(customTerrain.getRows()).forEach(row -> System.out.println("        \u6750\u8D28: " + row));
            }
        }
        if (! headerPrinted) {
            System.out.println("\u6CA1\u6709\u5B89\u88C5\u81EA\u5B9A\u4E49\u65B9\u5757");
        }

        for (Dimension dimension: world.getDimensions()) {
            dumpDimension(dimension);
        }
    }

    private static void dumpDimension(Dimension dimension) {
        System.out.println("\u7EF4\u5EA6: " + dimension.getName() + " (\u5E8F\u53F7: " + dimension.getAnchor().dim + ")");
        System.out.println("    \u5927\u5C0F: " + dimension.getWidth() + "x" + dimension.getHeight() + " \u4E2A\u5206\u533A");
        System.out.println("    \u6700\u897F\u5206\u533A: " + dimension.getLowestX() + "; \u6700\u4E1C\u5206\u533A: " + dimension.getHighestX());
        System.out.println("    \u6700\u5317\u5206\u533A: " + dimension.getLowestY() + "; \u6700\u5357\u5206\u533A: " + dimension.getHighestY());
        System.out.println("    \u5206\u533A\u603B\u6570: " + dimension.getTileCount());
        System.out.println("    \u751F\u6210\u5668: " + dimension.getGenerator());
        System.out.println("    WorldPainter \u79CD\u5B50: " + dimension.getSeed() + "; Minecraft \u79CD\u5B50: " + dimension.getMinecraftSeed());
        if (dimension.getBorder() != null) {
            if (dimension.getBorder().isEndless()) {
                switch (dimension.getBorder()) {
                    case ENDLESS_LAVA:
                    case ENDLESS_WATER:
                        System.out.println("    \u8FB9\u754C: " + dimension.getBorder() + " (\u9AD8\u5EA6; " + dimension.getBorderLevel() + ")");
                        break;
                    default:
                        System.out.println("    \u8FB9\u754C: " + dimension.getBorder());
                        break;
                }
            } else {
                switch (dimension.getBorder()) {
                    case LAVA:
                    case WATER:
                        System.out.println("    \u8FB9\u754C: " + dimension.getBorder() + " (\u5927\u5C0F: " + dimension.getBorderSize() + "; \u9AD8\u5EA6; " + dimension.getBorderLevel() + ")");
                        break;
                    default:
                        System.out.println("    \u8FB9\u754C: " + dimension.getBorder() + " (\u5927\u5C0F: " + dimension.getBorderSize() + ")");
                        break;
                }
            }
        } else {
            System.out.println("    \u8FB9\u754C: \u65E0");
        }
        if (dimension.getAnchor().invert) {
            System.out.println("    \u9876\u5C42\u9AD8\u5EA6: " + dimension.getCeilingHeight());
        }
        System.out.println("    \u6700\u5927\u9AD8\u5EA6: " + dimension.getMaxHeight());
        System.out.println("    \u7B49\u9AD8\u7EBF\u5206\u79BB: " + dimension.getContourSeparation());
        System.out.println("    \u7F51\u683C\u5927\u5C0F: " + dimension.getGridSize());
        System.out.println("    \u4E0A\u6B21\u89C6\u56FE\u4F4D\u7F6E: " + dimension.getLastViewPosition().x + "," + dimension.getLastViewPosition().y);
        for (Overlay overlay: dimension.getOverlays()) {
            System.out.println("    \u8986\u76D6\u56FE: " + overlay.getFile());
            System.out.println("        \u542F\u7528\u72B6\u6001: " + overlay.isEnabled());
            System.out.println("        \u504F\u79FB: " + overlay.getOffsetX() + "," + overlay.getOffsetY());
            System.out.println("        \u6BD4\u4F8B:" + overlay.getScale());
            System.out.println("        \u900F\u660E\u5EA6: " + overlay.getTransparency());
        }
        System.out.println("    \u5730\u4E0B\u6750\u8D28: " + dimension.getSubsurfaceMaterial());
        System.out.println("    \u9876\u5C42\u8986\u76D6\u5C42\u6DF1\u5EA6: " + dimension.getTopLayerMinDepth() + " - " + (dimension.getTopLayerMinDepth() + dimension.getTopLayerVariation()));

        Map<Layer, Integer> usedLayers = new HashMap<>();
        EnumSet<Terrain> terrainsUsed = EnumSet.noneOf(Terrain.class);
        float lowestSurface = Float.MAX_VALUE, highestSurface = -Float.MAX_VALUE;
        int lowestWaterlevel = Integer.MAX_VALUE, highestWaterlevel = Integer.MIN_VALUE;
        for (Tile tile: dimension.getTiles()) {
            for (Layer layer: tile.getLayers()) {
                Integer count = usedLayers.get(layer);
                if (count == null) {
                    usedLayers.put(layer, 1);
                } else {
                    usedLayers.put(layer, count + 1);
                }
            }
            for (int x = 0; x < Constants.TILE_SIZE; x++) {
                for (int y = 0; y < Constants.TILE_SIZE; y++) {
                    terrainsUsed.add(tile.getTerrain(x, y));
                    float height = tile.getHeight(x, y);
                    if (height < lowestSurface) {
                        lowestSurface = height;
                    }
                    if (height > highestSurface) {
                        highestSurface = height;
                    }
                    int waterLevel = tile.getWaterLevel(x, y);
                    if (waterLevel < lowestWaterlevel) {
                        lowestWaterlevel = waterLevel;
                    }
                    if (waterLevel > highestWaterlevel) {
                        highestWaterlevel = waterLevel;
                    }
                }
            }
        }
        System.out.println("    \u65B9\u5757\u9AD8\u5EA6: " + lowestSurface + " - " + highestSurface);
        System.out.println("    \u6C34\u5E73\u9762\u9AD8\u5EA6: " + lowestWaterlevel + " - " + highestWaterlevel);
        if ((dimension.getCustomBiomes() != null) && (! dimension.getCustomBiomes().isEmpty())) {
            System.out.println("    \u5B89\u88C5\u7684\u81EA\u5B9A\u4E49\u7FA4\u7CFB:");
            dimension.getCustomBiomes().forEach(customBiome -> System.out.println("        " + customBiome.getName() + " (" + customBiome.getId() + ")"));
        } else {
            System.out.println("    \u6CA1\u6709\u5B89\u88C5\u81EA\u5B9A\u4E49\u7FA4\u7CFB");
        }
        System.out.println("    \u4F7F\u7528\u7684\u8986\u76D6\u5C42:");
        usedLayers.entrySet().forEach(entry -> {
            Layer layer = entry.getKey();
            System.out.println("        \u540D\u79F0: " + layer.getName());
            System.out.println("            \u7C7B\u578B: " + layer.getClass().getSimpleName());
            System.out.println("            \u6570\u636E\u5927\u5C0F: " + layer.getDataSize());
            System.out.println("            \u5206\u533A\u6570\u91CF: " + entry.getValue());
        });
        Set<Layer> unusedLayers = new HashSet<>(dimension.getCustomLayers());
        unusedLayers.removeAll(usedLayers.keySet());
        if (! unusedLayers.isEmpty()) {
            System.out.println("    \u5DF2\u50A8\u5B58\u4F46\u672A\u4F7F\u7528\u7684\u8986\u76D6\u5C42:");
            unusedLayers.forEach(layer -> {
                System.out.println("        \u540D\u79F0: " + layer.getName());
                System.out.println("            \u79CD\u7C7B: " + layer.getClass().getSimpleName());
                System.out.println("            \u6570\u636E\u5927\u5C0F: " + layer.getDataSize());
            });
        }

        System.out.println("    \u5DF2\u4F7F\u7528\u7684\u65B9\u5757\u79CD\u7C7B:");
        terrainsUsed.forEach(terrain -> System.out.println("        " + terrain.getName() + " (\u5E8F\u53F7: " + terrain.ordinal() + ")"));

        List<String> problems = new ArrayList<>();
        if ((dimension.getAnchor().dim != DIM_NORMAL) && (dimension.getMaxHeight() != dimension.getWorld().getMaxHeight())) {
            problems.add("\u7EF4\u5EA6\u6700\u5927\u9AD8\u5EA6 (" + dimension.getMaxHeight() + ") \u4E0D\u7B49\u4E8E\u4E16\u754C\u6700\u5927\u9AD8\u5EA6 (" + dimension.getWorld().getMaxHeight() + ")");
        }
        for (Terrain terrain: terrainsUsed) {
            if (terrain.isCustom() && (! terrain.isConfigured())) {
                problems.add("\u53D1\u73B0\u672A\u914D\u7F6E\u7684\u81EA\u5B9A\u4E49\u65B9\u5757\u7C7B\u578B " + terrain.getName() + " (\u5E8F\u53F7: " + terrain.ordinal() + ")");
            }
        }
        if (! problems.isEmpty()) {
            System.out.println("    \u51FA\u73B0\u95EE\u9898:");
            problems.forEach(problem -> System.out.println("        " + problem));
        }

        // TODO: layer settings

        // TODO: tile factory
    }

    private static final String[] GAME_TYPES = {"\u751F\u5B58\u6A21\u5F0F", "\u521B\u9020\u6A21\u5F0F", "\u5192\u9669\u6A21\u5F0F", "\u6781\u9650\u6A21\u5F0F"};
    private static final String[] DIFFICULTIES = {"\u548C\u5E73", "\u7B80\u5355", "\u6B63\u5E38", "\u56F0\u96BE"};
}