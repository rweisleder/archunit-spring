/*
 * #%L
 * ArchUnit Spring Integration
 * %%
 * Copyright (C) 2026 Hasan Kara
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
package de.rweisleder.archunit.spring.boot;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.domain.properties.CanBeAnnotated;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import org.springframework.core.annotation.MergedAnnotations;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.tngtech.archunit.base.DescribedPredicate.describe;
import static com.tngtech.archunit.lang.ConditionEvent.createMessage;
import static com.tngtech.archunit.lang.SimpleConditionEvent.satisfied;
import static com.tngtech.archunit.lang.SimpleConditionEvent.violated;
import static com.tngtech.archunit.lang.conditions.ArchConditions.be;
import static com.tngtech.archunit.lang.conditions.ArchPredicates.are;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static de.rweisleder.archunit.spring.SpringAnnotationPredicates.springAnnotatedWith;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

/**
 * Collection of {@link ArchRule rules} that can be used to check the validation of classes
 * annotated with {@code @ConfigurationProperties}.
 * <p>
 * Spring Boot validates a configuration properties class only if it is annotated with {@code @Validated},
 * and Bean Validation only descends into a nested object if the corresponding property is annotated with {@code @Valid}.
 * Missing one of these annotations means that the affected constraints are silently ignored,
 * so that the application starts with an invalid configuration.
 *
 * @author Hasan Kara
 */
public final class SpringConfigurationPropertiesRules {

    private static final DescribedPredicate<CanBeAnnotated> annotatedWithConfigurationProperties = springAnnotatedWith("org.springframework.boot.context.properties.ConfigurationProperties");
    private static final DescribedPredicate<CanBeAnnotated> annotatedWithValidated = springAnnotatedWith("org.springframework.validation.annotation.Validated");
    private static final DescribedPredicate<CanBeAnnotated> annotatedWithConstraint = springAnnotatedWith("jakarta.validation.Constraint");
    private static final DescribedPredicate<CanBeAnnotated> annotatedWithValid = springAnnotatedWith("jakarta.validation.Valid");

    private SpringConfigurationPropertiesRules() {
    }

    /**
     * A rule that checks that classes annotated with {@code @ConfigurationProperties} are also annotated with
     * {@code @Validated} if they declare Bean Validation constraints, either directly or in a nested type.
     * <p>
     * Constraint annotations are detected by their meta-annotation {@code @Constraint},
     * so that custom constraint annotations are considered as well.
     * This includes constraints on the elements of a generic type, like {@code List<@NotBlank String>}.
     *
     * <p>
     * Produces violations for:
     * <pre>{@code
     * @ConfigurationProperties("app") // the constraint below is never validated
     * record AppProperties(@NotBlank String name) {
     * }
     * }</pre>
     *
     * <p>
     * Passes for:
     * <pre>{@code
     * @ConfigurationProperties("app")
     * @Validated
     * record AppProperties(@NotBlank String name) {
     * }
     *
     * @ConfigurationProperties("app") // no constraints, so nothing needs to be validated
     * record AppProperties(String name) {
     * }
     * }</pre>
     */
    public static final ArchRule ValidatedIsPresentIfConstraintsExist = classes()
            .that(are(annotatedWithConfigurationProperties))
            .and(declareBeanValidationConstraints())
            .should(be(annotatedWithValidated))
            .because("Spring Boot only validates configuration properties classes that are annotated with @Validated")
            .allowEmptyShould(true);

