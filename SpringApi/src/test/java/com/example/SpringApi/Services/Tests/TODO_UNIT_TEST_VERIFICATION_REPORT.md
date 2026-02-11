# UNIT TEST VERIFICATION REPORT — Todo

```
╔════════════════════════════════════════════════════════════╗
║           UNIT TEST VERIFICATION REPORT                    ║
║                                                            ║
║  Status: ❌ RED                                               ║
║  Services Analyzed: 1                                      ║
║  Public Methods Found: 5                                  ║
║  Test Files Expected: 5                                  ║
║  Test Files Found: 5                                     ║
║  Total Violations: 11                                    ║
╚════════════════════════════════════════════════════════════╝
```

VIOLATIONS BY RULE:

| Rule | Description | Count |
| --- | --- | --- |
| 6 | Centralized Mocking | 5 |
| 10 | Test Ordering | 1 |
| 14 | No Inline Mocks | 5 |


**FILE-BY-FILE BREAKDOWN**

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Todo/UpdateTodoTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.Todo
Class: * Test class for TodoService - UpdateTodo operation.
Extends: None
Lines of Code: 474
Last Modified: 2026-02-10 21:17:12
Declared Test Count: 19 (first occurrence line 31)
Actual @Test Count: 19

VIOLATIONS FOUND:

VIOLATION 1: Rule 6 - Centralized Mocking
- Severity: HIGH
- Line: 443 has mock usage `ITodoSubTranslator todoServiceMock = mock(ITodoSubTranslator.class);`
- Required: Move mocks to base test file.

VIOLATION 2: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 445 inline mock in `updateTodo_controller_permission_unauthorized`: `doThrow(new com.example.SpringApi.Exceptions.UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED))`
- Required: Move to base test stub method and call stub in test.
- Line: 446 inline mock in `updateTodo_controller_permission_unauthorized`: `.when(todoServiceMock).updateTodo(any(TodoRequestModel.class));`
- Required: Move to base test stub method and call stub in test.

REQUIRED FIXES SUMMARY:
- Fix Rule 6 issues above.
- Fix Rule 14 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Todo/AddTodoTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.Todo
Class: * Test class for TodoService.addTodo method.
Extends: None
Lines of Code: 496
Last Modified: 2026-02-10 20:20:42
Declared Test Count: 20 (first occurrence line 29)
Actual @Test Count: 20

VIOLATIONS FOUND:

VIOLATION 1: Rule 6 - Centralized Mocking
- Severity: HIGH
- Line: 465 has mock usage `ITodoSubTranslator todoServiceMock = mock(ITodoSubTranslator.class);`
- Required: Move mocks to base test file.

VIOLATION 2: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 467 inline mock in `addTodo_controller_permission_unauthorized`: `doThrow(new com.example.SpringApi.Exceptions.UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED))`
- Required: Move to base test stub method and call stub in test.
- Line: 468 inline mock in `addTodo_controller_permission_unauthorized`: `.when(todoServiceMock).addTodo(any(TodoRequestModel.class));`
- Required: Move to base test stub method and call stub in test.

REQUIRED FIXES SUMMARY:
- Fix Rule 6 issues above.
- Fix Rule 14 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Todo/GetTodoItemsTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.Todo
Class: * Test class for TodoService.getTodoItems method.
Extends: None
Lines of Code: 330
Last Modified: 2026-02-10 19:36:25
Declared Test Count: 13 (first occurrence line 28)
Actual @Test Count: 13

VIOLATIONS FOUND:

VIOLATION 1: Rule 6 - Centralized Mocking
- Severity: HIGH
- Line: 299 has mock usage `ITodoSubTranslator todoServiceMock = mock(ITodoSubTranslator.class);`
- Required: Move mocks to base test file.

VIOLATION 2: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 301 inline mock in `getTodoItems_controller_permission_unauthorized`: `doThrow(new com.example.SpringApi.Exceptions.UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED))`
- Required: Move to base test stub method and call stub in test.
- Line: 302 inline mock in `getTodoItems_controller_permission_unauthorized`: `.when(todoServiceMock).getTodoItems();`
- Required: Move to base test stub method and call stub in test.

