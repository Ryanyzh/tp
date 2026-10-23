package seedu.address.ui;

import java.util.List;
import java.util.OptionalInt;

/** Read-only guidance for incomplete commands. Never parses or executes a model command. */
public class CommandPreview {
    private static final List<String> SYNTAX = List.of(
            "add sn/STUDENT_NUMBER n/NAME e/EMAIL [tg/TELEGRAM] [t/TAG]...",
            "edit STUDENT_INDEX [sn/STUDENT_NUMBER] [n/NAME] [e/EMAIL] [tg/TELEGRAM] [t/TAG]...",
            "delete STUDENT_INDEX", "find NAME [MORE_NAMES]...", "list",
            "addclass m/MODULE_CODE c/CLASS_NAME",
            "editclass CLASS_INDEX [m/MODULE_CODE] [c/CLASS_NAME]",
            "deleteclass m/MODULE_CODE c/CLASS_NAME", "listclass",
            "listbyclass CLASS_INDEX", "listbystudent STUDENT_INDEX",
            "assign sn/STUDENT_NUMBER m/MODULE_CODE c/CLASS_NAME",
            "unassign sn/STUDENT_NUMBER m/MODULE_CODE c/CLASS_NAME",
            "addattendance STUDENT_INDEX CLASS_INDEX d/YYYY-MM-DD",
            "deleteattendance STUDENT_INDEX CLASS_INDEX d/YYYY-MM-DD",
            "help", "clear", "exit");

    private final String[] tokens;

    public CommandPreview(String draft) {
        tokens = draft.strip().split("\\s+");
    }

    public String getCommand() {
        return tokens[0];
    }

    public String getSyntax() {
        return SYNTAX.stream().filter(syntax -> syntax.split(" ")[0].equals(getCommand()))
                .findFirst().orElseGet(() -> {
                    String matches = SYNTAX.stream().map(syntax -> syntax.split(" ")[0])
                            .filter(command -> command.startsWith(getCommand()))
                            .reduce((first, next) -> first + " · " + next).orElse("Type help to see all commands");
                    return getCommand().isEmpty() ? "listbyclass 1" : matches;
                });
    }

    public String getTitle() {
        if (getCommand().isEmpty()) {
            return "A little context, as you type";
        }
        if (isAttendance()) {
            return getCommand().equals("addattendance") ? "Record a date" : "Correct a record";
        }
        return "Exploring · " + getCommand();
    }

    public String getDetail() {
        if (isAttendance()) {
            return "Student index comes first, then class index. The preview shows saved records only. "
                    + "Use a date in YYYY-MM-DD format.";
        }
        if (getCommand().equals("clear")) {
            return "Careful: pressing Enter will clear the address book.";
        }
        if (getCommand().equals("listbyclass")) {
            return "Preview a class by its displayed index. Enter will filter the student list to its members.";
        }
        if (getCommand().equals("assign") || getCommand().equals("unassign")) {
            return "Use the student's number and the class identity. Saved memberships change only after Enter.";
        }
        return "Type listbyclass CLASS_INDEX to preview its students. Drafts never change your data. "
                + "Press Enter when your command is ready.";
    }

    public OptionalInt getClassIndex() {
        if (isAttendance()) {
            return indexAt(2);
        }
        if (getCommand().equals("listbyclass") || getCommand().equals("editclass")) {
            return indexAt(1);
        }
        return OptionalInt.empty();
    }

    public OptionalInt getStudentIndex() {
        if (isAttendance() || List.of("edit", "delete", "listbystudent").contains(getCommand())) {
            return indexAt(1);
        }
        return OptionalInt.empty();
    }

    private boolean isAttendance() {
        return getCommand().equals("addattendance") || getCommand().equals("deleteattendance");
    }

    private OptionalInt indexAt(int position) {
        if (position >= tokens.length || !tokens[position].matches("[0-9]+")) {
            return OptionalInt.empty();
        }
        try {
            int index = Integer.parseInt(tokens[position]);
            return index > 0 ? OptionalInt.of(index - 1) : OptionalInt.empty();
        } catch (NumberFormatException e) {
            return OptionalInt.empty();
        }
    }
}
