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
import build.pluto.buildmaven.MavenDependencyResolver;
import build.pluto.buildmaven.input.MavenInput;
import build.pluto.buildmonto.util.JavaUtil;
import build.pluto.buildmonto.util.ManifestFileGenerator;
import build.pluto.output.None;
import build.pluto.output.Out;


public class ServicesJavascriptBuilder extends Builder<ServicesJavascriptInput, None> {

    public static BuilderFactory<ServicesJavascriptInput, None, ServicesJavascriptBuilder> factory
        = BuilderFactoryFactory.of(ServicesJavascriptBuilder.class, ServicesJavascriptInput.class);

    public ServicesJavascriptBuilder(ServicesJavascriptInput input) {
        super(input);
    }

    @Override
    public File persistentPath(ServicesJavascriptInput input) {
        return new File(input.targetDir, "services-javascript.dep");
    }

    @Override
    protected String description(ServicesJavascriptInput input) {
        return "Build monto:services-javascript";
    }

    @Override
    protected None build(ServicesJavascriptInput input) throws Throwable {
        //compile services-base-java
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
        this.requireBuild(javaRequest);

        //build jar
        File manifest = new File(input.targetDir, "manifest.mf");
        File currentWorkingDir = Paths.get("").toFile();
        ManifestFileGenerator mfGenerator = new ManifestFileGenerator(
                currentWorkingDir,
                manifest,
                "1.0",
                "monto.service.ecmascript.ECMAScriptServices",
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
