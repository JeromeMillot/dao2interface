package fr.inra.ctig.dao2interface.beans;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

public class ClassToInterface implements Serializable{
	
	protected String name;
	protected String packageName;
	protected Method[] methods;
	protected Field[] fields;
	
	public ClassToInterface() {
		super();
	}

	public ClassToInterface(String name, String packageName, Method[] methods, Field[] fields) {
		super();
		this.name = name;
		this.packageName = packageName;
		this.methods = methods;
		this.fields = fields;
	}

	public Field[] getFields() {
		return fields;
	}

	public void setFields(Field[] parameters) {
		this.fields = parameters;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public Method[] getMethods() {
		return methods;
	}

	public void setMethods(Method[] methods) {
		this.methods = methods;
	}

	@Override
	public String toString() {
		return "ClassToInterface [name=" + name + ", packageName=" + packageName + ", methods="
				+ Arrays.toString(methods) + ", fields=" + Arrays.toString(fields) + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(fields);
		result = prime * result + Arrays.hashCode(methods);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((packageName == null) ? 0 : packageName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ClassToInterface other = (ClassToInterface) obj;
		if (!Arrays.equals(fields, other.fields))
			return false;
		if (!Arrays.equals(methods, other.methods))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (packageName == null) {
			if (other.packageName != null)
				return false;
		} else if (!packageName.equals(other.packageName))
			return false;
		return true;
	}	
}
