package animalkeeping.ui.forms;

import animalkeeping.logging.Communicator;
import animalkeeping.model.*;
import animalkeeping.ui.Main;
import animalkeeping.ui.widgets.HousingDropDown;
import animalkeeping.ui.widgets.SpecialTextField;
import animalkeeping.util.DateTimeHelper;
import animalkeeping.util.EntityHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.util.StringConverter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.prefs.Preferences;

import static animalkeeping.util.Dialogs.editHousingUnitDialog;

/******************************************************************************
 animalBase
 animalkeeping.ui.forms

 Copyright (c) 2017 Neuroethology Lab, University of Tuebingen,
 Jan Grewe <jan.grewe@g-node.org>,
 Dennis Huben <dennis.huben@rwth-aachen.de>

 All rights reserved.

 Redistribution and use in source and binary forms, with or without modification,
 are permitted provided that the following conditions are met:

 1. Redistributions of source code must retain the above copyright notice, this list
 of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice, this
 list of conditions and the following disclaimer in the documentation and/or other
 materials provided with the distribution.

 3. Neither the name of the copyright holder nor the names of its contributors may
 be used to endorse or promote products derived from this software without specific
 prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 DAMAGE.

 *****************************************************************************/


public class AddSubjectsForm extends VBox {
    private HousingDropDown housingUnitCombo;
    private ComboBox<Person> responsiblePersonCombo;
    private ComboBox<SubjectType> subjectTypeComboBox;
    private ComboBox<SpeciesType> speciesComboBox;
    private ComboBox<SupplierType> supplierComboBox;
    private DatePicker housingDate;
    private Spinner<Integer> startIdSpinner;
    private Spinner<Integer> countSpinner;
    private TextField nameField;
    private SpecialTextField timeField;
    private SubjectType st;
    private Preferences prefs;
    private Label previewLabel;
    private ListView<String> previewList;

    public AddSubjectsForm() {
        this(null);
    }


    public AddSubjectsForm(HousingUnit unit) {
        super();
        this.setFillWidth(true);
        init(unit);
        applyPreferences();
    }


