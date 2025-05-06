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
package de.rweisleder.archunit.spring.retry;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import de.rweisleder.archunit.spring.framework.SpringProxyRules;

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
 * Collection of {@link ArchRule rules} that can be used to check the usage of Spring's declarative retry support.
 *
 * @author Roland Weisleder
 * @author Hasan Kara
 */
public final class SpringRetryRules {

    private SpringRetryRules() {
    }

    /**
     * A rule that checks that Spring can create proxies for methods that are annotated with {@code @Retryable}.
     * It is most convenient for Spring if such methods are public and not final,
     * and the bean classes containing these methods are also not final.
     * <p>
     * If Spring can not create a proxy for a method, this can lead to an exception when starting the context
     * or to unexpected behavior when calling the method.
     *
     * @see SpringProxyRules#beProxyable()
     */
    public static final ArchRule RetryableMethodsAreProxyable = all(availableMethods())
            .that(are(springAnnotatedWith("org.springframework.retry.annotation.Retryable")))
            .should(beProxyable())
            .allowEmptyShould(true);

    /**
     * A rule that checks that methods annotated with {@code @Retryable} are not called from within the same class.
     * Such internal calls bypass Spring's proxy mechanism, causing the intended retry behavior to be ignored.
     * <p>
     * Example of a violating method:
     * <pre>{@code
     * public class BookService {
     *
     *     @Retryable(retryFor = SQLException.class)
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
     * This rule should only be used if retrying is used in proxy mode, see the {@code @EnableRetry} annotation.
     *
     * @see SpringProxyRules#notBeCalledFromWithinTheSameClass()
     * @see <a href="https://github.com/spring-projects/spring-retry#javaConfigForRetryProxies">Java Configuration for Retry Proxies</a>
     */
    public static final ArchRule RetryableMethodsNotCalledFromSameClass = all(availableMethods())
            .that(are(springAnnotatedWith("org.springframework.retry.annotation.Retryable")))
            .should(notBeCalledFromWithinTheSameClass())
            .allowEmptyShould(true);

    /**
     * A rule that checks that the application contains a class annotated with {@code @EnableRetry} if any class
     * contains a method annotated with {@code @Retryable}.
     * The rule has no effect if no such method exists.
     *
     * @see #haveEnableRetryPresentIfRetryableMethodsExist()
     */
    public static final ArchRule EnableRetryIsPresentIfRetryableMethodsExist = classes()
            .should(haveEnableRetryPresentIfRetryableMethodsExist())
            .as("application should contain a class annotated with @EnableRetry if any method is annotated with @Retryable")
            .allowEmptyShould(true);

    /**
     * A condition that checks that the given classes contain a class annotated with {@code @EnableRetry} if any class
     * contains a method annotated with {@code @Retryable}.
     * The condition has no effect if no such method exists.
     *
     * @see #EnableRetryIsPresentIfRetryableMethodsExist
     */
    public static ArchCondition<JavaClass> haveEnableRetryPresentIfRetryableMethodsExist() {
        return new ArchCondition<JavaClass>("have @EnableRetry present if methods annotated with @Retryable exist") {

            private final DescribedPredicate<JavaMethod> annotatedWithRetryable = springAnnotatedWith("org.springframework.retry.annotation.Retryable").forSubtype();
            private final DescribedPredicate<JavaClass> annotatedWithEnableRetry = springAnnotatedWith("org.springframework.retry.annotation.EnableRetry").forSubtype();

            private boolean classesHaveMethodAnnotatedWithRetryable = false;
            private boolean hasClassAnnotatedWithEnableRetry = false;

            @Override
            public void init(Collection<JavaClass> javaClasses) {
                classesHaveMethodAnnotatedWithRetryable = javaClasses.stream()
                        .flatMap(javaClass -> javaClass.getAllMethods().stream())
                        .anyMatch(annotatedWithRetryable);
                hasClassAnnotatedWithEnableRetry = false;
            }

            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                if (classesHaveMethodAnnotatedWithRetryable) {
                    boolean classAnnotatedWithEnableRetry = annotatedWithEnableRetry.test(javaClass);
                    if (classAnnotatedWithEnableRetry) {
                        events.add(satisfied(javaClass, createMessage(javaClass, "is " + annotatedWithEnableRetry.getDescription())));
                    }

                    hasClassAnnotatedWithEnableRetry |= classAnnotatedWithEnableRetry;
                }
            }

            @Override
            public void finish(ConditionEvents events) {
                if (classesHaveMethodAnnotatedWithRetryable && !hasClassAnnotatedWithEnableRetry) {
                    events.add(violated(null, "application contains no class annotated with @EnableRetry"));
                }
            }
        };
    }
}
