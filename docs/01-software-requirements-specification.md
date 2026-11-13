# MedFinder — Software Requirements Specification (SRS)

| | |
|---|---|
| **Document** | 01 — Software Requirements Specification |
| **Version** | 1.2 (simplified baseline for implementation) |
| **Date** | 13 November 2026 |
| **Related documents** | 02 High-Level Design · 03 Architecture and Folder Structure · 04 Implementation Plan · 05 Team Responsibilities and Dependencies |

## Change Log

| Version | Date | Change |
|---|---|---|
| 1.0 | 11 Nov 2026 | First baseline |
| 1.1 | 11 Nov 2026 | Addis Ababa only; location = region, sub-city, optional woreda with area-match score; licence stored only and unique; administrator may withdraw a Verified pharmacy; `localities.csv`; NFR-09 (object-oriented design). |
| 1.2 | 13 Nov 2026 | **Simplification for a one-month, five-person course project.** **AI mode is search only:** the customer describes what they need and where they live; the LLM returns search keywords and, optionally, a sub-city; MedFinder searches its own data. AI medicine information (INFO intent), the AI-GENERATED label and caution text, strength/form filters, the 120-word limit and the per-run request limit are removed. **Location is sub-city only:** woreda is removed everywhere; match levels are *Same sub-city* and *Other area*; same-sub-city results are listed first. **Smaller scope:** pharmacy list by status, the pharmacy detail view and catalogue filters are removed. **New:** administrator can list all user accounts (FR-ACC-09). Section 7 lists every removed or changed requirement. Applies to all five documents. |

---

## 1. Introduction

### 1.1 Purpose

This document defines **what** the MedFinder system must do. It is the single reference for requirements: every feature to be built, every rule it must follow, and the conditions under which the finished system will be accepted. It does not describe how the system is built; that is covered in the High-Level Design.

### 1.2 Product Scope

MedFinder is a Java command-line application that helps patients find out, before travelling, which **verified pharmacies** have a medicine in stock, how recent that stock information is, and whether each pharmacy is in the customer's own sub-city. Version 1.2 serves pharmacies in **Addis Ababa only**; other regions can be added later as data (NFR-09). It also offers an **AI mode**: the customer describes in plain language what they need and where they live, a large language model (LLM) turns that into search keywords and a sub-city, and MedFinder searches its own data.

MedFinder provides **information only**. It does not sell, reserve or deliver medicines, take payments, validate prescriptions, or give medical information, diagnoses or treatment advice. The AI is used only to understand the customer's request; **no AI-written text is ever shown to the customer**. Displayed availability is the latest value reported by a pharmacy and is not a guarantee.

MedFinder is developed as a semester project for an Object-Oriented Programming course taught in Java (chapters 1–6: paradigms, Java basics, classes and objects, inheritance and abstraction, polymorphism, exception handling). The course requires that the system integrates an LLM.

### 1.3 Definitions

| Term | Meaning |
|---|---|
| Customer | Any person searching for a medicine. Customers need no account and do not log in |
| Pharmacist | A pharmacy staff member who registers one pharmacy and maintains its stock |
| Administrator | The person who verifies pharmacies |
| Medicine (catalogue entry) | A standard description of a medicine: name, category, dosage form, strength, prescription requirement |
| Stock record | A pharmacy's reported quantity and price for one medicine, with the time it was last confirmed |
| Reconfirm | Mark a stock record as checked again now, without changing its values |
| Freshness | A label describing how recently a stock record was confirmed: Fresh, Aging or Stale |
| Verified pharmacy | A pharmacy approved by the administrator; only these appear to customers |
| Normal mode | Search by typing a medicine name |
| AI mode | A free-text request ("I need amoxicillin and paracetamol, I live in Bole") interpreted by an LLM into search keywords and a sub-city |
| Keyword | A medicine name extracted by the LLM from the customer's request and searched with the normal-search rules |
| LLM | Large language model: an AI service that understands text, accessed over the internet |
| Relevance | A number from 0 to 1 describing how closely a medicine name matches the text searched |
| Region | The top-level administrative area served. In version 1.2 the only region is Addis Ababa |
| Sub-city | An administrative division of Addis Ababa (11 in total). In code and data it is called **district**, so that other regions can use their own term; the word shown to users comes from the settings |
| Location | A region and a sub-city. Both pharmacies and customers have one |
| Match level | How a pharmacy's location relates to the customer's: **Same sub-city** or **Other area** |
| Licence number | Free text entered by the pharmacist. MedFinder stores it but does not verify it; the administrator judges it outside the system |

