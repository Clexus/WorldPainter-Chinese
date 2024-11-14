package org.pepsoft.minecraft;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.BitSet;

import static org.pepsoft.minecraft.Constants.*;

/**
 * A database of legacy (pre-1.13) Minecraft block information. Accessed by using the block ID as index in the
 * {@link #BLOCKS} array. Implements the Enumeration pattern, meaning there is only ever one instance of this class for
 * each block ID, allowing use of the equals operator (==) for comparing instances.
 *
 * <p>Created by pepijn on 17-3-15.
 *
 * <p>Superseded by {@link Material}
 */
public final class Block implements Serializable {
    private Block(int id, int transparency, String name, boolean terrain,
                 boolean insubstantial, boolean veryInsubstantial, boolean resource, boolean tileEntity, boolean treeRelated,
                 boolean vegetation, int blockLight, boolean natural) {
        this.id = id;
        this.transparency = transparency;
        this.name = name;
        this.transparent = (transparency == 0);
        this.translucent = (transparency < 15);
        this.opaque = (transparency == 15);
        this.terrain = terrain;
        if (INSUBSTANTIAL_OVERRIDES.get(id)) {
            this.insubstantial = true;
            this.veryInsubstantial = true;
        } else {
            this.insubstantial = insubstantial;
            this.veryInsubstantial = veryInsubstantial;
        }
        this.solid = ! veryInsubstantial;
        this.resource = resource;
        this.tileEntity = tileEntity;
        this.treeRelated = treeRelated;
        this.vegetation = vegetation;
        this.blockLight = blockLight;
        this.lightSource = (blockLight > 0);
        this.natural = natural;

        // Sanity checks
        if ((id < 0) || (id > 4095)
                || (transparency < 0) || (transparency > 15)
                || (insubstantial && (! veryInsubstantial))
                || (blockLight < 0) || (blockLight > 15)
                || (treeRelated && vegetation)) {
            throw new IllegalArgumentException(Integer.toString(id));
        }

        // Determine the category
        if (id == BLK_AIR) {
            category = CATEGORY_AIR;
        } else if ((id == BLK_WATER) || (id == BLK_STATIONARY_WATER) || (id == BLK_LAVA) || (id == BLK_STATIONARY_LAVA)) {
            category = CATEGORY_FLUID;
        } else if (veryInsubstantial) {
            category = CATEGORY_INSUBSTANTIAL;
        } else if (! natural) {
            category = CATEGORY_MAN_MADE;
        } else if (resource) {
            category = CATEGORY_RESOURCE;
        } else {
            category = CATEGORY_NATURAL_SOLID;
        }
    }

    @Override
    public boolean equals(Object o) {
        return o == this;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return (name != null) ? name : Integer.toString(id);
    }

    /**
     * The block ID.
     */
    public final int id;

    /**
     * How much light the block blocks.
     */
    public final transient int transparency;

    /**
     * The name of the block.
     */
    public final transient String name;

    /**
     * Whether the block is fully transparent ({@link #transparency} == 0)
     */
    public final transient boolean transparent;

    /**
     * Whether the block is translucent ({@link #transparency} < 15)
     */
    public final transient boolean translucent;

    /**
     * Whether the block is fully opaque ({@link #transparency} == 15)
     */
    public final transient boolean opaque;

    /**
     * Whether the block is part of Minecraft-generated natural ground; more
     * specifically whether the block type should be assigned a terrain type
     * when importing a Minecraft map.
     */
    public final transient boolean terrain;

    /**
     * Whether the block is insubstantial, meaning that they are fully
     * transparent, not man-made, removing them would have no effect on the
     * surrounding blocks and be otherwise inconsequential. In other words
     * mostly decorative blocks that users presumably would not mind being
     * removed.
     */
    public final transient boolean insubstantial;

    /**
     * Whether the block is even more insubstantial. Implies
     * {@link #insubstantial} and adds air, water, lava and leaves.
     */
    public final transient boolean veryInsubstantial;

    /**
     * Whether the block is solid (meaning not {@link #insubstantial} or
     * {@link #veryInsubstantial}).
     */
    public final transient boolean solid;

    /**
     * Whether the block is a mineable ore or resource.
     */
    public final transient boolean resource;

    /**
     * Whether the block is a tile entity.
     */
    public final transient boolean tileEntity;

    /**
     * Whether the block is part of or attached to naturally occurring
     * trees or giant mushrooms. Also includes saplings, but not normal
     * mushrooms.
     */
    public final transient boolean treeRelated;

    /**
     * Whether the block is a plant. Excludes {@link #treeRelated} blocks.
     */
    public final transient boolean vegetation;

    /**
     * The amount of blocklight emitted by this block.
     */
    public final transient int blockLight;

    /**
     * Whether the block is a source of blocklight ({@link #blockLight} > 0).
     */
    public final transient boolean lightSource;

    /**
     * Whether the block can occur as part of a pristine Minecraft-generated
     * landscape, <em>excluding</em> artificial structures such as abandoned
     * mineshafts, villages, temples, strongholds, etc.
     */
    public final transient boolean natural;

    /**
     * Type of block encoded in a single category
     */
    public final transient int category;

