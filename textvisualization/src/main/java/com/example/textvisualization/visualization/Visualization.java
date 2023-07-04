package com.example.textvisualization.visualization;

import android.graphics.Color;

import java.util.regex.Pattern;

public class Visualization {


    final private TextColor WORDS = new TextColor(
            Pattern.compile(
                    "\\b(False|None|True|and|nonlocal|not|or|class|def|is|lambda)\\b"),
            Color.parseColor("#3e9cca")
    );

    final private TextColor NUMBERS = new TextColor(
            Pattern.compile("(\\b(\\d*[.]?\\d+)\\b)"),
            Color.parseColor("#2f5f93")
    );
    final private TextColor WORKING_METHODS_FIRST = new TextColor(
            Pattern.compile("\\b(read()|write()|tell()|seek()|close()|open()|closed|mode|name|softspace)\\b"),
                    Color.parseColor("#b3b102")
            );

    final private TextColor HASHTAG = new TextColor(
            Pattern.compile("\\B(\\#[a-zA-Z]+\\b)(?!;)"),
            Color.parseColor("#00b2ff")
    );
    final private TextColor HASHTAG_UA = new TextColor(
            Pattern.compile("\\B(\\#[а-яєiА-ЯЄІ]+\\b)(?!;)"),
            Color.parseColor("#00b2ff")
    );
    final private TextColor HASH_DOG = new TextColor(
            Pattern.compile("\\B(\\@[a-zA-Z]+\\b)(?!;)"),
            Color.parseColor("#00b2ff")
    );
    final private TextColor BRACKETS = new TextColor(
            Pattern.compile("[\\(\\)]"),
            Color.parseColor("#3e9cca")
    );
    final private TextColor SQUARE_BRACKETS = new TextColor(
            Pattern.compile("[\\[\\]]"),
            Color.parseColor("#3e9cca")
    );
    final private TextColor BRACES = new TextColor(
            Pattern.compile("[\\{\\}]"),
            Color.parseColor("#3e9cca")
    );

    /**
     * Регулятор для трех и менее ковычек
     */
    final private TextColor HTML_ENG = new TextColor(
            Pattern.compile("\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]"),
            Color.parseColor("#00b2ff")
    );
    /**
     * Регулятор для трех и менее ковычек
     */
    final private TextColor HTML_UA = new TextColor(
            Pattern.compile("\\b(https?|ftp|file)://[-а-яєiА-ЯЄІ0-9+&@#/%?=~_|!:,.;]*[-а-яєiА-ЯЄІ0-9+&@#/%=~_|]"),
            Color.parseColor("#00b2ff")
    );
    /**
     * регулятор для подсвтеки одиночных букв
     */
    private TextColor LETTERS = new TextColor(
            Pattern.compile("(\\b(q|w|e|r|t|y|u|i|o|p|a|s|d|f|g|h|j|k|l|z|x|c|v|b|n|m|Q|W|E|R|T|Y|U|I|O|P|A|S|D|F|G|H|J|K|L|Z|X|C|V|B|N|M)\\b)"),
            Color.parseColor("#648cb8")
    );
    private static Visualization visualization = new Visualization();
    static final TextColor[] colors = {
            visualization.WORDS,
            visualization.NUMBERS,
            visualization.LETTERS,
            visualization.WORKING_METHODS_FIRST,
            visualization.HASHTAG,
            visualization.HASHTAG_UA,
            visualization.HTML_ENG,
            visualization.HTML_UA,
            visualization.HASH_DOG,
            visualization.BRACKETS,
            visualization.SQUARE_BRACKETS,
            visualization.BRACES
    };

    public static TextColor[] getColors() {
        return colors;
    }

    public class TextColor {
        public final Pattern pattern;
        public final int color;

        TextColor(Pattern pattern, int color) {
            this.pattern = pattern;
            this.color = color;
        }
    }
}
