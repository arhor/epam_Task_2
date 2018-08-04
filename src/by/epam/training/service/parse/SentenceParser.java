package by.epam.training.service.parse;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import by.epam.training.model.CompositeObject;
import by.epam.training.model.IComposite;
import by.epam.training.model.Leaf;

public class SentenceParser extends Parser {
	
	private static final String WORD_OR_DELIMITER =
            "([A-Za-z0-9]+)|([^A-Za-z0-9])";
    private static final String WORD = "[A-Za-z0-9]+";

	public IComposite parse(String sentence) {
		CompositeObject compositeSentence = new CompositeObject();
        Pattern pattern = Pattern.compile(WORD_OR_DELIMITER);
        Matcher matcher = pattern.matcher(sentence);
        String current = "";
        while (matcher.find()) {
            current = matcher.group();
            if (!current.matches(WORD)) {
                compositeSentence.add(new Leaf(current));
            } else {
                compositeSentence.add(successor.parse(current));
            }
        }
        return compositeSentence;
	}
}