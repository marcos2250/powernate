package marcos2250.powernate.window;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import marcos2250.powernate.executors.ImportadorHibernateMetadata;
import marcos2250.powernate.util.PowernateSessionMediator;

public class ConfigurationWindow extends JInternalFrame {

    private static final long serialVersionUID = -6366676020087825029L;

    private final PowernateSessionMediator config;
    
    private JComboBox<String> cboDialect;
    private JTextField txtScanPackages;
    private JTextField txtSchema;
    private JTextField txtTablespace;
    private JTextField txtEnversControlTable;
    private JTextField txtEnversControlTableIdColumn;
    private JTextField txtEnversTypeControlTable;
    private JTextField txtEnversTypeControlTableIdColumn;
    private JTextField txtDefaultUserName;
    private JTextField txtDefaultUserGroupName;
    private JTextField txtDefaultUserGroupNameReadOnly;
    private JTextField txtDefaultETLGroupName;

    public ConfigurationWindow(final MainWindow mainWindow) {
        super("Project Configuration", true, false, false, false);
        setSize(640, 240);
        setLayout(new FlowLayout());

        config = new PowernateSessionMediator();
        mainWindow.setConfig(config);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Database", null, createDatabaseTab(), "Basic DB setup");
        tabbedPane.addTab("Users", null, createUsersTab(), "User group setup");
        tabbedPane.addTab("Envers", null, createEnversTab(), "Envers setup");
        add(tabbedPane);

        add(createButtons(mainWindow));
    }

    private Component createEnversTab() {
        JComponent panel1 = makePanel();

        panel1.add(new JLabel("Control table:"));
        txtEnversControlTable = new JTextField(config.getEnversControlTable());
        panel1.add(txtEnversControlTable);

        panel1.add(new JLabel("Id column:"));
        txtEnversControlTableIdColumn = new JTextField(config.getEnversControlTableIdColumn());
        panel1.add(txtEnversControlTableIdColumn);

        panel1.add(new JLabel("Type control table:"));
        txtEnversTypeControlTable = new JTextField(config.getEnversTypeControlTable());
        panel1.add(txtEnversTypeControlTable);

        panel1.add(new JLabel("Type control Id column:"));
        txtEnversTypeControlTableIdColumn = new JTextField(config.getEnversTypeControlTableIdColumn());
        panel1.add(txtEnversTypeControlTableIdColumn);
        
        return panel1;
    }

    private Component createUsersTab() {
        JComponent panel1 = makePanel();

        panel1.add(new JLabel("Default user name:"));
        txtDefaultUserName = new JTextField(config.getDefaultUserName());
        panel1.add(txtDefaultUserName);

        panel1.add(new JLabel("Default user group:"));
        txtDefaultUserGroupName = new JTextField(config.getDefaultUserGroupName());
        panel1.add(txtDefaultUserGroupName);

        panel1.add(new JLabel("Read only user group:"));
        txtDefaultUserGroupNameReadOnly = new JTextField(config.getDefaultUserGroupNameReadOnly());
        panel1.add(txtDefaultUserGroupNameReadOnly);

        panel1.add(new JLabel("ETL group:"));
        txtDefaultETLGroupName = new JTextField(config.getDefaultETLGroupName());
        panel1.add(txtDefaultETLGroupName);

        return panel1;
    }

    private JComponent createDatabaseTab() {
        JComponent panel1 = makePanel();

        panel1.add(new JLabel("Schema name:"));
        txtSchema = new JTextField(config.getDefaultSchema());
        panel1.add(txtSchema);

        panel1.add(new JLabel("Tablespace:"));
        txtTablespace = new JTextField(config.getDefaultTableSpace());
        panel1.add(txtTablespace);

        panel1.add(new JLabel("Scan packages:"));
        txtScanPackages = createTextScanPackages(config);
        panel1.add(txtScanPackages);

        panel1.add(new JLabel("SQL Dialect:"));
        cboDialect = createDialectCombo(config);
        panel1.add(cboDialect);

        return panel1;
    }

    private JPanel createButtons(final MainWindow mainWindow) {
        JPanel btnPanel = makePanel();

        JButton btn1 = new JButton("Import");
        btn1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                proceedImport(mainWindow, config);
            }
        });
        btnPanel.add(btn1);

        JButton btn2 = new JButton("Close");
        btn2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ConfigurationWindow.this.setVisible(false);
            }
        });
        btnPanel.add(btn2);
        return btnPanel;
    }

    protected JPanel makePanel() {
        JPanel panel = new JPanel(false);
        GridLayout layout = new GridLayout(0, 2);
        layout.setHgap(10);
        layout.setVgap(10);
        panel.setLayout(layout);
        return panel;
    }

    private void proceedImport(final MainWindow mainWindow, final PowernateSessionMediator config) {
        config.setScanEntityPackagePrefix(txtScanPackages.getText());
        config.setDialectClassName(cboDialect.getSelectedItem().toString());
        config.setSchema(txtSchema.getText());
        config.setTablespace(txtTablespace.getText());

        config.setEnversControlTable(txtEnversControlTable.getText());
        config.setEnversControlTableIdColumn(txtEnversControlTableIdColumn.getText());
        config.setEnversTypeControlTable(txtEnversTypeControlTable.getText());
        config.setEnversTypeControlTableIdColumn(txtEnversTypeControlTableIdColumn.getText());

        config.setDefaultETLGroupName(txtDefaultETLGroupName.getText());
        config.setDefaultUserGroupName(txtDefaultUserGroupName.getText());
        config.setDefaultUserGroupNameReadOnly(txtDefaultUserGroupNameReadOnly.getText());
        config.setDefaultUserName(txtDefaultUserName.getText());

        config.initialize();
        try {
            new ImportadorHibernateMetadata().executar(mainWindow, config);
            JOptionPane.showMessageDialog(ConfigurationWindow.this, "Import done!");
        } catch (Exception e) {
            e.printStackTrace();
            mainWindow.setStatusText("Runtime Error - see Console");
        }
    }

    private JTextField createTextScanPackages(final PowernateSessionMediator config) {
        String packageName = "";
        if (config.getScanEntityPackagePrefix() != null) {
            packageName = config.getScanEntityPackagePrefix();
        }
        return new JTextField(packageName.trim());
    }

    private JComboBox<String> createDialectCombo(final PowernateSessionMediator config) {
        JComboBox<String> cbo = new JComboBox<String>(getSQLDialects());
        if (config.getDialect() != null) {
            cbo.setSelectedItem(config.getDialect().getClass().getName());
        }
        return cbo;
    }

    private String[] getSQLDialects() {
        return new String[] { "org.hibernate.dialect.DB2Dialect", //
                "org.hibernate.dialect.DerbyDialect", //
                "org.hibernate.dialect.FirebirdDialect", //
                "org.hibernate.dialect.H2Dialect", //
                "org.hibernate.dialect.HSQLDialect", //
                "org.hibernate.dialect.MySQLDialect", //
                "org.hibernate.dialect.OracleDialect", //
                "org.hibernate.dialect.PostgreSQLDialect", //
                "org.hibernate.dialect.SQLServerDialect", //
        };
    }

}
