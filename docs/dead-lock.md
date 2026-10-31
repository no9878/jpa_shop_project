데드락 해결 과정

문제 발생 상황: 고객 A가 상품1, 상품2를 고객 B가 상품2, 상품1을 동시에 주문 하였을때 고객A가 상품1의 락을 획득 후 상품2의 락을 획득하려 하나 
고객 B가 상품 2의 락을 획득하였고 마찬가지로 상품1 락을 획득하려 하므로 교차대기로 인해 데드락 발생.

기존 락 순서 정렬의 효과를 검증하기 위해 정렬 코드를 주석 처리하고,
락 대기가 겹치도록 재고 차감 전 Thread.sleep(100)을 임시로 추가한 뒤 테스트 코드 작성 후 실행

실행 결과 테스트가 통과됨(데드락이 발생하지 않음).

원인을 찾기 위해 MySQL 확인.

![img.png](images/dead-lock/img.png)

performance_schema.data_locks 확인 결과, 상품 하나를 조회해도 모든 상품 행에 락이 걸리고 다른 트랜잭션은 대기하는 것을 확인.

원인: item.name에 인덱스가 없어 풀스캔이 발생하고, 조회 과정에서 읽은 행까지 락이 걸림. 
결과적으로 주문이 한 건씩 순서대로 처리되어 데드락이 발생하지 않고 테스트가 통과함. 

해결: 락 없이 상품 id를 먼저 조회한 뒤, PK로 해당 상품 행에만 락을 걸도록 쿼리 분리.

기존

![img_1.png](images/dead-lock/img_1.png)

변경

![img_2.png](images/dead-lock/img_2.png)

다시 테스트 실행

![img_3.png](images/dead-lock/img_3.png)
![img6.png](images/dead-lock/img6.png)


주문 성공이 1건으로 테스트 실패.
콘솔에서 "Deadlock found when trying to get lock" 예외 확인.
MySQL이 데드락을 감지해 한쪽 트랜잭션을 롤백하고, 나머지 트랜잭션만 커밋됨.

해결을 위해 상품 이름순으로 락 획득순서 정렬.

![img_4.png](images/dead-lock/img_4.png)


테스트 실행

![img_5.png](images/dead-lock/img_5.png)

테스트 성공 확인.

검증 후 재현용 Thread.sleep 제거, OrderServiceTest 전체 3개 테스트 통과 확인.

이후 데드락 해결과 별개로, 주문 시 상품 이름 조회의 풀스캔을 개선하기 위해 name 컬럼에 인덱스를 추가함.