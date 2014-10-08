package marcos2250.powernate.util;

import java.awt.Color;

public class ProjectModulesParams {

    private final int classCode;
    private final String description;
    private final int vbcolor;
    private final Color color;

    public ProjectModulesParams(int classCode, String description, int vbcolor, Color color) {
        this.classCode = classCode;
        this.description = description;
        this.vbcolor = vbcolor;
        this.color = color;
    }

    public int getClassCode() {
        return classCode;
    }

    public String getDescription() {
        return description;
    }

    public int getVbcolor() {
        return vbcolor;
    }

    public Color getColor() {
        return color;
    }

}