import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

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
	
	private static String[] _chapterName = {"Catalogue Home", "A Message from the President", "Admission and Orientation",
										"Tuition and Fees", "The Schools and Academic Units", "Programs, Minors and Certificates",
										"Academic and University Policies", "Undergraduate Education", 
										"Graduate and Professional Education", "The Graduate School"};
	
	public void preProcess(String inputFile, String processedFile) {
		_inputFile = inputFile;
		_processedFile = processedFile;
		
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
		
		System.out.println("Start the first round processing.");
		String line = null;
		try {
			while ((line = _bufferedReader.readLine()) != null) {
				line = line.replaceAll("<br></h", "</h");
				if (line.contains("<h1 class=\"Page\">") && isChapter(line)) {
					line = line.replaceAll("h1", "h0");
					System.out.println(line);
				}
				if (line.contains("class=\"Course\"")) {
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
		
		try{
			while ((line = _bufferedReader.readLine()) != null) {
				//End </strong> before <br> and create a new <strong>
				numStrong += countOccurence(line, "<strong>") - countOccurence(line, "</strong>");
				numEm += countOccurence(line, "<em>") - countOccurence(line, "</em>");
				numUnderline += countOccurence(line, "<u>") - countOccurence(line, "</u>");
				numHeader += countOccurence(line, "<h") - countOccurence(line,"<html>") 
						- ( countOccurence(line, "</h") - countOccurence(line,"</html>") ) ;
				
				if (line.contains("<br>")) {
					System.out.println(numStrong + "**" + numEm + "**" + numUnderline + "**" + numHeader + "*" + line);
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
			
			_bufferedReader.close();
			_bufferedWriter.close();
			_processedFileInProgress.delete();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Finished Pre-processing");
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
		for (String str: _chapterName) {
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
}
