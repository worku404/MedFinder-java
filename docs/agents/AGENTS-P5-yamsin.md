# AGENTS.md — P5 Yamsin Mustefa (MedFinder)

Version 1.2 — 13 November 2026 — aligned with documents 01–05 v1.2

> **To the AI agent reading this:** you are helping **one specific team member** of a five-person student team. Read this whole file before writing any code. Parts 1, 7, 8 and 9 are about *this person*; Parts 2–6 and 10–12 are shared by the whole team and are identical in every member's file. Your reply must always end with the **Work Report** described in Part 11.

## Part 1 — Who You Are Working For

| | |
|---|---|
| **Name** | Yamsin Mustefa |
| **Code** | P5 |
| **GitHub** | @Amuni |
| **Role** | Sample data, four file repositories, locality and catalogue services, start and administrator menus, test cases, user manual |
| **Task complexity** | Guided |
| **Size** | 8 classes + data, ≈ 335 lines of code + data |
| **Mission** | Supply the data the whole system runs on and the repositories that read it, two services and two menus, and the test scenarios that decide acceptance |
| **Your pull requests are reviewed by** | P3 Wubet (code and menus); P1 Worku (seed data, `test-cases.md`, user manual) |
| **Your supporters** | P1 and P4 (file repositories), P4 (services, seed format), P3 (menus, password hashes) |
| **Pair sessions** | With P3 and P4, starting 12 Nov |
| **Backup if you are unavailable** | P4 (data, repositories, services, menus), P3 (test cases, user manual) |

**How to explain things to Yamsin:** this is guided work, so use **the simplest explanations, with a concrete example from the project for every idea**. Show what a finished line of a CSV file or a finished menu screen looks like *before* writing the code that produces it. Build one method at a time and say in plain words why it exists ("`toLocation` makes sure nobody can save a sub-city that does not exist"). Explain each OOP idea the first time it appears (implementing two interfaces, constructor injection, extending `Menu`, overriding a method). Keep methods short. After each class, give Yamsin one question she can answer to check her understanding. Because Yamsin also writes the test cases and the manual, point out which acceptance test (AT-xx) each piece of code makes possible.

> **What changed for you in v1.2**
> - New for you: `FilePharmacyRepository` and `FileStockRepository` (T14, from P4). Each file repository `implements XRepository, RowMapper<X>`; the separate row-mapper classes and `Locality` are gone.
> - `LocalityService` gains `matchDistrict(text)`; `toLocation(district)` has no woreda. `CatalogueService` has no filters (`listActive`, `findById` only).
> - `StartMenu` routes by role with `switch (user.role())`; `AdministratorMenu` adds withdraw (from `listVerified`) and **list users**; the list-by-status screen is gone. Menus use `title()`, `showOptions()`, `handle(int)`.
> - Seed data: no woreda column; amoxicillin and paracetamol stocked in Bole and two other sub-cities. Acceptance scenarios are AT-01 … AT-22 from SRS Section 4.

---

## Part 2 — Hard Rules (apply to every reply, override everything except the developer's explicit instruction)

> If the developer asks you to break one of these rules, say which rule it breaks and ask them to confirm. If the change touches a shared contract (document 02, Section 4) or another member's file, tell them it needs P1's approval first.

1. **Edit only your own files.** Create or change only the files listed in *Part 7 — Files You Own*. If a change is needed in someone else's file, do **not** write it. Instead, put the exact wording of the requested change in the Work Report, section 9 ("Changes needed in other members' files").
2. **Contracts are fixed.** Class names (document 03, Section 5), operation names, inputs, outputs and exceptions (document 02, Section 4) must be used exactly as written. Do not rename, add parameters, or change exception types. If a contract seems wrong, stop and explain it in the Work Report.
3. **Plain Java console project.** Java 17, one Maven module, libraries **Gson and JUnit 5 only**. No Spring, Lombok, Guava, Apache Commons, databases, web servers, GUI (Swing/JavaFX), or any other dependency. Never edit `pom.xml` unless you are P1.
4. **Course-level Java only.** Write code using the concepts in *Part 6 — Course Scope*. Do not use streams, lambdas, method references, `record`, `var`, `Optional`, anonymous or inner classes, threads, or reflection. When the design needs something outside the course, use only the approved exceptions in Part 6 and point them out in the Work Report.
5. **Respect the package dependency rules** in Part 4. Reviewers reject any pull request that breaks them.
6. **One small task per session.** Work on one task ID (for example T17) at a time. Do not start the next task until the developer says so.
7. **Read before you write.** If the repository already contains a class you depend on, read the real file and use its real method names and types. Never guess a signature when the file exists. If it does not exist yet, follow the contract in document 02 and Part 6, and list the assumption in the Work Report, section 6.
8. **No secrets.** Never write an API key, password or token into any file except the developer's local, git-ignored `config/config.properties`. Never print the AI key in console output. The demo passwords from document 03, Section 6 are the only exception, and they belong only in the README and seed data.
9. **Honest reporting.** Never claim that code compiles or that tests pass unless you actually ran `mvn clean test` and saw the result. If you could not run it (for example in a web chat), say "Not run — please run `mvn clean test`".
10. **Always finish with a Work Report** (Part 11) after any reply that creates or changes a file. No exceptions.
11. **No business rules in menus, no printing in services or logic.** Menus call services; services and logic throw exceptions and return values.
12. **Never swallow exceptions.** No empty `catch` blocks. Catch, translate into a `MedFinderException` (keeping the cause), or let it propagate.

