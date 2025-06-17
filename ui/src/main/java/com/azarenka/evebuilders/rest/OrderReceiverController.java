package com.azarenka.evebuilders.rest;

import com.azarenka.evebuilders.domain.dto.TelegramRequestOrder;
import com.azarenka.evebuilders.domain.db.DistributedOrder;
import com.azarenka.evebuilders.service.api.IDistributedOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class OrderReceiverController {

    @Autowired
    private IDistributedOrderService distributedOrderService;

    @PostMapping(value = "/api/orders", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DistributedOrder> receiveOrder(@RequestBody TelegramRequestOrder telegramRequestOrder) {
        DistributedOrder distributedOrder = distributedOrderService.distributeOrder(telegramRequestOrder);
        return new ResponseEntity<>(distributedOrder, HttpStatus.CREATED);
    }

    @PostMapping(value = "/api/orders/validator", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<String>> validateOrder(@RequestBody TelegramRequestOrder telegramRequestOrder) {
        List<String> errors = distributedOrderService.validateRequest(telegramRequestOrder);
        return new ResponseEntity<>(errors, HttpStatus.CREATED);
    }
}
