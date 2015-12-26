package marcos2250.powernate.window;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import marcos2250.powernate.executors.ImportadorHibernateMetadata;
import marcos2250.powernate.util.PowernateSessionMediator;

public class ConfigurationWindow extends JInternalFrame {

    private static final long serialVersionUID = -6366676020087825029L;
    private JTextField txtScanPackages;
    private JComboBox<String> cboDialect;
    private JTextField txtSchema;
    private JTextField txtTablespace;

    public ConfigurationWindow(final MainWindow mainWindow) {
        super("Project Configuration", false, false, false, false);
        setSize(640, 320);

        final PowernateSessionMediator config = new PowernateSessionMediator();
        mainWindow.setConfig(config);

        GridLayout layout = new GridLayout(0, 2);
        layout.setHgap(20);
        layout.setVgap(40);
        setLayout(layout);

        add(new JLabel("Schema name:"));
        txtSchema = new JTextField(config.getDefaultSchema());
        add(txtSchema);

        add(new JLabel("Tablespace:"));
        txtTablespace = new JTextField(config.getDefaultTableSpace());
        add(txtTablespace);

        add(new JLabel("Scan packages:"));
        txtScanPackages = createTextScanPackages(config);
        add(txtScanPackages);

        add(new JLabel("SQL Dialect:"));
        cboDialect = createDialectCombo(config);
        add(cboDialect);

        JButton btn1 = new JButton("Import");
        btn1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                proceedImport(mainWindow, config);
            }
        });
        add(btn1);

        JButton btn2 = new JButton("Close");
        btn2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ConfigurationWindow.this.setVisible(false);
            }
        });
        add(btn2);

    }

    private void proceedImport(final MainWindow mainWindow, final PowernateSessionMediator config) {
        config.setScanEntityPackagePrefix(txtScanPackages.getText());
        config.setDialect(cboDialect.getSelectedItem().toString());
        config.setSchema(txtSchema.getText());
        config.setTablespace(txtTablespace.getText());
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
