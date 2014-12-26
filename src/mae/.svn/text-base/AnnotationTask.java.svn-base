/*
 * This file is part of MAE - Multi-purpose Annotation Environment
 * 
 * Copyright Amber Stubbs (astubbs@cs.brandeis.edu)
 * Department of Computer Science, Brandeis University
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package mae;

import java.util.*;

/** 
 * AnnotationTask serves as a go-between for MaeGui and the 
 * SQLite interface TagDB.  
 * 
 * 
 * @author Amber Stubbs 
 * @version 0.9.4 April 5, 2012
*/

class AnnotationTask {

    private Hashtable<String,Elem> elements;
    private Hashtable<String,AttID> idTracker;
    private HashCollection<String,String> idsExist;
    private TagDB tagTable;
    private DTD dtd;
    private boolean hasDTD;
    
    AnnotationTask(){
         tagTable = new TagDB();
         hasDTD = false;
    }
    
    public void reset_db(){
        tagTable.close_db();
        tagTable = new TagDB();
    }
    
    public void reset_IDTracker(){
        idTracker = createIDTracker();
        idsExist = createIDsExist();
    }
    
    private Hashtable<String,Elem> createHash(){
        Hashtable<String,Elem> es=new Hashtable<String,Elem>();
        ArrayList<Elem>elems = dtd.getElements();
        for(int i=0;i<elems.size();i++){
             es.put(elems.get(i).getName(),elems.get(i));
        } 
        return(es);
   }
   
/**
 * The IDTracker hashtable keeps one ID for each element that
 * has an ID, and increments the number so that no two 
 * tags of the same type will have the same ID.
 * 
 * @return a Hashtable containing a key for each element in the
 * dtd paired with an AttID that contains the information about 
 * the IDs for that element (prefix, name, etc).
 * 
 * @see AttID
*/
   private Hashtable<String,AttID> createIDTracker(){
       Hashtable<String,AttID> ids = new Hashtable<String,AttID>();
       ArrayList<Elem>elems = dtd.getElements();
        for(int i=0;i<elems.size();i++){
            ArrayList<Attrib> attribs = elems.get(i).getAttributes();
            for(int j=0;j<attribs.size();j++){
               if (attribs.get(j) instanceof AttID){
                   AttID oldid = (AttID)attribs.get(j);
                    AttID id = new AttID(oldid.getName(),
                            oldid.getPrefix(),true);
                    id.setNumber(0);
                    ids.put(elems.get(i).getName(),id);
                 }
            }
        }        
       return ids;
       }

/**
 * idsExist is a Hash collection used only when filling the 
 * database from a file.  It holds the IDs that have already 
 * been read in, and is used to ensure that no duplicate element-id
 * pairs are entered.  This used to be done by checking the database,
 * but this system is faster and allows the number of batch 
 * executions to be lowered.
 * 
 * @return a HashCollection that will contain all IDs that are in use 
 * for each tag in the DTD
*/
   private HashCollection<String,String> createIDsExist(){
       HashCollection<String,String> ids = new HashCollection<String,String>();
       ArrayList<Elem>elems = dtd.getElements();
        for(int i=0;i<elems.size();i++){
                    ids.putEnt(elems.get(i).getName(),"");
        }        
       return ids;
       }
   
   /**
    * Finds the next ID that can be used for that element
    * 
    * @param element the type tag seeking an ID
    * @return the ID that will be assigned to the tag being created
    */
   public String getNextID(String element){
       AttID id = idTracker.get(element);
       String nextid = id.getID();
       id.incrementNumber();
       //check to see if nextid is already in db
       //this will catch cases where two tags have
       //the same prefix
       try{
       while(tagTable.idExists(nextid)){
           nextid = id.getID();
           id.incrementNumber();
       }
       }catch(Exception e){
           System.out.println(e.toString());
           }
       return nextid;
       
   }
   


  // ***************
  // The methods enclosed between the ****** lines interact with the 
  // tag database (TabDB) in order to provide tag information to MAE.
  // Exceptions are usually caught here, rather than passed back to 
  // main.
   
   public String getLocByID(String id){  
	      try{
	          String loc = tagTable.getLocByID(id);
	          return loc;
	      }catch(Exception e){
	          System.out.println(e.toString());
	          return null;
	      }
	  }
  
  public String getElementByID(String id){
      try{
          String elem = tagTable.getElementByID(id);
          return elem;
      }catch(Exception e){
          System.out.println(e.toString());
          return null;
      }
  }
  
  public Hashtable<Integer,String> getLocationsbyElemLink(String elem){
      try{
          Hashtable<Integer,String> locs = tagTable.getLocationsbyElemLink(elem);
          return locs;
      }catch(Exception e){
          System.out.println(e.toString());
          return null;
      }
  }
  
