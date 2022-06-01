package javafx.Controllers;

import kanban.Model.Board;
import kanban.Model.Card;
import kanban.Model.Column;
import kanban.Model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import kanban.ServerController;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class BoardsViewController implements Initializable {

    private BoardViewController boardViewController;

    private LoginViewController controller;


    private User mainUser;

    @FXML
    private TextField boardNameField;

    @FXML
    private ComboBox<Board> listOfBoards;

    @FXML
    private Label usernameLabel;

    @FXML
    private void addNewBoard(ActionEvent e){
        String newBoardName = boardNameField.getText();
        if(!newBoardName.isBlank()){
            Board newBoard = new Board(newBoardName);

            List<Column> newColumns = new ArrayList<>();
            newColumns.add(new Column("todo"));
            newColumns.add(new Column("inProgress"));
            newColumns.add(new Column("done"));
            newBoard.setColumns(newColumns);

            ServerController server = new ServerController();
            server.post("boards", newBoard);
            server.addBoard("users", mainUser.getId().intValue(), newBoard);
            server.addObjectInBoard("user", newBoard.getId().intValue(),mainUser.getId().intValue());
            listOfBoards.getItems().add(newBoard);
        }
    }

    @FXML
    private void openBoard(ActionEvent e){
        Board selectedBoard = listOfBoards.getSelectionModel().getSelectedItem();
        if(selectedBoard != null){
            ServerController server = new ServerController();
            JSONObject jsonBoard = new JSONObject(server.getOne("boards", selectedBoard.getId().intValue()));

            List<User> users = getUsersFromJSON(jsonBoard);
            List<Column> columns =  getColumnFromJSON(jsonBoard);
            Board board = new Board(jsonBoard.getLong("id"), jsonBoard.getString("title"), users, columns);

            openBoardWindow(board);
        }

    }

    private void openBoardWindow(Board board) {
        try {
            Stage boardsStage = (Stage) usernameLabel.getScene().getWindow();

            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("/board-view.fxml"));
            Parent root = fxmlLoader.load();

            boardViewController = fxmlLoader.getController();
            boardViewController.setParent(this);
            boardViewController.setBoard(board);
            List<Column> columns = board.getColumns();
            boardViewController.addTasksAtToDoColumn(columns.get(0).getCards());
            boardViewController.addTasksAtInProgressColumn(columns.get(1).getCards());
            boardViewController.addTasksAtDoneColumn(columns.get(2).getCards());

            Stage stage = new Stage();
            MenuBar menuBar = (MenuBar)fxmlLoader.getNamespace().get("menuBar");
            menuBar.prefWidthProperty().bind(stage.widthProperty());
            stage.setTitle(board.getTitle());
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.show();

            boardsStage.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private List<Column> getColumnFromJSON(JSONObject jsonBoard) {
        List<Column> columns = new ArrayList<>();
        JSONArray jsonColumns = jsonBoard.getJSONArray("lists");
        for(int i = 0; i < jsonColumns.length(); i++){
            JSONObject jsonColumn = jsonColumns.getJSONObject(i);
            List<Card> cards = new ArrayList<>();
            JSONArray jsonCards = jsonBoard.getJSONArray("cards");
            for(int j = 0; j < jsonCards.length(); j++){
                JSONObject jsonCard = jsonCards.getJSONObject(i);
                List<User> usersOnCard = getUsersFromJSON(jsonCard);
                cards.add(new Card(jsonCard.getString("title"), usersOnCard, jsonCard.getString("description")));
            }
            //columns.add(new Column(jsonColumn.getLong("id"), jsonColumn.getString("title"), cards));
        }
        return columns;
    }

    private List<User> getUsersFromJSON(JSONObject jsonObj) {
        List<User> users = new ArrayList<>();
        JSONArray jsonUsers = jsonObj.getJSONArray("users");
        for(int i = 0; i < jsonUsers.length(); i++){
            JSONObject jsonUser = jsonUsers.getJSONObject(i);
            users.add(new User(jsonUser.getLong("id"), jsonUser.getString("username")));
        }
        return users;
    }

    public void setListOfBoards(List<Board> listOfBoards) {
        this.listOfBoards.getItems().addAll(listOfBoards);
    }

    public void setUsernameLabel(String username) {
        this.usernameLabel.setText(username);
    }

    public User getMainUser() {
        return mainUser;
    }

    public void setMainUser(User user) {
        this.mainUser = user;
    }

    public void setParent (LoginViewController controller){
        this.controller = controller;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}
