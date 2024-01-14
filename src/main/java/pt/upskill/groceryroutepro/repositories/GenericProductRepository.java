package pt.upskill.groceryroutepro.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pt.upskill.groceryroutepro.entities.GenericProduct;
import pt.upskill.groceryroutepro.entities.Product;
import pt.upskill.groceryroutepro.projections.ProductWPriceProjection;

import java.util.List;


@Repository
public interface GenericProductRepository extends JpaRepository<GenericProduct, Long> {


    @Query("SELECT p " +
            "FROM " +
            "GenericProduct p " +
            "JOIN p.categories c " +
            "JOIN p.chains ch " +
            "WHERE " +
            "p.name LIKE %:search% " +
            "AND c IN :categoryIds " +
            "AND ch IN :chainIds")
    Slice<GenericProduct> findGenericProductByParams (
            @Param("search") String search,
            @Param("categoryIds") List<Long> categoryIds,
            @Param("chainIds") List<Long> chainIds,
            Pageable pageable
    );

}
