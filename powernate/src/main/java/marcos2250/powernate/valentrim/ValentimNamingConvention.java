package marcos2250.powernate.valentrim;

import static com.google.common.collect.Maps.uniqueIndex;
import static com.google.common.collect.Sets.newHashSet;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.ForeignKey;
import org.hibernate.mapping.Table;
import org.hibernate.mapping.UniqueKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import marcos2250.powernate.util.DDLUtils;
import marcos2250.powernate.util.PowernateSessionMediator;

public class ValentimNamingConvention {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValentimNamingConvention.class);

    private static final String UNDERSCORE = "_";
    private static final String UNCHECKED = "unchecked";
    private static final String FK = "FK";
    private static final String PREFIXO_UK = "UK_";

    private static final int PREFIX_LENGTH = 3;
    private static final int COLUMN_NAME_LENGTH_FOR_UNIQUEKEY_NAME = 3 * PREFIX_LENGTH;
    private static final int COLUMN_NAME_MAX_LENGTH = 32;
    private static final int TABLE_NAME_MAX_LENGTH = 40;
    private static final int UK_NAME_MAX_LENGTH = 18;

    public void generalValidation(Collection<Table> tables) {
        // TODO add some validations here
        checkTablePrefixesUniqueness(tables);
    }

    public void validateTableName(Table table, PowernateSessionMediator config) {

        String name = table.getName();

        String prefix = (String) name.subSequence(0, PREFIX_LENGTH + 1);

        Pattern patt = Pattern.compile("^[A-Z]{" + PREFIX_LENGTH + "}_$");
        Matcher m = patt.matcher(prefix);

        List<String> violations = Lists.newArrayList();

        char tableNameFirstLetter = name.charAt(PREFIX_LENGTH + 1);
        char prefixFistLetter = prefix.charAt(0);

        if (!DDLUtils.isEnvers(table, config) && prefixFistLetter != tableNameFirstLetter) {
            violations.add("First letter of prefix '" + prefixFistLetter + "' doesn't match with table name '"
                    + tableNameFirstLetter + "' .");
        }

        if (!m.matches()) {
            violations.add("Prefix must have 3 uppercase letters !");
        }

        if (name.length() > TABLE_NAME_MAX_LENGTH) {
            violations.add("Table name " + name + " length exceeds the limit of " + TABLE_NAME_MAX_LENGTH);
        }

        if (!violations.isEmpty()) {
            LOGGER.error("Invalid table name " + name + "  : \n" + StringUtils.join(violations, "  \n"));
        }
    }

    public String getForeignKeyIndexName(Table table, Table refTable, ForeignKey foreignKey) {
        // IRABCXYZ => FKXYZABC
        // IRABCXY2 => FKXYZAB2
        // IRABCX12 => FKXYZA12
        String fkName = foreignKey.getName();

        String lastChar = String.valueOf(fkName.charAt(fkName.length() - 1));
        String penultimateChar = String.valueOf(fkName.charAt(fkName.length() - 2));

        if (StringUtils.isNumeric(lastChar)) {
            if (StringUtils.isNumeric(penultimateChar)) {
                return FK + tableNamePrefix(table) + tableNamePrefix(refTable).substring(0, PREFIX_LENGTH - 2)
                        + penultimateChar + lastChar;
            } else {
                return FK + tableNamePrefix(table) + tableNamePrefix(refTable).substring(0, PREFIX_LENGTH - 1)
                        + lastChar;
            }
        }
        return FK + tableNamePrefix(table) + tableNamePrefix(refTable);
    }

    public void validateNameForMultipleForeignKey(Table table, Table refTable, int numberOfFks, ForeignKey foreignKey) {
        String fkName = foreignKey.getName();
        String refTableName = tableNamePrefix(refTable);

        char lastCharOfPrefix = tableNamePrefix(table).charAt(PREFIX_LENGTH - 1);
        char penultimateCharOfPrefix = tableNamePrefix(table).charAt(PREFIX_LENGTH - 2);

        String format;

        if (numberOfFks < 10) {
            // "IRAAABBB" -> "IRAAABB9"
            CharSequence tableName = tableNamePrefix(table).subSequence(0, PREFIX_LENGTH - 1);
            format = "IR" + refTableName + tableName + '[' + lastCharOfPrefix + "|1-" + (numberOfFks + 1) + ']';
        } else {
            // "IRAAABBB" -> "IRAAAB99"
            CharSequence tableName = tableNamePrefix(table).subSequence(0, PREFIX_LENGTH - 2);
            format = "IR" + refTableName + tableName + '[' + penultimateCharOfPrefix + "\\d][" + lastCharOfPrefix
                    + "\\d]";
        }

        Pattern patt = Pattern.compile("^" + format + "$");
        Matcher m = patt.matcher(fkName);

        if (!m.matches()) {
            LOGGER.error("Foreign key of table " + table.getName() + " has invalid format: '" + fkName + "'. Expected '"
                    + format + "' ");
        }
    }

    public void validateColumnName(Table table, Column column, boolean validarPrefixoColuna) {
        String columnName = column.getName();
        Set<String> violations = Sets.newHashSet();

        if (columnName.length() < 5) {
            LOGGER.error("Column name too short! - " + columnName);
            return;
        }

        String columnPrefix = (String) columnName.subSequence(0, PREFIX_LENGTH);

        if (validarPrefixoColuna && !columnPrefix.equals(tableNamePrefix(table))) {
            violations.add("Column prefix " + columnName + " '" + columnPrefix + "' should be '"
                    + tableNamePrefix(table) + "'");
        }

        if (columnName.length() > COLUMN_NAME_MAX_LENGTH) {
            violations.add("Column table name " + columnName + "(" + columnName.length() + ") exceeds maximum length ("
                    + COLUMN_NAME_MAX_LENGTH + ")");
        }

        if (!violations.isEmpty()) {
            LOGGER.error(table.getName() + "... Invalid column name " + columnName + " : \n"
                    + StringUtils.join(violations, "\n"));
        }
    }

    private String tableNamePrefix(Table input) {
        return (String) input.getName().subSequence(0, PREFIX_LENGTH);
    }

    public String makeIRName(Table table, Table refTable) {
        return "IR" + tableNamePrefix(refTable) + tableNamePrefix(table);
    }

    public String getPkName(Table table) {
        return "PK_" + tableNamePrefix(table);
    }

    @SuppressWarnings(UNCHECKED)
    public String makeUKName(Table table, UniqueKey uk) {
        if (!uk.getName().startsWith(PREFIXO_UK + tableNamePrefix(table))) {

            // String ukName = makeUKName(table, uk);
            Set<Column> columns = newHashSet(uk.columnIterator());
            StringBuilder sb = new StringBuilder(PREFIXO_UK).append(tableNamePrefix(table));
            for (Column column : columns) {
                sb.append(UNDERSCORE);
                String uniqueKeyNamePartForColumn = (String) column.getName().subSequence(0,
                        Math.min(PREFIX_LENGTH + 1 + COLUMN_NAME_LENGTH_FOR_UNIQUEKEY_NAME, column.getName().length()));
                sb.append(uniqueKeyNamePartForColumn);
            }
            String ukName = sb.toString();

            return ukName.substring(0, Math.min(UK_NAME_MAX_LENGTH, ukName.length()));
        }
        return uk.getName();
    }


    private void checkTablePrefixesUniqueness(Collection<Table> tables) {
        LOGGER.debug("Checking table prefix uniqueness...");
        try {
            uniqueIndex(tables, new Function<Table, String>() {
                public String apply(Table input) {
                    return tableNamePrefix(input);
                }
            });
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Error on uniqueness check of table prefixes! ", e);
        }
        LOGGER.debug("Unicidade dos prefixos das tabelas OK!!");
    }

}
