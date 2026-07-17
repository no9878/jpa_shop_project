구현 기능

http://localhost:8080

응답형식: APIResponse 클래스로 통일.

요청형식: 메서드별 상이.

에러: CustomStatusException 클래스 생성 -> GlobalExceptionHandler로 관리.

초기 데이터 존재(InitDb 클래스)

로그인세션 없을시 기본 ADMIN 계정 로그인세션 적용.

GET

/api/members: 멤버관련 정보 조회  //관리자 권한

/api/items: 모든 상품 정보 조회 //관리자 권한

/api/orders: 모든 주문 정보 조회

/api/member-orders: 특정멤버 주문 조회 

/api/search: 상품 검색

    (파라미터:

    String itemName 이름(포함),

    String categoryName 카테고리이름(완전일치),

    Integer maxPrice 최대가격,

    Integer minPrice 최소가격,
    
    String sortFilter 정렬조건(oldDate 등록 오래된 순,newDate 등록 최신순,highPrice 가격높은순,
    lowPrice 가격낮은순)

POST

/api/join: 멤버 등록 (파라미터: name,password,city,street,zipcode)     //중복체크 기능 

/api/login: 멤버 로그인(파라미터: loginId,password)     

/api/item/new: 상품 생성 

    (공통 파라미터: category(1:Book,2:Album,3:Movie),name,price,stockQuantity
     
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



2026/7/1 첫 업로드

2026/7/2 로그인 기능,관리자 권한 체크,멤버 삭제,ApiResponse 형식 추가

2026/7/5 상품 생성,상품생성시 중복체크,필수 입력항목 체크 추가

2026/7/9 모든 상품 정보 조회,주문생성,모든주문조회 추가

2026/7/10 특정멤버 주문조회 추가

2026/7/13 상품 검색 추가

2026/7/16 코드 수정으로인한 side effect의 확인을 용이하게 하기위해 MockMvc를 활용한 각 컨트롤러의 api testCode를 
작성. 동시에 주문시 재고부족문제+그로인한 주문 동시생성 방지를 위해 Lock 적용(비관적,낙관적 Lock).
