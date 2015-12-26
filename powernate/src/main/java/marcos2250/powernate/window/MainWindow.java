package marcos2250.powernate.window;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyVetoException;

import javax.swing.BoxLayout;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

import marcos2250.powernate.executors.AbstractExecutor;
import marcos2250.powernate.executors.ExportadorComparacaoBancoHomologacao;
import marcos2250.powernate.executors.ExportadorDDLVisual;
import marcos2250.powernate.executors.GeradorGrafos;
import marcos2250.powernate.util.PowernateSessionMediator;
import marcos2250.powernate.vbscript.PowerDesignerVBScriptGenerator;

public class MainWindow extends JFrame implements ActionListener, JanelaNotificavel {

    private static final long serialVersionUID = -7821914581385199443L;

    private static final String APP_TITLE = "Powernate HBM-to-PDM converter";

    private static final String MENU_FILE = "File";
    private static final String MENU_SCAN = "Scan project...";
    private static final String MENU_GENERATE_GRAPH = "Generate visual arrangement...";
    private static final String MENU_SAVE = "Export to Powerdesigner";
    private static final String MENU_SAVE_COMPARE_DB = "Export comparison";
    private static final String MENU_EXIT = "Exit";
    private static final String MENU_HELP = "Help";
    private static final String MENU_ABOUT = "About...";

    private JDesktopPane desktop;

    private PowernateSessionMediator config = null;

    private ModelWindow modelWindow;

    private JLabel statusLabel;

    private AbstractExecutor executor = null;

    private boolean busy;

    private ConfigurationWindow configurationWindow;

    public MainWindow() {
        super(APP_TITLE);

        setBounds(320, 240, 800, 600);

        desktop = new JDesktopPane();
        desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
        add(desktop);

        setJMenuBar(createMenuBar());

        criarStatusBar();

        setStatusText("Click 'File' and 'Scan project...' to begin.");

        toFront();
        repaint();

        busy = false;
    }

