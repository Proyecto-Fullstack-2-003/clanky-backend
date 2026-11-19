

package cl.clanky.carrito.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;

@Component
public class CatalogoClient {

    private final RestClient restClient;
    private final String internalKey;

    public CatalogoClient(
            @Value("${app.catalogo-url}") String catalogoUrl,
            @Value("${app.internal-key}") String internalKey) {

        this.restClient = RestClient.builder()
            .baseUrl(catalogoUrl)
            .build();

        this.internalKey = internalKey;
    }

    public VentaResponse procesarVenta(
        String ventaId,
        List<ProductoVentaRequest> productos) {

    try {
        return restClient.post()
            .uri("/internal/ventas/{id}", ventaId)
            .header("X-Internal-Key", internalKey)
            .contentType(MediaType.APPLICATION_JSON)
            .body(productos)
            .retrieve()
    .onStatus(
        status -> status.value() == 409,
        (request, response) -> {
            throw new org.springframework.web.client.HttpClientErrorException(
                org.springframework.http.HttpStatus.CONFLICT,
                "Stock insuficiente"
            );
        }
    )
            .body(VentaResponse.class);

    } catch (Exception e) {
        e.printStackTrace();
        throw e;
    }
}

    public record ProductoVentaRequest(Long id, Integer cantidad) {}

public record VentaResponse(
    List<VentaLineaResponse> items,
    BigDecimal subtotal
) {}

public record VentaLineaResponse(
    Long id,
    String nombre,
    BigDecimal precio,
    int cantidad
) {}
}
