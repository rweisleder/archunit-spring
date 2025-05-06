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
package de.rweisleder.archunit.spring.boot;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.properties.CanBeAnnotated;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.conditions.ArchConditions;

import java.util.Collection;
import java.util.List;

import static com.tngtech.archunit.lang.ConditionEvent.createMessage;
import static com.tngtech.archunit.lang.SimpleConditionEvent.satisfied;
import static com.tngtech.archunit.lang.SimpleConditionEvent.violated;
import static com.tngtech.archunit.lang.conditions.ArchConditions.resideInAnyPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static de.rweisleder.archunit.spring.SpringAnnotationPredicates.springAnnotatedWith;
import static java.util.stream.Collectors.toList;

/**
 * Collection of {@link ArchRule rules} that can be used to check the structure of Spring Boot applications.
 *
 * @author Roland Weisleder
 */
public final class SpringBootRules {

    private static final DescribedPredicate<CanBeAnnotated> annotatedWithSpringBootApplication = springAnnotatedWith("org.springframework.boot.autoconfigure.SpringBootApplication");
    private static final DescribedPredicate<CanBeAnnotated> annotatedWithSpringBootConfiguration = springAnnotatedWith("org.springframework.boot.SpringBootConfiguration");

    private SpringBootRules() {
    }

    /**
     * A rule that checks that all classes are located in the same package or a sub-package of the application class.
     * The application class is the one annotated with {@code @SpringBootApplication} or {@code @SpringBootConfiguration}
     * and must be within the given classes.
     *
     * @see #beInApplicationPackage()
     */
    public static final ArchRule AllTypesInApplicationPackage = classes()
            .should(beInApplicationPackage())
            .as("all types of a Spring Boot application should be located in the same package or a sub-package of the application class")
            .allowEmptyShould(true);

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
                        .filter(annotatedWithSpringBootConfiguration)
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

    /**
     * A rule that checks that at most one class within the given classes is annotated with
     * {@code @SpringBootApplication} or {@code @SpringBootConfiguration}.
     * <p>
     * A Spring Boot application should only ever include a single {@code @SpringBootConfiguration}.
     * In most idiomatic Spring Boot applications, this configuration is inherited from {@code @SpringBootApplication}.
     * Having multiple such classes can lead to unexpected behavior and conflicts during application startup.
     *
     * @see #haveOnlyOneSpringBootConfiguration()
     */
    public static final ArchRule ApplicationHasOnlyOneSpringBootConfiguration = classes()
            .should(haveOnlyOneSpringBootConfiguration())
            .as("application should have only one class annotated with @SpringBootApplication or @SpringBootConfiguration")
            .allowEmptyShould(true);

    /**
     * A condition that checks that at most one class within the given classes is annotated with
     * {@code @SpringBootApplication} or {@code @SpringBootConfiguration}.
     * <p>
     * A Spring Boot application should only ever include a single {@code @SpringBootConfiguration}.
     * In most idiomatic Spring Boot applications, this configuration is inherited from {@code @SpringBootApplication}.
     * Having multiple such classes can lead to unexpected behavior and conflicts during application startup.
     *
     * @see #ApplicationHasOnlyOneSpringBootConfiguration
     */
    public static ArchCondition<JavaClass> haveOnlyOneSpringBootConfiguration() {
        return new ArchCondition<JavaClass>("have only one class annotated with @SpringBootApplication or @SpringBootConfiguration") {

            private List<JavaClass> springBootConfigurationClasses;

            @Override
            public void init(Collection<JavaClass> javaClasses) {
                springBootConfigurationClasses = javaClasses.stream()
                        .filter(annotatedWithSpringBootConfiguration)
                        .collect(toList());
            }

            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                String message;
                if (annotatedWithSpringBootApplication.test(javaClass)) {
                    message = createMessage(javaClass, "is annotated with @SpringBootApplication");
                } else if (annotatedWithSpringBootConfiguration.test(javaClass)) {
                    message = createMessage(javaClass, "is annotated with @SpringBootConfiguration");
                } else {
                    message = createMessage(javaClass, "is not annotated with @SpringBootApplication or @SpringBootConfiguration");
                }

                if (springBootConfigurationClasses.size() > 1 && springBootConfigurationClasses.contains(javaClass)) {
                    events.add(violated(javaClass, message));
                } else {
                    events.add(satisfied(javaClass, message));
                }
            }
        };
    }
}