    private void criarStatusBar() {
        JPanel statusPanel = new JPanel();
        statusPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));
        statusLabel = new JLabel();
        statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
        statusPanel.setPreferredSize(new Dimension(getWidth(), 16));
        statusPanel.add(statusLabel);
        add(statusPanel, java.awt.BorderLayout.SOUTH);
    }

    protected JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // menu arquivo
        menuBar.add(criaMenuArquivo());

        // menu ajuda
        menuBar.add(criaMenuAjuda());

        return menuBar;
    }

    private JMenu criaMenuArquivo() {

        JMenu menu = new JMenu(MENU_FILE);
        menu.setMnemonic(KeyEvent.VK_D);

        // novo
        JMenuItem menuItem = new JMenuItem(MENU_SCAN);
        menuItem.setMnemonic(KeyEvent.VK_S);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.ALT_MASK));
        menuItem.setActionCommand(MENU_SCAN);
        menuItem.addActionListener(this);
        menu.add(menuItem);

        // novo
        menuItem = new JMenuItem(MENU_GENERATE_GRAPH);
        menuItem.setMnemonic(KeyEvent.VK_G);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, ActionEvent.ALT_MASK));
        menuItem.setActionCommand(MENU_GENERATE_GRAPH);
        menuItem.addActionListener(this);
        menu.add(menuItem);

        // novo
        menuItem = new JMenuItem(MENU_SAVE_COMPARE_DB);
        menuItem.setMnemonic(KeyEvent.VK_H);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, ActionEvent.ALT_MASK));
        menuItem.setActionCommand(MENU_SAVE_COMPARE_DB);
        menuItem.addActionListener(this);
        menu.add(menuItem);

        // salvar
        menuItem = new JMenuItem(MENU_SAVE);
        menuItem.setMnemonic(KeyEvent.VK_S);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        menuItem.setActionCommand(MENU_SAVE);
        menuItem.addActionListener(this);
        menu.add(menuItem);

        // sair
        menuItem = new JMenuItem(MENU_EXIT);
        menuItem.setMnemonic(KeyEvent.VK_W);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK));
        menuItem.setActionCommand(MENU_EXIT);
        menuItem.addActionListener(this);
        menu.add(menuItem);

        return menu;
    }

    private JMenu criaMenuAjuda() {

        JMenu menuHelp = new JMenu(MENU_HELP);
        menuHelp.setMnemonic(KeyEvent.VK_J);

        // sobre
        JMenuItem menuItem = new JMenuItem(MENU_ABOUT);
        menuItem.setMnemonic(KeyEvent.VK_S);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.ALT_MASK));
        menuItem.setActionCommand(MENU_ABOUT);
        menuItem.addActionListener(this);
        menuHelp.add(menuItem);

        return menuHelp;
    }

    // acoes do menu
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        if (MENU_SAVE.equals(command)) {
            salvar();
        }

        if (MENU_SCAN.equals(command)) {
            importarModelo();
        }

        if (MENU_SAVE_COMPARE_DB.equals(command)) {
            compararHomologa();
        }

        if (MENU_GENERATE_GRAPH.equals(command)) {
            gerarGrafo();
        }

        if (MENU_EXIT.equals(command)) {
            quit();
        }

    }

    private void importarModelo() {
        if (!busy) {
            if (configurationWindow == null) {
                configurationWindow = new ConfigurationWindow(this);
                desktop.add(configurationWindow);
            }
            configurationWindow.setVisible(true);
            try {
                configurationWindow.setSelected(true);
            } catch (PropertyVetoException e) {
                e.printStackTrace();
            }
        }
    }

    private void compararHomologa() {
        if (!busy) {
            executor = new ExportadorComparacaoBancoHomologacao();
            executarCommandSelecionado();
        }
    }

    private void gerarGrafo() {
        if (!busy) {
            executor = new GeradorGrafos();
            executarCommandSelecionado();
            if (modelWindow == null) {
                modelWindow = new ModelWindow(this);
                desktop.add(modelWindow);
            }
            modelWindow.setVisible(true);
            try {
                modelWindow.setSelected(true);
            } catch (java.beans.PropertyVetoException e) {
                e.printStackTrace();
            }
            modelWindow.atualizarView();
        }
    }

    private void salvar() {
        if (!busy && config != null) {
            executor = new ExportadorDDLVisual();
            dialogoExportarDiagrama();
            executarCommandSelecionado();
        }
    }

    private void dialogoExportarDiagrama() {

        if (!busy && config != null && !config.isChangedGraph()) {
            return;
        }

        int n = JOptionPane.showConfirmDialog(this, //
                "Export new graph arrangement to Powerdesigner?", //
                "Graphical PDM model", //
                JOptionPane.YES_NO_OPTION);

        boolean resposta = n == 0;

        if (ExportadorDDLVisual.class.isInstance(executor)) {
            ((ExportadorDDLVisual) executor).setExportarGrafos(resposta);
        }
    }

    private void executarCommandSelecionado() {
        try {
            if (config == null || executor == null || busy) {
                return;
            }
            busy = true;
            executor.executar(this, config);
            busy = false;
        } catch (Exception e) {
            e.printStackTrace();
            setStatusText("Runtime Error - see Console");
            throw new RuntimeException(e);
        }
    }

    // sair
    protected void quit() {
        System.exit(0);
    }

    public PowernateSessionMediator getConfig() {
        return config;
    }

    public void setConfig(PowernateSessionMediator config) {
        this.config = config;
    }

    public PowerDesignerVBScriptGenerator getModeler() {
        if (config == null || config.getModeler() == null) {
            return null;
        }
        return config.getModeler();
    }

    public void setStatusText(String text) {
        this.statusLabel.setText(" " + text);
    }

    @Override
    public void notificar(String txt) {
        setStatusText(txt);
    }

}