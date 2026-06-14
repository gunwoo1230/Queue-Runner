 # 구르박스 (Gureubox) — 기말 발표 자료

> **변경 이력**: ~~Queue-Runner~~ → **구르박스 (Gureubox)**
> 스마트폰 게임 프로그래밍 (spgp2026) 기말 프로젝트

---

## 🎬 발표 영상



---

## 1. 게임 소개

분리수거장에서 굴러 나온 **박스**가, 자신을 잡으러 오는 사람들을 피해 도시 골목을 도망치는 **2D 무한 러너** 게임입니다.

사용자는 커맨드를 조합해 박스를 조작하고 Side-View 도주와 Top-View 미로 탈출을 반복하는 구조입니다.

---

## 2. 개발 계획 / 일정 / 실제 진행

### 2.1. 항목별 계획 대비 진행도

2차 발표 당시의 기능 시스템 단위를 기준으로 최종 진행 상황을 정리했습니다.

| 시스템 | 2차 발표 | 최종 | 남은 부분 |
|---|---:|---:|---|
| **커맨드 시스템** (O/X 2슬롯 밀어내기 큐) | 100% | **100%** | |
| **Side-View 캐릭터 (Player)** | 75% | **약 90%** | 점프/대기 애니메이션(현재 정지 비트맵 1장). |
| **추격자 (Janitor)** | 80% | **약 90%** |  추격 모션 애니메이션. |
| **장애물 시스템** | 15% | **약 85%** | 자동차(`Car`)는 스프라이트 미확보 + 밸런스 문제로 현재 스폰 목록에서 제외(정의만 존재) → 실제 출현은 5종. |
| **시점 전환 (Side ↔ Top-View)** | 0% | **약 85%** |  전환/출구 연출 애니메이션(코드에 슬롯만 존재), 미로 랜덤 생성(현재 고정 레이아웃). |
| **Top-View 미로** | 0% | **약 85%** |  미로 다양화, 미화원/박스  스프라이트. |
| **리소스 / 연출** | 25% | **약 50%** | 자동차/행인 스프라이트, 모든 캐릭터 애니메이션, 박스 굴러 나오는 오프닝, TopView 배경 **BGM/효과음**. |

### 2.2. 주차별 커밋 횟수 (GitHub 인용)

<figure>
  <img src="./image/commit (2).png">
  <figcaption>GitHub Insights - Commits</figcaption>
</figure>

| 주차 | 기간 | Commit 수 |
|---|---|---|
| 1주차 | 4/6 ~ 4/12 | 2 |
| 2주차 | 4/13 ~ 4/19 | 0 |
| 3주차 | 4/20 ~ 4/26 | 4 |
| 4주차 | 4/27 ~ 5/3 | 2 |
| 5주차 | 5/4 ~ 5/10 | 9 |
| 6주차 | 5/11 ~ 5/17 | 3 |
| 7주차 | 5/18 ~ 5/24 | 1 |
| 8주차 | 5/25 ~ 5/31 | 4 |
| 9주차 | 6/1 ~ 6/7 | 1 |
| 10주차 | 6/8 ~ 6/14 | 1 |
| **합계** |  | 27 |

### 2.3. 목표 변경 내용과 이유

2차 발표 이후 변경 없습니다.

---

## 3. 사용된 기술 · 참고 · 직접 개발

### 3.1. 사용된 기술

- **언어 / 플랫폼**: Kotlin, Android (AppCompatActivity 기반).
- **렌더링**: Android `Canvas` 2D 직접 그리기. 비트맵(`drawBitmap`) + `Path`(화살표/말풍선) + `Paint`.
- **게임 루프**: `Choreographer.FrameCallback`(vsync 기반)으로 매 프레임 `frameTime`(delta time)을 계산해 업데이트.
- **가상 좌표계**: 실제 해상도와 무관하게 1600×900 가상 좌표계로 그리고, `transformMatrix`로 화면에 맞춤(해상도 독립).
- **씬/오브젝트 구조**: `Scene` + `World`(레이어별 update/draw 순서 보장) + `SceneStack`(push / pop / change / popAll, 투명 overlay 씬 지원).
- **메모리/성능**: 장애물 **오브젝트 풀링**(`IRecyclable` + `World.obtain()` 재활용), update/draw hot-path에서 iterator 할당 회피.
- **알고리즘**: 미로 추격용 **4방향 BFS 최단경로**(`Maze.nextStepToward`), 점프 물리 역산(거리·높이·체공시간 → 속도·중력).
- **입력**: `MotionEvent`(ACTION_DOWN) 기반 버튼 터치, 좌표는 화면 → 가상 좌표 역변환.
- **사운드(가용)**: `SoundPool`(효과음) / `MediaPlayer`(BGM) 헬퍼 존재 — 단, 현재 게임에서는 미사용(4.3 참고).