### 1.4 Requirement Priorities

| Priority | Meaning |
|---|---|
| **Must** | Required for the system to be accepted |
| **Should** | Important; included unless time runs out (see the cut order in the Implementation Plan) |
| **Could** | Desirable; built only if time remains |

---

## 2. Overall Description

### 2.1 Problem Context

In Addis Ababa, a patient with a prescription must find a pharmacy that holds every item on it. Each pharmacy keeps stock records in paper registers or simple spreadsheets visible only inside that pharmacy. There is no shared place to check availability, so patients telephone or visit pharmacies one by one. Stock differs between pharmacies, shortages are common, and the age and reliability of any answer are unknown to the patient. Pharmacy staff spend time on repeated enquiries. The pilot story is the Kenema Pharmacy network (a non-profit, publicly managed network of pharmacies in Addis Ababa), but any pharmacy in Addis Ababa may take part. The system has no Kenema-specific rules or code, and all sample pharmacies are fictional.

### 2.2 Product Perspective

MedFinder is a stand-alone console application. All users run the same program against the same set of local data files. The only external dependency is an LLM service reached over HTTPS, used by AI mode only. Every other function works without internet access.

### 2.3 Summary of Functions

1. Accounts, login and role-based access for pharmacists and the administrator (customers use the system without an account)
2. Pharmacy registration and administrator verification (approve, reject with reason, or withdraw a Verified pharmacy with reason)
3. Medicine catalogue listing
4. Stock management by pharmacists, including one-action reconfirmation
5. Freshness classification of every stock record
6. Typo-tolerant medicine search ranked by stock, sub-city, relevance and freshness
7. Optional customer location (sub-city chosen from a list)
8. AI mode: turn a free-text request into search keywords and a sub-city, then search MedFinder's own data
9. Automatic fallback to normal search when AI mode fails

### 2.4 User Classes

| User class | Description | Capabilities |
|---|---|---|
| Customer | Patient or caregiver; any level of computer skill; anonymous, no account | Search (normal and AI mode), list the catalogue, choose a sub-city |
| Pharmacist | Staff member responsible for one pharmacy | Register, log in, submit and edit the pharmacy, manage its stock once Verified |
| Administrator | Quality controller; account created by the system | Log in, review Pending pharmacies, approve or reject, withdraw a Verified pharmacy, list user accounts, judge each licence number outside the system |

### 2.5 Operating Environment

Java 17 or later on Windows, Linux or macOS; a terminal supporting standard text output; internet access for AI mode only.

### 2.6 Constraints

| ID | Constraint |
|---|---|
| C-01 | Implemented in Java; command-line interface only (no graphical or web interface) |
| C-02 | Data stored in local delimited text (CSV) files; no database server |
| C-03 | External libraries limited to a JSON library (Gson) and JUnit 5 for tests |
| C-04 | The LLM is reached through a web API using Java's built-in HTTP client |
| C-05 | The delivery deadline is about one month after 11 November 2026 (planning date: 11 December 2026, to be confirmed with the instructor) |
| C-06 | The development team has five members with mixed Java experience; the design must stay simple and well separated |
| C-07 | The code is built mainly with the concepts of course chapters 1–6. Concepts from outside those chapters are limited to those listed in document 02, Section 15.3 |

### 2.7 Assumptions and Dependencies

- Pharmacists reconfirm stock regularly; otherwise results become Stale.
- The LLM service is available during development and demonstration, and an access key can be obtained for it. If not, AI mode shows "not available" and the rest of the system continues to work.
- Sample data (medicines, pharmacies, localities, stock) is prepared by the team. Pharmacy names and licence numbers are fictional and may be renamed later.
- MedFinder does not check whether a licence number is real. The administrator decides, using their own judgement and sources outside the system.
- Customers know their sub-city.
- One installation, with one set of data files, is shared by all users on that computer.

---

## 3. Specific Requirements

### 3.1 External Interface Requirements

