import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

public class SectionColChoicePanel extends JPanel implements ActionListener{
	
	private static final long serialVersionUID = 1L;
	
//	private static boolean macOS;
	
	private static JFrame _sectionColChoiceFrame;
	private static JButton _finishChooseColBtn, _upgradeLevelBtn, _downgradeLevelBtn, 
						_setDefaultLevelBtn, _setDefaultColBtn, _changeColNumBtn;
//						_saveConfigBtn, _applyChangeBtn;
	private static JScrollPane _sectionParamPane;
	private static JPanel _leftPanel, _rightPanel,  _southBtnsPanel;
	private static PreProcess _preProcess;
//	private static JFileChooser _fileChooser;
//	private static FileDialog _fileDialog;
	private static ConfigurationPanel _configPanel;
	
	private static JList<String> _sectionParamList;
	private static JComboBox<Integer> _sectionColChoiceCombobox;
	private static DefaultListModel<String> _sectionParamListModel;
	
	private static Integer[] _sectionColChoice = { 1, 2, 3};
	private static String _listDisplaySpacing = "                    ";
	
	private static Font _titleFont = new Font("Arial", Font.ITALIC, 18);

	public SectionColChoicePanel(JFrame sectionColChoiceFrame, PreProcess preProcess) {
		super();
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
//		this.add(Box.createHorizontalGlue());
		
		_preProcess = preProcess;
		_sectionColChoiceFrame = sectionColChoiceFrame;
		
		_leftPanel = new JPanel();
		_leftPanel.setLayout(new BoxLayout(_leftPanel, BoxLayout.Y_AXIS));
		TitledBorder sectionParamPanelTitledBorder = 
				new TitledBorder("Please select the sections that need to change title level or column number: ");
		sectionParamPanelTitledBorder.setTitleFont(_titleFont);
		_leftPanel.setBorder(sectionParamPanelTitledBorder);
		
		_sectionParamListModel = new DefaultListModel<String>();
		createSectionParamListModel();
		_sectionParamList = new JList<String>(_sectionParamListModel);
		_sectionParamList.setFont( _sectionParamList.getFont().deriveFont(Font.PLAIN));
		_sectionParamPane = new JScrollPane(_sectionParamList);
		TitledBorder sectionParamTitledBorder = new TitledBorder("USC Catalogue Sections: ");
		sectionParamTitledBorder.setTitleFont(_titleFont);
		_sectionParamPane.setViewportBorder(sectionParamTitledBorder);
		
		_sectionParamPane.setAlignmentX(LEFT_ALIGNMENT);
		_leftPanel.add(_sectionParamPane);

		JPanel changeTitleBtnPanel = new JPanel();
		changeTitleBtnPanel.setLayout(new BoxLayout(changeTitleBtnPanel, BoxLayout.X_AXIS));
		TitledBorder changeTitledBtnBorder = new TitledBorder("Press the button to change selected sections' title level: ");
		changeTitledBtnBorder.setTitleFont(_titleFont);
		changeTitleBtnPanel.setBorder(changeTitledBtnBorder);

		_upgradeLevelBtn = new JButton("Upgrade title Level");
		_upgradeLevelBtn.addActionListener(this);
		_downgradeLevelBtn = new JButton("Downgrade title Level");
		_downgradeLevelBtn.addActionListener(this);
		_setDefaultLevelBtn = new JButton("Reset to default title levels.");
		_setDefaultLevelBtn.addActionListener(this);
		
		changeTitleBtnPanel.add(_upgradeLevelBtn);
		changeTitleBtnPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		changeTitleBtnPanel.add(_downgradeLevelBtn);
		changeTitleBtnPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		changeTitleBtnPanel.add(_setDefaultLevelBtn);
		changeTitleBtnPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		_sectionColChoiceCombobox = new JComboBox<Integer>(_sectionColChoice);
		_sectionColChoiceCombobox.setSelectedIndex(1);
		Dimension preferredSize = _sectionColChoiceCombobox.getPreferredSize();
        preferredSize.height = 25;
        _sectionColChoiceCombobox.setMaximumSize(preferredSize);
		_changeColNumBtn = new JButton("Change column number.");
		_changeColNumBtn.addActionListener(this);
		_setDefaultColBtn = new JButton("Reset to default column numbers.");
		_setDefaultColBtn.addActionListener(this);
		
		JPanel changeColBtnPanel = new JPanel();
		changeColBtnPanel.setLayout(new BoxLayout(changeColBtnPanel, BoxLayout.X_AXIS));
		TitledBorder changeColBtnBorder = new TitledBorder("Press the button to change selected sections' column number: ");
		changeColBtnBorder.setTitleFont(_titleFont);
		changeColBtnPanel.setBorder(changeColBtnBorder);
		
		changeColBtnPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		changeColBtnPanel.add(_sectionColChoiceCombobox);
		changeColBtnPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		changeColBtnPanel.add(_changeColNumBtn);
		changeColBtnPanel.add(Box.createRigidArea(new Dimension(50, 0)));
		changeColBtnPanel.add(_setDefaultColBtn);
		changeColBtnPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		_leftPanel.add(changeTitleBtnPanel);
		_leftPanel.add(changeColBtnPanel);
		_leftPanel.setPreferredSize(
		 				new Dimension(_sectionColChoiceFrame.getWidth()*3/5, _sectionColChoiceFrame.getHeight()));
		this.add(_leftPanel);
		
		_rightPanel = new JPanel();
		_rightPanel.setLayout(new BoxLayout(_rightPanel, BoxLayout.Y_AXIS));
		TitledBorder rightTitledBorder = new TitledBorder("Please select the cell that needs to be changed: ");
		rightTitledBorder.setTitleFont(_titleFont);
		_rightPanel.setBorder(rightTitledBorder);
		
		try {
			_configPanel = new ConfigurationPanel(_sectionColChoiceFrame,_preProcess);
		} catch (FatalErrorException e1) {
			e1.printStackTrace();
		}
		
//		JPanel changeConfigPanel = new JPanel();
//		changeConfigPanel.setLayout(new BoxLayout(changeConfigPanel, BoxLayout.X_AXIS));
//		_textField = new JTextField();
//		_textField.setMaximumSize(new Dimension(_textField.getMaximumSize().width,_textField.getMinimumSize().height));
//		_applyChangeBtn = new JButton("Apply Change.");
//		_applyChangeBtn.addActionListener(this);
//		_saveConfigBtn = new JButton("Save the config mapping...");
//		_saveConfigBtn.addActionListener(this);
//		changeConfigPanel.add(_textField);
//		changeConfigPanel.add(Box.createRigidArea(new Dimension(10, 0)));
//		changeConfigPanel.add(_applyChangeBtn);
//		changeConfigPanel.add(Box.createRigidArea(new Dimension(20, 0)));
//		changeConfigPanel.add(_saveConfigBtn);
		
		_rightPanel.add(_configPanel);
//		_rightPanel.add(changeConfigPanel);
		_rightPanel.setPreferredSize(
 				new Dimension(_sectionColChoiceFrame.getWidth()*2/5, _sectionColChoiceFrame.getHeight()));
		this.add(_rightPanel);
		
		_southBtnsPanel = new JPanel();
		_sectionColChoiceFrame.add(_southBtnsPanel, BorderLayout.SOUTH);
		_finishChooseColBtn = new JButton("Start Conversion.");
		_finishChooseColBtn.addActionListener(this);
//		_saveConfigBtn = new JButton("Save the config mapping...");
//		_saveConfigBtn.addActionListener(this);
		_southBtnsPanel.add(_finishChooseColBtn);
//		_southBtnsPanel.add(_saveConfigBtn);

//		//TODO: change directory to be user input working directory.
//		if (macOS) {
//			_fileDialog = new FileDialog(_sectionColChoiceFrame, "Save As...", FileDialog.SAVE);
//			_fileDialog.setDirectory(System.getProperty("user.dir"));
//		} else {
//			_fileChooser = new JFileChooser();
//			_fileChooser.setDialogTitle("Save As...");
//			_fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
//			_fileChooser.setAcceptAllFileFilterUsed(false);
//		}
		
		//TODO: 3. add save output file button???
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == _finishChooseColBtn) {
			startConversion();
			JOptionPane.showMessageDialog(this, "Finish Conversion to Latex. Output has been saved to ...");
		} else if (e.getSource() == _upgradeLevelBtn) {
			addSectionLevel(-1);
		} else if (e.getSource() == _downgradeLevelBtn) {
			addSectionLevel(1);
		} else if (e.getSource() == _setDefaultLevelBtn) {
			_preProcess.resetCustomizedSectionParamsByLevels();
			resetSectionParamListModel();
		} else if (e.getSource() == _changeColNumBtn) {
			changeColNum((int)_sectionColChoiceCombobox.getSelectedItem());
		} else if (e.getSource() == _setDefaultColBtn) {
			_preProcess.resetCustomizedSectionParamsByColNums();
			resetSectionParamListModel();
		} 
//		else if (e.getSource() == _saveConfigBtn) {
//			saveConfig();
//		} else if (e.getSource() == _applyChangeBtn) {
//			//TODO:
//		}

