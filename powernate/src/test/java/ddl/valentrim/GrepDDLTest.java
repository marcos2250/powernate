package ddl.valentrim;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Test;

import com.google.common.collect.Lists;

import marcos2250.powernate.valentrim.GrepDDL;


@SuppressWarnings("PMD.MagicNumbers")
public class GrepDDLTest {

    @Test
    public void deveExcluirLinhasDeComentario() {

        GrepDDL grepDDL = new GrepDDL(ddlQualquer());
        Collection<String> filtrado = grepDDL //
                .semComentarios() //
                .filtrar();

        assertEquals(5, filtrado.size());
        assertTrue("lista filtrada contem comentario: " + filtrado, naoDeveConterComentarios(filtrado));
    }

    @Test
    public void deveExcluirLinhasDeCriacaoDeIndex() {

        GrepDDL grepDDL = new GrepDDL(ddlQualquer());
        Collection<String> filtrado = grepDDL //
                .semCreateIndex() //
                .filtrar();

        assertEquals(4, filtrado.size());
        assertTrue("Lista filtrada nao deveria conter create index: " + filtrado, naoDeveConterCreateIndex(filtrado));
    }

    @Test
    public void deveProcessarExcludes() {

        GrepDDL grepDDL = new GrepDDL(ddlQualquer());
        Collection<String> filtrado = grepDDL //
                .processarExcludes(Lists.newArrayList(statementQualquer())) //
                .filtrar();

        assertEquals(5, filtrado.size());
        assertTrue("Lista filtrada nao deveria conter statement: " + filtrado, naoDeveConterStatement(filtrado));
    }

    private Collection<String> ddlQualquer() {
        return Lists.newArrayList("create table abc", //
                "alter table xxx add column abc varchar", //
                "comment on column xxx.abc Blah 123 blah:/\\*`^", //
                "create index xyz", //
                "create unique index xyz", //
                statementQualquer());
    }

    private String statementQualquer() {
        return "alter table xxx.abc add constraint IRdefabc foreign key (ID1, ID2) references xxx.def";
    }

    private boolean naoDeveConterCreateIndex(Collection<String> filtrado) {
        for (String linha : filtrado) {
            if (linha.startsWith("create index") || linha.startsWith("create unique index")) {
                return false;
            }
        }
        return true;
    }

    private boolean naoDeveConterStatement(Collection<String> filtrado) {
        for (String linha : filtrado) {
            if (linha.contains(statementQualquer())) {
                return false;
            }
        }
        return true;
    }

    private boolean naoDeveConterComentarios(Collection<String> filtrado) {
        for (String linha : filtrado) {
            if (linha.startsWith("comment on")) {
                return false;
            }
        }
        return true;
    }
}
