package build.pluto.buildmonto;

import java.io.File;
import java.io.Serializable;

public class ServicesJavaInput implements Serializable {
    private static final long serialVersionUID = 1952189069839703973L;

    public final File baseTarget = new File("target/services-base-java");
    public final File baseJar = new File("target/sbj.jar");

    public final File srcDir;
    public final File targetDir;
    public final File jarLocation = new File("target/services-java.jar");

    public ServicesJavaInput(File srcDir, File targetDir) {
        this.srcDir = srcDir;
        this.targetDir = targetDir;
    }
}
