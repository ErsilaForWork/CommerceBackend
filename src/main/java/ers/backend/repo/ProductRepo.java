package ers.backend.repo;

import ers.backend.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepo extends JpaRepository<Product, Long> {

    @Query("select product from Product product where product.name ilike concat('%',:keyword,'%') or product.brand ilike concat('%',:keyword,'%') or product.description ilike concat('%',:keyword,'%')")
    List<Product> search(@Param("keyword") String keyword);

}
