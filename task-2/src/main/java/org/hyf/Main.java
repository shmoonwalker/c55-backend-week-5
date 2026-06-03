package org.hyf;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private static final String URL = "jdbc:postgresql://localhost:5432/library_db";
    private static final String USER = "hyfuser";
    private static final String PASSWORD = "hyfpassword";

    public static void main(String[] args) {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            insertData(connection);

            List<String> records = fetchData(connection);

            for (String record : records) {
                System.out.println(record);
            }

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    public static void insertData(Connection connection) throws SQLException {
        int publisherId = insertPublisher(connection, "O'Reilly");

        int javascriptBookId = insertBook(
                connection,
                "JavaScript: The Good Parts",
                "9780596517748",
                publisherId
        );

        int designPatternsBookId = insertBook(
                connection,
                "Head First Design Patterns",
                "9780596007126",
                publisherId
        );

        int douglasId = insertAuthor(connection, "Douglas Crockford");
        int ericId = insertAuthor(connection, "Eric Freeman");
        int elisabethId = insertAuthor(connection, "Elisabeth Robson");

        insertBookAuthor(connection, javascriptBookId, douglasId);
        insertBookAuthor(connection, designPatternsBookId, ericId);
        insertBookAuthor(connection, designPatternsBookId, elisabethId);

        int javascriptTagId = insertTag(connection, "javascript");
        int beginnersTagId = insertTag(connection, "beginners");
        int designPatternsTagId = insertTag(connection, "design-patterns");
        int javaTagId = insertTag(connection, "java");

        insertBookTag(connection, javascriptBookId, javascriptTagId);
        insertBookTag(connection, javascriptBookId, beginnersTagId);
        insertBookTag(connection, designPatternsBookId, designPatternsTagId);
        insertBookTag(connection, designPatternsBookId, javaTagId);

        int omarId = insertMember(
                connection,
                "Omar",
                "omar@example.com",
                "+31689012345",
                "Rijnkade 19, Utrecht"
        );

        int tomId = insertMember(
                connection,
                "Tom",
                "tom@example.com",
                "+31645678901",
                "Biltstraat 88, Utrecht"
        );

        int samId = insertMember(
                connection,
                "Sam",
                "sam@example.com",
                "+31612345678",
                "Oudegracht 10, Utrecht"
        );

        insertCard(connection, omarId, "C-1008", "2024-01-30", "2027-01-30");
        insertCard(connection, tomId, "C-1004", "2023-11-20", "2026-11-20");
        insertCard(connection, samId, "C-1001", "2024-01-10", "2027-01-10");

        int copy1Id = insertBookCopy(connection, javascriptBookId, "BC-0011", "B-03");
        int copy2Id = insertBookCopy(connection, javascriptBookId, "BC-0013", "B-03");
        int copy3Id = insertBookCopy(connection, designPatternsBookId, "BC-0024", "A-02");

        insertBorrowing(
                connection,
                omarId,
                copy1Id,
                "2025-12-02 08:08:00",
                "2025-12-23 08:08:00",
                "2025-12-13 08:08:00",
                "0.00"
        );

        insertBorrowing(
                connection,
                tomId,
                copy2Id,
                "2026-01-10 08:51:00",
                "2026-01-31 08:51:00",
                "2026-01-25 08:51:00",
                "0.00"
        );

        insertBorrowing(
                connection,
                samId,
                copy3Id,
                "2025-12-21 23:02:00",
                "2026-01-11 23:02:00",
                "2026-01-14 23:02:00",
                "0.75"
        );
    }

    public static List<String> fetchData(Connection connection) throws SQLException {
        String sql = """
                SELECT
                    m.name AS member_name,
                    m.email,
                    c.number AS card_number,
                    b.title AS book_title,
                    b.isbn,
                    p.name AS publisher_name,
                    bc.barcode,
                    bc.shelf_code,
                    br.borrowed_at,
                    br.due_date,
                    br.returned_at,
                    br.fine_eur
                FROM borrowing br
                JOIN member m ON br.member_id = m.id
                JOIN card c ON c.member_id = m.id
                JOIN book_copy bc ON br.book_copy_id = bc.id
                JOIN book b ON bc.book_id = b.id
                JOIN publisher p ON b.publisher_id = p.id
                ORDER BY br.id
                """;

        List<String> records = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String record = resultSet.getString("member_name")
                        + " borrowed "
                        + resultSet.getString("book_title")
                        + " | card: "
                        + resultSet.getString("card_number")
                        + " | copy: "
                        + resultSet.getString("barcode")
                        + " | shelf: "
                        + resultSet.getString("shelf_code")
                        + " | borrowed at: "
                        + resultSet.getTimestamp("borrowed_at")
                        + " | due: "
                        + resultSet.getTimestamp("due_date")
                        + " | returned: "
                        + resultSet.getTimestamp("returned_at")
                        + " | fine: €"
                        + resultSet.getBigDecimal("fine_eur");

                records.add(record);
            }
        }

        return records;
    }

    private static int insertMember(
            Connection connection,
            String name,
            String email,
            String phoneNumber,
            String address
    ) throws SQLException {
        String sql = """
                INSERT INTO member (name, email, phone_number, address)
                VALUES (?, ?, ?, ?)
                RETURNING id
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setString(2, email);
            statement.setString(3, phoneNumber);
            statement.setString(4, address);

            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            return resultSet.getInt("id");
        }
    }

    private static void insertCard(
            Connection connection,
            int memberId,
            String number,
            String issuedOn,
            String expiresOn
    ) throws SQLException {
        String sql = """
                INSERT INTO card (member_id, number, issued_on, expires_on)
                VALUES (?, ?, ?, ?)
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, memberId);
            statement.setString(2, number);
            statement.setDate(3, Date.valueOf(issuedOn));
            statement.setDate(4, Date.valueOf(expiresOn));
            statement.executeUpdate();
        }
    }

    private static int insertPublisher(Connection connection, String name) throws SQLException {
        String sql = """
                INSERT INTO publisher (name)
                VALUES (?)
                RETURNING id
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);

            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            return resultSet.getInt("id");
        }
    }

    private static int insertBook(
            Connection connection,
            String title,
            String isbn,
            int publisherId
    ) throws SQLException {
        String sql = """
                INSERT INTO book (title, isbn, publisher_id)
                VALUES (?, ?, ?)
                RETURNING id
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, title);
            statement.setString(2, isbn);
            statement.setInt(3, publisherId);

            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            return resultSet.getInt("id");
        }
    }

    private static int insertAuthor(Connection connection, String name) throws SQLException {
        String sql = """
                INSERT INTO author (name)
                VALUES (?)
                RETURNING id
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);

            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            return resultSet.getInt("id");
        }
    }

    private static void insertBookAuthor(
            Connection connection,
            int bookId,
            int authorId
    ) throws SQLException {
        String sql = """
                INSERT INTO book_author (book_id, author_id)
                VALUES (?, ?)
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, bookId);
            statement.setInt(2, authorId);
            statement.executeUpdate();
        }
    }

    private static int insertTag(Connection connection, String name) throws SQLException {
        String sql = """
                INSERT INTO tag (name)
                VALUES (?)
                RETURNING id
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);

            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            return resultSet.getInt("id");
        }
    }

    private static void insertBookTag(
            Connection connection,
            int bookId,
            int tagId
    ) throws SQLException {
        String sql = """
                INSERT INTO book_tag (book_id, tag_id)
                VALUES (?, ?)
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, bookId);
            statement.setInt(2, tagId);
            statement.executeUpdate();
        }
    }

    private static int insertBookCopy(
            Connection connection,
            int bookId,
            String barcode,
            String shelfCode
    ) throws SQLException {
        String sql = """
                INSERT INTO book_copy (book_id, barcode, shelf_code)
                VALUES (?, ?, ?)
                RETURNING id
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, bookId);
            statement.setString(2, barcode);
            statement.setString(3, shelfCode);

            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            return resultSet.getInt("id");
        }
    }

    private static void insertBorrowing(
            Connection connection,
            int memberId,
            int bookCopyId,
            String borrowedAt,
            String dueDate,
            String returnedAt,
            String fineEur
    ) throws SQLException {
        String sql = """
                INSERT INTO borrowing (
                    member_id,
                    book_copy_id,
                    borrowed_at,
                    due_date,
                    returned_at,
                    fine_eur
                )
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, memberId);
            statement.setInt(2, bookCopyId);
            statement.setTimestamp(3, Timestamp.valueOf(borrowedAt));
            statement.setTimestamp(4, Timestamp.valueOf(dueDate));
            statement.setTimestamp(5, Timestamp.valueOf(returnedAt));
            statement.setBigDecimal(6, new BigDecimal(fineEur));

            statement.executeUpdate();
        }
    }
}