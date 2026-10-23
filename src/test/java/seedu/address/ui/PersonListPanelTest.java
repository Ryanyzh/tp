package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import seedu.address.model.ModelManager;
import seedu.address.model.academicclass.AcademicClass;
import seedu.address.model.academicclass.ClassName;
import seedu.address.model.academicclass.ModuleCode;
import seedu.address.model.attendance.Attendance;
import seedu.address.model.person.Person;

public class PersonListPanelTest {

    @BeforeAll
    public static void setUpBeforeClass() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // JavaFX toolkit has already been initialized.
        }
    }

    @Test
    public void constructor_withAttendanceSuppliers_createsListView() {
        runOnFxThreadAndWait(() -> {
            AcademicClass academicClass = new AcademicClass(new ModuleCode("CS2103"),
                    new ClassName("F10-2"));
            Attendance attendance = new Attendance(ALICE, academicClass);
            attendance.addDate(LocalDate.of(2020, 1, 1));
            PersonListPanel panel = new PersonListPanel(FXCollections.observableArrayList(ALICE), () ->
                    Optional.of(academicClass), () -> List.of(attendance));

            ListView<?> listView = (ListView<?>) panel.getRoot().lookup(".list-view");
            assertNotNull(listView);
            assertEquals(List.of(ALICE), listView.getItems());
            assertNotNull(listView.getCellFactory().call(null));
        });
    }

    @Test
    public void updateItem_emptyCell_clearsGraphicAndText() {
        runOnFxThreadAndWait(() -> {
            PersonListPanel panel = new PersonListPanel(FXCollections.observableArrayList(),
                    Optional::empty, List::of);
            PersonListPanel.PersonListViewCell cell = panel.new PersonListViewCell();

            cell.updateItem(null, false);
            cell.updateItem(null, true);

            assertNull(cell.getGraphic());
            assertNull(cell.getText());
        });
    }

    @Test
    public void updateItem_person_createsAttendanceCard() {
        runOnFxThreadAndWait(() -> {
            AcademicClass academicClass = new AcademicClass(new ModuleCode("CS2103"),
                    new ClassName("F10-2"));
            PersonListPanel panel = new PersonListPanel(FXCollections.observableArrayList(ALICE), () ->
                    Optional.of(academicClass), List::of);
            PersonListPanel.PersonListViewCell cell = panel.new PersonListViewCell();

            cell.updateItem(ALICE, false);

            assertNotNull(cell.getGraphic());
            Label attendance = (Label) cell.getGraphic().lookup("#attendance");
            assertTrue(attendance.isVisible());
            assertEquals("No attendance recorded", attendance.getText());
        });
    }

    @Test
    public void attendanceChange_refreshesAlreadyVisibleCard() {
        runOnFxThreadAndWait(() -> {
            ModelManager model = new ModelManager();
            AcademicClass academicClass = new AcademicClass(new ModuleCode("CS2103"),
                    new ClassName("F10-2"));
            model.addPerson(ALICE);
            model.addAcademicClass(academicClass);
            model.assignStudentToClass(ALICE, academicClass);
            model.filterPersonsByClass(academicClass);
            model.setAttendanceDisplayClass(academicClass);

            PersonListPanel panel = new PersonListPanel(model.getFilteredPersonList(),
                    model::getAttendanceDisplayClass, model::getAttendanceList);
            PersonListPanel.PersonListViewCell displayedCell = panel.new PersonListViewCell();
            displayedCell.updateItem(ALICE, false);
            assertEquals("No attendance recorded", ((Label) displayedCell.getGraphic()
                    .lookup("#attendance")).getText());

            model.getFilteredPersonList().addListener((ListChangeListener<Person>) change ->
                    displayedCell.updateItem(ALICE, false));

            assertTrue(model.addAttendance(ALICE, academicClass, LocalDate.of(2020, 1, 1)));
            assertEquals("Attendance · 1 day", ((Label) displayedCell.getGraphic().lookup("#attendance")).getText());

            assertTrue(model.removeAttendance(ALICE, academicClass, LocalDate.of(2020, 1, 1)));
            assertEquals("No attendance recorded", ((Label) displayedCell.getGraphic()
                    .lookup("#attendance")).getText());
        });
    }

    private static void runOnFxThreadAndWait(Runnable action) {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> thrown = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                action.run();
            } catch (Throwable throwable) {
                thrown.set(throwable);
            } finally {
                latch.countDown();
            }
        });
        try {
            assertTrue(latch.await(10, TimeUnit.SECONDS), "JavaFX action timed out.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError(e);
        }
        if (thrown.get() != null) {
            throw new AssertionError(thrown.get());
        }
    }
}
