package marcos2250.powernate.util;

import java.awt.Color;
import java.util.List;
import java.util.Map;

import org.hibernate.dialect.Dialect;
import org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean;

import marcos2250.powernate.comment.ColumnCommentAppender;
import marcos2250.powernate.valentrim.CorretorDeScriptDDL;
import marcos2250.powernate.valentrim.Permission;
import marcos2250.powernate.vbscript.PowerDesignerVBScriptGenerator;

public abstract class Config {

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

    protected String dialectClassname;
    protected String sessionFactoryClassname;

    protected boolean rearrangeGraphEnversTables = false;
    protected boolean generateSQLComments = false;

    private Dialect dialect;
    private AnnotationSessionFactoryBean sessionFactory;

    private Map<Class<?>, ColumnCommentAppender> typeCommentAppenderMap;

    private CorretorDeScriptDDL corretorDeScriptDDL;
    private boolean changedGraph;

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
        if (modulesList == null) {
            modulesList = createModulesList();
        }
        return modulesList;
    }

    public Dialect getDialect() {
        if (dialect == null) {
            this.dialect = ClassloaderUtil.getInstanceFromClasspath(dialectClassname);
        }
        return dialect;
    }

    public AnnotationSessionFactoryBean getSessionFactory() {
        if (sessionFactory == null) {
            sessionFactory = ClassloaderUtil.getInstanceFromClasspath(sessionFactoryClassname);
        }
        return sessionFactory;
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

    protected abstract List<ProjectModulesParams> createModulesList();

    protected abstract Map<Class<?>, ColumnCommentAppender> createColumnTypeToCommentAppender();

    public abstract AnnotationSessionFactoryBean initializeSessionFactory();

    public void setGeneratedGraphs(boolean changedGraph) {
        this.changedGraph = changedGraph;
    }

}
