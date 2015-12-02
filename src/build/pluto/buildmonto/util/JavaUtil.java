package build.pluto.buildmonto.util;

import build.pluto.builder.BuildRequest;
import build.pluto.buildjava.JavaBuilder;
import build.pluto.buildjava.JavaInput;
import build.pluto.buildjava.JavaJar;
import build.pluto.buildjava.compiler.JavacCompiler;
import build.pluto.buildjava.util.FileExtensionFilter;
import org.sugarj.common.FileCommands;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Path;
import java.util.*;

public class JavaUtil {
    public static final FileFilter classFileFilter = new FileExtensionFilter("class");
    public static final FileFilter javaFileFilter = new FileExtensionFilter("java");
    public static final FileFilter jarFileFilter = new FileExtensionFilter("jar");

    public static BuildRequest<?, ?, ?, ?> compileJava(
            File src,
            File target,
            List<File> jarFiles,
            List<BuildRequest<?,?,?,?>> requiredUnits) {

        List<Path> javaPaths =
            FileCommands.listFilesRecursive(src.toPath(), javaFileFilter);
        List<File> javaFiles = convertPathToFileList(javaPaths);
        List<File> sourcePath =
            Arrays.asList(new File(src, "src"));
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
        return JavaBuilder.request(javaInput);
    }

    public static BuildRequest<?, ?, ?, ?> createJar(
            File target,
            File jarToCreate,
            File manifest,
            BuildRequest<?,?,?,?>[] requiredUnits) {

        List<Path> classfilePaths =
            FileCommands.listFilesRecursive(target.toPath(), classFileFilter);
        Map<File, Set<File>> classfiles = new HashMap<>();
        classfiles.put(
                target,
                new HashSet<>(convertPathToFileList(classfilePaths)));

        JavaJar.Input jarInput = new JavaJar.Input(
                JavaJar.Mode.CreateOrUpdate,
                jarToCreate,
                manifest,
                classfiles,
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
