# Peer File Sharer (Core Java)

A simple peer-to-peer file sharing application built using only core Java, without any external libraries. This project demonstrates basic HTTP file upload and download functionality, using Java's built-in networking and threading capabilities.

## Features

- **File Upload & Download:** Supports uploading and downloading files via HTTP requests.
- **Multithreaded Server:** Handles each client request in a separate thread for concurrent file transfers.
- **Minimal Dependencies:** Built with only core Java classes (no third-party libraries).
- **CORS:** Proper CORS Handling using response body of every request.

## Project Structure

```
src/
  main/
    java/
      main/
        App.java                # Entry point, starts the server
        controllers/
          FileController.java   # Handles HTTP requests for file operations
        services/
          FileParser.java       # Parses file upload/download requests
        utils/
          UploadUtils.java      # Utility methods for file handling
```

## Getting Started

### Prerequisites
- Java 8 or higher
- Maven (for building the project)

### Build & Run

1. **Clone the repository:**
   ```
   git clone <repo-url>
   cd peer_file_sharer
   ```
2. **Build the project:**
   ```
   mvn clean package
   ```
3. **Run the server:**
   ```
   java -cp target/classes main.App
   ```

### Usage

- **Upload a file:**
  - Send a POST request to `/upload` with the file as form data.
- **Download a file:**
  - Send a GET request to `/download?filename=<your_file>`

You can use tools like `curl` or Postman to test the endpoints.

## License
This project is licensed under the MIT License.