package com.enigcode.frozen_backend.production_orders.Model;

import com.enigcode.frozen_backend.batches.model.Batch;
import com.enigcode.frozen_backend.products.model.Product;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "production_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductionOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "production_orders_gen")
    @SequenceGenerator(name = "production_orders_gen", sequenceName = "production_orders_seq", allocationSize = 1)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_batch", referencedColumnName = "id")
    @NotNull
    private Batch batch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_product")
    @NotNull
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @NotNull
    private OrderStatus status;

    @Column(name = "validation_date")
    private OffsetDateTime validationDate;

    @NotNull
    @DecimalMin(value = "0.0")
    private Double quantity;

    @Column(name = "creation_date")
    @NotNull
    private OffsetDateTime creationDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private com.enigcode.frozen_backend.users.model.User createdByUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_user_id")
    private com.enigcode.frozen_backend.users.model.User approvedByUser;
}
