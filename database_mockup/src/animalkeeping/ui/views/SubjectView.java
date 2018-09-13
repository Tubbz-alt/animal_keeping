/******************************************************************************
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

 * Created by jan on 01.05.17.

 *****************************************************************************/
package animalkeeping.ui.views;

import animalkeeping.logging.Communicator;
import animalkeeping.model.Housing;
import animalkeeping.model.Subject;
import animalkeeping.model.SubjectNote;
import animalkeeping.model.Treatment;
import animalkeeping.ui.Main;
import animalkeeping.ui.tables.HousingTable;
import animalkeeping.ui.tables.NotesTable;
import animalkeeping.ui.tables.SubjectsTable;
import animalkeeping.ui.tables.TreatmentsTable;
import animalkeeping.ui.widgets.ControlLabel;
import animalkeeping.ui.widgets.TimelineController;
import animalkeeping.util.DateTimeHelper;
import animalkeeping.util.Dialogs;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import org.hibernate.Session;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Iterator;
import java.util.ResourceBundle;

public class SubjectView extends AbstractView implements Initializable {
    @FXML private ScrollPane tableScrollPane;
    @FXML private Label idLabel;
    @FXML private Label nameLabel;
    @FXML private Label genderLabel;
    @FXML private Label personLabel;
    @FXML private Label birthdateLabel;
    @FXML private Label housingStartLabel;
    @FXML private Label housingEndLabel;
    @FXML private Label statusLabel;
    @FXML private Label originLabel;
    @FXML private Label speciesLabel;
    @FXML private Tab housingHistoryTab;
    @FXML private Tab observationsTab;
    @FXML private Tab treatmentsTab;
    @FXML private VBox timelineVBox;

    private SubjectsTable subjectsTable;
    private HousingTable housingTable;
    private NotesTable<SubjectNote> notesTable;
    private TimelineController timeline;
    private TreatmentsTable treatmentsTable;
    private ControlLabel reportDead;
    private ControlLabel moveSubjectLabel;
    private ControlLabel editSubjectLabel;
    private ControlLabel deleteSubjectLabel;
    private ControlLabel addTreatmentLabel;
    private ControlLabel editTreatmentLabel;
    private ControlLabel deleteTreatmentLabel;
    private ControlLabel newComment;
    private ControlLabel editComment;
    private ControlLabel deleteComment;
    private VBox controls;
    private Session session;
    private Subject selectedSubject = null;

