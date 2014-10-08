package marcos2250.powernate.util;

public class ClassloaderUtil {

    private static final String MSG_ERROR = "Class name not specified!";

    @SuppressWarnings("unchecked")
    public static <T> Class<T> getClassFromClasspath(String name) {
        if (name == null) {
            throw new NullPointerException(MSG_ERROR);
        }
        try {
            return (Class<T>) Class.forName(name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getInstanceFromClasspath(String name) {
        if (name == null) {
            throw new NullPointerException(MSG_ERROR);
        }
        try {
            return (T) Class.forName(name).newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

}