---

## Part 3 — Project Summary

**MedFinder** is a Java **command-line** application for **Addis Ababa** that helps a patient find out, before travelling, which **verified pharmacy** has a medicine in stock, at what price, how recently that stock was confirmed, and whether the pharmacy is in the customer's own sub-city. It is an **Object-Oriented Programming course project** (chapters 1–6) built by five students in four weeks (11 Nov – 11 Dec 2026): **57 classes, about 2,000–2,400 lines of code** (tests not counted).

| Who | What they do |
|---|---|
| **Customer** (anonymous, no account) | Searches medicines by name (typos allowed), optionally chooses a sub-city from a list, lists the catalogue, uses AI mode |
| **Pharmacist** (registered account) | Registers one pharmacy (Pending until approved), edits it, resubmits after rejection, manages stock and reconfirms it |
| **Administrator** (one seeded account) | Reviews pending pharmacies (sees the licence number), approves, rejects with reason, withdraws a Verified pharmacy with reason, lists all user accounts |

| Feature | Key rule |
|---|---|
| Freshness | Computed at display time: ≤ 12 h Fresh, ≤ 48 h Aging, else Stale. Never stored. |
| Location | **Sub-city only** (called *district* in code and data). Match levels: **Same sub-city** / **Other area**. No coordinates, no distance. |
| Ordering (BR-10) | (1) in stock before quantity 0; (2) when a location is set, Same sub-city before Other area; (3) score = 0.6 × relevance + 0.4 × freshness value (Fresh 1.0, Aging 0.6, Stale 0.2), highest first; (4) lower price; (5) pharmacy name A–Z. At most 20 results. |
| Storage | Five CSV files (`users`, `pharmacies`, `medicines`, `stock`, `localities`), read by **header name**, loaded into memory, rewritten safely on every change. No log file: stack traces are printed only when `app.debug=true`. |
| AI mode (search only) | The LLM returns `{ "intent": "SEARCH" \| "UNSUPPORTED", "keywords": [ … ], "subCity": text or null }`. MedFinder shows the keywords and sub-city it understood, then searches **its own data** for each keyword. **No AI-written text is ever shown to the customer.** UNSUPPORTED prints a fixed message; any failure falls back to a normal search on the typed text. |

### 3.1 The five project documents (your source of truth)

The developer will give you these files (in `docs/` of the repository, or pasted into the chat). All are at **version 1.2 (13 Nov 2026)**. **When this file and a document disagree, the document wins** — tell the developer. If you cannot see a document you need, ask the developer to paste the section.

| Doc | Title | Use it for |
|---|---|---|
| 01 | Software Requirements Specification (SRS) | Requirement IDs (FR-…, BR-…, NFR-…), the AI JSON shape (Section 3.1), acceptance scenarios AT-01 … AT-22 (Section 4), out-of-scope list (Section 5), changes from v1.1 (Section 7) |
| 02 | High-Level Design (HLD) | **Operation contracts (Section 4)**, CSV columns (5), flows F0–F6 (6), algorithms and constants (7), AI design (8), exceptions and catch order (9), security (10), settings keys (11), tests to write (12), OOP and chapter map (15) |
| 03 | Architecture and Folder Structure | Exact folders and file names (3), package dependency rules (4), **class catalogue of 57 classes with owners (5)**, seed data requirements (6), conventions and Git workflow (7) |
| 04 | Implementation Plan | Task IDs with predecessors, due dates and **"Done when"** criteria (5), review pairs (7.2), Definition of Done (7.3), handoffs (7.4), cut order (11) |
| 05 | Team Responsibilities and Dependencies | Who needs what from whom and when (4, 5), backups (3), concepts each member must explain (4), collaboration rules (6) |

### 3.2 Schedule

| Milestone | Date | Meaning |
|---|---|---|
| M1 | 17 Nov | Project builds; model, exceptions, console helper, abstract `Menu`, `CsvFile` and repository interfaces exist; sample data ready; AI "hello" call works from Java |
| M2 | 24 Nov | All file repositories load the seed data; login, pharmacy registration and approval, stock add work through services; search and AI parsing work in tests |
| M3 | 1 Dec | **Feature complete** — every Must requirement usable from menus. No new features after this. |
| M4 | 8 Dec | Release candidate; all AT scenarios pass; slides and manual ready |
| Delivery | 11 Dec | Submission and presentation |

---

## Part 4 — Architecture and Package Rules

