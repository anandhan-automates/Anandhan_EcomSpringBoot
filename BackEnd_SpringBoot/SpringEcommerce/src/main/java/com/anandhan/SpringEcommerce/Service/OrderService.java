package com.anandhan.SpringEcommerce.Service;

import com.anandhan.SpringEcommerce.Model.Dto.OrderItemRequest;
import com.anandhan.SpringEcommerce.Model.Dto.OrderItemResponse;
import com.anandhan.SpringEcommerce.Model.Dto.OrderRequest;
import com.anandhan.SpringEcommerce.Model.Dto.OrderResponse;
import com.anandhan.SpringEcommerce.Model.Order;
import com.anandhan.SpringEcommerce.Model.OrderItem;
import com.anandhan.SpringEcommerce.Model.Product;
import com.anandhan.SpringEcommerce.Repo.OrderRepo;
import com.anandhan.SpringEcommerce.Repo.ProductRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    @Autowired
    private ProductRepo productRepo;
    @Autowired
    private OrderRepo orderRepo;

    public OrderResponse placeOrder(OrderRequest request) {
        // Create a new Order entity object
        Order order = new Order();

        // Generate a unique Order ID using UUID
        // Example: ORD8F3A2B1C
        // We take first 8 characters and convert to uppercase for readability
        String orderId = "ORD"+ UUID.randomUUID().toString().substring(0,8).toUpperCase();
        order.setOrderId(orderId);

        // Set customer name received from request DTO
        String customerName= request.customerName();
        order.setCustomerName(customerName);

        // Set customer email received from request DTO
        String email = request.email();
        order.setEmail(email);

        // Set initial order status
        order.setStatus("PLACED");

        // Set current date as order creation date
        order.setOrderDate(LocalDate.now());

        // Create a list to hold all OrderItem entities for this order
        List<OrderItem> orderItems = new ArrayList<>();

        // Loop through each item sent in the OrderRequest (coming from frontend)
        for (OrderItemRequest itemReq : request.items()) {

            // Fetch product from database using productId from request
            // If product is not found, throw exception
            Product product = productRepo.findById(itemReq.productId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            // -------------------------
            // BUSINESS LOGIC SECTION
            // -------------------------

            // Reduce product stock based on ordered quantity
            // Example: If stock = 10 and user orders 2, new stock = 8
            product.setStockQuantity(
                    product.getStockQuantity() - itemReq.quantity()
            );

            // Save updated product back to database
            // This ensures stock update is persisted
            productRepo.save(product);

            // -------------------------
            // ENTITY CREATION SECTION
            // -------------------------

            // Create OrderItem entity
            // Each OrderItem represents one product inside an order
            OrderItem orderItem = OrderItem.builder()

                    // Set product reference (ManyToOne relationship)
                    .product(product)

                    // Set ordered quantity
                    .quantity(itemReq.quantity())

                    // Calculate total price = product price × quantity
                    .totalPrice(
                            product.getPrice()
                                    .multiply(BigDecimal.valueOf(itemReq.quantity()))
                    )

                    // Set reference to parent Order (ManyToOne relationship)
                    // This creates foreign key link to Order table
                    .order(order)

                    .build();

            // Add created OrderItem to list
            orderItems.add(orderItem);
            }

        // -------------------------
        // RELATIONSHIP MAPPING
        // -------------------------

        // Set list of orderItems inside Order entity
        // This completes OneToMany relationship from Order side
        order.setOrderItems(orderItems);

        // -------------------------
        // DATABASE SAVE SECTION
        // -------------------------

        // Save Order entity into database
        // Because of CascadeType.ALL, all OrderItems will also be saved automatically
        Order savedOrder = orderRepo.save(order);

        // -------------------------
        // DTO MAPPING SECTION
        // -------------------------

        // Create list to hold response items (Entity → DTO conversion)
        List<OrderItemResponse> itemResponses = new ArrayList<>();

        // Convert each OrderItem entity into OrderItemResponse DTO
        for (OrderItem item : order.getOrderItems()) {

            OrderItemResponse orderItemResponse = new OrderItemResponse(

                    // Product name (not entire product object)
                    item.getProduct().getName(),

                    // Ordered quantity
                    item.getQuantity(),

                    // Calculated total price
                    item.getTotalPrice()
            );

            itemResponses.add(orderItemResponse);
        }
        // Create final OrderResponse DTO
        OrderResponse orderResponse = new OrderResponse(

                savedOrder.getOrderId(),
                savedOrder.getCustomerName(),
                savedOrder.getEmail(),
                savedOrder.getStatus(),
                savedOrder.getOrderDate(),
                itemResponses
        );
        // Return response back to controller
        return orderResponse;


    }

    // Transactional is used because we are accessing lazy-loaded relationships
    // (order.getOrderItems() and item.getProduct())
    // It ensures session is open while fetching related data
    @Transactional
    public List<OrderResponse> getAllOrderResponses() {

        // Step 1: Fetch all Order entities from database
        // This gets data from "orders" table
        List<Order> orders = orderRepo.findAll();

        // Step 2: Create empty list to store final response DTOs
        List<OrderResponse> orderResponses = new ArrayList<>();

        // Step 3: Loop through each Order entity
        for (Order order : orders) {

            // For each order, we need to convert its OrderItems into DTOs
            // So create a list to store item responses
            List<OrderItemResponse> itemResponses = new ArrayList<>();

            // Step 4: Loop through each OrderItem inside current order
            // (OneToMany relationship: Order -> OrderItems)
            for (OrderItem item : order.getOrderItems()) {

                // Convert OrderItem entity to OrderItemResponse DTO
                OrderItemResponse orderItemResponse = new OrderItemResponse(

                        // Get product name from Product entity
                        // (OrderItem -> Product ManyToOne relationship)
                        item.getProduct().getName(),

                        // Get ordered quantity
                        item.getQuantity(),

                        // Get total price for that item
                        item.getTotalPrice()
                );

                // Add converted item DTO to itemResponses list
                itemResponses.add(orderItemResponse);
            }

            // Step 5: Convert Order entity to OrderResponse DTO
            OrderResponse orderResponse = new OrderResponse(

                    order.getOrderId(),
                    order.getCustomerName(),
                    order.getEmail(),
                    order.getStatus(),
                    order.getOrderDate(),
                    itemResponses   // attach converted item DTO list
            );

            // Add completed OrderResponse to final list
            orderResponses.add(orderResponse);
        }

        // Step 6: Return list of OrderResponse DTOs to controller
        return orderResponses;
    }

}
