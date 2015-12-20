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


import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import edu.brandeis.cs.nlp.mae.model.*;
import edu.brandeis.cs.nlp.mae.util.HashedSet;
import edu.brandeis.cs.nlp.mae.util.SpanHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;

class DatabaseDriver {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    private String DATABASE_URL;
    private ConnectionSource cs;
    private String workingFileName;
    private IdHandler idHandler;

    private Dao<CharIndex, Integer> charIndexDao;
    private Dao<TagType, Integer> tagTypeDao;
    private Dao<ExtentTag, String> eTagDao;
    private Dao<LinkTag, String> lTagDao;
    private Dao<AttributeType, Integer> attTypeDao;
    private Dao<Attribute, Integer> attDao;
    private Dao<ArgumentType, Integer> argTypeDao;
    private Dao<Argument, Integer> argDao;

    private QueryBuilder<CharIndex, Integer> charIndexQuery;
    private QueryBuilder<TagType, Integer> tagTypeQuery;
    private QueryBuilder<ExtentTag, String> eTagQuery;
    private QueryBuilder<LinkTag, String> lTagQuery;
    private QueryBuilder<AttributeType, Integer> attTypeQuery;
    private QueryBuilder<Attribute, Integer> attQuery;
    private QueryBuilder<ArgumentType, Integer> argTypeQuery;
    private QueryBuilder<Argument, Integer> argQuery;

    private Dao[] allDaos;
    private QueryBuilder[] allQueryBuilders;

    public DatabaseDriver(String databaseUrl) throws SQLException {
        DATABASE_URL = databaseUrl;
        cs = new JdbcConnectionSource(DATABASE_URL);
        idHandler = new IdHandler();
        this.setupDatabase(cs);

    }

    private void setupDatabase(ConnectionSource source) throws SQLException {

        charIndexDao = DaoManager.createDao(source, CharIndex.class);
        tagTypeDao = DaoManager.createDao(source, TagType.class);
        eTagDao = DaoManager.createDao(source, ExtentTag.class);
        lTagDao = DaoManager.createDao(source, LinkTag.class);
        attTypeDao = DaoManager.createDao(source, AttributeType.class);
        attDao = DaoManager.createDao(source, Attribute.class);
        argTypeDao = DaoManager.createDao(source, ArgumentType.class);
        argDao = DaoManager.createDao(source, Argument.class);

        charIndexQuery = charIndexDao.queryBuilder();
        tagTypeQuery = tagTypeDao.queryBuilder();
        eTagQuery = eTagDao.queryBuilder();
        lTagQuery = lTagDao.queryBuilder();
        attTypeQuery = attTypeDao.queryBuilder();
        attQuery = attDao.queryBuilder();
        argTypeQuery = argTypeDao.queryBuilder();
        argQuery = argDao.queryBuilder();

        allDaos = new Dao[]{ charIndexDao, tagTypeDao, eTagDao, lTagDao, attTypeDao, attDao, argTypeDao, argDao};

        allQueryBuilders = new QueryBuilder[]{ charIndexQuery, tagTypeQuery, eTagQuery, lTagQuery, attTypeQuery, attQuery, argTypeQuery, argQuery};

        dropAllTables(source);
        createAllTables(source);

    }

    private void createAllTables(ConnectionSource source) throws SQLException {
        for (Dao dao : allDaos) {
            TableUtils.createTable(source, dao.getDataClass());

        }
    }

    protected void dropAllTables(ConnectionSource source) throws SQLException {
        for (Dao dao : allDaos) {
            TableUtils.dropTable(source, dao.getDataClass(), true);

        }
    }

    public void setUpDtd(String filename) {
        // TODO 151219 write this to init DTDLoader inside with (this) param,
        // add specifications from DTDLoader to DB
    }

    public void readAnnotation(String filename) {
        // TODO 151219 write this to init XMLLoader inside with (this) param,
        // add all existing annotation to DB
    }

    private void resetQueryBuilders() {
        for (QueryBuilder qb : allQueryBuilders) {
            qb.reset();
        }
    }

    public String getWorkingFileName() {
        return workingFileName;
    }

    public void setWorkingFileName(String fileName) {
        this.workingFileName = fileName;
    }

