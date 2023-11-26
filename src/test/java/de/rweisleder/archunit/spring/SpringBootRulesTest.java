package de.rweisleder.archunit.spring;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.lang.EvaluationResult;
import de.rweisleder.archunit.spring.testclasses.boot.app1.FirstSpringBootApplication;
import de.rweisleder.archunit.spring.testclasses.boot.app1.subpackage.FirstAppClassInSubpackage;
import de.rweisleder.archunit.spring.testclasses.boot.app2.SecondSpringBootApplication;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static de.rweisleder.archunit.spring.TestUtils.importClasses;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpringBootRulesTest {

    @Nested
    class Rule_AllTypesInApplicationPackage {

        @Test
        void provides_a_description() {
            String description = SpringBootRules.AllTypesInApplicationPackage.getDescription();
            assertThat(description).isEqualTo("all types of a Spring Boot application should be located in the same package or a sub-package of the application class");
        }

        @Test
        void has_no_violation_if_all_classes_are_in_application_package_or_subpackage() {
            JavaClasses classes = importClasses(FirstSpringBootApplication.class, FirstAppClassInSubpackage.class);
            EvaluationResult evaluationResult = SpringBootRules.AllTypesInApplicationPackage.evaluate(classes);
            assertThat(evaluationResult.hasViolation()).isFalse();
        }

        @Test
        void has_violation_for_class_outside_application_package() {
            Class<?> classOutsideApplicationPackage = String.class;
            JavaClasses classes = importClasses(FirstSpringBootApplication.class, classOutsideApplicationPackage);
            EvaluationResult evaluationResult = SpringBootRules.AllTypesInApplicationPackage.evaluate(classes);
            assertThat(evaluationResult.hasViolation()).isTrue();
            assertThat(evaluationResult.getFailureReport().getDetails()).anySatisfy(detail -> {
                assertThat(detail).contains(classOutsideApplicationPackage.getName(), "does not reside in any package");
            });
        }

        @Test
        void fails_if_classes_do_not_contain_a_Spring_Boot_application() {
            JavaClasses classes = importClasses(String.class);
            assertThatThrownBy(() -> SpringBootRules.AllTypesInApplicationPackage.evaluate(classes))
                    .isInstanceOf(AssertionError.class)
                    .hasMessageContaining("Could not locate a class annotated with @SpringBootApplication or @SpringBootConfiguration");
        }

        @Test
        void has_no_violation_if_classes_contain_multiple_distinct_Spring_Boot_applications() {
            JavaClasses classes = importClasses(FirstSpringBootApplication.class, SecondSpringBootApplication.class);
            EvaluationResult evaluationResult = SpringBootRules.AllTypesInApplicationPackage.evaluate(classes);
            assertThat(evaluationResult.hasViolation()).isFalse();
        }
    }
}
