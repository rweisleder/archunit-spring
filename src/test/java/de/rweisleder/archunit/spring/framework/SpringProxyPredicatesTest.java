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

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaMethod;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static de.rweisleder.archunit.spring.framework.SpringProxyPredicates.proxyable;
import static org.assertj.core.api.Assertions.assertThat;

class SpringProxyPredicatesTest {

    @Nested
    class Predicate_proxyable {

        @Test
        void provides_a_description() {
            DescribedPredicate<JavaMethod> predicate = proxyable();
            assertThat(predicate.getDescription()).isEqualTo("proxyable");
        }
    }
}
