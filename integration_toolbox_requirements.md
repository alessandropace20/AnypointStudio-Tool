# Integration Toolbox — Requirements Specification

**Document type:** Software Requirements Specification (SRS)  
**Project type:** Native desktop application  
**Primary goal:** Provide a lightweight, modern, local-first toolbox for integration developers, with a VS Code-inspired user interface.  
**Implementation language/framework:** The coding agent may choose the most suitable stack, but must justify the choice against the performance, footprint, maintainability, security, and UI requirements below.

---

## 1. Project overview

Build a desktop application named **Integration Toolbox** for developers who work with APIs, data transformations, and integration platforms such as MuleSoft and TIBCO BusinessWorks.

The application must consolidate frequent development utilities into one cohesive interface. It must not attempt to replace Anypoint Platform, generate API credentials, or create authentication keys.

The application must provide these initial feature areas:

1. API key encryption and decryption.
2. XML and JSON conversion and schema validation.
3. Structural comparison of XML and JSON documents.
4. A searchable DataWeave snippet library.
5. A searchable TIBCO BusinessWorks → MuleSoft equivalence/reference library.

The product must prioritize a **small resource footprint, fast startup, responsive interaction, local processing, and a polished developer-oriented UI**.

## 2. Core principles and constraints

- **Lightweight first:** minimize installer size, runtime memory use, startup time, and unnecessary dependencies.
- **Local-first:** process keys, tokens, payloads, schemas, snippets, and configuration data on the user's machine. No telemetry or network transmission by default.
- **Security by design:** never log or silently persist plaintext secrets.
- **Developer-focused UX:** the application should feel familiar to users of Visual Studio Code.
- **Modular architecture:** each feature area must be separable, testable, and maintainable.
- **Honest conversion:** clearly report lossy or approximate conversions between XML/XSD and JSON/JSON Schema.
- **No credential creation:** do not implement API key, client ID, client secret, token, certificate, or authentication-credential generation.
- **No platform impersonation:** the application must not claim to create, rotate, register, or manage credentials in Anypoint Platform.

## 3. Technology selection

The coding agent may choose the implementation language and framework. Evaluate at least the following options before committing:

- **C++ with Qt 6:** preferred candidate for a native desktop application and low runtime overhead.
- **Java with JavaFX or Swing:** acceptable if packaging, startup, and memory usage remain reasonable.
- **Python with a suitable desktop UI framework:** acceptable only if the packaged application remains reasonably lightweight and does not require a heavy runtime at launch.

The final decision must be documented in `docs/TECHNOLOGY_DECISION.md`, covering:

- Expected installer/package size.
- Runtime memory use and startup considerations.
- Cross-platform support.
- UI quality and ability to reproduce a VS Code-like interface.
- Available XML, JSON, schema-validation, cryptography, and testing libraries.
- Dependency and license implications.
- Packaging and update strategy.

Do not choose a technology solely because it is familiar. Avoid embedding a full browser engine or shipping a large runtime unless it is justified and no lighter practical option meets the UI requirements.

## 4. Target platforms and distribution

- Target desktop platforms: Windows first; keep the architecture portable to macOS and Linux where practical.
- The application must run locally after installation.
- Core features must not require an account, cloud service, or internet connection.
- Use a reproducible build and packaging process.
- Do not bundle unnecessary development tools or dependencies into the production package.
- Document build, test, and packaging instructions.

## 5. User interface and interaction requirements

### 5.1 Visual design

The interface must be modern, polished, dense enough for developer workflows, and strongly inspired by Visual Studio Code in its **interaction patterns and layout**, without copying proprietary branding or icons.

Required elements:

- Dark theme as the default, with a coherent light theme if feasible.
- Left activity bar with icons for the main feature areas.
- Side panel whose content changes based on the selected activity.
- Central workspace with editor-like views.
- Tabbed documents/tools across the top of the workspace.
- Bottom status bar for relevant state, validation results, encoding, and other contextual information.
- Command palette opened by `Ctrl+Shift+P`.
- Quick search/navigation where appropriate.
- Resizable panels and split views where useful.
- Consistent spacing, typography, iconography, focus states, hover states, and keyboard navigation.
- Clear empty states, progress states, success notifications, and actionable error messages.
- Support high-DPI displays and sensible window resizing.
- Do not use browser-like UI if a native desktop interface can meet the requirements with less overhead.

### 5.2 Main navigation

Provide these primary activity-bar sections:

