package org.pepsoft.worldpainter.biomeschemes;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static org.pepsoft.minecraft.Constants.MC_PLAINS;

/**
 * A combination of Minecraft 1.17 and earlier biomes with Minecraft 1.18+ biomes, providing continuity between them.
 * Technical string IDs for both are provided. For Minecraft 1.18+-only biomes, high numerical IDs have been
 * synthesised. Display names for biomes that occur in Minecraft 1.18 are those for Minecraft 1.18, for legacy biomes
 * they are those from Minecraft 1.17.
 */
public interface Minecraft1_20Biomes extends Minecraft1_17Biomes {
    int BIOME_WINDSWEPT_HILLS = 3;

    int BIOME_SNOWY_PLAINS = 12;

    int BIOME_SPARSE_JUNGLE = 23;
    int BIOME_STONY_SHORE = 25;

    int BIOME_OLD_GROWTH_PINE_TAIGA = 32;
    int BIOME_WINDSWEPT_FOREST = 34;
    int BIOME_WOODED_BADLANDS = 38;

    int BIOME_WINDSWEPT_GRAVELLY_HILLS = 131;

    int BIOME_OLD_GROWTH_BIRCH_FOREST = 155;

    int BIOME_OLD_GROWTH_SPRUCE_TAIGA = 160;
    int BIOME_WINDSWEPT_SAVANNA = 163;

    int BIOME_CHERRY_GROVE = 246;
    int BIOME_MANGROVE_SWAMP = 247;
    int BIOME_DEEP_DARK = 248;
    int BIOME_FROZEN_PEAKS = 249;
    int BIOME_GROVE = 250;
    int BIOME_JAGGED_PEAKS = 251;
    int BIOME_MEADOW = 252;
    int BIOME_SNOWY_SLOPES = 253;
    int BIOME_STONY_PEAKS = 254;

    int HIGHEST_BIOME_ID = BIOME_STONY_PEAKS;

