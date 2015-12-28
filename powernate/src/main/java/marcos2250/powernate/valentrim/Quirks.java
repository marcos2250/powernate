package marcos2250.powernate.valentrim;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

public class Quirks {

    private Properties properties;
    private Set<String> objectsToAvoid;
    private Set<String> collectionTables;

    public Quirks() {
        properties = new Properties();

        InputStream file = Thread.currentThread().getContextClassLoader().getResourceAsStream("quirks.properties");
        if (file != null) {
            try {
                properties.load(file);
            } catch (IOException e) {
                // Nao faz nada
                e.printStackTrace();
            }
        }

        readObjectsListToAvoid();
        readCollectionTables();
    }

    private void readObjectsListToAvoid() {
        objectsToAvoid = stringSeparadaPorVirgulasParaSet(properties.getProperty("FKsANaoGerar"));
    }

    private void readCollectionTables() {
        collectionTables = stringSeparadaPorVirgulasParaSet(properties.getProperty("collectionTables"));
    }

    private Set<String> stringSeparadaPorVirgulasParaSet(String valores) {
        if (StringUtils.isEmpty(valores)) {
            return Sets.newHashSet();
        }

        return Sets.newHashSet(StringUtils.splitByWholeSeparator(valores, ", "));
    }

    public Set<String> getObjectsToAvoid() {
        return ImmutableSet.copyOf(objectsToAvoid);
    }

    public Set<String> getCollectionTables() {
        return ImmutableSet.copyOf(collectionTables);
    }
}
