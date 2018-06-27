package org.scijava.script;

public class ScriptImport {
	private String scope;
	
	public ScriptImport(String scope) {
		this.scope = scope;
	}
	
	public String getScope() {
		return scope;
	}
	
	public boolean hasScope() {
		return !(scope == null || scope.isEmpty());
	}
}
