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
import com.tngtech.archunit.core.domain.properties.CanBeAnnotated;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.stereotype.Controller;

import static com.tngtech.archunit.base.DescribedPredicate.describe;
import static de.rweisleder.archunit.spring.MergedAnnotationPredicates.springAnnotatedWith;
import static org.assertj.core.api.Assertions.assertThat;

class MergedAnnotationPredicatesTest {

    @Nested
    class Predicate_springAnnotatedWith_with_Class {

        @Test
        void provides_a_description() {
            DescribedPredicate<CanBeAnnotated> predicate = springAnnotatedWith(Controller.class);
            assertThat(predicate.getDescription()).isEqualTo("annotated with @Controller");
        }
    }

    @Nested
    class Predicate_springAnnotatedWith_with_String {

        @Test
        void provides_a_description() {
            DescribedPredicate<CanBeAnnotated> predicate = springAnnotatedWith("org.springframework.stereotype.Controller");
            assertThat(predicate.getDescription()).isEqualTo("annotated with @Controller");
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
    }
}
