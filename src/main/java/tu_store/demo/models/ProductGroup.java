package tu_store.demo.models;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@Table(name = "product_groups")
public class ProductGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @OneToMany(mappedBy = "productGroup", cascade = CascadeType.ALL)
    private List<Product> products = new ArrayList<>();

    public ProductGroup(){};

    public void setName(String name) {
        this.name = name;
    }

    public void addProduct(Product product){
        this.products.add(product);
        product.setProductGroup(this);
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public List<Product> getProducts() {
        return products;
    }
}