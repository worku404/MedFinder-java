# AGENTS.md — P4 Tesnim Abdi (MedFinder)

Version 1.2 — 13 November 2026 — aligned with documents 01–05 v1.2

> **To the AI agent reading this:** you are helping **one specific team member** of a five-person student team. Read this whole file before writing any code. Parts 1, 7, 8 and 9 are about *this person*; Parts 2–6 and 10–12 are shared by the whole team and are identical in every member's file. Your reply must always end with the **Work Report** described in Part 11.

## Part 1 — Who You Are Working For

| | |
|---|---|
| **Name** | Tesnim Abdi |
| **Code** | P4 |
| **GitHub** | @tesnimabdi16-netizen |
| **Role** | Model, exceptions, freshness, pharmacist menu; testing |
| **Task complexity** | Medium, guided |
| **Size** | 18 classes (many small), ≈ 510 lines of code |
| **Mission** | Provide the shared building blocks every other member compiles against (exceptions, enums, entities), the freshness classifier, and the pharmacist's screens |
| **Your pull requests are reviewed by** | P3 Wubet |
| **You support** | P5's four file repositories, `LocalityService` and `CatalogueService`, and the seed data format (your Week 2 is free for this); you run the acceptance tests with P5 |
| **Pair sessions** | With P3, twice a week, starting 14 Nov |
| **Backup if you are unavailable** | P1 (model, exceptions, `FreshnessClassifier`), P3 (`PharmacistMenu`, test documents) |

**How to explain things to Tesnim:** this is guided work, so go **slowly and in small steps**. Build one class at a time, and before writing it say in two or three plain sentences what the class is for and which other members use it. Explain every OOP idea the first time it appears (encapsulation, constructor validation, `abstract`, `enum` with a field, overriding `equals`/`hashCode`, constructor chaining) with a tiny example from *this* project. Prefer short, simple methods over clever ones. After each class, give Tesnim one question she can answer to check she understood it. **Your 15 Nov work blocks the whole team**: skeleton first (fields, constructors, getters), validation later. If Tesnim is more than a day behind, remind her to tell P1.

> **What changed for you in v1.2**
> - `FilePharmacyRepository` and `FileStockRepository` moved to **P5** (you support). Removed: `Locality`, `StockView`, `LocationScoring`, `AreaMatchScoring` (T09), and the exceptions `InvalidCredentialsException`, `PharmacyNotVerifiedException`, `DuplicateEntryException`.
> - `FreshnessClassifier` (T08) is confirmed as yours. `MedFinderException` is now **unchecked** with **four** constructors.
> - `Location` is region + district only (no woreda); `MatchLevel` is `SAME_DISTRICT("Same sub-city")`, `OTHER_AREA("Other area")`. `StockRecord` uses `update(quantity, price, now)` and `this(...)` chaining.
> - `PharmacistMenu` uses `title()`, `showOptions()`, `handle(int)`; the sub-city is chosen from a list.

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

**MedFinder** is a Java **command-line** application for **Addis Ababa** that helps a patient find out, before travelling, which **verified pharmacy** has a medicine in stock, at what price, how recently that stock was confirmed, and whether the pharmacy is in the customer's own sub-city. It is an **Object-Oriented Programming course project** (chapters 1–6) built by five students in four weeks (14 Nov – 11 Dec 2026): **57 classes, about 2,000–2,400 lines of code** (tests not counted).

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
| M1 | 19 Nov | Project builds; model, exceptions, console helper, abstract `Menu`, `CsvFile` and repository interfaces exist; sample data ready; AI "hello" call works from Java |
| M2 | 26 Nov | All file repositories load the seed data; login, pharmacy registration and approval, stock add work through services; search and AI parsing work in tests |
| M3 | 2 Dec | **Feature complete** — every Must requirement usable from menus. No new features after this. |
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

You may create and edit **only** these files (paths under `src/main/java/medfinder/` unless shown otherwise). Total: **18 classes**.

| Area | Files |
|---|---|
| exception (5) | `MedFinderException`, `ValidationException`, `AuthorizationException`, `NotFoundException`, `DataStorageException` (**not** `AiServiceException` — P2) |
| model (11) | `Role`, `PharmacyStatus`, `Freshness`, `MatchLevel`, `Location`, `User`, `Pharmacist`, `Administrator`, `Medicine`, `Pharmacy`, `StockRecord` (**not** `SearchResult` — P1) |
| logic (1) | `FreshnessClassifier` |
| console (1) | `PharmacistMenu` |
| test | `src/test/java/medfinder/logic/FreshnessClassifierTest`; `src/test/java/medfinder/model/LocationTest`, `PharmacyTest` |
| testing | Your share of running `docs/test-cases.md` (T39, with P5) — P5 owns the document; you fill in results |

**Not yours:** `CsvFile`, `RowMapper`, the repository interfaces (P1); all four file repositories, `LocalityService`, `CatalogueService` and the seed files (P5) — you *support* them but P5 edits them; `PharmacyService`, `InventoryService`, `Menu`, `ConsoleHelper` (P3) — you *call* them. If one of them needs a change, ask the owner.

