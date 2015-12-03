package build.pluto.buildmonto.util;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sugarj.common.FileCommands;

import build.pluto.builder.BuildRequest;
import build.pluto.buildjava.JavaBulkBuilder;
import build.pluto.buildjava.JavaInput;
import build.pluto.buildjava.JavaJar;
import build.pluto.buildjava.compiler.JavacCompiler;
import build.pluto.buildjava.util.FileExtensionFilter;

public class JavaUtil {
    public static final FileFilter classFileFilter = new FileExtensionFilter("class");
    public static final FileFilter javaFileFilter = new FileExtensionFilter("java");
    public static final FileFilter jarFileFilter = new FileExtensionFilter("jar");

    public static BuildRequest<?, ?, ?, ?> compileJava(
            File src,
            File target,
            List<File> jarFiles,
            List<BuildRequest<?,?,?,?>> requiredUnits) {

    	// TODO need to install file/directory requirement to get notified about added Java files.
        List<Path> javaPaths = FileCommands.listFilesRecursive(src.toPath(), javaFileFilter);
        List<File> javaFiles = convertPathToFileList(javaPaths);
        List<File> sourcePath = Arrays.asList(new File(src, "src"));
        
        if (javaFiles.isEmpty())
        	throw new IllegalStateException("Tried to compile empty list of Java files");
        
        JavaInput javaInput = new JavaInput(
                javaFiles,
                target,
                sourcePath,
                jarFiles,
                null,
                "1.8",
                "1.8",
                requiredUnits,
                JavacCompiler.instance);
        return new BuildRequest<>(JavaBulkBuilder.factory, javaInput);
    }

    public static BuildRequest<?, ?, ?, ?> createJar(
            File target,
            File jarToCreate,
            File manifest,
            BuildRequest<?,?,?,?>[] requiredUnits) {

        List<Path> classfilePaths = FileCommands.listFilesRecursive(target.toPath(), classFileFilter);
        Set<File> classfileSet = new HashSet<>(convertPathToFileList(classfilePaths));

        if (classfileSet.isEmpty())
        	throw new IllegalStateException("Tried to create JAR file containing no class files");
        
        JavaJar.Input jarInput = new JavaJar.Input(
                JavaJar.Mode.CreateOrUpdate,
                jarToCreate,
                manifest,
                Collections.singletonMap(target, classfileSet),
                requiredUnits);
        return new BuildRequest<>(JavaJar.factory, jarInput);
    }

    private static List<File> convertPathToFileList(List<Path> pathList) {
        List<File> fileList = new ArrayList<>(pathList.size());
        for(Path p : pathList) {
            fileList.add(p.toFile());
        }
        return fileList;
    }
}
