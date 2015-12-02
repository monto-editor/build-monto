package build.pluto.buildmonto;

import java.io.File;

import build.pluto.builder.BuildManagers;
import build.pluto.builder.BuildRequest;

public class Main {

	public static void main(String[] args) throws Throwable {
        ServicesJavaInput javaInput = new ServicesJavaInput(
      		  new File ("target/services-java"),
      		  new File ("target/services-java.jar"),
      		  null);
        BuildManagers.build(new BuildRequest<>(ServicesJavaBuilder.factory, javaInput));
        
      ServicesJavascriptInput jsInput = new ServicesJavascriptInput(
    		  new File ("target/services-javascript"),
    		  new File ("target/services-javascript.jar"),
    		  null);
      BuildManagers.build(new BuildRequest<>(ServicesJavascriptBuilder.factory, jsInput));
	}

}
