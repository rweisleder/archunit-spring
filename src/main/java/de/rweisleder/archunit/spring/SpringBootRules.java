/*
 * #%L
 * ArchUnit Spring Integration
 * %%
 * Copyright (C) 2023 Roland Weisleder
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package de.rweisleder.archunit.spring;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.conditions.ArchConditions;

import java.util.Collection;
import java.util.List;

import static com.tngtech.archunit.lang.conditions.ArchConditions.resideInAnyPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static de.rweisleder.archunit.spring.MergedAnnotationPredicates.springAnnotatedWith;
import static java.util.stream.Collectors.toList;

/**
 * Collection of {@link ArchRule rules} that can be used to check the structure of Spring Boot applications.
 *
 * @author Roland Weisleder
 */
public class SpringBootRules {

    /**
     * A rule that checks that all classes are located in the same package or a sub-package of the application class.
     * The application class is the one annotated with {@code @SpringBootApplication} or {@code @SpringBootConfiguration}
     * and must be within the given classes.
     *
     * @see #beInApplicationPackage()
     */
    public static final ArchRule AllTypesInApplicationPackage = classes()
            .should(beInApplicationPackage())
            .as("all types of a Spring Boot application should be located in the same package or a sub-package of the application class");

    /**
     * A condition that checks that the given classes are located in the same package or a sub-package of the application class.
     * The application class is the one annotated with {@code @SpringBootApplication} or {@code @SpringBootConfiguration}
     * and must be within the given classes.
     * <p>
     * In case the application class is not within the given classes, consider using {@link ArchConditions#resideInAnyPackage(String...)} directly.
     *
     * @see #AllTypesInApplicationPackage
     */
    public static ArchCondition<JavaClass> beInApplicationPackage() {
        return new ArchCondition<JavaClass>("be located in the same package or a sub-package of the application class") {

            private ArchCondition<JavaClass> inApplicationPackageCondition;

            @Override
            public void init(Collection<JavaClass> javaClasses) {
                List<JavaClass> springBootApplicationClasses = javaClasses.stream()
                        .filter(springAnnotatedWith("org.springframework.boot.SpringBootConfiguration"))
                        .collect(toList());

                if (springBootApplicationClasses.isEmpty()) {
                    throw new AssertionError("Could not locate a class annotated with @SpringBootApplication or @SpringBootConfiguration");
                }

                String[] applicationPackageIdentifiers = springBootApplicationClasses.stream()
                        .map(javaClass -> javaClass.getPackageName() + "..")
                        .distinct().toArray(String[]::new);
                inApplicationPackageCondition = resideInAnyPackage(applicationPackageIdentifiers);
            }

            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                inApplicationPackageCondition.check(javaClass, events);
            }
        };
    }
}
