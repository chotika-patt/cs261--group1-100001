package tu_store.demo.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tu_store.demo.dto.ProductResponse;
import tu_store.demo.models.Category;
import tu_store.demo.models.Product;
import tu_store.demo.models.ProductGroup;
import tu_store.demo.models.ProductStatus;
import tu_store.demo.models.User;
import tu_store.demo.models.UserRole;
import tu_store.demo.repositories.ProductGroupRepository;
import tu_store.demo.repositories.ProductRepository;
import tu_store.demo.repositories.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductGroupService {
    @Autowired
    private ProductGroupRepository productGroupRepository;

    @Autowired
    private ProductRepository productRepository;

    public ProductGroup createProductGroup(List<Product> products){
        ProductGroup group = new ProductGroup();
        group.setProducts(products);

        productGroupRepository.save(group);

        return group;
    }

    public void addProductToGroup(ProductGroup group, Product product){
        if(group == null || product == null ) return;

        if(group.getProducts().contains(product)) return;

        ProductGroup oldGroup = product.getProductGroup();

        group.addProduct(product);
        productGroupRepository.save(group);

        if(oldGroup == null) return;
        if(oldGroup.getProducts().isEmpty()) productGroupRepository.delete(oldGroup);
    }

    public void removeProductFromGroup(ProductGroup group, Product product){
        if(group == null || product == null ) return;

        group.getProducts().remove(product);
        productGroupRepository.save(group);

        if(group.getProducts().isEmpty()) productGroupRepository.delete(group);
    }
}
