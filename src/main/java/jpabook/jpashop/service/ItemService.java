package jpabook.jpashop.service;


import jpabook.jpashop.domain.item.Book;
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
    public void saveItem(Item item){
        itemRepository.save(item);
    }

    @Transactional
    public void updateItem(Long itemId,String name,int price,int stockQuantity){

        Item findItem=itemRepository.findOne(itemId); //영속상태
       findItem.setName(name);
       findItem.setPrice(price);
       findItem.setStockQuantity(stockQuantity);
        //
        //itemRepository.save(findItem); 할필요없음.
        /* 실무에서는 위처럼 setter 하지말고 의미있는 메소드를 만들어 활용하자 */
    }



    public List<Item> findItems(){
        return itemRepository.findAll();
    }

    public Item findOne(Long itemid){
        return itemRepository.findOne(itemid);
    }

}
