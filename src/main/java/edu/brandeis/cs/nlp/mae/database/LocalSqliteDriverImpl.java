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
import edu.brandeis.cs.nlp.mae.io.MaeIODTDException;
import edu.brandeis.cs.nlp.mae.io.MaeIOXMLException;
import edu.brandeis.cs.nlp.mae.io.NewDTDLoader;
import edu.brandeis.cs.nlp.mae.io.NewXMLLoader;
import edu.brandeis.cs.nlp.mae.model.*;
import edu.brandeis.cs.nlp.mae.util.HashedSet;
import edu.brandeis.cs.nlp.mae.util.SpanHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.*;

// TODO 151225 split this big chunk of drivers into TagDriver.java, AttDriver.java, etc (names are subject to change)

public class LocalSqliteDriverImpl implements MaeDriverI {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    static final String JDBC_DRIVER = "jdbc:sqlite:";

    private String SQLITE_FILENAME;
    private ConnectionSource cs;
    private IdHandler idHandler;
    // this should be distinguishable over diff tasks and diff versions
    private Task workingTask;

    // TODO 151227 add another table: task with columns: dtd_root, dtd_filename, last_saved_xml_filename, ...
    // this will make task context persistent, then later can be used when recover from unexpected termination

    private Dao<Task, Integer> taskDao;
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

    public LocalSqliteDriverImpl(String sqlite_filename) throws MaeDBException {
        SQLITE_FILENAME = sqlite_filename;
        try {
            cs = new JdbcConnectionSource(JDBC_DRIVER + SQLITE_FILENAME);
            idHandler = new IdHandler();
            this.setupDatabase(cs);
            // put a placeholder for task metadata in DB
            workingTask = new Task(SQLITE_FILENAME);
            taskDao.create(workingTask);
        } catch (SQLException e) {
            throw catchSQLException(e);
        }

    }

    @Override
    public void setupDatabase(ConnectionSource source) throws MaeDBException {

        try {
            taskDao = DaoManager.createDao(source, Task.class);
            charIndexDao = DaoManager.createDao(source, CharIndex.class);
            tagTypeDao = DaoManager.createDao(source, TagType.class);
            eTagDao = DaoManager.createDao(source, ExtentTag.class);
            lTagDao = DaoManager.createDao(source, LinkTag.class);
            attTypeDao = DaoManager.createDao(source, AttributeType.class);
            attDao = DaoManager.createDao(source, Attribute.class);
            argTypeDao = DaoManager.createDao(source, ArgumentType.class);
            argDao = DaoManager.createDao(source, Argument.class);
        } catch (SQLException e) {
            throw catchSQLException(e);
        }

        charIndexQuery = charIndexDao.queryBuilder();
        tagTypeQuery = tagTypeDao.queryBuilder();
        eTagQuery = eTagDao.queryBuilder();
        lTagQuery = lTagDao.queryBuilder();
        attTypeQuery = attTypeDao.queryBuilder();
        attQuery = attDao.queryBuilder();
        argTypeQuery = argTypeDao.queryBuilder();
        argQuery = argDao.queryBuilder();

        allDaos = new Dao[]{ taskDao, charIndexDao, tagTypeDao, eTagDao, lTagDao, attTypeDao, attDao, argTypeDao, argDao};
        allQueryBuilders = new QueryBuilder[]{ charIndexQuery, tagTypeQuery, eTagQuery, lTagQuery, attTypeQuery, attQuery, argTypeQuery, argQuery};

        dropAllTables(source);
        createAllTables(source);

    }

    public void createAllTables(ConnectionSource source) throws MaeDBException {
        for (Dao dao : allDaos) {
            try {
                TableUtils.createTable(source, dao.getDataClass());
            } catch (SQLException e) {
                throw catchSQLException(e);
            }

        }
    }

    public void dropAllTables(ConnectionSource source) throws MaeDBException {
        for (Dao dao : allDaos) {
            try {
                TableUtils.dropTable(source, dao.getDataClass(), true);
            } catch (SQLException e) {
                throw catchSQLException(e);
            }

        }
    }

    @Override
    public void readTask(File file) throws MaeIODTDException, MaeDBException, FileNotFoundException {
        NewDTDLoader dtdl = new NewDTDLoader(this);
        dtdl.read(file);

    }

    @Override
    public void readAnnotation(File file) throws FileNotFoundException, MaeIOXMLException, MaeDBException {
        // TODO 151227 implement XMLLoader class
        NewXMLLoader xmll = new NewXMLLoader(this);
        xmll.read(file);

    }

