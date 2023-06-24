package com.example.cube.emoji;

import com.example.cube.models.EmojiMany;
import com.example.cube.R;


import java.util.ArrayList;
import java.util.List;

public class MyEmoji {
    //site where convert svg files to xml vector file https://svg2vector.com/
    static List<int[]> list = new ArrayList<>();
    static List<EmojiMany> manyReaction = new ArrayList<>();

    static public List<EmojiMany> getManyReaction() {
        manyReaction.clear();
        manyReaction.add(new EmojiMany(emoji));
        manyReaction.add(new EmojiMany(emoji_blue));
        manyReaction.add(new EmojiMany(flag));
        manyReaction.add(new EmojiMany(animals));
        return manyReaction;
    }

    public static List<int[]> getInstance() {
        list.add(emoji);
        list.add(emoji_blue);
        list.add(flag);
        list.add(animals);
        return list;
    }



    public static int emoji_blue[] = new int[]{
            R.drawable.emoji_a_blush_shy, R.drawable.emoji_a_laugh_lol_smiley, R.drawable.emoji_a_happy_smiley,
            R.drawable.emoji_a_cool_smiley_sunglasses, R.drawable.emoji_a_emoticons_tongue, R.drawable.emoji_a_kiss_smiley,
            R.drawable.emoji_a_heart_love_sexy, R.drawable.emoji_a_heart_love_smiley, R.drawable.emoji_a_angry_pissed,
            R.drawable.emoji_a_angry_sad_unhappy, R.drawable.emoji_a_confused_face, R.drawable.emoji_a_crying_sad,
            R.drawable.emoji_a_fear_scared, R.drawable.emoji_a_shock_shocked_smiley, R.drawable.emoji_a_think_thinking,
            R.drawable.emoji_a_sleep_sleeping, R.drawable.emoji_a_head_zombie, R.drawable.emoji_a_puke_sick

    };
    public static int[] emoji = new int[]{
            R.drawable.emoji_1f927, R.drawable.emoji_605, R.drawable.emoji_60e,
            R.drawable.emoji_607, R.drawable.emoji_610, R.drawable.emoji_61a,
            R.drawable.emoji_608a, R.drawable.emoji_611, R.drawable.emoji_61b,
            R.drawable.emoji_608b, R.drawable.emoji_612, R.drawable.emoji_61c,
            R.drawable.emoji_609, R.drawable.emoji_613, R.drawable.emoji_61d,
            R.drawable.emoji_60a, R.drawable.emoji_614, R.drawable.emoji_61e,
            R.drawable.emoji_60b, R.drawable.emoji_615, R.drawable.emoji_620,
            R.drawable.emoji_60c, R.drawable.emoji_616, R.drawable.emoji_621,
            R.drawable.emoji_60d, R.drawable.emoji_617, R.drawable.emoji_622,
            R.drawable.emoji_60f, R.drawable.emoji_619, R.drawable.emoji_624,
            R.drawable.emoji_618, R.drawable.emoji_623, R.drawable.emoji_62d,
            R.drawable.emoji_625, R.drawable.emoji_62e, R.drawable.emoji_636,
            R.drawable.emoji_626, R.drawable.emoji_62f, R.drawable.emoji_637,
            R.drawable.emoji_627, R.drawable.emoji_630, R.drawable.emoji_63a,
            R.drawable.emoji_628, R.drawable.emoji_631, R.drawable.emoji_63b,
            R.drawable.emoji_629, R.drawable.emoji_632, R.drawable.emoji_63c,
            R.drawable.emoji_62a, R.drawable.emoji_633, R.drawable.emoji_63d,
            R.drawable.emoji_62b, R.drawable.emoji_634, R.drawable.emoji_63e,
            R.drawable.emoji_62c, R.drawable.emoji_635, R.drawable.emoji_63f,
            R.drawable.emoji_62e, R.drawable.emoji_636, R.drawable.emoji_63h,
            R.drawable.emoji_635, R.drawable.emoji_63g, R.drawable.emoji_6,
            R.drawable.emoji_63i, R.drawable.emoji_911, R.drawable.emoji_923,
            R.drawable.emoji_641, R.drawable.emoji_912, R.drawable.emoji_924,
            R.drawable.emoji_642, R.drawable.emoji_913, R.drawable.emoji_925,
            R.drawable.emoji_643, R.drawable.emoji_914, R.drawable.emoji_92a,
            R.drawable.emoji_644, R.drawable.emoji_915, R.drawable.emoji_92b,
            R.drawable.emoji_64a, R.drawable.emoji_916, R.drawable.emoji_92c,
            R.drawable.emoji_64b, R.drawable.emoji_917, R.drawable.emoji_92d,
            R.drawable.emoji_64c, R.drawable.emoji_920, R.drawable.emoji_92e,
            R.drawable.emoji_910, R.drawable.emoji_922, R.drawable.emoji_970,
            R.drawable.emoji_921, R.drawable.emoji_92f, R.drawable.emoji_97a,
            R.drawable.emoji_971, R.drawable.emoji_ae10, R.drawable.emoji_2639,
            R.drawable.emoji_972, R.drawable.emoji_ae11, R.drawable.emoji_263a,
            R.drawable.emoji_973, R.drawable.emoji_ae1, R.drawable.emoji_600,
            R.drawable.emoji_974, R.drawable.emoji_ae2, R.drawable.emoji_601,
            R.drawable.emoji_975, R.drawable.emoji_ae3, R.drawable.emoji_602,
            R.drawable.emoji_976, R.drawable.emoji_ae4, R.drawable.emoji_603,
            R.drawable.emoji_978, R.drawable.emoji_ae6, R.drawable.emoji_604,
            R.drawable.emoji_979, R.drawable.emoji_ae7, R.drawable.emoji_ae0,
            R.drawable.emoji_ae9, R.drawable.emoji_1f929, R.drawable.emoji_ae8,
            R.drawable.emoji_1f928, R.drawable.emoji_606
    };
    public static int animals[] = new int[]{
            R.drawable.animal_1f400, R.drawable.animal_1f41d, R.drawable.animal_1f43c, R.drawable.animal_1f999,
            R.drawable.animal_1f401, R.drawable.animal_1f41e, R.drawable.animal_1f43d, R.drawable.animal_1f99a,
            R.drawable.animal_1f402, R.drawable.animal_1f420, R.drawable.animal_1f43e, R.drawable.animal_1f99b,
            R.drawable.animal_1f403, R.drawable.animal_1f421, R.drawable.animal_1f43f, R.drawable.animal_1f99c,
            R.drawable.animal_1f404, R.drawable.animal_1f422, R.drawable.animal_1f4, R.drawable.animal_1f99d,
            R.drawable.animal_1f405, R.drawable.animal_1f423, R.drawable.animal_1f577, R.drawable.animal_1f99e,
            R.drawable.animal_1f406, R.drawable.animal_1f424, R.drawable.animal_1f980, R.drawable.animal_1f99f,
            R.drawable.animal_1f407, R.drawable.animal_1f425, R.drawable.animal_1f981, R.drawable.animal_1f9a0,
            R.drawable.animal_1f408a, R.drawable.animal_1f426, R.drawable.animal_1f982, R.drawable.animal_1f9a1,
            R.drawable.animal_1f408b, R.drawable.animal_1f427, R.drawable.animal_1f983, R.drawable.animal_1f9a2,
            R.drawable.animal_1f409, R.drawable.animal_1f428, R.drawable.animal_1f984, R.drawable.animal_1f9a3,
            R.drawable.animal_1f40a, R.drawable.animal_1f429, R.drawable.animal_1f985, R.drawable.animal_1f9a4,
            R.drawable.animal_1f40b, R.drawable.animal_1f42a, R.drawable.animal_1f986, R.drawable.animal_1f9a5,
            R.drawable.animal_1f40c, R.drawable.animal_1f42b, R.drawable.animal_1f987, R.drawable.animal_1f9a6,
            R.drawable.animal_1f40d, R.drawable.animal_1f42c, R.drawable.animal_1f988, R.drawable.animal_1f9a7,
            R.drawable.animal_1f40e, R.drawable.animal_1f42d, R.drawable.animal_1f989, R.drawable.animal_1f9a8,
            R.drawable.animal_1f40f, R.drawable.animal_1f42e, R.drawable.animal_1f98a, R.drawable.animal_1f9a9,
            R.drawable.animal_1f410, R.drawable.animal_1f42f, R.drawable.animal_1f98b, R.drawable.animal_1f9aa,
            R.drawable.animal_1f411, R.drawable.animal_1f430, R.drawable.animal_1f98c, R.drawable.animal_1f9ab,
            R.drawable.animal_1f412, R.drawable.animal_1f431, R.drawable.animal_1f98d, R.drawable.animal_1f9ac,
            R.drawable.animal_1f413, R.drawable.animal_1f432, R.drawable.animal_1f98e, R.drawable.animal_1f9ad,
            R.drawable.animal_1f414, R.drawable.animal_1f433, R.drawable.animal_1f98f, R.drawable.animal_1f9ae,
            R.drawable.animal_1f415a, R.drawable.animal_1f434, R.drawable.animal_1f990, R.drawable.animal_1fab0,
            R.drawable.animal_1f415b, R.drawable.animal_1f435, R.drawable.animal_1f991, R.drawable.animal_1fab1,
            R.drawable.animal_1f416, R.drawable.animal_1f436, R.drawable.animal_1f992, R.drawable.animal_1fab2,
            R.drawable.animal_1f417, R.drawable.animal_1f437, R.drawable.animal_1f993, R.drawable.animal_1fab3,
            R.drawable.animal_1f418, R.drawable.animal_1f438, R.drawable.animal_1f994, R.drawable.animal_1f419,
            R.drawable.animal_1f41b, R.drawable.animal_1f43bc, R.drawable.animal_1f997, R.drawable.animal_1f41a,
            R.drawable.animal_1f41c, R.drawable.animal_1f43b, R.drawable.animal_1f998, R.drawable.animal_1f43a,
            R.drawable.animal_1f439, R.drawable.animal_1f995, R.drawable.animal_1f996

    };
    public static int flag[] = new int[]{
            R.drawable.flaga1007f, R.drawable.flaga1fe0f, R.drawable.flagb1f1f7, R.drawable.flagc1f1f5, R.drawable.flage1f1ed, R.drawable.flagg1f1e7, R.drawable.flagh1f1f7, R.drawable.flagk1f1f7, R.drawable.flago1f1f0, R.drawable.flagp1f1ee, R.drawable.flagq1f1fe, R.drawable.flags1f1f8, R.drawable.flagt1f1fc,
            R.drawable.flaga1e0f, R.drawable.flagb1007f, R.drawable.flagb1f1f8, R.drawable.flagc1f1f7, R.drawable.flage1f1f7, R.drawable.flagg1f1e9, R.drawable.flagh1f1f9, R.drawable.flagk1f1f8, R.drawable.flago1f1f1, R.drawable.flagp1f1f1, R.drawable.flagr1f1ea, R.drawable.flags1f1f9, R.drawable.flagt1f1ff,
            R.drawable.flaga1f1e6, R.drawable.flagb10, R.drawable.flagb1f1f9, R.drawable.flagc1f1f9, R.drawable.flage1f1f8, R.drawable.flagg1f1ea, R.drawable.flagh1f1fa, R.drawable.flagk1f1f9, R.drawable.flago1f1f2, R.drawable.flagp1f1f2, R.drawable.flagr1f1f4, R.drawable.flags1f1fb, R.drawable.flagu1f1e6,
            R.drawable.flaga1f1e8, R.drawable.flagb12, R.drawable.flagb1f1fb, R.drawable.flagc1f1fa, R.drawable.flage1f1f9, R.drawable.flagg1f1eb, R.drawable.flagi1f1e6, R.drawable.flagm1f1ea, R.drawable.flago1f1f3, R.drawable.flagp1f1f4, R.drawable.flagr1f1f8, R.drawable.flags1f1fd, R.drawable.flagu1f1ec,
            R.drawable.flaga1f1e9, R.drawable.flagb14, R.drawable.flagb1f1fc, R.drawable.flagc1f1fb, R.drawable.flage1f1fa, R.drawable.flagg1f1ec, R.drawable.flagi1f1e8, R.drawable.flagm1f1ec, R.drawable.flago1f1f4, R.drawable.flagp1f1f5, R.drawable.flagr1f1fa, R.drawable.flags1f1fe, R.drawable.flagu1f1f2,
            R.drawable.flaga1f1ea, R.drawable.flagb17, R.drawable.flagb1f1fe, R.drawable.flagc1f1fc, R.drawable.flagf1e6, R.drawable.flagg1f1ed, R.drawable.flagi1f1ea, R.drawable.flagm1f1ed, R.drawable.flago1f1f5, R.drawable.flagp1f1f7, R.drawable.flagr1f1fc, R.drawable.flags1f1ff, R.drawable.flagu1f1f3,
            R.drawable.flaga1f1eb, R.drawable.flagb1e, R.drawable.flagb1f1ff, R.drawable.flagc1f1fd, R.drawable.flagf1e7, R.drawable.flagg1f1ee, R.drawable.flagi1f1eb, R.drawable.flagm1f1ee, R.drawable.flago1f1f6, R.drawable.flagp1f1fa, R.drawable.flags1f1e6, R.drawable.flagt1f1e6, R.drawable.flagu1f1f8,
            R.drawable.flaga1f1ec, R.drawable.flagb1f1e6, R.drawable.flagb1f, R.drawable.flagc1f1fe, R.drawable.flagf1e8, R.drawable.flagg1f1f1, R.drawable.flagi1f1ec, R.drawable.flagm1f1f2, R.drawable.flago1f1f7, R.drawable.flagp1f1ff, R.drawable.flags1f1e7, R.drawable.flagt1f1e8, R.drawable.flagu1f1fe,
            R.drawable.flaga1f1ee, R.drawable.flagb1f1e7, R.drawable.flagc1f1e6, R.drawable.flagc1f1ff, R.drawable.flagf1ea, R.drawable.flagg1f1f2, R.drawable.flagi1f1ee, R.drawable.flagm1f1f3, R.drawable.flago1f1f8, R.drawable.flagq1f1e6, R.drawable.flags1f1e8, R.drawable.flagt1f1e9, R.drawable.flagu1f1ff,
            R.drawable.flaga1f1f1, R.drawable.flagb1f1e9, R.drawable.flagc1f1e8, R.drawable.flagd1f1ea, R.drawable.flagf1ee, R.drawable.flagg1f1f3, R.drawable.flagi1f1f0, R.drawable.flagm1f1f5, R.drawable.flago1f1f9, R.drawable.flagq1f1ea, R.drawable.flags1f1e9, R.drawable.flagt1f1eb, R.drawable.flagv1f1e6,
            R.drawable.flaga1f1f2, R.drawable.flagb1f1ea, R.drawable.flagc1f1e9, R.drawable.flagd1f1ec, R.drawable.flagf1f0, R.drawable.flagg1f1f5, R.drawable.flagi1f1f3, R.drawable.flagm1f1f7, R.drawable.flago1f1fa, R.drawable.flagq1f1eb, R.drawable.flags1f1ea, R.drawable.flagt1f1ec, R.drawable.flagv1f1f2,
            R.drawable.flaga1f1f4, R.drawable.flagb1f1eb, R.drawable.flagc1f1ea, R.drawable.flagd1f1ef, R.drawable.flagf1f2, R.drawable.flagg1f1f6, R.drawable.flagi1f1f8, R.drawable.flagm1f1fc, R.drawable.flago1f1fb, R.drawable.flagq1f1ec, R.drawable.flags1f1ec, R.drawable.flagt1f1ed, R.drawable.flagv1f1fc,
            R.drawable.flaga1f1f6, R.drawable.flagb1f1ec, R.drawable.flagc1f1eb, R.drawable.flagd1f1f0, R.drawable.flagf1f4, R.drawable.flagg1f1f7, R.drawable.flagi1f1fa, R.drawable.flagm1f1fe, R.drawable.flago1f1fc, R.drawable.flagq1f1ed, R.drawable.flags1f1ed, R.drawable.flagt1f1ef,
            R.drawable.flaga1f1f7, R.drawable.flagb1f1ed, R.drawable.flagc1f1ec, R.drawable.flagd1f1f2, R.drawable.flagf1f5, R.drawable.flagg1f1f8, R.drawable.flagk1f1e8, R.drawable.flagm1f1ff, R.drawable.flago1f1fd, R.drawable.flagq1f1f0, R.drawable.flags1f1ee, R.drawable.flagt1f1f0,
            R.drawable.flaga1f1f8, R.drawable.flagb1f1ee, R.drawable.flagc1f1ed, R.drawable.flagd1f1f4, R.drawable.flagf1f7, R.drawable.flagg1f1f9, R.drawable.flagk1f1e9, R.drawable.flago1f1e6, R.drawable.flago1f1fe, R.drawable.flagq1f1f1, R.drawable.flags1f1ef, R.drawable.flagt1f1f1,
            R.drawable.flaga1f1f9, R.drawable.flagb1f1ef, R.drawable.flagc1f1ee, R.drawable.flagd1f1ff, R.drawable.flagf1f8, R.drawable.flagg1f1fa, R.drawable.flagk1f1ea, R.drawable.flago1f1e8, R.drawable.flago1f1ff, R.drawable.flagq1f1f2, R.drawable.flags1f1f0, R.drawable.flagt1f1f2,
            R.drawable.flaga1f1fa, R.drawable.flagb1f1f1, R.drawable.flagc1f1f0, R.drawable.flage107f, R.drawable.flagf1f9, R.drawable.flagg1f1fc, R.drawable.flagk1f1f1, R.drawable.flago1f1e9, R.drawable.flagp1f1e6, R.drawable.flagq1f1f3, R.drawable.flags1f1f1, R.drawable.flagt1f1f3,
            R.drawable.flaga1f1fc, R.drawable.flagb1f1f2, R.drawable.flagc1f1f1, R.drawable.flage1f1e6, R.drawable.flagf1fa, R.drawable.flagg1f1fe, R.drawable.flagk1f1f2, R.drawable.flago1f1ea, R.drawable.flagp1f1e8, R.drawable.flagq1f1f7, R.drawable.flags1f1f2, R.drawable.flagt1f1f4,
            R.drawable.flaga1f1fd, R.drawable.flagb1f1f3, R.drawable.flagc1f1f2, R.drawable.flage1f1e8, R.drawable.flagf1fb, R.drawable.flagh1f1f0, R.drawable.flagk1f1f3, R.drawable.flago1f1eb, R.drawable.flagp1f1ea, R.drawable.flagq1f1f8, R.drawable.flags1f1f3, R.drawable.flagt1f1f7,
            R.drawable.flaga1f1ff, R.drawable.flagb1f1f4, R.drawable.flagc1f1f3, R.drawable.flage1f1ea, R.drawable.flagf1fe, R.drawable.flagh1f1f2, R.drawable.flagk1f1f4, R.drawable.flago1f1ec, R.drawable.flagp1f1eb, R.drawable.flagq1f1f9, R.drawable.flags1f1f4, R.drawable.flagt1f1f9,
            R.drawable.flaga1f308, R.drawable.flagb1f1f6, R.drawable.flagc1f1f4, R.drawable.flage1f1ec, R.drawable.flagg1f1e6, R.drawable.flagh1f1f3, R.drawable.flagk1f1f6, R.drawable.flago1f1ed, R.drawable.flagp1f1ec, R.drawable.flagq1f1fc, R.drawable.flags1f1f7, R.drawable.flagt1f1fb
    };
}
