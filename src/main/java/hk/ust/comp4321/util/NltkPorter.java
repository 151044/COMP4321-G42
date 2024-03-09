/*
 * Copyright 2024 @151044
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * Note: Since the NLTK code is Apache, this file is also Apache Licensed.
 */
package hk.ust.comp4321.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This is the Porter stemming algorithm. It follows the algorithm
 * presented in
 * <p>
 * Porter, M. "An algorithm for suffix stripping." Program 14.3 (1980): 130-137.
 * <p>
 * with some optional deviations that can be turned on or off with the
 * `mode` argument to the constructor.
 * <p>
 * Martin Porter, the algorithm's inventor, maintains a web page about the
 * algorithm at
 * <p>
 *     <a href="https://www.tartarus.org/~martin/PorterStemmer/">...</a>
 * <p>
 * which includes another Python implementation and other implementations
 * in many languages.
 * <p>
 * This is a reimplementation of the NLTK Porter stemmer in Java.
 *
 * @author 151044
 */
public class NltkPorter {
    private static final List<Character> VOWELS = List.of('a', 'e', 'i', 'o', 'u');
    private static final Predicate<String> TRUE = (ignored) -> true;
    private static final Map<String, String> IRREGULAR_FORMS = new HashMap<>();

    static {
        // final static block, yay!
        IRREGULAR_FORMS.put("skies", "sky");
        IRREGULAR_FORMS.put("sky", "sky");
        IRREGULAR_FORMS.put("dying", "die");
        IRREGULAR_FORMS.put("lying", "lie");
        IRREGULAR_FORMS.put("tying", "tie");
        IRREGULAR_FORMS.put("news", "news");
        IRREGULAR_FORMS.put("innings", "inning");
        IRREGULAR_FORMS.put("inning", "inning");
        IRREGULAR_FORMS.put("outings", "outing");
        IRREGULAR_FORMS.put("outing", "outing");
        IRREGULAR_FORMS.put("cannings", "canning");
        IRREGULAR_FORMS.put("canning", "canning");
        IRREGULAR_FORMS.put("howe", "howe");
        IRREGULAR_FORMS.put("proceed", "proceed");
        IRREGULAR_FORMS.put("exceed", "exceed");
        IRREGULAR_FORMS.put("succeed", "succeed");
    }

    /**
     * Finds if the letter at a specified index is a consonant or not.
     * A consonant is defined in the paper as follows:
     * <p>
     *             A consonant in a word is a letter other than A, E, I, O or
     *             U, and other than Y preceded by a consonant. (The fact that
     *             the term "consonant" is defined to some extent in terms of
     *             itself does not make it ambiguous.) So in TOY the consonants
     *             are T and Y, and in SYZYGY they are S, Z and G. If a letter
     *             is not a consonant it is a vowel.
     * @param word The word to index
     * @param index The index to find if word[index] is consonant
     * @return Returns True if word[index] is a consonant, False otherwise
     */
    private static boolean isConsonant(String word, int index) {
        char toIndex = word.charAt(index);
        if (VOWELS.contains(toIndex)) {
            return false;
        } else if (toIndex == 'y') {
            return index == 0 || !isConsonant(word, index - 1);
        } else {
            return true;
        }
    }

    private static boolean hasVowel(String word) {
        return IntStream.range(0, word.length()).anyMatch(i -> !isConsonant(word, i));
    }