    private void resetQueryBuilders() {
        for (QueryBuilder qb : allQueryBuilders) {
            qb.reset();
        }
    }

    @Override
    public String getAnnotationFileName() throws MaeDBException {
        return workingTask.getAnnotationFileName();
    }

    @Override
    public void setAnnotationFileName(String fileName) throws MaeDBException {
        try {
            this.workingTask.setAnnotationFileName(fileName);
            taskDao.update(workingTask);
        } catch (SQLException e) {
            throw catchSQLException(e);
        }
    }

    @Override
    public String getPrimaryText() throws MaeDBException {
        return workingTask.getPrimaryText();
    }

    @Override
    public void setPrimaryText(String text) throws MaeDBException {
        try {
            this.workingTask.setPrimaryText(text);
            taskDao.update(workingTask);
        } catch (SQLException e) {
            throw catchSQLException(e);
        }
    }

    @Override
    public String getTaskName() {
        return workingTask.getName();
    }

    @Override
    public void setTaskName(String name) throws MaeDBException {
        try {
            // need to clear task table before updating id column of it
            TableUtils.clearTable(cs, taskDao.getDataClass());
            workingTask.setName(name);
            taskDao.create(workingTask);
        } catch (SQLException e) {
            throw catchSQLException(e);
        }
    }

    @Override
    public String getTaskFileName() throws MaeDBException {
        return workingTask.getTaskFileName();
    }

    @Override
    public void setTaskFileName(String fileName) throws MaeDBException {
        try {
            this.workingTask.setTaskFileName(fileName);
            taskDao.update(workingTask);
        } catch (SQLException e) {
            throw catchSQLException(e);
        }
    }

    public boolean isDtdLoaded() {
        return workingTask.isDtdLoaded();
    }

    public boolean isAnnotationLoaded() {
        return workingTask.isAnnotationLoaded();
    }

    public boolean isPrimaryTextLoaded() {
        return workingTask.isPrimaryTextLoaded();
    }

    public List<ExtentTag> getTagsAt(int loc) throws MaeDBException {

        try {
            List<ExtentTag> results = null;
            charIndexQuery.where().eq(DBSchema.TAB_CI_COL_LOCATION, loc);
            results = eTagQuery.join(charIndexQuery).query();
            resetQueryBuilders();
            return results;
        } catch (SQLException e) {
            throw catchSQLException(e);
        }
    }

    public List<String> getTagIdsAt(int loc) throws MaeDBException {
        List<String> tids = new ArrayList<>();
        for (ExtentTag tag : getTagsAt(loc)) {
            tids.add(tag.getId());
        }
        return tids;
    }

    public HashedSet<CharIndex, ExtentTag> getAllLocationsWithTags() throws MaeDBException{
        // TODO 151214 when making hghlights, implement getProperColor()
        // to get the first turned-on TagType from a sorted List<TagType>, and also check that's the last (to make it bold)

        HashedSet<CharIndex, ExtentTag> locationsWithTags = new HashedSet<>();

        try {
            for (CharIndex location : charIndexDao.queryForAll()) {
                locationsWithTags.putItem(location, location.getTag());
            }
            return locationsWithTags;
        } catch (SQLException e) {
            throw catchSQLException(e);
        }

    }

    public List<Integer> getAllLocationsOfTagType(TagType type) throws MaeDBException{

        try {
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
        } catch (SQLException e) {
            throw catchSQLException(e);
        }

    }

    public List<Integer> getAllLocationsOfTagType(TagType type, List<TagType> exculdes) throws MaeDBException{
        List<Integer> targetSpans = getAllLocationsOfTagType(type);
        for (TagType exclude : exculdes) {
            targetSpans.removeAll(getAllLocationsOfTagType(exclude));
        }
        return targetSpans;

    }

    public List<ExtentTag> getArgumentTags(LinkTag linker) {
        return linker.getArgumentTags();
    }

    public int[] getSpansByTid(String tid) throws MaeDBException{
        try {
            return eTagDao.queryForId(tid).getSpansAsArray();
        } catch (SQLException e) {
            throw catchSQLException(e);
        }
    }

    @Override
    public Tag getTagByTid(String tid) throws MaeDBException {
        try {
            if (eTagDao.queryForId(tid) != null) {
                return eTagDao.queryForId(tid);
            } else {
                return lTagDao.queryForId(tid);
            }
        } catch (SQLException e) {
            throw catchSQLException(e);
        }
    }

    public TagType getTagTypeByTid(String tid) throws MaeDBException {
        return getTagByTid(tid).getTagtype();

    }