		this.revalidate();
		this.repaint();
	}

	
	private void createSectionParamListModel() {
		for (Map.Entry<String, int[]> entr : _preProcess.getCustomizedSectionParams()) {
			_sectionParamListModel.addElement(createListElem(entr));;
		}
	}
	
	private void resetSectionParamListModel() {
		//TODO: non-ascii char in title.
		int index = 0;
		for (Map.Entry<String, int[]> entr : _preProcess.getCustomizedSectionParams()) {
			_sectionParamListModel.set(index, createListElem(entr));
			index ++;
		}
	}
	
	private String createListElem(Map.Entry<String, int[]> entr) {	
		String elem = "";
		for (int i = 0; i < entr.getValue()[0]; i++) {
			elem += _listDisplaySpacing;
		}
		elem += entr.getKey() + " : Title Level = " + entr.getValue()[0] + 
				", Column number = " + entr.getValue()[1];
		return elem;
	}
	
	private void addSectionLevel(int offset) {
		int[] selectedIndices = _sectionParamList.getSelectedIndices();
		for (int index : selectedIndices) {
			_preProcess.addCustomizedSectionLevels(index, offset);
		}
		
		//update JList's title level
		int lastChangedIndex = -1;
		for (int index : selectedIndices) {
			if (index <= lastChangedIndex) continue;
			int selectedlevel = _preProcess.getCustomizedSectionParams().get(index).getValue()[0];
			for (int i = index; i < _preProcess.getCustomizedSectionParams().size(); i++) {
				Map.Entry<String, int[]> entr = _preProcess.getCustomizedSectionParams().get(i);
				if (i != index && entr.getValue()[0] <= selectedlevel) {
					lastChangedIndex = i - 1;
					break;
				}
				_sectionParamListModel.set(i, createListElem(entr));
			}
		}
	}
	
	private void changeColNum(int colNum) {
		int[] selectedIndices = _sectionParamList.getSelectedIndices();
		for (int index : selectedIndices) {
			Map.Entry<String, int[]> entr = _preProcess.setCustomizedSectionColNums(index, colNum);
			_sectionParamListModel.set(index, createListElem(entr));
		}
	}

	public void startConversion() {
		try {
			Parser parser = new Parser();
			parser.parse(new File(Main.getProcessedFile()), new ParserHandler(new File(Main.getOutputFile()), _preProcess));
			LatexCompilerExecutor.CompileLatexFile(Main.getOutputFile());
		} catch (FatalErrorException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		} catch (Exception e) {
			e.getMessage();
			e.printStackTrace();
		}
	}
}