    private Object readResolve() throws ObjectStreamException {
        return BLOCKS[id];
    }

    public static final Block[] BLOCKS = new Block[4096];

    private static final BitSet INSUBSTANTIAL_OVERRIDES = new BitSet();

    static {
        String insubStr = System.getProperty("org.pepsoft.worldpainter.insubstantialBlocks");
        if (insubStr != null) {
            Arrays.stream(insubStr.split("[, ]+")).forEach(s -> INSUBSTANTIAL_OVERRIDES.set(Integer.parseInt(s)));
        }
    }

    // Tr == Transparency, meaning in this case how much light is *blocked* by the block, 0 being fully transparent and
    // 15 being fully opaque
    static {
        System.arraycopy(new Block[] {
//                         ID, Tr,                      Display Name, Terra, Insub, VryIn, Resou, TileE, TreeR, Veget, Li, Natural
                new Block(  0,  0,                             "\u7A7A\u6C14", false, false,  true, false, false, false, false,  0,  true),
                new Block(  1, 15,                           "\u77F3\u5934",  true, false, false, false, false, false, false,  0,  true),
                new Block(  2, 15,                           "\u8349\u65B9\u5757",  true, false, false, false, false, false, false,  0,  true),
                new Block(  3, 15,                            "\u6CE5\u571F",  true, false, false, false, false, false, false,  0,  true),
                new Block(  4, 15,                     "\u5706\u77F3", false, false, false, false, false, false, false,  0, false),
                new Block(  5, 15,                    "\u6728\u677F", false, false, false, false, false, false, false,  0, false),
                new Block(  6,  0,                         "\u6811\u82D7", false,  true,  true, false, false,  true, false,  0, false),
                new Block(  7, 15,                         "\u57FA\u5CA9",  true, false, false, false, false, false, false,  0,  true),
                new Block(  8,  3,                           "\u6C34\u6E90", false, false,  true, false, false, false, false,  0,  true),
                new Block(  9,  3,                "\u9759\u6C34", false, false,  true, false, false, false, false,  0,  true),
//                         ID, Tr,                      Display Name, Terra, Insub, VryIn, Resou, TileE, TreeR, Veget, Li, Natural
                new Block( 10,  0,                            "\u6D41\u52A8\u5CA9\u6D46", false, false,  true, false, false, false, false, 15,  true),
                new Block( 11,  0,                 "\u5CA9\u6D46\u6E90", false, false,  true, false, false, false, false, 15,  true),
                new Block( 12, 15,                            "\u6C99\u5B50",  true, false, false, false, false, false, false,  0,  true),
                new Block( 13, 15,                          "\u7802\u783E",  true, false, false, false, false, false, false,  0,  true),
                new Block( 14, 15,                        "\u91D1\u77FF\u77F3",  true, false, false,  true, false, false, false,  0,  true),
                new Block( 15, 15,                        "\u94C1\u77FF\u77F3",  true, false, false,  true, false, false, false,  0,  true),
                new Block( 16, 15,                        "\u7164\u77FF\u77F3",  true, false, false,  true, false, false, false,  0,  true),
                new Block( 17, 15,                            "\u6728\u5934", false, false, false, false, false,  true, false,  0,  true),
                new Block( 18,  1,                          "\u6811\u53F6", false, false,  true, false, false,  true, false,  0,  true),
                new Block( 19, 15,                          "\u6D77\u7EF5", false, false, false, false, false, false, false,  0, false),
//                         ID, Tr,                      Display Name, Terra, Insub, VryIn, Resou, TileE, TreeR, Veget, Li, Natural
                new Block( 20,  0,                           "\u73BB\u7483", false, false, false, false, false, false, false,  0, false),
                new Block( 21, 15,                "\u9752\u91D1\u77F3\u77FF\u77F3",  true, false, false,  true, false, false, false,  0,  true),
                new Block( 22, 15,              "\u9752\u91D1\u77F3\u5757", false, false, false, false, false, false, false,  0, false),
                new Block( 23, 15,                       "\u53D1\u5C04\u5668", false, false, false, false,  true, false, false,  0, false),
                new Block( 24, 15,                       "\u6C99\u77F3",  true, false, false, false, false, false, false,  0,  true),
                new Block( 25, 15,                      "\u97F3\u7B26\u76D2", false, false, false, false,  true, false, false,  0, false),
                new Block( 26,  0,                             "\u5E8A", false, false, false, false, false, false, false,  0, false),
                new Block( 27,  0,                    "\u52A8\u529B\u94C1\u8F68", false, false, false, false, false, false, false,  0, false),
                new Block( 28,  0,                   "\u63A2\u6D4B\u94C1\u8F68", false, false, false, false, false, false, false,  0, false),
                new Block( 29, 15,                   "\u7C98\u6027\u6D3B\u585E", false, false, false, false, false, false, false,  0, false),
//                         ID, Tr,                      Display Name, Terra, Insub, VryIn, Resou, TileE, TreeR, Veget, Li, Natural
                new Block( 30,  0,                          "\u8718\u86DB\u7F51", false,  true,  true, false, false, false, false,  0, false),
                new Block( 31,  0,                      "\u9AD8\u8349", false,  true,  true, false, false, false,  true,  0,  true),
                new Block( 32,  0,                       "\u67AF\u840E\u7684\u704C\u6728", false,  true,  true, false, false, false,  true,  0,  true),
                new Block( 33,  0,                          "\u6D3B\u585E", false, false, false, false, false, false, false,  0, false),
                new Block( 34,  0,                "\u6D3B\u585E\u5934", false, false, false, false,  true, false, false,  0, false),
                new Block( 35, 15,                            "\u7F8A\u6BDB", false, false, false, false, false, false, false,  0, false),
                new Block( 36,  0,                              null, false, false, false, false, false, false, false,  0, false),
                new Block( 37,  0,                       "\u84B2\u516C\u82F1", false,  true,  true, false, false, false,  true,  0,  true),
                new Block( 38,  0,                          "\u82B1", false,  true,  true, false, false, false,  true,  0,  true),
                new Block( 39,  0,                  "\u68D5\u8272\u8611\u83C7", false,  true,  true, false, false, false,  true,  1,  true),
//                         ID, Tr,                      Display Name, Terra, Insub, VryIn, Resou, TileE, TreeR, Veget, Li, Natural
                new Block( 40,  0,                    "\u7EA2\u8272\u8611\u83C7", false,  true,  true, false, false, false,  true,  0,  true),
                new Block( 41, 15,                      "\u91D1\u5757", false, false, false, false, false, false, false,  0, false),
                new Block( 42, 15,                      "\u94C1\u5757", false, false, false, false, false, false, false,  0, false),
                new Block( 43, 15,                    "\u53CC\u5C42\u53F0\u9636", false, false, false, false, false, false, false,  0, false),
                new Block( 44, 15,                            "\u53F0\u9636", false, false, false, false, false, false, false,  0, false),
                new Block( 45, 15,                     "\u7816\u5757", false, false, false, false, false, false, false,  0, false),
                new Block( 46, 15,                             "TNT", false, false, false, false, false, false, false,  0, false),
                new Block( 47, 15,                       "\u4E66\u67B6", false, false, false, false, false, false, false,  0, false),
                new Block( 48, 15,               "\u82D4\u77F3", false, false, false, false, false, false, false,  0, false),
                new Block( 49, 15,                        "\u9ED1\u66DC\u77F3",  true, false, false, false, false, false, false,  0,  true),
//                         ID, Tr,                      Display Name, Terra, Insub, VryIn, Resou, TileE, TreeR, Veget, Li, Natural
                new Block( 50,  0,                           "\u706B\u628A", false, false, false, false, false, false, false, 14, false),
                new Block( 51,  0,                            "\u706B", false,  true,  true, false, false, false, false, 15,  true),
                new Block( 52, 15,                 "\u5237\u602A\u7B3C", false, false, false, false,  true, false, false,  0, false),
                new Block( 53, 15,                   "\u6728\u697C\u68AF", false, false, false, false, false, false, false,  0, false),
                new Block( 54,  0,                           "\u7BB1\u5B50", false, false, false, false,  true, false, false,  0, false),
                new Block( 55,  0,                   "\u7EA2\u77F3\u7EBF", false, false, false, false, false, false, false,  0, false),
                new Block( 56, 15, "\u94BB\u77F3\u77FF\u77F3", true, false, false, true, false, false, false, 0, true),
                new Block( 57, 15, "\u94BB\u77F3\u5757", false, false, false, false, false, false, false, 0, false),
                new Block( 58, 15, "\u5DE5\u4F5C\u53F0", false, false, false, false, false, false, false, 0, false),
                new Block( 59, 0, "\u5C0F\u9EA6", false, true, true, false, false, false, true, 0, false),
                new Block( 60, 15, "\u8015\u5730", true, false, false, false, false, false, false, 0, false),
                new Block( 61, 15, "\u7194\u7089", false, false, false, false, true, false, false, 0, false),
                new Block( 62, 15, "\u71C3\u70E7\u7684\u7194\u7089", false, false, false, false, true, false, false, 13, false),
                new Block( 63, 0, "\u544A\u793A\u724C", false, false, false, false, true, false, false, 0, false),
                new Block( 64, 0, "\u6728\u95E8", false, false, false, false, false, false, false, 0, false),
                new Block( 65, 0, "\u68AF\u5B50", false, false, false, false, false, false, false, 0, false),
                new Block( 66, 0, "\u94C1\u8F68", false, false, false, false, false, false, false, 0, false),
                new Block( 67, 15, "\u5706\u77F3\u9636\u68AF", false, false, false, false, false, false, false, 0, false),
                new Block( 68, 0, "\u60AC\u6302\u7684\u544A\u793A\u724C", false, false, false, false, true, false, false, 0, false),
                new Block( 69, 0, "\u62C9\u6746", false, false, false, false, false, false, false, 0, false),
                new Block( 70, 0, "\u77F3\u8D28\u538B\u529B\u677F", false, false, false, false, false, false, false, 0, false),
                new Block( 71, 0, "\u94C1\u95E8", false, false, false, false, false, false, false, 0, false),
                new Block( 72, 0, "\u6728\u8D28\u538B\u529B\u677F", false, false, false, false, false, false, false, 0, false),
                new Block( 73, 15, "\u7EA2\u77F3\u77FF\u77F3", true, false, false, true, false, false, false, 0, true),
                new Block( 74, 15, "\u53D1\u5149\u7EA2\u77F3\u77FF\u77F3", true, false, false, false, false, false, false, 9, true),
                new Block( 75, 0, "\u7EA2\u77F3\u706B\u628A\uFF08\u5173\u95ED\uFF09", false, false, false, false, false, false, false, 0, false),
                new Block( 76, 0, "\u7EA2\u77F3\u706B\u628A\uFF08\u5F00\u542F\uFF09", false, false, false, false, false, false, false, 7, false),
                new Block( 77, 0, "\u77F3\u6309\u94AE", false, false, false, false, false, false, false, 0, false),
                new Block( 78, 0, "\u96EA", false, true, true, false, false, false, false, 0, true),
                new Block( 79, 3, "\u51B0", false, false, false, false, false, false, false, 0, true),
                new Block( 80, 15, "\u96EA\u5757", true, true, true, false, false, false, false, 0, true),
                new Block( 81, 0, "\u4ED9\u4EBA\u638C", false, true, true, false, false, false, true, 0, true),
                new Block( 82, 15, "\u7C98\u571F\u5757", true, false, false, false, false, false, false, 0, true),
                new Block( 83, 0, "\u751C\u83DC", false, true, true, false, false, false, true, 0, true),
                new Block( 84, 15, "\u5531\u7247\u673A", false, false, false, false, true, false, false, 0, false),
                new Block( 85, 0, "\u6805\u680F", false, false, false, false, false, false, false, 0, false),
                new Block( 86, 0, "\u5357\u74DC", false, true, true, false, false, false, true, 0, true),
                new Block( 87, 15, "\u4E0B\u754C\u5CA9", true, false, false, false, false, false, false, 0, true),
                new Block( 88, 15, "\u7075\u9B42\u6C99", true, false, false, false, false, false, false, 0, true),
                new Block( 89, 15, "\u8367\u77F3", false, false, false, false, false, false, false, 15, true),
                new Block( 90, 0, "\u4F20\u9001\u95E8", false, false, false, false, false, false, false, 11, false),
                new Block( 91, 15, "\u5357\u74DC\u706F", false, false, false, false, false, false, false, 15, false),
                new Block( 92, 0, "\u86CB\u7CD5", false, false, false, false, false, false, false, 0, false),
                new Block( 93, 0, "\u7EA2\u77F3\u4E2D\u7EE7\u5668\uFF08\u5173\u95ED\uFF09", false, false, false, false, false, false, false, 0, false),
                new Block( 94, 0, "\u7EA2\u77F3\u4E2D\u7EE7\u5668\uFF08\u5F00\u542F\uFF09", false, false, false, false, false, false, false, 9, false),
                new Block( 95, 15, "\u67D3\u8272\u73BB\u7483", false, false, false, false, false, false, false, 0, false),
                new Block( 96, 0, "\u6D3B\u677F\u95E8", false, false, false, false, false, false, false, 0, false),
                new Block( 97, 15, "\u866B\u8680\u65B9\u5757", true, false, false, false, false, false, false, 0, true),
                new Block( 98, 15, "\u77F3\u7816", false, false, false, false, false, false, false, 0, false),
                new Block( 99, 15, "\u5DE8\u578B\u68D5\u8272\u8611\u83C7", false, false, false, false, false, true, false, 0, true),
//                         ID, Tr,                      Display Name, Terra, Insub, VryIn, Resou, TileE, TreeR, Veget, Li, Natural
                new Block(100, 15, "\u5DE8\u578B\u7EA2\u8272\u8611\u83C7", false, false, false, false, false, true, false, 0, true),
                new Block(101, 0, "\u94C1\u6805\u680F", false, false, false, false, false, false, false, 0, false),
                new Block(102, 0, "\u73BB\u7483\u677F", false, false, false, false, false, false, false, 0, false),
                new Block(103, 15, "\u897F\u74DC", false, true, true, false, false, false, true, 0, false),
                new Block(104, 0, "\u5357\u74DC\u6897", false, true, true, false, false, false, true, 0, false),
                new Block(105, 0, "\u897F\u74DC\u6897", false, true, true, false, false, false, true, 0, false),
                new Block(106, 0, "\u85E4\u8513", false, true, true, false, false, true, false, 0, true),
                new Block(107, 0, "\u6805\u680F\u95E8", false, false, false, false, false, false, false, 0, false),
                new Block(108, 15, "\u7816\u9636", false, false, false, false, false, false, false, 0, false),
                new Block(109, 15, "\u77F3\u7816\u9636\u68AF", false, false, false, false, false, false, false, 0, false),
                new Block(110, 15, "\u83CC\u4E1D\u4F53", true, false, false, false, false, false, false, 0, true),
                new Block(111, 0, "\u7761\u83B2", false, true, true, false, false, false, true, 0, true),
                new Block(112, 15, "\u4E0B\u754C\u7816", false, false, false, false, false, false, false, 0, false),
                new Block(113, 0, "\u4E0B\u754C\u7816\u6805\u680F", false, false, false, false, false, false, false, 0, false),
                new Block(114, 15, "\u4E0B\u754C\u7816\u697C\u68AF", false, false, false, false, false, false, false, 0, false),
                new Block(115, 0, "\u4E0B\u754C\u75A3", false, true, true, false, false, false, true, 0, true),
                new Block(116, 0, "\u9644\u9B54\u53F0", false, false, false, false, true, false, false, 0, false),
                new Block(117, 0, "\u917F\u9020\u53F0", false, false, false, false, true, false, false, 1, false),
                new Block(118, 0, "\u5769\u57DA", false, false, false, false, false, false, false, 0, false),
                new Block(119, 0, "\u672B\u5730\u4F20\u9001\u95E8", false, false, false, false, true, false, false, 15, false),
                new Block(120, 15, "\u672B\u5730\u4F20\u9001\u95E8\u6846\u67B6", false, false, false, false, false, false, false, 1, false),
                new Block(121, 15, "\u672B\u5730\u77F3", true, false, false, false, false, false, false, 0, true),
                new Block(122, 0, "\u9F99\u86CB", false, false, false, false, false, false, false, 1, false),
                new Block(123, 15, "\u7EA2\u77F3\u706F\uFF08\u5173\u95ED\uFF09", false, false, false, false, false, false, false, 0, false),
                new Block(124, 15, "\u7EA2\u77F3\u706F\uFF08\u5F00\u542F\uFF09", false, false, false, false, false, false, false, 15, false),
                new Block(125, 15, "\u6728\u8D28\u53CC\u5C42\u53F0\u9636", false, false, false, false, false, false, false, 0, false),
                new Block(126, 15, "\u6728\u8D28\u53F0\u9636", false, false, false, false, false, false, false, 0, false),
                new Block(127, 0, "\u53EF\u53EF\u8C46\u690D\u682A", false, true, true, false, false, true, false, 0, true),
                new Block(128, 15, "\u6C99\u5CA9\u9636\u68AF", false, false, false, false, false, false, false, 0, false),
                new Block(129, 15, "\u7EFF\u5B9D\u77F3\u77FF\u77F3", false, false, false, true, false, false, false, 0, true),
                new Block(130, 0, "\u672B\u5F71\u7BB1", false, false, false, false, true, false, false, 7, false),
                new Block(131, 0, "\u7ECA\u7EBF\u94A9", false, false, false, false, false, false, false, 0, false),
                new Block(132, 0, "\u7ECA\u7EBF", false, false, false, false, false, false, false, 0, false),
                new Block(133, 15, "\u7EFF\u5B9D\u77F3\u5757", false, false, false, false, false, false, false, 0, false),
                new Block(134, 15, "\u6A61\u6728\u9636\u68AF", false, false, false, false, false, false, false, 0, false),
                new Block(135, 15, "\u767D\u6866\u6728\u9636\u68AF", false, false, false, false, false, false, false, 0, false),
                new Block(136, 15, "\u4E1B\u6797\u6728\u9636\u68AF", false, false, false, false, false, false, false, 0, false),
                new Block(137, 15, "\u547D\u4EE4\u65B9\u5757", false, false, false, false, true, false, false, 0, false),
                new Block(138, 15, "\u4FE1\u6807", false, false, false, false, true, false, false, 15, false),
                new Block(139, 0, "\u5706\u77F3\u5899", false, false, false, false, false, false, false, 0, false),
                new Block(140, 0, "\u82B1\u76C6", false, false, false, false, true, false, false, 0, false),
                new Block(141, 0, "\u80E1\u841D\u535C", false, true, true, false, false, false, true, 0, false),
                new Block(142, 0, "\u571F\u8C46", false, true, true, false, false, false, true, 0, false),
                new Block(143, 0, "\u6728\u8D28\u6309\u94AE", false, false, false, false, false, false, false, 0, false),
                new Block(144, 0, "\u5934\u9885", false, false, false, false, true, false, false, 0, false),
                new Block(145, 15, "\u94C1\u7827", false, false, false, false, false, false, false, 0, false),
                new Block(146, 0, "\u9677\u9631\u7BB1", false, false, false, false, true, false, false, 0, false),
                new Block(147, 0, "\u8F7B\u8D28\u6D4B\u91CD\u538B\u529B\u677F", false, false, false, false, false, false, false, 0, false),
                new Block(148, 0, "\u91CD\u8D28\u6D4B\u91CD\u538B\u529B\u677F", false, false, false, false, false, false, false, 0, false),
                new Block(149, 0, "\u7EA2\u77F3\u6BD4\u8F83\u5668\uFF08\u672A\u5145\u80FD\uFF09", false, false, false, false, true, false, false, 0, false),
//                         ID, Tr,                      Display Name, Terra, Insub, VryIn, Resou, TileE, TreeR, Veget, Li, Natural
                new Block(150, 0, "\u7EA2\u77F3\u6BD4\u8F83\u5668\uFF08\u5DF2\u5145\u80FD\uFF09", false, false, false, false, true, false, false, 0, false),
                new Block(151, 0, "\u9633\u5149\u63A2\u6D4B\u5668", false, false, false, false, true, false, false, 0, false),
                new Block(152, 15, "\u7EA2\u77F3\u5757", false, false, false, false, false, false, false, 0, false),
                new Block(153, 15, "\u4E0B\u754C\u77F3\u82F1\u77FF", true, false, false, true, false, false, false, 0, true),
                new Block(154, 0, "\u6F0F\u6597", false, false, false, false, true, false, false, 0, false),
                new Block(155, 15, "\u77F3\u82F1\u5757", false, false, false, false, false, false, false, 0, false),
                new Block(156, 15, "\u77F3\u82F1\u697C\u68AF", false, false, false, false, false, false, false, 0, false),
                new Block(157, 0, "\u6FC0\u6D3B\u8F68\u9053", false, false, false, false, false, false, false, 0, false),
                new Block(158, 0, "\u6295\u63B7\u5668", false, false, false, false, true, false, false, 0, false),
                new Block(159, 15, "\u67D3\u8272\u9676\u74E6", true, false, false, false, false, false, false, 0, true),
                new Block(160, 0, "\u67D3\u8272\u73BB\u7483\u677F", false, false, false, false, false, false, false, 0, false),
                new Block(161, 1, "\u6811\u53F62", false, false, true, false, false, true, false, 0, true),
                new Block(162, 15, "\u6728\u677F2", false, false, false, false, false, true, false, 0, true),
                new Block(163, 15, "\u91D1\u5408\u6B22\u6728\u697C\u68AF", false, false, false, false, false, false, false, 0, false),
                new Block(164, 15, "\u6DF1\u8272\u6A61\u6728\u697C\u68AF", false, false, false, false, false, false, false, 0, false),
                new Block(165, 0, "\u7C98\u6DB2\u5757", false, false, false, false, false, false, false, 0, false),
                new Block(166, 0, "\u5C4F\u969C", false, false, false, false, false, false, false, 0, false),
                new Block(167, 0, "\u94C1\u6D3B\u677F\u95E8", false, false, false, false, false, false, false, 0, false),
                new Block(168, 15, "\u6D77\u6676\u77F3", false, false, false, false, false, false, false, 0, false),
                new Block(169, 15, "\u6D77\u6676\u706F", false, false, false, false, false, false, false, 15, false),
                new Block(170, 15, "\u5E72\u8349\u5757", false, false, false, false, false, false, false, 0, false),
                new Block(171, 0, "\u5730\u6BEF", false, false, false, false, false, false, false, 0, false),
                new Block(172, 15, "\u9676\u74E6", true, false, false, false, false, false, false, 0, true),
                new Block(173, 15, "\u7164\u70AD\u5757", false, false, false, false, false, false, false, 0, false),
                new Block(174, 15, "\u6D6E\u51B0", false, false, false, false, false, false, false, 0, true),
                new Block(175, 0, "\u82B1\u4E1B", false, true, true, false, false, false, true, 0, true),
                new Block(176, 0, "\u65D7\u5E1C", false, false, false, false, true, false, false, 0, false),
                new Block(177, 0, "\u60AC\u6302\u65D7\u5E1C", false, false, false, false, true, false, false, 0, false),
                new Block(178, 0, "\u6708\u5149\u63A2\u6D4B\u5668", false, false, false, false, true, false, false, 0, false),
                new Block(179, 15, "\u7EA2\u7802\u5CA9", true, false, false, false, false, false, false, 0, true),
                new Block(180, 15, "\u7EA2\u7802\u5CA9\u697C\u68AF", false, false, false, false, false, false, false, 0, false),
                new Block(181, 15, "\u53CC\u7EA2\u7802\u5CA9\u53F0\u9636", false, false, false, false, false, false, false, 0, false),
                new Block(182, 15, "\u7EA2\u7802\u5CA9\u53F0\u9636", false, false, false, false, false, false, false, 0, false),
                new Block(183, 0, "\u6A61\u6728\u6805\u680F\u95E8", false, false, false, false, false, false, false, 0, false),
                new Block(184, 0, "\u767D\u6866\u6728\u6805\u680F\u95E8", false, false, false, false, false, false, false, 0, false),
                new Block(185, 0, "\u4E1B\u6797\u6728\u6805\u680F\u95E8", false, false, false, false, false, false, false, 0, false),
                new Block(186, 0, "\u6DF1\u8272\u6A61\u6728\u6805\u680F\u95E8", false, false, false, false, false, false, false, 0, false),
                new Block(187, 0, "\u91D1\u5408\u6B22\u6728\u6805\u680F\u95E8", false, false, false, false, false, false, false, 0, false),
                new Block(188, 0, "\u6A61\u6728\u6805\u680F", false, false, false, false, false, false, false, 0, false),
                new Block(189, 0, "\u767D\u6866\u6728\u6805\u680F", false, false, false, false, false, false, false, 0, false),
                new Block(190, 0, "\u4E1B\u6797\u6728\u6805\u680F", false, false, false, false, false, false, false, 0, false),
                new Block(191, 0, "\u6DF1\u8272\u6A61\u6728\u6805\u680F", false, false, false, false, false, false, false, 0, false),
                new Block(192, 0, "\u91D1\u5408\u6B22\u6728\u6805\u680F", false, false, false, false, false, false, false, 0, false),
                new Block(193, 0, "\u6A61\u6728\u95E8", false, false, false, false, false, false, false, 0, false),
                new Block(194, 0, "\u767D\u6866\u6728\u95E8", false, false, false, false, false, false, false, 0, false),
                new Block(195, 0, "\u4E1B\u6797\u6728\u95E8", false, false, false, false, false, false, false, 0, false),
                new Block(196, 0, "\u91D1\u5408\u6B22\u6728\u95E8", false, false, false, false, false, false, false, 0, false),
                new Block(197, 0, "\u6DF1\u8272\u6A61\u6728\u95E8", false, false, false, false, false, false, false, 0, false),
                new Block(198, 0, "\u672B\u5730\u70DB", false, false, false, false, false, false, false, 14, false),
                new Block(199, 0, "\u7D2B\u9882\u690D\u7269", false, true, true, false, false, false, true, 0, true),
                new Block(200, 0, "\u7D2B\u9882\u82B1", false, true, true, false, false, false, true, 0, true),
                new Block(201, 15, "\u7D2B\u73C0\u5757", false, false, false, false, false, false, false, 0, false),
                new Block(202, 15, "\u7D2B\u73C0\u67F1", false, false, false, false, false, false, false, 0, false),
                new Block(203, 15, "\u7D2B\u73C0\u697C\u68AF", false, false, false, false, false, false, false, 0, false),
                new Block(204, 15, "\u53CC\u7D2B\u73C0\u53F0\u9636", false, false, false, false, false, false, false, 0, false),
                new Block(205, 15, "\u7D2B\u73C0\u53F0\u9636", false, false, false, false, false, false, false, 0, false),
                new Block(206, 15, "\u672B\u5730\u7816", false, false, false, false, false, false, false, 0, false),
                new Block(207, 0, "\u751C\u83DC\u6839", false, true, true, false, false, false, true, 0, false),
                new Block(208, 15, "\u8349\u5F84", true, false, false, false, false, false, false, 0, false),
                new Block(209, 15, "\u672B\u5730\u4F20\u9001\u95E8", false, false, false, false, false, false, false, 0, false),
                new Block(210, 15, "\u5FAA\u73AF\u547D\u4EE4\u65B9\u5757", false, false, false, false, false, false, false, 0, false),
                new Block(211, 15, "\u8FDE\u9501\u547D\u4EE4\u65B9\u5757", false, false, false, false, false, false, false, 0, false),
                new Block(212, 0, "\u971C\u51B0", false, false, false, false, false, false, false, 0, false),
                new Block(213, 0, "\u5CA9\u6D46\u5757", false, false, false, false, false, false, false, 0, false),
                new Block(214, 0, "\u4E0B\u754C\u75A3\u5757", false, false, false, false, false, false, false, 0, false),
                new Block(215, 0, "\u9AA8\u5757", false, false, false, false, false, false, false, 0, false),
                new Block(216, 0, "\u7ED3\u6784\u7A7A\u4F4D", false, false, false, false, false, false, false, 0, false),
                new Block(217, 0, "\u89C2\u6D4B\u5668", false, false, false, false, false, false, false, 0, false),
                new Block(218, 0, "\u767D\u8272\u6F5C\u5F71\u76D2", false, false, false, false, false, false, false, 0, false),
                new Block(219, 0, "\u6A59\u8272\u6F5C\u5F71\u76D2", false, false, false, false, false, false, false, 0, false),
                new Block(220, 0, "\u9EC4\u8272\u6F5C\u5F71\u76D2", false, false, false, false, false, false, false, 0, false),
                new Block(221, 0, "\u54C1\u7EA2\u8272\u6F5C\u5F71\u76D2", false, false, false, false, true, false, false, 0, false),
                new Block(222, 0, "\u6D45\u84DD\u8272\u6F5C\u5F71\u76D2", false, false, false, false, true, false, false, 0, false),
                new Block(223, 0, "\u9EC4\u8272\u6F5C\u5F71\u76D2", false, false, false, false, true, false, false, 0, false),
                new Block(224, 0, "\u9EC4\u7EFF\u8272\u6F5C\u5F71\u76D2", false, false, false, false, true, false, false, 0, false),
                new Block(225, 0, "\u7C89\u8272\u6F5C\u5F71\u76D2", false, false, false, false, true, false, false, 0, false),
                new Block(226, 0, "\u7070\u8272\u6F5C\u5F71\u76D2", false, false, false, false, true, false, false, 0, false),
                new Block(227, 0, "\u6D45\u7070\u8272\u6F5C\u5F71\u76D2", false, false, false, false, true, false, false, 0, false),
                new Block(228, 0, "\u9752\u8272\u6F5C\u5F71\u76D2", false, false, false, false, true, false, false, 0, false),
                new Block(229, 0, "\u7D2B\u8272\u6F5C\u5F71\u76D2", false, false, false, false, true, false, false, 0, false),
                new Block(230, 0, "\u84DD\u8272\u6F5C\u5F71\u76D2", false, false, false, false, true, false, false, 0, false),
                new Block(231, 0, "\u68D5\u8272\u6F5C\u5F71\u76D2", false, false, false, false, true, false, false, 0, false),
                new Block(232, 0, "\u7EFF\u8272\u6F5C\u5F71\u76D2", false, false, false, false, true, false, false, 0, false),
                new Block(233, 0, "\u7EA2\u8272\u6F5C\u5F71\u76D2", false, false, false, false, true, false, false, 0, false),
                new Block(234, 0, "\u9ED1\u8272\u6F5C\u5F71\u76D2", false, false, false, false, true, false, false, 0, false),
                new Block(235, 15, "\u767D\u8272\u5E26\u91C9\u9676\u74E6", false, false, false, false, false, false, false, 0, false),
                new Block(236, 15, "\u6A59\u8272\u5E26\u91C9\u9676\u74E6", false, false, false, false, false, false, false, 0, false),
                new Block(237, 15, "\u54C1\u7EA2\u8272\u5E26\u91C9\u9676\u74E6", false, false, false, false, false, false, false, 0, false),
                new Block(238, 15, "\u6D45\u84DD\u8272\u5E26\u91C9\u9676\u74E6", false, false, false, false, false, false, false, 0, false),
                new Block(239, 15, "\u9EC4\u8272\u5E26\u91C9\u9676\u74E6", false, false, false, false, false, false, false, 0, false),
                new Block(240, 15, "\u9EC4\u7EFF\u8272\u5E26\u91C9\u9676\u74E6", false, false, false, false, false, false, false, 0, false),
                new Block(241, 15, "\u7C89\u8272\u5E26\u91C9\u9676\u74E6", false, false, false, false, false, false, false, 0, false),
                new Block(242, 15, "\u7070\u8272\u5E26\u91C9\u9676\u74E6", false, false, false, false, false, false, false, 0, false),
                new Block(243, 15, "\u6D45\u7070\u8272\u5E26\u91C9\u9676\u74E6", false, false, false, false, false, false, false, 0, false),
                new Block(244, 15, "\u9752\u8272\u5E26\u91C9\u9676\u74E6", false, false, false, false, false, false, false, 0, false),
                new Block(245, 15, "\u7D2B\u8272\u5E26\u91C9\u9676\u74E6", false, false, false, false, false, false, false, 0, false),
                new Block(246, 15, "\u84DD\u8272\u5E26\u91C9\u9676\u74E6", false, false, false, false, false, false, false, 0, false),
                new Block(247, 15, "\u68D5\u8272\u5E26\u91C9\u9676\u74E6", false, false, false, false, false, false, false, 0, false),
                new Block(248, 15, "\u7EFF\u8272\u5E26\u91C9\u9676\u74E6", false, false, false, false, false, false, false, 0, false),
                new Block(249, 15, "\u7EA2\u8272\u5E26\u91C9\u9676\u74E6", false, false, false, false, false, false, false, 0, false),
                new Block(250, 15, "\u9ED1\u8272\u5E26\u91C9\u9676\u74E6", false, false, false, false, false, false, false, 0, false),
                new Block(251, 15, "\u6DF7\u51DD\u571F", false, false, false, false, false, false, false, 0, false),
                new Block(252, 15, "\u6DF7\u51DD\u571F\u7C89\u672B", false, false, false, false, false, false, false, 0, false),
                new Block(253, 0, null, false, false, false, false, false, false, false, 0, false),
                new Block(254, 0, null, false, false, false, false, false, false, false, 0, false),
                new Block(255, 15, "\u7ED3\u6784\u65B9\u5757", false, false, false, false, true, false, false, 0, false)
//                         ID, Tr,                      Display Name, Terra, Insub, VryIn, Resou, TileE, TreeR, Veget, Li, Natural
        }, 0, BLOCKS, 0, 256);

        for (int i = 256; i < 4096; i++) {
            BLOCKS[i] = new Block(i, 15, null, false, false, false, false, false, false, false, 0, false);
        }
    }

    public static final String[] BLOCK_TYPE_NAMES = new String[HIGHEST_KNOWN_BLOCK_ID + 1];
    public static final int[] BLOCK_TRANSPARENCY = new int[256];
    public static final int[] LIGHT_SOURCES = new int[256];

    static {
        for (int i = 0; i < 256; i++) {
            BLOCK_TYPE_NAMES[i] = BLOCKS[i].name;
            BLOCK_TRANSPARENCY[i] = BLOCKS[i].transparency;
            LIGHT_SOURCES[i] = BLOCKS[i].blockLight;
        }
    }

    public static final int CATEGORY_AIR           = 0;
    public static final int CATEGORY_FLUID         = 1;
    public static final int CATEGORY_INSUBSTANTIAL = 2;
    public static final int CATEGORY_MAN_MADE      = 3;
    public static final int CATEGORY_RESOURCE      = 4;
    public static final int CATEGORY_NATURAL_SOLID = 5;

    private static final long serialVersionUID = 3037884633022467720L;
}