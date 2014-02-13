package net.keraj.pkg;
import java.io.*;
import java.util.*;
import java.util.zip.*;


public class Packages {

	public void load(File f) throws IOException {
		ZipFile rt = new ZipFile(f);
		
		Enumeration<? extends ZipEntry> entries = rt.entries();
		
		while(entries.hasMoreElements()) {
			ZipEntry ent = entries.nextElement();
			
			String path = ent.getName();
			
			if(path.endsWith(".class")) {
				Klass k = new Klass(path);
				
				Package p = getPackage(k.pkg);
				
				k.pack = p;
				p.classes.add(k);
				klasses.put(k.full, k);
			} else if(!ent.isDirectory()) {
				String pkg = path.substring(0, path.lastIndexOf("/")).replaceAll("/", "\\.");
				Package p = getPackage(pkg);
				p.markUsed();
			}
		}
		
		rt.close();
	}
	
	
	public final Map<String, Package> packages = new HashMap<>();
	public final Map<String, Klass> klasses = new HashMap<>();
	
	public Packages() {
		packages.put("", new Package(""));
		
	}
	
	Package getPackage(String name) {
		Package p = packages.get(name);
		if(p == null) {
			p = new Package(name);
			p.parent = getPackage(p.getParentName());
			p.parent.children.add(p);
			packages.put(name, p);
		}
		return p;
	}
	

	public static class Package {
		public final String pkg;
		public Package parent;
		public final List<Package> children = new ArrayList<>();
		public final List<Klass> classes = new ArrayList<>();
		public boolean used = false;
		
		private String getParentName() {
			int idx = pkg.lastIndexOf(".");
			if(idx==-1)
				return "";
			return pkg.substring(0, idx);
		}
		
		public Package(String pkg) {
			this.pkg = pkg;
		}
		
		public void markUsed() {
			if(used) return;
			used  = true;
			if(parent!=null)
				parent.markUsed();
		}
		
		public void markUsedChildren() {
			used = true;
			for(Package p : children) {
				p.markUsedChildren();
			}
			for(Klass k : classes) {
				k.used = true;
			}
		}
	}
	public static class Klass {
		public Package pack;
		public final String full;
		public final String pkg;
		
		public boolean used = false;
		
		public void markUsed() {
			if(used) return;
			used = true;
			pack.markUsed();
		}
		
		public Klass(String path) {
			full = path.substring(0, path.length()-6).replaceAll("\\\\|/", "\\.");
			pkg = path.substring(0, path.lastIndexOf("/")).replaceAll("/", "\\.");
			
			//System.out.println(full + " / " + pkg);
        }
	}
}
