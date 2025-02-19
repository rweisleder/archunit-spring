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
import java.util.concurrent.Future;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.assignableTo;
import static com.tngtech.archunit.lang.ConditionEvent.createMessage;
import static com.tngtech.archunit.lang.SimpleConditionEvent.satisfied;
import static com.tngtech.archunit.lang.SimpleConditionEvent.violated;
import static com.tngtech.archunit.lang.conditions.ArchConditions.haveRawReturnType;
import static com.tngtech.archunit.lang.conditions.ArchPredicates.are;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.all;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static de.rweisleder.archunit.spring.SpringAnnotationPredicates.springAnnotatedWith;
import static de.rweisleder.archunit.spring.framework.SpringAsyncPredicates.consideredAsAsynchronous;
import static de.rweisleder.archunit.spring.framework.SpringProxyRules.beProxyable;
import static de.rweisleder.archunit.spring.framework.SpringProxyRules.notBeCalledFromWithinTheSameClass;
import static de.rweisleder.archunit.spring.internal.InternalUtils.availableMethods;

/**
 * Collection of {@link ArchRule rules} that can be used to check the usage
 * of Spring's support for asynchronous method execution.
 *
 * @author Roland Weisleder
 */
public final class SpringAsyncRules {

    private SpringAsyncRules() {
    }

    /**
     * A rule that checks that Spring can create proxies for methods that are
     * {@link SpringAsyncPredicates#consideredAsAsynchronous() considered as asynchronous}.
     * It is most convenient for Spring if such methods are public and not final,
     * and the bean classes containing these methods are also not final.
     * <p>
     * If Spring can not create a proxy for a method, this can lead to an exception when starting the context
     * or to unexpected behavior when calling the method.
     *
     * @see SpringAsyncPredicates#consideredAsAsynchronous()
     * @see SpringProxyRules#beProxyable()
     */
    public static final ArchRule AsyncMethodsAreProxyable = all(availableMethods())
            .that(are(consideredAsAsynchronous()))
            .should(beProxyable());

    /**
     * A rule that checks that methods that are {@link SpringAsyncPredicates#consideredAsAsynchronous() considered as asynchronous}
     * have a suitable return type.
     * It is most convenient that such methods return {@code void} or an object implementing {@code java.util.concurrent.Future}.
     * <p>
     * If such methods have other return types, Spring may discard the return value or calling such a method may lead to an exception.
     *
     * @see SpringAsyncPredicates#consideredAsAsynchronous()
     */
    public static final ArchRule AsyncMethodsHaveSuitableReturnType = methods()
            .that(are(consideredAsAsynchronous()))
            .should(haveRawReturnType(assignableTo(Void.TYPE).or(assignableTo(Future.class))).as("have return type void or java.util.concurrent.Future"));

    /**
     * A rule that checks that methods that are {@link SpringAsyncPredicates#consideredAsAsynchronous() considered as asynchronous}
     * are not called from within the same class.
     * Such internal calls bypass Spring's proxy mechanism, causing the intended asynchronous behavior to be ignored.
     * <p>
     * This rule should only be used if asynchronous method execution is used in proxy mode, see the {@code @EnableAsync} annotation.
     *
     * @see SpringAsyncPredicates#consideredAsAsynchronous()
     * @see SpringProxyRules#notBeCalledFromWithinTheSameClass()
     */
    public static final ArchRule AsyncMethodsNotCalledFromSameClass = all(availableMethods())
            .that(are(consideredAsAsynchronous()))
            .should(notBeCalledFromWithinTheSameClass());

    /**
     * A rule that checks that the application contains a class annotated with {@code @EnableAsync} if any class
     * contains a method that is {@link SpringAsyncPredicates#consideredAsAsynchronous() considered as asynchronous}.
     * The rule has no effect if no such method exists.
     *
     * @see SpringAsyncPredicates#consideredAsAsynchronous()
     * @see #haveEnableAsyncPresentIfAsyncMethodsExist()
     */
    public static final ArchRule EnableAsyncIsPresentIfAsyncMethodsExist = classes()
            .should(haveEnableAsyncPresentIfAsyncMethodsExist())
            .as("application should contain a class annotated with @EnableAsync if any method is annotated with @Async");

    /**
     * A condition that checks that the given classes contain a class annotated with {@code @EnableAsync} if any class
     * contains a method that is {@link SpringAsyncPredicates#consideredAsAsynchronous() considered as asynchronous}.
     * The condition has no effect if no such method exists.
     *
     * @see SpringAsyncPredicates#consideredAsAsynchronous()
     * @see #EnableAsyncIsPresentIfAsyncMethodsExist
     */
    public static ArchCondition<JavaClass> haveEnableAsyncPresentIfAsyncMethodsExist() {
        return new ArchCondition<JavaClass>("have @EnableAsync present if methods annotated with @Async exist") {

            private final DescribedPredicate<JavaMethod> consideredAsAsynchronous = consideredAsAsynchronous();
            private final DescribedPredicate<JavaClass> annotatedWithEnableAsync = springAnnotatedWith("org.springframework.scheduling.annotation.EnableAsync").forSubtype();

            private boolean classesHaveMethodConsideredAsAsynchronous = false;
            private boolean hasClassAnnotatedWithEnableAsync = false;

            @Override
            public void init(Collection<JavaClass> javaClasses) {
                classesHaveMethodConsideredAsAsynchronous = javaClasses.stream()
                        .flatMap(javaClass -> javaClass.getAllMethods().stream())
                        .anyMatch(consideredAsAsynchronous);
            }

            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                if (classesHaveMethodConsideredAsAsynchronous) {
                    boolean classAnnotatedWithEnableAsync = annotatedWithEnableAsync.test(javaClass);
                    if (classAnnotatedWithEnableAsync) {
                        events.add(satisfied(javaClass, createMessage(javaClass, "is " + annotatedWithEnableAsync.getDescription())));
                    }

                    hasClassAnnotatedWithEnableAsync |= classAnnotatedWithEnableAsync;
                }
            }

            @Override
            public void finish(ConditionEvents events) {
                if (classesHaveMethodConsideredAsAsynchronous && !hasClassAnnotatedWithEnableAsync) {
                    events.add(violated(null, "application contains no class annotated with @EnableAsync"));
                }
            }
        };
    }
}
