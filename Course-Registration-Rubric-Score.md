# AI Usage Record: Course Waitlist

Prompts are quoted as typed, with spelling corrected for readability. Where a prompt also covered unrelated housekeeping, only the relevant part is quoted. Prompts marked _(summarised)_ are paraphrased without changing their meaning.

---

## 1. Summary

### Contribution

The developer owned the engineering decisions and directed, reviewed and verified everything the AI produced:

- **Scope and policy.** Read the brief, chose the in-memory fallback when no starter project or rubric was supplied, and made the ten design decisions D1–D10 (section 3). That includes the policy question the brief singles out: an ineligible learner at the front of the waitlist is **skipped**.
- **Task decomposition.** Split the build into phases (scaffold → model → repositories → service → tests → review → docs). Each phase was reviewed and approved before the next one started.
- **Review.** Ran an adversarial review of the code and decided each finding: **3 applied, 2 rejected**, the remaining candidates dismissed or deferred (entries 12–20).
- **Overrode AI advice.** Twice on project structure: the package name (entries 24–25), and the package layout so that `service/` holds only services (entries 26–27).
- **Kept tests focused.** Kept the **16 tests that map to the brief's acceptance criteria**, so the suite is easy to understand (entry 28).

## 2. Tools and way of working

|                                    |                                                                                                                                                                                          |
| ---------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Planning (entries 1–4)             | Claude chat (claude.ai), **Claude Opus 5.5**                                                                                                                                             |
| Build, review, docs (entries 5–28) | Claude Code , **Claude Sonnet 5.5**                                                                                                                                                      |
| Working pattern                    | Plan and decisions fixed in a document before any code. One phase per prompt. A test run after every change. The developer approved each checkpoint.                                     |
| Handoff                            | Decisions carried from the chat through two working files, `Design Approach.md` and `Implementation Approach.md` (entry 4), so the coding assistant worked from the same fixed decisions |
| Data hygiene                       | No credentials, secrets or personal data in any prompt. IDs are synthetic (`L1`–`L6`, `CS101`, `CS201-FALL-2026`).                                                                       |

## 3. Design decisions made by the developer

| #   | Question                                        | Decision                                                                                              |
| --- | ----------------------------------------------- | ----------------------------------------------------------------------------------------------------- |
| D1  | Prerequisite rule                               | One completed course per offering; none means no prerequisite                                         |
| D2  | Ineligible learner at the front of the waitlist | **Skip**: remove them from the waitlist, report them in the result, promote the next eligible learner |
| D3  | Ineligible learner asks to enroll               | Reject with `PREREQUISITE_NOT_MET`; never joins the waitlist                                          |
| D4  | Duplicate enroll                                | No-op: `ALREADY_ENROLLED` / `ALREADY_WAITLISTED`                                                      |
| D5  | Drop while waitlisted                           | Allowed: `LEFT_WAITLIST`                                                                              |
| D6  | Drop when not enrolled, or a repeated drop      | `NOT_ENROLLED`, state unchanged                                                                       |
| D7  | Re-enroll after dropping                        | Allowed as a new request (back of the queue if the course is full)                                    |
| D8  | Promotion                                       | Inside the same locked drop call; loop until the seat is filled or the queue is empty                 |
| D9  | Concurrency                                     | One `ReentrantLock` per offering around every command                                                 |
| D10 | Persistence                                     | In-memory repositories behind interfaces (no starter project was supplied)                            |

Other choices made during planning: Java 17 with Maven, REST API only (no CLI), duplicate enroll returns 200 and a drop when not enrolled returns 409.

---

## 4. At a glance

| #     | Date       | Activity                                                           | Decision                                                          |
| ----- | ---------- | ------------------------------------------------------------------ | ----------------------------------------------------------------- |
| 1     | 2026-11-06 | Read the brief; handle the missing starter project and rubric      | Accepted after checking against the PDF                           |
| 2     | 2026-11-06 | Working plan and open questions                                    | **Edited**: developer answered the questions and set D1–D10       |
| 3     | 2026-11-06 | AI redesign of the agreed planin way different approach            | **Rejected**;Previosu Agreed plan restored.                       |
| 4     | 2026-11-06 | Handoff files for the build                                        | Accepted                                                          |
| 5–11  | 2026-11-06 | Phased build: scaffold, model, repositories, service, tests, RESTt | Accepted phase by phase                                           |
| 12–20 | 2026-11-06 | Adversarial review: 5 findings plus candidates                     | **3 applied, 2 rejected**, rest dismissed or deferred             |
| 21–23 | 2026-11-06 | Plan alignment, README, design docs                                | Accepted                                                          |
| 24    | 2026-11-07 | Structure and naming review                                        | **Partly rejected**                                               |
| 25    | 2026-11-07 | Package rename                                                     | Accepted                                                          |
| 26    | 2026-11-07 | Question: enums in the domain package                              | Accepted, then challenged                                         |
| 27    | 2026-11-08 | Layered package layout                                             | **AI advice overridden** and Sugggest different correct approoach |
| 28    | 2026-11-08 | Test suite on problem statement scope                              | **Developer decision**: 30 → 16 tests                             |

---

## 5. Log entries

### A. Planning (Claude chat, Claude Opus 5.5)

#### Entry 1: Understanding the brief

> "I have got an course waitlist enrollment assessment from a company as part of an interview process. The objective is to develop a focused course-registration service that supports enrollment, dropping, prerequisite validation, and FIFO-based waitlist promotion for a single course offering.Act as a Senior Backend Engineer and Software Architect. Help me establish a technically sound solution design that aligns with the assessment requirements."

