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


import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;

/**
 * TagDB is the class that handles all the calls to the 
 * SQLite database.  TagDB in MAE has two tables:
 * 1) extents, with columns: location int(5), element_name, id
 * 2) links, with columns: id,fromid,from_name,toid,to_name,element_name
 * <p>
 * User-defined attribute information about the tags that are being 
 * created is not stored in the database; it exists only in the 
 * tables that are part of MaeGui.  Therefore if the program 
 * is closed without the file being saved, the tags cannot
 * be completely recovered from the database.
 *
 * @author Amber Stubbs, Keigh Rim
 * @version v0.10
 *
 */

class AnnotDB {
    // mod by krim: renamed corresponding MAI

    private PreparedStatement mExtInsert;
    private PreparedStatement mLinkInsert;
    private Connection mConn;


    /**
     * Clears out the database and creates the 
     * tables and PreparedStatements.
     * 
     */
    AnnotDB(){
        try{
            Class.forName("org.sqlite.JDBC");
            mConn = DriverManager.getConnection("jdbc:sqlite:tag.db");
            Statement stat = mConn.createStatement();
            stat.executeUpdate("drop table if exists extents;");
            stat.executeUpdate("create table extents (location int(5), element_name, id);");
            stat.executeUpdate("drop table if exists links;");
            stat.executeUpdate("create table links (id,fromid,from_name,toid,to_name,element_name);");
            mExtInsert = mConn.prepareStatement("insert into extents values (?, ?, ?);");
            mLinkInsert = mConn.prepareStatement("insert into links values (?, ?, ?, ?, ?, ?);");
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void print_extents(){
        System.out.println("Extents in DB:");
        try {
            Statement stat = mConn.createStatement();
            ResultSet rs = stat.executeQuery("select * from extents;");
            int i = 0;
            while (rs.next()) {
                if (i % 10 == 0) {
                    System.out.printf("%20s\t%20s\t%20s%n",
                            "location", "element", "id");
                }
                System.out.printf("%20s\t%20s\t%20s%n",
                        rs.getString("location"),
                        rs.getString("element_name"),
                        rs.getString("id"));
                i++;
            }
            rs.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void print_links(){
        System.out.println("Links in DB:");
        try {
            Statement stat = mConn.createStatement();
            ResultSet rs = stat.executeQuery("select * from links;");
            int i = 0;
            while (rs.next()) {
                if (i % 10 == 0) {
                    System.out.printf("%20s\t%20s\t%20s\t%20s\t%20s%%%\t%20s%n",
                            "id", "from", "f_name", "to", "t_name", "element");
                }
                System.out.printf("%20s\t%20s\t%20s%n",
                        rs.getString("id"),
                        rs.getString("fromid"),
                        rs.getString("from_name"),
                        rs.getString("toid"),
                        rs.getString("to_name"),
                        rs.getString("element_name"));
                i++;
            }
            rs.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 
     * @param loc the character offset of the location being looked at
     * @return ArrayList of strings containing the types of elements at a location
     * @throws Exception 
     */
    ArrayList<String> getElementsAtLoc(int loc)
            throws Exception{
        Statement stat = mConn.createStatement();
        String query = "select * from extents where location = " + loc + ";";
        ResultSet rs = stat.executeQuery(query);
        ArrayList<String> elems = new ArrayList<String>();
        while(rs.next()){
            elems.add(rs.getString("element_name"));
        }
        rs.close();
        return elems;
    }

    /**
     * Used to color all the extent tag locations when an 
     * annotation is loaded.
     * 
     * @return Returns a HashCollection of all the tags in the 
     * annotation; only locations where tags exists are used as 
     * keys
     * 
     * @throws Exception
     */
    HashCollection<String,String> getElementsAllLocs()
            throws Exception{
        HashCollection<String,String>elems = new HashCollection<String,String>();
        Statement stat = mConn.createStatement();
        String query = "select location,element_name from extents;";
        ResultSet rs = stat.executeQuery(query);
        while(rs.next()){
            elems.putEnt(rs.getString("location"), rs.getString("element_name"));
        }
        rs.close();
        return elems;
    }

    /**
     * Gets all character offsets of tags that are used to anchor the type 
     * of link selected.
     *
     * @param elem the name of the link tag being searched for
     * @return A hashtable where the keys are the character offsets 
     * of each anchor that tag type uses.
     * @throws Exception
     */
    Hashtable<Integer,String> getLocationsbyElemLink(String elem)
            throws Exception{
        Statement stat = mConn.createStatement();
        //first, get all the IDs for the extents associated with the ElemLink
        String query = "select * from links where element_name = '" + elem + "';";
        ResultSet rs = stat.executeQuery(query);
        ArrayList<String>ids = new ArrayList<String>();
        while (rs.next()){
            ids.add(rs.getString("fromid"));
            ids.add(rs.getString("toid"));
        }
        rs.close();

        Hashtable<Integer,String> locs = new Hashtable<Integer,String>();
        for (int i = 0;i<ids.size();i++){
            String id = ids.get(i);
            query = "select * from extents where id = '" + id + "';";
            rs = stat.executeQuery(query);
            while (rs.next()){
                locs.put((Integer.parseInt(rs.getString("location"))),"");
            }
        }
        return(locs);
    }

    /**
     * Used to determine what areas of the text should be bolded
     * and italicized when a link tag is unselected from the 
     * menu.
     *
     * @param elem name of the link tag being looked at
     * @param active an ArrayList of the 
     * @return a hashTable of locations that should be 
     * bolded and italicized based on the selections in the
     * GUI menu
     * @throws Exception
     */
    Hashtable<Integer,String> getLocationsbyElemLink(
            String elem, ArrayList<String>active) throws Exception{
        Statement stat = mConn.createStatement();
        //first, get all the IDs for the extents associated with the ElemLink
        String query = "select * from links where element_name = '" + elem + "';";
        ResultSet rs = stat.executeQuery(query);
        ArrayList<String>ids = new ArrayList<String>();
        while (rs.next()){
            ids.add(rs.getString("fromid"));
            ids.add(rs.getString("toid"));
        }
        rs.close();

        //then, go through and remove all the IDs that are associated with other 
        //actively bolded link tags
        for(int i=0;i<active.size();i++){
            String activeElem = active.get(i);
            ArrayList<String>outIDs = new ArrayList<String>();
            query = "select * from links where element_name = '" + activeElem + "';";
            rs = stat.executeQuery(query);
            while (rs.next()){
                outIDs.add(rs.getString("fromid"));
                outIDs.add(rs.getString("toid"));
            }
            ids.removeAll(outIDs);
        }
        //now that the list is down to only the IDs that will be removed,
        //get their locations
        Hashtable<Integer,String> locs = new Hashtable<Integer,String>();
        for (int i = 0;i<ids.size();i++){
            String id = ids.get(i);
            query = "select * from extents where id = '" + id + "';";
            rs = stat.executeQuery(query);
            while (rs.next()){
                locs.put((Integer.parseInt(rs.getString("location"))),"");
            }
        }
        return(locs);
    }

    /**
     * Returns the start and end of an extent tag based on the ID.
     * mod by krim: multi-span support
     *
     * @param id the ID tag being searched for
     * @return a string containing the start and end locations
     * of the tag being searched for.
     * 
     * @throws Exception
     */
    String getLocByID(String id)
            throws Exception{
        Statement stat = mConn.createStatement();
        String query = "select * from extents where id = '" + id + "';";
        ResultSet rs = stat.executeQuery(query);
        ArrayList<Integer>locs = new ArrayList<Integer>();
        while (rs.next()){
            locs.add(Integer.parseInt(rs.getString("location")));
        }

        Collections.sort(locs);

        rs.close();

        // add by krim: make a string representing multiple spans then return it
        int initLoc, endCandi;
        initLoc = endCandi = locs.get(0);
        String s = Integer.toString(initLoc);

        if (locs.size()>1) {
            for (int loc : locs) {
                if (loc <= endCandi+1) {
                    endCandi = loc;
                }
                else {
                    s += MaeMain.SPANDELIMITER + (endCandi+1) +
                            MaeMain.SPANSEPARATOR + loc;
                    endCandi = loc;
                }
            }
        }
        s += MaeMain.SPANDELIMITER + (locs.get(locs.size()-1)+1);
        return s;
    }

    /**
     * 
     * @param id the ID of the string being searched for
     * @return the tag name of the ID being searched for
     * @throws Exception
     */
    String getElementByID(String id)
            throws Exception{
        Statement stat = mConn.createStatement();
        String query = "select * from extents where id = '" + id + "';";
        ResultSet rs = stat.executeQuery(query);
        String elemName =  rs.getString("element_name");
        rs.close();
        return elemName;
    }

    /**
     * Removes an extent tag from the extents table
     * 
     * @param element_name the name of the tag type being removed
     * @param id the ID of the tag being removed
     * @throws Exception
     */
    void removeExtentTags(String element_name, String id)
            throws Exception{
        Statement stat = mConn.createStatement();
        String delete = ("delete from extents where id = '" 
                +id + "'and element_name = '" + element_name+ "';");
        stat.executeUpdate(delete);  
    }

    /**
     * Returns the links that an extent participates in as 
     * a to or from anchor.
     * 
     * @param element_name type of tag being searched for
     * @param id ID of tag being searched for
     * @return HashCollection of tag names and IDs that are 
     * associated with the extent being searched for
     * @throws Exception
     */
    HashCollection<String,String> getLinksByExtentID(String element_name, String id)
            throws Exception{
        HashCollection<String,String>links = new HashCollection<String,String>();
        Statement stat = mConn.createStatement();
        String query = ("select id,element_name from links where fromid = '" +
                id + "' and from_name  ='" + element_name + "';");
        ResultSet rs = stat.executeQuery(query);
        while(rs.next()){
            links.putEnt(rs.getString("element_name"),rs.getString("id"));
        }
        rs.close();

        String query2 = ("select id,element_name from links where toid = '" +
                id + "' and to_name  ='" + element_name + "';");
        ResultSet rs2 = stat.executeQuery(query2);
        while(rs2.next()){
            links.putEnt(rs2.getString("element_name"),rs2.getString("id"));
        }
        rs2.close();
        return links;
    }


    /**
     * Returns a HashCollection of ids and element types
     * that exist between the start and end character offsets.
     *
     * @param begin starting location being searched for
     * @param end ending location being searched for
     * @return HashCollection of ids and element types
     * that exist between the start and end character offsets with the
     * tag name as keys and IDs as values.
     * @throws Exception
     */
    HashCollection<String,String> getTagsInSpan(int begin, int end)
            throws Exception{
        Statement stat = mConn.createStatement();
        String query = "";
        if(begin!=end){
            query = ("select distinct(id), element_name from extents where location >= "
                    + begin + " and location <=" + end + ";");
        }
        else{
            query = ("select distinct(id), element_name from extents where location = "
                    + begin + ";");
        }

        ResultSet rs = stat.executeQuery(query);
        HashCollection<String,String> tags = new HashCollection<String,String>();
        while(rs.next()){
            tags.putEnt(rs.getString("element_name"), rs.getString("id"));
        }
        rs.close();
        return tags;
    }

    /**
     * Returns tags in the provided span as well as all non-consuming tags
     * 
     * @param begin starting location being searched for
     * @param end ending location being searched for
     * @return HashCollection of ids and element types
     * that exist between the start and end character offsets with the
     * tag name as keys and IDs as values.
     */
    HashCollection<String,String> getTagsInSpanAndNC(int begin, int end)
            throws Exception{
        Statement stat = mConn.createStatement();
        String query = "";
        if(begin!=end){
            query = ("select distinct(id), element_name from extents where location >= " 
                    + begin + " and location <=" + end + ";");
        }
        else{
            query = ("select distinct(id), element_name from extents where location = " 
                    + begin + ";");
        }

        ResultSet rs = stat.executeQuery(query);
        HashCollection<String,String> tags = new HashCollection<String,String>();
        while(rs.next()){
            tags.putEnt(rs.getString("element_name"),rs.getString("id"));
        }
        rs.close();

        //now get the non-consuming tags
        query = ("select distinct(id), element_name from extents where location = -1;");
        rs = stat.executeQuery(query);
        while(rs.next()){
            tags.putEnt(rs.getString("element_name"),rs.getString("id"));
        }
        rs.close();

        return tags;
    }

    /**
     * Checks to see if an ID is already in use in the DB.
     * 
     * @param id ID being searched for
     * @return true or false
     * @throws Exception
     */
    boolean idExists(String id)
            throws Exception{
        Statement stat = mConn.createStatement();
        String query = "select count(id) from extents where id = '" + id + "';";
        ResultSet rs = stat.executeQuery(query);
        int num = rs.getInt(1);
        rs.close();
        if (num>0){
            return true;
        }
        //also check link table    
        String query2 = "select count(id) from links where id = '" + id + "';";
        ResultSet rs2 = stat.executeQuery(query2);
        int num2 = rs2.getInt(1);
        rs2.close();
        if (num2>0){
            return true;
        }

        return false;
    }


    /**
     * Adds a single extent to the batch command
     * 
     * @param location character offset
     * @param element tag name
     * @param id ID
     * @throws Exception
     */
    void add_extent(int location, String element, String id)
            throws Exception{
        mExtInsert.setInt(1, location);
        mExtInsert.setString(2, element);
        mExtInsert.setString(3, id);
        mExtInsert.addBatch();
    }

    /**
     * Adds a set of extents to the DB at once
     * 
     * @throws Exception
     */
    void batchExtents() throws Exception{
        mConn.setAutoCommit(false);
        mExtInsert.executeBatch();
        mConn.setAutoCommit(true);
    }

    /**
     * Adds a single extent to the DB
     * @param location character offset
     * @param element tag name
     * @param id ID
     * @throws Exception
     */
    void insert_extent(int location, String element, String id)
            throws Exception{
        mExtInsert.setInt(1, location);
        mExtInsert.setString(2, element);
        mExtInsert.setString(3, id);
        mExtInsert.addBatch();
        mConn.setAutoCommit(false);
        mExtInsert.executeBatch();
        mConn.setAutoCommit(true);
    }

    /**
     * Adds a set of links to the DB at once
     * @throws Exception
     */
    void batchLinks() throws Exception{
        mConn.setAutoCommit(false);
        mLinkInsert.executeBatch();
        mConn.setAutoCommit(true);
    }

    /**
     * Adds a link to the batch 
     * @param newID ID string
     * @param linkName type of link being added
     * @param linkFrom ID of the from anchor
     * @param from_name tag type of the from anchor
     * @param linkTo ID of the to anchor
     * @param to_name tag type of the to anchor
     * @throws Exception
     */
    void add_link(String newID, String linkName, String linkFrom, 
            String from_name, String linkTo, String to_name) throws Exception{
        mLinkInsert.setString(1, newID);
        mLinkInsert.setString(2, linkFrom);
        mLinkInsert.setString(3, from_name);
        mLinkInsert.setString(4, linkTo);
        mLinkInsert.setString(5, to_name);
        mLinkInsert.setString(6, linkName);
        mLinkInsert.addBatch();
    }

    /**
     * 
     * @param newID String of the ID being added
     * @param linkName tag type of the link being added
     * @param linkFrom ID of the from anchor
     * @param from_name tag type of the from anchor
     * @param linkTo ID of the to anchor
     * @param to_name tag type of the to anchor
     * @throws Exception
     */
    void insert_link(String newID, String linkName, String linkFrom, 
            String from_name, String linkTo, String to_name) throws Exception{
        mLinkInsert.setString(1, newID);
        mLinkInsert.setString(2, linkFrom);
        mLinkInsert.setString(3, from_name);
        mLinkInsert.setString(4, linkTo);
        mLinkInsert.setString(5, to_name);
        mLinkInsert.setString(6, linkName);
        mLinkInsert.addBatch();
        mConn.setAutoCommit(false);
        mLinkInsert.executeBatch();
        mConn.setAutoCommit(true);
    }

    /**
     * Closes the connection to the DB
     */
    void close_db(){
        try{
            mConn.close();
        }catch(Exception e){
            System.out.println(e.toString());
        }
    }
}
