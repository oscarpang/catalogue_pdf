import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
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
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

public class UserSettingFrame extends JFrame implements ActionListener{
	
	private static final long serialVersionUID = 1L;
	
	private static JButton _startConversionBtn, _upgradeLevelBtn, _downgradeLevelBtn, 
						_setDefaultLevelBtn, _setDefaultColBtn, _changeColNumBtn;
	private static JScrollPane _sectionParamPane;
	private static JPanel _contentPanel, _leftPanel, _rightPanel,  _southBtnsPanel;
	private static PreProcess _preProcess;
	private static ConfigurationPanel _configPanel;
	
	private static JList<String> _sectionParamList;
	private static JComboBox<Integer> _sectionColChoiceCombobox;
	private static DefaultListModel<String> _sectionParamListModel;
	
	private static Integer[] _sectionColChoice = { 1, 2, 3};
	private static String _listDisplaySpacing = "                    ";
	
	private static Font _titleFont = new Font("Arial", Font.ITALIC, 18);
	
	private static boolean macOS;

	public UserSettingFrame(String name, PreProcess preProcess) {
		super("USC Catalogue Print to PDF");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setBounds(50, 50, 1500, 800);
		
		_preProcess = preProcess;
		String osName = System.getProperty("os.name");
	    macOS = osName.indexOf("Mac") >= 0 ? true : false;
		
		_contentPanel = new JPanel();
		_contentPanel.setLayout(new BoxLayout(_contentPanel, BoxLayout.X_AXIS));
		_contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
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
		 				new Dimension(this.getWidth()*3/5, this.getHeight()));
		_contentPanel.add(_leftPanel);
		
		_rightPanel = new JPanel();
		_rightPanel.setLayout(new BoxLayout(_rightPanel, BoxLayout.Y_AXIS));
		TitledBorder rightTitledBorder = new TitledBorder("Please select the cell that needs to be changed: ");
		rightTitledBorder.setTitleFont(_titleFont);
		_rightPanel.setBorder(rightTitledBorder);
		
		try {
			_configPanel = new ConfigurationPanel(this,_preProcess);
		} catch (FatalErrorException e1) {
			e1.printStackTrace();
		}
		
		_rightPanel.add(_configPanel);
		_rightPanel.setPreferredSize(
 				new Dimension(this.getWidth()*2/5, this.getHeight()));
		_contentPanel.add(_rightPanel);
		
		_southBtnsPanel = new JPanel();
		_startConversionBtn = new JButton("Start Conversion.");
		_startConversionBtn.addActionListener(this);
		_southBtnsPanel.add(_startConversionBtn);
		this.add(_southBtnsPanel, BorderLayout.SOUTH);
		
		this.add(_contentPanel, BorderLayout.CENTER);
		
		

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
		
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == _startConversionBtn) {
			String inputFile = Main.getHtmlFile();
			String name = inputFile.substring(0, inputFile.indexOf(".html"));
			while (name.contains(System.getProperty("file.separator"))){
				name = name.substring(name.indexOf(System.getProperty("file.separator")) + 1);
			}
			String workingDir = Main.getWorkingDir();
//			Main.setProcessedFile(workingDir + name + "_processed.html");
			Main.setOutputFile(workingDir + name + ".tex");
			System.out.println(Main.getProcessedFile() + "---" + Main.getOutputFile());
			
			//TODO: Prompt a file chooser to save output file.
			if (macOS || handleIfExist(Main.getOutputFile())) {
				startConversion();
				JOptionPane.showMessageDialog(this, "Finish Conversion to Latex. Output has been saved to ...");
			}
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

		this.revalidate();
		this.repaint();
	}
	
	private boolean handleIfExist(String name) {
		File file = new File(name);
		if (file.exists()) {
			int reply = JOptionPane.showConfirmDialog(null, "Replace existing output file?", 
					"Replace exisitng output file?", JOptionPane.YES_NO_OPTION);
			if (reply == JOptionPane.NO_OPTION) {
				//TODO: add optionpane to get file name.
//				String name = JOptionPane.showInputDialog(frame, "What's your name?");
				return false;
			}
		}
		return true;
	}
	
	private void createSectionParamListModel() {
		for (Map.Entry<String, int[]> entr : _preProcess.getCustomizedSectionParams()) {
			_sectionParamListModel.addElement(createListElem(entr));;
		}
	}
	
	private void resetSectionParamListModel() {
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
			File preProcessFile = new File(Main.getProcessedFile());
			preProcessFile.delete();
			System.out.println("-----BEFORE CONVERT LATEX TO PDF-----");
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
