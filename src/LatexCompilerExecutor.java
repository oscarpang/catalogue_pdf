import java.io.File;

public class LatexCompilerExecutor {

	public static void main(String[] args) {
		File dir = new File("/usr/local/texlive/");
		boolean texlive_exist = dir.exists();
		System.out.println(texlive_exist);
		
	}

}
