package de.rweisleder.archunit.spring;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaConstructor;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaParameter;
import com.tngtech.archunit.core.domain.properties.CanBeAnnotated;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.tngtech.archunit.base.DescribedPredicate.describe;
import static de.rweisleder.archunit.spring.MergedAnnotationPredicates.springAnnotatedWith;
import static de.rweisleder.archunit.spring.TestUtils.importClass;
import static org.assertj.core.api.Assertions.assertThat;

class MergedAnnotationPredicatesTest {

    @Nested
    class Predicate_springAnnotatedWith_with_Class {

        @Test
        void provides_a_description() {
            DescribedPredicate<CanBeAnnotated> predicate = springAnnotatedWith(Controller.class);
            assertThat(predicate.getDescription()).isEqualTo("annotated with @Controller");
        }

        @Test
        void accepts_class_with_annotation_directly_present_on_class() {
            JavaClass controllerClass = importClass(ControllerClass.class);
            DescribedPredicate<CanBeAnnotated> predicate = springAnnotatedWith(Controller.class);
            assertThat(predicate).accepts(controllerClass);
        }

        @Test
        void accepts_class_with_annotation_meta_present_on_class() {
            JavaClass controllerClass = importClass(ControllerClass.class);
            DescribedPredicate<CanBeAnnotated> predicate = springAnnotatedWith(Component.class);
            assertThat(predicate).accepts(controllerClass);
        }

        @Test
        void rejects_class_with_annotation_not_present_on_class() {
            JavaClass controllerClass = importClass(ControllerClass.class);
            DescribedPredicate<CanBeAnnotated> predicate = springAnnotatedWith(Service.class);
            assertThat(predicate).rejects(controllerClass);
        }

        @Test
        void accepts_field_with_annotation_directly_present_on_field() {
            JavaClass classWithFieldAnnotation = importClass(ClassWithFieldAnnotation.class);
            JavaField optionalAutowiredField = classWithFieldAnnotation.getField("o");
            DescribedPredicate<CanBeAnnotated> predicate = springAnnotatedWith(OptionalAutowired.class);
            assertThat(predicate).accepts(optionalAutowiredField);
        }

        @Test
        void accepts_field_with_annotation_meta_present_on_field() {
            JavaClass classWithFieldAnnotation = importClass(ClassWithFieldAnnotation.class);
            JavaField optionalAutowiredField = classWithFieldAnnotation.getField("o");
            DescribedPredicate<CanBeAnnotated> predicate = springAnnotatedWith(Autowired.class);
            assertThat(predicate).accepts(optionalAutowiredField);
        }

        @Test
        void rejects_field_with_annotation_not_present_on_field() {
            JavaClass classWithFieldAnnotation = importClass(ClassWithFieldAnnotation.class);
            JavaField optionalAutowiredField = classWithFieldAnnotation.getField("o");
            DescribedPredicate<CanBeAnnotated> predicate = springAnnotatedWith(Lazy.class);
            assertThat(predicate).rejects(optionalAutowiredField);
        }

        @Test
        void accepts_constructor_with_annotation_directly_present_on_constructor() {
            JavaClass classWithConstructorAnnotation = importClass(ClassWithConstructorAnnotation.class);
            JavaConstructor optionalAutowiredConstructor = classWithConstructorAnnotation.getConstructor();
            DescribedPredicate<CanBeAnnotated> predicate = springAnnotatedWith(OptionalAutowired.class);
            assertThat(predicate).accepts(optionalAutowiredConstructor);
        }

        @Test
        void accepts_constructor_with_annotation_meta_present_on_constructor() {
            JavaClass classWithConstructorAnnotation = importClass(ClassWithConstructorAnnotation.class);
            JavaConstructor optionalAutowiredConstructor = classWithConstructorAnnotation.getConstructor();
            DescribedPredicate<CanBeAnnotated> predicate = springAnnotatedWith(Autowired.class);
            assertThat(predicate).accepts(optionalAutowiredConstructor);
        }

        @Test
        void rejects_constructor_with_annotation_not_present_on_constructor() {
            JavaClass classWithConstructorAnnotation = importClass(ClassWithConstructorAnnotation.class);
            JavaConstructor optionalAutowiredConstructor = classWithConstructorAnnotation.getConstructor();
            DescribedPredicate<CanBeAnnotated> predicate = springAnnotatedWith(Lazy.class);
            assertThat(predicate).rejects(optionalAutowiredConstructor);
        }

