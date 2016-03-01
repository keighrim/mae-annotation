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

package edu.brandeis.cs.nlp.mae.util;

import edu.brandeis.cs.nlp.mae.MaeStrings;
import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by krim on 12/18/2015.
 */
public class SpanHandlerTest {

    @Test
    public void testConvertStringToPairs() throws Exception {
        String ncString = MaeStrings.NCSPAN_PLACEHOLDER;
        List<int[]> pairs = SpanHandler.convertStringToPairs(ncString);

        assertTrue(
                "Should convert non-consuming, found: (" + pairs.size() + ") " + Arrays.toString(pairs.get(0)),
                SpanHandler.listOfArraysEquals(Arrays.asList(new int[]{-1,-1}), pairs)
        );

        String cString = String.format("%d%s%d", 0, MaeStrings.SPANRANGE, 3);
        pairs = SpanHandler.convertStringToPairs(cString);
        List<int[]> gold = new LinkedList<>();
        gold.add(new int[]{0, 3});
        assertTrue(
                "Should convert a single span, found: " + SpanHandler.listOfArraysToString(pairs),
                SpanHandler.listOfArraysEquals(pairs, gold)
        );

        cString = String.format("%d%s%d%s%d%s%d%s%d%s%d",
                0, MaeStrings.SPANRANGE, 3, MaeStrings.SPANDELIMITER,
                7, MaeStrings.SPANRANGE, 11, MaeStrings.SPANDELIMITER,
                20, MaeStrings.SPANRANGE, 24);
        pairs = SpanHandler.convertStringToPairs(cString);
        gold = new LinkedList<>();
        gold.add(new int[]{0, 3});
        gold.add(new int[]{7,11});
        gold.add(new int[]{20,24});
        assertTrue(
                "Should convert a multispan, found: " + SpanHandler.listOfArraysToString(pairs),
                SpanHandler.listOfArraysEquals(pairs, gold)
        );

    }

    @Test
    public void testConvertPairsToString() throws Exception {
        List<int[]> pairs = new LinkedList<>();
        String string = SpanHandler.convertPairsToString(pairs);
        assertEquals(
                "Should convert an emtpy to a NC, found: " + string,
                MaeStrings.NCSPAN_PLACEHOLDER, string
        );

        pairs.add(new int[]{-1,-1});
        string = SpanHandler.convertPairsToString(pairs);
        assertEquals(
                "Should convert an NC to a NC, found: " + string,
                MaeStrings.NCSPAN_PLACEHOLDER, string
        );

        pairs.clear();
        pairs.add(new int[]{0,3});
        pairs.add(new int[]{7,11});
        pairs.add(new int[]{20,24});
        string = SpanHandler.convertPairsToString(pairs);
        assertEquals(
                "Should convert an multispan, found: " + string,
                String.format("%d%s%d%s%d%s%d%s%d%s%d",
                        0, MaeStrings.SPANRANGE, 3, MaeStrings.SPANDELIMITER,
                        7, MaeStrings.SPANRANGE, 11, MaeStrings.SPANDELIMITER,
                        20, MaeStrings.SPANRANGE, 24),
                string
        );

        pairs.clear();
        pairs.add(new int[]{7,11});
        pairs.add(new int[]{20,24});
        pairs.add(new int[]{0,3});
        string = SpanHandler.convertPairsToString(pairs);
        assertEquals(
                "Should convert an multispan, found: " + string,
                String.format("%d%s%d%s%d%s%d%s%d%s%d",
                        0, MaeStrings.SPANRANGE, 3, MaeStrings.SPANDELIMITER,
                        7, MaeStrings.SPANRANGE, 11, MaeStrings.SPANDELIMITER,
                        20, MaeStrings.SPANRANGE, 24),
                string
        );
    }

