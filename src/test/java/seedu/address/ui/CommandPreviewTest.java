package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class CommandPreviewTest {
    @Test
    public void attendance_indices_followSeparateDisplayedLists() {
        CommandPreview preview = new CommandPreview("  addattendance 2 3 d/2024-02-01");
        assertEquals(1, preview.getStudentIndex().orElseThrow());
        assertEquals(2, preview.getClassIndex().orElseThrow());
        assertTrue(preview.getSyntax().contains("STUDENT_INDEX CLASS_INDEX"));
    }

    @Test
    public void incompleteOrInvalidIndices_doNotCrashOrSelectAnEntity() {
        for (String index : new String[] {"", "-1", "0", "1x", "99999999999999999999999"}) {
            CommandPreview preview = new CommandPreview("addattendance " + index);
            assertTrue(preview.getStudentIndex().isEmpty());
            assertTrue(preview.getClassIndex().isEmpty());
        }
        assertTrue(new CommandPreview("listbyclass").getClassIndex().isEmpty());
    }

    @Test
    public void classAndStudentCommands_doNotConfuseIndexTypes() {
        assertEquals(1, new CommandPreview("listbyclass 2").getClassIndex().orElseThrow());
        assertTrue(new CommandPreview("listbyclass 2").getStudentIndex().isEmpty());
        assertEquals(2, new CommandPreview("listbystudent 3").getStudentIndex().orElseThrow());
        assertTrue(new CommandPreview("listbystudent 3").getClassIndex().isEmpty());
        assertTrue(new CommandPreview("deleteclass m/CS2103 c/F10").getClassIndex().isEmpty());
    }

    @Test
    public void partialAndUnknownCommands_provideGuidance() {
        assertTrue(new CommandPreview("addatt").getSyntax().contains("addattendance"));
        assertFalse(new CommandPreview("").getTitle().isEmpty());
        assertEquals("Type help to see all commands", new CommandPreview("unknown").getSyntax());
    }
}
