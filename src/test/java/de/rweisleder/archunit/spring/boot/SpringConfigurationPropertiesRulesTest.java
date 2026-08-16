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

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Map;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.assertThat;

class SpringConfigurationPropertiesRulesTest {

    @Constraint(validatedBy = {})
    @Target({FIELD, METHOD, PARAMETER})
    @Retention(RUNTIME)
    @interface UpperCase {

        String message() default "must be upper case";

        Class<?>[] groups() default {};

        Class<? extends Payload>[] payload() default {};
    }

    @Validated
    @Target(TYPE)
    @Retention(RUNTIME)
    @interface ValidatedProperties {
    }

    private static List<String> violations(ArchRule rule, Class<?>... classes) {
        return rule.evaluate(new ClassFileImporter().importClasses(classes)).getFailureReport().getDetails();
    }

    @Nested
    class Rule_ValidatedIsPresentIfConstraintsExist {

        @ConfigurationProperties("app")
        @Validated
        record ValidatedRecordProperties(@NotBlank String name) {
        }

        @ConfigurationProperties("app")
        record NotValidatedRecordProperties(@NotBlank String name) {
        }

        @ConfigurationProperties("app")
        record UnconstrainedRecordProperties(String name) {
        }

        @ConfigurationProperties("app")
        record CustomConstraintRecordProperties(@UpperCase String name) {
        }

        @ConfigurationProperties("app")
        @ValidatedProperties
        record MetaValidatedRecordProperties(@NotBlank String name) {
        }

        @ConfigurationProperties("app")
        static class NotValidatedClassProperties {

            @NotBlank
            private String name;

            public String getName() {
                return name;
            }
        }

        @ConfigurationProperties("app")
        static class GetterConstrainedClassProperties {

            private String name;

            @NotBlank
            public String getName() {
                return name;
            }
        }

        @ConfigurationProperties("app")
        record NestedConstraintRecordProperties(@Valid Database database) {
        }

        record Database(@NotBlank String host) {
        }

        record ConstrainedButNoConfigurationProperties(@NotBlank String name) {
        }

        @ConfigurationProperties("app")
        record GenericElementConstraintRecordProperties(List<@NotBlank String> names) {
        }

        @ConfigurationProperties("app")
        record UnconstrainedGenericRecordProperties(List<String> names) {
        }

        @Test
        void provides_a_description() {
            String description = SpringConfigurationPropertiesRules.ValidatedIsPresentIfConstraintsExist.getDescription();
            assertThat(description).isEqualTo("classes that are annotated with @ConfigurationProperties and declare Bean Validation constraints (directly or in a nested type) should be annotated with @Validated, because Spring Boot only validates configuration properties classes that are annotated with @Validated");
        }

        @Test
        void passes_for_record_annotated_with_Validated() {
            assertThat(violations(SpringConfigurationPropertiesRules.ValidatedIsPresentIfConstraintsExist, ValidatedRecordProperties.class)).isEmpty();
        }

        @Test
        void violates_for_record_not_annotated_with_Validated() {
            assertThat(violations(SpringConfigurationPropertiesRules.ValidatedIsPresentIfConstraintsExist, NotValidatedRecordProperties.class))
                    .singleElement().asString()
                    .startsWith("Class <" + NotValidatedRecordProperties.class.getName() + "> is not annotated with @Validated");
        }

        @Test
        void passes_for_record_without_constraints() {
            assertThat(violations(SpringConfigurationPropertiesRules.ValidatedIsPresentIfConstraintsExist, UnconstrainedRecordProperties.class)).isEmpty();
        }

        @Test
        void detects_custom_constraint_annotations() {
            assertThat(violations(SpringConfigurationPropertiesRules.ValidatedIsPresentIfConstraintsExist, CustomConstraintRecordProperties.class))
                    .singleElement().asString()
                    .contains(CustomConstraintRecordProperties.class.getName());
        }

        @Test
        void passes_for_record_meta_annotated_with_Validated() {
            assertThat(violations(SpringConfigurationPropertiesRules.ValidatedIsPresentIfConstraintsExist, MetaValidatedRecordProperties.class)).isEmpty();
        }

        @Test
        void violates_for_class_not_annotated_with_Validated() {
            assertThat(violations(SpringConfigurationPropertiesRules.ValidatedIsPresentIfConstraintsExist, NotValidatedClassProperties.class))
                    .singleElement().asString()
                    .contains(NotValidatedClassProperties.class.getName());
        }

