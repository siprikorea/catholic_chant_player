package com.siprikorea.catholicchant.search

import com.siprikorea.catholicchant.data.Song

data class SearchResult(
    val song: Song,
    val score: Int,
    /** 제목에서 일치한 글자 범위 (원문 인덱스) */
    val titleRanges: List<IntRange> = emptyList(),
    /** 첫 소절에서 일치한 글자 범위 (원문 인덱스) */
    val firstLineRanges: List<IntRange> = emptyList(),
    /** 작곡가에서 일치한 글자 범위 (원문 인덱스) */
    val composerRanges: List<IntRange> = emptyList(),
)

/**
 * 성가 번호/제목/첫 소절 퍼지 검색.
 *
 * 점수(높을수록 우선):
 *  - 번호 정확 일치 > 번호 앞자리 일치
 *  - 제목 접두 > 단어 시작 > 부분 문자열 (자모 단위라 입력 중인 글자도 일치)
 *  - 초성 일치 ("ㅈㅎㄴㄴ" → "주 하느님 크시도다")
 *  - 자모 서브시퀀스 (중간 글자 생략)
 *  - 자모 편집 거리 (오타 허용)
 *  작곡가·첫 소절 일치는 제목보다 낮은 가중치를 받는다 (제목 > 작곡가 > 첫 소절).
 */
class SongSearcher(songs: List<Song>) {
    private class Entry(
        val song: Song,
        val title: IndexedText,
        val composer: IndexedText,
        val firstLine: IndexedText,
    )

    private val entries = songs.map {
        Entry(it, IndexedText(it.title), IndexedText(it.composer), IndexedText(it.firstLine))
    }

    fun search(query: String): List<SearchResult> {
        val trimmed = query.trim().removeSuffix("번").trim()
        if (trimmed.isEmpty()) return entries.map { SearchResult(it.song, 0) }

        if (trimmed.all { it.isDigit() }) return searchNumber(trimmed)

        val q = IndexedText(trimmed)
        if (q.jamo.isEmpty()) return emptyList()
        val choQuery = q.kept.all { Hangul.isConsonant(it) }

        return entries.mapNotNull { entry ->
            val title = match(entry.title, q, choQuery)
            val composer = match(entry.composer, q, choQuery)?.let { it.copy(score = it.score * 7 / 10) }
            val firstLine = match(entry.firstLine, q, choQuery)?.let { it.copy(score = it.score * 6 / 10) }
            // 같은 점수면 제목 > 작곡가 > 첫 소절 순으로 우선
            val best = listOfNotNull(title, composer, firstLine).maxByOrNull { it.score } ?: return@mapNotNull null
            when (best) {
                title -> SearchResult(entry.song, best.score, titleRanges = best.ranges)
                composer -> SearchResult(entry.song, best.score, composerRanges = best.ranges)
                else -> SearchResult(entry.song, best.score, firstLineRanges = best.ranges)
            }
        }.sortedWith(compareByDescending<SearchResult> { it.score }.thenBy { it.song.no })
    }

    private fun searchNumber(digits: String): List<SearchResult> {
        val target = digits.trimStart('0').ifEmpty { return emptyList() }
        return entries.mapNotNull { entry ->
            val no = entry.song.no.toString()
            when {
                no == target -> SearchResult(entry.song, 2000)
                no.startsWith(target) -> SearchResult(entry.song, 1500 - (no.length - target.length) * 10)
                else -> null
            }
        }.sortedWith(compareByDescending<SearchResult> { it.score }.thenBy { it.song.no })
    }

    private data class Match(val score: Int, val ranges: List<IntRange>)

    private fun match(t: IndexedText, q: IndexedText, choQuery: Boolean): Match? {
        if (t.jamo.isEmpty()) return null

        // 1. 자모 부분 문자열
        val idx = t.jamo.indexOf(q.jamo)
        if (idx >= 0) {
            val start = t.jamoToChar[idx]
            val end = t.jamoToChar[idx + q.jamo.length - 1]
            val score = when {
                idx == 0 -> 1000
                t.isWordStart(start) -> 950
                else -> 900 - minOf(start, 20)
            }
            return Match(score, listOf(start..end))
        }

        // 2. 초성
        if (choQuery) {
            val choIdx = t.cho.indexOf(q.kept)
            if (choIdx >= 0) {
                val start = t.choToChar[choIdx]
                val end = t.choToChar[choIdx + q.kept.length - 1]
                return Match(if (choIdx == 0) 850 else 800 - minOf(choIdx, 20), listOf(start..end))
            }
            subsequence(t.cho, q.kept)?.let { positions ->
                val span = positions.last() - positions.first() + 1
                if (span <= q.kept.length * 2) {
                    return Match(300 + 300 * q.kept.length / span, toRanges(positions.map { t.choToChar[it] }))
                }
            }
            return null
        }

        // 3. 자모 서브시퀀스
        subsequence(t.jamo, q.jamo)?.let { positions ->
            val span = positions.last() - positions.first() + 1
            if (span <= q.jamo.length * 2) {
                return Match(300 + 300 * q.jamo.length / span, toRanges(positions.map { t.jamoToChar[it] }))
            }
        }

        // 4. 오타 허용 (근사 부분 문자열 매칭)
        if (q.jamo.length >= 4) {
            val maxDistance = (q.jamo.length / 4).coerceIn(1, 3)
            approximate(t.jamo, q.jamo, maxDistance)?.let { (distance, range) ->
                val chars = t.jamoToChar[range.first]..t.jamoToChar[range.last]
                return Match(280 - distance * 60, listOf(chars))
            }
        }
        return null
    }

