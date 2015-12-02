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
import build.pluto.buildhttp.HTTPDownloader;
import build.pluto.buildhttp.HTTPInput;
import build.pluto.buildmaven.MavenDependencyResolver;
import build.pluto.buildmaven.input.MavenInput;
import build.pluto.buildmonto.util.JavaUtil;
import build.pluto.buildmonto.util.ManifestFileGenerator;
import build.pluto.dependency.RemoteRequirement;
import build.pluto.output.None;
import build.pluto.output.Out;

public class ServicesJava extends Builder<ServicesJava.Input, None> {

	public static class Input implements Serializable {
	    private static final long serialVersionUID = 1952189069839703973L;

	    public final File targetDir;
	    public final File jarLocation;
	    public List<BuildRequest<?, ?, ?, ?>> requiredUnits;
	    
	    public Input(File targetDir, File jarLocation, List<BuildRequest<?, ?, ?, ?>> requiredUnits) {
	        this.targetDir = targetDir;
	        this.jarLocation = jarLocation;
	        this.requiredUnits = requiredUnits;
	    }
	}

	
    public static BuilderFactory<Input, None, ServicesJava> factory = BuilderFactoryFactory.of(ServicesJava.class, Input.class);

    public ServicesJava(Input input) {
        super(input);
    }

    @Override
    public File persistentPath(Input input) {
        return new File(input.targetDir, "services-java.dep");
    }

    @Override
    protected String description(Input input) {
        return "Build monto:services-java";
    }

    @Override
    protected None build(Input input) throws Throwable {
        //compile services-base-java and build jar
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
                        MavenDependencies.COMMONS_CLI)).build();
        BuildRequest<?, Out<ArrayList<File>>, ?, ?> mavenRequest =
            new BuildRequest<>(MavenDependencyResolver.factory, mavenInput);
        ArrayList<File> classpath = this.requireBuild(mavenRequest).val();

        //get antlr-4.4-complete
        File antlrJar = new File(input.targetDir, "lib/antlr-4.4-complete.jar");
        HTTPInput httpInput = new HTTPInput(
                 "http://www.antlr.org/download/antlr-4.4-complete.jar",
                 antlrJar,
                 RemoteRequirement.CHECK_NEVER);
        BuildRequest<?, ?, ?, ?> httpRequest =
            new BuildRequest<>(HTTPDownloader.factory, httpInput);
        this.requireBuild(httpRequest);

        //git sync src
        File checkoutDir = new File(input.targetDir + "-src");
        // TODO require git sync here
        
        //compile src
        classpath.add(antlrJar);
        classpath.add(servicesBaseJavaJar);
        List<BuildRequest<?, ?, ?, ?>> requiredUnits =
            Arrays.<BuildRequest<?, ?, ?, ?>>asList(baseRequest, mavenRequest, httpRequest);
        BuildRequest<?, ?, ?, ?> javaRequest = JavaUtil.compileJava(
                checkoutDir,
                input.targetDir,
                classpath,
                requiredUnits);
        this.requireBuild(javaRequest);

        //build jar
        File manifest = new File(input.targetDir, "manifest.mf");
        File currentWorkingDir = Paths.get("").toFile();
        ManifestFileGenerator mfGenerator = new ManifestFileGenerator(
                currentWorkingDir,
                manifest,
                "1.0",
                "monto.service.java8.JavaServices",
                classpath,
                false);
        mfGenerator.generate();
        provide(manifest);
        
        BuildRequest<?, ?, ?, ?>[] requiredUnitsForJar = { javaRequest };
        BuildRequest<?, ?, ?, ?> jarRequest = JavaUtil.createJar(
                input.targetDir,
                input.jarLocation,
                manifest,
                requiredUnitsForJar);
        this.requireBuild(jarRequest);
        return None.val;
    }
}
