/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pepsoft.worldpainter.heightMaps.gui;

import org.pepsoft.worldpainter.HeightMap;
import org.pepsoft.worldpainter.heightMaps.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;

/**
 *
 * @author pepijn
 */
public class HeightMapPropertiesPanel extends JPanel {
    public HeightMapPropertiesPanel() {
        setLayout(new GridBagLayout());
    }

    public HeightMap getHeightMap() {
        return heightMap;
    }

    public void setHeightMap(HeightMap heightMap) {
        this.heightMap = heightMap;
        removeAll();
        addField("\u7C7B\u578B:", heightMap.getClass().getSimpleName());
        addField("\u540D\u79F0:", heightMap, "name");
        if (heightMap instanceof ConstantHeightMap) {
            addField("\u9AD8\u5EA6: ", heightMap, "height");
        } else if (heightMap instanceof NinePatchHeightMap) {
            addField("\u9AD8\u5EA6:", heightMap, "height");
            addField("\u6C34\u5E73\u6709\u6548\u5C3A\u5BF8:", heightMap, "innerSizeX", 0, null);
            addField("\u5782\u76F4\u6709\u6548\u5C3A\u5BF8:", heightMap, "innerSizeY", 0, null);
            addField("\u8FB9\u754C\u5C3A\u5BF8:", heightMap, "borderSize", 0, null);
            addField("\u6D77\u5CB8\u7EBF\u5C3A\u5BF8:", heightMap, "coastSize", 0, null);
        } else if (heightMap instanceof NoiseHeightMap) {
            addField("\u9AD8\u5EA6:", heightMap, "height", 0f, null);
            addField("\u6BD4\u4F8B:", heightMap, "scale", 0.0, null);
            addField("\u566A\u58F0\u9891\u7387\u5C42\u7EA7\u53E0\u52A0\u6570\u91CF:", heightMap, "octaves", 1, 8);
        } else if (heightMap instanceof TransformingHeightMap) {
            addField("\u6C34\u5E73\u6BD4\u4F8B:", heightMap, "scaleX", 0, null);
            addField("\u5782\u76F4\u6BD4\u4F8B:", heightMap, "scaleY", 0, null);
            addField("\u6C34\u5E73\u504F\u79FB:", heightMap, "offsetX");
            addField("\u5782\u76F4\u504F\u79FB:", heightMap, "offsetY");
            addField("\u65CB\u8F6C:", heightMap, "rotation");
        } else if (heightMap instanceof BitmapHeightMap) {
            BufferedImage image = ((BitmapHeightMap) heightMap).getImage();
            int noOfChannels = image.getColorModel().getNumComponents();
            addField("\u901A\u9053\u6570:", heightMap, "channel", 0, noOfChannels - 1);
            addField("\u662F\u5426\u91CD\u590D\u94FA\u5C55:", heightMap, "repeat");
            addField("\u53CC\u4E09\u6B21\u63D2\u503C\u7F29\u653E:", heightMap, "smoothScaling");
        } else if (heightMap instanceof BandedHeightMap) {
            addField("\u5206\u6BB51\u957F\u5EA6:", heightMap, "segment1Length");
            addField("\u5206\u6BB51\u7ED3\u675F\u9AD8\u5EA6:", heightMap, "segment1EndHeight");
            addField("\u5206\u6BB52\u957F\u5EA6:", heightMap, "segment2Length");
            addField("\u5206\u6BB52\u7ED3\u675F\u9AD8\u5EA6:", heightMap, "segment2EndHeight");
            addField("\u5E73\u6ED1\u5EA6:", heightMap, "smooth");
        } else if (heightMap instanceof ShelvingHeightMap) {
            addField("\u53F0\u9636\u9AD8\u5EA6:", heightMap, "shelveHeight");
            addField("\u53F0\u9636\u5F3A\u5EA6:", heightMap, "shelveStrength");
        }
        double[] range = heightMap.getRange();
        addField("\u8303\u56F4:", "[" + range[0] + ", " + range[1] + "]");
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.weighty = 1.0;
        add(Box.createGlue(), constraints);
        validate();
        repaint();
    }

    public HeightMapListener getListener() {
        return listener;
    }

    public void setListener(HeightMapListener listener) {
        this.listener = listener;
    }

