package org.pepsoft.worldpainter.layers;

public class NotPresentBlock extends Layer {
    private NotPresentBlock() {
        super(NotPresentBlock.class.getName(), "\u6D88\u5931\u65B9\u5757\u6807\u8BB0", "\u6807\u8BB0\u88AB\u8BA4\u4E3A\u4E0D\u5B58\u5728\u7684\u65B9\u5757", DataSize.BIT, false, 91);
    }

    public static final NotPresentBlock INSTANCE = new NotPresentBlock();

    private static final long serialVersionUID = 1L;
}