        @Test
        void accepts_method_with_annotation_directly_present_on_method() {
            JavaClass classWithMethodAnnotation = importClass(ClassWithMethodAnnotation.class);
            JavaMethod getMappingMethod = classWithMethodAnnotation.getMethod("get");
            DescribedPredicate<CanBeAnnotated> predicate = springAnnotatedWith(GetMapping.class);
            assertThat(predicate).accepts(getMappingMethod);
        }

        @Test
        void accepts_method_with_annotation_meta_present_on_method() {
            JavaClass classWithMethodAnnotation = importClass(ClassWithMethodAnnotation.class);
            JavaMethod getMappingMethod = classWithMethodAnnotation.getMethod("get");
            DescribedPredicate<CanBeAnnotated> predicate = springAnnotatedWith(RequestMapping.class);
            assertThat(predicate).accepts(getMappingMethod);
        }

        @Test
        void rejects_method_with_annotation_not_present_on_method() {
            JavaClass classWithMethodAnnotation = importClass(ClassWithMethodAnnotation.class);
            JavaMethod getMappingMethod = classWithMethodAnnotation.getMethod("get");
            DescribedPredicate<CanBeAnnotated> predicate = springAnnotatedWith(PostMapping.class);
            assertThat(predicate).rejects(getMappingMethod);
        }

        @Test
        void accepts_constructor_parameter_with_annotation_directly_present_on_parameter() {
            JavaClass classWithConstructorParameterAnnotation = importClass(ClassWithConstructorParameterAnnotation.class);
            JavaParameter optionalAutowiredParameter = classWithConstructorParameterAnnotation.getConstructor(Object.class).getParameters().get(0);
            DescribedPredicate<CanBeAnnotated> predicate = springAnnotatedWith(OptionalAutowired.class);
            assertThat(predicate).accepts(optionalAutowiredParameter);
        }

        @Test
        void accepts_constructor_parameter_with_annotation_meta_present_on_parameter() {
            JavaClass classWithConstructorParameterAnnotation = importClass(ClassWithConstructorParameterAnnotation.class);
            JavaParameter optionalAutowiredParameter = classWithConstructorParameterAnnotation.getConstructor(Object.class).getParameters().get(0);
            DescribedPredicate<CanBeAnnotated> predicate = springAnnotatedWith(Autowired.class);
            assertThat(predicate).accepts(optionalAutowiredParameter);
        }

        @Test
        void rejects_constructor_parameter_with_annotation_not_present_on_parameter() {
            JavaClass classWithConstructorParameterAnnotation = importClass(ClassWithConstructorParameterAnnotation.class);
            JavaParameter optionalAutowiredParameter = classWithConstructorParameterAnnotation.getConstructor(Object.class).getParameters().get(0);
            DescribedPredicate<CanBeAnnotated> predicate = springAnnotatedWith(Lazy.class);
            assertThat(predicate).rejects(optionalAutowiredParameter);
        }

        @Test
        void accepts_method_parameter_with_annotation_directly_present_on_parameter() {
            JavaClass classWithMethodParameterAnnotation = importClass(ClassWithMethodParameterAnnotation.class);
            JavaParameter optionalAutowiredParameter = classWithMethodParameterAnnotation.getMethod("get", Object.class).getParameters().get(0);
            DescribedPredicate<CanBeAnnotated> predicate = springAnnotatedWith(OptionalAutowired.class);
            assertThat(predicate).accepts(optionalAutowiredParameter);
        }

        @Test
        void accepts_method_parameter_with_annotation_meta_present_on_parameter() {
            JavaClass classWithMethodParameterAnnotation = importClass(ClassWithMethodParameterAnnotation.class);
            JavaParameter optionalAutowiredParameter = classWithMethodParameterAnnotation.getMethod("get", Object.class).getParameters().get(0);
            DescribedPredicate<CanBeAnnotated> predicate = springAnnotatedWith(Autowired.class);
            assertThat(predicate).accepts(optionalAutowiredParameter);
        }

        @Test
        void rejects_method_parameter_with_annotation_not_present_on_parameter() {
            JavaClass classWithMethodParameterAnnotation = importClass(ClassWithMethodParameterAnnotation.class);
            JavaParameter optionalAutowiredParameter = classWithMethodParameterAnnotation.getMethod("get", Object.class).getParameters().get(0);
            DescribedPredicate<CanBeAnnotated> predicate = springAnnotatedWith(Lazy.class);
            assertThat(predicate).rejects(optionalAutowiredParameter);
        }
    }

    @Nested
    class Predicate_springAnnotatedWith_with_String {

