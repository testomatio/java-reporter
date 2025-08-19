Reporting Parameterized Tests
Parameterized tests (also known as data-driven tests) run the same test logic with different input data. Testomat.io provides a way to report these tests clearly, showing both the test structure and the specific data used for each test run.

Using the example Parameter
When reporting parameterized tests, use the example parameter to identify the specific data set used in each test execution:

POST https://app.testomat.io/api/reporter/run-id-12345/testrun?api_key=tstmt_your_api_key
Content-Type: application/json

{
"title": "User login with different roles",
"status": "passed",
"test_id": "T1",
"rid": "login-admin-role",
"example": {
"username": "admin",
"role": "administrator",
"expectedPermissions": ["read", "write", "delete"]
}
}

In this example, the example parameter contains the specific data used for this test run.

Reporting Multiple Parameterized Test Runs
For tests that run with multiple data sets, report each execution with the same test ID but different RIDs and examples:

POST https://app.testomat.io/api/reporter/run-id-12345/testrun?api_key=tstmt_your_api_key
Content-Type: application/json

{
"tests": [
{
"title": "User login with different roles",
"status": "passed",
"test_id": "T1",
"rid": "login-admin-role",
"example": {
"username": "admin",
"role": "administrator",
"expectedPermissions": ["read", "write", "delete"]
}
},
{
"title": "User login with different roles",
"status": "passed",
"test_id": "T1",
"rid": "login-editor-role",
"example": {
"username": "editor",
"role": "content_editor",
"expectedPermissions": ["read", "write"]
}
},
{
"title": "User login with different roles",
"status": "failed",
"test_id": "T1",
"rid": "login-viewer-role",
"example": {
"username": "viewer",
"role": "readonly",
"expectedPermissions": ["read"]
},
"message": "User was granted write permission when they should only have read"
}
],
}

Example Structure
The example parameter can contain any JSON structure that represents your test data:

Simple parameters: {"username": "admin", "password": "secret"}
Complex objects: {"user": {"id": 1, "role": "admin"}, "settings": {"theme": "dark"}}
A string can also be passed as example, in this case it will be reported as:

{ "example": "your string" }

Complete Example: Table-Driven Test
Here’s a complete example of reporting a table-driven test that verifies email validation with different inputs:

POST https://app.testomat.io/api/reporter/run-id-12345/testrun?api_key=tstmt_your_api_key
Content-Type: application/json

{
"tests": [
{
"title": "Email validation",
"status": "passed",
"test_id": "@T01010101",
"example": {
"email": "user@example.com",
"expectedValid": true
}
},
{
"title": "Email validation",
"status": "passed",
"test_id": "@T01010101",
"example": {
"email": "user@subdomain.example.com",
"expectedValid": true
}
},
{
"title": "Email validation",
"status": "passed",
"test_id": "@T01010101",
"example": {
"email": "userexample.com",
"expectedValid": false
}
},
{
"title": "Email validation",
"status": "failed",
"test_id": "@T01010101",
"example": {
"email": "user+code@example.com",
"expectedValid": true
},
"message": "Valid email with + character was rejected",
"stack": "Error: Expected validation to pass but got false\n    at validateEmail (/tests/validation.js:45:7)"
}
],
"batch_index": 1
}

By properly structuring your parameterized test reports with examples, you’ll be able to quickly identify which specific data sets are causing test failures and understand the context of each test execution.

