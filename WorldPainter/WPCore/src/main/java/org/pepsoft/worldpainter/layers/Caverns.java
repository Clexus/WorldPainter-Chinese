/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pepsoft.worldpainter.layers;

/**
 *
 * @author pepijn
 */
public class Caverns extends Layer {
    private Caverns() {
        super("\u6D1E\u7A9F", "\u751F\u6210\u5927\u5C0F\u53EF\u53D8\u7684\u5730\u4E0B\u6D1E\u7A9F", DataSize.NIBBLE, false, 20, 'c');
    }

    public static final Caverns INSTANCE = new Caverns();
    
    private static final long serialVersionUID = 2011040701L;
}