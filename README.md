# Live Match Center — Candidate Interview Project

Welcome! This project is the **shell** for a Live Match Center feature in a sports betting app. The UI is fully built and the data layer is scaffolded. **Your job is to wire everything together.**

> The app compiles and runs. It shows placeholder/empty states until you implement the architecture below.

---

## Architecture Overview

```
com.massa.livecenter
├── data
│   ├── local/db          # Room: MatchEntity, OddsEntity, RemoteKeyEntity + DAOs
│   ├── remote
│   │   ├── rest          # Retrofit: MatchApiService, DTOs
│   │   ├── websocket     # OddsWebSocketClient (OkHttp WebSocket)
│   │   └── sse           # CommentarySseClient (OkHttp streaming)
│   └── repository        # LiveMatchRepositoryImpl, MatchRemoteMediator
├── domain
│   ├── model             # Match, Odds, Commentary, CommentaryType
│   ├── repository        # LiveMatchRepository (interface)
│   └── usecase           # GetLiveMatchesPagerUseCase, ObserveOddsUseCase, etc.
├── presentation
│   ├── contract          # LiveMatchUiState, LiveMatchUiEvent, LiveMatchUiEffect
│   ├── viewmodel         # LiveMatchViewModel (@HiltViewModel)
│   └── ui
│       ├── screen        # LiveMatchCenterScreen (DO NOT MODIFY)
│       └── component     # MatchCard, OddsChip, CommentaryItem, StatusBadge (DO NOT MODIFY)
└── di                    # NetworkModule, DatabaseModule, RepositoryModule
```

---

## Your Tasks

### Task 1 — Bind the Repository in `RepositoryModule`

Open `di/RepositoryModule.kt`. Add a `@Binds @Singleton` method so Hilt can inject `LiveMatchRepository` by providing `LiveMatchRepositoryImpl`.

**Hint:** `@Binds` requires an `abstract class`, not an `object`.

---

### Task 2 — Implement `LiveMatchRepositoryImpl`

File: `data/repository/LiveMatchRepositoryImpl.kt`

1. **`getLiveMatchesPager()`** — Wire `MatchRemoteMediator` to the `Pager`:
   ```kotlin
   // TODO: add remoteMediator = matchRemoteMediator
   ```
   Then map each `MatchEntity` to a domain `Match`.

2. **`observeOddsForMatch(matchId)`** — Filter `oddsWebSocketClient.oddsFlow` by `matchId`, map `OddsUpdateDto → Odds`.

3. **`observeCommentary(matchId)`** — Call `commentarySseClient.observeCommentary(matchId)`, map `CommentaryEventDto → Commentary` (parse `CommentaryType` from the string, defaulting to `GENERAL`).

4. **`observeConnectionState()`** — Return `oddsWebSocketClient.connectionState`.

5. **`refreshMatches()`** — Invalidate the current `PagingSource` to trigger a fresh REFRESH load.

---

### Task 3 — Implement `MatchRemoteMediator.load()`

File: `data/repository/MatchRemoteMediator.kt`

| LoadType | Behaviour |
|----------|-----------|
| `REFRESH` | Fetch page 1 from REST. In a single Room **transaction**: clear all matches & keys, insert fresh data. |
| `PREPEND` | Return `Success(endOfPaginationReached = true)` — no prepend needed. |
| `APPEND`  | Look up the remote key for the last visible item. If `nextPage == null` → end of pages. Otherwise fetch that page and insert. |

Always wrap in `try/catch` and return `MediatorResult.Error(e)` on failure.

---

### Task 4 — Implement `OddsWebSocketClient`

File: `data/remote/websocket/OddsWebSocketClient.kt`

- **`connect()`**: Build an OkHttp `WebSocket`. In the listener:
  - `onOpen` → set `_connectionState` to `Connected`
  - `onMessage` → parse JSON with `Gson` → emit to `_oddsFlow` (use a `CoroutineScope`)
  - `onFailure` → set `_connectionState` to `Reconnecting`
  - `onClosing` → set `_connectionState` to `Disconnected`
- **`disconnect()`**: Call `webSocket?.close(1000, …)`, set state to `Disconnected`.

---

### Task 5 — Implement `CommentarySseClient.observeCommentary()`

File: `data/remote/sse/CommentarySseClient.kt`

- Use OkHttp `execute()` to open a streaming connection.
- Read line by line from `response.body.byteStream()`.
- For lines starting with `data:`, strip the prefix, parse JSON, and `emit()` the DTO.
- In the `finally` block, cancel the OkHttp `Call` to stop streaming when the collector cancels.

---

### Task 6 — Implement All Use Cases

File: `domain/usecase/UseCases.kt`

Each use case should delegate directly to the matching repository method. Remove each `TODO()` and replace with the appropriate `return repository.*()` call.

---

### Task 7 — Implement `LiveMatchViewModel`

File: `presentation/viewmodel/LiveMatchViewModel.kt`

1. **`init` block**:
   - Collect `observeConnectionState()` → update `_uiState.connectionState`
   - Collect `getLiveMatchesPager().cachedIn(viewModelScope)` → update `_uiState.matches`

2. **`onEvent(SelectMatch)`**:
   - Update `selectedMatchId` in state
   - Cancel `oddsJob` and `commentaryJob`
   - Call `observeOddsForMatch()` and `observeCommentaryForMatch()`

3. **`onEvent(Refresh)`**:
   - Set `isRefreshing = true`
   - Call `refreshMatches()` in a coroutine
   - On success: emit `ScrollToTop` effect
   - On exception: emit `ShowError` effect
   - In `finally`: set `isRefreshing = false`

4. **`onEvent(DismissDetail)`**:
   - Clear `selectedMatchId` and `commentary` in state
   - Cancel `oddsJob` and `commentaryJob`

---

## API References

| Resource | Details |
|----------|---------|
| REST | `GET https://api.superbet.dev/v1/matches/live?page={n}&size={n}` |
| WebSocket | `wss://live.superbet.dev/odds` |
| SSE | `https://api.superbet.dev/v1/matches/{matchId}/commentary` |

---

## Running the App

The project runs as-is and shows placeholder states. No configuration needed.

Good luck! 🏆