    List<ExtentTag> getTagsAt(int loc) throws SQLException {

        List<ExtentTag> results = null;
        charIndexQuery.where().eq(DBSchema.TAB_CI_COL_LOCATION, loc);
        results = eTagQuery.join(charIndexQuery).query();
        resetQueryBuilders();
        return results;
    }

    List<String> getTagIdsAt(int loc) throws SQLException {
        List<String> tids = new ArrayList<>();
        for (ExtentTag tag : getTagsAt(loc)) {
            tids.add(tag.getId());
        }
        return tids;
    }

    public HashedSet<CharIndex, ExtentTag> getAllLocationsWithTags() throws SQLException{
        // TODO 151214 when making hghlights, implement getProperColor()
        // to get the first turned-on TagType from a sorted List<TagType>, and also check that's the last (to make it bold)

        HashedSet<CharIndex, ExtentTag> locationsWithTags = new HashedSet<>();

        for (CharIndex location : charIndexDao.queryForAll()) {
            locationsWithTags.putItem(location, location.getTag());
        }

        return locationsWithTags;

    }

    public List<Integer> getAllLocationsOfTagType(TagType type) throws SQLException{

        List<CharIndex> locations;

        if (type.isExtent()) {
            eTagQuery.where().eq(DBSchema.TAB_TAG_FCOL_TT, type);
            locations = charIndexQuery.join(eTagQuery).query();

        } else {
            lTagQuery.where().eq(DBSchema.TAB_TAG_FCOL_TT, type);
            argQuery.join(lTagQuery).selectColumns(DBSchema.TAB_ARG_FCOL_ETAG).distinct();
            eTagQuery.join(argQuery);
            locations = charIndexQuery.join(eTagQuery).query();

        }

        ArrayList<Integer> locationList = new ArrayList<>();
        for (CharIndex ci : locations) {
            locationList.add(ci.getLocation());
        }
        resetQueryBuilders();
        return locationList;

    }

    public List<Integer> getAllLocationsOfTagType(TagType type, List<TagType> exculdes) throws SQLException{
        List<Integer> targetSpans = getAllLocationsOfTagType(type);
        for (TagType exclude : exculdes) {
            targetSpans.removeAll(getAllLocationsOfTagType(exclude));
        }
        return targetSpans;

    }

    public List<ExtentTag> getArgumentTags(LinkTag linker) {
        return linker.getArgumentTags();
    }

    public int[] getSpansByTid(String tid) throws SQLException{
        return eTagDao.queryForId(tid).getSpansAsArray();
    }

    public TagType getTagTypeByTid(String tid) throws SQLException {
        if (eTagDao.queryForId(tid) != null) {
            return eTagDao.queryForId(tid).getTagtype();
        } else {
            return lTagDao.queryForId(tid).getTagtype();
        }
    }

    public void removeTag(Tag tag) throws Exception {
        if (tag instanceof ExtentTag) {
            eTagDao.delete((ExtentTag) tag);
        } else {
            lTagDao.delete((LinkTag) tag);
        }
    }

    HashedSet<TagType, LinkTag> getLinksHasArgumentOf(ExtentTag argument) throws SQLException{
        HashedSet<TagType, LinkTag> links = new HashedSet<>();
        List<Argument> results = argQuery.where().eq(DBSchema.TAB_ARG_FCOL_ETAG, argument).query();
        for (Argument result : results) {
            LinkTag linker = result.getLinker();
            links.putItem(linker.getTagtype(), linker);
        }
        resetQueryBuilders();
        return links;
    }

    public HashedSet<TagType, ExtentTag> getAllExtentTagsByTypes(boolean consumingOnly) throws SQLException {
        HashedSet<TagType, ExtentTag> tagsByTypes = new HashedSet<>();
        for (TagType type : tagTypeDao.queryForAll()) {
            if (type.isExtent()) {
                tagsByTypes.putCollection(type, type.getExtentTagsAsList(consumingOnly));
            }
        }
        return tagsByTypes;

    }

    HashedSet<TagType,ExtentTag> getTagsByTypesAt(int location) throws SQLException{
        HashedSet<TagType, ExtentTag> tags = new HashedSet<>();
        for (ExtentTag tag : getTagsAt(location)) {
            tags.putItem(tag.getTagtype(), tag);
        }
        return tags;
    }

