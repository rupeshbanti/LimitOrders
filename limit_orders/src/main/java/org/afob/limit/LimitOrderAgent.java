package org.afob.limit;

import org.afob.execution.ExecutionClient;
import org.afob.execution.ExecutionClient.ExecutionException;
import org.afob.prices.PriceListener;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

public class LimitOrderAgent implements PriceListener {
    private final ExecutionClient executionClient;
    private final List<Order> orderList;

    public LimitOrderAgent(final ExecutionClient executionClient) {
        this.executionClient = executionClient;
        this.orderList = new ArrayList<>();
    }

    @Override
    public void priceTick(String productId, BigDecimal price) {
        Iterator<Order> iterator = orderList.iterator();
        while (iterator.hasNext()) {
            Order order = iterator.next();
            if (order.getProductId().equals(productId)) {
                if (order.isBuy() && price.compareTo(order.getLimitPrice()) <= 0) {
                    processOrder(order);
                    iterator.remove(); 
                } else if (!order.isBuy() && price.compareTo(order.getLimitPrice()) >= 0) {         
              	processOrder(order);
                    iterator.remove(); 
                }
            }
        }
    }

    public void addOrder(boolean isBuy, String productId, int amount, BigDecimal limitPrice) {
    	orderList.add(new Order(isBuy, productId, amount, limitPrice));
    }

    private void processOrder(Order order) {
        try {
            if (order.isBuy()) {
                executionClient.buy(order.getProductId(), order.getAmount());
            } else {
                executionClient.sell(order.getProductId(), order.getAmount());
            }
        } catch (ExecutionException executionException) {
            System.err.println("Order processing failed: " + executionException.getMessage());
        }
    }

    /** Inner class for Order representation **/
    
    private static class Order {
        private final boolean isBuy;
        private final String productId;
        private final int amount;
        private final BigDecimal limitPrice;

        public Order(boolean isBuy, String productId, int amount, BigDecimal limitPrice) {
            this.isBuy = isBuy;
            this.productId = productId;
            this.amount = amount;
            this.limitPrice = limitPrice;
        }

        public boolean isBuy() {
            return isBuy;
        }

        public String getProductId() {
            return productId;
        }

        public int getAmount() {
            return amount;
        }

        public BigDecimal getLimitPrice() {
            return limitPrice;
        }
    }
}
