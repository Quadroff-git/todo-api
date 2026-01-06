# todo-api
A simple web api to keep track of a todo list built to practice writing tests, using Hibernate ORM and manually configured components of Spring framework, 
as well as some other popular libraries 

## The stack
- Maven used as build tool
- Hibernate ORM + MySQL for persistence
- Certain Spring components:
  - DI and IoC container
  - Hibernate integration
  - Transaction management
  - Web MVC (WIP)
- JUnit tests
  - Pure unit tests with mock objects for certain components (WIP)
  - Integration tests using both Spring Test and manual DI
- Lombok to reduce boilerplate

## Building
```maven
mvn clean package
```

## Potential features to add
- Dynamic query building instead of rigid selection API currently implented in persistence and service layers