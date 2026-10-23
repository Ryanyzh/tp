package seedu.address.ui;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Predicate;

import javafx.beans.binding.Bindings;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.logic.Logic;
import seedu.address.logic.parser.ArgumentMultimap;
import seedu.address.logic.parser.ArgumentTokenizer;
import seedu.address.logic.parser.CliSyntax;
import seedu.address.model.academicclass.AcademicClass;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Person;

/** Bottom-right student and attendance view. Drafts only affect this preview, never model filters. */
public class DynamicContentPanel extends VBox {
    private final Logic logic;
    private final Label title = new Label("Students");
    private final Label studentCount = new Label();
    private final FilteredList<Person> previewStudents;
    private final PersonListPanel personListPanel;
    private AcademicClass commandClass;
    private AcademicClass attendanceClass;
    private String draft = "";
    private String previewWarning = "";

    /** Creates a dynamic view that preserves command indices when showing a subset of students. */
    public DynamicContentPanel(Logic logic) {
        super(8);
        this.logic = logic;
        getStyleClass().add("student-workspace");
        title.setId("contentTitle");
        title.getStyleClass().add("section-title");
        title.setWrapText(true);
        title.setMinWidth(0);
        studentCount.setId("shownStudentCount");
        studentCount.getStyleClass().add("count-badge");
        studentCount.setMinWidth(USE_PREF_SIZE);
        studentCount.setTooltip(new Tooltip("Students currently shown, including any search or class preview."));
        previewStudents = new FilteredList<>(logic.getFilteredPersonList());
        studentCount.textProperty().bind(Bindings.size(previewStudents).asString("%d shown"));
        personListPanel = new PersonListPanel(previewStudents, () -> Optional.ofNullable(attendanceClass),
                logic::getAttendanceList, person -> logic.getFilteredPersonList().indexOf(person) + 1);
        VBox.setVgrow(personListPanel.getRoot(), Priority.ALWAYS);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox header = new HBox(8, title, spacer, studentCount);
        header.setAlignment(Pos.CENTER_LEFT);
        getChildren().addAll(header, personListPanel.getRoot());
        refresh();
    }

    public PersonListPanel getPersonListPanel() {
        return personListPanel;
    }

    public String getPreviewWarning() {
        return previewWarning;
    }

    /** Changes the view while leaving saved data and command indices unchanged. */
    public void updateDraft(String draft) {
        this.draft = draft;
        refresh();
    }

    /** Returns to the resulting model view after a successful command. */
    public void commandExecuted(String commandText) {
        draft = commandText;
        commandClass = resolveClass(new CommandPreview(commandText));
        if (List.of("list", "find", "listbystudent", "clear").contains(new CommandPreview(commandText).getCommand())) {
            commandClass = null;
        }
        draft = "";
        refresh();
    }

    /** Refreshes the visible cards and guidance from current saved state. */
    public void refresh() {
        CommandPreview preview = new CommandPreview(draft);
        AcademicClass target = resolveClass(preview);
        attendanceClass = target == null && draft.isBlank()
                ? logic.getAttendanceDisplayClass().orElse(null) : target;
        Predicate<Person> filter = person -> true;
        String warning = "";
        if (attendanceClass != null) {
            filter = person -> attendanceClass.getStudents().contains(person);
        }
        OptionalInt classIndex = preview.getClassIndex();
        boolean invalidClass = classIndex.isPresent()
                && classIndex.getAsInt() >= logic.getFilteredAcademicClassList().size();
        if (invalidClass) {
            warning = "Class index is outside the displayed class list.";
            filter = person -> false;
        }
        OptionalInt studentIndex = preview.getStudentIndex();
        if (studentIndex.isPresent()) {
            if (studentIndex.getAsInt() < logic.getFilteredPersonList().size()) {
                Person student = logic.getFilteredPersonList().get(studentIndex.getAsInt());
                // Retained attendance is inspectable even after the student leaves this class.
                filter = person -> person.equals(student);
                if (attendanceClass != null && !attendanceClass.getStudents().contains(student)) {
                    warning = "This student is not currently assigned to the class. Saved history is shown.";
                }
            } else {
                warning += (warning.isEmpty() ? "" : " ") + "Student index is outside the displayed student list.";
                filter = person -> false;
            }
        }
        if (preview.getCommand().equals("find")) {
            String keywords = draft.strip().substring("find".length()).strip();
            if (!keywords.isEmpty()) {
                filter = new NameContainsKeywordsPredicate(Arrays.asList(keywords.split("\\s+")));
                attendanceClass = null;
            }
        }
        if (invalidClass) {
            filter = person -> false;
        }
        previewStudents.setPredicate(filter);
        title.setText(attendanceClass == null ? "Students"
                : attendanceClass.getModuleCode() + " · " + attendanceClass.getClassName());
        previewWarning = warning;
        personListPanel.refresh();
    }

    private AcademicClass resolveClass(CommandPreview preview) {
        OptionalInt index = preview.getClassIndex();
        if (index.isPresent()) {
            return index.getAsInt() < logic.getFilteredAcademicClassList().size()
                    ? logic.getFilteredAcademicClassList().get(index.getAsInt()) : null;
        }
        if (List.of("assign", "unassign", "deleteclass").contains(preview.getCommand())) {
            ArgumentMultimap arguments = ArgumentTokenizer.tokenize(draft,
                    CliSyntax.PREFIX_MODULE_CODE, CliSyntax.PREFIX_CLASS_NAME, CliSyntax.PREFIX_STUDENT_NUMBER);
            String module = arguments.getValue(CliSyntax.PREFIX_MODULE_CODE).orElse("");
            String name = arguments.getValue(CliSyntax.PREFIX_CLASS_NAME).orElse("");
            if (!module.isEmpty() && !name.isEmpty()) {
                return logic.getAcademicClassList().stream().filter(item ->
                        item.getModuleCode().value.equalsIgnoreCase(module)
                                && item.getClassName().value.equalsIgnoreCase(name)).findFirst().orElse(null);
            }
        }
        if (List.of("list", "find", "clear", "add", "addclass", "listbystudent").contains(preview.getCommand())) {
            return null;
        }
        return commandClass == null ? null : logic.getAcademicClassList().stream()
                .filter(item -> item.isSameClass(commandClass)).findFirst().orElse(null);
    }
}
