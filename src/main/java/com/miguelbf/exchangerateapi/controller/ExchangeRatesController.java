package com.miguelbf.exchangerateapi.controller;


import com.miguelbf.exchangerateapi.domain.enums.Currency;
import com.miguelbf.exchangerateapi.domain.record.RatesResponse;
import com.miguelbf.exchangerateapi.service.IExchangeRatesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/rates")
public class ExchangeRatesController {

    private final IExchangeRatesService exchangeRatesService;

    public ExchangeRatesController(IExchangeRatesService exchangeRatesService) {
        this.exchangeRatesService = exchangeRatesService;
    }

    @GetMapping
    public ResponseEntity<RatesResponse> getExchangeRates(
        @RequestParam Currency from,
        @RequestParam(required = false) Currency to
    ){
        RatesResponse response = exchangeRatesService.getRates(from, to);
        return ResponseEntity.ok(response);
    }

}