    /**
     * Display names of the biomes,
     */
    String[] BIOME_NAMES = {
            "\u6d77\u6d0b",
            "\u5e73\u539f",
            "\u6c99\u6f20",
            "\u98CE\u88AD\u68EE\u6797",
            "\u68ee\u6797",
            "\u9488\u53f6\u6797",
            "\u6cbc\u6cfd",
            "\u6cb3\u6d41",
            "\u4e0b\u754c\u8352\u5730",
            "\u672b\u5730",

            "\u51bb\u6d0b",
            "\u51bb\u6cb3",
            "\u96EA\u539F",
            "\u96EA\u5C71",
            "\u8611\u83c7\u5c9b",
            "\u8611\u83c7\u5c9b\u5CB8",
            "\u6d77\u6ee9",
            "\u6c99\u6f20\u4e18\u9675",
            "\u7E41\u8302\u7684\u4E18\u9675",
            "\u9488\u53f6\u6797\u4e18\u9675",

            "\u5c71\u5730\u8fb9\u7f18",
            "\u4e1b\u6797",
            "\u4e1b\u6797\u4e18\u9675",
            "\u7A00\u758F\u4E1B\u6797",
            "\u6df1\u6d77",
            "\u77F3\u5CB8",
            "\u79EF\u96EA\u6C99\u6EE9",
            "\u6866\u6728\u68ee\u6797",
            "\u6866\u6728\u68ee\u6797\u4e18\u9675",
            "\u9ED1\u68EE\u6797",

            "\u79EF\u96EA\u9488\u53F6\u6797",
            "\u79EF\u96EA\u9488\u53F6\u6797\u5C71\u4E18",
            "\u539F\u59CB\u677E\u6728\u9488\u53F6\u6797",
            "\u5de8\u578b\u9488\u53f6\u6797\u4e18\u9675",
            "\u98CE\u88AD\u68EE\u6797",
            "\u70ed\u5e26\u8349\u539f",
            "\u70ed\u5e26\u9ad8\u539f",
            "\u6076\u5730",
            "\u7E41\u8302\u6076\u5730",
            "\u6076\u5730\u9ad8\u539f",

            "\u672b\u5730\u5c0f\u578b\u5c9b\u5c7f",
            "\u672b\u5730\u5185\u9646",
            "\u672b\u5730\u9ad8\u5730",
            "\u672b\u5730\u8352\u5730",
            "\u6696\u6c34\u6d77\u6d0b",
            "\u6e29\u6c34\u6d77\u6d0b",
            "\u51b7\u6c34\u6d77\u6d0b",
            "\u6696\u6c34\u6df1\u6d77",
            "\u6e29\u6c34\u6df1\u6d77",
            "\u51b7\u6c34\u6df1\u6d77",

            "\u51b0\u51bb\u6df1\u6d77",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,

            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,

            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,

            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,

            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,

            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,

            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,

            null,
            null,
            null,
            null,
            null,
            null,
            null,
            "\u865A\u7A7A",
            null,
            "\u5411\u65e5\u8475\u5e73\u539f",

            "\u6c99\u6f20\u6e56\u6cca",
            "\u98CE\u88AD\u7802\u783E\u4E18\u9675",
            "\u7e41\u82b1\u68ee\u6797",
            "\u9488\u53f6\u6797\u5c71\u5730",
            "\u6cbc\u6cfd\u5c71\u5730",
            null,
            null,
            null,
            null,
            null,

            "\u51b0\u523a\u4e4b\u5730",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            "\u4e1b\u6797\u53d8\u79cd",

            null,
            "\u4e1b\u6797\u8fb9\u7f18\u53d8\u79cd",
            null,
            null,
            null,
            "\u539F\u59CB\u6866\u6728\u68EE\u6797",
            "\u9ad8\u5927\u6866\u6728\u4e18\u9675",
            "\u9ed1\u68ee\u6797\u4e18\u9675",
            "\u79ef\u96ea\u7684\u9488\u53f6\u6797\u5c71\u5730",
            null,

            "\u539F\u59CB\u4E91\u6749\u9488\u53F6\u6797",
            "\u5de8\u578b\u4e91\u6749\u9488\u53f6\u6797\u4e18\u9675",
            "\u7802\u783e\u5c71\u5730\u53d8\u79cd",
            "\u98CE\u88AD\u70ED\u5E26\u8349\u539F",
            "\u7834\u788e\u7684\u70ed\u5e26\u9ad8\u539f",
            "\u98ce\u8680\u6076\u5730",
            "\u7e41\u8302\u7684\u6076\u5730\u9ad8\u539f\u53d8\u79cd",
            "\u6076\u5730\u9ad8\u539f\u53d8\u79cd",
            "\u7af9\u6797",
            "\u7af9\u6797\u4e18\u9675",

            "\u7075\u9b42\u6c99\u5ce1\u8c37",
            "\u7eef\u7ea2\u68ee\u6797",
            "\u8be1\u5f02\u68ee\u6797",
            "\u7384\u6b66\u5ca9\u4e09\u89d2\u6d32",
            "\u6eb6\u6d1e",
            "\u7e41\u8302\u6d1e\u7a74",
            null,
            null,
            null,
            null,

            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,

            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,

            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,

            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,

            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,

            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,

            null,
            null,
            null,
            null,
            null,
            null,
            "\u6A31\u82B1\u6811\u6797",
            "\u7EA2\u6811\u6797\u6CBC\u6CFD",
            "\u6DF1\u6697\u4E4B\u57DF",
            "\u51B0\u5C01\u5C71\u5CF0",

            "\u96EA\u6797",
            "\u5C16\u5CED\u5C71\u5CF0",
            "\u8349\u7538",
            "\u79EF\u96EA\u5C71\u5761",
            "\u88F8\u5CA9\u5C71\u5CF0",
            // Add 1.18+ biomes that have no numerical ID from the end, replacing nulls above here. This is to minimise
            // the chance of collisions with IDs used as custom biomes for Minecraft 1.17- maps.
            null // Automatic biome/default value of Biome layer; don't map to biome to prevent confusion
    };

