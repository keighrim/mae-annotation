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
import java.util.Iterator;

/** 
 * AnnotationTask serves as a go-between for MaeGui and the 
 * SQLite interface TagDB.  
 *
 * @author Amber Stubbs, Keigh Rim
 * @version v0.10
 */

class AnnotationTask {

    private Hashtable<String,Elem> mElements;
    private Hashtable<String,AttID> mIdTracker;
    private HashCollection<String,String> mIdsExist;

    private AnnotDB mDB;
    private DTD mDtd;
    private boolean hasDTD;
    
    AnnotationTask(){
         mDB = new AnnotDB();
         hasDTD = false;
    }
    
    public void reset_db(){
        mDB.close_db();
        mDB = new AnnotDB();
    }
    
    public void reset_IDTracker() {
        mIdTracker = createIDTracker();
        mIdsExist = createIDsExist();
    }

    private Hashtable<String,Elem> createHash() {
        Hashtable<String, Elem> es = new Hashtable<String, Elem>();
        ArrayList<Elem> elems = mDtd.getElements();
        for (int i = 0; i < elems.size(); i++) {
            es.put(elems.get(i).getName(), elems.get(i));
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
        for (int i = 0; i < elems.size(); i++) {
            ArrayList<Attrib> attribs = elems.get(i).getAttributes();
            for (int j = 0; j < attribs.size(); j++) {
                if (attribs.get(j) instanceof AttID) {
                    AttID oldid = (AttID) attribs.get(j);
                    AttID id = new AttID(oldid.getName(),
                            oldid.getPrefix(), true);
                    id.setNumber(0);
                    ids.put(elems.get(i).getName(), id);
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
        for (int i = 0; i < elems.size(); i++) {
            ids.putEnt(elems.get(i).getName(), "");
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
            while (mDB.idExists(nextid)) {
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
            String loc = mDB.getLocByID(id);
            return loc;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getElementByID(String id){
        try{
            String elem = mDB.getElementByID(id);
            return elem;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public Hashtable<Integer,String> getLocationsbyElemLink(String elem){
        try{
            Hashtable<Integer,String> locs = mDB.getLocationsbyElemLink(elem);
            return locs;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public Hashtable<Integer,String> getLocationsbyElemLink
            (String elem, ArrayList<String> active){
        try{
            Hashtable<Integer,String> locs = mDB.getLocationsbyElemLink(elem,active);
            return locs;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

   public void removeExtentByID(String e_name,String id){
       try{
          mDB.removeExtentTags(e_name, id);
       }catch(Exception e){
           e.printStackTrace();
       }
   }
   
   public HashCollection<String,String> getLinksByExtentID(String e_name,String id){
       try{
          return(mDB.getLinksByExtentID(e_name,id));
       }catch(Exception e){
           e.printStackTrace();
       }
       return (new HashCollection<String,String>());
   }
   
   public HashCollection<String,String> getElementsAllLocs(){
       try{
           return(mDB.getElementsAllLocs());
       }catch(Exception e){
           e.printStackTrace();
       }
       return (new HashCollection<String,String>());
   }

    void addToDB(int start, String elem, String id, boolean insert){
        try{
            if (insert){
                mDB.insert_extent(start, elem, id);
                mIdsExist.putEnt(elem, id);
            }
            else{
                mDB.add_extent(start, elem, id);
                mIdsExist.putEnt(elem, id);

            }
        } catch(Exception e) {
            System.out.println("Error adding extent to DB");
            e.printStackTrace();
        }

    }

    public void addToDB(String newID, String linkName, String linkFrom, 
        String from_name, String linkTo, String to_name, boolean insert){
        try{
            if (insert){
                mDB.insert_link(newID, linkName, linkFrom, from_name, linkTo, to_name);
                mIdsExist.putEnt(linkName, newID);
            }
            else{
                mDB.add_link(newID, linkName, linkFrom, from_name, linkTo, to_name);
                mIdsExist.putEnt(linkName, newID);
            }
        }catch(Exception e){
            System.out.println("Error adding link to DB");
            e.printStackTrace();
        }
        
    }
    
    ArrayList<String> getElemntsLoc(int loc){
        try{
          return (mDB.getElementsAtLoc(loc));
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Get all tags in target spans
     * Added by krim
     * @param spans a list of start-end pairs
     * @return all tags in every spans
     * @throws Exception
     */
    public HashCollection<String,String> getTagsSpan(ArrayList<int[]> spans) {
        HashCollection<String, String> tagsAndAtts = new HashCollection<String, String>();
        for (int[] span : spans) {
            tagsAndAtts.putAll(getTagsSpan(span[0], span[1]));
        }
        return tagsAndAtts;
    }

    public HashCollection<String,String> getTagsSpan(int begin, int end){

        try{
            return (mDB.getTagsInSpan(begin, end));
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }



    /**
     * Get all tags in target spans
     * Added by krim
     *
     * @param spans a sorted set of start-end pairs
     * @return all tags in every spans
     * @throws Exception
     */
    public HashCollection<String,String> getTagsSpanAndNC(ArrayList<int[]> spans) throws Exception{
        HashCollection<String, String> hc = new HashCollection<String, String>();
        Iterator<int[]> iter = spans.iterator();
        while (iter.hasNext()) {
            int[] span = iter.next();
            if (iter.hasNext()) {
                hc.putAll(getTagsSpan(span[0], span[1]));
            } else {
                hc.putAll(getTagsSpanAndNC(span[0], span[1]));
            }
        }
        return hc;
    }

    public HashCollection<String,String> getTagsSpanAndNC(int begin, int end){
        try{
            return (mDB.getTagsInSpanAndNC(begin,end));
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
    
    public void batchExtents(){
        try{
            mDB.batchExtents();
        }catch(Exception e){
            System.out.println("Error adding all extents to DB");
            e.printStackTrace();
        }
    }
        
    public void batchLinks(){
            try{
                mDB.batchLinks();
            }catch(Exception e){
                System.out.println("Error adding all links to DB");
                e.printStackTrace();
            }
        }
    
    // ****************
    
    // the remaining methods provide information about the DTD and
    // its elements to MaeGui

    public void setDTD(DTD d){
       mDtd =d;
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
        if (ids.contains(id)){
            return true;
        }
        return false;
    }
  
  
    public ArrayList<String> getExtentElements(){
        ArrayList<String> extents = new ArrayList<String>();
        ArrayList<Elem> elems = mDtd.getElements();
        for(int i=0;i<elems.size();i++){
            Elem e = elems.get(i);
            if(e instanceof ElemExtent){
                extents.add(e.getName());
            }
        }
        return extents;
    }
  
    public ArrayList<String> getLinkElements(){
        ArrayList<String> links = new ArrayList<String>();
        ArrayList<Elem> elems = mDtd.getElements();
        for(int i=0;i<elems.size();i++){
            Elem e = elems.get(i);
            if(e instanceof ElemLink){
                links.add(e.getName());
            }
        }
        return links;
    }
  
    public ArrayList<String> getEmptyExtentElements(){
        //this method returns a list of element types 
        //where start and end are optional
        ArrayList<String> extents = new ArrayList<String>();
        ArrayList<Elem> elems = mDtd.getElements();
        for(int i=0;i<elems.size();i++){
            Elem e = elems.get(i);
            if(e instanceof ElemExtent){
                ElemExtent ee = (ElemExtent)e;
                Attrib a = ee.getAttribute("id");
                if(!a.getRequired()){
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

}