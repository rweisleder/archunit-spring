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

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.AccessTarget;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaCodeUnit;
import com.tngtech.archunit.core.domain.JavaConstructor;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.domain.JavaMember;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaParameter;
import com.tngtech.archunit.core.domain.JavaStaticInitializer;
import com.tngtech.archunit.core.domain.properties.CanBeAnnotated;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ReflectionUtils;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import static com.tngtech.archunit.base.DescribedPredicate.describe;
import static com.tngtech.archunit.core.domain.Formatters.ensureSimpleName;
import static de.rweisleder.archunit.spring.internal.InternalUtils.isSpringFramework6;
import static java.util.Collections.emptyList;

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
public final class SpringAnnotationPredicates {

    private static final MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory();

    private SpringAnnotationPredicates() {
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
        return springAnnotatedWith(describe("@" + annotationType.getSimpleName(), mergedAnnotations -> {
            return mergedAnnotations.isPresent(annotationType);
        }));
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
     * springAnnotatedWith("org.springframework.web.bind.annotation.RestController")
     * springAnnotatedWith("org.springframework.stereotype.Controller")
     * springAnnotatedWith("org.springframework.stereotype.Component")
     * springAnnotatedWith("org.springframework.web.bind.annotation.ResponseBody")
     *
     * // does not match the class:
     * springAnnotatedWith("org.springframework.stereotype.Service")
     * }</pre>
     *
     * @param annotationTypeName the fully qualified class name of the annotation type to check
     * @see MergedAnnotations#isPresent(String)
     * @see CanBeAnnotated.Predicates#annotatedWith(String)
     * @see CanBeAnnotated.Predicates#metaAnnotatedWith(String)
     */
    public static DescribedPredicate<CanBeAnnotated> springAnnotatedWith(String annotationTypeName) {
        return springAnnotatedWith(describe("@" + ensureSimpleName(annotationTypeName), mergedAnnotations -> {
            return mergedAnnotations.isPresent(annotationTypeName);
        }));
    }

    /**
     * Returns a predicate that matches elements that are directly or meta-annotated with the given annotation type
     * matching the given predicate.
     * <p>
     * As an example:
     * <pre>{@code
     * @RestController("demo")
     * class DemoRestController {
     * }
     *
     * // matches the class:
     * springAnnotatedWith(RestController.class, describe("@RestController('demo')", restController -> restController.value().equals("demo"))
     * springAnnotatedWith(Controller.class, describe("@Controller('demo')", controller -> controller.value().equals("demo"))
     * springAnnotatedWith(Component.class, describe("@Component('demo')", component -> component.value().equals("demo"))
     *
     * // does not match the class:
     * springAnnotatedWith(RestController.class, describe("@RestController('demoRestController')", restController -> restController.value().equals("demoRestController"))
     * springAnnotatedWith(Service.class, describe("@Service('demo')", service -> service.value().equals("demo"))
     * }</pre>
     *
     * @see CanBeAnnotated.Predicates#annotatedWith(DescribedPredicate)
     * @see CanBeAnnotated.Predicates#metaAnnotatedWith(DescribedPredicate)
     */
    public static <T extends Annotation> DescribedPredicate<CanBeAnnotated> springAnnotatedWith(Class<T> annotationType, DescribedPredicate<T> predicate) {
        return springAnnotatedWith(describe(predicate.getDescription(), mergedAnnotations -> {
            MergedAnnotation<T> mergedAnnotation = mergedAnnotations.get(annotationType);
            if (!mergedAnnotation.isPresent()) {
                return false;
            }

            T synthesizedAnnotation = mergedAnnotation.synthesize();
            return predicate.test(synthesizedAnnotation);
        }));
    }

    /**
     * Returns a predicate that matches elements that are directly or meta-annotated with annotations matching the
     * given predicate.
     * <p>
     * As an example:
     * <pre>{@code
     * @RestController
     * class DemoRestController {
     * }
     *
     * // matches the class:
     * springAnnotatedWith(describe("@RestController", annotations -> annotations.isPresent(RestController.class)))
     * springAnnotatedWith(describe("@Controller", annotations -> annotations.isPresent(Controller.class)))
     * springAnnotatedWith(describe("@Component", annotations -> annotations.isPresent(Component.class)))
     *
     * // does not match the class:
     * springAnnotatedWith(describe("@Controller", annotations -> annotations.isDirectlyPresent(Controller.class)))
     * springAnnotatedWith(describe("@Service", annotations -> annotations.isPresent(Service.class)))
     * }</pre>
     *
     * @see CanBeAnnotated.Predicates#annotatedWith(DescribedPredicate)
     * @see CanBeAnnotated.Predicates#metaAnnotatedWith(DescribedPredicate)
     */
    public static DescribedPredicate<CanBeAnnotated> springAnnotatedWith(DescribedPredicate<MergedAnnotations> predicate) {
        return describe("annotated with " + predicate.getDescription(), annotated -> {
            MergedAnnotations mergedAnnotations = mergedAnnotations(annotated);
            if (mergedAnnotations == null) {
                return false;
            }

            return predicate.test(mergedAnnotations);
        });
    }

