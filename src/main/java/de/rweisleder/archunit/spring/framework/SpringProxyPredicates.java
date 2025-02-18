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
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;

import static de.rweisleder.archunit.spring.framework.SpringProxyRules.beProxyable;

/**
 * Collection of {@link DescribedPredicate predicates} that can be used to check the usage of Spring's proxy mechanism.
 *
 * @author Roland Weisleder
 */
public final class SpringProxyPredicates {

    private SpringProxyPredicates() {
    }

    /**
     * Returns a predicate that matches methods for which Spring can create a proxy.
     * It is most convenient for Spring if such methods are public and not final,
     * and the bean classes containing these methods are also not final.
     * <p>
     * If Spring can not create a proxy for a method, this can lead to an exception when starting the context
     * or to unexpected behavior when calling the method.
     *
     * @see SpringProxyRules#beProxyable()
     */
    public static DescribedPredicate<JavaMethod> proxyable() {
        return new DescribedPredicate<JavaMethod>("proxyable") {

            private final ArchCondition<JavaMethod> isProxyable = beProxyable();

            @Override
            public boolean test(JavaMethod method) {
                ConditionEvents events = ConditionEvents.Factory.create();
                isProxyable.check(method, events);
                return !events.containViolation();
            }
        };
    }
}
