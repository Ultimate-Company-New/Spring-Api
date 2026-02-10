# Purpose

This instruction file defines **strict, non-negotiable rules** for how the Coding AI (GPT‑4.1) must **verify, audit, and report on unit tests** in this codebase. The AI is **not allowed to modify code directly**. Its sole responsibility is to **analyze existing tests**, identify **gaps or violations**, and produce a **clear, structured verification report**.

This document is intentionally explicit and redundant to compensate for weaker reasoning and to prevent interpretation drift.

---

# Scope of Analysis

The AI must analyze the following layers **together**:

* Service files
* Unit test folders corresponding to those services
* Base test files used for mocking/initialization
* Controller permission/auth tests
* Error message constants files

The AI must treat **each public service method as a contract that must be tested**.

---

# Hard Validation Rules (Must Enforce All)

## Rule 1: One Test File per Public Service Method

* Every **public method** in a service **MUST** have:

  * Its **own dedicated unit test file**
  * Located in the same test folder hierarchy as the service
* Shared or combined test files for multiple public methods are **NOT allowed**

❌ Violation if:

* A public service method has no test file
* Multiple public methods are tested in a single file

---

## Rule 2: Test Count Declaration

* Each unit test file **MUST declare the total number of tests at the very top** of the file
* Format must be explicit and human‑readable (example):

  ```
  // Total Tests: 7
  ```

❌ Violation if:

* Count is missing
* Count does not match actual number of tests

---

## Rule 3: Mandatory Controller Permission Test

* Each unit test file **MUST include at least one permission/auth test**
* This test **MUST directly hit the controller**, not the service

❌ Violation if:

* No controller permission test exists
* Permission test mocks or bypasses the controller

---

## Rule 4: Test Annotations (Strict)

* Every test **MUST** use:

  * `@Test`
  * `@DisplayName("<non-empty text>")`

* **Absolutely forbidden**:

  * `@ParameterizedTest`
  * Any other test annotations

❌ Violation if:

* Missing `@DisplayName`
* Empty or meaningless display name
* Any parameterized or alternate test annotation is used

---

## Rule 5: Test Method Naming Convention

Each test method name **MUST** follow this exact format:

```
<serviceMethodName>_<typeOfTest>_<outcome>
```

Examples:

* `createUser_success`
* `createUser_validation_failure`
* `getOrderById_notFound_exception`

❌ Violation if:

* Naming deviates from format
* Method name does not map to the actual service method

---

## Rule 6: Centralized Mocking & Initialization

* **ALL mocking and initialization MUST occur in the base test file**
* Individual test files **MUST NOT**:

  * Initialize services
  * Configure mocks
  * Create shared test objects

❌ Violation if:

* Any mock setup exists outside the base test file

---

## Rule 7: Exception Assertion Strictness

* Every test validating an exception **MUST**:

  * Assert the **exact exception type**
  * Assert the **exact error message string**

❌ Violation if:

* Error message is not asserted
* Partial or contains‑only checks are used

---

## Rule 8: Error Message Constants Only

* Error messages **MUST** come from the centralized error message constants file
* **NO hardcoded strings** are allowed in assertions

❌ Violation if:

* Any string literal is used instead of a constant

---

## Rule 9: Mandatory Test Documentation Block

Every unit test **MUST** include a comment block directly above it with **this exact structure**:

```
// Purpose:
// Expected Result:
// Assertions:
```

❌ Violation if:

* Comment block is missing
* Structure is altered

---

## Rule 10: Test Grouping & Ordering

Tests inside each file **MUST be grouped and ordered as follows**:

### Section 1: Success Tests

* All success paths
* Sorted alphabetically A–Z by test method name

### Section 2: Failure / Exception Tests

* All validation, error, and exception cases
* Sorted alphabetically A–Z

### Section 3: Controller Permission/Auth Tests

* All controller‑level permission tests
* Sorted alphabetically A–Z

❌ Violation if:

* Sections are mixed
* Alphabetical order is incorrect

---

## Rule 11: Comprehensive Coverage

* Every logical code path in the service **MUST be covered**:

  * Happy path
  * Validation failures
  * Not‑found scenarios
  * Permission failures
  * Any conditional branches

❌ Violation if:

* Any reachable branch has no test

---

# Required Output: Verification Report

The AI **MUST generate a structured report** with the following sections:

## 1. Summary Status

* Overall status: ✅ GREEN or ❌ RED
* Total services analyzed
* Total public methods
* Total test files found

## 2. Missing or Incorrect Test Files

For each issue:

* Service name
* Public method name
* Missing or incorrect file path
* Exact rule(s) violated

## 3. Test Content Violations

For each test file:

* Missing test count
* Incorrect annotations
* Naming violations
* Missing documentation blocks
* Ordering/grouping issues

## 4. Mocking & Initialization Violations

* Any mocking found outside base test file
* File name and line reference

## 5. Exception & Error Message Violations

* Tests missing exact message assertions
* Tests using hardcoded error strings

## 6. Coverage Gaps

* Specific code paths not covered
* Method and branch description

## 7. Final Verdict

* If **no issues exist**:

  * Produce a clean **GREEN report**
  * Explicitly state that all rules are satisfied
* If **issues exist**:

  * Clearly list **what must be added or fixed**
  * No vague language allowed

---

# Tone & Style Requirements

* Be **strict, deterministic, and explicit**
* No assumptions
* No praise
* No speculative language
* No refactoring suggestions beyond what is required to satisfy the rules

---

# Absolute Rule

If something is **uncertain**, treat it as **non‑compliant** and report it.
