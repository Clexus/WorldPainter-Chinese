/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pepsoft.minecraft;

import org.pepsoft.worldpainter.gardenofeden.Garden;
import org.pepsoft.worldpainter.layers.Layer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author pepijn
 */
public interface ChunkFactory {
    /**
     * Create the chunk at the specified location, if present. No lighting need
     * be performed.
     *
     * @param x The X coordinate in the Minecraft coordinate space of the chunk
     *     to generate.
     * @param z The Z coordinate in the Minecraft coordinate space of the chunk
     *     to generate.
     * @return The generated chunk, in a data structure along with some
     *     statistics about it, or {@code null} if no chunk is present at
     *     the specified coordinates.
     */
    ChunkCreationResult createChunk(int x, int z);

    /**
     * Get the depth of the chunks this factory will create.
     *
     * @return The depth of the chunks this factory will create.
     */
    int getMinHeight();

    /**
     * Get the height of the chunks this chunk factory will create.
     *
     * @return The height of the chunks this factory will create.
     */
    int getMaxHeight();

    class ChunkCreationResult {
        public Chunk chunk;
        public final Stats stats = new Stats();
        public String errors, warnings;
    }

    class Stats {
        public long surfaceArea, landArea, waterArea, size, time;
        /**
         * Total time spent on each stage in nanoseconds. A stage can be:
         *
         * <ul>
         *     <li>A {@link Layer}
         *     <li>A {@link Stage}
         * </ul>
         */
        public final Map<Object, AtomicLong> timings = new ConcurrentHashMap<>();

    }
    
    enum Stage {
        /**
         * Creating the chunk and generating terrain and water or lava (excluding border and wall chunks).
         */
        TERRAIN_GENERATION("\u5E73\u539F", "\u751F\u6210\u5E73\u539F\u3001\u6C34\u548C\u5CA9\u6D46"),

        /**
         * Post-processing the generated chunks (including border and wall chunks).
         */
        POST_PROCESSING("\u540E\u5904\u7406", "\u540E\u5904\u7406\u6240\u6709\u533A\u5757"),

        /**
         * Creating border or wall chunks (including layers but excluding post-processing).
         */
        BORDER_CHUNKS("\u8FB9\u754C\u533A\u5757", "\u521B\u5EFA\u8FB9\u754C\u533A\u5757"),

        /**
         * Exporting the {@link Garden} seeds.
         */
        SEEDS("\u79CD\u5B50", "\u5BFC\u51FA\u79CD\u5B50"),

        /**
         * Calculating and propagating block properties such as lighting and leaf distances.
         */
        BLOCK_PROPERTIES( "\u65B9\u5757\u5C5E\u6027", "\u8BA1\u7B97\u5149\u7167\u548C/\u6216\u6811\u53F6\u8DDD\u79BB"),

        /**
         * Saving the generated chunks to disk.
         */
        DISK_WRITING ("\u4FDD\u5B58", "\u5C06\u533A\u5757\u4FDD\u5B58\u5230\u786C\u76D8"),

        /**
         * Applying region-straddling layers along region boundaries, not differentiated by layer.
         */
        FIXUPS("\u4FEE\u590D", "\u4FEE\u590D\u533A\u57DF\u8FB9\u754C");

        private final String name, description;

        Stage(String name, String description) {
            this.name = name;
            this.description = description;
        }
    }
}