  public Hashtable<Integer,String> getLocationsbyElemLink(
     String elem,ArrayList<String> active){
      try{
          Hashtable<Integer,String> locs = tagTable.getLocationsbyElemLink(elem,active);
          return locs;
      }catch(Exception e){
          System.out.println(e.toString());
          return null;
      }
  }

   public void removeExtentByID(String e_name,String id){
       try{
          tagTable.removeExtentTags(e_name,id);
       }catch(Exception e){
           System.out.println(e.toString());
       }
   }
   
   public HashCollection<String,String> getLinksByExtentID(String e_name,String id){
       try{
          return(tagTable.getLinksByExtentID(e_name,id));
       }catch(Exception e){
           System.out.println(e.toString());
       }
       return (new HashCollection<String,String>());
   }
   
   public HashCollection<String,String> getElementsAllLocs(){
       try{
           return(tagTable.getElementsAllLocs());
       }catch(Exception e){
           System.out.println(e.toString());
       }
       return (new HashCollection<String,String>());
   }

    void addToDB(int start, String elem, String id, boolean insert){
            try{
                if (insert==true){
                    tagTable.insert_extent(start,elem,id);
                    idsExist.putEnt(elem,id);
                }
                else{
                    tagTable.add_extent(start,elem,id);
                    idsExist.putEnt(elem,id);

                }
            }catch(Exception e){
                System.out.println("Error adding extent to DB");
                System.out.println(e.toString());
            }
            
        }
        
    public void addToDB(String newID, String linkName, String linkFrom, 
        String from_name, String linkTo, String to_name, boolean insert){
        try{
            if (insert==true){
                tagTable.insert_link(newID,linkName,linkFrom,from_name,linkTo,to_name);
                idsExist.putEnt(linkName,newID);
            }
            else{
                tagTable.add_link(newID,linkName,linkFrom,from_name,linkTo,to_name);
                idsExist.putEnt(linkName,newID);
            }
        }catch(Exception e){
            System.out.println("Error adding link to DB");
            System.out.println(e.toString());
        }
        
    }
    
    ArrayList<String> getElemntsLoc(int loc){
        try{
          return (tagTable.getElementsAtLoc(loc));
        }catch(Exception e){
            System.out.println(e.toString());
        }
        return null;
    }
    

    public HashCollection<String,String> getTagsSpan(int begin, int end){
        try{
          return (tagTable.getTagsInSpan(begin,end));
        }catch(Exception e){
            System.out.println(e.toString());
        }
        return null;
    }
    
    
    
      public HashCollection<String,String> getTagsSpanAndNC(int begin, int end){
        try{
          return (tagTable.getTagsInSpanAndNC(begin,end));
        }catch(Exception e){
            System.out.println(e.toString());
        }
        return null;
    }
    
    public void batchExtents(){
            try{
                tagTable.batchExtents();
            }catch(Exception e){
                System.out.println("Error adding all extents to DB");
                System.out.println(e.toString());
            }
        }
        
    public void batchLinks(){
            try{
                tagTable.batchLinks();
            }catch(Exception e){
                System.out.println("Error adding all links to DB");
                System.out.println(e.toString());
            }
        }
    
    // ****************
    
    // the remaining methods provide information about the DTD and
    // its elements to MaeGui

   public void setDTD(DTD d){
       dtd=d;
       elements = createHash();
       idTracker = createIDTracker();
       hasDTD=true;
   }

  public ArrayList<Elem> getElements(){
      return dtd.getElements();
  }
  
  public ArrayList<Elem> getNCElements(){
      return dtd.getNCElements();
   }
 
  
  public boolean idExists(String tagname, String id){
      ArrayList<String> ids = idsExist.get(tagname);
      if (ids.contains(id)){
          return true;
      }
      return false;
  }
  
  
  public ArrayList<String> getExtentElements(){
      ArrayList<String> extents = new ArrayList<String>();
      ArrayList<Elem> elems = dtd.getElements();
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
      ArrayList<Elem> elems = dtd.getElements();
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
      ArrayList<Elem> elems = dtd.getElements();
      for(int i=0;i<elems.size();i++){
          Elem e = elems.get(i);
          if(e instanceof ElemExtent){
              ElemExtent ee = (ElemExtent)e;
              Attrib a = ee.getAttribute("id");
              if(a.getRequired()==false){
                extents.add(e.getName());
              }
          }
      }
      return extents;
  }
  
  public Hashtable<String,Elem> getElemHash(){
      return elements;
  }
  
  public Elem getElem(String name){
      return elements.get(name);
  }
  
  public boolean hasDTD(){
      return hasDTD;
  }
  
  public String getDTDName(){
      return dtd.getName();
  }

}