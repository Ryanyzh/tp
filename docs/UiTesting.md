# UI and command testing checklist

## Get the branch from Ryan's fork

Run these from an existing clone of the team project (Java 25 is required):

```bash
git fetch https://github.com/Ryanyzh/tp.git refresh-ui
git switch -c refresh-ui FETCH_HEAD
```

If you already have the local branch, use this after the fetch instead:

```bash
git switch refresh-ui
git merge --ff-only FETCH_HEAD
```

## Launch an isolated demo

From the project terminal, run:

```bash
./gradlew -I config/ui-demo.gradle run
```

This instance uses `_temp/ui-manual-testing/session/` for its data and preferences.
Your regular application's data stays in its usual location. Close any other
TAssist window first so you can identify the demo window easily.

Enter the commands below **one line at a time in the application's command box**.
The initial `clear` deletes the isolated demo's data, including any previous demo run.

## 1. Populate students and classes

```text
clear
addclass m/CS2103 c/F10
addclass m/CS2103 c/F11
addclass m/CS2101 c/TUTORIAL-GROUP-WITH-A-LONG-NAME
add sn/DEMO0001 n/Alice Tan e/alice@example.com tg/alice_demo t/tutorial
add sn/DEMO0002 n/Ben Lim e/ben@example.com tg/ben_demo t/project
add sn/DEMO0003 n/Cara Ng e/cara@example.com t/tutorial
assign sn/DEMO0001 m/CS2103 c/F10
assign sn/DEMO0002 m/CS2103 c/F10
assign sn/DEMO0003 m/CS2103 c/F11
assign sn/DEMO0001 m/CS2101 c/TUTORIAL-GROUP-WITH-A-LONG-NAME
```

Check:

- Class header: **3 shown / 3 total**. Student header: **3 shown**.
- Class memberships: F10 has 2 students, F11 has 1, and the long-name class has 1.
- The long class name wraps fully. Alice and Ben have Telegram handles; Cara does not.
- Resize the window and drag the divider. Student details and attendance chips should wrap.

## 2. Exercise filters and header counts

```text
listbystudent 1
listclass
listbyclass 1
list
find Alice
list
```

Expected checkpoints, in order:

- `listbystudent 1`: **2 shown / 3 total** classes for Alice.
- `listclass`: **3 shown / 3 total** classes again.
- `listbyclass 1`: **2 shown** students, Alice and Ben.
- `list`: **3 shown** students.
- `find Alice`: **1 shown** student.
- Final `list`: **3 shown** students.

Also test the read-only UI previews:

1. With all students displayed, type `listbyclass 2` without pressing Enter.
   The right panel previews Cara with
   her command index **3**, and its header says **1 shown**.
2. Replace the draft with `listbyclass 1`. The preview shows Alice and Ben, with **2 shown**.
3. Type `find Cara` without pressing Enter. The count should become **1 shown**.
4. Replace it with `find Nobody` without pressing Enter: **0 shown**.
5. Clear the input. Previewing should not have changed saved data or model filters.
6. Run `list` to return to the full list before continuing.

Draft class commands and searches preview the **current command student list**.
Run `list` first when you want to preview across all students. Class rows are
read-only; commands execute only when you press Enter in the command box.

## 3. Build attendance strips across two months

```text
listclass
listbyclass 1
addattendance 1 1 d/2026-09-01
addattendance 1 1 d/2026-09-02
addattendance 1 1 d/2026-09-03
addattendance 1 1 d/2026-09-04
addattendance 1 1 d/2026-09-05
addattendance 1 1 d/2026-09-06
addattendance 1 1 d/2026-09-07
addattendance 1 1 d/2026-09-08
addattendance 1 1 d/2026-09-09
addattendance 1 1 d/2026-09-10
addattendance 1 1 d/2026-10-01
addattendance 1 1 d/2026-10-02
addattendance 2 1 d/2026-09-01
addattendance 2 1 d/2026-09-03
```