1. **Security**
2. **Data Tools**
3. **Structural Diff**
4. **DataWeave Library**
5. **TIBCO → MuleSoft**
6. **Settings**

Each section should open its tools in workspace tabs. Users must be able to switch between tabs without losing unsaved editor contents.

### 5.3 Keyboard interaction

Implement common, predictable shortcuts. At minimum:

- `Ctrl+Shift+P`: open command palette.
- `Ctrl+W`: close the active tab.
- `Ctrl+Tab`: switch between open tabs.
- `Ctrl+F`: find within the active editor or view.
- `Ctrl+Shift+F`: search the relevant local library or project dataset when applicable.
- `Ctrl+S`: save an editable library entry or exportable document where appropriate.
- `Ctrl+Enter`: run the active conversion/validation/diff action when applicable.
- `Escape`: close the command palette or dismiss transient overlays.

Shortcuts must not trigger destructive actions without confirmation. Display shortcuts in menus and tooltips. Avoid conflicts with text editing.

### 5.4 Command palette

The command palette must:

- Search commands by name and keyword.
- Open each feature directly.
- Run common operations such as Format XML, Format JSON, Validate XML, Validate JSON, Convert XML to JSON, Convert JSON to XML, Compare Documents, and Open DataWeave Library.
- Support keyboard-only operation.
- Display the relevant shortcut when one exists.

### 5.5 Editors and tab behavior

- Use syntax highlighting for XML, JSON, XSD, JSON Schema, DataWeave, and properties files wherever practical.
- Provide line numbers, indentation, find, select-all, copy, paste, and clear actions.
- Provide separate input and output panes for conversion tools.
- Provide a one-click copy action for generated output.
- Warn before discarding unsaved edits where applicable.
- Do not silently replace input content with output.
- Long operations must not freeze the main UI thread.

## 6. Feature A — API key encryption and decryption

### 6.1 Scope

Provide a utility to encrypt and decrypt user-supplied API key values. The tool must only process values the user provides; it must not generate credentials or retrieve keys from external platforms.

### 6.2 Required behavior

- Separate **Encrypt** and **Decrypt** modes.
- Accept input through a protected text field or editor.
- Mask plaintext or secret values by default where appropriate, with an explicit reveal/hide control.
- Provide copy-to-clipboard actions.
- Clearly distinguish ciphertext from plaintext.
- Display safe, actionable errors for invalid input, wrong passwords, unsupported formats, or failed decryption.
- Never log plaintext keys, passwords, or decrypted results.
- Never send keys to a remote service.
- Do not persist entered secrets automatically.
- Clear sensitive fields on explicit user request and when a security-sensitive view is closed, where practical.
- Avoid storing secrets in application history, recent-file lists, crash reports, or debug logs.

### 6.3 Cryptographic requirements

- Use established, maintained cryptographic libraries; do not implement cryptographic primitives from scratch.
- Do not invent a custom encryption algorithm or custom cryptographic protocol.
- If password-based encryption is used, use a modern authenticated encryption design and a well-established password-based key derivation function with suitable parameters.
- Use unique, cryptographically secure random nonces/salts where required by the chosen algorithm.
- Validate and version the encrypted data format so future versions can handle it safely.
- Document the algorithm, key derivation approach, format version, and compatibility implications in `docs/SECURITY.md`.
- Do not claim compatibility with Anypoint Platform encryption formats unless that compatibility has been verified against official documentation and tested.
- Do not generate API keys, client IDs, client secrets, OAuth tokens, certificates, or other authentication credentials.

### 6.4 Acceptance criteria

- Valid user-supplied input can be encrypted and decrypted successfully using the documented workflow.
- Incorrect passwords or malformed ciphertext produce a clear error without exposing sensitive input.
- A round-trip test confirms that decrypting newly encrypted data recovers the original value.
- Automated tests confirm that secrets are not written to logs or persistent history.
- The UI clearly communicates which value is plaintext and which is encrypted.

## 7. Feature B — XML, JSON, XSD, and JSON Schema tools

### 7.1 Supported operations

Implement:

- XML formatting and validation.
- JSON formatting and validation.
- XML → JSON conversion.
- JSON → XML conversion.
- XML validation against XSD.
- JSON validation against JSON Schema.
- XSD → JSON Schema conversion where feasible.
- JSON Schema → XSD conversion where feasible.

Schema-to-schema conversion must be presented as best-effort, not guaranteed lossless conversion. If a construct cannot be represented accurately, report it in a conversion warnings panel.

### 7.2 Input and output UX

