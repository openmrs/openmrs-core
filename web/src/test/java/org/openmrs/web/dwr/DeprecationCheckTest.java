package org.openmrs.web.dwr;

import org.junit.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.SystemPropertyUtils;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.fail;

/**
 * Unit Test class to ensure that none of the DWR services have deprecated methods.
 * For reasoning behind this see TRUNK-2517.
 *
 */
public class DeprecationCheckTest {

    private static final String OPENMRS_DWR_PACKAGE_NAME = "org.openmrs.web.dwr";

    @Test
    public void checkThatNoDeprecatedMethodExistsInServiceClassesInDWRPackage(){
        try {
            List<Class> candidates = findDWRServiceClassesWhichContainDeprecatedAnnotation(OPENMRS_DWR_PACKAGE_NAME);
            if(candidates.size()>0){
                String message = "Found classes in DWR package which contain @Deprecated annotation. " +
                        "Deprecation of DWR classes/methods is not allowed. You should just go ahead and modify/delete the method. " +
                        "Please check the following classes: ";
                for(Class c: candidates){
                    message += c.getCanonicalName()+",";
                }
                message = message.substring(0,message.length()-1);
                fail(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns a list of classes which contain the @Deprecated annotation. Does this search ONLY for
     *
     * Found the basic code here:
     * http://stackoverflow.com/questions/1456930/how-do-i-read-all-classes-from-a-java-package-in-the-classpath
     *
     * @param basePackage The package in which the classes should be searched for.
     * @return List of classes which contain the Deprecated annotation (@Deprecated)
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private List<Class> findDWRServiceClassesWhichContainDeprecatedAnnotation(String basePackage) throws IOException, ClassNotFoundException
    {
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resourcePatternResolver);

        List<Class> candidateClasses = new ArrayList<Class>();
        String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
                                   resolveBasePackage(basePackage) + "/" + "**/*.class";
        Resource[] resources = resourcePatternResolver.getResources(packageSearchPath);
        for (Resource resource : resources) {
            if (resource.isReadable()) {
                MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
                if (doesClassContainDeprecatedAnnotation(metadataReader)) {
                    candidateClasses.add(Class.forName(metadataReader.getClassMetadata().getClassName()));
                }
            }
        }

        return candidateClasses;
    }

    private String resolveBasePackage(String basePackage) {
        return ClassUtils.convertClassNameToResourcePath(SystemPropertyUtils.resolvePlaceholders(basePackage));
    }

    /**
     * For the given class, checks if its a DWR*Service class, and does not contain any @Deprecated annotation.
     * @param metadataReader
     * @return true if it finds @Deprecated annotation in the class or any of its methods.
     * @throws ClassNotFoundException
     */
    private boolean doesClassContainDeprecatedAnnotation(MetadataReader metadataReader) throws ClassNotFoundException
    {
        try {
            Class dwrClass = Class.forName(metadataReader.getClassMetadata().getClassName());

            //If Not a DWR Service, then ignore this class. If a Test class, then also ignore it
            String canonicalName = dwrClass.getCanonicalName();
            if(!canonicalName.contains("Service") || canonicalName.contains("Test"))
                return false;

            if(dwrClass.isAnnotationPresent(Deprecated.class)){
                return true;
            }

            Method[] methods = dwrClass.getDeclaredMethods();
            for(Method method: methods){
                if(method.isAnnotationPresent(Deprecated.class))
                    return true;
            }
        }
        catch(Throwable e){
        }
        return false;
    }
}
