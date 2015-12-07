package build.pluto.buildmonto.util;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sugarj.common.FileCommands;

import build.pluto.buildjava.JavaInput;
import build.pluto.buildjava.JavaJar;
import build.pluto.buildjava.compiler.JavacCompiler;
import build.pluto.buildjava.util.FileExtensionFilter;
import build.pluto.dependency.Origin;

public class JavaUtil {
    public static final FileFilter classFileFilter = new FileExtensionFilter("class");
    public static final FileFilter javaFileFilter = new FileExtensionFilter("java");
    public static final FileFilter jarFileFilter = new FileExtensionFilter("jar");

    public static JavaInput compileJava(
            File src,
            File target,
            List<File> jarFiles,
            Origin sourceOrigin,
            Origin classOrigin) {

    	// TODO need to install file/directory requirement to get notified about added Java files.
        List<File> javaFiles = FileCommands.listFilesRecursive(src, javaFileFilter);
        List<File> sourcePath = Arrays.asList(new File(src, "src"));
        
        if (javaFiles.isEmpty())
        	throw new IllegalStateException("Tried to compile empty list of Java files");
        
        JavaInput javaInput = new JavaInput
        		.Builder()
        		.addInputFiles(javaFiles)
        		.setTargetDir(target)
        		.addSourcePaths(sourcePath)
        		.addClassPaths(jarFiles)
        		.setSourceRelease("1.8")
        		.setTargetRelease("1.8")
        		.setSourceOrigin(sourceOrigin)
        		.setClassOrigin(classOrigin)
        		.setCompiler(JavacCompiler.instance)
        		.get();
        
        return javaInput;
    }

    public static JavaJar.Input createJar(
            File target,
            File jarToCreate,
            File manifest,
            Origin classOrigin) {

    	List<File> classfilePaths = FileCommands.listFilesRecursive(target, classFileFilter);
        Set<File> classfileSet = new HashSet<>(classfilePaths);

        if (classfileSet.isEmpty())
        	throw new IllegalStateException("Tried to create JAR file containing no class files");
        
        JavaJar.Input jarInput = new JavaJar.Input(
                JavaJar.Mode.CreateOrUpdate,
                jarToCreate,
                manifest,
                Collections.singletonMap(target, classfileSet),
                classOrigin);
        return jarInput;
    }
}
