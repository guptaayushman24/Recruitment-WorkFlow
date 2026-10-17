import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * One-time script: walks a local framework directory and stores its folder/file tree
 * in the development_framework_setup table (folders as rows, file bytes as BYTEA), in a single transaction.
 *
 * Requires the PostgreSQL JDBC driver on the classpath:
 *   Maven:  org.postgresql:postgresql:42.7.4
 *
 * Run (Java 21, single-file):
 *   java -cp postgresql-42.7.4.jar UploadFrameworkToPostgres.java
 */
public class UploadFrameworkToPostgres {

    // 1. Database connection settings
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "root";

    // 2. Configuration for this run
    private static final String TARGET_DIRECTORY = "/Users/ayushmangupta/Downloads/demo"; // Change this to your framework path
    private static final long PROJECT_ID = 1L;                                 // Unique ID tracking this framework

    private static final String INSERT_FOLDER_SQL = """
            INSERT INTO recruitment_workflow.development_framework_setup (project_id, name, type, parent_id, content, path_directory)
            VALUES (?, ?, 'folder', ?, NULL, ?) RETURNING id
            """;

    private static final String INSERT_FILE_SQL = """
            INSERT INTO recruitment_workflow.development_framework_setup (project_id, name, type, parent_id, content, path_directory)
            VALUES (?, ?, 'file', ?, ?, ?)
            """;

    public static void main(String[] args) {
        uploadFrameworkToPostgres();
    }

    private static void uploadFrameworkToPostgres() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // Start a single transaction so it is atomic (all files upload, or none do)
            conn.setAutoCommit(false);

            try (PreparedStatement folderStmt = conn.prepareStatement(INSERT_FOLDER_SQL);
                 PreparedStatement fileStmt = conn.prepareStatement(INSERT_FILE_SQL)) {

                Path absoluteRootPath = Paths.get(TARGET_DIRECTORY).toAbsolutePath().normalize();
                String rootFolderName = absoluteRootPath.getFileName().toString();

                System.out.printf("🚀 Analyzing structure for framework root: \"%s\"%n", rootFolderName);

                // Insert the main root folder entry (parent_id is NULL)
                long rootDbId = insertFolder(folderStmt, rootFolderName, null, rootFolderName);

                // Execute processing loop from root
                walkAndInsert(folderStmt, fileStmt, absoluteRootPath, absoluteRootPath, rootDbId);

                conn.commit();
                System.out.println("\n✅ Success! Framework directory structural backup completed in PostgreSQL.");

            } catch (SQLException | IOException e) {
                conn.rollback();
                System.err.println("\n❌ Migration failed. Changes rolled back. Error: " + e.getMessage());
            }

        } catch (SQLException e) {
            System.err.println("\n❌ Could not connect to PostgreSQL. Error: " + e.getMessage());
        }
    }

    /**
     * Recursive tree walk (processes directories top-down first).
     * The parent's DB id is passed down directly, so no path->id map is needed.
     */
    private static void walkAndInsert(PreparedStatement folderStmt,
                                      PreparedStatement fileStmt,
                                      Path rootPath,
                                      Path currentPath,
                                      long currentDirId) throws SQLException, IOException {

        // Split into folders and files to process structural directories first
        List<Path> folders = new ArrayList<>();
        List<Path> files = new ArrayList<>();

        try (DirectoryStream<Path> items = Files.newDirectoryStream(currentPath)) {
            for (Path item : items) {
                // NOFOLLOW_LINKS mirrors Node's Dirent.isDirectory()/isFile(): symlinks are skipped
                if (Files.isDirectory(item, LinkOption.NOFOLLOW_LINKS)) {
                    folders.add(item);
                } else if (Files.isRegularFile(item, LinkOption.NOFOLLOW_LINKS)) {
                    files.add(item);
                }
            }
        }

        // A. Insert sub-folders and recurse into each immediately
        for (Path folder : folders) {
            long folderId = insertFolder(folderStmt, folder.getFileName().toString(), currentDirId,
                    pathDirectory(rootPath, folder));
            walkAndInsert(folderStmt, fileStmt, rootPath, folder, folderId);
        }

        // B. Insert raw files
        for (Path file : files) {
            // Reads raw bytes (maps directly to BYTEA)
            byte[] binaryContent = Files.readAllBytes(file);

            fileStmt.setLong(1, PROJECT_ID);
            fileStmt.setString(2, file.getFileName().toString());
            fileStmt.setLong(3, currentDirId);
            fileStmt.setBytes(4, binaryContent);
            fileStmt.setString(5, pathDirectory(rootPath, file));
            fileStmt.executeUpdate();

            System.out.println("  📄 Uploaded: " + rootPath.relativize(file));
        }
    }

    /**
     * Path sent to the frontend: relative to the framework root, prefixed with the root folder
     * name and always '/'-separated (e.g. "demo/src/App.js"), regardless of the OS running this script.
     */
    private static String pathDirectory(Path rootPath, Path item) {
        String relative = rootPath.relativize(item).toString().replace('\\', '/');
        return rootPath.getFileName() + "/" + relative;
    }

    /** Inserts a folder row and returns its generated id. parentId == null means root. */
    private static long insertFolder(PreparedStatement folderStmt, String name, Long parentId, String pathDirectory) throws SQLException {
        folderStmt.setLong(1, PROJECT_ID);
        folderStmt.setString(2, name);
        if (parentId == null) {
            folderStmt.setNull(3, java.sql.Types.BIGINT);
        } else {
            folderStmt.setLong(3, parentId);
        }
        folderStmt.setString(4, pathDirectory);

        try (ResultSet rs = folderStmt.executeQuery()) {
            rs.next();
            return rs.getLong("id");
        }
    }
}