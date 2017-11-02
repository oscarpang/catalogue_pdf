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
	private static JScrollPane _sectionParamPane, _configPane;
	private static JPanel _chooseBtnPanel;
	private static PreProcess _preProcess;

	private static JPanel _southBtnsPanel;
	private static ConfigurationPanel _configPanel;
	private static JFrame _config_setting_frame;

	
	private static JList<String> _sectionParamList;
	private static JComboBox<Integer> _sectionColChoiceCombobox;
	private static DefaultListModel<String> _sectionParamListModel;
	
	private static Integer[] _sectionColChoice = { 1, 2, 3};
	private static String _listDisplaySpacing = "                    ";
	
	private static Font _titleFont = new Font("Arial", Font.ITALIC, 18);

	public SectionColChoicePanel(JFrame sectionColChoiceFrame, PreProcess preProcess) {
		super();
		this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
//		this.add(Box.createHorizontalGlue());
		
		_preProcess = preProcess;
		_sectionColChoiceFrame = sectionColChoiceFrame;
		
		_sectionParamListModel = new DefaultListModel<String>();
		setSectionParamListModel();
		_sectionParamList = new JList<String>(_sectionParamListModel);
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
//		_chooseBtnPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		
		this.add(_chooseBtnPanel);
		this.add(Box.createRigidArea(new Dimension(10, 0)));
		try {
			_configPanel = new ConfigurationPanel();
		} catch (FatalErrorException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		this.add(_configPanel);
//		_configPane = new JScrollPane();
////		TitledBorder oneColSectionTitledBorder = new TitledBorder("USC Catalogue Chapters And Sections: ");
////		oneColSectionTitledBorder.setTitleFont(_titleFont);
////		_oneColSectionPane.setViewportBorder(oneColSectionTitledBorder);
//		this.add(_configPane);
//		
		_sectionColChoiceFrame.add(this, BorderLayout.CENTER);
		
		_southBtnsPanel = new JPanel();
		_sectionColChoiceFrame.add(_southBtnsPanel, BorderLayout.SOUTH);
		_finishChooseColBtn = new JButton("Done");
		_finishChooseColBtn.addActionListener(this);
		_southBtnsPanel.add(_finishChooseColBtn);
//		_sectionColChoiceFrame.add(_finishChooseColBtn, BorderLayout.SOUTH);
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
		} else if (e.getSource() == _changeColNumBtn) {
			int selectColNum = (int)_sectionColChoiceCombobox.getSelectedItem();
			changeColNum(selectColNum);
		} else if (e.getSource() == _setDefaultColBtn) {
			_preProcess.resetCustomizedSectionParamsByColNums();
		}

//		System.out.println("-----2-----------");
		setSectionParamListModel();
//		System.out.println("-----3----------");
		this.revalidate();
		this.repaint();
	}
	
	public void setSectionParamListModel() {
		_sectionParamListModel.clear();
//		System.out.println(_preProcess.getCustomizedSectionParams().size());
//		int index = 0;
		for (Map.Entry<String, int[]> entr : _preProcess.getCustomizedSectionParams()) {
//			index ++;
//			System.out.println(index);
			String indexing = "";
			for (int i = 0; i < entr.getValue()[0]; i++) {
				indexing += _listDisplaySpacing;
			}
			_sectionParamListModel.addElement(indexing + entr.getKey() + " : " + entr.getValue()[1]);
		}
	}
	
	public void addSectionLevel(int offset) {
		int[] selectedIndices = _sectionParamList.getSelectedIndices();
		for (int index : selectedIndices) {
			System.out.println("--addSectionLevel---" + index + "--" + offset);
			_preProcess.addCustomizedSectionLevels(index, offset);
		}
	}
	
	public void changeColNum(int colNum) {
		int[] selectedIndices = _sectionParamList.getSelectedIndices();
		for (int index : selectedIndices) {
			System.out.println("--changeColNum---" + index + "--" + colNum);
			_preProcess.setCustomizedSectionColNums(index, colNum);
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
