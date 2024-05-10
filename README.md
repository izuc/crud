# React-Java-MySQL CRUD Generator

The React-Java-MySQL CRUD Generator is a Java-based desktop application that automates the generation of CRUD (Create, Read, Update, Delete) functionality for a React frontend and Java backend using a MySQL database. It simplifies the process of creating APIs and database operations by automatically generating the necessary code based on the selected database tables.

## Features

- Connects to a MySQL database and retrieves a list of tables
- Allows selecting specific tables for CRUD generation
- Generates React components for each selected table
- Creates Java classes for models, resources, and storage
- Generates MySQL queries for CRUD operations
- Provides a user-friendly GUI for easy configuration and generation

## Prerequisites

Before running the CRUD Generator, ensure that you have the following:

- Java Development Kit (JDK) installed
- MySQL database with the required tables

## Getting Started

1. Clone the repository or download the source code files.

2. Open the project in your preferred Java IDE.

3. Update the database connection details in the `App` class:
   - Set the values for `txtHost`, `txtPort`, `txtUsername`, `txtPassword`, and `txtDatabase` to match your MySQL database configuration.

4. Build and run the `App` class.

5. In the CRUD Generator GUI:
   - Enter the package name for the generated code in the "Package Name" field.
   - Click the "Connect" button to establish a connection to the MySQL database.
   - Select the desired tables from the list of available tables.
   - Click the "Generate API" button to generate the CRUD code.

6. The generated code will be saved in the specified package directory.

## Generated Code Structure

The CRUD Generator creates the following code structure:

- React Components:
  - `[TableName]FormView.js`: Form component for creating and editing records.
  - `[TableName]Page.js`: Main page component for displaying and managing records.

- Java Classes:
  - `[TableName].java`: Model class representing the table structure.
  - `[TableName]Resource.java`: Resource class for handling API endpoints.

- Redux Store:
  - `[tableName].js`: Redux store configuration for managing table data.

- MySQL Queries:
  - Select, insert, update, and delete queries for each table.

## Dependencies

The CRUD Generator utilizes the following dependencies:

- Java Swing: For creating the GUI components.
- MySQL Connector/J: For connecting to the MySQL database.
- Regex: For processing and manipulating table and column names.

## License

This project is licensed under the [MIT License](LICENSE).

## Acknowledgements

The React-Java-MySQL CRUD Generator was developed by [Lance](https://www.lance.name).
