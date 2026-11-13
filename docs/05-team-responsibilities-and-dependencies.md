# MedFinder — Team Responsibilities and Dependencies

| | |
|---|---|
| **Document** | 05 — Team Responsibilities and Dependencies |
| **Version** | 1.2 (simplified scope — see the 01 SRS change log. `CustomerMenu` moves from P3 to P2; pharmacy and stock file repositories move from P4 to P5) |
| **Date** | 13 November 2026 |
| **Depends on** | 03 Architecture and Folder Structure (class owners), 04 Implementation Plan (task IDs and dates) |

---

## 1. Purpose

This document answers two questions for every member: **what am I responsible for**, and **who needs my work, and whose work do I need**. Task IDs (T01 …) and file names refer to documents 03 and 04.

## 2. Team

| Code | Name | GitHub | Role | Classes | ≈ Lines of code | Reviewed by |
|---|---|---|---|---|---|---|
| P1 | Worku Wondoson | @worku404 | Project manager; data-access core, search engine, integration; lead presenter | 14 | 460 | P2 |
| P2 | Tensaye Daniel | @Tensaye-Daniel | AI integration lead; configuration; customer and AI menus | 10 | 415 | P1 |
| P3 | Wubet Tamrat | @wubettamrat525-prog | Business services, console helper, abstract `Menu`; presentation material | 7 | 405 | P2 |
| P4 | Tesnim Abdi | @tesnimabdi16-netizen | Model, exceptions, freshness, pharmacist menu; testing | 18 (many small) | 510 | P3 |
| P5 | Yamsin Mustefa | @Amuni | Sample data, four file repositories, locality and catalogue services, start and administrator menus, test cases, user manual | 8 + data | 335 + data | P3 |
| | | | **Total** | **57** | **≈ 2,100–2,200** | |

Lines are actual code, without Javadoc, comments, blank lines or tests. Every member must be able to explain the OOP and chapter concepts in their own classes (Section 4 lists them; document 02, Section 15.2 has the full table).

## 3. Responsibility Summary

R = responsible, S = supports, V = reviews.

| Area | R | S | V | Backup |
|---|---|---|---|---|
| Repository, build, branch rules, README | P1 | — | P2 | P2 |
| Exceptions (except `AiServiceException`), enums, model classes, `Pharmacy` status rules | P4 | P1 | P3 | P1 |
| `FreshnessClassifier` | P4 | P3 | P3 | P1 |
| `TextSimilarity`, `SearchService`, `SearchResult` | P1 | P3 | P2 | P3 |
| `PasswordHasher` | P3 | P1 | P2 | P2 |
| `CsvFile`, `RowMapper`, repository interfaces, `FileUserRepository`, `SeedDataCopier` | P1 | — | P2 | P2 |
| `FilePharmacyRepository`, `FileStockRepository`, `FileMedicineRepository`, `FileLocalityRepository` | P5 | P1, P4 | P3 | P4 |
| `LocalityService`, `CatalogueService` | P5 | P4 | P3 | P4 |
| `AppConfig` | P2 | P1 | P1 | P1 |
| `AppContext`, `Main` | P1 | — | P2 | P2 |
| `Session`, `AuthService`, `PharmacyService`, `InventoryService` | P3 | P2 | P2 | P2 |
| AI package (6 classes) and `AiServiceException` | P2 | P1 | P1 | P1 |
| `ConsoleHelper`, abstract `Menu` | P3 | P1 | P2 | P1 |
| `CustomerMenu`, `AiModeMenu` | P2 | P3 | P1 | P3 |
| `StartMenu`, `AdministratorMenu` | P5 | P3 | P3 | P4 |
| `PharmacistMenu` | P4 | P3 | P3 | P3 |
| Sample data | P5 | P3, P4 | P1 | P4 |
| Test case document, manual testing | P5, P4 | P1 | P1 | P3 |
| Integration, defect list, tuning | P1 | all | — | P3 |
| User manual | P5 | P3 | P1 | P3 |
| Slides and demonstration script | P3 | P1 | P1 | P1 |
| Presentation (lead) | P1 | P3 | — | P3 |
| Instructor communication, schedule, risks, scope | P1 | — | — | P3 |

## 4. Individual Responsibilities