    private void init(HousingUnit unit) {
        prefs = Preferences.userNodeForPackage(AddSubjectsForm.class);

        DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

        previewLabel = new Label();
        previewLabel.setStyle("-fx-text-fill: gray; -fx-font-size: 9;");

        housingUnitCombo = new HousingDropDown();
        housingUnitCombo.setHousingUnit(unit);
        supplierComboBox = new ComboBox<>();
        supplierComboBox.setConverter(new StringConverter<SupplierType>() {
            @Override
            public String toString(SupplierType object) {
                return object.getName();
            }

            @Override
            public SupplierType fromString(String string) {
                return null;
            }
        });
        speciesComboBox = new ComboBox<>();
        speciesComboBox.setConverter(new StringConverter<SpeciesType>() {
            @Override
            public String toString(SpeciesType object) {
                return object.getName();
            }

            @Override
            public SpeciesType fromString(String string) {
                return null;
            }
        });
        responsiblePersonCombo = new ComboBox<>();
        responsiblePersonCombo.setConverter(new StringConverter<Person>() {
            @Override
            public String toString(Person object) {
                if (object.getId() == null) {
                    return "None";
                }
                return (object.getFirstName() + " " + object.getLastName());
            }

            @Override
            public Person fromString(String string) {
                return null;
            }
        });
        responsiblePersonCombo.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.BACK_SPACE) {
               responsiblePersonCombo.getSelectionModel().select(null);
        }
        });
        subjectTypeComboBox = new ComboBox<>();
        subjectTypeComboBox.setConverter(new StringConverter<SubjectType>() {
            @Override
            public String toString(SubjectType object) {
                return object.getName();
            }

            @Override
            public SubjectType fromString(String string) {
                return null;
            }
        });
        housingDate = new DatePicker(LocalDate.now());
        startIdSpinner = new Spinner<>(0, 9999, 1);
        countSpinner = new Spinner<>(0, 9999, 10);
        timeField = new SpecialTextField("##:##:##");
        timeField.setTooltip(new Tooltip("Import time use HH:mm:ss format"));
        timeField.setText(timeFormat.format(new Date()));

        Button newHousingUnit = new Button("+");
        newHousingUnit.setTooltip(new Tooltip("create a new housing unit"));
        newHousingUnit.setOnAction(event -> createNewHousingButton());

        Button newSpecies = new Button("+");
        newSpecies.setTooltip(new Tooltip("create a new species entry"));
        newSpecies.setDisable(true);
        
        Button newSubjectType = new Button("+");
        newSubjectType.setTooltip(new Tooltip("create a new subject type entry"));
        newSubjectType.setDisable(true);

        Button newSupplier = new Button("+");
        newSupplier.setTooltip(new Tooltip("create a new supplier entry"));
        newSupplier.setDisable(true);

        Button newPerson = new Button("+");
        newPerson.setTooltip(new Tooltip("create a new person entry"));
        newPerson.setDisable(true);

        previewList = new ListView<>();
        previewList.setEditable(false);
        previewList.setPrefHeight(100);

        List<SupplierType> supplier = EntityHelper.getEntityList("from SupplierType order by name asc", SupplierType.class);
        List<SpeciesType> species = EntityHelper.getEntityList("from SpeciesType order by name asc", SpeciesType.class);
        List<SubjectType> subjectTypes = EntityHelper.getEntityList("from SubjectType order by name asc", SubjectType.class);
        List<Person> persons;
        if (Main.getSettings().getBoolean("app_settings_activePersonSelection", true)) {
            persons = EntityHelper.getEntityList("from Person where active = True order by lastName asc", Person.class);
        } else {
            persons = EntityHelper.getEntityList("from Person order by lastName asc", Person.class);
        }
        Person p = new Person();
        persons.add(p);

        subjectTypeComboBox.getItems().addAll(subjectTypes);
        supplierComboBox.getItems().addAll(supplier);
        speciesComboBox.getItems().addAll(species);
        responsiblePersonCombo.getItems().addAll(persons);
        nameField = new TextField();
        nameField.textProperty().addListener((observable, oldValue, newValue) -> createPreview());
        Label heading = new Label("Add subjects:");
        heading.setFont(new Font(Font.getDefault().getFamily(), 16));
        this.getChildren().add(heading);

        GridPane grid = new GridPane();
        ColumnConstraints column1 = new ColumnConstraints(105,105, Double.MAX_VALUE);
        column1.setHgrow(Priority.NEVER);
        ColumnConstraints column2 = new ColumnConstraints(100, 150, Double.MAX_VALUE);
        column2.setHgrow(Priority.ALWAYS);
        ColumnConstraints column3 = new ColumnConstraints(30, 30, Double.MAX_VALUE);
        column3.setHgrow(Priority.NEVER);
        grid.getColumnConstraints().addAll(column1, column2, column3);

        nameField.prefWidthProperty().bind(column2.maxWidthProperty());
        previewLabel.prefWidthProperty().bind(column2.maxWidthProperty());
        speciesComboBox.prefWidthProperty().bind(column2.maxWidthProperty());
        supplierComboBox.prefWidthProperty().bind(column2.maxWidthProperty());
        subjectTypeComboBox.prefWidthProperty().bind(column2.maxWidthProperty());
        housingUnitCombo.prefWidthProperty().bind(column2.maxWidthProperty());
        responsiblePersonCombo.prefWidthProperty().bind(column2.maxWidthProperty());
        timeField.prefWidthProperty().bind(column2.maxWidthProperty());
        housingDate.prefWidthProperty().bind(column2.maxWidthProperty());
        startIdSpinner.prefWidthProperty().bind(column2.maxWidthProperty());
        countSpinner.prefWidthProperty().bind(column2.maxWidthProperty());

        grid.setVgap(5);
        grid.setHgap(2);
        grid.add(new Label("name pattern(*):"), 0, 0);
        grid.add(nameField, 1, 0, 2, 1);

        grid.add(previewLabel, 1, 1);

        grid.add(new Label("housing unit(*):"), 0, 2);
        grid.add(housingUnitCombo, 1, 2, 1, 1 );
        grid.add(newHousingUnit, 2, 2, 1, 1);

        grid.add(new Label("resp. person:"), 0, 3);
        grid.add(responsiblePersonCombo, 1, 3, 1, 1 );
        grid.add(newPerson, 2, 3, 1, 1);

        grid.add(new Label("supplier(*):"), 0, 4);
        grid.add(supplierComboBox, 1, 4, 1,1);
        grid.add(newSupplier, 2, 4, 1, 1);

        grid.add(new Label("species(*):"), 0,5);
        grid.add(speciesComboBox, 1,5, 1, 1);
        grid.add(newSpecies, 2, 5, 1, 1);

        grid.add(new Label("subject type(*):"), 0,6);
        grid.add(subjectTypeComboBox, 1,6, 1, 1);
        grid.add(newSubjectType, 2, 6, 1, 1);

        grid.add(new Label("import date(*):"), 0, 7);
        grid.add(housingDate, 1, 7, 2, 1);

        grid.add(new Label("time(*):"), 0, 8);
        grid.add(timeField, 1, 8, 2, 1);

        grid.add(new Label("subject count"), 0, 9);
        grid.add(countSpinner, 1, 9, 2, 1);
        countSpinner.valueProperty().addListener(((observable, oldValue, newValue) -> updatePreviewList()));

        grid.add(new Label("start index:"), 0, 10);
        grid.add(startIdSpinner, 1, 10, 2, 1);
        startIdSpinner.valueProperty().addListener((observable, oldValue, newValue) -> createPreview());

        grid.add(new Label("preview:"), 0, 11);
        grid.add(previewList, 0, 12, 3, 2);
        grid.add(new Label("(*) required"), 0, 14);
        this.getChildren().add(grid);
    }

    private String createPreview() {
        LocalDate hdate = housingDate.getValue();
        String prefix = String.valueOf(hdate.getYear());
        String name = nameField.getText();
        Integer initial_id = startIdSpinner.getValue();
        String previewName = createName(prefix, name, initial_id, 4);
        previewLabel.setText("(preview: " + previewName + ")");
        updatePreviewList();
        return previewName;
    }

    private void updatePreviewList() {
        Integer count = countSpinner.getValue();
        LocalDate hdate = housingDate.getValue();
        String prefix = String.valueOf(hdate.getYear());
        String name = nameField.getText();
        Integer initial_id = startIdSpinner.getValue();
        ObservableList<String> list = FXCollections.observableArrayList();
        for (int i=0; i < count; i++) {
            String previewName = createName(prefix, name, initial_id + i, 4);
            list.add(i, previewName);
        }
        previewList.setItems(list);
    }

    private String createName(String prefix, String name, Integer count, Integer digit_count) {
        String full_name = prefix + name;
        String count_str = count.toString();
        int fill = digit_count - count_str.length();
        for ( int i=0; i < fill; i++) {
            full_name += "0";
        }
        full_name += count_str;
        return full_name;
    }

    private boolean validateTime(String time_str) {
        DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        try {
            timeFormat.parse(time_str);
            return true;
        } catch (Exception e) {
            return  false;
        }
    }

    public Integer persistSubjects() {
        Housing h;
        LocalDate hdate = housingDate.getValue();
        String d = hdate.toString();
        if (!validateTime(timeField.getText())) {
            return -1;
        }
        String t = timeField.getText();
        String datetimestr = d + " " + t;
        DateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date date;
        try {
            date = dateTimeFormat.parse(datetimestr);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }

        String prefix = String.valueOf(hdate.getYear());
        String name = nameField.getText();
        Integer initial_id = startIdSpinner.getValue();

        for (int i = 0; i < countSpinner.getValue(); i++) {
            String full_name = createName(prefix, name, initial_id + i, 4);
            Subject s = new Subject();
            s.setName(full_name);
            s.setSpeciesType(speciesComboBox.getValue());
            s.setSupplier(supplierComboBox.getValue());
            s.setGender(Gender.unknown);
            s.setResponsiblePerson(responsiblePersonCombo.getId() != null ? responsiblePersonCombo.getValue() : null);
            s.setSubjectType(subjectTypeComboBox.getValue());

            h = new Housing(s, housingUnitCombo.getHousingUnit(), date);
            HashSet<Housing> housings = new HashSet<>(1);
            housings.add(h);
            s.setHousings(housings);
            Communicator.pushSaveOrUpdate(s);
        }
        storePreferences();
        return 0;
    }

    private void applyPreferences() {
        if (housingUnitCombo.getHousingUnit() == null && !prefs.get("housing_unit", "").isEmpty()) {
            List<HousingUnit> hs = EntityHelper.getEntityList("from HousingUnit where id = " + prefs.get("housing_unit", ""), HousingUnit.class);
            if (hs.size() > 0)
                housingUnitCombo.setHousingUnit(hs.get(0));
        }
        if (!prefs.get("supplier", "").isEmpty()) {
            List<SupplierType> sppl = EntityHelper.getEntityList("from SupplierType where id = " + prefs.get("supplier", ""),  SupplierType.class);
            if (sppl.size() > 0)
                supplierComboBox.getSelectionModel().select(sppl.get(0));
        }
        nameField.setText(prefs.get("subject_name", ""));
        if (!prefs.get("subject_person", "").isEmpty()) {
            List<Person> persons = EntityHelper.getEntityList("from Person where id = " + prefs.get("subject_person", ""),  Person.class);
            if (persons.size() > 0)
                responsiblePersonCombo.getSelectionModel().select(persons.get(0));
        }
        if (!prefs.get("subject_subjecttype", "").isEmpty()) {
            List<SubjectType> sts = EntityHelper.getEntityList("from SubjectType where id = " + prefs.get("subject_subjecttype", ""),  SubjectType.class);
            if (sts.size() > 0)
                subjectTypeComboBox.getSelectionModel().select(sts.get(0));
        }
        if (!prefs.get("subject_species", "").isEmpty()) {
            List<SpeciesType> sps = EntityHelper.getEntityList("from SpeciesType where id = " + prefs.get("subject_species", ""),  SpeciesType.class);
            if (sps.size() > 0)
                speciesComboBox.getSelectionModel().select(sps.get(0));
        }
        if (!prefs.get("import_date", "").isEmpty()) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
                housingDate.setValue(DateTimeHelper.toLocalDate(dateFormat.parse(prefs.get("import_date", ""))));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    private void storePreferences() {
        prefs.put("housing_unit", housingUnitCombo.getHousingUnit().getId().toString());
        prefs.put("supplier", supplierComboBox.getValue().getId().toString());
        prefs.put("subject_name", nameField.getText());
        prefs.put("subject_count", ((Integer)(startIdSpinner.getValue() + countSpinner.getValue())).toString());
        prefs.put("subject_person", (responsiblePersonCombo.getValue() != null && responsiblePersonCombo.getValue().getId() != null)
                ? responsiblePersonCombo.getValue().getId().toString() : "");
        prefs.put("subject_species", speciesComboBox.getValue().getId().toString());
        prefs.put("subject_subjecttype", subjectTypeComboBox.getValue().getId().toString());
        prefs.put("import_date", housingDate.getValue().toString());
    }

    private void createNewHousingButton() {
        HousingUnit u = editHousingUnitDialog(null, this.housingUnitCombo.getHousingUnit());
        if (u != null) {
            this.housingUnitCombo.setHousingUnit(u);
        }
    }

    public boolean validate(Vector<String> messages) {
        boolean valid = true;
        if (!housingUnitCombo.getHousingUnit().getHousingType().getCanHoldSubjects()) {
            messages.add("The selected housing unit can not hold subjects!");
            valid = false;
        }
        if (nameField.getText().isEmpty()) {
            messages.add("The Name must not be empty!");
            valid = false;
        }
        if (responsiblePersonCombo.getValue() != null && responsiblePersonCombo.getValue().getId() != null &&
                !responsiblePersonCombo.getValue().getActive()) {
            if (!housingDate.getValue().isBefore(LocalDate.now().minusDays(7))) {
                messages.add("The selected responsible person must not be marked inactive. Cross check with the person info.");
                valid = false;
            }
        }
        if (supplierComboBox.getValue() == null) {
            messages.add("An animal supplier must be given.");
            valid = false;
        }
        if (speciesComboBox.getValue() == null) {
            messages.add("Species information must not be empty!");
            valid = false;
        }
        if (subjectTypeComboBox.getValue() == null) {
            messages.add("A subject type must be given.");
            valid = false;
        }
        if (housingDate.getValue() == null) {
            messages.add("Import date must not be null!");
            valid = false;
        }
        String firstName = createPreview();
        Vector<String> params = new Vector<>();
        params.add("name");
        Vector<Object> objects = new Vector<>();
        objects.add(firstName);
        if (EntityHelper.getEntityList("From Subject where name like :name", params, objects, Subject.class).size() > 0) {
            messages.add("Subject name is already used! Select another name pattern, or increase start index!");
            valid = false;
        }
        return valid;
    }
}
