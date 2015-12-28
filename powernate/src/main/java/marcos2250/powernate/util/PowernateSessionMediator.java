package marcos2250.powernate.util;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import marcos2250.powernate.comment.ColumnCommentAppender;
import marcos2250.powernate.comment.EnumCommentAppender;
import marcos2250.powernate.valentrim.CorretorDeScriptDDL;
import marcos2250.powernate.valentrim.Permission;
import marcos2250.powernate.vbscript.PowerDesignerVBScriptGenerator;

import org.apache.commons.lang.StringUtils;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.Dialect;
import org.reflections.Reflections;

import com.google.common.collect.Maps;

public class PowernateSessionMediator {

    protected String defaultUserName;
    protected String defaultUserGroupName;
    protected String defaultUserGroupNameReadOnly;
    protected String defaultETLGroupName;
    protected String defaultTableSpace;

    protected String enversControlTable;
    protected String enversControlTableIdColumn;
    protected String enversTypeControlTable;
    protected String enversTypeControlTableIdColumn;
    protected int enversTableCode = 99;

    protected String defaultSchema;
    protected String scanEntityPackagePrefix;

    protected List<ProjectModulesParams> modulesList;

    protected int numerOfRows = 70000;

    protected boolean rearrangeGraphEnversTables = false;
    protected boolean generateSQLComments = false;

    private String dialectClassName;
    private Dialect dialect;

    private Map<Class<?>, ColumnCommentAppender> typeCommentAppenderMap;

    private boolean changedGraph;
    private CorretorDeScriptDDL corretorDeScriptDDL;

    // hibernate
    private SessionFactory sessionFactory;
    private Configuration hibernateConfiguration;

