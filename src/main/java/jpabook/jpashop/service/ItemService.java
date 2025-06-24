package jpabook.jpashop.service;

import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    @Transactional
    public void saveItem(Item item) {
        itemRepository.save(item);
    }

    /**
     * 8. 웹 계층 개발 - 변경 감지와 병합(merge)
     *   - 준영속 엔티티를 수정하는 방법 2가지
     *     - 변경 감지 기능 사용
     *     - 병합(merge) 사용
     *   - 아래는 변경 감지 기능 사용
     *   <b>주의:
     *   변경 감지 기능을 사용하면 원하는 속성만 선택해서 변경할 수 있지만, 병합(merge)을 사용하면 모든 속성이 변경된다.
     *   병합시 값이 없으면 `null` 로 업데이트 할 위험도 있다. (병합은 모든 필드를 교체한다.)</b>
     */
    @Transactional
    public void updateItem(Long id, String name, int price, int stockQuantity) { // 파라미터로 넘어온 준영속 엔티티
        // 같은 엔티티를 조회(persistItem 변수는 em.find()로 찾아온 변수이기 때문에 영속 대상)
        Item persistItem = itemRepository.findOne(id);

        // 데이터 수정 및 UPDATE 쿼리 발생
        persistItem.setName(name);
        persistItem.setPrice(price);
        persistItem.setStockQuantity(stockQuantity);
    }

    public List<Item> findItems() {
        return itemRepository.findAll();
    }

    public Item findItem(Long id) {
        return itemRepository.findOne(id);
    }
}
