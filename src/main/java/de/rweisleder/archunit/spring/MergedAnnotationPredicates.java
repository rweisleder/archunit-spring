package de.rweisleder.archunit.spring;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaCodeUnit;
import com.tngtech.archunit.core.domain.JavaConstructor;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaParameter;
import com.tngtech.archunit.core.domain.properties.CanBeAnnotated;
import org.springframework.core.annotation.MergedAnnotations;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static com.tngtech.archunit.base.DescribedPredicate.describe;

/**
 * Collection of {@link DescribedPredicate predicates} that can be used with ArchUnit to check elements for the
 * presence of annotations according to the
 * <a href="https://github.com/spring-projects/spring-framework/wiki/Spring-Annotation-Programming-Model">Spring Annotation Model</a>.
 * Essentially, it is about composable annotations using meta-annotations, and aliases for annotation attributes.
 *
 * @author Roland Weisleder
 * @see MergedAnnotations
 * @see CanBeAnnotated.Predicates
 */
public final class MergedAnnotationPredicates {

    private MergedAnnotationPredicates() {
    }

    /**
     * Returns a predicate that matches elements that are directly or meta-annotated with the given annotation type.
     * <p>
     * As an example:
     * <pre>{@code
     * @RestController
     * class DemoRestController {
     * }
     *
     * // matches the class:
     * springAnnotatedWith(RestController.class)
     * springAnnotatedWith(Controller.class)
     * springAnnotatedWith(Component.class)
     * springAnnotatedWith(ResponseBody.class)
     *
     * // does not match the class:
     * springAnnotatedWith(Service.class)
     * }</pre>
     *
     * @see MergedAnnotations#isPresent(Class)
     * @see CanBeAnnotated.Predicates#annotatedWith(Class)
     * @see CanBeAnnotated.Predicates#metaAnnotatedWith(Class)
     */
    public static DescribedPredicate<CanBeAnnotated> springAnnotatedWith(Class<? extends Annotation> annotationType) {
        return describe("annotated with @" + annotationType.getSimpleName(), annotated -> {
            MergedAnnotations mergedAnnotations = getMergedAnnotations(annotated);
            return mergedAnnotations.isPresent(annotationType);
        });
    }

    private static MergedAnnotations getMergedAnnotations(CanBeAnnotated annotated) {
        AnnotatedElement annotatedElement = asAnnotatedElement(annotated);
        return MergedAnnotations.from(annotatedElement);
    }

    private static AnnotatedElement asAnnotatedElement(CanBeAnnotated annotated) {
        if (annotated instanceof JavaClass) {
            return ((JavaClass) annotated).reflect();
        }

        if (annotated instanceof JavaField) {
            return ((JavaField) annotated).reflect();
        }

        if (annotated instanceof JavaConstructor) {
            return ((JavaConstructor) annotated).reflect();
        }

        if (annotated instanceof JavaMethod) {
            return ((JavaMethod) annotated).reflect();
        }

        if (annotated instanceof JavaParameter) {
            JavaCodeUnit owner = ((JavaParameter) annotated).getOwner();
            int index = ((JavaParameter) annotated).getIndex();

            if (owner instanceof JavaConstructor) {
                Constructor<?> constructor = ((JavaConstructor) owner).reflect();
                return constructor.getParameters()[index];
            }

            if (owner instanceof JavaMethod) {
                Method method = ((JavaMethod) owner).reflect();
                return method.getParameters()[index];
            }
        }

        throw new RuntimeException(annotated + " cannot be converted to " + AnnotatedElement.class);
    }
}
