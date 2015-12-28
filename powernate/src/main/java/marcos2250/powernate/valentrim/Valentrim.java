package marcos2250.powernate.valentrim;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.uniqueIndex;
import static com.google.common.collect.Sets.difference;
import static com.google.common.collect.Sets.newHashSet;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import marcos2250.powernate.util.DDLUtils;
import marcos2250.powernate.util.PowernateSessionMediator;
import marcos2250.powernate.vbscript.PowerDesignerVBScriptGenerator;

import org.apache.commons.lang.StringUtils;
import org.hibernate.cfg.Configuration;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.ForeignKey;
import org.hibernate.mapping.Index;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.PrimaryKey;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.Table;
import org.hibernate.mapping.UniqueKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;

//CHECKSTYLE:OFF devido Fan-out excessivo
@SuppressWarnings({"PMD.ExcessiveImports", "PMD.CouplingBetweenObjects", "PMD.FanOut"})
public class Valentrim {
    // CHECKSTYLE:ON

    private static final Logger LOGGER = LoggerFactory.getLogger(Valentrim.class);

    private static final String UNCHECKED = "unchecked";
    private static final String UNDERSCORE = "_";

    private static final String FK = "FK";
    private static final String PREFIXO_UK = "UK_";
    private static final int PREFIX_LENGTH = 3;
    private static final int COLUMN_NAME_MAX_LENGTH = 32;
    private static final int TABLE_NAME_MAX_LENGTH = 40;
    private static final int COLUMN_NAME_LENGTH_FOR_UNIQUEKEY_NAME = 3 * PREFIX_LENGTH;
    private static final int UK_NAME_MAX_LENGTH = 18;

    private Set<Table> tables;
    private Multimap<Table, PersistentClass> tableToClass;
    private Map<String, ForeignKey> fkNameToForeignKey;
    private Multimap<String, Property> columnAliasToProperty;

    private Configuration hibernateConfiguration;
    private Multimap<Table, Index> tableToIndexes;

    private PowerDesignerVBScriptGenerator geradorVBScript;

    private PowernateSessionMediator config;

    private Quirks quirks;

    public Valentrim(Configuration hibernateConfiguration, PowernateSessionMediator config) {
        this.hibernateConfiguration = hibernateConfiguration;
        this.config = config;
    }

    public void process() {
        init();
        checkTablePrefixesUniqueness();

        geradorVBScript = new PowerDesignerVBScriptGenerator(config, quirks, tableToClass, columnAliasToProperty);

        geradorVBScript.doPreProcessings(tables);

        for (Table table : tables) {
            String nomeTabela = table.getName();
            LOGGER.info("Processando " + nomeTabela);
            validateTableName(table);
            processSequences(table);
            processPrimaryKey(table);
            processForeignKeys(table);
            processUniqueConstraints(table);
            processColumns(table);
            processComments(table);
        }

        geradorVBScript.doPostProcessings();

    }

    private void init() {
        hibernateConfiguration.setNamingStrategy(DefaultNamingStrategy.INSTANCE);
        hibernateConfiguration.buildMappings();

        fkNameToForeignKey = Maps.newHashMap();
        tableToIndexes = HashMultimap.create();

        quirks = new Quirks();

        tables = newHashSet(hibernateConfiguration.getTableMappings());
        criarMapeamentoTableParaPersistentClass();
        criarMapeamentoColumnAliasParaTable();
        criarMapeamentoColumnAliasToProperty();
    }

    private void criarMapeamentoTableParaPersistentClass() {
        tableToClass = Multimaps.index(hibernateConfiguration.getClassMappings(),
                new Function<PersistentClass, Table>() {
                    public Table apply(PersistentClass input) {
                        return input.getTable();
                    }
                });
    }

    @SuppressWarnings(UNCHECKED)
    private void criarMapeamentoColumnAliasParaTable() {
        Map<String, Table> columnAliasToTable = Maps.newHashMap();
        for (Table table : tables) {
            HashSet<Column> cols = newHashSet(table.getColumnIterator());
            for (Column column : cols) {
                columnAliasToTable.put(getColumnUniqueAlias(table, column), table);
            }
        }
    }

