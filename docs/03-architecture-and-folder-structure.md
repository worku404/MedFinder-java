* [ ] 

# MedFinder — Architecture and Folder Structure

|                      |                                                                       |
| -------------------- | --------------------------------------------------------------------- |
| **Document**   | 03 — Architecture and Folder Structure                               |
| **Version**    | 1.2 (simplified scope — see the 01 SRS change log; 78 → 57 classes) |
| **Date**       | 13 November 2026                                                      |
| **Depends on** | 01 SRS, 02 High-Level Design                                          |

**Owner codes used in this document** (full details in document 05): **P1** Worku Wondoson · **P2** Tensaye Daniel · **P3** Wubet Tamrat · **P4** Tesnim Abdi · **P5** Yamsin Mustefa.

---

## 1. Purpose

This document fixes the physical layout of the project: the technology and build set-up, every folder and package, every class that must exist, who owns it, the rules about which package may use which, and the conventions all members follow. It contains no program code. Members create their files exactly where this document says, with exactly these names, so that the parts fit together when merged.

## 2. Technology and Build

| Item            | Choice                                                                              |
| --------------- | ----------------------------------------------------------------------------------- |
| Language        | Java 17 or later                                                                    |
| Build tool      | Maven (single module)                                                               |
| Libraries       | Gson (JSON); JUnit 5 (tests). Nothing else.                                         |
| Packaging       | `mvn package` produces one runnable file `target/medfinder.jar` containing Gson |
| Entry point     | class `medfinder.Main`                                                            |
| Version control | Git, hosted on GitHub; one shared repository                                        |

Everyday commands (run in the project root):

| Purpose                   | Command                            |
| ------------------------- | ---------------------------------- |
| Compile and run all tests | `mvn clean test`                 |
| Build the runnable file   | `mvn clean package`              |
| Run the application       | `java -jar target/medfinder.jar` |

## 3. Repository Layout

```
medfinder/
├── pom.xml                              Maven build file
├── README.md                            What it is, how to build, configure and run
├── .gitignore                           target/, data/, config/config.properties, IDE folders
│
├── config/
│   └── config.properties.example        Example settings (committed); real file is local and ignored
│
├── data/                                Working data files, created on first run (git-ignored)
│
├── docs/
│   ├── 01-software-requirements-specification.md
│   ├── 02-high-level-design.md
│   ├── 03-architecture-and-folder-structure.md
│   ├── 04-implementation-plan.md
│   ├── 05-team-responsibilities-and-dependencies.md
│   ├── user-manual.md                   How each role uses the program
│   └── test-cases.md                    Manual test scenarios and results
│
└── src/
    ├── main/
    │   ├── java/medfinder/
    │   │   ├── Main.java
    │   │   ├── AppContext.java
    │   │   ├── config/                  AppConfig
    │   │   ├── model/                   12 classes
    │   │   ├── exception/               6 classes
    │   │   ├── logic/                   3 classes
    │   │   ├── repository/              5 interfaces
    │   │   │   └── file/                8 classes
    │   │   ├── service/                 7 classes
    │   │   ├── ai/                      6 classes
    │   │   └── console/                 7 classes
    │   └── resources/
    │       ├── seed/                    Sample data copied to data/ on first run
    │       │   ├── users.csv
    │       │   ├── pharmacies.csv
    │       │   ├── medicines.csv
    │       │   ├── stock.csv                (uses ageHours; converted on first run)
    │       │   └── localities.csv
    │       └── ai/
    │           └── system-prompt.txt    Fixed instructions sent to the LLM
    │
    └── test/
        └── java/medfinder/
            ├── support/                 FakeLlmClient (in-memory repositories optional)
            ├── logic/                   TextSimilarityTest, FreshnessClassifierTest
            ├── model/                   LocationTest, PharmacyTest
            └── ai/                      AiResponseParserTest, AiAssistantServiceTest
```

## 4. Packages, Responsibilities and Allowed Dependencies

