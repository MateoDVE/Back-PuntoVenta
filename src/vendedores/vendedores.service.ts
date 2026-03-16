import {
  BadRequestException,
  Injectable,
  InternalServerErrorException,
} from '@nestjs/common';
import { SupabaseService } from '../supabase/supabase.service';
import { CreateVendedorDto } from './dto/create-vendedor.dto';

@Injectable()
export class VendedoresService {
  constructor(private readonly supabaseService: SupabaseService) {}

  async create(createVendedorDto: CreateVendedorDto) {
    const { nombre, email, password } = createVendedorDto;
    const supabase = this.supabaseService.getClient();

    const { data: usuarioExistente, error: errorBusqueda } = await supabase
      .from('usuarios')
      .select('*')
      .eq('email', email)
      .maybeSingle();

    if (errorBusqueda) {
      throw new InternalServerErrorException(errorBusqueda.message);
    }

    if (usuarioExistente) {
      throw new BadRequestException('Ya existe un usuario con ese email');
    }

    let authData: { user?: { id?: string } } | null = null;

    try {
      authData = await this.supabaseService.createUserByAdmin(
        email,
        password,
        nombre,
        'VENDEDOR',
      );
} catch (error: unknown) {
      const errorMessage =
        error instanceof Error
          ? error.message
          : 'Error al registrar usuario en Supabase Auth';

      throw new BadRequestException(errorMessage);
    }

    if (!authData?.user) {
      throw new InternalServerErrorException(
        'No se pudo crear el usuario en Supabase Auth',
      );
    }

    const { data: nuevoVendedor, error: errorInsercion } = await supabase
      .from('usuarios')
      .insert([
        {
          nombre,
          email,
          rol: 'VENDEDOR',
          estado: 'ACTIVO',
        },
      ])
      .select();

    if (errorInsercion) {
      throw new InternalServerErrorException(errorInsercion.message);
    }

    return nuevoVendedor;
}


  async findAll() {
    const supabase = this.supabaseService.getClient();

    const { data: vendedores, error } = await supabase
      .from('usuarios')
      .select('id_usuario, nombre, email, rol, estado, created_at')
      .eq('rol', 'VENDEDOR')
      .order('id_usuario', { ascending: true });

    if (error) {
      throw new InternalServerErrorException(error.message);
    }

    return vendedores;
  }
}