        @Test
        void detects_constraints_declared_on_accessors() {
            assertThat(violations(SpringConfigurationPropertiesRules.ValidatedIsPresentIfConstraintsExist, GetterConstrainedClassProperties.class))
                    .singleElement().asString()
                    .contains(GetterConstrainedClassProperties.class.getName());
        }

        @Test
        void violates_if_only_a_nested_type_declares_constraints() {
            assertThat(violations(SpringConfigurationPropertiesRules.ValidatedIsPresentIfConstraintsExist, NestedConstraintRecordProperties.class, Database.class))
                    .singleElement().asString()
                    .contains(NestedConstraintRecordProperties.class.getName());
        }

        @Test
        void detects_constraints_on_elements_of_a_generic_type() {
            assertThat(violations(SpringConfigurationPropertiesRules.ValidatedIsPresentIfConstraintsExist, GenericElementConstraintRecordProperties.class))
                    .singleElement().asString()
                    .contains(GenericElementConstraintRecordProperties.class.getName());
        }

        @Test
        void passes_for_generic_type_without_constraints() {
            assertThat(violations(SpringConfigurationPropertiesRules.ValidatedIsPresentIfConstraintsExist, UnconstrainedGenericRecordProperties.class)).isEmpty();
        }

        @Test
        void ignores_classes_not_annotated_with_ConfigurationProperties() {
            assertThat(violations(SpringConfigurationPropertiesRules.ValidatedIsPresentIfConstraintsExist, ConstrainedButNoConfigurationProperties.class)).isEmpty();
        }
    }

    @Nested
    class Rule_ValidationIsCascadedToNestedTypes {

        @ConfigurationProperties("app")
        @Validated
        record CascadingRecordProperties(@NotBlank String name, @Valid Database database) {
        }

        @ConfigurationProperties("app")
        @Validated
        record NotCascadingRecordProperties(Database database) {
        }

        record Database(@NotBlank String host) {
        }

        @ConfigurationProperties("app")
        @Validated
        record MultiLevelRecordProperties(Server server) {
        }

        record Server(Credentials credentials) {
        }

        record Credentials(@NotBlank String username) {
        }

        @ConfigurationProperties("app")
        @Validated
        record CascadingMultiLevelRecordProperties(@Valid CascadingServer server) {
        }

        record CascadingServer(@NotBlank String host, @Valid CascadingCredentials credentials) {
        }

        record CascadingCredentials(@NotBlank String username) {
        }

        @ConfigurationProperties("app")
        @Validated
        record UnconstrainedNestedRecordProperties(Marker marker) {
        }

        record Marker(String value) {
        }

        @ConfigurationProperties("app")
        @Validated
        static class NotCascadingClassProperties {

            private Database database;

            public Database getDatabase() {
                return database;
            }
        }

        @ConfigurationProperties("app")
        @Validated
        static class AccessorCascadingClassProperties {

            private Database database;

            @Valid
            public Database getDatabase() {
                return database;
            }
        }

        @ConfigurationProperties("app")
        @Validated
        record ListRecordProperties(List<Database> databases) {
        }

        @ConfigurationProperties("app")
        @Validated
        record CascadedListRecordProperties(@Valid List<Database> databases) {
        }

        @ConfigurationProperties("app")
        @Validated
        record CascadedElementListRecordProperties(List<@Valid Database> databases) {
        }

        @ConfigurationProperties("app")
        @Validated
        record MapRecordProperties(Map<String, Database> databases) {
        }

        @ConfigurationProperties("app")
        @Validated
        record ArrayRecordProperties(Database[] databases) {
        }

        @ConfigurationProperties("app")
        @Validated
        record CyclicRecordProperties(@Valid Node node) {
        }

        record Node(@NotBlank String name, @Valid Node next) {
        }

        @Test
        void provides_a_description() {
            String description = SpringConfigurationPropertiesRules.ValidationIsCascadedToNestedTypes.getDescription();
            assertThat(description).isEqualTo("classes that are annotated with @ConfigurationProperties should cascade validation to nested types that declare Bean Validation constraints, because Bean Validation only validates nested objects if the corresponding property is annotated with @Valid");
        }

