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

import edu.brandeis.cs.nlp.mae.model.TagType;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

/**
 * Created by krim on 12/16/15.
 */
public class IdHandler {

    private Map<TagType, TreeSet<Integer>> tracker;
    private int startFrom;

    public IdHandler() {
        this(0);
    }

    public IdHandler(int idStartFrom) {
        startFrom = idStartFrom;
        tracker = new HashMap<>();
    }

    public boolean addId(TagType type, int id) {
        if (!tracker.containsKey(type)) {
            TreeSet<Integer> ids = new TreeSet<>();
            ids.add(id);
            tracker.put(type, ids);
        } else if (!tracker.get(type).contains(id)) {
            tracker.get(type).add(id);
        } else {
            return false;
        }
        return true;
    }

    public boolean addId(TagType type, String tid) {
        int id = Integer.parseInt(tid.substring(type.getPrefix().length(), tid.length()));
        return addId(type, id);
    }

    /**
     * Finds the next ID that can be used for that element
     */
    public String getNextID(TagType type) {

        TreeSet<Integer> existingIds = tracker.get(type);
        if (existingIds == null) {
            return type.getPrefix() + startFrom;
        }

        if (existingIds.last() + 1 == existingIds.size() + startFrom) {
            return type.getPrefix() + (existingIds.last() + 1);
        }

        Iterator<Integer> iter = existingIds.iterator();
        // legacy id numbering starts from 0
        // checking id from 0 every time would be extremely inefficient, maybe reverse way is better?
        int prev = startFrom;

        while (iter.hasNext()) {
            int next = iter.next();
            if (next >= prev + 1) {
                break;
            } else {
                prev++;
            }
        }
        return type.getPrefix() + prev;
    }

}

