/*
 * #%L
 * ArchUnit Spring Integration
 * %%
 * Copyright (C) 2023 - 2025 Roland Weisleder
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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SpringScheduledRulesTest {

    @Nested
    class Rule_EnableSchedulingIsPresentIfScheduledMethodsExist {

        @Test
        void provides_a_description() {
            String description = SpringScheduledRules.EnableSchedulingIsPresentIfScheduledMethodsExist.getDescription();
            assertThat(description).isEqualTo("application should contain a class annotated with @EnableScheduling if any method is annotated with @Scheduled");
        }
    }
}
