package marcos2250.powernate.valentrim;

import static org.apache.commons.lang.StringUtils.upperCase;

import org.hibernate.cfg.ImprovedNamingStrategy;
import org.hibernate.cfg.NamingStrategy;

public class DefaultNamingStrategy implements NamingStrategy {

    public static final NamingStrategy INSTANCE = new DefaultNamingStrategy();
    private static final NamingStrategy DELEGATE = ImprovedNamingStrategy.INSTANCE;

    public String classToTableName(String className) {
        return upperCase(DELEGATE.classToTableName(className));
    }

    public String propertyToColumnName(String propertyName) {
        return upperCase(DELEGATE.propertyToColumnName(propertyName));
    }

    public String tableName(String tableName) {
        return upperCase(DELEGATE.tableName(tableName));
    }

    public String columnName(String columnName) {
        return upperCase(DELEGATE.columnName(columnName));
    }

    public String collectionTableName(String ownerEntity, String ownerEntityTable, String associatedEntity,
            String associatedEntityTable, String propertyName) {
        return upperCase(DELEGATE.collectionTableName(ownerEntity, ownerEntityTable, associatedEntity,
                associatedEntityTable, propertyName));
    }

    public String joinKeyColumnName(String joinedColumn, String joinedTable) {
        return upperCase(DELEGATE.joinKeyColumnName(joinedColumn, joinedTable));
    }

    public String foreignKeyColumnName(String propertyName, String propertyEntityName, String propertyTableName,
            String referencedColumnName) {
        return upperCase(DELEGATE.foreignKeyColumnName(propertyName, propertyEntityName, propertyTableName,
                referencedColumnName));
    }

    public String logicalColumnName(String columnName, String propertyName) {
        return upperCase(DELEGATE.logicalColumnName(columnName, propertyName));
    }

    public String logicalCollectionTableName(String tableName, String ownerEntityTable, String associatedEntityTable,
            String propertyName) {
        return upperCase(DELEGATE.logicalCollectionTableName(tableName, ownerEntityTable, associatedEntityTable,
                propertyName));
    }

    public String logicalCollectionColumnName(String columnName, String propertyName, String referencedColumn) {
        return upperCase(DELEGATE.logicalCollectionColumnName(columnName, propertyName, referencedColumn));
    }

}