    @SuppressWarnings(UNCHECKED)
    private void criarMapeamentoColumnAliasToProperty() {
        Multimap<Table, Property> tableToProperties = HashMultimap.create();
        for (PersistentClass persistentClass : newHashSet(hibernateConfiguration.getClassMappings())) {
            tableToProperties.putAll(persistentClass.getTable(),
                    Sets.newHashSet(persistentClass.getIdentifierProperty()));
            tableToProperties.putAll(persistentClass.getTable(),

            Sets.<Property> newHashSet(persistentClass.getDeclaredPropertyIterator()));
        }

        columnAliasToProperty = HashMultimap.create();
        for (Table table : tableToProperties.keySet()) {
            Collection<Property> properties = tableToProperties.get(table);
            for (Property property : properties) {
                HashSet<Column> columns = Sets.<Column> newHashSet(property.getColumnIterator());
                for (Column column : columns) {
                    columnAliasToProperty.put(getColumnUniqueAlias(table, column), property);
                }
            }
        }
    }

    private String getColumnUniqueAlias(Table table, Column column) {
        return column.getAlias(config.getDialect(), table);
    }

    private void checkTablePrefixesUniqueness() {
        LOGGER.debug("Checando unicidade dos prefixos das tabelas...");
        try {
            uniqueIndex(tables, new Function<Table, String>() {
                public String apply(Table input) {
                    return tableNamePrefix(input);
                }
            });
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Erro ao checar unicidade de siglas de tabelas! ", e);
        }
        LOGGER.debug("Unicidade dos prefixos das tabelas OK!!");
    }

    private void validateTableName(Table table) {

        String name = table.getName();

        String prefix = (String) name.subSequence(0, PREFIX_LENGTH + 1);

        Pattern patt = Pattern.compile("^[A-Z]{" + PREFIX_LENGTH + "}_$");
        Matcher m = patt.matcher(prefix);

        List<String> violations = Lists.newArrayList();

        char tableNameFirstLetter = name.charAt(PREFIX_LENGTH + 1);
        char prefixFistLetter = prefix.charAt(0);

        if (!DDLUtils.isEnvers(table, config) && prefixFistLetter != tableNameFirstLetter) {
            violations.add("Primeira letra do prefixo '" + prefixFistLetter
                    + "' nao bate com primeira letra do nome da tabela '" + tableNameFirstLetter + "' .");
        }

        if (!m.matches()) {
            violations.add("Prefixo nao e composto de tres letras maiusculas!");
        }

        if (name.length() > TABLE_NAME_MAX_LENGTH) {
            violations.add("Tamanho do nome da tabela " + name + " maior que o limite de " + TABLE_NAME_MAX_LENGTH);
        }

        if (!violations.isEmpty()) {
            LOGGER.error("Nome de tabela invalido " + name + "  : \n" + StringUtils.join(violations, "  \n"));
        }
    }

    private void processSequences(Table table) {
        geradorVBScript.createSequence(table);
    }

    private void processForeignKeys(Table table) {
        ListMultimap<Table, ForeignKey> refTableToForeignKey = getReferencedTableToForeignKeyMapping(table);

        for (Table refTable : refTableToForeignKey.keySet()) {
            List<ForeignKey> foreignKeysReferencingRefTable = newArrayList(refTableToForeignKey.get(refTable));

            int numberOfFks = foreignKeysReferencingRefTable.size();

            for (ForeignKey foreignKey : foreignKeysReferencingRefTable) {

                // CHECKSTYLE:OFF
                if (numberOfFks == 1) {
                    String originalName = foreignKey.getName();
                    foreignKey.setName("IR" + tableNamePrefix(refTable) + tableNamePrefix(table));
                    LOGGER.debug(table.getName() + "... Alterando nome de FK: " + originalName + " -> "
                            + foreignKey.getName());
                } else {
                    // Quando existe mais de uma FK referenciando a mesma refTable, o nome devera ser previamente
                    // definido. Ele nao sera gerado! Nao e seguro gerar o nome da FK nesse caso, pois nao haveria como
                    // definir um nome que independesse dos nomes das outras fks.
                    LOGGER.debug(table.getName() + "... FKs referenciando mesma tabela: " + foreignKey.getName()
                            + " . Nome da FK NAO foi alterado!");
                    validateNameForMultipleForeignKey(table, refTable, numberOfFks, foreignKey);
                }
                // CHECKSTYLE:ON
                validateForeignKeyNameUniqueness(foreignKey);

                if (isForeignKeyReferenceToSingleColumnPrimaryKey(foreignKey)) {
                    LOGGER.debug(table.getName()
                            + "... ignorando foreign key que nao e referencia a chave primaria simples!");

                    continue;
                }

                Column column = foreignKey.getColumn(0);

                createForeignKeyIndex(table, refTable, foreignKey, column);
            }
        }
    }

