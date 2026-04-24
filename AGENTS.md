# Repository Guidelines

## Project Structure & Module Organization
This repository is a Maven multi-module project with three Spring Boot services and shared libraries. `foodie-common` holds shared config, utilities, exceptions, and base YAML. `foodie-pojo` contains entities, DTOs, and VOs. Runtime services live in `foodie-platform-server`, `foodie-merchant-server`, and `foodie-user-server`, with Java code under `src/main/java` and MyBatis XML under `src/main/resources/mapper`.

Frontend apps are under `vue/`: `foodie_plateform` for the platform console, `foodie_merchant` for merchant operations, and `my_foodie_app` for the customer app. Keep Vue code inside `src/api`, `src/views`, `src/components`, and `src/stores` or `src/store`.

## Build, Test, and Development Commands
Run backend builds from the repo root:

- `mvn clean install`: build all Java modules.
- `mvn test`: run the current backend test set.
- `mvn -pl foodie-platform-server spring-boot:run`: start platform API on `:8081`.
- `mvn -pl foodie-merchant-server spring-boot:run`: start merchant API on `:8082`.
- `mvn -pl foodie-user-server spring-boot:run`: start user API on `:8083`.

Run frontend apps from their own directories after `npm install`:

- `npm run dev`: start Vite dev server.
- `npm run build`: create production assets.
- `npm run preview`: preview the build locally.

## Coding Style & Naming Conventions
Follow existing conventions: 4-space indentation in Java, camelCase fields/methods, PascalCase classes, and package paths rooted at `com.foodie.<domain>`. Keep controllers, services, mappers, and handlers in their existing layer folders. Vue files use ES modules, single quotes, and PascalCase component filenames; keep route/view folders lowercase (`views/order`, `views/review`).

No formatter config is checked in, so match surrounding code and keep imports tidy before committing.

## Testing Guidelines
Backend test coverage is currently light, so add focused tests with every non-trivial change. Place tests under `src/test/java` using `*Test` or `*Tests` suffixes. Prefer service or controller-level smoke tests for new business logic, and run `mvn -pl <module> test` before opening a PR.

## Commit & Pull Request Guidelines
Recent commits use short, module-focused summaries such as `购物车管理实现` and `WebMvc重构`. Keep commit titles concise, imperative, and scoped to one change. PRs should list affected modules, config changes, verification steps, and screenshots for UI work. Link related issues and call out any new environment variables, ports, or external dependencies.

## Security & Configuration Tips
Configuration is split between module `application*.yml` files and `foodie-common/src/main/resources/application-dev-common.yml`. Do not commit real database, Redis, SMS, or payment secrets; prefer environment overrides such as `DB_PASSWORD` and `REDIS_PASSWORD`.