**User interface.** A text menu interface. Each menu prints numbered options, the user types a number, and the program acts. Every menu has a Back or Logout option. Invalid input produces a clear message and the menu is shown again. Tables are printed as aligned columns; the freshness label is printed as a word.

**Software interface (LLM).** The application sends the customer's text and fixed instructions to the LLM and must receive a JSON object of this form:

```
{
  "intent":   "SEARCH" | "UNSUPPORTED",
  "keywords": [ text, ... ],
  "subCity":  text or null
}
```

`SEARCH` carries one to five keywords (medicine names only, without strength or form) and, when the customer mentioned where they live, a sub-city. `UNSUPPORTED` is used for anything that is not a request to find medicines (for example dose, diagnosis or "what is X used for"). Any other shape is treated as a failure.

**File interface.** Data is read from and written to CSV files described in Section 3.4.

### 3.2 Functional Requirements

#### 3.2.1 Accounts and Access (ACC)

| ID | Requirement | Priority |
|---|---|---|
| FR-ACC-01 | A visitor can register as a pharmacist with full name, email address and password. The email must be valid and unique; the password must have at least 8 characters. Customers do not register. | Must |
| FR-ACC-02 | A pharmacist or the administrator can log in with email and password. A wrong email or password is refused with a message that does not reveal which one was wrong. | Must |
| FR-ACC-03 | A logged-in user can log out and return to the start menu. | Must |
| FR-ACC-04 | After login a pharmacist or the administrator sees only the menu of their role. Every pharmacist and administrator operation checks the role of the current user and refuses anything not allowed. | Must |
| FR-ACC-05 | Passwords are never stored or shown in readable form (salted hash). | Must |
| FR-ACC-06 | An administrator account exists from the first start. There is no administrator registration. | Must |
| FR-ACC-07 | The start menu gives customers direct access to the customer menu (normal search, AI mode, catalogue, location) without registration or login. | Must |
| FR-ACC-08 | No personal data about customers is collected or stored. A customer's chosen sub-city is kept only in memory and is cleared when the customer returns to the start menu. | Must |
| FR-ACC-09 | The administrator can list all user accounts showing name, email and role. Password hashes are never shown. | Should |

#### 3.2.2 Pharmacy Management (PHA)

| ID | Requirement | Priority |
|---|---|---|
| FR-PHA-01 | A pharmacist can register one pharmacy with name, licence number, sub-city (chosen from a numbered list) and address (free text, for example a landmark). The region is set automatically to the active region (Addis Ababa). It starts as **Pending**. | Must |
| FR-PHA-02 | A pharmacist can view the pharmacy's status and, if Rejected, the rejection reason. | Must |
| FR-PHA-03 | A pharmacist can edit the pharmacy's details. Editing the licence number or sub-city of a Verified pharmacy returns it to Pending, and it leaves customer search until re-approved. Editing only the name or address does not change the status. Editing a Rejected pharmacy and resubmitting returns it to Pending. | Must |
| FR-PHA-04 | The administrator can list all Pending pharmacies and view the full details of one. | Must |
| FR-PHA-05 | The administrator can approve a Pending pharmacy, making it **Verified**. | Must |
| FR-PHA-06 | The administrator can reject a Pending pharmacy; a non-empty reason is mandatory. The status becomes **Rejected**. | Must |
| FR-PHA-07 | *Removed in v1.2.* | — |
| FR-PHA-08 | The administrator can **withdraw** a Verified pharmacy by rejecting it; a non-empty reason is mandatory. The status becomes **Rejected** and the pharmacy leaves customer search. There is no separate Suspended status. The administrator picks the pharmacy from a list of Verified pharmacies. | Must |
| FR-PHA-09 | The licence number must not be empty and must be unique (ignoring letter case and spaces). No other format check is made. The licence number is shown to the pharmacist and the administrator, never to customers. | Must |

#### 3.2.3 Medicine Catalogue (CAT)

| ID | Requirement | Priority |
|---|---|---|
| FR-CAT-01 | The catalogue is loaded from a data file when the application starts. | Must |
| FR-CAT-02 | Customers and pharmacists can list the active catalogue (name, strength, dosage form, category, prescription required). No filters. | Must |
| FR-CAT-03 | Inactive medicines never appear in search results and cannot be added to stock. | Must |

