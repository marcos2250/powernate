package marcos2250.powernate;

import marcos2250.powernate.executors.ExportadorDDLVisual;
import marcos2250.powernate.executors.ImportadorHibernateMetadata;
import marcos2250.powernate.util.ClassloaderUtil;
import marcos2250.powernate.util.Config;
import marcos2250.powernate.window.JanelaNotificavel;

import org.apache.commons.lang.time.DurationFormatUtils;

public class Powernate {

    public static void main(String[] args) {

        if (args == null || args.length == 0) {
            return;
        }

        Config config = ClassloaderUtil.getInstanceFromClasspath(args[0]);

        if (config == null) {
            return;
        }

        iniciar(config);
    }

    public static void iniciar(Config config) {

        long tempoInicio = System.currentTimeMillis();
        
        JanelaNotificavel console = new JanelaNotificavel() {
            @Override
            public void notificar(String arg0) {
                System.out.println(arg0);
            }
        };

        new ImportadorHibernateMetadata().executar(console, config);

        ExportadorDDLVisual exportadorDDLVisual = new ExportadorDDLVisual();
        exportadorDDLVisual.setExportarGrafos(false);

        exportadorDDLVisual.executar(console, config);

        long tempoFim = System.currentTimeMillis();
        console.notificar("DDL Exporter finished by " + formataDuracao(tempoFim - tempoInicio));
    }
    
    public static String formataDuracao(long milis) {
        return DurationFormatUtils.formatDuration(milis, "H'h' m'min' s's'");
    }

}
