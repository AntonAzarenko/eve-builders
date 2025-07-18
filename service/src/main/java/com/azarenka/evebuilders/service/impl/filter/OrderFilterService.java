package com.azarenka.evebuilders.service.impl.filter;

import com.azarenka.evebuilders.domain.db.OrderFilter;
import com.azarenka.evebuilders.repository.database.properties.IOrderFilterRepository;
import com.azarenka.evebuilders.service.api.IOrderFilterService;
import com.azarenka.evebuilders.service.impl.auth.SecurityUtils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class OrderFilterService implements IOrderFilterService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderFilterService.class);

    @Autowired
    private IOrderFilterRepository repository;

    @Override
    @Transactional
    public void saveFilter(OrderFilter orderFilter) {
        if (StringUtils.isEmpty(orderFilter.getUserId())) {
            var userName = SecurityUtils.getUserName();
            orderFilter.setUserId(userName);
        }
        LOGGER.info("Saving filter for user {}", orderFilter.getUserId());
        repository.save(orderFilter);
    }

    @Override
    public OrderFilter getOrderFilter() {
        var userName = SecurityUtils.getUserName();
        return repository.findByUserId(userName).orElse(new OrderFilter());
    }
}