#### 3.2.4 Inventory (INV)

| ID | Requirement | Priority |
|---|---|---|
| FR-INV-01 | A pharmacist of a Verified pharmacy can add a stock record by choosing a catalogue medicine and entering a quantity (whole number, 0 or more) and a price (greater than 0, in Ethiopian Birr). | Must |
| FR-INV-02 | A pharmacy can have at most one stock record per medicine; a duplicate is refused. | Must |
| FR-INV-03 | A pharmacist can edit the quantity and price of an existing record. | Must |
| FR-INV-04 | A pharmacist can remove a stock record. | Must |
| FR-INV-05 | A pharmacist can **reconfirm** one, several selected (numbers separated by commas), or all stock records in a single action without changing quantity or price. | Must |
| FR-INV-06 | Adding, editing or reconfirming a record sets its last-confirmed time to the current time. | Must |
| FR-INV-07 | A pharmacist can change only the stock of their own pharmacy, and only while it is Verified. Otherwise the action is refused with an explanation. | Must |
| FR-INV-08 | The stock list shows medicine, quantity, price, last-confirmed time and freshness label. | Must |

#### 3.2.5 Freshness (FRE)

| ID | Requirement | Priority |
|---|---|---|
| FR-FRE-01 | Every stock record is classified when it is displayed: **Fresh** if confirmed 12 hours ago or less; **Aging** if more than 12 and up to 48 hours ago; **Stale** if more than 48 hours ago. The label is never stored. | Must |
| FR-FRE-02 | The quantity and the freshness label are always displayed together. A quantity of 0 means the pharmacy reported none; Stale means the figure has not been confirmed recently. These two states are different and must not be confused. | Must |

#### 3.2.6 Location (LOC)

| ID | Requirement | Priority |
|---|---|---|
| FR-LOC-01 | A customer can optionally set a location by choosing a sub-city from a numbered list of the active region's sub-cities, and can clear it. The location is kept only until the customer returns to the start menu. | Must |
| FR-LOC-02 | When a location is set, each result shows its match level: **Same sub-city** or **Other area**, and same-sub-city results are listed before other-area results (BR-10). | Must |
| FR-LOC-03 | When no location is set, search still works; no match level is shown and location is not used for ordering. | Must |
| FR-LOC-04 | The list of regions and sub-cities is read from a data file, not written into the program. The active region and the user-facing name of its divisions ("Sub-city") are settings. | Must |
| FR-LOC-05 | A sub-city is only ever accepted if it exists in the locality list. In menus it is chosen from a numbered list; a sub-city extracted by the AI is matched against the list (FR-AI-05). | Must |

#### 3.2.7 Normal Search (SRC)

| ID | Requirement | Priority |
|---|---|---|
| FR-SRC-01 | A customer can search by typing a medicine name. Matching ignores letter case and surrounding spaces and tolerates spelling mistakes. | Must |
| FR-SRC-02 | A catalogue medicine is a match when its relevance is at least 0.6. A name that starts with, or contains, the typed text also counts as a match. | Must |
| FR-SRC-03 | Only active medicines and stock records of **Verified** pharmacies are considered. | Must |
| FR-SRC-04 | Each result shows: medicine (name, strength, dosage form), pharmacy name, sub-city and address, price, quantity, match level (when a location is set) and freshness label. The licence number is never shown. | Must |
| FR-SRC-05 | Results are ordered by the rules of BR-10. | Must |
| FR-SRC-06 | Records with quantity 0 are listed after all records with stock, and are labelled out of stock. | Must |
| FR-SRC-07 | At most 20 results are shown. | Should |
| FR-SRC-08 | *Removed in v1.2* (all needed details are already in the result row). | — |
| FR-SRC-09 | When nothing matches, a clear message says so. | Must |

#### 3.2.8 AI Mode — search only (AI)

