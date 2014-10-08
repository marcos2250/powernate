package marcos2250.powernate.valentrim;

import static com.google.common.io.Files.append;
import static com.google.common.io.Files.createParentDirs;
import static com.google.common.io.Files.touch;
import static com.google.common.io.Files.write;
import static java.nio.charset.Charset.defaultCharset;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import com.google.common.base.Joiner;
import com.google.common.io.Resources;

public class FileMaker {

    public static final String ERRO_AO_GERAR_ARQUIVO = "Erro ao gerar arquivo";
    public static final String ERRO_AO_LER_ARQUIVO = "Erro ao ler arquivo";
    private static final String SQL_COMMANDS_DELIMITER = "; \n\n";

    private final URL ddlAppendFileUrl = Thread.currentThread().getContextClassLoader().getResource("ddl-append.sql");
    private final URL ddlExcludeFileUrl = Thread.currentThread().getContextClassLoader().getResource("ddl-exclude.sql");

    private final GrepDDL grepDDL;
    private final File arquivo;

    public FileMaker(GrepDDL grepDDL, String arquivo) {
        this.grepDDL = grepDDL;
        this.arquivo = new File(arquivo);
        try {
            this.grepDDL.processarExcludes(Resources.readLines(ddlExcludeFileUrl, defaultCharset()));
        } catch (IOException e) {
            throw new IllegalStateException(ERRO_AO_LER_ARQUIVO, e);
        }
    }

    @SuppressWarnings("PMD.SystemPrintln")
    public void gerar() {

        criarArquivo();
        escreverArquivo();
        System.out.println("Arquivo de DDL gerado: " + arquivo.getAbsolutePath());
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
            write(Joiner.on(SQL_COMMANDS_DELIMITER).join(grepDDL.filtrar()) + SQL_COMMANDS_DELIMITER, arquivo,
                    defaultCharset());
            append(Resources.toString(ddlAppendFileUrl, defaultCharset()), arquivo, defaultCharset());
        } catch (IOException e) {
            throw new IllegalStateException(ERRO_AO_GERAR_ARQUIVO, e);
        }
    }
}