## Part 8 — Your Tasks (in order)

Full details and "Done when" criteria: document 04, Section 5.

| ID | Task | Pred. | Due | Done when |
|---|---|---|---|---|
| T03 | Exceptions: `MedFinderException` (unchecked, four constructors) and `ValidationException`, `AuthorizationException`, `NotFoundException`, `DataStorageException` | T01 (P1) | **15 Nov** | All compile; each calls `super(...)` |
| T04 | Enums `Role`, `PharmacyStatus`, `Freshness`, `MatchLevel` (with labels and values); `final class Location` with `matchLevel`, `equals`, `hashCode` | T01 | **15 Nov** | `Location` immutable with value equality |
| T05a | Model skeleton: `User` (abstract), `Pharmacist`, `Administrator`, `Medicine`, `Pharmacy`, `StockRecord` with fields and constructors (including `this(...)` chaining) | T03, T04 | **15 Nov** | Others can compile against them |
| T08 | `FreshnessClassifier` (static) with boundary tests | T04 | 16 Nov | Tests at 12 h and 48 h pass |
| T05b | Model validation and `Pharmacy` status methods; `LocationTest`, `PharmacyTest` (with P3) | T05a | 18 Nov | Every disallowed transition (BR-11) throws `ValidationException` |
| — | Week 2: support P5 with T14 (pharmacy and stock repositories), T15, T24 | — | 20–26 Nov | — |
| T34 | `PharmacistMenu` (status and reason, register/edit pharmacy with sub-city list, resubmit, stock list/add/edit/remove, reconfirm selected or all, catalogue) | T10b, T20, T28 (P3), T24b (P5) | 2 Dec | AT-01 to AT-06, AT-21, AT-22 work |
| T39 | Execute all scenarios on three computers with P5; record results | T37 (P5), T38 (P1) | 5 Dec | Results table filled |
| T40 | Fix defects in your own files | T38 | 7 Dec | No open Must defect |

**Others are waiting for:** exceptions and model skeleton, `Location`, `MatchLevel` (15 Nov) → **P1, P2, P3, P5** (the earliest and most widely needed handoff); `FreshnessClassifier` (16 Nov) → P1 `SearchService`, P3 `InventoryService`, your own `PharmacistMenu`; validated model with `Pharmacy` status rules (18 Nov) → P1, P3, P5.

**You need from others:** project skeleton (P1, 14 Nov); `ConsoleHelper` and `Menu` (P3, 18–19 Nov); `LocalityService` and `CatalogueService` (P5, 23 Nov); `PharmacyService` (P3, 25 Nov); `InventoryService` (P3, 28 Nov).

## Part 9 — Design Rules for Your Classes

**Order of work on 15 Nov:** exceptions → enums → `Location` → entities with fields, constructors and getters only. Push that as one small pull request so others can compile. Add validation in T05b. If late by more than one day, tell P1.

**Exceptions (T03, HLD 9).** `public class MedFinderException extends RuntimeException` (**unchecked**) with four constructors: `()`, `(String message)`, `(String message, Throwable cause)`, `(Throwable cause)` — each calls `super(...)`. Subclasses extend `MedFinderException` and have at least `(String message)` and `(String message, Throwable cause)`. One-line Javadoc per class with its meaning from HLD 9:
- `ValidationException` — field empty, malformed, out of range, or **duplicate** (email, licence, second pharmacy, stock record).
- `AuthorizationException` — not logged in, wrong role, **wrong credentials**, or **pharmacy not Verified**.
- `NotFoundException` — unknown id. `DataStorageException` — file cannot be read or written (wraps the checked `IOException`).
Messages are written for the end user (plain English, no stack-trace words).

**Enums (T04).** `Role { PHARMACIST, ADMINISTRATOR }` each with a label; `PharmacyStatus { PENDING, VERIFIED, REJECTED }`; `Freshness { FRESH, AGING, STALE }` each with a label ("Fresh", "Aging", "Stale") and a ranking value (1.0, 0.6, 0.2); `MatchLevel { SAME_DISTRICT("Same sub-city"), OTHER_AREA("Other area") }`. Each has a `private final` field, a constructor and an accessor such as `label()` (HLD 6 uses `role().label()`). Teaching example: **an enum can have fields, a constructor and methods**.

