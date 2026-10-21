package com.mercadocentro.payment;

import com.mercadocentro.sale.SaleRepository;
import com.mercadocentro.sale.SaleStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class MercadoPagoWebhookController {

    private final SaleRepository saleRepo;
    private final RestClient mercadoPagoRestClient;

    @PostMapping("/mercadopago")
    public ResponseEntity<Void> handleWebhook(@RequestBody(required = false) Map<String, Object> payload, @RequestParam Map<String, String> params) {
        log.info("Webhook MP recibido - payload: {} params: {}", payload, params);

        try {
            String type = null;
            String paymentId = null;

            if (payload != null && payload.get("data") != null) {
                Map data = (Map) payload.get("data");
                if (data.get("id") != null) {
                    paymentId = String.valueOf(data.get("id"));
                }
                type = (String) payload.get("type");
            }

            if (paymentId == null && params.containsKey("data.id")) {
                paymentId = params.get("data.id");
            }
            if (type == null && params.containsKey("type")) {
                type = params.get("type");
            }

            if (paymentId == null) return ResponseEntity.ok().build();
            if (type != null && !"payment".equals(type)) return ResponseEntity.ok().build();

            Map payment = mercadoPagoRestClient.get()
                    .uri("/v1/payments/" + paymentId)
                    .retrieve()
                    .body(Map.class);

            if (payment == null) return ResponseEntity.ok().build();

            String status = (String) payment.get("status");
            String externalRef = (String) payment.get("external_reference");

            if ("approved".equals(status) && externalRef != null) {
                saleRepo.findByInvoiceNumber(externalRef).ifPresent(sale -> {
                    if (sale.getStatus() == SaleStatus.PENDING) {
                        sale.setStatus(SaleStatus.PAID);
                        sale.setPaidAt(LocalDateTime.now());
                        sale.setMpPaymentId(paymentId);
                        saleRepo.save(sale);
                        log.info("Venta {} marcada como PAGADA", externalRef);
                    }
                });
            }

        } catch (Exception e) {
            log.error("Error en webhook", e);
        }

        return ResponseEntity.ok().build();
    }
}