# EMI Loan Tracker

A Spring Boot application to calculate, track and manage loan EMIs.

## Quick start

1. Database
   - Create a MySQL database named `loantracker` (or update `spring.datasource.url` in `application.properties`).
   - Example (MySQL):
     - CREATE DATABASE loantracker;

2. Environment variables (mail)
   - `MAIL_USERNAME` - SMTP username (e.g., Gmail address)
   - `MAIL_PASSWORD` - SMTP app password

3. Run
   - From project root (PowerShell):

```powershell
mvn -f "c:\Users\sanja\Desktop\loan-tracker\emi\pom.xml" spring-boot:run
```

Application will start on the port configured in `application.properties` (default 8081).

## API Endpoints

- POST /api/users - add new user
- GET /api/users - list users
- POST /api/loans - create loan (EMI schedule auto-generated)
- GET /api/loans/user/{id} - list loans for user
- GET /api/loans/{id}/calculate-emi - calculate EMI for existing loan
- GET /api/loans/{id}/summary - loan summary
- POST /api/emipayments - record EMI payment
- GET /api/emipayments/loan/{id} - EMI payments for loan
- POST /api/emipayments/pay/{paymentId} - mark EMI payment
- GET /api/reports/loans/user/{id} - loan summary for user
- GET /api/reports/user/{id} - detailed user loan report
- POST /api/reminders/send - trigger email reminders manually

## Notes

- Email sending relies on Spring Mail. For Gmail you must use an app password and enable SMTP access.
- The project uses `spring.jpa.hibernate.ddl-auto=update` to create/update tables automatically for development.

- Email sending toggle: there's an `app.mail.enabled` property in `application.properties`. It defaults to
   `false` for local development. Set it to `true` in production and provide valid SMTP credentials.

## Next steps / enhancements

- Add Spring Security for authentication and role-based access.
- Add more unit/integration tests around `LoanService` (EMI calc and payments).
- Improve UI with Thymeleaf forms to create loans/users from the browser.