### 3.2. 참고한 것들

- **레퍼런스 게임**: 쿠키런 류 횡스크롤 러너(가로 비율·지면 점프 감각).
- **그래픽 리소스**: 박스·아저씨·미화원·배경·장애물·버튼 포즈 등 **모든 이미지 리소스는 AI로 직접 생성**했습니다(외부 에셋 미사용).
- **수업 예제에서 아이디어만 가져와 다르게 구현한 것**

| 구르박스 | 참고한 수업 예제 (아이디어) | 구르박스에서 바뀐 점 (→ 직접 구현) |
|---|---|---|
| `MapObject`(추상) | **CookieRun `MapObject`** — `Sprite` 상속, 좌측 스크롤, `right<0`이면 self-remove, `IRecyclable`+abstract `layer` | `Sprite` 미상속 + **좌표 모델 변경**(`screenX = PLAYER_SCREEN_X + (virtualX − player.virtualX)`로 박스 기준 동기화 → 박스가 멈추면 장애물도 멈춤) + **얇은 띠 충돌** + `effect`/`tileCount` 추가 |
| `Player` | **CookieRun `Player`** — 화면 고정 + 맵 스크롤 러너, `jump()` 상태기계, `hurt()` 반응 | 점프 구현이 다름: 쿠키런은 `−JUMP_POWER`+일정 중력+바닥 탐지. 구르박스는 **콤보 → (거리·높이·체공) → 속도·중력 역산** 포물선(거리 자체가 핵심) + 단일 지면 + 슬로우/게임오버 효과 |
| `CollisionChecker` | **CookieRun `CollisionChecker`**(구조·생성자 일치). 동일 컨트롤러 패턴이 DragonFlight·TUDefence에도 등장 | 단일 OBSTACLE만 + **점프/입력 중 스킵 게이트** + `applyHit(effect)` **효과 분기**, ITEM 검사 없음 |
| `ObstacleSpawner` | **DragonFlight `EnemyGenerator`·TUDefence `WaveGen`**(주기적 wave 스폰) + **CookieRun `MapLoader`**(`Registry.create→world.add(it.layer)`로 화면 앞쪽 생성) | **시간 wave/스테이지 파일 → 이동 거리 임계 + 랜덤 + 종류별 최소 간격** |
| `Maze`(경로탐색) | **TUDefence `PathFinder`** — 타일 격자 경로탐색(A* + 경로 단순화 + 곡선 waypoint, 적이 *고정 경로* 추종) | **BFS · 4방향 · "다음 한 칸"만 매 프레임 재계산**(이동하는 박스를 *동적 추격*). 고정 경로 A*가 아님 |
| `GameOverScene` | **TUDefence `PauseScene`** — `isTransparent` 오버레이 Scene + `LabelUtil` + `onBackPressed` + `popAll/pop` | 게임오버 전용: world 없이 직접 그리기 + 어두운 막 + Restart/Exit 버튼 + 최종 점수 |
| `ScoreDisplay` | **TUDefence `Score`**(=`ImageNumber` 래퍼, 우상단) + DragonFlight `ScoreNumber` | **정렬(RIGHT/CENTER)·중앙 평행이동·씬 간 누적 유지(`GameSession`)** 추가 |
| `ProgressGauge` | **CookieRun `MapLoader`의 진행 게이지**(`Gauge`를 상단 `200/100/1200`에 표시 — 좌표·크기 동일) + DragonFlight `Gauge` 사용 | 진행 기준을 맵 컬럼 → **박스 전환 진행률** + **박스·추격자 위치 마커** 추가 |

