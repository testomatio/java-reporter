package io.testomat.core.artifact;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.testomat.core.facade.methods.artifact.TempArtifactDirectoriesStorage;
import io.testomat.core.step.StepData;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@DisplayName("TempArtifactDirectoriesStorage Tests")
class TempArtifactDirectoriesStorageTest {

    @BeforeEach
    void setUp() {
        // Clean storage before each test
        TempArtifactDirectoriesStorage.DIRECTORIES.get().clear();
        TempArtifactDirectoriesStorage.STEP_DATA.clear();
    }

    @AfterEach
    void tearDown() {
        // Clean storage after each test
        TempArtifactDirectoriesStorage.DIRECTORIES.remove();
        TempArtifactDirectoriesStorage.STEP_DATA.clear();
    }

    @Test
    @DisplayName("Should initialize with empty list")
    void testInitializeWithEmptyList() {
        List<String> directories = TempArtifactDirectoriesStorage.DIRECTORIES.get();

        assertNotNull(directories);
        assertTrue(directories.isEmpty());
    }

    @Test
    @DisplayName("Should store single directory path")
    void testStoreSingleDirectory() {
        String directory = "/tmp/artifacts/test1";

        TempArtifactDirectoriesStorage.store(directory);

        List<String> directories = TempArtifactDirectoriesStorage.DIRECTORIES.get();
        assertFalse(directories.isEmpty());
        assertEquals(1, directories.size());
        assertEquals(directory, directories.get(0));
    }

    @Test
    @DisplayName("Should store multiple directory paths")
    void testStoreMultipleDirectories() {
        String dir1 = "/tmp/artifacts/test1";
        String dir2 = "/tmp/artifacts/test2";
        String dir3 = "/tmp/artifacts/test3";

        TempArtifactDirectoriesStorage.store(dir1);
        TempArtifactDirectoriesStorage.store(dir2);
        TempArtifactDirectoriesStorage.store(dir3);

        List<String> directories = TempArtifactDirectoriesStorage.DIRECTORIES.get();
        assertEquals(3, directories.size());
        assertEquals(dir1, directories.get(0));
        assertEquals(dir2, directories.get(1));
        assertEquals(dir3, directories.get(2));
    }

    @Test
    @DisplayName("Should maintain order of stored directories")
    void testMaintainOrderOfStoredDirectories() {
        TempArtifactDirectoriesStorage.store("first");
        TempArtifactDirectoriesStorage.store("second");
        TempArtifactDirectoriesStorage.store("third");

        List<String> directories = TempArtifactDirectoriesStorage.DIRECTORIES.get();
        assertEquals("first", directories.get(0));
        assertEquals("second", directories.get(1));
        assertEquals("third", directories.get(2));
    }

    @Test
    @DisplayName("Should handle absolute paths")
    void testHandleAbsolutePaths() {
        String absolutePath = "/home/user/artifacts/screenshots";

        TempArtifactDirectoriesStorage.store(absolutePath);

        List<String> directories = TempArtifactDirectoriesStorage.DIRECTORIES.get();
        assertEquals(absolutePath, directories.get(0));
    }

    @Test
    @DisplayName("Should handle relative paths")
    void testHandleRelativePaths() {
        String relativePath = "target/test-artifacts";

        TempArtifactDirectoriesStorage.store(relativePath);

        List<String> directories = TempArtifactDirectoriesStorage.DIRECTORIES.get();
        assertEquals(relativePath, directories.get(0));
    }

    @Test
    @DisplayName("Should handle Windows-style paths")
    void testHandleWindowsStylePaths() {
        String windowsPath = "C:\\Users\\Test\\artifacts";

        TempArtifactDirectoriesStorage.store(windowsPath);

        List<String> directories = TempArtifactDirectoriesStorage.DIRECTORIES.get();
        assertEquals(windowsPath, directories.get(0));
    }

    @Test
    @DisplayName("Should handle paths with special characters")
    void testHandlePathsWithSpecialCharacters() {
        String pathWithSpaces = "/tmp/test artifacts/folder name";

        TempArtifactDirectoriesStorage.store(pathWithSpaces);

        List<String> directories = TempArtifactDirectoriesStorage.DIRECTORIES.get();
        assertEquals(pathWithSpaces, directories.get(0));
    }

    @Test
    @DisplayName("Should allow duplicate directory paths")
    void testAllowDuplicateDirectoryPaths() {
        String directory = "/tmp/artifacts";

        TempArtifactDirectoriesStorage.store(directory);
        TempArtifactDirectoriesStorage.store(directory);

        List<String> directories = TempArtifactDirectoriesStorage.DIRECTORIES.get();
        assertEquals(2, directories.size());
        assertEquals(directory, directories.get(0));
        assertEquals(directory, directories.get(1));
    }

