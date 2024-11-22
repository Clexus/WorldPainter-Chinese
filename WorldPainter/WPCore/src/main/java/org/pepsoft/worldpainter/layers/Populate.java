/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pepsoft.worldpainter.layers;

/**
 *
 * @author pepijn
 */
public class Populate extends Layer {
    public Populate() {
        super("\u586B\u5145", "\u8BA9 Minecraft \u5728\u5730\u4E0A\u751F\u6210\u690D\u88AB\u3001\u96EA\u3001\u8D44\u6E90\uFF08\u7164\u77FF\u3001\u77FF\u77F3\u7B49\uFF09\u4EE5\u53CA\u6C34\u548C\u7194\u5CA9\u6C60\u3002", Layer.DataSize.BIT_PER_CHUNK, false, 0);
    }

    public static final Populate INSTANCE = new Populate();

    private static final long serialVersionUID = 2011040701L;
}