package org.pepsoft.worldpainter.biomeschemes;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * All Minecraft 1.7 and 1.8 biome IDs, plus names and other info.
 *
 * Created by pepijn on 27-4-15.
 */
public interface Minecraft1_7Biomes {
    int BIOME_OCEAN = 0;
    int BIOME_PLAINS = 1;
    int BIOME_DESERT = 2;
    int BIOME_EXTREME_HILLS = 3;
    int BIOME_FOREST = 4;
    int BIOME_TAIGA = 5;
    int BIOME_SWAMPLAND = 6;
    int BIOME_RIVER = 7;
    int BIOME_HELL = 8;
    int BIOME_SKY = 9;

    int BIOME_FROZEN_OCEAN = 10;
    int BIOME_FROZEN_RIVER = 11;
    int BIOME_ICE_PLAINS = 12;
    int BIOME_ICE_MOUNTAINS = 13;
    int BIOME_MUSHROOM_ISLAND = 14;
    int BIOME_MUSHROOM_ISLAND_SHORE = 15;
    int BIOME_BEACH = 16;
    int BIOME_DESERT_HILLS = 17;
    int BIOME_FOREST_HILLS = 18;
    int BIOME_TAIGA_HILLS = 19;

    int BIOME_EXTREME_HILLS_EDGE = 20;
    int BIOME_JUNGLE = 21;
    int BIOME_JUNGLE_HILLS = 22;
    int BIOME_JUNGLE_EDGE = 23;
    int BIOME_DEEP_OCEAN = 24;
    int BIOME_STONE_BEACH = 25;
    int BIOME_COLD_BEACH = 26;
    int BIOME_BIRCH_FOREST = 27;
    int BIOME_BIRCH_FOREST_HILLS = 28;
    int BIOME_ROOFED_FOREST = 29;

    int BIOME_COLD_TAIGA = 30;
    int BIOME_COLD_TAIGA_HILLS = 31;
    int BIOME_MEGA_TAIGA = 32;
    int BIOME_MEGA_TAIGA_HILLS = 33;
    int BIOME_EXTREME_HILLS_PLUS = 34;
    int BIOME_SAVANNA = 35;
    int BIOME_SAVANNA_PLATEAU = 36;
    int BIOME_MESA = 37;
    int BIOME_MESA_PLATEAU_F = 38;
    int BIOME_MESA_PLATEAU = 39;

    int BIOME_SUNFLOWER_PLAINS = 129;
    int BIOME_DESERT_M = 130;
    int BIOME_EXTREME_HILLS_M = 131;
    int BIOME_FLOWER_FOREST = 132;
    int BIOME_TAIGA_M = 133;
    int BIOME_SWAMPLAND_M = 134;

    int BIOME_ICE_PLAINS_SPIKES = 140;
    int BIOME_JUNGLE_M = 149;

    int BIOME_JUNGLE_EDGE_M = 151;
    int BIOME_BIRCH_FOREST_M = 155;
    int BIOME_BIRCH_FOREST_HILLS_M = 156;
    int BIOME_ROOFED_FOREST_M = 157;
    int BIOME_COLD_TAIGA_M = 158;

    int BIOME_MEGA_SPRUCE_TAIGA = 160;
    int BIOME_MEGA_SPRUCE_TAIGA_HILLS = 161;
    int BIOME_EXTREME_HILLS_PLUS_M = 162;
    int BIOME_SAVANNA_M = 163;
    int BIOME_SAVANNA_PLATEAU_M = 164;
    int BIOME_MESA_BRYCE = 165;
    int BIOME_MESA_PLATEAU_F_M = 166;
    int BIOME_MESA_PLATEAU_M = 167;

    int FIRST_UNALLOCATED_ID = BIOME_MESA_PLATEAU + 1;
    int HIGHEST_BIOME_ID = BIOME_MESA_PLATEAU_M;

