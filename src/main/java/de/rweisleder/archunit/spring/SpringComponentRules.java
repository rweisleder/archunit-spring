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

import com.tngtech.archunit.lang.ArchRule;

/**
 * @deprecated use {@link de.rweisleder.archunit.spring.framework.SpringComponentRules} instead
 */
@Deprecated
public class SpringComponentRules {

    /**
     * @deprecated use {@link de.rweisleder.archunit.spring.framework.SpringComponentRules#DependenciesOfControllers} instead
     */
    @Deprecated
    public static final ArchRule DependenciesOfControllers = de.rweisleder.archunit.spring.framework.SpringComponentRules.DependenciesOfControllers;

    /**
     * @deprecated use {@link de.rweisleder.archunit.spring.framework.SpringComponentRules#DependenciesOfServices} instead
     */
    @Deprecated
    public static final ArchRule DependenciesOfServices = de.rweisleder.archunit.spring.framework.SpringComponentRules.DependenciesOfServices;

    /**
     * @deprecated use {@link de.rweisleder.archunit.spring.framework.SpringComponentRules#DependenciesOfRepositories} instead
     */
    @Deprecated
    public static final ArchRule DependenciesOfRepositories = de.rweisleder.archunit.spring.framework.SpringComponentRules.DependenciesOfRepositories;
}
