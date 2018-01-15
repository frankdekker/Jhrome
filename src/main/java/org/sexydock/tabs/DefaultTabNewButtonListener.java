package org.sexydock.tabs;

import javax.swing.*;

public class DefaultTabNewButtonListener implements ITabNewButtonListener
{
    @Override
    public void tabNewButtonPressed(JTabbedPane tabbedPane)
    {
        tabbedPane.addTab("New tab", new JPanel());
    }
}
