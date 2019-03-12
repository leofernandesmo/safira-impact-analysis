package saferefactor.core.analysis.analyzer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.ProjectHelper;

import saferefactor.core.analysis.Report;
import saferefactor.core.util.Constants;
import saferefactor.core.util.Project;
import saferefactor.core.util.ast.Method;
import saferefactor.core.util.ast.Clazz;
import saferefactor.core.util.ast.ConstructorImp;
import saferefactor.core.util.ast.MethodImp;
import saferefactor.core.analysis.naive.NaiveReport;
import saferefactor.core.analysis.safira.analyzer.ImpactAnalysis;

public class SafiraAnalyzer implements TransformationAnalyzer {

	private List<Clazz> sourceClasses;
	private List<Clazz> targetClasses;
	private final Project source;
	private final Project target;
	private final String tmpDir;
	private String bin = "";
	private ImpactAnalysis ia;
	private boolean isIntraClass = false;

	public SafiraAnalyzer(Project source, Project target, String tmpDir, boolean isIntraClass) {
		this.source = source;
		this.target = target;
		this.tmpDir = tmpDir;
		this.bin = source.getBin();
		this.isIntraClass = isIntraClass;
	}

	public Report analyze() throws Exception {
		
		Report result = new NaiveReport();
		
//	TODO - Precisa implementar a busca pelas classes de dependência. 	
//		sourceClasses = getClasses(source);
//		targetClasses = getClasses(target);
		
		
		ia = new ImpactAnalysis(source.getProjectFolder().getAbsolutePath(), 
				target.getProjectFolder().getAbsolutePath(), bin, isIntraClass);
		List<Method> methods_to_test = ia.getMethods_to_test();
		result.setMethodsToTest(methods_to_test);
		
		//LEO: Add constructors of Types that are in the parameters of the methods.
		//TODO - Precisa implementar no Safira. Estava implementado no SareRefactor, mas ele usava um esquema com o Ant para compilar e configurar o classpath.
//		addDependencyMethods(result);
//		addRequiredClassesToTest(result);
		
		return result;		
	}
	
	/**
	 * LEO: Adicionado para informar as classes que precisarão ser testadas.
	 * @param result
	 */
	private void addRequiredClassesToTest(Report result) {
		List<String> classesToTest = new ArrayList<String>();
		for (Clazz clazz : sourceClasses) {
			classesToTest.add(clazz.getFullName());
		}
		result.setRequiredClassesToTest(classesToTest);
	}

	
	/* Add constructors from the Types in parameters */
	/**
	 * LEO: Adiciona metodos das classes dependentes (normalmente construtores).
	 * @param result
	 */
	private void addDependencyMethods(Report result) {
		List<Method> methodsToTest = result.getMethodsToTest();
		ArrayList<String> classes = new ArrayList<String>();
		for (Method method : methodsToTest) {			
			for (String parameter : method.getParameterList()) {
				if (parameter.equals("int") || parameter.equals("byte") || parameter.equals("short")
						|| parameter.equals("long") || parameter.equals("float") || parameter.equals("double")
						|| parameter.equals("boolean") || parameter.equals("char")) {
				} else {
					classes.add(parameter);
				}
			}
		}
		try {
			List<Clazz> classesInfo = ProjectAnalyzer.getClassesInfo(classes);
			for (Clazz clazz : classesInfo) {
				for(ConstructorImp c : clazz.getConstructors()) {
					result.addAMethodToTest(c);
				}
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.out.println("We could not recover dependency classes.");
		}
	}
	


	

	public ImpactAnalysis getIa() {
		return ia;
	}

	public void setIa(ImpactAnalysis ia) {
		this.ia = ia;
	}

	

	
}