    @Test
    @DisplayName("Should be thread-safe with ThreadLocal")
    void testThreadSafety() throws InterruptedException {
        Thread thread1 = new Thread(() -> {
            TempArtifactDirectoriesStorage.store("thread1-dir1");
            TempArtifactDirectoriesStorage.store("thread1-dir2");

            List<String> directories = TempArtifactDirectoriesStorage.DIRECTORIES.get();
            assertEquals(2, directories.size());
            assertTrue(directories.contains("thread1-dir1"));
            assertTrue(directories.contains("thread1-dir2"));
            assertFalse(directories.contains("thread2-dir1"));
        });

        Thread thread2 = new Thread(() -> {
            TempArtifactDirectoriesStorage.store("thread2-dir1");
            TempArtifactDirectoriesStorage.store("thread2-dir2");

            List<String> directories = TempArtifactDirectoriesStorage.DIRECTORIES.get();
            assertEquals(2, directories.size());
            assertTrue(directories.contains("thread2-dir1"));
            assertTrue(directories.contains("thread2-dir2"));
            assertFalse(directories.contains("thread1-dir1"));
        });

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();
    }

    @Test
    @DisplayName("Should isolate storage between threads")
    void testStorageIsolationBetweenThreads() throws InterruptedException {
        final int[] thread1Count = {0};
        final int[] thread2Count = {0};

        Thread thread1 = new Thread(() -> {
            TempArtifactDirectoriesStorage.store("thread1-path");
            thread1Count[0] = TempArtifactDirectoriesStorage.DIRECTORIES.get().size();
        });

        Thread thread2 = new Thread(() -> {
            TempArtifactDirectoriesStorage.store("thread2-path1");
            TempArtifactDirectoriesStorage.store("thread2-path2");
            thread2Count[0] = TempArtifactDirectoriesStorage.DIRECTORIES.get().size();
        });

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        assertEquals(1, thread1Count[0]);
        assertEquals(2, thread2Count[0]);
    }

    @Test
    @DisplayName("Should clear directories list")
    void testClearDirectoriesList() {
        TempArtifactDirectoriesStorage.store("dir1");
        TempArtifactDirectoriesStorage.store("dir2");

        List<String> directories = TempArtifactDirectoriesStorage.DIRECTORIES.get();
        directories.clear();

        assertTrue(directories.isEmpty());
    }

    @Test
    @DisplayName("Should retrieve all stored directories")
    void testRetrieveAllStoredDirectories() {
        TempArtifactDirectoriesStorage.store("dir1");
        TempArtifactDirectoriesStorage.store("dir2");
        TempArtifactDirectoriesStorage.store("dir3");

        List<String> directories = TempArtifactDirectoriesStorage.DIRECTORIES.get();

        assertEquals(3, directories.size());
        assertTrue(directories.contains("dir1"));
        assertTrue(directories.contains("dir2"));
        assertTrue(directories.contains("dir3"));
    }

    @Test
    @DisplayName("Should handle empty string directories")
    void testHandleEmptyStringDirectories() {
        TempArtifactDirectoriesStorage.store("");

        List<String> directories = TempArtifactDirectoriesStorage.DIRECTORIES.get();
        assertEquals(1, directories.size());
        assertEquals("", directories.get(0));
    }

    @Test
    @DisplayName("Should handle long directory paths")
    void testHandleLongDirectoryPaths() {
        String longPath = "/very/long/path/to/artifacts/with/many/nested/directories/for/testing/purposes";

        TempArtifactDirectoriesStorage.store(longPath);

        List<String> directories = TempArtifactDirectoriesStorage.DIRECTORIES.get();
        assertEquals(longPath, directories.get(0));
    }

    @Test
    @DisplayName("Should support iteration over stored directories")
    void testIterationOverStoredDirectories() {
        TempArtifactDirectoriesStorage.store("dir1");
        TempArtifactDirectoriesStorage.store("dir2");
        TempArtifactDirectoriesStorage.store("dir3");

        List<String> directories = TempArtifactDirectoriesStorage.DIRECTORIES.get();

        int count = 0;
        for (String dir : directories) {
            assertNotNull(dir);
            count++;
        }

        assertEquals(3, count);
    }

    @Test
    @DisplayName("Should handle ThreadLocal cleanup")
    void testThreadLocalCleanup() {
        TempArtifactDirectoriesStorage.store("dir1");

        List<String> directoriesBefore = TempArtifactDirectoriesStorage.DIRECTORIES.get();
        assertEquals(1, directoriesBefore.size());

        TempArtifactDirectoriesStorage.DIRECTORIES.remove();

        List<String> directoriesAfter = TempArtifactDirectoriesStorage.DIRECTORIES.get();
        assertTrue(directoriesAfter.isEmpty());
    }

