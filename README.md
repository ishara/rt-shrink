RT Shrink
=========

A small script/program to analyse and remove unused classes from bundled JRE's rt.jar (and more, if you need)

How it works?
-------------

First you need to run your application with <code>-verbose:class</code> jvm parameter and redirect output to some file.

For example:  
<code>openjdk/jre/bin/java -server -verbose:class -cp ./* MyMainClass > output.log</code>

JVM will now output all the classes it loads at runtime int stdout, which will be redirected to output.log

Then we can use this log file to gather list of all classes that were loaded at runtime. After that, we can remove all classes from rt.jar that were never used at runtime.

example use:

<code>
		// false means we want to delete individual files
		Shrinker m = new Shrinker(false);
		
		// load list of all classes from rt.jar
		m.loadRT("/openjdk/jre/lib/rt.jar");
		
		// find all classes loaded at runtime and mark them as used
		m.readLog("output.log");
		
		// we asume that everything from under java.lang should be included and never removed
		m.markUsed("java.lang");
		
		//m.printUnusedPackages();
		
		// remove all unused classes/packages and store result in local rt.jar file
		m.process("/openjdk/jre/lib/rt.jar", "rt.jar", true); // true for compression
		
		System.out.println("Finished");
</code>


Dangers
-------

It is obvious that this program can only detect which clases were not used during one run of our application.
There is a high risk that some classes were not loaded, even though our app (or jvm) could potentially use them in some rare cases.

That would result in runtime errors because of those missing classes.

This is very important to "try everything" with our program, to ensure almost all cases are covered.
Do a couple of runs, on different machines, maybe even crash it once (with uncaught exception).
But even then we are not sure if we covered all of them. Some packages, like "java.lang" should be always preserved for that reason.

I will try to gather list of packages that are absolutely required.