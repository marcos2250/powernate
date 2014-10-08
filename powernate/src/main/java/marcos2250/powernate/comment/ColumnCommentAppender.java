package marcos2250.powernate.comment;

import org.hibernate.mapping.Property;

public interface ColumnCommentAppender {

    String NAO_SE_APLICA = "n/a.";

    String createDomainComment(Property property);

    String createSourceComment(Property property);

}
