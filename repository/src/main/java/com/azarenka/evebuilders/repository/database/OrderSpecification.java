package com.azarenka.evebuilders.repository.database;

import com.azarenka.evebuilders.domain.db.DistributedOrder;
import com.azarenka.evebuilders.domain.db.Order;
import com.azarenka.evebuilders.domain.db.OrderFilter;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class OrderSpecification {

    public static Specification<Order> withFilter(OrderFilter filter) {
        return (root, query, cb) -> {
            Predicate predicate = cb.conjunction();
            if (filter.getStatuses() != null && !filter.getStatuses().isEmpty()) {
                predicate = cb.and(predicate, root.get("orderStatus").in(filter.getStatuses()));
            }
            if (filter.getTypesOrder() != null && !filter.getTypesOrder().isEmpty()) {
                predicate = cb.and(predicate, root.get("category").in(filter.getTypesOrder()));
            }
            if (filter.getMinFreeCount() != null && filter.getMinFreeCount() > 0) {
                Expression<Integer> countExpr = root.get("count");
                Expression<Integer> inProgressExpr = root.get("inProgressCount");
                Expression<Integer> freeExpr = cb.diff(countExpr, inProgressExpr);
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(freeExpr, filter.getMinFreeCount()));
            }
            if (filter.isDistributed() != null) {
                if (filter.isDistributed()) {
                    Expression<Integer> diff = cb.diff(root.get("count"), root.get("inProgressCount"));
                    predicate = cb.and(predicate, cb.equal(diff, 0));
                } else {
                    Expression<Integer> diff = cb.diff(root.get("count"), root.get("inProgressCount"));
                    predicate = cb.and(predicate, cb.greaterThan(diff, 0));
                }
            }
            return predicate;
        };
    }

    public static Specification<DistributedOrder> withDistributedFilter(OrderFilter filter) {
        return (root, query, cb) -> {
            Predicate predicate = cb.conjunction();
            if (filter.getUserId() != null && !filter.getUserId().isEmpty()){
                predicate = cb.and(predicate, cb.equal(root.get("userName"), filter.getUserId()));
            }
            if (filter.getStatuses() != null && !filter.getStatuses().isEmpty()) {
                predicate = cb.and(predicate, root.get("orderStatus").in(filter.getStatuses()));
            }
            if (filter.getTypesOrder() != null && !filter.getTypesOrder().isEmpty()) {
                predicate = cb.and(predicate, root.get("category").in(filter.getTypesOrder()));
            }
            if (filter.getMinFreeCount() != null && filter.getMinFreeCount() > 0) {
                Expression<Integer> countExpr = root.get("count");
                Expression<Integer> inProgressExpr = root.get("inProgressCount");
                Expression<Integer> freeExpr = cb.diff(countExpr, inProgressExpr);
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(freeExpr, filter.getMinFreeCount()));
            }
            if (filter.isDistributed() != null && filter.isDistributed()) {
                predicate = cb.and(predicate, cb.equal(root.get("count"), root.get("inProgressCount")));
            }
            return predicate;
        };
    }
}
