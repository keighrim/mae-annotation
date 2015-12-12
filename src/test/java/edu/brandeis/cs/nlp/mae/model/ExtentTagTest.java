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
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import edu.brandeis.cs.nlp.mae.MaeStrings;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
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

    protected static final Logger logger = LoggerFactory.getLogger(ExtentTagTest.class.getName());

    private final static String DATABASE_URL = MaeStrings.TEST_DB_URL;
    private ConnectionSource cs;

    Dao<CharIndex, Integer> charIndexDao;
    Dao<ExtentTag, Integer> eTagDao;
    Dao<TagType, Integer> tagTypeDao;
    Dao<AttributeType, Integer> attTypeDao;
    Dao<Attribute, Integer> attDao;

    TagType noun;
    TagType verb;

    @Before
    public void setUp() throws Exception {

        cs = new JdbcConnectionSource(DATABASE_URL);
        this.setupDatabase(cs);
    }

    private void setupDatabase(ConnectionSource source) throws SQLException {
        eTagDao = DaoManager.createDao(source, ExtentTag.class);
        tagTypeDao = DaoManager.createDao(source, TagType.class);
        attTypeDao = DaoManager.createDao(source, AttributeType.class);
        attDao = DaoManager.createDao(source, Attribute.class);
        charIndexDao = DaoManager.createDao(source, CharIndex.class);

        dropAllTables(source);

        TableUtils.createTable(source, CharIndex.class);
        TableUtils.createTable(source, ExtentTag.class);
        TableUtils.createTable(source, TagType.class);
        TableUtils.createTable(source, AttributeType.class);
        TableUtils.createTable(source, Attribute.class);

        noun = new TagType("NOUN", "N");
        verb = new TagType("VERB", "V");
        tagTypeDao.create(noun);
        tagTypeDao.create(verb);

    }

    private void dropAllTables(ConnectionSource source) throws SQLException {
        TableUtils.dropTable(source, CharIndex.class, true);
        TableUtils.dropTable(source, ExtentTag.class, true);
        TableUtils.dropTable(source, TagType.class, true);
        TableUtils.dropTable(source, AttributeType.class, true);
        TableUtils.dropTable(source, Attribute.class, true);
    }

    @After
    public void tearDown() throws Exception {
        // destroy the data source which should close underlying connections
        if (cs != null){
//            this.dropAllTables(cs);
            cs.close();
        }
    }

    @Test
    public void canSaveTag() throws Exception {
        ExtentTag tag = new ExtentTag("N01", noun);
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
        ArrayList<int[]> spans = new ArrayList<int[]>();
        spans.add(span);
        ExtentTag nTag = new ExtentTag("N01", noun);
        for (CharIndex ci: nTag.setSpans(spans)) { charIndexDao.create(ci); }
        nTag.setText("Crown");
        eTagDao.create(nTag);

        List<ExtentTag> retrievedTags = eTagDao.queryForEq("tid", "N01");
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
        ExtentTag nTag = new ExtentTag("N01", noun);
        for (CharIndex ci: nTag.setSpans(0, 1, 2, 3, 4)) { charIndexDao.create(ci); }
        nTag.setText("Crown");
        eTagDao.create(nTag);

        int[] span = new int[] {2, 5};
        ArrayList<int[]> spans = new ArrayList<int[]>();
        spans.add(span);
        ExtentTag vTag = new ExtentTag("V01", verb);
        for (CharIndex ci: vTag.setSpans(spans)) { charIndexDao.create(ci); }
        eTagDao.create(vTag);
        vTag.setText("own");
        eTagDao.update(vTag);

        assertEquals(
                "Expected 2 tags in DB, found " + eTagDao.countOf(),
                2, eTagDao.countOf());

        // TODO 151209 make column names modularized as string resources
        ExtentTag retrievedNTag
                = eTagDao.queryBuilder().where().eq("tagtype_id", noun.getName()).queryForFirst();
        ExtentTag retrievedVTag
                = eTagDao.queryBuilder().where().eq("tagtype_id", verb.getName()).query().get(0);
        assertEquals(
                "Expected 3 chars allocated to vTag set by List, found: " + retrievedVTag.getSpans().size(),
                3, retrievedVTag.getSpansAsList().size()
        );
        assertEquals(
                "Expected same text of nTag after retrieved, found " + retrievedNTag.getText(),
                "Crown", retrievedNTag.getText()
        );
    }

    @Test
    public void canHaveMutableAttribute() throws Exception {

        ExtentTag nTag = new ExtentTag("N01", noun);
        for (CharIndex ci: nTag.setSpans(0, 1, 2, 3, 4)) { charIndexDao.create(ci); }
        nTag.setText("Crown");
        eTagDao.create(nTag);

        AttributeType properNoun = new AttributeType(noun, "isProper");
        attTypeDao.create(properNoun);
        Attribute att = new Attribute(nTag, properNoun, Boolean.toString(true));
        attDao.create(att);

        List<Attribute> retrievedAtts = attDao.queryForEq("extenttag_id", "N01");
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

    }

    @Ignore
    @Test
    public void canQueryByLocation() throws Exception {
        //TODO 151209 write here

    }
    //            "jonathan"
}