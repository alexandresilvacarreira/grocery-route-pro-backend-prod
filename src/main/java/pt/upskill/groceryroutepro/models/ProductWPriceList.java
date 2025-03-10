package pt.upskill.groceryroutepro.models;

import pt.upskill.groceryroutepro.projections.ProductWPriceProjection;

import java.util.List;

public class ProductWPriceList {

    private List<ProductWPriceProjection> products;
    private boolean success;
    private String errorMessage;
    private Pagination pagination;

    public ProductWPriceList() {
    }

    public List<ProductWPriceProjection> getProducts() {
        return products;
    }

    public void setProducts(List<ProductWPriceProjection> products) {
        this.products = products;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }
}
