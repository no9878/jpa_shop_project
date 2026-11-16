# Lock 전략 비교와 잠금 범위 확인

주문의 재고 차감에 어떤 방식을 쓸지 정하려고, 같은 조건에서 네 가지 방식을 측정하고 MySQL에서 실제로 잠기는 행을 확인함.

## 1. Lock 전략 비교

### 측정 환경

- 로컬 MySQL 8.4(Docker Compose), 격리 수준 기본값(REPEATABLE READ), `innodb_lock_wait_timeout` 3초
- 재고 20 · 동시 주문 20건 (재고 = 주문 수로 맞춰, 실패가 나면 재고 부족이 아니라 동시성 문제라는 것이 바로 보이게 함)
- 20개 스레드를 `CountDownLatch`로 동시에 출발시키고 성공 · 재고 부족 · 재시도 소진 · 기타 실패 건수와 최종 재고를 확인
- 모든 방식은 같은 경로(트랜잭션 바깥 `OrderFacade` — 버전 충돌 · 데드락 · Lock 대기 초과 시 최대 10회 재시도)로 측정
- 방식마다 5회 반복

### 결과

| 방식 | 성공 | 재고 차감 누락 | 재시도 | 소요 시간 |
|---|---|---|---|---|
| Lock 없음 | 20 | **10~13건** | 주문당 최대 4~5회 | 약 950ms |
| 낙관적 Lock (`@Version`) | 20 | 0 | 총 57~64회, 주문당 최대 8~9회 | 약 1420ms |
| 비관적 Lock (`SELECT … FOR UPDATE`) | 20 | 0 | 0 | 약 900ms |
| 조건부 UPDATE (`stock = stock - ? WHERE stock >= ?`) | 20 | 0 | 0 | 약 900ms |

- 재고 차감 누락 = 성공 건수 − (20 − 최종 재고)
- Lock 없음은 20건이 모두 성공했지만 재고는 7~10개만 줄어, 앞선 차감이 덮어써짐(Lost Update)

### 낙관적 Lock을 동작하게 만들기까지

처음 구현은 재시도가 한 번도 일어나지 않았음.

1. **재시도 위치**: 상품 단위로 재시도했는데, 상품 차감은 주문 트랜잭션에 합류해 있어 버전 충돌은 주문 트랜잭션이 커밋할 때 발생함. 재시도 루프를 이미 빠져나온 뒤라 예외를 잡지 못함
   → 트랜잭션 바깥(`OrderFacade`)에서 **주문 전체를 새 트랜잭션으로** 재시도하도록 변경. 시도마다 새 영속성 컨텍스트에서 최신 버전을 다시 읽음
2. **데드락**: 버전 비교 전에 데드락이 남. Hibernate는 커밋 시 INSERT를 UPDATE보다 먼저 보내는데, 주문상품 INSERT의 FK 검사가 상품 행에 공유(S) Lock을 걸고, 이어지는 재고 UPDATE가 배타(X) Lock으로 올리려다 두 주문이 서로를 기다림
   → 주문 저장 전에 `itemRepository.flush()`로 재고 UPDATE를 먼저 보내, 처음부터 X Lock을 잡게 함
3. **재시도 간격**: 50ms + 0~50ms 무작위 값으로, 실패한 주문들이 동시에 다시 몰리지 않게 함

### 재시도 횟수에 대해 관찰한 것

- 늦게 성공한 주문일수록 시도 횟수가 늘어남. 누군가 성공해 버전이 올라갈 때마다, 같은 버전을 들고 기다리던 주문들이 모두 충돌하기 때문
- 재고 20 · 동시 주문 400 · 최대 20회에서는 재시도 소진 실패가 0건. 버전이 올라가는 횟수(= 성공할 수 있는 주문 수)가 재고만큼뿐이라, 주문 수를 늘려도 늘어나는 건 재고 부족 실패
- 재고를 30으로 늘리고 최대 재시도를 5회로 줄이자, 재고가 남았는데도 재시도를 다 써서 실패하는 주문이 생김
- 즉 재시도 소진은 동시 요청 수가 아니라 **한 상품에서 성공하는 주문 수와 최대 재시도 횟수의 관계**로 정해짐

### 선택: 비관적 Lock 유지

- 낙관적 Lock: 정합성은 지켰지만, 한 상품에 주문이 몰리면 재시도가 쌓여 느려지고(1420ms) 재시도를 다 쓰면 실패할 수 있음
- 조건부 UPDATE: 비관적 Lock과 결과 · 시간이 같았음. 하지만 재고 규칙이 엔티티(`Item.removeStock`)에서 SQL로 옮겨 가, 회원당 구매 제한 같은 조건이 늘면 쿼리가 복잡해지고 취소(`addStock`)와도 방식이 갈라짐
- 그래서 규칙을 엔티티에 둘 수 있는 **비관적 Lock**을 유지. Lock 대기가 병목으로 측정되면 조건부 UPDATE로 바꾸는 것을 대안으로 둠
- 비교용 코드(`@Version` 주석, `decreaseStock`, `createOrderItemQuery`)는 재측정할 수 있도록 남겨 둠

