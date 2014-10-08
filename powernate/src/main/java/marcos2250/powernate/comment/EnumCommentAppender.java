package marcos2250.powernate.comment;

import org.hibernate.mapping.Property;

public class EnumCommentAppender implements ColumnCommentAppender {

    private static final int LIMITE_COMENTARIO = 512;

    public EnumCommentAppender() {
        // singleton
    }

    public String createDomainComment(Property property) {

        StringBuilder sb = new StringBuilder();

        Class<?> returnedClass = property.getValue().getType().getReturnedClass();
        if (returnedClass.isEnum()) {
            Enum<?>[] enumConstants = (Enum<?>[]) returnedClass.getEnumConstants();
            for (Enum<?> enumConstant : enumConstants) {
                sb.append(enumConstant.ordinal() + " : " + enumConstant.name() + " | ");
            }
        } else {
            sb.append(NAO_SE_APLICA);
        }

        if (sb.length() > LIMITE_COMENTARIO) {
            sb.setLength(LIMITE_COMENTARIO);
        }

        return sb.toString();
    }

    public String createSourceComment(Property property) {

        Class<?> returnedClass = property.getValue().getType().getReturnedClass();

        if (returnedClass.isEnum()) {
            return "Enumeration " + returnedClass.getSimpleName() + " da aplicacao";
        }

        return NAO_SE_APLICA;

    }

}