Base package: `medfinder`. Source in `src/main/java/medfinder/`, tests in `src/test/java/medfinder/` (shared test helper `FakeLlmClient` in `support/`).

| Package | Responsibility | May use |
|---|---|---|
| `medfinder` (root) | `Main` (start-up), `AppContext` (wiring) | everything |
| `config` | `AppConfig` — settings file, environment, fixed search constants | nothing |
| `model` | Entities, enums, value and view objects | `exception` |
| `exception` | All custom exceptions | nothing |
| `logic` | Pure calculations: text similarity, freshness, password hashing | `model`, `exception`, `config` |
| `repository` | Storage interfaces, one per entity | `model`, `exception` |
| `repository.file` | CSV implementations (each also a `RowMapper`) | `repository`, `model`, `exception`, `config` |
| `service` | Business rules and use cases | `repository`, `logic`, `model`, `exception`, `config` |
| `ai` | LLM access, prompt, response validation, AI orchestration | `model`, `exception`, `config` |
| `console` | Menus, keyboard input, printing | `service`, `ai`, `logic` (only `FreshnessClassifier`, for labels), `model`, `exception` |

**Six rules checked on every pull request:**
1. `console` never uses `repository` directly.
2. `service` and `logic` never print or read the keyboard.
3. `ai` never uses `service` or `repository`; `AiModeMenu` connects them.
4. `model` and `exception` use nothing else (except `model` → `exception`).
5. No business rule in a menu class.
6. No class creates its own collaborators. They are passed through the constructor by `AppContext` (or by tests). This is **constructor injection**.

---

## Part 5 — Ownership Map (who owns which file)

Only the owner edits a file. If you are not the owner, do not create or change it — ask through the Work Report. This map lists all **57 classes** of document 03, Section 5.

| Owner | Classes | Other files |
|---|---|---|
| **P1 Worku** (14) | `Main`, `AppContext`; model `SearchResult`; logic `TextSimilarity`; repository `UserRepository`, `PharmacyRepository`, `MedicineRepository`, `StockRepository`, `LocalityRepository`; `repository/file/CsvFile`, `RowMapper`, `FileUserRepository`, `SeedDataCopier`; service `SearchService` | `pom.xml`, `.gitignore`, `README.md`; test `logic/TextSimilarityTest` |
| **P2 Tensaye** (10) | `config/AppConfig`; exception `AiServiceException`; ai `LlmClient`, `HttpLlmClient`, `PromptBuilder`, `AiResponseParser`, `AiOutcome`, `AiAssistantService`; console `CustomerMenu`, `AiModeMenu` | `config/config.properties.example`, `resources/ai/system-prompt.txt`; test `support/FakeLlmClient`, `ai/AiResponseParserTest`, `ai/AiAssistantServiceTest` |
| **P3 Wubet** (7) | logic `PasswordHasher`; service `Session`, `AuthService`, `PharmacyService`, `InventoryService`; console `ConsoleHelper`, `Menu` | Slides and demonstration script; polish of `docs/user-manual.md` |
| **P4 Tesnim** (18) | model `Role`, `PharmacyStatus`, `Freshness`, `MatchLevel`, `Location`, `User`, `Pharmacist`, `Administrator`, `Medicine`, `Pharmacy`, `StockRecord`; exception `MedFinderException`, `ValidationException`, `AuthorizationException`, `NotFoundException`, `DataStorageException`; logic `FreshnessClassifier`; console `PharmacistMenu` | test `logic/FreshnessClassifierTest`, `model/LocationTest`, `model/PharmacyTest`; results of test runs (T39) |
| **P5 Yamsin** (8) | `repository/file/FilePharmacyRepository`, `FileStockRepository`, `FileMedicineRepository`, `FileLocalityRepository`; service `LocalityService`, `CatalogueService`; console `StartMenu`, `AdministratorMenu` | All five `resources/seed/*.csv`; `docs/test-cases.md`; draft of `docs/user-manual.md` |

Tests: the test list is document 03, Section 5.9 (shown above). In-memory repositories are optional test helpers; add one only after P1 approves the new file.

**Review pairs:** P1 → reviewed by P2 · P2 → by P1 · P3 → by P2 · P4 → by P3 · P5 → by P3. P1 merges.

---

## Part 6 — Course Scope and Java Conventions

This is an OOP course. The instructor must recognise every concept in your code, and the developer must be able to explain it. Use what the course teaches; avoid advanced Java.

### 6.1 Course chapters (what you may use freely)

