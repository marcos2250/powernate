package marcos2250.powernate.executors;

import java.util.Collection;

import marcos2250.powernate.util.PowernateSessionMediator;
import marcos2250.powernate.valentrim.CorretorDeScriptDDL;
import marcos2250.powernate.valentrim.FileMaker;
import marcos2250.powernate.valentrim.GrepDDL;
import marcos2250.powernate.window.JanelaNotificavel;

public class ExportadorDDLVisual implements AbstractExecutor {

    private boolean gerarGrafos;

    @Override
    public void executar(JanelaNotificavel janela, PowernateSessionMediator config) {

        if (config == null || config.getCorretorDeScriptDDL() == null) {
            return;
        }

        CorretorDeScriptDDL corretorDeScriptDDL = config.getCorretorDeScriptDDL();

        Collection<String> scriptDDLGerado = corretorDeScriptDDL.corrigir();

        GrepDDL grepDDL = new GrepDDL(scriptDDLGerado);
        FileMaker geradorArquivo = new FileMaker(grepDDL, "target\\ddl\\ddl.sql");
        geradorArquivo.gerar();

        corretorDeScriptDDL.getValentrim().getGeradorVBScript().writeToFile(gerarGrafos);

        janela.notificar("Arquivos salvos em " + System.getProperty("user.dir")
                + "\\target\\ddl\\ddl.sql e VBScript_PowerDesigner.txt");

    }

    public void setExportarGrafos(boolean gerarGrafos) {
        this.gerarGrafos = gerarGrafos;
    }

}
