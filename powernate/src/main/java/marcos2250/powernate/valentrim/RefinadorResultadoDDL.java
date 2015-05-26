package marcos2250.powernate.valentrim;

import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.Lists.newArrayList;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

import marcos2250.powernate.util.PowernateSessionMediator;

public class RefinadorResultadoDDL {

    private static final String WHITESPACE = " ";
    private static final String ALLOW_REVERSE_SCANS = " allow reverse scans";
    private static final String EMPTY = "";
    private static final String SEQUENCE_OPTIONS = //
    " increment by 1 start with 1 maxvalue 2147483647 minvalue 1 no cycle cache 20 no order";

    private static final String CREATE_INDEX_PATTERN = "^((create)( unique)? index)(.*)";

    private final PowernateSessionMediator config;

    public RefinadorResultadoDDL(PowernateSessionMediator config) {
        this.config = config;
    }

    private String getCreateTablePattern(PowernateSessionMediator config) {
        return "^(" + config.getDialect().getCreateTableString() + ")(.*)";
    }

    private String getCreateSequencePattern(PowernateSessionMediator config) {
        return "^(" + config.getCreateSequenceString(EMPTY) + ")(.*)";
    }

    private Function<String, String> getTableSpaceNameTransformation(final PowernateSessionMediator config) {
        return new Function<String, String>() {
            public String apply(String input) {
                String result = input;
                if (input.matches(getCreateTablePattern(config))) {
                    result = input + " in " + config.getDefaultTableSpace();
                }
                return result;
            }
        };
    }

    private Function<String, String> getSequenceOptionsTransformation(final PowernateSessionMediator config) {
        return new Function<String, String>() {
            public String apply(String input) {
                String result = input;
                if (input.matches(getCreateSequencePattern(config))) {
                    result = input + SEQUENCE_OPTIONS;
                }
                return result;
            }
        };
    }

    private Function<String, String> CREATE_INDEX_TRANSFORMATION = //
    new Function<String, String>() {
        public String apply(String input) {
            String result = input;
            Matcher m = Pattern.compile(CREATE_INDEX_PATTERN).matcher(input);
            if (m.matches()) {
                String createIndex = m.group(1);
                StringBuilder sb = new StringBuilder();
                sb.append(input.replace(createIndex + WHITESPACE, createIndex + WHITESPACE + config.getDefaultSchema()
                        + "."));
                sb.append(ALLOW_REVERSE_SCANS);
                result = sb.toString();
            }
            return result;
        }
    };

    public Collection<String> refinar(PowernateSessionMediator config, String[] argScript) {
        List<String> script = newArrayList(argScript);

        List<String> scriptProcessado = newArrayList(//
        transform(transform(transform(//
                script, //
                getTableSpaceNameTransformation(config)), //
                getSequenceOptionsTransformation(config)), //
                CREATE_INDEX_TRANSFORMATION));

        List<String> createTableCommands = filterCommands(getCreateTablePattern(config), script);
        scriptProcessado.addAll(transform(createTableCommands, new TablePermissionsFunction(config, Permission.READ)));
        scriptProcessado.addAll(transform(createTableCommands, new TablePermissionsFunction(config, Permission.WRITE)));

        List<String> createSequenceCommands = filterCommands(getCreateSequencePattern(config), script);
        scriptProcessado.addAll(transform(createSequenceCommands, new SequencePermissionsFunction(config)));

        List<String> createIndexCommands = filterCommands(CREATE_INDEX_PATTERN, scriptProcessado);
        scriptProcessado.removeAll(createIndexCommands);
        Collections.sort(createIndexCommands);
        scriptProcessado.addAll(createIndexCommands);

        return scriptProcessado;
    }

    private List<String> filterCommands(final String pattern, List<String> processedScript) {
        return newArrayList(filter(processedScript, new Predicate<String>() {
            public boolean apply(String input) {
                return input.matches(pattern);
            }
        }));
    }
}
