# Test Plan — Sunrise Dental Clinic Appointment & Patient Management System

## 1. Rationale and approach: Test-Driven Development (TDD)

This project used TDD's red-green-refactor cycle for the service layer (the
classes carrying the brief's business rules): a failing test was written
first to pin down the expected behaviour, the smallest amount of production
code was written to make it pass, then the code was cleaned up with the
test suite as a safety net. For example, `PasswordUtilTest` was written
before `PasswordUtil.verify()` existed: `verifyReturnsFalseForWrongPassword`
and `twoUsersWithTheSamePasswordGetDifferentHashesBecauseSaltsDiffer` were
red first, `PasswordUtil` was implemented until they went green, and the
constant-time string comparison was then added as a refactor without
changing any test — proof the refactor didn't change behaviour.

TDD was chosen over "write the code, then test it afterwards" because the
brief's core complaint about the manual system is *incorrect behaviour*
(double bookings, billing errors) rather than missing features. Writing the
test first forces the specific failure mode ("two appointments for the same
dentist at the same time must not both succeed") to be stated precisely
before any code exists to satisfy it, which is exactly the kind of defect
this project needed to design out from the start.

**Test isolation strategy.** Every service-layer test runs against
in-memory fake DAOs (`src/test/java/com/sunrisedental/fakes`) instead of a
live MySQL database. This is standard practice, not a workaround: unit
tests should be fast, deterministic, and independent of external state.
Because `AppointmentService`, `AuthenticationService` and `BillService`
depend only on the `IAppointmentDAO`/`IUserDAO`/`IBillDAO` *interfaces*
(the DAO pattern from Task A), swapping the real MySQL implementation for
an in-memory one requires no change to the classes under test — this is
exactly why the DAO pattern was chosen in the architecture.

## 2. Test data

| Category | Valid example | Invalid / boundary example | Why chosen |
|---|---|---|---|
| Contact number | `0771234567` | `771234567` (no leading 0), `12345` (too short), blank | Off-by-one boundary on length + format, matching the brief's requirement to "restrict invalid entries" |
| Patient name | `Kasun Perera`, `Anne-Marie O'Brien` | `1Kasun` (starts with digit), `K` (too short) | Covers punctuation that must be *allowed* as well as input that must be *rejected* |
| Appointment date | today, tomorrow | yesterday, +7 months, `03/09/2026` (wrong format) | Boundary either side of the valid window, plus a format error |
| Appointment time | `08:00`, `20:00` (exact clinic-hours boundary) | `07:59`, `20:01` | Boundary values are the most common place off-by-one bugs hide |
| Dentist/treatment slot | two different times, same dentist | same dentist, same date, same time (twice) | Directly tests the brief's named problem: "double bookings" |
| Bill discount | `0`, `500` | `-100` (negative), `999999` (exceeds bill total) | Business rule boundaries on `Bill.calculateTotal()` |
| Login | correct username + password | wrong password, unknown username, blank fields | Confirms the generic-error security decision (Task A, Section 4.1) actually holds in code, not just on paper |

## 3. Test suite summary

48 automated JUnit 5 tests across 6 classes:

| Class | Tests | What it proves |
|---|---|---|
| `PasswordUtilTest` | 5 | Salting/hashing behaves correctly and consistently |
| `InputValidatorTest` | 17 | Every validation rule behind "Validate Appointment Data" (Task A «include») holds at its boundaries |
| `JsonTest` | 4 | The hand-rolled JSON layer round-trips objects, arrays, escaping and `null` correctly |
| `AuthenticationServiceTest` | 5 | Login sequence diagram (Task A Fig. 3), including the generic-error security rule |
| `AppointmentServiceTest` | 10 | Register/search/reschedule/cancel sequence diagrams (Task A Fig. 4), including double-booking rejection and re-booking after cancellation |
| `BillServiceTest` | 7 | Calculate & Print Bill sequence diagram (Task A Fig. 5), including the «extend» discount case |

Run with `mvn test` (Maven downloads JUnit 5 automatically) or from
IntelliJ's built-in test runner. All 48 tests were also executed against
these exact assertions using a minimal reflection-based runner during
development, confirming 48/48 pass, before the suite was finalised for
submission.

## 4. Traceability: requirement → design → test

| Brief requirement | Task A artefact | Code | Test |
|---|---|---|---|
| Secure login | Login sequence diagram | `AuthenticationService` | `AuthenticationServiceTest` |
| "restrict invalid entries" | Validate Appointment Data «include» | `InputValidator` | `InputValidatorTest` |
| Prevent double bookings (named clinic problem) | — (assumption, Task A §2.3) | `AppointmentService` + `trg_prevent_double_booking` | `doubleBookingTheSameDentistSlotIsRejected` |
| Calculate total treatment cost & print bill | Calculate & Print Bill sequence diagram | `BillService`, `Bill.calculateTotal()` | `BillServiceTest` |
| Apply discount/insurance (optional) | Apply Discount «extend» | `BillService.generateBill(discount)` | `discountIsSubtractedFromTheTotal` |

## 5. Test automation / CI

The same `mvn test` command used locally is wired into GitHub Actions
(`.github/workflows/ci.yml`) so every push and pull request automatically
re-runs the full suite — see the Task D section of the report for the
workflow file and a walkthrough of what it does.

## 6. Known limitation

This suite deliberately does not include integration tests that hit a live
MySQL instance, because the development sandbox used to prepare this
project could not install MySQL (see README.md, "Known limitation"). The
DAO implementations (`AppointmentDAOImpl` etc.) were written against the
same `java.sql` API used throughout, reviewed by hand against `schema.sql`,
and are structurally identical to the fakes that *are* tested — but running
them against a real database, and adding a small number of integration
tests with `@Tag("integration")` that only run when a database is
available, is recommended as the next step once the project is opened in
an environment with MySQL installed.
