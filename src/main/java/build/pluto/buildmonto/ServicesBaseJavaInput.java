package build.pluto.buildmonto;

import build.pluto.builder.BuildManagers;
import build.pluto.builder.BuildRequest;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

public class ServicesBaseJavaInput implements Serializable {

    public final File src;
    public final File target;
    public final File jarLocation;

    public ServicesBaseJavaInput(File src, File target, File jarLocation) {
        this.src = src;
        this.target = target;
        this.jarLocation = jarLocation;
    }
    public static void main(String[] args) throws IOException {
        ServicesBaseJavaInput input = new ServicesBaseJavaInput(new File("services-base-java"), new File("targetsb"), new File("sbj.jar"));
        BuildManagers.build(new BuildRequest<>(ServicesBaseJavaBuilder.factory, input));
	}
}