    private static MergedAnnotations mergedAnnotations(CanBeAnnotated annotated) {
        try {
            AnnotatedElement annotatedElement = asAnnotatedElement(annotated);
            if (annotatedElement == null) {
                return null;
            }
            return MergedAnnotations.from(annotatedElement);
        } catch (EvaluationException e) {
            throw e;
        } catch (Exception | NoClassDefFoundError ignored) {
        }

        return mergedAnnotationsFromMetadata(annotated);
    }

    private static AnnotatedElement asAnnotatedElement(CanBeAnnotated annotated) {
        if (annotated instanceof JavaClass) {
            return ((JavaClass) annotated).reflect();
        }

        if (annotated instanceof JavaStaticInitializer) {
            // Contrary to the JLS, ArchUnit considers static initializers to have annotations.
            return null;
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

        if (annotated instanceof AccessTarget) {
            Optional<? extends JavaMember> accessedMember = ((AccessTarget) annotated).resolveMember();
            if (!accessedMember.isPresent()) {
                // If the accessedMember is not present, this indicates that the owning class of accessedMember was not imported.
                // For strategies on handling such cases of missing classes, refer to the "Dealing with Missing Classes" section in the ArchUnit documentation.
                // In such cases, the method AccessTarget#isAnnotatedWith(..) will return false.
                return null;
            }
            return asAnnotatedElement(accessedMember.get());
        }

        throw new EvaluationException(annotated + " cannot be converted to " + AnnotatedElement.class);
    }

    private static MergedAnnotations mergedAnnotationsFromMetadata(CanBeAnnotated annotated) {
        if (annotated instanceof JavaClass) {
            AnnotationMetadata annotationMetadata = readAnnotationMetadata((JavaClass) annotated);
            if (annotationMetadata == null) {
                return null;
            }

            return annotationMetadata.getAnnotations();
        }

        if (annotated instanceof JavaMethod) {
            AnnotationMetadata annotationMetadata = readAnnotationMetadata(((JavaMethod) annotated).getOwner());
            if (annotationMetadata == null) {
                return null;
            }

            MethodMetadata methodMetadata = findMethodMetadata(annotationMetadata, (JavaMethod) annotated);
            if (methodMetadata == null) {
                return null;
            }

            return methodMetadata.getAnnotations();
        }

        return null;
    }

    private static AnnotationMetadata readAnnotationMetadata(JavaClass javaClass) {
        try {
            MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(javaClass.getName());
            return metadataReader.getAnnotationMetadata();
        } catch (IOException ignored) {
            return null;
        }
    }

    private static MethodMetadata findMethodMetadata(AnnotationMetadata annotationMetadata, JavaMethod javaMethod) {
        Collection<MethodMetadata> methodMetadataSet = emptyList();
        if (isSpringFramework6()) {
            methodMetadataSet = annotationMetadata.getDeclaredMethods();
        } else {
            Field annotatedMethodsField = ReflectionUtils.findField(annotationMetadata.getClass(), "annotatedMethods");
            if (annotatedMethodsField != null) {
                ReflectionUtils.makeAccessible(annotatedMethodsField);
                MethodMetadata[] annotatedMethods = (MethodMetadata[]) ReflectionUtils.getField(annotatedMethodsField, annotationMetadata);
                if (annotatedMethods != null) {
                    methodMetadataSet = Arrays.asList(annotatedMethods);
                }
            }
        }

        MethodMetadata matchingMethodMetadata = null;
        for (MethodMetadata methodMetadata : methodMetadataSet) {
            if (methodMetadata.getMethodName().equals(javaMethod.getName())) {
                if (matchingMethodMetadata == null) {
                    matchingMethodMetadata = methodMetadata;
                } else {
                    // As we can only compare by name and not by signature,
                    // we cannot identify overloaded methods at this point.
                    return null;
                }
            }
        }

        return matchingMethodMetadata;
    }

    public static class EvaluationException extends RuntimeException {

        EvaluationException(String message) {
            super(message);
        }
    }
}
