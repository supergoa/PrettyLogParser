package log_parser;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Nicholas Ostaffe on 6/23/2016.
 * Represents one file that has been opened in the program and begins the dynamic creation of FX objects.
 *
 * FXML Hierarchy: TabPane -> Tabs -> ScrollPane -> VBox -> TitledPanes -> TextAreas
 *
 * Content of a Tab is encompassed in a Log object, which handles searching, sorting, and creation of entries.
 * Furthermore, Log is responsible for reading in the file chosen and parsing new entries.
 */
public class Log {

	private String name;
	private List<Entry> entryList = new LinkedList<>();
    private List<Entry> currentList = entryList;
    private static List<String> entryTypes = new ArrayList<>();
    private final File file;
	private boolean isReversed = false;

	public static final int INITIAL_LOG_COUNT = 50;
	public static final int INCREMENTAL_LOG_COUNT = 20;
	private BufferedReader reader;
	private int rowCount = 0;

	private static Stage stage = Main.getStage();
	private Tab t;
	private VBox v;
	private ScrollPane sp;


	public Log(String name, File file) {
		this.name = name;
        this.file = file;
		t = new Tab(name);
		v = new VBox();
		sp = new ScrollPane();
		setScrollPaneProperties();
		setTabProperties();
		readInFile();
        stage.show();
    }

	/**
	 *
	 * @param type represents a type for a given Entry
	 * @return the index(representing a priority level) of the type
	 */
	public static int getTypePriority(String type) {
		return entryTypes.indexOf(type);
	}

    /**
     * Only add unique types to the List. This list is created to manage the priority of certain
     * entry types so there is a known way to sort them by type later.
     * @param type is the Type to be added to the local List
     */
	public static void addEntryType(String type) {
		if (!entryTypes.contains(type)) {
			entryTypes.add(type);
		}
	}

	/**
	 * Adds @param entry to the local log
	 * 
	 * @param entry to add the entryList
	 */
	public void addEntry(Entry entry) {
		this.entryList.add(entry);
	}

	/**
	 *
	 * @return the title of the log
	 */
	public String getName() {
		return name;
	}

	/**
	 *
	 * @return the list of all Entries in the log
	 */
	public List<Entry> getEntryList() {
		return entryList;
	}

	/**
	 * Searches Entries with AND/OR logic and adds them to an ArrayList to
	 * return
	 * 
	 * @param keyword to search for
	 * @return an ArrayList Entries that had word(s) matching the keyword
	 */
	public List<Entry> search(String keyword) {
		LinkedList<Entry> ret = new LinkedList<>();
		String keywords[] = null;

		if(keyword.equals("")) {
			currentList = entryList;
			return entryList;
		}

		// Ensure all keywords are present in the entry if AND logic is found
		if (keyword.contains("AND(")) {
			int size = keyword.split("AND\\(")[1].split(",").length;
			keywords = new String[size];
			for (int i = 0; i < keywords.length - 1; i++) {
				keywords[i] = keyword.split("AND\\(")[1].split(",")[i];
			}
			keywords[size - 1] = keyword.split("AND\\(")[1].split(",")[size - 1].replace(")", "");

			int containCount = 0;
			for (Entry e : entryList) {
				for (int i = 0; i < keywords.length; i++) {
					if (e.getEntry().toLowerCase().contains(keywords[i].toLowerCase())) {
						containCount++;
					}
				}
				if (containCount == size) {
					ret.add(e);
				}
				containCount = 0;
			}
		}

		// Ensure at least one keyword is present in the entry if OR logic is
		// found
		else if (keyword.contains("OR(")) {
			int size = keyword.split("OR\\(")[1].split(",").length;
			keywords = new String[size];
			for (int i = 0; i < keywords.length - 1; i++) {
				keywords[i] = keyword.split("OR\\(")[1].split(",")[i];
			}
			keywords[size - 1] = keyword.split("OR\\(")[1].split(",")[size - 1].replace(")", "");

			boolean containFlag = false;
			for (Entry e : entryList) {
				for (int i = 0; i < keywords.length; i++) {
					if (e.getEntry().toLowerCase().contains(keywords[i].toLowerCase())) {
						containFlag = true;
					}
				}
				if (containFlag) {
					ret.add(e);
				}
				containFlag = false;
			}
		}
		// search on the raw string
		else {
			for (Entry e : entryList) {
				if (e.getEntry().toLowerCase().contains(keyword.toLowerCase())) {
					ret.add(e);
				}
			}
		}
        this.currentList = ret;
		return ret;
	}