    @Test
    public void testConvertArrayToPairs() throws Exception {
        int[] array = new int[0];
        List<int[]> gold = new LinkedList<>();
        gold.add(new int[]{-1,-1});

        List<int[]> pairs = SpanHandler.convertArrayToPairs(array);
        assertTrue(
                "Should convert an empty to a NC span, found: " + SpanHandler.listOfArraysToString(pairs),
                SpanHandler.listOfArraysEquals(pairs, gold)
        );

        array = new int[]{9};
        pairs = SpanHandler.convertArrayToPairs(array);
        gold.clear();
        gold.add(new int[]{9,10});
        assertTrue(
                "Should convert a singleton span, found: " + SpanHandler.listOfArraysToString(pairs),
                SpanHandler.listOfArraysEquals(pairs, gold)
        );

        array = new int[]{0,1,2,3};
        pairs = SpanHandler.convertArrayToPairs(array);
        gold.clear();
        gold.add(new int[]{0,4});
        assertTrue(
                "Should convert a single span, found: " + SpanHandler.listOfArraysToString(pairs),
                SpanHandler.listOfArraysEquals(pairs, gold)
        );

        array = new int[]{0,1,2,3,7,8,9,10};
        pairs = SpanHandler.convertArrayToPairs(array);
        gold.add(new int[]{7,11});
        assertTrue(
                "Should convert a multispan, found: " + SpanHandler.listOfArraysToString(pairs),
                SpanHandler.listOfArraysEquals(pairs, gold)
        );

    }


    @Test
    public void testConvertPairsToArray() throws Exception {
        List<int[]> pairs = new LinkedList<>();
        int[] array = SpanHandler.convertPairsToArray(pairs);
        assertEquals(
                "Should convert an emtpy to an empty, found: " + Arrays.toString(array),
                Arrays.toString(new int[0]), Arrays.toString(array)
        );

        pairs.add(new int[]{-1,-1});
        array = SpanHandler.convertPairsToArray(pairs);
        assertEquals(
                "Should convert a NC to an empty, found: " + Arrays.toString(array),
                Arrays.toString(new int[0]), Arrays.toString(array)
        );

        pairs.clear();
        pairs.add(new int[]{0,3});
        array = SpanHandler.convertPairsToArray(pairs);
        assertEquals(
                "Should convert a single spans, found: " + Arrays.toString(array),
                Arrays.toString(new int[]{0,1,2}), Arrays.toString(array)
        );

        pairs.clear();
        pairs.add(new int[]{0,3});
        pairs.add(new int[]{7,11});
        pairs.add(new int[]{20,24});
        array = SpanHandler.convertPairsToArray(pairs);
        assertEquals(
                "Should convert an multispan, found: " + Arrays.toString(array),
                Arrays.toString(new int[]{0,1,2,7,8,9,10,20,21,22,23}), Arrays.toString(array)
        );

        pairs.clear();
        pairs.add(new int[]{7,11});
        pairs.add(new int[]{20,24});
        pairs.add(new int[]{0,3});
        array = SpanHandler.convertPairsToArray(pairs);
        assertEquals(
                "Should convert an not-sorted multispan, found: " + Arrays.toString(array),
                Arrays.toString(new int[]{0,1,2,7,8,9,10,20,21,22,23}), Arrays.toString(array)
        );
    }

    @Test
    public void testConvertArrayToString() throws Exception {
        int[] array = new int[0];
        String string = SpanHandler.convertArrayToString(array);
        assertEquals(
                "Should convert an empty to a NC span, found: " + string,
                MaeStrings.NCSPAN_PLACEHOLDER, string
        );

        array = new int[]{0,1,2,3};
        string = SpanHandler.convertArrayToString(array);
        assertEquals(
                "Should convert an single span, found: " + string,
                String.format("%d%s%d", 0, MaeStrings.SPANRANGE, 4), string
        );

        array = new int[]{0,1};
        string = SpanHandler.convertArrayToString(array);
        assertEquals(
                "Should convert an doubleton span, found: " + string,
                String.format("%d%s%d", 0, MaeStrings.SPANRANGE, 2), string
        );

         array = new int[]{0};
        string = SpanHandler.convertArrayToString(array);
        assertEquals(
                "Should convert an singleton span, found: " + string,
                String.format("%d%s%d", 0, MaeStrings.SPANRANGE, 1), string
        );

        array = new int[]{0,1,2,3,7,8,9,10};
        string = SpanHandler.convertArrayToString(array);
        assertEquals(
                "Should convert an multispan, found: " + string,
                String.format("%d%s%d%s%d%s%d", 0, MaeStrings.SPANRANGE, 4, MaeStrings.SPANDELIMITER, 7, MaeStrings.SPANRANGE, 11),
                string
        );

    }

