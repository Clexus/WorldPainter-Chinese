/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pepsoft.worldpainter.layers;

/**
 *
 * @author pepijn
 */
public class FloodWithLava extends Layer {
    private FloodWithLava() {
        super("\u5CA9\u6D46\u5DE5\u5177", "\u586B\u5145\u5CA9\u6D46", DataSize.BIT, false, 0);
    }

    public static final FloodWithLava INSTANCE = new FloodWithLava();

    private static final long serialVersionUID = 2011033001L;
}