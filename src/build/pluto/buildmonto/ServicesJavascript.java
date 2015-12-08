package build.pluto.buildmonto;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import build.pluto.builder.Builder;
import build.pluto.builder.BuilderFactory;
import build.pluto.builder.BuilderFactoryFactory;
import build.pluto.buildgit.GitInput;
import build.pluto.buildgit.GitRemoteSynchronizer;
import build.pluto.buildgit.bound.BranchBound;
import build.pluto.buildjava.JavaBulkCompiler;
import build.pluto.buildjava.JavaCompilerInput;
import build.pluto.buildjava.JavaJar;
import build.pluto.buildjava.JarManifestGenerator;
import build.pluto.buildmaven.MavenDependencyResolver;
import build.pluto.buildmaven.input.MavenInput;
import build.pluto.buildmonto.util.JavaUtil;
import build.pluto.dependency.Origin;
import build.pluto.dependency.RemoteRequirement;
import build.pluto.output.None;


public class ServicesJavascript extends Builder<ServicesJavascript.Input, None> {

	public static final String REPO_URL = "https://github.com/monto-editor/services-javascript";
	
	public static class Input implements Serializable {
	    private static final long serialVersionUID = 6927714821370770411L;

	    public final File targetDir;
	    public final File jarLocation;

	    public Input(File targetDir, File jarLocation) {
	        this.targetDir = targetDir;
	        this.jarLocation = jarLocation;
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
    	Origin.Builder classOrigin = new Origin.Builder();
    	Origin.Builder requiredForJar = new Origin.Builder();
    	
        //compile services-base-java
    	File servicesBaseJavaJar = new File("target/services-base-java.jar");
        ServicesBaseJava.Input baseInput = new ServicesBaseJava.Input(
        		new File("target/services-base-java"),
        		servicesBaseJavaJar);
        requireBuild(ServicesBaseJava.factory, baseInput);
        classOrigin.add(lastBuildReq());

        //resolve maven dependencies
        MavenInput mavenInput = 
        		new MavenInput.Builder()
        		.addDependency(MavenDependencies.JEROMQ)
        		.addDependency(MavenDependencies.JSON)
        		.addDependency(MavenDependencies.COMMONS_CLI)
        		.addDependency(MavenDependencies.ANTLR)
        		.build();

        List<File> mavenJars = requireBuild(MavenDependencyResolver.factory, mavenInput).val();
        classOrigin.add(lastBuildReq());

        //git sync source code of services-javascript
        File checkoutDir = new File(input.targetDir + "-src");
        GitInput gitInput = 
        		new GitInput.Builder(checkoutDir, REPO_URL)
                .setBound(new BranchBound(REPO_URL, "master"))
                .setConsistencyCheckInterval(RemoteRequirement.CHECK_ALWAYS)
                .build();
        requireBuild(GitRemoteSynchronizer.factory, gitInput);
        Origin sourceOrigin = Origin.from(lastBuildReq());
        
        //compile src
        List<File> classpath = new ArrayList<>(mavenJars);
        classpath.add(servicesBaseJavaJar);
        JavaCompilerInput javaInput = JavaUtil.compileJava(
                checkoutDir,
                input.targetDir,
                classpath,
                sourceOrigin,
                classOrigin.get());
        requireBuild(JavaBulkCompiler.factory, javaInput);
        requiredForJar.add(lastBuildReq());

        //build jar
        File manifest = new File(input.targetDir, "manifest.mf");
        File currentWorkingDir = Paths.get("").toFile();
        JarManifestGenerator.Input mfGeneratorInput = new JarManifestGenerator.Input(
                currentWorkingDir,
                manifest,
                "1.0",
                "monto.service.ecmascript.ECMAScriptServices",
                classpath,
                false);
        requiredForJar.add(JarManifestGenerator.factory, mfGeneratorInput);
        
        JavaJar.Input jarInput = JavaUtil.createJar(
                input.targetDir,
                input.jarLocation,
                manifest,
                requiredForJar.get());
        requireBuild(JavaJar.factory, jarInput);
        
        return None.val;
    }
}
