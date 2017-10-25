import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.tree.DefaultMutableTreeNode;

public class PreProcess {
	
	/** Input HTML file. */
	private static String _inputFile = null;
	/** Output HTML file. */
	private static String _processedFile = null;
	
	private static File _processedFileInProgress = null;
	private static FileReader _fileReader = null;
	private static FileWriter _fileWriter = null;
	private static BufferedReader _bufferedReader = null;
	private static BufferedWriter _bufferedWriter = null;
	
	private static String[] _prefixIgnored = {"<ul></ul>", "<ul> </ul>", "<li></li>", "<li> </li>",
											"<p></p>", "<p> </p>", "<h4> </h4>", "<h3> </h3>", "<h2> </h2>",
											"<br>", "", "	", "<p><br></p>"}; // � is the encoding of UTF8 none breaking space.
	
	private static String[] _chapterNames = {"Catalogue Home", "A Message from the President", "Admission and Orientation",
										"Tuition and Fees", "The Schools and Academic Units", "Programs, Minors and Certificates",
										"Academic and University Policies", "Undergraduate Education", 
										"Graduate and Professional Education", "The Graduate School"};

	private static DefaultMutableTreeNode _sectionNamesTree;
	
	private static ArrayList<Integer> _tableColNum;
	
	private static ArrayList<ArrayList<Double>> _tableColWidth;
	