    public void removeTag(Tag tag) throws Exception {
        if (tag instanceof ExtentTag) {
            eTagDao.delete((ExtentTag) tag);
        } else {
            lTagDao.delete((LinkTag) tag);
        }
    }

    public HashedSet<TagType, LinkTag> getLinksHasArgumentTag(ExtentTag argument) throws MaeDBException{
        try {
            HashedSet<TagType, LinkTag> links = new HashedSet<>();
            List<Argument> results = null;
            results = argQuery.where().eq(DBSchema.TAB_ARG_FCOL_ETAG, argument).query();
            for (Argument result : results) {
                LinkTag linker = result.getLinker();
                links.putItem(linker.getTagtype(), linker);
            }
            resetQueryBuilders();
            return links;
        } catch (SQLException e) {
            throw catchSQLException(e);
        }
    }

    public HashedSet<TagType, ExtentTag> getAllExtentTagsByTypes(boolean consumingOnly) throws MaeDBException {
        HashedSet<TagType, ExtentTag> tagsByTypes = new HashedSet<>();
        for (TagType type : getAllTagTypes()) {
            if (type.isExtent()) {
                tagsByTypes.putCollection(type, type.getExtentTagsAsList(consumingOnly));
            }
        }
        return tagsByTypes;

    }

    @Override
    public HashedSet<TagType,ExtentTag> getTagsByTypesAt(int location) throws MaeDBException{
        HashedSet<TagType, ExtentTag> tags = new HashedSet<>();
        for (ExtentTag tag : getTagsAt(location)) {
            tags.putItem(tag.getTagtype(), tag);
        }
        return tags;
    }

    @Override
    public HashedSet<TagType,ExtentTag> getTagsByTypesIn(int... locations) throws MaeDBException{
        HashedSet<TagType, ExtentTag> tags = new HashedSet<>();
        for (int location : locations) {
            tags.merge(getTagsByTypesAt(location));
        }
        return tags;
    }

    public HashedSet<TagType,ExtentTag> getTagsByTypesIn(String spansString) throws MaeDBException {
        return getTagsByTypesIn(SpanHandler.convertStringToPairs(spansString));
    }

    public HashedSet<TagType,ExtentTag> getTagsByTypesIn(ArrayList<int[]> spansPairs) throws MaeDBException {
        HashedSet<TagType, ExtentTag> tags = new HashedSet<>();
        for (int[] pair : spansPairs) {
            tags.merge(getTagsByTypesBetween(pair[0], pair[1]));
        }
        return tags;
    }

    @Override
    public HashedSet<TagType,ExtentTag> getTagsByTypesBetween(int begin, int end) throws MaeDBException{
        HashedSet<TagType, ExtentTag> tags = new HashedSet<>();
        for (int i=begin; i<end; i++) {
            tags.merge(getTagsByTypesAt(i));
        }
        return tags;
    }

