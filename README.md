# MedFinder

MedFinder is a Java **command-line** application for Addis Ababa. It helps a patient find out, before travelling, which **verified pharmacy** has a medicine in stock, at what price, how recently that stock was confirmed, and whether the pharmacy is in the customer's own sub-city.

It is an Object-Oriented Programming course project built by a five-person team.

> **Status:** project skeleton (tasks T01 and T02). Build, settings and demo-account instructions are added in task T43.

## Requirements

- **Java 17 or later** (`java -version`)
- **Maven 3.8 or later** (`mvn -v`)

### Quick Maven Installation (if not already installed)

- **Windows (PowerShell — 1-minute setup, no admin required):**

  ```powershell
  Invoke-WebRequest -Uri "https://archive.apache.org/dist/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.zip" -OutFile "$env:TEMP\maven.zip"; Expand-Archive -Path "$env:TEMP\maven.zip" -DestinationPath "$HOME" -Force; [Environment]::SetEnvironmentVariable("Path", [Environment]::GetEnvironmentVariable("Path", "User") + ";$HOME\apache-maven-3.9.9\bin", "User"); $env:Path += ";$HOME\apache-maven-3.9.9\bin"; mvn -v
  ```

  _(After running, close and reopen your terminal or IDE)._

- **macOS:**

  ```bash
  brew install maven
  ```

- **Linux (Ubuntu / Debian):**
  ```bash
  sudo apt update && sudo apt install maven
  ```

## Build and run

Run these commands in the project root.

| Purpose                   | Command                          |
| ------------------------- | -------------------------------- |
| Compile and run all tests | `mvn clean test`                 |
| Build the runnable file   | `mvn clean package`              |
| Run the application       | `java -jar target/medfinder.jar` |

`mvn clean package` builds one runnable `target/medfinder.jar` that contains Gson. Until task T21 the program only prints a placeholder line.

## Project layout

```
pom.xml                  Maven build file
config/                  Example settings (the real config.properties is local and git-ignored)
data/                    Working data files, created on first run (git-ignored)
docs/                    Requirements, design, architecture, plan, team responsibilities
src/main/java/medfinder/ Production code (packages: config, model, exception, logic,
                         repository, repository.file, service, ai, console)
src/main/resources/      seed/ (sample CSV data) and ai/ (LLM instructions)
src/test/java/medfinder/ Unit tests (local helpers only)
```

The full layout, package dependency rules and class catalogue are in `docs/03-architecture-and-folder-structure.md`.

## Team workflow (branch rules)

| Item          | Rule                                                                                                                              |
| ------------- | --------------------------------------------------------------------------------------------------------------------------------- |
| Main branch   | `main` is always buildable. Nobody commits to it directly.                                                                        |
| Branches      | `feature/<area>-<short-name>`, for example `feature/service-inventory`; or `fix/<short-name>`                                     |
| Commits       | Small, with a message starting with a verb: "Add freshness classifier"                                                            |
| Pull requests | One per task. The description names the task ID from `docs/04-implementation-plan.md`. At least one review approval before merge. |
| Merging       | P1 merges after the review. `mvn clean test` must pass.                                                                           |
| Never commit  | `config/config.properties`, API keys, the `data/` folder, the `target/` folder, personal IDE files                                |

Each member edits only the files they own (see `.github/CODEOWNERS` and `docs/03-architecture-and-folder-structure.md`, Section 5).

## Team

| Code | Name           | GitHub                |
| ---- | -------------- | --------------------- |
| P1   | Worku Wondoson | @worku404             |
| P2   | Tensaye Daniel | @Tensaye-Daniel       |
| P3   | Wubet Tamrat   | @wubettamrat525-prog  |
| P4   | Tesnim Abdi    | @tesnimabdi16-netizen |
| P5   | Yamsin Mustefa | @Amuni                |

## License

See `LICENSE`.
