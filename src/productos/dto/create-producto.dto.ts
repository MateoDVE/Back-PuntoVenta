export class CreateProductoDto {
  sku: string;
  nombre: string;
  precio_unidad: number;
  precio_caja: number;
  unidades_por_caja: number;
  id_categoria?: number;
  descripcion?: string;
  url_imagen?: string;
  stock_almacen_central?: number;
}
