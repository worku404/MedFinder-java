# AGENTS.md — P2 Tensaye Daniel (MedFinder)

Version 1.2 — 13 November 2026 — aligned with documents 01–05 v1.2

> **To the AI agent reading this:** you are helping **one specific team member** of a five-person student team. Read this whole file before writing any code. Parts 1, 7, 8 and 9 are about *this person*; Parts 2–6 and 10–12 are shared by the whole team and are identical in every member's file. Your reply must always end with the **Work Report** described in Part 11.

## Part 1 — Who You Are Working For

| | |
|---|---|
| **Name** | Tensaye Daniel |
| **Code** | P2 |
| **GitHub** | @Tensaye-Daniel |
| **Role** | AI integration lead; configuration; customer and AI menus |
| **Task complexity** | High |
| **Size** | 10 classes, ≈ 415 lines of code |
| **Mission** | Make AI mode turn a request into keywords and a sub-city reliably, fall back gracefully, and build the customer's screens |
| **Your pull requests are reviewed by** | P1 Worku |
| **You review** | P1's and P3's pull requests; you are the backup for P3's services and `PasswordHasher`, and for P1's repository, CSV core and wiring |
| **Backup if you are unavailable** | P1 (AI package, `AiServiceException`, `AppConfig`), P3 (`CustomerMenu`, `AiModeMenu`) |

**How to explain things to Tensaye:** concise and technical, but **always explain the AI-safety decisions** — why a response is rejected, why no AI-written text ever reaches the screen, how the fallback works — because Tensaye will defend these at the presentation (demo steps 5 and 6 of document 04). Point out every place where an AI failure could reach the user, and how it is handled.

> **What changed for you in v1.2**
> - AI mode is **search only**: intents are SEARCH and UNSUPPORTED; there is no INFO intent, no `items[]`/strength/form, no AI-GENERATED label or caution, no 120-word limit and no 20-request limit. Removed: `AiIntent`, `RequestItem`, `AiDisclaimer`, `QueryInterpreter` and its two implementations.
> - `AiOutcome` holds a nested `enum Kind { SEARCH, UNSUPPORTED, FALLBACK }` (the only nested type allowed). `interpret(text)` never throws.
> - New for you: **`CustomerMenu`** (T35, from P3) and `support/FakeLlmClient` (from P1). `AiModeMenu` resolves the sub-city through `LocalityService.matchDistrict`.
> - `AiServiceException` is unchecked. T42 hardening uses 20 test requests.

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

You may create and edit **only** these files (paths under `src/main/java/medfinder/` unless shown otherwise). Total: **10 classes**.

