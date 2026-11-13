# MedFinder — High-Level Design (HLD)

| | |
|---|---|
| **Document** | 02 — High-Level Design |
| **Version** | 1.2 (simplified scope — see the 01 SRS change log: AI search only, sub-city only, fewer classes) |
| **Date** | 13 November 2026 |
| **Depends on** | 01 Software Requirements Specification (requirement IDs such as FR-SRC-05 refer to it) |
| **Followed by** | 03 Architecture and Folder Structure, 04 Implementation Plan |

---

## 1. Purpose

This document describes **how** MedFinder is designed: its layers, components, the responsibilities and operations of each component, the data design, the main flows, the algorithms and the AI integration. It contains no program code. Members build components in parallel, so the **operations tables in Section 4 are the contracts** between components: a component's name, inputs, outputs and exceptions must not change without the team agreeing.

## 2. Design Goals and Principles

| Goal | How the design achieves it |
|---|---|
| Built from course chapters 1–6 | Plain classes, abstract classes, interfaces, inheritance, polymorphism, arrays, strings, `Scanner`, custom exceptions. Concepts from outside the chapters are few and isolated (Section 15.3) |
| Understandable by a team with mixed Java experience | 57 small classes, each with one job; menus use `switch`, not lambdas |
| Parallel work | Layers and interfaces let each member build against agreed contracts and test with substitutes |
| Correct rules in one place | All business rules live in services, entities and logic classes, never in menus |
| Replaceable parts | Storage and the LLM sit behind interfaces |
| Safe AI use | The AI only turns text into keywords and a sub-city. No AI text is ever shown; all stock facts come from MedFinder's data |
| Demonstrates object-oriented design (NFR-09) | Mapped concept by concept, chapter by chapter, in Section 15 |

## 3. Architecture Overview

### 3.1 Layers

```
 ┌────────────────────────────────────────────────────────────┐
 │  CONSOLE LAYER     menus, input reading, table printing    │
 │  (package console)                                         │
 └──────────────────────────────┬─────────────────────────────┘
                                │ calls
 ┌──────────────────────────────▼─────────────────────────────┐
 │  SERVICE LAYER     business rules and use cases            │
 │  (packages service, ai)                                    │
 │  AuthService · PharmacyService · LocalityService           │
 │  InventoryService · CatalogueService · SearchService       │
 │  AiAssistantService                                        │
 └───────────┬───────────────────────────────┬────────────────┘
             │ uses                          │ uses
 ┌───────────▼─────────────┐     ┌───────────▼────────────────┐
 │  LOGIC (package logic)  │     │  DATA ACCESS               │
 │  pure calculations:     │     │  (package repository)      │
 │  TextSimilarity         │     │  interfaces + file-based   │
 │  FreshnessClassifier    │     │  implementations, CsvFile  │
 │  PasswordHasher         │     └───────────┬────────────────┘
 └───────────┬─────────────┘                 │ reads / writes
             │                               ▼
             ▼                         CSV data files
   MODEL (package model): entities, enums      EXCEPTION (package exception)
                                               used by all layers

   External: LLM web API — reached only through ai.LlmClient
```

### 3.2 Dependency Rule

A layer may use only the layers below it. Menus never read files, never calculate freshness or ordering, and never apply rules. Services never print or read from the keyboard. `model` and `exception` are used by everyone and depend on nothing else (except that `model` throws `exception` types).

### 3.3 Components at a Glance

| Component | Responsibility |
|---|---|
| `Main` and menus | `Main` builds `AppContext` and runs `StartMenu`. Every menu extends the abstract `Menu` class, whose `final run()` method owns the shared loop |
| `Session` | Holds the logged-in user and the customer's chosen sub-city (memory only) |
| `AuthService` | Pharmacist registration, login, logout, role checks, list users |
| `PharmacyService` | Pharmacy registration, editing, verification workflow (approve, reject, withdraw, resubmit) |
| `LocalityService` | Lists the active region's sub-cities, validates a sub-city, matches an AI-extracted sub-city |
| `InventoryService` | Stock add, edit, remove, reconfirm with all rules |
| `CatalogueService` | List active medicines, find one by id |
| `SearchService` | Find, score and order results for one keyword |
| `AiAssistantService` | Turn a free-text request into an `AiOutcome` (keywords + sub-city, unsupported, or fallback) |
| `LlmClient` | Send one prompt to the LLM and return its raw text |
| Repositories | Load and save entities; hide the file format |
| Logic classes | Text similarity, freshness classification, password hashing |
| `AppConfig` | Read settings (file, environment) and hold the fixed search constants |
| `AppContext` | Create and connect all components once at start-up |

