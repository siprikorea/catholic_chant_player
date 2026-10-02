# 가톨릭 성가 (Catholic Chant)

가톨릭 성가 529곡의 번호·제목 퍼지 검색, 악보 보기, 음원 듣기 앱.
Kotlin **Compose Multiplatform** 하나의 UI 코드로 **Android / iOS / Web(Wasm)** 을 지원합니다.

## 구조

```
composeApp/        공통 코드 (UI, 검색, ViewModel) + 플랫폼별 미디어 구현
  commonMain/      App.kt, search/(한글 퍼지 검색), ui/, data/
  androidMain/     MediaPlayer + AssetManager
  iosMain/         AVAudioPlayer + 앱 번들
  wasmJsMain/      HTML <audio> + fetch, index.html
androidApp/        Android 애플리케이션 모듈
androidMediaPack/  Play Asset Delivery install-time 팩 (media/ 심볼릭 링크)
iosApp/            Xcode 프로젝트 (xcodegen: iosApp/project.yml)
media/             악보(sheet/NNN.jpg)·음원(mp3/NNN.mp3) 원본
tools/fetch_songs.py  굿뉴스 성가 검색에서 곡 메타데이터 수집 → songs.json
```

## 검색

입력할 때마다 즉시 검색합니다 (`composeApp/src/commonMain/.../search/SongSearcher.kt`).

- 번호: `2` → 2번, 20~29번, 200번대 순
- 제목/첫 소절: 띄어쓰기 무시, 자모 단위 비교라 입력 중인 글자도 일치 (`하는` → 하느님)
- 초성: `ㅈㅎㄴㄴ` → 주 하느님 크시도다
- 오타 허용: `축하함니다` → 축하합니다

## 빌드 환경

- JDK 17 이상 (Gradle 데몬은 `gradle/gradle-daemon-jvm.properties` 의 JDK 25 사용)
- Android SDK (compileSdk 37), Xcode

### Android

```bash
./gradlew :androidApp:installDebug     # 디버그: media/ 를 APK assets 에 직접 포함
./gradlew :androidApp:bundleRelease    # 릴리스: media 는 install-time asset pack 으로 분리
```

음원이 1.3GB 라 AAB 기본 모듈 한도(200MB)를 넘기 때문에 릴리스는 asset pack 을 사용합니다.
asset pack 도 `AssetManager` 로 읽으므로 코드는 동일합니다.

### iOS

```bash
cd iosApp && xcodegen generate   # project.yml 수정 시에만
open iosApp/iosApp.xcodeproj
```

빌드 단계에서 Kotlin 프레임워크를 컴파일하고 `media/sheet`, `media/mp3` 를 앱 번들에 복사합니다.
실기기 실행/배포 시 Xcode 에서 Signing Team 을 지정하세요.

### Web

```bash
./gradlew :composeApp:wasmJsBrowserDevelopmentRun   # 개발 서버
./gradlew :composeApp:wasmJsBrowserDistribution     # 배포물: composeApp/build/dist/wasmJs/productionExecutable
```

웹은 미디어를 원격에서 불러옵니다. URL 템플릿은 `composeApp/src/wasmJsMain/resources/index.html` 의
`window.CHANT_MEDIA_URL` 에서 설정합니다 (기본값: Firebase Storage `catholichymnal-c3c1a.firebasestorage.app`).

- `{path}` → URL 인코딩된 경로 (`sheet%2F002.jpg`), `{rawPath}` → 원래 경로 (`sheet/002.jpg`)
- 로컬 `media/` 폴더로 테스트: 개발 서버 주소에 `?media=local` 추가

Firebase Storage 사용 시 필요한 설정:

1. 보안 규칙에서 `sheet/`, `mp3/` 공개 읽기 허용
   ```
   rules_version = '2';
   service firebase.storage {
     match /b/{bucket}/o {
       match /sheet/{file} { allow read: if true; }
       match /mp3/{file}   { allow read: if true; }
     }
   }
   ```
2. 악보는 `fetch` 로 읽으므로 버킷 CORS 설정 (`cors.json`):
   ```json
   [{ "origin": ["https://<배포 도메인>"], "method": ["GET"], "maxAgeSeconds": 3600 }]
   ```
   `gcloud storage buckets update gs://catholichymnal-c3c1a.firebasestorage.app --cors-file=cors.json`

## 곡 데이터 갱신

```bash
python3 tools/fetch_songs.py            # 굿뉴스에서 수집 (tools/.cache 에 캐시)
python3 tools/fetch_songs.py --offline  # 캐시만으로 재생성 (media/ 변경 시 hasSheet/hasAudio 갱신)
```

## 테스트

```bash
./gradlew :composeApp:iosSimulatorArm64Test
```

## 라이선스

- 폰트: Pretendard (SIL Open Font License 1.1) — `composeResources/files/PRETENDARD_LICENSE.txt`
- 곡 메타데이터 출처: [굿뉴스 가톨릭 성가 검색](https://maria.catholic.or.kr/sungga/search/sungga_search.asp)
