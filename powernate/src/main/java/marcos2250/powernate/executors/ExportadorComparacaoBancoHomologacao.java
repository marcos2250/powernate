package marcos2250.powernate.executors;

import java.util.Collection;

import marcos2250.powernate.util.Config;
import marcos2250.powernate.valentrim.CorretorDeScriptDDL;
import marcos2250.powernate.valentrim.FileMaker;
import marcos2250.powernate.valentrim.GrepDDL;
import marcos2250.powernate.window.JanelaNotificavel;

public class ExportadorComparacaoBancoHomologacao implements AbstractExecutor {

    @Override
    public void executar(JanelaNotificavel janela, Config config) {

        if (config == null || config.getCorretorDeScriptDDL() == null) {
            return;
        }

        CorretorDeScriptDDL corretorDeScriptDDL = config.getCorretorDeScriptDDL();

        corretorDeScriptDDL.setGerarApenasUpdates(true);

        Collection<String> scriptDDLGerado = corretorDeScriptDDL.corrigir();

        GrepDDL grepDDL = new GrepDDL(scriptDDLGerado) //
                .semComentarios() //
                .semCreateIndex();

        FileMaker geradorArquivo = new FileMaker(grepDDL, "target\\ddl\\ddl.sql");
        geradorArquivo.gerar();

        // corretorDeScriptDDL.getValentrim().getGeradorVBScript().writeToFile(false);

        janela.notificar("Arquivo salvo em " + System.getProperty("user.dir") + "\\target\\ddl\\ddl.sql");

    }
}
