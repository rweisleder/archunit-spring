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

import com.tngtech.archunit.lang.ArchRule;

import java.util.concurrent.Future;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.assignableTo;
import static com.tngtech.archunit.lang.conditions.ArchConditions.haveRawReturnType;
import static com.tngtech.archunit.lang.conditions.ArchPredicates.are;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.all;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
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
}
