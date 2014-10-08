package marcos2250.powernate.valentrim;

import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.Mapping;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Index;

import com.google.common.collect.Sets;

class UniqueIndex extends Index {

    private static final long serialVersionUID = -2602681844849417874L;

    private static final boolean IS_UNIQUE_INDEX = true;

    UniqueIndex(Index index) {
        setTable(index.getTable());
        @SuppressWarnings("unchecked")
        Set<Column> columns = Sets.newHashSet(index.getColumnIterator());
        for (Column column : columns) {
            addColumn(column);
        }
        setName(index.getName());
    }

    @Override
    public String sqlCreateString(Dialect dialect, Mapping mapping, String defaultCatalog, String defaultSchema)
            throws HibernateException {
        return buildSqlCreateIndexString(dialect, getName(), getTable(), getColumnIterator(), IS_UNIQUE_INDEX,
                defaultCatalog, defaultSchema);
    }

}
