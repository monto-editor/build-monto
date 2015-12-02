package build.pluto.buildmonto;

import build.pluto.builder.BuildManagers;
import build.pluto.builder.BuildRequest;

import java.io.File;
import java.io.Serializable;

public class ServicesJavaInput implements Serializable {
    private static final long serialVersionUID = 1952189069839703973L;

    public final File baseTarget = new File("target-sbj");
    public final File baseJar = new File("sbj.jar");

    public final File srcDir;
    public final File targetDir;
    public final File jarLocation = new File("services-java.jar");

    public ServicesJavaInput(File srcDir, File targetDir) {
        this.srcDir = srcDir;
        this.targetDir = targetDir;
    }

    public static void main(String[] args) throws Throwable {
        ServicesJavaInput input = new ServicesJavaInput(
                new File("services-java"),
                new File("targetsj"));
        BuildManagers.build(
                new BuildRequest<>(ServicesJavaBuilder.factory, input));
    }
}