    /**
     * Responsible for detecting new entries in the file selected and adds them to
     * this Log's local list of Entries.
     */
	private void readInFile() {
		// open the file
        try {
            rowCount = 0;
            reader = new BufferedReader(new FileReader(file));

            boolean firstLoop = true;
            String currentLine = "";
            String builtLine = "";
            // read until end of file
            while ((currentLine = reader.readLine()) != null) {

                // populate builtLine on first cycle
                if (firstLoop) {
                    builtLine = currentLine;
                }

                // Checks that currentLine has a format: "Number-Number-Number"
                if (currentLine.length() >= 10 && currentLine.substring(0, 10).matches("([0-9]+-+[0-9]+-+[0-9]+)+")) {
                    Entry entry = new Entry(builtLine);
                    this.addEntry(entry);

                    if (rowCount <= INITIAL_LOG_COUNT) {
                        rowCount++;
                        if (!entry.isVisible()) {
                            v.getChildren().add(entry.getTitledPane());
                            entry.setVisible(true);
                        }
                        stage.show();
                    }
                    if (firstLoop) {
                        currentLine = reader.readLine();
                        firstLoop = false;
                    }

                    builtLine = "";
                }
                builtLine += currentLine + System.lineSeparator();
            }

            // Catch the last line of the entry
            // Checks that builtLine has a format: "Number-Number-Number"
            if (builtLine.length() >= 10 && builtLine.substring(0, 10).matches("([0-9]+-+[0-9]+-+[0-9]+)+")) {
                Entry entry = new Entry(builtLine);
                addEntry(entry);
                if (rowCount <= INITIAL_LOG_COUNT) {
                    rowCount++;
                    if (!entry.isVisible()) {
                        v.getChildren().add(entry.getTitledPane());
                        entry.setVisible(true);
                    }
                    stage.show();
                }
            }

        }
		catch (Exception ex) {
            ex.printStackTrace();
        }
	}

    /**
     * Sets up an event which is triggered when a user scrolls down to the end of a page.
     * It manages loading the file further by displaying 20 new entries at a time. The already
     * built VBox is also added to the ScrollPane during this time.
     */
    private void setScrollPaneProperties() {
		sp.vvalueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
				if ((double) new_val >= .99) {
					int size = 0;
					int startIndex = rowCount;
                    if (rowCount + INCREMENTAL_LOG_COUNT < currentList.size()) {
						rowCount += INCREMENTAL_LOG_COUNT;
						size = rowCount;
					} else {
						size = currentList.size();
						rowCount = size;
					}
					for (int i = startIndex; i < size; i++) {
						if (!currentList.get(i).isVisible()) {
							v.getChildren().add(currentList.get(i).getTitledPane());
							currentList.get(i).setVisible(true);
						}
						stage.show();
					}
					v.setVisible(true);
					stage.show();
				}
			}
		});
        sp.setContent(v);
	}

    /**
     * Sets the tab's closing events and to it the already built ScrollPane
     */
    private void setTabProperties() {
		t.setOnClosed(t1 -> {

			// Remove log instances associated with closing tab(s)
			Tab temp = (Tab) t1.getSource();
			for (int i = 0; i < Main.getLogviewController().getLogs().size(); i++) {
                if (Main.getLogviewController().getLogs().get(i).getName().equals(temp.getText())) {
					Main.getLogviewController().getLogs().remove(i);
					i--;
				}
			}

			// Disable 'sort', 'search', and 'collapse all' features if no other tabs are open
			if (Main.getLogviewController().getTabPane().getTabs().isEmpty()) {
				Main.getLogviewController().getCollapseButton().setDisable(true);
				Main.getLogviewController().getSortBy().setDisable(true);
				Main.getLogviewController().getSearchBox().setDisable(true);
                Main.getLogviewController().getRverseRowsCheckbox().setDisable(true);
				stage.show();
			}
		});
        Main.getLogviewController().getTabPane().getTabs().add(t);
        t.setContent(sp);
        t.setClosable(true);
	}

    /**
     *
     * @param rowCount is the int to set to this Log's local rowCount
     */
    public void setRowCount(int rowCount){
        this.rowCount = rowCount;
    }


    /**
     *
     * @return the current list that the is being displayed on the screen
     */
    public List<Entry> getCurrentList() {
        return currentList;
    }


    /**
     * INITIAL_LOG_COUNT represents the initial amount of entries that should be initially loaded onto the screen
     * @return the local final initial log count
     */
    public static int getInitialLogCount() {
        return INITIAL_LOG_COUNT;
    }

    /**
     *
     * @return the VBox responsible for holding all entries of this Log
     */
    public VBox getVBox() {
        return v;
    }
}
