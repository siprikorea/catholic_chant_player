package com.siprikorea.catholicchant.search

/**
 * 한글 음절/자모 유틸리티.
 *
 * 음절을 자모 단위로 분해하면 입력 중인 미완성 글자("주하ㄴ", "하는" → "하느님")나
 * 한 자모만 틀린 오타도 매칭할 수 있다. 겹받침·이중모음도 기본 자모로 풀어서
 * IME 조합 중간 상태("고" → "과")와 일치하도록 한다.
 */
internal object Hangul {
    private const val SYLLABLE_FIRST = 0xAC00
    private const val SYLLABLE_LAST = 0xD7A3
    private const val JUNG_COUNT = 21
    private const val JONG_COUNT = 28

    private const val CHO = "ㄱㄲㄴㄷㄸㄹㅁㅂㅃㅅㅆㅇㅈㅉㅊㅋㅌㅍㅎ"
    private val JUNG = arrayOf(
        "ㅏ", "ㅐ", "ㅑ", "ㅒ", "ㅓ", "ㅔ", "ㅕ", "ㅖ", "ㅗ", "ㅗㅏ", "ㅗㅐ",
        "ㅗㅣ", "ㅛ", "ㅜ", "ㅜㅓ", "ㅜㅔ", "ㅜㅣ", "ㅠ", "ㅡ", "ㅡㅣ", "ㅣ",
    )
    private val JONG = arrayOf(
        "", "ㄱ", "ㄲ", "ㄱㅅ", "ㄴ", "ㄴㅈ", "ㄴㅎ", "ㄷ", "ㄹ", "ㄹㄱ", "ㄹㅁ", "ㄹㅂ", "ㄹㅅ", "ㄹㅌ",
        "ㄹㅍ", "ㄹㅎ", "ㅁ", "ㅂ", "ㅂㅅ", "ㅅ", "ㅆ", "ㅇ", "ㅈ", "ㅊ", "ㅋ", "ㅌ", "ㅍ", "ㅎ",
    )

    /** 단독으로 입력된 겹자모(호환 자모)의 분해 */
    private val COMPAT_COMPOUND = mapOf(
        'ㄳ' to "ㄱㅅ", 'ㄵ' to "ㄴㅈ", 'ㄶ' to "ㄴㅎ", 'ㄺ' to "ㄹㄱ", 'ㄻ' to "ㄹㅁ", 'ㄼ' to "ㄹㅂ",
        'ㄽ' to "ㄹㅅ", 'ㄾ' to "ㄹㅌ", 'ㄿ' to "ㄹㅍ", 'ㅀ' to "ㄹㅎ", 'ㅄ' to "ㅂㅅ",
        'ㅘ' to "ㅗㅏ", 'ㅙ' to "ㅗㅐ", 'ㅚ' to "ㅗㅣ", 'ㅝ' to "ㅜㅓ", 'ㅞ' to "ㅜㅔ", 'ㅟ' to "ㅜㅣ", 'ㅢ' to "ㅡㅣ",
    )

    fun isSyllable(c: Char): Boolean = c.code in SYLLABLE_FIRST..SYLLABLE_LAST

    /** 호환 자모 자음(ㄱ~ㅎ) 여부 — 초성 검색 판별에 사용 */
    fun isConsonant(c: Char): Boolean = c in 'ㄱ'..'ㅎ'

    /** 음절의 초성. 한글 음절이 아니면 그대로 반환한다. */
    fun choseong(c: Char): Char =
        if (isSyllable(c)) CHO[(c.code - SYLLABLE_FIRST) / (JUNG_COUNT * JONG_COUNT)] else c

    /** 한 글자를 기본 자모 시퀀스로 분해한다. 한글이 아니면 그 글자 하나를 반환한다. */
    fun decompose(c: Char): String {
        if (isSyllable(c)) {
            val offset = c.code - SYLLABLE_FIRST
            val cho = offset / (JUNG_COUNT * JONG_COUNT)
            val jung = offset % (JUNG_COUNT * JONG_COUNT) / JONG_COUNT
            val jong = offset % JONG_COUNT
            return CHO[cho] + JUNG[jung] + JONG[jong]
        }
        return COMPAT_COMPOUND[c] ?: c.toString()
    }
}
