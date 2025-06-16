package com.ambev.order.infrastructure.persistence.mongo;

import com.ambev.order.infrastructure.persistence.entity.OrderDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MongoOrderSpringRepository extends MongoRepository<OrderDocument, String> {
}