    /**
     * Returns the 'measure' of stem, per definition in the paper.
     * <p>
     *         From the paper:
     * <p>
     *             A consonant will be denoted by c, a vowel by v. A list
     *             ccc... of length greater than 0 will be denoted by C, and a
     *             list vvv... of length greater than 0 will be denoted by V.
     *             Any word, or part of a word, therefore has one of the four
     *             forms:
     * <p>
     *                 CVCV ... C
     *                 CVCV ... V
     *                 VCVC ... C
     *                 VCVC ... V
     * <p>
     *             These may all be represented by the single form
     * <p>
     *                 [C]VCVC ... [V]
     * <p>
     *             where the square brackets denote arbitrary presence of their
     *             contents. Using (VC){m} to denote VC repeated m times, this
     *             may again be written as
     * <p>
     *                 [C](VC){m}[V].
     * <p>
     *             m will be called the \measure\ of any word or word part when
     *             represented in this form. The case m = 0 covers the null
     *             word. Here are some examples:
     * <p>
     *                 m=0    TR,  EE,  TREE,  Y,  BY.
     *                 m=1    TROUBLE,  OATS,  TREES,  IVY.
     *                 m=2    TROUBLES,  PRIVATE,  OATEN,  ORRERY.
     * @return The measure of the stem
     */
    private static int measure(String stem) {
        // Consonant-Vowel Sequence
        String cvSequence = IntStream.range(0, stem.length()).mapToObj(i -> isConsonant(stem, i))
                .map(b -> b ? "c" : "v").collect(Collectors.joining());
        int count = 0, offset = 0, index;
        while ((index = cvSequence.indexOf("cv", offset)) != -1) {
            offset = index + 2;
            count++;
        }
        return count;
    }
    private static boolean hasPositiveMeasure(String stem) {
        return measure(stem) > 0;
    }

    /**
     * Checks if the word ends in two consonants.
     * @param word The word to check
     * @return True if the word ends in two consonants, false otherwise
     */
    private static boolean endsDoubleConsonant(String word) {
        int wordLen = word.length();
        return wordLen >= 2
                && (word.charAt(wordLen - 1) == word.charAt(wordLen - 2))
                && isConsonant(word, wordLen - 1);
    }

    // Implementation helper
    private static final List<Character> WXY = List.of('w', 'x', 'y');
    /**
     * Checks if the word ends in consonant-vowel-consonant, and the second consonant is not one of w, x, or y.
     * @param word The word to check
     * @return True if the word ends in consonant-vowel-consonant, false otherwise
     */
    private static boolean endsCvc(String word) {
        int wordLen = word.length();
        return (wordLen >= 3 && IntStream.range(wordLen - 3, wordLen).allMatch(i -> isConsonant(word, i))
                && !WXY.contains(word.charAt(wordLen - 1)))
                || (wordLen == 2 && !isConsonant(word, 0) && isConsonant(word, 1));
    }
    private static String replaceSuffix(String word, String suffix, String replacement) {
        if (!word.endsWith(suffix)) {
            throw new IllegalArgumentException("Word " + word + "  does not end with suffix " + suffix);
        }
        return word.substring(0, word.length() - suffix.length()) + replacement;
    }
    private record SuffixRule(String suffix, String replacement, Predicate<String> condition) {}
    private static String applyRules(String word, List<SuffixRule> rules) {
        for (SuffixRule rule : rules) {
            boolean matched = false;
            String stem = "HAHA"; // Oopsie!
            if (rule.suffix.equals("*d") && endsDoubleConsonant(word)) {
                matched = true;
                stem = word.substring(0, word.length() - 2);
            }
            if (word.endsWith(rule.suffix)) {
                matched = true;
                stem = replaceSuffix(word, rule.suffix, "");
            }
            if (matched) {
                if (rule.condition.test(stem)) {
                    return stem + rule.replacement;
                } else {
                    return word;
                }
            }
        }
        return word;
    }

    private static final List<SuffixRule> RULES_1A = List.of(
            new SuffixRule("sses", "ss", TRUE),
            new SuffixRule("ies", "i", TRUE),
            new SuffixRule("ss", "ss", TRUE),
            new SuffixRule("s", "", TRUE)
    );
    /**
     * Implements Step 1a from "An algorithm for suffix stripping"
     * <p>
     *         From the paper:
     * <p>
     *             SSES -> SS                         caresses  ->  caress
     *             IES  -> I                          ponies    ->  poni
     *                                                ties      ->  ti
     *             SS   -> SS                         caress    ->  caress
     *             S    ->                            cats      ->  cat
     * @param word The word to apply the rules to
     * @return The word after applying rule 1a
     */
    private static String rule1a(String word) {
        // NLTK extension
        if (word.endsWith("ies") && word.length() == 4) {
            return replaceSuffix(word, "ies", "ie");
        }
        return applyRules(word, RULES_1A);
    }