    @Test
    public void testConvertStringToArray() throws Exception {
        String ncString = MaeStrings.NCSPAN_PLACEHOLDER;
        int[] array = SpanHandler.convertStringToArray(ncString);
        assertArrayEquals(
                "Should convert a non-consuming into an empty array, found: " + Arrays.toString(array),
                new int[0], array
        );

        String cString = String.format("%d%s%d", 0, MaeStrings.SPANRANGE, 3);
        array = SpanHandler.convertStringToArray(cString);
        assertArrayEquals(
                "Should convert a single span, found: " + Arrays.toString(array),
                new int[]{0,1,2}, array
        );

        cString = String.format("%d%s%d%s%d%s%d%s%d%s%d",
                0, MaeStrings.SPANRANGE, 3, MaeStrings.SPANDELIMITER,
                7, MaeStrings.SPANRANGE, 11, MaeStrings.SPANDELIMITER,
                20, MaeStrings.SPANRANGE, 24);
        array = SpanHandler.convertStringToArray(cString);
        assertArrayEquals(
                "Should convert a multispan, found: " + Arrays.toString(array),
                new int[]{0,1,2,7,8,9,10,20,21,22,23}, array
        );

        cString = String.format("%d%s%d%s%d%s%d%s%d%s%d",
                20, MaeStrings.SPANRANGE, 24, MaeStrings.SPANDELIMITER,
                7, MaeStrings.SPANRANGE, 11, MaeStrings.SPANDELIMITER,
                0, MaeStrings.SPANRANGE, 3);
        array = SpanHandler.convertStringToArray(cString);
        assertArrayEquals(
                "Should convert a not-sorted multispan, found: " + Arrays.toString(array),
                new int[]{0,1,2,7,8,9,10,20,21,22,23}, array
        );

    }

    @Test
    public void testRange() throws Exception {
        assertTrue(
                "Expected range(0,4) to generate [0,1,2,3], found: " + Arrays.toString(SpanHandler.range(0, 4)),
                Arrays.equals(SpanHandler.range(0, 4), new int[]{0,1,2,3})
        );

        assertTrue(
                "Expected range(0,0) to generate an empty array, found: " + Arrays.toString(SpanHandler.range(0, 0)),
                Arrays.equals(SpanHandler.range(0, 0), new int[]{})
        );
    }

    @Test
    public void testConcatenateArrays() throws Exception {
        int[] a = new int[]{0,1,2,3};
        int[] b = new int[]{10,11,12,13};
        List<int[]> two = Arrays.asList(a, b);
        int[] c = SpanHandler.concatenateArrays(two);
        assertTrue(
                "Can merge two arrays, found: " + Arrays.toString(c),
                Arrays.equals(new int[]{0,1,2,3,10,11,12,13}, c)
        );

        two = Arrays.asList(b, a);
        c = SpanHandler.concatenateArrays(two);
        assertTrue(
                "Can merge two arrays and sort, found: " + Arrays.toString(c),
                Arrays.equals(new int[]{0,1,2,3,10,11,12,13}, c)
        );

        List<int[]> three = Arrays.asList(a, b, c);
        int[] d = SpanHandler.concatenateArrays(three);
        assertTrue(
                "Can merge three arrays and remove all duplicates, found: " + Arrays.toString(d),
                Arrays.equals(new int[]{0,1,2,3,10,11,12,13}, d)
        );

    }


}