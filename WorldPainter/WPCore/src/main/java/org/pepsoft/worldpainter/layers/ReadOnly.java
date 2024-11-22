/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.layers;

/**
 *  Technical layer which indicates that a chunk should not be merged when the
 *  world is merged with an existing map.
 *
 * @author pepijn
 */
public class ReadOnly extends Layer {
    public ReadOnly() {
        super("只读", "将区块标记为合并时不被修改", Layer.DataSize.BIT_PER_CHUNK, false, 90);
    }
    
    public static final ReadOnly INSTANCE = new ReadOnly();

    private static final long serialVersionUID = 2011042801L;
}