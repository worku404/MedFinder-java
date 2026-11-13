# MedFinder — Implementation Plan

| | |
|---|---|
| **Document** | 04 — Implementation Plan |
| **Version** | 1.2 (simplified scope — see the 01 SRS change log. Removed tasks: T06b (optional now), T09, T18, T24 filters. Changed owners: T14 → P5, T35 → P2) |
| **Date** | 13 November 2026 |
| **Depends on** | 01 SRS, 02 High-Level Design, 03 Architecture and Folder Structure |
| **Companion** | 05 Team Responsibilities and Dependencies |

**Owner codes:** **P1** Worku Wondoson (project manager) · **P2** Tensaye Daniel · **P3** Wubet Tamrat · **P4** Tesnim Abdi · **P5** Yamsin Mustefa.

---

## 1. Purpose

This plan turns the requirements and design into work: which phase the project is in, the schedule, every task with its owner, supporting person, predecessors, due date and completion criteria, how the team reviews, tests and integrates, what could go wrong, and what to cut if time runs short.

**Size target (v1.2):** 57 Java classes, about **2,000–2,400 lines of code** (about 2,800–3,200 with Javadoc, comments and blank lines), tests not counted. That is about 400–500 lines per member.

## 2. Development Lifecycle

| Phase | Purpose | Status | Output |
|---|---|---|---|
| 1. Planning | Problem, objectives, scope, feasibility, team, schedule | **Complete**, open items in Section 3 | This plan |
| 2. Requirements | What the system must do | **Complete (v1.2)** | Document 01 (SRS) |
| 3. Design | Layers, components, data, algorithms, AI | **Complete (v1.2)** | Documents 02 and 03 |
| 4. Implementation | Write the code | **Starts 14 Nov** | Source code, unit tests |
| 5. Testing | Verify against the SRS acceptance scenarios | Overlaps phase 4; main test period 3–8 Dec | Test results, bug fixes |
| 6. Delivery | Package, document, demonstrate | 8–11 Dec | Release v1.0, user manual, slides, demo |
| 7. Maintenance | After delivery | Outside this semester | Handover notes in the README |

## 3. Planning Checklist (open items)

| Item | Status | Owner | Due |
|---|---|---|---|
| Confirm the delivery date and what is submitted with the instructor | **To do** | P1 | 15 Nov |
| **Get the instructor's agreement to the v1.2 scope** (SRS Section 7: AI search only, sub-city only, removed requirements) | **To do** | P1 | 15 Nov |
| Confirm with the instructor that a command-line application is acceptable | **To do** | P1 | 15 Nov |
| Choose the LLM service and obtain an access key for every member who runs AI mode | **To do** | P2 | 15 Nov |
| Create the GitHub repository and invite all members | **To do** | P1 | 14 Nov |
| Install Java 17+, Maven and an editor; confirm `mvn -v` works | **To do** | everyone | 14 Nov |

**Planning assumption:** delivery on **Friday 11 December 2026**. If the date changes, P1 shifts Week 4 and the buffer, not the earlier weeks.

## 4. Schedule Overview

| Week | Dates | Theme | Milestone at the end of the week |
|---|---|---|---|
| 1 | Sat 14 – Thu 19 Nov | Foundation | **M1 (19 Nov):** project builds; model, exceptions, console helper, abstract `Menu`, `CsvFile` and repository interfaces exist; sample data ready; AI "hello" call works from Java |
| 2 | Fri 20 – Thu 26 Nov | Data access and services | **M2 (26 Nov):** all file repositories load the seed data; login, pharmacy registration and approval, stock add work through services; search and AI parsing work in tests |
| 3 | Fri 27 Nov – Wed 2 Dec | Menus | **M3 (2 Dec): feature complete.** Every Must requirement can be used from the menus |
| 4 | Thu 3 – Tue 8 Dec | Integration, testing, delivery material | **M4 (8 Dec):** release candidate; all acceptance scenarios pass; slides and manual ready |
| Buffer | Wed 9 – Thu 10 Dec | Rehearsal and emergency fixes | Tag v1.0 |
| Delivery | Fri 11 Dec | Submission and presentation | — |

