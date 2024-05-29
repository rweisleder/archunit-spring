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
package de.rweisleder.archunit.spring.internal;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.lang.AbstractClassesTransformer;
import com.tngtech.archunit.lang.ClassesTransformer;
import org.springframework.util.ClassUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * For internal use only.
 */
public final class InternalUtils {

    private static Boolean isSpringFramework6;

    private InternalUtils() {
    }

    public static ClassesTransformer<JavaMethod> availableMethods() {
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

    public static boolean isSpringFramework6() {
        if (isSpringFramework6 == null) {
            isSpringFramework6 = ClassUtils.isPresent("org.springframework.aot.AotDetector", null);
        }
        return isSpringFramework6;
    }
}