## 4. Component Design

Operations are described as: **name (inputs) → output; refused with: exceptions**.

### 4.1 Domain Model

```
 User (abstract)  ◄── Pharmacist
   id, fullName,  ◄── Administrator
   email, passwordHash
   + abstract role() : Role
   + toString()  (overridden in each subclass)

 Customers are anonymous visitors: they have no User object and no stored data.

 Pharmacy ── ownerUserId ──► Pharmacist
   id, name, licenceNumber, location (Location), address,
   status (PharmacyStatus), rejectionReason
   + approve(), reject(reason), resubmit(), changeDetails(...)

 Medicine
   id, name, category, dosageForm, strength,
   prescriptionRequired, active

 StockRecord ── pharmacyId ──► Pharmacy
   quantity, price, lastConfirmedAt ── medicineId ──► Medicine
   two constructors: full, and one that chains with this(...) using "now"
   + reconfirm(now), update(quantity, price, now)

 Location     region, district                 — final, immutable value object
   + matchLevel(other Location) → MatchLevel, equals(), hashCode(), toString()

 Enums        Role (PHARMACIST, ADMINISTRATOR), each with a label
              PharmacyStatus (PENDING, VERIFIED, REJECTED)
              Freshness (FRESH, AGING, STALE), each with a label and a ranking value
              MatchLevel (SAME_DISTRICT "Same sub-city", OTHER_AREA "Other area")

 View object  SearchResult (medicine, pharmacy, price, quantity, match level or null,
              freshness, score) — immutable
```

`User` is abstract; its subclasses supply the role. Entities validate their own fields in constructors and methods (non-empty text, quantity ≥ 0, price > 0) and throw `ValidationException`.

**Pharmacy owns its status rules (encapsulation).** There is no public status setter. `approve()` allows only PENDING → VERIFIED; `reject(reason)` allows PENDING or VERIFIED → REJECTED and requires a non-empty reason; `resubmit()` allows REJECTED → PENDING and clears the reason; `changeDetails(name, licence, location, address)` returns a VERIFIED pharmacy to PENDING when the licence or location changed (SRS BR-11). Any other transition throws `ValidationException`. The service decides *who* may act; the entity decides *what* is allowed.

**Location (value object).** `final` class with `final` fields. Two locations with the same region and district are equal (compared ignoring case and extra spaces). `matchLevel(other)` returns SAME_DISTRICT when region and district are equal, otherwise OTHER_AREA. The code word is **district**; the label shown to users ("Sub-city") comes from the settings.

### 4.2 Session and AuthService

| Operation | Behaviour | Refused with |
|---|---|---|
| registerPharmacist (fullName, email, password) → Pharmacist | Validates input, checks the email is unused, hashes the password, saves | ValidationException (also for a used email) |
| login (email, password) → User | Verifies the hash, stores the user in the `Session` | AuthorizationException ("Wrong email or password") |
| logout () | Clears the current user and the customer location | — |
| requirePharmacist () → Pharmacist | Checks `currentUser instanceof Pharmacist p` and returns `p` | AuthorizationException |
| requireAdministrator () → Administrator | Same idea for the administrator | AuthorizationException |
| listUsers () → list of User | Administrator only; returns pharmacists and administrators in one list | AuthorizationException |

`Session` operations: `currentUser()`, `setUser(u)`, `clear()`, `district()`, `setDistrict(d)`, `clearDistrict()`. Pharmacy, inventory, verification and user-list operations call `requirePharmacist` or `requireAdministrator` first. Catalogue, search and AI operations are public.

### 4.3 PharmacyService

