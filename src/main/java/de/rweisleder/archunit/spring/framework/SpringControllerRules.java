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

import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.conditions.ArchConditions.be;
import static com.tngtech.archunit.lang.conditions.ArchPredicates.are;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static de.rweisleder.archunit.spring.SpringAnnotationPredicates.springAnnotatedWith;
import static de.rweisleder.archunit.spring.framework.SpringControllerPredicates.intentionallyAnnotatedWithControllerWithName;

/**
 * Collection of {@link ArchRule rules} that can be used to check {@code @Controller} classes.
 *
 * @author Roland Weisleder
 */
public final class SpringControllerRules {

    private SpringControllerRules() {
    }

    /**
     * A rule that checks that named controllers ({@code @Controller} or {@code @RestController} etc.)
     * are also annotated with {@code @RequestMapping} to avoid misinterpretation of the name as a path mapping.
     * <p>
     * As an example:
     * <pre>{@code
     * @RestController("/hello")
     * class HelloController {
     *
     *     @GetMapping
     *     String hello() {
     *         return "Hello World";
     *     }
     * }
     * }</pre>
     * is technically valid, but might not behave as intended. The developer probably meant:
     * <pre>{@code
     * @RestController
     * @RequestMapping("/hello")
     * class HelloController {
     *
     *     @GetMapping
     *     String hello() {
     *         return "Hello World";
     *     }
     * }
     * }</pre>
     *
     * <p>
     * Produces violations for:
     * <pre>{@code
     * @Controller("/hello") // might be misinterpreted as path mapping
     * class HelloController {
     *     // ...
     * }
     * }</pre>
     *
     * <p>
     * Passes for:
     * <pre>{@code
     * @Controller
     * @RequestMapping("/hello")
     * class HelloController {
     *     // ...
     * }
     *
     * @Controller // @RequestMapping is not mandatory at class level
     * class HelloController {
     *     // ...
     * }
     *
     * @Controller("hello")
     * @RequestMapping("/hello") // bean name and path are clearly separated
     * class HelloController {
     *     // ...
     * }
     * }</pre>
     */
    public static final ArchRule ControllerNameWithoutRequestMapping = classes()
            .that(are(intentionallyAnnotatedWithControllerWithName()))
            .should(be(springAnnotatedWith("org.springframework.web.bind.annotation.RequestMapping")))
            .because("developers might misinterpret the name as a path mapping")
            .allowEmptyShould(true);
}
