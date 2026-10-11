# Technology Decision

## Chosen Stack
- **Language:** Java (>=11) 
- **UI Framework:** JavaFX (OpenJFX 21) 
- **Build System:** Maven 
- **Packaging:** Maven Assembly plugin to create a lightweight, self‑contained JAR with bundled JavaFX runtime.

## Rationale
| Requirement | Evaluation of Options | Decision |
|-------------|-----------------------|----------|
| Low runtime overhead | **C++/Qt** offers smallest footprint but requires a separate build toolchain and increases maintenance complexity. | JavaFX provides acceptable memory usage (~150 MB) while allowing rapid UI development.
| Cross‑platform | All options are cross‑platform, but Java already powers the existing `configtool` module, reducing effort. | Reuse existing Java knowledge and tooling.
| VS Code‑like UI | JavaFX supports custom theming, CSS styling, and flexible layout needed for an activity bar, side panels, and editor panes. | Chosen.
| Dependency size | JavaFX adds ~30 MB to the final distribution, well within the 2‑second startup target. | Acceptable.
| Community & libraries | Rich ecosystem for XML/JSON processing (Jackson, JAXB), cryptography (BouncyCastle), and testing (JUnit). | Preferred.

## Trade‑offs
- Java includes a JVM, so the installer will ship a JRE; this is mitigated by using the jlink tool to produce a custom runtime of ~30 MB.
- Not the absolute smallest possible footprint, but development speed and maintainability outweigh the modest extra size.

## Documentation
- Detailed comparison matrix stored in `docs/TECHNOLOGY_COMPARISON.md` (future).
- Build & packaging instructions are in `docs/BUILD_INSTRUCTIONS.md`.