| Area | Files |
|---|---|
| config | `config/AppConfig.java`; project-root `config/config.properties.example` (committed, no secret values) |
| exception | `AiServiceException.java` (extends P4's `MedFinderException`) |
| ai | `LlmClient.java`, `HttpLlmClient.java`, `PromptBuilder.java`, `AiResponseParser.java`, `AiOutcome.java`, `AiAssistantService.java` |
| resources | `src/main/resources/ai/system-prompt.txt` |
| console | `CustomerMenu.java`, `AiModeMenu.java` |
| test | `src/test/java/medfinder/support/FakeLlmClient`; `src/test/java/medfinder/ai/AiResponseParserTest`, `AiAssistantServiceTest` |

**Not yours:** `SearchService` and `SearchResult` (P1); `LocalityService` and `CatalogueService` (P5); `Session`, `ConsoleHelper` and `Menu` (P3); `FreshnessClassifier` (P4). The T22 proof-of-concept program is **throwaway**: keep it outside `src/` and never commit it with a key.

## Part 8 — Your Tasks (in order)

Full details and "Done when" criteria: document 04, Section 5.

| ID | Task | Pred. | Due | Done when |
|---|---|---|---|---|
| — | Choose the LLM service and obtain an access key for every member who runs AI mode (document 04, Section 3) | — | 13 Nov | Service chosen; keys handed out (never committed) |
| T22 | LLM service chosen; key obtained; a small Java program sends one request and prints the answer | — | 14 Nov | Real answer received from Java |
| T23 | AI contracts: `LlmClient`, `AiOutcome` (nested `Kind`, factories), `AiServiceException` | T03 (P4) | **17 Nov** | Reviewed by P1 |
| T16 | `AppConfig` (settings, environment override, `public static final` constants) and example settings file | T01 (P1) | 19 Nov | Settings and environment override work |
| T25 | `HttpLlmClient`: request, 15 s timeout, multi-catch → `AiServiceException` | T22, T23 | 20 Nov | Timeout and no-network give `AiServiceException` |
| T26 | `system-prompt.txt` and `PromptBuilder` | T23 | 21 Nov | Valid JSON for ten sample requests |
| T27 | `AiResponseParser` with tests (SEARCH with/without sub-city, UNSUPPORTED, fenced, invalid, six keywords) | T23 | 24 Nov | All sample cases pass |
| T35 (empty) | Empty `CustomerMenu` merged so `StartMenu` can open it | T10b (P3) | **25 Nov** | P5's `StartMenu` can open it |
| T30 | `AiAssistantService` with `AiAssistantServiceTest` using `FakeLlmClient` | T25, T26, T27 | 27 Nov | Every row of HLD 8.4 gives the right outcome |
| T35 | `CustomerMenu` (search by name, results table with match level, catalogue, set/clear sub-city) | T10b (P3), T24, T24b (P5), T29 (P1) | 29 Nov | AT-07 to AT-10 work |
| T36 | `AiModeMenu` (flow F5: configured check, keywords, sub-city resolution, per-keyword results, unsupported, fallback) | T29 (P1), T30, T24b (P5), T35 | 1 Dec | AT-11 to AT-15 work |
| T42 | AI hardening: prompt adjustments from real answers; 20 test requests (searches with and without sub-city, misspelt sub-city, dose question, nonsense) | T36 | 5 Dec | No AI text displayed; failures fall back correctly |
| T40 | Fix defects in your own files | T38 (P1) | 7 Dec | No open Must defect |

Delivery checklist (document 04, Section 12): AI mode tested on the presentation computer, plus plan B (disconnect the network and show the fallback).

**Others are waiting for:** AI contracts (17 Nov) → P1 for review; `AppConfig` (19 Nov) → P1, P3, P5; an empty `CustomerMenu` (25 Nov) → P5's `StartMenu`; working `AiAssistantService` (27 Nov); the AI demonstration plan. `CustomerMenu` and `AiModeMenu` are on the **critical path** at the end.

**You need from others:** exception base class (P4, 13 Nov); `ConsoleHelper` and `Menu` (P3, 16–17 Nov); `CatalogueService` and `LocalityService` (P5, 21 Nov); `SearchService` (P1, 26 Nov; signature fixed in HLD 4.7).

## Part 9 — Design Rules for Your Classes

**Golden rules of AI mode (FR-AI-03, FR-AI-06, FR-AI-10, HLD 8).**
1. The `ai` package **never** imports `service` or `repository`. `AiAssistantService` never calls `SearchService` or `LocalityService`; only `AiModeMenu` connects the outcome to search.
2. The LLM only turns text into **keywords and a sub-city**. Stock, price and pharmacy data always come from MedFinder's own data.
3. **No text written by the AI is ever shown.** The menu prints only the keywords, the sub-city, fixed Java messages and MedFinder's own results.
4. Only the customer's typed text plus the fixed instructions are sent.
5. Any failure → `AiServiceException` → FALLBACK. `interpret` never throws to the menu; AI mode never crashes the program.

**Contracts (T23) — publish early, keep small.**
- `LlmClient` interface: `String complete(String systemPrompt, String userText)`. Failures are reported as `AiServiceException` (unchecked).
- `AiOutcome` (immutable, HLD 4.8): `public enum Kind { SEARCH, UNSUPPORTED, FALLBACK }` nested inside it (**the only allowed nested type**, Part 6.3); private `final` fields; private constructor; `kind()`, `keywords()` (unmodifiable copy, `List.copyOf`), `subCity()` (null = none), `notice()`; static factory methods `search(keywords, subCity)`, `unsupported()`, `fallback(notice, typedText)` — the fallback's only keyword is the typed text.
- `AiServiceException extends MedFinderException` (unchecked) with `(message)`, `(message, cause)` and `(cause)` constructors.

**`AppConfig` (T16, HLD 7.4 and 11).** Loads `config/config.properties` with `java.util.Properties` inside try-with-resources. **A missing file is not an error** — use the defaults of HLD 11. Keys: `data.directory`, `time.zone`, `app.debug`, `ai.enabled`, `ai.endpoint`, `ai.model`, `ai.timeoutSeconds`, `location.activeRegion`, `location.districtLabel`. Environment variables override the file; proposed convention: `MEDFINDER_` + key in upper case with dots → underscores (record it in the example file and README via P1). The key comes from `MEDFINDER_AI_KEY` or `ai.apiKey` (local file only). Holds the search constants as `public static final`: `MATCH_THRESHOLD` 0.6, `WEIGHT_RELEVANCE` 0.6, `WEIGHT_FRESHNESS` 0.4, `FRESH_HOURS` 12, `AGING_HOURS` 48, `MAX_RESULTS` 20, `MAX_AI_KEYWORDS` 5 (freshness values live in P4's `Freshness` enum). Others use them through `import static`. Invalid number text → clear `ValidationException` naming the key. `config` imports nothing from the project.

**`HttpLlmClient` (T25, HLD 8.4).** The **only** class that knows the provider URL, headers and JSON message format. Uses `java.net.http.HttpClient` with the `ai.timeoutSeconds` limit (default 15). `catch (IOException | InterruptedException e)` (multi-catch) → `throw new AiServiceException(e)` (re-set the interrupt flag first); non-2xx status and missing key also → `AiServiceException`. Never print the key or full request headers.

**`PromptBuilder` + `system-prompt.txt` (T26, HLD 8.2).** Load the instructions from the classpath resource `ai/system-prompt.txt`, not from Java strings; build the request text with `StringBuilder`. The prompt tells the model to: act only as a medicine-request interpreter for a pharmacy search system in Addis Ababa; return **only** the JSON object of SRS 3.1, no other text; use SEARCH for requests to find medicines, putting each medicine **name only** in `keywords` (no strength, form or dose; at most five) and the sub-city the customer says they live in, if any, in `subCity`; use UNSUPPORTED for anything else (what a medicine is used for, dosage, diagnosis, treatment); never invent pharmacies, prices, availability or quantities.

**`AiResponseParser` (T27, HLD 8.3).** Remove code-fence markers, parse with Gson (`JsonParser`/`JsonObject`, no custom adapters), then require: `intent` is SEARCH or UNSUPPORTED; SEARCH has 1–5 keywords, each 1–60 characters after trimming; `subCity` is null or 1–40 characters. Keywords beyond five are dropped (`MAX_AI_KEYWORDS`). Any violation → `AiServiceException`. Gson's own exceptions are caught and translated (Chapter 6.6).

**`AiAssistantService` (T30, HLD 4.8, 8.4).** `boolean isConfigured()` — false when `ai.enabled` is false or no key is set. `AiOutcome interpret(String text)` — `PromptBuilder` → `LlmClient` → `AiResponseParser`; returns SEARCH or UNSUPPORTED; every `AiServiceException` becomes `AiOutcome.fallback("AI is not available right now", text)`. It never throws.

**`CustomerMenu` (T35, flows F0, F2a, F4).** `extends Menu` (P3's Template Method: `title()`, `showOptions()`, `boolean handle(int choice)`, classic `switch`). Receives `SearchService`, `CatalogueService`, `LocalityService`, `Session`, `AiModeMenu` and `ConsoleHelper` through the constructor. No login (FR-ACC-07, AT-16); never offers pharmacist or administrator actions. Options: 1 Search by name, 2 AI mode, 3 Catalogue, 4 Set sub-city, 5 Clear sub-city, 0 Back.
- **Set sub-city:** numbered list from `LocalityService.districts()` with the label from `districtLabel()` → `Session.setDistrict`. Never ask the user to type a sub-city name. **Clear** → `Session.clearDistrict()`.
- **Results table** (FR-SRC-04, FR-SRC-06, FR-SRC-09): medicine (name, strength, form), pharmacy name, sub-city and address, price, quantity (or "Out of stock"), match level label (only when a sub-city is set), freshness label. Never the licence number. Clear message when nothing matches. Write one table-printing method and reuse it in `AiModeMenu`.
- **Catalogue** (FR-CAT-02): `CatalogueService.listActive()` as a plain list, no filters.
- Agree with P5 who clears the customer's sub-city when the customer returns to the start menu (FR-ACC-08) and record it in the Work Report.

**`AiModeMenu` (T36, flow F5).** `extends Menu`; receives `AiAssistantService`, `SearchService`, `LocalityService`, `Session` and `ConsoleHelper`.
1. If `isConfigured()` is false, print "AI mode is not configured" and offer normal search (AT-15).
2. Read the request; print "Interpreting your request…" (FR-AI-12); call `interpret`.
3. **SEARCH:** print the keywords and the sub-city. If a sub-city was given, `LocalityService.matchDistrict(subCity)`; if that returns null, let the customer choose from the numbered list or skip (AT-12). Store a resolved sub-city with `Session.setDistrict`. With no sub-city in the response, use the session's current one. For each keyword call `SearchService.searchByName(keyword, district)` and print a table under the keyword (AT-11).
4. **UNSUPPORTED:** print exactly "AI mode only finds medicines in pharmacies. For questions about doses or treatment, please ask a pharmacist or doctor." (AT-13).
5. **FALLBACK:** print the notice and run a normal search on the typed text (AT-14).

**Tests (HLD 12, document 03 Section 5.9).** `FakeLlmClient implements LlmClient` returns canned text or throws — **no real network in unit tests**. `AiResponseParserTest`: valid SEARCH with and without sub-city, UNSUPPORTED, fenced JSON, missing keywords, six keywords, invalid JSON. `AiAssistantServiceTest`: every row of HLD 8.4.

**Concepts you must be able to explain** (document 05, Section 4.2): interface and polymorphism (`LlmClient` with `HttpLlmClient` and the fake); multi-catch and exception chaining (`HttpLlmClient`); `StringBuilder` (`PromptBuilder`); immutable class with static factory methods and a nested enum (`AiOutcome`); `public static final` constants and `import static` (`AppConfig`); custom exception (`AiServiceException`); `Menu` subclasses overriding abstract methods.

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
