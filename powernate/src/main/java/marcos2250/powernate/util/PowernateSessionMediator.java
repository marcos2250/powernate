package marcos2250.powernate.util;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import marcos2250.powernate.comment.ColumnCommentAppender;
import marcos2250.powernate.comment.EnumCommentAppender;
import marcos2250.powernate.valentrim.CorretorDeScriptDDL;
import marcos2250.powernate.valentrim.Permission;
import marcos2250.powernate.vbscript.PowerDesignerVBScriptGenerator;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.SessionFactoryImplementor;

import com.google.common.collect.Maps;

public class PowernateSessionMediator {

    protected String defaultUserName;
    protected String defaultUserGroupName;
    protected String defaultUserGroupNameReadOnly;
    protected String defaultETLGroupName;
    protected String defaultTableSpace;

    protected String enversControlTable;
    protected String enversTypeControlTable;
    protected String enversControlTableIdColumn;
    protected int enversTableCode = 99;

    protected String defaultSchema;

    protected List<ProjectModulesParams> modulesList;

    protected int numerOfRows = 70000;

    protected boolean rearrangeGraphEnversTables = false;
    protected boolean generateSQLComments = false;

    private Dialect dialect;

    private Map<Class<?>, ColumnCommentAppender> typeCommentAppenderMap;

    private boolean changedGraph;
    private CorretorDeScriptDDL corretorDeScriptDDL;

    // hibernate
    private SessionFactory sessionFactory;
    private Configuration hibernateConfiguration;

    public void initialize() {

        Properties properties = new Properties();

        InputStream file = Thread.currentThread().getContextClassLoader().getResourceAsStream("powernate.properties");

        if (file != null) {
            try {
                properties.load(file);
            } catch (IOException e) {
                e.getMessage();
            }
        }

        readProperties(properties);

        loadModulesList(properties);

        startHibernate();
    }

    private void readProperties(Properties properties) {
        defaultUserName = properties.getProperty("defaultUserName");
        defaultUserGroupName = properties.getProperty("defaultUserGroupName");
        defaultUserGroupNameReadOnly = properties.getProperty("defaultUserGroupNameReadOnly");
        defaultETLGroupName = properties.getProperty("defaultETLGroupName");
        defaultTableSpace = properties.getProperty("defaultTableSpace");
        defaultSchema = properties.getProperty("defaultSchema");
        enversControlTable = properties.getProperty("enversControlTable");
        enversTypeControlTable = properties.getProperty("enversTypeControlTable");
        enversControlTableIdColumn = properties.getProperty("enversControlTableIdColumn");
    }

    private void loadModulesList(Properties properties) {
        modulesList = new ArrayList<ProjectModulesParams>();

        int i = 0;
        while (true) {
            String string = "module" + String.format("%02d", i);
            String moduleDefinition = properties.getProperty(string);
            if (moduleDefinition == null) {
                break;
            }
            String[] split = moduleDefinition.split(",");

            ProjectModulesParams modulesParams = //
            new ProjectModulesParams(i, split[0], Integer.parseInt(split[1]), Color.getColor(split[1]));

            modulesList.add(modulesParams);

            i++;
        }
    }

    private void startHibernate() {

        hibernateConfiguration = new Configuration();
        hibernateConfiguration.configure();
        sessionFactory = hibernateConfiguration.buildSessionFactory();
        sessionFactory.openSession();

        dialect = ((SessionFactoryImplementor) sessionFactory).getDialect();
    }

    public int getClassCode(String className) {
        if (className != null) {
            for (ProjectModulesParams module : getModules()) {
                if (className.contains(module.getDescription())) {
                    return module.getClassCode();
                }
            }
        }
        return 0;
    }

    public int getVBColor(String className) {
        if (className != null) {
            for (ProjectModulesParams module : getModules()) {
                if (className.contains(module.getDescription())) {
                    return module.getVbcolor();
                }
            }
        }
        return 0;
    }

    public Color getAWTColor(int classCode) {
        for (ProjectModulesParams module : getModules()) {
            if (module.getClassCode() == classCode) {
                return module.getColor();
            }
        }
        return Color.CYAN;
    }

    public List<ProjectModulesParams> getModules() {
        return modulesList;
    }

    public Dialect getDialect() {
        return dialect;
    }

    public void setDialect(Dialect dialect) {
        this.dialect = dialect;
    }

    public Map<Class<?>, ColumnCommentAppender> getColumnTypeToCommentAppender() {
        if (typeCommentAppenderMap == null) {
            typeCommentAppenderMap = createColumnTypeToCommentAppender();
        }
        return typeCommentAppenderMap;
    }

    public String getCreateSequenceString(String sequenceName) {
        return "create sequence " + sequenceName; // TODO especifico do dialeto do DB2
    }

    public String getPermissionGroupName(Permission write) {
        if (Permission.WRITE.equals(write)) {
            return getDefaultUserGroupName();
        }
        return getDefaultUserGroupNameReadOnly();
    }

    public String getDefaultUserName() {
        return defaultUserName;
    }

    public String getDefaultUserGroupNameReadOnly() {
        return defaultUserGroupNameReadOnly;
    }

    public String getDefaultETLGroupName() {
        return defaultETLGroupName;
    }

    public String getDefaultTableSpace() {
        return defaultTableSpace;
    }

    public String getDefaultUserGroupName() {
        return defaultUserGroupName;
    }

    public String getEnversControlTable() {
        return enversControlTable;
    }

    public String getEnversControlTableIdColumn() {
        return enversControlTableIdColumn;
    }

    public int getEnversTableCode() {
        return enversTableCode;
    }

    public String getEnversTypeControlTable() {
        return enversTypeControlTable;
    }

    public List<ProjectModulesParams> getModulesList() {
        return modulesList;
    }

    public int getNumerOfRows() {
        return numerOfRows;
    }

    public boolean isRearrangeGraphEnversTables() {
        return rearrangeGraphEnversTables;
    }

    public boolean isGenerateSQLComments() {
        return generateSQLComments;
    }

    public String getDefaultSchema() {
        return defaultSchema;
    }

    public CorretorDeScriptDDL getCorretorDeScriptDDL() {
        return corretorDeScriptDDL;
    }

    public boolean isChangedGraph() {
        return changedGraph;
    }

    public PowerDesignerVBScriptGenerator getModeler() {
        if (corretorDeScriptDDL == null || corretorDeScriptDDL.getValentrim() == null) {
            return null;
        }
        return corretorDeScriptDDL.getValentrim().getGeradorVBScript();
    }

    public void setCorretorDeScriptDDL(CorretorDeScriptDDL corretorDeScriptDDL) {
        this.corretorDeScriptDDL = corretorDeScriptDDL;
    }

    protected Map<Class<?>, ColumnCommentAppender> createColumnTypeToCommentAppender() {

        Map<Class<?>, ColumnCommentAppender> columnTypeToCommentAppender = Maps.newHashMap();

        EnumCommentAppender enumCommentAppender = new EnumCommentAppender();

        columnTypeToCommentAppender.put(Enum.class, enumCommentAppender);

        return columnTypeToCommentAppender;
    }

    public void setGeneratedGraphs(boolean changedGraph) {
        this.changedGraph = changedGraph;
    }

    public Configuration getConfiguration() {
        return hibernateConfiguration;
    }

    @SuppressWarnings("deprecation")
    public Connection getConnection() {
        return sessionFactory.getCurrentSession().connection();
    }

}
