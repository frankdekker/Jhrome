package org.sexydock.tabs.demos;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.sexydock.tabs.*;
import org.sexydock.tabs.jhrome.JhromeTabbedPaneUI;

@SuppressWarnings("serial")
public class NotepadDemo extends JFrame implements ISexyTabsDemo, ITabbedPaneWindow, ITabbedPaneWindowFactory, ITabFactory {
    private ImageIcon image;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new NotepadDemo().start();
            }
        });
    }

    public NotepadDemo() {
        image = new ImageIcon("C:\\Projects\\Jhrome\\drs16.png");
        initGUI();
    }

    private void initGUI() {
        setTitle("Notepad");

        tabbedPane = new JTabbedPane();
        tabbedPane.setUI(new JhromeTabbedPaneUI());
        tabbedPane.putClientProperty(JhromeTabbedPaneUI.NEW_TAB_BUTTON_VISIBLE, true);
        tabbedPane.putClientProperty(JhromeTabbedPaneUI.USE_UNIFORM_WIDTH, false);
        tabbedPane.putClientProperty(JhromeTabbedPaneUI.TAB_CLOSE_BUTTONS_VISIBLE, true);
        tabbedPane.putClientProperty(JhromeTabbedPaneUI.TAB_DROP_FAILURE_HANDLER, new DefaultTabDropFailureHandler(this));
        tabbedPane.putClientProperty(JhromeTabbedPaneUI.TAB_FACTORY, this);
        tabbedPane.putClientProperty(JhromeTabbedPaneUI.FLOATING_TAB_HANDLER, new DefaultFloatingTabHandler());
        tabbedPane.putClientProperty(JhromeTabbedPaneUI.TAB_CLOSE_BUTTONS_VISIBLE, true);
        tabbedPane.putClientProperty(JhromeTabbedPaneUI.MAX_UNIFORM_WIDTH, 200);
        tabbedPane.putClientProperty(JhromeTabbedPaneUI.TAB_NEW_BUTTON_LISTENER, new ITabNewButtonListener() {
            public void tabNewButtonPressed(JTabbedPane tabbedPane) {
                NotepadPane pane = new NotepadPane();
                tabbedPane.addTab(pane.getTitle(), image, pane);
                tabbedPane.setSelectedComponent(pane);
            }
        });
        tabbedPane.putClientProperty(JhromeTabbedPaneUI.DND_POLICY, new ITabbedPaneDndPolicy() {
            @Override
            public boolean isTearAwayAllowed(JTabbedPane tabbedPane, Tab tab) {
                return true;
            }

            @Override
            public boolean isSnapInAllowed(JTabbedPane tabbedPane, Tab tab) {
                return tab.getContent() instanceof NotepadPane;
            }
        });
        tabbedPane.putClientProperty(JhromeTabbedPaneUI.TAB_CONTEXTMENU_LISTENER, new ITabContextMenuListener() {
            @Override
            public void tabContextMenuTriggered(JTabbedPane tabbedPane, Component c, Point p) {
                System.out.println("show jpopup menu");
            }
        });
        tabbedPane.putClientProperty(JhromeTabbedPaneUI.TAB_SELECTION_LISTENER, new ITabSelectionListener() {
            @Override
            public void tabSelected(JTabbedPane tabbedPane, Component c) {
                System.out.println("tab selected");
            }

            @Override
            public void tabActivated(JTabbedPane tabbedPane, Component c) {
                System.out.println("tab activated");
            }

            @Override
            public void tabRemoved(JTabbedPane tabbedPane, Component c) {
                System.out.println("tab removed");
            }
        });

        getContentPane().add(tabbedPane, BorderLayout.CENTER);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        addWindowListener(new DefaultWindowsClosedHandler());

        saveAction = new SaveAction();
        JMenuItem   openItem   = new JMenuItem(new OpenAction());
        JMenuItem   saveItem   = new JMenuItem(saveAction);
        JMenuItem   saveAsItem = new JMenuItem(new SaveAsAction());
        final JMenu fileMenu   = new JMenu("File");
        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.add(saveAsItem);
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);

        tabbedPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                updateTitle();
                saveAction.update();

                fileMenu.setEnabled(tabbedPane.getSelectedComponent() instanceof NotepadPane);
            }
        });

        tabbedPane.addContainerListener(new ContainerAdapter() {
            @Override
            public void componentRemoved(ContainerEvent e) {
                if (tabbedPane.getTabCount() == 0) {
                    dispose();
                }
            }
        });

        tabbedPane.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("indexForTitle".equals(evt.getPropertyName())) {
                    updateTitle();
                }
            }
        });

    }

    private void updateTitle() {
        int index = tabbedPane.getSelectedIndex();
        setTitle(index < 0 ? "Notepad" : "Notepad - " + tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()));
    }

    private JTabbedPane tabbedPane;
    private SaveAction  saveAction;

    private static class NotepadPane extends JPanel {
        public NotepadPane() {
            initGUI();
        }

        private JTextArea   textArea;
        private JScrollPane textScrollPane;

        private String savedText = "";
        private boolean dirty;

        private File file;

        private void initGUI() {
            textArea = new JTextArea();
            textArea.setLineWrap(true);
            textScrollPane = new JScrollPane(textArea);
            textScrollPane.setPreferredSize(new Dimension(800, 600));

            setLayout(new BorderLayout());
            add(textScrollPane, BorderLayout.CENTER);

            textArea.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void removeUpdate(DocumentEvent e) {
                    updateDirty();
                }

                @Override
                public void insertUpdate(DocumentEvent e) {
                    updateDirty();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    updateDirty();
                }
            });
        }

        private void updateDirty() {
            boolean newDirty = !textArea.getText().equals(savedText);
            if (dirty != newDirty) {
                dirty = newDirty;
                updateTabTitle();
                NotepadDemo notepadDemo = getNotepadDemo();
                if (notepadDemo != null) {
                    notepadDemo.saveAction.update();
                }
            }
        }

        public boolean isDirty() {
            return dirty;
        }

        public File getFile() {
            return file;
        }

        public void open(File file) throws IOException {
            InputStream is = null;
            try {
                is = new FileInputStream(file);
                byte[] data = new byte[is.available()];
                is.read(data);
                textArea.setText(new String(data));
                savedText = textArea.getText();
                this.file = file;
                updateDirty();
                updateTabTitle();
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }

        public void saveTo(File destFile) throws IOException {
            BufferedWriter writer = null;
            try {
                writer = new BufferedWriter(new FileWriter(destFile));
                writer.write(textArea.getText());
            } finally {
                if (writer != null) {
                    writer.close();
                }
            }
            file = destFile;
            savedText = textArea.getText();
            updateDirty();
            updateTabTitle();
        }

        public void save() throws IOException {
            saveTo(file);
        }

        public JTextArea getTextArea() {
            return textArea;
        }

        private void updateTabTitle() {
            JTabbedPane tabbedPane = getTabbedPane();
            if (tabbedPane != null) {
                int index = tabbedPane.indexOfComponent(this);
                if (index >= 0) {
                    tabbedPane.setTitleAt(index, getTitle());
                }
            }
        }

        public String getTitle() {
            return (isDirty() ? "*" : "") + (file == null ? "Untitled document" : file.getName());
        }

        private JTabbedPane getTabbedPane() {
            Component c = getParent();
            while (c != null) {
                if (c instanceof JTabbedPane) {
                    return (JTabbedPane) c;
                }
                c = c.getParent();
            }
            return null;
        }

        private NotepadDemo getNotepadDemo() {
            Component c = getParent();
            while (c != null) {
                if (c instanceof NotepadDemo) {
                    return (NotepadDemo) c;
                }
                c = c.getParent();
            }
            return null;
        }
    }

    private class OpenAction extends AbstractAction {
        public OpenAction() {
            super("Open...");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            NotepadPane currentPane = (NotepadPane) tabbedPane.getSelectedComponent();
            File        file        = currentPane.getFile();

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Text Files (*.txt)", "txt"));
            fileChooser.setAcceptAllFileFilterUsed(true);
            if (file != null) {
                fileChooser.setCurrentDirectory(file.getParentFile());
            }

            int choice = fileChooser.showOpenDialog(NotepadDemo.this);

            if (choice == JFileChooser.APPROVE_OPTION) {
                try {
                    NotepadPane pane = currentPane;
                    if (currentPane.getFile() != null || currentPane.isDirty()) {
                        pane = new NotepadPane();
                    }
                    pane.open(fileChooser.getSelectedFile());
                    if (pane != currentPane) {
                        tabbedPane.addTab(pane.getTitle(), image, pane);
                        tabbedPane.setSelectedComponent(pane);
                    }
                } catch (IOException e1) {
                    handleException("Failed to open file; ", e1);
                }
            }
        }
    }

    private class SaveAction extends AbstractAction {
        public SaveAction() {
            super("Save");
        }

        public void update() {
            if (tabbedPane.getSelectedComponent() instanceof NotepadPane) {
                NotepadPane currentPane = (NotepadPane) tabbedPane.getSelectedComponent();
                setEnabled(currentPane != null && currentPane.getFile() != null && currentPane.isDirty());
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            NotepadPane currentPane = (NotepadPane) tabbedPane.getSelectedComponent();
            try {
                currentPane.save();
            } catch (IOException e1) {
                handleException("Failed to save file", e1);
            }
        }
    }

    private class SaveAsAction extends AbstractAction {
        public SaveAsAction() {
            super("Save As...");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            NotepadPane currentPane = (NotepadPane) tabbedPane.getSelectedComponent();
            File        file        = currentPane.getFile();

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Text Files (*.txt)", "txt"));
            fileChooser.setAcceptAllFileFilterUsed(true);
            fileChooser.setSelectedFile(file);

            int choice = fileChooser.showSaveDialog(NotepadDemo.this);

            if (choice == JFileChooser.APPROVE_OPTION) {
                try {
                    currentPane.saveTo(fileChooser.getSelectedFile());
                } catch (IOException e1) {
                    handleException("Failed to save file; ", e1);
                }
            }
        }
    }

    private void handleException(String message, Exception e) {
        JOptionPane.showConfirmDialog(this, message + e.getLocalizedMessage(), "I/O error", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public Tab createTab() {
        return new Tab();
    }

    @Override
    public Tab createTabWithContent() {
        Tab tab = new Tab();
        tab.setTitle("Untitled");
        tab.setIcon(getImageIcon("agenda_16.png"));
        tab.setContent(new NotepadPane());
        return tab;
    }

    public static ImageIcon getImageIcon(String image) {
        try {
            InputStream resourceAsStream = NotepadDemo.class.getResourceAsStream(image);
            if (resourceAsStream == null) {
                return null;
            }

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            int    nRead;
            byte[] data = new byte[16384];

            while ((nRead = resourceAsStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }

            buffer.flush();

            resourceAsStream.close();
            buffer.close();

            return new ImageIcon(buffer.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ITabbedPaneWindow createWindow() {
        return new NotepadDemo();
    }

    @Override
    public JTabbedPane getTabbedPane() {
        return tabbedPane;
    }

    @Override
    public Window getWindow() {
        return this;
    }

    @Override
    public void start() {
        NotepadDemo notepadDemo = new NotepadDemo();
        Tab         newTab      = notepadDemo.createTabWithContent();
        notepadDemo.getTabbedPane().addTab(newTab.getTitle(), newTab.getIcon(), newTab.getContent());
        notepadDemo.pack();
        notepadDemo.setLocationRelativeTo(null);
        notepadDemo.setVisible(true);
        NotepadPane notepadPane = (NotepadPane) newTab.getContent();
        notepadPane.getTextArea().requestFocus();
    }
}
