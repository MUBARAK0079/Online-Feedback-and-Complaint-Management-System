# Automated Web Testing Pipeline: Selenium, JUnit 5 & Formal QA Design

> **Course:** Software Testing & Quality Assurance (ISE2501-CA3)
> **Target Application:** Online Feedback and Complaint Management System (`http://localhost:8080`)

An end-to-end automated test suite for a web-based feedback and complaint portal. It combines **Selenium WebDriver** and **JUnit 5** automation with formal test design techniques: **Equivalence Class Partitioning (ECP)**, **Boundary Value Analysis (BVA)** and **white-box Control Flow Graph (CFG) analysis**.

---

## Table of Contents

1. [Application Scope](#application-scope)
2. [Tech Stack](#tech-stack)
3. [Project Structure](#project-structure)
4. [Test Design](#test-design)
   - [Equivalence Class Partitioning](#1-equivalence-class-partitioning-ecp)
   - [Boundary Value Analysis](#2-boundary-value-analysis-bva)
   - [Control Flow Graph & Basis Paths](#3-control-flow-graph-cfg--cyclomatic-complexity)
5. [Automation Implementation](#automation-implementation)
6. [Getting Started](#getting-started)
7. [Execution Results](#execution-results)
8. [Team & Contributions](#team--contributions)

---

## Application Scope

The suite covers the full user journey of the portal:

**Open App → Register → Authenticate → Submit Ticket → Track Status**

| Area | Coverage |
|---|---|
| **User Onboarding** | Portal availability and registration validation |
| **Session Security** | Valid login and invalid credential rejection |
| **Feedback Intake** | Structured user feedback submission flow |
| **Complaint Lifecycle** | Ticket creation and live status tracking |

**7 automated scenarios** are implemented in total.

---

## Tech Stack

| Component | Purpose |
|---|---|
| **Selenium WebDriver** | ChromeDriver browser control |
| **JUnit 5 (Jupiter)** | Test lifecycle, ordering, parameterization and `TestWatcher` hooks |
| **Assertions (JUnit / AssertJ)** | DOM and URL verification |
| **Maven** | Build and dependency management |
| **Embedded server (`WebApp.java`)** | Hosts the target application on port 8080 |
| **Local reporter** | HTML results dashboard served at `/results` |

The **Page Object Model (POM)** separates UI locators and actions from test assertions, keeping tests readable and easy to maintain.

---

## Project Structure

```
feedback-test/
├── pom.xml                         # JUnit 5 + Selenium dependencies
├── src/main/java/app/
│   ├── WebApp.java                 # Embedded server
│   └── pages/                      # Page Objects (locators & actions)
└── src/test/java/app/
    └── FeedbackSystemTest.java     # Test suite
```

---

## Test Design

### 1. Equivalence Class Partitioning (ECP)

| Input Field | Valid Partition | Invalid Partitions | Test Value | Expected Outcome |
|---|---|---|---|---|
| Email ID | Standard format (`a@b.c`) | Missing `@` or domain | `user@domain.com` | Accepted |
| Password | 8–20 alphanumeric chars | < 8 chars, > 20 chars, blank | `Pass@1234` | Session Created |
| Login Auth | Registered credentials | Wrong password / unknown user | `wrong@123` | Access Denied |
| Ticket Text | 10–500 characters | Empty or > 500 chars | `Portal lag report` | Ticket Logged |

### 2. Boundary Value Analysis (BVA)

**Domain:** Feedback & complaint text length. **Valid range:** 10 to 500 characters.

| Boundary Point | Min − 1 | Min | Min + 1 | Nominal | Max − 1 | Max | Max + 1 |
|---|---|---|---|---|---|---|---|
| **Char Count** | 9 | 10 | 11 | 250 | 499 | 500 | 501 |
| **System Action** | Reject | Accept | Accept | Accept | Accept | Accept | Reject |

### 3. Control Flow Graph (CFG) & Cyclomatic Complexity

Method under analysis:

```java
// Node 1: Entry
boolean validateSubmission(String txt, String auth) {
  // Node 2: Auth Predicate (P1)
  if (auth == null || !isValid(auth)) {
    return false; // Node 3
  }
  // Node 4: Length Predicate (P2)
  if (txt.length() < 10 || txt.length() > 500) {
    return false; // Node 5
  }
  return true; // Node 6: Exit
}
```

| Node | Description |
|---|---|
| N1 | Method entry |
| N2 (P1) | Auth session check |
| N3 | Reject unauthenticated |
| N4 (P2) | Bounds check (10..500) |
| N5 / N6 | Reject / commit exit |

**Cyclomatic complexity**

| Method | Result |
|---|---|
| Edges − Nodes + 2 | 7 − 6 + 2 = **3** |
| Predicate nodes + 1 | 2 + 1 = **3** |
| Regions | 2 enclosed + 1 outer = **3** |

**Basis paths**

| Path | Route | Meaning |
|---|---|---|
| 1 | N1 → N2 → N3 → N6 | Invalid session triggers immediate rejection |
| 2 | N1 → N2 → N4 → N5 → N6 | Valid session with out-of-range length |
| 3 | N1 → N2 → N4 → N6 | Valid session and compliant input accepted |

---

## Automation Implementation

Key framework highlights:

- **`@BeforeAll` setup:** launches the embedded server and ChromeDriver.
- **`@TestMethodOrder`:** enforces sequential user-journey state (register → login → submit → track).
- **`TestWatcher` hook:** records live PASS/FAIL results per test for the `/results` dashboard.
- **Explicit assertions:** validate DOM transitions and redirects without hardcoded `Thread.sleep` calls.

**Parameterized tests** bind the ECP and BVA matrices directly to Page Object actions:

```java
@ParameterizedTest
@CsvSource({
  "valid@test.com, Pass@1234, true",
  "invalid-email,  Pass@1234, false",
  "valid@test.com, short,     false"
})
void testLoginPartitions(String user, String pwd, boolean ok) {
  LoginPage page = new LoginPage(driver);
  page.submitCredentials(user, pwd);
  assertEquals(ok, page.isDashboardVisible());
}
```

---

## Getting Started

### Prerequisites

- JDK 11 or higher (match the version configured in `pom.xml`)
- Apache Maven 3.6+
- Google Chrome (a compatible ChromeDriver is required; Selenium 4 can manage this automatically)
- Port **8080** free on your machine

### Clone

```bash
git clone https://github.com/<username>/online-feedback-complaint-management-system.git
cd online-feedback-complaint-management-system/feedback-test
```

### Run the tests

```bash
mvn clean test
```

The test class starts the embedded server (`WebApp.start()`) and launches Chrome automatically. Chrome opens, runs the 7 scenarios in order, and then shows the results page.

You can also run `FeedbackSystemTest` directly from Eclipse or IntelliJ using the JUnit 5 runner.

### View the report

While the server is running, open:

```
http://localhost:8080/results
```

---

## Execution Results

| Metric | Value |
|---|---|
| Tests executed & passed | **7 / 7** |
| Failures / errors | **0** |
| Assertion pass rate | **100%** |
| Total suite duration | **14.855 s** (Eclipse JUnit runner: 8.996 s) |

| # | Test Case | Result |
|---|---|---|
| 1 | `verifyWebsiteOpens` | PASS |
| 2 | `verifyRegistration` | PASS |
| 3 | `verifyLoginValid` | PASS |
| 4 | `verifyLoginInvalid` | PASS |
| 5 | `verifyFeedbackSubmission` | PASS |
| 6 | `verifyComplaintSubmission` | PASS |
| 7 | `verifyComplaintStatus` | PASS |

> Add your screenshots here, for example `docs/junit-runner.png` and `docs/results-dashboard.png`:
>
> `![JUnit 5 Runner](docs/junit-runner.png)`
> `![Live Results Dashboard](docs/results-dashboard.png)`

---

## Team & Contributions

| Student | Role | Contribution |
|---|---|---|
| **Student A** — [Name / USN / Section] | Automation Architect (50%) | Maven `pom.xml`, ChromeDriver setup, Page Objects, `TestWatcher` reporting |
| **Student B** — [Name / USN / Section] | Test & White-Box Specialist (50%) | ECP partitions, BVA tables, CFG basis paths, parameterized JUnit 5 tests |

---

## License

Developed for academic purposes as part of ISE2501-CA3. Add a license here if you plan to share the code publicly.