### 4.1 P1 — Worku Wondoson (Project Manager, Data Access Core, Search)

**Mission:** keep the structure sound, build the data-access pattern and the search engine, integrate everything, and present the result.

| Responsibility | Tasks and files |
|---|---|
| Foundation | T01, T02: repository, Maven, packaging, README stub |
| Data access core | T06, T07, T13: 5 repository interfaces, `CsvFile`, `RowMapper`, `FileUserRepository` |
| Search engine | T17, T29: `TextSimilarity`, `SearchService`, `SearchResult` |
| Wiring | T21: `AppContext`, `Main`, `SeedDataCopier` |
| Integration and release | T38, T41, T43, T46, T47, T48 |
| Management | Daily check-in, merging, schedule and risks, instructor communication (**v1.2 scope agreement on 13 Nov**), cut decisions |

**Concepts to explain:** interfaces and a class implementing two interfaces (`FileUserRepository`); generic interface with a `default` method (`RowMapper`); checked `IOException`, try-with-resources, exception translation (`CsvFile`); 2D arrays and nested loops (`TextSimilarity`); static methods; `Comparator` and immutable `SearchResult`; composition and constructor injection (`AppContext`); `try … finally` (`Main`).

**Needs from others:** model skeleton and `Location` (P4, 13 Nov); `FreshnessClassifier` (P4, 14 Nov); `AppConfig` (P2, 19 Nov); four file repositories (P5, 19–20 Nov); AI contracts (P2, 17 Nov, for review).

**Provides to others:** project skeleton (12 Nov); repository interfaces (13 Nov); `CsvFile` (15 Nov) and the file repository pattern (17 Nov); `AppContext` (22 Nov); `SearchService` (26 Nov); reviews and merges.

### 4.2 P2 — Tensaye Daniel (AI Integration Lead, Customer-Facing Menus)

**Mission:** make AI mode turn a request into keywords and a sub-city reliably, fall back gracefully, and build the customer's screens.

| Responsibility | Tasks and files |
|---|---|
| Choice and proof of concept | T22: choose the LLM service, obtain keys, first working call |
| AI contracts | T23: `LlmClient`, `AiOutcome`, `AiServiceException` |
| Settings | T16: `AppConfig`, `config.properties.example` |
| AI components | T25, T26, T27, T30: `HttpLlmClient`, `PromptBuilder`, `system-prompt.txt`, `AiResponseParser`, `AiAssistantService`, with tests and `FakeLlmClient` |
| Menus | T35, T36: `CustomerMenu`, `AiModeMenu` |
| Hardening | T42: twenty prepared requests, prompt adjustments |
| Review | Reviews P1's and P3's pull requests; backup for P3's services |

**Concepts to explain:** interface and polymorphism (`LlmClient` with `HttpLlmClient` and the fake); multi-catch and exception chaining (`HttpLlmClient`); `StringBuilder` (`PromptBuilder`); immutable class with static factory methods and a nested enum (`AiOutcome`); `public static final` constants and `import static` (`AppConfig`); custom exception (`AiServiceException`); `Menu` subclasses overriding abstract methods.

**Needs from others:** exception base class (P4, 13 Nov); `ConsoleHelper` and `Menu` (P3, 16–17 Nov); `CatalogueService` and `LocalityService` (P5, 21 Nov); `SearchService` (P1, 26 Nov; signature fixed in HLD 4.7).

**Provides to others:** AI contracts (17 Nov); `AppConfig` (19 Nov) to P1, P3, P5; an empty `CustomerMenu` (25 Nov) so `StartMenu` can open it; working `AiAssistantService` (27 Nov); the AI demonstration plan.

### 4.3 P3 — Wubet Tamrat (Services, Console Foundation, Presentation)

**Mission:** implement the business rules that make roles, verification and stock correct, give every menu author a solid console foundation, and prepare the presentation.

| Responsibility | Tasks and files |
|---|---|
| Console foundation | T10, T10b: `ConsoleHelper`, abstract `Menu` |
| Security helper | T11: `PasswordHasher` |
| Services | T19, T20, T28: `Session`, `AuthService`, `PharmacyService`, `InventoryService` |
| Presentation | T44 (polish), T45, T47 |
| Support | Reviews P4's and P5's pull requests; pair sessions with P4 and P5 |