        @Test
        void provides_a_description() {
            DescribedPredicate<CanBeAnnotated> predicate = springAnnotatedWith("org.springframework.stereotype.Controller");
            assertThat(predicate.getDescription()).isEqualTo("annotated with @Controller");
        }

        @Test
        void accepts_matching_class() {
            JavaClass controllerClass = importClass(ControllerClass.class);
            DescribedPredicate<CanBeAnnotated> predicate = springAnnotatedWith("org.springframework.stereotype.Controller");
            assertThat(predicate).accepts(controllerClass);
        }

        @Test
        void rejects_non_matching_class() {
            JavaClass controllerClass = importClass(ControllerClass.class);
            DescribedPredicate<CanBeAnnotated> predicate = springAnnotatedWith("org.springframework.stereotype.Service");
            assertThat(predicate).rejects(controllerClass);
        }
    }

    @Nested
    class Predicate_springAnnotatedWith_with_Class_and_DescribedPredicate {

        @Test
        void provides_a_description() {
            DescribedPredicate<CanBeAnnotated> predicate = springAnnotatedWith(Controller.class, describe("@Controller(value='')",
                    (Controller controller) -> controller.value().isEmpty()
            ));
            assertThat(predicate.getDescription()).isEqualTo("annotated with @Controller(value='')");
        }

        @Test
        void accepts_class_with_matching_annotation_and_matching_predicate() {
            JavaClass controllerClass = importClass(ControllerClass.class);
            DescribedPredicate<CanBeAnnotated> predicate = springAnnotatedWith(Controller.class, describe("@Controller(value='')",
                    (Controller controller) -> controller.value().isEmpty()
            ));
            assertThat(predicate).accepts(controllerClass);
        }

        @Test
        void rejects_class_with_matching_annotation_and_non_matching_predicate() {
            JavaClass controllerClass = importClass(ControllerClass.class);
            DescribedPredicate<CanBeAnnotated> predicate = springAnnotatedWith(Controller.class, describe("@Controller(value='hello')",
                    (Controller controller) -> controller.value().equals("hello")
            ));
            assertThat(predicate).rejects(controllerClass);
        }

        @Test
        void rejects_class_with_non_matching_annotation() {
            JavaClass controllerClass = importClass(ControllerClass.class);
            DescribedPredicate<CanBeAnnotated> predicate = springAnnotatedWith(Service.class, describe("@Service(value='')",
                    (Service service) -> service.value().isEmpty()
            ));
            assertThat(predicate).rejects(controllerClass);
        }
    }

    @Nested
    class Predicate_springAnnotatedWith_with_DescribedPredicate {

        @Test
        void provides_a_description() {
            DescribedPredicate<CanBeAnnotated> predicate = springAnnotatedWith(describe("@Controller",
                    (MergedAnnotations annotations) -> annotations.isPresent(Controller.class))
            );
            assertThat(predicate.getDescription()).isEqualTo("annotated with @Controller");
        }

        @Test
        void accepts_matching_class() {
            JavaClass controllerClass = importClass(ControllerClass.class);
            DescribedPredicate<CanBeAnnotated> predicate = springAnnotatedWith(describe("@Controller",
                    (MergedAnnotations annotations) -> annotations.isPresent(Controller.class))
            );
            assertThat(predicate).accepts(controllerClass);
        }

        @Test
        void rejects_non_matching_class() {
            JavaClass controllerClass = importClass(ControllerClass.class);
            DescribedPredicate<CanBeAnnotated> predicate = springAnnotatedWith(describe("@Service",
                    (MergedAnnotations annotations) -> annotations.isPresent(Service.class))
            );
            assertThat(predicate).rejects(controllerClass);
        }
    }

    @Controller
    static class ControllerClass {
    }

    static class ClassWithFieldAnnotation {

        @OptionalAutowired
        @SuppressWarnings("unused")
        Object o;
    }

    static class ClassWithConstructorAnnotation {

        @OptionalAutowired
        ClassWithConstructorAnnotation() {
        }
    }

    static class ClassWithMethodAnnotation {

        @GetMapping
        @SuppressWarnings("unused")
        void get() {
        }
    }

    static class ClassWithConstructorParameterAnnotation {

        @SuppressWarnings("unused")
        ClassWithConstructorParameterAnnotation(@OptionalAutowired Object o) {
        }
    }

    static class ClassWithMethodParameterAnnotation {

        @SuppressWarnings("unused")
        void get(@OptionalAutowired Object o) {
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Autowired(required = false)
    @interface OptionalAutowired {
    }
}