	public void preProcess(String inputFile, String processedFile) {
		_inputFile = inputFile;
		_processedFile = processedFile;
		_tableColNum = new ArrayList<Integer>();
		_tableColWidth = new ArrayList<ArrayList<Double>>();
		_sectionNamesTree = new DefaultMutableTreeNode("USC_Catalogue_Chapters_And_Sections: ");
		
		firstRoundPreProcessing();
		secondRoundPreProcessing();
		thirdRoundPreProcessing();
	}
	
	
	private void firstRoundPreProcessing() {
		System.out.println("Start the first round processing.");
		
		try {
			_fileReader = new FileReader(new File(_inputFile));
			_processedFileInProgress = new File("progress.html");
			_fileWriter = new FileWriter(_processedFileInProgress);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		_bufferedReader = new BufferedReader(_fileReader);
		_bufferedWriter = new BufferedWriter(_fileWriter);
		
		String line = null;
		DefaultMutableTreeNode currentChapterNode = new DefaultMutableTreeNode();
		try {
			while ((line = _bufferedReader.readLine()) != null) {
				line = line.replaceAll("<br></h", "</h");
				if (line.contains("<h1 class=\"Page\">")) {
					//promote some h1 to be chapter (h0). and add all sectionNames into a tree.
					String name = line.replace("<h1 class=\"Page\">", "");
					boolean isChapter = isChapter(line);
					line = isChapter ? line.replaceAll("h1", "h0") : line;
					while (!line.contains("</")) {
						_bufferedWriter.write(line);
						_bufferedWriter.newLine();
						line = _bufferedReader.readLine();
						name += " " + line;
					}
					name = name.contains("</") ? name.substring(0, name.indexOf("</")) : name;
					
					if (isChapter) {
						currentChapterNode = new DefaultMutableTreeNode(name);
						_sectionNamesTree.add(currentChapterNode);
					} else {
						currentChapterNode.add(new DefaultMutableTreeNode(name));
					}
				}
				if (line.contains("class=\"Course\"") || line.contains("Return to")) { // should ignore
					while (!line.contains("</")) {
						line = _bufferedReader.readLine();
					}
				} else if (!shouldIgnore(line)) {
					_bufferedWriter.write(line);
					_bufferedWriter.newLine();
				}
			}
			_bufferedReader.close();
			_bufferedWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Finish first round processing.");
	}

	private void secondRoundPreProcessing() {
		System.out.println("Start the second round processing.");
		try {
			_fileReader = new FileReader(_processedFileInProgress);
			_fileWriter = new FileWriter(new File(_processedFile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		_bufferedReader = new BufferedReader(_fileReader);
		_bufferedWriter = new BufferedWriter(_fileWriter);
		
		int numStrong, numEm, numUnderline, numHeader;
		numStrong = numEm = numUnderline = numHeader = 0;
		boolean prevIsUl = false;
		String line = null;
		
		boolean inTable = false;
		int curColNum = 0;
		int prevRowColspan = 0;
		int curTableColNum = 0;
		
		try{
			while ((line = _bufferedReader.readLine()) != null) {
				if (line.contains("<tbody>")) {
					inTable = true;
				} else if (line.contains("</tbody>")) {
					inTable = false;
					_tableColNum.add(curTableColNum);
					
					curTableColNum = 0;
				}		
				
				if (inTable) {
					line = line.replaceAll("<p>", "<table_p>");
					line = line.replaceAll("</p>", "</table_p>");
					
					if (line.contains("<td")) { // start a new column
						int colspan = getSpan(line, "colspan");
						int rowspan = getSpan(line, "rowspan");
						curColNum += colspan;
						if (rowspan > 1) {
							prevRowColspan += colspan;
						}
					} else if (line.contains("</tr>")) { // end a row
						curTableColNum = Integer.max(curTableColNum, curColNum);
						curColNum = prevRowColspan;
						prevRowColspan = 0;
					}
				}
				
				//End </strong> before <br> and create a new <strong>
				numStrong += countOccurence(line, "<strong>") - countOccurence(line, "</strong>");
				numEm += countOccurence(line, "<em>") - countOccurence(line, "</em>");
				numUnderline += countOccurence(line, "<u>") - countOccurence(line, "</u>");
				numHeader += countOccurence(line, "<h") - countOccurence(line,"<html>") 
						- ( countOccurence(line, "</h") - countOccurence(line,"</html>") ) ;
				
				if (line.contains("<br>")) {
//					System.out.println(numStrong + "**" + numEm + "**" + numUnderline + "**" + numHeader + "*" + line);
					if (numStrong > 0) {
						line = line.replaceAll("<br>", "</strong><br><strong>");
//						System.out.println("-----------" + line);
					} else if (numEm > 0) {
						line = line.replaceAll("<br>", "</em><br><em>");
//						System.out.println("~~~~~~~~~~~" + line);
					} else if (numUnderline > 0) {
						line = line.replaceAll("<br>", "</u><br><u>");
//						System.out.println("^^^^^^^^^^^" + line);	
					} else if (numHeader > 0) {
						line = line.replaceAll("<br>", "");
//						System.out.println("%%%%%%%%%%%" + line);
					}
				}
				
				//remove empty list
				if (line.equals("<ul>")) {
					prevIsUl = true;
					continue;
				}
				
				if (prevIsUl && !line.equals("</ul>")) {
					_bufferedWriter.write("<ul>");
					_bufferedWriter.newLine();
					_bufferedWriter.write(line);
					_bufferedWriter.newLine();
				} else if (!prevIsUl){
					_bufferedWriter.write(line);
					_bufferedWriter.newLine();
				}
				prevIsUl = false;
			}
			
			System.out.println(_tableColNum);
			
			_bufferedReader.close();
			_bufferedWriter.close();
			_processedFileInProgress.delete();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Finish second round processing.");
	}
	
	private void thirdRoundPreProcessing() {
		System.out.println("Start the third round processing.");
		try {
			_fileReader = new FileReader(_processedFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		_bufferedReader = new BufferedReader(_fileReader);
		
		String line = null;
		
		boolean inTable = false;
		int curColNum = 0;
		int prevRowColspan = 0;
		
		int curTableColNum = 0;
		ArrayList<Double> curTableColWidth = new ArrayList<Double>();
		int numRow = 0;
		boolean isFirstLine = true;
		
		try{
			while ((line = _bufferedReader.readLine()) != null) {
				if (line.contains("<tbody>")) {
					inTable = true;
				} else if (line.contains("</tbody>")) {
					inTable = false;
					double sum = 0;
					for (int i = 0; i < curTableColWidth.size(); i ++) {
						double average = curTableColWidth.get(i)/numRow;
						curTableColWidth.set(i, average);
						sum += average;
					}
//					System.out.println("sum : " + sum);
//					System.out.println(curTableColWidth);
					for (int i = 0; i < curTableColWidth.size(); i ++) {
						curTableColWidth.set(i, curTableColWidth.get(i)/sum);
					}
//					System.out.println(curTableColWidth);
					_tableColWidth.add(curTableColWidth);
					
					curTableColWidth = new ArrayList<Double>();
					numRow = 0;
					isFirstLine = true;
				}		
				
				if (inTable) {
					if (line.contains("<td")) { // start a new column
						int colspan = getSpan(line, "colspan");
						int rowspan = getSpan(line, "rowspan");
						int curColWidth = getCurColWidth(line);
						for (int i = 0; i < colspan; i++) {
							if (isFirstLine) {
								curTableColWidth.add(curColWidth*1.0/colspan);
							} else {
								curTableColWidth.set(curColNum + i, 
										curTableColWidth.get(curColNum + i) + curColWidth/colspan);
							}
						}
						curColNum += colspan;
						if (rowspan > 1) {
							prevRowColspan += colspan;
						}
					} else if (line.contains("</tr>")) { // end a row
						curTableColNum = Integer.max(curTableColNum, curColNum);
						curColNum = prevRowColspan;
						prevRowColspan = 0;
						numRow ++;
						isFirstLine = false;
					}
				}
			}
			
			System.out.println(_tableColWidth);
			
			_bufferedReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Finish third round processing.");
	}

	private boolean shouldIgnore(String line) {
		for (String str : _prefixIgnored) {
			if (line.equals(str)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isChapter(String line) {
		for (String str: _chapterNames) {
			if (line.contains(str)) {
				return true;
			}
		}
		return false;
	}

	private int countOccurence(String str, String target) {
		int index = str.indexOf(target, 0);
		int count = 0;
		
		while (index != -1) {
			count ++;
			index = str.indexOf(target, index + target.length());
		}
		return count;
	}
	
	public int getSpan(String line, String str) {
		int span = 1;
		if (line.contains(str)) {
			int index = line.indexOf(str) + str.length() + 2; // 2 = length of "=\""
			span = Integer.parseInt(line.substring(index, index + 1));
		}
		return span;
	}
	
	public int getCurColWidth(String line) {
		int width = 0;
		boolean ignore = false;
		try {
			while (true) {
				for (char c : line.toCharArray()) {
					if (c == '<') {
						ignore = true;
					}
					width += ignore ? 0 : 1;
					if (c == '>') {
						ignore = false;
					}
				}
				if (line.contains("</td>")) {
					break;
				}
				width ++;
				line = _bufferedReader.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return width;
	}
	
	public DefaultMutableTreeNode getSectionNamesTree() {
		return _sectionNamesTree;
	}
	
	public ArrayList<Integer> getTableColNum() {
		return _tableColNum;
	}
	
	public ArrayList<ArrayList<Double>> getTableColWidth() {
		return _tableColWidth;
	}
}