    private ListMultimap<Table, ForeignKey> getReferencedTableToForeignKeyMapping(Table table) {
        @SuppressWarnings(UNCHECKED)
        Set<ForeignKey> foreignKeys = newHashSet(table.getForeignKeyIterator());
        return Multimaps.index(foreignKeys, new Function<ForeignKey, Table>() {
            public Table apply(ForeignKey input) {
                return input.getReferencedTable();
            }
        });
    }

    private void createForeignKeyIndex(Table table, Table refTable, ForeignKey foreignKey, Column column) {

        Set<Column> pkColumns = getPkColumns(table);

        // Somente cria index para colunas de FKs que nao sao PKs ou quando PK e
        // composta
        // pkColumns.size() > 1 => captura jointables (why?)
        if (!pkColumns.contains(column) || (pkColumns.size() > 1)) {
            String inComplianceFKName = getForeignKeyIndexName(table, refTable, foreignKey);
            Index index = table.getOrCreateIndex(inComplianceFKName);
            index.addColumn(column);
            LOGGER.debug(table.getName() + "... Criando index " + index.getName() + " da FK " + foreignKey.getName());

            tableToIndexes.put(table, index);

            String irComment = "Integridade referencial entre a tabela mae " + refTable.getName() + "  e a coluna "
                    + column.getName() + " da tabela filha " + table.getName();
            String fkComment = "Chave estrangeira entre a tabela mae " + refTable.getName() + " e a tabela filha "
                    + table.getName();

            geradorVBScript.processRelation(refTable.getName(), table.getName(), foreignKey.getName(),
                    inComplianceFKName, irComment, fkComment, column);

            return;
        }

        LOGGER.debug(table.getName() + "... FK " + foreignKey.getName() + " nao teve nenhum index criado.");
    }

    @SuppressWarnings(UNCHECKED)
    private Set<Column> getPkColumns(Table table) {
        Set<Column> pkColumns = new HashSet<Column>();
        if (table.hasPrimaryKey()) {
            pkColumns.addAll(table.getPrimaryKey().getColumns());
        }
        return pkColumns;
    }

    private String getForeignKeyIndexName(Table table, Table refTable, ForeignKey foreignKey) {
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

    private boolean isForeignKeyReferenceToSingleColumnPrimaryKey(ForeignKey foreignKey) {
        return (!foreignKey.isReferenceToPrimaryKey() || (foreignKey.getColumnSpan() > 1));
    }

    private void validateForeignKeyNameUniqueness(ForeignKey foreignKey) {
        if (fkNameToForeignKey.containsKey(foreignKey.getName())) {
            throw new IllegalArgumentException(" Foreign key '" + foreignKey.getName() + "' referente a coluna "
                    + foreignKey.getColumn(0).getName() + " tem nome duplicado com foreign key referente a coluna "
                    + fkNameToForeignKey.get(foreignKey.getName()).getColumn(0).getName());
        } else {
            fkNameToForeignKey.put(foreignKey.getName(), foreignKey);
        }
    }

    private void validateNameForMultipleForeignKey(Table table, Table refTable, int numberOfFks, ForeignKey foreignKey) {
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
            LOGGER.error("Nome de foreign key da tabela " + table.getName() + " em formato invalido: '" + fkName
                    + "'. Formato esperado '" + format + "' ");
        }
    }

    private void processPrimaryKey(Table table) {
        if (table.hasPrimaryKey()) {
            PrimaryKey pk = table.getPrimaryKey();

            // Se tiver pk, troca por instancia de PrimaryKeyWithConstraintName
            // para que nome da pk seja colocado na definicao da table
            LOGGER.debug(table.getName() + "... table has primary key " + pk.getName()
                    + ". Switching PrimaryKey by PrimaryKeyWithConstraintName.");

            pk.setName(getPkName(table));
            table.setPrimaryKey(new PrimaryKeyWithConstraintName(table.getPrimaryKey()));
        } else {
            LOGGER.warn(table.getName() + "... table does not have any PKs! Generating a key using all columns...");
            PrimaryKey pk = gerarChaveComTodasAsColunas(table);
            table.setPrimaryKey(pk);
        }
    }