| Operation | Behaviour | Refused with |
|---|---|---|
| registerPharmacy (name, licenceNumber, district, address) → Pharmacy | Pharmacist only; region = active region; district checked through LocalityService; licence non-empty and unique ignoring case and spaces; one pharmacy per pharmacist; status PENDING | ValidationException, AuthorizationException |
| updatePharmacy (name, licenceNumber, district, address) → Pharmacy | Owner only; calls `Pharmacy.changeDetails` | ValidationException, NotFoundException |
| resubmit () → Pharmacy | Owner only; REJECTED → PENDING | ValidationException, NotFoundException |
| getMyPharmacy () → Pharmacy or null | Owner's pharmacy | AuthorizationException |
| listPending () → list | Administrator only | AuthorizationException |
| listVerified () → list | Administrator only; used to choose a pharmacy to withdraw | AuthorizationException |
| approve (pharmacyId) | PENDING → VERIFIED | AuthorizationException, NotFoundException, ValidationException |
| reject (pharmacyId, reason) | PENDING → REJECTED, reason mandatory | as above |
| withdraw (pharmacyId, reason) | VERIFIED → REJECTED, reason mandatory | as above |

### 4.4 LocalityService

| Operation | Behaviour | Refused with |
|---|---|---|
| activeRegion () → text | From the settings (default Addis Ababa) | — |
| districts () → list of text | Sub-cities of the active region, sorted, for numbered menus | — |
| districtLabel () → text | User-facing word (default "Sub-city") | — |
| toLocation (district) → Location | Builds a Location in the active region after checking the district exists | ValidationException |
| matchDistrict (text) → text or null | For AI mode: exact match ignoring case, otherwise the closest district with relevance ≥ 0.6 (uses `TextSimilarity`), otherwise null | — |

### 4.5 InventoryService

| Operation | Behaviour | Refused with |
|---|---|---|
| listStock () → list of StockRecord | The pharmacist's records (the menu shows the medicine name via `CatalogueService.findById` and the label via `FreshnessClassifier`) | AuthorizationException |
| addStock (medicineId, quantity, price) | Creates the record; last-confirmed = now | AuthorizationException (not Verified), ValidationException (duplicate or bad value), NotFoundException (unknown or inactive medicine) |
| editStock (medicineId, quantity, price) | Updates and sets last-confirmed = now | as above |
| removeStock (medicineId) | Deletes the record | AuthorizationException, NotFoundException |
| reconfirm (medicineIds) | Sets last-confirmed = now for the listed records, values unchanged | AuthorizationException, NotFoundException |
| reconfirmAll () | Same for every record | AuthorizationException |

### 4.6 CatalogueService

| Operation | Behaviour |
|---|---|
| listActive () → list | All active medicines, sorted by name |
| findById (id) → Medicine | Throws NotFoundException |

### 4.7 SearchService

| Operation | Behaviour |
|---|---|
| searchByName (text, district or null) → list of SearchResult | Runs the search algorithm (Section 7.3) and returns at most 20 ordered results |

The ordering rules (BR-10) and the `score(relevance, freshness)` method live in `SearchService` as one private method and one `Comparator`. The constants come from `AppConfig` through `import static`.

### 4.8 AiAssistantService (package ai)

| Operation | Behaviour |
|---|---|
| isConfigured () → yes/no | False when AI is disabled or no key is set |
| interpret (text) → AiOutcome | Builds the prompt (`PromptBuilder`), calls `LlmClient`, validates the answer (`AiResponseParser`). Returns one outcome kind: **SEARCH** (1–5 keywords, sub-city text or null), **UNSUPPORTED**, or **FALLBACK** (with a notice such as "AI is not available right now" and the typed text as the only keyword). It never throws to the menu: every `AiServiceException` becomes FALLBACK |

The service never calls `SearchService` or `LocalityService`. `AiModeMenu` receives the outcome, resolves the sub-city through `LocalityService.matchDistrict`, and runs the searches itself, so the `ai` package stays independent of search code.

`AiOutcome` is immutable: `kind()`, `keywords()` (an unmodifiable copy), `subCity()`, `notice()`, with static factory methods `search(...)`, `unsupported()`, `fallback(...)`. Its kind is a small nested enum.

### 4.9 Repositories

| Interface | Operations |
|---|---|
| UserRepository | findById, findByEmail, findAll, save (insert or update), nextId |
| PharmacyRepository | findById, findByOwner, findByLicence, findByStatus, save, nextId |
| MedicineRepository | findById, findActive |
| StockRepository | findByPharmacy, findByMedicine, find (pharmacy, medicine), save, delete, saveAll |
| LocalityRepository | findDistricts (region), exists (region, district) |

