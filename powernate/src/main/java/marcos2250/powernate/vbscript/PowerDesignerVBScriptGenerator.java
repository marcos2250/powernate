package marcos2250.powernate.vbscript;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;

import marcos2250.powernate.comment.CommentBuilder;
import marcos2250.powernate.graph.GraphGenerator;
import marcos2250.powernate.graph.Node;
import marcos2250.powernate.util.PowernateSessionMediator;
import marcos2250.powernate.util.DDLUtils;
import marcos2250.powernate.valentrim.Quirks;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.hibernate.envers.AuditTable;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

public class PowerDesignerVBScriptGenerator {

    private static final int PD_GRAPH_X_MAX = 520000;
    private static final int PD_GRAPH_Y_MAX = 707700;

    private static final int GRAPH_SCALE_X = GraphGenerator.X_AXIS_WIDTH;
    private static final int GRAPH_SCALE_Y = GraphGenerator.Y_AXIS_HEIGHT;

    private static final String EMPTY_STRING = "";
    private static final String NEWLINE = "\n";
    private static final String UNDERSCORE = "_";
    private static final String WHITE_SPACE = " ";

    private static final String OUTPUT_FILE_NAME = "target/ddl/VBScript_PowerDesigner.txt";

    private static final Logger LOGGER = LoggerFactory.getLogger(PowerDesignerVBScriptGenerator.class);

    private Multimap<Table, PersistentClass> tableToClass;
    private Multimap<String, Property> columnAliasToProperty;

    private GraphGenerator graphGenerator;

    private StringBuffer buffer;

    private PowernateSessionMediator config;

    private Quirks quirks;

    public PowerDesignerVBScriptGenerator(PowernateSessionMediator config, Quirks quirks, Multimap<Table, PersistentClass> tableToClass,
            Multimap<String, Property> columnAliasToProperty) {

        this.config = config;
        this.quirks = quirks;

        this.tableToClass = tableToClass;
        this.columnAliasToProperty = columnAliasToProperty;

        graphGenerator = new GraphGenerator();

        buffer = new StringBuffer();

    }

    public void doPreProcessings(Set<Table> tables) {
        preProcessTables(tables);
    }

    public void doPostProcessings() {
        processUnconstrainedMappings();
        println("doPostProcessings");
    }

    public void processTableProperties(Table table) {

        String tableName = table.getName();
        String className = getQualifiedClassName(table);

        CommentBuilder commentBuilder = new CommentBuilder(config).withHumanizedTableName(table, className);

        if (config.isGenerateSQLComments()) {
            table.setComment(commentBuilder.createSQLStandardizedTableComment());
        } else {
            table.setComment(null);
        }

        printf("setTableProperties \"%s\", \"%s\", %s\n", //
                tableName, //
                commentBuilder.createVBStandardizedTableComment(), //
                getColorForTable(className));

        // graphGenerator.addNode(tableName, getTableClassIndex(table, className));

        processTableColumnsProperties(table);

        grantGroupPermissionForTable(table, config.getDefaultUserGroupName(), "DELETE,INSERT,SELECT,UPDATE");

        grantGroupPermissionForTable(table, config.getDefaultUserGroupNameReadOnly(), "SELECT");

    }

    private void grantGroupPermissionForTable(Table table, String groupName, String operationsToGrant) {
        printf("grantGroupPermissionForTable \"%s\", \"%s\", \"%s\"\n", //
                table.getName(), groupName, operationsToGrant);
    }

    private Integer getColorForTable(String className) {
        return config.getVBColor(className);
    }

    private Integer getTableClassIndex(String className) {
        return config.getClassCode(className);
    }

    private void processTableColumnsProperties(Table table) {
        @SuppressWarnings("unchecked")
        Set<Column> columns = Sets.newHashSet(table.getColumnIterator());

        String tableName = WordUtils.capitalizeFully(table.getName().toLowerCase()).replace(UNDERSCORE, WHITE_SPACE);

        CommentBuilder commentBuilder = new CommentBuilder(config).withTableName(tableName);

        for (Column column : columns) {

            String columnName = WordUtils.capitalizeFully(column.getName().toLowerCase()).replace(UNDERSCORE,
                    WHITE_SPACE);
            Property property = getProperty(table, column);

            boolean isPk = table.getPrimaryKey() != null && table.getPrimaryKey().getColumns().contains(column);

            commentBuilder.withProperty(property).withColumnName(columnName);

            String sequenceName = obterNomeDaSequenceDaColunaSeExistente(table, isPk);

            printf("setColumnProperties \"%s\", \"%s\", \"%s\", \"%s\", \"%s\", %s\n", //
                    table.getName(), //
                    column.getName(), //
                    commentBuilder.createVBStandardizedColumnComment(), //
                    sequenceName, //
                    getSuggestedDefaultValue(column), //
                    isMandatory(column, isPk));

            if (config.isGenerateSQLComments()) {
                column.setComment(commentBuilder.createSQLStandardizedColumnComment());
            }

        }
    }

