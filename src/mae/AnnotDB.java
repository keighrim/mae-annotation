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


import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;

/**
 * TagDB is the class that handles all the calls to the 
 * SQLite database.  TagDB in MAE has two tables:
 * krim - these column design was originally from Amber, after re-writing modify here
 * 1) extents, with columns: 1-location int(5), 2-element_name, 3-id
 * <del>2) links, with columns: 1-id,2-fromid,3-from_name,4-toid,5-to_name,6-element_name</del>
 * mod by krim - links table is redesign for multi-linking support
 * 2) links, with columns: 1-location int(5), 2-element_name, 3-id,
 *    4 ... - arg0...argN
 *    4+mMaxArgs ... - arg0_name...argN_name
 * <p>
 * User-defined attribute information about the tags that are being 
 * created is not stored in the database; it exists only in the 
 * tables that are part of MaeGui.  Therefore if the program 
 * is closed without the file being saved, the tags cannot
 * be completely recovered from the database.
 * 
 * @author Amber Stubbs
 * @revised Keigh Rim
 *
 */

class AnnotDB {
    // mod by krim: class renamed corresponding MAI

    private PreparedStatement mExt2Insert;
    private PreparedStatement mLink2Insert;
    private Connection mConn;

    // integers for each column in the table
    final int LOC_COL = 1;
    final int NAME_COL = 2;
    final int ID_COL = 3;
    final int ARG0_COL = 4;
    private int mMaxArgs = 10; // by default, set max args to 10
    private int ARG0_TYPE_COL = ARG0_COL + mMaxArgs;
    // DO we need more than 10 arguments?

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
            stat.executeUpdate("DROP TABLE if exists extents;");
            stat.executeUpdate("CREATE TABLE extents (location INT(5), element_name, id);");
            stat.executeUpdate("DROP TABLE if exists links;");
            stat.executeUpdate("CREATE TABLE links (location INT(5), element_name, id);");
            for (int i=0;i<mMaxArgs;i++) {
                String colname = "arg"+i;
                stat.executeUpdate("ALTER TABLE links ADD '" + colname + "';");
            }
            for (int i=0;i<mMaxArgs;i++) {
                String colname = "arg"+i+"_name";
                stat.executeUpdate("ALTER TABLE links ADD '" + colname + "';");
            }
//            stat.executeUpdate("create table links (id,fromid,from_name,toid,to_name,element_name);");
            mExt2Insert = mConn.prepareStatement("insert into extents values (?, ?, ?);");
            String nullargs = "";
            for (int i=0;i<mMaxArgs;i++) {
                nullargs += ", ?, ?";
            }
            mLink2Insert = mConn.prepareStatement("insert into links values (?, ?, ?" +
                    nullargs + ");");
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void printExtents(){
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

    // TODO need serious revision: right now, this method is not used at anywhere so leave it now
    public void printLinks(){
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
            for (int i=0;i<mMaxArgs;i++) {
                String colName = "arg"+i;
                String id = rs.getString(colName);
                if (rs.wasNull()) {
                    break;
                } else {
                    ids.add(id);
                }
            }
        }
        rs.close();

        Hashtable<Integer,String> locs = new Hashtable<Integer,String>();
        for (String id : ids) {
            query = "select * from extents where id = '" + id + "';";
            rs = stat.executeQuery(query);
            while (rs.next()) {
                locs.put((Integer.parseInt(rs.getString("location"))), "");
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
            for (int i=0;i<mMaxArgs;i++) {
                String colName = "arg"+i;
                String id = rs.getString(colName);
                if (rs.wasNull()) {
                    break;
                } else {
                    ids.add(id);
                }
            }
        }
        rs.close();

        //then, go through and remove all the IDs that are associated with other 
        //actively bolded link tags
        for (String activeElem : active) {
            ArrayList<String> outIDs = new ArrayList<String>();
            query = "select * from links where element_name = '" + activeElem + "';";
            rs = stat.executeQuery(query);
            while (rs.next()){
                for (int i=0;i<mMaxArgs;i++) {
                    String colName = "arg"+i;
                    String id = rs.getString(colName);
                    if (rs.wasNull()) {
                        break;
                    } else {
                        outIDs.add(id);
                    }
                }
            }
            ids.removeAll(outIDs);
        }
        //now that the list is down to only the IDs that will be removed,
        //get their locations
        Hashtable<Integer,String> locs = new Hashtable<Integer,String>();
        for (String id : ids) {
            query = "select * from extents where id = '" + id + "';";
            rs = stat.executeQuery(query);
            while (rs.next()) {
                locs.put((Integer.parseInt(rs.getString("location"))), "");
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
     * @param extType type of tag being searched for
     * @param extID ID of tag being searched for
     * @return HashCollection of tag names and IDs that are 
     * associated with the extent being searched for
     * @throws Exception
     */
    HashCollection<String,String> getLinksByExtentID(String extType, String extID)
            throws Exception{
        HashCollection<String,String>links = new HashCollection<String,String>();
        Statement stat = mConn.createStatement();
        for (int i=0; i<mMaxArgs; i++) {
            String argIDCol = "arg" + i, argNameCol = "arg" + i + "_name";
            String query = (String.format("select id,element_name from links " +
                    "where %s = '%s' and %s  ='%s';"
                    , argIDCol, extID, argNameCol, extType));

            ResultSet rs = stat.executeQuery(query);
            while (rs.next()) {
                links.putEnt(rs.getString("element_name"), rs.getString("id"));
            }
            rs.close();
        }
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
    void addExtent(int location, String element, String id)
            throws Exception{
        mExt2Insert.setInt(LOC_COL, location);
        mExt2Insert.setString(NAME_COL, element);
        mExt2Insert.setString(ID_COL, id);
        mExt2Insert.addBatch();
    }

    /**
     * Adds a set of extents to the DB at once
     * 
     * @throws Exception
     */
    void batchExtents() throws Exception{
        mConn.setAutoCommit(false);
        mExt2Insert.executeBatch();
        mConn.setAutoCommit(true);
    }

    /**
     * Adds a single extent to the DB
     * @param location character offset
     * @param element tag name
     * @param id ID
     * @throws Exception
     */
    void insertExtent(int location, String element, String id)
            throws Exception{
        mExt2Insert.setInt(LOC_COL, location);
        mExt2Insert.setString(NAME_COL, element);
        mExt2Insert.setString(ID_COL, id);
        mExt2Insert.addBatch();
        mConn.setAutoCommit(false);
        mExt2Insert.executeBatch();
        mConn.setAutoCommit(true);
    }

    /**
     * Adds a set of links to the DB at once
     * @throws Exception
     */
    void batchLinks() throws Exception{
        mConn.setAutoCommit(false);
        mLink2Insert.executeBatch();
        mConn.setAutoCommit(true);
    }

    /**
     * Adds a link to the batch
     * @param id ID string for a new link
     * @param name type of link being added
     * @param args list of ids of relevent arguments
     * @param argTypes list if names of relevent arguments (should correspond to args)
     */
    void addLink(String id, String name,
                 ArrayList<String> args, ArrayList<String> argTypes) {
        // first check args and argTypes are matching
        // TODO need these checkings also in main class
        // (maybe checking here is redundant, since we can't give any message to a user
        if (args.size() != argTypes.size()) {
            System.out.println("args and argTypes not matching");
        }
        // or the number of arguments is acceptable
        else if (args.size() > mMaxArgs) {
            System.out.println("too many arguments");
        }
        // if there are two arguments, use legacy from-to structure
        else if (args.size() == 2) {
            try {
                addBinaryLink(id, name,
                        args.get(0), argTypes.get(0),  // fromID, fromName
                        args.get(1), argTypes.get(1)); // toID, toName
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // else, that is, for a link with more than three arguments,
        else {
            try {
                mLink2Insert.setString(ID_COL, id);
                mLink2Insert.setString(NAME_COL, id);
                for (int i=0;i<args.size();i++) {
                    mLink2Insert.setString(ARG0_COL + i, args.get(i));
                    mLink2Insert.setString(ARG0_TYPE_COL + i, argTypes.get(i));
                }
                mLink2Insert.addBatch();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // TODO in Task and Main class, find usages of this method and modify parameter
    void insertLink(String id, String name,
                 ArrayList<String> args, ArrayList<String> argTypes) throws Exception {
        addLink(id, name, args, argTypes);
        batchLinks();
    }

    /**
     * krim - original addLink() method by Amber,
     * now only used when adding a binary link
     *
     * @param newID ID string
     * @param linkName type of link being added
     * @param linkFrom ID of the from anchor
     * @param fromType tag type of the from anchor
     * @param linkTo ID of the to anchor
     * @param toType tag type of the to anchor
     * @throws Exception
     */
    void addBinaryLink(String newID, String linkName, String linkFrom,
                 String fromType, String linkTo, String toType) throws Exception{
        mLink2Insert.setString(ID_COL, newID);
        mLink2Insert.setString(NAME_COL, linkName);
        mLink2Insert.setString(ARG0_COL, linkFrom);
        mLink2Insert.setString(ARG0_TYPE_COL, fromType);
        mLink2Insert.setString(ARG0_COL + 1, linkTo);
        mLink2Insert.setString(ARG0_TYPE_COL + 1, toType);
        mLink2Insert.addBatch();
    }

    /*
     * krim - deprecated old method
     * TODO remove this when everything works
     * @param newID String of the ID being added
     * @param linkName tag type of the link being added
     * @param linkFrom ID of the from anchor
     * @param from_name tag type of the from anchor
     * @param linkTo ID of the to anchor
     * @param to_name tag type of the to anchor
     * @throws Exception
    void insertLink(String newID, String linkName, String linkFrom,
                    String from_name, String linkTo, String to_name) throws Exception{
        mLink2Insert.setString(ID_COL, newID);
        mLink2Insert.setString(NAME_COL, linkName);
        mLink2Insert.setString(ARG0_COL, linkFrom);
        mLink2Insert.setString(ARG0_TYPE_COL, from_name);
        mLink2Insert.setString(ARG0_COL + 1, linkTo);
        mLink2Insert.setString(ARG0_TYPE_COL + 1, to_name);
        mLink2Insert.addBatch();
        mConn.setAutoCommit(false);
        mLink2Insert.executeBatch();
        mConn.setAutoCommit(true);
    }
    */

    /**
     * Closes the connection to the DB
     */
    void closeDb(){
        try{
            mConn.close();
        }catch(Exception e){
            System.out.println(e.toString());
        }
    }


    /**
     * added by krim - set max # arguments
     */
    void setMaxArgs(int i) {
        mMaxArgs = i;
        ARG0_TYPE_COL = ARG0_COL + i;
    }
}

// TODO seems done here for now (1/9)
