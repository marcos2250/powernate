package marcos2250.powernate.valentrim;

import static org.apache.commons.lang.StringUtils.EMPTY;

import com.google.common.base.Function;

import marcos2250.powernate.util.Config;

public class SequencePermissionsFunction implements Function<String, String> {

    private static final int SEQUENCE_NAME_INDEX = 2;

    private Config config;

    public SequencePermissionsFunction(Config config) {
        this.config = config;
    }

    public String apply(String input) {
        int pos = input.indexOf(config.getCreateSequenceString(""));
        boolean isCreateSequenceCommand = pos >= 0;
        String sequenceName = input.split(" ")[SEQUENCE_NAME_INDEX];
        if (isCreateSequenceCommand) {
            return "grant usage, alter on sequence " + sequenceName + " to "
                    + config.getPermissionGroupName(Permission.WRITE);
        }
        return EMPTY;
    }

}
