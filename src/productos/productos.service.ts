import { Injectable, InternalServerErrorException, NotFoundException } from '@nestjs/common';
import { SupabaseService } from '../supabase/supabase.service';
import { SupabaseStorageService } from '../supabase/supabase-storage.service';
import { CreateProductoDto, UpdateProductoDto } from './dto';

@Injectable()
export class ProductosService {
  constructor(
    private readonly supabaseService: SupabaseService,
    private readonly storageService: SupabaseStorageService,
  ) {}

  async findAll() {
    const supabase = this.supabaseService.getClient();
    const { data, error } = await supabase.from('productos').select('*');

    if (error) throw new InternalServerErrorException('Error al conectar con el inventario');
    return data;
  }

  async findOne(id: string) {
    const supabase = this.supabaseService.getClient();
    const { data, error } = await supabase
      .from('productos')
      .select('*')
      .eq('id_producto', id)
      .single();

    if (error || !data) throw new NotFoundException('Producto no encontrado');
    return data;
  }

  async create(createProductoDto: CreateProductoDto) {
    const supabase = this.supabaseService.getClient();
    
    const { data, error } = await supabase
      .from('productos')
      .insert([createProductoDto])
      .select(); 

    if (error) throw new InternalServerErrorException('Error al guardar el producto');
    return data[0];
  }

  async update(id: string, updateProductoDto: UpdateProductoDto) {
    const supabase = this.supabaseService.getClient();

    const { data, error } = await supabase
      .from('productos')
      .update(updateProductoDto)
      .eq('id_producto', id)
      .select();

    if (error) throw new InternalServerErrorException('Error al actualizar el producto');
    if (!data || data.length === 0) throw new NotFoundException('Producto no encontrado');
    
    return data[0];
  }

  async delete(id: string) {
    const supabase = this.supabaseService.getClient();

    const { data, error } = await supabase
      .from('productos')
      .delete()
      .eq('id_producto', id)
      .select();

    if (error) throw new InternalServerErrorException('Error al eliminar el producto');
    if (!data || data.length === 0) throw new NotFoundException('Producto no encontrado');
    
    return { message: 'Producto eliminado correctamente' };
  }

  /**
   * Sube una imagen de producto a Supabase Storage
   * @param fileBuffer - Buffer del archivo
   * @param fileName - Nombre original del archivo
   * @param productId - ID del producto (opcional)
   * @returns URL pública del archivo subido
   */
  async uploadProductImage(
    fileBuffer: Buffer,
    fileName: string,
    productId?: number,
  ): Promise<{ imageUrl: string }> {
    const imageUrl = await this.storageService.uploadProductImage(
      fileBuffer,
      fileName,
      productId,
    );
    return { imageUrl };
  }

  /**
   * Elimina una imagen de un producto
   * @param imageUrl - URL pública de la imagen
   */
  async deleteProductImage(imageUrl: string): Promise<void> {
    await this.storageService.deleteProductImage(imageUrl);
  }
}