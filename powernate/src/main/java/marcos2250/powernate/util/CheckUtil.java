package marcos2250.powernate.util;

import java.util.Collections;
import java.util.List;

public class CheckUtil {

    public static <O> List<O> checkedList(List<O> lista, Class<O> classe) {
        return Collections.checkedList(lista, classe);
    }

}
