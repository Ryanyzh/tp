package seedu.address.ui;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.model.academicclass.AcademicClass;
import seedu.address.model.attendance.Attendance;
import seedu.address.model.person.Person;

/**
 * A UI component that displays information of a {@code Person}.
 */
public class PersonCard extends UiPart<Region> {

    private static final String FXML = "PersonListCard.fxml";

    /**
     * Note: Certain keywords such as "location" and "resources" are reserved keywords in JavaFX.
     * As a consequence, UI elements' variable names cannot be set to such keywords
     * or an exception will be thrown by JavaFX during runtime.
     *
     * @see <a href="https://github.com/se-edu/addressbook-level4/issues/336">The issue on AddressBook level 4</a>
     */

    public final Person person;

    @FXML
    private HBox cardPane;
    @FXML
    private Label name;
    @FXML
    private Label id;
    @FXML
    private Label studentNumber;
    @FXML
    private Label email;
    @FXML
    private Label telegram;
    @FXML
    private VBox attendanceBox;
    @FXML
    private FlowPane tags;

    /**
     * Creates a {@code PersonCard} with the given {@code Person} and index to display.
     */
    public PersonCard(Person person, int displayedIndex) {
        this(person, displayedIndex, null);
    }

    /** Creates a card and optionally shows this student's attendance for a class. */
    public PersonCard(Person person, int displayedIndex, AcademicClass attendanceClass) {
        this(person, displayedIndex, attendanceClass, List.of());
    }

    /** Creates a card and optionally shows this student's attendance for a class. */
    public PersonCard(Person person, int displayedIndex, AcademicClass attendanceClass,
            List<Attendance> attendanceRecords) {
        super(FXML);
        this.person = person;
        id.setText(displayedIndex + ". ");
        name.setText(person.getName().fullName);
        studentNumber.setText(person.getStudentNumber().value);
        email.setText(person.getEmail().value);
        if (person.getTelegram().isPresent()) {
            telegram.setText("@" + person.getTelegram().get().value);
        } else {
            telegram.setVisible(false);
            telegram.setManaged(false);
        }
        if (attendanceClass == null) {
            attendanceBox.setVisible(false);
            attendanceBox.setManaged(false);
        } else {
            updateAttendance(attendanceClass, attendanceRecords);
        }
        person.getTags().stream()
                .sorted(Comparator.comparing(tag -> tag.tagName))
                .forEach(tag -> tags.getChildren().add(new Label(tag.tagName)));
    }

    private void updateAttendance(AcademicClass attendanceClass, List<Attendance> attendanceRecords) {
        Attendance attendanceRecord = attendanceRecords.stream()
                .filter(record -> record.getStudent().isSamePerson(person)
                        && record.getAcademicClass().isSameClass(attendanceClass))
                .findFirst()
                .orElse(null);
        attendanceBox.getChildren().setAll(new AttendanceStrip(attendanceRecord == null
                ? Set.of() : attendanceRecord.getAttendedDates()));
    }
}
