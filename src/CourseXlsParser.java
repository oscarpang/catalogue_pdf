

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

public class CourseXlsParser {
	private File input_file_;
	private List<Course> course_list;
	private FileInputStream inputStream_;
	private Workbook workbook_;
	private Iterator<Row> course_iterator_;
	private static int COLUMN_NUMBER = 76;

	public CourseXlsParser(File inputFile) throws IOException {
		input_file_ = inputFile;
		course_list = new ArrayList<Course>();

		inputStream_ = new FileInputStream(input_file_);
		workbook_ = new HSSFWorkbook(inputStream_);
		Sheet firstSheet = workbook_.getSheetAt(0);
		course_iterator_ = firstSheet.iterator();
	}

	public void Parse() {
		if (!ParseHeader())
			return;
		ParseContent();
	}
	
	public List<Course> GetCourseList() {
		return course_list;
	}

	private boolean ParseHeader() {
		if (course_iterator_.hasNext()) {
			Row nextRow = course_iterator_.next();
			Iterator<Cell> cellIterator = nextRow.cellIterator();
			int count = 0;
			while (cellIterator.hasNext()) {
				cellIterator.next();
				count++;
			}
			if (count != COLUMN_NUMBER) {
				System.out.println("Wrong number of columns in csv file, do not preceed: " + count);
				return false;
			}
		}
		return true;
	}

	private void ParseContent() {
		while (course_iterator_.hasNext()) {
			Row nextRow = course_iterator_.next();
			List<String> row = ReadRow(nextRow);

			Course c = new Course(row.get(11), row.get(12), row.get(13), row.get(14), row.get(15), row.get(16),
					row.get(17), row.get(20), row.get(23), row.get(26), row.get(29), row.get(32), row.get(33),
					row.get(34), row.get(35), row.get(42), row.get(49), row.get(52), row.get(53), row.get(54),
					row.get(58));
			course_list.add(c);
		}
	}

	public List<String> ReadRow(Row row) {
		List<String> words = new ArrayList<String>();
		for (int cn = 0; cn < row.getLastCellNum(); cn++) {
			Cell cell = row.getCell(cn);
			if (cell == null) {
				words.add("");
			} else {
				words.add(cell.getStringCellValue());
			}
		}
		return words;
	}
}