### 3.3. 수업 내용에서 차용한 것

- **`a2dg` 게임 프레임워크 전체 (수업 제공 공용 라이브러리)** — `Scene`/`World`/`SceneStack`, `BaseGameActivity`/`GameView`, `GameContext`/`GameMetrics`(가상 좌표계·`transformMatrix`), `Sprite`/`AnimSprite`/`SheetSprite`/`DrawableSprite`, `ImageNumber`, `Gauge`, `HorzScrollBackground`, `Button`, 인터페이스(`IGameObject`/`IBoxCollidable`/`IRecyclable`/`ITouchable`), `GameResources`/`BitmapPool`/`Sound`, `LabelUtil` 등을 그대로 사용.
- **`MapObjectRegistry`** — **CookieRun `MapObjectRegistry`와 사실상 100% 동일**(`fun interface MapObjectCreator(gctx, tile, left, top)` 시그니처, `creators` 맵, `register(Char)`/`register(CharRange)`/`create()`까지 주석만 빼면 그대로). *가장 확실한 차용.*
- **`MapObjectCatalog`** — CookieRun `MapObjectCatalog`와 동일 구조(`object` + `registered` 플래그 + `registerAll()` + `MapObjectRegistry.register(...)` 나열). 타일 문자 집합과 `ALL_TILES`(랜덤 스폰용)만 구르박스 고유.
- **레이어 enum (`MainScene.Layer`)** — **CookieRun `MainLayer`**의 구성 `BG·FLOOR·ITEM·OBSTACLE·PLAYER·…·CONTROLLER`을 거의 그대로 채택(구르박스가 실제로 안 쓰는 `FLOOR`/`ITEM` 레이어까지 남아 있음). `CONTROLLER` 레이어 패턴은 CookieRun·TUDefence 공통.
- **게임 Activity 골격 (`QueueRunnerActivity`)** — CookieRun `CookieRunActivity`·TUDefence `MainGameActivity`와 거의 동일(`BaseGameActivity` 상속 + 디버그 플래그 3종 + `createRootScene()`에서 **`metrics.setSize(1600,900)` 가로 설정**). 차이는 stage/cookie intent extra 대신 `GameSession.reset()`.
- **타이틀 → 게임 Activity 진입 (`MainActivity`)** — 세 예제 공통 패턴(레이아웃 + 시작 버튼 → `startActivity`). 구르박스는 단순화(스테이지/쿠키 선택 없음, `finish()` 추가).
- **오브젝트 풀링(재활용) 패턴** — 세 예제 공통(`Bullet`/`Enemy`, `Floor`/`JellyItem`/장애물, `Explosion`/`Fly`/`Shell`)의 `companion get() = world.obtain(::class.java) ?: 생성 → init()` + `IRecyclable.onRecycle()` + "화면 밖이면 self-`remove()`" 흐름을 `MapObject`/`MapObjectCatalog`가 그대로 차용.

### 3.4. 직접 개발한 것

**입력**
- `CommandController` / `TopCommandController` — **2슬롯 밀어내기 커맨드 큐**, 콤보 발동, 점프/이동 중 선입력 게이트, 머리 위 말풍선·좌우 화살표 UI.

**Side-View 게임플레이**
- `Player` — **콤보 → 점프 물리 역산**(거리·높이·체공 → 속도·중력), 슬로우 1칸 이동, 게임오버 상태기계.
- `Janitor` — 거리 기반 일정 속도 추격 + `isCaught` latch.
- `Cleaner` — 진행 끝에 서서 시점 전환 단서가 되는 정적 NPC.

**장애물 시스템**
- `Singletileobstacles`(쓰레기봉투·음식물통·고양이) / `Doubletileobstacles`(웅덩이·맨홀) / `Stopobstacles`(자동차) — 6종 구체 장애물.
- `HitEffect` — 충돌 효과 enum(슬로우 / 게임오버).

