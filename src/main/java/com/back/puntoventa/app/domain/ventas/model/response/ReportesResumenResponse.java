package com.back.puntoventa.app.domain.ventas.model.response;

import com.back.puntoventa.app.domain.productos.model.Producto;
import com.back.puntoventa.app.domain.vendedores.model.Vendedor;
import java.util.List;

public record ReportesResumenResponse(
    List<VentaResumenResponse> ventas,
    List<Vendedor> vendedores,
    List<Producto> productos,
    List<DiscrepanciaVendedorDto> discrepancias
) {}
