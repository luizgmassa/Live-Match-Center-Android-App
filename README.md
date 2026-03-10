# Live Match Center — Websocket, SSE and REST API usages in a real-world application use case

A production-ready Android application demonstrating real-time sports betting features using modern Android architecture and reactive programming patterns.

## Overview

Live Match Center is a sports betting Android app that displays live matches with real-time odds updates and match commentary. The project showcases modern Android development practices including Clean Architecture, MVVM, and reactive data streaming.

## Features

- **Live Match Listings** - Paginated list of ongoing sports matches with real-time status updates
- **Real-time Odds** - Live odds streaming via WebSocket connections
- **Match Commentary** - Live match commentary via Server-Sent Events (SSE)
- **Offline Support** - Local caching with Room database for offline access
- **Pull-to-Refresh** - Manual refresh capability for latest data

## Architecture

```
com.massa.livecenter
├── data                    # Data layer
│   ├── local/db           # Room: MatchEntity, OddsEntity, RemoteKeyEntity + DAOs
│   ├── remote
│   │   ├── rest           # Retrofit: MatchApiService, DTOs
│   │   ├── websocket      # OddsWebSocketClient (OkHttp WebSocket)
│   │   └── sse            # CommentarySseClient (OkHttp streaming)
│   └── repository         # LiveMatchRepositoryImpl, MatchRemoteMediator
├── domain                  # Domain layer
│   ├── model               # Match, Odds, Commentary, CommentaryType
│   ├── repository          # LiveMatchRepository (interface)
│   └── usecase             # GetLiveMatchesPagerUseCase, ObserveOddsUseCase, etc.
├── presentation             # Presentation layer
│   ├── contract             # LiveMatchUiState, LiveMatchUiEvent, LiveMatchUiEffect
│   ├── viewmodel            # LiveMatchViewModel (@HiltViewModel)
│   └── ui
│       ├── screen           # LiveMatchCenterScreen
│       └── component        # MatchCard, OddsChip, CommentaryItem, StatusBadge
└── di                       # NetworkModule, DatabaseModule, RepositoryModule
```

## Tech Stack

| Category | Technology |
|----------|------------|
| Language | Kotlin |
| UI | Jetpack Compose (Material 3) |
| Architecture | Clean Architecture + MVVM |
| DI | Hilt |
| Database | Room |
| Networking | Retrofit + OkHttp |
| Real-time | WebSocket + SSE |
| Pagination | Paging 3 |
| Async | Kotlin Coroutines + Flow |

## Getting Started

The project compiles and runs. It shows placeholder/empty states until the architecture is wired together.

See the [README](README.md) for implementation tasks and detailed architecture documentation.

## API References

| Resource | Details |
|----------|---------|
| REST | `GET https://api.superbet.dev/v1/matches/live?page={n}&size={n}` |
| WebSocket | `wss://live.superbet.dev/odds` |
| SSE | `https://api.superbet.dev/v1/matches/{matchId}/commentary` |

## License

This project is for demonstration and interview purposes.