| Chapter | Topics you can use |
|---|---|
| 1 | Objects, classes, abstraction, encapsulation, inheritance, polymorphism; high cohesion, low coupling |
| 2 | Program structure, primitive types, casting, operators, `if`/`switch` statements (classic `case … break`), loops, `break`/`continue`, `Scanner`, `Math`, `String` methods, `StringBuilder`, arrays, `java.util.Arrays` |
| 3 | Classes, objects, fields, constructors, overloading, `this`, constructor chaining `this(...)`, `static`, access modifiers, getters/setters, **defensive copying**, packages and imports (including `import static`) |
| 4 | `extends`, `super(...)`, overriding with `@Override`, `final` (variables, methods, classes), `abstract` classes and methods, **Template Method pattern**, interfaces (`implements`, constants, simple `default`/`static` methods, one class implementing two interfaces), Liskov Substitution, composition over inheritance |
| 5 | Polymorphic references, dynamic binding, heterogeneous collections, Open–Closed Principle, upcasting/downcasting, `instanceof` (including pattern matching `if (x instanceof Pharmacist p)`) |
| 6 | Checked vs unchecked exceptions, `try`/`catch`/`finally`, multi-catch, `throw`, `throws`, **try-with-resources**, custom exceptions with the four canonical constructors, **exception chaining** (cause), exception translation between layers; anti-patterns to avoid |
| 7 | **Not used.** Chapter 7 is about window (GUI) applications. MedFinder is a command-line program, so never use Swing, AWT, JavaFX or any window, dialog or event-listener code. |

### 6.1b Unit tests are local helpers only

The course has not taught unit testing, so the team **does not present tests to the instructor**. Tests are still written (with AI help) because they prove the code works while you build it.
- Keep every test in `src/test/java/` only. Production code in `src/main/java/` must never depend on test code, so the program still compiles and runs when `src/test/` is removed.
- Before the demonstration, the team removes `src/test/` from the copy that is shown (recommended: a `demo` branch, so the tests are not lost).
- Spend your explanations on the **production code** the developer must understand and present. For tests, a one-line summary per test method is enough.

### 6.2 Approved exceptions (outside the chapters, needed by the design)

Use these, and only these, and name them in Work Report section 4 under "Beyond the course":

| Item | Why it is needed | How to use it simply |
|---|---|---|
| `enum` (with a field and constructor, e.g. a display label) | `Role`, `PharmacyStatus`, `Freshness`, `MatchLevel`, `AiOutcome.Kind` are fixed in the design | `enum MatchLevel { SAME_DISTRICT("Same sub-city"), OTHER_AREA("Other area"); private final String label; … }` |
| One nested type: `public enum Kind` inside `AiOutcome` (P2) | HLD 4.8 and document 03, Section 5.7 put the outcome kind inside `AiOutcome` | `public enum Kind { SEARCH, UNSUPPORTED, FALLBACK }`. **This is the only allowed nested type** in the project. |
| `List`/`ArrayList`, `Map`/`HashMap` | Repositories, CSV rows and results need growable collections | Declare as the interface (`List<Medicine> list = new ArrayList<>();`). Loop with for-each. Return `new ArrayList<>(internal)` or `List.copyOf` (defensive copy). |
| Using generic types (`List<Pharmacy>`, `Map<String,String>`) | Type safety; avoids downcasting (a Chapter 5 smell) | Only **one** generic type is *declared* in the project: P1's `RowMapper<T>`. Nobody else declares generic classes or methods. |
| `Comparable<SearchResult>` | BR-10 ordering of search results. HLD 4.7 says "one `Comparator`"; the team implements it as `Comparable` on `SearchResult` | `SearchResult implements Comparable<SearchResult>`; `compareTo` applies the BR-10 keys in order (a `null` match level counts as equal). `SearchService` calls `Collections.sort(results)`. No lambdas, no extra comparator class. |
| `java.time.Instant`, `Duration`, `ZoneId`, `Clock` | Times are stored in UTC ISO-8601 and displayed in Addis Ababa time | `Instant.parse(text)`, `instant.toString()`, `Duration.between(a, b).toHours()`. Services receive a `Clock` in the constructor and call `Instant.now(clock)` so tests can fix the time. |
| `java.nio.file.Files`, `Path`, `BufferedReader`, `BufferedWriter`, UTF-8 | CSV reading and safe writing (P1's `CsvFile`, `SeedDataCopier`) | Always inside try-with-resources. `CsvFile` throws `IOException`; each repository translates it into `DataStorageException(message, cause)`. |
| `java.util.Properties` | Settings file (P2) | `props.load(reader)` inside try-with-resources |
| `java.net.http.HttpClient` and Gson | LLM call and JSON (P2 only) | Keep inside `HttpLlmClient` and `AiResponseParser` |
| `MessageDigest` (SHA-256), `SecureRandom`, `Base64` | Salted password hashing (P3 only) | Keep inside `PasswordHasher`; store `salt:hash`, both Base64 |
| `Objects.equals`, `Objects.hash` | `equals`/`hashCode` of value objects | Override both together, plus `toString` |
| JUnit 5 (`@Test`, `assertEquals`, `assertTrue`, `fail`) | Unit tests | See 6.4 for how to test exceptions without lambdas |

### 6.3 Not allowed (even though modern Java has them)

Streams (`.stream()`, `.filter`, `.map`, `.collect`), lambdas (`->`), method references (`::`), `record`, `var`, `Optional`, anonymous classes, inner/nested classes (**the only allowed nested type is the `public enum Kind` inside `AiOutcome` (P2)**), `sealed`, switch **expressions** (`case X ->`), text blocks (`"""`), threads/concurrency, reflection, annotations other than `@Override` and JUnit's, and declaring your own generic classes or methods (except P1's `RowMapper<T>`).

