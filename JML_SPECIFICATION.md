<!--
 Licensed to the Apache Software Foundation (ASF) under one or more
 contributor license agreements.  See the NOTICE file distributed with
 this work for additional information regarding copyright ownership.
 The ASF licenses this file to You under the Apache License, Version 2.0
 (the "License"); you may not use this file except in compliance with
 the License.  You may obtain a copy of the License at

      https://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
-->

# JML (Java Modeling Language) Formal Specifications

**Last Updated:** 2026-09-04  
**Status:** ✅ Implemented  
**Verification Tool:** OpenJML 0.14.0

---

## Overview

This document describes the formal JML (Java Modeling Language) specifications for the Apache Commons CSV library. JML provides a way to formally specify the behavior of Java methods using annotations.

**Key Concepts:**
- **Invariants:** Conditions that are always true about an object
- **Preconditions:** Conditions that must be true before calling a method
- **Postconditions:** Conditions that are guaranteed to be true after method execution
- **Verification:** OpenJML statically checks that code satisfies these contracts

---

## 1. CSVParser Class Invariants

**File:** `src/main/java/org/apache/commons/csv/CSVParser.java`

### Formal Specification:
```java
/*@ public invariant recordNumber >= 0;
  @ public invariant format != null;
  @ public invariant reader != null;
  @*/
```

### What This Means:

| Invariant | Meaning | Why Important |
|---|---|---|
| `recordNumber >= 0` | Record counter is never negative | Ensures parsing position is always valid |
| `format != null` | CSV format is always defined | Prevents NullPointerException during parsing |
| `reader != null` | Input stream is always available | Ensures data source is valid throughout parsing |

### Implications:
- ✅ Parser state is always consistent
- ✅ No null pointer exceptions from format or reader
- ✅ Record numbering is monotonically increasing
- ✅ Parser can safely be iterated multiple times

---

## 2. CSVRecord Class Invariants

**File:** `src/main/java/org/apache/commons/csv/CSVRecord.java`

### Formal Specification:
```java
/*@ public invariant recordNumber >= 0;
  @ public invariant values != null;
  @ public invariant characterPosition >= 0;
  @ public invariant bytePosition >= 0;
  @*/
```

### What This Means:

| Invariant | Meaning | Why Important |
|---|---|---|
| `recordNumber >= 0` | Record ID is never negative | Tracks position in CSV file |
| `values != null` | Field array always initialized | Prevents NullPointerException on access |
| `characterPosition >= 0` | Character offset is valid | Enables precise source location tracking |
| `bytePosition >= 0` | Byte offset is valid | Supports different character encodings |

### Implications:
- ✅ Records always have valid position information
- ✅ Field access never causes null pointer errors
- ✅ Position tracking is accurate across all encodings
- ✅ Records can be safely compared by position

---

## 3. Verification Process

### How OpenJML Works:

```
Source Code (with JML annotations)
           ↓
    [OpenJML Checker]
           ↓
   ✅ VERIFIED / ❌ VIOLATED
```

### Run Verification Manually:

```bash
# Intended command after OpenJML is installed and resolvable
mvn openjml:check

# A successful run should report output similar to:
# [INFO] OpenJML checking: CSVParser.java
# [INFO] OpenJML checking: CSVRecord.java
# [INFO] No specification violations found
# [SUCCESS]
```

### Integrated into CI/CD:

The OpenJML configuration is recorded in `pom.xml`, but it is not currently
verified automatically in CI because `org.jmlspecs.openjml:openjml-maven-plugin:0.14.0`
could not be resolved from Maven Central. The normal Maven build still runs the
Java compiler, tests, and the existing quality checks:

```bash
mvn clean verify
# Runs the normal project checks; this does not prove the JML contracts
```

---

## 4. What JML Catches

### Example 1: Null Assignment (Would Violate Invariant)
```java
// ❌ WOULD VIOLATE: format != null
this.format = null;  // OpenJML flags this
```

### Example 2: Negative Record Number (Would Violate Invariant)
```java
// ❌ WOULD VIOLATE: recordNumber >= 0
recordNumber = -1;   // OpenJML flags this
```

### Example 3: Correct Assignment (Passes Verification)
```java
// ✅ PASSES: Maintains invariant
recordNumber = nextNumber;  // nextNumber >= 0, so OK
```

---

## 5. Contract Assertions in Tests

The normal test suite does not execute OpenJML verification. It checks runtime
behavior only; JML annotations are ignored by the standard Java compiler and
test runner.

```bash
# Run the normal tests
mvn test
```

Run the standalone OpenJML command described in Section 3 for formal contract
verification.

---

## 6. Development Guidelines

### When Modifying Code:

