/*
 * #%L
 * ArchUnit Spring Integration
 * %%
 * Copyright (C) 2023 Roland Weisleder
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

import static com.tngtech.archunit.lang.conditions.ArchConditions.dependOnClassesThat;
import static com.tngtech.archunit.lang.conditions.ArchConditions.not;
import static com.tngtech.archunit.lang.conditions.ArchPredicates.are;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static de.rweisleder.archunit.spring.framework.SpringComponentPredicates.springConfiguration;
import static de.rweisleder.archunit.spring.framework.SpringComponentPredicates.springController;
import static de.rweisleder.archunit.spring.framework.SpringComponentPredicates.springRepository;
import static de.rweisleder.archunit.spring.framework.SpringComponentPredicates.springService;

/**
 * Collection of {@link ArchRule rules} that can be used to check the dependencies between Spring components
 * annotated with one of the Spring stereotype annotations.
 *
 * @author Roland Weisleder
 */
public final class SpringComponentRules {

    private SpringComponentRules() {
    }

    /**
     * A rule that checks that all {@link SpringComponentPredicates#springController() controller classes}
     * only depend on {@link SpringComponentPredicates#springService() service classes}
     * or {@link SpringComponentPredicates#springRepository() repository classes},
     * and not on other {@link SpringComponentPredicates#springController() controller classes}
     * or {@link SpringComponentPredicates#springConfiguration() configuration classes}.
     */
    public static final ArchRule DependenciesOfControllers = classes()
            .that(are(springController()))
            .should(not(dependOnClassesThat(
                    are(springController().or(springConfiguration()))
            )))
            .as("Spring controller should only depend on other Spring components that are services or repositories");

    /**
     * A rule that checks that all {@link SpringComponentPredicates#springService() service classes}
     * only depend on other {@link SpringComponentPredicates#springService() service classes}
     * or {@link SpringComponentPredicates#springRepository() repository classes},
     * and not on {@link SpringComponentPredicates#springController() controller classes}
     * or {@link SpringComponentPredicates#springConfiguration() configuration classes}.
     */
    public static final ArchRule DependenciesOfServices = classes()
            .that(are(springService()))
            .should(not(dependOnClassesThat(
                    are(springController().or(springConfiguration()))
            )))
            .as("Spring services should only depend on other Spring components that are services or repositories");

    /**
     * A rule that checks that all {@link SpringComponentPredicates#springRepository() repository classes}
     * only depend on other {@link SpringComponentPredicates#springRepository() repository classes},
     * and not on {@link SpringComponentPredicates#springController() controller classes},
     * {@link SpringComponentPredicates#springService() service classes},
     * or {@link SpringComponentPredicates#springConfiguration() configuration classes}.
     */
    public static final ArchRule DependenciesOfRepositories = classes()
            .that(are(springRepository()))
            .should(not(dependOnClassesThat(
                    are(springController().or(springService()).or(springConfiguration()))
            )))
            .as("Spring repositories should only depend on other Spring components that are repositories");
}
