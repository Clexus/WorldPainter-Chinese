package org.pepsoft.worldpainter.biomeschemes;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * All Minecraft 1.17 biome IDs, plus names and other info.
 */
public interface Minecraft1_17Biomes extends Minecraft1_7Biomes {
    int BIOME_MOUNTAINS = 3;
    int BIOME_SWAMP = 6;
    int BIOME_NETHER_WASTES = 8;
    int BIOME_THE_END = 9;

    int BIOME_SNOWY_TUNDRA = 12;
    int BIOME_SNOWY_MOUNTAINS = 13;
    int BIOME_MUSHROOM_FIELDS = 14;
    int BIOME_MUSHROOM_FIELD_SHORE = 15;
    int BIOME_WOODED_HILLS = 18;

    int BIOME_MOUNTAIN_EDGE = 20;
    int BIOME_STONE_SHORE = 25;
    int BIOME_SNOWY_BEACH = 26;
    int BIOME_DARK_FOREST = 29;

    int BIOME_SNOWY_TAIGA = 30;
    int BIOME_SNOWY_TAIGA_HILLS = 31;
    int BIOME_GIANT_TREE_TAIGA = 32;
    int BIOME_GIANT_TREE_TAIGA_HILLS = 33;
    int BIOME_WOODED_MOUNTAINS = 34;
    int BIOME_BADLANDS = 37;
    int BIOME_WOODED_BADLANDS_PLATEAU = 38;
    int BIOME_BADLANDS_PLATEAU = 39;

    int BIOME_SMALL_END_ISLANDS = 40;
    int BIOME_END_MIDLANDS = 41;
    int BIOME_END_HIGHLANDS = 42;
    int BIOME_END_BARRENS = 43;
    int BIOME_WARM_OCEAN = 44;
    int BIOME_LUKEWARM_OCEAN = 45;
    int BIOME_COLD_OCEAN = 46;
    int BIOME_DEEP_WARM_OCEAN = 47;
    int BIOME_DEEP_LUKEWARM_OCEAN = 48;
    int BIOME_DEEP_COLD_OCEAN = 49;

    int BIOME_DEEP_FROZEN_OCEAN = 50;

    int BIOME_THE_VOID = 127;

    int BIOME_DESERT_LAKES = 130;
    int BIOME_GRAVELLY_MOUNTAINS = 131;
    int BIOME_TAIGA_MOUNTAINS = 133;
    int BIOME_SWAMP_HILLS = 134;

    int BIOME_ICE_SPIKES = 140;
    int BIOME_MODIFIED_JUNGLE = 149;

    int BIOME_MODIFIED_JUNGLE_EDGE = 151;
    int BIOME_TALL_BIRCH_FOREST = 155;
    int BIOME_TALL_BIRCH_HILLS = 156;
    int BIOME_DARK_FOREST_HILLS = 157;
    int BIOME_SNOWY_TAIGA_MOUNTAINS = 158;

    int BIOME_GIANT_SPRUCE_TAIGA = 160;
    int BIOME_GIANT_SPRUCE_TAIGA_HILLS = 161;
    int BIOME_MODIFIED_GRAVELLY_MOUNTAINS = 162;
    int BIOME_SHATTERED_SAVANNA = 163;
    int BIOME_SHATTERED_SAVANNA_PLATEAU = 164;
    int BIOME_ERODED_BADLANDS = 165;
    int BIOME_MODIFIED_WOODED_BADLANDS_PLATEAU = 166;
    int BIOME_MODIFIED_BADLANDS_PLATEAU = 167;
    int BIOME_BAMBOO_JUNGLE = 168;
    int BIOME_BAMBOO_JUNGLE_HILLS = 169;

    int BIOME_SOUL_SAND_VALLEY = 170;
    int BIOME_CRIMSON_FOREST = 171;
    int BIOME_WARPED_FOREST = 172;
    int BIOME_BASALT_DELTAS = 173;
    int BIOME_DRIPSTONE_CAVES = 174;
    int BIOME_LUSH_CAVES = 175;

    int FIRST_UNALLOCATED_ID = BIOME_DEEP_FROZEN_OCEAN + 1;
    int HIGHEST_BIOME_ID = BIOME_LUSH_CAVES;

    String[] BIOME_NAMES = {
            "\u6d77\u6d0b",
            "\u5e73\u539f",
            "\u6c99\u6f20",
            "\u5C71\u5730",
            "\u68ee\u6797",
            "\u9488\u53f6\u6797",
            "\u6cbc\u6cfd",
            "\u6cb3\u6d41",
            "\u4e0b\u754c\u8352\u5730",
            "\u672b\u5730",

            "\u51bb\u6d0b",
            "\u51bb\u6cb3",
            "\u79EF\u96EA\u7684\u51BB\u539F",
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
            "\u4e1b\u6797\u8fb9\u7f18",
            "\u6df1\u6d77",
            "\u77F3\u5CB8",
            "\u79EF\u96EA\u6C99\u6EE9",
            "\u6866\u6728\u68ee\u6797",
            "\u6866\u6728\u68ee\u6797\u4e18\u9675",
            "\u9ED1\u68EE\u6797",

            "\u79EF\u96EA\u9488\u53F6\u6797",
            "\u79EF\u96EA\u9488\u53F6\u6797\u5C71\u4E18",
            "\u5de8\u578b\u9488\u53f6\u6797",
            "\u5de8\u578b\u9488\u53f6\u6797\u4e18\u9675",
            "\u7E41\u8302\u7684\u5C71\u5730",
            "\u70ed\u5e26\u8349\u539f",
            "\u70ed\u5e26\u9ad8\u539f",
            "\u6076\u5730",
            "\u7E41\u8302\u6076\u5730\u9AD8\u539F",
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
            "\u7802\u783E\u5C71\u5730",
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
            "\u9AD8\u5927\u6866\u6728\u68EE\u6797",
            "\u9ad8\u5927\u6866\u6728\u4e18\u9675",
            "\u9ed1\u68ee\u6797\u4e18\u9675",
            "\u79ef\u96ea\u7684\u9488\u53f6\u6797\u5c71\u5730",
            null,

            "\u5DE8\u578B\u4E91\u6749\u9488\u53F6\u6797",
            "\u5de8\u578b\u4e91\u6749\u9488\u53f6\u6797\u4e18\u9675",
            "\u7802\u783e\u5c71\u5730\u53d8\u79cd",
            "\u7834\u788E\u7684\u70ED\u5E26\u8349\u539F",
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
            "\u7e41\u8302\u6d1e\u7a74"
    };

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
    }
}