    /**
     * A rule that checks that classes annotated with {@code @ConfigurationProperties} annotate all properties
     * with {@code @Valid} whose type declares Bean Validation constraints.
     * The nested types are checked recursively, so that every property leading to a constraint needs to be annotated.
     * <p>
     * The elements of generic types and arrays are checked as well, so a property of type {@code List<Database>}
     * needs to be declared as either {@code @Valid List<Database>} or {@code List<@Valid Database>}.
     *
     * <p>
     * Produces violations for:
     * <pre>{@code
     * @ConfigurationProperties("app")
     * @Validated
     * record AppProperties(Database database) { // the constraints of Database are never validated
     * }
     *
     * record Database(@NotBlank String host) {
     * }
     * }</pre>
     *
     * <p>
     * Passes for:
     * <pre>{@code
     * @ConfigurationProperties("app")
     * @Validated
     * record AppProperties(@Valid Database database) {
     * }
     *
     * record Database(@NotBlank String host) {
     * }
     * }</pre>
     *
     * @see #cascadeValidationToNestedTypes()
     */
    public static final ArchRule ValidationIsCascadedToNestedTypes = classes()
            .that(are(annotatedWithConfigurationProperties))
            .should(cascadeValidationToNestedTypes())
            .because("Bean Validation only validates nested objects if the corresponding property is annotated with @Valid")
            .allowEmptyShould(true);

