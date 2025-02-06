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
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaMethodCall;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;

import static com.tngtech.archunit.lang.ConditionEvent.createMessage;
import static com.tngtech.archunit.lang.SimpleConditionEvent.satisfied;
import static com.tngtech.archunit.lang.SimpleConditionEvent.violated;
import static com.tngtech.archunit.lang.conditions.ArchConditions.beProtected;
import static com.tngtech.archunit.lang.conditions.ArchConditions.bePublic;
import static com.tngtech.archunit.lang.conditions.ArchConditions.not;
import static com.tngtech.archunit.lang.conditions.ArchConditions.notBeFinal;
import static com.tngtech.archunit.lang.conditions.ArchConditions.notBePrivate;
import static de.rweisleder.archunit.spring.internal.InternalUtils.isSpringFramework6;

/**
 * Collection of {@link ArchRule rules} that can be used to check the usage of Spring's proxy mechanism.
 *
 * @author Roland Weisleder
 */
public final class SpringProxyRules {

    private SpringProxyRules() {
    }

    /**
     * A condition that checks that Spring can create proxies for the given methods.
     * It is most convenient for Spring if such methods are public and not final,
     * and the bean classes containing these methods are also not final.
     * <p>
     * If Spring can not create a proxy for a method, this can lead to an exception when starting the context
     * or to unexpected behavior when calling the method.
     */
    public static ArchCondition<JavaMethod> beProxyable() {
        return new ArchCondition<JavaMethod>("be proxyable") {

            private final boolean isSpringFramework6 = isSpringFramework6();

            private final ArchCondition<JavaMethod> notDeclaredInInterface = not(new ArchCondition<JavaMethod>("declared in class that is an interface") {
                @Override
                public void check(JavaMethod javaMethod, ConditionEvents events) {
                    if (javaMethod.getOwner().isInterface()) {
                        events.add(satisfied(javaMethod, createMessage(javaMethod, "is declared in class that is an interface")));
                    } else {
                        events.add(violated(javaMethod, createMessage(javaMethod, "is not declared in class that is an interface")));
                    }
                }
            });

            private final ArchCondition<JavaClass> nonFinalClass = notBeFinal();

            private final ArchCondition<JavaMethod> nonFinalMethod = notBeFinal();

            private final ArchCondition<JavaMethod> publicMethod = bePublic();

            private final ArchCondition<JavaMethod> publicOrProtectedMethod = bePublic().or(beProtected()).forSubtype();

            private final ArchCondition<JavaMethod> nonPrivateMethod = notBePrivate();

            @Override
            public void check(JavaMethod method, ConditionEvents events) {
                JavaClass owner = method.getOwner();
                nonFinalClass.check(owner, events);

                for (JavaClass subclass : owner.getAllSubclasses()) {
                    nonFinalClass.check(subclass, events);
                }

                notDeclaredInInterface.check(method, events);

                nonFinalMethod.check(method, events);

                if (isSpringFramework6) {
                    if (hasSubclassInDifferentPackage(owner)) {
                        publicOrProtectedMethod.check(method, events);
                    } else {
                        nonPrivateMethod.check(method, events);
                    }
                } else {
                    publicMethod.check(method, events);
                }
            }

            private boolean hasSubclassInDifferentPackage(JavaClass javaClass) {
                for (JavaClass subclass : javaClass.getAllSubclasses()) {
                    if (!subclass.getPackage().equals(javaClass.getPackage())) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    /**
     * A condition that checks that the given methods are not called from within the same class.
     * Such internal calls bypass Spring's proxy mechanism, causing the intended caching behavior to be ignored.
     * <p>
     * This condition should only be used for JDK proxy-based advices.
     */
    public static ArchCondition<JavaMethod> notBeCalledFromWithinTheSameClass() {
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
