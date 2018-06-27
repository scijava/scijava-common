package org.scijava.module.process;

import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.script.ScriptContext;

import org.scijava.log.LogService;
import org.scijava.module.Module;
import org.scijava.module.ModuleInfo;
import org.scijava.module.ModuleService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.script.ScriptImport;
import org.scijava.script.ScriptModule;

@Plugin(type = PreprocessorPlugin.class) // TODO set the priority?
public class ScriptImportPreprocessor extends AbstractPreprocessorPlugin {
	
	@Parameter
	private LogService log;
	
	@Parameter
	private ModuleService modules;

	@Override
	public void process(final Module module) {
		if (!(module instanceof ScriptModule)) return;
		// get import declarations from ScriptInfo
		@SuppressWarnings("unchecked")
		Map<String, ScriptImport> importMap = (Map<String, ScriptImport>) ((ScriptModule) module).getInfo().getProperty("imports");
		if (importMap == null) return;
		for (String moduleImport : importMap.keySet()) {
			ModuleInfo importModule = modules.getModuleByName(moduleImport);
			if (importModule == null) {
				log.warn("No module found for import: " + moduleImport);
				continue;
			}
			try {
				Module mod = modules.run(importModule, false).get();
				Map<String, Object> outputs = mod.getOutputs();
				((ScriptModule) module).getEngine().getBindings(ScriptContext.ENGINE_SCOPE).putAll(outputs);
			} catch (InterruptedException | ExecutionException exc) {
				log.warn("Error running import module: " + moduleImport, exc);
			}
		}
	}
}
