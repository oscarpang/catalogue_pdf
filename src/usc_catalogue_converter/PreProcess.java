package usc_catalogue_converter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

public class PreProcess {

	/** Input HTML file. */
	private String _inputFile = null;
	/** Input Xls file. */
	private String _courseXlsFile = null;
	/** Output HTML file. */
	private String _processedFile = null;

	private File _processedFileInProgress = null;
	private FileReader _fileReader = null;
	private FileWriter _fileWriter = null;
	private BufferedReader _bufferedReader = null;
	private BufferedWriter _bufferedWriter = null;

	//lines that should be ignored. Note: � is the encoding of UTF8 none breaking space.
	private static String[] _prefixIgnored = { "<ul></ul>", "<ul> </ul>", "<li></li>", "<li> </li>",
			"<h1></h1>", "<h2></h2>", "<h3></h3>", "<h4></h4>", "<h5></h5>", "<h6></h6>",
			"<h1> </h1>", "<h2> </h2>", "<h3> </h3>", "<h4> </h4>", "<h5> </h5>", "<h6> </h6>",
			"<p></p>", "<p> </p>", "<br>", "", "	", "<p><br></p>"}; 

	private static String[] _chapterNames = { "Catalogue Home", "A Message from the President",
			"Admission and Orientation", "Tuition and Fees", "The Schools and Academic Units",
			"Programs, Minors and Certificates", "Academic and University Policies", 
			"Undergraduate Education", "Graduate and Professional Education", "The Graduate School" };

	// the column number for each table
	private ArrayList<Integer> _tableColNum; 

	// the row number for each table
	private ArrayList<Integer> _tableRowNum; 

	// the average character length for each column
	private ArrayList<ArrayList<Double>> _tableColWidth; 

	// By default, 1 column for sections that contain tables, 3 columns for course of instructions 
	//	and 2 columns for other sections.
	private ArrayList<Map.Entry<String, int[]>> _defaultSectionParams; 

	private ArrayList<Map.Entry<String, int[]>> _customizedSectionParams;
	
	private HashSet<Character> _non_ascii_charset;