All throw `DataStorageException` (unchecked) on file problems. File-based implementations live in `repository.file`. Each `FileXRepository` **implements two interfaces**: its repository interface and `RowMapper<X>` (header names, row → entity, entity → row). `RowMapper` has a **default method** `mapAll(rows)` that converts every row and skips a malformed one with a warning (FR-DAT-03). `CsvFile` reads columns **by header name** into `Map<String,String>` rows, so adding or reordering a column does not break existing code. `CsvFile` throws the checked `IOException`; each repository catches it and **translates** it into `DataStorageException(message, cause)`. `FileLocalityRepository` reads the district column into a `List<String>` and needs no entity class.

### 4.10 Logic Classes (pure calculations, no input or output)

| Class | Operation |
|---|---|
| TextSimilarity | static normalize (text) → text; static distance (a, b) → whole number (Levenshtein, 2D array); static relevance (typed, name) → number 0 to 1 |
| FreshnessClassifier | static classify (lastConfirmedAt, now) → Freshness |
| PasswordHasher | static hash (password) → "salt:hash"; static verify (password, stored) → yes/no (salted SHA-256 with `MessageDigest` and `SecureRandom`) |

## 5. Data Design

### 5.1 Files

All files are UTF-8 text with a header line. Fields that may contain commas are quoted; quotes inside are doubled.

| File | Columns |
|---|---|
| users.csv | id, role, fullName, email, passwordHash |
| pharmacies.csv | id, name, licenceNumber, region, district, address, status, rejectionReason, ownerUserId |
| medicines.csv | id, name, category, dosageForm, strength, prescriptionRequired, active |
| stock.csv | pharmacyId, medicineId, quantity, price, lastConfirmedAt |
| localities.csv | region, district |

**Sample stock file.** The bundled sample `stock.csv` uses `ageHours` instead of `lastConfirmedAt` (for example 5, 30, 60). On the first run `SeedDataCopier` writes the working file with `lastConfirmedAt = now − ageHours`.

### 5.2 Conventions

- **Identifiers** are positive whole numbers; a new id is the largest existing id plus one.
- **Times** are stored in UTC (ISO-8601 text, for example `2026-11-11T08:30:00Z`) and displayed in the configured time zone (default Africa/Addis_Ababa).
- **Password hash** is stored as `salt:hash`, both Base64 text.
- **Booleans** are `true` or `false`; **status and role** are the enum names.

### 5.3 Persistence Strategy

At start-up each repository loads its file into memory. Every change updates memory and then rewrites the whole file safely: write to a temporary file, then replace the original in one step (try-with-resources closes the writer). The program is single-user and single-threaded, so no locking is needed.

## 6. Key Flows

**F0 — Customer access.** The start menu opens the customer menu directly. There is no registration or login for customers.

**F1 — Pharmacist registration and login.** Start menu → `AuthService.registerPharmacist` → login → `StartMenu` chooses the next menu with a `switch` on `user.role()` and calls `run()` on it (`PharmacistMenu` or `AdministratorMenu`, both used as `Menu`).

**F2 — Pharmacy verification.** Pharmacist menu → choose sub-city from the numbered list → `PharmacyService.registerPharmacy` (PENDING) → administrator lists pending, views details including the licence → approve (VERIFIED) or reject with reason → pharmacist sees status or reason → edit and resubmit (PENDING). Later the administrator may withdraw a VERIFIED pharmacy with a reason (REJECTED).

**F2a — Customer location.** Customer menu → Set sub-city → numbered list from `LocalityService.districts` → `Session.setDistrict`. "Clear sub-city" removes it.

**F3 — Stock update.** Pharmacist menu → `InventoryService` (checks pharmacist, owner, VERIFIED) → `StockRepository.save` → next customer search sees it.

**F4 — Normal search.** Customer menu → `SearchService.searchByName(text, session district)` → results table printed by the menu.

**F5 — AI search.**
1. Customer chooses AI mode. If `isConfigured()` is false, the menu prints "AI mode is not configured" and offers normal search.
2. Customer types the request; the menu prints "Interpreting your request…" and calls `AiAssistantService.interpret`.
3. **SEARCH:** the menu prints the keywords and the sub-city. If a sub-city was given, `LocalityService.matchDistrict` resolves it; if null, the customer chooses from the list or skips. A resolved sub-city is stored in the `Session`. For each keyword, the menu calls `SearchService.searchByName` and prints a table under the keyword.
4. **UNSUPPORTED:** the menu prints the fixed message "AI mode only finds medicines in pharmacies. For questions about doses or treatment, please ask a pharmacist or doctor."
5. **FALLBACK:** the menu prints the notice and runs a normal search on the typed text.

