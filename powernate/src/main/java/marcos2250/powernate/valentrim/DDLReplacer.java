package marcos2250.powernate.valentrim;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Lists;

public class DDLReplacer {

    private static final String S = "s";

    private List<Substituicao> substituicoes;

    public DDLReplacer(URL file) {
        substituicoes = Lists.newArrayList();
        preencherSubstituicoes(file);
    }

    private void preencherSubstituicoes(URL arquivoSubstituicoes) {
        try {
            Properties props = new Properties();
            props.load(arquivoSubstituicoes.openStream());

            int i = 1;
            boolean sucesso = true;
            while (sucesso) {
                sucesso = preencherSubstituicao(i++, props);
            }
        } catch (IOException e) {
            // Nao faz nada
            e.printStackTrace();
        }
    }

    private boolean preencherSubstituicao(int i, Properties props) {
        String pattern = props.getProperty(S + i + ".pattern");
        if (pattern == null) {
            return false;
        }

        String replace = props.getProperty(S + i + ".replace");

        substituicoes.add(new Substituicao(Pattern.compile(pattern), replace));
        return true;
    }

    public Collection<String> substituir(Collection<String> comandos) {
        Collection<String> resultado = Lists.newArrayList();

        for (String comandoOriginal : comandos) {
            String comandoSubstituido = comandoOriginal;

            for (Substituicao substituicao : substituicoes) {
                Matcher m = substituicao.pattern.matcher(comandoSubstituido);
                comandoSubstituido = m.replaceAll(substituicao.replace);
            }

            resultado.add(comandoSubstituido);
        }

        return resultado;
    }

    // CHECKSTYLE:OFF
    private static class Substituicao {
        public Pattern pattern;
        public String replace;

        public Substituicao(Pattern pattern, String replace) {
            this.pattern = pattern;
            this.replace = replace;
        }
    }
}
