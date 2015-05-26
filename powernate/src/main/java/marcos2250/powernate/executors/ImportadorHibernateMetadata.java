package marcos2250.powernate.executors;

import marcos2250.powernate.util.PowernateSessionMediator;
import marcos2250.powernate.valentrim.CorretorDeScriptDDL;
import marcos2250.powernate.valentrim.RefinadorResultadoDDL;
import marcos2250.powernate.window.JanelaNotificavel;

public class ImportadorHibernateMetadata implements AbstractExecutor {

    public void executar(JanelaNotificavel janela, PowernateSessionMediator config) {

        janela.notificar("Carregando Hibernate...");

        config.initialize();

        janela.notificar("Refinando resultado DDL...");

        RefinadorResultadoDDL refinadorResultadoDDL = new RefinadorResultadoDDL(config);

        janela.notificar("Corrigindo resultado DDL...");

        CorretorDeScriptDDL corretorDeScriptDDL = new CorretorDeScriptDDL(config, refinadorResultadoDDL);

        janela.notificar("Processando metadados do Hibernate...");

        corretorDeScriptDDL.processarHibernateMetadata();

        config.setCorretorDeScriptDDL(corretorDeScriptDDL);

        janela.notificar("Metadados do modelo importados com sucesso!");

    }

}
