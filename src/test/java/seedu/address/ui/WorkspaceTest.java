package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import javafx.util.Duration;
import seedu.address.commons.core.GuiSettings;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.academicclass.AcademicClass;
import seedu.address.model.academicclass.ClassName;
import seedu.address.model.academicclass.ModuleCode;
import seedu.address.model.person.Person;
import seedu.address.model.util.SampleDataUtil;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/** Exercises the assembled FXML workspace with real command execution and isolated data. */
public class WorkspaceTest {
    @TempDir
    public Path testFolder;

    @BeforeAll
    public static void initializeToolkit() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Another UI test has already started the toolkit.
        }
        Platform.setImplicitExit(false);
    }

    @Test
    public void workspace_draftsAreReadOnlyAndCommandsRefreshAttendance() throws Exception {
        runOnFxThreadAndWait(() -> {
            Logic logic = createLogic(sampleBook());
            logic.setGuiSettings(new GuiSettings(1040, 760, 0, 0));
            Stage stage = new Stage();
            try {
                MainWindow window = new MainWindow(stage, logic, testFolder.resolve("addressbook.json"));
                window.fillInnerParts();
                window.show();
                layout(stage);
                ListView<?> board = (ListView<?>) stage.getScene().lookup("#academicClassListView");
                assertEquals(Orientation.VERTICAL, board.getOrientation());
                assertNull(board.getSelectionModel());
                assertEquals(3, board.getItems().size());
                assertContains(stage.getScene().getRoot(), "3 shown / 3 total");
                assertEquals("6 shown", ((Label) stage.getScene().lookup("#shownStudentCount")).getText());
                capture(stage, "workspace");

                TextField input = (TextField) stage.getScene().lookup("#commandTextField");
                TextArea output = (TextArea) stage.getScene().lookup("#resultDisplay");
                Node bottom = stage.getScene().lookup(".workspace-bottom");
                assertTrue(input.localToScene(input.getBoundsInLocal()).getMinY()
                        < output.localToScene(output.getBoundsInLocal()).getMinY());
                assertTrue(output.localToScene(output.getBoundsInLocal()).getMaxY()
                        <= bottom.localToScene(bottom.getBoundsInLocal()).getMinY());
                ListView<?> students = (ListView<?>) window.getPersonListPanel().getRoot().lookup("#personListView");
                input.setText("find Bernice");
                assertEquals(1, students.getItems().size());
                assertEquals("1 shown", ((Label) stage.getScene().lookup("#shownStudentCount")).getText());
                assertEquals(6, logic.getFilteredPersonList().size());
                int recordsBefore = logic.getAttendanceList().stream().mapToInt(record -> record.getAttendanceCount())
                        .sum();
                input.setText("addattendance 2 1 d/2024-02-06");
                assertContains(stage.getScene().getRoot(), "CS2103 · F10");
                assertEquals(recordsBefore, logic.getAttendanceList().stream()
                        .mapToInt(record -> record.getAttendanceCount()).sum());
                capture(stage, "attendance-draft");

                input.fireEvent(new ActionEvent());
                assertEquals("", input.getText());
                assertEquals(recordsBefore + 1, logic.getAttendanceList().stream()
                        .mapToInt(record -> record.getAttendanceCount()).sum());
                assertContains(stage.getScene().getRoot(), "CS2103 · F10");

                input.setText("listbyclass 2");
                assertContains(stage.getScene().getRoot(), "CS2101 · T02");
                assertEquals(2, students.getItems().size());
                assertEquals("2 shown", ((Label) stage.getScene().lookup("#shownStudentCount")).getText());
                assertEquals(Set.of("2. ", "5. "), students.lookupAll("#id").stream()
                        .map(node -> ((Label) node).getText()).collect(java.util.stream.Collectors.toSet()));
                input.setText("assign c/T02 sn/A0234567Y m/CS2101");
                assertContains(stage.getScene().getRoot(), "CS2101 · T02");
                assertEquals(6, logic.getFilteredPersonList().size());
                input.setText("listbyclass 1");
                assertEquals(6, logic.getFilteredPersonList().size());
                input.fireEvent(new ActionEvent());
                assertEquals(4, logic.getFilteredPersonList().size());
                assertNull(board.getSelectionModel());
                assertContains(stage.getScene().getRoot(), "CS2103 · F10");
                capture(stage, "class-attendance");
                input.setText("addattendance 9999 9999 d/");
                assertContains(stage.getScene().getRoot(), "outside the displayed student list");
                assertContains(stage.getScene().getRoot(), "outside the displayed class list");
                assertEquals(0, students.getItems().size());
                assertEquals("0 shown", ((Label) stage.getScene().lookup("#shownStudentCount")).getText());
                input.setText("");
                stage.setWidth(740);
                stage.setHeight(600);
                capture(stage, "compact-workspace");
            } finally {
                stage.hide();
            }
        });
    }

    @Test
    public void workspace_emptyModel_hasUsefulEmptyStates() throws Exception {
        runOnFxThreadAndWait(() -> {
            Stage stage = new Stage();
            try {
                Logic logic = createLogic(new AddressBook());
                logic.setGuiSettings(new GuiSettings(1100, 730, 0, 0));
                MainWindow window = new MainWindow(stage, logic, testFolder.resolve("empty"));
                assertEquals(1100, stage.getWidth());
                assertEquals(730, stage.getHeight());
                window.fillInnerParts();
                window.show();
                layout(stage);
                assertContains(stage.getScene().getRoot(), "No students shown");
                capture(stage, "empty-workspace");
            } finally {
                stage.hide();
            }
        });
    }

    private Logic createLogic(AddressBook book) {
        return new LogicManager(new ModelManager(book, new UserPrefs()), new StorageManager(
                new JsonAddressBookStorage(testFolder.resolve("addressbook.json")),
                new JsonUserPrefsStorage(testFolder.resolve("preferences.json"))));
    }

    private AddressBook sampleBook() {
        AddressBook book = new AddressBook();
        Person[] students = SampleDataUtil.getSamplePersons();
        for (Person student : students) {
            book.addPerson(student);
        }
        AcademicClass first = new AcademicClass(new ModuleCode("CS2103"), new ClassName("F10"));
        AcademicClass second = new AcademicClass(new ModuleCode("CS2101"), new ClassName("T02"));
        AcademicClass third = new AcademicClass(new ModuleCode("CS2100"),
                new ClassName("L03-COMPUTER-ARCHITECTURE"));
        for (AcademicClass academicClass : List.of(first, second, third)) {
            book.addAcademicClass(academicClass);
        }
        for (int index = 0; index < 4; index++) {
            book.assignStudentToClass(students[index], first);
            for (int day = 1; day <= 5; day++) {
                if ((index + day) % 3 != 0) {
                    book.addAttendance(students[index], first, LocalDate.of(2024, 2, day));
                }
            }
        }
        book.assignStudentToClass(students[1], second);
        book.assignStudentToClass(students[4], second);
        book.assignStudentToClass(students[5], third);
        return book;
    }

    private static void assertContains(Node root, String text) {
        root.applyCss();
        if (root instanceof javafx.scene.Parent parent) {
            parent.requestLayout();
            parent.layout();
        }
        assertTrue(root.lookupAll(".label").stream().filter(Label.class::isInstance)
                .map(Label.class::cast).anyMatch(label -> label.getText().contains(text)), text);
    }

    private static void layout(Stage stage) {
        stage.getScene().getRoot().requestLayout();
        stage.getScene().getRoot().applyCss();
        stage.getScene().getRoot().layout();
        stage.getScene().getRoot().layout();
    }

    private static void capture(Stage stage, String name) {
        layout(stage);
        if (!"1".equals(System.getenv("TASSIST_CAPTURE_UI"))) {
            return;
        }
        // Let control skins and the native window resize settle before rendering the scene.
        Object frame = new Object();
        PauseTransition pause = new PauseTransition(Duration.millis(100));
        pause.setOnFinished(event -> Platform.exitNestedEventLoop(frame, null));
        pause.play();
        Platform.enterNestedEventLoop(frame);
        WritableImage snapshot = stage.getScene().snapshot(null);
        int width = (int) snapshot.getWidth();
        int height = (int) snapshot.getHeight();
        int[] pixels = new int[width * height];
        snapshot.getPixelReader().getPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), pixels, 0, width);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, width, height, pixels, 0, width);
        try {
            Path directory = Path.of("build", "ui-preview");
            Files.createDirectories(directory);
            ImageIO.write(image, "png", directory.resolve(name + ".png").toFile());
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    private static void runOnFxThreadAndWait(Runnable action) throws Exception {
        FutureTask<Void> task = new FutureTask<>(action, null);
        Platform.runLater(task);
        task.get(20, TimeUnit.SECONDS);
    }
}
