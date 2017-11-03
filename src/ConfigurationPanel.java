import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;

public class ConfigurationPanel extends JTabbedPane {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3087284870831158695L;
	JTabbedPane _main_pane;
	Configuration _config;
	JPanel _command_config_panel;
	JPanel _char_config_panel;
	JTable _element_table;
	JTable _char_config_table;
	JPanel _main_panel;
	PreProcess _preprocess;
	
	public ConfigurationPanel(PreProcess preprocess) throws FatalErrorException {
		super();
		_preprocess = preprocess;
		_config = new Configuration();
		_command_config_panel = new JPanel();
		_char_config_panel = new JPanel();
		
		PopulateCommandConfig();
		PopulateCharConfig();
	}
	
	private void PopulateCommandConfig() {
		HashMap<String, ElementConfigItem> elements = _config.get_elements();
		Object[][] elements_info = new Object[elements.size()][5];
		int row_idx = 0;
		for(Map.Entry<String, ElementConfigItem> entry: elements.entrySet()) {
			elements_info[row_idx][0] = entry.getKey();
			elements_info[row_idx][1] = entry.getValue().getStart();
			elements_info[row_idx][2] = entry.getValue().getEnd();
			elements_info[row_idx][3] = entry.getValue().ignoreContent();
			elements_info[row_idx][4] = entry.getValue().ignoreStyles();
			row_idx++;
		}
		String[] column_names = {"Name", "Start", "End", "IgnoreContent", "IgnoreStyles"};
		_element_table = new JTable(elements_info,  column_names){
		    public String getToolTipText( MouseEvent e )
		    {
		        int row = rowAtPoint( e.getPoint() );
		        int column = columnAtPoint( e.getPoint() );

		        Object value = getValueAt(row, column);
		        return value == null ? null : value.toString();
		    }
		};
		JScrollPane scrollPane = new JScrollPane(_element_table);
		_element_table.setFillsViewportHeight(true);
		this.addTab("HTML Element", scrollPane);
	}
	
	private void PopulateCharConfig() {
		HashMap<Integer, String> elements = _config.get_charsNum();
		HashSet<Character> non_ascii_set = _preprocess.getNonAsciiSet();
		HashSet<Character> non_config_chars = new HashSet();
		for(Character c : non_ascii_set) {
			if(!elements.containsKey((int)c)) {
				non_config_chars.add(c);
			}
		}
		
		Object[][] elements_info = new Object[elements.size() + non_config_chars.size()][2];
		int row_idx = 0;
		for(Map.Entry<Integer, String> entry: elements.entrySet()) {
			elements_info[row_idx][0] = entry.getKey();
			elements_info[row_idx][1] = entry.getValue();
			row_idx++;
		}
		
		for(Character c : non_config_chars) {
			System.out.println("Not found: " + (int)c);
			elements_info[row_idx][0] = (int)c;
			elements_info[row_idx][1] = "UNDEFINED";
			row_idx++;
		}
		
		String[] column_names = {"Char Num", "convertTo"};
		_element_table = new JTable(elements_info,  column_names);
		JScrollPane scrollPane = new JScrollPane(_element_table);
		_element_table.setFillsViewportHeight(true);
		this.addTab("Config", scrollPane);
	}

}
