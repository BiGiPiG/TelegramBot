
import io.github.bigpig.services.MessageService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MessageServiceTest {

    private final MessageService messageService = new MessageService();

    @Test
    void testEscapeMarkdown_withMarkdownSymbols() {
        String input = "This is _italic_, [link](url), and some *bold* text!";
        String expected = "This is \\_italic\\_, \\[link\\]\\(url\\), and some *bold* text\\!";
        String result = messageService.escapeMarkdownSymbols(input);
        assertEquals(expected, result);
    }

    @Test
    void testEscapeMarkdown_withNewLines() {
        String input = "Line1\\nLine2\\nLine3";
        String expected = "Line1\nLine2\nLine3";
        String result = messageService.escapeMarkdownSymbols(input);
        assertEquals(expected, result);
    }

    @Test
    void testEscapeMarkdown_ignoresDoubleAsterisk() {
        String input = "Some **bold** text";
        String expected = "Some *bold* text";
        String result = messageService.escapeMarkdownSymbols(input);
        assertEquals(expected, result);
    }

    @Test
    void testEscapeMarkdown_withAllSymbols() {
        String input = "_ [ ] ( ) ~ > # + - = | . ! :";
        String expected = "\\_ \\[ \\] \\( \\) \\~ \\> \\# \\+ \\- \\= \\| \\. \\! \\:";
        String result = messageService.escapeMarkdownSymbols(input);
        assertEquals(expected, result);
    }

    @Test
    void testEscapeMarkdown_emptyString() {
        String input = "";
        String result = messageService.escapeMarkdownSymbols(input);
        assertEquals("", result);
    }

    @Test
    void testEscapeMarkdown_noMarkdownSymbols() {
        String input = "Just a normal string without symbols";
        String result = messageService.escapeMarkdownSymbols(input);
        assertEquals(input, result);
    }
}