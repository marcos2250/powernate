package marcos2250.powernate.comment;

import static org.apache.commons.lang.WordUtils.capitalizeFully;

import java.util.Map;

import org.hibernate.mapping.Property;
import org.hibernate.mapping.Table;

import marcos2250.powernate.valentrim.DefaultNamingStrategy;

class CommentBuilderUtil {

    private static final String UNDERSCORE = "_";
    private static final String WHITE_SPACE = " ";

    private static ColumnCommentAppender getColumnCommentAppenderFor(Property property,
            Map<Class<?>, ColumnCommentAppender> columnTypeToCommentAppender) {
        if (property != null) {
            Class<?> clazz = property.getType().getReturnedClass();
            for (Class<?> key : columnTypeToCommentAppender.keySet()) {
                if (key.isAssignableFrom(clazz)) {
                    return columnTypeToCommentAppender.get(key);
                }
            }
        }
        return DefaultColumnCommentAppender.INSTANCE;
    }

    public static String createColumnDomainComment(Property property,
            Map<Class<?>, ColumnCommentAppender> columnTypeToCommentAppender) {
        return getColumnCommentAppenderFor(property, columnTypeToCommentAppender).createDomainComment(property);
    }

    public static String createColumnSourceComment(Property property,
            Map<Class<?>, ColumnCommentAppender> columnTypeToCommentAppender) {
        return getColumnCommentAppenderFor(property, columnTypeToCommentAppender).createSourceComment(property);
    }

    public static String createHumanNameForTable(Table table, String qualifiedClassName) {
        String name;
        if (qualifiedClassName == null) {
            name = capitalizeFully(table.getName().replace(UNDERSCORE, WHITE_SPACE));
        } else {
            name = capitalizeFully(DefaultNamingStrategy.INSTANCE.classToTableName(qualifiedClassName).replace(UNDERSCORE,
                    WHITE_SPACE));
        }
        return name;
    }

}
