/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pepsoft.worldpainter.layers;

/**
 *
 * @author pepijn
 */
public class Chasms extends Layer {
    private Chasms() {
        super("org.pepsoft.Chasms", "\u5CE1\u8C37", "\u751F\u6210\u5927\u5C0F\u53EF\u53D8\u7684\u5E95\u4E0B\u5CE1\u8C37", DataSize.NIBBLE, false, 21);
    }

    public static final Chasms INSTANCE = new Chasms();
    
    private static final long serialVersionUID = 1L;
}