After M3 no new features are added.

## 5. Work Breakdown

Effort is in working days for one person (estimate). "Pred." lists tasks that must be finished (or have a usable early version) first.

### 5.1 Setup and Foundation (Week 1)

| ID | Task | Owner | Support | Pred. | Effort | Due | Done when |
|---|---|---|---|---|---|---|---|
| T01 | Repository, Maven project, `.gitignore`, package folders, README stub, branch rules | P1 | — | — | 0.5 | 14 Nov | Everyone can clone and run `mvn clean test` |
| T02 | Gson and JUnit 5; packaging into `target/medfinder.jar` | P1 | — | T01 | 0.5 | 14 Nov | `java -jar` starts a placeholder |
| T03 | Exceptions: `MedFinderException` (unchecked, four constructors) and `ValidationException`, `AuthorizationException`, `NotFoundException`, `DataStorageException` | P4 | P1 | T01 | 0.5 | 15 Nov | All compile; each calls `super(...)` |
| T04 | Enums `Role`, `PharmacyStatus`, `Freshness`, `MatchLevel` (with labels and values); `final class Location` with `matchLevel`, `equals`, `hashCode` | P4 | P1 | T01 | 1 | 15 Nov | `Location` immutable with value equality |
| T05a | Model skeleton: `User` (abstract), `Pharmacist`, `Administrator`, `Medicine`, `Pharmacy`, `StockRecord` with fields and constructors (including `this(...)` chaining) | P4 | P1 | T03, T04 | 1 | 15 Nov | Others can compile against them |
| T05b | Model validation and `Pharmacy` status methods; `LocationTest`, `PharmacyTest` | P4 | P3 | T05a | 1.5 | 18 Nov | Every disallowed transition (BR-11) throws `ValidationException` |
| T06 | Repository interfaces (5) | P1 | — | T05a | 0.5 | 15 Nov | Match HLD 4.9 |
| T07 | `CsvFile` (read by header into `List<Map<String,String>>`, safe write, quoting; try-with-resources; `throws IOException`) and `RowMapper<T>` with `default mapAll` | P1 | — | T01 | 1.5 | 17 Nov | Round trip with commas and quotes; columns in a different order read correctly; a malformed row is skipped with a warning |
| T08 | `FreshnessClassifier` (static) with boundary tests | P4 | P3 | T04 | 0.5 | 16 Nov | Tests at 12 h and 48 h pass |
| T10 | `ConsoleHelper` (`implements AutoCloseable`; overloaded `readInt`/`readText`; yes/no; numbered-list choice; aligned table; catches `NumberFormatException`) | P3 | P1 | T01 | 1.5 | 18 Nov | Demonstrated with a small test menu |
| T10b | Abstract `Menu`: `final run()` with `do … while`, abstract `title()`, `showOptions()`, `handle(int)`, ordered catch blocks (HLD 9) | P3 | P1 | T10, T03 | 0.5 | 19 Nov | The T10 test menu is rewritten as a subclass and works |
| T11 | `PasswordHasher` (salted SHA-256) with a quick check | P3 | P1 | T01 | 0.5 | 16 Nov | Hash differs per call; verify works; wrong password fails |
| T12 | Seed data: all five files (11 sub-cities, 12 pharmacies, 13 users, ≥ 40 medicines, ≥ 30 stock rows with `ageHours`) | P5 | P3 (hashes), P4 (format) | T11 | 2 | 18 Nov | Columns exactly as HLD 5.1; sub-city list confirmed |
| T13 | `FileUserRepository implements UserRepository, RowMapper<User>` (sets the pattern for all file repositories) | P1 | — | T05a, T06, T07 | 1 | 19 Nov | Loads seed users as `Pharmacist`/`Administrator`; saves and reloads |
| T22 | LLM service chosen; key obtained; a small Java program sends one request and prints the answer | P2 | P1 | — | 1 | 16 Nov | Real answer received from Java |
| T23 | AI contracts: `LlmClient`, `AiOutcome` (nested `Kind`, factories), `AiServiceException` | P2 | P1 | T03 | 0.5 | 19 Nov | Reviewed by P1 |