    /**
     * A condition that checks that the given classes annotate all properties with {@code @Valid}
     * whose type declares Bean Validation constraints.
     * The nested types are checked recursively, so that every property leading to a constraint needs to be annotated.
     * <p>
     * Note that only the root class needs to be annotated with {@code @Validated},
     * the nested types are reached via {@code @Valid}.
     *
     * @see #ValidationIsCascadedToNestedTypes
     */
    public static ArchCondition<JavaClass> cascadeValidationToNestedTypes() {
        return new ArchCondition<JavaClass>("cascade validation to nested types that declare Bean Validation constraints") {

            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                checkProperties(javaClass, new HashSet<>(), events);
            }

            private void checkProperties(JavaClass javaClass, Set<JavaClass> checkedTypes, ConditionEvents events) {
                if (!checkedTypes.add(javaClass)) {
                    return;
                }

                for (JavaField property : propertiesOf(javaClass)) {
                    for (JavaClass propertyType : nestedTypesOf(property)) {
                        if (!requiresValidation(propertyType, new HashSet<>())) {
                            continue;
                        }

                        if (cascadesValidation(property, propertyType)) {
                            events.add(satisfied(property, createMessage(property, "is annotated with @Valid")));
                        } else {
                            events.add(violated(property, createMessage(property, "is not annotated with @Valid, so the Bean Validation constraints of " + propertyType.getName() + " are not validated")));
                        }

                        checkProperties(propertyType, checkedTypes, events);
                    }
                }
            }
        };
    }

    private static DescribedPredicate<JavaClass> declareBeanValidationConstraints() {
        return describe("declare Bean Validation constraints (directly or in a nested type)", javaClass -> requiresValidation(javaClass, new HashSet<>()));
    }

    /**
     * Checks whether the given type declares Bean Validation constraints itself
     * or whether one of its property types does so, no matter how deeply nested.
     */
    private static boolean requiresValidation(JavaClass javaClass, Set<JavaClass> checkedTypes) {
        if (!canDeclareConstraints(javaClass) || !checkedTypes.add(javaClass)) {
            return false;
        }

        if (declaresConstraints(javaClass)) {
            return true;
        }

        return propertiesOf(javaClass).stream()
                .flatMap(property -> nestedTypesOf(property).stream())
                .anyMatch(nestedType -> requiresValidation(nestedType, checkedTypes));
    }

    private static boolean canDeclareConstraints(JavaClass javaClass) {
        return !javaClass.isPrimitive() && !javaClass.isArray() && !javaClass.isEnum() && !javaClass.getPackageName().startsWith("java.");
    }

    /**
     * Returns all raw types involved in the declared type of a property, which includes
     * the type arguments of generic types and the component types of arrays.
     * For a property of type {@code Map<String, List<Database>>} these are
     * {@code Map}, {@code String}, {@code List} and {@code Database}.
     */
    private static Set<JavaClass> nestedTypesOf(JavaField property) {
        return property.getType().getAllInvolvedRawTypes();
    }

    /**
     * Bean Validation evaluates constraints on fields as well as on property accessors,
     * for records the constraints of a record component are propagated to both of them.
     * Constraints on the elements of a generic type, like {@code List<@NotBlank String>},
     * are only visible as type annotations.
     */
    private static boolean declaresConstraints(JavaClass javaClass) {
        return propertiesOf(javaClass).stream().anyMatch(SpringConfigurationPropertiesRules::declaresConstraints)
                || javaClass.getAllMethods().stream().anyMatch(annotatedWithConstraint);
    }

    private static boolean declaresConstraints(JavaField property) {
        return annotatedWithConstraint.test(property)
                || annotationsOfNestedTypes(property, nestedType -> true).anyMatch(annotation -> isAnnotation(annotation, "jakarta.validation.Constraint"));
    }

    private static boolean cascadesValidation(JavaField property, JavaClass propertyType) {
        if (annotatedWithValid.test(property) || accessorOf(property).map(annotatedWithValid::test).orElse(false)) {
            return true;
        }

        // Cascading into the elements of a generic type can also be declared as List<@Valid Database>.
        return annotationsOfNestedTypes(property, nestedType -> nestedType.getTypeName().equals(propertyType.getName()))
                .anyMatch(annotation -> isAnnotation(annotation, "jakarta.validation.Valid"));
    }

    /**
     * ArchUnit does not import type annotations, so the annotations on the type arguments of a property,
     * like {@code List<@Valid Database>}, are read via reflection.
     */
    private static Stream<Annotation> annotationsOfNestedTypes(JavaField property, Predicate<Type> typeFilter) {
        List<AnnotatedType> nestedTypes = new ArrayList<>();
        try {
            collectNestedTypes(property.reflect().getAnnotatedType(), nestedTypes);
        } catch (Exception | NoClassDefFoundError ignored) {
            nestedTypes = emptyList();
        }

        return nestedTypes.stream()
                .filter(nestedType -> typeFilter.test(nestedType.getType()))
                .flatMap(nestedType -> Arrays.stream(nestedType.getAnnotations()));
    }

    private static void collectNestedTypes(AnnotatedType annotatedType, List<AnnotatedType> collected) {
        if (annotatedType instanceof AnnotatedParameterizedType) {
            for (AnnotatedType typeArgument : ((AnnotatedParameterizedType) annotatedType).getAnnotatedActualTypeArguments()) {
                collected.add(typeArgument);
                collectNestedTypes(typeArgument, collected);
            }
        } else if (annotatedType instanceof AnnotatedArrayType) {
            AnnotatedType componentType = ((AnnotatedArrayType) annotatedType).getAnnotatedGenericComponentType();
            collected.add(componentType);
            collectNestedTypes(componentType, collected);
        }
    }

    private static boolean isAnnotation(Annotation annotation, String annotationTypeName) {
        Class<? extends Annotation> annotationType = annotation.annotationType();
        return annotationType.getName().equals(annotationTypeName) || MergedAnnotations.from(annotationType).isPresent(annotationTypeName);
    }

    private static List<JavaField> propertiesOf(JavaClass javaClass) {
        return javaClass.getAllFields().stream()
                .filter(field -> !field.getModifiers().contains(JavaModifier.STATIC))
                .filter(field -> !field.getModifiers().contains(JavaModifier.SYNTHETIC))
                .collect(toList());
    }

    private static Optional<JavaMethod> accessorOf(JavaField property) {
        JavaClass owner = property.getOwner();
        String name = property.getName();
        String suffix = Character.toUpperCase(name.charAt(0)) + name.substring(1);

        Optional<JavaMethod> accessor = owner.tryGetMethod(name);
        if (!accessor.isPresent()) {
            accessor = owner.tryGetMethod("get" + suffix);
        }
        if (!accessor.isPresent()) {
            accessor = owner.tryGetMethod("is" + suffix);
        }
        return accessor;
    }
}
