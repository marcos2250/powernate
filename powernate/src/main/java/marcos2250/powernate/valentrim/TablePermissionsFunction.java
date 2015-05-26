package marcos2250.powernate.valentrim;

import static org.apache.commons.lang.StringUtils.EMPTY;

import com.google.common.base.Function;

import marcos2250.powernate.util.PowernateSessionMediator;

/**
 * Cria permissoes padrao de READ ou WRITE correspondentes a cada tabela
 * 
 * Exemplo:
 * 
 * (entrada) create table AAA.XYZ (...)
 * 
 * ( saida READ) grant delete, insert, select, update on AAA.XYZ to GAAADW
 * 
 * ou
 * 
 * ( saida WRITE) grant select on AAA.XYZ to GAAADR
 */
public class TablePermissionsFunction implements Function<String, String> {

    private static final String WHITESPACE = " ";
    private static final int TABLE_NAME_INDEX = 2;
    private static final String TO = " to ";

    private Permission permission;

    private PowernateSessionMediator config;

    public TablePermissionsFunction(PowernateSessionMediator config, Permission pemission) {
        this.config = config;
        this.permission = pemission;
    }

    public String apply(String input) {
        int pos = input.indexOf(config.getDialect().getCreateTableString());
        boolean isCreateTableCommand = pos >= 0;
        String tableName = input.split(WHITESPACE)[TABLE_NAME_INDEX];
        if (isCreateTableCommand) {
            if (Permission.WRITE.equals(this.permission)) {
                return "grant delete, insert, select, update on " + tableName + TO
                        + config.getPermissionGroupName(Permission.WRITE);
            }
            if (Permission.READ.equals(this.permission)) {
                return "grant select on " + tableName + TO + config.getPermissionGroupName(Permission.READ);
            }
        }
        return EMPTY;
    }

}
