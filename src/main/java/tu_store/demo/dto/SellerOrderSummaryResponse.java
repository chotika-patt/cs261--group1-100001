package tu_store.demo.dto;

public class SellerOrderSummaryResponse  {
    private Long totalOrders;
    private Double totalSales;
    private Double pendingPayments;

    public SellerOrderSummaryResponse(){}

    public SellerOrderSummaryResponse(Long totalOrders, Double totalSales, Double pendingPayments) {
        this.totalOrders = totalOrders;
        this.totalSales = totalSales;
        this.pendingPayments = pendingPayments;
    }

    public Double getPendingPayments() {
        return pendingPayments;
    }

    public Long getTotalOrders() {
        return totalOrders;
    }

    public Double getTotalSales() {
        return totalSales;
    }
    public void setPendingPayments(Double pendingPayments) {
        this.pendingPayments = pendingPayments;
    }

    public void setTotalOrders(Long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public void setTotalSales(Double totalSales) {
        this.totalSales = totalSales;
    }

}
