package Library;

import java.sql.*;
import java.util.Scanner;

public class LB {
    private static Connection connection;

    public static void main(String[] args) {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/Librarymanagement", "root", "Sathvika@123#");

            Scanner scanner = new Scanner(System.in);
            System.out.println("Welcome to the Library Management System");
            
            System.out.print("Enter User ID: ");
            String userId = scanner.nextLine();
            System.out.print("Enter Password: ");
            String password = scanner.nextLine();

            if (authenticateUser(userId, password)) {
                System.out.println("Login successful!");
                
                while (true) {
                    System.out.println("\nMain Menu:");
                    System.out.println("1. Borrow a Book");
                    System.out.println("2. Return a Book");
                    System.out.println("3. View All Books");
                    System.out.println("4. Exit");
                    System.out.print("Enter your choice: ");
                    
                    int choice;
                    try {
                        choice = Integer.parseInt(scanner.nextLine());
                    } catch (NumberFormatException e) {
                        System.out.println("Please enter a valid number!");
                        continue;
                    }

                    switch (choice) {
                        case 1:
                            borrowBook(scanner,userId);
                            break;
                        case 2:
                            returnBook(scanner);
                            break;
                        case 3:
                            viewAllBooks();
                            break;
                        case 4:
                            System.out.println("Thank you for using the Library Management System!");
                            connection.close();
                            System.exit(0);
                        default:
                            System.out.println("Invalid choice. Please try again.");
                    }
                }
            } else {
                System.out.println("Invalid login credentials.");
            }
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
    }

