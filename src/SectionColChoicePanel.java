import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.border.TitledBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

public class SectionColChoicePanel extends JPanel implements ActionListener{
	
	private static final long serialVersionUID = 1L;
	
	private static JFrame _sectionColChoiceFrame;
	private static JButton _finishChooseColBtn, _setOneColBtn, _rmOneColBtn, _setDefaultColBtn;
	private static JButton _charConfigBtn;
	private static JScrollPane _sectionNameScrollPane, _oneColSectionPane;
	private static JTree _sectionNamesTree;
	private static JPanel _chooseBtnPanel;
	private static PreProcess _preProcess;
	private static JList<String> _oneColSectionList;
	private static DefaultListModel<String> _oneColSectionListModel;
	private static JPanel _southBtnsPanel;
	private static ConfigurationPanel _configPanel;
	
	
	private static JFrame _config_setting_frame;
	
	private static Font _titleFont = new Font("Arial", Font.ITALIC, 18);

	public SectionColChoicePanel(JFrame sectionColChoiceFrame, PreProcess preProcess) {
		super();
		this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
//		this.add(Box.createHorizontalGlue());
		
		_preProcess = preProcess;
		_sectionColChoiceFrame = sectionColChoiceFrame;
		
		_sectionNamesTree = new JTree(_preProcess.getSectionNamesTree());
		_sectionNameScrollPane = new JScrollPane(_sectionNamesTree);
		TitledBorder sectionNameTitledBorder = new TitledBorder("All Sections in USC Catalogue");
		sectionNameTitledBorder.setTitleFont(_titleFont);
		_sectionNameScrollPane.setViewportBorder(sectionNameTitledBorder);
		this.add(_sectionNameScrollPane);
		this.add(Box.createRigidArea(new Dimension(10, 0)));
		
		_chooseBtnPanel = new JPanel();
		_chooseBtnPanel.setLayout(new BoxLayout(_chooseBtnPanel, BoxLayout.Y_AXIS));
		
		_setOneColBtn = new JButton(">>");
		_setOneColBtn.addActionListener(this);
		_setOneColBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
		_rmOneColBtn = new JButton("<<");
		_rmOneColBtn.addActionListener(this);
		_rmOneColBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
		_setDefaultColBtn = new JButton("Set by default.");
		_setDefaultColBtn.addActionListener(this);
		_setDefaultColBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		_chooseBtnPanel.add(_setOneColBtn);
		_chooseBtnPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		_chooseBtnPanel.add(_rmOneColBtn);
		_chooseBtnPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		_chooseBtnPanel.add(_setDefaultColBtn);
		_chooseBtnPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		
		this.add(_chooseBtnPanel);
		this.add(Box.createRigidArea(new Dimension(10, 0)));
		
		_oneColSectionListModel = new DefaultListModel<String>();
		setOneColSectionListModel();
		_oneColSectionList = new JList<String>(_oneColSectionListModel);
		_oneColSectionPane = new JScrollPane(_oneColSectionList);
		TitledBorder oneColSectionTitledBorder = new TitledBorder("Sections that will be shown as one column.");
		oneColSectionTitledBorder.setTitleFont(_titleFont);
		_oneColSectionPane.setViewportBorder(oneColSectionTitledBorder);
		this.add(_oneColSectionPane);
		
		_sectionColChoiceFrame.add(this, BorderLayout.CENTER);
		
		_southBtnsPanel = new JPanel();
		_sectionColChoiceFrame.add(_southBtnsPanel, BorderLayout.SOUTH);
		_finishChooseColBtn = new JButton("Done");
		_finishChooseColBtn.addActionListener(this);
		_southBtnsPanel.add(_finishChooseColBtn);
//		_sectionColChoiceFrame.add(_finishChooseColBtn, BorderLayout.SOUTH);
		
		_charConfigBtn = new JButton("Char Config");
		_charConfigBtn.addActionListener(this);
		_southBtnsPanel.add(_charConfigBtn);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == _finishChooseColBtn) {
			startConversion();
			JOptionPane.showMessageDialog(this, "Finish Conversion to Latex. Where do you want to save the file?");
		} else if (e.getSource() == _setOneColBtn) {
			addOneColSections();
			_sectionNamesTree.clearSelection();
		} else if (e.getSource() == _rmOneColBtn) {
			removeOneColSections();
		} else if (e.getSource() == _setDefaultColBtn) {
			_preProcess.resetCustomizedSectionColNums();
		} else if (e.getSource() == _charConfigBtn) {
			System.out.println("_charConfigBtn");
			try {
				_configPanel = new ConfigurationPanel();
			} catch (FatalErrorException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			_config_setting_frame = new JFrame("Config Setting");
			_config_setting_frame.add(_configPanel, BorderLayout.CENTER);
			_config_setting_frame.setVisible(true);
		}
		
		setOneColSectionListModel();
		
		this.revalidate();
		this.repaint();
	}
	
	public void setOneColSectionListModel() {
		_oneColSectionListModel.clear();
		for (Map.Entry<String, Integer> entr : _preProcess.getCustomizedSectionColNums()) {
			if (entr.getValue() == 1) {
				_oneColSectionListModel.addElement(entr.getKey());
			}
		}
	}
	
	public void addOneColSections() {
		TreePath[] treePath = _sectionNamesTree.getSelectionPaths();
		if (treePath == null) {
			return;
		}
		for (TreePath p : treePath) {
			String sectionName = ((DefaultMutableTreeNode)p.getLastPathComponent()).getUserObject().toString();
			_preProcess.setCustomizedSectionColNums(sectionName, 1);
		}
	}
	
	public void removeOneColSections() {
		List<String> selectionList = _oneColSectionList.getSelectedValuesList();
		if (selectionList.isEmpty()) {
			return;
		}
		for (String str : selectionList) {
			_preProcess.setCustomizedSectionColNums(str, 2);
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
