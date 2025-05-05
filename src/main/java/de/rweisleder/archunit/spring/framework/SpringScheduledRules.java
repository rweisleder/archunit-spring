/*
 * #%L
 * ArchUnit Spring Integration
 * %%
 * Copyright (C) 2023 - 2025 Roland Weisleder
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
package de.rweisleder.archunit.spring.framework;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;

import java.util.Collection;

import static com.tngtech.archunit.lang.ConditionEvent.createMessage;
import static com.tngtech.archunit.lang.SimpleConditionEvent.satisfied;
import static com.tngtech.archunit.lang.SimpleConditionEvent.violated;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static de.rweisleder.archunit.spring.SpringAnnotationPredicates.springAnnotatedWith;

/**
 * Collection of {@link ArchRule rules} that can be used to check the usage
 * of Spring's scheduled task execution capability.
 *
 * @author Roland Weisleder
 */
public final class SpringScheduledRules {

    private SpringScheduledRules() {
    }

    /**
     * A rule that checks that the application contains a class annotated with {@code @EnableScheduling} if any class
     * contains a method that is annotated with {@code @Scheduled}.
     * The rule has no effect if no such method exists.
     *
     * @see #haveEnableSchedulingPresentIfScheduledMethodsExist()
     */
    public static final ArchRule EnableSchedulingIsPresentIfScheduledMethodsExist = classes()
            .should(haveEnableSchedulingPresentIfScheduledMethodsExist())
            .as("application should contain a class annotated with @EnableScheduling if any method is annotated with @Scheduled");

    /**
     * A condition that checks that the given classes contain a class annotated with {@code @EnableScheduling} if any class
     * contains a method that is annotated with {@code @Scheduled}.
     * The condition has no effect if no such method exists.
     *
     * @see #EnableSchedulingIsPresentIfScheduledMethodsExist
     */
    public static ArchCondition<JavaClass> haveEnableSchedulingPresentIfScheduledMethodsExist() {
        return new ArchCondition<JavaClass>("have @EnableScheduling present if methods annotated with @Scheduled exist") {

            private final DescribedPredicate<JavaMethod> annotatedWithScheduled = springAnnotatedWith("org.springframework.scheduling.annotation.Scheduled").forSubtype();
            private final DescribedPredicate<JavaClass> annotatedWithEnableScheduling = springAnnotatedWith("org.springframework.scheduling.annotation.EnableScheduling").forSubtype();

            private boolean classesHaveMethodAnnotatedWithScheduled = false;
            private boolean hasClassAnnotatedWithEnableScheduling = false;

            @Override
            public void init(Collection<JavaClass> javaClasses) {
                classesHaveMethodAnnotatedWithScheduled = javaClasses.stream()
                        .flatMap(javaClass -> javaClass.getAllMethods().stream())
                        .anyMatch(annotatedWithScheduled);
                hasClassAnnotatedWithEnableScheduling = false;
            }

            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                if (classesHaveMethodAnnotatedWithScheduled) {
                    boolean classAnnotatedWithEnableScheduling = annotatedWithEnableScheduling.test(javaClass);
                    if (classAnnotatedWithEnableScheduling) {
                        events.add(satisfied(javaClass, createMessage(javaClass, "is " + annotatedWithEnableScheduling.getDescription())));
                    }

                    hasClassAnnotatedWithEnableScheduling |= classAnnotatedWithEnableScheduling;
                }
            }

            @Override
            public void finish(ConditionEvents events) {
                if (classesHaveMethodAnnotatedWithScheduled && !hasClassAnnotatedWithEnableScheduling) {
                    events.add(violated(null, "application contains no class annotated with @EnableScheduling"));
                }
            }
        };
    }
}
