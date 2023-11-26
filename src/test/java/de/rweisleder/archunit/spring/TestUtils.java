package de.rweisleder.archunit.spring;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;

public class TestUtils {

    public static JavaClasses importClasses(Class<?>... classes) {
        return new ClassFileImporter().importClasses(classes);
    }

    public static JavaClass importClass(Class<?> classToImport) {
        return new ClassFileImporter().importClass(classToImport);
    }
}
