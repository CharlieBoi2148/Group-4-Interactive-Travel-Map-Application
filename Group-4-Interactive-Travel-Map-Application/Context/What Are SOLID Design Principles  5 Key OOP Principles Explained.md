# What are SOLID design principles?

SOLID is a set of **five object-oriented design principles** that focus on creating elegant, robust, and maintainable object-oriented code. Think of them as the pillars of good software architecture. Let's break each one down:

## S: Single Responsibility Principle (SRP)

**Concept**: A class should have only one reason to change, meaning it should have only one job or responsibility.

**Real-World Analogy**: Like a chef in a restaurant, who is responsible only for cooking and not for taking orders or serving food.

**In Practice**: If you have a class called `OrderProcessor`, it should only process orders, not handle database storage or error logging.


SOLID Design Principles
 
**Example:** To follow the Single Responsibility Principle, we should split the `OrderProcessor` class into smaller classes:

Python3

. . . .

By splitting the `OrderProcessor` class into `OrderProcessor`, `DatabaseManager`, and `ErrorLogger`, each class now has a single responsibility. This separation makes the code easier to manage, test, and extend. For instance, if the way errors are logged changes, you only need to modify the ErrorLogger class without affecting the other parts of the system.

## O: Open/Closed Principle

**Concept**: Software entities (classes, modules, functions, etc.) should be open for extension, but closed for modification.

**Real-World Analogy**: Think of a graphic design software. It allows you to add new functionalities (like new shapes) without changing the core drawing engine.

**In Practice**: Implementing new functionalities via interfaces or abstract classes, allowing new functionalities to be added without changing existing code.

**Example:** To follow the Open/Closed Principle, we can use abstract classes and inheritance to add new payment types without changing existing code:

Python3

. . . .

By using an abstract class `Payment` and creating concrete classes `CreditCardPayment` and `PayPalPayment`, we ensure that the `PaymentProcessor` class does not need to change when new payment types are introduced. This approach allows the system to be easily extended with new payment methods without modifying existing code, thus reducing the risk of introducing bugs.

## L: Liskov Substitution Principle

**Concept**: Objects of a superclass should be replaceable with objects of subclasses without affecting the correctness of the program.

**Real-World Analogy**: If you're driving a vehicle, you should be able to replace it with a different type of vehicle (like switching a car for a truck) and still be able to drive without issues.

**In Practice**: Ensure that subclasses don't alter expected behavior. For example, if you have a class Bird with a method `move()`, each subclass should implement the `move()` method to reflect its specific type of movement (e.g., Sparrow should fly, Penguin should swim).

**Example:** To follow the [Liskov Substitution Principle](https://www.designgurus.io/answers/detail/what-is-an-example-of-the-liskov-substitution-principle), we should ensure that subclasses override methods to reflect their behavior correctly:

Python3

. . . .

By designing the `Bird` class with a move method instead of fly, we allow subclasses to provide their specific implementation of movement. This ensures that substituting `Sparrow` with `Penguin` does not break the expected behavior of the program. Each subclass can provide its behavior that aligns with the Bird interface, maintaining the program's correctness.

## I: Interface Segregation Principle

**Concept**: Clients should not be forced to depend on interfaces they do not use.

**Real-World Analogy**: It’s like having a universal remote control with so many buttons, most of which you never use. Instead, you would prefer a simpler remote with only the buttons you need.

**In Practice**: Instead of creating a large interface with many methods, create smaller, more specific interfaces for each type of functionality (e.g., `Switchable` for turning on/off, `Timer` for setting a timer). This ensures that classes only implement the methods they actually need, making the code more modular and easier to maintain.

**Example:** To follow the [Interface Segregation Principle](https://www.designgurus.io/answers/detail/when-should-i-use-an-interface-and-when-should-i-use-a-base-class), we should create specific interfaces for each type of functionality:

Python3

. . . .

By creating specific interfaces like `Switchable` and `Timer`, we ensure that the `Fan` class only depends on the methods it needs. This approach makes the Fan class easier to understand and maintain. If we have other appliances that need a timer, they can implement the Timer interface without being forced to implement unrelated methods.

## D: Dependency Inversion Principle

**Concept**: High-level modules should not depend on low-level modules. Both should depend on abstractions. Also, abstractions should not depend on details; details should depend on abstractions.

**Real-World Analogy**: Like using a standard power socket in your home – it doesn’t need to know what type of device will be plugged in (the high-level policy). The devices (low-level modules) are designed to fit the socket.

**In Practice**: A class should receive its dependencies from the outside rather than creating them itself. This can be achieved using interfaces or abstract classes, making the code more flexible and easier to test.

**Example:** To follow the Dependency Inversion Principle, we should depend on abstractions rather than concrete classes:

Python3

. . . .

`SMTPService` is a concrete implementation of the `EmailService` interface. It uses the Simple Mail Transfer Protocol (SMTP) to send emails. SMTP is a protocol used to send emails over the Internet.

By relying on the EmailService abstraction, the EmailSender class gains flexibility. It can work with any implementation of the EmailService interface. For example, it can use `SMTPService` for real email sending or `MockEmailService` for testing.

This design makes the system easier to extend and test. You can switch from `SMTPService` to `MockEmailService` during testing without changing the EmailSender class.