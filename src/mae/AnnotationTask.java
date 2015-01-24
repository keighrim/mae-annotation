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

package mae;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/** 
 * AnnotationTask serves as a go-between for MaeGui and the 
 * SQLite interface TagDB.  
 * @author Amber Stubbs, Keigh Rim
 * @version v0.11
 *
 */

class AnnotationTask {

    private Hashtable<String,Elem> mElements;
    private Hashtable<String,AttID> mIdTracker;
    private HashCollection<String,String> mIdsExist;

    private AnnotDB mDb;
    private DTD mDtd;
    private boolean hasDTD;
    private int mMaxArgs;
    
    AnnotationTask(){
         mDb = new AnnotDB();
         hasDTD = false;
    }
    
    public void resetDb(){
        mDb.closeDb();
        mDb = new AnnotDB();
        mDb.setMaxArgs(mMaxArgs);
    }
    
    public void resetIdTracker() {
        mIdTracker = createIDTracker();
        mIdsExist = createIDsExist();
    }

    private Hashtable<String,Elem> createHash() {
        Hashtable<String, Elem> es = new Hashtable<String, Elem>();
        ArrayList<Elem> elems = mDtd.getElements();
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
    private Hashtable<String,AttID> createIDTracker() {
        Hashtable<String, AttID> ids = new Hashtable<String, AttID>();
        ArrayList<Elem> elems = mDtd.getElements();
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
    private HashCollection<String,String> createIDsExist() {
        HashCollection<String, String> ids = new HashCollection<String, String>();
        ArrayList<Elem> elems = mDtd.getElements();
        for (Elem elem : elems) {
            ids.putEnt(elem.getName(), "");
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

    public String getLocByID(String id) {
        try {
            return mDb.getLocByID(id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getElementByID(String id){
        try{
            return mDb.getElementByID(id);
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public Hashtable<Integer,String> getLocationsbyElemLink(String elem){
        try{
            return mDb.getLocationsbyElemLink(elem);
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public Hashtable<Integer,String> getLocationsbyElemLink
            (String elem, ArrayList<String> active){
        try{
            return mDb.getLocationsbyElemLink(elem,active);
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

   public void removeExtentByID(String e_name,String id){
       try{
          mDb.removeExtentTags(e_name, id);
       }catch(Exception e){
           e.printStackTrace();
       }
   }
   
   public HashCollection<String,String> getLinksByExtentID(String e_name,String id){
       try {
           return(mDb.getLinksByExtentID(e_name, id));
       }catch(Exception e){
           e.printStackTrace();
       }
       return (new HashCollection<String,String>());
   }
   
   public HashCollection<String,String> getElementsAllLocs(){
       try{
           return(mDb.getElementsAllLocs());
       }catch(Exception e){
           e.printStackTrace();
       }
       return (new HashCollection<String,String>());
   }

    void addExtToBatch(int pos, String elemName, String newId){
        try{
            mDb.addExtent(pos, elemName, newId);
            mIdsExist.putEnt(elemName, newId);
        } catch(Exception e) {
            System.out.println("Error adding extent to DB");
            e.printStackTrace();
        }

    }

    /**
     * Method to add a link tag to SQL batch for adding later
     * Use runBatchLink() to add everything in batch to DB 
     *  @param elemName Type of new link tag being added
     * @param newID ID of new link tag being added
     * @param argIds List of arg IDs
     * @param argTypes List of arg Types
     */
    public void addLinkToBatch(String elemName, String newID,
                               List<String> argIds, List<String> argTypes) {
        mDb.addLink(newID, elemName, argIds, argTypes);
        mIdsExist.putEnt(elemName, newID);
    }

    ArrayList<String> getElemntsLoc(int loc){
        try{
          return (mDb.getElementsAtLoc(loc));
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Get all tags in target spans
     * Added by krim
     * @param spans a list of start-end pairs
     * @return all tags in every span
     */
    HashCollection<String,String> getTagsIn(ArrayList<int[]> spans) {
        HashCollection<String, String> nameToId = new HashCollection<String, String>();
        for (int[] span : spans) {
            nameToId.putAll(getTagsBetween(span[0], span[1]));
        }
        return nameToId;
    }

    HashCollection<String,String> getTagsBetween(int begin, int end){
        try{
            return (mDb.getTagsInSpan(begin, end));
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    HashCollection<String, String> getNCTags() {
        try {
            return mDb.getAllNCTags();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    HashCollection<String, String> getAllExtTags(boolean includeNC) {
        HashCollection<String, String> hc = new HashCollection<String, String>();
        try {
            hc.putAll(mDb.getAllExtTags());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (includeNC) {
            hc.putAll(getNCTags());
        }
        return hc;
    }
    
    /**
     * Get all tags in target spans and also NC tags
     * Added by krim
     *
     * @param spans a sorted set of start-end pairs
     * @return all tags in every spans
     * @throws Exception
     */
    public HashCollection<String,String> getTagsInSpansAndNC(ArrayList<int[]> spans) throws Exception{
        HashCollection<String, String> hc = new HashCollection<String, String>();
        hc.putAll(getTagsIn(spans));
        hc.putAll(getNCTags());
        return hc;
        /*
        Iterator<int[]> iter = spans.iterator();
        while (iter.hasNext()) {
            int[] span = iter.next();
            if (iter.hasNext()) {
                hc.putAll(getTagsBetween(span[0], span[1]));
            } else {
                hc.putAll(getTagsInSpansAndNC(span[0], span[1]));
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
    
    public void runBatchExtents(){
        try{
            mDb.batchExtents();
        }catch(Exception e){
            System.out.println("Error adding all extents to DB");
            e.printStackTrace();
        }
    }
        
    public void runBatchLinks(){
            try{
                mDb.batchLinks();
            }catch(Exception e){
                System.out.println("Error adding all links to DB");
                e.printStackTrace();
            }
        }
    
    // ****************
    
    // the remaining methods provide information about the DTD and
    // its elements to MaeGui

    public void setDtd(DTD d){
        mDtd = d;
        mMaxArgs =  d.getMaxArgs();
        mDb.setMaxArgs(mMaxArgs);
        mElements = createHash();
        mIdTracker = createIDTracker();
        hasDTD=true;
    }
    
    public ArrayList<Elem> getElements(){
        return mDtd.getElements();
    }
  
    public ArrayList<Elem> getNCElements(){
        return mDtd.getNCElements();
    }
 
  
    public boolean idExists(String tagname, String id){
        ArrayList<String> ids = mIdsExist.get(tagname);
        return ids.contains(id);
    }
  
  
    public ArrayList<String> getExtElemNames(){
        ArrayList<String> extents = new ArrayList<String>();
        ArrayList<Elem> elems = mDtd.getElements();
        for (Elem e : elems) {
            if (e instanceof ElemExtent) {
                extents.add(e.getName());
            }
        }
        return extents;
    }
  
    public ArrayList<String> getLinkElemNames(){
        ArrayList<String> links = new ArrayList<String>();
        ArrayList<Elem> elems = mDtd.getElements();
        for (Elem e : elems) {
            if (e instanceof ElemLink) {
                links.add(e.getName());
            }
        }
        return links;
    }

    public ArrayList<String> getLinkIdsByName(String linkName) {
        return mDb.getLinkIdsByName(linkName);
    }
    
    public ArrayList<String> getLinkIds() {
        ArrayList<String> linkids = new ArrayList<String>();
        for (String linkName : getLinkElemNames()) {
            for (String id : mDb.getLinkIdsByName(linkName)) {
                linkids.add(id);
            }
        }
        return linkids;
    }
  
    public ArrayList<String> getEmptyExtentElements(){
        //this method returns a list of element types 
        //where start and end are optional
        ArrayList<String> extents = new ArrayList<String>();
        ArrayList<Elem> elems = mDtd.getElements();
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

    public Hashtable<String,Elem> getElemHash(){
      return mElements;
    }

    public Elem getElem(String name) {
        return mElements.get(name);
    }

    public boolean hasDTD() {
        return hasDTD;
    }

    public String getDTDName() {
        return mDtd.getName();
    }

    public ArrayList<String> getArguments(String name) {
        try {
            ElemLink linkElem = (ElemLink) mElements.get(name);
            return linkElem.getArguments();
        } catch (ClassCastException e) {
            e.printStackTrace();
            System.out.println("Invalid name: not a link tag");
            return null;
        }
    }
}