package com.be.java.foxbase.repository;

import com.be.java.foxbase.db.entity.Order;
import com.be.java.foxbase.utils.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    Optional<Order> findByAppTransId(String appTransId);
    
    List<Order> findByUser_Username(String username);
    
    List<Order> findByStatusAndWebhookReceived(OrderStatus status, Boolean webhookReceived);
    
    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.queryAttempts < :maxAttempts AND o.createdAt > :since")
    List<Order> findPendingOrdersForQuery(
        @Param("status") OrderStatus status,
        @Param("maxAttempts") Integer maxAttempts,
        @Param("since") LocalDateTime since
    );
    
    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.expiredAt < :now")
    List<Order> findExpiredOrders(
        @Param("status") OrderStatus status,
        @Param("now") LocalDateTime now
    );
}

