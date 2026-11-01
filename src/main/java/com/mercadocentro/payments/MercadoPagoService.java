package com.mercadocentro.payment;

import com.mercadocentro.config.MercadoPagoConfig;
import com.mercadocentro.sale.Sale;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.resources.preference.Preference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MercadoPagoService {

    private final MercadoPagoConfig config;
    private final RestClient mercadoPagoRestClient;

    public Preference createPreference(Sale sale) {
        try {
            var items = sale.getItems().stream().map(item ->
                PreferenceItemRequest.builder()
                    .title(item.getProduct().getName())
                    .quantity(item.getQuantity())
                    .unitPrice(item.getPrice())
                    .currencyId("ARS")
                    .build()
            ).toList();

            var request = PreferenceRequest.builder()
                .items(items)
                .externalReference(sale.getInvoiceNumber())
                .notificationUrl("https://tu-api.com/api/webhooks/mercadopago")
                .backUrls(PreferenceBackUrlsRequest.builder()
                    .success("https://mifuturofront.com/success?invoice=" + sale.getInvoiceNumber())
                    .failure("https://mifuturofront.com/failure?invoice=" + sale.getInvoiceNumber())
                    .pending("https://mifuturofront.com/pending?invoice=" + sale.getInvoiceNumber())
                    .build())
                .autoReturn("approved")
                .build();

            return new PreferenceClient().create(request);

        } catch (Exception e) {
            log.error("Error creando preferencia para sale {}", sale.getInvoiceNumber(), e);
            throw new RuntimeException("Error creando preferencia MP", e);
        }
    }

    public Map transferirAutomaticamente(Sale sale, String aliasCbu, String idempotencyKey) {
        var body = Map.of(
            "amount", sale.getTotal(),
            "description", "Liquidacion " + sale.getInvoiceNumber(),
            "external_reference", sale.getInvoiceNumber(),
            "destination", Map.of(
                "type", "bank_account",
                "id", aliasCbu
            ),
            "currency_id", "ARS"
        );

        try {
            Map response = mercadoPagoRestClient.post()
                .uri("/v1/payouts")
                .header("X-Idempotency-Key", idempotencyKey)
                .body(body)
                .retrieve()
                .body(Map.class);

            log.info("Payout creado para {}: {}", sale.getInvoiceNumber(), response);
            return response;

        } catch (Exception e) {
            log.error("Error transfiriendo {}", sale.getInvoiceNumber(), e);
            throw new RuntimeException("Error en transferencia MP", e);
        }
    }

    public Map consultarPayout(String payoutId) {
        return mercadoPagoRestClient.get()
            .uri("/v1/payouts/" + payoutId)
            .retrieve()
            .body(Map.class);
    }
}