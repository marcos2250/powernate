package marcos2250.powernate;

import marcos2250.powernate.executors.ExportadorDDLVisual;
import marcos2250.powernate.executors.ImportadorHibernateMetadata;
import marcos2250.powernate.util.PowernateSessionMediator;
import marcos2250.powernate.window.JanelaNotificavel;

import org.apache.commons.lang.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Powernate {

    public static final Logger LOGGER = LoggerFactory.getLogger(Powernate.class);

    public static void main(String[] args) {
        iniciar();
    }

    public static void iniciar() {

        long tempoInicio = System.currentTimeMillis();

        JanelaNotificavel console = new JanelaNotificavel() {
            @Override
            public void notificar(String arg0) {
                LOGGER.info(arg0);
            }
        };

        PowernateSessionMediator config = new PowernateSessionMediator();
        
        config.initialize();

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
