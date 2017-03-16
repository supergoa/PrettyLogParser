package log_parser;

import java.io.*;
import java.util.*;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * Created by Nicholas Ostaffe on 6/20/2016.
 */
public class LogviewController {
    /**
     * Reference to the main stage
     */
    private Stage stage;


    private int rowCount = 0;

    /**
     * A list of logs that have been opened in the program
     */
    private static List<Log> logs;

    @FXML
    private Button collapseButton;

    @FXML
    private AnchorPane pane;

    @FXML
    private TabPane tabPane;

    @FXML
    private Button logCombination;

    @FXML
    private Button newLog;

    @FXML
    private TextField searchBox;

    @FXML
    private ComboBox<String> sortBy;

    @FXML
    private CheckBox reverseRowsCheckbox;

    /**
     * Initializes variables and sets initial properties for the opening screen
     */
    public void initialize() {
        // Save stage reference locally
        stage = Main.getStage();

        // Set TabPane properties
        tabPane.tabClosingPolicyProperty().set(TabPane.TabClosingPolicy.ALL_TABS);
        tabPane.autosize();

        // Disable sorting, searching, and collapse all until a log is opened
        sortBy.setDisable(true);
        searchBox.setDisable(true);
        collapseButton.setDisable(true);
        reverseRowsCheckbox.setDisable(true);

        // Set options for sorting
        sortBy.getItems().removeAll(sortBy.getItems());
        sortBy.setItems(FXCollections.observableArrayList("Date", "Type"));

        logs = new LinkedList<>();
        stage.show();
    }