**Top-View 미로**
- `Maze` — 미로 데이터 + 렌더 + 시야 컬링 + **4방향 BFS 경로탐색**.
- `TopPlayer` — 셀 단위 보간 이동 + 벽 충돌 + 카메라 동기화.
- `TopCleaner` — **BFS 기반 미로 추격 AI**.
- `TopViewScene` — 미로 루프, 출구 도달 판정, Side 복귀.

**씬 흐름 / 드로잉**
- `GameSession` — **씬 간 누적 점수·사이클 보존** 상태(싱글턴).
- `ArrowDrawer` — `Canvas` `Path` 단색 화살표(말풍선·버튼 공용).
- **Side ↔ Top 무한 전환** — `change()` 기반 사이클.

---

## 4. 아쉬운 것들

### 4.1. 하고 싶었지만 못 한 것

- **사운드(BGM/효과음)**: 프레임워크에 재생 기능이 있는데도 시간상 게임에 연결하지 못해 현재 무음입니다.
- **캐릭터 애니메이션**: 박스의 점프/구르기, 추격자의 달리기, 박스가 굴러 나오는 오프닝 등 모든 모션이 정지 이미지로 남았습니다.
- **행인 4종**: 도시 골목 분위기 연출용 행인은 구현하지 못했습니다.
- **장애물 다양화 / 미로 다양화**: 자동차(넘지 못하는 장애물) 및 미로 랜덤 생성 미완성
- **시점 전환 연출**: 코드에 슬롯만 두고 페이드/연출 없이 즉시 전환됩니다.

### 4.2. (스토어 판매 시) 보충할 것

- 사운드·BGM·햅틱 등 기본 피드백.
- 타이틀/일시정지/설정 화면, 최고 점수 저장.
- 점진적 난이도 상승(속도·장애물 밀도)과 튜토리얼.
- 다양한 미로/배경/장애물, 캐릭터 스킨 등 콘텐츠 볼륨.
- 광고/결제 등 수익화 요소 및 개인정보·연령 등급 대응.

### 4.3. 해결하지 못한 문제

- **장애물 조합 알고리즘**: 랜덤 생성시 클리어가 불가능하지 않고 난이도가 어려워지지 않도록 장애물 간 최소 간격을 보장하는 로직이 미완성입니다.

### 4.4. 어려웠던 점
- **Side-View <-> Top-View 전환**: 씬 전환 방법에 대한 고민과 시도는 많았지만, 결국 `change()`로 매번 새 씬을 만드는 방식으로 구현했습니다.
---

## 5. 수업에 대한 내용 *(발표 영상에도 포함)*

### 5.1. 이번 수업에서 기대한 것 / 얻은 것 / 얻지 못한 것

- **기대한 것**: Kotlin으로 Android 게임 개발, 2D 게임 프로그래밍 전반(게임 루프·입력·충돌·씬 관리 등), 간단한 게임 프로젝트 경험.
- **얻은 것**: Kotlin Android 게임 개발 경험, 2D 게임 프로그래밍 전반에 대한 이해, 간단한 게임 프로젝트 완성 경험, AI로 리소스 생성 및 적용 경험.
- **얻지 못한 것**: Kotlin의 심화 문법 및 기능들은 아직 이해하지 못했습니다. 게임 디자인 측면에서는 레벨 디자인, 밸런싱, 플레이테스트 등도 경험해보고 싶었지만 시간상 구현과 테스트에 집중하느라 충분히 다뤄보지 못했습니다.

### 5.2. 더 좋은 수업이 되기 위해 변화할 점

- 타 강의에서 한주에 온라인, 오프라인 혼합 수업을 경험해본 적 있었는데, 이 강의에서 적용하면 좋을 것 같은 부분이 있었습니다.
	- 예시) 
		- 온라인 수업 - commit을 따라가며 대략적인 코드 구조와 흐름 설명
		- 오프라인 - 중요한 부분을 함께 코딩, 다른 방식으로 구현 혹은 commit에 없는 내용 추가
- 마지막으로 교수님의 AI 리소스 생성 팁을 알 수 있다면 좋을 것 같습니다.

---
