package alt.collections.store;

import java.util.ArrayList;
import java.util.List;

import alt.collections.store.impl.MultipleFileStore;
import alt.collections.store.impl.SingleFileStore;
import alt.collections.util.MapFileMode;
import alt.collections.util.PageSize;
import alt.collections.util.Requires;

/**
 * FileStoreBuilder
 * 
 * Order of files is important
 * 
 * @author Albert Shift
 *
 */
public class FileStoreBuilder extends AbstractStoreBuilder<FileStoreBuilder> {
	
	public static final int DIR_MAX_FILES = 512;
	
	private MapFileMode fileMode = MapFileMode.READ_WRITE;
	private final List<FileEntry> files = new ArrayList<FileEntry>();
	private boolean deleteOnExit = false;
	
	public FileStoreBuilder() {
	}

	public FileStoreBuilder withPageSize(int pageSize) {
		if (!files.isEmpty()) {
			throw new IllegalStateException("files already added to the builder");
		}
		super.withPageSize(pageSize);
		return this;
	}
	
	public FileStoreBuilder readOnly() {
		this.fileMode = MapFileMode.READ_ONLY;
		return this;
	}
	
	public FileStoreBuilder withFileMode(MapFileMode fileMode) {
		Requires.nonNull(fileMode, "fileMode");
		this.fileMode = fileMode;
		return this;
	}
	
	public FileStoreBuilder deleteOnExit() {
		return deleteOnExit(true);
	}
	
	public FileStoreBuilder deleteOnExit(boolean flag) {
		this.deleteOnExit = flag;
		return this;
	}
	
	/**
	 * 
	 * @param filePath File path
	 * @param maxSize of this file
	 * @return
	 */
	
	public FileStoreBuilder addFile(String filePath, long size) {
		Requires.nonNull(filePath, "filePath");
		Requires.nonNull(pageSize, "pageSize");
		Requires.aligned(pageSize, size, "size");
		files.add(new FileEntry(filePath, size));
		return this;
	}
	
	/**
	 * 
	 * @param filePathPattern String pattern first %s - directory Number, second %s - fileNumber, example "folder%s/file%s.mmf"
	 * @param numFiles from 1...numFiles
	 * @param maxSize of each file
	 * @return
	 */
	
	public FileStoreBuilder addFiles(String filePathPattern, int numFiles, long size) {
		Requires.nonNull(filePathPattern, "filePathPattern");
		Requires.nonNull(pageSize, "pageSize");
		Requires.aligned(pageSize, size, "size");
		
		for (int i = 1; i != numFiles+1; ++i) {
			String filePath = String.format(filePathPattern, i / DIR_MAX_FILES, i % DIR_MAX_FILES);
			files.add(new FileEntry(filePath, size));
		}
		
		return this;
	}

	public FileStore build() throws Exception {
		Requires.nonNull(pageSize, "pageSize");
		if (files.isEmpty()) {
			throw new IllegalStateException("empty files");
		}
		if (files.size() == 1) {
			return new SingleFileStore(pageSize, pageNum, innerRef, fileMode, files.get(0), deleteOnExit);
		}
		return new MultipleFileStore(pageSize, pageNum, innerRef, fileMode, files, deleteOnExit);
	}
	
	public static class FileEntry {
		
		private final String filePath;
		private final long size;
		
		public FileEntry(String filePath, long size) {
			this.filePath = filePath;
			this.size = size;
		}

		public String getFilePath() {
			return filePath;
		}

		public long getSize() {
			return size;
		}

	}
	
}
