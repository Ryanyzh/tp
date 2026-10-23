package seedu.address.ui;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** Compact, wrapping month rows containing only recorded attendance dates. */
public class AttendanceStrip extends VBox {
    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH);

    /** Displays the total and all recorded dates, with the latest month first. */
    public AttendanceStrip(Set<LocalDate> dates) {
        super(7);
        getStyleClass().add("attendance-strip");
        Label total = new Label(dates.isEmpty() ? "No attendance recorded"
                : "Attendance · " + dates.size() + (dates.size() == 1 ? " day" : " days"));
        total.setId("attendance");
        total.getStyleClass().addAll("cell_small_label", "attendance-total");
        total.setWrapText(true);
        getChildren().add(total);
        Map<YearMonth, Set<LocalDate>> months = new TreeMap<>(Comparator.reverseOrder());
        dates.forEach(date -> months.computeIfAbsent(YearMonth.from(date), month -> new TreeSet<>()).add(date));
        months.forEach((month, recordedDates) -> {
            Label monthLabel = new Label(month.format(MONTH_FORMAT));
            monthLabel.getStyleClass().add("attendance-month");
            monthLabel.setMinWidth(76);
            FlowPane days = new FlowPane(5, 5);
            days.getStyleClass().add("attendance-days");
            days.setMinWidth(0);
            days.setPrefWrapLength(240);
            HBox.setHgrow(days, Priority.ALWAYS);
            for (LocalDate date : recordedDates) {
                Label day = new Label(String.format(Locale.ENGLISH, "%02d", date.getDayOfMonth()));
                day.getStyleClass().add("day-chip");
                day.setTooltip(new Tooltip(date + " · Attendance recorded"));
                day.setAccessibleText("Attendance recorded on " + date);
                days.getChildren().add(day);
            }
            HBox row = new HBox(10, monthLabel, days);
            row.getStyleClass().add("attendance-month-row");
            getChildren().add(row);
        });
    }
}
