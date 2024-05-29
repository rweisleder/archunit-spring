/*
 * #%L
 * ArchUnit Spring Integration
 * %%
 * Copyright (C) 2023 - 2024 Roland Weisleder
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
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.lang.ArchRule;

import java.util.concurrent.Future;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.assignableTo;
import static com.tngtech.archunit.lang.conditions.ArchConditions.haveRawReturnType;
import static com.tngtech.archunit.lang.conditions.ArchPredicates.are;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.all;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static de.rweisleder.archunit.spring.SpringAnnotationPredicates.springAnnotatedWith;
import static de.rweisleder.archunit.spring.Utils.availableMethods;
import static de.rweisleder.archunit.spring.Utils.isSpringFramework6;
import static de.rweisleder.archunit.spring.framework.SpringProxyRules.beProxyable;
import static de.rweisleder.archunit.spring.framework.SpringProxyRules.notBeCalledFromWithinTheSameClass;

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
     * A rule that checks that Spring can create proxies for methods that are annotated with {@code @Async} or {@code @Asynchronous}.
     * It is most convenient for Spring if such methods are public and not final,
     * and the bean classes containing these methods are also not final.
     * <p>
     * If Spring can not create a proxy for a method, this can lead to an exception when starting the context
     * or to unexpected behavior when calling the method.
     *
     * @see SpringProxyRules#beProxyable()
     */
    public static final ArchRule AsyncMethodsAreProxyable = all(availableMethods())
            .that(are(annotatedWithAsyncOrAsynchronous()))
            .should(beProxyable());

    /**
     * A rule that checks that methods annotated with {@code @Async} or {@code @Asynchronous} have a suitable return type.
     * It is most convenient that such methods return {@code void} or an object implementing {@code java.util.concurrent.Future}.
     * <p>
     * If such methods have other return types, Spring may discard the return value or calling such a method may lead to an exception.
     */
    public static final ArchRule AsyncMethodsHaveSuitableReturnType = methods()
            .that(are(annotatedWithAsyncOrAsynchronous()))
            .should(haveRawReturnType(assignableTo(Void.TYPE).or(assignableTo(Future.class))).as("have return type void or java.util.concurrent.Future"));

    private static DescribedPredicate<JavaMethod> annotatedWithAsyncOrAsynchronous() {
        DescribedPredicate<JavaMethod> async = springAnnotatedWith("org.springframework.scheduling.annotation.Async").forSubtype();

        DescribedPredicate<JavaMethod> asynchronous = isSpringFramework6()
                ? springAnnotatedWith("jakarta.ejb.Asynchronous").or(springAnnotatedWith("jakarta.enterprise.concurrent.Asynchronous")).forSubtype()
                : springAnnotatedWith("javax.ejb.Asynchronous").forSubtype();

        return async.or(asynchronous).as("annotated with @Async or @Asynchronous").forSubtype();
    }
}