    private static final List<String> INTERMEDIATE_1B = List.of("ed", "ing");
    private static final List<SuffixRule> RULES_1B = List.of(
            new SuffixRule("at", "ate", TRUE),
            new SuffixRule("bl", "ble", TRUE),
            new SuffixRule("iz", "ize", TRUE),
            new SuffixRule("", "e", word -> measure(word) == 1 && endsCvc(word))
    );
    private static final List<Character> LSZ = List.of('l', 's', 'z');
    /**
     * Implements Step 1b from "An algorithm for suffix stripping"
     * <p>
     *         From the paper:
     * <p>
     *             (m>0) EED -> EE                    feed      ->  feed
     *                                                agreed    ->  agree
     *             (*v*) ED  ->                       plastered ->  plaster
     *                                                bled      ->  bled
     *             (*v*) ING ->                       motoring  ->  motor
     *                                                sing      ->  sing
     * <p>
     *         If the second or third of the rules in Step 1b is successful,
     *         the following is done:
     * <p>
     *             AT -> ATE                       conflat(ed)  ->  conflate
     *             BL -> BLE                       troubl(ed)   ->  trouble
     *             IZ -> IZE                       siz(ed)      ->  size
     *             (*d and not (*L or *S or *Z))
     *                -> single letter
     *                                             hopp(ing)    ->  hop
     *                                             tann(ed)     ->  tan
     *                                             fall(ing)    ->  fall
     *                                             hiss(ing)    ->  hiss
     *                                             fizz(ed)     ->  fizz
     *             (m=1 and *o) -> E               fail(ing)    ->  fail
     *                                             fil(ing)     ->  file
     * <p>
     *         The rule to map to a single letter causes the removal of one of
     *         the double letter pair. The -E is put back on -AT, -BL and -IZ,
     *         so that the suffixes -ATE, -BLE and -IZE can be recognised
     *         later. This E may be removed in step 4.
     * @param word The word to apply the rules to
     * @return The word after apply rule 1b
     */
    private static String rule1b(String word) {
        // NLTK Extension
        if (word.endsWith("ied")) {
            return replaceSuffix(word, "ied", word.length() == 4 ? "ie" : "i");
        }
        if (word.endsWith("eed")) {
            String stem = replaceSuffix(word, "eed", "");
            if (measure(stem) > 0) {
                return stem + "ee";
            } else {
                return word;
            }
        }
        for (String suffix : INTERMEDIATE_1B) {
            if (word.endsWith(suffix)) {
                String intermediateStem = replaceSuffix(word, suffix, "");
                if (hasVowel(intermediateStem)) {
                    // Do a copy and insert our own new rule
                    List<SuffixRule> suffixRules = new ArrayList<>(RULES_1B);
                    suffixRules.add(new SuffixRule("*d", intermediateStem.charAt(intermediateStem.length() - 1) + "",
                            (w) -> !LSZ.contains(w.charAt(w.length() - 1))));
                    return applyRules(intermediateStem, suffixRules);
                }
            }
        }
        return word;
    }