    public List<? extends Tag> getAllTagsOfType(TagType type) throws MaeDBException {
        // TODO 151215 split into two methods if necessary (each for link and etag)
        try {
            tagTypeDao.refresh(type);
            return new ArrayList<>(type.getTags());
        } catch (SQLException e) {
            throw catchSQLException(e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ExtentTag> getAllExtentTagsOfType(TagType type) throws MaeDBException, IllegalArgumentException {
        return (List<ExtentTag>) getAllTagsOfType(type);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<LinkTag> getAllLinkTagsOfType(TagType type) throws MaeDBException, IllegalArgumentException {
        return (List<LinkTag>) getAllTagsOfType(type);

    }

    @Override
    public TagType createTagType(String typeName, String prefix, boolean isLink) throws MaeDBException {
        try {
            TagType type  = new TagType(typeName, prefix, isLink);
            tagTypeDao.create(type);
            return type;
        } catch (SQLException e) {
            throw catchSQLException(e);
        }
    }

    @Override
    public AttributeType createAttributeType(TagType tagType, String attName) throws  MaeDBException {
        try {
            AttributeType attType = new AttributeType(tagType, attName);
            attTypeDao.create(attType);
            return attType;
        } catch (SQLException e) {
            throw catchSQLException(e);
        }
    }

    @Override
    public List<AttributeType> getAttributeTypesOfTagType(TagType type) throws MaeDBException {
        try {
            return new ArrayList<>(attTypeDao.queryForEq(DBSchema.TAB_AT_FCOL_TT, type));
        } catch (SQLException e) {
            throw catchSQLException(e);
        }
    }

    @Override
    public Map<String, String> getAttributeMapOfTag(Tag tag) throws MaeDBException {
        return tag.getAttbutesWithNames();

    }

    @Override
    public ArgumentType createArgumentType(TagType tagType, String argName) throws MaeDBException {
        try {
            ArgumentType argType = new ArgumentType(tagType, argName);
            argTypeDao.create(argType);
            return argType;
        } catch (SQLException e) {
            throw catchSQLException(e);
        }
    }

    @Override
    public ExtentTag createExtentTag(String tid, TagType tagType, String text, int... spans) throws MaeDBException {
        try {
            ExtentTag tag = new ExtentTag(tid, tagType, workingTask.getAnnotationFileName());
            tag.setText(text);
            for (CharIndex ci: tag.setSpans(spans)) {
                charIndexDao.create(ci);
            }
            eTagDao.create(tag);
            boolean added = idHandler.addId(tagType, tid);
            if (!added) {
                throw new MaeDBException("tag id is already in DB!: " + tid);
            }
            return tag;
        } catch (SQLException e) {
            throw catchSQLException(e);
        }
    }

    public ExtentTag createExtentTag(String tid, TagType tagType, String text, ArrayList<int[]> spansList) throws MaeDBException {
        return createExtentTag(tid, tagType, text, SpanHandler.convertPairsToArray(spansList));
    }

    public ExtentTag createExtentTag(String tid, TagType tagType, String text, String spansString) throws MaeDBException {
        return createExtentTag(tid, tagType, text, SpanHandler.convertStringToPairs(spansString));
    }

    @Override
    public ExtentTag createExtentTag(TagType tagType, String text, int...spans) throws MaeDBException {
        String tid = idHandler.getNextID(tagType);
        return createExtentTag(tid, tagType, text, spans);
    }

    public ExtentTag createExtentTag(TagType tagType, String text, ArrayList<int[]> spansList) throws MaeDBException {
        String tid = idHandler.getNextID(tagType);
        return createExtentTag(tid, tagType, text, spansList);
    }

    public ExtentTag createExtentTag(TagType tagType, String text, String spansString) throws MaeDBException {
        String tid = idHandler.getNextID(tagType);
        return createExtentTag(tid, tagType, text, spansString);
    }

    @Override
    public LinkTag createLinkTag(String tid, TagType tagType) throws MaeDBException {
        try {
            LinkTag link = new LinkTag(tid, tagType, workingTask.getAnnotationFileName());
            lTagDao.create(link);
            return link;
        } catch (SQLException e) {
            throw catchSQLException(e);
        }
    }

    @Override
    public LinkTag createLinkTag(TagType tagtype) throws MaeDBException {
        return createLinkTag(idHandler.getNextID(tagtype), tagtype);
    }

    public LinkTag createLinkTag(String tid, TagType tagType, HashMap<ArgumentType, ExtentTag> arguments) throws MaeDBException {
        LinkTag link = new LinkTag(tid, tagType, workingTask.getAnnotationFileName());
        for (ArgumentType argType : arguments.keySet()) {
            addArgument(link, argType, arguments.get(argType));
        }
        try {
            lTagDao.create(link);
            return link;
        } catch (SQLException e) {
            throw catchSQLException(e);
        }
    }

    @Override
    public Attribute addAttribute(Tag tag, AttributeType attType, String attValue) throws MaeDBException {
        try {
            Attribute att = new Attribute(tag, attType, attValue);
            attDao.create(att);
            return att;
        } catch (SQLException e) {
            throw catchSQLException(e);
        } catch (MaeModelException e) {
            throw new MaeDBException("failed to add an attribute: " + e.getMessage(), e);
        }

    }

    @Override
    public Argument addArgument(LinkTag linker, ArgumentType argType, ExtentTag argument) throws MaeDBException {
        try {
            Argument arg = new Argument(linker, argType, argument);
            argDao.create(arg);
            return arg;
        } catch (SQLException e) {
            throw catchSQLException(e);
        }
    }

    /**
     * Shut down data source connection and delete all table from DB.
     */
    @Override
    public void destroy() throws MaeDBException {
        if (cs != null){
            dropAllTables(cs);
            try {
                cs.close();
            } catch (SQLException e) {
                throw catchSQLException(e);
            }
            logger.info("closing JDBC datasource and deleting DB file: " + SQLITE_FILENAME);
            File dbFile = new File(SQLITE_FILENAME);
            if (dbFile.delete()) {
                logger.info("driver is completely destroyed");
            } else {
                logger.error("DB file is not deleted: " + SQLITE_FILENAME);

            }
        }
    }

    public List<TagType> getTagTypes(boolean includeExtent, boolean includeLink) throws MaeDBException {
        try {
            ArrayList<TagType> types = new ArrayList<>();
            for (TagType type : tagTypeDao.queryForAll()) {
                if (type.isLink() && includeLink) {
                    types.add(type);
                } else if (type.isExtent() && includeExtent) {
                    types.add(type);
                }
            }
            return types;
        } catch (SQLException e) {
            throw catchSQLException(e);
        }
    }

    @Override
    public List<TagType> getAllTagTypes() throws MaeDBException {
        return getTagTypes(true, true);
    }

    @Override
    public List<TagType> getExtentTagTypes() throws MaeDBException {
        return getTagTypes(true, false);
    }

    @Override
    public List<TagType> getLinkTagTypes() throws MaeDBException {
        return getTagTypes(false, true);
    }

    public List<TagType> getNonConsumingTagTypes() throws MaeDBException {
        try {
            ArrayList<TagType> types = new ArrayList<>();
            for (TagType type : tagTypeDao.queryForAll()) {
                if (type.isNonConsuming()) {
                    types.add(type);
                }
            }
            return types;
        } catch (SQLException e) {
            throw catchSQLException(e);
        }
    }

    public boolean idExists(String tid) throws MaeDBException {
        try {
            return (eTagDao.queryForId(tid) != null || lTagDao.queryForId(tid) != null);
        } catch (SQLException e) {
            throw catchSQLException(e);
        }

    }

    @Override
    public TagType getTagTypeByName(String typeName) throws MaeDBException {
        try {
            return tagTypeDao.queryForEq(DBSchema.TAB_TT_COL_NAME, typeName).get(0);
        } catch (SQLException e) {
            throw catchSQLException(e);
        }
    }

    // TODO 151216 do we need this?
//    public boolean hasDTD() {
//        return hasDTD;
//    }

    // TODO 151216 how to keep DTD name in the future?
//    public String getDTDName() {
//        return mDtd.getName();
//    }

    @Override
    public List<ArgumentType> getArgumentTypesOfLinkTagType(TagType link) throws MaeDBException {
        try {
            return new ArrayList<>(argTypeDao.queryForEq(DBSchema.TAB_ART_FCOL_TT, link));
        } catch (SQLException e) {
            throw catchSQLException(e);
        }
    }

    @Override
    public boolean setTagTypePrefix(TagType tagType, String prefix) throws MaeDBException {
        try {
            tagType.setPrefix(prefix);
            return tagTypeDao.update(tagType) == 1;
        } catch (SQLException e) {
            throw catchSQLException(e);
        }
    }

    @Override
    public boolean setTagTypeNonConsuming(TagType tagType, boolean b) throws MaeDBException {
        tagType.setNonConsuming(b);
        try {
            return tagTypeDao.update(tagType) == 1;
        } catch (SQLException e) {
            throw catchSQLException(e);
        }
    }

    @Override
    public void setAttributeTypeValueSet(AttributeType attType, List<String> validValues) throws MaeDBException {
        attType.setValuesetFromList(validValues);
        try {
            attTypeDao.update(attType);
        } catch (SQLException e) {
            throw catchSQLException(e);
        }
    }

    public void setAttributeTypeValueSet(AttributeType attType, String...validValues) throws MaeDBException {
        setAttributeTypeValueSet(attType, Arrays.asList(validValues));
    }

    @Override
    public void setAttributeTypeDefaultValue(AttributeType attType, String defaultValue) throws MaeDBException {
        try {
            attType.setDefaultValue(defaultValue);
            attTypeDao.update(attType);
        } catch (SQLException e) {
            throw catchSQLException(e);
        }
    }

    @Override
    public void setAttributeTypeIDRef(AttributeType attType, boolean b) throws MaeDBException {
        try {
            attType.setIdRef(b);
            attTypeDao.update(attType);
        } catch (SQLException e) {
            throw catchSQLException(e);
        }
    }

    @Override
    public void setAttributeTypeRequired(AttributeType attType) throws MaeDBException {
        try {
            attType.setRequired(true);
            attTypeDao.update(attType);
        } catch (SQLException e) {
            throw catchSQLException(e);
        }

    }

    @Override
    public void setArgumentTypeRequired(ArgumentType argType) throws MaeDBException {
        try {
            argType.setRequired(true);
            argTypeDao.update(argType);
        } catch (SQLException e) {
            throw catchSQLException(e);
        }
    }

    private MaeDBException catchSQLException(SQLException e) {
        String message = "caught sql error: " + e.getMessage();
        logger.error(message);
        return new MaeDBException(message, e);
    }

}

