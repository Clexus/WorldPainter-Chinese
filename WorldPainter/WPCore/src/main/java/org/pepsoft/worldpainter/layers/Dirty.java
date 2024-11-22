/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.layers;

/**
 *
 * @author pepijn
 */
public class Dirty extends Layer {
    private Dirty() {
        super("\u810F\u533A\u5757", "\u8FFD\u8E2A\u81EA\u5BFC\u5165\u5730\u56FE\u4EE5\u6765\u5DF2\u88AB\u4FEE\u6539\u7684\u533A\u5757", Layer.DataSize.BIT_PER_CHUNK, true, 0);
    }
    
    public static final Dirty INSTANCE = new Dirty();
    
    private static final long serialVersionUID = 2011071701L;
}