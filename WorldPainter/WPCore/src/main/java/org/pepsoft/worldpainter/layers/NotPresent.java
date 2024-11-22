package org.pepsoft.worldpainter.layers;

/**
 * Technical layer which indicates that a chunk is to be considered not present
 * and should not be exported, merged or displayed in an editor or viewer.
 *
 * Created by Pepijn Schmitz on 30-06-15.
 */
public class NotPresent extends Layer {
    private NotPresent() {
        super(NotPresent.class.getName(), "\u6D88\u5931\u533A\u5757\u6807\u8BB0", "\u6807\u8BB0\u90A3\u4E9B\u5728\u5BFC\u5165\u4E16\u754C\u7684\u5730\u56FE\u4E2D\u4E0D\u5B58\u5728\u7684\u533A\u5757", DataSize.BIT_PER_CHUNK, false, 91);
    }

    public static final NotPresent INSTANCE = new NotPresent();

    private static final long serialVersionUID = 1L;
}