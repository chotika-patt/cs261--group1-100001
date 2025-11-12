package tu_store.demo.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import tu_store.demo.models.*;
import tu_store.demo.repositories.*;
import tu_store.demo.services.*;

@Service
public class DatabaseFixService {

    @Autowired 
    private ProductRepository productRepository;


    public void update(){
        fixProductNullValue();
    }


    @Transactional
    private void fixProductNullValue(){
        List<Product> products = productRepository.findAllByRatingAvgIsNullOrRatingCountIsNull();

        for (Product product : products) {
            product.setRatingCount(0);
            product.setRatingAvg(0.0);

            productRepository.save(product);
        }
    }
}
