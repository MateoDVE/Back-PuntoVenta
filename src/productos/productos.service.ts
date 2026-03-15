import { Injectable, InternalServerErrorException } from '@nestjs/common';
import { SupabaseService } from '../supabase/supabase.service';

@Injectable()
export class ProductosService {
  constructor(private readonly supabaseService: SupabaseService) {}


  async findAll() {
    const supabase = this.supabaseService.getClient();
    const { data, error } = await supabase.from('productos').select('*');

    if (error) throw new InternalServerErrorException('Error al conectar con el inventario');
    return data;
  }


  async create(nuevoProducto: any) {
    const supabase = this.supabaseService.getClient();
    
    const { data, error } = await supabase
      .from('productos')
      .insert([nuevoProducto])
      .select(); 

    if (error) throw new InternalServerErrorException('Error al guardar el producto');
    return data;
  }
}