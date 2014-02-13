package net.keraj.pkg;
import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.zip.*;

import net.keraj.pkg.Packages.Package;
import net.keraj.pkg.Packages.*;

import sun.misc.Unsafe;

public class Shrinker {

	private boolean packages;
	Packages pkgs = new Packages();
	
	/** @param package_only If set to true, then only unused packages will be removed, otherwise individual unused files are removed.
	 * A package is "unused" only when all of its files and subpackages are unused. */
	public Shrinker(boolean packagesOnly) throws IOException {
		this.packages = packagesOnly;
	}
	
	public void loadRT(String path) throws IOException {
		pkgs.load(new File(path));
		System.out.println("loaded " + pkgs.klasses.size() + " classes from " + path);
	}
	
	/** Reads program output log and searches for lines starting with "[Loaded " */
	public void readLog(String logpath) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(logpath)));
		int marked = 0;
		try {
			String line;
	        while((line = br.readLine()) != null) {

	        	if(line.startsWith("[Loaded ")) {
	        		int idx = line.indexOf(' ', 8);
	        		String classname = line.substring(8, idx);

		    		Klass k = pkgs.klasses.get(classname);
		    		if(k!=null) {
		        		k.markUsed();
		        		marked++;
		    		}
	        	}
	        }
		} finally {
			br.close();
		}
		System.out.println("Marked " + marked + " classes from " + logpath);
	}
	
	/**
	 * copies jar contents, excluding unused classes or packages
	 */
	public void process(String rt, String file, boolean compress) throws Exception {
	    ZipInputStream zin = new ZipInputStream(new FileInputStream(rt), Charset.forName("UTF8"));
	    ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(file), Charset.forName("UTF8"));
		zout.setLevel(9);
		zout.setMethod(compress ? ZipOutputStream.DEFLATED : ZipOutputStream.STORED);
	  
	    int removed = 0;
	    for(;;) {
	    	ZipEntry ent = zin.getNextEntry();
	    	if(ent==null)
	    		break;
	    	if(ent.isDirectory())
	    		continue;
	    	String path = ent.getName();
	    	
	    	if(packages) {
		    	String pkg = path.substring(0, path.lastIndexOf("/")).replaceAll("/", "\\.");
		    	Package p = pkgs.getPackage(pkg);
		    	if(!p.used) {
		    		removed++;
		    		continue;
		    	}
	    	}
	    	else {
		    	if(path.endsWith(".class")) {
		    		Klass tmp = new Klass(path);
		    		Klass k = pkgs.klasses.get(tmp.full);
		    		if(k==null || !k.used) {
			    		removed++;
		    			continue;
		    		}
		    	}
	    	}
	    	
	    	ZipEntry t = new ZipEntry(path);
	    	t.setSize(ent.getSize());
	    	t.setCrc(ent.getCrc());
	    	
	    	zout.putNextEntry(t);
	    	
	    	pipe(zin,zout);
	    	
	    	zout.closeEntry();
	    }
	    
	    zout.flush();
	    zout.finish();
	    zout.flush();
	    zout.close();
	    
	    System.out.println("removed " + removed + " files");
    }
	
	/** marks package and all it's subpackages/files as used
	 * should be used after loading logs and before processing rt.jar */
	public void markUsed(String name) {
		Package p = pkgs.getPackage(name);
		if(p!=null) {
			p.markUsedChildren();
			System.out.println("required: " + name);
		}
		else
			System.err.println("Warning: no package named " + name);
	}
	
	private void pipe(InputStream in, OutputStream out) throws IOException {
		byte[] buf = new byte[2048];
		for(;;) {
			int r = in.read(buf);
			if(r==-1)
				break;
			out.write(buf, 0, r);
		}
	}
	
	/** Prints completely unused packages, whose not even a single class of them was used during runtime. */
	public void printUnusedPackages() {
		printUnused(pkgs.packages.get(""));
	}
	
	private void printUnused(Package p) {
		if(p.used) {
			for(Package c : p.children) {
				printUnused(c);
			}
		} else {
			System.out.println("Unused package: " + p.pkg);
		}
	}
	
	
}
