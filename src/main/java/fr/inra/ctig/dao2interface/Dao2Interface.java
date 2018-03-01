package fr.inra.ctig.dao2interface;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import fr.inra.ctig.dao2interface.beans.ClassToInterface;

public class Dao2Interface {

	private static String projectName = "projetsCoeur";
	private static String projetUrl = "/home/jmillot/devJava/" + projectName + "/";
	public static List<File> listPackages;
	public static List<String> excludedFolderNames = Arrays.asList(".m2", "src");
	public static List<String> excludedTypeNames = Arrays.asList("", "?", "T", "boolean", "String", "void", "int", "double", "float", "long", "char", "byte", "java.lang.String", "java.lang.Class<?>", "java.lang.Class");

	public static void main(String[] args) throws ClassNotFoundException, IOException {
		Dao2Interface dao2Interface = new Dao2Interface();
		File packageFolder = new File(projetUrl);
		
		dao2Interface.setClassPath(dao2Interface.getClassPath());
		System.out.println("PackageFolder url : " + packageFolder.getPath());
		listPackages = dao2Interface.searchDAOPackages(packageFolder);
		System.out.println("DAO Entries : " + listPackages.size());		
		List<Class<?>> classToConvert = dao2Interface.readPackages(listPackages);	
		dao2Interface.createInterfaceFromDAOClasses(classToConvert);
	}

	public List<File> searchDAOPackages(File packageFolder) {
		List<File> daoPackages = new ArrayList<File>();
		
		for(File folder : packageFolder.listFiles()) {
			if (folder.isDirectory() && !excludedFolderNames.contains(folder.getName())) {
				if (folder.getName().toLowerCase().contains("target")) {
					System.out.println("Folder: " + folder.getAbsolutePath());
					daoPackages.add(folder);
				}
				daoPackages.addAll(searchDAOPackages(folder));
			}
		}
		return daoPackages;
	}

	public List<Class<?>> readPackages(List<File> packages) throws IOException, ClassNotFoundException {
		Dao2Interface dao2Interface = new Dao2Interface();
		
		for(File pack : packages) {
			JarFile jarFile;
			for(File file : pack.listFiles()) {				
				if(file.getName().toLowerCase().contains(".jar")) {
					System.out.println("JAR file found : " + file.getName());
					jarFile = new JarFile(file.getAbsolutePath());
					URL[] urls = { new URL("jar:file:" + file.getAbsolutePath() +"!/") };
					return dao2Interface.convertJarFileToClasses(urls, jarFile);
				}
			}
		}
		return null;
	}

	public List<String> getClassPath() {
		ClassLoader cl = ClassLoader.getSystemClassLoader();
		URL[] urls = ((URLClassLoader)cl).getURLs();
		List<String> classPath = new ArrayList<String>();
		
		for(URL url: urls){
			classPath.add(url.getFile());			;
		}
		return classPath;
	}