| ID | Requirement | Priority |
|---|---|---|
| FR-AI-01 | A customer can choose **AI mode** from the customer menu, separately from normal search. | Must |
| FR-AI-02 | In AI mode the customer types, in plain language, what they need and optionally where they live (for example "I need amoxicillin and paracetamol syrup for my child, I live in Bole"). | Must |
| FR-AI-03 | The application sends only the typed request, with fixed instructions, to the LLM and obtains the structured response described in Section 3.1. | Must |
| FR-AI-04 | If the intent is **SEARCH**, the application shows the keywords the AI understood (and the sub-city, if any), then runs its own search (FR-SRC rules) for each keyword and displays results grouped per keyword. | Must |
| FR-AI-05 | If the response contains a sub-city, it is matched against the locality list (ignoring case; spelling mistakes tolerated with relevance ≥ 0.6). If it matches, that sub-city is used for this search and becomes the customer's location. If it does not match, the customer is asked to choose from the numbered list or skip. If the response has no sub-city, the customer's current location (if any) is used. | Must |
| FR-AI-06 | The AI is never used to give medical information. No text written by the AI is ever shown to the customer; only keywords and the sub-city are used. | Must |
| FR-AI-07 | If the intent is **UNSUPPORTED** (for example a dose, diagnosis or "what is X used for" question), a fixed message explains that AI mode only finds medicines in pharmacies and suggests asking a pharmacist or doctor. | Must |
| FR-AI-08 | The AI response is validated. An invalid, incomplete or empty response is treated as a failure. | Must |
| FR-AI-09 | On any failure (no key, no internet, timeout, error, invalid response) the customer is told, and the application runs a normal search using the typed text. | Must |
| FR-AI-10 | The LLM may extract the customer's stated sub-city, but stock, price and pharmacy data always come from MedFinder's own data. | Must |
| FR-AI-11 | The LLM access key is never stored in the source code or in the repository. It is read from an environment variable or a local configuration file. If it is missing, AI mode reports that it is not configured and the rest of the system works. | Must |
| FR-AI-12 | An AI request times out after at most 15 seconds (configurable), and a waiting message is shown while the application waits. | Must |
| FR-AI-13 | *Removed in v1.2.* | — |
| FR-AI-14 | At most five keywords from one AI request are processed. | Should |
| FR-AI-15 | *Removed in v1.2.* | — |

#### 3.2.9 Data and Start-up (DAT)

| ID | Requirement | Priority |
|---|---|---|
| FR-DAT-01 | All data is stored in CSV files, loaded at start-up and saved immediately after every change. | Must |
| FR-DAT-02 | On the first run, working data files are created from the bundled sample data. Sample stock records store their age in hours; when copied, each age is turned into a real last-confirmed time (now minus that age), so the Fresh, Aging and Stale examples are correct on every fresh installation. | Must |
| FR-DAT-03 | A malformed record in a data file is skipped with a warning, and the application still starts. A missing mandatory file stops start-up with a message naming the file. | Should |
| FR-DAT-04 | A failed write never leaves a data file half-written. | Should |

### 3.3 Business Rules

| ID | Rule |
|---|---|
| BR-01 | A pharmacist has at most one pharmacy. |
| BR-02 | Licence numbers are unique across pharmacies, compared ignoring letter case and spaces. MedFinder stores the licence number only; deciding whether it is genuine is the administrator's responsibility. |
| BR-03 | Only Verified pharmacies appear to customers and only they may hold stock changes. |
| BR-04 | There is at most one stock record per pharmacy and medicine. |
| BR-05 | Freshness: Fresh ≤ 12 h; Aging > 12 h and ≤ 48 h; Stale > 48 h. A record exactly 12 hours old is Fresh; exactly 48 hours old is Aging. |
| BR-06 | A rejected pharmacy always has a reason; a Verified or Pending pharmacy has none. |
| BR-07 | Pharmacist and administrator functions are available only after login with the proper role. The customer menu offers none of them. |
| BR-08 | Results describe the latest reported record. They are not a guarantee that the medicine is on the shelf. |
| BR-09 | *Removed in v1.2* (no AI text is shown, so no AI label or caution is needed). |
| BR-10 | **Ordering of results.** Results are sorted by these keys, in order: (1) quantity > 0 before quantity 0; (2) when a location is set, Same sub-city before Other area; (3) score, highest first, where score = 0.6 × relevance + 0.4 × freshness value (Fresh 1.0, Aging 0.6, Stale 0.2); (4) lower price; (5) pharmacy name A–Z. The weights and freshness values are constants in one settings class. |
| BR-11 | Pharmacy status transitions. Pending → Verified (approve). Pending → Rejected (reject, reason required). Verified → Rejected (withdraw, reason required). Rejected → Pending (edit and resubmit). Verified → Pending (licence or sub-city edited). No other transition is allowed. |
| BR-12 | A pharmacy's region must be the active region, and its sub-city must exist in the locality data for that region. |

