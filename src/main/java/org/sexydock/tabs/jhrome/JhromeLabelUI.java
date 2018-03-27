package org.sexydock.tabs.jhrome;

import javax.swing.*;
import javax.swing.plaf.basic.BasicLabelUI;
import java.awt.*;

public class JhromeLabelUI extends BasicLabelUI {

    private int mMinWidth = 50;

    @Override
    public void paint(Graphics g, JComponent c) {
        JLabel label = (JLabel)c;
        Icon icon = label.getIcon();

        if (icon == null) {
            super.paint(g, c);
            return;
        }

        int width = label.getWidth();
        if (width < mMinWidth) {
            label.setIcon(null);
        }

        super.paint(g, c);

        if (width <= mMinWidth) {
            label.setIcon(icon);
        }
    }
}
