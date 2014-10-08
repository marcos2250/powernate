package marcos2250.powernate.comment;

import org.hibernate.mapping.Property;

public class DefaultColumnCommentAppender implements ColumnCommentAppender {

    public static final DefaultColumnCommentAppender INSTANCE = new DefaultColumnCommentAppender();

    private DefaultColumnCommentAppender() {
        // singleton
    }

    public String createDomainComment(Property property) {
        return NAO_SE_APLICA;
    }

    public String createSourceComment(Property property) {
        return "Dados gerados pelo sistema ou usuario.";
    }

}
