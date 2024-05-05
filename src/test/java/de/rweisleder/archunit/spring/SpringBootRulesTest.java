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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SpringBootRulesTest {

    @Nested
    class Rule_AllTypesInApplicationPackage {

        @Test
        void provides_a_description() {
            String description = SpringBootRules.AllTypesInApplicationPackage.getDescription();
            assertThat(description).isEqualTo("all types of a Spring Boot application should be located in the same package or a sub-package of the application class");
        }
    }
}
