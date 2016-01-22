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

package edu.brandeis.cs.nlp.mae.util;

import edu.brandeis.cs.nlp.mae.MaeException;
import edu.brandeis.cs.nlp.mae.MaeStrings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by krim on 12/16/15.
 */
public class SpanHandler {

    private static final Logger logger = LoggerFactory.getLogger(SpanHandler.class.getName());

    /**
     * Takes a string representing (possibly) multiple spans of an extent tag
     * then return list of integer pairs
     *
     * @param spansString - string of spans
     * @return a ArrayList of int[]
     */
    public static ArrayList<int[]> convertStringToPairs(String spansString) {
        logger.debug(String.format("=== String %s -> Pairs ===", spansString));
        ArrayList<int[]> spans = new ArrayList<>();
        if (spansString == null || spansString.equals("") || spansString.equals(MaeStrings.NCSPAN_PLACEHOLDER)) {
            spans.add(new int[]{MaeStrings.NC_START, MaeStrings.NC_END});
            return spans;
        }

        // split each span
        String[] pairs = spansString.split(MaeStrings.SPANSEPARATOR);
        for (String pair : pairs) {
            logger.debug(String.format("converting \"%s\" into a pair", pair));
            int[] span = new int[2];
            int i = 0;
            for (String loc : pair.split(MaeStrings.SPANDELIMITER)) {
                logger.debug(String.format("start/end: %d, offset: %s", i, loc));
                span[i++] = Integer.parseInt(loc);
                logger.debug(String.format("after putting: %s", Arrays.toString(span)));
            }
            spans.add(span);
        }
        Collections.sort(spans, new SpansPairComparator());
        logger.debug("=== Conversion finished ===");
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
    public static String convertPairsToString(List<int[]> spans) {
        return convertArrayToString(convertPairsToArray(spans));
//        if (spans.size() == 0) {
//            return MaeStrings.NCSPAN_PLACEHOLDER;
//        }
//
//        String spanString = "";
//        Iterator<int[]> iter = spans.iterator();
//        while (iter.hasNext()) {
//            int[] span = iter.next();
//            spanString += span[0] + MaeStrings.SPANDELIMITER + span[1];
//            if (iter.hasNext()) {
//                spanString += MaeStrings.SPANSEPARATOR;
//            }
//        }
//        logger.debug("=== Conversion finished ===");
//        return spanString;
    }

    /**
     * Takes an array of CharIndex, make it into an array of int pairs,
     * which can be used in convertPairsToString()
     *
     * @param spans - an sorted set of integer pairs
     * @return a ArrayList of int[]
     */
    public static ArrayList<int[]> convertArrayToPairs(int[] spans) {
        logger.debug(String.format("=== Array %s -> Pairs ===", Arrays.toString(spans)));

        if (spans == null || spans.length == 0) {
            ArrayList<int[]> nonComsumingSpan = new ArrayList<>();
            nonComsumingSpan.add(new int[]{MaeStrings.NC_START, MaeStrings.NC_END});
            return nonComsumingSpan;
        }

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
        logger.debug("=== Conversion finished ===");
        return spansList;

    }

    public static int[] convertPairsToArray(List<int[]> spansPairs) {
        logger.debug(String.format("=== Pairs %s -> Array ===", listOfArraysToString(spansPairs)));
        ArrayList<int[]> ranges = new ArrayList<>();
        for (int[] pair : spansPairs) {
            ranges.add(range(pair[0], pair[1]));
        }
        // concatArrays will do sorting and dupe removal, duplicate work in advance is no need
        logger.debug("=== Conversion finished ===");
        return concatenateArrays(ranges);

    }

    public static String convertArrayToString(int[] spans) {
        logger.debug(String.format("=== Array %s -> String ===", Arrays.toString(spans)));

        if (spans == null || spans.length == 0) {
            return MaeStrings.NCSPAN_PLACEHOLDER;
        }

        Arrays.sort(spans);

        int prev = spans[0];
        if (spans.length == 1) {
            return String.format("%d%s%d", prev, MaeStrings.SPANDELIMITER, prev+1);
        }
        String spansString = Integer.toString(prev);
        for (int i = 1; i < spans.length; i++) {
            if (i == spans.length - 1) {
                // +1's for exclusive end
                spansString += MaeStrings.SPANDELIMITER + (spans[i] + 1);
            } else if (prev + 1 < spans[i]) {
                spansString += MaeStrings.SPANDELIMITER + (prev + 1) + MaeStrings.SPANSEPARATOR + spans[i];
                prev = spans[i];
            } else {
                prev = spans[i];
            }
        }
        logger.debug("=== Conversion finished ===");
        return spansString;

    }

    public static int[] convertStringToArray(String spansString) throws MaeException {
        logger.debug(String.format("=== String %s -> Array ===", spansString));

        if (spansString == null || spansString.equals("") || spansString.equals(MaeStrings.NCSPAN_PLACEHOLDER)) {
            return new int[0];
        }

        List<int[]> spansArrays = new LinkedList<>();
        // split each span
        String[] pairs = spansString.split(MaeStrings.SPANSEPARATOR);
        for (String pair : pairs) {
            try {
                int start = Integer.parseInt(pair.split(MaeStrings.SPANDELIMITER)[0]);
                int end = Integer.parseInt(pair.split(MaeStrings.SPANDELIMITER)[1]);
                if (start >= end) {
                    throw new MaeException("SpanString ill-formed: start of each span should be smaller than its paired end");
                }
                spansArrays.add(range(start, end));
            } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                throw new MaeException("SpanString ill-formed: make sure using proper delimiters; \"~\" and \",\" (no space)");
            }
        }

        logger.debug("=== Conversion finished ===");
        return concatenateArrays(spansArrays);

    }

    public static int[] convertIntegerlistToIntegerarray(List<Integer> list) {
        int[] array = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return array;
    }

    public static List<Integer> convertIntegerarrayToIntegerlist(int[] array) {
        List<Integer> list = new LinkedList<>();
        for (int i : array) {
            list.add(i);
        }
        return list;
    }

    public static int[] range(int inclusiveStart, int exclusiveEnd) {
        int[] range = new int[exclusiveEnd - inclusiveStart];
        for (int i = inclusiveStart; i < exclusiveEnd; i++) {
            range[i - inclusiveStart] = i;
        }
        return range;
    }

    public static int[] concatenateArrays(Collection<int[]> arrays) {

        TreeSet<Integer> concatenateAndSort = new TreeSet<>();
        for (int[] array : arrays) {
            for (int item : array) {
                concatenateAndSort.add(item);
            }
        }
        int[] concatenatedArray = new int[concatenateAndSort.size()];
        int i = 0;
        for (Integer item : concatenateAndSort) {
            concatenatedArray[i++] = item;
        }
        return concatenatedArray;
    }

    private static class SpansPairComparator implements Comparator<int[]> {

        @Override
        public int compare(int[] a1, int[] a2) {
            return a1[0] - a2[0];
        }

    }

    public static boolean listOfArraysEquals(List<int[]> a, List<int[]> b) {
        if (a.size() != b.size()) {
            return false;
        }
        for (int i = 0; i < a.size(); i++) {
            if (!Arrays.equals(a.get(i), b.get(i))) {
                return false;
            }
        }
        return true;

    }

    public static String listOfArraysToString(List<int[]> list) {
        if (list.size() == 0) {
            return MaeStrings.NCSPAN_PLACEHOLDER;
        }
        String string = "";
        for (int[] pair : list) {
            string += Arrays.toString(pair) + ", ";
        }
        return string.substring(0,string.length()-2);
    }
}

