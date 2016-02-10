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
 * For feedback, reporting bugs, use the project on Github
 * @see <a href="https://github.com/keighrim/mae-annotation">https://github.com/keighrim/mae-annotation</a>.
 */

package edu.brandeis.cs.nlp.mae.database;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import edu.brandeis.cs.nlp.mae.model.Attribute;
import edu.brandeis.cs.nlp.mae.model.CharIndex;
import edu.brandeis.cs.nlp.mae.model.ExtentTag;

import java.sql.SQLException;

/**
 * Created by krim on 12/15/2015.
 */
public class ExtentTagDao extends BaseDaoImpl<ExtentTag, String> {

    Dao<Attribute, Integer> attDao;
    Dao<CharIndex, Integer> charIndexDao;

    public ExtentTagDao(Class dataClass) throws SQLException {
        super(dataClass);
        setDaos();
    }

    public ExtentTagDao(ConnectionSource connectionSource,
                           Class dataClass) throws SQLException {
        super(connectionSource, dataClass);
        setDaos();
    }

    public ExtentTagDao(ConnectionSource connectionSource,
                           DatabaseTableConfig tableConfig) throws SQLException {
        super(connectionSource, tableConfig);
        setDaos();
    }

    private void setDaos() throws SQLException {
        attDao = DaoManager.createDao(getConnectionSource(), Attribute.class);
        charIndexDao = DaoManager.createDao(getConnectionSource(), CharIndex.class);

    }

    @Override
    public int update(ExtentTag tag) throws SQLException {
        refresh(tag);
        for (Attribute att : tag.getAttributes()) {
            attDao.createOrUpdate(att);
        }
        for (CharIndex ci : tag.getSpans()) {
            charIndexDao.createOrUpdate(ci);
        }
        return super.update(tag);
    }

    @Override
    public int delete(ExtentTag tag) throws SQLException {
        refresh(tag);
        for (Attribute att : tag.getAttributes()) {
            attDao.delete(att);
        }
        for (CharIndex ci : tag.getSpans()) {
            charIndexDao.delete(ci);
        }
        return super.delete(tag);
    }
}