**`Location` (T04, HLD 4.1) — immutable value object.**
- `public final class Location` with `private final String region` and `private final String district`.
- Constructor validates that region and district are not empty → else `ValidationException`.
- `equals` and `hashCode` overridden (same region and district, ignoring case and extra spaces) plus `toString`. Use a **private** helper `normalize(String)` inside `Location`. Do **not** call `TextSimilarity` (model must not depend on logic).
- `MatchLevel matchLevel(Location other)`: SAME_DISTRICT when region and district are equal; otherwise OTHER_AREA.
- No setters. The code word is **district**; the user-facing word "Sub-city" comes from settings (P5's `LocalityService.districtLabel()`).

**`User` family (T05a/b).** `public abstract class User` with `id`, `fullName`, `email`, `passwordHash`; validated constructor; getters; `public abstract Role role();`. `Pharmacist` and `Administrator` call `super(...)`, override `role()` and **`toString()`** (used by "list users", AT-19 — it must never include the password hash). Key teaching example of **abstract class + inheritance + polymorphism**.

**`Medicine`.** `id`, `name`, `category`, `dosageForm`, `strength`, `boolean prescriptionRequired`, `boolean active`; name, category and dosage form not empty. **Overloaded constructors** with `this(...)` chaining.

**`Pharmacy` (T05b, HLD 4.1, BR-11) — the most important encapsulation example.**
- Fields: `id`, `name`, `licenceNumber`, `Location location`, `address`, `PharmacyStatus status`, `String rejectionReason` (null when none), `int ownerUserId`.
- Constructors: one for a new pharmacy (status PENDING, reason null) and a full one used when loading from file (a REJECTED pharmacy must have a reason, a Pending or Verified one has none — BR-06).
- **No public status setter.** Only:
  - `approve()` — PENDING → VERIFIED only.
  - `reject(String reason)` — PENDING or VERIFIED → REJECTED; reason must be non-empty (AT-02, AT-20).
  - `resubmit()` — REJECTED → PENDING; clears the reason.
  - `changeDetails(String name, String licence, Location location, String address)` — validates all values; if status is VERIFIED **and** the licence or the location changed, status becomes PENDING (AT-22). A name-only or address-only edit keeps VERIFIED.
  - Every other transition throws `ValidationException` with a clear message (for example "Only a pending pharmacy can be approved.").
- Licence **uniqueness** is not checked here — that is P3's `PharmacyService`. "The service decides *who*, the entity decides *what*."

**`StockRecord` (HLD 4.1).** `pharmacyId`, `medicineId`, `int quantity` (≥ 0), `double price` (> 0), `Instant lastConfirmedAt`. Two constructors: the full one, and one that chains with `this(...)` using "now". `reconfirm(Instant now)` sets `lastConfirmedAt = now` and keeps quantity and price (AT-06). `update(int quantity, double price, Instant now)` validates and sets all three.

**`FreshnessClassifier` (T08, HLD 4.10, 7.1).** `public static Freshness classify(Instant lastConfirmedAt, Instant now)`; `Duration age = Duration.between(lastConfirmedAt, now)`; FRESH if age ≤ 12 h, AGING if ≤ 48 h, else STALE (`FRESH_HOURS`, `AGING_HOURS` from `AppConfig`). Tests (HLD 12): 11 h 59 m, exactly 12 h (FRESH), 47 h, exactly 48 h (AGING), 49 h (STALE), plus 5 / 30 / 60 h (AT-07). No `Instant.now()` inside the class.

**`PharmacistMenu` (T34, flows F1–F3).** `extends Menu` (P3's Template Method: override `title()`, `showOptions()`, `boolean handle(int choice)` with a classic `switch`). Constructor receives `PharmacyService`, `InventoryService`, `LocalityService`, `CatalogueService`, `ConsoleHelper`. Logout option.
- **Status:** pharmacy name, sub-city, address, status, and the rejection reason when REJECTED (FR-PHA-02). The licence is shown to its own pharmacist.
- **Register pharmacy:** name, licence, sub-city from a **numbered list** (`localityService.districts()`, label from `districtLabel()`), address.
- **Edit pharmacy:** before saving a licence or sub-city change on a VERIFIED pharmacy, warn that it will return to Pending and ask yes/no. **Resubmit** when REJECTED (the entity refuses otherwise).
- **Stock:** list (table: medicine name from `CatalogueService.findById`, quantity, price, last confirmed in the display time zone, freshness label from `FreshnessClassifier`); add; edit; remove (confirm yes/no); reconfirm selected (numbers separated by commas) or all. **Catalogue:** `CatalogueService.listActive()`. If the pharmacy is not VERIFIED, the service throws `AuthorizationException` — `Menu` already prints its message.
- No business rules in the menu — it only asks, calls a service, and prints.

**Testing (T39).** Follow `docs/test-cases.md` step by step, start each run from a reset `data/` folder, and record actual result, Pass/Fail, OS and date for each AT. Log every failure as an issue with the owner of the file that is wrong.

**Concepts you must be able to explain** (document 05, Section 4.4): abstract class with an abstract method and subclasses calling `super(...)` (`User`); overriding `toString`, `equals`, `hashCode` with `@Override`; `final` class and `final` fields (`Location`); encapsulation and invariants (`Pharmacy` status methods, no setters); constructor overloading and `this(...)` chaining (`StockRecord`, `Medicine`); exception hierarchy with four constructors (`MedFinderException`); enums with fields; a pure static function (`FreshnessClassifier`).

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
