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

package edu.brandeis.cs.nlp.mae.model;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import edu.brandeis.cs.nlp.mae.MaeStrings;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by krim on 12/9/2015.
 */
public class ExtentTagTest {

    protected final static String DATABASE_URL = "jdbc:sqlite:" + MaeStrings.TEST_DB_FILE;
    protected ConnectionSource cs;

    Dao<CharIndex, Integer> charIndexDao;
    Dao<ExtentTag, String> eTagDao;
    Dao<TagType, Integer> tagTypeDao;
    Dao<AttributeType, Integer> attTypeDao;
    Dao<Attribute, Integer> attDao;

    Dao<LinkTag, Integer> lTagDao;
    Dao<ArgumentType, Integer> argTypeDao;
    Dao<Argument, Integer> argDao;

    TagType noun;
    TagType verb;

    @Before
    public void setUp() throws Exception {

        cs = new JdbcConnectionSource(DATABASE_URL);
        this.setupDatabase(cs);
    }

    protected void setupDatabase(ConnectionSource source) throws Exception {
        eTagDao = DaoManager.createDao(source, ExtentTag.class);
        tagTypeDao = DaoManager.createDao(source, TagType.class);
        attTypeDao = DaoManager.createDao(source, AttributeType.class);
        attDao = DaoManager.createDao(source, Attribute.class);
        charIndexDao = DaoManager.createDao(source, CharIndex.class);

        lTagDao = DaoManager.createDao(source, LinkTag.class);
        argTypeDao = DaoManager.createDao(source, ArgumentType.class);
        argDao = DaoManager.createDao(source, Argument.class);

        dropAllTables(source);

        TableUtils.createTable(source, CharIndex.class);
        TableUtils.createTable(source, ExtentTag.class);
        TableUtils.createTable(source, TagType.class);
        TableUtils.createTable(source, AttributeType.class);
        TableUtils.createTable(source, Attribute.class);

        TableUtils.createTable(source, LinkTag.class);
        TableUtils.createTable(source, ArgumentType.class);
        TableUtils.createTable(source, Argument.class);

        noun = new TagType("NOUN", "N", false);
        verb = new TagType("VERB", "V", false);
        tagTypeDao.create(noun);
        tagTypeDao.create(verb);

    }

    protected void dropAllTables(ConnectionSource source) throws Exception {
        TableUtils.dropTable(source, CharIndex.class, true);
        TableUtils.dropTable(source, ExtentTag.class, true);
        TableUtils.dropTable(source, TagType.class, true);
        TableUtils.dropTable(source, AttributeType.class, true);
        TableUtils.dropTable(source, Attribute.class, true);

        TableUtils.dropTable(source, LinkTag.class, true);
        TableUtils.dropTable(source, ArgumentType.class, true);
        TableUtils.dropTable(source, Argument.class, true);
    }

    @After
    public void tearDown() throws Exception {
        // destroy the data source which should close underlying connections
        if (cs != null){
            this.dropAllTables(cs);
            cs.close();
        }
    }

    protected ExtentTag createTag(String tid, TagType tagType, String text, int[] spans) throws Exception {
        ExtentTag tag = new ExtentTag(tid, tagType, "filename");
        for (CharIndex ci: tag.setSpans(spans)) { charIndexDao.create(ci); }
        tag.setText(text);
        eTagDao.create(tag);
        return tag;
    }

    @Test
    public void canSaveTag() throws Exception {
        ExtentTag tag = new ExtentTag("N01", noun, "filename");
        tag.setText("John");
        for (CharIndex ci: tag.setSpans(1, 2, 3, 4)) { charIndexDao.create(ci); }
        eTagDao.create(tag);
        assertEquals(
                "Expected 1 tag in DB, found " + eTagDao.countOf(),
                1, eTagDao.countOf()
        );
        ExtentTag retrievedTag = eTagDao.queryForAll().get(0);
        assertEquals(
                "Expected same text after retrieved, found " + retrievedTag.getText(),
                "John", retrievedTag.getText());
        assertEquals(
                "Expected 4 chars allocated, found " + retrievedTag.getSpans().size(),
                4, retrievedTag.getSpans().size()
        );
    }

    @Test
    public void canQueryByTid() throws Exception {
        int[] span = new int[] {0, 4};
        ArrayList<int[]> spans = new ArrayList<>();
        spans.add(span);
        ExtentTag nTag = new ExtentTag("N01", noun, "filename");
        for (CharIndex ci: nTag.setSpans(spans)) { charIndexDao.create(ci); }
        nTag.setText("Crown");
        eTagDao.create(nTag);

        List<ExtentTag> retrievedTags
                = eTagDao.queryForEq(DBSchema.TAB_TAG_COL_TID, "N01");
        assertEquals(
                "Expected 1 tag in DB, found " + retrievedTags.size(),
                1, retrievedTags.size());

        ExtentTag retrievedTag = retrievedTags.get(0);
        assertEquals(
                "Expected tag has text 'Crown', found: " + retrievedTag.getText(),
                "Crown", retrievedTag.getText());
    }