| Package              | Responsibility                                                  | May use                                                                                            |
| -------------------- | --------------------------------------------------------------- | -------------------------------------------------------------------------------------------------- |
| `medfinder` (root) | Start-up (`Main`) and wiring (`AppContext`)                 | everything                                                                                         |
| `config`           | Read settings from file and environment; fixed constants        | —                                                                                                 |
| `model`            | Entities, enums, value and view objects                         | `exception`                                                                                      |
| `exception`        | All custom exception classes                                    | —                                                                                                 |
| `logic`            | Pure calculations: text similarity, freshness, password hashing | `model`, `exception`, `config`                                                               |
| `repository`       | Storage interfaces, one per entity                              | `model`, `exception`                                                                           |
| `repository.file`  | CSV-file implementations of those interfaces                    | `repository`, `model`, `exception`, `config`                                               |
| `service`          | Business rules and use cases                                    | `repository`, `logic`, `model`, `exception`, `config`                                    |
| `ai`               | LLM access, prompt, response validation, AI orchestration       | `model`, `exception`, `config`                                                               |
| `console`          | Menus, keyboard input, printing                                 | `service`, `ai`, `logic` (only `FreshnessClassifier` for labels), `model`, `exception` |

**Rules that reviewers check on every pull request**

1. `console` never uses `repository` directly.
2. `service` and `logic` never print or read from the keyboard.
3. `ai` never uses `service` or `repository` (the AI menu connects them).
4. `model` and `exception` use nothing from other packages (except `model` → `exception`).
5. No business rule appears in a menu class.
6. No class creates its collaborators; they are passed in through the constructor by `AppContext` (or by tests).

## 5. Class Catalogue (57 classes)

### 5.1 Root and config (3)

| Class          | Kind  | Responsibility                                                                                                                                                 | Owner |
| -------------- | ----- | -------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----- |
| `Main`       | class | Creates `AppContext`, runs `StartMenu` inside `try … finally` (prints "Goodbye", closes input)                                                          | P1    |
| `AppContext` | class | Builds repositories, services, AI components and menus once; calls `SeedDataCopier` on first run                                                             | P1    |
| `AppConfig`  | class | Loads `config.properties`, applies environment overrides, exposes typed settings; holds the `public static final` search constants (High-Level Design 7.4) | P2    |

### 5.2 model (12)

| Class                             | Kind                                     | Responsibility                                                                                                                                          | Owner |
| --------------------------------- | ---------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------- | ----- |
| `Role`                          | enum                                     | PHARMACIST, ADMINISTRATOR, each with a label                                                                                                            | P4    |
| `PharmacyStatus`                | enum                                     | PENDING, VERIFIED, REJECTED                                                                                                                             | P4    |
| `Freshness`                     | enum                                     | FRESH, AGING, STALE, each with a label and a ranking value (1.0, 0.6, 0.2)                                                                              | P4    |
| `MatchLevel`                    | enum                                     | SAME_DISTRICT ("Same sub-city"), OTHER_AREA ("Other area")                                                                                              | P4    |
| `Location`                      | `final` class (immutable value object) | Region and district; validation; `equals`, `hashCode`, `toString`; `matchLevel(other)`                                                          | P4    |
| `User`                          | abstract class                           | id, full name, email, password hash; `abstract Role role()`                                                                                           | P4    |
| `Pharmacist`, `Administrator` | classes                                  | Subclasses of `User`; call `super(...)`; override `role()` and `toString()`                                                                     | P4    |
| `Medicine`                      | class                                    | Catalogue entry with validation; overloaded constructors                                                                                                | P4    |
| `Pharmacy`                      | class                                    | Name, licence, `Location`, address, status and rejection reason; status changes only through `approve`, `reject`, `resubmit`, `changeDetails` | P4    |
| `StockRecord`                   | class                                    | Quantity, price, last-confirmed time; `reconfirm(now)`, `update(quantity, price, now)`; constructor chaining with `this(...)`                     | P4    |
| `SearchResult`                  | class (immutable)                        | One result row: medicine, pharmacy, price, quantity, match level (or null), freshness, score                                                            | P1    |

### 5.3 exception (6)

| Class                      | Responsibility                                                             | Owner |
| -------------------------- | -------------------------------------------------------------------------- | ----- |
| `MedFinderException`     | Unchecked base class (`extends RuntimeException`) with four constructors | P4    |
| `ValidationException`    | Bad or duplicate input                                                     | P4    |
| `AuthorizationException` | Not logged in, wrong role, wrong credentials, pharmacy not Verified        | P4    |
| `NotFoundException`      | Unknown id                                                                 | P4    |
| `DataStorageException`   | File problem (wraps `IOException`)                                       | P4    |
| `AiServiceException`     | AI call or response failed                                                 | P2    |

### 5.4 logic (3)

