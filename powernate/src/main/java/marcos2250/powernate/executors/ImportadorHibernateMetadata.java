package marcos2250.powernate.executors;

import marcos2250.powernate.util.Config;
import marcos2250.powernate.valentrim.ConectorHibernate;
import marcos2250.powernate.valentrim.CorretorDeScriptDDL;
import marcos2250.powernate.valentrim.RefinadorResultadoDDL;
import marcos2250.powernate.window.JanelaNotificavel;

public class ImportadorHibernateMetadata implements AbstractExecutor {

    public void executar(JanelaNotificavel janela, Config config) {

        janela.notificar("Carregando Hibernate...");

        ConectorHibernate conectorHibernate = new ConectorHibernate(config);

        janela.notificar("Refinando resultado DDL...");

        RefinadorResultadoDDL refinadorResultadoDDL = new RefinadorResultadoDDL(config);

        janela.notificar("Corrigindo resultado DDL...");

        CorretorDeScriptDDL corretorDeScriptDDL = new CorretorDeScriptDDL(config, conectorHibernate,
                refinadorResultadoDDL);

        janela.notificar("Processando metadados do Hibernate...");

        corretorDeScriptDDL.processarHibernateMetadata();

        config.setCorretorDeScriptDDL(corretorDeScriptDDL);

        janela.notificar("Metadados do modelo importados com sucesso!");

    }

}
