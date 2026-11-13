# AGENTS.md — MedFinder (Team Router)

> **To the AI agent reading this:** this repository is an Object-Oriented Programming (OOP) course project built by a five-person student team. Each member has their own assigned tasks and strict file ownership. Follow the instructions below in order.

Documents and agent files are at version 1.2.

| Code | Name           | GitHub                | Instruction file                     |
| ---- | -------------- | --------------------- | ------------------------------------ |
| P1   | Worku Wondoson | @worku404             | `docs/agents/AGENTS-P1-worku.md`   |
| P2   | Tensaye Daniel | @Tensaye-Daniel       | `docs/agents/AGENTS-P2-tensaye.md` |
| P3   | Wubet Tamrat   | @wubettamrat525-prog  | `docs/agents/AGENTS-P3-wubet.md`   |
| P4   | Tesnim Abdi    | @tesnimabdi16-netizen | `docs/agents/AGENTS-P4-tesnim.md`  |
| P5   | Yamsin Mustefa | @Amuni                | `docs/agents/AGENTS-P5-yamsin.md`  |

---

## How to Use This (for AI agents AND web chats)

This works the same way whether you are a coding agent with file access (Antigravity, Cursor, Claude Code, Codex) or a web chat (ChatGPT, Claude, Gemini) where the developer has uploaded these documents.

### Step 1 — Identify the team member

If the developer has not yet said who they are, ask exactly this and wait:

> "Which team member are you? (P1 Worku, P2 Tensaye, P3 Wubet, P4 Tesnim, or P5 Yamsin)"

### Step 2 — Read their file and list their tasks

Once you know the member code (for example "I am P4"):

1. Read their instruction file (for example `docs/agents/AGENTS-P4-tesnim.md`) **in full**.
2. **Do not generate any code yet.**
3. Print a table of **all their assigned tasks** from Part 8 of their file:

   | Task ID | Title | Due Date | Done When |
   |---------|-------|----------|-----------|
   | (from Part 8) | ... | ... | ... |

4. Then ask:

   > "Which task from your list would you like to work on?"

### Step 3 — Implement the chosen task

Once the developer picks a task ID:

1. Follow Parts 2–6, 9, 10 and 11 of their instruction file exactly.
2. Edit or create **only** the files listed in Part 7 of their file.
3. Use the contracts in `docs/02-high-level-design.md` Section 4 (especially Section 4.1a for constructor signatures and accessor names) as the source of truth for how other members' classes look.
4. **You do not need the other members' Java source code.** The five project documents (`docs/01` through `docs/05`) contain every contract, constructor signature, method name and parameter type you need. If you cannot find a signature, say so — do not guess.
5. Always finish with the **Work Report** described in Part 11.

---

## Rules that apply before you know who the developer is

- Do not create, edit or delete any file.
- The project documents are in `docs/` (01 SRS, 02 High-Level Design, 03 Architecture and Folder Structure, 04 Implementation Plan, 05 Team Responsibilities). They are the source of truth.
- If an instruction file and documents 01–05 disagree, the documents win; tell the developer.
- Plain Java 17 console app, Maven, Gson and JUnit 5 only. **No streams, no lambdas, no `var`, no `record`, no `Optional`.**
- Every reply that changes a file ends with the **Work Report** described in the member's file.

---

## For web chat users (ChatGPT, Claude, Gemini) — Quick Start

If you are a student using a web chat that cannot read files from your computer:

1. **Upload the `docs/` folder** (or upload your own `AGENTS-P*-<name>.md` file plus documents `01`–`05`). You do **not** need to upload any `.java` files from other team members.
2. **Send this prompt:**
   > "I am team member P? (name). Read AGENTS.md and my instruction file. Do not write code yet. List all my Task IDs, titles, and due dates. Then ask me which task to work on."
3. **Pick a task** from the list the AI shows you.
4. **Copy the generated Java files** into your local `src/main/java/medfinder/...` folder.
5. **Run `mvn clean test`** in your terminal. If there are errors, paste the error back and ask the AI to fix it.
6. **Commit and open a Pull Request** with the branch name and message from the Work Report.
