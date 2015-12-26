package marcos2250.powernate.executors;

import marcos2250.powernate.util.PowernateSessionMediator;
import marcos2250.powernate.valentrim.CorretorDeScriptDDL;
import marcos2250.powernate.vbscript.PowerDesignerVBScriptGenerator;
import marcos2250.powernate.window.JanelaNotificavel;

public class GeradorGrafos implements AbstractExecutor {

    @Override
    public void executar(JanelaNotificavel janela, PowernateSessionMediator config) {

        if (config == null) {
            return;
        }

        CorretorDeScriptDDL corretorDeScriptDDL = config.getCorretorDeScriptDDL();

        if (corretorDeScriptDDL == null || corretorDeScriptDDL.getValentrim() == null //
                || corretorDeScriptDDL.getValentrim().getGeradorVBScript() == null) {
            return;
        }

        PowerDesignerVBScriptGenerator processadorVBScript = //
        corretorDeScriptDDL.getValentrim().getGeradorVBScript();

        janela.notificar("Arranging new graph...");

        processadorVBScript = corretorDeScriptDDL.getValentrim().getGeradorVBScript();

        processadorVBScript.getGraphGenerator().build();

        config.setGeneratedGraphs(true);

        janela.notificar("Graph generated!");

    }

}
