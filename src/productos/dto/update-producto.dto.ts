export class UpdateProductoDto {
  sku?: string;
  nombre?: string;
  descripcion?: string;
  url_imagen?: string;
  precio_unidad?: number;
  precio_caja?: number;
  unidades_por_caja?: number;
  id_categoria?: number;
  stock_almacen_central?: number;
}
