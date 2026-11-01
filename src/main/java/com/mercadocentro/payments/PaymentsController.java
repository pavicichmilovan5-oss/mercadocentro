@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final SaleService saleService;
    private final MercadoPagoService mercadoPagoService;

    @PostMapping("/checkout")
    public ResponseEntity<?> createCheckout(@RequestBody SaleRequestDTO dto) {
        var sale = saleService.createSale(dto); 
        var pref = mercadoPagoService.createPreference(sale);
        
        return ResponseEntity.ok(Map.of(
            "init_point", pref.getInitPoint(),
            "saleId", sale.getId()
        ));
    }

    @PostMapping("/payout/{saleId}")
    public ResponseEntity<?> payout(@PathVariable Long saleId) {
        var sale = saleService.findById(saleId);
        String idempotencyKey = UUID.randomUUID().toString();
        
        var result = mercadoPagoService.transferirAutomaticamente(
            sale, 
            "alias.del.vendedor",
            idempotencyKey
        );
        
        return ResponseEntity.ok(result);
    }
}