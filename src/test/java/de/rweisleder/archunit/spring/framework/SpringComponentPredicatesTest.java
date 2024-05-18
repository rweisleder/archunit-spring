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

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static de.rweisleder.archunit.spring.framework.SpringComponentPredicates.springComponent;
import static de.rweisleder.archunit.spring.framework.SpringComponentPredicates.springConfiguration;
import static de.rweisleder.archunit.spring.framework.SpringComponentPredicates.springController;
import static de.rweisleder.archunit.spring.framework.SpringComponentPredicates.springRepository;
import static de.rweisleder.archunit.spring.framework.SpringComponentPredicates.springService;
import static org.assertj.core.api.Assertions.assertThat;

class SpringComponentPredicatesTest {

    @Nested
    class Predicate_springComponent {

        @Test
        void provides_a_description() {
            DescribedPredicate<JavaClass> predicate = springComponent();
            assertThat(predicate.getDescription()).isEqualTo("Spring component");
        }
    }

    @Nested
    class Predicate_springController {

        @Test
        void provides_a_description() {
            DescribedPredicate<JavaClass> predicate = springController();
            assertThat(predicate.getDescription()).isEqualTo("Spring controller");
        }
    }

    @Nested
    class Predicate_springService {

        @Test
        void provides_a_description() {
            DescribedPredicate<JavaClass> predicate = springService();
            assertThat(predicate.getDescription()).isEqualTo("Spring service");
        }
    }

    @Nested
    class Predicate_springRepository {

        @Test
        void provides_a_description() {
            DescribedPredicate<JavaClass> predicate = springRepository();
            assertThat(predicate.getDescription()).isEqualTo("Spring repository");
        }
    }

    @Nested
    class Predicate_springConfiguration {

        @Test
        void provides_a_description() {
            DescribedPredicate<JavaClass> predicate = springConfiguration();
            assertThat(predicate.getDescription()).isEqualTo("Spring configuration");
        }
    }
}