	public void preProcess(String inputFile, String processedFile, String courseXlsFile) {
		_inputFile = inputFile;
		_courseXlsFile = courseXlsFile;
		_processedFile = processedFile;
		_tableColNum = new ArrayList<Integer>();
		_tableRowNum = new ArrayList<Integer>();
		_tableColWidth = new ArrayList<ArrayList<Double>>();
		_defaultSectionParams = new ArrayList<Map.Entry<String, int[]>>();
		_customizedSectionParams = new ArrayList<Map.Entry<String, int[]>>();
		
		_non_ascii_charset = new HashSet<>();

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
		try {
			while ((line = _bufferedReader.readLine()) != null) {
				if (shouldIgnore(line)) continue;
				//remove <br> in header.
				line = line.replaceAll("<br></h", "</h");
				// promote some h1 to be chapter (h0).
				line = isChapter(line) ? line.replaceAll("h1", "h0") : line;
				
				//Add course of instructions to the same HTML
				if (line.contains("</body")) {
					CourseXlsParser.ParseToHTMLWriter(_courseXlsFile,_bufferedWriter);
				}

				//should ignore those lines.
				if (line.contains("class=\"Course\"") || line.contains("Return to")) {
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

		try {
			while ((line = _bufferedReader.readLine()) != null) {
				line = new String(line.getBytes(), "UTF8");
				for(char c : line.toCharArray()) {
					if((int) c >= 128) {
						_non_ascii_charset.add(c);
					}
				}
				
				if (line.contains("<tbody>")) {
					inTable = true;
				} else if (line.contains("</tbody>")) {
					inTable = false;
					_tableColNum.add(curTableColNum);

					curTableColNum = 0;
				}

				if (inTable) {
					line = line.replaceAll("<br>", "");
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

				// End </strong> before <br> and create a new <strong>
				numStrong += countOccurence(line, "<strong>") - countOccurence(line, "</strong>");
				numEm += countOccurence(line, "<em>") - countOccurence(line, "</em>");
				numUnderline += countOccurence(line, "<u>") - countOccurence(line, "</u>");
				numHeader += countOccurence(line, "<h") - countOccurence(line, "<html>") - countOccurence(line,"<head>")
						- (countOccurence(line, "</h") - countOccurence(line, "</html>") - countOccurence(line,"</head>"));

				if (line.contains("<br>")) {
					if (numStrong > 0) {
						line = line.replaceAll("<br>", "</strong><br><strong>");
					} else if (numEm > 0) {
						line = line.replaceAll("<br>", "</em><br><em>");
					} else if (numUnderline > 0) {
						line = line.replaceAll("<br>", "</u><br><u>");
					} else if (numHeader > 0) {
						line = line.replaceAll("<br>", "");
					}
				}

				// remove empty list
				if (line.equals("<ul>")) {
					prevIsUl = true;
					continue;
				}

				if (prevIsUl && !line.equals("</ul>")) {
					_bufferedWriter.write("<ul>");
					_bufferedWriter.newLine();
					_bufferedWriter.write(line);
					_bufferedWriter.newLine();
				} else if (!prevIsUl) {
					_bufferedWriter.write(line);
					_bufferedWriter.newLine();
				}
				prevIsUl = false;
			}

			System.out.println("_tableColNum : " + _tableColNum);

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
		String curSectionName = null;
		
		int defaultColNum = 2;

		try {
			while ((line = _bufferedReader.readLine()) != null) {
				//get sectionParams.
				if (line.contains("<h") && !line.contains("<html>") && !line.contains("<head>")) {
					curSectionName = line.substring(line.indexOf(">")+1);
					int curSectionLevel = Integer.parseInt("" + line.charAt(line.indexOf("<h") + 2));
					
					while (!line.contains("</")) {
						line = _bufferedReader.readLine();
						curSectionName += " " + line;
					}
					curSectionName = curSectionName.contains("</") ?
							 curSectionName.substring(0, curSectionName.indexOf("</")) : curSectionName;
							 
					curSectionName = curSectionName.contains(">") ?
							 curSectionName.substring(curSectionName.indexOf(">") + 1) : curSectionName;

					//By default, Course Of Instructions should be shown in 3 columns.
					if (curSectionName.equals("Courses of Instruction")) {
						defaultColNum = 3;
					}
					int[] curSectionParam = {curSectionLevel, defaultColNum};
					_defaultSectionParams.add(new AbstractMap.SimpleEntry<String, int[]>(curSectionName, curSectionParam));
					_customizedSectionParams.add(new AbstractMap.SimpleEntry<String, int[]>(curSectionName, curSectionParam.clone()));
				}
				
				if (line.contains("<tbody>")) {
					inTable = true;
					// By default, sections with tables should be shown in one column.
					Map.Entry<String, int[]> entr = _defaultSectionParams.get(_defaultSectionParams.size() - 1);
					int[] curParam = entr.getValue();
					curParam[1] = 1;
					entr.setValue(curParam);
					_customizedSectionParams.get(_defaultSectionParams.size() - 1).setValue(curParam.clone());
				} else if (line.contains("</tbody>")) {
					inTable = false;
					double sum = 0;
					for (int i = 0; i < curTableColWidth.size(); i++) {
						double average = curTableColWidth.get(i) / numRow;
						curTableColWidth.set(i, average);
						sum += average;
					}
					for (int i = 0; i < curTableColWidth.size(); i++) {
						curTableColWidth.set(i, curTableColWidth.get(i) / sum);
					}
					_tableColWidth.add(curTableColWidth);
					_tableRowNum.add(numRow);
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
								curTableColWidth.add(curColWidth * 1.0 / colspan);
							} else {
								curTableColWidth.set(curColNum + i,
										curTableColWidth.get(curColNum + i) + curColWidth / colspan);
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
						numRow++;
						isFirstLine = false;
					}
				}
			}
			System.out.println("_tableRowNum : " + _tableRowNum);
			System.out.println("_tableColWidth : " + _tableColWidth);
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
		for (String str : _chapterNames) {
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
			count++;
			index = str.indexOf(target, index + target.length());
		}
		return count;
	}

	private int getSpan(String line, String str) {
		if (line.contains(str)) {
			// 2 = length of "=\""
			int index = line.indexOf(str) + str.length() + 2;
			return Integer.parseInt(line.substring(index, index + 1));
		}
		return 1;
	}

	private int getCurColWidth(String line) {
		int width = 0;
		boolean ignore = false;
		try {
			while (true) {
				for (char c : line.toCharArray()) {
					ignore = (c == '<') ? true : ignore;
					width += ignore ? 0 : 1;
					ignore = (c == '>') ? false : ignore;
				}
				if (line.contains("</td>")) {
					break;
				}
				width++; // add the extra space.
				line = _bufferedReader.readLine();
				line = new String(line.getBytes(), "UTF8");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return width;
	}
	
	public Map.Entry<String, int[]> setCustomizedSectionColNums(int index, int value) {
		Map.Entry<String, int[]> entr = _customizedSectionParams.get(index);
		int[] curParam = entr.getValue();
		int oldColNum = curParam[1];
		curParam[1] = value;
		System.out.println("--setCustomizedSectionColNums---" + entr.getKey() 
							+ "--" + oldColNum+ "->" + value);
		return entr;
	}
	
	//Note: this function also upgrade/downgrade subsections.
	public Map.Entry<String, int[]> addCustomizedSectionLevels(int index, int offset) {
		Map.Entry<String, int[]> entr = _customizedSectionParams.get(index);
		int oldLevel = entr.getValue()[0];
		for (int i = index; i < _customizedSectionParams.size(); i++) {
			entr = _customizedSectionParams.get(i);
			if (i != index && entr.getValue()[0] <= oldLevel) {
				break;
			}
			int[] curParam = entr.getValue();
			int oldTitleLevel = curParam[0];
			curParam[0] += offset;
			curParam[0] = curParam[0] > 6 ? 6 : curParam[0];
			curParam[0] = curParam[0] < 0 ? 0 : curParam[0];
			System.out.println("--addCustomizedSectionLevels---" + entr.getKey() 
								+ "--" + oldTitleLevel + "->" + curParam[0]);
		}
		
		return _customizedSectionParams.get(index);
	}
	
	public void resetCustomizedSectionParamsByLevels() {
		for (int i = 0; i < _defaultSectionParams.size(); i++) {
			Map.Entry<String, int[]> defaultEntr = _defaultSectionParams.get(i);
			Map.Entry<String, int[]> customEntr = _customizedSectionParams.get(i);
			int[] param = customEntr.getValue();
			if (param[0] != defaultEntr.getValue()[0]) {
				System.out.println("--resetCustomizedSectionParamsByLevels--" + defaultEntr.getKey() 
						+ "--" + customEntr.getValue()[0] + "->" + defaultEntr.getValue()[0]);
			}
			param[0] = defaultEntr.getValue()[0];
		}
	}
	
	public void resetCustomizedSectionParamsByColNums() {
		for (int i = 0; i < _defaultSectionParams.size(); i++) {
			Map.Entry<String, int[]> defaultEntr = _defaultSectionParams.get(i);
			Map.Entry<String, int[]> customEntr = _customizedSectionParams.get(i);
			int[] param = customEntr.getValue();
			if (param[1] != defaultEntr.getValue()[1]) {
				System.out.println("--resetCustomizedSectionParamsByColNums--" + defaultEntr.getKey()
						+ "--" + customEntr.getValue()[1] + "->" + defaultEntr.getValue()[1]);
			}
			param[1] = defaultEntr.getValue()[1];
		}
	}

	public ArrayList<Integer> getTableColNum() {
		return _tableColNum;
	}

	public ArrayList<Integer> getTableRowNum() {
		return _tableRowNum;
	}

	public ArrayList<ArrayList<Double>> getTableColWidth() {
		return _tableColWidth;
	}

	public ArrayList<Map.Entry<String, int[]>> getDefaultSectionParams() {
		return _defaultSectionParams;
	}

	public ArrayList<Map.Entry<String, int[]>> getCustomizedSectionParams() {
		return _customizedSectionParams;
	}
	
	public HashSet<Character> getNonAsciiSet(){
		return _non_ascii_charset;
	}
}
