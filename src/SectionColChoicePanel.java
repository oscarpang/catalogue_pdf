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

public class SectionColChoicePanel extends JPanel implements ActionListener{
	
	private static final long serialVersionUID = 1L;
	
	private static JFrame _sectionColChoiceFrame;

	private static JButton _finishChooseColBtn, _upgradeLevelBtn, _downgradeLevelBtn, 
						_setDefaultLevelBtn, _setDefaultColBtn, _changeColNumBtn;
	private static JScrollPane _sectionParamPane;
	private static JPanel _chooseBtnPanel;
	private static PreProcess _preProcess;

	private static JPanel _southBtnsPanel;
	private static ConfigurationPanel _configPanel;

	
	private static JList<String> _sectionParamList;
	private static JComboBox<Integer> _sectionColChoiceCombobox;
	private static DefaultListModel<String> _sectionParamListModel;
	
	private static Integer[] _sectionColChoice = { 1, 2, 3};
	private static String _listDisplaySpacing = "                    ";
//	private static String _listDisplaySpacing = "--------------------";
	
	private static Font _titleFont = new Font("Arial", Font.ITALIC, 18);

	public SectionColChoicePanel(JFrame sectionColChoiceFrame, PreProcess preProcess) {
		super();
		this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
//		this.add(Box.createHorizontalGlue());
		
		_preProcess = preProcess;
		_sectionColChoiceFrame = sectionColChoiceFrame;
		
		_sectionParamListModel = new DefaultListModel<String>();
		createSectionParamListModel();
		_sectionParamList = new JList<String>(_sectionParamListModel);
		_sectionParamList.setFont( _sectionParamList.getFont().deriveFont(Font.PLAIN));
		_sectionParamPane = new JScrollPane(_sectionParamList);
		TitledBorder sectionParamTitledBorder = new TitledBorder("USC Catalogue Chapters And Sections: ");
		sectionParamTitledBorder.setTitleFont(_titleFont);
		_sectionParamPane.setViewportBorder(sectionParamTitledBorder);
		this.add(_sectionParamPane);
		this.add(Box.createRigidArea(new Dimension(10, 0)));
		
		_chooseBtnPanel = new JPanel();
		_chooseBtnPanel.setLayout(new BoxLayout(_chooseBtnPanel, BoxLayout.Y_AXIS));
		
		_upgradeLevelBtn = new JButton("Upgrade title Level");
		_upgradeLevelBtn.addActionListener(this);
		_upgradeLevelBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
		_downgradeLevelBtn = new JButton("Downgrade title Level");
		_downgradeLevelBtn.addActionListener(this);
		_downgradeLevelBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
		_setDefaultLevelBtn = new JButton("Reset to default title levels.");
		_setDefaultLevelBtn.addActionListener(this);
		_setDefaultLevelBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		_sectionColChoiceCombobox = new JComboBox<Integer>(_sectionColChoice);
		_sectionColChoiceCombobox.setSelectedIndex(1);
		_sectionColChoiceCombobox.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		Dimension preferredSize = _sectionColChoiceCombobox.getPreferredSize();
        preferredSize.height = 25;
        _sectionColChoiceCombobox.setMaximumSize(preferredSize);
		
		_changeColNumBtn = new JButton("Change column number.");
		_changeColNumBtn.addActionListener(this);
		_changeColNumBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		_setDefaultColBtn = new JButton("Reset to default column numbers.");
		_setDefaultColBtn.addActionListener(this);
		_setDefaultColBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		_chooseBtnPanel.add(_upgradeLevelBtn);
		_chooseBtnPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		_chooseBtnPanel.add(_downgradeLevelBtn);
		_chooseBtnPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		_chooseBtnPanel.add(_setDefaultLevelBtn);
		_chooseBtnPanel.add(Box.createRigidArea(new Dimension(0, 50)));
		_chooseBtnPanel.add(_sectionColChoiceCombobox);
		_chooseBtnPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		_chooseBtnPanel.add(_changeColNumBtn);
		_chooseBtnPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		_chooseBtnPanel.add(_setDefaultColBtn);
		
		this.add(_chooseBtnPanel);
		this.add(Box.createRigidArea(new Dimension(10, 0)));
		try {
			_configPanel = new ConfigurationPanel(_preProcess);
		} catch (FatalErrorException e1) {
			e1.printStackTrace();
		}
		this.add(_configPanel);
		
		_southBtnsPanel = new JPanel();
		_sectionColChoiceFrame.add(_southBtnsPanel, BorderLayout.SOUTH);
		_finishChooseColBtn = new JButton("Done");
		_finishChooseColBtn.addActionListener(this);
		_southBtnsPanel.add(_finishChooseColBtn);
		
		//TODO: move btn. add save config button. add save output file button???
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == _finishChooseColBtn) {
			startConversion();
			JOptionPane.showMessageDialog(this, "Finish Conversion to Latex. Where do you want to save the file?");
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
	
	private void createSectionParamListModel() {
		for (Map.Entry<String, int[]> entr : _preProcess.getCustomizedSectionParams()) {
			_sectionParamListModel.addElement(createListElem(entr));;
		}
	}
	
	private void resetSectionParamListModel() {
		//TODO: bold the level and column width. + corresponding name.
		//TODO: non-ascii char in title.
		int index = 0;
		for (Map.Entry<String, int[]> entr : _preProcess.getCustomizedSectionParams()) {
			_sectionParamListModel.set(index, createListElem(entr));
			index ++;
		}
	}
	
	private String createListElem(Map.Entry<String, int[]> entr) {
//		String elem = "<html><b><font color=white>";
//		for (int i = 0; i < entr.getValue()[0]; i++) {
//			elem += _listDisplaySpacing;
//		}
//		elem += "</font>" + entr.getKey() + " : </b><i> Title Level = </i>" + entr.getValue()[0] + 
//				", <i>Column number = </i>" + entr.getValue()[1] + "</html>";
		
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
		} catch (FatalErrorException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		} catch (Exception e) {
			e.getMessage();
			e.printStackTrace();
		}
	}
}