- Provide an editor for the input document and a separate output editor.
- Allow users to paste text or open a local file.
- Allow copying output and saving it to a chosen path.
- Provide Format, Validate, Convert, Clear, and Copy actions as applicable.
- Show errors with line and column information when the underlying parser provides it.
- Preserve the input by default.
- Display conversion warnings separately from fatal errors.
- Provide a concise summary of validation results.

### 7.3 XML-specific requirements

Handle correctly, where supported by the chosen libraries:

- XML namespaces and namespace prefixes.
- Attributes.
- Repeated elements.
- Empty elements.
- Text content.
- XML declarations and character encoding.
- Nested structures.
- Schema imports/includes where the validation library supports them.

Security requirements:

- Disable external entity resolution and unsafe external resource loading by default.
- Protect against XXE and other unsafe XML parser configurations.
- Do not automatically download schemas or DTDs from the network.
- Set reasonable input-size and nesting limits, with understandable errors.

### 7.4 JSON-specific requirements

- Correctly distinguish objects, arrays, strings, numbers, booleans, and null.
- Report malformed JSON with useful location details where possible.
- Support a documented JSON Schema draft or drafts supported by the chosen library.
- Clearly identify unsupported schema keywords or draft mismatches.
- Do not silently coerce values to make validation pass.

### 7.5 Conversion semantics

XML and JSON have different data models. Provide configurable conversion options where useful, including:

- Representation of XML attributes.
- Treatment of repeated XML elements.
- Treatment of empty elements and null-like values.
- Handling of namespaces.
- Root element naming for JSON → XML.
- Pretty-printed versus compact output.

Document defaults and make them consistent. Show warnings when conversion may lose information, such as mixed content, namespace semantics, or type constraints.

### 7.6 Acceptance criteria

- Valid XML and JSON can be formatted without changing their logical content.
- Invalid documents produce actionable diagnostics.
- XML/XSD and JSON/JSON Schema validation return clear pass/fail results.
- Conversion options are visible and documented.
- Lossy or approximate schema conversions produce warnings.
- Tests cover nested structures, arrays/repeated elements, attributes, namespaces, empty values, special characters, malformed inputs, and schema failures.

## 8. Feature C — Structural XML/JSON diff

### 8.1 Purpose

Compare two XML or two JSON documents structurally rather than relying only on line-by-line text comparison.

### 8.2 Required behavior

- Provide two independent input panes: **Left** and **Right**.
- Detect added, removed, and changed values.
- Identify the path to each difference.
- Support JSON object comparison independent of property order.
- Treat array order as significant by default; optionally offer a clearly labeled mode to ignore order only when safe and meaningful.
- For XML, compare elements, attributes, text values, and namespace-aware names where practical.
- Do not ignore XML element order by default.
- Offer whitespace-normalization options without silently changing the source documents.
- Display a summary of the number and type of differences.
- Let users navigate between differences.
- Highlight differences in context and provide a readable path such as `$.employee.name` or an XPath-like XML path.
- Allow copying the diff report.
- Never overwrite either input during comparison.

### 8.3 Acceptance criteria

- Documents with different formatting but identical structure can be reported as equivalent when normalization is enabled.
- Added, removed, and modified values are correctly distinguished.
- JSON property ordering does not create false-positive differences by default.
- XML namespace and element-order behavior is documented.
- Tests cover nested objects, arrays, attributes, namespaces, missing fields, null values, and whitespace options.

## 9. Feature D — DataWeave snippet library

### 9.1 Purpose

Provide a searchable, editable local library of reusable DataWeave examples and patterns.

### 9.2 Entry fields

Each snippet should support:

- Title.
- Category.
- Tags.
- Short description.
- DataWeave code.
- Example input.
- Expected output.
- Compatibility notes or DataWeave version, when known.
- Optional notes and source/reference URL.

### 9.3 Required behavior

- Search by title, description, category, tags, and code text.
- Filter by category.
- Create, edit, duplicate, and delete entries.
- Copy code with one action.
- Preview example input and expected output.
- Mark favorites or pinned snippets.
- Include a small set of useful starter snippets, with clear attribution where required.
- Store the library locally in a documented, portable format such as JSON.
- Provide import and export with validation and duplicate handling.
- Do not claim that snippets compile unless they have actually been validated with a compatible DataWeave runtime.

### 9.4 Suggested starter categories

