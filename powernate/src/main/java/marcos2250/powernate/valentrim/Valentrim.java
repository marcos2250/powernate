package marcos2250.powernate.valentrim;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.difference;
import static com.google.common.collect.Sets.newHashSet;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;

import marcos2250.powernate.util.DDLUtils;
import marcos2250.powernate.util.PowernateSessionMediator;
import marcos2250.powernate.vbscript.PowerDesignerVBScriptGenerator;

//CHECKSTYLE:OFF devido Fan-out excessivo
@SuppressWarnings({ "PMD.ExcessiveImports", "PMD.CouplingBetweenObjects", "PMD.FanOut" })
public class Valentrim {
    // CHECKSTYLE:ON

    private static final Logger LOGGER = LoggerFactory.getLogger(Valentrim.class);
    private static final String UNCHECKED = "unchecked";

    private Set<Table> tables;
    private Multimap<Table, PersistentClass> tableToClass;
    private Map<String, ForeignKey> fkNameToForeignKey;
    private Multimap<String, Property> columnAliasToProperty;

    private Configuration hibernateConfiguration;
    private Multimap<Table, Index> tableToIndexes;

    private PowerDesignerVBScriptGenerator geradorVBScript;
    private PowernateSessionMediator config;
    private Quirks quirks;
    private ValentimNamingConvention namingConvention;

    public Valentrim(Configuration hibernateConfiguration, PowernateSessionMediator config) {
        this.hibernateConfiguration = hibernateConfiguration;
        this.namingConvention = new ValentimNamingConvention();
        this.config = config;
    }

