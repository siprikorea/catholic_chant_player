#!/usr/bin/env python3
"""media/ 에 없는 악보(sheet/NNN.jpg)와 음원(mp3/NNN.mp3)을 굿뉴스 성가 페이지에서 받아 채운다.

기존 media 는 굿뉴스의 '단성 악보' 이미지와 MP3 파일과 동일하므로 같은 것을 받는다.
단성 악보가 없는 곡은 혼성 악보로 대신한다. 성가 페이지는 fetch_songs.py 가 만든
tools/.cache 를 사용한다 (없으면 먼저 fetch_songs.py 실행).

사용법: python3 tools/fetch_media.py && python3 tools/fetch_songs.py --offline
"""
import re
import time
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
CACHE = ROOT / "tools" / ".cache"
MEDIA = ROOT / "media"
SITE = "https://maria.catholic.or.kr"
LAST = 529


def download(url: str, dest: Path, expect: bytes) -> bool:
    req = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0"})
    for attempt in range(3):
        try:
            with urllib.request.urlopen(req, timeout=60) as res:
                data = res.read()
            if not data.startswith(expect):
                return False
            dest.parent.mkdir(parents=True, exist_ok=True)
            dest.write_bytes(data)
            time.sleep(0.3)
            return True
        except Exception as e:  # noqa: BLE001
            print(f"  retry {url} ({e})")
            time.sleep(2 * (attempt + 1))
    return False


def sheet_url(page: str) -> str | None:
    for alt in ("단성악보", "혼성악보"):
        m = re.search(r'<img src="([^"]+)" alt="' + alt + '"', page)
        if m:
            return SITE + m.group(1)
    return None


def mp3_url(page: str) -> str | None:
    m = re.search(r"viewFile\('(\d+)','mp3'\)", page)
    return f"{SITE}/sungga/download.asp?strFileID={m.group(1)}&FileType=mp3" if m else None


def main() -> None:
    added = {"sheet": [], "mp3": []}
    unavailable = {"sheet": [], "mp3": []}
    for no in range(1, LAST + 1):
        file_no = f"{no:03d}"
        cached = CACHE / f"{file_no}.html"
        page = cached.read_text(encoding="utf-8") if cached.exists() else ""
        targets = (
            ("sheet", MEDIA / "sheet" / f"{file_no}.jpg", sheet_url(page), b"\xff\xd8"),
            # ID3 태그 또는 MPEG 프레임 동기 바이트
            ("mp3", MEDIA / "mp3" / f"{file_no}.mp3", mp3_url(page), b""),
        )
        for kind, dest, url, magic in targets:
            if dest.exists():
                continue
            ok = url is not None and download(url, dest, magic)
            if ok and kind == "mp3" and not (dest.read_bytes()[:3] == b"ID3" or dest.read_bytes()[:1] == b"\xff"):
                dest.unlink()
                ok = False
            (added if ok else unavailable)[kind].append(no)
            print(f"{kind} {file_no}: {'ok' if ok else 'unavailable'}")
    for kind in ("sheet", "mp3"):
        print(f"{kind}: added {len(added[kind])}, unavailable {unavailable[kind]}")


if __name__ == "__main__":
    main()