**F6 — List users.** Administrator menu → `AuthService.listUsers()` → the menu loops over the `List<User>` and prints each item through its overridden `toString()` and `role().label()` — a heterogeneous collection processed polymorphically.

## 7. Algorithms

### 7.1 Freshness

age = now − lastConfirmedAt. FRESH if age ≤ 12 h; AGING if age ≤ 48 h; otherwise STALE. Calculated at display time, never stored. The current time is passed in so tests can fix it.

### 7.2 Text Similarity

1. Normalise both texts: lowercase, trim, collapse repeated spaces.
2. Edit distance (Levenshtein) computed with a two-dimensional `int[][]` table and nested `for` loops: the least number of single-character insertions, deletions or substitutions that turn one text into the other.
3. similarity = 1 − distance ÷ length of the longer text.
4. Relevance: 1.0 if equal; otherwise the larger of the similarity and 0.85 if the name starts with the typed text (typed text at least 3 characters), or 0.75 if the name contains it.

Example: "amoxcillin" against "amoxicillin": one edit, longer length 11, similarity 0.91.

### 7.3 Search Algorithm

1. Load active medicines. For each, compute relevance against the typed text; keep those with relevance ≥ 0.6.
2. For each kept medicine, load its stock records; keep those whose pharmacy is VERIFIED.
3. For each record compute freshness, match level (if a sub-city is given) and score = 0.6 × relevance + 0.4 × freshness value.
4. Sort with one `Comparator` by: quantity > 0 first; Same sub-city before Other area (only when a sub-city is given); score descending; price ascending; pharmacy name.
5. Return at most 20 results.

### 7.4 Constants (in `AppConfig`, `public static final`)

| Constant | Value |
|---|---|
| MATCH_THRESHOLD | 0.6 |
| WEIGHT_RELEVANCE, WEIGHT_FRESHNESS | 0.6, 0.4 |
| Freshness values (Fresh, Aging, Stale) | 1.0, 0.6, 0.2 (stored in the `Freshness` enum) |
| FRESH_HOURS, AGING_HOURS | 12, 48 |
| MAX_RESULTS | 20 |
| MAX_AI_KEYWORDS | 5 |

## 8. AI Integration Design

### 8.1 Structure

```
 AiModeMenu ──► AiAssistantService ──► LlmClient (interface)
     │                 │                  └─ HttpLlmClient (java.net.http, web API)
     │                 ├─ PromptBuilder   (fixed instructions + user text, StringBuilder)
     │                 └─ AiResponseParser (JSON → validated AiOutcome)
     ├──► LocalityService.matchDistrict (sub-city)
     └──► SearchService.searchByName (one call per keyword)
```

`LlmClient` is the only abstraction the service sees. Tests pass a `FakeLlmClient`; changing provider means writing a new class that implements `LlmClient`.

### 8.2 Prompt Design

The fixed instructions (kept in `system-prompt.txt`, not in code) tell the model to:

- act only as a medicine-request interpreter for a pharmacy search system in Addis Ababa;
- return **only** a JSON object in the shape of SRS Section 3.1, with no other text;
- use `SEARCH` for requests to find medicines: put each medicine **name only** in `keywords` (no strength, form or dose; at most five) and put the sub-city the customer says they live in, if any, in `subCity`;
- use `UNSUPPORTED` for anything else, including questions about what a medicine is used for, dosage, diagnosis or treatment;
- never invent pharmacies, prices, availability or quantities.

The user's typed text is the only user data sent.

### 8.3 Validation (AiResponseParser)

Remove any code-fence markers, parse the JSON (Gson), then require: `intent` is SEARCH or UNSUPPORTED; SEARCH has 1–5 keywords, each 1–60 characters after trimming; `subCity` is null or 1–40 characters. Extra keywords beyond five are dropped. Any violation throws `AiServiceException`.

### 8.4 Failure Handling

| Situation | Result |
|---|---|
| No key configured | "AI mode is not configured"; normal search remains available |
| No internet, timeout (15 s), server error | `HttpLlmClient` catches `IOException \| InterruptedException` (multi-catch) and throws `AiServiceException(cause)` → FALLBACK |
| Invalid response | `AiServiceException` → FALLBACK |
| UNSUPPORTED | Fixed message; no AI text shown |

