package build.pluto.buildmonto;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import build.pluto.builder.BuildRequest;
import build.pluto.builder.Builder;
import build.pluto.builder.BuilderFactory;
import build.pluto.builder.BuilderFactoryFactory;
import build.pluto.buildmaven.MavenDependencyResolver;
import build.pluto.buildmaven.input.MavenInput;
import build.pluto.buildmonto.util.JavaUtil;
import build.pluto.buildmonto.util.ManifestFileGenerator;
import build.pluto.output.None;
import build.pluto.output.Out;


public class ServicesJavascript extends Builder<ServicesJavascript.Input, None> {

	public static class Input implements Serializable {
	    private static final long serialVersionUID = 6927714821370770411L;

	    public final File targetDir;
	    public final File jarLocation;
	    public List<BuildRequest<?, ?, ?, ?>> requiredUnits;

	    public Input(File targetDir, File jarLocation, List<BuildRequest<?, ?, ?, ?>> requiredUnits) {
	        this.targetDir = targetDir;
	        this.jarLocation = jarLocation;
	        this.requiredUnits = requiredUnits;
	    }
	}

    public static BuilderFactory<Input, None, ServicesJavascript> factory
        = BuilderFactoryFactory.of(ServicesJavascript.class, Input.class);

    public ServicesJavascript(Input input) {
        super(input);
    }

    @Override
    public File persistentPath(Input input) {
        return new File(input.targetDir, "services-javascript.dep");
    }

    @Override
    protected String description(Input input) {
        return "Build monto:services-javascript";
    }

    @Override
    protected None build(Input input) throws Throwable {
        //compile services-base-java
    	File servicesBaseJavaJar = new File("target/services-base-java.jar");
        ServicesBaseJava.Input baseInput = new ServicesBaseJava.Input(
        		new File("target/services-base-java"),
        		servicesBaseJavaJar,
                null);

        BuildRequest<?, ?, ?, ?> baseRequest =
            new BuildRequest<>(ServicesBaseJava.factory, baseInput);
        this.requireBuild(baseRequest);

        //resolve maven dependencies
        MavenInput mavenInput = new MavenInput.Builder(
                    Arrays.asList(
                        MavenDependencies.JEROMQ,
                        MavenDependencies.JSON,
                        MavenDependencies.COMMONS_CLI,
                        MavenDependencies.ANTLR)).build();
        BuildRequest<?, Out<ArrayList<File>>, ?, ?> mavenRequest =
            new BuildRequest<>(MavenDependencyResolver.factory, mavenInput);
        ArrayList<File> classpath = this.requireBuild(mavenRequest).val();

        //checkout src
        File checkoutDir = new File(input.targetDir + "-src");
        // TODO require git sync here
        
        //compile src
        classpath.add(servicesBaseJavaJar);
        BuildRequest<?, ?, ?, ?> javaRequest = JavaUtil.compileJava(
                checkoutDir,
                input.targetDir,
                classpath,
                Arrays.<BuildRequest<?, ?, ?, ?>>asList(baseRequest, mavenRequest));

        //build jar
        File manifest = new File(input.targetDir, "manifest.mf");
        File currentWorkingDir = Paths.get("").toFile();
        ManifestFileGenerator.Input mfGeneratorInput = new ManifestFileGenerator.Input(
                currentWorkingDir,
                manifest,
                "1.0",
                "monto.service.ecmascript.ECMAScriptServices",
                classpath,
                false);
        BuildRequest<?,?,?,?> mfGeneratorReq = new BuildRequest<>(ManifestFileGenerator.factory, mfGeneratorInput);
        
        BuildRequest<?, ?, ?, ?>[] requiredUnitsForJar = { javaRequest, mfGeneratorReq };
        BuildRequest<?, ?, ?, ?> jarRequest = JavaUtil.createJar(
                input.targetDir,
                input.jarLocation,
                manifest,
                requiredUnitsForJar);
        this.requireBuild(jarRequest);
        return None.val;
    }
}
