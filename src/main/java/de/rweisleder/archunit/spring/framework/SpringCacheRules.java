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
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;

import java.util.Collection;

import static com.tngtech.archunit.lang.ConditionEvent.createMessage;
import static com.tngtech.archunit.lang.SimpleConditionEvent.satisfied;
import static com.tngtech.archunit.lang.SimpleConditionEvent.violated;
import static com.tngtech.archunit.lang.conditions.ArchPredicates.are;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.all;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static de.rweisleder.archunit.spring.SpringAnnotationPredicates.springAnnotatedWith;
import static de.rweisleder.archunit.spring.framework.SpringProxyRules.beProxyable;
import static de.rweisleder.archunit.spring.framework.SpringProxyRules.notBeCalledFromWithinTheSameClass;
import static de.rweisleder.archunit.spring.internal.InternalUtils.availableMethods;

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
    public static final ArchRule CacheableMethodsAreProxyable = all(availableMethods())
            .that(are(springAnnotatedWith("org.springframework.cache.annotation.Cacheable")))
            .should(beProxyable())
            .allowEmptyShould(true);

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
     *
     * @see SpringProxyRules#notBeCalledFromWithinTheSameClass()
     */
    public static final ArchRule CacheableMethodsNotCalledFromSameClass = all(availableMethods())
            .that(are(springAnnotatedWith("org.springframework.cache.annotation.Cacheable")))
            .should(notBeCalledFromWithinTheSameClass())
            .allowEmptyShould(true);

    /**
     * A rule that checks that the application contains a class annotated with {@code @EnableCaching} if any class
     * contains a method annotated with {@code @Cacheable}.
     * The rule has no effect if no such method exists.
     *
     * @see #haveEnableCachingPresentIfCacheableMethodsExist()
     */
    public static final ArchRule EnableCachingIsPresentIfCacheableMethodsExist = classes()
            .should(haveEnableCachingPresentIfCacheableMethodsExist())
            .as("application should contain a class annotated with @EnableCaching if any method is annotated with @Cacheable")
            .allowEmptyShould(true);

    /**
     * A condition that checks that the given classes contain a class annotated with {@code @EnableCaching} if any class
     * contains a method annotated with {@code @Cacheable}.
     * The condition has no effect if no such method exists.
     *
     * @see #EnableCachingIsPresentIfCacheableMethodsExist
     */
    public static ArchCondition<JavaClass> haveEnableCachingPresentIfCacheableMethodsExist() {
        return new ArchCondition<JavaClass>("have @EnableCaching present if methods annotated with @Cacheable exist") {

            private final DescribedPredicate<JavaMethod> annotatedWithCacheable = springAnnotatedWith("org.springframework.cache.annotation.Cacheable").forSubtype();
            private final DescribedPredicate<JavaClass> annotatedWithEnableCaching = springAnnotatedWith("org.springframework.cache.annotation.EnableCaching").forSubtype();

            private boolean classesHaveMethodAnnotatedWithCacheable = false;
            private boolean hasClassAnnotatedWithEnableCaching = false;

            @Override
            public void init(Collection<JavaClass> javaClasses) {
                classesHaveMethodAnnotatedWithCacheable = javaClasses.stream()
                        .flatMap(javaClass -> javaClass.getAllMethods().stream())
                        .anyMatch(annotatedWithCacheable);
                hasClassAnnotatedWithEnableCaching = false;
            }

            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                if (classesHaveMethodAnnotatedWithCacheable) {
                    boolean classAnnotatedWithEnableCaching = annotatedWithEnableCaching.test(javaClass);
                    if (classAnnotatedWithEnableCaching) {
                        events.add(satisfied(javaClass, createMessage(javaClass, "is " + annotatedWithEnableCaching.getDescription())));
                    }

                    hasClassAnnotatedWithEnableCaching |= classAnnotatedWithEnableCaching;
                }
            }

            @Override
            public void finish(ConditionEvents events) {
                if (classesHaveMethodAnnotatedWithCacheable && !hasClassAnnotatedWithEnableCaching) {
                    events.add(violated(null, "application contains no class annotated with @EnableCaching"));
                }
            }
        };
    }
}
