# Architecture Overview

The **Integration Toolbox** is organized as a modular Java project using Maven with the following high‑level layers:

1. **Presentation (UI) Layer** – JavaFX based views, activity bar, side panels, tabbed workspace.  Implemented in the `com.integrationtoolbox.ui` package.
2. **Application (Controller) Layer** – Coordinates UI actions, holds state, and invokes services.  Planned package `com.integrationtoolbox.controller`.
3. **Domain (Business) Layer** – Core feature implementations:
   - **Security** – `com.integrationtoolbox.security.EncryptionUtil` (AES‑GCM with PBKDF2).
   - **Data Tools** – `com.integrationtoolbox.datatools.DataTools` (JSON formatting, XML pretty‑print, validation placeholders).
   - **Diff** – Future `com.integrationtoolbox.diff` package.
   - **Libraries** – Future `com.integrationtoolbox.library` packages for DataWeave snippets and TIBCO→Mule mappings.
4. **Infrastructure Layer** – Maven build, dependency management, packaging with the Maven Assembly plugin to produce a runnable JAR containing the JavaFX runtime.
5. **Resources** – Configuration files, CSS theme, sample data, and documentation under `src/main/resources`.

All modules share a common set of dependencies defined in the root `pom.xml` (JavaFX, Jackson, JAXB, BouncyCastle, JUnit). The root POM aggregates the existing `configtool` module (legacy utility) and the new `integration-toolbox` module.

The architecture favours **separation of concerns** and **testability**:
- UI components are thin and delegate to services.
- Business logic is pure Java, independent of JavaFX, making unit testing straightforward.
- Security‑sensitive code lives in a dedicated package with clear audit boundaries.

Future work will flesh out the remaining feature areas while keeping this structure intact.
