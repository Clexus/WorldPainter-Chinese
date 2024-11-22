/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.layers;

/**
 *
 * @author pepijn
 */
public class Void extends Layer {
    private Void() {
        super("\u865A\u7A7A", "\u76F4\u63A5\u6295\u5165\u865A\u65E0", DataSize.BIT, false, 0);
    }
    
    public static final Void INSTANCE = new Void();
    
    private static final long serialVersionUID = 2011100801L;
}