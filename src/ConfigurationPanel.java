import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;

public class ConfigurationPanel extends JPanel {
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
	
	public ConfigurationPanel() throws FatalErrorException {
		super();
		_config = new Configuration();
		
		this.setLayout(new BorderLayout());
		_main_pane = new JTabbedPane();
		_command_config_panel = new JPanel();
		_char_config_panel = new JPanel();
		PopulateCommandConfig();
		PopulateCharConfig();
		_main_pane.addTab("Html Element", _command_config_panel);
		_main_pane.addTab("Char Num", _char_config_panel);
		this.add(_main_pane, BorderLayout.CENTER);
		
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
		_element_table = new JTable(elements_info,  column_names);
		JScrollPane scrollPane = new JScrollPane(_element_table);
		_element_table.setFillsViewportHeight(true);
		_command_config_panel.add(scrollPane);
	}
	
	private void PopulateCharConfig() {
		HashMap<Integer, String> elements = _config.get_charsNum();
		Object[][] elements_info = new Object[elements.size()][2];
		int row_idx = 0;
		for(Map.Entry<Integer, String> entry: elements.entrySet()) {
			elements_info[row_idx][0] = entry.getKey();
			elements_info[row_idx][1] = entry.getValue();
			row_idx++;
		}
		String[] column_names = {"Char Num", "convertTo"};
		_element_table = new JTable(elements_info,  column_names);
		JScrollPane scrollPane = new JScrollPane(_element_table);
		_element_table.setFillsViewportHeight(true);
		_char_config_panel.add(scrollPane);
	}

}
