package seedu.address.ui;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * The UI component that is responsible for receiving user command inputs.
 */
public class CommandBox extends UiPart<Region> {

    public static final String ERROR_STYLE_CLASS = "error";
    private static final String FXML = "CommandBox.fxml";

    private final CommandExecutor commandExecutor;
    private final CommandHistory commandHistory = new CommandHistory();
    private String previewWarning = "";

    @FXML
    private TextField commandTextField;

    @FXML
    private Label commandHint;

    /**
     * Creates a {@code CommandBox} with the given {@code CommandExecutor}.
     */
    public CommandBox(CommandExecutor commandExecutor) {
        super(FXML);
        this.commandExecutor = commandExecutor;
        // calls #setStyleToDefault() whenever there is a change to the text of the command box.
        commandTextField.textProperty().addListener((observable, previous, current) -> {
            setStyleToDefault();
            updateHint();
        });
        commandTextField.addEventFilter(KeyEvent.KEY_PRESSED, this::handleHistoryNavigation);
        updateHint();
    }

    public ReadOnlyStringProperty textProperty() {
        return commandTextField.textProperty();
    }

    /** Shows any read-only preview warning beside the command's syntax guidance. */
    public void setPreviewWarning(String warning) {
        previewWarning = warning;
        updateHint();
    }

    private void updateHint() {
        commandHint.getStyleClass().remove("preview-warning");
        if (commandTextField.getText().isBlank()) {
            commandHint.setText("Type listbyclass 1 to preview a class. Press Enter to run a command.");
        } else if (!previewWarning.isEmpty()) {
            commandHint.setText(previewWarning + " Press Enter to run; typing only previews.");
            commandHint.getStyleClass().add("preview-warning");
        } else {
            commandHint.setText(new CommandPreview(commandTextField.getText()).getSyntax()
                    + "  ·  Press Enter to run");
        }
    }

    /**
     * Recalls commands without executing them when unmodified Up or Down is pressed.
     */
    private void handleHistoryNavigation(KeyEvent event) {
        if (event.getCode() != KeyCode.UP && event.getCode() != KeyCode.DOWN) {
            return;
        }
        if (event.isShiftDown() || event.isControlDown() || event.isAltDown() || event.isMetaDown()) {
            return;
        }
        String currentInput = commandTextField.getText();
        String recalledInput = event.getCode() == KeyCode.UP
                ? commandHistory.previous(currentInput) : commandHistory.next(currentInput);
        if (!recalledInput.equals(currentInput)) {
            commandTextField.setText(recalledInput);
            commandTextField.positionCaret(recalledInput.length());
        }
        event.consume();
    }

    /**
     * Handles the Enter button pressed event.
     */
    @FXML
    private void handleCommandEntered() {
        String commandText = commandTextField.getText();
        if (commandText.isBlank()) {
            return;
        }

        try {
            commandExecutor.execute(commandText);
            commandHistory.add(commandText);
            commandTextField.setText("");
        } catch (CommandException | ParseException e) {
            commandHistory.resetNavigation(commandText);
            setStyleToIndicateCommandFailure();
        }
    }

    /**
     * Sets the command box style to use the default style.
     */
    private void setStyleToDefault() {
        commandTextField.getStyleClass().remove(ERROR_STYLE_CLASS);
    }

    /**
     * Sets the command box style to indicate a failed command.
     */
    private void setStyleToIndicateCommandFailure() {
        ObservableList<String> styleClass = commandTextField.getStyleClass();

        if (styleClass.contains(ERROR_STYLE_CLASS)) {
            return;
        }

        styleClass.add(ERROR_STYLE_CLASS);
    }

    /**
     * Represents a function that can execute commands.
     */
    @FunctionalInterface
    public interface CommandExecutor {
        /**
         * Executes the command and returns the result.
         *
         * @see seedu.address.logic.Logic#execute(String)
         */
        CommandResult execute(String commandText) throws CommandException, ParseException;
    }

}