    private static boolean authenticateUser(String userId, String password) throws SQLException {
        String query = "SELECT * FROM employee WHERE user_id = ? AND password = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, userId);
            preparedStatement.setString(2, password);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private static void borrowBook(Scanner scanner,String userId) {
        try {
            System.out.println("\nSearch for a book to borrow:");
            System.out.println("1. Search by Name");
            System.out.println("2. Search by Author");
            System.out.println("3. Search by Genre");
            System.out.println("4. Search by Book ID");
            System.out.print("Enter your search option: ");
            
            int searchOption;
            try {
                searchOption = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number!");
                return;
            }

            String searchQuery = "";
            String searchTerm = "";
            String columnName = "";

            switch (searchOption) {
                case 1:
                    columnName = "name";
                    System.out.print("Enter book name: ");
                    break;
                case 2:
                    columnName = "author";
                    System.out.print("Enter author name: ");
                    break;
                case 3:
                    columnName = "Genre";
                    System.out.print("Enter book genre: ");
                    break;
                case 4:
                    columnName = "book_id";
                    System.out.print("Enter book ID: ");
                    break;
                default:
                    System.out.println("Invalid search option.");
                    return;
            }

            searchTerm = scanner.nextLine();
            if (searchOption == 4) {
                searchQuery = "SELECT * FROM books WHERE book_id = ?";
            } else {
                searchQuery = "SELECT * FROM books WHERE " + columnName + " LIKE ?";
            }

            try (PreparedStatement searchStmt = connection.prepareStatement(searchQuery)) {
                if (searchOption == 4) {
                    searchStmt.setInt(1, Integer.parseInt(searchTerm));
                } else {
                    searchStmt.setString(1, "%" + searchTerm + "%");
                }

                try (ResultSet resultSet = searchStmt.executeQuery()) {
                    System.out.println("\nSearch Results:");
                    System.out.println("-----------------------------------------------------------------");
                    System.out.printf("%-10s %-30s %-20s %-15s %-10s %-10s\n", 
                                     "Book ID", "Name", "Author", "Genre", "Available", "Borrowed");
                    System.out.println("-----------------------------------------------------------------");
                    
                    boolean found = false;
                    while (resultSet.next()) {
                        found = true;
                        System.out.printf("%-10d %-30s %-20s %-15s %-10d %-10d\n",
                                          resultSet.getInt("book_id"),
                                          resultSet.getString("name"),
                                          resultSet.getString("author"),
                                          resultSet.getString("Genre"),
                                          resultSet.getInt("available"),
                                          resultSet.getInt("borrowed"));
                    }
                    System.out.println("-----------------------------------------------------------------");
                    
                    if (!found) {
                        System.out.println("No books found matching your search.");
                        return;
                    }
                }
            }

            System.out.print("\nEnter the Book ID to borrow: ");
            int bookId;
            try {
                bookId = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid Book ID!");
                return;
            }

            String queryCheckStock = "SELECT available FROM books WHERE book_id = ?";
            try (PreparedStatement checkStockStmt = connection.prepareStatement(queryCheckStock)) {
                checkStockStmt.setInt(1, bookId);
                try (ResultSet stockResult = checkStockStmt.executeQuery()) {
                    if (stockResult.next() && stockResult.getInt("available") > 0) {
                        String queryUpdateStock = "UPDATE books SET available = available - 1, borrowed = borrowed + 1 WHERE book_id = ?";
                        try (PreparedStatement updateStockStmt = connection.prepareStatement(queryUpdateStock)) {
                            updateStockStmt.setInt(1, bookId);
                            updateStockStmt.executeUpdate();
                        }

                        String queryAddTransaction = "INSERT INTO transactions (book_id, user_id, action, transaction_date) VALUES (?, ?, 'Borrow', NOW())";
                        try (PreparedStatement addTransactionStmt = connection.prepareStatement(queryAddTransaction)) {
                            addTransactionStmt.setInt(1, bookId);
                            addTransactionStmt.setString(2, userId); // ✅ Use the logged-in user's ID
                            addTransactionStmt.executeUpdate();
                        }

                        System.out.println("Book borrowed successfully!");
                    } else {
                        System.out.println("Book not available for borrowing.");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error during borrowing: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number!");
        }
    }

    private static void returnBook(Scanner scanner) {
        try {
            System.out.print("\nEnter Book ID to return: ");
            int bookId;
            try {
                bookId = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid Book ID!");
                return;
            }

            String queryCheckBook = "SELECT borrowed FROM books WHERE book_id = ?";
            try (PreparedStatement checkBookStmt = connection.prepareStatement(queryCheckBook)) {
                checkBookStmt.setInt(1, bookId);
                try (ResultSet bookResult = checkBookStmt.executeQuery()) {
                    if (bookResult.next() && bookResult.getInt("borrowed") > 0) {
                        String queryUpdateStock = "UPDATE books SET available = available + 1, borrowed = borrowed - 1 WHERE book_id = ?";
                        try (PreparedStatement updateStockStmt = connection.prepareStatement(queryUpdateStock)) {
                            updateStockStmt.setInt(1, bookId);
                            updateStockStmt.executeUpdate();
                        }

                        String queryAddTransaction = "INSERT INTO transactions (book_id, user_id, action, transaction_date) VALUES (?, ?, 'Return', NOW())";
                        try (PreparedStatement addTransactionStmt = connection.prepareStatement(queryAddTransaction)) {
                            addTransactionStmt.setInt(1, bookId);
                            addTransactionStmt.setString(2, "current_user"); // Replace with actual user
                            addTransactionStmt.executeUpdate();
                        }

                        System.out.println("Book returned successfully!");
                    } else {
                        System.out.println("This book is not currently borrowed or doesn't exist.");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error during return: " + e.getMessage());
        }
    }

    private static void viewAllBooks() {
        try {
            String query = "SELECT * FROM books";
            try (Statement stmt = connection.createStatement();
                 ResultSet resultSet = stmt.executeQuery(query)) {
                
                System.out.println("\nAll Books in Library:");
                System.out.println("-----------------------------------------------------------------");
                System.out.printf("%-10s %-30s %-20s %-15s %-10s %-10s\n", 
                                 "Book ID", "Name", "Author", "Genre", "Available", "Borrowed");
                System.out.println("-----------------------------------------------------------------");
                
                while (resultSet.next()) {
                    System.out.printf("%-10d %-30s %-20s %-15s %-10d %-10d\n",
                                    resultSet.getInt("book_id"),
                                    resultSet.getString("name"),
                                    resultSet.getString("author"),
                                    resultSet.getString("Genre"),
                                    resultSet.getInt("available"),
                                    resultSet.getInt("borrowed"));
                }
                System.out.println("-----------------------------------------------------------------");
            }
        } catch (SQLException e) {
            System.err.println("Database error while fetching books: " + e.getMessage());
        }
    }
}