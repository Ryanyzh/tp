package seedu.address.ui;

import java.util.logging.Logger;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.academicclass.AcademicClass;

/**
 * Panel containing the list of academic classes.
 */
public class AcademicClassListPanel extends UiPart<Region> {
    private static final String FXML = "AcademicClassListPanel.fxml";
    private final Logger logger = LogsCenter.getLogger(AcademicClassListPanel.class);

    @FXML
    private ListView<AcademicClass> academicClassListView;

    /**
     * Creates an {@code AcademicClassListPanel} with the given {@code ObservableList}.
     */
    public AcademicClassListPanel(ObservableList<AcademicClass> academicClassList) {
        super(FXML);
        academicClassListView.setItems(academicClassList);
        academicClassListView.setCellFactory(listView -> new AcademicClassListViewCell());
        academicClassListView.setPlaceholder(new Label("No classes yet. Try addclass m/CS2103 c/F10"));
    }

    public ReadOnlyObjectProperty<AcademicClass> selectedClassProperty() {
        return academicClassListView.getSelectionModel().selectedItemProperty();
    }

    /** Refreshes memberships after a command without changing the displayed class indices. */
    public void refresh() {
        academicClassListView.refresh();
    }

    /** Selects and reveals the displayed class targeted by a successful command. */
    public void selectClass(int displayedIndex) {
        academicClassListView.getSelectionModel().select(displayedIndex);
        academicClassListView.scrollTo(displayedIndex);
    }

    /**
     * Custom {@code ListCell} that displays the graphics of an {@code AcademicClass}
     * using an {@code AcademicClassCard}.
     */
    class AcademicClassListViewCell extends ListCell<AcademicClass> {
        AcademicClassListViewCell() {
            addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
                if (!isEmpty()) {
                    academicClassListView.getSelectionModel().select(getIndex());
                }
            });
        }

        @Override
        protected void updateItem(AcademicClass academicClass, boolean empty) {
            super.updateItem(academicClass, empty);

            if (empty || academicClass == null) {
                setGraphic(null);
                setText(null);
            } else {
                setGraphic(new AcademicClassCard(academicClass, getIndex() + 1).getRoot());
            }
        }
    }
}