    private static final List<SuffixRule> RULES_1C = List.of(
            new SuffixRule("y", "i", stem -> stem.length() > 1 && isConsonant(stem, stem.length() - 1))
    );
    /**
     * Implements Step 1c from "An algorithm for suffix stripping"
     * <p>
     *         From the paper:
     * <p>
     *         Step 1c
     * <p>
     *             (*v*) Y -> I                    happy        ->  happi
     *                                             sky          ->  sky
     * @param word The word to apply the rules to
     * @return The word after apply rule 1c
     */
    private static String rule1c(String word) {
        // we use NLTK extended rule here
        return applyRules(word, RULES_1C);
    }
    private static String safeSubstring(String word, int start, int end) {
        if (start > end) {
            return "";
        } else {
            return word.substring(start, end);
        }
    }
    private static final List<SuffixRule> RULES_2 = new ArrayList<>();
    static {
        // as much as I hate these blocks...
        RULES_2.add(new SuffixRule("ational", "ate", NltkPorter::hasPositiveMeasure));
        RULES_2.add(new SuffixRule("tional", "tion", NltkPorter::hasPositiveMeasure));
        RULES_2.add(new SuffixRule("enci", "ence", NltkPorter::hasPositiveMeasure));
        RULES_2.add(new SuffixRule("anci", "ance", NltkPorter::hasPositiveMeasure));
        RULES_2.add(new SuffixRule("izer", "ize", NltkPorter::hasPositiveMeasure));
        RULES_2.add(new SuffixRule("bli", "ble", NltkPorter::hasPositiveMeasure));
        RULES_2.add(new SuffixRule("alli", "al", NltkPorter::hasPositiveMeasure));
        RULES_2.add(new SuffixRule("entli", "ent", NltkPorter::hasPositiveMeasure));
        RULES_2.add(new SuffixRule("eli", "e", NltkPorter::hasPositiveMeasure));
        RULES_2.add(new SuffixRule("ousli", "ous", NltkPorter::hasPositiveMeasure));
        RULES_2.add(new SuffixRule("ization", "ize", NltkPorter::hasPositiveMeasure));
        RULES_2.add(new SuffixRule("ation", "ate", NltkPorter::hasPositiveMeasure));
        RULES_2.add(new SuffixRule("ator", "ate", NltkPorter::hasPositiveMeasure));
        RULES_2.add(new SuffixRule("alism", "al", NltkPorter::hasPositiveMeasure));
        RULES_2.add(new SuffixRule("iveness", "ive", NltkPorter::hasPositiveMeasure));
        RULES_2.add(new SuffixRule("fulness", "ful", NltkPorter::hasPositiveMeasure));
        RULES_2.add(new SuffixRule("ousness", "ous", NltkPorter::hasPositiveMeasure));
        RULES_2.add(new SuffixRule("aliti", "al", NltkPorter::hasPositiveMeasure));
        RULES_2.add(new SuffixRule("iviti", "ive", NltkPorter::hasPositiveMeasure));
        RULES_2.add(new SuffixRule("biliti", "ble", NltkPorter::hasPositiveMeasure));
        RULES_2.add(new SuffixRule("logi", "log", word -> hasPositiveMeasure(safeSubstring(word, 0, word.length() - 3))));
    }
    /**
     * Implements Step 2 from "An algorithm for suffix stripping"
     * <p>
     *         From the paper:
     * <p>
     *         Step 2
     * <p>
     *             (m>0) ATIONAL ->  ATE       relational     ->  relate
     *             (m>0) TIONAL  ->  TION      conditional    ->  condition
     *                                         rational       ->  rational
     *             (m>0) ENCI    ->  ENCE      valenci        ->  valence
     *             (m>0) ANCI    ->  ANCE      hesitanci      ->  hesitance
     *             (m>0) IZER    ->  IZE       digitizer      ->  digitize
     *             (m>0) ABLI    ->  ABLE      conformabli    ->  conformable
     *             (m>0) ALLI    ->  AL        radicalli      ->  radical
     *             (m>0) ENTLI   ->  ENT       differentli    ->  different
     *             (m>0) ELI     ->  E         vileli        - >  vile
     *             (m>0) OUSLI   ->  OUS       analogousli    ->  analogous
     *             (m>0) IZATION ->  IZE       vietnamization ->  vietnamize
     *             (m>0) ATION   ->  ATE       predication    ->  predicate
     *             (m>0) ATOR    ->  ATE       operator       ->  operate
     *             (m>0) ALISM   ->  AL        feudalism      ->  feudal
     *             (m>0) IVENESS ->  IVE       decisiveness   ->  decisive
     *             (m>0) FULNESS ->  FUL       hopefulness    ->  hopeful
     *             (m>0) OUSNESS ->  OUS       callousness    ->  callous
     *             (m>0) ALITI   ->  AL        formaliti      ->  formal
     *             (m>0) IVITI   ->  IVE       sensitiviti    ->  sensitive
     *             (m>0) BILITI  ->  BLE       sensibiliti    ->  sensible
     * @param word The word to apply the rules to
     * @return The word after apply rule 2
     */
    private static String rule2(String word) {
        // another NLTK extension
        if (word.endsWith("alli") && hasPositiveMeasure(replaceSuffix(word, "alli", ""))) {
            return rule2(replaceSuffix(word, "alli", ""));
        }
        return applyRules(word, RULES_2);
    }
    private static final List<SuffixRule> RULES_3 = List.of(
            new SuffixRule("icate", "ic", NltkPorter::hasPositiveMeasure),
            new SuffixRule("ative", "", NltkPorter::hasPositiveMeasure),
            new SuffixRule("alize", "al", NltkPorter::hasPositiveMeasure),
            new SuffixRule("iciti", "ic", NltkPorter::hasPositiveMeasure),
            new SuffixRule("ical", "ic", NltkPorter::hasPositiveMeasure),
            new SuffixRule("ful", "", NltkPorter::hasPositiveMeasure),
            new SuffixRule("ness", "", NltkPorter::hasPositiveMeasure)
    );