    private PrimaryKey gerarChaveComTodasAsColunas(Table table) {
        // Por algum motivo, tabelas criadas automaticamente (por @JoinColumn e @CollectionTable) estao vindo sem PK.
        // Nao sei se e um erro do hibernate ou alguma coisa que fizemos. Para contornar isso, geramos uma chave
        // incluindo todas as colunas da tabela.
        // Isso nao funciona para algumas tabelas @CollectionTable que tem colunas que neo participam da PK. Nesses
        // casos, convertemos uma UniqueKey especificada manualmente em PK.
        PrimaryKey pkBasica = new PrimaryKey();
        pkBasica.setName(getPkName(table));
        pkBasica.setTable(table);

        // Correcao para gerar as primary keys de @CollectionTables a partir de uma UK definida manualmente.
        // alimentado por quirks.properties
        if (quirks.getCollectionTables().contains(table.getName())) {
            Map<String, UniqueKey> uniqueKeysMap = getUniqueKeysMap(table);
            int numeroUKs = uniqueKeysMap.size();
            if (numeroUKs != 1) {
                throw new IllegalStateException("Table does not have PK and has " + numeroUKs
                        + " UKs, is not possible to make the PK.");
            }

            UniqueKey uk = uniqueKeysMap.entrySet().iterator().next().getValue();
            pkBasica.addColumns(uk.getColumnIterator());

            // Infelizmente, mesmo removendo a UK ela volta, nao consegui descobrir onde. Uma possibilidade e usar o
            // mecanismo de substituicao para remove-la.
            uniqueKeysMap.remove(uk.getName());
        } else {
            pkBasica.addColumns(table.getColumnIterator());
        }

        return new PrimaryKeyWithConstraintName(pkBasica);
    }

    private String getPkName(Table table) {
        return "PK_" + tableNamePrefix(table);
    }

    @SuppressWarnings(UNCHECKED)
    private void processUniqueConstraints(Table table) {
        Set<UniqueKey> uniques = newHashSet(table.getUniqueKeyIterator());
        Map<String, UniqueKey> uniqueKeys = getUniqueKeysMap(table);

        for (UniqueKey uk : uniques) {
            uniqueKeys.remove(uk.getName());

            Index redundantIndex = getRedundantIndexForUniqueKey(uk, table);
            if (redundantIndex == null) {
                if (!nomeEspecificadoManualmente(uk.getName(), table)) {
                    String ukName = makeUKName(table, uk);
                    uk.setName(ukName.substring(0, Math.min(UK_NAME_MAX_LENGTH, ukName.length())));
                }

                LOGGER.debug(table.getName() + "... Substituindo UniqueKey por UniqueKeyWithConstraintName para UK "
                        + uk.getName());
                uniqueKeys.put(uk.getName(), new UniqueKeyWithConstraintName(uk));

                validarUniqueComColunasNullable(uk, table.getName());

            } else {
                LOGGER.debug(table.getName() + "... Index " + redundantIndex.getName() + " � redundante com UK "
                        + uk.getName() + "." + " UK ser� removida e substitu�da por UniqueIndex "
                        + redundantIndex.getName());
                Map<String, Index> indexesMap = getIndexesMap(table);
                indexesMap.remove(redundantIndex.getName());
                indexesMap.put(redundantIndex.getName(), new UniqueIndex(redundantIndex));
            }
        }
    }

    /**
     * Checa se ja existe algum index de foreign key relativo as mesmas colunas da UK
     */
    @SuppressWarnings(UNCHECKED)
    private Index getRedundantIndexForUniqueKey(UniqueKey uk, Table table) {
        Set<Column> ukColumns = Sets.newHashSet(uk.getColumnIterator());
        for (Index index : tableToIndexes.get(table)) {
            Set<Column> indexColumns = Sets.newHashSet(index.getColumnIterator());
            if (ukColumns.equals(indexColumns)) {
                return index;
            }
        }
        return null;
    }

    private boolean nomeEspecificadoManualmente(String name, Table table) {
        return name.startsWith(PREFIXO_UK + tableNamePrefix(table));
    }

    @SuppressWarnings(UNCHECKED)
    private String makeUKName(Table table, UniqueKey uk) {
        Set<Column> columns = newHashSet(uk.columnIterator());
        StringBuilder sb = new StringBuilder(PREFIXO_UK).append(tableNamePrefix(table));
        for (Column column : columns) {
            sb.append(UNDERSCORE);
            String uniqueKeyNamePartForColumn = (String) column.getName().subSequence(0,
                    Math.min(PREFIX_LENGTH + 1 + COLUMN_NAME_LENGTH_FOR_UNIQUEKEY_NAME, column.getName().length()));
            sb.append(uniqueKeyNamePartForColumn);
        }
        return sb.toString();
    }

