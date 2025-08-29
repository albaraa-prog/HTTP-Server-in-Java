# HTTP Server in Java

A simple, multi-threaded HTTP server built from scratch in Java.

## 🚀 Features

- **Multi-threaded**: Handles multiple concurrent connections
- **Configurable Port**: Default port 8080, customizable via command line
- **Clean Architecture**: Well-structured, modular design
- **Pure Java**: No external dependencies, built with standard Java libraries

## 📁 Project Structure

```
src/main/java/HTTP/
├── Main.java              # Entry point
├── Server.java            # Main server class
├── HttpRequest.java       # HTTP request model
├── HttpResponse.java      # HTTP response model
├── HttpMethod.java        # HTTP methods enum
├── HttpStatusCode.java    # HTTP status codes
├── RequestRunner.java     # Request handler interface
├── Routes.java            # Route management
├── HttpDecoder.java       # HTTP request parsing
├── HttpHandler.java       # Request handling logic
├── handleConnection.java  # Connection management
├── handleRequest.java     # Request processing
├── buildHeaderStrings.java # Header formatting utilities
├── getResponseString.java  # Response string utilities
└── writeResponse.java     # Response writing utilities
```

## 🛠️ Requirements

- Java 17 or higher
- Maven 3.6+

## 🚀 Getting Started

### 1. Clone the repository

```bash
git clone <your-repo-url>
cd HTTP-Server-Java
```

### 2. Compile the project

```bash
mvn compile
```

### 3. Run the server

```bash
mvn exec:java -Dexec.mainClass="HTTP.Main"
```

Or with a custom port:

```bash
mvn exec:java -Dexec.mainClass="HTTP.Main" -Dexec.args="9000"
```

## 🌐 Usage

Once running, the server will:

- Start listening on the specified port (default: 8080)
- Accept incoming HTTP connections
- Handle each connection in a separate thread
- Log connection information to console

## 📝 Current Status

**Phase 1: Basic Server Foundation** ✅

- [x] Basic server structure
- [x] Multi-threaded connection handling
- [x] Server startup and shutdown
- [x] Basic connection logging

**Phase 2: HTTP Protocol Implementation** 🚧

- [x] HTTP request parsing
- [x] HTTP response generation
- [x] Route handling
- [x] Basic HTTP methods (GET, POST)

**Phase 3: Advanced Features** 📋

- [ ] Static file serving
- [ ] Error handling
- [ ] Logging and monitoring
- [ ] Configuration management

## 🔧 Development

### Building

```bash
mvn clean compile
```

### Running Tests

```bash
mvn test
```

### Package

```bash
mvn package
```

## 📚 Learning Goals

This project demonstrates:

- **Socket Programming**: Raw TCP socket handling
- **Multi-threading**: Concurrent connection management
- **HTTP Protocol**: Understanding HTTP request/response cycle
- **Java NIO**: Efficient I/O operations
- **Design Patterns**: Clean architecture and separation of concerns

## 🤝 Contributing

This is a learning project. Feel free to:

- Report issues
- Suggest improvements
- Submit pull requests
- Ask questions

## 📄 License

This project is open source and available under the [MIT License](LICENSE).

## 🎯 Next Steps

1. **Implement HTTP request parsing**
2. **Add route handling**
3. **Create proper HTTP responses**
4. **Add static file serving**
5. **Implement error handling**

---

_Built with ❤️ for learning HTTP server development_
