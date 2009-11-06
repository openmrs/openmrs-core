package org.openmrs.module;


import org.junit.Assert;
import org.junit.Test;
import org.openmrs.test.Verifies;

public class ModuleUtilTest {
	
	/**
     * @see {@link ModuleUtil#getPathForResource(Module,String)}
     */
    @Test
    @Verifies(value = "should handle ui springmvc css ui dot css example", method = "getPathForResource(Module,String)")
    public void getPathForResource_shouldHandleUiSpringmvcCssUiDotCssExample() throws Exception {
    	Module module = new Module("Unit test");
    	module.setModuleId("ui.springmvc");
    	String path = "/ui/springmvc/css/ui.css";
    	Assert.assertEquals("/css/ui.css", ModuleUtil.getPathForResource(module, path));
    }

	/**
     * @see {@link ModuleUtil#getModuleForPath(String)}
     */
    @Test
    @Verifies(value = "should handle ui springmvc css ui dot css when ui dot springmvc module is running", method = "getModuleForPath(String)")
    public void getModuleForPath_shouldHandleUiSpringmvcCssUiDotCssWhenUiDotSpringmvcModuleIsRunning() throws Exception {
    	ModuleFactory.getStartedModulesMap().clear();
	    Module module = new Module("For Unit Test");
	    module.setModuleId("ui.springmvc");
	    ModuleFactory.getStartedModulesMap().put(module.getModuleId(), module);
	    
	    String path = "/ui/springmvc/css/ui.css";
	    Assert.assertEquals(module, ModuleUtil.getModuleForPath(path));
    }

	/**
     * @see {@link ModuleUtil#getModuleForPath(String)}
     */
    @Test
    @Verifies(value = "should handle ui springmvc css ui dot css when ui module is running", method = "getModuleForPath(String)")
    public void getModuleForPath_shouldHandleUiSpringmvcCssUiDotCssWhenUiModuleIsRunning() throws Exception {
    	ModuleFactory.getStartedModulesMap().clear();
	    Module module = new Module("For Unit Test");
	    module.setModuleId("ui");
	    ModuleFactory.getStartedModulesMap().put(module.getModuleId(), module);
	    
	    String path = "/ui/springmvc/css/ui.css";
	    Assert.assertEquals(module, ModuleUtil.getModuleForPath(path));
    }

	/**
     * @see {@link ModuleUtil#getModuleForPath(String)}
     */
    @Test
    @Verifies(value = "should return null for ui springmvc css ui dot css when no relevant module is running", method = "getModuleForPath(String)")
    public void getModuleForPath_shouldReturnNullForUiSpringmvcCssUiDotCssWhenNoRelevantModuleIsRunning() throws Exception {
    	ModuleFactory.getStartedModulesMap().clear();
	    String path = "/ui/springmvc/css/ui.css";
	    Assert.assertNull(ModuleUtil.getModuleForPath(path));
    }

}