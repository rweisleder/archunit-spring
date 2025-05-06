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
import com.tngtech.archunit.core.domain.JavaClass;
import org.springframework.stereotype.Controller;

import static com.tngtech.archunit.base.DescribedPredicate.describe;
import static de.rweisleder.archunit.spring.SpringAnnotationPredicates.springAnnotatedWith;

/**
 * Collection of {@link DescribedPredicate predicates} that can be used to match {@code @Controller} classes.
 *
 * @author Roland Weisleder
 */
public final class SpringControllerPredicates {

    private SpringControllerPredicates() {
    }

    /**
     * Returns a predicate that matches classes annotated with {@code @Controller(<name>)}.
     */
    public static DescribedPredicate<JavaClass> intentionallyAnnotatedWithControllerWithName() {
        return springAnnotatedWith(Controller.class, describe("@Controller(<name>)", controller -> !controller.value().isEmpty()))
                .as("intentionally annotated with @Controller(<name>) or @RestController(<name>)")
                .forSubtype();
    }
}
