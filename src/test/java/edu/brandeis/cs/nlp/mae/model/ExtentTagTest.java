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
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

import static org.junit.Assert.assertEquals;

/**
 * Created by krim on 12/9/2015.
 */
public class ExtentTagTest {

    protected static final Logger logger = LoggerFactory.getLogger(ExtentTagTest.class.getName());

    private final static String DATABASE_URL = MaeStrings.TEST_DB_URL;
    private ConnectionSource cs;

    Dao<CharIndex, Integer> charIndexDao;
    Dao<ExtentTag, Integer> tagDao;
    Dao<TagType, Integer> tagTypeDao;
    Dao<AttributeType, Integer> attTypeDao;
    Dao<Attribute, Integer> attDao;

    @Before
    public void setUp() throws Exception {

        cs = new JdbcConnectionSource(DATABASE_URL);
        this.setupDatabase(cs);
    }

    private void setupDatabase(ConnectionSource source) throws SQLException {
        tagDao = DaoManager.createDao(source, ExtentTag.class);
        tagTypeDao = DaoManager.createDao(source, TagType.class);
        attTypeDao = DaoManager.createDao(source, AttributeType.class);
        attDao = DaoManager.createDao(source, Attribute.class);
        charIndexDao = DaoManager.createDao(source, CharIndex.class);

        TableUtils.dropTable(source, CharIndex.class, true);
        TableUtils.dropTable(source, ExtentTag.class, true);
        TableUtils.dropTable(source, TagType.class, true);
        TableUtils.dropTable(source, AttributeType.class, true);
        TableUtils.dropTable(source, Attribute.class, true);

        TableUtils.createTable(source, CharIndex.class);
        TableUtils.createTable(source, ExtentTag.class);
        TableUtils.createTable(source, TagType.class);
        TableUtils.createTable(source, AttributeType.class);
        TableUtils.createTable(source, Attribute.class);

    }

    @After
    public void tearDown() throws Exception {
        // destroy the data source which should close underlying connections
        if (cs != null){
            cs.close();
        }
    }

    @Test
    public void canSaveTagsOfTagType() {
        try {
            TagType noun = new TagType("NOUN", "N");
            tagTypeDao.create(noun);
            ExtentTag tag = new ExtentTag("N01", noun);
            tag.setSpans(1, 2, 3, 4);
            tag.setText("John");
            tagDao.create(tag);
            assertEquals("Expected 1 tag in DB, found " + tagDao.countOf(), 1, tagDao.countOf());
//            Tag retrievedTag = tagDao.countOf();
        } catch (SQLException e) {
            logger.error("SQL Error!");
            e.printStackTrace();
        }
    }

//            "jonathan"
}