    HashedSet<TagType,ExtentTag> getTagsByTypesIn(int...locations) throws SQLException{
        HashedSet<TagType, ExtentTag> tags = new HashedSet<>();
        for (int location : locations) {
            tags.merge(getTagsByTypesAt(location));
        }
        return tags;
    }

    HashedSet<TagType,ExtentTag> getTagsByTypesIn(String spansString) throws SQLException {
        return getTagsByTypesIn(SpanHandler.convertStringToPairs(spansString));
    }

    HashedSet<TagType,ExtentTag> getTagsByTypesIn(ArrayList<int[]> spansPairs) throws SQLException {
        HashedSet<TagType, ExtentTag> tags = new HashedSet<>();
        for (int[] pair : spansPairs) {
            tags.merge(getTagsByTypesBetween(pair[0], pair[1]));
        }
        return tags;
    }

    HashedSet<TagType,ExtentTag> getTagsByTypesBetween(int begin, int end) throws SQLException{
        HashedSet<TagType, ExtentTag> tags = new HashedSet<>();
        for (int i=begin; i<end; i++) {
            tags.merge(getTagsByTypesAt(i));
        }
        return tags;
    }

    List<? extends Tag> getAllTagsOfType(TagType type) throws SQLException {
        // TODO 151215 need thorough test, split into two methods if necessary (each for link and etag)
        tagTypeDao.refresh(type);
        return new ArrayList<>(type.getTags());
    }

    @SuppressWarnings("unchecked")
    List<ExtentTag> getAllExtentTagsOfType(TagType type) throws SQLException, IllegalArgumentException {
        return (List<ExtentTag>) getAllTagsOfType(type);
    }

    @SuppressWarnings("unchecked")
    List<LinkTag> getAllLinkTagsOfType(TagType type) throws SQLException, IllegalArgumentException {
        return (List<LinkTag>) getAllTagsOfType(type);

    }

    public TagType createTagType(String typeName, String prefix) throws SQLException {
        TagType type  = new TagType(typeName, prefix);
        tagTypeDao.create(type);
        return type;
    }

    public ArgumentType createArgumentType(TagType tagType, String argName) throws SQLException {
        ArgumentType argType = new ArgumentType(tagType, argName);
        argTypeDao.create(argType);
        return argType;
    }

    public ExtentTag createExtentTag(String tid, TagType tagType, String text, int...spans) throws SQLException {
        ExtentTag tag = new ExtentTag(tid, tagType, workingFileName);
        tag.setText(text);
        for (CharIndex ci: tag.setSpans(spans)) { charIndexDao.create(ci); }
        eTagDao.create(tag);
        idHandler.addId(tagType, tid);
        return tag;
    }

    public ExtentTag createExtentTag(String tid, TagType tagType, String text, ArrayList<int[]> spans) throws SQLException {
        return createExtentTag(tid, tagType, text, SpanHandler.convertPairsToArray(spans));
    }

    public ExtentTag createExtentTag(String tid, TagType tagType, String text, String spansString) throws SQLException {
        return createExtentTag(tid, tagType, text, SpanHandler.convertStringToPairs(spansString));
    }

    // no text
    public ExtentTag createExtentTag(String tid, TagType tagType, int...spans) throws SQLException {
        // TODO 151215 add getTextFromSpans(), or so in somewhere
//        return createExtentTag(tid, tagType, getTextFromSpans(spans), ModelHelpers.convertArrayToPairs(spans));
        return null;
    }

    public ExtentTag createExtentTag(String tid, TagType tagType, ArrayList<int[]> spans) throws SQLException {
//        return createExtentTag(tid, tagType, getTextFromSpans(spans), spans);
        return null;
    }

    public ExtentTag createExtentTag(String tid, TagType tagType, String spansString) throws SQLException {
//        return createExtentTag(tid, tagType, getTextFromSpans(spans), ModelHelpers.convertStringToPairs(spansString));
        return null;
    }

    //no tid, but text
    public ExtentTag createExtentTag(TagType tagType, String text, int...spans) throws SQLException {
//        return createExtentTag(tid, tagType, getTextFromSpans(spans), ModelHelpers.convertArrayToPairs(spans));
        return null;
    }

