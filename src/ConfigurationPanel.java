import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.TableModel;

public class ConfigurationPanel extends JTabbedPane{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3087284870831158695L;
	Configuration _config;
	JTable _element_table;
	JTable _char_config_table;
	JPanel _main_panel;
	PreProcess _preprocess;
	
	public ConfigurationPanel(PreProcess preprocess) throws FatalErrorException {
		super();
		_preprocess = preprocess;
		_config = new Configuration();

		PopulateCommandConfig();
		PopulateCharConfig();
	}
	
	private void PopulateCommandConfig() {
		HashMap<String, ElementConfigItem> elements = _config.get_elements();
		Object[][] elements_info = new Object[elements.size()][6];
		int row_idx = 0;
		for(Map.Entry<String, ElementConfigItem> entry: elements.entrySet()) {
			elements_info[row_idx][0] = entry.getKey();
			elements_info[row_idx][1] = entry.getValue().getStart();
			elements_info[row_idx][2] = entry.getValue().getEnd();
			elements_info[row_idx][3] = entry.getValue().leaveText();
			elements_info[row_idx][4] = entry.getValue().ignoreContent();
			elements_info[row_idx][5] = entry.getValue().ignoreStyles();
			row_idx++;
		}
		String[] column_names = {"Name", "Start", "End", "LeaveText", "IgnoreContent", "IgnoreStyles"};
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
		HashSet<Character> non_config_chars = new HashSet<Character>();
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
		_char_config_table = new JTable(elements_info,  column_names);
		JScrollPane scrollPane = new JScrollPane(_char_config_table);
		_char_config_table.setFillsViewportHeight(true);
		this.addTab("Config", scrollPane);
	}
	
	public void saveCharConfig(Configuration config_to_save) {
		TableModel char_config_table_model = _char_config_table.getModel();
		int row_count = char_config_table_model.getRowCount();
		for(int i = 0; i < row_count; i++) {
			Integer charNum = (Integer) char_config_table_model.getValueAt(i, 0);
			String convertTo = (String) char_config_table_model.getValueAt(i, 1);
			config_to_save.get_charsNum().put(charNum, convertTo);
		}
	}
	
	public void saveCommandConfig(Configuration config_to_save) {
		TableModel element_table_model = _element_table.getModel();
		int row_count = element_table_model.getRowCount();
		for(int i = 0; i < row_count; i++) {
			String element_name = (String) element_table_model.getValueAt(i, 0);
			config_to_save.get_elements().put(element_name, 
					new ElementConfigItem((String) element_table_model.getValueAt(i, 1),
							(String) element_table_model.getValueAt(i, 2),
							(boolean) element_table_model.getValueAt(i, 3) ? "yes" : "no",
							(boolean) element_table_model.getValueAt(i, 4) ? "yes" : "no",
							(boolean) element_table_model.getValueAt(i, 5) ? "yes" : "no"));
		}
	}
	
	public void saveConfiguration(String filePath){
		Configuration config_to_save = new Configuration(_config);
		saveCommandConfig(config_to_save);
		saveCharConfig(config_to_save);
		try {
			config_to_save.saveConfiguration(filePath);
		} catch (FatalErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