    @Test
    public void canQueryByTagType() throws Exception {

        createTag("N01", noun, "Crown", new int[]{0,1,2,3,4});
        createTag("V01", verb, "own", new int[]{2,3,4});

        assertEquals(
                "Expected 2 tags in DB, found " + eTagDao.countOf(),
                2, eTagDao.countOf());

        ExtentTag retrievedNTag
                = eTagDao.queryBuilder().where().
                eq(DBSchema.TAB_TAG_FCOL_TT, noun.getName()).queryForFirst();
        ExtentTag retrievedVTag
                = eTagDao.queryBuilder().where().
                eq(DBSchema.TAB_TAG_FCOL_TT, verb.getName()).query().get(0);
        assertEquals(
                "Expected 3 chars allocated to vTag set by List, found: " + retrievedVTag.getSpans().size(),
                3, retrievedVTag.getSpansAsArray().length
        );
        assertEquals(
                "Expected same text of nTag after retrieved, found " + retrievedNTag.getText(),
                "Crown", retrievedNTag.getText()
        );
    }

    @Test
    public void canHaveMutableAttribute() throws Exception {

        ExtentTag nTag = createTag("N01", noun, "Crown", new int[]{0,1,2,3,4});

        AttributeType properNoun = new AttributeType(noun, "isProper");
        attTypeDao.create(properNoun);
        Attribute att = new Attribute(nTag, properNoun, Boolean.toString(true));
        attDao.create(att);

        List<Attribute> retrievedAtts
                = attDao.queryForEq(DBSchema.TAB_ATT_FCOL_ETAG, "N01");
        assertEquals(
                "Expected 1 att is assgined, found: " + retrievedAtts.size(),
                1, retrievedAtts.size());
        Attribute retrievedAtt = retrievedAtts.get(0);
        assertEquals(
                "Expected N01 to have att 'isProper', found: " + retrievedAtt.getName(),
                "isProper", retrievedAtt.getName());
        assertEquals(
                "Expected N01 to be proper noun, found: " + retrievedAtt.getValue(),
                Boolean.toString(true), retrievedAtt.getValue());

        retrievedAtt.setValue(Boolean.toString(false));
        attDao.update(retrievedAtt);
        eTagDao.update(nTag);

        List<Attribute> retrievedAttsAfterUpdate
                = attDao.queryForEq(DBSchema.TAB_ATT_FCOL_ETAG, "N01");
        Attribute retrievedAttAfterUpdate = retrievedAttsAfterUpdate.get(0);
        assertEquals(
                "Expected N01 to updated to non proper noun, found: " + retrievedAttAfterUpdate.getValue(),
                Boolean.toString(false), retrievedAttAfterUpdate.getValue());

    }

    @Test
    public void canReturnSpansString() throws Exception {
        createTag("N02", noun,
                "John ... Smith", new int[]{3,4,5,6,10,11,12,13,14});

        ExtentTag retrievedTag = eTagDao.queryForAll().get(0);

        assertEquals(
                "Expected ETag can generate spans string, found: " + retrievedTag.getSpansAsString(),
                "3~7,10~15", retrievedTag.getSpansAsString());
    }

    @Test
    public void canQueryByLocation() throws Exception {
        createTag("N01", noun, "Crown", new int[]{0,1,2,3,4});
        createTag("V01", verb, "own", new int[]{2,3,4});

        List<CharIndex> retrievedIndices
                = charIndexDao.queryForEq(DBSchema.TAB_CI_COL_LOCATION, 3);

        assertEquals(
                "Expected 2 tags at offset 3, found: " + retrievedIndices.size(),
                2, retrievedIndices.size()
        );

        QueryBuilder<CharIndex, Integer> ciQb = charIndexDao.queryBuilder();
        ciQb.where().eq(DBSchema.TAB_CI_COL_LOCATION, 3);
        QueryBuilder<ExtentTag, String> tagQb = eTagDao.queryBuilder();
        List<ExtentTag> retrievedTags = tagQb.join(ciQb).query();

        assertEquals(
                "Expected 2 tags from querying 3, found: " + retrievedTags.size(),
                2, retrievedTags.size()
        );

        ciQb.reset();
        tagQb.reset();
        ciQb.where().eq(DBSchema.TAB_CI_COL_LOCATION, 1);
        retrievedTags = tagQb.join(ciQb).query();

        assertEquals(
                "Expected 1 tags from querying 1, found: " + retrievedTags.size(),
                1, retrievedTags.size()
        );
    }

    @Test
    public void canDeleteTag() throws Exception{

        ExtentTag nTag = createTag("N01", noun, "John", new int[]{0,1,2,3,4});

        AttributeType properNoun = new AttributeType(noun, "isProper");
        attTypeDao.create(properNoun);
        Attribute att = new Attribute(nTag, properNoun, Boolean.toString(true));
        attDao.create(att);

        ExtentTag retrievedTag = eTagDao.queryForAll().get(0);
        assertEquals(
                "Expected 1 att is assigned to N01, found: " + retrievedTag.getAttributes().size(),
                1, retrievedTag.getAttributes().size()
        );


        Attribute retrievedAtt = (new ArrayList<>(retrievedTag.getAttributes())).get(0);
        assertEquals(
                "Expected N01 to have att 'isProper', found: " + retrievedAtt.getName(),
                "isProper", retrievedAtt.getName());

        eTagDao.delete(retrievedTag);
        assertEquals(
                "Expected the tag is gone, found: " + eTagDao.queryForAll().size(),
                0, eTagDao.queryForAll().size()
        );

        List<Attribute> retrievedAtts
                = attDao.queryForEq(DBSchema.TAB_ATT_FCOL_ETAG, "N01");
        assertEquals(
                "Expected att is gone; not retrievable by N01, found: " + retrievedAtts.size() + " attribute",
                0, retrievedAtts.size());
        retrievedAtts
                = attDao.queryForAll();
        assertEquals(
                "Expected att is gone; not retrievable by queryAll, found: " + retrievedAtts.size() + " attribute",
                0, retrievedAtts.size());

    }

}