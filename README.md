# 회원, 상품, 주문 REST API 서버
회원가입부터 상품 검색, 주문까지 처리하는 쇼핑몰 백엔드 API 서버입니다.
동시 주문 환경에서 재고 정합성을 지키는 데 중점을 두고, 문제를 테스트로 재현하고 검증하며 개선했습니다.

## 기술 스택

Java 17, Spring Boot, Spring Data JPA, Querydsl, MySQL, Docker Compose, JUnit5, MockMvc, BCrypt

## 핵심 문제 해결

- [주문 트랜잭션 개선: 재고 불일치와 커넥션 풀 고갈 해결](docs/order-transaction.md)
- [데드락 해결: 원인 분석과 Lock 획득 순서 고정](docs/dead-lock.md)

## 실행 방법

MySQL 컨테이너를 먼저 실행합니다.

```bash
docker compose up -d
```

애플리케이션을 실행합니다.

```bash
./gradlew bootRun
```

- 주소: http://localhost:8080
- `ddl-auto: create` 설정으로 실행할 때마다 테이블과 데이터가 초기화됩니다.
- 초기 데이터는 `InitDb` 클래스에서 생성됩니다.
- local 프로필에서는 로그인 세션이 없으면 기본 ADMIN 계정 세션이 자동 적용됩니다.

## 테스트

MySQL 컨테이너가 실행 중이어야 합니다.

```bash
./gradlew test
```

## API 명세

응답형식: ApiResponse 클래스로 통일.

요청형식: 메서드별 상이.

에러: CustomStatusException 클래스 생성 -> GlobalExceptionHandler로 관리.


GET

/api/members: 멤버관련 정보 조회  //관리자 권한

/api/items: 모든 상품 정보 조회 //관리자 권한

/api/orders: 모든 주문 정보 조회 //관리자 권한

/api/member-orders: 로그인한 멤버의 주문 조회 

/api/search: 상품 검색

    (파라미터:

    String itemName 이름(포함),

    String categoryName 카테고리이름(완전일치),

    Integer maxPrice 최대가격,

    Integer minPrice 최소가격,
    
    String sortFilter 정렬조건(oldDate 등록 오래된 순,newDate 등록 최신순,highPrice 가격높은순,
    lowPrice 가격낮은순))

POST

/api/join: 멤버 등록 (파라미터: name,password,city,street,zipcode)     //중복체크 기능 

/api/login: 멤버 로그인(파라미터: loginId,password)     

/api/category/new: 카테고리 생성  //관리자 권한

    카테고리 생성
    필수파라미터: name
    선택파라미터: isChild,parentId

/api/item/new: 상품 생성 

    (공통 파라미터: category(2:Book,3:Album,4:Movie),categoryIds(1:ITEM,2:BOOK,3:ALBUM,4:MOVIE),name,price,stockQuantity
     
     항목별 파라미터:
     
     Book: author,isbn
     
     Album: artist,etc
     
     Movie: director,actor)      //관리자 권한 

/api/order/new: 주문 생성

파라미터: address,orderItems(itemName,categoryName(최하위 카테고리 입력),quantity)


PUT

/api/update/{id}: 멤버 이름 수정 (파라미터: name)  //관리자 권한

DELETE

/api/delete/{id}: 멤버 삭제 //관리자 권한


## 변경 이력

2026/7/1 첫 업로드

2026/7/2 로그인 기능,관리자 권한 체크,멤버 삭제,ApiResponse 형식 추가

2026/7/5 카테고리 생성,상품 생성,상품생성시 중복체크,필수 입력항목 체크 추가

2026/7/9 모든 상품 정보 조회,주문생성,모든주문조회 추가

2026/7/10 특정멤버 주문조회 추가

2026/7/13 상품 검색 추가

2026/7/16 코드 수정으로인한 side effect의 확인을 용이하게 하기위해 MockMvc를 활용한 회원가입, 로그인, 주문 API 테스트 코드 작성.
동시에 주문시 재고부족문제+그로인한 주문 동시생성 방지를 위해 Lock 적용(비관적/낙관적 Lock 검토 후 비관적 Lock 적용).

2026/8/17 실행시 기본 local 프로필로 실행되도록 변경, 별도의 H2 데이터베이스 서버 실행 없이 Spring Boot 실행만으로 데이터베이스가 생성됩니다.(10/31 MySQL로 변경되어 현재는 해당 없음)
남아있던 기존 실습의 화면 관련 코드 및 Thymeleaf 의존성 삭제.

2026/10/30 비밀번호 BCrypt 해시 저장으로 변경
- 기존: 비밀번호를 평문으로 저장하고 equals로 비교
- 변경: 가입 시 BCryptPasswordEncoder로 암호화 해 저장, 로그인 시 matches()로 검증

주문 트랜잭션 통합(REQUIRES_NEW 제거), 락 순서 고정, 동시성·롤백 테스트 추가
- 참고: OptimisticItemLock은 현재 비활성(단일 트랜잭션 구조와 맞지 않음)
- 상세: [주문 트랜잭션 개선 과정](docs/order-transaction.md)

2026/10/31
- H2 데이터 베이스를 MySQL로 변경적용.
- 서로 다른 사람이 상품을 교차순서로 동시주문할 시 발생할수 있는 데드락 문제 해결. 
- 상세: [데드락 해결과정](docs/dead-lock.md)
- Item 엔티티의 name 컬럼에 인덱스 추가(주문 시 상품 이름 조회의 풀스캔 개선).

2026/11/1
- Lock 대기시간 설정을 'innodb_lock_wait_timeout'으로 변경.
- 주문 상품 정렬 기준에 카테고리를 추가해 이름이 같은 상품의 Lock 순서도 고정.