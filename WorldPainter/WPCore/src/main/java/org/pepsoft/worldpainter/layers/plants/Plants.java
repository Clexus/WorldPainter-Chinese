package org.pepsoft.worldpainter.layers.plants;

import org.pepsoft.minecraft.Direction;
import org.pepsoft.minecraft.Material;
import org.pepsoft.worldpainter.Platform;
import org.pepsoft.worldpainter.exporting.MinecraftWorld;

import java.util.Random;

import static org.pepsoft.minecraft.Constants.*;
import static org.pepsoft.minecraft.Material.*;
import static org.pepsoft.worldpainter.layers.plants.Category.*;

/**
 * A collection of Minecraft plants. These are prototypes which cannot be
 * actually be rendered; you must always invoke
 * {@link Plant#realise(int, Platform)} to obtain a concrete instances of the
 * plant which can be rendered. The dimensions of the prototypes indicate the
 * maximum dimensions of the plant.
 */
public class Plants {
    public static void main(String[] args) {
        for (Plant plant: ALL_PLANTS) {
            System.out.println(plant);
        }
    }

    public static final Plant GRASS = new SimplePlant("\u8349", Material.GRASS, PLANTS_AND_FLOWERS);
    public static final Plant FERN = new SimplePlant("\u8568", Material.FERN, PLANTS_AND_FLOWERS);
    public static final Plant DEAD_SHRUB = new SimplePlant("\u67AF\u840E\u7684\u704C\u6728", Material.DEAD_SHRUBS, PLANTS_AND_FLOWERS) {
        @Override
        public Category isValidFoundation(MinecraftWorld world, int x, int y, int z, boolean checkBlockBelow) {
            final Material material = world.getMaterialAt(x, y, z);
            return ((! checkBlockBelow)
                    || material.modded
                    || material.isNamedOneOf(MC_GRASS_BLOCK, MC_SAND, MC_RED_SAND, MC_DIRT, MC_TERRACOTTA, MC_PODZOL, MC_COARSE_DIRT, MC_ROOTED_DIRT, MC_MOSS_BLOCK, MC_MUD)
                    || material.name.endsWith("_terracotta"))
                ? PLANTS_AND_FLOWERS
                : null;
        }
    };
    public static final Plant DANDELION = new SimplePlant("\u84B2\u516C\u82F1", Material.DANDELION, PLANTS_AND_FLOWERS);
    public static final Plant POPPY = new SimplePlant("\u865E\u7F8E\u4EBA", Material.ROSE, PLANTS_AND_FLOWERS);
    public static final Plant BLUE_ORCHID = new SimplePlant("\u5170\u82B1", Material.BLUE_ORCHID, PLANTS_AND_FLOWERS);
    public static final Plant ALLIUM = new SimplePlant("\u7ED2\u7403\u8471", Material.ALLIUM, PLANTS_AND_FLOWERS);
    public static final Plant AZURE_BLUET = new SimplePlant("\u831C\u8349\u82B1", Material.AZURE_BLUET, PLANTS_AND_FLOWERS);
    public static final Plant TULIP_RED = new SimplePlant("\u7EA2\u8272\u90C1\u91D1\u9999", Material.RED_TULIP, PLANTS_AND_FLOWERS);
    public static final Plant TULIP_ORANGE = new SimplePlant("\u6A59\u8272\u90C1\u91D1\u9999", Material.ORANGE_TULIP, PLANTS_AND_FLOWERS);
    public static final Plant TULIP_WHITE = new SimplePlant("\u767D\u8272\u90C1\u91D1\u9999", Material.WHITE_TULIP, PLANTS_AND_FLOWERS);
    public static final Plant TULIP_PINK = new SimplePlant("\u7C89\u8272\u90C1\u91D1\u9999", Material.PINK_TULIP, PLANTS_AND_FLOWERS);
    public static final Plant OXEYE_DAISY = new SimplePlant("\u6EE8\u83CA", Material.OXEYE_DAISY, PLANTS_AND_FLOWERS);
    public static final Plant CORNFLOWER = new SimplePlant("\u77E2\u8F66\u83CA", Material.CORNFLOWER, PLANTS_AND_FLOWERS);
    public static final Plant LILY_OF_THE_VALLEY = new SimplePlant("\u94C3\u5170", Material.LILY_OF_THE_VALLEY, PLANTS_AND_FLOWERS);
    public static final Plant WITHER_ROSE = new SimplePlant("\u51CB\u7075\u73AB\u7470", Material.WITHER_ROSE, NETHER);
    public static final Plant SUNFLOWER = new DoubleHighPlant("\u5411\u65E5\u8475", Material.SUNFLOWER_LOWER, "block/sunflower_front.png", PLANTS_AND_FLOWERS);
    public static final Plant LILAC = new DoubleHighPlant("\u4E01\u9999\u82B1", Material.LILAC_LOWER, PLANTS_AND_FLOWERS);
    public static final Plant TALL_GRASS = new DoubleHighPlant("\u957F\u8349", Material.TALL_GRASS_LOWER, PLANTS_AND_FLOWERS);
    public static final Plant LARGE_FERN = new DoubleHighPlant("\u5927\u578B\u8568", Material.LARGE_FERN_LOWER, PLANTS_AND_FLOWERS);
    public static final Plant ROSE_BUSH = new DoubleHighPlant("\u73AB\u7470\u4E1B", Material.ROSE_BUSH_LOWER, PLANTS_AND_FLOWERS);
    public static final Plant PEONY = new DoubleHighPlant("\u7261\u4E39", Material.PEONY_LOWER, PLANTS_AND_FLOWERS);
    public static final Plant SAPLING_OAK = new SimplePlant("\u6A61\u6811\u6811\u82D7", Material.OAK_SAPLING, SAPLINGS);
    public static final Plant SAPLING_DARK_OAK = new SimplePlant("\u6DF1\u8272\u6A61\u6728\u6811\u82D7", Material.DARK_OAK_SAPLING, SAPLINGS);
    public static final Plant SAPLING_PINE = new SimplePlant("\u677E\u6811\u82D7", Material.PINE_SAPLING, SAPLINGS);
    public static final Plant SAPLING_BIRCH = new SimplePlant("\u767D\u6866\u6811\u82D7", Material.BIRCH_SAPLING, SAPLINGS);
    public static final Plant SAPLING_JUNGLE = new SimplePlant("\u4E1B\u6797\u6811\u82D7", Material.JUNGLE_SAPLING, SAPLINGS);
    public static final Plant SAPLING_ACACIA = new SimplePlant("\u91D1\u5408\u6B22\u6811\u82D7", Material.ACACIA_SAPLING, SAPLINGS);
    public static final Plant MUSHROOM_RED = new SimplePlant("\u7EA2\u8272\u8611\u83C7", Material.RED_MUSHROOM, MUSHROOMS);
    public static final Plant MUSHROOM_BROWN = new SimplePlant("\u68D5\u8272\u8611\u83C7", Material.BROWN_MUSHROOM, MUSHROOMS);
    public static final Plant WHEAT = new AgingPlant("\u5C0F\u9EA6", Material.WHEAT, "block/wheat_stage7.png", 8, CROPS);
    public static final Plant CARROTS = new AgingPlant("\u80E1\u841D\u535C", Material.CARROTS, "block/carrots_stage3.png", 8, CROPS);
    public static final Plant POTATOES = new AgingPlant("\u9A6C\u94C3\u85AF", Material.POTATOES, "block/potatoes_stage3.png", 8, CROPS);
    public static final Plant PUMPKIN_STEMS = new AgingPlant("\u5357\u74DC\u6897", Material.PUMPKIN_STEM, "block/pumpkin_side.png", 8, CROPS) {
        @Override
        public Plant realise(int growth, Platform platform) {
            return new SimplePlant("\u5357\u74DC\u6897", Material.PUMPKIN_STEM.withProperty(AGE, growth - 1).withProperty(FACING, Direction.values()[RANDOM.nextInt(4)]), categories);
        }
    };
    public static final Plant MELON_STEMS = new AgingPlant("\u897F\u74DC\u6897", Material.MELON_STEM, "block/melon_side.png", 8, CROPS) {
        @Override
        public Plant realise(int growth, Platform platform) {
            return new SimplePlant("\u897F\u74DC\u6897", Material.MELON_STEM.withProperty(AGE, growth - 1).withProperty(FACING, Direction.values()[RANDOM.nextInt(4)]), categories);
        }
    };
    public static final Plant BEETROOTS = new AgingPlant("\u751C\u83DC", Material.BEETROOTS, "block/beetroots_stage3.png", 4, CROPS);
    public static final Plant SWEET_BERRY_BUSH = new AgingPlant("\u6D46\u679C\u4E1B", Material.SWEET_BERRY_BUSH, "block/sweet_berry_bush_stage3.png", 4, PLANTS_AND_FLOWERS);
    public static final Plant CACTUS = new VariableHeightPlant("\u4ED9\u4EBA\u638C", Material.CACTUS, "block/cactus_side.png", 3, Category.CACTUS);
    public static final Plant SUGAR_CANE = new VariableHeightPlant("\u7518\u8517", Material.SUGAR_CANE, 3, Category.SUGAR_CANE);
    public static final Plant LILY_PAD = new SimplePlant("\u8377\u53F6", Material.LILY_PAD, Category.FLOATING_PLANTS);
    public static final Plant NETHER_WART = new AgingPlant("\u4E0B\u754C\u75A3", Material.NETHER_WART, "block/nether_wart_stage2.png", 4, Category.NETHER) {
        @Override
        public Plant realise(int growth, Platform platform) {
            return new SimplePlant("\u4E0B\u754C\u75A3", Material.NETHER_WART.withProperty(AGE, growth - 1), categories) {
                @Override
                public Category isValidFoundation(MinecraftWorld world, int x, int y, int height, boolean checkBlockBelow) {
                    final Material material = world.getMaterialAt(x, y, height);
                    return ((! checkBlockBelow) || material.modded || material.isNamed(MC_SOUL_SAND)) ? NETHER : null;
                }
            };
        }
    };
    public static final Plant CHORUS_PLANT = new VariableHeightPlant("\u7D2B\u9882\u82B1", Material.CHORUS_PLANT, Material.CHORUS_FLOWER, "block/chorus_flower.png", 5, Category.END);
    public static final Plant TUBE_CORAL = new SimplePlant("\u7BA1\u73CA\u745A", Material.TUBE_CORAL, WATER_PLANTS);
    public static final Plant BRAIN_CORAL = new SimplePlant("\u8111\u7EB9\u73CA\u745A", Material.BRAIN_CORAL, WATER_PLANTS);
    public static final Plant BUBBLE_CORAL = new SimplePlant("\u6C14\u6CE1\u73CA\u745A", Material.BUBBLE_CORAL, WATER_PLANTS);
    public static final Plant FIRE_CORAL = new SimplePlant("\u706B\u73CA\u745A", Material.FIRE_CORAL, WATER_PLANTS);
    public static final Plant HORN_CORAL = new SimplePlant("\u9E7F\u89D2\u73CA\u745A", Material.HORN_CORAL, WATER_PLANTS);
    public static final Plant TUBE_CORAL_FAN = new SimplePlant("\u7BA1\u73CA\u745A\u6247", Material.TUBE_CORAL_FAN, WATER_PLANTS);
    public static final Plant BRAIN_CORAL_FAN = new SimplePlant("\u8111\u7EB9\u73CA\u745A\u6247", Material.BRAIN_CORAL_FAN, WATER_PLANTS);
    public static final Plant BUBBLE_CORAL_FAN = new SimplePlant("\u6C14\u6CE1\u73CA\u745A\u6247", Material.BUBBLE_CORAL_FAN, WATER_PLANTS);
    public static final Plant FIRE_CORAL_FAN = new SimplePlant("\u706B\u73CA\u745A\u6247", Material.FIRE_CORAL_FAN, WATER_PLANTS);
    public static final Plant HORN_CORAL_FAN = new SimplePlant("\u9E7F\u89D2\u73CA\u745A\u6247", Material.HORN_CORAL_FAN, WATER_PLANTS);
    public static final Plant KELP = new VariableHeightPlant("\u6D77\u5E26", Material.KELP_PLANT, Material.KELP, 26, WATER_PLANTS) {
        @Override
        public VariableHeightPlant realise(int growth, Platform platform) {
            return new VariableHeightPlant("\u6D77\u5E26", Material.KELP_PLANT, Material.KELP.withProperty(AGE, RANDOM.nextInt(26)), growth, categories);
        }
    };
    public static final Plant SEAGRASS = new SimplePlant("\u6D77\u8349", Material.SEAGRASS, WATER_PLANTS);
    public static final Plant TALL_SEAGRASS = new DoubleHighPlant("\u9AD8\u6D77\u8349", Material.TALL_SEAGRASS_LOWER, WATER_PLANTS);
    public static final Plant SEA_PICKLE = new PlantWithGrowth("\u6D77\u6CE1\u83DC", Material.SEA_PICKLE_1, "item/sea_pickle.png", 4, WATER_PLANTS) {
        @Override
        public SimplePlant realise(int growth, Platform platform) {
            return new SimplePlant(name, material.withProperty(PICKLES, growth), iconName, categories);
        }
    };
    public static final Plant BAMBOO = new VariableHeightPlant("\u7AF9\u5B50", BAMBOO_NO_LEAVES, BAMBOO_LARGE_LEAVES, "item/bamboo.png", 16, PLANTS_AND_FLOWERS) {
        @Override
        public Plant realise(int growth, Platform platform) {
            return new VariableHeightPlant("\u7AF9\u5B50", BAMBOO_NO_LEAVES, BAMBOO_LARGE_LEAVES, "item/bamboo.png", growth, PLANTS_AND_FLOWERS) {
                @Override
                public Material getMaterial(int x, int y, int z) {
                    final int age = (growth > 4) ? 1 : 0;
                    switch (z) {
                        case 0:
                            return BAMBOO_NO_LEAVES.withProperty(AGE, age);
                        case 1:
                        case 2:
                            if (growth <= 4) {
                                return BAMBOO_SMALL_LEAVES.withProperty(AGE, age);
                            } else {
                                return BAMBOO_NO_LEAVES.withProperty(AGE, age);
                            }
                        default:
                            if (z >= growth - 2) {
                                return BAMBOO_LARGE_LEAVES.withProperty(AGE, age);
                            } else if (z == growth - 3) {
                                return BAMBOO_SMALL_LEAVES.withProperty(AGE, age);
                            } else {
                                return BAMBOO_NO_LEAVES.withProperty(AGE, age);
                            }
                    }
                }
            };
        }
    };
    public static final Plant SAPLING_AZALEA = new SimplePlant("\u675C\u9E43\u82B1\u4E1B", Material.AZALEA, "block/azalea_plant.png", SAPLINGS);
    public static final Plant SAPLING_FLOWERING_AZALEA = new SimplePlant("\u76DB\u5F00\u7684\u675C\u9E43\u82B1\u4E1B", Material.FLOWERING_AZALEA, "block/flowering_azalea_side.png", SAPLINGS);
    public static final Plant CRIMSON_FUNGUS = new SimplePlant("\u7EEF\u7EA2\u83CC", Material.CRIMSON_FUNGUS, NETHER);
    public static final Plant WARPED_FUNGUS = new SimplePlant("\u8BE1\u5F02\u83CC", Material.WARPED_FUNGUS, NETHER);
    public static final Plant CRIMSON_ROOTS = new SimplePlant("\u7EEF\u7EA2\u83CC\u7D22", Material.CRIMSON_ROOTS, NETHER);
    public static final Plant WARPED_ROOTS = new SimplePlant("\u8BE1\u5F02\u83CC\u7D22", Material.WARPED_ROOTS, NETHER);
    public static final Plant NETHER_SPROUTS = new SimplePlant("\u4E0B\u754C\u82D7", Material.NETHER_SPROUTS, NETHER);
    public static final Plant TWISTING_VINES = new VariableHeightPlant("\u7F20\u6028\u85E4", Material.TWISTING_VINES_PLANT, TWISTING_VINES_25, 10, MUSHROOMS); // TODO not really mushrooms, but for now those are presented as "Various"
    public static final Plant GLOW_LICHEN = new SimplePlant("\u53D1\u5149\u83CC\u5CA9", Material.GLOW_LICHEN_DOWN, MUSHROOMS, WATER_PLANTS, HANGING_DRY_PLANTS, HANGING_WATER_PLANTS); // TODO not really mushrooms, but for now those are presented as "Various"
    public static final Plant MOSS_CARPET = new SimplePlant("\u8986\u5730\u82D4\u85D3", Material.MOSS_CARPET, "block/moss_block.png", MUSHROOMS); // TODO not really mushrooms, but for now those are presented as "Various"
    public static final Plant BIG_DRIPLEAF = new VariableHeightPlant("\u5927\u578B\u5782\u6EF4\u53F6", Material.BIG_DRIPLEAF_STEM_SOUTH, Material.BIG_DRIPLEAF_SOUTH, "block/big_dripleaf_top.png", 10, PLANTS_AND_FLOWERS, DRIPLEAF) {
        @Override
        public VariableHeightPlant realise(int growth, Platform platform) {
            final Direction facing = Direction.values()[RANDOM.nextInt(4)];
            return new VariableHeightPlant("\u5927\u578B\u5782\u6EF4\u53F6", Material.BIG_DRIPLEAF_STEM_SOUTH.withProperty(MC_FACING, facing.toString()), Material.BIG_DRIPLEAF_SOUTH.withProperty(MC_FACING, facing.toString()), growth, categories);
        }
    };
    public static final Plant PUMPKIN = new SimplePlant("\u5357\u74DC", Material.PUMPKIN, "block/pumpkin_side.png", PLANTS_AND_FLOWERS) {
        @Override
        public Category isValidFoundation(MinecraftWorld world, int x, int y, int height, boolean checkBlockBelow) {
            final Material material = world.getMaterialAt(x, y, height);
            return ((! checkBlockBelow) || material.modded || material.solid) ? PLANTS_AND_FLOWERS : null;
        }
    };
    public static final Plant MELON = new SimplePlant("\u897F\u74DC", Material.MELON, "block/melon_side.png", PLANTS_AND_FLOWERS) {
        @Override
        public Category isValidFoundation(MinecraftWorld world, int x, int y, int height, boolean checkBlockBelow) {
            final Material material = world.getMaterialAt(x, y, height);
            return ((! checkBlockBelow) || material.modded || material.solid) ? PLANTS_AND_FLOWERS : null;
        }
    };
    public static final Plant CARVED_PUMPKIN = new SimplePlant("\u96D5\u523B\u5357\u74DC", Material.CARVED_PUMPKIN_SOUTH_FACE, MUSHROOMS) { // TODO not really mushrooms, but for now those are presented as "Various"
        @Override
        public Plant realise(int growth, Platform platform) {
            return new SimplePlant("\u96D5\u523B\u5357\u74DC", Material.CARVED_PUMPKIN_SOUTH_FACE.withProperty(FACING, Direction.values()[RANDOM.nextInt(4)]), categories) {
                @Override
                public Category isValidFoundation(MinecraftWorld world, int x, int y, int height, boolean checkBlockBelow) {
                    final Material material = world.getMaterialAt(x, y, height);
                    return ((! checkBlockBelow) || material.modded || material.solid) ? MUSHROOMS : null;
                }
            };
        }
    };
    public static final Plant JACK_O_LANTERN = new SimplePlant("\u5357\u74DC\u706F", Material.JACK_O_LANTERN_SOUTH_FACE, MUSHROOMS) { // TODO not really mushrooms, but for now those are presented as "Various"
        @Override
        public Plant realise(int growth, Platform platform) {
            return new SimplePlant("\u5357\u74DC\u706F", Material.JACK_O_LANTERN_SOUTH_FACE.withProperty(FACING, Direction.values()[RANDOM.nextInt(4)]), categories) {
                @Override
                public Category isValidFoundation(MinecraftWorld world, int x, int y, int height, boolean checkBlockBelow) {
                    final Material material = world.getMaterialAt(x, y, height);
                    return ((! checkBlockBelow) || material.modded || material.solid) ? MUSHROOMS : null;
                }
            };
        }
    };
    public static final Plant VINE = new VariableHeightPlant("\u85E4\u8513", Material.VINE, 10, HANGING_DRY_PLANTS) {
        @Override
        public Plant realise(int growth, Platform platform) {
            final String directionProperty = Direction.values()[RANDOM.nextInt(4)].name().toLowerCase();
            return new VariableHeightPlant("\u85E4\u8513",
                    Material.VINE.withProperty(DOWN, true).withProperty(directionProperty, "true"),
                    Material.VINE.withProperty(directionProperty, "true"),
                    Material.VINE.withProperty(directionProperty, "true"),
                    growth,
                    categories);
        }
    };
    public static final Plant SPORE_BLOSSOM = new SimplePlant("\u5B62\u5B50\u82B1", Material.SPORE_BLOSSOM, HANGING_DRY_PLANTS);
    public static final Plant WEEPING_VINES = new VariableHeightPlant("\u5782\u6CEA\u85E4", Material.WEEPING_VINES_PLANT, Material.WEEPING_VINES, "block/weeping_vines_plant.png", 10, HANGING_DRY_PLANTS);
    public static final Plant HANGING_ROOTS = new SimplePlant("\u5782\u6839", Material.HANGING_ROOTS, HANGING_DRY_PLANTS, HANGING_WATER_PLANTS);
    public static final Plant GLOW_BERRIES = new VariableHeightPlant("\u53D1\u5149\u6D46\u679C", Material.CAVE_VINES_PLANT_NO_BERRIES, Material.CAVE_VINES_NO_BERRIES, "block/cave_vines_lit.png", 10, HANGING_DRY_PLANTS) {
        @Override
        public Plant realise(int growth, Platform platform) {
            return new VariableHeightPlant("\u53D1\u5149\u6D46\u679C", Material.CAVE_VINES_PLANT_NO_BERRIES, Material.CAVE_VINES_NO_BERRIES, "block/cave_vines_lit.png", growth, categories) {
                @Override
                public Material getMaterial(int x, int y, int z) {
                    // Randomly add berries to one in four blocks
                    return super.getMaterial(x, y, z).withProperty(BERRIES, RANDOM.nextInt(4) == 0);
                }
            };
        }
    };
    public static final Plant SMALL_DRIPLEAF = new DoubleHighPlant("\u5C0F\u578B\u5782\u6EF4\u53F6", Material.SMALL_DRIPLEAF_SOUTH_LOWER, DRIPLEAF) {
        @Override
        public DoubleHighPlant realise(int growth, Platform platform) {
            return new DoubleHighPlant("\u5C0F\u578B\u5782\u6EF4\u53F6", Material.SMALL_DRIPLEAF_SOUTH_LOWER.withProperty(FACING, Direction.values()[RANDOM.nextInt(4)]), DRIPLEAF, platform);
        }
    };
    public static final Plant MANGROVE_PROPAGULE = new SimplePlant("\u7EA2\u6811\u80CE\u751F\u82D7", Material.MANGROVE_PROPAGULE, SAPLINGS, WATER_PLANTS);
    public static final Plant SAPLING_CHERRY = new SimplePlant("\u6A31\u82B1\u6811\u82D7", Material.CHERRY_SAPLING, SAPLINGS);
    public static final Plant PINK_PETALS = new PlantWithGrowth("\u7C89\u7EA2\u8272\u82B1\u7C07", Material.PINK_PETALS_1, "block/pink_petals.png", 4, PLANTS_AND_FLOWERS) {
        @Override
        public Plant realise(int growth, Platform platform) {
            return new SimplePlant("\u7C89\u7EA2\u8272\u82B1\u7C07", PINK_PETALS_1
                    .withProperty(FLOWER_AMOUNT, growth)
                    .withProperty(FACING, Direction.values()[RANDOM.nextInt(4)]), PLANTS_AND_FLOWERS);
        }
    };
    public static final Plant PITCHER_POD = new AgingPlant("\u74F6\u5B50\u8349\u835A\u679C", Material.PITCHER_CROP_0_LOWER, "item/pitcher_pod.png", 5, CROPS) {
        @Override
        public Plant realise(int growth, Platform platform) {
            if (growth < 4) {
                return new SimplePlant("\u74F6\u5B50\u8349\u835A\u679C", Material.PITCHER_CROP_0_LOWER.withProperty(AGE, growth - 1), CROPS);
            } else {
                return new DoubleHighPlant("\u74F6\u5B50\u8349\u835A\u679C", Material.PITCHER_CROP_0_LOWER.withProperty(AGE, growth - 1), CROPS, platform);
            }
        }
    };
    public static final Plant PITCHER_PLANT = new DoubleHighPlant("\u74F6\u5B50\u8349", Material.PITCHER_PLANT_LOWER, "block/pitcher_crop_top_stage_4.png", PLANTS_AND_FLOWERS);
    public static final Plant TORCHFLOWER_SEED = new AgingPlant("\u706B\u628A\u82B1\u79CD\u5B50", Material.TORCHFLOWER_CROP, "item/torchflower_seeds.png", 2, CROPS);
    public static final Plant TORCHFLOWER = new SimplePlant("\u706B\u628A\u82B1", Material.TORCHFLOWER, PLANTS_AND_FLOWERS);

