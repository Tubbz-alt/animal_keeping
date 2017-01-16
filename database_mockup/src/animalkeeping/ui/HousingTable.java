package animalkeeping.ui;

import animalkeeping.model.*;
import animalkeeping.ui.controller.InventoryController;
import javafx.beans.property.*;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by jan on 01.01.17.
 */
public class HousingTable extends TableView{
    private TableColumn<Housing, Number> idCol;
    private TableColumn<Housing, String> housingUnitNameCol;
    private TableColumn<Housing, String> subjectNameCol;
    private TableColumn<Housing, String> subjectSpeciesCol;
    private TableColumn<Housing, Date> startCol;
    private TableColumn<Housing, Date> endCol;


    public HousingTable(HousingUnit unit) {
        this(unit, false);
    }


    public HousingTable(HousingUnit unit, Boolean showAll) {
        initTable();
        initTableContent(unit, showAll);
    }


    public HousingTable(Collection<Housing> housings) {
        initTable();
        this.setHousings(housings);
    }


    private void initTable() {
        idCol = new TableColumn<Housing, Number>("id");
        idCol.setCellValueFactory(data -> new ReadOnlyLongWrapper(data.getValue().getId()));
        housingUnitNameCol = new TableColumn<Housing, String>("housing unit");
        housingUnitNameCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getHousing().getName()));
        subjectNameCol = new TableColumn<Housing, String>("subject");
        subjectNameCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getSubject().getName()));
        subjectSpeciesCol = new TableColumn<Housing, String>("species");
        subjectSpeciesCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getSubject().getSpeciesType().getName()));
        startCol= new TableColumn<Housing, Date>("from");
        startCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<Date>(data.getValue().getStart()));
        endCol= new TableColumn<Housing, Date>("until");
        endCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<Date>(data.getValue().getEnd()));
        this.getColumns().addAll(idCol, housingUnitNameCol, subjectNameCol, subjectSpeciesCol, startCol, endCol);
    }

    private void initTableContent(Collection<Housing> housings) {
        this.setHousings(housings);
    }


    private void initTableContent(HousingUnit unit, Boolean show_all) {
        Set<Housing> housings = new HashSet<>();
        InventoryController.collectHousings(housings, unit, !show_all);
        initTableContent(housings);
    }

    public void setHousings(Collection<Housing> housings) {
        this.getItems().clear();
        this.getItems().addAll(housings);
    }
}