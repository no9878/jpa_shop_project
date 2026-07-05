구현 기능

http://localhost:8080


GET

/api/members: 멤버관련 정보 조회  //관리자 권한


POST

/api/join: 멤버 등록 (파라미터: name,password)     //중복체크 기능 

/api/login: 멤버 로그인(파라미터: loginId,password)     

/api/item/new: 상품 생성   

     (공통 파라미터: category(1:Book,2:Album,3:Movie),name,price,stockQuantity
     
     항목별 파라미터:
     
     Book: author,isbn
     
     Album: artist,etc
     
     Movie: director,actor)      //관리자 권한 


PUT

/api/update/{id}: 멤버 이름 수정 (파라미터: name)  //관리자 권한

DELETE

/api/delete/{id}: 멤버 삭제 //관리자 권한



2026/7/1 첫 업로드

2026/7/2 로그인 기능,관리자 권한 체크,멤버 삭제,ApiResponse 형식 추가

2026/7/5 상품 생성기능,상품생성시 중복체크 기능 추가