    /**
     * Implements Step 3 from "An algorithm for suffix stripping"
     * <p>
     *         From the paper:
     * <p>
     *         Step 3
     * <p>
     *             (m>0) ICATE ->  IC              triplicate     ->  triplic
     *             (m>0) ATIVE ->                  formative      ->  form
     *             (m>0) ALIZE ->  AL              formalize      ->  formal
     *             (m>0) ICITI ->  IC              electriciti    ->  electric
     *             (m>0) ICAL  ->  IC              electrical     ->  electric
     *             (m>0) FUL   ->                  hopeful        ->  hope
     *             (m>0) NESS  ->                  goodness       ->  good
     * @param word The word to apply the rules to
     * @return The word after apply rule 3
     */
    private static String rule3(String word) {
        return applyRules(word, RULES_3);
    }
    private static final Predicate<String> MEASURE_GT_ONE = (str) -> measure(str) > 1;
    private static final List<Character> ST = List.of('s', 't');
    private static final List<SuffixRule> RULES_4 = new ArrayList<>();
    static {
        // Hated block, round 2
        RULES_4.add(new SuffixRule("al", "", MEASURE_GT_ONE));
        RULES_4.add(new SuffixRule("ance", "", MEASURE_GT_ONE));
        RULES_4.add(new SuffixRule("ence", "", MEASURE_GT_ONE));
        RULES_4.add(new SuffixRule("er", "", MEASURE_GT_ONE));
        RULES_4.add(new SuffixRule("ic", "", MEASURE_GT_ONE));
        RULES_4.add(new SuffixRule("able", "", MEASURE_GT_ONE));
        RULES_4.add(new SuffixRule("ible", "", MEASURE_GT_ONE));
        RULES_4.add(new SuffixRule("ant", "", MEASURE_GT_ONE));
        RULES_4.add(new SuffixRule("ement", "", MEASURE_GT_ONE));
        RULES_4.add(new SuffixRule("ment", "", MEASURE_GT_ONE));
        RULES_4.add(new SuffixRule("ent", "", MEASURE_GT_ONE));
        RULES_4.add(new SuffixRule("ou", "", MEASURE_GT_ONE));
        RULES_4.add(new SuffixRule("ism", "", MEASURE_GT_ONE));
        RULES_4.add(new SuffixRule("ate", "", MEASURE_GT_ONE));
        RULES_4.add(new SuffixRule("iti", "", MEASURE_GT_ONE));
        RULES_4.add(new SuffixRule("ous", "", MEASURE_GT_ONE));
        RULES_4.add(new SuffixRule("ive", "", MEASURE_GT_ONE));
        RULES_4.add(new SuffixRule("ize", "", MEASURE_GT_ONE));
        RULES_4.add(new SuffixRule("ion", "", MEASURE_GT_ONE
                .and((str) -> ST.contains(str.charAt(str.length() - 1)))));
    }

