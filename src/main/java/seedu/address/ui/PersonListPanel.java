package seedu.address.ui;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.logging.Logger;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.academicclass.AcademicClass;
import seedu.address.model.attendance.Attendance;
import seedu.address.model.person.Person;

/**
 * Panel containing the list of persons.
 */
public class PersonListPanel extends UiPart<Region> {
    private static final String FXML = "PersonListPanel.fxml";
    private final Logger logger = LogsCenter.getLogger(PersonListPanel.class);
    private final Supplier<Optional<AcademicClass>> attendanceClassSupplier;
    private final Supplier<List<Attendance>> attendanceListSupplier;
    private final ToIntFunction<Person> displayedIndex;

    @FXML
    private ListView<Person> personListView;

    /** Creates a person list whose cards can display the currently selected class attendance. */
    public PersonListPanel(ObservableList<Person> personList,
            Supplier<Optional<AcademicClass>> attendanceClassSupplier,
            Supplier<List<Attendance>> attendanceListSupplier) {
        this(personList, attendanceClassSupplier, attendanceListSupplier, person -> personList.indexOf(person) + 1);
    }

    /** Creates a preview list that preserves the actual command indices for its student cards. */
    public PersonListPanel(ObservableList<Person> personList,
            Supplier<Optional<AcademicClass>> attendanceClassSupplier,
            Supplier<List<Attendance>> attendanceListSupplier, ToIntFunction<Person> displayedIndex) {
        super(FXML);
        this.attendanceClassSupplier = attendanceClassSupplier;
        this.attendanceListSupplier = attendanceListSupplier;
        this.displayedIndex = displayedIndex;
        personListView.setItems(personList);
        personListView.setPlaceholder(new Label("No students shown. Try list or add a student."));
        personList.addListener((ListChangeListener<Person>) change -> {
            logger.fine("Refreshing person cards after the displayed person list changed.");
            personListView.refresh();
        });
        personListView.setCellFactory(listView -> new PersonListViewCell());
    }

    /** Updates visible attendance when the inspected class changes. */
    public void refresh() {
        personListView.refresh();
    }

    /**
     * Custom {@code ListCell} that displays the graphics of a {@code Person} using a {@code PersonCard}.
     */
    class PersonListViewCell extends ListCell<Person> {
        @Override
        protected void updateItem(Person person, boolean empty) {
            super.updateItem(person, empty);

            if (empty || person == null) {
                setGraphic(null);
                setText(null);
            } else {
                setGraphic(new PersonCard(person, displayedIndex.applyAsInt(person),
                        attendanceClassSupplier.get().orElse(null), attendanceListSupplier.get()).getRoot());
            }
        }
    }

}