### 6.4 Team Java conventions (where the documents leave a choice open)

These are defaults. **A real file already in the repository overrides them**, and documents 01–05 override both.

| Topic | Convention |
|---|---|
| Exceptions | `MedFinderException` is **unchecked** (`extends RuntimeException`) with four constructors: `()`, `(String message)`, `(String message, Throwable cause)`, `(Throwable cause)`. Each subclass calls `super(...)` and has at least `(String message)` and `(String message, Throwable cause)`. Services **do not** declare `throws` for project exceptions (document them with Javadoc `@throws`). Only `IOException` is checked: it is thrown by `CsvFile` and `HttpLlmClient` and translated at that boundary. `Menu.run()` catches, in this order: `ValidationException`, then `MedFinderException`, then `RuntimeException`. |
| Identifiers | `int`, positive. New id = largest existing id + 1 (`nextId()` in the repository). |
| Quantity / price | `int quantity` (≥ 0); `double price` in Birr (> 0). |
| Times | `Instant` in UTC. Display in `ZoneId.of(timeZone)` from settings. |
| "or none" **return** values (HLD says "X or null") | Return `null`, and say so in the Javadoc: `@return the pharmacy, or null if the pharmacist has none`. Callers check `if (x == null)`. |
| "or none" **parameters** (location, district) | `Location location` / `String district`, with `null` meaning none. |
| Lists | Never return `null` for a list; return an empty list. Return defensive copies. |
| "Current time" | Logic classes receive `now` as a parameter. Services receive a `Clock` in the constructor. |
| Menus | Template Method (Chapter 4.5): abstract `Menu` with a `final run()` loop; subclasses override `title()`, `showOptions()` and `boolean handle(int choice)`, using a classic `switch`. `StartMenu` chooses the next menu with a classic `switch (user.role())`. **The real `Menu` file from P3 is the authority** — read it before writing a menu. |
| Testing exceptions | No `assertThrows` (it needs a lambda). Use: `try { service.approve(99); fail("Expected NotFoundException"); } catch (NotFoundException e) { assertTrue(e.getMessage().contains("not found")); }` |
| Javadoc | Every public class and method has a short `/** … */` comment saying **what** it does. |
| Method size | About 40 lines at most; split into private helpers. |
| Magic numbers | Named `public static final` constants in `AppConfig` (used with `import static`) or settings. |
| Text | All user-visible text in English, short, telling the user what to do next. |
| Region names | Never write "Addis Ababa" or sub-city names in Java code. They come from `localities.csv` and settings. |

---

## Part 7 — Files You Own

You may create and edit **only** these files (paths under `src/main/java/medfinder/` unless shown otherwise). Total: **8 classes + data**.

| Area | Files |
|---|---|
| seed data | `src/main/resources/seed/medicines.csv`, `localities.csv`, `pharmacies.csv`, `stock.csv`, `users.csv` |
| repository/file | `FilePharmacyRepository`, `FileStockRepository`, `FileMedicineRepository`, `FileLocalityRepository` |
| service | `LocalityService`, `CatalogueService` |
| console | `StartMenu`, `AdministratorMenu` |
| test | None required by document 03, Section 5.9; optional local tests go under `src/test/java/medfinder/` (Part 6.1b) |
| documents | `docs/test-cases.md`; first draft of `docs/user-manual.md` (P3 polishes it) |

**Not yours:** the entities (`Medicine`, `Location`, `Pharmacy`, `StockRecord`, `User` — P4) — you *use* them; `CsvFile`, `RowMapper`, repository interfaces, `FileUserRepository`, `SeedDataCopier`, `TextSimilarity` (P1); `PasswordHasher`, `Session`, `AuthService`, `PharmacyService`, `Menu`, `ConsoleHelper` (P3); `CustomerMenu`, `AiModeMenu` (P2); `PharmacistMenu` (P4). If one of them needs a change, ask its owner.

## Part 8 — Your Tasks (in order)

Full details and "Done when" criteria: document 04, Section 5.

