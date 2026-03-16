import { Injectable, InternalServerErrorException, NotFoundException } from '@nestjs/common';
import { SupabaseService } from '../supabase/supabase.service';
import { CreateProductoDto, UpdateProductoDto } from './dto';

@Injectable()
export class ProductosService {
  constructor(private readonly supabaseService: SupabaseService) {}

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
}