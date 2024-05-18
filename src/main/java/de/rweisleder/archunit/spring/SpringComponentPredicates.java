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
package de.rweisleder.archunit.spring;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;

/**
 * @deprecated use {@link de.rweisleder.archunit.spring.framework.SpringComponentPredicates} instead
 */
@Deprecated
public class SpringComponentPredicates {

    /**
     * @deprecated use {@link de.rweisleder.archunit.spring.framework.SpringComponentPredicates#springComponent()} instead
     */
    @Deprecated
    public static DescribedPredicate<JavaClass> springComponent() {
        return de.rweisleder.archunit.spring.framework.SpringComponentPredicates.springComponent();
    }

    /**
     * @deprecated use {@link de.rweisleder.archunit.spring.framework.SpringComponentPredicates#springController()} instead
     */
    @Deprecated
    public static DescribedPredicate<JavaClass> springController() {
        return de.rweisleder.archunit.spring.framework.SpringComponentPredicates.springController();
    }

    /**
     * @deprecated use {@link de.rweisleder.archunit.spring.framework.SpringComponentPredicates#springService()} instead
     */
    @Deprecated
    public static DescribedPredicate<JavaClass> springService() {
        return de.rweisleder.archunit.spring.framework.SpringComponentPredicates.springService();
    }

    /**
     * @deprecated use {@link de.rweisleder.archunit.spring.framework.SpringComponentPredicates#springRepository()} instead
     */
    @Deprecated
    public static DescribedPredicate<JavaClass> springRepository() {
        return de.rweisleder.archunit.spring.framework.SpringComponentPredicates.springRepository();
    }

    /**
     * @deprecated use {@link de.rweisleder.archunit.spring.framework.SpringComponentPredicates#springConfiguration()} instead
     */
    @Deprecated
    public static DescribedPredicate<JavaClass> springConfiguration() {
        return de.rweisleder.archunit.spring.framework.SpringComponentPredicates.springConfiguration();
    }
}
