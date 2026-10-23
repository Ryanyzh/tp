package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.Set;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;

public class AttendanceStripTest {
    @BeforeAll
    public static void initializeToolkit() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Another test has started JavaFX.
        }
    }

    @Test
    public void dates_groupByMonthAndYearAndKeepFullDatesAccessible() throws Exception {
        FutureTask<Void> task = new FutureTask<>(() -> {
            AttendanceStrip strip = new AttendanceStrip(Set.of(LocalDate.of(2024, 9, 2),
                    LocalDate.of(2024, 9, 1), LocalDate.of(2025, 9, 1)));
            assertEquals("Attendance · 3 days", ((Label) strip.getChildren().getFirst()).getText());
            HBox latest = (HBox) strip.getChildren().get(1);
            assertEquals("Sep 2025", ((Label) latest.getChildren().getFirst()).getText());
            HBox older = (HBox) strip.getChildren().get(2);
            assertEquals("Sep 2024", ((Label) older.getChildren().getFirst()).getText());
            FlowPane days = (FlowPane) older.getChildren().get(1);
            assertEquals("01", ((Label) days.getChildren().getFirst()).getText());
            assertEquals("02", ((Label) days.getChildren().get(1)).getText());
            Label day = (Label) days.getChildren().getFirst();
            assertTrue(day.getTooltip().getText().contains("2024-09-01"));
            assertTrue(day.getAccessibleText().contains("2024-09-01"));
            return null;
        });
        Platform.runLater(task);
        task.get(10, TimeUnit.SECONDS);
    }
}
