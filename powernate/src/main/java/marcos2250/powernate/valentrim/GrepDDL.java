package marcos2250.powernate.valentrim;

import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import com.google.common.base.Predicate;

public class GrepDDL {

    private List<String> script;
    private Set<String> excludes;

    public GrepDDL(Collection<String> script) {
        this.script = newArrayList(script);
        excludes = newHashSet();
    }

    public GrepDDL semComentarios() {
        excludes.add("^comment on [\\w\\W]*$");
        return this;
    }

    public GrepDDL semCreateIndex() {
        excludes.add("^create (unique )?index [\\w\\W]*$");
        return this;
    }

    public GrepDDL processarExcludes(List<String> statements) {
        for (String statement : statements) {
            excludes.add("^.*" + Pattern.quote(statement) + ".*$");
        }
        return this;
    }

    public Collection<String> filtrar() {
        return newArrayList(filter(script, filtraLinhasExcluidas()));
    }

    private Predicate<String> filtraLinhasExcluidas() {
        return new Predicate<String>() {
            public boolean apply(String linha) {
                return filter(excludes, verificaLinhaDeveSerExcluida(linha)).isEmpty();
            }
        };
    }

    private Predicate<String> verificaLinhaDeveSerExcluida(final String linha) {
        return new Predicate<String>() {
            public boolean apply(String pattern) {
                return linha.matches(pattern);
            }
        };
    }
}
