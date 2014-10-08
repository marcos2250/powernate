package marcos2250.powernate.executors;

import marcos2250.powernate.util.Config;
import marcos2250.powernate.window.JanelaNotificavel;

public abstract interface AbstractExecutor {

    void executar(JanelaNotificavel janela, Config config);

}