| ID | Task | Pred. | Due | Done when |
|---|---|---|---|---|
| T12 | Seed data: all five files (11 sub-cities, 12 pharmacies, 13 users, ≥ 40 medicines, ≥ 30 stock rows with `ageHours`) — medicines and sub-city list first (13–14 Nov); ask P3 for the hashing helper on 14 Nov | T11 (P3) | **16 Nov** | Columns exactly as HLD 5.1; sub-city list confirmed |
| T15 | `FileMedicineRepository` and `FileLocalityRepository` | T13 (P1) | 19 Nov | `findDistricts("Addis Ababa")` returns 11 sub-cities |
| T14 | `FilePharmacyRepository` and `FileStockRepository` (each also implements `RowMapper`) | T13 (P1) | 20 Nov | Load seed, save, reload |
| T24 | `CatalogueService` (`listActive`, `findById`) | T15 | 21 Nov | Works with the seed data |
| T24b | `LocalityService` (`districts`, `toLocation`, `matchDistrict` using `TextSimilarity`) | T15, T16 (P2), T17 (P1) | 21 Nov | Unknown sub-city refused; "bole" and "Bolle" match Bole |
| T32 | `StartMenu` (customer entry without login, login with `switch` on role, pharmacist registration, exit) | T10b, T19 (P3), T21 (P1), T35 empty menu (P2) | 26 Nov | AT-16, AT-18 and the login flows work |
| T37 | `test-cases.md`: every AT scenario as numbered steps with expected result | T12 | 27 Nov | Reviewed by P1 |
| T33 | `AdministratorMenu` (pending list, review with licence, approve, reject with reason, withdraw from the Verified list, list users) | T10b, T20, T19 (P3) | 30 Nov | AT-01 to AT-04 admin parts, AT-19, AT-20 work |
| T39 | Execute all scenarios on three computers with P4; record results | T37, T38 (P1) | 5 Dec | Results table filled |
| T44 | First draft of `docs/user-manual.md` (per role) | M3 | 6 Dec | Covers every menu |
| T40 | Fix defects in your own files | T38 | 7 Dec | No open Must defect |

Also: support P1 with T41 (result order with the sample data, 4 Dec) and T46 (reset data, 9 Dec); regression run of all scenarios after the last fix (8 Dec).

**Others are waiting for:** seed data (16 Nov) → P1, P3 and every demonstration; four file repositories (19–20 Nov) → P1 `SearchService` and all services (**critical path**); `LocalityService` (21 Nov) → P3 `PharmacyService`, P2 `CustomerMenu`/`AiModeMenu`, P4 `PharmacistMenu`; `CatalogueService` (21 Nov) → P2 `CustomerMenu`, P4 `PharmacistMenu`; test scenarios (27 Nov); scenario results (5 Dec).

**You need from others:** `PasswordHasher` (P3, 14 Nov); column layouts (document 02, Section 5); `CsvFile` (P1, 15 Nov) and the `FileUserRepository` pattern (P1, 17 Nov); `TextSimilarity` (P1, 19 Nov); `ConsoleHelper` and `Menu` (P3, 16–17 Nov); `AuthService` (P3, 21 Nov); `PharmacyService` (P3, 23 Nov); empty `CustomerMenu` (P2, 25 Nov).

## Part 9 — Design Rules for Your Work

**Seed data (T12, HLD 5.1–5.2, document 03 Section 6).** UTF-8, one header line, **exact column names**; quote any field that contains a comma and double quotes inside it. Booleans `true`/`false`; status and role are enum names. All names and licence numbers are **fictional**.
- `localities.csv` — `region, district`: region `Addis Ababa`, the 11 sub-cities of document 03 Section 6.1, spelled exactly the same everywhere.
- `medicines.csv` — `id, name, category, dosageForm, strength, prescriptionRequired, active`: ≥ 40 common medicines across several categories and dosage forms; must include **Amoxicillin** (AT-08) and **Paracetamol** (AT-11), **two medicines with similar names** and **at least one with `active=false`**.
- `pharmacies.csv` — `id, name, licenceNumber, region, district, address, status, rejectionReason, ownerUserId`: exactly the **12 pharmacies of document 03, Section 6.2** (ids, names, licences, sub-cities, statuses). Pharmacy 12 is REJECTED with the exact reason from Section 6.2, written inside double quotes. `ownerUserId` = the owning pharmacist's user id. Add a fictional address for each.
- `users.csv` — `id, role, fullName, email, passwordHash`: 1 administrator (`admin@medfinder.test`, password Admin@2026) and 12 pharmacists (`pharmacistN@medfinder.test`, N = pharmacy id, password Demo@2026). **Recommended ids: pharmacists 1–12 (same as their pharmacy), administrator 13** — confirm with P1 and note it in the Work Report. **Every hash must come from P3's `PasswordHasher`** (format `salt:hash`, Base64). Never type a made-up hash; the agent must never invent one either.
- `stock.csv` (sample only) — `pharmacyId, medicineId, quantity, price, ageHours`: ≥ 30 records over ≥ 6 **Verified** pharmacies; ages under 12 h, 12–48 h and over 48 h, including **5, 30 and 60** (AT-07); ≥ 2 records with quantity 0 (AT-10); **amoxicillin and paracetamol stocked in Bole and in at least two other sub-cities** (AT-09, AT-11). Prices in Birr, > 0. No duplicate (pharmacyId, medicineId) pair.
- Before pushing, check every rule above with a small table in the Work Report (row counts, statuses, ages).

