package org.sexydock.tabs;

import javax.swing.*;
import java.awt.*;

public interface ITabContextMenuListener
{
    public void tabContextMenuTriggered(JTabbedPane tabbedPane, Component c, Point p);
}
