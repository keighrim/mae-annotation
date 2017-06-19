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

package edu.brandeis.cs.nlp.mae.io;

import org.junit.Ignore;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.StringReader;

/**
 * Created by krim on 3/10/17.
 */
public class MaeFileWriterTest {

    private void writeAndPrintFile(String text) throws Exception {
        File testOutput = new File("test-outout.txt");
        if (!testOutput.exists()) testOutput.createNewFile();
            MaeFileWriter.writeTextToEmptyXML(new BufferedReader(new StringReader(text)),
                    "test_task", testOutput);

        BufferedReader br = new BufferedReader(new FileReader(testOutput));
        String line;
        while ((line = br.readLine()) != null) {
            System.out.println(line);
        }
    }

    @Ignore
    public void canWriteTextToEmptyXML() throws Exception {
        String test = "President Trump has inherited an economy that set a record Friday " +
                "with 77 consecutive months of job growth, or, as put it, a “mess.”\n" +
                "In Trump’s first full month in office, the economy added 235,000 jobs," +
                " the unemployment rate ticked down to 4.7 percent, and wage growth " +
                "picked up a little to hit 2.8 percent over the past year. \nThe U.S. " +
                "economy might not quite be great again, but it’s on its way — and it" +
                " has been for a long time. Indeed, as you can see below, this is the " +
                "same recovery we’ve had for almost eight years: slow and steady growth, " +
                "averaging somewhere around 180,000 and 200,000 jobs a month over the " +
                "past three years. This hasn't been enough to cut unemployment at anything " +
                "but a gradual pace, but 77 months of a gradual pace can get you pretty far.";
        writeAndPrintFile(test);
    }

    @Ignore
    public void canWriteUnicodeTextToEmptyXML() throws Exception {
       writeAndPrintFile("" +
               "698517229072035841 ...' it was accidental, ..but it sounded \uD83D\uDD0A good, ...so the \uD83D\uDC1E\uD83D\uDC1E\uD83D\uDC1E. \uD83D\uDC1E  decided to stick with it... \uD83D\uDC35\n" +
               "698507655384465408 @ScottyMcCreery  's voice and the lyrics of five more minutes =perfection esp the last part.  \uD83D\uDE0D\uD83D\uDE22\uD83D\uDC4C \uD83C\uDFB6\n" +
               "698507375100170240 \uD83D\uDC65\uD83D\uDC65 (the vamps) \n" +
               "+ \uD83C\uDF0A (the tide)\n" +
               " + \uD83C\uDFA4\uD83C\uDFB6\n" +
               " + \uD83C\uDF5F\uD83C\uDF55 \n" +
               "+ ☀ \n" +
               "+ \uD83D\uDE02\uD83D\uDE0C♥ (happiness)\n" +
               "698507113841070080 RT @PatamaNiJuan: I will never forget :\n" +
               "\n" +
               "1. God \uD83D\uDC92\n" +
               "2. My Parents \uD83D\uDC75\uD83D\uDC74\n" +
               "3. My Friends \uD83D\uDE1C \uD83D\uDE01 \n" +
               "4. Food \uD83C\uDF55 \uD83C\uDF5F \n" +
               "5. Password of our WIFI \n" +
               "\n" +
               "698507368846508033 @tracyvwilson I say NO! I got married July 2015 and skipped it. my mother was concerned a/b being improper, I was concerned about \uD83C\uDF0E and \uD83D\uDCB2\n" +
               "698515384761069568 Happy birthday beautiful! \uD83C\uDF89 I hope your day is as great as you are and more! \uD83D\uDE1B\uD83E\uDD18\uD83C\uDFFD Love you sm \uD83D\uDE0D\uD83C\uDF6F\uD83D\uDC9B @MariahLynnn__\n" +
               "698604202315149316 I need more book shelves for my books \uD83D\uDE2D\n" +
               "698604599712878592 RT @shoestylezz: Retweet if you play Any:\n" +
               "✔ Basketball\n" +
               "✔ Football\n" +
               "✔ Volleyball\n" +
               "✔ Baseball\n" +
               "✔ Hockey\n" +
               "✔ Soccer\n" +
               "✔ Tennis\n" +
               "✔ Golf\n" +
               "✔ Cheer\n" +
               "✔ Track…\n" +
               "698604101618245634 RT @_toniyah: that lil \uD83D\uDE3B got some power \uD83D\uDCAA\uD83C\uDFFD \uD83D\uDE39\n" +
               "698604322985218048 RT @camerondallas: Im going to be at Magcon for the rest of the day \uD83D\uDE0A \n" +
               "\n" +
               "RT this for a follow! I'm going to follow a bunch of you at the end…\n" +
               "698517539031117824 RT @_keilondon: Choose your man wisely, always remember he represents you!!! \uD83D\uDD11\n" +
               "698517467707129856 @chancetherapper GOAT \uD83D\uDC10 you sir are the goat\n" +
               "698517426594381826 Goodnight \uD83D\uDC4B\n" +
               "698515449529528320 Such a windy night \uD83C\uDF00");
    }
}