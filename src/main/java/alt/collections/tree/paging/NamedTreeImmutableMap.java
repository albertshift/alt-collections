package alt.collections.tree.paging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import alt.collections.concurrent.IntegerCas;
import alt.collections.concurrent.PageNumCas;
import alt.collections.paging.PageReader;
import alt.collections.paging.PageWriter;
import alt.collections.paging.Paging;
import alt.collections.util.ThreadUtil;

/**
 * NamedTreeImmutableMap is the Tree that stores associated pairs
 * String -> PageNumCas
 * 
 * Where String - is the treeName
 * PageNumCas - is the place for storing root node if the tree
 * 
 * Entries in Root Tree can not be deleted, because PageNumCas must be fixed in the tree.
 * Or can be deleted in shutdown.
 * 
 * Storing information:
 * 
 * Each Entry is stored information in this form:
 * [lesser:int], [greater:int], [root:pageNum], [key:string]
 * 
 * 
 * @author Albert Shift
 *
 */

public final class NamedTreeImmutableMap implements NamedTreeMap {

	private final Paging paging;
	private final VirtualSpace space; 
	private final IntegerCas rootEntry;
	
	public NamedTreeImmutableMap(Paging paging) {
		
		MasterPage masterPage = MasterPage.concurrentGetOrCreate(paging);  
		PagedVirtualSpace pagedVirtualSpace = new PagedVirtualSpace(paging, masterPage, masterPage);
		
		this.paging = paging;
		this.space = pagedVirtualSpace;
		this.rootEntry = masterPage.getRootEntry();
	}
	
	public NamedTreeImmutableMap(Paging paging, VirtualSpace space, IntegerCas rootEntry) {
		this.paging = paging;
		this.space = space;
		this.rootEntry = rootEntry;
	}
	
	@Override
	public PageNumCas findOrCreate(String treeName) {
		return doFindOrCreate(treeName, 0, null);
	}
	
	@Override
	public List<String> getAllNames() {
		
		int entryRef = rootEntry.getInt();
		if (entryRef == 0) {
			return Collections.emptyList();
		}
		
		List<String> listOfNames = new ArrayList<String>();
		PageReader pageReader = new PageReader(paging);

		collectTreeNames(pageReader, entryRef, listOfNames);
		
		return listOfNames;
		
	}
	
	private void collectTreeNames(PageReader pageReader, int entryRef, List<String> listOfNames) {
		
		if (entryRef == 0) {
			return;
		}
		
		long saveAddress = pageReader.getAddress();
		
		space.seek(pageReader, entryRef);
		int entryPos = pageReader.getPosition();
		
		pageReader.seek(getLesserPos(entryPos));
		collectTreeNames(pageReader, pageReader.readInt(), listOfNames);
		
		pageReader.seek(getTreeNamePos(entryPos));
		String treeName = pageReader.readString();
		listOfNames.add(treeName);
		
		pageReader.seek(getGreaterPos(entryPos));
		collectTreeNames(pageReader, pageReader.readInt(), listOfNames);
		
		pageReader.switchAddress(saveAddress);
		
	}
	
	@Override
	public PageNumCas find(String treeName) {
		
		PageReader pageReader = new PageReader(paging);
		
		int entryRef = rootEntry.getInt();
		if (entryRef == 0) {
			return null;
		}
		
		while(true) {
		
			space.seek(pageReader, entryRef);
			int entryPos = pageReader.getPosition();
			
			pageReader.seek(getTreeNamePos(entryPos));
			int c = pageReader.compareToString(treeName);
		
			if (c > 0) {
				pageReader.seek(getLesserPos(entryPos));
				int lesserEntryRef = pageReader.readInt();
				if (lesserEntryRef != 0) {
					entryRef = lesserEntryRef;
					continue;
				}
				
				return null;
			}
			else if (c < 0) {
				pageReader.seek(getGreaterPos(entryPos));
				int greaterEntryRef = pageReader.readInt();
				if (greaterEntryRef != 0) {
					entryRef = greaterEntryRef;
					continue;
				}
				
				return null;
			}
			else {
				// found exact match
				pageReader.seek(getTreePos(entryPos));
				return pageReader.readPageNumCas();
			}
		
		}
		
	}
	