VIOLATION 3: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section SUCCESS not alphabetical.
- Current order: getTodoItems_emptyList_success, getTodoItems_fieldsCorrectlyMapped_success, getTodoItems_manyTodos_success, getTodoItems_mixedIsDoneValues_success, getTodoItems_multipleTodos_success, getTodoItems_repositoryCalledOnce_success, getTodoItems_success_returnsTodos, getTodoItems_success_logsOperation, getTodoItems_variousSpecialCharacters_success, getTodoItems_verifyRepositoryCall_success
- Required order: getTodoItems_emptyList_success, getTodoItems_fieldsCorrectlyMapped_success, getTodoItems_manyTodos_success, getTodoItems_mixedIsDoneValues_success, getTodoItems_multipleTodos_success, getTodoItems_repositoryCalledOnce_success, getTodoItems_success_logsOperation, getTodoItems_success_returnsTodos, getTodoItems_variousSpecialCharacters_success, getTodoItems_verifyRepositoryCall_success

REQUIRED FIXES SUMMARY:
- Fix Rule 6 issues above.
- Fix Rule 14 issues above.
- Fix Rule 10 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Todo/DeleteTodoTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.Todo
Class: * Test class for TodoService - DeleteTodo operation.
Extends: None
Lines of Code: 287
Last Modified: 2026-02-10 20:20:42
Declared Test Count: 12 (first occurrence line 27)
Actual @Test Count: 12

VIOLATIONS FOUND:

VIOLATION 1: Rule 6 - Centralized Mocking
- Severity: HIGH
- Line: 256 has mock usage `ITodoSubTranslator todoServiceMock = mock(ITodoSubTranslator.class);`
- Required: Move mocks to base test file.

VIOLATION 2: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 258 inline mock in `deleteTodo_controller_permission_unauthorized`: `doThrow(new com.example.SpringApi.Exceptions.UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED))`
- Required: Move to base test stub method and call stub in test.
- Line: 259 inline mock in `deleteTodo_controller_permission_unauthorized`: `.when(todoServiceMock).deleteTodo(anyLong());`
- Required: Move to base test stub method and call stub in test.

REQUIRED FIXES SUMMARY:
- Fix Rule 6 issues above.
- Fix Rule 14 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Todo/ToggleTodoTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.Todo
Class: * Test class for TodoService - ToggleTodo operation.
Extends: None
Lines of Code: 344
Last Modified: 2026-02-10 20:20:42
Declared Test Count: 15 (first occurrence line 28)
Actual @Test Count: 15

VIOLATIONS FOUND:

VIOLATION 1: Rule 6 - Centralized Mocking
- Severity: HIGH
- Line: 313 has mock usage `ITodoSubTranslator todoServiceMock = mock(ITodoSubTranslator.class);`
- Required: Move mocks to base test file.

VIOLATION 2: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 315 inline mock in `toggleTodo_controller_permission_unauthorized`: `doThrow(new com.example.SpringApi.Exceptions.UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED))`
- Required: Move to base test stub method and call stub in test.
- Line: 316 inline mock in `toggleTodo_controller_permission_unauthorized`: `.when(todoServiceMock).toggleTodo(anyLong());`
- Required: Move to base test stub method and call stub in test.

REQUIRED FIXES SUMMARY:
- Fix Rule 6 issues above.
- Fix Rule 14 issues above.


**IMPLEMENTATION PLAN (STEP-BY-STEP)**
1. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Todo/UpdateTodoTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
2. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Todo/AddTodoTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
3. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Todo/GetTodoItemsTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
4. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Todo/DeleteTodoTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
5. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Todo/ToggleTodoTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.

Verification Commands (run after fixes):
- mvn -Dtest=UpdateTodoTest test
- mvn -Dtest=AddTodoTest test
- mvn -Dtest=GetTodoItemsTest test
- mvn -Dtest=DeleteTodoTest test
- mvn -Dtest=ToggleTodoTest test