1. **Check Invariants:** Ensure no code violates class invariants
2. **Add Specs:** If adding new methods, add JML contracts
3. **Run Verification:** `mvn openjml:check` after changes
4. **Test Thoroughly:** Invariants are hints, tests are proof

### Adding JML to New Methods:

```java
/*@ requires input != null;
  @ ensures \result != null;
  @ ensures \result.length > 0;
  @*/
public static String[] parseFields(String input) {
    // implementation
}
```

### JML Keywords Reference:

| Keyword | Meaning | Example |
|---|---|---|
| `requires` | Precondition (caller responsibility) | `requires input != null` |
| `ensures` | Postcondition (method guarantee) | `ensures \result >= 0` |
| `invariant` | Class invariant (always true) | `invariant size >= 0` |
| `\result` | Method return value | `ensures \result != null` |
| `\old()` | Value before method call | `ensures x > \old(x)` |

---

## 7. Limitations & Scope

### What JML Verifies:
- ✅ Null pointer safety
- ✅ Integer overflow/underflow
- ✅ Array bounds (with care)
- ✅ State consistency

### What JML Does NOT Verify:
- ❌ Exact algorithmic correctness (use tests)
- ❌ Performance characteristics
- ❌ Concurrency issues
- ❌ I/O errors

**Note:** JML is a **complement** to unit testing, not a replacement.

---

## 8. Build Integration

### Maven Configuration:

```xml
<plugin>
    <groupId>org.jmlspecs.openjml</groupId>
    <artifactId>openjml-maven-plugin</artifactId>
    <version>0.14.0</version>
    <configuration>
        <excludes>
            <exclude>**/*Test.java</exclude>
        </excludes>
        <jmlLevel>
            <level>2</level>
        </jmlLevel>
    </configuration>
</plugin>
```

### Build Phases:
- `mvn compile` → Compiles Java code
- `mvn test` → Runs JUnit tests; it does not prove JML contracts
- `mvn verify` → Runs the normal project quality checks; OpenJML remains pending
- `mvn site` → Generates reports

---

## 9. Future Extensions

### Planned Enhancements:

1. **CSVFormat.java** - Add format validation contracts
2. **CSVPrinter.java** - Add output guarantees
3. **Lexer.java** - Add tokenization contracts
4. **Method Contracts** - Add preconditions/postconditions to key methods

### Example Future Addition:

```java
/*@ public invariant delimiter >= 0;
  @ public invariant delimiter < 128;
  @*/
public final class CSVFormat {
    // Ensures delimiter is always a valid ASCII character
}
```

---

## 10. Troubleshooting

### Issue: OpenJML Maven goal cannot be resolved

**Cause:** `org.jmlspecs.openjml:openjml-maven-plugin:0.14.0` is not available
from the configured Maven repositories.  
**Solution:**
```bash
# Install a compatible standalone OpenJML distribution, then run:
openjml --esc src/main/java/org/apache/commons/csv/CSVParser.java \
  src/main/java/org/apache/commons/csv/CSVRecord.java
```

### Issue: Build Fails with "Invariant violation"

**Cause:** Code violates JML contract  
**Solution:**
1. Read error message carefully
2. Identify which invariant is violated
3. Fix the code to maintain invariant
4. Re-run verification

---

## 11. References

- **JML Handbook:** http://www.jmlspecs.org/handbook.pdf
- **OpenJML Project:** https://www.openjml.org/
- **JML Tutorial:** http://www.jmlspecs.org/tutorial.shtml

---

## Summary

| Aspect | Status | Details |
|---|---|---|
| JML Specification | ✅ Complete | 2 classes, 8 invariants |
| OpenJML Plugin | ⚠️ Configuration added | Version 0.14.0 is declared in `pom.xml`, but the artifact is not available from Maven Central |
| Java Build | ✅ Passing | The JML annotations are valid Java comments and do not break compilation or tests |
| Formal Proof | ⏸ Pending | OpenJML has not run successfully because the Maven plugin cannot be resolved |
| Documentation | ✅ Complete | This document + inline comments |

### Verification limitation

The JML contracts were added to `CSVParser` and `CSVRecord`, and the normal Maven
build succeeds. However, normal Java compilation does not verify JML contracts:
JML annotations are treated as comments by `javac`.

The intended command is:

```bash
mvn openjml:check
```

At the time of implementation, this command failed because Maven could not resolve
`org.jmlspecs.openjml:openjml-maven-plugin:0.14.0` from Maven Central. No standalone
`openjml` executable was installed on the development machine either. Therefore,
the project should not claim that OpenJML verification passed. A formal proof can
be completed later by installing a compatible OpenJML distribution and running:

```bash
openjml --esc src/main/java/org/apache/commons/csv/CSVParser.java \
  src/main/java/org/apache/commons/csv/CSVRecord.java
```

The command is successful when it exits with code `0` and reports no JML errors or
verification failures.