    public PowernateSessionMediator() {
        Properties properties = new Properties();

        InputStream file = Thread.currentThread().getContextClassLoader().getResourceAsStream("powernate.properties");

        if (file != null) {
            try {
                properties.load(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        readProperties(properties);

        loadModulesList(properties);
    }

    public void initialize() {
        startHibernate();
    }

    private void readProperties(Properties properties) {
        defaultUserName = properties.getProperty("defaultUserName");
        defaultUserGroupName = properties.getProperty("defaultUserGroupName");
        defaultUserGroupNameReadOnly = properties.getProperty("defaultUserGroupNameReadOnly");
        defaultETLGroupName = properties.getProperty("defaultETLGroupName");
        defaultTableSpace = properties.getProperty("defaultTableSpace");
        defaultSchema = properties.getProperty("defaultSchema");
        dialectClassName = properties.getProperty("dialect");
        scanEntityPackagePrefix = properties.getProperty("scanEntityPackagePrefix");
        enversControlTable = properties.getProperty("enversControlTable");
        enversControlTableIdColumn = properties.getProperty("enversControlTableIdColumn");
        enversTypeControlTable = properties.getProperty("enversTypeControlTable");
        enversTypeControlTableIdColumn = properties.getProperty("enversTypeControlTableIdColumn");
    }

    private void startHibernate() {

        loadDialect();

        hibernateConfiguration = new Configuration();
        hibernateConfiguration.configure();
        hibernateConfiguration.setProperty("hibernate.dialect", dialectClassName);
        hibernateConfiguration.setProperty("org.hibernate.envers.revision_field_name", enversControlTableIdColumn);
        hibernateConfiguration.setProperty("org.hibernate.envers.revision_type_field_name", enversTypeControlTableIdColumn);

        Set<Class<?>> annotatedClasses = getAnnotatedClasses();
        for (Class<?> annotatedClass : annotatedClasses) {
            hibernateConfiguration.addAnnotatedClass(annotatedClass);
        }

        sessionFactory = hibernateConfiguration.buildSessionFactory();

        // For hibernate 4+ compatibility (sessionFactory.openSession)
        try {
            sessionFactory.getClass().getMethod("openSession").invoke(sessionFactory);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao invocar openSession do Hibernate.SessionFactory!", e);
        }
    }

    private Set<Class<?>> getAnnotatedClasses() {
        Reflections reflections = new Reflections(scanEntityPackagePrefix);
        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(javax.persistence.Entity.class);
        annotated.addAll(reflections.getTypesAnnotatedWith(org.hibernate.annotations.Entity.class));
        return annotated;
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

    public void loadDialect() {
        if (StringUtils.isEmpty(dialectClassName)) {
            return;
        }
        this.dialect = ClassloaderUtil.getInstanceFromClasspath(dialectClassName.trim());
    }

    public Map<Class<?>, ColumnCommentAppender> getColumnTypeToCommentAppender() {
        if (typeCommentAppenderMap == null) {
            typeCommentAppenderMap = createColumnTypeToCommentAppender();
        }
        return typeCommentAppenderMap;
    }

    public PowerDesignerVBScriptGenerator getModeler() {
        if (corretorDeScriptDDL == null || corretorDeScriptDDL.getValentrim() == null) {
            return null;
        }
        return corretorDeScriptDDL.getValentrim().getGeradorVBScript();
    }

    protected Map<Class<?>, ColumnCommentAppender> createColumnTypeToCommentAppender() {
        Map<Class<?>, ColumnCommentAppender> columnTypeToCommentAppender = Maps.newHashMap();
        EnumCommentAppender enumCommentAppender = new EnumCommentAppender();
        columnTypeToCommentAppender.put(Enum.class, enumCommentAppender);
        return columnTypeToCommentAppender;
    }

    @SuppressWarnings("deprecation")
    public Connection getConnection() {
        return sessionFactory.getCurrentSession().connection();
    }

    public String getCreateSequenceString(String sequenceName) {
        // TODO especifico do dialeto do DB2
        return "create sequence " + sequenceName;
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

    public String getEnversTypeControlTableIdColumn() {
        return enversTypeControlTableIdColumn;
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

    public void setCorretorDeScriptDDL(CorretorDeScriptDDL corretorDeScriptDDL) {
        this.corretorDeScriptDDL = corretorDeScriptDDL;
    }

    public void setGeneratedGraphs(boolean changedGraph) {
        this.changedGraph = changedGraph;
    }

    public Configuration getConfiguration() {
        return hibernateConfiguration;
    }

    public String getScanEntityPackagePrefix() {
        return scanEntityPackagePrefix;
    }

    public void setScanEntityPackagePrefix(String scanEntityPackagePrefix) {
        this.scanEntityPackagePrefix = scanEntityPackagePrefix;
    }

    public void setSchema(String text) {
        this.defaultSchema = text;
    }

    public void setTablespace(String text) {
        this.defaultTableSpace = text;
    }

    public void setEnversControlTable(String text) {
        this.enversControlTable = text;
    }

    public void setEnversTypeControlTable(String text) {
        this.enversTypeControlTable = text;
    }

    public void setEnversControlTableIdColumn(String text) {
        this.enversControlTableIdColumn = text;
    }

    public void setDefaultETLGroupName(String defaultETLGroupName) {
        this.defaultETLGroupName = defaultETLGroupName;
    }

    public void setDefaultUserGroupName(String defaultUserGroupName) {
        this.defaultUserGroupName = defaultUserGroupName;
    }

    public void setDefaultUserName(String defaultUserName) {
        this.defaultUserName = defaultUserName;
    }

    public void setDefaultUserGroupNameReadOnly(String defaultUserGroupNameReadOnly) {
        this.defaultUserGroupNameReadOnly = defaultUserGroupNameReadOnly;
    }

    public void setEnversTypeControlTableIdColumn(String enversTypeControlTableIdColumn) {
        this.enversTypeControlTableIdColumn = enversTypeControlTableIdColumn;
    }

    public void setDialectClassName(String dialectClassName) {
        this.dialectClassName = dialectClassName;
    }

}
