/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.layers;

/**
 *
 * @author pepijn
 */
public class River extends Layer {
    private River() {
        super(River.class.getName(), "\u6CB3\u6D41", "\u751F\u6210\u4E00\u6761\u6709\u6CB3\u5E8A\u548C\u659C\u5761\u8FB9\u7F18\u7684\u6CB3\u6D41", DataSize.BIT, false, 25);
    }
    
    public static final River INSTANCE = new River();

    private static final long serialVersionUID = 1L;
}