### 5.2 Data Access, Services and Engines (Week 2)

| ID | Task | Owner | Support | Pred. | Effort | Due | Done when |
|---|---|---|---|---|---|---|---|
| T14 | `FilePharmacyRepository` and `FileStockRepository` (each also implements `RowMapper`) | P5 | P1 | T13 | 2 | 22 Nov | Load seed, save, reload |
| T15 | `FileMedicineRepository` and `FileLocalityRepository` | P5 | P4 | T13 | 1 | 21 Nov | `findDistricts("Addis Ababa")` returns 11 sub-cities |
| T16 | `AppConfig` (settings, environment override, `public static final` constants) and example settings file | P2 | P1 | T01 | 1 | 21 Nov | Settings and environment override work |
| T17 | `TextSimilarity` (static; `int[][]` Levenshtein) with tests ("amoxcillin" example) | P1 | — | T01 | 1 | 21 Nov | All sample cases pass |
| T19 | `Session` and `AuthService` (`requirePharmacist` with `instanceof` pattern, `requireAdministrator`, `listUsers`) | P3 | P2 | T05b, T06, T11 | 1.5 | 23 Nov | Every refusal in HLD 4.2 checked |
| T20 | `PharmacyService` (register, update, resubmit, approve, reject, withdraw, list pending/verified; licence unique ignoring case and spaces) | P3 | P2 | T19, T24b | 1.5 | 25 Nov | Every transition in BR-11 works, including Verified → Pending after a licence or sub-city edit |
| T21 | `AppContext`, `Main` (`try … finally`), `SeedDataCopier` (`ageHours` → `lastConfirmedAt`) | P1 | — | T13, T16 | 1 | 24 Nov | A fresh run loads files and shows Fresh, Aging and Stale records |
| T24 | `CatalogueService` (`listActive`, `findById`) | P5 | P4 | T15 | 0.5 | 23 Nov | Works with the seed data |
| T24b | `LocalityService` (`districts`, `toLocation`, `matchDistrict` using `TextSimilarity`) | P5 | P1 | T15, T16, T17 | 0.5 | 23 Nov | Unknown sub-city refused; "bole" and "Bolle" match Bole |
| T25 | `HttpLlmClient`: request, 15 s timeout, multi-catch → `AiServiceException` | P2 | P1 | T22, T23 | 1 | 22 Nov | Timeout and no-network give `AiServiceException` |
| T26 | `system-prompt.txt` and `PromptBuilder` | P2 | P1 | T23 | 0.5 | 23 Nov | Valid JSON for ten sample requests |
| T27 | `AiResponseParser` with tests (SEARCH with/without sub-city, UNSUPPORTED, fenced, invalid, six keywords) | P2 | P1 | T23 | 1 | 26 Nov | All sample cases pass |

### 5.3 Search, AI Orchestration, Inventory (late Week 2 – Week 3)

| ID | Task | Owner | Support | Pred. | Effort | Due | Done when |
|---|---|---|---|---|---|---|---|
| T28 | `InventoryService` (add, edit, remove, reconfirm selected/all) | P3 | P2 | T20, T08 | 1.5 | 28 Nov | Every refusal in HLD 4.5 checked |
| T29 | `SearchService` (HLD 7.3, one `Comparator`) and `SearchResult` | P1 | P3 | T14, T15, T17, T08 | 2 | 28 Nov | Correct order: in stock first, same sub-city first, score, price, name; unverified excluded |
| T30 | `AiAssistantService` with `AiAssistantServiceTest` using `FakeLlmClient` | P2 | P1 | T25, T26, T27 | 1 | 29 Nov | Every row of HLD 8.4 gives the right outcome |

