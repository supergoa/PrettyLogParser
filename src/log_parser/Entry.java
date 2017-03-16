package log_parser;

import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;

/**
 * Created by Nicholas Ostaffe on 6/23/2016. Represents a single Entry(row) in a
 * Log and is responsible for further parsing of the file, GUI components, and
 * sorting logic
 */
public class Entry implements Comparable<Entry> {
	/**
	 * The raw text for the entry
	 */
	private String entry;
	private String date;
	private String type;
	private TitledPane tp;
	private TextArea ta;
	private static String sortOption;
	private boolean isVisible = false;
	private boolean titledPaneCreated = false;

	/**
	 * Further parses @param entry futher into local type and date variables and
	 * builds GUI objects around the entry
	 * 
	 * @param entry is the entire String from the initial entry parsed on the date Regex
	 */
	public Entry(String entry) {
		this.entry = entry;
		parsePhrase();
		try {
			date = (entry.length() > 24) ? entry.substring(0, 24).replaceAll("(\\s+)|(-)|(,)|(:)", "") : entry.replaceAll("(\\s+)|(-)|(,)|(:)", "");
			type = (entry.length() > 50) ? entry.substring(0, 50).split(" ")[2] : entry.split(" ")[2];
		} catch (ArrayIndexOutOfBoundsException e) {
		}

		// Send type to Log to export to file so the type can be inferred in the future.
		Log.addEntryType(type);
	}

	/**
	 * Parses found word segments and separates them with a newline character
	 */
	private void parsePhrase() {
		boolean foundOpenParenthesis = false;
		boolean foundOpenBracket = false;
		int openParenthesisCount = 0;
		int openBracketCount = 0;

		for (int i = 0; i < entry.length(); i++) {
			char c = entry.charAt(i);
			if (c == '(') {
				if (foundOpenParenthesis) {
					openParenthesisCount++;
				} else {
					foundOpenParenthesis = true;
				}
			}
			if (c == '[') {
				if (foundOpenBracket) {
					openBracketCount++;
				} else {
					foundOpenBracket = true;
				}
			}
			if (foundOpenParenthesis && c == ')') {
				if (openParenthesisCount != 0) {
					openParenthesisCount--;
				} else {
					entry = entry.substring(0, i + 1) + System.lineSeparator() + entry.substring(i + 1);
					foundOpenParenthesis = false;
				}
			}
			if (foundOpenBracket && c == ']') {
				if (openBracketCount != 0) {
					openBracketCount--;
				} else {
					entry = entry.substring(0, i + 1) + System.lineSeparator() + entry.substring(i + 1);
					foundOpenBracket = false;
				}
			}
		}
	}

	/**
	 * Manages the sorting option for all Entries
	 * 
	 * @param sortOption is the selected sorting option in the GUI
	 */
	public static void setSortOption(String sortOption) {
		Entry.sortOption = sortOption;
	}

	/**
	 * Attaches style sheets to Entries of specific types
	 */
	private void setEntryColor() {
		if (this.type.equals("WARN"))
			tp.getStylesheets().add(this.getClass().getResource("WarningEntry.css").toExternalForm());
		else if (this.type.equals("INFO"))
			tp.getStylesheets().add(this.getClass().getResource("InformationEntry.css").toExternalForm());
		else if (this.type.equals("ERROR"))
			tp.getStylesheets().add(this.getClass().getResource("ErrorEntry.css").toExternalForm());
		else if (this.type.equals("DEBUG"))
			tp.getStylesheets().add(this.getClass().getResource("DebugEntry.css").toExternalForm());
		else
			tp.getStylesheets().add(this.getClass().getResource("DefaultEntry.css").toExternalForm());
	}
	
	public String getEntry() {
		return entry;
	}

	public String getTitle() {
		return ((entry.length() > 160) ? entry.substring(0, 160).trim() : entry).replace(System.lineSeparator(), "").trim() + "...";
	}

	public TitledPane getTitledPane() {
		if(!titledPaneCreated) {
			// TitledPane creates a dropdown functionality
			tp = new TitledPane();
			tp.setExpanded(false);
			setEntryColor(); // colors!
			tp.setText(this.getTitle());
			tp.setMaxWidth(1000);

			// TextArea to be stored in TitledPane
			ta = new TextArea(this.getEntry());
			ta.setPrefSize(999, 100);
			ta.setEditable(false);
			DragResizer.makeResizable(ta);

			// Add the TextArea to the TitledPane
			tp.setContent(ta);
			titledPaneCreated = true;
		}
		return tp;
	}

	/**
	 * The sorting logic for Entry types which is based on the local date and
	 * type variables
	 * 
	 * @param other object to be compared to
	 * @return -1, 0, or 1 depending on the comparison of 'this' and 'other'
	 */
	@Override
	public int compareTo(Entry other) {
		if (sortOption.equals("Date")) {
			if (this.date.equals(other.date)) {
				return 0;
			} else if (this.date.compareTo(other.date) > 0) {
				return 1;
			} else if (this.date.compareTo(other.date) < 0) {
				return -1;
			}

		} else if (sortOption.equals("Type")) {
			if (this.type.equals(other.type)) {
				return 0;
			} else if (Log.getTypePriority(this.type) > Log.getTypePriority(other.type)) {
				return 1;
			} else if (Log.getTypePriority(this.type) < Log.getTypePriority(other.type)) {
				return -1;
			}
		}

		// Execution should not reach this point
		System.err.println("Something went wrong in compareTo");
		return 0;
	}

	/**
	 *
	 * @return the Entry's title
     */
	@Override
	public String toString() {
		return getTitle();
	}

	/**
	 *
	 * @return whether the entry is currently visible
     */
	public boolean isVisible() {
		return isVisible;
	}

	/**
	 *
	 * @param visible is the boolean to set the local visible variable
     */
	public void setVisible(boolean visible) {
		isVisible = visible;
	}

	/**
	 *
	 * @return true if the TitledPane for this entry has been created and false otherwise
     */
	public boolean titledPaneWasCreated() {
		return titledPaneCreated;
	}
}