**File repositories (T14, T15, HLD 4.9).** Copy P1's `FileUserRepository` pattern exactly — do not invent your own.
- `FilePharmacyRepository implements PharmacyRepository, RowMapper<Pharmacy>`: `headers()` are the `pharmacies.csv` columns; `fromRow` builds a `Location` and uses `PharmacyStatus.valueOf`; empty `rejectionReason` ↔ null. Operations: `findById`, `findByOwner`, `findByLicence` (ignoring case and spaces; null if none), `findByStatus`, `save` (insert or update, then rewrite the whole file), `nextId`.
- `FileStockRepository implements StockRepository, RowMapper<StockRecord>`: `pharmacyId, medicineId, quantity, price, lastConfirmedAt` (`Integer.parseInt`, `Double.parseDouble`, `Instant.parse` / `toString`). The `ageHours` sample column is handled by P1's `SeedDataCopier`, **not** by you. Key is (pharmacyId, medicineId): `findByPharmacy`, `findByMedicine`, `find` (null if none), `save`, `delete`, `saveAll`.
- `FileMedicineRepository implements MedicineRepository, RowMapper<Medicine>` — **read-only**: `findById` (null if none), `findActive`. Check `true`/`false` text before `Boolean.parseBoolean`.
- `FileLocalityRepository implements LocalityRepository` (no `RowMapper`, no entity class) — **read-only**: reads the district column into a `List<String>` per region; `findDistricts(region)`, `exists(region, district)` compared ignoring case and repeated spaces.
- Each repository catches `CsvFile`'s `IOException` and throws `DataStorageException(message, cause)`. A malformed row is skipped by `RowMapper.mapAll`.

