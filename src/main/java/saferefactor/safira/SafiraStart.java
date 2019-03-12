package saferefactor.safira;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

import saferefactor.core.analysis.Report;
import saferefactor.core.analysis.analyzer.SafiraAnalyzer;
import saferefactor.core.analysis.analyzer.TransformationAnalyzer;
import saferefactor.core.analysis.safira.analyzer.ImpactAnalysis;
import saferefactor.core.util.Project;
import saferefactor.core.util.ast.Method;

public class SafiraStart {
	private Report analysisReport;
	private TransformationAnalyzer analyzer;
	private ImpactAnalysis ia;

	private List<Method> methodsToTest;
	private List<String> requiredClassesToTest;

	private Project source;
	private Project target;
	private String tmpFolder = ""; // TODO - Aparentemente esta variavel esta sem uso na classe SafiraAnalyzer
	
	private static String srcPath = "";
	private static String binPath = "";
	private static String libPath = "";
	private static String pathToSource = "C:\\workspace\\safira-impact-analysis\\example\\FieldUtilsClass\\original";
	private static String pathToTarget = "C:\\workspace\\safira-impact-analysis\\example\\FieldUtilsClass\\mutant02";
	private static boolean isIntraClass = false; 


	public static void main(String[] args) {

		if(args.length > 1) {
			pathToSource = args[0];
			pathToTarget = args[1];
			if(args.length > 2 && args[2].contains("-intraclass")) {
				isIntraClass = true;
			}
		}

		SafiraStart myapp = new SafiraStart();
		try {
			myapp.setupFields();
			myapp.safiraAnalyzer();
		} catch (Exception e) {
			e.printStackTrace();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private void safiraAnalyzer() throws Exception {
		double start = System.currentTimeMillis();

		analyzer = new SafiraAnalyzer(source, target, tmpFolder, isIntraClass);
		analysisReport = analyzer.analyze();
		ia = ((SafiraAnalyzer) analyzer).getIa();
		methodsToTest = analysisReport.getMethodsToTest();
//		requiredClassesToTest = analysisReport.getRequiredClassesToTest();

		System.out.println("Size: " + methodsToTest.size());
                String result = "";
                for(Method res : methodsToTest){
                    result += res.toString() + "|";
                }
		System.out.println(result);
//		System.out.println(requiredClassesToTest);

		double stop = System.currentTimeMillis();
		double total = ((stop - start) / 1000);
//		System.out.println("time to identify common methods (s): " + total);
	}

	private void setupFields() throws Throwable {
		File sourceFile = new File(pathToSource);
		File targetFile = new File(pathToTarget);
		if (!sourceFile.exists())
			throw new Throwable("Directory not found:" + sourceFile.getAbsolutePath());

		source = getProject(sourceFile);
		target = getProject(targetFile);
	}

	private static Project getProject(File sourceFile) {
		File binSource = new File(sourceFile, binPath);
		File srcSource = new File(sourceFile, srcPath);
		File libSource = new File(sourceFile, libPath);

		Project sourceProject = new Project();
		sourceProject.setProjectFolder(sourceFile.getAbsoluteFile());
		sourceProject.setSrcFolder(srcSource);
		sourceProject.setBuildFolder(binSource);
		sourceProject.setLibFolder(libSource);
		return sourceProject;
	}

}
