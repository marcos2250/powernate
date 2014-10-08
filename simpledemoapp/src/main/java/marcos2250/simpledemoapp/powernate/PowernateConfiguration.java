package marcos2250.simpledemoapp.powernate;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import marcos2250.powernate.comment.ColumnCommentAppender;
import marcos2250.powernate.comment.EnumCommentAppender;
import marcos2250.powernate.util.Config;
import marcos2250.powernate.util.ProjectModulesParams;

import org.hibernate.cfg.Environment;
import org.hibernate.dialect.HSQLDialect;
import org.hibernate.envers.event.AuditEventListener;
import org.hsqldb.jdbc.JDBCDriver;
import org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean;

import com.google.common.collect.Maps;

public class PowernateConfiguration extends Config {

    private static final String ENVERS_TABLE_PK = "AUD_ID";
    private static final String APP = "APP";
    private static final String[] PACKAGES = {"marcos2250.simpledemoapp.domain.**"};
    private static final Class<?> DIALECT = HSQLDialect.class;

    public PowernateConfiguration() {

        this.dialectClassname = DIALECT.getName();
        this.sessionFactoryClassname = AnnotationSessionFactoryBean.class.getName();

        this.rearrangeGraphEnversTables = false;

        this.defaultUserName = APP;
        this.defaultUserGroupName = "GAPPDW";
        this.defaultUserGroupNameReadOnly = "GAPPDR";
        this.defaultETLGroupName = "GETLDW";
        this.defaultTableSpace = "TSAPP4"; // TSAPP32

        this.defaultSchema = APP;

        this.enversControlTable = "AUD_AUDITORIA";
        this.enversTypeControlTable = "TAL_TIPO_ALTERACAO";
        this.enversControlTableIdColumn = ENVERS_TABLE_PK;
    }

    @Override
    protected Map<Class<?>, ColumnCommentAppender> createColumnTypeToCommentAppender() {

        Map<Class<?>, ColumnCommentAppender> columnTypeToCommentAppender = Maps.newHashMap();

        EnumCommentAppender enumCommentAppender = new EnumCommentAppender();

        columnTypeToCommentAppender.put(Enum.class, enumCommentAppender);

        return columnTypeToCommentAppender;
    }

    @Override
    protected List<ProjectModulesParams> createModulesList() {

        List<ProjectModulesParams> list = new ArrayList<ProjectModulesParams>();

        list.add(new ProjectModulesParams(0, "person", 65535, Color.RED));
        list.add(new ProjectModulesParams(1, "project", 32768, Color.BLUE));

        return list;
    }

    @Override
    public AnnotationSessionFactoryBean initializeSessionFactory() {

        AnnotationSessionFactoryBean sessionFactoryBean = getSessionFactory();

        sessionFactoryBean.setPackagesToScan(PACKAGES);
        Properties hibernateProperties = new Properties();
        hibernateProperties.put(Environment.DIALECT, DIALECT.getName());
        hibernateProperties.put(Environment.DEFAULT_SCHEMA, defaultSchema);
        hibernateProperties.put(Environment.FORMAT_SQL, Boolean.toString(true));
        hibernateProperties.put(Environment.FORMAT_SQL, Boolean.toString(true));
        hibernateProperties.put(Environment.URL, "jdbc:hsqldb:mem://localhost");
        hibernateProperties.put(Environment.DRIVER, JDBCDriver.class.getName());
        hibernateProperties.put(Environment.USER, "sa");
        hibernateProperties.put(Environment.PASS, "");
        hibernateProperties.put(Environment.HBM2DDL_AUTO, "update");
        hibernateProperties.put("org.hibernate.envers.revision_field_name", ENVERS_TABLE_PK);
        hibernateProperties.put("org.hibernate.envers.revision_type_field_name", "TAL_ID");
        Map<String, Object> eventListeners = Maps.newHashMap();
        AuditEventListener auditEventListener = new AuditEventListener();
        eventListeners.put("post-insert", auditEventListener);
        eventListeners.put("post-update", auditEventListener);
        eventListeners.put("post-delete", auditEventListener);
        eventListeners.put("pre-collection-update", auditEventListener);
        eventListeners.put("pre-collection-remove", auditEventListener);
        eventListeners.put("post-collection-recreate", auditEventListener);
        sessionFactoryBean.setEventListeners(eventListeners);
        sessionFactoryBean.setHibernateProperties(hibernateProperties);

        // load configurations
        try {
            sessionFactoryBean.afterPropertiesSet();
            // CHECKSTYLE:OFF
        } catch (Exception e) {
            // CHECKSTYLE:ON
            throw new IllegalStateException("Erro ao carregar configuracoes Spring.", e);
        }

        return sessionFactoryBean;
    }

}
