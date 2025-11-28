# Peer File Sharer (Core Java) [Backend only]

A simple peer-to-peer file sharing application built using only core Java, without any external libraries. This project demonstrates basic HTTP file upload and download functionality, using Java's built-in networking and threading capabilities.

**Note:** This project uses Java's built-in `HttpServer`, `HttpExchange`, and `HttpHandler` classes to handle HTTP routes and requests, providing a minimal REST API without any frameworks.

## Features

-   **File Upload & Download:** Supports uploading and downloading files via HTTP requests.
-   **Multithreaded Server:** Handles each client request in a separate thread for concurrent file transfers.

```
src/
  main/
    java/
        App.java                # Main server entry point
        controllers/
          FileController.java   # Manages HTTP endpoints for file API routes and CORS
          DownloadHandler.java  # Handles file download requests
          UploadHandler.java    # Handles file upload requests (manual parsing)
        services/
          FileParser.java       # Creates new threads for every request (using socket connection)
        utils/
          FileUtils.java        # Helper for generating port number
```

## Wanna try it?

### Prerequisites

-   Java 8 or higher
-   Maven (for building the project)v

### Build & Run

1. **Clone the repository:**
    ```
    git clone https://github.com/somesh4747/peer-file-sharer.git
    cd peer-file-sharer
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

-   **Upload a file:**
    -   Send a POST request to `/upload` with the file as form data.
-   **Download a file:**
    -   Send a GET request to `/download?port=<port-number>`

You can use tools like `curl` or Postman to test the endpoints.
