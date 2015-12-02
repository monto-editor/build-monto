package build.pluto.buildmonto;

import build.pluto.builder.BuildManagers;
import build.pluto.builder.BuildRequest;

import java.io.File;
import java.io.Serializable;

public class ServicesJavascriptInput implements Serializable {
    private static final long serialVersionUID = 6927714821370770411L;

    public final File baseTarget = new File("target-sbj");
    public final File baseJar = new File("sbj.jar");

    public final File srcDir;
    public final File targetDir;
    public final File jarLocation = new File("services-javascript.jar");

    public ServicesJavascriptInput(File srcDir, File targetDir) {
        this.srcDir = srcDir;
        this.targetDir = targetDir;
    }

    public static void main(String[] args) throws Throwable {
        ServicesJavascriptInput input = new ServicesJavascriptInput(
                new File("services-javascript"),
                new File("targetsjs"));
        BuildManagers.build(
                new BuildRequest<>(ServicesJavascriptBuilder.factory, input));
    }
}
