/*
 * MAE - Multi-purpose Annotation Environment
 *
 * Copyright Keigh Rim (krim@brandeis.edu)
 * Department of Computer Science, Brandeis University
 * Original program by Amber Stubbs (astubbs@cs.brandeis.edu)
 *
 * MAE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, @see <a href="http://www.gnu.org/licenses">http://www.gnu.org/licenses</a>.
 *
 * For feedback, reporting bugs, use the project repo on github
 * @see <a href="https://github.com/keighrim/mae-annotation">https://github.com/keighrim/mae-annotation</a>
 */

package edu.brandeis.cs.nlp.mae.database;

import edu.brandeis.cs.nlp.mae.model.*;
import edu.brandeis.cs.nlp.mae.util.MappedList;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * AnnotationTask serves as a go-between for MaeGui and the
 * SQLite interface TagDB.
 *
 * @author Amber Stubbs, Keigh Rim
 */

// TODO 151219 no longer needed, all functionality is re-implemented in DBDriver
@Deprecated
public class AnnotationTask {

    private Hashtable<String, Elem> mElements;
    private Hashtable<String, AttID> mIdTracker;
    private MappedList<String, String> mIdsExist;

    private AnnotDB mDb;
    private DTD mDtd;
    private boolean hasDTD;
    private int mMaxArgs;

    public AnnotationTask() {
        mDb = new AnnotDB();
        hasDTD = false;
    }

    public void resetDb() {
        mDb.closeDb();
        mDb = new AnnotDB(mMaxArgs);
    }

    public void setWorkingFile(String filename) {
        mDb.setWorkingFile(filename);
    }

    public void resetIdTracker() {
        mIdTracker = createIDTracker();
        mIdsExist = createIDsExist();
    }

    private Hashtable<String, Elem> createHash() {
        Hashtable<String, Elem> es = new Hashtable<String, Elem>();
        ArrayList<Elem> elems = mDtd.getAllTagTypes();
        for (Elem elem : elems) {
            es.put(elem.getName(), elem);
        }
        return (es);
    }

    /**
     * The IDTracker hashtable keeps one ID for each element that
     * has an ID, and increments the number so that no two
     * tags of the same type will have the same ID.
     *
     * @return a Hashtable containing a key for each element in the
     * mDtd paired with an AttID that contains the information about
     * the IDs for that element (prefix, name, etc).
     *
     * @see AttID
     */
    private Hashtable<String, AttID> createIDTracker() {
        Hashtable<String, AttID> ids = new Hashtable<String, AttID>();
        ArrayList<Elem> elems = mDtd.getAllTagTypes();
        for (Elem elem : elems) {
            ArrayList<Attrib> attribs = elem.getAttributes();
            for (Attrib attrib : attribs) {
                if (attrib instanceof AttID) {
                    AttID oldid = (AttID) attrib;
                    AttID id = new AttID(oldid.getName(),
                            oldid.getPrefix(), true);
                    id.setNumber(0);
                    ids.put(elem.getName(), id);
                }
            }
        }
        return ids;
    }

    /**
     * mIdsExist is a Hash collection used only when filling the
     * database from a file.  It holds the IDs that have already
     * been read in, and is used to ensure that no duplicate element-id
     * pairs are entered.  This used to be done by checking the database,
     * but this system is faster and allows the number of batch
     * executions to be lowered.
     *
     * @return a HashCollection that will contain all IDs that are in use
     * for each tag in the DTD
     */
    private MappedList<String, String> createIDsExist() {
        MappedList<String, String> ids = new MappedList<String, String>();
        ArrayList<Elem> elems = mDtd.getAllTagTypes();
        for (Elem elem : elems) {
            ids.putItem(elem.getName(), "");
        }
        return ids;
    }

