package animalkeeping.ui;

import animalkeeping.logging.ChangeLogInterceptor;
import animalkeeping.model.Person;
import animalkeeping.model.DatabaseUserType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.hibernate.HibernateException;
import org.hibernate.Session;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static animalkeeping.util.Dialogs.showInfo;

/**
 * Created by huben on 17.02.17.
 */
public class AddDatabaseUserForm extends VBox {
    TextField usernameField;
    PasswordField pwField;
    PasswordField pwConfirmField;
    private ComboBox<DatabaseUserType> userClassComboBox;
    private Person person;

    public AddDatabaseUserForm(Person p){
        this.person = p;
        init();
    }

    private void init() {
        userClassComboBox = new ComboBox<DatabaseUserType>();
        usernameField = new TextField();
        pwField = new PasswordField();
        pwConfirmField = new PasswordField();

        userClassComboBox.setConverter(new StringConverter<DatabaseUserType>() {
            @Override
            public String toString(DatabaseUserType object) {
                return object.getName();
            }

            @Override
            public DatabaseUserType fromString(String string) {
                return null;
            }
        });

        List<DatabaseUserType> userTypes = new ArrayList(0);
        Session session = Main.sessionFactory.openSession();
        try {
            session.beginTransaction();
            userTypes = session.createQuery("from DatabaseUserType", DatabaseUserType.class).list();
            session.getTransaction().commit();
            session.beginTransaction();
            session.close();
        } catch (HibernateException e) {
            e.printStackTrace();
            if (session.isOpen()) {
                session.close();
            }
        }

        userClassComboBox.getItems().addAll(userTypes);

        GridPane grid = new GridPane();
        ColumnConstraints column1 = new ColumnConstraints(100,100, Double.MAX_VALUE);
        column1.setHgrow(Priority.NEVER);
        ColumnConstraints column2 = new ColumnConstraints(100, 150, Double.MAX_VALUE);
        column2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(column1, column2);
        userClassComboBox.prefWidthProperty().bind(column2.maxWidthProperty());
        usernameField.prefWidthProperty().bind(column2.maxWidthProperty());
        pwField.prefWidthProperty().bind(column2.maxWidthProperty());
        pwConfirmField.prefWidthProperty().bind(column2.maxWidthProperty());

        grid.setVgap(5);
        grid.setHgap(2);
        grid.add(new Label("User name:"), 0, 0);
        grid.add(usernameField, 1, 0, 1, 1);

        grid.add(new Label("Password:"), 0, 1);
        grid.add(pwField, 1, 1, 1, 1 );

        grid.add(new Label("Confirm password:"), 0, 2);
        grid.add(pwConfirmField, 1, 2, 1,1);

        grid.add(new Label("User type:"), 0,3);
        grid.add(userClassComboBox, 1,3, 1, 1);

        this.getChildren().add(grid);

    }



    public boolean addUser(Connection connection){
        if (pwField.getText().equals(pwConfirmField.getText())) {
            animalkeeping.model.DatabaseUser user = new animalkeeping.model.DatabaseUser();
            user.setName(usernameField.getText());
            user.setType(userClassComboBox.getValue());
            user.setPerson(person);
            createUser(connection, user, pwField.getText());
            person.setUser(user);
            ChangeLogInterceptor interceptorX = new ChangeLogInterceptor();
            Session session = Main.sessionFactory.withOptions().interceptor(interceptorX).openSession();
            interceptorX.setSession(session);
            try {
                session.beginTransaction();
                session.saveOrUpdate(person);
                session.getTransaction().commit();
                session.close();
            } catch (HibernateException he) {
                showInfo(he.getLocalizedMessage());
                session.close();
            }
            return true;
        }
        else{
            System.out.println("Passwords do not match!");
            return false;
        }
    }

    private Boolean createUser(Connection connection, animalkeeping.model.DatabaseUser user, String password) {
        try {
            Statement stmt = connection.createStatement();
            String createUser = "CREATE USER " + user.getName() + "@localhost IDENTIFIED BY \"" + password + "\"";
            String grantPrivilege = "GRANT " + user.getType().getPrivileges() + " ON * . * TO '" + user.getName() + "'@'%'";
            stmt.executeUpdate(createUser);
            stmt.executeUpdate(grantPrivilege);
            showInfo("Successfully added user to database!");
        } catch (SQLException e) {
            showInfo(e.getMessage());
            System.out.println(e.getMessage());
            return false;
        }
        Session session = Main.sessionFactory.openSession();
        session.beginTransaction();
        session.saveOrUpdate(user);
        session.getTransaction().commit();
        session.close();
        return true;
    }
}