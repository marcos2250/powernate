package marcos2250.simpledemoapp.powernate;

import marcos2250.powernate.VisualPowernate;
import marcos2250.powernate.util.Config;

public class PowernateStarter {

    public static void main(String[] args) {

        Config config = new PowernateConfiguration();

        VisualPowernate.startApplication(config);

    }
}