    /**
     * Finds the next ID that can be used for that element
     *
     * @param element the type tag seeking an ID
     * @return the ID that will be assigned to the tag being created
     */
    public String getNextID(String element) {
        AttID id = mIdTracker.get(element);
        String nextid = id.getID();
        id.incrementNumber();
        //check to see if nextid is already in db
        //this will catch cases where two tags have
        //the same prefix
        try {
            while (mDb.idExists(nextid)) {
                nextid = id.getID();
                id.incrementNumber();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return nextid;
    }


    // ***************
    // The methods enclosed between the ****** lines interact with the
    // tag database (TabDB) in order to provide tag information to MAE.
    // Exceptions are usually caught here, rather than passed back to
    // main.

    public ArrayList<int[]> getSpansByTid(String id) {
        try {
            return mDb.getSpansByTid(id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getTagTypeByTid(String id) {
        try {
            return mDb.getTagTypeByTid(id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Hashtable<Integer, String> getAllLocationsOfTagType(String elem) {
        try {
            return mDb.getAllLocationsOfTagType(elem);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Hashtable<Integer, String> getAllLocationsOfTagType
            (String elem, ArrayList<String> active) {
        try {
            return mDb.getAllLocationsOfTagType(elem, active);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void removeLinkByID(String id) {
        try {
            mDb.removeLinkTag(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeExtentByID(String id) {
        try {
            mDb.removeExtentTag(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public MappedList<String, String> getLinksHasArgumentOf(String e_name, String id) {
        try {
            return (mDb.getLinksHasArgumentOf(e_name, id));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (new MappedList<String, String>());
    }

    public MappedList<String, String> getAllLocationsWithTags() {
        try {
            return (mDb.getLocElemHash());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (new MappedList<String, String>());
    }

    public void addExtToBatch(int pos, String elemName, String newId) {
        try {
            mDb.createExtentTag(pos, elemName, newId);
            mIdsExist.putItem(elemName, newId);
        } catch (Exception e) {
            System.err.println("Error adding extent to DB");
            e.printStackTrace();
        }

    }

    /**
     * Method to add a link tag to SQL batch for adding later
     * Use runBatchLink() to add everything in batch to DB
     *
     * @param elemName Type of new link tag being added
     * @param newID    ID of new link tag being added
     * @param argIds   List of arg IDs
     * @param argTypes List of arg Types
     */
    public void addLinkToBatch(String elemName, String newID,
                               List<String> argIds, List<String> argTypes) {
        mDb.createLinkTag(newID, elemName, argIds, argTypes);
        mIdsExist.putItem(elemName, newID);
    }

    public void addArgument(
            String id, int argNum, String argId, String argType) {
        try {
            mDb.addArgument(id, argNum, argId, argType);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public ArrayList<String> getTagsAt(int loc) {
        try {
            return (mDb.getTagsAt(loc));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Get all tags in target spans
     * Added by krim
     *
     * @param spans a list of start-end pairs
     * @return all tags in every span
     */
    public MappedList<String, String> getTagsByTypesIn(ArrayList<int[]> spans) {
        MappedList<String, String> nameToId = new MappedList<String, String>();
        for (int[] span : spans) {
            nameToId.merge(getTagsByTypesBetween(span[0], span[1]));
        }
        return nameToId;
    }

    public MappedList<String, String> getTagsByTypesBetween(int begin, int end) {
        try {
            return (mDb.getTagsByTypesBetween(begin, end));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    MappedList<String, String> getNCTags() {
        try {
            return mDb.getAllNCTags();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    MappedList<String, String> getAllExtTags(boolean includeNC) {
        MappedList<String, String> hc = new MappedList<String, String>();
        try {
            hc.merge(mDb.getAllConsumingTags());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (includeNC) {
            try {
                hc.merge(mDb.getAllNCTags());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return hc;
    }

    /**
     * Get all tags in target spans and also NC tags
     * Added by krim
     *
     * @param spans a sorted set of start-end pairs
     * @return all tags in every spans
     */
    public MappedList<String, String> getTagsInSpansAndNC(ArrayList<int[]> spans) throws Exception {
        MappedList<String, String> hc = new MappedList<String, String>();
        hc.merge(getTagsByTypesIn(spans));
        hc.merge(mDb.getAllNCTags());
        return hc;
        /*
        Iterator<int[]> iter = spans.iterator();
        while (iter.hasNext()) {
            int[] span = iter.next();
            if (iter.hasNext()) {
                hc.merge(getTagsByTypesBetween(span[0], span[1]));
            } else {
                hc.merge(getTagsInSpansAndNC(span[0], span[1]));
            }
        }
        return hc;
    }

    public HashCollection<String,String> getTagsInSpansAndNC(int begin, int end){
        try{
            return (mDb.getTagsInSpansAndNC(begin,end));
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
        */
    }

    public void runBatchExtents() {
        try {
            mDb.batchExtents();
        } catch (Exception e) {
            System.err.println("Error adding all extents to DB");
            e.printStackTrace();
        }
    }

    public void runBatchLinks() {
        try {
            mDb.batchLinks();
        } catch (Exception e) {
            System.err.println("Error adding all links to DB");
            e.printStackTrace();
        }
    }

    // ****************

    // the remaining methods provide information about the DTD and
    // its elements to MaeGui

    public void setDtd(DTD d) {
        mDtd = d;
        mMaxArgs = d.getMaxArgs();
        mDb = new AnnotDB(mMaxArgs);
//        mDb.setMaxArgs(mMaxArgs);
        mElements = createHash();
        mIdTracker = createIDTracker();
        hasDTD = true;
    }

    public List<Elem> getAllTagTypes() {
        return mDtd.getAllTagTypes();
    }

    public List<Elem> getNonConsumingTagTypes() {
        return mDtd.getNonConsumingTagTypes();
    }


    public boolean idExists(String tagname, String id) {
        ArrayList<String> ids = (ArrayList<String>) mIdsExist.get(tagname);
        return ids.contains(id);
    }


    public ArrayList<String> getExtentTagTypes() {
        ArrayList<String> extents = new ArrayList<String>();
        ArrayList<Elem> elems = mDtd.getAllTagTypes();
        for (Elem e : elems) {
            if (e instanceof ElemExtent) {
                extents.add(e.getName());
            }
        }
        return extents;
    }

    public ArrayList<String> getLinkTagTypes() {
        ArrayList<String> links = new ArrayList<String>();
        ArrayList<Elem> elems = mDtd.getAllTagTypes();
        for (Elem e : elems) {
            if (e instanceof ElemLink) {
                links.add(e.getName());
            }
        }
        return links;
    }

    public List<String> getAllLinkTagsOfType(String linkName) {
        return mDb.getAllLinkTagsOfType(linkName);
    }

    public List<String> getAllLinkIds() {
        ArrayList<String> linkids = new ArrayList<String>();
        for (String linkName : getLinkTagTypes()) {
            for (String id : mDb.getAllLinkTagsOfType(linkName)) {
                linkids.add(id);
            }
        }
        return linkids;
    }

    public List<String> getAllExtentTagsOfType(String elemName) {
        return mDb.getAllExtentTagsOfType(elemName);
    }

    public List<String> getAllExtIds() {
        ArrayList<String> extIds = new ArrayList<String>();
        for (String extName : getExtentTagTypes()) {
            for (String id : mDb.getAllExtentTagsOfType(extName)) {
                extIds.add(id);
            }
        }
        return extIds;
    }

    public ArrayList<String> getEmptyExtentElements() {
        //this method returns a list of element types 
        //where start and end are optional
        ArrayList<String> extents = new ArrayList<String>();
        ArrayList<Elem> elems = mDtd.getAllTagTypes();
        for (Elem e : elems) {
            if (e instanceof ElemExtent) {
                ElemExtent ee = (ElemExtent) e;
                Attrib a = ee.getAttribute("id");
                if (!a.isRequired()) {
                    extents.add(e.getName());
                }
            }
        }
        return extents;
    }

    public Hashtable<String, Elem> getElemHash() {
        return mElements;
    }

    public Elem getTagTypeByName(String name) {
        return mElements.get(name);
    }

    public boolean hasDTD() {
        return hasDTD;
    }

    public String getDTDName() {
        return mDtd.getName();
    }

    public List<String> getArgumentTypesOfLinkTagType(String name) {
        try {
            ElemLink linkElem = (ElemLink) mElements.get(name);
            return linkElem.getArguments();
        } catch (ClassCastException e) {
            e.printStackTrace();
            System.err.println("Invalid name: not a link tag");
            return null;
        }
    }
}