## 9. Error Handling Strategy

| Exception | Kind | Meaning |
|---|---|---|
| `MedFinderException` | unchecked base (extends `RuntimeException`), four constructors: `()`, `(message)`, `(message, cause)`, `(cause)` | Parent of all below |
| `ValidationException` | unchecked | Field empty, malformed, out of range, or duplicate (email, licence, second pharmacy, stock record) |
| `AuthorizationException` | unchecked | Not logged in, wrong role, wrong credentials, or pharmacy not Verified |
| `NotFoundException` | unchecked | Unknown id |
| `DataStorageException` | unchecked | File cannot be read or written (wraps the checked `IOException`) |
| `AiServiceException` | unchecked | AI call or response failed (wraps the cause) |

**Checked vs unchecked.** The checked `IOException` is used where Java requires it (`CsvFile`, `HttpLlmClient`) and is caught or declared there. The project's own exceptions are unchecked so that service methods do not need long `throws` lists; they are all caught in one place, `Menu.run()`.

**Catch order in `Menu.run()`** (subclass before superclass): `ValidationException` → print "Please check: …"; `MedFinderException` → print its message; `RuntimeException` → print "Something went wrong, returning to the menu" (stack trace only when `app.debug=true`). `Main` has a `try … finally` that always prints "Goodbye" and closes the `Scanner`. No catch block is empty. The program never ends because of a user mistake.

## 10. Security Design

- Passwords: salted SHA-256 (`SecureRandom` salt, `MessageDigest`), stored as `salt:hash`; the plain password is never stored or printed.
- Role enforcement: every pharmacy, inventory, verification and user-list operation begins with `requirePharmacist()` or `requireAdministrator()` and, for pharmacists, an ownership check.
- Customers are anonymous: no personal data is stored; the chosen sub-city exists only in memory.
- Secrets: the LLM key comes from the environment variable `MEDFINDER_AI_KEY` or the local, git-ignored settings file.
- Input validation: every external value (typed input, file field, AI response) is validated before use.
- These measures suit a course project and are not a claim of production-grade security.

## 11. Configuration

Settings file `config/config.properties` (an example file with the same keys is committed; the real file is git-ignored). Environment variables override the file.

| Key | Purpose | Default |
|---|---|---|
| data.directory | Folder of working data files | data |
| time.zone | Display time zone | Africa/Addis_Ababa |
| app.debug | Print stack traces for unexpected errors | false |
| ai.enabled | Turn AI mode on or off | true |
| ai.endpoint | LLM web address | (provider-specific) |
| ai.model | Model name | (provider-specific) |
| ai.timeoutSeconds | Request time limit | 15 |
| location.activeRegion | Region served; must exist in localities.csv | Addis Ababa |
| location.districtLabel | Word shown to users for a district | Sub-city |

The key itself: `MEDFINDER_AI_KEY` (environment) or `ai.apiKey` (local file only). Search constants are fixed in code (Section 7.4).

## 12. Testing Design

| Target | Technique |
|---|---|
| FreshnessClassifier | Unit tests at 11 h 59 m, exactly 12 h, 47 h, exactly 48 h, 49 h |
| TextSimilarity | Exact, one-typo, prefix, contains, unrelated names |
| AiResponseParser | Valid SEARCH with and without sub-city, UNSUPPORTED, fenced JSON, missing keywords, six keywords, invalid JSON |
| Location | Same sub-city, different sub-city, case and space differences, `equals`/`hashCode` |
| Pharmacy status rules | Every allowed transition in BR-11 succeeds; every other transition is refused |
| Services and menus | Manual scenarios AT-01 to AT-22 from the SRS (in-memory repositories optional) |

## 13. Design Decisions

