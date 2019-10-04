package queues;

import java.io.File;

public class SoundFile {
	private String fileAbsolutePath = null;

	public SoundFile(String FileAbsolutePath) {
		this.fileAbsolutePath = FileAbsolutePath;
	}

	public SoundFile(File smallFile) {
		if(smallFile != null) {
			this.fileAbsolutePath = smallFile.getAbsolutePath();
		}
	}

	public String getFileAbsolutePath() {
		return this.fileAbsolutePath;
	}
	
	public File toFile() {
		return new File(this.fileAbsolutePath);
	}

	public boolean exists() {
		return new File(this.fileAbsolutePath).exists();
	}

	public void delete() {
		if (new File(this.fileAbsolutePath).exists()) {
			try {
				new File(this.fileAbsolutePath).delete();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
