package de.rweisleder.archunit.spring;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import de.rweisleder.archunit.spring.testclasses.component.SpringComponents.ComponentWithoutDependency;
import de.rweisleder.archunit.spring.testclasses.component.SpringComponents.ConfigurationWithoutDependency;
import de.rweisleder.archunit.spring.testclasses.component.SpringComponents.ControllerWithoutDependency;
import de.rweisleder.archunit.spring.testclasses.component.SpringComponents.RepositoryWithoutDependency;
import de.rweisleder.archunit.spring.testclasses.component.SpringComponents.ServiceWithoutDependency;
import de.rweisleder.archunit.spring.testclasses.component.SpringComponents.SpringDataRepositoryWithoutDependency;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.repository.CrudRepository;

import static de.rweisleder.archunit.spring.SpringComponentPredicates.springComponent;
import static de.rweisleder.archunit.spring.SpringComponentPredicates.springConfiguration;
import static de.rweisleder.archunit.spring.SpringComponentPredicates.springController;
import static de.rweisleder.archunit.spring.SpringComponentPredicates.springRepository;
import static de.rweisleder.archunit.spring.SpringComponentPredicates.springService;
import static de.rweisleder.archunit.spring.TestUtils.importClass;
import static org.assertj.core.api.Assertions.assertThat;

class SpringComponentPredicatesTest {

    @Nested
    class Predicate_springComponent {

        @Test
        void provides_a_description() {
            DescribedPredicate<JavaClass> predicate = springComponent();
            assertThat(predicate.getDescription()).isEqualTo("Spring component");
        }

        @Test
        void accepts_class_annotated_with_Component() {
            JavaClass componentClass = importClass(ComponentWithoutDependency.class);
            DescribedPredicate<JavaClass> predicate = springComponent();
            assertThat(predicate).accepts(componentClass);
        }

        @Test
        void accepts_class_annotated_with_Configuration() {
            JavaClass configurationClass = importClass(ConfigurationWithoutDependency.class);
            DescribedPredicate<JavaClass> predicate = springComponent();
            assertThat(predicate).accepts(configurationClass);
        }

        @Test
        void accepts_class_annotated_with_Controller() {
            JavaClass controllerClass = importClass(ControllerWithoutDependency.class);
            DescribedPredicate<JavaClass> predicate = springComponent();
            assertThat(predicate).accepts(controllerClass);
        }

        @Test
        void accepts_class_annotated_with_Service() {
            JavaClass serviceClass = importClass(ServiceWithoutDependency.class);
            DescribedPredicate<JavaClass> predicate = springComponent();
            assertThat(predicate).accepts(serviceClass);
        }

        @Test
        void accepts_class_annotated_with_Repository() {
            JavaClass repositoryClass = importClass(RepositoryWithoutDependency.class);
            DescribedPredicate<JavaClass> predicate = springComponent();
            assertThat(predicate).accepts(repositoryClass);
        }

        @Test
        void accepts_class_implementing_Spring_Data_Repository() {
            JavaClass repositoryClass = importClass(SpringDataRepositoryWithoutDependency.class);
            DescribedPredicate<JavaClass> predicate = springComponent();
            assertThat(predicate).accepts(repositoryClass);
        }

        @Test
        void rejects_class_annotated_with_NoRepositoryBean() {
            JavaClass noRepositoryBeanClass = importClass(CrudRepository.class);
            DescribedPredicate<JavaClass> predicate = springComponent();
            assertThat(predicate).rejects(noRepositoryBeanClass);
        }
    }

    @Nested
    class Predicate_springController {

        @Test
        void provides_a_description() {
            DescribedPredicate<JavaClass> predicate = springController();
            assertThat(predicate.getDescription()).isEqualTo("Spring controller");
        }

        @Test
        void rejects_class_annotated_with_Component() {
            JavaClass componentClass = importClass(ComponentWithoutDependency.class);
            DescribedPredicate<JavaClass> predicate = springController();
            assertThat(predicate).rejects(componentClass);
        }