- Arrays and collections.
- Objects.
- Strings.
- Null handling and defaults.
- Dates and timestamps.
- XML and namespaces.
- JSON transformations.
- Reduce and aggregation.
- Error payloads.
- Common integration patterns.

### 9.5 Acceptance criteria

- Users can search and retrieve a snippet quickly.
- CRUD operations persist after application restart.
- Importing invalid library data produces a clear error without corrupting existing entries.
- Exported libraries can be re-imported.
- Copying a snippet preserves its code exactly.

## 10. Feature E — TIBCO BusinessWorks → MuleSoft equivalence library

### 10.1 Purpose

Provide a searchable reference library that helps developers understand how common TIBCO BusinessWorks concepts and activities may be implemented in MuleSoft.

This is a reference and learning tool, not an automatic full-project migration engine.

### 10.2 Entry fields

Each equivalence entry should support:

- TIBCO activity or concept.
- TIBCO version/context, when relevant.
- MuleSoft component, pattern, or DataWeave alternative.
- Category.
- Explanation of original behavior.
- Suggested MuleSoft approach.
- Code or configuration example, where applicable.
- Semantic differences and migration caveats.
- Limitations and cases where the mapping is not one-to-one.
- Tags and search keywords.
- Optional documentation references.

### 10.3 Required behavior

- Search by TIBCO activity, MuleSoft term, category, tag, and free text.
- Filter by category.
- Show the original concept and proposed MuleSoft equivalent side by side.
- Clearly label approximate mappings.
- Allow users to add, edit, duplicate, and delete custom entries.
- Provide import/export in a documented local format.
- Include starter entries only when they are accurate and appropriately caveated.
- Avoid asserting that two components are equivalent if their behavior or execution semantics differ.

### 10.4 Suggested categories

- Mapping and transformations.
- XPath and DataWeave.
- Loops and collections.
- Conditions and routing.
- Variables and scope.
- Error handling.
- HTTP and REST.
- SOAP and XML.
- File operations.
- Messaging.
- Batch and pagination.
- Configuration and properties.

### 10.5 Acceptance criteria

- Users can search by either platform's terminology.
- Results explicitly explain semantic differences.
- Custom entries persist and can be exported/imported.
- The library is usable offline.
- No entry is presented as an exact one-to-one mapping unless the claim is justified.

## 11. Local data, persistence, and privacy

- Keep user libraries local by default.
- Use a documented format for user-created snippets and equivalence entries.
- Do not persist secret inputs or decrypted values.
- Separate ordinary library persistence from security-sensitive transient data.
- Provide explicit export/import actions.
- Do not add telemetry, analytics, crash-upload services, or remote AI features by default.
- If a future version introduces any network-based feature, it must be opt-in, clearly disclosed, and must not transmit secrets silently.
- Make backup and restore of non-sensitive libraries straightforward.

## 12. Performance and resource targets

The application should remain responsive on an ordinary Windows desktop without a dedicated GPU.

Treat these as initial engineering targets, to be measured and reported rather than assumed:

- Cold startup target: approximately 2 seconds or less on a representative modern desktop, excluding first-run OS security checks.
- Idle memory target: approximately 150 MB or less for a native implementation with no large embedded runtime.
- No main-thread blocking during parsing, validation, or comparison of large documents.
- Avoid unnecessary background services and auto-start behavior.
- Load libraries lazily where appropriate.
- Use streaming or size limits for large inputs where the selected parser supports it.
- Debounce live search and avoid re-rendering the whole interface on each keystroke.
- Establish a documented test corpus for performance measurements.
- If a target cannot reasonably be met, document the measured result and the reason instead of hiding the limitation.

These targets are goals, not permission to compromise correctness, security, or accessibility.

## 13. Accessibility and usability

- Support full keyboard navigation.
- Provide visible focus indicators.
- Use readable contrast in dark and light themes.
- Do not communicate errors or status by color alone.
- Provide tooltips for icon-only controls.
- Make buttons and controls accessible to screen readers where the UI framework supports it.
- Ensure text remains usable when the window is resized or display scaling is increased.

## 14. Error handling and diagnostics

- Show concise, actionable error messages.
- Include line/column or path information when available.
- Separate validation errors, warnings, and informational messages.
- Never include secret values in error messages.
- Avoid silently swallowing exceptions.
- A diagnostic mode may log non-sensitive technical details, but it must redact sensitive content.
- Provide a way to copy non-sensitive diagnostic information for troubleshooting.

## 15. Testing requirements

Implement automated tests for:

