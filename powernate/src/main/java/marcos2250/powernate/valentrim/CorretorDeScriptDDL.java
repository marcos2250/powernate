package marcos2250.powernate.valentrim;

import java.sql.SQLException;
import java.util.Collection;

import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.Dialect;
import org.hibernate.tool.hbm2ddl.DatabaseMetadata;

import marcos2250.powernate.util.Config;

public class CorretorDeScriptDDL {

    public static final String ERRO_AO_GERAR_O_SCRIPT_DDL = "Erro ao gerar o script DDL.";

    private ConectorHibernate conectorHibernate;
    private RefinadorResultadoDDL refinadorResultadoDDL;
    private Configuration hibernateConfig;
    private boolean gerarApenasUpdates;
    private Valentrim valentrim;

    private Config config;

    public CorretorDeScriptDDL(Config config, ConectorHibernate conectorHibernate,
            RefinadorResultadoDDL refinadorResultadoDDL) {
        this.config = config;
        this.conectorHibernate = conectorHibernate;
        this.refinadorResultadoDDL = refinadorResultadoDDL;
        hibernateConfig = conectorHibernate.getConfiguration();
    }

    public Collection<String> corrigir() {
        String[] script = gerarScript();
        return refinadorResultadoDDL.refinar(config, script);
    }

    public void setGerarApenasUpdates(boolean gerarApenasUpdates) {
        this.gerarApenasUpdates = gerarApenasUpdates;
    }

    public void processarHibernateMetadata() {
        try {
            conectorHibernate.getSessionFactoryBean();
            valentrim = new Valentrim(hibernateConfig, config);
            valentrim.process();
            // CHECKSTYLE:OFF
        } catch (Exception e) {
            // CHECKSTYLE:ON
            throw new IllegalStateException(ERRO_AO_GERAR_O_SCRIPT_DDL, e);
        }
    }

    private String[] gerarScript() {
        try {
            Dialect dialect = config.getDialect();

            if (gerarApenasUpdates) {
                return hibernateConfig.generateSchemaUpdateScript(dialect, //
                        new DatabaseMetadata(conectorHibernate.getConnection(), dialect));
            } else {

                return hibernateConfig.generateSchemaCreationScript(dialect);
            }
        } catch (SQLException e) {
            throw new IllegalStateException(ERRO_AO_GERAR_O_SCRIPT_DDL, e);
        }
    }

    public Valentrim getValentrim() {
        return valentrim;
    }

}
