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

class SpringAsyncRulesTest {

    @Nested
    class Rule_AsyncMethodsAreProxyable {

        @Test
        void provides_a_description() {
            String description = SpringAsyncRules.AsyncMethodsAreProxyable.getDescription();
            assertThat(description).isEqualTo("methods that are annotated with @Async or @Asynchronous (directly or at class level) should be proxyable");
        }
    }

    @Nested
    class Rule_AsyncMethodsHaveSuitableReturnType {

        @Test
        void provides_a_description() {
            String description = SpringAsyncRules.AsyncMethodsHaveSuitableReturnType.getDescription();
            assertThat(description).isEqualTo("methods that are annotated with @Async or @Asynchronous (directly or at class level) should have return type void or java.util.concurrent.Future");
        }
    }

    @Nested
    class Rule_AsyncMethodsNotCalledFromSameClass {

        @Test
        void provides_a_description() {
            String description = SpringAsyncRules.AsyncMethodsNotCalledFromSameClass.getDescription();
            assertThat(description).isEqualTo("methods that are annotated with @Async or @Asynchronous (directly or at class level) should not be called from within the same class");
        }
    }

    @Nested
    class Rule_EnableAsyncIsPresentIfAsyncMethodsExist {

        @Test
        void provides_a_description() {
            String description = SpringAsyncRules.EnableAsyncIsPresentIfAsyncMethodsExist.getDescription();
            assertThat(description).isEqualTo("application should contain a class annotated with @EnableAsync if any method is annotated with @Async");
        }
    }
}
