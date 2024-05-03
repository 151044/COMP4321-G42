package hk.ust.comp4321.se;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SearchVectorTest {

    @Test
    void cosineSim() {
        SearchVector doc = new SearchVector(List.of("rage", "against", "dying", "light"), List.of(0.2, 0.1, 0.1, 0.1));
        assertEquals(0, doc.cosineSim(new SearchVector("alpha-beta pruning is a technique")));
        assertNotEquals(0, doc.cosineSim(new SearchVector("dying light")));
        assertNotEquals(0, doc.cosineSim(new SearchVector("dying of the light")));
        assertEquals(doc.cosineSim(new SearchVector("dying light eye universe")),
                doc.cosineSim(new SearchVector("dying of the light")));
        assertTrue(doc.cosineSim(new SearchVector("dying light")) >
                doc.cosineSim(new SearchVector("dying of the light")));
        assertTrue(doc.cosineSim(new SearchVector("dying light")) >
                doc.cosineSim(new SearchVector("dying")));
    }

    @Test
    void getRequiredTerms() {
        assertEquals(List.of(), new SearchVector("alpha-beta pruning").getRequiredTerms());
        assertEquals(List.of(), new SearchVector("alpha-beta \"pruning").getRequiredTerms());
        assertEquals(List.of(List.of("alpha-beta", "prun")),
                new SearchVector("\"alpha-beta pruning\" is a technique").getRequiredTerms());
        assertEquals(List.of(List.of("pleasur", "form"), List.of("discourse")),
                new SearchVector("induces \"pleasure forms\" knowledge produces \"discourse\"").getRequiredTerms());
    }
}