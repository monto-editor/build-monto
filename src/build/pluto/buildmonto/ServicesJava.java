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
import build.pluto.buildhttp.HTTPDownloader;
import build.pluto.buildhttp.HTTPInput;
import build.pluto.buildjava.JavaBulkBuilder;
import build.pluto.buildjava.JavaInput;
import build.pluto.buildjava.JavaJar;
import build.pluto.buildmaven.MavenDependencyResolver;
import build.pluto.buildmaven.input.MavenInput;
import build.pluto.buildmonto.util.JavaUtil;
import build.pluto.buildmonto.util.ManifestFileGenerator;
import build.pluto.dependency.Origin;
import build.pluto.dependency.RemoteRequirement;
import build.pluto.output.None;

public class ServicesJava extends Builder<ServicesJava.Input, None> {

	public static final String REPO_URL = "https://github.com/monto-editor/services-java";
//	public static final String REPO_URL = "file:///Users/seba/projects/monto/services-java";
	
	public static class Input implements Serializable {
	    private static final long serialVersionUID = 1952189069839703973L;

	    public final File targetDir;
	    public final File jarLocation;
	    
	    public Input(File targetDir, File jarLocation) {
	        this.targetDir = targetDir;
	        this.jarLocation = jarLocation;
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
    	Origin.Builder classOrigin = new Origin.Builder();
    	Origin.Builder requiredForJar = new Origin.Builder();
    	
        //compile services-base-java and build jar
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
        		.build();

        List<File> mavenJars = requireBuild(MavenDependencyResolver.factory, mavenInput).val();
        classOrigin.add(lastBuildReq());

        //get antlr-4.4-complete
        File antlrJar = new File(input.targetDir, "lib/antlr-4.4-complete.jar");
        HTTPInput httpInput = new HTTPInput(
                 "http://www.antlr.org/download/antlr-4.4-complete.jar",
                 antlrJar,
                 RemoteRequirement.CHECK_NEVER);
        requireBuild(HTTPDownloader.factory, httpInput);
        classOrigin.add(lastBuildReq());

        //git sync source code of services-java
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
        classpath.add(antlrJar);
        classpath.add(servicesBaseJavaJar);
        JavaInput javaInput = JavaUtil.compileJava(
                checkoutDir,
                input.targetDir,
                classpath,
                sourceOrigin,
                classOrigin.get());
        requireBuild(JavaBulkBuilder.factory, javaInput);
        requiredForJar.add(lastBuildReq());
        
        //build jar
        File manifest = new File(input.targetDir, "manifest.mf");
        File currentWorkingDir = Paths.get("").toFile();
        ManifestFileGenerator.Input mfGeneratorInput = new ManifestFileGenerator.Input(
                currentWorkingDir,
                manifest,
                "1.0",
                "monto.service.java8.JavaServices",
                classpath,
                false);
        requireBuild(ManifestFileGenerator.factory, mfGeneratorInput);
        requiredForJar.add(lastBuildReq());
        
        JavaJar.Input jarInput = JavaUtil.createJar(
                input.targetDir,
                input.jarLocation,
                manifest,
                requiredForJar.get());
        requireBuild(JavaJar.factory, jarInput);
        
        return None.val;
    }
}