    /**
     * Implements Step 4 from "An algorithm for suffix stripping"
     * <p>
     *         Step 4
     * <p>
     *             (m>1) AL    ->                  revival        ->  reviv
     *             (m>1) ANCE  ->                  allowance      ->  allow
     *             (m>1) ENCE  ->                  inference      ->  infer
     *             (m>1) ER    ->                  airliner       ->  airlin
     *             (m>1) IC    ->                  gyroscopic     ->  gyroscop
     *             (m>1) ABLE  ->                  adjustable     ->  adjust
     *             (m>1) IBLE  ->                  defensible     ->  defens
     *             (m>1) ANT   ->                  irritant       ->  irrit
     *             (m>1) EMENT ->                  replacement    ->  replac
     *             (m>1) MENT  ->                  adjustment     ->  adjust
     *             (m>1) ENT   ->                  dependent      ->  depend
     *             (m>1 and (*S or *T)) ION ->     adoption       ->  adopt
     *             (m>1) OU    ->                  homologou      ->  homolog
     *             (m>1) ISM   ->                  communism      ->  commun
     *             (m>1) ATE   ->                  activate       ->  activ
     *             (m>1) ITI   ->                  angulariti     ->  angular
     *             (m>1) OUS   ->                  homologous     ->  homolog
     *             (m>1) IVE   ->                  effective      ->  effect
     *             (m>1) IZE   ->                  bowdlerize     ->  bowdler
     * <p>
     *         The suffixes are now removed. All that remains is a little
     *         tidying up.
     * @param word The word to apply the rules to
     * @return The word after apply rule 4
     */
    private static String rule4(String word) {
        return applyRules(word, RULES_4);
    }

    /**
     * Implements Step 5b from "An algorithm for suffix stripping"
     * <p>
     *         From the paper:
     * <p>
     *         Step 5a
     * <p>
     *             (m>1) E     ->                  probate        ->  probat
     *                                             rate           ->  rate
     *             (m=1 and not *o) E ->           cease          ->  ceas
     * @param word The word to apply the rules to
     * @return The word after apply rule 5a
     */
    private static String rule5a(String word) {
        if (word.endsWith("e")) {
            String stem = replaceSuffix(word, "e", "");
            if (measure(stem) > 1 || (measure(stem) > 1 && !endsCvc(stem))) {
                return stem;
            }
        }
        return word;
    }

    private static final List<SuffixRule> RULES_5B = List.of(
            new SuffixRule("ll", "l", word -> measure(word.substring(0, word.length() - 1)) > 1)
    );
    /**
     * Implements Step 5a from "An algorithm for suffix stripping"
     * <p>
     *         From the paper:
     * <p>
     *         Step 5b
     * <p>
     *             (m > 1 and *d and *L) -> single letter
     *                                     controll       ->  control
     *                                     roll           ->  roll
     * @param word The word to apply the rules to
     * @return The word after apply rule 5b
     */
    private static String rule5b(String word) {
        return applyRules(word, RULES_5B);
    }

    /**
     * Stems the given word.
     * @param word The word to stem
     * @return The stemmed word
     */
    public static String stem(String word) {
        String lower = word.toLowerCase();
        if (lower.length() <= 2) {
            return lower;
        }
        if (IRREGULAR_FORMS.containsKey(lower)) {
            return IRREGULAR_FORMS.get(lower);
        }
        return rule5b(rule5a(rule4(rule3(rule2(rule1c(rule1b(rule1a(lower))))))));
    }
}