    companion object {
        /** query 의 모든 글자가 text 에 순서대로 나타나는 가장 짧은 구간의 위치들 (fzf v1 방식) */
        internal fun subsequence(text: String, query: String): List<Int>? {
            if (query.isEmpty()) return null
            var qi = 0
            var end = -1
            for (ti in text.indices) {
                if (text[ti] == query[qi] && ++qi == query.length) {
                    end = ti
                    break
                }
            }
            if (end < 0) return null
            // 끝에서부터 거꾸로 훑어 가장 늦은 시작점을 찾아 구간을 좁힌다.
            qi = query.length - 1
            var start = end
            for (ti in end downTo 0) {
                if (text[ti] == query[qi] && --qi < 0) {
                    start = ti
                    break
                }
            }
            val positions = ArrayList<Int>(query.length)
            qi = 0
            for (ti in start..end) {
                if (qi < query.length && text[ti] == query[qi]) {
                    positions += ti
                    qi++
                }
            }
            return positions
        }

        /**
         * Sellers 알고리즘: text 의 임의 부분 문자열과 query 사이의 최소 편집 거리.
         * maxDistance 이하이면 (거리, text 내 일치 범위)를 반환한다.
         */
        internal fun approximate(text: String, query: String, maxDistance: Int): Pair<Int, IntRange>? {
            val m = query.length
            var prev = IntArray(m + 1) { it }
            var prevStart = IntArray(m + 1)
            var cur = IntArray(m + 1)
            var curStart = IntArray(m + 1)
            var best = Int.MAX_VALUE
            var bestRange = IntRange.EMPTY
            for (j in 1..text.length) {
                cur[0] = 0
                curStart[0] = j
                for (i in 1..m) {
                    val cost = if (query[i - 1] == text[j - 1]) 0 else 1
                    var value = prev[i - 1] + cost
                    var start = prevStart[i - 1]
                    if (cur[i - 1] + 1 < value) {
                        value = cur[i - 1] + 1
                        start = curStart[i - 1]
                    }
                    if (prev[i] + 1 < value) {
                        value = prev[i] + 1
                        start = prevStart[i]
                    }
                    cur[i] = value
                    curStart[i] = start
                }
                if (cur[m] < best && curStart[m] < j) {
                    best = cur[m]
                    bestRange = curStart[m]..<j
                }
                prev = cur.also { cur = prev }
                prevStart = curStart.also { curStart = prevStart }
            }
            return if (best <= maxDistance) best to bestRange else null
        }

        internal fun toRanges(sortedIndices: List<Int>): List<IntRange> {
            val ranges = mutableListOf<IntRange>()
            for (index in sortedIndices.distinct()) {
                val last = ranges.lastOrNull()
                if (last != null && index == last.last + 1) {
                    ranges[ranges.lastIndex] = last.first..index
                } else {
                    ranges += index..index
                }
            }
            return ranges
        }
    }
}

/**
 * 검색용으로 정규화한 텍스트. 공백/문장부호를 제거하고 소문자화한 뒤
 * 자모 시퀀스와 초성 시퀀스를 만들며, 각각 원문 글자 인덱스로의 매핑을 보관한다.
 */
internal class IndexedText(val original: String) {
    val kept: String
    val jamo: String
    val jamoToChar: IntArray
    val cho: String
    val choToChar: IntArray

    init {
        val keptBuilder = StringBuilder()
        val jamoBuilder = StringBuilder()
        val jamoMap = ArrayList<Int>()
        val choMap = ArrayList<Int>()
        val choBuilder = StringBuilder()
        original.forEachIndexed { index, raw ->
            if (!raw.isLetterOrDigit()) return@forEachIndexed
            val c = raw.lowercaseChar()
            keptBuilder.append(c)
            choBuilder.append(Hangul.choseong(c))
            choMap += index
            for (j in Hangul.decompose(c)) {
                jamoBuilder.append(j)
                jamoMap += index
            }
        }
        kept = keptBuilder.toString()
        jamo = jamoBuilder.toString()
        jamoToChar = jamoMap.toIntArray()
        cho = choBuilder.toString()
        choToChar = choMap.toIntArray()
    }

    fun isWordStart(charIndex: Int): Boolean =
        charIndex == 0 || !original[charIndex - 1].isLetterOrDigit()
}