    /**
     *
     * @param f1 file1 is the file to combine with file2
     * @param f2 file2 is the file appended to @param f1
     * @return a combined file of contents of @param f1 and @param f2
     * @throws FileNotFoundException
     */
    public static File combineFiles(File f1,File f2) throws FileNotFoundException, InterruptedException {
        File f = null;
        try {
            f = new File("CombinedFile.txt");
            f.createNewFile();
            Process p = Runtime.getRuntime().exec("cmd /c copy /b "+f1.getAbsolutePath()+"+"+f2.getAbsolutePath()+" CombinedFile.txt");
            Thread.sleep(3000);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return f;
    }

    /**
     * Creates all objects associated with creating a new tab: Log, Entry.
     * Sets properties to these objects. Keeps track of these objects dynamically
     * in global var. "logs"
     *
     * @param event
     */
    @FXML
    void addTab(ActionEvent event) {

        boolean combineLogs = ((Button)event.getSource()).getId().equals("logCombination");
        final FileChooser fileChooser = new FileChooser();
        List<File> list = null;
        try {
            // Prompt the user to choose files from a directory
            list = fileChooser.showOpenMultipleDialog(stage);
            // Check if the user chose "+ Combine Files"
            if(combineLogs) {
                File combinedFile;

                // No point in combining files if the user didn't select 2 or more
                if(list.size() >= 2) {
                    combinedFile = combineFiles(list.get(0),list.get(1));

                    for (int i = 2; i < list.size(); i++) {
                        combinedFile = combineFiles(combinedFile, list.get(i));
                    }

                    list = new ArrayList<>();
                    list.add(combinedFile);
                }
            }
            /*
                This loop is only run once if the user has chosen "+ Combine Files".
                Otherwise (the user chose "+ New File"), a new tab is created for each file selected
                and the INITIAL_LOG_COUNT (of FINAL value 50) amount of rows is added to the screen.
                A listener (on each ScrollPane of each tab) waits until the user has scrolled ot the
                bottom of the screen to then add 20 rows at a time.
             */
            for (File f : list) {
                String fileName = parseFileName(f);
                Log log =  new Log(fileName, f);
                logs.add(log);
            }

            // Enable sorting, searching, and collapse all
            collapseButton.setDisable(false);
            sortBy.setDisable(false);
            searchBox.setDisable(false);
            reverseRowsCheckbox.setDisable(false);

            // refresh
            stage.show();

        } catch (Exception ex) {

        }
    }

    /**
     *
     * @param f the file to create the name for
     * @return a unquie file name
     */
    private String parseFileName(File f) {
        // Get the name of the file the user has chosen to build the Tab and Log objects
        String[] partialParse = f.getPath().split("\\\\");
        String finalTabName = partialParse[partialParse.length - 1];
        final String BASE_NAME = finalTabName;

        int uniqueFileName = 1;  // A counter used to append to the BASE_NAME
        boolean uniqueFileNameAchieved = false;
        boolean firstPass = true;

        // Append '(1)' then '(2)' then '(3)' etc to BASE_NAME until a unique filename is achieved.
        while(!uniqueFileNameAchieved) {
            if(!firstPass) {
                finalTabName = BASE_NAME + "(" + uniqueFileName + ")";
                uniqueFileName++;
            }

            uniqueFileNameAchieved = true;

            for (Log l : logs) {
                if(l.getName().equals(finalTabName)) {
                    uniqueFileNameAchieved = false;
                }
            }

            firstPass = false;
        }
        return finalTabName;
    }

    @FXML
    /**
     * Reverses the rows on the current selected tab by reversing the order of log.currentList()
     * and appending that to the main VBox
     */
    void reverseRows(ActionEvent event) {
        Log l = getCurrentLog();
        VBox v = l.getVBox();
        v.getChildren().clear();
        hideAllEntries(l);

        int size = l.getCurrentList().size() > Log.getInitialLogCount() ? Log.getInitialLogCount() : l.getCurrentList().size();

        Collections.reverse(l.getCurrentList());

        for (int i = 0; i < size; i++) {
            v.getChildren().add(l.getCurrentList().get(i).getTitledPane());
            l.getCurrentList().get(i).setVisible(true);
        }
        l.setRowCount(size);

        stage.show();
    }

    /**
     * Sorts the Entries in the selected tab based on the sort chosen (type or date)
     *
     * @param event
     */
    @FXML
    void sortRows(ActionEvent event) {
        collapseAll();
        Entry.setSortOption(sortBy.getSelectionModel().getSelectedItem());
        Log l = getCurrentLog();

        // sort the Entries in the found log

        // l.calculateVisibleEntryList();
        Collections.sort(l.getCurrentList());
        VBox v = l.getVBox();
        v.getChildren().clear();
        hideAllEntries(l);

        int size = l.getCurrentList().size() > Log.getInitialLogCount() ? Log.getInitialLogCount() : l.getCurrentList().size();
        for (int i = 0; i < size; i++) {
            v.getChildren().add(l.getCurrentList().get(i).getTitledPane());
            l.getCurrentList().get(i).setVisible(true);
        }
        l.setRowCount(size);

        // refresh
        stage.show();
    }

    /**
     * Searches the rows in the selected tab based on the entered keyword
     *
     * @param event
     */
    @FXML
    void searchBy(ActionEvent event) {
        collapseAll();
        Log l = getCurrentLog();
        List<Entry> matchedEntries = l.search(searchBox.getText());

        VBox v = l.getVBox();
        v.getChildren().clear();
        hideAllEntries(l);

        // Add up to 50 entries to the tab
        int size = matchedEntries.size() > Log.getInitialLogCount() ? Log.getInitialLogCount() : matchedEntries.size();
        for (int i = 0; i < size; i++) {
            matchedEntries.get(i).getTitledPane().setExpanded(false);
            v.getChildren().add(matchedEntries.get(i).getTitledPane());
            matchedEntries.get(i).setVisible(true);
        }
        l.setRowCount(size);

        // refresh
        stage.show();


    }

    /**
     * Returns the current log based on the tab that is currently open.
     */
    private Log getCurrentLog() {
        Tab tab = tabPane.getSelectionModel().getSelectedItem();
        for (Log l : logs) {
            if (l.getName().equals(tab.getText())) {
                return l;
            }
        }
        return null;
    }

    /**
     * Sets all Entrys' visibility to false
     * @param l the log who entries are to hide
     */
    private void hideAllEntries(Log l){
        for (Entry e :  l.getEntryList()) {
            e.setVisible(false);
        }
    }

    /**
     * All TitledPanes on the screen are retracted into an unopened position
     */
    private void collapseAll() {
        Tab tab = tabPane.getSelectionModel().getSelectedItem();
        for (Log l : logs) {
            if (l.getName().equals(tab.getText())) {
                for (Entry e : l.getEntryList()) {
                    if(e.titledPaneWasCreated()) {
                        e.getTitledPane().setExpanded(false);
                    }
                }
            }
        }
    }

    @FXML
    void collapseAll(ActionEvent event) {
        collapseAll();
    }

    /**
     *
     * @return all logs that are currently open within the program.
     */
    public List<Log> getLogs() {
        return logs;
    }

    /**
     *
     * @return the main tabPane for the application which holds all contents
     */
    public TabPane getTabPane() {
        return tabPane;
    }

    /**
     *
     * @return the button that triggers the action to collapse all the entries
     */
    public Button getCollapseButton(){
        return collapseButton;
    }

    /**
     *
     * @return the TextField that triggers the action to perform a search on a keyword
     */
    public TextField getSearchBox() {
        return searchBox;
    }

    /**
     *
     * @return the ComboBox that triggers the action to sort the entries
     */
    public ComboBox<String> getSortBy() {
        return sortBy;
    }

    /**
     *
     * @return the CheckBox that triggers the action to reverse the entries
     */
    public CheckBox getRverseRowsCheckbox() {
        return reverseRowsCheckbox;
    }
}