    /**
     * Minecraft 1.18+ technical names of the biomes as stored on disk, indexed by the legacy numerical ID with which
     * they correspond, if any. <strong>Note</strong> that because biomes have been consolidated, some modern IDs can
     * map to multiple indices!
     */
    String[] MODERN_IDS = {
            "minecraft:ocean",
            MC_PLAINS,
            "minecraft:desert",
            "minecraft:windswept_hills",
            "minecraft:forest",
            "minecraft:taiga",
            "minecraft:swamp",
            "minecraft:river",
            "minecraft:nether_wastes",
            "minecraft:the_end",

            "minecraft:frozen_ocean",
            "minecraft:frozen_river",
            "minecraft:snowy_plains",
            "minecraft:snowy_plains",
            "minecraft:mushroom_fields",
            "minecraft:mushroom_fields",
            "minecraft:beach",
            "minecraft:desert",
            "minecraft:forest",
            "minecraft:taiga",

            "minecraft:windswept_hills",
            "minecraft:jungle",
            "minecraft:jungle",
            "minecraft:sparse_jungle",
            "minecraft:deep_ocean",
            "minecraft:stony_shore",
            "minecraft:snowy_beach",
            "minecraft:birch_forest",
            "minecraft:birch_forest",
            "minecraft:dark_forest",

            "minecraft:snowy_taiga",
            "minecraft:snowy_taiga",
            "minecraft:old_growth_pine_taiga",
            "minecraft:old_growth_pine_taiga",
            "minecraft:windswept_forest",
            "minecraft:savanna",
            "minecraft:savanna_plateau",
            "minecraft:badlands",
            "minecraft:wooded_badlands",
            "minecraft:badlands",

            "minecraft:small_end_islands",
            "minecraft:end_midlands",
            "minecraft:end_highlands",
            "minecraft:end_barrens",
            "minecraft:warm_ocean",
            "minecraft:lukewarm_ocean",
            "minecraft:cold_ocean",
            "minecraft:warm_ocean",
            "minecraft:deep_lukewarm_ocean",
            "minecraft:deep_cold_ocean",

            "minecraft:deep_frozen_ocean",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,

            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,

            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,

            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,

            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,

            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,

            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,

            null,
            null,
            null,
            null,
            null,
            null,
            null,
            "minecraft:the_void",
            null,
            "minecraft:sunflower_plains",

            "minecraft:desert",
            "minecraft:windswept_gravelly_hills",
            "minecraft:flower_forest",
            "minecraft:taiga",
            "minecraft:swamp",
            null,
            null,
            null,
            null,
            null,

            "minecraft:ice_spikes",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            "minecraft:jungle",

            null,
            "minecraft:sparse_jungle",
            null,
            null,
            null,
            "minecraft:old_growth_birch_forest",
            "minecraft:old_growth_birch_forest",
            "minecraft:dark_forest",
            "minecraft:snowy_taiga",
            null,

            "minecraft:old_growth_spruce_taiga",
            "minecraft:old_growth_spruce_taiga",
            "minecraft:windswept_gravelly_hills",
            "minecraft:windswept_savanna",
            "minecraft:windswept_savanna",
            "minecraft:eroded_badlands",
            "minecraft:wooded_badlands",
            "minecraft:badlands",
            "minecraft:bamboo_jungle",
            "minecraft:bamboo_jungle",

            "minecraft:soul_sand_valley",
            "minecraft:crimson_forest",
            "minecraft:warped_forest",
            "minecraft:basalt_deltas",
            "minecraft:dripstone_caves",
            "minecraft:lush_caves",
            null,
            null,
            null,
            null,

            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,

            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,

            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,

            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,

            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,

            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,

            null,
            null,
            null,
            null,
            null,
            null,
            "minecraft:cherry_grove",
            "minecraft:mangrove_swamp",
            "minecraft:deep_dark",
            "minecraft:frozen_peaks",

            "minecraft:grove",
            "minecraft:jagged_peaks",
            "minecraft:meadow",
            "minecraft:snowy_slopes",
            "minecraft:stony_peaks",
            // Add 1.18+ biomes that have no numerical ID from the end, replacing nulls above here. This is to minimise
            // the chance of collisions with IDs used as custom biomes for Minecraft 1.17- maps.
            null
    };

    Map<String, Integer> BIOMES_BY_MODERN_ID = StaticInitialiser.biomesByModernId();

    boolean[][][] BIOME_PATTERNS = StaticInitialiser.loadPatterns();

