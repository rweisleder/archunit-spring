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

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaMethodCall;
import com.tngtech.archunit.lang.AbstractClassesTransformer;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ClassesTransformer;
import com.tngtech.archunit.lang.ConditionEvents;

import java.util.HashSet;
import java.util.Set;

import static com.tngtech.archunit.lang.SimpleConditionEvent.violated;
import static com.tngtech.archunit.lang.conditions.ArchPredicates.are;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.all;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static de.rweisleder.archunit.spring.SpringAnnotationPredicates.springAnnotatedWith;
import static de.rweisleder.archunit.spring.framework.SpringProxyRules.beProxyable;

/**
 * Collection of {@link ArchRule rules} that can be used to check the usage of Spring's generic cache abstraction.
 *
 * @author Roland Weisleder
 */
public final class SpringCacheRules {

    private SpringCacheRules() {
    }

    /**
     * A rule that checks that Spring can create proxies for methods that are annotated with {@code @Cacheable}.
     * It is most convenient for Spring if such methods are public and not final,
     * and the bean classes containing these methods are also not final.
     * <p>
     * If Spring can not create a proxy for a method, this can lead to an exception when starting the context
     * or to unexpected behavior when calling the method.
     *
     * @see SpringProxyRules#beProxyable()
     */
    public static final ArchRule CacheableMethodIsProxyable = all(availableMethods())
            .that(are(springAnnotatedWith("org.springframework.cache.annotation.Cacheable")))
            .should(beProxyable());

    private static ClassesTransformer<JavaMethod> availableMethods() {
        return new AbstractClassesTransformer<JavaMethod>("methods") {
            @Override
            public Iterable<JavaMethod> doTransform(JavaClasses javaClasses) {
                Set<JavaMethod> availableMethods = new HashSet<>();
                for (JavaClass javaClass : javaClasses) {
                    availableMethods.addAll(javaClass.getAllMethods());
                }
                return availableMethods;
            }
        };
    }

    /**
     * A rule that checks that methods annotated with {@code @Cacheable} are not called from within the same class.
     * Such internal calls bypass Spring's proxy mechanism, causing the intended caching behavior to be ignored.
     * <p>
     * Example of a violating method:
     * <pre>{@code
     * public class BookService {
     *
     *     @Cacheable("books")
     *     public Book findBook(String isbn) {
     *         return database.findBook(isbn);
     *     }
     *
     *     public String findBookTitle(String isbn) {
     *         Book book = findBook(isbn); // Violation, as this internal call bypasses the proxy functionality
     *         return book.getTitle();
     *     }
     * }
     * }</pre>
     * <p>
     * This rule should only be used if caching is used in proxy mode, see the {@code @EnableCaching} annotation.
     */
    public static final ArchRule CacheableMethodNotCalledFromSameClass = methods()
            .that(are(springAnnotatedWith("org.springframework.cache.annotation.Cacheable")))
            .should(notBeCalledFromWithinTheSameClass());

    private static ArchCondition<JavaMethod> notBeCalledFromWithinTheSameClass() {
        return new ArchCondition<JavaMethod>("not be called from within the same class") {
            @Override
            public void check(JavaMethod method, ConditionEvents events) {
                for (JavaMethodCall methodCall : method.getCallsOfSelf()) {
                    boolean calledFromWithinSameClass = methodCall.getOriginOwner().equals(methodCall.getTargetOwner());
                    if (calledFromWithinSameClass) {
                        events.add(violated(method, methodCall.getDescription()));
                    }
                }
            }
        };
    }
}
