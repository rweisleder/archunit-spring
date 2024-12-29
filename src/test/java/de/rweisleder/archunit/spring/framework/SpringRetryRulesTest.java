/*
 * #%L
 * ArchUnit Spring Integration
 * %%
 * Copyright (C) 2024 Hasan Kara
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

class SpringRetryRulesTest {

    @Nested
    class Rule_RetryableMethodsAreProxyable {

        @Test
        void provides_a_description() {
            String description = SpringRetryRules.RetryableMethodsAreProxyable.getDescription();
            assertThat(description).isEqualTo("methods that are annotated with @Retryable should be proxyable");
        }
    }

    @Nested
    class Rule_RetryableMethodsNotCalledFromSameClass {

        @Test
        void provides_a_description() {
            String description = SpringRetryRules.RetryableMethodsNotCalledFromSameClass.getDescription();
            assertThat(description).isEqualTo("methods that are annotated with @Retryable should not be called from within the same class");
        }
    }
}
