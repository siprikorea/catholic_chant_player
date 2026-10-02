#!/usr/bin/env python3
"""굿뉴스 가톨릭 성가 검색에서 성가 메타데이터(번호/제목/전례별/형식별/작곡가/첫 소절)를 수집해
composeApp 리소스의 songs.json 을 생성한다. media/ 폴더를 스캔해 악보/음원 보유 여부도 기록한다.

사용법: python3 tools/fetch_songs.py            # 수집 + 생성
        python3 tools/fetch_songs.py --offline  # 캐시(tools/.cache)만 사용해 재생성
"""
import html
import json
import re
import sys
import time
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
CACHE = ROOT / "tools" / ".cache"
MEDIA = ROOT / "media"
OUT = ROOT / "composeApp" / "src" / "commonMain" / "composeResources" / "files" / "songs.json"
URL = "https://maria.catholic.or.kr/sungga/search/sungga_view.asp?ctxtIndex={}"
LAST = 529

# 사이트에 메타데이터가 비어 있어 악보 이미지를 보고 채운 값
OVERRIDES = {
    529: {"title": "이 땅에 빛을", "composer": "김은선", "firstLine": "이 겨레 깊은 밤 한줄기 빛에"},
}


def fetch(no: int, offline: bool) -> str:
    CACHE.mkdir(parents=True, exist_ok=True)
    cached = CACHE / f"{no:03d}.html"
    if cached.exists():
        return cached.read_text(encoding="utf-8")
    if offline:
        return ""
    req = urllib.request.Request(URL.format(no), headers={"User-Agent": "Mozilla/5.0"})
    for attempt in range(3):
        try:
            with urllib.request.urlopen(req, timeout=20) as res:
                text = res.read().decode("utf-8", errors="replace")
            cached.write_text(text, encoding="utf-8")
            time.sleep(0.3)
            return text
        except Exception as e:  # noqa: BLE001
            print(f"  retry {no} ({e})", file=sys.stderr)
            time.sleep(2 * (attempt + 1))
    return ""


def field(page: str, label: str) -> str:
    m = re.search(r'<th scope="row">' + re.escape(label) + r'</th>\s*<td[^>]*>(.*?)</td>', page, re.S)
    if not m:
        return ""
    value = re.sub(r"<[^>]+>", "", m.group(1))
    return re.sub(r"\s+", " ", html.unescape(value)).strip()


def main() -> None:
    offline = "--offline" in sys.argv
    sheets = {int(p.stem) for p in (MEDIA / "sheet").glob("*.jpg") if p.stem.isdigit()}
    audios = {int(p.stem) for p in (MEDIA / "mp3").glob("*.mp3") if p.stem.isdigit()}
    songs, missing = [], []
    for no in range(1, LAST + 1):
        page = fetch(no, offline)
        song = {
            "no": no,
            "title": field(page, "성가 제목"),
            "category": field(page, "전례별"),
            "type": field(page, "형식별"),
            "composer": field(page, "작곡가"),
            "firstLine": field(page, "첫 소절"),
            "hasSheet": no in sheets,
            "hasAudio": no in audios,
        }
        song.update({k: v for k, v in OVERRIDES.get(no, {}).items() if not song[k]})
        if not song["title"]:
            missing.append(no)
        songs.append(song)
        if no % 50 == 0:
            print(f"{no}/{LAST}")
    OUT.parent.mkdir(parents=True, exist_ok=True)
    OUT.write_text(json.dumps(songs, ensure_ascii=False, indent=1) + "\n", encoding="utf-8")
    print(f"wrote {OUT.relative_to(ROOT)}: {len(songs)} songs, {len(sheets)} sheets, {len(audios)} audios")
    if missing:
        print(f"title missing: {missing}")


if __name__ == "__main__":
    main()