## 2. 잠금 범위 확인 (`performance_schema.data_locks`)

[데드락 해결](dead-lock.md)에서 상품 조회와 Lock을 분리했는데, 쿼리를 나누지 않고 `name` 인덱스만 추가해도 되지 않았는지 확인함. 같은 조건의 SQL을 MySQL 세션 두 개로 직접 실행하고 `data_locks`를 조회함.

### 분리 전 쿼리 + `name` 인덱스

```sql
START TRANSACTION;
SELECT i.item_id
  FROM item i
  JOIN category_item ci ON ci.item_id = i.item_id
  JOIN category c       ON c.category_id = ci.category_id
 WHERE i.name = 'SPRING1 BOOK' AND c.name = 'BOOK'
 LIMIT 1
 FOR UPDATE;
```

| object_name | index_name | lock_mode | lock_data |
|---|---|---|---|
| item | idx_item_name | X | 'SPRING1 BOOK', 3 |
| item | idx_item_name | X,GAP | 'SPRING2 BOOK', 4 |
| item | PRIMARY | X,REC_NOT_GAP | 3 |
| category | PRIMARY | X | 1 |
| category | PRIMARY | X | 2 |
| category_item | FK(item_id) | X | 3, 5 / 3, 6 |
| category_item | PRIMARY | X,REC_NOT_GAP | 5 / 6 |

- 상품 행은 주문한 1개만 잠겼지만, `category.name`에 인덱스가 없어 **category 행이 전부(2개)** 잠김
- 다른 세션에서 **다른 상품**(SPRING2 BOOK)을 같은 방식으로 잠그자, 상품 4번 행을 먼저 잡은 뒤 category 1번 행에서 대기함
- 이어서 첫 세션이 상품 4번을 요청하자 `ERROR 1213 Deadlock found` — MySQL이 item을 먼저, category를 나중에 잠그는 순서로 실행해 교차 대기가 생김

### 현재 방식: Lock 없이 ID 조회 → PK로 item만 잠금

```sql
START TRANSACTION;
SELECT * FROM item WHERE item_id = 3 FOR UPDATE;
```

| object_name | index_name | lock_mode | lock_data |
|---|---|---|---|
| item | PRIMARY | X,REC_NOT_GAP | 3 |

- 주문한 상품 행 1개만 잠김. category · category_item 잠금 없음
- 다른 세션에서 상품 4번을 잠그자 대기 없이 바로 성공(0.00 sec)
- 두 세션이 3 → 4, 4 → 3 순서로 교차해 잠그면 데드락이 남 → **상품 ID 순으로 정렬해 모든 주문이 같은 순서로 잠그도록 함**

### 정리

| 방식 | 주문 1건이 잠그는 범위 | 다른 상품 주문 | 교차 순서 주문 |
|---|---|---|---|
| 조인 + `FOR UPDATE`, 인덱스 없음 | item **전체 행** + supremum | 대기 | 순차 처리 → **데드락이 가려짐** |
| 조인 + `FOR UPDATE`, `name` 인덱스만 | item 1행 + **category 전체** + 연결 행 + 인덱스 간격 | **대기** (category) | **데드락** |
| ID 조회 후 PK로 item만 잠금 | **item 1행만** | **대기 없음** | 정렬 없으면 데드락 → **ID 순 정렬로 방지** |

- 첫 번째 행의 잠금 범위는 [데드락 해결](dead-lock.md)의 캡처 기준이고, "다른 상품 주문 → 대기"는 전체 행이 잠기는 것으로 판단함
- 직접 실행한 SQL은 같은 조건으로 손으로 쓴 것이라, Hibernate가 만든 SQL과 가져오는 컬럼 등은 다를 수 있음

## 3. 데드락 재시도와 순서 고정

- `OrderFacade`는 데드락도 재시도함. 정렬을 끄고 Facade로 교차 주문을 실행하면 한 주문은 1회, 다른 주문은 2회 시도로 둘 다 성공함
- 하지만 재시도는 롤백된 작업을 처음부터 다시 하고, 대기 시간이 붙고, 정해진 횟수 안에 성공한다는 보장이 없음. 또 재시도가 데드락을 가려 정렬이 빠져도 테스트가 통과함
- 그래서 **Lock 순서 고정을 기본**으로 하고 재시도는 예상하지 못한 데드락에 대한 대비책으로 둠
- 데드락 테스트는 재시도 없이 `OrderService`를 직접 호출해, 정렬이 빠지면 실패하도록 함