    private void addField(String name, String text) {
        addField(name, null, null, null, null, text);
    }

    private void addField(String name, Object bean, String propertyName) {
        addField(name, bean, propertyName, null, null, null);
    }

    private void addField(String name, Object bean, String propertyName, Number min, Number max) {
        addField(name, bean, propertyName, min, max, null);
    }

    private void addField(String name, Object bean, String propertyName, Number min, Number max, String text) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.insets = new Insets(2, 2, 2, 2);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.BASELINE_LEADING;
        JLabel label = new JLabel(name);
        add(label, constraints);
        if (text != null) {
            constraints.gridwidth = GridBagConstraints.REMAINDER;
            constraints.weightx = 1.0;
            JLabel valueLabel = new JLabel();
            valueLabel.setText(text);
            add(valueLabel, constraints);
            return;
        }
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(bean.getClass());
            for (PropertyDescriptor propertyDescriptor: beanInfo.getPropertyDescriptors()) {
                if (propertyDescriptor.getName().equalsIgnoreCase(propertyName)) {
                    constraints.gridwidth = GridBagConstraints.REMAINDER;
                    constraints.weightx = 1.0;
                    if (propertyDescriptor.getWriteMethod() == null) {
                        JLabel valueLabel = new JLabel();
                        Object value = propertyDescriptor.getReadMethod().invoke(bean);
                        if (value != null) {
                            valueLabel.setText(value.toString());
                        }
                        add(valueLabel, constraints);
                        return;
                    }
                    Class<?> propertyType = propertyDescriptor.getPropertyType();
                    if (propertyType == String.class) {
                        JTextField field = new JTextField();
                        field.setText((String) propertyDescriptor.getReadMethod().invoke(bean));
                        field.getDocument().addDocumentListener(new DocumentListener() {
                            @Override
                            public void insertUpdate(DocumentEvent event) {
                                updateProperty();
                            }

                            @Override
                            public void removeUpdate(DocumentEvent event) {
                                updateProperty();
                            }

                            @Override
                            public void changedUpdate(DocumentEvent event) {
                                updateProperty();
                            }

                            private void updateProperty() {
                                try {
                                    propertyDescriptor.getWriteMethod().invoke(bean, field.getText());
                                } catch (IllegalAccessException | InvocationTargetException e) {
                                    throw new RuntimeException(e);
                                }
                                updateListener(propertyName);
                            }
                        });
                        add(field, constraints);
                    } else if ((propertyType == boolean.class) || (propertyType == Boolean.class)) {
                        JCheckBox checkBox = new JCheckBox(" ");
                        checkBox.setSelected(Boolean.TRUE.equals(propertyDescriptor.getReadMethod().invoke(bean)));
                        checkBox.addActionListener(event -> {
                            try {
                                propertyDescriptor.getWriteMethod().invoke(bean, checkBox.isSelected());
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                throw new RuntimeException(e);
                            }
                            updateListener(propertyName);
                        });
                        add(checkBox, constraints);
                    } else if ((Number.class.isAssignableFrom(propertyType)) || (propertyType.isPrimitive() && (propertyType != boolean.class) && (propertyType != char.class))) {
                        JSpinner spinner = new JSpinner(new SpinnerNumberModel((Number) propertyDescriptor.getReadMethod().invoke(bean), (Comparable) min, (Comparable) max, 1));
                        spinner.addChangeListener(event -> {
                            try {
                                propertyDescriptor.getWriteMethod().invoke(bean, spinner.getValue());
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                throw new RuntimeException(e);
                            }
                            updateListener(propertyName);
                        });
                        add(spinner, constraints);
                    } else {
                        throw new IllegalArgumentException("Property " + propertyName + " of type " + propertyType.getSimpleName() + " not supported");
                    }
                    return;
                }
            }
            throw new IllegalArgumentException("Bean of type " + bean.getClass().getSimpleName() + " has no property named " + propertyName);
        } catch (IntrospectionException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void updateListener(String propertyName) {
        if (listener != null) {
            listener.heightMapChanged(heightMap, propertyName);
        }
    }

    private HeightMap heightMap;
    private HeightMapListener listener;

    public interface HeightMapListener {
        void heightMapChanged(HeightMap heightMap, String propertyName);
    }
}