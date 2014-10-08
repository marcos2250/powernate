package marcos2250.powernate.valentrim;

import static org.apache.commons.lang.StringUtils.isNotEmpty;

import java.util.Set;

import org.hibernate.dialect.Dialect;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.PrimaryKey;

import com.google.common.collect.Sets;

class PrimaryKeyWithConstraintName extends PrimaryKey {

    private static final long serialVersionUID = -5901829161995890046L;

    @SuppressWarnings("unchecked")
    PrimaryKeyWithConstraintName(PrimaryKey original) {
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
        if (isNotEmpty(getName())) {
            sb.append("constraint " + getName() + " ");
        }
        sb.append(super.sqlConstraintString(dialect));
        return sb.toString();
    }
}