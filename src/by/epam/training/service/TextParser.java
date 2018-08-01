/*
 * class: TextParser
 *
 * version: 1.0 26 Jul 2018
 *
 * author: Maxim Burishinets
 */


package by.epam.training.service;

import by.epam.training.model.IComposite;
import by.epam.training.model.CompositeObject;
import by.epam.training.model.Leaf;

import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class TextParser {

    private static final String PARAGRAPH_OR_LISTING =
            "((\\s*)(\\\\~)([\\w\\W]+)(~\\\\)(\\s+))|((\\s*)(.+)(\\s*))";
    private static final String LISTING =
            "(\\s*)(\\\\~)(\\w|\\W)+(~\\\\)(\\s+)";
    private static final String SENTENCE = "([^.!?]+)(\\.|!|\\?|\\s)*";
    private static final String WORD_OR_DELIMITER =
            "([A-Za-z0-9]+)|([^A-Za-z0-9])";
    private static final String WORD = "[A-Za-z0-9]+";

    /*
     * method takes a string that represents whole text, splits it to separate
     * paragraphs and code-listing blocks (text blocks between /~ and ~/ signs)
     * then add them to composite-object text (composite paragraphs and leaf
     * listing-blocks) and returns it
     */
    public static IComposite parseToParagraphs(String text, FileWriter destination) throws IOException {
        CompositeObject compositeText = new CompositeObject();
        Pattern pattern = Pattern.compile(PARAGRAPH_OR_LISTING);
        Matcher matcher = pattern.matcher(text);
        String current = "";
        while (matcher.find()) {
            current = matcher.group();
            if (current.matches(LISTING)) {
                try {
                    destination.write("[Listing]:\r\n" + current + "\r\n");
                } catch (IOException e) {
                    throw e;
                }
                compositeText.add(new Leaf(current));
            } else {
                try {
                    destination.write("[Paragraph]:\r\n" + current + "\r\n");
                } catch (IOException e) {
                    throw e;
                }
                compositeText.add(parseToSentences(current, destination));
            }
        }
        return compositeText;
    }

    /*
     * method takes a string that represents a single paragraph and split it
     * to separate sentences, adds them as composite-objects to composite-
     * object paragraph and returns it
     */

    private static IComposite parseToSentences(String paragraph, FileWriter destination) throws IOException {
        CompositeObject compositeParagraph = new CompositeObject();
        Pattern pattern = Pattern.compile(SENTENCE);
        Matcher matcher = pattern.matcher(paragraph);
        String current = "";
        while (matcher.find()) {
            current = matcher.group();
            try {
                destination.write("[Sentence]:\n\n" + current + "\r\n");
            } catch (IOException e) {
                throw e;
            }
            compositeParagraph.add(parseToWords(current));
        }
        return compositeParagraph;
    }

    /*
     * method takes a string that represents a single sentence and splits it to
     * separate words and delimiters, then method adds words as composite-
     * objects and delimiters as leaf-objects to composite-object sentence
     * and returns it
     */

    private static IComposite parseToWords(String sentence) {
        CompositeObject compositeSentence = new CompositeObject();
        Pattern pattern = Pattern.compile(WORD_OR_DELIMITER);
        Matcher matcher = pattern.matcher(sentence);
        String current = "";
        while (matcher.find()) {
            current = matcher.group();
            if (!current.matches(WORD)) {
                compositeSentence.add(new Leaf(current));
            } else {
                compositeSentence.add(parseToChars(current));
            }
        }
        return compositeSentence;
    }

    /*
     * method takes a sequence of letters that represents a word and splits it
     * to separate characters, each of them is added to IComposite-object word
     * as a Leaf-object, then method returns IComposite-object word
     */
    private static IComposite parseToChars(String word) {
        CompositeObject compositeWord = new CompositeObject();
        for (char character : word.toCharArray()) {
            String content = String.valueOf(character);
            compositeWord.add(new Leaf(content));
        }
        return compositeWord;
    }

    // returns string that contains information about parsed text
    public static String getTextInfo(IComposite wholeText) {
        int listing = 0;
        int paragraph = 0;
        int sentence = 0;
        int word = 0;
        int delimiter = 0;
        int letter = 0;
        for (int i = 0; i < ((CompositeObject)wholeText).size(); i++) {
            IComposite object1 = wholeText.get(i);
            if (object1 instanceof Leaf) {
                listing++;
            } else {
                paragraph++;
                for (int j = 0; j < ((CompositeObject)object1).size(); j++) {
                    sentence++;
                    IComposite object2 = object1.get(j);
                    for (int k = 0; k < ((CompositeObject)object2).size(); k++) {
                        IComposite object3 = object2.get(k);
                        if (object3 instanceof Leaf) {
                            delimiter++;
                        } else {
                            word++;
                            letter += ((CompositeObject)object3).size();
                        }
                    }
                }
            }
        }
        return String.format("listing: %d%n" +
                "paragraph: %d%n" +
                "sentence: %d%n" +
                "word: %d%n" +
                "delimiter: %d%n" +
                "letter: %d%n",
                listing, paragraph, sentence, word, delimiter, letter);
    }

    //text method for REGEX
    /*public static void getLines(String text) {
        Pattern pattern = Pattern.compile(TEXT_LINE);
        Matcher matcher = pattern.matcher(text);
        String currentLine = new String();
        StringBuilder sb = new StringBuilder();
        int counter = 1;
        boolean isListing = false;
        while (matcher.find()) {
            currentLine = matcher.group();
            if (currentLine.matches(LISTING_START) && !isListing) {
                isListing = true;
                sb.append(currentLine);
                System.out.println("LISTING STARTED!!!");
                continue;
            } else if (isListing && currentLine.matches(LISTING_END)) {
                isListing = false;
            }
            sb.append(currentLine);
            System.out.printf("%2d: %s%n", counter++, sb.toString());
            sb = new StringBuilder();
        }
    }*/

    /*public static IComposite parseToParagraphs(String text) {
        CompositeObject compositeText = new CompositeObject();
        StringBuilder sb = new StringBuilder();
        boolean isListing = false;
        boolean listingPresent = false;
        for (int i = 0; i < text.length(); i++) {
            String currentSymbol = text.substring(i, i + 1);
            sb.append(currentSymbol);
            if (currentSymbol.equals("\\") && !isListing && sb.length() == 1) {
                if (i < text.length() - 1
                        && text.substring(i, i + 2).equals("\\~")) {
                    isListing = true;
                }
            } else if (isListing && currentSymbol.equals("~")){
                if (i < text.length() - 1
                        && text.substring(i, i + 2).equals("~\\")) {
                    isListing = false;
                    listingPresent = true;
                }
            }
            // TODO: fix below - method doesn't add listing without "~/" sign
            if (!isListing) {
                if (currentSymbol.equals("\n") || i == text.length() - 1) {
                    if (listingPresent) {
                        compositeText.add(new Leaf(sb.toString()));
                        listingPresent = false;
                    } else {
                        compositeText.add(parseToSentences(sb.toString()));
                    }
                    sb = new StringBuilder();
                }
            }
        }
        return compositeText;
    }*/

    /*private static IComposite parseToSentences(String paragraph) {
        CompositeObject compositeParagraph = new CompositeObject();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < paragraph.length(); i++) {
            String currentSymbol = paragraph.substring(i, i + 1);
            sb.append(currentSymbol);
            if (i == paragraph.length() - 1
                    || (currentSymbol.matches("[.?!]")
                    && paragraph.substring(i + 1, i + 2).matches("[^.?!]"))) {
                compositeParagraph.add(parseToWords(sb.toString()));
                sb = new StringBuilder();
            }
        }
        return compositeParagraph;
    }*/

    /*private static IComposite parseToWords(String sentence) {
        CompositeObject compositeSentence = new CompositeObject();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sentence.length(); i++) {
            String currentSymbol = sentence.substring(i, i + 1);
            if (currentSymbol.matches("[A-Za-z0-9]")) {
                sb.append(currentSymbol);
            } else {
                compositeSentence.add(new Leaf(currentSymbol)); // delimiter
            }
            if (
                    sb.length() != 0
                            &&(i == sentence.length() - 1
                            || sentence.substring(i + 1, i + 2).matches("[^A-Za-z0-9]"))
                    ) {
                compositeSentence.add(parseToChars(sb.toString())); // word
                sb = new StringBuilder();
            }
        }
        return compositeSentence;
    }*/
}
