package build.pluto.buildmonto.util;

import org.sugarj.common.FileCommands;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class ManifestFileGenerator {
    private final File root;
    private final File path;
    private final String version;
    private final String entryPoint;
    private final List<File> classpath;
    private final boolean sealedPackages;

    public ManifestFileGenerator(File root, File path, String version, String entryPoint, List<File> classpath, boolean sealedPackages) {
        this.root = root;
        this.path = path;
        this.version = version != null ? version : "1.0";
        this.entryPoint = entryPoint;
        this.classpath = classpath;
        this.sealedPackages = sealedPackages;
    }

    public void generate() throws IOException {
        StringBuffer sb = new StringBuffer();
        sb.append("Manifest-Version: ").append(version);
        sb.append("\n");
        if (entryPoint != null) {
            sb.append("Main-Class: ").append(entryPoint);
            sb.append("\n");
        }
        if (classpath != null && !classpath.isEmpty()) {
            sb.append("Class-Path: ");
            for (File f: classpath) {
                if(root != null) {
                    Path relPath = FileCommands.getRelativePath(root, f);
                    sb.append(relPath.toString());
                } else {
                    sb.append("file://" + f.getAbsolutePath().toString());
                }
                sb.append(" \n ");
            }
            sb.append("\n");
        }
        if(sealedPackages) {
            sb.append("Sealed: ").append(sealedPackages);
            sb.append("\n");
        }
        sb.append("\n");
        if (!path.exists()) {
            FileCommands.createFile(path.getAbsoluteFile());
        }
        FileCommands.writeToFile(path.getAbsoluteFile(), sb.toString());
    }
}