    public SubjectView() {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/animalkeeping/ui/fxml/SubjectView.fxml"));
        loader.setController(this);
        try {
            this.getChildren().add(loader.load());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        session = Main.sessionFactory.openSession();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        subjectsTable = new SubjectsTable();
        subjectsTable.getSelectionModel().getSelectedItems().addListener(new ListChangeListener<SubjectsTable.SubjectTableItem>() {
            @Override
            public void onChanged(Change<? extends SubjectsTable.SubjectTableItem> c) {
                if (!subjectsTable.getSelectionModel().isEmpty()) {
                    Subject s = session.get(Subject.class, subjectsTable.getSelectedSubjectId());
                    subjectSelected(s);
                } else {
                    subjectSelected(null);
                }
            }
        });
        //subjectsTable.setAliveFilter(true);
        timeline = new TimelineController();

        this.tableScrollPane.setContent(subjectsTable);
        this.tableScrollPane.prefHeightProperty().bind(this.heightProperty());
        this.tableScrollPane.prefWidthProperty().bind(this.widthProperty());
        this.timelineVBox.getChildren().add(timeline);
        idLabel.setText("");
        nameLabel.setText("");
        speciesLabel.setText("");
        personLabel.setText("");
        originLabel.setText("");
        statusLabel.setText("");
        housingEndLabel.setText("");
        housingStartLabel.setText("");

        treatmentsTable = new TreatmentsTable();
        treatmentsTable.getSelectionModel().getSelectedItems().addListener((ListChangeListener<Treatment>) c -> treatmentSelected(c.getList().size() > 0 ? c.getList().get(0) : null));
        treatmentsTab.setContent(treatmentsTable);

        housingTable = new HousingTable();
        housingHistoryTab.setContent(housingTable);

        notesTable = new NotesTable<>();
        notesTable.getSelectionModel().getSelectedItems().addListener((ListChangeListener<SubjectNote>) c -> noteSelected(c.getList().size() > 0 ? c.getList().get(0) : null));
        observationsTab.setContent(notesTable);

        controls = new VBox();
        ControlLabel newSubjectLabel = new ControlLabel("new subject", "Create a new subject entry.", false);
        newSubjectLabel.setOnMouseClicked(event -> {
            if(event.getButton().equals(MouseButton.PRIMARY)){
                editSubject(null);
            }
        });
        controls.getChildren().add(newSubjectLabel);
        editSubjectLabel = new ControlLabel("edit subject", "Edit the selected subject's information", true);
        editSubjectLabel.setOnMouseClicked(event -> {
            if(event.getButton().equals(MouseButton.PRIMARY)){
                editSubject(selectedSubject);
            }
        });
        controls.getChildren().add(editSubjectLabel);
        deleteSubjectLabel = new ControlLabel("delete subject", "Delete the selected subject (only possible, if not referenced).", true);
        deleteSubjectLabel.setOnMouseClicked(event -> {
            if(event.getButton().equals(MouseButton.PRIMARY)){
                deleteSubject();
                subjectsTable.refresh(); //TODO check refresh methods!
            }
        });
        controls.getChildren().add(deleteSubjectLabel);

        controls.getChildren().add(new Separator(Orientation.HORIZONTAL));

        addTreatmentLabel = new ControlLabel("new treatment", "Add a treatment entry for the selected subject", true);
        addTreatmentLabel.setOnMouseClicked(event -> {
            if (event.getButton().equals(MouseButton.PRIMARY)) {
                addTreatment(selectedSubject);
                treatmentsTable.refresh();
            }
        });
        controls.getChildren().add(addTreatmentLabel);
        editTreatmentLabel = new ControlLabel("edit treatment", "Edit the selected treatment information.", true);
        editTreatmentLabel.setOnMouseClicked(event -> {
            if (event.getButton().equals(MouseButton.PRIMARY)) {
                editTreatment(treatmentsTable.getSelectionModel().getSelectedItem());
                treatmentsTable.refresh();
            }
        });
        controls.getChildren().add(editTreatmentLabel);
        deleteTreatmentLabel = new ControlLabel("remove treatment", "Delete the selected subject treatment.",true);
        deleteTreatmentLabel.setOnMouseClicked(event -> {
            if(event.getButton().equals(MouseButton.PRIMARY)){
                deleteTreatment();
                treatmentsTable.refresh();
            }
        });
        controls.getChildren().add(deleteTreatmentLabel);

        controls.getChildren().add(new Separator(Orientation.HORIZONTAL));
        newComment = new ControlLabel("add observation", "Add an observation note to the selected subject.", true);
        newComment.setOnMouseClicked(event -> {
            if (event.getButton().equals(MouseButton.PRIMARY)) {
                newSubjectObservation(selectedSubject);
            }
        });
        controls.getChildren().add(newComment);
        editComment = new ControlLabel("edit observation", "Edit an observation.", true);
        editComment.setOnMouseClicked(event -> {
            if (event.getButton().equals(MouseButton.PRIMARY)) {
                editSubjectObservation(notesTable.getSelectionModel().getSelectedItem());
            }
        });
        controls.getChildren().add(editComment);
        deleteComment = new ControlLabel("delete observation", "Delete the selected observation.", true);
        deleteComment.setOnMouseClicked(event -> {
            if (event.getButton().equals(MouseButton.PRIMARY)) {
                deleteObservation(notesTable.getSelectionModel().getSelectedItem());
            }
        });
        controls.getChildren().add(deleteComment);

        controls.getChildren().add(new Separator(Orientation.HORIZONTAL));
        moveSubjectLabel = new ControlLabel("move subject", "Relocate the selected subject to a different housing unit.", true);
        moveSubjectLabel.setOnMouseClicked(event -> {
            if(event.getButton().equals(MouseButton.PRIMARY)){
                moveSubject(selectedSubject);
            }
        });
        controls.getChildren().add(moveSubjectLabel);

        reportDead = new ControlLabel("report dead",  "Report that the selected subject deceased.", true);
        reportDead.setOnMouseClicked(event -> {
            if(event.getButton().equals(MouseButton.PRIMARY)){
                reportDead(selectedSubject);
            }
        });
        controls.getChildren().add(reportDead);
    }


    private void subjectSelected(Subject s) {
        selectedSubject = s;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        if (selectedSubject != null) {
            Iterator<Housing> iter = selectedSubject.getHousings().iterator();
            Housing firstHousing = null;
            Housing lastHousing = null;
            if (iter.hasNext()) {
                firstHousing = iter.next();
            }
            while (iter.hasNext()) {
                lastHousing = iter.next();
            }
            idLabel.setText(selectedSubject.getId().toString());
            String alias = selectedSubject.getAlias() != null ? " (" + selectedSubject.getAlias() + ")" : "";
            nameLabel.setText(selectedSubject.getName() + alias);
            genderLabel.setText(selectedSubject.getGender().toString());
            String agestr = "";
            if (selectedSubject.getBirthday() != null) {
                LocalDate ld = LocalDate.now();
                if (lastHousing != null && lastHousing.getEnd() != null)
                    ld = DateTimeHelper.toLocalDate(lastHousing.getEnd());
                DateTimeHelper.Age age = DateTimeHelper.age(DateTimeHelper.toLocalDate(selectedSubject.getBirthday()), ld);
                agestr = " ("+ age.getYears() + "|" + age.getMonths() + "|" + age.getDays() + ")";
            }
            birthdateLabel.setText((selectedSubject.getBirthday() != null ? dateFormat.format(selectedSubject.getBirthday()) : "unknown") + agestr);
            speciesLabel.setText(selectedSubject.getSpeciesType().getName());
            originLabel.setText(selectedSubject.getSupplier().getName());
            personLabel.setText(selectedSubject.getResponsiblePerson() != null ? (selectedSubject.getResponsiblePerson().getFirstName() +
                    " " + selectedSubject.getResponsiblePerson().getLastName()) : "");

            housingStartLabel.setText(firstHousing != null ? timestampFormat.format(firstHousing.getStart()) : "");
            if (lastHousing != null) {
                housingEndLabel.setText(lastHousing.getEnd() != null ? timestampFormat.format(lastHousing.getEnd()) : "");
            } else if (firstHousing != null){
                housingEndLabel.setText(firstHousing.getEnd() != null ? timestampFormat.format(firstHousing.getEnd()) : "");
            }
            Iterator<Treatment> titer = selectedSubject.getTreatments().iterator();
            Treatment t = null;
            while (titer.hasNext()) {
                t = titer.next();
            }
            if (t != null && t.getEnd() == null) {
                statusLabel.setText("In treatment: " + t.getTreatmentType().getName());
            } else if (selectedSubject.getCurrentHousing() != null) {
                statusLabel.setText("Available: " + selectedSubject.getCurrentHousing().getHousing().getName());
            } else {
                statusLabel.setText("Unavailable");
            }
            treatmentsTable.setSubject(selectedSubject);
            timeline.setTreatments(selectedSubject.getTreatments());
            housingTable.setSubject(selectedSubject);
            notesTable.setNotes(selectedSubject.getNotes());
        } else {
            idLabel.setText("");
            nameLabel.setText("");
            genderLabel.setText("");
            birthdateLabel.setText("");
            originLabel.setText("");
            speciesLabel.setText("");
            statusLabel.setText("");
            personLabel.setText("");
            housingEndLabel.setText("");
            housingStartLabel.setText("");
            treatmentsTable.setTreatments(null);
            timeline.setTreatments(null);
            housingTable.clear();
            notesTable.setNotes(null);
        }
        Boolean alive = selectedSubject != null && selectedSubject.getCurrentHousing() != null;
        moveSubjectLabel.setDisable(selectedSubject == null || !alive);
        deleteSubjectLabel.setDisable(selectedSubject == null ||!alive) ;
        editSubjectLabel.setDisable(selectedSubject == null);
        reportDead.setDisable(selectedSubject == null || !alive);
        addTreatmentLabel.setDisable(selectedSubject==null || !alive);
        newComment.setDisable(selectedSubject==null);
    }

    private void treatmentSelected(Treatment t) {
        editTreatmentLabel.setDisable(t == null);
        deleteTreatmentLabel.setDisable(t == null);
    }

    private void noteSelected(SubjectNote n) {
        newComment.setDisable(n == null);
        editComment.setDisable(n == null);
        deleteComment.setDisable(n == null);
    }

    public void nameFilter(String name) {
        this.subjectsTable.setNameFilter(name);
    }


    public void idFilter(Long id) {
        this.subjectsTable.setIdFilter(id);
    }

    private void deleteSubject() {
        subjectsTable.deleteSubject(selectedSubject);
    }

    private void deleteTreatment() {
        treatmentsTable.deleteTreatment(treatmentsTable.getSelectionModel().getSelectedItem());
    }

    private void deleteObservation(SubjectNote n) {
        notesTable.getSelectionModel().select(null);
        Communicator.pushDelete(n);
        notesTable.getItems().removeAll(n);
    }

    @Override
    public VBox getControls() {
        return controls;
    }

    private void showInfo(String  info) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(info);
        alert.show();
    }