| Class                   | Responsibility                                                      | Owner |
| ----------------------- | ------------------------------------------------------------------- | ----- |
| `TextSimilarity`      | `static` normalise, Levenshtein distance (`int[][]`), relevance | P1    |
| `FreshnessClassifier` | `static` age → `Freshness`                                     | P4    |
| `PasswordHasher`      | `static` salted SHA-256 hash and verify                           | P3    |

### 5.5 repository (5) and repository.file (8)

| Class                                                                                                           | Kind                                                          | Responsibility                                                                                                                                                               | Owner |
| --------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----- |
| `UserRepository`, `PharmacyRepository`, `MedicineRepository`, `StockRepository`, `LocalityRepository` | interfaces                                                    | Contracts in High-Level Design, Section 4.9                                                                                                                                  | P1    |
| `file/CsvFile`                                                                                                | class                                                         | Read all rows **by header name** into `List<Map<String,String>>`; write all rows safely (temporary file, then replace); quoting and escaping; `throws IOException` | P1    |
| `file/RowMapper<T>`                                                                                           | generic interface                                             | `String[] headers()`, `T fromRow(Map)`, `Map toRow(T)`, `default List<T> mapAll(rows)` (skips malformed rows with a warning)                                         | P1    |
| `file/FileUserRepository`                                                                                     | class, `implements UserRepository, RowMapper<User>`         | users.csv; creates `Pharmacist` or `Administrator` from the role column                                                                                                  | P1    |
| `file/FilePharmacyRepository`                                                                                 | class, `implements PharmacyRepository, RowMapper<Pharmacy>` | pharmacies.csv                                                                                                                                                               | P5    |
| `file/FileStockRepository`                                                                                    | class, `implements StockRepository, RowMapper<StockRecord>` | stock.csv                                                                                                                                                                    | P5    |
| `file/FileMedicineRepository`                                                                                 | class, `implements MedicineRepository, RowMapper<Medicine>` | medicines.csv (read-only)                                                                                                                                                    | P5    |
| `file/FileLocalityRepository`                                                                                 | class, `implements LocalityRepository`                      | localities.csv into `List<String>` per region (read-only)                                                                                                                  | P5    |
| `file/SeedDataCopier`                                                                                         | class                                                         | First run: copy seed files to `data/`, converting `ageHours` to `lastConfirmedAt`                                                                                      | P1    |

### 5.6 service (7)

| Class                | Responsibility                                                                                      | Owner |
| -------------------- | --------------------------------------------------------------------------------------------------- | ----- |
| `Session`          | Current user (or null) and the customer's chosen district (or null)                                 | P3    |
| `AuthService`      | Register pharmacists, login, logout, `requirePharmacist`, `requireAdministrator`, `listUsers` | P3    |
| `PharmacyService`  | Pharmacy registration, editing, resubmit, approve, reject, withdraw, list pending/verified          | P3    |
| `InventoryService` | Stock add, edit, remove, reconfirm one/several/all                                                  | P3    |
| `LocalityService`  | Active region, its sub-cities, district label, `toLocation`, `matchDistrict` (AI)               | P5    |
| `CatalogueService` | `listActive`, `findById`                                                                        | P5    |
| `SearchService`    | Matching, score, one `Comparator` for BR-10 ordering                                              | P1    |

### 5.7 ai (6)

| Class                  | Kind              | Responsibility                                                                                     | Owner |
| ---------------------- | ----------------- | -------------------------------------------------------------------------------------------------- | ----- |
| `LlmClient`          | interface         | `String complete(String systemPrompt, String userText)`                                          | P2    |
| `HttpLlmClient`      | class             | Web API call (`java.net.http`) with time limit; multi-catch → `AiServiceException`            | P2    |
| `PromptBuilder`      | class             | Loads `system-prompt.txt` and builds the request text (`StringBuilder`)                        | P2    |
| `AiResponseParser`   | class             | JSON (Gson) → validated `AiOutcome`                                                             | P2    |
| `AiOutcome`          | class (immutable) | Nested enum `Kind {SEARCH, UNSUPPORTED, FALLBACK}`, keywords, sub-city, notice; static factories | P2    |
| `AiAssistantService` | class             | `isConfigured()`, `interpret(text)`; turns every failure into FALLBACK                         | P2    |

### 5.8 console (7)

