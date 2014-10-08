package marcos2250.powernate.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Util {

    public static Object[] lerConfigurations() {

        List<String> options = new ArrayList<String>();

        InputStream file = Thread.currentThread().getContextClassLoader().getResourceAsStream("configurations.txt");

        BufferedReader br = new BufferedReader(new InputStreamReader(file));

        String line;

        try {

            while ((line = br.readLine()) != null) {
                options.add(line);
            }

        } catch (Exception e) {
            return null;
        }

        return options.toArray();

    }

}
