/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.layers;

/**
 * A legacy (numbered) biome.
 *
 * @author pepijn
 */
public class Biome extends Layer {
    private Biome() {
        super("\u7FA4\u7CFB", "\u5C55\u793AMinecraft\u8981\u751F\u6210\u7684\u7FA4\u7CFB", Layer.DataSize.BYTE, true, 70);
    }

    @Override
    public int getDefaultValue() {
        return 255;
    }

    public static final Biome INSTANCE = new Biome();

    private static final long serialVersionUID = -5510962172433402363L;
}