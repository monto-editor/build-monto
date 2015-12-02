package build.pluto.buildmonto;

import java.io.File;
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

public class ServicesJavaBuilder extends Builder<ServicesJavaInput, None> {

    public static BuilderFactory<ServicesJavaInput, None, ServicesJavaBuilder> factory
        = BuilderFactoryFactory.of(ServicesJavaBuilder.class, ServicesJavaInput.class);

    public ServicesJavaBuilder(ServicesJavaInput input) {
        super(input);
    }

    @Override
    public File persistentPath(ServicesJavaInput input) {
        return new File(input.targetDir, "services-java.dep");
    }

    @Override
    protected String description(ServicesJavaInput input) {
        return "Build monto:services-java";
    }

    @Override
    protected None build(ServicesJavaInput input) throws Throwable {
        //compile services-base-java and build jar
    	File servicesBaseJavaJar = new File("target/services-base-java.jar");
    	ServicesBaseJavaInput baseInput = new ServicesBaseJavaInput(
        		new File("target/services-base-java"),
        		servicesBaseJavaJar,
                null);
        BuildRequest<?, ?, ?, ?> baseRequest =
            new BuildRequest<>(ServicesBaseJavaBuilder.factory, baseInput);
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
        HTTPInput httpInput = new HTTPInput(
                 "http://www.antlr.org/download/antlr-4.4-complete.jar",
                 new File("lib/antlr-4.4-complete.jar"),
                 RemoteRequirement.CHECK_NEVER);
        BuildRequest<?, ?, ?, ?> httpRequest =
            new BuildRequest<>(HTTPDownloader.factory, httpInput);
        this.requireBuild(httpRequest);

        //git sync src
        File checkoutDir = new File(input.targetDir + "-src");
        // TODO require git sync here
        
        //compile src
        classpath.add(new File("lib/antlr-4.4-complete.jar"));
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