- Alice: **Attendance · 12 days**, with October 2026 above September 2026.
- Ben: **Attendance · 2 days**, with September chips `01` and `03`.
- The student header remains **2 shown**; attendance totals count dates, not students.
- Hover over a chip to see the complete date.
- Make the window narrower: the September chips should wrap without disappearing.

Remove one date:

```text
deleteattendance 1 1 d/2026-09-10
```

Alice should now have **11 days** and no September `10` chip.

## 4. Invalid commands and error styling

These commands are expected to fail. Saved students, classes, and attendance
should remain unchanged. The shown-student count may change with the draft
preview. The command stays in the input box with error styling; editing the input
removes that styling.

```text
addattendance 1 1 d/2026-09-01
deleteattendance 1 1 d/2026-08-01
addattendance 1 1 d/2099-01-01
addattendance 1 1 d/2026-02-30
addattendance 9999 1 d/2026-09-01
addattendance 1 9999 d/2026-09-01
addattendance 1 2 d/2026-09-01
addclass m/CS2103 c/F10
add sn/DEMO0004 n/Invalid Student e/not-an-email
delete 9999
assign sn/UNKNOWN m/CS2103 c/F10
listclass extra
unknown
```

These cover duplicate attendance, missing attendance, future and malformed dates,
invalid indices, a student not assigned to the selected class, duplicate classes,
invalid email, missing student, unexpected arguments, and an unknown command.

## 5. Edit details while preserving membership and history

```text
list
find Alice
edit 1 n/Alice Tan Updated e/alice.updated@example.com tg/ t/project
list
listclass
editclass 1 c/F10-UPDATED
listbyclass 1
```

- Alice's name, email, and tags update; her Telegram handle disappears.
- The class becomes F10-UPDATED and keeps both members.
- Attendance history stays attached: Alice has 11 dates and Ben has 2.

Check Cara's classes, then restore the full class list:

```text
list
listbystudent 3
listclass
```

The class count should change to **1 shown / 3 total**, then back to **3 shown / 3 total**.

## 6. Unassign, correct retained history, and reassign

```text
unassign sn/DEMO0002 m/CS2103 c/F10-UPDATED
listbyclass 1
list
deleteattendance 2 1 d/2026-09-01
assign sn/DEMO0002 m/CS2103 c/F10-UPDATED
listbyclass 1
```

- After unassigning Ben, F10-UPDATED has 1 member and its roster shows 1 student.
- `list` restores Ben's command index to 2 so his retained history can be corrected.
- Reassigning Ben restores a 2-student roster. His remaining attendance is
  **1 day**, September `03`. Alice still has 11 days.

## 7. Delete records and check persistence

```text
list
listclass
delete 3
deleteclass m/CS2103 c/F11
list
listclass
help
```

- Cara is deleted: **2 shown** students.
- F11 is deleted: **2 shown / 2 total** classes.
- Help opens with the matching colour theme. Test Copy URL, then close Help.

Try keyboard behaviour before exiting:

- Press Up/Down to recall commands and recover an unfinished draft.
- Recalled commands should execute only after Enter.
- Modified arrow keys should retain their normal editing behaviour.
- A blank command should do nothing. F1 should open Help.
- The command input and output remain above the dynamic lower panels.

```text
exit
```

Relaunch with the same terminal command:

```bash
./gradlew -I config/ui-demo.gradle run
```

Expect 2 students and 2 classes. Run `listbyclass 1` to confirm that Alice's
11 attendance dates and Ben's 1 date were saved.

For a final empty-state check, run `clear` **in this isolated demo instance**.
Both headers should become zero and the lists should show their empty states.

## Automated checks and normal launch

Run the project's automated tests and Checkstyle from the project terminal:

```bash
./gradlew check
```

To launch normally with your regular saved data:

```bash
./gradlew run
```
