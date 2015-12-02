package build.pluto.buildmonto;

import java.io.File;

import build.pluto.builder.BuildManagers;
import build.pluto.builder.BuildRequest;

public class Main {

	public static void main(String[] args) throws Throwable {
        ServicesJavaInput javaInput = new ServicesJavaInput(
                new File("services-java"),
                new File("target/services-java"));
        BuildManagers.build(new BuildRequest<>(ServicesJavaBuilder.factory, javaInput));
        
      ServicesJavascriptInput jsInput = new ServicesJavascriptInput(
    		  new File("services-javascript"),
    		  new File ("target/services-javascript"));
      BuildManagers.build(new BuildRequest<>(ServicesJavascriptBuilder.factory, jsInput));
	}

}
