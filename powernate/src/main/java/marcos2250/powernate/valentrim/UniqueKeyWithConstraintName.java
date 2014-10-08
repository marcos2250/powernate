package marcos2250.powernate.valentrim;

import java.util.Set;

import org.hibernate.dialect.Dialect;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.UniqueKey;

import com.google.common.collect.Sets;

class UniqueKeyWithConstraintName extends UniqueKey {

    private static final long serialVersionUID = 1668100002300282145L;

    @SuppressWarnings("unchecked")
    UniqueKeyWithConstraintName(UniqueKey original) {
        setName(original.getName());
        setTable(original.getTable());
        Set<Column> columns = Sets.<Column> newHashSet(original.columnIterator());
        for (Column column : columns) {
            addColumn(column);
        }
    }

    @Override
    public String sqlConstraintString(Dialect dialect) {
        StringBuilder sb = new StringBuilder();
        String sqlConstraintString = super.sqlConstraintString(dialect);
        if (sqlConstraintString != null) {
            sb.append("constraint " + getName() + " ");
            sb.append(sqlConstraintString);
            return sb.toString();
        }
        return super.sqlConstraintString(dialect);
    }
}