- Encryption/decryption round trips and invalid input.
- Secret redaction and absence of sensitive values in logs.
- XML and JSON parsing and formatting.
- XML/XSD validation.
- JSON Schema validation.
- XML ↔ JSON conversion edge cases.
- Structural diff behavior.
- DataWeave library CRUD, import, export, and search.
- TIBCO → MuleSoft library CRUD, import, export, and search.
- Keyboard shortcuts and core navigation where practical.
- Error handling and malformed input.
- Packaging and clean installation.

Use representative fixtures, including nested data, arrays, namespaces, repeated XML elements, attributes, empty values, Unicode, malformed files, and large documents.

Tests must be repeatable and must not require production credentials or external network access.

## 16. Documentation and deliverables

The coding agent must produce:

- Application source code.
- Reproducible build and packaging instructions.
- `README.md` with installation, usage, supported features, and limitations.
- `docs/TECHNOLOGY_DECISION.md`.
- `docs/SECURITY.md`.
- `docs/ARCHITECTURE.md`.
- `docs/TESTING.md`.
- Sample non-sensitive XML, XSD, JSON, JSON Schema, DataWeave snippets, and equivalence entries.
- Automated tests.
- A list of third-party dependencies and their licenses.
- A clear list of implemented, partially implemented, and deferred features.

## 17. Development approach

Implement the project incrementally. Do not attempt to build every feature in one unverified change.

### Phase 1 — Foundation and UI

- Evaluate and document the technology choice.
- Set up the project structure and build.
- Implement the main window, activity bar, side panel, workspace tabs, status bar, command palette, and keyboard navigation.
- Establish theme and reusable UI components.
- Verify the application starts and remains responsive.

### Phase 2 — Core data tools

- Implement XML/JSON formatting.
- Implement XML and JSON parsing and validation.
- Add XSD and JSON Schema validation.
- Add XML ↔ JSON conversion with explicit options and warnings.
- Add automated tests for edge cases.

### Phase 3 — Structural diff

- Implement XML and JSON structural comparison.
- Add difference navigation, summaries, and normalization options.
- Test arrays, property ordering, namespaces, and element order.

### Phase 4 — Developer reference libraries

- Implement DataWeave snippet search, CRUD, favorites, import, and export.
- Implement TIBCO → MuleSoft equivalence search, CRUD, import, and export.
- Add starter data with documented caveats.

### Phase 5 — Security utility

- Implement encryption and decryption using a maintained cryptographic library.
- Review secret handling, clipboard behavior, logging, and persistence.
- Add security tests and document the data format.

### Phase 6 — Hardening and packaging

- Measure startup time, memory use, and large-document responsiveness.
- Improve keyboard navigation and accessibility.
- Run all automated tests.
- Produce a clean installer/package and validate installation on a clean environment.
- Complete documentation and dependency-license review.

At the end of each phase, build and test the application before moving to the next. Do not leave the project in a state that no longer compiles.

## 18. Definition of Done

The project is considered complete when:

- The application builds and installs using documented instructions.
- The interface is polished, consistent, and recognizably inspired by VS Code's interaction patterns.
- All five core feature areas are implemented and usable.
- XML/JSON conversion and validation report limitations honestly.
- Structural comparison distinguishes meaningful differences without common false positives.
- Both reference libraries support search and persistent local editing.
- The security tool encrypts and decrypts user-provided values without creating credentials.
- No secrets are transmitted remotely or written to logs/persistent history by default.
- Automated tests pass.
- Performance measurements and known limitations are documented.
- No primary UI action is merely decorative or non-functional.
- Any deferred requirement is explicitly listed rather than represented as complete.

## 19. Agent execution instructions

Before coding:

1. Inspect the existing repository and preserve any working functionality unless a change is necessary and documented.
2. Identify the current language, framework, build system, and existing encryption implementation, if present.
3. Write a concise implementation plan and the technology decision.
4. Identify security-sensitive code paths before modifying them.
5. Implement in the phases above.

During coding:

- Prefer small, testable changes.
- Compile and run tests after each significant milestone.
- Do not replace working functionality with placeholders.
- Do not mark a feature complete until it has been implemented and verified.
- Do not introduce large dependencies without explaining why they are necessary.
- Keep UI logic, business logic, parsing, cryptography, and persistence separated.
- Record important design decisions and known limitations.

At completion, report:

- Features implemented.
- Build and test results.
- Packaging location and instructions.
- Measured startup time and idle memory usage, including test environment.
- Security decisions and limitations.
- Known defects and deferred requirements.