### 5.4 Console Layer (Week 3)

| ID | Task | Owner | Support | Pred. | Effort | Due | Done when |
|---|---|---|---|---|---|---|---|
| T32 | `StartMenu` (customer entry without login, login with `switch` on role, pharmacist registration, exit) | P5 | P3 | T10b, T19, T21, T35 (empty menu) | 1 | 28 Nov | AT-16, AT-18 and the login flows work |
| T33 | `AdministratorMenu` (pending list, review with licence, approve, reject with reason, withdraw from the Verified list, list users) | P5 | P3 | T10b, T20, T19 | 1.5 | 1 Dec | AT-01 to AT-04 admin parts, AT-19, AT-20 work |
| T34 | `PharmacistMenu` (status and reason, register/edit pharmacy with sub-city list, resubmit, stock list/add/edit/remove, reconfirm selected or all, catalogue) | P4 | P3 | T10b, T20, T24b, T28 | 2.5 | 2 Dec | AT-01 to AT-06, AT-21, AT-22 work |
| T35 | `CustomerMenu` (search by name, results table with match level, catalogue, set/clear sub-city). An empty menu is merged by 27 Nov so `StartMenu` can open it | P2 | P3 | T10b, T24, T24b, T29 | 1.5 | 30 Nov | AT-07 to AT-10 work |
| T36 | `AiModeMenu` (flow F5: configured check, keywords, sub-city resolution, per-keyword results, unsupported, fallback) | P2 | P3 | T29, T30, T24b, T35 | 1.5 | 2 Dec | AT-11 to AT-15 work |

### 5.5 Integration, Testing and Delivery (Week 4 and buffer)

| ID | Task | Owner | Support | Pred. | Effort | Due | Done when |
|---|---|---|---|---|---|---|---|
| T37 | `test-cases.md`: every AT scenario as numbered steps with expected result | P5 | P1 | T12 | 1 | 29 Nov | Reviewed by P1 |
| T38 | First end-to-end run on a clean checkout; defect list | P1 | all | M3 | 1 | 3 Dec | Defects logged with owner |
| T39 | Execute all scenarios on three computers; record results | P5, P4 | P1 | T37, T38 | 2 | 5 Dec | Results table filled |
| T40 | Fix defects (each owner in their own files) | all | — | T38 | 3 | 7 Dec | No open Must defect |
| T41 | Check the result order with the sample data; adjust constants if needed | P1 | P5 | T38 | 0.5 | 4 Dec | Orders look sensible in AT-07 to AT-11 |
| T42 | AI hardening: prompt adjustments from real answers; 20 test requests (searches with and without sub-city, misspelt sub-city, dose question, nonsense) | P2 | P3 | T36 | 1 | 5 Dec | No AI text displayed; failures fall back correctly |
| T43 | README complete (build, run, settings, demo accounts, reset data) | P1 | — | M3 | 0.5 | 5 Dec | A new person can run the program from the README alone |
| T44 | User manual `user-manual.md` (per role) | P5 draft, P3 polish | — | M3 | 1.5 | 6 Dec | Covers every menu |
| T45 | Slides and demonstration script, including the chapter coverage table (HLD 15.2) | P3 | P1 | M3 | 2 | 8 Dec | Approved by P1 |
| T46 | Release candidate; reset `data`; tag v1.0 | P1 | P5 | T40 | 0.5 | 9 Dec | Clean clone runs the demo with correct freshness labels |
| T47 | Rehearsal (twice, timed); every member explains their own classes and their chapter concepts | P1, P3 | all | T45 | 0.5 | 9–10 Dec | Fits the allowed time |
| T48 | Submission package handed in | P1 | — | T46 | 0.5 | 11 Dec | Confirmed by the instructor |

