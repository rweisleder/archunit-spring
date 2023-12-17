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
package de.rweisleder.archunit.spring;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.lang.EvaluationResult;
import de.rweisleder.archunit.spring.testclasses.component.SpringComponents.ControllerWithDependencyToConfiguration;
import de.rweisleder.archunit.spring.testclasses.component.SpringComponents.ControllerWithDependencyToController;
import de.rweisleder.archunit.spring.testclasses.component.SpringComponents.ControllerWithDependencyToRepository;
import de.rweisleder.archunit.spring.testclasses.component.SpringComponents.ControllerWithDependencyToService;
import de.rweisleder.archunit.spring.testclasses.component.SpringComponents.ControllerWithDependencyToSpringDataRepository;
import de.rweisleder.archunit.spring.testclasses.component.SpringComponents.ControllerWithoutDependency;
import de.rweisleder.archunit.spring.testclasses.component.SpringComponents.RepositoryWithDependencyToConfiguration;
import de.rweisleder.archunit.spring.testclasses.component.SpringComponents.RepositoryWithDependencyToController;
import de.rweisleder.archunit.spring.testclasses.component.SpringComponents.RepositoryWithDependencyToRepository;
import de.rweisleder.archunit.spring.testclasses.component.SpringComponents.RepositoryWithDependencyToService;
import de.rweisleder.archunit.spring.testclasses.component.SpringComponents.RepositoryWithDependencyToSpringDataRepository;
import de.rweisleder.archunit.spring.testclasses.component.SpringComponents.RepositoryWithoutDependency;
import de.rweisleder.archunit.spring.testclasses.component.SpringComponents.ServiceWithDependencyToConfiguration;
import de.rweisleder.archunit.spring.testclasses.component.SpringComponents.ServiceWithDependencyToController;
import de.rweisleder.archunit.spring.testclasses.component.SpringComponents.ServiceWithDependencyToRepository;
import de.rweisleder.archunit.spring.testclasses.component.SpringComponents.ServiceWithDependencyToService;
import de.rweisleder.archunit.spring.testclasses.component.SpringComponents.ServiceWithDependencyToSpringDataRepository;
import de.rweisleder.archunit.spring.testclasses.component.SpringComponents.ServiceWithoutDependency;
import de.rweisleder.archunit.spring.testclasses.component.SpringComponents.SpringDataRepositoryWithDependencyToConfiguration;
import de.rweisleder.archunit.spring.testclasses.component.SpringComponents.SpringDataRepositoryWithDependencyToController;
import de.rweisleder.archunit.spring.testclasses.component.SpringComponents.SpringDataRepositoryWithDependencyToRepository;
import de.rweisleder.archunit.spring.testclasses.component.SpringComponents.SpringDataRepositoryWithDependencyToService;
import de.rweisleder.archunit.spring.testclasses.component.SpringComponents.SpringDataRepositoryWithDependencyToSpringDataRepository;
import de.rweisleder.archunit.spring.testclasses.component.SpringComponents.SpringDataRepositoryWithoutDependency;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static de.rweisleder.archunit.spring.TestUtils.importClasses;
import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("CodeBlock2Expr")
class SpringComponentRulesTest {

    @Nested
    class Rule_DependenciesOfControllers {

        @Test
        void provides_a_description() {
            String description = SpringComponentRules.DependenciesOfControllers.getDescription();
            assertThat(description).isEqualTo("Spring controller should only depend on other Spring components that are services or repositories");
        }

        @Test
        void controller_without_dependency_is_not_a_violation() {
            JavaClasses classes = importClasses(ControllerWithoutDependency.class);
            EvaluationResult evaluationResult = SpringComponentRules.DependenciesOfControllers.evaluate(classes);
            assertThat(evaluationResult.hasViolation()).isFalse();
        }

        @Test
        void controller_with_dependency_to_other_controller_is_a_violation() {
            JavaClasses classes = importClasses(ControllerWithDependencyToController.class);
            EvaluationResult evaluationResult = SpringComponentRules.DependenciesOfControllers.evaluate(classes);
            assertThat(evaluationResult.hasViolation()).isTrue();
            assertThat(evaluationResult.getFailureReport().getDetails()).anySatisfy(detail -> {
                assertThat(detail).contains(ControllerWithDependencyToController.class.getName());
            });
        }

        @Test
        void controller_with_dependency_to_service_is_not_a_violation() {
            JavaClasses classes = importClasses(ControllerWithDependencyToService.class);
            EvaluationResult evaluationResult = SpringComponentRules.DependenciesOfControllers.evaluate(classes);
            assertThat(evaluationResult.hasViolation()).isFalse();
        }

        @Test
        void controller_with_dependency_to_repository_is_not_a_violation() {
            JavaClasses classes = importClasses(ControllerWithDependencyToRepository.class);
            EvaluationResult evaluationResult = SpringComponentRules.DependenciesOfControllers.evaluate(classes);
            assertThat(evaluationResult.hasViolation()).isFalse();
        }

