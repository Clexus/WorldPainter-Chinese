/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.layers;

/**
 *
 * @author pepijn
 */
public class Resources extends Layer {
    private Resources() {
        super("\u8D44\u6E90", "\u5730\u4E0B\u7684\u7164\u77FF\u3001\u77FF\u77F3\u3001\u6C99\u783E\u3001\u6CE5\u571F\u3001\u7194\u5CA9\u548C\u6C34\u7B49\u77FF\u85CF", DataSize.NIBBLE, false, 10, 'r');
    }
    
    public static final Resources INSTANCE = new Resources();
    
    private static final long serialVersionUID = 1L;
}