| Decision | Reason |
|---|---|
| Console interface | Uses material every team member has learned (Ch 2.6 `Scanner`) |
| CSV files, no database | No server, easy to inspect, enough for the data volume |
| Freshness computed, not stored | Cannot go out of date |
| AI returns keywords and sub-city only; AI text never shown | Removes medical-information risk and the label/caution rules; much less code |
| Sub-city only, same sub-city first | Easy to explain; customers know their sub-city |
| Strict ordering instead of a weighted location score | One comparator, no location-scoring interface needed |
| Unchecked domain exceptions, checked `IOException` translated at the boundary | Shows both kinds (Ch 6.3) and exception translation (Ch 6.6) without `throws` on every method |
| Abstract `Menu` with `final run()` and abstract `title/showOptions/handle` | Template Method (Ch 4.5) without lambdas |
| `FileXRepository implements XRepository, RowMapper<X>` | Multiple interfaces (Ch 4.6); fewer classes |
| `requirePharmacist()` uses `instanceof` pattern matching | One justified downcast (Ch 5.4); everywhere else, polymorphism |
| No customer accounts, no log file | No feature needs them |
| Localities as data (`localities.csv`) | A new region is new rows plus one setting, no code change |

## 14. Possible Extensions (not part of this version)

Graphical interface over the same services; database storage behind the same repository interfaces; another AI provider as a new `LlmClient`; other regions (rows in `localities.csv` plus `location.activeRegion`); woreda or distance matching (a new field in `Location` and one more comparator key).

## 15. Object-Oriented Design and Course Chapters 1–6

### 15.1 OOP Concept Map

| Concept | Where it appears | What it shows |
|---|---|---|
| Encapsulation | `Pharmacy` (status changes only through `approve`, `reject`, `resubmit`, `changeDetails`); `StockRecord`, `Medicine`, `User` validate in constructors; private fields everywhere; `AiOutcome.keywords()` returns an unmodifiable copy | Objects protect their own rules |
| Immutable value objects | `final class Location`, `SearchResult`, `AiOutcome` | Equality by value; safe to share |
| Inheritance | `User` (abstract) → `Pharmacist`, `Administrator`; `Menu` (abstract) → `StartMenu`, `CustomerMenu`, `AiModeMenu`, `PharmacistMenu`, `AdministratorMenu`; `MedFinderException` → 5 subclasses | Shared behaviour in one parent, `super(...)` calls |
| Abstraction | Abstract classes `User`, `Menu`; interfaces `UserRepository`, `PharmacyRepository`, `MedicineRepository`, `StockRepository`, `LocalityRepository`, `RowMapper<T>`, `LlmClient` | Callers depend on what, not how |
| Polymorphism | `Menu next = new PharmacistMenu(...); next.run();` (dynamic dispatch); `List<User>` in "list users"; `LlmClient` used for both `HttpLlmClient` and the test fake | One call, different behaviour |
| Composition | `AppContext` creates every object once and passes dependencies through constructors; `Pharmacy` has a `Location`; `AuthService` has a `Session` | Parts are assembled, not hard-wired |
| Exceptions | `MedFinderException` hierarchy (Section 9) | Errors as types, handled in one place |

### 15.2 Chapter-by-Chapter Coverage