| Class                 | Responsibility                                                                                                                                                                    | Owner |
| --------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----- |
| `ConsoleHelper`     | `implements AutoCloseable`; wraps one `Scanner`; overloaded `readInt` and `readText`; yes/no; choose from a numbered list; aligned tables                                 | P3    |
| `Menu`              | Abstract class: `final run()` (template), `abstract title()`, `abstract showOptions()`, `abstract boolean handle(int choice)`; ordered catch blocks (High-Level Design 9) | P3    |
| `StartMenu`         | 1 Find medicines (no login), 2 Login, 3 Register as a pharmacist, 0 Exit. After login: `switch (user.role())` → `PharmacistMenu` or `AdministratorMenu`                    | P5    |
| `CustomerMenu`      | 1 Search by name, 2 AI mode, 3 Catalogue, 4 Set sub-city, 5 Clear sub-city, 0 Back                                                                                                | P2    |
| `AiModeMenu`        | AI flow F5 (High-Level Design 6)                                                                                                                                                  | P2    |
| `PharmacistMenu`    | Pharmacy status/reason, register (sub-city list), edit, resubmit, stock list/add/edit/remove/reconfirm, catalogue                                                                 | P4    |
| `AdministratorMenu` | Pending list, review (shows licence), approve, reject, withdraw a Verified pharmacy, list users                                                                                   | P5    |

### 5.9 test

| Folder       | Content                                              | Owner  |
| ------------ | ---------------------------------------------------- | ------ |
| `support/` | `FakeLlmClient` (returns canned text or throws)    | P2     |
| `logic/`   | `TextSimilarityTest`, `FreshnessClassifierTest`  | P1, P4 |
| `model/`   | `LocationTest`, `PharmacyTest`                   | P4     |
| `ai/`      | `AiResponseParserTest`, `AiAssistantServiceTest` | P2     |

## 6. Resources and Data Files

| File                                 | Content requirement                                                                                                                                                                                                                                                                                                                                 | Owner      |
| ------------------------------------ | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ---------- |
| `seed/medicines.csv`               | At least 40 common medicines across several categories and dosage forms, including two with similar names and at least one inactive                                                                                                                                                                                                                 | P5         |
| `seed/localities.csv`              | The 11 sub-cities of Addis Ababa (Section 6.1), region "Addis Ababa"                                                                                                                                                                                                                                                                                | P5         |
| `seed/pharmacies.csv`              | The 12 fictional pharmacies of Section 6.2                                                                                                                                                                                                                                                                                                          | P5         |
| `seed/stock.csv`                   | Columns `pharmacyId, medicineId, quantity, price, ageHours`. At least 30 records over at least 6 Verified pharmacies, with ages under 12 h, 12–48 h and over 48 h (including 5, 30 and 60 for AT-07), and at least 2 with quantity 0. Amoxicillin and paracetamol are stocked in Bole and in at least two other sub-cities (for AT-09 and AT-11) | P5         |
| `seed/users.csv`                   | 1 administrator and 12 pharmacists (one per pharmacy); password hashes generated with `PasswordHasher`                                                                                                                                                                                                                                            | P5 with P3 |
| `ai/system-prompt.txt`             | Fixed instructions of High-Level Design, Section 8.2                                                                                                                                                                                                                                                                                                | P2         |
| `config/config.properties.example` | All keys of High-Level Design, Section 11                                                                                                                                                                                                                                                                                                           | P2         |

**Demonstration accounts** (demonstration use only; passwords documented in the README)

| Role                 | Email                                                                            | Password           |
| -------------------- | -------------------------------------------------------------------------------- | ------------------ |
| Administrator        | admin@medfinder.test                                                             | Admin@2026         |
| Pharmacists (all 12) | pharmacist1@medfinder.test … pharmacist12@medfinder.test (number = pharmacy id) | Demo@2026 (shared) |

To reset before a demonstration, delete the `data` folder and start the program again.

### 6.1 Sub-cities (`localities.csv`)

Addis Ketema · Akaky Kaliti · Arada · Bole · Gullele · Kirkos · Kolfe Keranio · Lemi Kura · Lideta · Nifas Silk-Lafto · Yeka

### 6.2 Sample Pharmacies

All names and licence numbers are fictional.