    private boolean isMandatory(Column column, boolean isPk) {
        return isPk || !column.isNullable();
    }

    private String getSuggestedDefaultValue(Column column) {
        boolean necessitaDefaultValue = !column.isNullable() && column.getValue().isSimpleValue();

        String typeName = column.getValue().getType().getName();

        if (necessitaDefaultValue) {
            if ("PersistentLocalDateTime".equals(typeName)) {
                return "current timestamp";
            }
            if ("PersistentLocalDate".equals(typeName) || "LocalDateUserType".equals(typeName)) {
                return "current date";
            }
            return "0";
        }

        return EMPTY_STRING;
    }

    public void processRelation(String parentTableName, String childTableName, String irName, String fkName,
            String irComment, String fkComment, Column childColumn) {
        String childColumnName = childColumn.getName();

        println("\n\'Relationship for: " + parentTableName + " to " + childTableName);

        String fkNameArgumento = fkName;
        if (quirks.getObjectsToAvoid().contains(fkName)) {
            fkNameArgumento = StringUtils.EMPTY;
        }

        printf("createRelation \"%s\", \"%s\", \"%s\", \"%s\", \"%s\", \"%s\", \"%s\"\n", //
                parentTableName, childTableName, childColumnName, irName, fkNameArgumento, irComment, fkComment);
        println();

        graphGenerator.addNeighbor(parentTableName, childTableName);
    }

    private String getQualifiedClassName(Table table) {
        Collection<PersistentClass> classes = tableToClass.get(table);
        // TODO: pegar a PersistentClass correta quando ha mais de uma!
        PersistentClass entity = classes.size() == 1 ? classes.iterator().next() : null;
        if ((entity != null) && (entity.getMappedClass() != null)) {
            return entity.getMappedClass().getName();
        }
        return null;
    }

    private Property getProperty(Table table, Column column) {
        Collection<Property> properties = columnAliasToProperty.get(getColumnUniqueAlias(table, column));
        for (Property prop : properties) {
            if (tableToClass.get(table).contains(prop.getPersistentClass())) {
                return prop;
            }
        }
        return null;
    }

    public void createSequence(Table table) {
        println("\n\'Table properties for: " + table.getName());
        String sequenceName = getSequenceNameString(table);
        if (StringUtils.isNotBlank(sequenceName)) {
            printf("createSequence \"%s\"\n", sequenceName);
        }
    }

    private String obterNomeDaSequenceDaColunaSeExistente(Table table, boolean isPk) {
        if (!isPk) {
            return EMPTY_STRING;
        }
        return getSequenceNameString(table);
    }

    @SuppressWarnings("unchecked")
    private String getSequenceNameString(Table table) {

        Collection<PersistentClass> collection = tableToClass.get(table);

        if (collection.isEmpty()) {
            return EMPTY_STRING;
        }

        int rootclasses = 0;

        PersistentClass rootClass = null;

        for (PersistentClass persistentClass : collection) {
            if (persistentClass.getMappedClass() == null) {
                continue; // joined tables
            }
            if (persistentClass.getMappedClass().isAnnotationPresent(SequenceGenerator.class)) {
                rootClass = persistentClass;
                rootclasses++;
            }
        }

        if (rootclasses == 0) {
            return EMPTY_STRING;
        }

        if (rootclasses > 1) {
            throw new IllegalStateException("El Multimapa da tabela " + table.getName()
                    + " lo contiene mas clases raizes que lo necessario: " + rootclasses);
        }

        return SequenceGenerator.class.cast(rootClass.getMappedClass()//
                .getAnnotation(SequenceGenerator.class)).sequenceName();
    }

    private void printGraphTablePositions(PrintWriter output) {
        output.println("\n\'Rearranging table graph positions");
        Set<Node> allNodes = graphGenerator.getAllNodes();
        String strPosX;
        String strPosY;
        int xCenter = PD_GRAPH_X_MAX;
        int yCenter = PD_GRAPH_Y_MAX;
        int rescaleFactorX = (PD_GRAPH_X_MAX + PD_GRAPH_X_MAX) / GRAPH_SCALE_X;
        int rescaleFactorY = (PD_GRAPH_Y_MAX + PD_GRAPH_Y_MAX) / GRAPH_SCALE_Y;

        for (Node node : allNodes) {
            strPosX = EMPTY_STRING + (node.getCoordinateX() * rescaleFactorX - xCenter);
            strPosY = EMPTY_STRING + (node.getCoordinateY() * -rescaleFactorY + yCenter);
            output.printf("setTablePosition \"%s\", %s, %s\n", //
                    node.getRelationName(), strPosX, strPosY);
        }
    }