## 6. Week-by-Week Plan

| Week | P1 | P2 | P3 | P4 | P5 |
|---|---|---|---|---|---|
| 1 | T01, T02, T06, T07, T13 | T22, T23 | T10, T10b, T11 | T03, T04, T05a, T05b, T08 | T12 |
| 2 | T17, T21; start T29 | T16, T25, T26, T27 | T19, T20 | review, help P5 with T14 | T14, T15, T24, T24b |
| 3 | T29 | T30, T35, T36 | T28 | T34 | T32, T33, T37 |
| 4 | T38, T41, T43, T46 | T42 | T44 polish, T45 | T39, T40 | T39, T44 |
| Buffer | T47, T48 | support | T47 | fixes | fixes |

All members also review pull requests and fix their own defects throughout.

## 7. Working Practices

### 7.1 Daily and Weekly Rhythm

- **Daily check-in** (10 minutes): what I finished, what I do next, what blocks me. A blocker lasting more than one working day is escalated to P1.
- **Pair sessions:** P4 with P3 and P5 with P3 twice a week (45 minutes).
- **Weekly review:** P1 checks the milestone. Anything not done is reassigned or reduced the same day.

### 7.2 Code Review

| Author | Reviewer |
|---|---|
| P1 | P2 |
| P2 | P1 |
| P3 | P2 |
| P4 | P3 |
| P5 | P3 |

A reviewer checks: the task's "Done when"; the dependency rules of document 03, Section 4; naming and comment conventions; no empty catch blocks; no secrets; `mvn clean test` passes. Reviews are answered within one working day. P1 merges.

### 7.3 Definition of Done (every task)

1. Code follows the conventions of document 03.
2. Public classes and methods have Javadoc comments.
3. Unit tests exist for the classes listed in NFR-06 and pass.
4. A teammate has reviewed it and the pull request is merged to `main`.
5. The owner has run the program once and used the new feature.

### 7.4 Early Handoffs Between Members

| What | From | To | When |
|---|---|---|---|
| Repository interfaces and model skeleton | P1, P4 | everyone | 15 Nov |
| `Location` and `MatchLevel` | P4 | P1, P3, P5 | 15 Nov |
| `PasswordHasher` | P3 | P5 (seed hashes) | 16 Nov |
| `ConsoleHelper` | P3 | all menu authors | 18 Nov |
| Abstract `Menu` | P3 | all menu authors | 19 Nov |
| AI contracts (`LlmClient`, `AiOutcome`) | P2 | P1 | 19 Nov |
| `FileUserRepository` (pattern) | P1 | P5 | 19 Nov |
| `LocalityService` and `SearchService` signatures (fixed in HLD 4.4 and 4.7) | P5, P1 | P2, P3, P4 | now; working versions 23 and 28 Nov |

## 8. Testing Strategy

| Level | What | Who | When |
|---|---|---|---|
| Unit | `TextSimilarity`, `FreshnessClassifier`, `Location`, `Pharmacy`, `AiResponseParser`, `AiAssistantService` | Each author | With the code |
| Integration | Services with file repositories and real seed data | P1 | 3 Dec |
| System / acceptance | Scenarios AT-01 to AT-22 following `test-cases.md` | P5, P4, with P1 | 3–6 Dec |
| AI behaviour | 20 prepared requests; fallback with the network disconnected | P2, P3 | 5 Dec |
| Regression | Re-run all scenarios after the last fix | P5 | 8 Dec |

Defects are recorded as GitHub issues with: scenario ID, steps, expected and actual result, owner.

## 9. Integration Plan

1. **M1–M2:** members work against interfaces; every merge keeps `main` buildable.
2. **27 Nov:** `StartMenu` and an empty `CustomerMenu` exist, so each menu can be run as soon as it is merged.
3. **28 Nov:** `SearchService` merged; menus show real results.
4. **2 Dec (M3):** all menus merged; P1 runs the first end-to-end test on 3 Dec.
5. **After M3:** only defect fixes and tuning; no new files without P1's approval.