        @Test
        void controller_with_dependency_to_Spring_Data_repository_is_not_a_violation() {
            JavaClasses classes = importClasses(ControllerWithDependencyToSpringDataRepository.class);
            EvaluationResult evaluationResult = SpringComponentRules.DependenciesOfControllers.evaluate(classes);
            assertThat(evaluationResult.hasViolation()).isFalse();
        }

        @Test
        void controller_with_dependency_to_configuration_is_a_violation() {
            JavaClasses classes = importClasses(ControllerWithDependencyToConfiguration.class);
            EvaluationResult evaluationResult = SpringComponentRules.DependenciesOfControllers.evaluate(classes);
            assertThat(evaluationResult.hasViolation()).isTrue();
            assertThat(evaluationResult.getFailureReport().getDetails()).anySatisfy(detail -> {
                assertThat(detail).contains(ControllerWithDependencyToConfiguration.class.getName());
            });
        }
    }

    @Nested
    class Rule_DependenciesOfServices {

        @Test
        void provides_a_description() {
            String description = SpringComponentRules.DependenciesOfServices.getDescription();
            assertThat(description).isEqualTo("Spring services should only depend on other Spring components that are services or repositories");
        }

        @Test
        void service_without_dependency_is_not_a_violation() {
            JavaClasses classes = importClasses(ServiceWithoutDependency.class);
            EvaluationResult evaluationResult = SpringComponentRules.DependenciesOfServices.evaluate(classes);
            assertThat(evaluationResult.hasViolation()).isFalse();
        }

        @Test
        void service_with_dependency_to_controller_is_a_violation() {
            JavaClasses classes = importClasses(ServiceWithDependencyToController.class);
            EvaluationResult evaluationResult = SpringComponentRules.DependenciesOfServices.evaluate(classes);
            assertThat(evaluationResult.hasViolation()).isTrue();
            assertThat(evaluationResult.getFailureReport().getDetails()).anySatisfy(detail -> {
                assertThat(detail).contains(ServiceWithDependencyToController.class.getName());
            });
        }

        @Test
        void service_with_dependency_to_other_service_is_not_a_violation() {
            JavaClasses classes = importClasses(ServiceWithDependencyToService.class);
            EvaluationResult evaluationResult = SpringComponentRules.DependenciesOfServices.evaluate(classes);
            assertThat(evaluationResult.hasViolation()).isFalse();
        }

        @Test
        void service_with_dependency_to_repository_is_not_a_violation() {
            JavaClasses classes = importClasses(ServiceWithDependencyToRepository.class);
            EvaluationResult evaluationResult = SpringComponentRules.DependenciesOfServices.evaluate(classes);
            assertThat(evaluationResult.hasViolation()).isFalse();
        }

        @Test
        void service_with_dependency_to_Spring_Data_repository_is_not_a_violation() {
            JavaClasses classes = importClasses(ServiceWithDependencyToSpringDataRepository.class);
            EvaluationResult evaluationResult = SpringComponentRules.DependenciesOfServices.evaluate(classes);
            assertThat(evaluationResult.hasViolation()).isFalse();
        }

        @Test
        void service_with_dependency_to_configuration_is_a_violation() {
            JavaClasses classes = importClasses(ServiceWithDependencyToConfiguration.class);
            EvaluationResult evaluationResult = SpringComponentRules.DependenciesOfServices.evaluate(classes);
            assertThat(evaluationResult.hasViolation()).isTrue();
            assertThat(evaluationResult.getFailureReport().getDetails()).anySatisfy(detail -> {
                assertThat(detail).contains(ServiceWithDependencyToConfiguration.class.getName());
            });
        }
    }

    @Nested
    class Rule_DependenciesOfRepositories {

        @Test
        void provides_a_description() {
            String description = SpringComponentRules.DependenciesOfRepositories.getDescription();
            assertThat(description).isEqualTo("Spring repositories should only depend on other Spring components that are repositories");
        }

        @Test
        void repository_without_dependency_is_not_a_violation() {
            JavaClasses classes = importClasses(RepositoryWithoutDependency.class);
            EvaluationResult evaluationResult = SpringComponentRules.DependenciesOfRepositories.evaluate(classes);
            assertThat(evaluationResult.hasViolation()).isFalse();
        }

        @Test
        void repository_with_dependency_to_controller_is_a_violation() {
            JavaClasses classes = importClasses(RepositoryWithDependencyToController.class);
            EvaluationResult evaluationResult = SpringComponentRules.DependenciesOfRepositories.evaluate(classes);
            assertThat(evaluationResult.hasViolation()).isTrue();
            assertThat(evaluationResult.getFailureReport().getDetails()).anySatisfy(detail -> {
                assertThat(detail).contains(RepositoryWithDependencyToController.class.getName());
            });
        }