    class StaticInitialiser {
        @SuppressWarnings("ConstantConditions") // Our responsibility
        private static boolean[][][] loadPatterns() {
            boolean[][][] patterns = new boolean[256][][];
            try {
                BufferedImage image = ImageIO.read(ClassLoader.getSystemResourceAsStream("org/pepsoft/worldpainter/icons/swamp_pattern.png"));
                patterns[BIOME_SWAMPLAND] = createPattern(image);
                patterns[BIOME_SWAMPLAND_M] = createPattern(image);

                image = ImageIO.read(ClassLoader.getSystemResourceAsStream("org/pepsoft/worldpainter/icons/mountains_pattern.png"));
                patterns[BIOME_EXTREME_HILLS] = createPattern(image);
                patterns[BIOME_EXTREME_HILLS_M] = createPattern(image);
                patterns[BIOME_EXTREME_HILLS_PLUS] = createPattern(image);
                patterns[BIOME_EXTREME_HILLS_PLUS_M] = createPattern(image);
                patterns[BIOME_ICE_MOUNTAINS] = createPattern(image);
                patterns[BIOME_END_HIGHLANDS] = createPattern(image);
                patterns[BIOME_FROZEN_PEAKS] = createPattern(image);
                patterns[BIOME_JAGGED_PEAKS] = createPattern(image);
                patterns[BIOME_STONY_PEAKS] = createPattern(image);

                image = ImageIO.read(ClassLoader.getSystemResourceAsStream("org/pepsoft/worldpainter/icons/deciduous_trees_pattern.png"));
                patterns[BIOME_FOREST] = createPattern(image);
                patterns[BIOME_FLOWER_FOREST] = createPattern(image);
                patterns[BIOME_CRIMSON_FOREST] = createPattern(image);
                patterns[BIOME_WARPED_FOREST] = createPattern(image);

                image = ImageIO.read(ClassLoader.getSystemResourceAsStream("org/pepsoft/worldpainter/icons/roofed_trees_pattern.png"));
                patterns[BIOME_ROOFED_FOREST] = createPattern(image);

                image = ImageIO.read(ClassLoader.getSystemResourceAsStream("org/pepsoft/worldpainter/icons/birch_trees_pattern.png"));
                patterns[BIOME_BIRCH_FOREST] = createPattern(image);

                image = ImageIO.read(ClassLoader.getSystemResourceAsStream("org/pepsoft/worldpainter/icons/spruce_trees_pattern.png"));
                patterns[BIOME_TAIGA] = createPattern(image);
                patterns[BIOME_COLD_TAIGA] = createPattern(image);

                image = ImageIO.read(ClassLoader.getSystemResourceAsStream("org/pepsoft/worldpainter/icons/pine_trees_pattern.png"));
                patterns[BIOME_MEGA_TAIGA] = createPattern(image);
                patterns[BIOME_MEGA_SPRUCE_TAIGA] = createPattern(image);

                image = ImageIO.read(ClassLoader.getSystemResourceAsStream("org/pepsoft/worldpainter/icons/hills_pattern.png"));
                patterns[BIOME_DESERT_HILLS] = createPattern(image);
                patterns[BIOME_EXTREME_HILLS_EDGE] = createPattern(image);
                patterns[BIOME_END_MIDLANDS] = createPattern(image);
                patterns[BIOME_SNOWY_SLOPES] = createPattern(image);

                image = ImageIO.read(ClassLoader.getSystemResourceAsStream("org/pepsoft/worldpainter/icons/deciduous_hills_pattern.png"));
                patterns[BIOME_FOREST_HILLS] = createPattern(image);

                image = ImageIO.read(ClassLoader.getSystemResourceAsStream("org/pepsoft/worldpainter/icons/roofed_hills_pattern.png"));
                patterns[BIOME_ROOFED_FOREST_M] = createPattern(image);

                image = ImageIO.read(ClassLoader.getSystemResourceAsStream("org/pepsoft/worldpainter/icons/birch_hills_pattern.png"));
                patterns[BIOME_BIRCH_FOREST_M] = createPattern(image);
                patterns[BIOME_BIRCH_FOREST_HILLS] = createPattern(image);
                patterns[BIOME_BIRCH_FOREST_HILLS_M] = createPattern(image);

                image = ImageIO.read(ClassLoader.getSystemResourceAsStream("org/pepsoft/worldpainter/icons/spruce_hills_pattern.png"));
                patterns[BIOME_TAIGA_M] = createPattern(image);
                patterns[BIOME_TAIGA_HILLS] = createPattern(image);
                patterns[BIOME_COLD_TAIGA_HILLS] = createPattern(image);
                patterns[BIOME_COLD_TAIGA_M] = createPattern(image);
                patterns[BIOME_GROVE] = createPattern(image);

                image = ImageIO.read(ClassLoader.getSystemResourceAsStream("org/pepsoft/worldpainter/icons/pine_hills_pattern.png"));
                patterns[BIOME_MEGA_TAIGA_HILLS] = createPattern(image);
                patterns[BIOME_MEGA_SPRUCE_TAIGA_HILLS] = createPattern(image);

                image = ImageIO.read(ClassLoader.getSystemResourceAsStream("org/pepsoft/worldpainter/icons/jungle_trees_pattern.png"));
                patterns[BIOME_JUNGLE] = createPattern(image);

                image = ImageIO.read(ClassLoader.getSystemResourceAsStream("org/pepsoft/worldpainter/icons/bamboo_pattern.png"));
                patterns[BIOME_BAMBOO_JUNGLE] = createPattern(image);

                image = ImageIO.read(ClassLoader.getSystemResourceAsStream("org/pepsoft/worldpainter/icons/jungle_hills_pattern.png"));
                patterns[BIOME_JUNGLE_HILLS] = createPattern(image);
                patterns[BIOME_JUNGLE_M] = createPattern(image);

                image = ImageIO.read(ClassLoader.getSystemResourceAsStream("org/pepsoft/worldpainter/icons/bamboo_hills_pattern.png"));
                patterns[BIOME_BAMBOO_JUNGLE_HILLS] = createPattern(image);

                image = ImageIO.read(ClassLoader.getSystemResourceAsStream("org/pepsoft/worldpainter/icons/savanna_pattern.png"));
                patterns[BIOME_SAVANNA] = createPattern(image);
                patterns[BIOME_SAVANNA_PLATEAU] = createPattern(image);

                image = ImageIO.read(ClassLoader.getSystemResourceAsStream("org/pepsoft/worldpainter/icons/savanna_hills_pattern.png"));
                patterns[BIOME_SAVANNA_M] = createPattern(image);
                patterns[BIOME_SAVANNA_PLATEAU_M] = createPattern(image);

                image = ImageIO.read(ClassLoader.getSystemResourceAsStream("org/pepsoft/worldpainter/icons/mesa_pattern.png"));
                patterns[BIOME_MESA] = createPattern(image);
                patterns[BIOME_MESA_PLATEAU] = createPattern(image);
                patterns[BIOME_MESA_PLATEAU_F] = createPattern(image);
                patterns[BIOME_MESA_PLATEAU_F_M] = createPattern(image);
                patterns[BIOME_MESA_PLATEAU_M] = createPattern(image);

                image = ImageIO.read(ClassLoader.getSystemResourceAsStream("org/pepsoft/worldpainter/icons/mesa_bryce_pattern.png"));
                patterns[BIOME_MESA_BRYCE] = createPattern(image);

                image = ImageIO.read(ClassLoader.getSystemResourceAsStream("org/pepsoft/worldpainter/icons/jungle_edge_pattern.png"));
                patterns[BIOME_JUNGLE_EDGE] = createPattern(image);

                image = ImageIO.read(ClassLoader.getSystemResourceAsStream("org/pepsoft/worldpainter/icons/jungle_edge_hills_pattern.png"));
                patterns[BIOME_JUNGLE_EDGE_M] = createPattern(image);

                image = ImageIO.read(ClassLoader.getSystemResourceAsStream("org/pepsoft/worldpainter/icons/ice_spikes.png"));
                patterns[BIOME_ICE_SPIKES] = createPattern(image);

                image = ImageIO.read(ClassLoader.getSystemResourceAsStream("org/pepsoft/worldpainter/icons/mangrove_trees_pattern.png"));
                patterns[BIOME_MANGROVE_SWAMP] = createPattern(image);

                image = ImageIO.read(ClassLoader.getSystemResourceAsStream("org/pepsoft/worldpainter/icons/cherry_grove_pattern.png"));
                patterns[BIOME_CHERRY_GROVE] = createPattern(image);

                image = ImageIO.read(ClassLoader.getSystemResourceAsStream("org/pepsoft/worldpainter/icons/basalt_deltas_pattern.png"));
                patterns[BIOME_BASALT_DELTAS] = createPattern(image);
            } catch (IOException e) {
                throw new RuntimeException("I/O error loading image", e);
            }
            return patterns;
        }

        private static boolean[][] createPattern(BufferedImage image) {
            boolean[][] pattern = new boolean[16][];
            for (int x = 0; x < 16; x++) {
                pattern[x] = new boolean[16];
                for (int y = 0; y < 16; y++) {
                    pattern[x][y] = image.getRGB(x, y) != -1;
                }
            }
            return pattern;
        }

        private static Map<String, Integer> biomesByModernId() {
            Map<String, Integer> biomesByModernId = new HashMap<>();
            for (int i = 0; i < 256; i++) {
                if ((MODERN_IDS[i] != null) && (! biomesByModernId.containsKey(MODERN_IDS[i]))) {
                    biomesByModernId.put(MODERN_IDS[i], i);
                }
            }
            return unmodifiableMap(biomesByModernId);
        }
    }
}