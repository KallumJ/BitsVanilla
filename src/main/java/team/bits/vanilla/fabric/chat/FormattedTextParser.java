package team.bits.vanilla.fabric.chat;

import java.util.*;

public class FormattedTextParser {

    private final String rawText;

    public FormattedTextParser(String rawText) {
        this.rawText = rawText;
    }

    public Stack<FormattedBlock> parse() {
        // the parsing process consists of two steps, first we parse
        // the string into blocks, then we combine blocks to create
        // formatted strings.
        Stack<ParsedBlock> parsedBlocks = this.parseBlocks();
        return this.formatBlocks(parsedBlocks);
    }

    /**
     * Parses the raw string into blocks. A block is either
     * a string of text, or a format specifier.
     * <p>
     * For example, the string "*test*"
     * would be parsed into three blocks:
     * - ITALIC
     * - "test"
     * - ITALIC
     */
    private Stack<ParsedBlock> parseBlocks() {
        Stack<ParsedBlock> parsingStack = new Stack<>();
        parsingStack.push(new TextBlock(new StringBuilder()));
        // we loop over all characters in the string. if the character
        // is a format specifier, we push that format specifier onto the
        // stack. otherwise, we append the character to the topmost text
        // block on the stack.
        for (char character : this.rawText.toCharArray()) {
            ParsedBlock lastBlock = parsingStack.peek();
            switch (character) {
                case '*':
                    // bold is just two italic specifiers next to each other
                    if (lastBlock == FormatType.ITALIC) {
                        parsingStack.pop();
                        parsingStack.push(FormatType.BOLD);
                    } else {
                        parsingStack.push(FormatType.ITALIC);
                    }
                    break;
                case '_':
                    parsingStack.push(FormatType.UNDERLINED);
                    break;
                case '~':
                    parsingStack.push(FormatType.STRIKETHROUGH);
                    break;
                default:
                    if (lastBlock instanceof TextBlock lastText) {
                        lastText.text().append(character);
                    } else {
                        // if the topmost block is not a text block, push
                        // a new text block to the stack
                        parsingStack.push(new TextBlock(new StringBuilder(String.valueOf(character))));
                    }
                    break;
            }
        }
        return parsingStack;
    }

    /**
     * Format the parsed blocks into formatted blocks.
     * <p>
     * For example, the blocks
     * - ITALIC
     * - "test"
     * - ITALIC
     * would be formatted in one:
     * {ITALIC, "test"}
     */
    private Stack<FormattedBlock> formatBlocks(Stack<ParsedBlock> blocks) {
        Stack<FormattedBlock> formattedStack = new Stack<>();
        while (!blocks.isEmpty()) {
            ParsedBlock block = blocks.remove(0);
            // if the block is a format specifier, look for a matching
            // closing format specifier.
            if (block instanceof FormatType type) {

                // try to find a closing format specifier matching this one
                boolean foundClosing = false;
                for (ParsedBlock next : blocks) {
                    if (next == type) {
                        foundClosing = true;
                        break;
                    }
                }

                // if we found a closing format specifier, format the entire block in between
                if (foundClosing) {
                    StringBuilder content = new StringBuilder();
                    ParsedBlock next;
                    // concatenate all the content between the starting and closing format specifiers
                    while ((next = blocks.remove(0)) != type && !blocks.isEmpty()) {
                        content.append(next.string());
                    }
                    formattedStack.push(new FormattedBlock(type, content.toString()));

                } else {
                    // if we can't find a closing format specifier, wrap the raw character that
                    // make up this format specifier into a PLAIN block
                    formattedStack.push(new FormattedBlock(FormatType.PLAIN, type.string()));
                }
            } else if (block instanceof TextBlock text) {
                // any text outside of format specifiers gets wrapped in a PLAIN block
                formattedStack.push(new FormattedBlock(FormatType.PLAIN, text.text().toString()));
            }
        }
        return formattedStack;
    }

    public record FormattedBlock(FormatType type, String text) {
    }

    public interface ParsedBlock {
        String string();
    }

    public record TextBlock(StringBuilder text) implements ParsedBlock {
        @Override
        public String string() {
            return text().toString();
        }
    }

    public enum FormatType implements ParsedBlock {
        PLAIN(""),
        ITALIC("*"),
        BOLD("**"),
        UNDERLINED("_"),
        STRIKETHROUGH("~");

        private final String value;

        FormatType(String value) {
            this.value = value;
        }

        @Override
        public String string() {
            return this.value;
        }
    }
}