| Id | Name                      | Licence    | Sub-city         | Status                                                                                          |
| -- | ------------------------- | ---------- | ---------------- | ----------------------------------------------------------------------------------------------- |
| 1  | Selam Pharmacy            | AA-PH-1001 | Bole             | Verified                                                                                        |
| 2  | Tena Pharmacy             | AA-PH-1002 | Bole             | Verified                                                                                        |
| 3  | Bethel Pharmacy           | AA-PH-1003 | Bole             | Verified                                                                                        |
| 4  | Arada Health Pharmacy     | AA-PH-1004 | Arada            | Verified                                                                                        |
| 5  | Piazza Care Pharmacy      | AA-PH-1005 | Arada            | Verified                                                                                        |
| 6  | Kirkos Community Pharmacy | AA-PH-1006 | Kirkos           | Verified                                                                                        |
| 7  | Lideta Family Pharmacy    | AA-PH-1007 | Lideta           | Verified                                                                                        |
| 8  | Yeka Hope Pharmacy        | AA-PH-1008 | Yeka             | Verified                                                                                        |
| 9  | Gullele Green Pharmacy    | AA-PH-1009 | Gullele          | Verified                                                                                        |
| 10 | Nifas Silk Pharmacy       | AA-PH-1010 | Nifas Silk-Lafto | Pending                                                                                         |
| 11 | Kolfe Pharmacy            | AA-PH-1011 | Kolfe Keranio    | Pending                                                                                         |
| 12 | Akaky Pharmacy            | AA-PH-1012 | Akaky Kaliti     | Rejected — "Licence number does not match the document provided; please correct and resubmit." |

Pharmacies 1–3 share Bole and 4–5 share Arada, so "Same sub-city first" can be demonstrated.

### 6.3 How to Add a Field (checklist)

1. Add the private field, validation and getter to the entity, for example `Pharmacy`.
2. Add the column name and its conversion in `FilePharmacyRepository` (`headers`, `fromRow`, `toRow`).
3. Add the column to the seed file (an empty value is read as absent, so old files still load).
4. Add one prompt to the menu that creates or edits the entity.

No other class changes, because `CsvFile` reads columns by name.

## 7. Conventions

### 7.1 Naming

| Item                   | Rule                                          | Example                                      |
| ---------------------- | --------------------------------------------- | -------------------------------------------- |
| Packages               | lower case                                    | `medfinder.service`                        |
| Classes and interfaces | UpperCamelCase, nouns                         | `InventoryService`                         |
| Methods                | lowerCamelCase, verbs                         | `reconfirmAll`                             |
| Constants              | UPPER_SNAKE_CASE                              | `MATCH_THRESHOLD`                          |
| Interfaces             | no `I` prefix; implementations describe how | `StockRepository`, `FileStockRepository` |
| Test classes           | class name + `Test`                         | `FreshnessClassifierTest`                  |

### 7.2 Code Style

- One top-level class per file; file name equals class name.
- Fields are private. Entities validate in constructors. Status fields have no public setter.
- Value objects (`Location`, `SearchResult`, `AiOutcome`) are immutable: `final` fields, no setters.
- Depend on interfaces, not implementations, except in `AppContext`.
- Menus use `switch` on the typed number; no lambdas or method references in menus.
- Never write a region or sub-city name in Java code; read them from `localities.csv` and the settings.
- Every public class and method has a short Javadoc comment saying what it does.
- No method longer than about 40 lines; split instead.
- No magic numbers: hours, weights and limits are named constants in `AppConfig`.
- Never leave a catch block empty. Catch, translate into a `MedFinderException`, or let it propagate.
- Never print stack traces to the user unless `app.debug=true`.
- All user-visible text is in English, short, and says what to do next.

### 7.3 Git Workflow

| Item          | Rule                                                                                                                |
| ------------- | ------------------------------------------------------------------------------------------------------------------- |
| Main branch   | `main` is always buildable; nobody commits to it directly                                                         |
| Branches      | `feature/<area>-<short-name>`, for example `feature/service-inventory`; `fix/<short-name>`                    |
| Commits       | Small, with a message starting with a verb: "Add freshness classifier"                                              |
| Pull requests | One per task; description names the task ID from the Implementation Plan; at least one review approval before merge |
| Merging       | P1 merges after the review; `mvn clean test` must pass                                                            |
| Never commit  | `config.properties`, API keys, the `data` folder, the `target` folder, personal IDE files                     |

### 7.4 Settings and Secrets

The real `config/config.properties` and any LLM key stay on each member's own computer. The example file shows the keys without secret values. If a key is ever committed by mistake, tell P1 immediately; the key must be revoked and replaced.