### 3.4 Data Requirements

| Entity | Fields | Rules |
|---|---|---|
| User | id, role, full name, email, salted password hash | Email unique; role is Pharmacist or Administrator (customers have no stored data) |
| Pharmacy | id, name, licence number, region, district (sub-city), address, status, rejection reason, owner (pharmacist) | Name, licence, region, district and address not empty; status Pending, Verified or Rejected; BR-02, BR-06, BR-12 |
| Medicine | id, name, category, dosage form, strength, prescription required, active | Name, category and dosage form not empty |
| Stock record | pharmacy, medicine, quantity, price, last-confirmed time | Quantity whole number ≥ 0; price > 0; unique per pharmacy and medicine |
| Locality | region, district | One row per sub-city; the pair is unique. Used for the sub-city list and to validate locations. Adding a region means adding rows, not code |

### 3.5 Non-Functional Requirements

| ID | Category | Requirement |
|---|---|---|
| NFR-01 | Usability | A new user can complete any task within three menu levels. All messages use plain language and state what to do next. |
| NFR-02 | Performance | A normal search over 10,000 stock records returns within 1 second. |
| NFR-03 | Reliability | Invalid input or an unexpected error never ends the program; it shows a message and returns to a menu. |
| NFR-04 | Data safety | Files are written safely so that a crash cannot corrupt them (see FR-DAT-04). |
| NFR-05 | Security | Passwords stored only as salted hashes; role checked inside every operation; access key kept out of the repository. |
| NFR-06 | Maintainability | Code is separated into layers; each public class and method has a short description; pure calculation classes (`TextSimilarity`, `FreshnessClassifier`, `AiResponseParser`) have unit tests. |
| NFR-07 | Portability | Runs unchanged on Windows, Linux and macOS with Java 17 or later. |
| NFR-08 | Testability | Time, the LLM and data storage can be replaced by test substitutes. |
| NFR-09 | Object-oriented design and extensibility | The code must clearly demonstrate the concepts of course chapters 1–6: encapsulation, inheritance, abstraction (abstract classes and interfaces), polymorphism, composition and exception handling (mapped to classes in document 02, Section 15). Likely changes must stay local: adding a region means adding locality data and changing one setting; adding a pharmacy field touches only the entity, its file repository and one menu prompt; a different LLM provider is a new `LlmClient` class; a different storage is a new set of repository classes. Speculative mechanisms (plug-in loaders, unused statuses, generic attribute maps) are deliberately not built. |

---

## 4. Acceptance Criteria

The system is accepted when all **Must** requirements are implemented and all scenarios below pass on a clean installation with the bundled sample data.