    @Test
    @DisplayName("Should handle multiple store operations in sequence")
    void testMultipleStoreOperationsInSequence() {
        for (int i = 0; i < 10; i++) {
            TempArtifactDirectoriesStorage.store("dir-" + i);
        }

        List<String> directories = TempArtifactDirectoriesStorage.DIRECTORIES.get();
        assertEquals(10, directories.size());

        for (int i = 0; i < 10; i++) {
            assertEquals("dir-" + i, directories.get(i));
        }
    }

    @Test
    @DisplayName("Should store step directory")
    void stepStoreShouldStoreDirectory(){
        UUID stepId = UUID.randomUUID();

        TempArtifactDirectoriesStorage.stepStore(stepId,"dir1");

        StepData stepData =
            TempArtifactDirectoriesStorage.STEP_DATA
                .get(Thread.currentThread().getId())
                .get(stepId);

        assertNotNull(stepData);
        assertEquals(1, stepData.getDirectories().size());
        assertEquals("dir1", stepData.getDirectories().get(0));
    }

    @Test
    @DisplayName("Should store multiple directories per step")
    void stepStoreShouldStoreMultiple(){
        UUID stepId = UUID.randomUUID();

        TempArtifactDirectoriesStorage.stepStore(stepId,"dir1");
        TempArtifactDirectoriesStorage.stepStore(stepId,"dir2");

        StepData stepData =
            TempArtifactDirectoriesStorage.STEP_DATA
                .get(Thread.currentThread().getId())
                .get(stepId);

        assertEquals(2, stepData.getDirectories().size());
        assertTrue(stepData.getDirectories().contains("dir1"));
        assertTrue(stepData.getDirectories().contains("dir2"));
    }

    @Test
    @DisplayName("Should isolate different step ids")
    void stepStoreShouldIsolateSteps(){
        UUID step1 = UUID.randomUUID();
        UUID step2 = UUID.randomUUID();

        TempArtifactDirectoriesStorage.stepStore(step1,"dir1");
        TempArtifactDirectoriesStorage.stepStore(step2,"dir2");

        Map<UUID, StepData> stepData =
            TempArtifactDirectoriesStorage.STEP_DATA
                .get(Thread.currentThread().getId());

        assertEquals(1, stepData.get(step1).getDirectories().size());
        assertEquals(1, stepData.get(step2).getDirectories().size());

        assertFalse(
            stepData.get(step1)
                .getDirectories()
                .contains("dir2")
        );
    }

    @Test
    @DisplayName("Should be thread safe for step store")
    void stepStoreShouldBeThreadSafe() throws InterruptedException {
        UUID stepId = UUID.randomUUID();

        Thread t1=new Thread(() -> {
            for(int i = 0; i < 100; i++){
                TempArtifactDirectoriesStorage.stepStore(stepId,"t1-"+i);
            }
        });

        Thread t2=new Thread(() -> {
            for(int i = 0; i < 100; i++){
                TempArtifactDirectoriesStorage.stepStore(stepId,"t2-"+i);
            }
        });

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        assertEquals(2, TempArtifactDirectoriesStorage.STEP_DATA.size());

        for (Map<UUID, StepData> stepData :
            TempArtifactDirectoriesStorage.STEP_DATA.values()) {

            assertEquals(
                100,
                stepData.get(stepId).getDirectories().size()
            );
        }
    }

    @Test
    @DisplayName("Should create list only once")
    void stepStoreShouldReuseList(){
        UUID stepId=UUID.randomUUID();

        TempArtifactDirectoriesStorage
            .stepStore(stepId,"dir1");

        StepData first =
            TempArtifactDirectoriesStorage.STEP_DATA
                .get(Thread.currentThread().getId())
                .get(stepId);

        TempArtifactDirectoriesStorage.stepStore(stepId, "dir2");

        StepData second =
            TempArtifactDirectoriesStorage.STEP_DATA
                .get(Thread.currentThread().getId())
                .get(stepId);

        assertSame(first, second);
    }

    @Test
    void stepStoreShouldHandleNullDir(){
        UUID stepId=UUID.randomUUID();

        TempArtifactDirectoriesStorage
            .stepStore(stepId,null);

        StepData stepData =
            TempArtifactDirectoriesStorage.STEP_DATA
                .get(Thread.currentThread().getId())
                .get(stepId);

        assertEquals(1, stepData.getDirectories().size());
        assertNull(stepData.getDirectories().get(0));
    }

    @Test
    void stepDataShouldAllowRemoval() {
        UUID stepId = UUID.randomUUID();

        TempArtifactDirectoriesStorage.stepStore(stepId, "dir");

        TempArtifactDirectoriesStorage.STEP_DATA
            .get(Thread.currentThread().getId())
            .remove(stepId);

        assertNull(
            TempArtifactDirectoriesStorage.STEP_DATA
                .get(Thread.currentThread().getId())
                .get(stepId)
        );
    }
}
