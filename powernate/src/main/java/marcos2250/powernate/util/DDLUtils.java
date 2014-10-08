package marcos2250.powernate.util;

import java.util.List;

import org.hibernate.mapping.Column;
import org.hibernate.mapping.Table;

public class DDLUtils {

    @SuppressWarnings("unchecked")
    public static boolean isEnvers(Table table, Config config) {
        if (table.getPrimaryKey() == null) {
            return false;
        }
        if (table.getName().equals(config.enversControlTable)) {
            return true;
        }
        if (table.getName().equals(config.enversTypeControlTable)) {
            return true;
        }
        List<Column> columns = CheckUtil.checkedList(table.getPrimaryKey().getColumns(), Column.class);
        for (Column column : columns) {
            if (config.enversControlTableIdColumn.equals(column.getName())) {
                return true;
            }
        }
        return false;
    }

}