| ID | Scenario | Expected result |
|---|---|---|
| AT-01 | A pharmacist registers, then registers a pharmacy | The pharmacy is Pending and does not appear in any customer search |
| AT-02 | The administrator rejects it without a reason | The action is refused |
| AT-03 | The administrator rejects it with a reason | The pharmacist sees status Rejected and the reason; after correcting and resubmitting it is Pending |
| AT-04 | The administrator approves it; the pharmacist adds stock | The stock appears in the next customer search |
| AT-05 | A pharmacist adds a second record for the same medicine | The action is refused |
| AT-06 | A pharmacist reconfirms several records | Their last-confirmed time becomes the current time and their label becomes Fresh |
| AT-07 | Sample records aged 5 h, 30 h and 60 h are searched | Labels Fresh, Aging and Stale are shown correctly |
| AT-08 | A customer searches with a misspelt name (for example "amoxcillin") | The correct medicine is found |
| AT-09 | A customer sets a sub-city and repeats a search | Match levels (Same sub-city / Other area) appear and same-sub-city results with stock come first; clearing the location removes the labels |
| AT-10 | A record with quantity 0 matches a search | It appears after all records with stock and is labelled out of stock |
| AT-11 | In AI mode, a customer types "I need amoxicillin and paracetamol, I live in Bole" | The keywords and the sub-city Bole are shown, then results for each medicine with Bole pharmacies first |
| AT-12 | In AI mode, the AI returns a sub-city that is not in the list (for example "Bolle Bulbula") | It is matched if close enough; otherwise the customer is asked to choose from the list or skip |
| AT-13 | In AI mode, a customer asks for a dose or "what is paracetamol used for?" | The fixed "AI mode only finds medicines" message appears; no AI text is shown |
| AT-14 | Internet is disconnected and AI mode is used | The customer is told, and normal search runs on the typed text |
| AT-15 | The access key is removed | AI mode reports it is not configured; everything else works |
| AT-16 | Without registering or logging in, a customer opens the customer menu from the start menu | Search, AI mode, catalogue and location work; no account is requested; no pharmacist or administrator function is offered |
| AT-17 | The application is closed and restarted | All data, including changes, is still present |
| AT-18 | Pharmacist or administrator operations are called without login, or with the wrong role | They are refused |
| AT-19 | The administrator lists user accounts | Both pharmacists and the administrator appear with their role; no password data is shown |
| AT-20 | The administrator withdraws a Verified pharmacy without a reason, then with a reason | Without a reason it is refused; with a reason the status is Rejected and its stock no longer appears in search |
| AT-21 | A pharmacist registers a pharmacy with an empty licence, then with a licence that differs from an existing one only in case or spaces | Both are refused |
| AT-22 | The pharmacist of a Verified pharmacy edits its address, then its sub-city | After the address edit it stays Verified; after the sub-city edit it is Pending and leaves search |

---

## 5. Out of Scope

- Ordering, reservations, payments, refunds and delivery
- Dispensing, prescription validation or prescription image reading
- Any medical information, diagnosis, dosage or treatment advice, including from the AI
- AI-written text shown to customers; strength or dosage-form filtering of AI keywords
- Integration with pharmacy sales, billing or enterprise systems
- Interactive maps, route directions, automatic device location, distance calculation, coordinates and woreda-level matching
- Regions other than Addis Ababa in version 1.2 (supported later by adding data)
- Automatic licence verification or lookup against any official register
- A Suspended status or a reinstate operation (withdrawal uses Rejected)
- Customer accounts, saved searches, favourites and search history
- Password reset, email notifications, third-party sign-in
- A catalogue-editing screen, catalogue filters, pharmacy list by status, staff invitation
- Log files (errors are shown as short messages; details only in debug mode)
- Graphical, web and mobile interfaces
- A central database shared between computers

## 6. Traceability

| Requirement group | Verified by |
|---|---|
| ACC | AT-16, AT-17, AT-18, AT-19 |
| PHA | AT-01 to AT-04, AT-20 to AT-22 |
| CAT | AT-04, AT-08 |
| INV | AT-04 to AT-06 |
| FRE | AT-06, AT-07, AT-10 |
| LOC | AT-09, AT-12 |
| SRC | AT-07 to AT-10 |
| AI | AT-11 to AT-15 |
| DAT | AT-17 |

## 7. Changes from v1.1 (summary for the instructor)

| v1.1 item | v1.2 status |
|---|---|
| INFO intent, `items[]` with strength/form, `subject`, `info` (3.1) | Replaced by `keywords[]` and `subCity` |
| FR-AI-04 strength/form filter and relaxation | Removed; keywords are names only |
| FR-AI-05 AI-generated information | Replaced by sub-city matching rule |
| FR-AI-06 limits on AI information | Replaced by "no AI text is ever shown" |
| FR-AI-13 120-word limit, FR-AI-15 20-request limit, BR-09 caution text | Removed |
| Woreda (FR-PHA-01/03, FR-LOC-01/02, FR-SRC-08, BR-11, BR-12, data) | Removed; sub-city only |
| BR-10 weighted location score (1.0 / 0.7 / 0.2) | Replaced by strict ordering: same sub-city first |
| FR-PHA-07 list by status, FR-SRC-08 detail view, FR-CAT-02 filters | Removed (FR-CAT-02 kept as a plain list, now Must) |
| AT-12, AT-13, AT-19 (old), AT-09, AT-11, AT-22 | Rewritten for the new scope |
| — | Added FR-ACC-09 (list users) and AT-19 (new) |
