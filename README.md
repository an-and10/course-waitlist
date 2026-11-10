# Course Waitlist

A small Spring Boot REST service for one course offering: enroll, drop, FIFO waitlist promotion and a prerequisite check. A learner can hold only one place. When the course is full, eligible learners join the waitlist. When a seat opens, the earliest eligible learner is promoted.

No starter project or rubric was supplied, so this uses the brief's fallback: a minimal service with an in-memory store.

## Requirements

Java 17 or later. Maven is not needed; use the wrapper. The build targets Java 17 .

## Commands

| Task                                     | Command                  |
| ---------------------------------------- | ------------------------ |
| Run all tests                            | `./mvnw clean test`      |
| Start the app on port 8080               | `./mvnw spring-boot:run` |
| Run the smoke test, in a second terminal | `bash smoke.sh`          |

State is in memory, so run `smoke.sh` against a started app.

## Seeding data

| Offering          | Capacity | Prerequisite |
| ----------------- | -------- | ------------ |
| `CS201-FALL-2026` | 2        | `CS101`      |

| Learners                     | Completed `CS101`? | Eligible? |
| ---------------------------- | ------------------ | --------- |
| `L1`, `L2`, `L3`, `L5`, `L6` | Yes                | Yes       |
| `L4`                         | No                 | No        |

## API

| Method | Path                                              | Body                 | Purpose                     |
| ------ | ------------------------------------------------- | -------------------- | --------------------------- |
| POST   | `/offerings/{offeringId}/enrollments`             | `{"learnerId":"L1"}` | Enroll or join the waitlist |
| DELETE | `/offerings/{offeringId}/enrollments/{learnerId}` | None                 | Drop, or leave the waitlist |
| GET    | `/offerings/{offeringId}`                         | None                 | Read the current state      |

| Result                                                               | HTTP status |
| -------------------------------------------------------------------- | ----------- |
| `ENROLLED`, `WAITLISTED`                                             | 201         |
| `ALREADY_ENROLLED`, `ALREADY_WAITLISTED`, `DROPPED`, `LEFT_WAITLIST` | 200         |
| `NOT_ENROLLED`                                                       | 409         |
| `PREREQUISITE_NOT_MET`                                               | 422         |
| Unknown offering or learner                                          | 404         |
| Blank or missing `learnerId`, or malformed JSON                      | 400         |

### Examples

Enroll a learner:

```bash
curl -X POST http://localhost:8080/offerings/CS201-FALL-2026/enrollments \
  -H "Content-Type: application/json" -d '{"learnerId":"L1"}'
```

```json
{ "outcome": "ENROLLED", "learnerId": "L1", "waitlistPosition": null }
```

Join the waitlist when the course is full (HTTP 201):

```json
{ "outcome": "WAITLISTED", "learnerId": "L3", "waitlistPosition": 1 }
```

Drop a learner, which promotes the head of the waitlist:

```bash
curl -X DELETE http://localhost:8080/offerings/CS201-FALL-2026/enrollments/L1
```

```json
{
  "outcome": "DROPPED",
  "learnerId": "L1",
  "promoted": "L3",
  "skipped": [],
  "seatsAvailable": 0
}
```

Read the state:

```bash
curl http://localhost:8080/offerings/CS201-FALL-2026
```

```json
{
  "offeringId": "CS201-FALL-2026",
  "capacity": 2,
  "prerequisiteCourseId": "CS101",
  "seatsAvailable": 0,
  "enrolled": ["L2", "L3"],
  "waitlist": []
}
```

## Assumptions

- **Prerequisite:** each offering has one required completed course, or none.
- **Ineligible learner reaches the front of the waitlist:** they are skipped. They are removed from the waitlist, listed in `skipped`, and the next eligible learner is promoted.
- **Ineligible learner requests a place:** rejected with `PREREQUISITE_NOT_MET`. They never join the waitlist.
- **Duplicate enroll:** a no-op returning `ALREADY_ENROLLED` or `ALREADY_WAITLISTED`.
- **Drop while waitlisted:** allowed, and returns `LEFT_WAITLIST`.
- **Repeated drop, or drop when not enrolled:** returns `NOT_ENROLLED` and changes nothing.
- **Re-enroll after a drop:** treated as a fresh request, so it goes to the back of the queue if the course is full.