        @Test
        void repository_with_dependency_to_service_is_a_violation() {
            JavaClasses classes = importClasses(RepositoryWithDependencyToService.class);
            EvaluationResult evaluationResult = SpringComponentRules.DependenciesOfRepositories.evaluate(classes);
            assertThat(evaluationResult.hasViolation()).isTrue();
            assertThat(evaluationResult.getFailureReport().getDetails()).anySatisfy(detail -> {
                assertThat(detail).contains(RepositoryWithDependencyToService.class.getName());
            });
        }

        @Test
        void repository_with_dependency_to_other_repository_is_not_a_violation() {
            JavaClasses classes = importClasses(RepositoryWithDependencyToRepository.class);
            EvaluationResult evaluationResult = SpringComponentRules.DependenciesOfRepositories.evaluate(classes);
            assertThat(evaluationResult.hasViolation()).isFalse();
        }

        @Test
        void repository_with_dependency_to_Spring_Data_repository_is_not_a_violation() {
            JavaClasses classes = importClasses(RepositoryWithDependencyToSpringDataRepository.class);
            EvaluationResult evaluationResult = SpringComponentRules.DependenciesOfRepositories.evaluate(classes);
            assertThat(evaluationResult.hasViolation()).isFalse();
        }

        @Test
        void repository_with_dependency_to_configuration_is_a_violation() {
            JavaClasses classes = importClasses(RepositoryWithDependencyToConfiguration.class);
            EvaluationResult evaluationResult = SpringComponentRules.DependenciesOfRepositories.evaluate(classes);
            assertThat(evaluationResult.hasViolation()).isTrue();
            assertThat(evaluationResult.getFailureReport().getDetails()).anySatisfy(detail -> {
                assertThat(detail).contains(RepositoryWithDependencyToConfiguration.class.getName());
            });
        }

        @Test
        void Spring_Data_repository_without_dependency_is_not_a_violation() {
            JavaClasses classes = importClasses(SpringDataRepositoryWithoutDependency.class);
            EvaluationResult evaluationResult = SpringComponentRules.DependenciesOfRepositories.evaluate(classes);
            assertThat(evaluationResult.hasViolation()).isFalse();
        }

        @Test
        void Spring_Data_repository_with_dependency_to_controller_is_a_violation() {
            JavaClasses classes = importClasses(SpringDataRepositoryWithDependencyToController.class);
            EvaluationResult evaluationResult = SpringComponentRules.DependenciesOfRepositories.evaluate(classes);
            assertThat(evaluationResult.hasViolation()).isTrue();
            assertThat(evaluationResult.getFailureReport().getDetails()).anySatisfy(detail -> {
                assertThat(detail).contains(SpringDataRepositoryWithDependencyToController.class.getName());
            });
        }

        @Test
        void Spring_Data_repository_with_dependency_to_service_is_a_violation() {
            JavaClasses classes = importClasses(SpringDataRepositoryWithDependencyToService.class);
            EvaluationResult evaluationResult = SpringComponentRules.DependenciesOfRepositories.evaluate(classes);
            assertThat(evaluationResult.hasViolation()).isTrue();
            assertThat(evaluationResult.getFailureReport().getDetails()).anySatisfy(detail -> {
                assertThat(detail).contains(SpringDataRepositoryWithDependencyToService.class.getName());
            });
        }

        @Test
        void Spring_Data_repository_with_dependency_to_repository_is_not_a_violation() {
            JavaClasses classes = importClasses(SpringDataRepositoryWithDependencyToRepository.class);
            EvaluationResult evaluationResult = SpringComponentRules.DependenciesOfRepositories.evaluate(classes);
            assertThat(evaluationResult.hasViolation()).isFalse();
        }

        @Test
        void Spring_Data_repository_with_dependency_to_other_Spring_Data_repository_is_not_a_violation() {
            JavaClasses classes = importClasses(SpringDataRepositoryWithDependencyToSpringDataRepository.class);
            EvaluationResult evaluationResult = SpringComponentRules.DependenciesOfRepositories.evaluate(classes);
            assertThat(evaluationResult.hasViolation()).isFalse();
        }

        @Test
        void Spring_Data_repository_with_dependency_to_configuration_is_a_violation() {
            JavaClasses classes = importClasses(SpringDataRepositoryWithDependencyToConfiguration.class);
            EvaluationResult evaluationResult = SpringComponentRules.DependenciesOfRepositories.evaluate(classes);
            assertThat(evaluationResult.hasViolation()).isTrue();
            assertThat(evaluationResult.getFailureReport().getDetails()).anySatisfy(detail -> {
                assertThat(detail).contains(SpringDataRepositoryWithDependencyToConfiguration.class.getName());
            });
        }
    }
}
