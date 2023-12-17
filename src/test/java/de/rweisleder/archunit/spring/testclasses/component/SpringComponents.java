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
package de.rweisleder.archunit.spring.testclasses.component;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

public class SpringComponents {

    @Component
    public static class ComponentWithoutDependency {
    }

    @Controller
    public static class ControllerWithoutDependency {
    }

    @Controller
    public static class ControllerWithDependencyToController {
        @SuppressWarnings("unused")
        public ControllerWithDependencyToController(ControllerWithoutDependency controller) {
        }
    }

    @Controller
    public static class ControllerWithDependencyToService {
        @SuppressWarnings("unused")
        public ControllerWithDependencyToService(ServiceWithoutDependency service) {
        }
    }

    @Controller
    public static class ControllerWithDependencyToRepository {
        @SuppressWarnings("unused")
        public ControllerWithDependencyToRepository(RepositoryWithoutDependency repository) {
        }
    }

    @Controller
    public static class ControllerWithDependencyToSpringDataRepository {
        @SuppressWarnings("unused")
        public ControllerWithDependencyToSpringDataRepository(SpringDataRepositoryWithoutDependency repository) {
        }
    }

    @Controller
    public static class ControllerWithDependencyToConfiguration {
        @SuppressWarnings("unused")
        public ControllerWithDependencyToConfiguration(ConfigurationWithoutDependency configuration) {
        }
    }

    @Service
    public static class ServiceWithoutDependency {
    }

    @Service
    public static class ServiceWithDependencyToController {
        @SuppressWarnings("unused")
        public ServiceWithDependencyToController(ControllerWithoutDependency controller) {
        }
    }

    @Service
    public static class ServiceWithDependencyToService {
        @SuppressWarnings("unused")
        public ServiceWithDependencyToService(ServiceWithoutDependency service) {
        }
    }

    @Service
    public static class ServiceWithDependencyToRepository {
        @SuppressWarnings("unused")
        public ServiceWithDependencyToRepository(RepositoryWithoutDependency repository) {
        }
    }

    @Service
    public static class ServiceWithDependencyToSpringDataRepository {
        @SuppressWarnings("unused")
        public ServiceWithDependencyToSpringDataRepository(SpringDataRepositoryWithoutDependency repository) {
        }
    }

    @Service
    public static class ServiceWithDependencyToConfiguration {
        @SuppressWarnings("unused")
        public ServiceWithDependencyToConfiguration(ConfigurationWithoutDependency configuration) {
        }
    }

    @Repository
    public static class RepositoryWithoutDependency {
    }

    @Repository
    public static class RepositoryWithDependencyToController {
        @SuppressWarnings("unused")
        public RepositoryWithDependencyToController(ControllerWithoutDependency controller) {
        }
    }

    @Repository
    public static class RepositoryWithDependencyToService {
        @SuppressWarnings("unused")
        public RepositoryWithDependencyToService(ServiceWithoutDependency service) {
        }
    }

    @Repository
    public static class RepositoryWithDependencyToRepository {
        @SuppressWarnings("unused")
        public RepositoryWithDependencyToRepository(RepositoryWithoutDependency repository) {
        }
    }

    @Repository
    public static class RepositoryWithDependencyToSpringDataRepository {
        @SuppressWarnings("unused")
        public RepositoryWithDependencyToSpringDataRepository(SpringDataRepositoryWithoutDependency repository) {
        }
    }

    @Repository
    public static class RepositoryWithDependencyToConfiguration {
        @SuppressWarnings("unused")
        public RepositoryWithDependencyToConfiguration(ConfigurationWithoutDependency configuration) {
        }
    }

    public abstract static class SpringDataRepositoryWithoutDependency implements CrudRepository<Object, Object> {
    }

    public abstract static class SpringDataRepositoryWithDependencyToController implements CrudRepository<Object, Object> {
        @SuppressWarnings("unused")
        public SpringDataRepositoryWithDependencyToController(ControllerWithoutDependency controller) {
        }
    }

    public abstract static class SpringDataRepositoryWithDependencyToService implements CrudRepository<Object, Object> {
        @SuppressWarnings("unused")
        public SpringDataRepositoryWithDependencyToService(ServiceWithoutDependency service) {
        }
    }

    public abstract static class SpringDataRepositoryWithDependencyToRepository implements CrudRepository<Object, Object> {
        @SuppressWarnings("unused")
        public SpringDataRepositoryWithDependencyToRepository(RepositoryWithoutDependency repository) {
        }
    }

    public abstract static class SpringDataRepositoryWithDependencyToSpringDataRepository implements CrudRepository<Object, Object> {
        @SuppressWarnings("unused")
        public SpringDataRepositoryWithDependencyToSpringDataRepository(SpringDataRepositoryWithoutDependency repository) {
        }
    }

    public abstract static class SpringDataRepositoryWithDependencyToConfiguration implements CrudRepository<Object, Object> {
        @SuppressWarnings("unused")
        public SpringDataRepositoryWithDependencyToConfiguration(ConfigurationWithoutDependency configuration) {
        }
    }

    @Configuration
    public static class ConfigurationWithoutDependency {
    }
}
