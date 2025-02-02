# mRegions

**mRegions** is a Minecraft plugin designed to help manage regions on your server. Built using the Spigot API and Maven, mRegions offers an extensible framework for creating, editing, and protecting regions. Its architecture leverages modern Java features and popular libraries such as HikariCP for database connection pooling, FastInv for intuitive GUI handling, and SQLite for lightweight data storage.

## Features

- **Region Management:** Easily create, modify, and delete regions.
- **Database Integration:** Uses SQLite via JDBC for storing region data.
- **Performance Optimizations:** HikariCP ensures efficient database connections.
- **User-Friendly GUIs:** FastInv simplifies the creation of custom inventory interfaces.
- **Modern Java:** Built with Java 9 compatibility in mind.
- **Maven-Based Build:** Simplify dependency management and build processes.

## Installation

1. **Build the Plugin:**
   - Ensure you have [Maven](https://maven.apache.org/) installed.
   - Clone the repository:
     ```bash
     git clone https://github.com/m0nxef/mRegions.git
     ```
   - Navigate to the project folder and build the JAR:
     ```bash
     cd mRegions
     mvn clean package
     ```
2. **Deploy to Your Server:**
   - Locate the generated JAR file (the `mRegions` artifact).
   - Copy the JAR file to your Minecraft server’s `plugins` directory.
   - Restart your server to load the plugin.

## Configuration

After the plugin is installed and your server is running, configuration files may be generated automatically in your server’s plugin folder. Edit these configuration files to customize settings such as region rules, database options, and GUI configurations.

## Dependencies

This project relies on the following libraries:
- **Spigot API (1.21.4-R0.1-SNAPSHOT):** Core API for Minecraft plugin development.
- **HikariCP (5.1.0):** For efficient database connection pooling.
- **FastInv (3.1.1):** Simplifies the creation of in-game inventory interfaces.
- **Lombok (1.18.30):** Reduces boilerplate code (provided at compile-time).
- **SQLite JDBC (3.44.1.0):** Lightweight database engine for persistent storage.

## Development

### Prerequisites

- Java 9 or above
- Maven

### Building from Source

To build the project from source, run:

```bash
mvn clean package
