package org.sexydock.tabs;

import javax.swing.*;
import java.awt.*;

public interface ITabSelectionListener
{
    /**
     * When the user clicks with the mouse to activate the tab
     */
    public void tabSelected(JTabbedPane tabbedPane, Component c);

    /**
     * When the user drags out a tab, and this tab becomes active
     */
    public void tabActivated(JTabbedPane tabbedPane, Component c);
}
