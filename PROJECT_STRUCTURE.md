# StockSimulator-BE 프로젝트 구조도

## 전체 시스템 아키텍처

```mermaid
graph TB
    subgraph "Client Layer"
        Client[Frontend Client]
    end
    
    subgraph "API Gateway Layer"
        Security[SecurityConfig<br/>JWT 인증/인가]
        Filter[TokenCheckFilter<br/>ApiLoginFilter]
    end
    
    subgraph "Controller Layer"
        AgentCtrl[AgentController<br/>/api/agent]
        UserCtrl[UserController<br/>/api/user]
        ProfileCtrl[UserProfileController<br/>/api/userprofile]
        HoldingsCtrl[HoldingsController<br/>/api/holdings]
        OfferCtrl[OfferController<br/>/api/offer]
        MarketCtrl[MarketDataController<br/>DbCandleController]
        NewsCtrl[NewsController<br/>/api/news]
        RedisCtrl[RedisController<br/>/api/redis]
    end
    
    subgraph "Service Layer"
        AgentSvc[AgentService<br/>예측/포트폴리오/LLM]
        UserSvc[UserService<br/>사용자 관리]
        ProfileSvc[UserProfileService<br/>프로필 관리]
        HoldingsSvc[HoldingsService<br/>보유주식 관리]
        OfferSvc[OfferService<br/>주문 관리]
        MarketSvc[DbMarketService<br/>시장 데이터]
        NewsSvc[NewsService<br/>뉴스 크롤링]
    end
    
    subgraph "Repository Layer"
        UserRepo[UserRepository]
        ProfileRepo[UserProfileRepository]
        HoldingsRepo[HoldingsRepository]
        OfferRepo[OfferRepository]
        MarketRepo[MarketStockRepository<br/>ReportRepository]
        NewsRepo[NewsRepository]
    end
    
    subgraph "External Services"
        FastAPI[FastAPI<br/>Python ML 서버]
        Gemini[Google Gemini API<br/>LLM]
        Finnhub[Finnhub API<br/>시장 데이터]
        MariaDB[(MariaDB)]
        Redis[(Redis Cache)]
    end
    
    Client -->|HTTP| Security
    Security --> Filter
    Filter --> AgentCtrl
    Filter --> UserCtrl
    Filter --> ProfileCtrl
    Filter --> HoldingsCtrl
    Filter --> OfferCtrl
    Filter --> MarketCtrl
    Filter --> NewsCtrl
    Filter --> RedisCtrl
    
    AgentCtrl --> AgentSvc
    UserCtrl --> UserSvc
    ProfileCtrl --> ProfileSvc
    HoldingsCtrl --> HoldingsSvc
    OfferCtrl --> OfferSvc
    MarketCtrl --> MarketSvc
    NewsCtrl --> NewsSvc
    RedisCtrl --> MarketSvc
    
    AgentSvc --> FastAPI
    AgentSvc --> Gemini
    AgentSvc --> MarketSvc
    UserSvc --> UserRepo
    UserSvc --> ProfileRepo
    ProfileSvc --> ProfileRepo
    HoldingsSvc --> HoldingsRepo
    OfferSvc --> OfferRepo
    MarketSvc --> MarketRepo
    NewsSvc --> NewsRepo
    
    UserRepo --> MariaDB
    ProfileRepo --> MariaDB
    HoldingsRepo --> MariaDB
    OfferRepo --> MariaDB
    MarketRepo --> MariaDB
    NewsRepo --> MariaDB
    
    RedisCtrl --> Redis
    MarketSvc --> Redis
    UserSvc --> Redis
```

## 패키지 구조 상세도

```mermaid
graph LR
    subgraph "team.shdsesc.stocksimul"
        subgraph "agent 패키지"
            AC[AgentController]
            AS[AgentService]
            ADTO[Predict/Portfolio DTOs]
            AS -->|uses| ChatModel[ChatModel<br/>Spring AI]
        end
        
        subgraph "auth 패키지"
            AF[ApiLoginFilter<br/>TokenCheckFilter]
            AH[Login Handlers]
            AJ[JwtTokenProvider]
        end
        
        subgraph "user 패키지"
            UC[UserController]
            US[UserService]
            UE[UserEntity]
            UR[UserRepository]
        end
        
        subgraph "userprofile 패키지"
            PC[UserProfileController]
            PS[UserProfileService]
            PE[UserProfileEntity]
            PR[UserProfileRepository]
        end
        
        subgraph "holdings 패키지"
            HC[HoldingsController]
            HS[HoldingsService]
            HE[HoldingsEntity]
            HR[HoldingsRepository]
        end
        
        subgraph "order 패키지"
            OC[OfferController]
            OS[OfferService]
            OE[OfferEntity]
            OR[OfferRepository]
        end
        
        subgraph "market 패키지"
            MC[MarketDataController<br/>DbCandleController]
            MS[DbMarketService]
            ME[Stock Entity<br/>Report Entity]
            MR[MarketStockRepository<br/>ReportRepository]
        end
        
        subgraph "news 패키지"
            NC[NewsController]
            NS[NewsService]
            NE[NewsEntity]
            NR[NewsRepository]
        end
        
        subgraph "redis 패키지"
            RC[RedisController]
            RDAO[StockRedisDAO<br/>RedisDAO]
        end
        
        subgraph "config 패키지"
            SC[SecurityConfig]
            QC[QueryDslConfig]
            RC2[RedisConfig]
            SWC[SwaggerConfig]
        end
    end
    
    AC --> AS
    UC --> US
    PC --> PS
    HC --> HS
    OC --> OS
    MC --> MS
    NC --> NS
    RC --> RDAO
    
    US --> UR
    PS --> PR
    HS --> HR
    OS --> OR
    MS --> MR
    NS --> NR
    
    AS --> MS
    AS --> ChatModel
```