**Concepts to explain:** abstract class and Template Method with a `final` method (`Menu.run()`); ordered catch blocks, subclass before superclass (`Menu`); `AutoCloseable` and method overloading (`ConsoleHelper`); `Scanner` and `NumberFormatException`; `instanceof` pattern matching as a justified downcast (`AuthService.requirePharmacist`); heterogeneous `List<User>` (`listUsers`); composition (`AuthService` has a `Session`); `throw` of custom exceptions in services.

**Needs from others:** model skeleton and validation (P4, 13 and 16 Nov); exceptions (P4, 13 Nov); repository interfaces (P1, 13 Nov); `FileUserRepository` (P1, 17 Nov); `FreshnessClassifier` (P4, 14 Nov); `LocalityService` (P5, 21 Nov).

**Provides to others:** `PasswordHasher` (14 Nov, for seed hashes); `ConsoleHelper` (16 Nov) and `Menu` (17 Nov) for all menu authors; `AuthService` (21 Nov); `PharmacyService` (23 Nov); `InventoryService` (26 Nov); slides and script (8 Dec).

### 4.4 P4 — Tesnim Abdi (Model, Exceptions, Pharmacist Menu, Testing)

**Mission:** provide the shared building blocks every other member compiles against, and the pharmacist's screens.

| Responsibility | Tasks and files |
|---|---|
| Exceptions | T03: `MedFinderException`, `ValidationException`, `AuthorizationException`, `NotFoundException`, `DataStorageException` |
| Model | T04, T05a, T05b: `Role`, `PharmacyStatus`, `Freshness`, `MatchLevel`, `Location`, `User`, `Pharmacist`, `Administrator`, `Medicine`, `Pharmacy`, `StockRecord`, with `LocationTest` and `PharmacyTest` |
| Logic | T08: `FreshnessClassifier` with tests |
| Pharmacist interface | T34: `PharmacistMenu` |
| Testing | T39 (with P5), T40 |

**Concepts to explain:** abstract class with an abstract method and subclasses calling `super(...)` (`User`); overriding `toString`, `equals`, `hashCode` with `@Override`; `final` class and `final` fields (`Location`); encapsulation and invariants (`Pharmacy` status methods, no setters); constructor overloading and `this(...)` chaining (`StockRecord`, `Medicine`); exception hierarchy with four constructors (`MedFinderException`); enums with fields; a pure static function (`FreshnessClassifier`).

**Needs from others:** project skeleton (P1, 12 Nov); `ConsoleHelper` and `Menu` (P3, 16–17 Nov); `LocalityService` (P5, 21 Nov); `PharmacyService` (P3, 23 Nov); `InventoryService` (P3, 26 Nov); `CatalogueService` (P5, 21 Nov).

**Provides to others:** exceptions and model skeleton (13 Nov — the earliest and most widely needed handoff); `Location` and `MatchLevel` (13 Nov); `FreshnessClassifier` (14 Nov); validated model with status rules (16 Nov).

**First three days:** exceptions and enums (13 Nov); model skeleton with fields and constructors only, validation later (13 Nov). If late by more than one day, tell P1. Pair sessions with P3 start on 12 Nov.

### 4.5 P5 — Yamsin Mustefa (Data, Four Repositories, Locality and Catalogue, Two Menus, Test Cases)

**Mission:** supply the data the whole system runs on and the repositories that read it, two services and two menus, and the test scenarios that decide acceptance.

