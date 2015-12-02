package build.pluto.buildmonto;

import java.io.File;
import java.io.Serializable;

public class ServicesJavascriptInput implements Serializable {
    private static final long serialVersionUID = 6927714821370770411L;

    public final File baseTarget = new File("target/services-base-java");
    public final File baseJar = new File("target/sbj.jar");

    public final File srcDir;
    public final File targetDir;
    public final File jarLocation = new File("services-javascript.jar");

    public ServicesJavascriptInput(File srcDir, File targetDir) {
        this.srcDir = srcDir;
        this.targetDir = targetDir;
    }
}
