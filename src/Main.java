import net.keraj.pkg.Shrinker;


public class Main {

	
	// change it depending on where you java executable is
	public static final String JAVA_RT = "C:\\openjdk7\\jre\\lib\\rt.jar";
	

	public static void main(String[] args) throws Exception {
		Shrinker m = new Shrinker(false); // false means we want to delete individual files
		
		// load list of all classes from rt.jar
		m.loadRT(JAVA_RT);
		
		// find all classes loaded at runtime and mark them as used
		m.readLog("output.log");
		
		// we asume that everything from under java.lang should be included
		m.markUsed("java.lang");
		
		//m.printUnusedPackages();
		
		// remove all unused classes/packages and store result in local rt.jar file
		m.process(JAVA_RT, "rt.jar", true); // true for compression
		
		System.out.println("Finished");
	}

}