    public void process() {
        init();
        namingConvention.generalValidation(tables);

        geradorVBScript = new PowerDesignerVBScriptGenerator(config, quirks, tableToClass, columnAliasToProperty);
        geradorVBScript.doPreProcessings(tables);

        for (Table table : tables) {
            String nomeTabela = table.getName();
            LOGGER.info("Processando " + nomeTabela);
            namingConvention.validateTableName(table, config);
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
                    foreignKey.setName(namingConvention.makeIRName(table, refTable));
                    LOGGER.debug(
                            table.getName() + "... Changed FK name: " + originalName + " -> " + foreignKey.getName());
                } else {
                    // Quando existe mais de uma FK referenciando a mesma
                    // refTable, o nome devera ser previamente
                    // definido. Ele nao sera gerado! Nao e seguro gerar o nome
                    // da FK nesse caso, pois nao haveria como
                    // definir um nome que independesse dos nomes das outras
                    // fks.
                    LOGGER.debug(table.getName() + "... FKs referring same table: " + foreignKey.getName()
                            + " . FK name was NOT changed!");
                    namingConvention.validateNameForMultipleForeignKey(table, refTable, numberOfFks, foreignKey);
                }
                // CHECKSTYLE:ON
                validateForeignKeyNameUniqueness(foreignKey);

                if (isForeignKeyReferenceToSingleColumnPrimaryKey(foreignKey)) {
                    LOGGER.debug(table.getName() + "... ignoring foreign key not referring a simple primary key!");
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
        // composta pkColumns.size() > 1 => captura jointables (why?)
        if (!pkColumns.contains(column) || (pkColumns.size() > 1)) {
            String inComplianceFKName = namingConvention.getForeignKeyIndexName(table, refTable, foreignKey);
            Index index = table.getOrCreateIndex(inComplianceFKName);
            index.addColumn(column);
            LOGGER.debug(table.getName() + "... Creating index " + index.getName() + " for FK " + foreignKey.getName());

            tableToIndexes.put(table, index);

            String irComment = "Referencial integrity between strong table " + refTable.getName() + "  and column "
                    + column.getName() + " of weak table " + table.getName();
            String fkComment = "Foreign key between strong table " + refTable.getName() + " and weak table "
                    + table.getName();

            geradorVBScript.processRelation(refTable.getName(), table.getName(), foreignKey.getName(),
                    inComplianceFKName, irComment, fkComment, column);

            return;
        }

        LOGGER.debug(table.getName() + "... FK " + foreignKey.getName() + " doesn't have any index created.");
    }

    @SuppressWarnings(UNCHECKED)
    private Set<Column> getPkColumns(Table table) {
        Set<Column> pkColumns = new HashSet<Column>();
        if (table.hasPrimaryKey()) {
            pkColumns.addAll(table.getPrimaryKey().getColumns());
        }
        return pkColumns;
    }

    private boolean isForeignKeyReferenceToSingleColumnPrimaryKey(ForeignKey foreignKey) {
        return (!foreignKey.isReferenceToPrimaryKey() || (foreignKey.getColumnSpan() > 1));
    }

    private void validateForeignKeyNameUniqueness(ForeignKey foreignKey) {
        if (fkNameToForeignKey.containsKey(foreignKey.getName())) {
            throw new IllegalArgumentException(" Foreign key '" + foreignKey.getName() + "' referring column "
                    + foreignKey.getColumn(0).getName() + " has duplicated foreign key for column "
                    + fkNameToForeignKey.get(foreignKey.getName()).getColumn(0).getName());
        } else {
            fkNameToForeignKey.put(foreignKey.getName(), foreignKey);
        }
    }

    private void processPrimaryKey(Table table) {
        if (table.hasPrimaryKey()) {
            PrimaryKey pk = table.getPrimaryKey();

            // Se tiver pk, troca por instancia de PrimaryKeyWithConstraintName
            // para que nome da pk seja colocado na definicao da table
            LOGGER.debug(table.getName() + "... table has primary key " + pk.getName()
                    + ". Switching PrimaryKey by PrimaryKeyWithConstraintName.");

            pk.setName(namingConvention.getPkName(table));
            table.setPrimaryKey(new PrimaryKeyWithConstraintName(table.getPrimaryKey()));
        } else {
            LOGGER.warn(table.getName() + "... table does not have any PKs! Generating a key using all columns...");
            PrimaryKey pk = gerarChaveComTodasAsColunas(table);
            table.setPrimaryKey(pk);
        }
    }

    private PrimaryKey gerarChaveComTodasAsColunas(Table table) {
        // Por algum motivo, tabelas criadas automaticamente (por @JoinColumn e
        // @CollectionTable) estao vindo sem PK.
        // Nao sei se e um erro do hibernate ou alguma coisa que fizemos. Para
        // contornar isso, geramos uma chave
        // incluindo todas as colunas da tabela.
        // Isso nao funciona para algumas tabelas @CollectionTable que tem
        // colunas que neo participam da PK. Nesses
        // casos, convertemos uma UniqueKey especificada manualmente em PK.
        PrimaryKey pkBasica = new PrimaryKey();
        pkBasica.setName(namingConvention.getPkName(table));
        pkBasica.setTable(table);

        // Correcao para gerar as primary keys de @CollectionTables a partir de
        // uma UK definida manualmente.
        // alimentado por quirks.properties
        if (quirks.getCollectionTables().contains(table.getName())) {
            Map<String, UniqueKey> uniqueKeysMap = getUniqueKeysMap(table);
            int numeroUKs = uniqueKeysMap.size();
            if (numeroUKs != 1) {
                throw new IllegalStateException(
                        "Table does not have PK and has " + numeroUKs + " UKs, is not possible to make the PK.");
            }

            UniqueKey uk = uniqueKeysMap.entrySet().iterator().next().getValue();
            pkBasica.addColumns(uk.getColumnIterator());

            // Infelizmente, mesmo removendo a UK ela volta, nao consegui
            // descobrir onde. Uma possibilidade e usar o
            // mecanismo de substituicao para remove-la.
            uniqueKeysMap.remove(uk.getName());
        } else {
            pkBasica.addColumns(table.getColumnIterator());
        }

        return new PrimaryKeyWithConstraintName(pkBasica);
    }

    @SuppressWarnings(UNCHECKED)
    private void processUniqueConstraints(Table table) {
        Set<UniqueKey> uniques = newHashSet(table.getUniqueKeyIterator());
        Map<String, UniqueKey> uniqueKeys = getUniqueKeysMap(table);

        for (UniqueKey uk : uniques) {
            uniqueKeys.remove(uk.getName());

            Index redundantIndex = getRedundantIndexForUniqueKey(uk, table);
            if (redundantIndex == null) {
                uk.setName(namingConvention.makeUKName(table, uk));

                LOGGER.debug(table.getName() + "... Replacing UniqueKey by UniqueKeyWithConstraintName for the UK "
                        + uk.getName());
                uniqueKeys.put(uk.getName(), new UniqueKeyWithConstraintName(uk));

                validarUniqueComColunasNullable(uk, table.getName());

            } else {
                LOGGER.debug(table.getName() + "... Index " + redundantIndex.getName() + " is redundant to UK "
                        + uk.getName() + "." + " UK will be removed and replaced by UniqueIndex "
                        + redundantIndex.getName());
                Map<String, Index> indexesMap = getIndexesMap(table);
                indexesMap.remove(redundantIndex.getName());
                indexesMap.put(redundantIndex.getName(), new UniqueIndex(redundantIndex));
            }
        }
    }

    /**
     * Checa se ja existe algum index de foreign key relativo as mesmas colunas
     * da UK
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

    private void validarUniqueComColunasNullable(UniqueKey uk, String tableName) {
        Iterator<?> columnIterator = uk.getColumnIterator();
        while (columnIterator.hasNext()) {
            Column coluna = (Column) columnIterator.next();
            if (coluna.isNullable()) {
                LOGGER.warn(
                        "UK " + uk.getName() + " ( " + tableName + ") contains nullable columns and shall be ignored.");
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
                throw new IllegalStateException(
                        "Column " + coluna.getName() + ": Do not use unique = true, instead, make a @UniqueConstraint "
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
            namingConvention.validateColumnName(table, column, validarPrefixoColuna);
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

    public PowerDesignerVBScriptGenerator getGeradorVBScript() {
        return geradorVBScript;
    }

}
