package org.pepsoft.worldpainter.layers;

/**
 * Created by Pepijn on 15-1-2017.
 */
public class Caves extends Layer {
    private Caves() {
        super("org.pepsoft.Caves", "\u6D1E\u7A74", "\u751F\u6210\u5927\u5C0F\u53EF\u53D8\u7684\u5E95\u4E0B\u901A\u9053\u7C7B\u6D1E\u7A74", DataSize.NIBBLE, false, 23);
    }

    public static final Caves INSTANCE = new Caves();

    private static final long serialVersionUID = 1L;
}