- **Decision:** **Accepted.**

#### Entry 2: Working plan

> "Lets establish a working plan for this implemtation using Java 17, SpringBoot, Maven, and JUnit. I want to document the propsed architcutre, impln phases, key design decisions, business rules, and testing approach before starting development."

- **Decision:** **Edited.** The developer answered all 11 questions: skip (D2), a single prerequisite course (D1), reject at request time (D3), allow leaving the waitlist (D5), Java 17 + Maven, REST only, 200 for a duplicate enroll and 409 for a drop when not enrolled, seed data of capacity 2 with six learners, list every AI tool used, and submit as a zip.

#### Entry 3: Preserve agreed design decisions

> "Lets keep the implementation straight forward and easy to write and maitain without compromising the design decisions we've already agreed upon.I want to simplify the code wherever possible, but I don't want to unnecessarily change the established architecture, business rules, concurrency strategy, or API contracts.Any proposed design change should be evaluated independently, with its impact and trade-offs explained before we make a decision.."

- **Decision:** **Accepted.**

#### Entry 4: Handoff to VS Code

> "Now I want to move ahead with the actual implementation in VS Code. Since our planning discussion is happening separately, I need a way to carry the agreed context and decisions into the coding environment.
> Can you prepare a Design Apprach.md with the project context, architecture, finalized decisions and development guidelines? Also create an Implementation Appraoch.md with the phased implementation steps and checkpoints.

Make sure these documents contain everything the coding assistant needs to continue the work without making assumptions or changing decisions we've already finalized."

- **Decision:** **Accepted.** These are working files, not deliverables.

### B. Phased Implementation (Claude Code, Claude Sonnet 5.5, Opus 5.5)

-- This phase prompt and implemetation is not added

### C. Review , Impact and Code review and Test Exection with Document

#### Entry 12: Review request

> "Before we consider the implementation complete, I want to do an independent review of WaitlistService, CourseOffering and the associated tests.Review it from the perspective of a senior engineer who is trying to find actual problems rather than just confirming that the implementation looks good.Look for potential bugs, race conditions, invariant violations and important edge cases that might have been missed.For each finding, explain the scenario, expected vs actual behaviour and provide a test that can reproduce the issue.Don't change any production code yet. Let's review the findings first and decide which ones are actually relevant to our assessment."

| #   | Finding (from a failing probe test)                                                                                                   | Decision      | Reason                                                                          | Affected               |
| --- | ------------------------------------------------------------------------------------------------------------------------------------- | ------------- | ------------------------------------------------------------------------------- | ---------------------- |
| 13  | `enroll` read the learner **before** waiting for the lock, so it could decide on a stale record: an ineligible learner got `ENROLLED` | **Applied**   | A real correctness bug. Fixed by reading the learner inside the lock.           | `WaitlistService.java` |
| 14  | `CourseOffering` could be overfilled when called directly                                                                             | **Applied**   | Cheap guards that protect the rules at their source                             | `CourseOffering.java`  |
| 15  | A null id caused a `NullPointerException` instead of a not-found result                                                               | **Applied**   | A clear result instead of a crash (acceptance criterion 5)                      | In-memory repositories |
| 16  | A storage failure in the middle of promotion loses learners                                                                           | **Rejected**  | The in-memory store cannot fail; recorded as a limitation for a future database | None                   |
| 17  | A blank prerequisite string blocks everyone                                                                                           | **Rejected**  | Only the seed data creates offerings                                            | None                   |
| 18  | Check order, skip edge cases, constructor validation                                                                                  | **Rejected**  | The probe tests passed; there is no bug                                         | None                   |
| 19  | Stronger concurrency tests                                                                                                            | **Deferred**, | Outside "first three only" at the time                                          | Tests                  |
| 20  | Lock-map growth, lock fairness, overwritten `promoted` variable                                                                       | **Dismissed** | Not possible with one offering and a fixed capacity                             | None                   |

## 6. Where the developer rejected or changed AI output

| Entry | AI output                                                                       | Developer's action                                  |
| ----- | ------------------------------------------------------------------------------- | --------------------------------------------------- |
| 2     | Plan with open questions                                                        | Answered them and set D1–D10                        |
| 3     | Redesign of the agreed plan (`synchronized`, fewer classes, fewer status codes) | **Rejected**; agreed plan restored                  |
| 12–20 | Eight review findings or candidates                                             | 3 applied, 2 rejected, others dismissed or deferred |
| 24    | "Keep the package name"                                                         | **Overridden**; renamed                             |
| 26–27 | "The layout is fine as it is"                                                   | **Overridden**; layered layout                      |
| 27    | Package-by-feature, hexagonal                                                   | Rejected as too heavy for one feature               |

## 7. Issues caught by verification

| Entry | Issue                                                   | Caught by      |
| ----- | ------------------------------------------------------- | -------------- |
| 1     | AI test-helper bug made 10 tests error                  | First test run |
| 2     | A concurrency test that could not detect a missing lock | Mutation check |

## 8. Final verification (2026-11-08)

| Check               | Result                                                                                                       |
| ------------------- | ------------------------------------------------------------------------------------------------------------ |
| `./mvnw clean test` | **16 tests, 0 failures**: `WaitlistServiceTest` 9, `EnrollmentControllerTest` 6, `WaitlistConcurrencyTest` 1 |

## 9. Developer sign-off

- [x] I ran `./mvnw clean test` and saw 16 tests pass.
- [x] I can explain the lock, the promotion loop and the skip rule in `WaitlistService`.
- [x] I removed the lock on a copy and saw the concurrency test fail.
- [x] I reviewed each entry above and agree with its decision.