    public ExtentTag createExtentTag(TagType tagType, String text, ArrayList<int[]> spans) throws SQLException {
//        return createExtentTag(tid, tagType, getTextFromSpans(spans), spans);
        return null;
    }

    public ExtentTag createExtentTag(TagType tagType, String text, String spansString) throws SQLException {
//        return createExtentTag(tid, tagType, getTextFromSpans(spans), ModelHelpers.convertStringToPairs(spansString));
        return null;
    }

    // no text, no tid
    public ExtentTag createExtentTag(TagType tagType, int...spans) throws SQLException {
//        return createExtentTag(tid, tagType, getTextFromSpans(spans), ModelHelpers.convertArrayToPairs(spans));
        return null;
    }

    public ExtentTag createExtentTag(TagType tagType, ArrayList<int[]> spans) throws SQLException {
//        return createExtentTag(tid, tagType, getTextFromSpans(spans), spans);
        return null;
    }

    public ExtentTag createExtentTag(TagType tagType, String spansString) throws SQLException {
//        return createExtentTag(tid, tagType, getTextFromSpans(spans), ModelHelpers.convertStringToPairs(spansString));
        return null;
    }

    public LinkTag createLinkTag(String tid, TagType tagType) throws SQLException {
        LinkTag link = new LinkTag(tid, tagType, workingFileName);
        lTagDao.create(link);
        return link;
    }

    public LinkTag createLinkTag(String tid, TagType tagType, HashMap<ArgumentType, ExtentTag> arguments) throws SQLException {
        LinkTag link = new LinkTag(tid, tagType, workingFileName);
        for (ArgumentType argType : arguments.keySet()) {
            addArgument(link, argType, arguments.get(argType));
        }
        lTagDao.create(link);
        return link;
    }

    public void addAttribute(Tag tag, AttributeType attType, String attValue) throws SQLException, ModelException {
        attDao.create(new Attribute(tag, attType, attValue));

    }

    public void addArgument(LinkTag linker, ArgumentType argType, ExtentTag argument) throws SQLException {
        argDao.create(new Argument(linker, argType, argument));

    }

    /**
     * Shut down data source connection and delete all table from DB.
     */
    public void destroy() throws SQLException {
        if (cs != null){
            dropAllTables(cs);
            cs.close();
        }
    }

    public List<TagType> getTagTypes(boolean includeExtent, boolean includeLink) throws SQLException {
        ArrayList<TagType> types = new ArrayList<>();
        for (TagType type : tagTypeDao.queryForAll()) {
            if (type.isLink() && includeLink) {
                types.add(type);
            } else if (type.isExtent() && includeExtent) {
                types.add(type);
            }
        }
        return types;
    }

    public List<TagType> getAllTagTypes() throws SQLException {
        return getTagTypes(true, true);
    }

    public List<TagType> getExtentTagTypes() throws SQLException {
        return getTagTypes(true, false);
    }

    public List<TagType> getLinkTagTypes() throws SQLException {
        return getTagTypes(false, true);
    }

    public List<TagType> getNonConsumingTagTypes() throws SQLException {
        ArrayList<TagType> types = new ArrayList<>();
        for (TagType type : tagTypeDao.queryForAll()) {
            if (type.isNonConsuming()) {
                types.add(type);
            }
        }
        return types;
    }

    public boolean idExists(String tid) throws SQLException {
        return (eTagDao.queryForId(tid) != null || lTagDao.queryForId(tid) != null);

    }

    public TagType getTagTypeByName(String typeName) throws SQLException {
        return tagTypeDao.queryForEq(DBSchema.TAB_TT_COL_NAME, typeName).get(0);
    }

    // TODO 151216 do we need this?
//    public boolean hasDTD() {
//        return hasDTD;
//    }

    // TODO 151216 how to keep DTD name in the future?
//    public String getDTDName() {
//        return mDtd.getName();
//    }

    public List<ArgumentType> getArgumentTypesOfLinkTagType(TagType link) throws SQLException {
        return new ArrayList<>(argTypeDao.queryForEq(DBSchema.TAB_ART_FCOL_TT, link));
   }
}

