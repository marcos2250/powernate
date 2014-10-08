package marcos2250.powernate.valentrim;

import java.sql.Connection;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean;

import marcos2250.powernate.util.Config;

public class ConectorHibernate {

    private AnnotationSessionFactoryBean sessionFactoryBean;

    public ConectorHibernate(Config config) {
        sessionFactoryBean = config.initializeSessionFactory();
    }

    public AnnotationSessionFactoryBean getSessionFactoryBean() {
        return sessionFactoryBean;
    }

    public Configuration getConfiguration() {
        return sessionFactoryBean.getConfiguration();
    }

    @SuppressWarnings("deprecation")
    public Connection getConnection() {
        return getSessionFactory().openSession().connection();
    }

    private SessionFactory getSessionFactory() {
        return sessionFactoryBean.getObject();
    }

}