        @Test
        void rejects_class_annotated_with_Configuration() {
            JavaClass configurationClass = importClass(ConfigurationWithoutDependency.class);
            DescribedPredicate<JavaClass> predicate = springController();
            assertThat(predicate).rejects(configurationClass);
        }

        @Test
        void accepts_class_annotated_with_Controller() {
            JavaClass controllerClass = importClass(ControllerWithoutDependency.class);
            DescribedPredicate<JavaClass> predicate = springController();
            assertThat(predicate).accepts(controllerClass);
        }
    }

    @Nested
    class Predicate_springService {

        @Test
        void provides_a_description() {
            DescribedPredicate<JavaClass> predicate = springService();
            assertThat(predicate.getDescription()).isEqualTo("Spring service");
        }

        @Test
        void rejects_class_annotated_with_Component() {
            JavaClass componentClass = importClass(ComponentWithoutDependency.class);
            DescribedPredicate<JavaClass> predicate = springService();
            assertThat(predicate).rejects(componentClass);
        }

        @Test
        void rejects_class_annotated_with_Controller() {
            JavaClass controllerClass = importClass(ControllerWithoutDependency.class);
            DescribedPredicate<JavaClass> predicate = springService();
            assertThat(predicate).rejects(controllerClass);
        }

        @Test
        void accepts_class_annotated_with_Service() {
            JavaClass serviceClass = importClass(ServiceWithoutDependency.class);
            DescribedPredicate<JavaClass> predicate = springService();
            assertThat(predicate).accepts(serviceClass);
        }
    }

    @Nested
    class Predicate_springRepository {

        @Test
        void provides_a_description() {
            DescribedPredicate<JavaClass> predicate = springRepository();
            assertThat(predicate.getDescription()).isEqualTo("Spring repository");
        }

        @Test
        void rejects_class_annotated_with_Component() {
            JavaClass componentClass = importClass(ComponentWithoutDependency.class);
            DescribedPredicate<JavaClass> predicate = springRepository();
            assertThat(predicate).rejects(componentClass);
        }

        @Test
        void rejects_class_annotated_with_Service() {
            JavaClass serviceClass = importClass(ServiceWithoutDependency.class);
            DescribedPredicate<JavaClass> predicate = springRepository();
            assertThat(predicate).rejects(serviceClass);
        }

        @Test
        void accepts_class_annotated_with_Repository() {
            JavaClass repositoryClass = importClass(RepositoryWithoutDependency.class);
            DescribedPredicate<JavaClass> predicate = springRepository();
            assertThat(predicate).accepts(repositoryClass);
        }

        @Test
        void accepts_class_implementing_Spring_Data_Repository() {
            JavaClass repositoryClass = importClass(SpringDataRepositoryWithoutDependency.class);
            DescribedPredicate<JavaClass> predicate = springRepository();
            assertThat(predicate).accepts(repositoryClass);
        }

        @Test
        void rejects_class_annotated_with_NoRepositoryBean() {
            JavaClass noRepositoryBeanClass = importClass(CrudRepository.class);
            DescribedPredicate<JavaClass> predicate = springRepository();
            assertThat(predicate).rejects(noRepositoryBeanClass);
        }
    }

    @Nested
    class Predicate_springConfiguration {

        @Test
        void provides_a_description() {
            DescribedPredicate<JavaClass> predicate = springController();
            assertThat(predicate.getDescription()).isEqualTo("Spring controller");
        }

        @Test
        void rejects_class_annotated_with_Component() {
            JavaClass componentClass = importClass(ComponentWithoutDependency.class);
            DescribedPredicate<JavaClass> predicate = springConfiguration();
            assertThat(predicate).rejects(componentClass);
        }

        @Test
        void accepts_class_annotated_with_Configuration() {
            JavaClass configurationClass = importClass(ConfigurationWithoutDependency.class);
            DescribedPredicate<JavaClass> predicate = springConfiguration();
            assertThat(predicate).accepts(configurationClass);
        }

        @Test
        void rejects_class_annotated_with_Controller() {
            JavaClass controllerClass = importClass(ControllerWithoutDependency.class);
            DescribedPredicate<JavaClass> predicate = springConfiguration();
            assertThat(predicate).rejects(controllerClass);
        }
    }
}
