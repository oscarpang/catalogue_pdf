import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Collections;
import java.nio.file.Paths;
import java.nio.file.Path;

public class LatexCompilerExecutor {

	public static void main(String[] args) {
		String tex_install_path = "/usr/local/texlive/";
		File dir = new File(tex_install_path);
		boolean texlive_exist = dir.exists();
		System.out.println(texlive_exist);
		File subdir[] = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.matches("[0-9]+");
			}
		});
		if(subdir.length > 0) {
			Arrays.sort(subdir, (f1, f2) -> {return f2.getName().compareTo(f1.getName()); });
			Path pdflatex_path;
			if(System.getProperty("os.arch").contains("x86_64"))
				pdflatex_path = Paths.get(subdir[0].getAbsolutePath(), "bin", "x86_64-darwin", "pdflatex");
			else
				pdflatex_path = Paths.get(subdir[0].getAbsolutePath(), "bin", "universal-darwin", "pdflatex");
			File pdflatex_bin = pdflatex_path.toFile();
			System.out.println(pdflatex_bin.exists());			
		}
		
		

	}

}
