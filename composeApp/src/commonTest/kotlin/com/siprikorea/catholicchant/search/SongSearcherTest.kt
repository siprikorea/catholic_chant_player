package com.siprikorea.catholicchant.search

import com.siprikorea.catholicchant.data.Song
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SongSearcherTest {
    private val songs = listOf(
        Song(1, "나는 믿나이다", firstLine = "나는 굳게 믿나이다"),
        Song(2, "주 하느님 크시도다", firstLine = "주하느님 지으신 모든 세계"),
        Song(12, "찬미 노래 부르며"),
        Song(120, "주님의 기도"),
        Song(151, "주여 임하소서"),
        Song(269, "마리아의 노래"),
        Song(528, "축하합니다"),
    )
    private val searcher = SongSearcher(songs)

    private fun numbers(query: String) = searcher.search(query).map { it.song.no }

    @Test
    fun emptyQueryReturnsAllInOrder() {
        assertEquals(songs.map { it.no }, numbers("  "))
    }

    @Test
    fun exactNumberComesFirstThenPrefix() {
        assertEquals(listOf(12, 120), numbers("12"))
        assertEquals(listOf(2, 269), numbers("2"))
        assertEquals(listOf(2, 269), numbers("02"))
        assertEquals(listOf(528), numbers("528번"))
    }

    @Test
    fun titleSubstringIgnoresSpaces() {
        assertEquals(2, numbers("주하느님").first())
        assertEquals(2, numbers("하느님 크시").first())
    }

    @Test
    fun incompleteSyllableWhileTyping() {
        // IME 조합 중: "하느님" 을 치는 도중 "하는" 상태
        assertEquals(2, numbers("하는").first())
        assertEquals(2, numbers("주하ㄴ").first())
    }

    @Test
    fun choseongSearch() {
        assertEquals(listOf(2), numbers("ㅈㅎㄴㄴ"))
        assertEquals(269, numbers("ㅁㄹㅇ").first())
    }

    @Test
    fun subsequenceSkipsCharacters() {
        assertTrue(151 in numbers("주임하"))
    }

    @Test
    fun toleratesTypo() {
        // "축하합니다" → "축하함니다" (받침 오타)
        assertEquals(528, numbers("축하함니다").first())
        assertEquals(269, numbers("마리야의").first())
    }

    @Test
    fun firstLineMatchesWithLowerPriority() {
        val results = searcher.search("굳게 믿")
        assertEquals(1, results.first().song.no)
        assertTrue(results.first().firstLineRanges.isNotEmpty())
    }

    @Test
    fun highlightRangesPointToOriginalText() {
        val result = searcher.search("하느님").first()
        assertEquals(listOf(2..4), result.titleRanges)
    }

    @Test
    fun noMatchReturnsEmpty() {
        assertTrue(numbers("xyz").isEmpty())
        assertTrue(numbers("999").isEmpty())
    }
}
