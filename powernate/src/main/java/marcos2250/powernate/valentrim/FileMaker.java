package marcos2250.powernate.valentrim;

import static com.google.common.io.Files.append;
import static com.google.common.io.Files.createParentDirs;
import static com.google.common.io.Files.touch;
import static com.google.common.io.Files.write;
import static java.nio.charset.Charset.defaultCharset;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.io.Resources;

public class FileMaker {

    public static final String ERRO_AO_GERAR_ARQUIVO = "Erro ao gerar arquivo";
    public static final String ERRO_AO_LER_ARQUIVO = "Erro ao ler arquivo";
    private static final String SQL_COMMANDS_DELIMITER = "; \n\n";

    private static final Logger LOGGER = LoggerFactory.getLogger(FileMaker.class);

    private final URL ddlAppendFileUrl = Thread.currentThread().getContextClassLoader().getResource("ddl-append.sql");
    private final URL ddlExcludeFileUrl = Thread.currentThread().getContextClassLoader().getResource("ddl-exclude.sql");

    private final URL ddlSubstituicoesFileUrl = Thread.currentThread().getContextClassLoader()
            .getResource("ddl-replacements.properties");

    private final GrepDDL grepDDL;
    private final File arquivo;

    public FileMaker(GrepDDL grepDDL, String arquivo) {
        this.grepDDL = grepDDL;
        this.arquivo = new File(arquivo);
        preencherListaDeExclusoes();
    }

    private void preencherListaDeExclusoes() {
        try {
            this.grepDDL.processarExcludes(Resources.readLines(ddlExcludeFileUrl, defaultCharset()));
        } catch (IOException e) {
            throw new IllegalStateException(ERRO_AO_LER_ARQUIVO, e);
        }
    }

    public void gerar() {

        criarArquivo();
        escreverArquivo();
        LOGGER.info("Arquivo de DDL gerado: " + arquivo.getAbsolutePath());
    }

    private void criarArquivo() {
        try {
            createParentDirs(arquivo);
            touch(arquivo);
        } catch (IOException e) {
            throw new IllegalStateException(ERRO_AO_GERAR_ARQUIVO, e);
        }
    }

    private void escreverArquivo() {
        try {
            Collection<String> comandosGerados = grepDDL.filtrar();

            DDLReplacer substituidor = new DDLReplacer(ddlSubstituicoesFileUrl);
            Collection<String> comandosAposSubstituicao = substituidor.substituir(comandosGerados);

            escreverComandosGerados(comandosAposSubstituicao);

            escreverComandosIncluidosManualmente();
        } catch (IOException e) {
            throw new IllegalStateException(ERRO_AO_GERAR_ARQUIVO, e);
        }
    }

    private void escreverComandosIncluidosManualmente() throws IOException {
        append(Resources.toString(ddlAppendFileUrl, defaultCharset()), arquivo, defaultCharset());
    }

    private void escreverComandosGerados(Collection<String> comandosGerados) throws IOException {
        write(Joiner.on(SQL_COMMANDS_DELIMITER).join(comandosGerados) + SQL_COMMANDS_DELIMITER, arquivo,
                defaultCharset());
    }
}