    String[] BIOME_NAMES = {
            "\u6d77\u6d0b",
            "\u5e73\u539f",
            "\u6c99\u6f20",
            "\u5ced\u58c1",
            "\u68ee\u6797",
            "\u9488\u53f6\u6797",
            "\u6cbc\u6cfd",
            "\u6cb3\u6d41",
            "\u4e0b\u754c",
            "\u672b\u5730",

            "\u51bb\u6d0b",
            "\u51bb\u6cb3",
            "\u51b0\u539f",
            "\u51b0\u5c71",
            "\u8611\u83c7\u5c9b",
            "\u8611\u83c7\u5c9b\u6d77\u5cb8",
            "\u6d77\u6ee9",
            "\u6c99\u6f20\u4e18\u9675",
            "\u68ee\u6797\u4e18\u9675",
            "\u9488\u53f6\u6797\u4e18\u9675",

            "\u5ced\u58c1\u8fb9\u7f18",
            "\u4e1b\u6797",
            "\u4e1b\u6797\u4e18\u9675",
            "\u4e1b\u6797\u8fb9\u7f18",
            "\u6df1\u6d77",
            "\u77f3\u5cb8",
            "\u79ef\u96ea\u6c99\u6ee9",
            "\u6866\u6728\u68ee\u6797",
            "\u6866\u6728\u68ee\u6797\u4e18\u9675",
            "\u9ed1\u68ee\u6797",

            "\u79ef\u96ea\u9488\u53f6\u6797",
            "\u79ef\u96ea\u9488\u53f6\u6797\u4e18\u9675",
            "\u539f\u59cb\u677e\u6728\u9488\u53f6\u6797",
            "\u5de8\u578b\u9488\u53f6\u6797\u4e18\u9675",
            "\u98ce\u88ad\u4e18\u9675+",
            "\u70ed\u5e26\u8349\u539f",
            "\u70ed\u5e26\u9ad8\u539f",
            "\u6076\u5730",
            "\u6076\u5730\u9ad8\u539fF",
            "\u6076\u5730\u9ad8\u539f",

            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,

            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,

            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,

            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,

            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,

            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,

            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,

            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,

            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            "\u5411\u65e5\u8475\u5e73\u539f",

            "\u6C99\u6F20M",
            "\u98CE\u88AD\u4E18\u9675M",
            "\u7e41\u82b1\u68ee\u6797",
            "\u9488\u53F6\u6797M",
            "\u6CBC\u6CFDM",
            null,
            null,
            null,
            null,
            null,

            "\u51B0\u523A\u4E4B\u5730",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            "\u4E1B\u6797M",

            null,
            "\u4E1B\u6797\u8FB9\u7F18M",
            null,
            null,
            null,
            "\u6866\u6728\u68EE\u6797M",
            "\u6866\u6728\u68EE\u6797\u4E18\u9675M",
            "\u9ED1\u68EE\u6797M",
            "\u79EF\u96EA\u9488\u53F6\u6797M",
            null,

            "\u7EA2\u6728\u68EE\u6797",
            "\u7EA2\u6728\u68EE\u6797\u5C71\u4E18",
            "\u98CE\u88AD\u4E18\u9675+ M",
            "\u70ED\u5E26\u8349\u539F M",
            "\u70ED\u5E26\u9AD8\u539F M",
            "\u5E73\u9876\u5C71 (\u5CA9\u67F1)",
            "\u5E73\u9876\u5C71\u9AD8\u539F F M",
            "\u5E73\u9876\u5C71\u9AD8\u539F M"
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

                image = ImageIO.read(ClassLoader.getSystemResourceAsStream("org/pepsoft/worldpainter/icons/deciduous_trees_pattern.png"));
                patterns[BIOME_FOREST] = createPattern(image);
                patterns[BIOME_FLOWER_FOREST] = createPattern(image);

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

                image = ImageIO.read(ClassLoader.getSystemResourceAsStream("org/pepsoft/worldpainter/icons/jungle_hills_pattern.png"));
                patterns[BIOME_JUNGLE_HILLS] = createPattern(image);
                patterns[BIOME_JUNGLE_M] = createPattern(image);

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
                patterns[BIOME_ICE_PLAINS_SPIKES] = createPattern(image);
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