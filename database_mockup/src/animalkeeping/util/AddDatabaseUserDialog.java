package animalkeeping.util;

import animalkeeping.ui.forms.AddDatabaseUserForm;
import animalkeeping.model.Person;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;

import java.sql.Connection;
import java.util.Optional;

/**
 * Created by huben on 20.02.17.
 */
public class AddDatabaseUserDialog extends Dialogs {

    public static void addDatabaseUser(Connection connection, Person p) {
        if(connection == null || p == null) {
            return;
        }

        AddDatabaseUserForm aduf = new AddDatabaseUserForm(p);
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Create new Database User");
        dialog.setResizable(true);
        dialog.getDialogPane().setContent(aduf);
        dialog.setWidth(300);
        aduf.prefWidthProperty().bind(dialog.widthProperty());

        ButtonType buttonTypeOk = new ButtonType("ok", ButtonBar.ButtonData.OK_DONE);
        ButtonType buttonTypeCancel = new ButtonType("cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeOk);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeCancel);
        dialog.setResultConverter(b -> {
            if (b == buttonTypeOk) {
                return aduf.addUser(connection);
            }
            return null;
        });

        Optional<Boolean> result = dialog.showAndWait();
        if (result.isPresent() && result.get()) {
            //showInfo("Successfully added user to database!");
        }
    }
}
