package fr.inra.ctig.dao2interface.beans;

import java.util.ArrayList;

public class PackageElement {
	
	private String name;
	private ArrayList<Class> classList = new ArrayList<Class>();
	private String returnType;
	private ArrayList<String> parameters = new ArrayList<String>();
	
	public PackageElement() {
		super();		
	}
		

	public PackageElement(ArrayList<Class> classList, String returnType, ArrayList<String> parameters, String name) {
		super();
		this.name = name;
		this.classList = classList;
		this.returnType = returnType;
		this.parameters = parameters;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<Class> getClassList() {
		return classList;
	}

	public void setClassList(ArrayList<Class> classList) {
		this.classList = classList;
	}

	public String getReturnType() {
		return returnType;
	}

	public void setReturnType(String returnType) {
		this.returnType = returnType;
	}

	public ArrayList<String> getParameters() {
		return parameters;
	}

	public void setParameters(ArrayList<String> parameters) {
		this.parameters = parameters;
	}	

}
