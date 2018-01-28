package animalkeeping.ui.forms;

import animalkeeping.logging.Communicator;
import animalkeeping.model.Housing;
import animalkeeping.model.HousingUnit;
import animalkeeping.model.Subject;
import animalkeeping.ui.Main;
import animalkeeping.ui.widgets.HousingDropDown;
import animalkeeping.ui.widgets.SpecialTextField;
import animalkeeping.util.DateTimeHelper;
import animalkeeping.util.EntityHelper;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

/**
 * Created by jan on 19.04.17.
 */
public class HousingForm extends VBox {
    private DatePicker startDate, endDate;
    private SpecialTextField startTimeField, endTimeField;
    private TextArea commentArea;
    private ComboBox<Subject> subjectCombo;
    private HousingDropDown unitCombo;
    private Housing housing = null;

    public HousingForm() {
        this.setFillWidth(true);
        this.init();
    }

    public HousingForm(Housing h) {
        this();
        setHousing(h);
    }

    private  void init() {
        Label idLabel = new Label();

        startDate = new DatePicker();
        endDate = new DatePicker();

        startTimeField = new SpecialTextField("##:##:##");
        endTimeField = new SpecialTextField("##:##:##");

        subjectCombo = new ComboBox<>();
        subjectCombo.setConverter(new StringConverter<Subject>() {
            @Override
            public String toString(Subject object) {
                return object.getName();
            }

            @Override
            public Subject fromString(String string) {
                return null;
            }
        });
        unitCombo = new HousingDropDown();
        commentArea = new TextArea();

        Button newPersonButton = new Button("+");
        newPersonButton.setTooltip(new Tooltip("create a new person entry"));
        newPersonButton.setDisable(true);
        Button newPersonButton2 = new Button("+");
        newPersonButton2.setTooltip(new Tooltip("create a new person entry"));
        newPersonButton2.setDisable(true);

        GridPane grid = new GridPane();
        ColumnConstraints column1 = new ColumnConstraints(100,100, Double.MAX_VALUE);
        column1.setHgrow(Priority.NEVER);
        ColumnConstraints column2 = new ColumnConstraints(100, 150, Double.MAX_VALUE);
        column2.setHgrow(Priority.ALWAYS);
        ColumnConstraints column3 = new ColumnConstraints(30, 30, Double.MAX_VALUE);
        column3.setHgrow(Priority.NEVER);
        grid.getColumnConstraints().addAll(column1, column2, column3);

        subjectCombo.prefWidthProperty().bind(column2.maxWidthProperty());
        unitCombo.prefWidthProperty().bind(column2.maxWidthProperty());
        startDate.prefWidthProperty().bind(column2.maxWidthProperty());
        endDate.prefWidthProperty().bind(column2.maxWidthProperty());
        startTimeField.prefWidthProperty().bind(column2.maxWidthProperty());
        endTimeField.prefWidthProperty().bind(column2.maxWidthProperty());
        commentArea.prefWidthProperty().bind(column2.maxWidthProperty());

        grid.setVgap(5);
        grid.setHgap(2);
        grid.add(new Label("ID:"), 0, 0);
        grid.add(idLabel, 1, 0);

        grid.add(new Label("subject:"), 0,1);
        grid.add(subjectCombo, 1,1, 2, 1);

        grid.add(new Label("housing unit:"), 0, 2);
        grid.add(unitCombo, 1, 2, 2,1);

        grid.add(new Label("from date:"), 0, 3);
        grid.add(startDate, 1, 3, 2, 1);

        grid.add(new Label("from time:"), 0, 4);
        grid.add(startTimeField, 1, 4, 2, 1 );

        grid.add(new Label("end date:"), 0, 5);
        grid.add(endDate, 1, 5, 2, 1 );
        //grid.add(newPersonButton2, 2, 5, 1, 1);

        grid.add(new Label("end time:"), 0, 6);
        grid.add(endTimeField, 1, 6, 2,1);

        grid.add(new Label("comment:"), 0, 7);
        grid.add(commentArea, 0, 8, 3, 4);
        this.getChildren().add(grid);

        List<Subject> subjects;
        if (Main.getSettings().getBoolean("app_settings_availableSubjectsSelection", true)) {
            subjects = EntityHelper.getEntityList("SELECT s FROM Subject s, Housing h WHERE h.subject = s and h.end IS NULL order by s.name asc", Subject.class);
        } else {
            subjects = EntityHelper.getEntityList("FROM Subject order by name asc", Subject.class);
        }
        subjectCombo.getItems().addAll(subjects);
    }

    private void setHousing(Housing h) {
        this.housing = h;
        if (h == null)
            return;

        if (subjectCombo.getItems().contains(h.getSubject()))
            subjectCombo.getSelectionModel().select(h.getSubject());
        else {
            subjectCombo.getItems().clear();
            subjectCombo.getItems().add(h.getSubject());
            subjectCombo.getSelectionModel().select(0);
        }
        unitCombo.setHousingUnit(h.getHousing());
        startDate.setValue(DateTimeHelper.toLocalDate(h.getStart()));
        DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        startTimeField.setText(timeFormat.format(h.getStart()));
        endDate.setValue(h.getEnd() != null ? DateTimeHelper.toLocalDate(h.getEnd()) : null);
        endTimeField.setText(h.getEnd() != null ? timeFormat.format(h.getEnd()) : "");
        commentArea.setText(h.getComment());
    }

    public Housing persistHousing() {
        if (this.housing == null) {
            this.housing = new Housing();
        }
        housing.setSubject(subjectCombo.getValue());
        housing.setHousing(unitCombo.getHousingUnit());
        housing.setStart(DateTimeHelper.getDateTime(startDate.getValue(), startTimeField.getText()));
        housing.setComment(commentArea.getText());
        if (endDate.getValue() != null) {
            housing.setEnd(DateTimeHelper.getDateTime(endDate.getValue(), endTimeField.getText()));
        }
        if (Communicator.pushSaveOrUpdate(housing)) {
            return housing;
        }
        return null;
    }

    public boolean validate(Vector<String> messages) {
        boolean valid = true;
        if (subjectCombo.getValue() == null) {
            messages.add("Subject can not be empty!");
            valid = false;
        }
        if (endDate.getValue() != null && endDate.getValue().isBefore(startDate.getValue())) {
            messages.add("End date can not be before start date!");
            valid = false;
        }
        if (!unitCombo.getHousingUnit().getHousingType().getCanHoldSubjects()) {
            messages.add("Selected housing unit can not hold subjects!");
            valid = false;
        }
        if (endDate.getValue() != null && endDate.getValue().isEqual(startDate.getValue())) {
            if (!endTimeField.getText().isEmpty()) {
                SimpleDateFormat tf = new SimpleDateFormat("hh:mm:ss");
                try {
                    Date d = tf.parse(endTimeField.getText());
                    Date d2 = tf.parse(startTimeField.getText());
                    if (d.before(d2)) {
                        messages.add("End time can not be before start time!");
                    }
                } catch (Exception ee) {
                    ee.printStackTrace();
                }
            }
        }
        return valid;
    }
}