    private void reportDead(Subject s) {
        subjectsTable.reportSubjectDead(s);
    }

    private void moveSubject(Subject s) {
        subjectsTable.moveSubject(s);
    }

    private void editTreatment(Treatment t) {
        Dialogs.editTreatmentDialog(t);
    }

    private void addTreatment(Subject s) {
        Dialogs.editTreatmentDialog(s);
    }

    private void editSubject(Subject s) {
        subjectsTable.editSubject(s);
    }

    private void newSubjectObservation(Subject s) {
        Dialogs.editSubjectNoteDialog(s);
    }

    private void editSubjectObservation(SubjectNote sn) {
        Dialogs.editSubjectNoteDialog(sn, sn.getSubject());
    }

    @Override
    public void refresh() {
        fireEvent(new ViewEvent(ViewEvent.REFRESHING));
        subjectsTable.addEventHandler(ViewEvent.REFRESHED, event -> {
            if (event.getEventType()== ViewEvent.REFRESHED) {
                subjectsTable.setSelectedSubject(selectedSubject);
                fireEvent(new ViewEvent(ViewEvent.REFRESHED));
            }
        });
        selectedSubject = null;
        subjectsTable.refresh();
    }

    public static Tooltip getToolTip() {
        return new Tooltip("Manage Subjects, add Treatments, observations etc.");
    }
}