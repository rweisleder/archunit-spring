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
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.properties.CanBeAnnotated;

import static com.tngtech.archunit.core.domain.JavaMember.Predicates.declaredIn;
import static de.rweisleder.archunit.spring.SpringAnnotationPredicates.springAnnotatedWith;
import static de.rweisleder.archunit.spring.framework.SpringProxyPredicates.proxyable;
import static de.rweisleder.archunit.spring.internal.InternalUtils.isSpringFramework6;

/**
 * Collection of {@link DescribedPredicate predicates} that can be used to check the usage
 * of Spring's support for asynchronous method execution.
 *
 * @author Roland Weisleder
 */
public final class SpringAsyncPredicates {

    private SpringAsyncPredicates() {
    }

    /**
     * Returns a predicate that matches methods that Spring's support for asynchronous method execution would consider
     * as candidates for asynchronous execution. Essentially, these are methods that are annotated with {@code @Async}
     * or methods that are declared in classes annotated with {@code @Async}.
     */
    public static DescribedPredicate<JavaMethod> consideredAsAsynchronous() {
        DescribedPredicate<CanBeAnnotated> annotatedWithAsync = annotatedWithAsyncOrAsynchronous();

        DescribedPredicate<JavaMethod> methodAnnotatedWithAsync = annotatedWithAsync.forSubtype();

        DescribedPredicate<JavaMethod> declaredInClassAnnotatedWithAsync = declaredIn(annotatedWithAsync).forSubtype();
        DescribedPredicate<JavaMethod> methodIsProxyable = proxyable();

        return methodAnnotatedWithAsync.or(declaredInClassAnnotatedWithAsync.and(methodIsProxyable))
                .as("annotated with @Async or @Asynchronous (directly or at class level)");
    }

    private static DescribedPredicate<CanBeAnnotated> annotatedWithAsyncOrAsynchronous() {
        DescribedPredicate<CanBeAnnotated> async = springAnnotatedWith("org.springframework.scheduling.annotation.Async");

        DescribedPredicate<CanBeAnnotated> asynchronous = isSpringFramework6()
                ? springAnnotatedWith("jakarta.ejb.Asynchronous").or(springAnnotatedWith("jakarta.enterprise.concurrent.Asynchronous"))
                : springAnnotatedWith("javax.ejb.Asynchronous");

        return async.or(asynchronous).as("annotated with @Async or @Asynchronous");
    }
}