| Responsibility | Tasks and files |
|---|---|
| Sample data | T12: all five seed files |
| Repositories | T14, T15: `FilePharmacyRepository`, `FileStockRepository`, `FileMedicineRepository`, `FileLocalityRepository` (each following P1's `FileUserRepository` pattern) |
| Services | T24, T24b: `CatalogueService`, `LocalityService` |
| Menus | T32, T33: `StartMenu`, `AdministratorMenu` |
| Testing and documentation | T37, T39, T44 (draft) |

**Concepts to explain:** implementing two interfaces in one class and overriding interface methods (file repositories); reading and converting CSV values (`Integer.parseInt`, `Double.parseDouble`, `Instant.parse`); `switch` on an enum to choose the next menu, with dynamic dispatch of `run()` (`StartMenu`); string methods and similarity matching (`LocalityService.matchDistrict`); polymorphic printing of a `List<User>` (`AdministratorMenu`).

**Needs from others:** `PasswordHasher` (P3, 14 Nov); column layouts (document 02, Section 5); `CsvFile` (P1, 15 Nov) and `FileUserRepository` pattern (P1, 17 Nov); `TextSimilarity` (P1, 19 Nov); `ConsoleHelper` and `Menu` (P3, 16–17 Nov); `AuthService` (P3, 21 Nov); `PharmacyService` (P3, 23 Nov); empty `CustomerMenu` (P2, 25 Nov).

**Provides to others:** seed data (16 Nov); four file repositories (19–20 Nov) needed by `SearchService` and all services; `LocalityService` and `CatalogueService` (21 Nov); test scenarios (27 Nov); results of scenario runs (5 Dec).

**First three days:** medicine list of at least 40 entries (13–14 Nov); sub-city list (confirm with the team); the 12 pharmacies of document 03, Section 6.2; ask P3 for the hashing helper on 14 Nov. Pair sessions with P3 and P4 start on 12 Nov.

## 5. Who Depends on Whom

Each row reads: *the person on the left needs this from the person in the third column*.

| Who needs it | What | From | Due |
|---|---|---|---|
| Everyone | Project skeleton, build, branch rules | P1 | 12 Nov |
| P1, P2, P3, P5 | Exceptions and model skeleton, `Location`, `MatchLevel` | P4 | 13 Nov |
| P3, P5 | Repository interfaces | P1 | 13 Nov |
| P5 | `PasswordHasher` (seed hashes) | P3 | 14 Nov |
| P1 (`SearchService`), P3 (`InventoryService`), P4 (`PharmacistMenu`) | `FreshnessClassifier` | P4 | 14 Nov |
| P5 | `CsvFile` | P1 | 15 Nov |
| P1, P3, P5 | Validated model with `Pharmacy` status rules | P4 | 16 Nov |
| P1, P3 | Seed data | P5 | 16 Nov |
| All menu authors (P2, P4, P5) | `ConsoleHelper` | P3 | 16 Nov |
| All menu authors (P2, P4, P5) | Abstract `Menu` | P3 | 17 Nov |
| P5 | `FileUserRepository` as the pattern | P1 | 17 Nov |
| P1 (review) | AI contracts | P2 | 17 Nov |
| P1, P3, P5 | `AppConfig` | P2 | 19 Nov |
| P5 (`LocalityService`) | `TextSimilarity` | P1 | 19 Nov |
| P1 (`SearchService`), P3 (services) | Four file repositories | P5 | 19–20 Nov |
| P3 (`PharmacyService`), P2 (`CustomerMenu`, `AiModeMenu`), P4 (`PharmacistMenu`) | `LocalityService` | P5 | 21 Nov |
| P2 (`CustomerMenu`), P4 (`PharmacistMenu`) | `CatalogueService` | P5 | 21 Nov |
| P5 (`StartMenu`, `AdministratorMenu`) | `AuthService`, `Session` | P3 | 21 Nov |
| P1 (`AppContext` wiring) | All services as they are merged | P3, P5, P2 | 21–27 Nov |
| P4 (`PharmacistMenu`), P5 (`AdministratorMenu`) | `PharmacyService` | P3 | 23 Nov |
| P5 (`StartMenu`) | Empty `CustomerMenu` | P2 | 25 Nov |
| P4 (`PharmacistMenu`) | `InventoryService` | P3 | 26 Nov |
| P2 (`CustomerMenu`, `AiModeMenu`) | `SearchService` | P1 | 26 Nov |
| P5 (running test cases), P3 (slides) | Finished menus and features | all | 1 Dec (M3) |
| Everyone | Defect list and priorities | P1 | 2 Dec |

### Dependency Picture

```
WEEK 1
 P1 T01 skeleton ──┬──► P4 T03–T05 exceptions, enums, Location, model ──► P1 T06 interfaces
                   │                                                    └─► P3, P5 compile against the model
                   ├──► P3 T11 PasswordHasher ──► P5 T12 seed data
                   ├──► P3 T10 ConsoleHelper ──► P3 T10b Menu ──────────────► all menu authors
                   └──► P2 T22 AI spike ──► P2 T23 AI contracts

WEEK 2
 P1 T07 CsvFile ──► P1 T13 FileUserRepository ──► P5 T14, T15 four file repositories
                                                     └──► P5 T24 CatalogueService, T24b LocalityService
 P1 T17 TextSimilarity ──► P5 T24b (matchDistrict)
 P3 T19 AuthService + P5 T24b ──► P3 T20 PharmacyService
 P2 T16 config, T25 client, T26 prompt, T27 parser

WEEK 3
 P5 T14/T15 + P1 T17 + P4 T08 ──► P1 T29 SearchService ──┬──► P2 T35 CustomerMenu
                                                         └──► P2 T36 AiModeMenu
 P2 T25–T27 ──► P2 T30 AiAssistantService ────────────────────► P2 T36
 P3 T20, T28 services ──► P4 T34 PharmacistMenu, P5 T33 AdministratorMenu
 P3 T19 ──► P5 T32 StartMenu

WEEK 4
 all menus ──► P1 T38 integration ──► all T40 fixes ──► P5/P4 T39 test runs ──► P1 T46 release
 working features ──► P3 T45 slides ──► P1/P3 T47 rehearsal
```

### Critical Path

**T01 skeleton → T05a model → T06/T07 interfaces and CSV helper → T13 first file repository → T14/T15 remaining file repositories → T29 SearchService → T35 and T36 customer and AI menus → T38 integration → T40 fixes → T46 release.**

Any delay on this chain delays delivery day for day. The most sensitive handoffs are the **model skeleton (13 Nov)**, the **file repository pattern (17 Nov)**, **P5's file repositories (20 Nov)** and **SearchService (26 Nov)**.

### Who Is Depended On Most

| Person | Why | Protection |
|---|---|---|
| P4 | Model and exceptions are needed by everyone in the first week | Skeleton first, validation later; P1 reviews the same day; P3 pairs |
| P3 | `ConsoleHelper`, `Menu` and services are needed by all menu authors | Console foundation first; P2 is the backup |
| P1 | File repository pattern and `SearchService` feed several people | P1 takes no new tasks after Week 3; P3 is the backup for search |
| P5 | Four file repositories feed `SearchService` and every service | They copy one finished pattern (`FileUserRepository`); P4 supports in Week 2 (P4's Week 2 is free); P1 reviews quickly |
| P2 | Customer and AI menus are on the critical path at the end | `CustomerMenu` is short; empty version merged by 25 Nov; P3 is the backup |

## 6. Collaboration Rules

1. **One owner per file.** Only the owner edits a file. Others ask the owner, or open a pull request that the owner reviews.
2. **Contracts are fixed.** Names, inputs, outputs and exceptions in document 02, Section 4, and the class names in document 03 change only after the whole team agrees; P1 records the change.
3. **Early versions beat late perfect versions.** A class that compiles, with correct operation names and simple behaviour, is delivered first.
4. **Blocked for more than one working day → tell P1.**
5. **Reviews within one working day.**
6. **`main` always builds.** Nobody merges a change that breaks `mvn clean test`.
7. **Honest status.** "Not finished, here is what remains" is always acceptable at the check-in.
8. **Share knowledge.** Every member must be able to explain, at the presentation, the part they built, the chapter concepts in it (Section 4), and how it connects to its neighbours.

## 7. If Something Goes Wrong

| Situation | Action |
|---|---|
| A member is absent for more than two days | P1 reassigns open tasks to the backup owners in Section 3 |
| A task is behind by more than two days | P1 splits it, pairs someone in, or applies the cut order of document 04, Section 11 |
| The AI service cannot be used | P2 keeps all AI tests on `FakeLlmClient`; the fallback still demonstrates normal search; plan B demonstration prepared |
| The instructor does not accept the v1.2 scope | P1 restores the smallest removed item first (document 04, Section 10) and re-plans with the team |
| Two members need to change the same file | The owner makes the change; the other gives exact wording in the pull request |
| Disagreement on a design question | P1 decides after hearing both sides; the decision is written into the relevant document |
