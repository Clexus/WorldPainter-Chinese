/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pepsoft.worldpainter.layers;

/**
 *
 * @author pepijn
 */
public class Frost extends Layer {
    private Frost() {
        super("\u971C\u51BB", "\u7528\u96EA\u8986\u76D6\u5730\u8868\uFF0C\u5E76\u4F7F\u6C34\u7ED3\u51B0", DataSize.BIT, false, 60, 'o');
    }

    public static final Frost INSTANCE = new Frost();
    
    private static final long serialVersionUID = 2011032901L;
}