    private void validarUniqueComColunasNullable(UniqueKey uk, String tableName) {
        Iterator<?> columnIterator = uk.getColumnIterator();
        while (columnIterator.hasNext()) {
            Column coluna = (Column) columnIterator.next();
            if (coluna.isNullable()) {
                LOGGER.warn("UK " + uk.getName() + " ( " + tableName + ") cont�m colunas nullable e ser� ignorada.");
                return;
            }
        }
    }

    // "Hibernate wont let me... entao vamos usar um pouco de forca bruta!"
    // Pega a Map "uniqueKeys", que e um campo private de "Table" para permitir
    // substituir instancias de UniqueKey por instancias de
    // UniqueKeyWithConstraintName
    @SuppressWarnings(UNCHECKED)
    private Map<String, UniqueKey> getUniqueKeysMap(Table table) {
        Field f;
        Map<String, UniqueKey> uniqueKeys;
        try {
            f = table.getClass().getDeclaredField("uniqueKeys");
            f.setAccessible(true);
            uniqueKeys = (Map<String, UniqueKey>) f.get(table);
            f.setAccessible(false);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return uniqueKeys;
    }

    // Pega a Map "indexes", que e um campo private de "Table" para permitir
    // substituir instancias de Index por instancias de UniqueIndex
    @SuppressWarnings(UNCHECKED)
    private Map<String, Index> getIndexesMap(Table table) {
        Field f;
        Map<String, Index> indexes;
        try {
            f = table.getClass().getDeclaredField("indexes");
            f.setAccessible(true);
            indexes = (Map<String, Index>) f.get(table);
            f.setAccessible(false);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return indexes;
    }

    private void processColumns(Table table) {
        validarColunasUnique(table);
        validarPrefixoDeColunas(table);
    }

    private void validarColunasUnique(Table table) {
        Iterator<?> iterator = table.getColumnIterator();
        while (iterator.hasNext()) {
            Column coluna = (Column) iterator.next();
            if (coluna.isUnique()) {
                throw new IllegalStateException("Coluna " + coluna.getName()
                        + ": Do not use unique = true, instead, make a @UniqueConstraint "
                        + "in @Table to follow the default for any table.");
            }
        }
    }

    @SuppressWarnings(UNCHECKED)
    private void validarPrefixoDeColunas(Table table) {
        Set<Column> fkColumns = getFkColumns(table);

        boolean validarPrefixoColuna = !DDLUtils.isEnvers(table, config);

        // valida nomes de colunas que nao sao FKs
        Set<Column> notFkColumns = difference(newHashSet(table.getColumnIterator()), fkColumns);
        for (Column column : notFkColumns) {
            validateColumnName(table, column, validarPrefixoColuna);
        }

    }

    @SuppressWarnings(UNCHECKED)
    private Set<Column> getFkColumns(Table table) {
        Set<Column> fkColumns = new HashSet<Column>();

        Set<ForeignKey> fks = Sets.newHashSet(table.getForeignKeyIterator());
        for (ForeignKey fk : fks) {
            fkColumns.addAll(newHashSet(fk.getColumnIterator()));
        }
        return fkColumns;
    }

    private void processComments(Table table) {
        geradorVBScript.processTableProperties(table);
    }

    private void validateColumnName(Table table, Column column, boolean validarPrefixoColuna) {
        String columnName = column.getName();
        Set<String> violations = Sets.newHashSet();

        if (columnName.length() < 5) {
            LOGGER.error("Nome da coluna muito curto! - " + columnName);
            return;
        }

        String columnPrefix = (String) columnName.subSequence(0, PREFIX_LENGTH);

        if (validarPrefixoColuna && !columnPrefix.equals(tableNamePrefix(table))) {
            violations.add("Prefixo da coluna " + columnName + " '" + columnPrefix + "' deveria ser '"
                    + tableNamePrefix(table) + "'");
        }

        if (columnName.length() > COLUMN_NAME_MAX_LENGTH) {
            violations.add("Tamanho do nome da coluna " + columnName + "(" + columnName.length()
                    + ") excede tamanho maximo de (" + COLUMN_NAME_MAX_LENGTH + ")");
        }

        if (!violations.isEmpty()) {
            LOGGER.error(table.getName() + "... Nome de coluna invalido " + columnName + " : \n"
                    + StringUtils.join(violations, "\n"));
        }
    }

    protected static String tableNamePrefix(Table input) {
        return (String) input.getName().subSequence(0, PREFIX_LENGTH);
    }

    public PowerDesignerVBScriptGenerator getGeradorVBScript() {
        return geradorVBScript;
    }

}