## 10. Risks and Responses

| Risk | Likelihood | Effect | Response |
|---|---|---|---|
| Instructor does not accept the v1.2 scope | Low | Rework | P1 confirms on 15 Nov using SRS Section 7; if refused, restore FR-AI-05 (INFO) first — it is the smallest addition |
| LLM service unavailable or key not obtained | Medium | AI mode cannot be demonstrated | Free-tier service chosen by 15 Nov; `FakeLlmClient` for tests; recorded backup demonstration |
| LLM answers in an unexpected shape | High | Parsing failures | Strict validation and fallback; prompt tuning in T42 |
| LLM returns a sub-city spelling not in the list | Medium | Wrong ordering | `matchDistrict` tolerance; otherwise the customer picks from the list (AT-12) |
| A foundation task is late | Medium | Blocks others | Skeleton first (T05a); P1 steps in |
| Members with less Java experience are blocked | Medium | Delays | Pair sessions; small tasks; reviews within one day |
| Merge conflicts | Medium | Lost time | Small pull requests; one owner per class; update `main` daily |
| Seed data ages | Low | Labels wrong | Ages converted on first run; reset by deleting `data` |
| Member cannot explain the OOP concepts in their code | Medium | Lower marks | HLD 15.2 chapter table; rehearsal T47 includes a two-minute explanation per member |
| Secret key committed by mistake | Low | Key misuse | `.gitignore`; review rule; revoke and replace |
| Absence of a member | Low | Gap | Backup owners in document 05 |

## 11. Cut Order if Time Runs Short

Cut in this order, stopping as soon as the schedule is safe:

1. FR-ACC-09 (list users) — but keep the `List<User>` polymorphism example somewhere else if cut.
2. FR-SRC-07 (result limit) and FR-AI-14 (keyword limit).
3. FR-DAT-03 (skipping malformed lines) and FR-DAT-04 (safe write; write directly instead).

**Must requirements are not cut.** The AI search and the fallback are never cut. If a Must is at risk, P1 reduces its scope (for example, fewer sample medicines) rather than skipping it.

## 12. Delivery Checklist

| Item | Owner |
|---|---|
| Source code tagged v1.0 on `main`, builds from a clean clone | P1 |
| `docs/` folder with documents 01 to 05, user manual and test results | P1, P5 |
| README with build, run, settings and demonstration accounts | P1 |
| Presentation slides and demonstration script | P3 |
| Sample data reset; demonstration accounts working | P5 |
| AI mode tested on the presentation computer; plan B (disconnect the network and show the fallback) | P2 |
| Presentation rehearsed twice | P1, P3 |
| Demonstration computer prepared: Java, settings file and key, `data` folder reset | P1 |

### Suggested Demonstration Order (about 10 minutes)

1. The problem in one slide (patients visiting pharmacies in vain).
2. Architecture in one slide (layers and where the AI sits).
3. Live: log in as pharmacist, register a pharmacy by choosing a sub-city (Pending); log in as administrator, view the licence, approve; pharmacist adds stock.
4. Live: a customer, without logging in, searches with a spelling mistake; sets sub-city Bole; results show "Same sub-city" first; freshness labels explained.
5. Live: AI mode — "I need amoxicillin and paracetamol, I live in Bole": keywords and sub-city shown, results per medicine with Bole first. Then a dose question showing the fixed "AI mode only finds medicines" message.
6. Live: disconnect the network; the AI request falls back to normal search.
7. OOP and chapter highlights (HLD 15.2): `User` and `Menu` hierarchies, `Menu.run()` template, `Pharmacy` guarding its own status, `final class Location`, a class implementing two interfaces, `LlmClient` and the fake, exception hierarchy and translation; then the tests. Show "add a region" as a data-only change.