	public void setClassPath(List<String> classPath) throws IOException {
		boolean isAlreadyPresent = false;
		File f = new File(projetUrl + "target/classes/");
		
		if(!classPath.contains(f.getAbsolutePath() + "/")) {
			try {				
				URL u = f.toURI().toURL();
				System.out.println("Path créé : " + f.getPath());
				URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
				Class urlClass = URLClassLoader.class;
				Method method = urlClass.getDeclaredMethod("addURL", new Class[]{URL.class});
				method.setAccessible(true);
				method.invoke(urlClassLoader, new Object[]{u});		
			} catch (NoSuchMethodException e) {			
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (MalformedURLException e) {			
				e.printStackTrace();
			}
		} else {
			System.out.println("Path du projet déjà présent dans le CLASSPATH, il ne sera pas ajouter.");
		}			

	}

	public List<Class<?>> convertJarFileToClasses(URL[] url, JarFile jar) throws ClassNotFoundException, IOException {
		List<Class<?>> classList = new ArrayList<Class<?>>();		
		URLClassLoader loader = new URLClassLoader(url);
		Enumeration<JarEntry> e = jar.entries();		

		while (e.hasMoreElements()) {
			JarEntry je = e.nextElement();		

			if(je.isDirectory() || !je.getName().endsWith(".class") || !je.getName().contains("DAO")){
				continue;
			}
			// -6 because of .class
			String className = je.getName().substring(0,je.getName().length()-6);
			className = className.replace('/', '.');
			Class<?> c = loader.loadClass(className);
			System.out.println("Class found: " + c.getName());
			classList.add(c);
		}
		System.out.println("Class list size : " + classList.size());
		jar.close();
		loader.close();
		return classList;
	}

	public void createInterfaceFromDAOClasses(List<Class<?>> classes) throws FileNotFoundException {
		File interfaceFolder = new File(projetUrl + "src/main/java/fr/inra/ctig/" + projectName + "/idao/");		
		ClassToInterface toInterface;
		BufferedOutputStream interfaceWriter = null;		
		interfaceFolder.mkdir();
		if(interfaceFolder.exists() && interfaceFolder.isDirectory()) {
			System.out.println("Created folder : " + interfaceFolder.getPath());			
		}

		for(Class<?> c : classes) {
			byte[] buffer;
			String name = getClassName(c);
			String packageName = getPackageName(c);
			Method[] methods = c.getMethods();
			Field[] fields = c.getFields();
			toInterface = new ClassToInterface(name, packageName, methods, fields);			
			File IDao = new File(interfaceFolder.getAbsolutePath() + "/I" + toInterface.getName() + ".java");			
			try {
				interfaceWriter = new BufferedOutputStream(new FileOutputStream(IDao));
				String packaging = "package " + toInterface.getPackageName().substring(0, toInterface.getPackageName().length() - 3) + "idao;\n\n";
				HashSet<String> imports = new HashSet<String>();
				buffer = packaging.getBytes(); 
				interfaceWriter.write(buffer);
				for(Method method : methods) {
					String type = method.getGenericReturnType().toString();
					String imp = "import ";					
					if(type.contains("class")) {
						int startIndex = 0;
						startIndex = type.indexOf(" ") + 1;
						type = type.substring(startIndex);
					} else if (type.contains("interface")) {
						int startIndex = 0;
						startIndex = type.indexOf(" ") + 1;
						type = type.substring(startIndex);
					} else if (type.contains("<")) {
						int arrowIndex = 0;						
						arrowIndex = type.indexOf("<");
						String MType = type.substring(0, arrowIndex);
						String genType = type.substring(arrowIndex + 1, type.length() - 1);						
						String imp2 = "import " + genType + ";\n";					    
						if (!imports.contains(imp2) && !excludedTypeNames.contains(genType)) {						
							imports.add(imp2);
							buffer = imp2.getBytes(); 
							interfaceWriter.write(buffer);
						}
						type = MType;
					}
					imp += type + ";\n";					
					if (!imports.contains(imp) && !excludedTypeNames.contains(type)) {						
						imports.add(imp);
						buffer = imp.getBytes(); 
						interfaceWriter.write(buffer);
					}
				}
				String ret = "\n";
				buffer = ret.getBytes(); 
				interfaceWriter.write(buffer);				
				String interfaceSignature = "public interface I" + toInterface.getName() + " {\n\n";
				buffer = interfaceSignature.getBytes();
				interfaceWriter.write(buffer);
				for(Method method : methods) {
					String methodSignature;
					String parameters = "";
					for(Parameter param : method.getParameters()) {
						if(parameters.length() > 0) {
							parameters += ", ";
						}						
						parameters += getNameOnly(param.getParameterizedType().getTypeName()) + " " + param.getName();
					}
					if (method.getName() != "close" && method.getName() != "wait" && method.getName() != "toString" && method.getName() != "hashCode" 
							&& method.getName() != "equals" && method.getName() != "getClass" && method.getName() != "notify" && method.getName() != "notifyAll" ) {
						methodSignature = "public " + getNameOnly(method.getGenericReturnType().toString()) + " " + method.getName() + " (" + parameters + ");\n\n";
						buffer = methodSignature.getBytes();
						interfaceWriter.write(buffer);
					}

				}
				String closingBrace = "}";
				buffer = closingBrace.getBytes();
				interfaceWriter.write(buffer);
				System.out.println("Interface : " + IDao.toString());
			} catch (IOException e) {				
				e.printStackTrace();
			} finally {
				try {
					interfaceWriter.flush();
				} catch (IOException e) {					
					e.printStackTrace();
				}
			}			
		}
	}

	public static String getPackageName(Class<?> c) {
		String fullyQualifiedName = c.getName();
		int lastDot = fullyQualifiedName.lastIndexOf ('.');
		if (lastDot==-1){ return ""; }
		return fullyQualifiedName.substring (0, lastDot);
	}

	public static String getClassName(Class<?> c) {
		String FQClassName = c.getName();
		int firstChar;
		firstChar = FQClassName.lastIndexOf ('.') + 1;
		if ( firstChar > 0 ) {
			FQClassName = FQClassName.substring ( firstChar );
		}
		return FQClassName;
	}

	public static String getNameOnly(String str) {
		String nameOnly = str;
		int firstChar;
		if (!str.contains("<")) {
			firstChar = nameOnly.lastIndexOf ('.') + 1;
			if ( firstChar > 0 ) {
				nameOnly = nameOnly.substring ( firstChar );
			}
		} else {
			int arrowIndex = 0;
			arrowIndex = nameOnly.indexOf("<");
			String type = nameOnly.substring(0, arrowIndex);
			String genType = nameOnly.substring(arrowIndex);
			firstChar = type.lastIndexOf ('.') + 1;
			if ( firstChar > 0 ) {
				type = type.substring ( firstChar );
			}
			firstChar = genType.lastIndexOf ('.') + 1;
			if ( firstChar > 0 ) {
				genType = "<" + genType.substring ( firstChar );
			}
			nameOnly = type + genType;
		}	    
		return nameOnly;
	}


}
