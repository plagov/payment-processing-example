# Payment Processing Sample Project

This is a sample payment processing project I made as a homework assignment during the interview for 
a backend developer role for one of the banks in Baltic states. 
It exposes three endpoints:
* create a payment
* cancel a payment
* query payments by status and amount

The project is built using Java 21 and Spring Boot 3 with a Postgres database.

## Run the service with docker compose
To run the service locally, run the following docker compose command from the root of the project directory:

```shell
docker compose up --build
```

The web service will be available on port `8083`. 
Navigate to `http://localhost:8083/swagger-ui.html` address to open the Swagger documentation.

## Run the service within IDE (preferably IntelliJ IDEA)
Locate the `TestPaymentProcessingApplication` class under the `src/test/java/io/plagov/payment_processing/` path 
and run it using the IDE runner. It will start the application itself along with a Postgres database inside the docker 
container. The service will start on the `8080` port (NB! make sure the port is available).

## Test the application
### Create a payment
To create a payment, send the payload of the following format to the POST endpoint: `/api/v1/payments`
```json
{
  "amount": "EUR 150.25",
  "debtorIban": "EE382200221020145685",
  "creditorIban": "LT121000011101001000",
  "details": "test details"
}
```
or using curl:
```shell
curl -X POST http://localhost:8083/api/v1/payments \
  -H "Content-Type: application/json" \
  -d '{
    "amount": "EUR 150.25",
    "debtorIban": "EE382200221020145685",
    "creditorIban": "LT121000011101001000",
    "details": "test details"
  }'
```
### Cancel payment
To cancel a payment, call the POST endpoint `/api/v1/payments/{paymentId}/cancel` (use an ID of the existing payment):
```shell
curl -X POST http://localhost:8083/api/v1/payments/3fa85f64-5717-4562-b3fc-2c963f66afa6/cancel
```

### Query payments
Payments can be retrieved by the mandatory `status` query parameter and the following optional amount query params:
`isEqualTo`, `isGreaterThan` and `isLessThan`. Available options for the status are: `ACCEPTED` and `CANCELLED`.
The following example will search for payments with `ACCEPTED` status and amount more than `125.00`:
```shell
curl -X GET "http://localhost:8083/api/v1/payments?status=ACCEPTED&isGreaterThan=125.00"
```
The endpoint will return an empty list if no payments are found.

## Consumer country resolution
The application also tries to resolve the country of the API consumer and logs it.
If the country resolution failed to succeed, an error will be logged, but it won't affect the running of the
payment processing application.

## Notification service
The payment processing application sends an event notification to a fake notification service when a payment is created
or when a payment is canceled.