        @Test
        void passes_for_nested_record_annotated_with_Valid() {
            assertThat(violations(SpringConfigurationPropertiesRules.ValidationIsCascadedToNestedTypes, CascadingRecordProperties.class, Database.class)).isEmpty();
        }

        @Test
        void violates_for_nested_record_not_annotated_with_Valid() {
            assertThat(violations(SpringConfigurationPropertiesRules.ValidationIsCascadedToNestedTypes, NotCascadingRecordProperties.class, Database.class))
                    .singleElement().asString()
                    .startsWith("Field <" + NotCascadingRecordProperties.class.getName() + ".database> is not annotated with @Valid,"
                            + " so the Bean Validation constraints of " + Database.class.getName() + " are not validated");
        }

        @Test
        void reports_every_level_of_nesting() {
            assertThat(violations(SpringConfigurationPropertiesRules.ValidationIsCascadedToNestedTypes, MultiLevelRecordProperties.class, Server.class, Credentials.class))
                    .hasSize(2)
                    .anySatisfy(violation -> assertThat(violation).contains(MultiLevelRecordProperties.class.getName() + ".server"))
                    .anySatisfy(violation -> assertThat(violation).contains(Server.class.getName() + ".credentials"));
        }

        @Test
        void passes_for_multiple_levels_annotated_with_Valid() {
            assertThat(violations(SpringConfigurationPropertiesRules.ValidationIsCascadedToNestedTypes, CascadingMultiLevelRecordProperties.class, CascadingServer.class, CascadingCredentials.class)).isEmpty();
        }

        @Test
        void passes_for_nested_type_without_constraints() {
            assertThat(violations(SpringConfigurationPropertiesRules.ValidationIsCascadedToNestedTypes, UnconstrainedNestedRecordProperties.class, Marker.class)).isEmpty();
        }

        @Test
        void violates_for_nested_type_of_a_regular_class() {
            assertThat(violations(SpringConfigurationPropertiesRules.ValidationIsCascadedToNestedTypes, NotCascadingClassProperties.class, Database.class))
                    .singleElement().asString()
                    .contains(NotCascadingClassProperties.class.getName() + ".database");
        }

        @Test
        void passes_if_Valid_is_declared_on_the_accessor() {
            assertThat(violations(SpringConfigurationPropertiesRules.ValidationIsCascadedToNestedTypes, AccessorCascadingClassProperties.class, Database.class)).isEmpty();
        }

        @Test
        void violates_for_elements_of_a_generic_type_without_Valid() {
            assertThat(violations(SpringConfigurationPropertiesRules.ValidationIsCascadedToNestedTypes, ListRecordProperties.class, Database.class))
                    .singleElement().asString()
                    .startsWith("Field <" + ListRecordProperties.class.getName() + ".databases> is not annotated with @Valid,"
                            + " so the Bean Validation constraints of " + Database.class.getName() + " are not validated");
        }

        @Test
        void passes_for_generic_type_annotated_with_Valid() {
            assertThat(violations(SpringConfigurationPropertiesRules.ValidationIsCascadedToNestedTypes, CascadedListRecordProperties.class, Database.class)).isEmpty();
        }

        @Test
        void passes_for_elements_of_a_generic_type_annotated_with_Valid() {
            assertThat(violations(SpringConfigurationPropertiesRules.ValidationIsCascadedToNestedTypes, CascadedElementListRecordProperties.class, Database.class)).isEmpty();
        }

        @Test
        void violates_for_values_of_a_map_without_Valid() {
            assertThat(violations(SpringConfigurationPropertiesRules.ValidationIsCascadedToNestedTypes, MapRecordProperties.class, Database.class))
                    .singleElement().asString()
                    .contains(MapRecordProperties.class.getName() + ".databases");
        }

        @Test
        void violates_for_elements_of_an_array_without_Valid() {
            assertThat(violations(SpringConfigurationPropertiesRules.ValidationIsCascadedToNestedTypes, ArrayRecordProperties.class, Database.class))
                    .singleElement().asString()
                    .contains(ArrayRecordProperties.class.getName() + ".databases");
        }

        @Test
        void terminates_for_cyclic_references() {
            assertThat(violations(SpringConfigurationPropertiesRules.ValidationIsCascadedToNestedTypes, CyclicRecordProperties.class, Node.class)).isEmpty();
        }
    }
}
