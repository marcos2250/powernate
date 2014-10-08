package marcos2250.powernate.comment;

import java.util.Map;

import org.hibernate.mapping.Property;
import org.hibernate.mapping.Table;

import marcos2250.powernate.util.Config;

public class CommentBuilder {

    private static final String TAG_NOME = "/NOME: ";
    private static final String DE = " de ";
    private static final String DOUBLE_QUOTES = "\"";
    private static final String VB_LINE_BREAK = "+ vbcrlf +";
    private static final String DOT = ".";
    private static final String NEWLINE = "\n";

    private Property property;
    private String tableName;
    private String columnName;

    private Map<Class<?>, ColumnCommentAppender> columnTypeToCommentAppender;

    public CommentBuilder(Config config) {
        this.columnTypeToCommentAppender = config.getColumnTypeToCommentAppender();
    }

    public CommentBuilder withProperty(Property property) {
        this.property = property;
        return this;
    }

    public CommentBuilder withTableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    public CommentBuilder withHumanizedTableName(Table table, String className) {
        this.tableName = CommentBuilderUtil.createHumanNameForTable(table, className);
        return this;
    }

    public CommentBuilder withColumnName(String columnName) {
        this.columnName = columnName;
        return this;
    }

    public String createVBStandardizedTableComment() {
        StringBuilder sb = new StringBuilder();
        sb.append(TAG_NOME);
        sb.append(tableName);
        sb.append(DOT);
        sb.append(DOUBLE_QUOTES);
        sb.append(VB_LINE_BREAK);
        sb.append("\"/FUNCAO: Guardar instancias de ");
        sb.append(tableName);
        sb.append(" geradas pela aplicacao.\"");
        sb.append(VB_LINE_BREAK);
        sb.append("\"/EXEMPLOS: Ver exemplos nos campos.\"");
        sb.append(VB_LINE_BREAK); //
        sb.append("\"/FONTE: Dados informados pelo usuario e gerados pelo sistema.");
        return sb.toString();
    }

    public String createSQLStandardizedTableComment() {
        StringBuilder sb = new StringBuilder();
        sb.append(TAG_NOME);
        sb.append(tableName);
        sb.append(".\n");
        sb.append("/FUNCAO: Guardar instancias de ");
        sb.append(tableName);
        sb.append(" geradas pela aplicacao.\n");
        sb.append("/EXEMPLOS: Ver exemplos nos campos.\n"); //
        sb.append("/FONTE: Dados informados pelo usuario e gerados pelo sistema.");
        return sb.toString();
    }

    public String createVBStandardizedColumnComment() {

        String domainComment = CommentBuilderUtil.createColumnDomainComment(property, columnTypeToCommentAppender);
        String sourceComment = CommentBuilderUtil.createColumnSourceComment(property, columnTypeToCommentAppender);

        StringBuilder sb = new StringBuilder();
        sb.append(TAG_NOME);
        sb.append(columnName);
        sb.append(DOT);
        sb.append(DOUBLE_QUOTES);
        sb.append(VB_LINE_BREAK);
        sb.append("\"/FUNCAO: Guardar ");
        sb.append(columnName);
        sb.append(DE);
        sb.append(tableName);
        sb.append(DOT);
        sb.append(DOUBLE_QUOTES);
        sb.append(VB_LINE_BREAK);
        sb.append("\"/DOMINIO: ");
        sb.append(domainComment);
        sb.append(DOUBLE_QUOTES);
        sb.append(VB_LINE_BREAK);
        sb.append("\"/FONTE: ");
        sb.append(sourceComment);
        return sb.toString();
    }

    public String createSQLStandardizedColumnComment() {

        String domainComment = CommentBuilderUtil.createColumnDomainComment(property, columnTypeToCommentAppender);
        String sourceComment = CommentBuilderUtil.createColumnSourceComment(property, columnTypeToCommentAppender);

        StringBuilder sb = new StringBuilder();
        sb.append(TAG_NOME);
        sb.append(columnName);
        sb.append(NEWLINE);
        sb.append("/FUNCAO: Guardar ");
        sb.append(columnName);
        sb.append(DE);
        sb.append(tableName);
        sb.append(DOT);
        sb.append(NEWLINE);
        sb.append("/DOMINIO: ");
        sb.append(domainComment);
        sb.append(NEWLINE);
        sb.append("/FONTE: ");
        sb.append(sourceComment);
        sb.append(NEWLINE);
        return sb.toString();
    }

}