    private String getColumnUniqueAlias(Table table, Column column) {
        return column.getAlias(config.getDialect(), table);
    }

    private void processUnconstrainedMappings() {
        Collection<Entry<Table, PersistentClass>> entries = tableToClass.entries();
        for (Entry<Table, PersistentClass> entry : entries) {
            Table table = entry.getKey();
            processUnconstrainedMappingsForTable(table);
        }
    }

    private void processUnconstrainedMappingsForTable(Table table) {
        Collection<PersistentClass> collection = tableToClass.get(table);

        for (PersistentClass persistentClass : collection) {

            if (persistentClass.getMappedClass() == null) {
                continue;
            }

            Class<?> superEntity = getEntitySuperclass(persistentClass.getMappedClass());

            if (superEntity == null) {
                continue;
            }

            Collection<Entry<Table, PersistentClass>> entries = tableToClass.entries();
            for (Entry<Table, PersistentClass> entry : entries) {
                PersistentClass value = entry.getValue();
                if (value.getMappedClass() == null) {
                    continue;
                }
                if (value.getMappedClass().equals(superEntity)) {
                    graphGenerator.addNeighbor(value.getTable().getName(), table.getName());
                }
            }

        }

    }

    private Class<?> getEntitySuperclass(Class<?> mappedClass) {
        Class<?> superclass = mappedClass.getSuperclass();
        if (superclass == null) {
            return null;
        }
        if (superclass.isAnnotationPresent(Entity.class)) {
            return superclass;
        }
        return getEntitySuperclass(superclass);
    }

    private void preProcessTables(Set<Table> tables) {

        if (config.isRearrangeGraphEnversTables()) {
            graphGenerator.addNode(config.getEnversControlTable(), //
                    config.getEnversTableCode());
            graphGenerator.addNode(config.getEnversControlTable(), //
                    config.getEnversTableCode());

            graphGenerator.addNeighbor(config.getEnversControlTable(), //
                    config.getEnversTypeControlTable());

            for (Table table : tables) {
                Collection<PersistentClass> persistentClasses = tableToClass.get(table);

                for (PersistentClass persistentClass : persistentClasses) {

                    Class<?> mappedClass = persistentClass.getMappedClass();
                    if (mappedClass != null && mappedClass.isAnnotationPresent(AuditTable.class)) {

                        AuditTable annotation = (AuditTable) mappedClass.getAnnotation(AuditTable.class);
                        graphGenerator.addNode(annotation.value(), config.getEnversTableCode());
                        graphGenerator.addNeighbor(annotation.value(), config.getEnversControlTable());
                    }
                }
            }
        }

        for (Table table : tables) {
            if (!config.isRearrangeGraphEnversTables() && DDLUtils.isEnvers(table, config)) {
                continue;
            }

            graphGenerator.addNode(table.getName(), getTableClassIndex(getQualifiedClassName(table)));
        }
    }

    private void printf(String string, Object... args) {
        buffer.append(String.format(string, args));
    }

    private void println() {
        buffer.append(NEWLINE);
    }

    private void println(String string) {
        buffer.append(string);
        buffer.append(NEWLINE);
    }

    public GraphGenerator getGraphGenerator() {
        return graphGenerator;
    }

    public void writeToFile(boolean changeGraphs) {

        PrintWriter output;
        File file;

        try {
            file = new File(OUTPUT_FILE_NAME);
            Files.createParentDirs(file);
            Files.touch(file);
            output = new PrintWriter(file);

            // print header
            InputStream headerFile = Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream("vbscript-powerdesigner-header.txt");
            IOUtils.copy(headerFile, output);
            headerFile.close();

            // print project properties
            output.println("'Default project properties");
            output.println("'--------------------------");

            output.printf("defaultUserName = \"%s\"\n", config.getDefaultUserName());
            output.printf("defaultUserGroupName = \"%s\"\n", config.getDefaultUserGroupName());
            output.printf("defaultEtlGroupName = \"%s\"\n", config.getDefaultETLGroupName());
            output.printf("defaultTableSpace = \"%s\"\n", config.getDefaultTableSpace());
            output.printf("numberOfRows = \"%s\"\n", config.getNumerOfRows());

            // print commands
            output.print(buffer.toString());

            // print graph rearranging commands
            if (changeGraphs) {
                printGraphTablePositions(output);
            }

            output.close();
            LOGGER.info("\nSaved PDM-VBScript file: " + file.getAbsolutePath());

        } catch (IOException e) {
            LOGGER.error("PDM-VBScript generation error!", e);
        }

    }

}