## 데이터 흐름도 - 투자 점검 API

```mermaid
sequenceDiagram
    participant Client
    participant AgentController
    participant AgentService
    participant FastAPI
    participant GeminiAPI
    participant MarketService
    participant Redis
    
    Client->>AgentController: POST /api/agent/investment/review
    AgentController->>AgentService: getInvestmentReview()
    
    par 병렬 호출
        AgentService->>FastAPI: POST /predict
        FastAPI-->>AgentService: PredictResponseDTO
    and
        AgentService->>FastAPI: POST /portfolio/analysis
        FastAPI-->>AgentService: PortfolioResponseDTO
    end
    
    AgentService->>AgentService: buildInvestmentReviewPrompt()
    AgentService->>GeminiAPI: ChatModel.call()
    GeminiAPI-->>AgentService: 자연어 투자 점검 리포트
    
    AgentService->>MarketService: getCandles() (차트 데이터)
    MarketService->>Redis: 캐시 조회
    Redis-->>MarketService: 캐시 데이터
    MarketService-->>AgentService: CandleResponse
    
    AgentService-->>AgentController: InvestmentReviewResponseDTO
    AgentController-->>Client: JSON Response
```

## 엔티티 관계도

```mermaid
erDiagram
    UserEntity ||--o{ UserProfileEntity : "has"
    UserEntity ||--o{ UsersLikesEntity : "has"
    UserProfileEntity ||--o{ HoldingsEntity : "has"
    UserProfileEntity ||--o{ OfferEntity : "has"
    UserProfileEntity ||--o{ TimeLineEntity : "has"
    Stock ||--o{ HoldingsEntity : "referenced_by"
    Stock ||--o{ ReportEntity : "has"
    
    UserEntity {
        string usersId PK
        string email
        string password
        long lastProfileId
    }
    
    UserProfileEntity {
        long profileId PK
        string usersId FK
        string profileName
    }
    
    HoldingsEntity {
        long holdingsId PK
        long usersProfileId FK
        string ticker
        double quantity
    }
    
    OfferEntity {
        long offerId PK
        long usersProfileId FK
        string ticker
        double quantity
        string offerType
    }
    
    Stock {
        string ticker PK
        string name
    }
    
    NewsEntity {
        long newsId PK
        string ticker
        string title
        string content
    }
```

## 기술 스택 구조

```mermaid
graph TB
    subgraph "Spring Boot 3.5.5"
        Spring[Spring Framework]
        Security[Spring Security]
        DataJPA[Spring Data JPA]
        WebFlux[Spring WebFlux]
    end
    
    subgraph "AI & ML"
        SpringAI[Spring AI 1.1.1]
        Gemini[Google Gemini 2.0 Flash]
    end
    
    subgraph "Database"
        MariaDB[(MariaDB)]
        Redis[(Redis)]
    end
    
    subgraph "Query"
        QueryDSL[QueryDSL 5.0.0]
    end
    
    subgraph "External APIs"
        FastAPI[FastAPI<br/>Python ML Server]
        Finnhub[Finnhub API]
    end
    
    subgraph "Tools"
        Swagger[Swagger/OpenAPI]
        Lombok[Lombok]
        JWT[JWT]
    end
    
    Spring --> Security
    Spring --> DataJPA
    Spring --> WebFlux
    Spring --> SpringAI
    SpringAI --> Gemini
    DataJPA --> MariaDB
    DataJPA --> QueryDSL
    Spring --> Redis
    Spring --> Swagger
    Spring --> JWT
```

## 주요 API 엔드포인트

```mermaid
graph LR
    subgraph "인증/인가"
        A1[POST /api/auth/login]
        A2[POST /api/user/register]
        A3[POST /api/user/logout]
        A4[GET /api/user/me]
    end
    
    subgraph "AI 에이전트"
        B1[POST /api/agent/predict]
        B2[POST /api/agent/portfolio/analysis]
        B3[POST /api/agent/investment/review]
    end
    
    subgraph "사용자 프로필"
        C1[GET /api/userprofile/profiles/{email}]
        C2[GET /api/userprofile/profile/{pid}]
        C3[POST /api/userprofile/create]
        C4[PUT /api/userprofile/update]
    end
    
    subgraph "보유 주식"
        D1[GET /api/holdings/{profileId}]
        D2[POST /api/holdings]
        D3[PUT /api/holdings]
    end
    
    subgraph "주문"
        E1[POST /api/offer/update]
        E2[GET /api/offer/history/{usersProfileId}]
    end
    
    subgraph "시장 데이터"
        F1[GET /api/market/candles]
        F2[GET /api/market/symbols]
        F3[GET /api/market/tickers]
    end
    
    subgraph "뉴스"
        G1[GET /api/news/{ticker}]
    end
    
    subgraph "Redis"
        H1[GET /api/redis/status]
        H2[POST /api/redis/start]
        H3[POST /api/redis/stop]
    end
```
