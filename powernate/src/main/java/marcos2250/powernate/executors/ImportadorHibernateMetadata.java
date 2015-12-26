package marcos2250.powernate.executors;

import marcos2250.powernate.util.PowernateSessionMediator;
import marcos2250.powernate.valentrim.CorretorDeScriptDDL;
import marcos2250.powernate.valentrim.RefinadorResultadoDDL;
import marcos2250.powernate.window.JanelaNotificavel;

public class ImportadorHibernateMetadata implements AbstractExecutor {

    public void executar(JanelaNotificavel janela, PowernateSessionMediator config) {

        janela.notificar("Loading Hibernate...");

        //config.initialize();

        janela.notificar("Refine DDL result...");

        RefinadorResultadoDDL refinadorResultadoDDL = new RefinadorResultadoDDL(config);

        janela.notificar("Correcting DDL result...");

        CorretorDeScriptDDL corretorDeScriptDDL = new CorretorDeScriptDDL(config, refinadorResultadoDDL);

        janela.notificar("Processing Hibernate Metadata...");

        corretorDeScriptDDL.processarHibernateMetadata();

        config.setCorretorDeScriptDDL(corretorDeScriptDDL);

        janela.notificar("Model metadata loaded successfully!");

    }

}