    // The code which uses this assumes there will never be more than 128 plants. If that ever happens it needs to be
    // overhauled! IMPORTANT: indices into this array are stored in layer settings! New entries MUST be added at the
    // end, and the order MUST never be changed!
    public static final Plant[] ALL_PLANTS = { GRASS, TALL_GRASS, FERN, LARGE_FERN, DEAD_SHRUB, DANDELION, POPPY,
            BLUE_ORCHID, ALLIUM, AZURE_BLUET, TULIP_RED, TULIP_ORANGE, TULIP_WHITE, TULIP_PINK, OXEYE_DAISY, SUNFLOWER,
            LILAC, ROSE_BUSH, PEONY, SAPLING_OAK, SAPLING_DARK_OAK, SAPLING_PINE, SAPLING_BIRCH, SAPLING_JUNGLE,
            SAPLING_ACACIA, MUSHROOM_RED, MUSHROOM_BROWN, WHEAT, CARROTS, POTATOES, PUMPKIN_STEMS, MELON_STEMS, CACTUS,
            SUGAR_CANE, LILY_PAD, BEETROOTS, NETHER_WART, CHORUS_PLANT, TUBE_CORAL, BRAIN_CORAL, BUBBLE_CORAL,
            FIRE_CORAL, HORN_CORAL, TUBE_CORAL_FAN, BRAIN_CORAL_FAN, BUBBLE_CORAL_FAN, FIRE_CORAL_FAN, HORN_CORAL_FAN,
            KELP, SEAGRASS, TALL_SEAGRASS, SEA_PICKLE, CORNFLOWER, LILY_OF_THE_VALLEY, WITHER_ROSE, SWEET_BERRY_BUSH,
            BAMBOO, SAPLING_AZALEA, SAPLING_FLOWERING_AZALEA, CRIMSON_FUNGUS, WARPED_FUNGUS, CRIMSON_ROOTS,
            WARPED_ROOTS, NETHER_SPROUTS, TWISTING_VINES, GLOW_LICHEN, MOSS_CARPET, BIG_DRIPLEAF, PUMPKIN, MELON,
            CARVED_PUMPKIN, JACK_O_LANTERN, VINE, SPORE_BLOSSOM, WEEPING_VINES, HANGING_ROOTS, GLOW_BERRIES,
            SMALL_DRIPLEAF, MANGROVE_PROPAGULE, SAPLING_CHERRY, PINK_PETALS, PITCHER_POD, PITCHER_PLANT,
            TORCHFLOWER_SEED, TORCHFLOWER };

    private static final Random RANDOM = new Random();
}