**`LocalityService` (T24b, HLD 4.4).** Receives `LocalityRepository` and the settings values `activeRegion` and `districtLabel` (passed in by P1's `AppContext`).
- `activeRegion()`, `districtLabel()` return them. `districts()` returns the active region's sub-cities **sorted** (`Collections.sort(list)`).
- `Location toLocation(String district)` — refuses an unknown sub-city with `ValidationException`; uses the **official spelling** from the file.
- `String matchDistrict(String text)` — for AI mode: exact match ignoring case; otherwise the closest district with relevance ≥ 0.6 (`TextSimilarity.relevance`); otherwise `null` (AT-12).

**`CatalogueService` (T24, HLD 4.6).** Receives `MedicineRepository`. `List<Medicine> listActive()` — all active medicines, sorted by name; `Medicine findById(int id)` — `NotFoundException` when unknown. No filters.

**`StartMenu` (T32, flows F0, F1).** `extends Menu` (P3's Template Method: override `title()`, `showOptions()`, `boolean handle(int choice)` with a classic `switch`). Constructor receives `AuthService`, `CustomerMenu`, `PharmacistMenu`, `AdministratorMenu`, `ConsoleHelper` (P1's `AppContext` creates them). Options: 1 Find medicines (**no login**, AT-16), 2 Login, 3 Register as a pharmacist, 0 Exit.
- **Login:** read email and password, call `authService.login`. Never say whether the email or the password was wrong — print the service's message. Then choose the next menu with a classic `switch (user.role())`: `Menu next = pharmacistMenu;` or `administratorMenu;` then `next.run()` (dynamic dispatch). After it returns, `authService.logout()`.
- **Register:** full name, email, password typed twice (must match) → `authService.registerPharmacist`. There is **no** way to create an administrator.
- Agree with P2 who clears the customer's sub-city when the customer returns here (FR-ACC-08) and write the decision in the Work Report.

**`AdministratorMenu` (T33, flows F2, F6).** `extends Menu`. Constructor receives `PharmacyService`, `AuthService` and `ConsoleHelper`. Logout option.
- **Pending list** (`listPending`) → **Review** one pharmacy: all details including the **licence number** (only the administrator and its pharmacist see it) → Approve · Reject with reason (the service refuses an empty reason, AT-02).
- **Withdraw a Verified pharmacy:** choose from `listVerified()`, reason required, confirm yes/no (AT-20). Explain on screen that its stock will no longer appear in search.
- **List users** (`authService.listUsers()`, AT-19): loop over the `List<User>` and print each through its overridden `toString()` and `role().label()` — never any password data.
- No business rules in the menu — it only asks, calls a service, and prints.

**`docs/test-cases.md` (T37).** One section per scenario AT-01 to AT-22 (SRS Section 4). For each: ID and title, requirement IDs (FR/BR), preconditions (always "delete the `data/` folder and start the program" first), **numbered steps with the exact text to type** (for example "Type `amoxcillin`"), expected result copied from the SRS, then the empty results columns: Actual result · Pass/Fail · Tester · OS · Date. End with a summary table (AT id × computer). AT-14 and AT-15 need the AI key and Internet settings — write the exact setup steps (ask P2). Get P1's review.

**User manual draft (T44).** Sections: Starting the program and resetting data · Customer (search, AI mode, catalogue, sub-city) · Pharmacist · Administrator · Demonstration accounts · Common error messages. Screens as plain sample text copied from the real program.

**Testing (T39).** Run every scenario on three computers (one per OS if possible) with P4; record results; log each failure as an issue for the owner of the file that is wrong.

**Concepts you must be able to explain** (document 05, Section 4.5): implementing two interfaces in one class and overriding interface methods (file repositories); reading and converting CSV values (`Integer.parseInt`, `Double.parseDouble`, `Instant.parse`); `switch` on an enum to choose the next menu, with dynamic dispatch of `run()` (`StartMenu`); string methods and similarity matching (`LocalityService.matchDistrict`); polymorphic printing of a `List<User>` (`AdministratorMenu`).

---

## Part 10 — How to Work in Each Session

Follow these steps in order, every time.

1. **Confirm the task.** Repeat back in one or two sentences which task ID you are working on and its "Done when" criterion from document 04, Section 5. If the developer did not name a task, propose the next unfinished one from Part 8 and wait for a yes.
2. **Check prerequisites.** Look at the "Pred." column of the task. For each class you depend on, check whether it exists in the repository (or ask the developer to paste it). If it exists, read it. If not, follow the contract in document 02, Section 4, and record the assumption.
3. **Plan briefly.** List the files you will create or change (all must be in Part 7) and, in one line each, what each class does. Wait for the developer's approval if the plan touches more than three files or changes anything already merged.
4. **Write complete files.** Give each file in full — never "… rest unchanged". Put the full path above each code block, for example:
   `src/main/java/medfinder/logic/TextSimilarity.java`
   Include the `package` line, imports, Javadoc, and the test file.
5. **Write the tests** required by the task and by document 02, Section 12.
6. **Run or ask to run** `mvn clean test`. If you can run it, report the real result. If not, say "Not run".
7. **Finish with the Work Report** (Part 11).

**If you are blocked** (a needed class is missing and the contract is unclear, or two documents disagree): stop, explain the blocker in plain words, suggest the smallest workaround, and remind the developer that blockers longer than one working day go to P1.

**If a dependency does not exist yet:** ask its owner for an early skeleton (correct class and method names, simple behaviour). In tests, use `FakeLlmClient` from `src/test/java/medfinder/support/` (P2) or a small test-only substitute in `src/test/` (new files need P1's approval). Never put a temporary stand-in class into `src/main`.

---

## Part 11 — The Work Report (mandatory after every file change)

The developer must be able to explain this code at the review and at the presentation. The report is how they learn what you built. Write it in plain English, under the heading **`## Work Report`**, with these numbered sections. Do not skip a section; write "None" if it is empty.

1. **Task** — task ID, title, and the "Done when" criterion.
2. **Files created or changed** — a table: path · new/changed · one-line purpose.
3. **How it works** — for each class, a short walkthrough: its job, its fields, each public method in one or two sentences, and the reason for any non-obvious decision. Use the class and method names exactly.
4. **OOP and course concepts used** — a table: concept · where in the code · chapter/section of the course (for example "Encapsulation — `Pharmacy.approve()` checks the status — Ch. 3.4"). Then a second short list, **"Beyond the course"**, naming every approved exception from Part 6.2 you used and why. If you used anything not in Part 6, say so here and explain why.
5. **Contracts implemented** — which operations from document 02, Section 4 you implemented, with their exact signatures in Java.
6. **Dependencies** — which classes from other members you used, and for each: *read from the real file* or *assumed from the contract* (list the assumed signature).
7. **Tests (local only, not presented)** — one line per test method: what it checks. Keep this short (see Part 6.1b).
8. **Build result** — the real output of `mvn clean test`, or "Not run — please run `mvn clean test`".
9. **Changes needed in other members' files** — the owner, the file, and the exact change requested. "None" if none.
10. **Done-when check** — the task's criterion, and "Met / Partly met / Not met" with one sentence why.
11. **Not finished / shortcuts** — anything left out, simplified, or marked TODO.
12. **How to check it by hand** — numbered steps the developer can follow to see it work (commands to run, menu choices to type, expected output).
13. **Commit and pull request** — a branch name (`feature/<area>-<short-name>`), a commit message starting with a verb, and a pull-request description naming the task ID and the reviewer.
14. **Questions for you** — anything you need the developer to decide or confirm.
15. **Explain-it-yourself check** — two or three short questions the reviewer or instructor might ask about this code, each with a one-line answer, so the developer can practise.

---

## Part 12 — Final Reminder

Before you send any reply that contains code, check:

- [ ] Every file I wrote is in Part 7 (my files).
- [ ] Class and method names match documents 02 and 03 exactly.
- [ ] No package dependency rule (Part 4) is broken.
- [ ] Only course-level Java plus approved exceptions (Part 6). No streams, lambdas, `record`, `var`, `Optional`.
- [ ] No secrets in any file.
- [ ] Tests included.
- [ ] I did not claim the build passed unless I ran it.
- [ ] The reply ends with the full **Work Report**.
