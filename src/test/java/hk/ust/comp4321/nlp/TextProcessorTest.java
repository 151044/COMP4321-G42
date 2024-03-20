package hk.ust.comp4321.nlp;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TextProcessorTest {

    @Test
    void toSentence() {
        List<String> paragraphs = List.of(
                "Mr. Smith ate an apple. He was..., was..., he was? He wasn't. He never existed. He was always " +
                        "a figment of your imagination.",
                "When talking about OOP, we use the dot notation (.) a lot. It can be used to access fields like this: " +
                        "apple.color. It can also be used to access methods like this: apple.burn().",
                "Is it natural to think about what happens after death? How do humans cope with the concept of dying? " +
                        "If we all die someday, is there meaning to life? These are all questions that the course will " +
                        "address through a interdisciplinary lens - both scientific thinking and humanities will be employed " +
                        "to address the issues surrounding death and dying.",
                "COMP course irl (Gone Wrong!?) (With Guns) (Oh noes!!) (GG nah?!)"
        );
        assertEquals(5, TextProcessor.getInstance().toSentence(paragraphs.get(0)).size());
        assertEquals(3, TextProcessor.getInstance().toSentence(paragraphs.get(1)).size());
        assertEquals(4, TextProcessor.getInstance().toSentence(paragraphs.get(2)).size());
        assertEquals(1, TextProcessor.getInstance().toSentence(paragraphs.get(3)).size());
    }

    @Test
    void toTokens() {
        List<String> sentences = List.of("The compositional approach of Chopin varied throughout his life - his early " +
                "period (around 1817-1829) had relatively simple pieces, while his later periods " +
                " (around 1845-1849) showed an increasing amount of polyphony.",
                "The sea was blue, the sky was blue, and you are still blue.",
                "This concept is quasi-connected to the intra-school dynamics of a university.");
        System.out.println(TextProcessor.getInstance().toTokens(sentences.get(0)));
        assertEquals(37, TextProcessor.getInstance().toTokens(sentences.get(0)).size());
        assertEquals(16, TextProcessor.getInstance().toTokens(sentences.get(1)).size());
        assertEquals(12, TextProcessor.getInstance().toTokens(sentences.get(2)).size());
    }
}