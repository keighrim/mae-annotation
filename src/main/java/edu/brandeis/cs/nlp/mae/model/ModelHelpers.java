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

import edu.brandeis.cs.nlp.mae.MaeStrings;

import java.util.*;

/**
 * Collection of static helper methods used within DB schema (model package)
 * Created by krim on 12/13/2015.
 */
public class ModelHelpers {

    /**
     * Takes a string representing (possibly) multiple spans of an extent tag
     * then return list of integer pairs
     *
     * @param spansString - string of spans
     * @return a ArrayList of int[]
     */
    public static ArrayList<int[]> parseSpansString(String spansString) {
        ArrayList<int[]> spans = new ArrayList<>();
        if (spansString == null || spansString.equals("") || spansString.equals("-1~-1")) {
            spans.add(new int[]{-1, -1});
            return spans;
        }

        // split each span
        String[] pairs = spansString.split(MaeStrings.SPANSEPARATOR);
        for (String pair : pairs) {
            int[] span = new int[2];
            int i = 0;
            for (String loc : pair.split(MaeStrings.SPANDELIMITER)) {
                span[i++] = Integer.parseInt(loc);
            }
            spans.add(span);
        }
        return spans;
    }

    /**
     * Takes an array of integer pairs, then merge it into a string. Each span
     * separated by SPANSEPARATOR start and end point of each span joined with
     * SPANDELIMITER
     *
     * @param spans - an sorted set of integer pairs
     * @return a formatted string of spans of a tag
     */
    public static String spansToString(ArrayList<int[]> spans) {
        String spanString = "";
        Iterator<int[]> iter = spans.iterator();
        while (iter.hasNext()) {
            int[] span = iter.next();
            if (iter.hasNext()) {
                spanString += span[0] + MaeStrings.SPANDELIMITER + span[1]
                        + MaeStrings.SPANSEPARATOR;
            } else {
                spanString += span[0] + MaeStrings.SPANDELIMITER + span[1];
            }
        }
        return spanString;
    }

    /**
     * Takes an array of CharIndex, make it into an array of int pairs,
     * which can be used in spansToString()
     *
     * @param spans - an sorted set of integer pairs
     * @return a ArrayList of int[]
     */
    public static ArrayList<int[]> parseCharIndices(List<CharIndex> spans) {
        if (spans == null || spans.size() == 0) {
            ArrayList<int[]> nonComsumingSpan = new ArrayList<>();
            nonComsumingSpan.add(new int[]{-1, -1});
            return nonComsumingSpan;
        }

        int[] locations = new int[spans.size()];
        for (int i = 0; i < spans.size(); i++) {
            locations[i] = (spans.get(i).getLocation());
        }
        return spansArrayToList(locations);

    }

    public static ArrayList<int[]> spansArrayToList(int[] spans) {

        Arrays.sort(spans);

        ArrayList<int[]> spansList = new ArrayList<>();
        int start = spans[0];
        int prev = spans[0];
        for (int i = 1; i < spans.length; i++) {
            if (i == spans.length - 1) {
                spansList.add(new int[]{start, spans[i] + 1});
            } else if (prev + 1 < spans[i]) {
                spansList.add(new int[]{start, prev + 1});
                prev = spans[i];
                start = spans[i];
            } else {
                prev = spans[i];
            }
        }
        return spansList;

    }

    public static Map<String, String> getArgumentsWithNames(Collection<Argument> arguments) {
        HashMap<String, String> argumentsWithNames = new HashMap<>();
        for (Argument argument : arguments) {
            argumentsWithNames.put(argument.getName(), argument.getArgument().getTid());
        }
        return argumentsWithNames;

    }

    public static Map<String, String> getAttbutesWithNames(Collection<Attribute> attributes) {
        HashMap<String, String> attributesWithNames = new HashMap<>();
        for (Attribute attribute : attributes) {
            attributesWithNames.put(attribute.getName(), attribute.getValue());
        }
        return attributesWithNames;

    }
}
