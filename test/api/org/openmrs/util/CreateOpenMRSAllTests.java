package org.openmrs.util;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class CreateOpenMRSAllTests {
	
	public static void main(String[] args) throws IOException {
		File root = new File("test" + File.separator + "api" + File.separator +
			"org" + File.separator + "openmrs");
		System.out.println(root.getAbsolutePath());
		String basePrefix = "org.openmrs.";
		createTests(root, basePrefix, "");
	}
	
	private static String createTests(File root, String curPref, String curTestName) throws IOException {
		ArrayList<String> imports = new ArrayList<String>();
		ArrayList<String> classes = new ArrayList<String>();
		for(File f : root.listFiles()) {
			if(f.isDirectory()) {
				if(f.getName().startsWith(".") || f.getName().equals("test"))
					continue;
				String nextPref = curPref + f.getName() + ".";
				String nextTestAdd = f.getName().toLowerCase();
				String nextTestName = curTestName + Character.toUpperCase(nextTestAdd.charAt(0)) + 
					nextTestAdd.substring(1);
				String suiteName = createTests(f, nextPref, nextTestName);
				if(suiteName == null)
					continue;
				classes.add(suiteName);
				imports.add(nextPref + suiteName);
			} else if(f.getName().endsWith("Test.java")){
				String className = f.getName().substring(0, f.getName().length() - 5);
				classes.add(className);
			}
		}
		if(classes.isEmpty())
			return null;
		
		String testName = "All" + curTestName + "Tests";
		File testsFile = new File(root.getAbsolutePath() + File.separator + testName + ".java");
		FileWriter writer = new FileWriter(testsFile);
		writer.write("package " + curPref.substring(0, curPref.length() - 1) + ";\n\n");
		for(String imp : imports)
			writer.write("import " + imp + ";\n");
		writer.write("import org.junit.runner.RunWith;\n");
		writer.write("import org.junit.runners.Suite;\n");
		writer.write("import org.junit.runners.Suite.SuiteClasses;\n");
		writer.write("\n@RunWith(Suite.class)\n");
		writer.write("@SuiteClasses({\n");
		for(int i = 0; i < classes.size() - 1; i++)
			writer.write("\t" + classes.get(i) + ".class,\n");
		writer.write("\t" + classes.get(classes.size() - 1) + ".class\n})");
		writer.write("\npublic class " + testName + "{}");
		writer.close();
		
		return testName;
	}
}
