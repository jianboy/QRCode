package me.yoqi.android.utils;

import android.util.Patterns;

/**
 * 常用正则表达式
 *
 * @author liuyuqi.gov@msn.cn
 * @createTime 2020-08-17
 */
public class RegexParser {
    static final String PHONE_PATTERN = Patterns.PHONE.pattern();
    static final String EMAIL_PATTERN = Patterns.EMAIL_ADDRESS.pattern();

    //at、话题、链接匹配表达式
    static final String MENTION_PATTERN = "@.{1,15}?\\s";
    static final String HASHTAG_PATTERN = "#.{1,15}?\\s";
    static final String URL_PATTERN = "(http|https|ftp|svn)://([a-zA-Z0-9]+[/?.?])" +
            "+[a-zA-Z0-9]*\\??([a-zA-Z0-9]*=[a-zA-Z0-9]*&?)*";

}