	private PageNumCas doFindOrCreate(String treeName, int createdEntryRef, PageNumCas createdTreePageNum) {
		
		int entryRef = rootEntry.getInt();
		if (entryRef == 0) {
			// no tree, create a tree with a single entry
			
			createdEntryRef = space.allocate(estimateEntrySize(treeName));
			createdTreePageNum = writeEntry(createdEntryRef, treeName);
			
			if (rootEntry.casInt(0, createdEntryRef)) {
				return createdTreePageNum;
			}
			
			ThreadUtil.loopSleep();
			return doFindOrCreate(treeName, createdEntryRef, createdTreePageNum);
		}
		
		PageReader pageReader = new PageReader(paging);
		
		while(true) {
		
			space.seek(pageReader, entryRef);
			int entryPos = pageReader.getPosition();
			
			pageReader.seek(getTreeNamePos(entryPos));
			
			int c = pageReader.compareToString(treeName);
	       
			if (c > 0) {
				pageReader.seek(getLesserPos(entryPos));
				int lesserEntryRef = pageReader.readInt();
				if (lesserEntryRef != 0) {
					entryRef = lesserEntryRef;
					continue;
				}
				
				if (createdEntryRef == 0) {
					createdEntryRef = space.allocate(estimateEntrySize(treeName));
					createdTreePageNum = writeEntry(createdEntryRef, treeName);
				}
				
				pageReader.seek(getLesserPos(entryPos));
				IntegerCas lessCas = pageReader.readIntegerCas();
				
				if (lessCas.casInt(0, createdEntryRef)) {
					return createdTreePageNum;
				}
				
				ThreadUtil.loopSleep();
				return doFindOrCreate(treeName, createdEntryRef, createdTreePageNum);
				
			}
			else if (c < 0) {
				pageReader.seek(getGreaterPos(entryPos));
				int greaterEntryRef = pageReader.readInt();
				if (greaterEntryRef != 0) {
					entryRef = greaterEntryRef;
					continue;
				}
				
				if (createdEntryRef == 0) {
					createdEntryRef = space.allocate(estimateEntrySize(treeName));
					createdTreePageNum = writeEntry(createdEntryRef, treeName);
				}
				
				pageReader.seek(getGreaterPos(entryPos));
				IntegerCas greaterCas = pageReader.readIntegerCas();
				
				if (greaterCas.casInt(0, createdEntryRef)) {
					return createdTreePageNum;
				}
				
				ThreadUtil.loopSleep();
				return doFindOrCreate(treeName, createdEntryRef, createdTreePageNum);
				
			}
			else {
				// found exact match
				pageReader.seek(getTreePos(entryPos));
				return pageReader.readPageNumCas();
			}
		
		}
		
	}
	
	private int getTreeNamePos(int entryPos) {
		return entryPos + 4 + 4 + paging.getPageNum().size();
	}
	
	private int getLesserPos(int entryPos) {
		return entryPos;
	}

	private int getGreaterPos(int entryPos) {
		return entryPos + 4;
	}

	private int getTreePos(int entryPos) {
		return entryPos + 4 + 4;
	}

	private int estimateEntrySize(String treeName) {
		return  4 + 4 + paging.getPageNum().size() + PageWriter.estimateStringSize(treeName);
	}
	
	private PageNumCas writeEntry(int entryRef, String treeName) {
		
		PageWriter pageWriter = new PageWriter(paging);
		space.seek(pageWriter, entryRef);
		
		pageWriter.writeInt(0);
		pageWriter.writeInt(0);
		
		PageNumCas treePageNum = pageWriter.writePageNumCas();
		treePageNum.putPageNum(0);
		
		pageWriter.writeString(treeName);
		
		return treePageNum;
	}
	
}