| Chapter topic | Where it is shown |
|---|---|
| 1.2 Paradigms | Object-oriented decomposition everywhere; procedural loops inside `TextSimilarity`; pure functions in `FreshnessClassifier` and `TextSimilarity`; immutability in `Location` |
| 1.3 Cohesion / coupling | One job per class; layers; services depend on interfaces, not on CSV code |
| 2.3 Syntax, constants, casting | `public static final` constants in `AppConfig`; `(long)`/`(int)` narrowing casts when converting `ageHours`; widening `int` → `double` in score maths |
| 2.4 Operators | Score formula; `/` and `%` when formatting an age as "3 h 20 min"; short-circuit `&&` in validation |
| 2.5 Control flow | `switch` in every menu's `handle`; `do … while` in `Menu.run()`; `while` input retries; nested `for` in Levenshtein; `continue` for malformed rows |
| 2.6 Scanner, Math | `ConsoleHelper` wraps `Scanner`; `Math.min`, `Math.max` in `TextSimilarity` |
| 2.7 Strings | `trim`, `toLowerCase`, `equalsIgnoreCase`, `contains`, `startsWith`, `split`, `replace`; `StringBuilder` in `PromptBuilder` and `CsvFile` |
| 2.8 Arrays | `String[]` from `split` in `CsvFile`; `int[][]` table in `TextSimilarity`; `String[] headers()` in `RowMapper` |
| 3.1–3.3 Objects, scope, pass-by-value | Instance fields in models; local variables in services; passing a `StockRecord` reference to `reconfirm` shows that the reference is copied but the object is shared |
| 3.4 Encapsulation | `private` fields, getters, no setters for ids; defensive copies with `List.copyOf`; `protected` helpers in `Menu` |
| 3.5 Constructors | Parameterised constructors with `this.field = field`; constructor overloading and `this(...)` chaining in `StockRecord` and `Medicine` |
| 3.6 Methods | `static` methods in the logic classes; overloading `ConsoleHelper.readInt(prompt)` / `readInt(prompt, min, max)` and `readText(prompt)` / `readText(prompt, optional)` |
| 3.7 Packages | `medfinder.model`, `.service`, `.repository.file`, `.ai`, `.console`; `import static` of `AppConfig` constants |
| 4.1–4.2 Inheritance, `super` | `User` → `Pharmacist`, `Administrator` with `super(...)`; exception subclasses calling `super(message, cause)` |
| 4.3 Overriding, LSP | `@Override` of `toString`, `equals`, `hashCode`, `role()`; every `Menu` subclass overrides `title`, `showOptions`, `handle`; any `User` subclass works wherever a `User` is expected |
| 4.4 `final` | `final class Location`; `final` fields; `final` method `Menu.run()` |
| 4.5 Abstract classes, Template Method | `abstract class Menu` (`run()` is the template); `abstract class User` with `abstract Role role()` |
| 4.6 Interfaces | Five repository interfaces, `LlmClient`, `RowMapper<T>`; a class implementing two interfaces; a `default` method `RowMapper.mapAll` |
| 5.1–5.2 Polymorphism, OCP | Dynamic dispatch of menus; heterogeneous `List<User>`; new LLM provider or storage = new class, no change to callers |
| 5.3–5.4 Casting, `instanceof` | Implicit upcasting `User u = new Pharmacist(...)`; one justified `instanceof` pattern match in `AuthService.requirePharmacist()` |
| 6.1–6.3 Exceptions, checked/unchecked | Checked `IOException` in `CsvFile`/`HttpLlmClient`; unchecked project exceptions; built-in `NumberFormatException` caught in `ConsoleHelper` |
| 6.4 try/catch/finally, throw/throws | Multi-catch in `HttpLlmClient`; ordered catches in `Menu.run()`; `finally` in `Main`; `throw new ValidationException(...)` in entities and services; `throws IOException` in `CsvFile` |
| 6.5 Try-with-resources | `BufferedReader` and `BufferedWriter` in `CsvFile`; `ConsoleHelper implements AutoCloseable` |
| 6.6 Custom exceptions, chaining, translation | `MedFinderException` with four constructors; `IOException` → `DataStorageException(msg, cause)`; HTTP errors → `AiServiceException(cause)` |

### 15.3 Concepts Used from Outside Chapters 1–6 (kept few and isolated)

| Concept | Where | Why |
|---|---|---|
| AI over HTTP, JSON (Gson) | `HttpLlmClient`, `AiResponseParser` only | Required by the course (LLM integration) |
| Collections (`List`, `ArrayList`, `Map`, `HashMap`) | Repositories, services | Arrays are too rigid for growing data; explained as "resizable arrays" |
| Generics | `RowMapper<T>`, `List<User>` | Type-safe collections |
| Enums | `Role`, `PharmacyStatus`, `Freshness`, `MatchLevel`, `AiOutcome.Kind` | Fixed sets of values (like `final` classes with fixed instances) |
| Date/time (`Instant`, `Duration`) | `FreshnessClassifier`, `StockRecord`, `SeedDataCopier` | Freshness labels |
| Files (`java.nio.file`) | `CsvFile`, `SeedDataCopier` | Persistence |
| Hashing (`MessageDigest`, `SecureRandom`) | `PasswordHasher` | Password security |
| One `Comparator` | `SearchService` | Result ordering |

Deliberately not used: records, sealed classes, streams beyond the one comparator, threads, reflection, frameworks, databases, GUI.

### 15.4 How Likely Changes Stay Local

| Change | What you touch | What you do not touch |
|---|---|---|
| Add a region | Rows in `localities.csv`; `location.activeRegion` | Any Java class |
| Add a field to Pharmacy (for example phone) | `Pharmacy`, `FilePharmacyRepository` (row mapping), one prompt in `PharmacistMenu`, a column in `pharmacies.csv` | Other repositories, services, files |
| Change AI provider | A new class implementing `LlmClient`, wiring in `AppContext` | Everything else |
| Replace CSV with a database | New repository implementations, wiring in `AppContext` | Services, menus, logic |
