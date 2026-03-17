import {
  BadRequestException,
  Injectable,
  InternalServerErrorException,
  NotFoundException,
} from '@nestjs/common';
import { SupabaseService } from '../supabase/supabase.service';
import { CreateVendedorDto } from './dto/create-vendedor.dto';
import { UpdateVendedorDto } from './dto/update-vendedor.dto';

@Injectable()
export class VendedoresService {
  constructor(private readonly supabaseService: SupabaseService) {}

  async create(createVendedorDto: CreateVendedorDto) {
    const { nombre, email, password } = createVendedorDto;
    const normalizedEmail = email.trim().toLowerCase();
    const normalizedNombre = nombre.trim(); 
    const supabase = this.supabaseService.getClient();

    const { data: usuarioExistente, error: errorBusqueda } = await supabase
      .from('usuarios')
      .select('*')
      .ilike('email', normalizedEmail)
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
        normalizedEmail,
        password,
        normalizedNombre,
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

    const authUserId = authData.user.id;
    if (!authUserId) {
      throw new InternalServerErrorException(
        'Supabase Auth no devolvio el id del usuario creado',
      );
    } 

    const { data: nuevoVendedor, error: errorInsercion } = await supabase
      .from('usuarios')
      .upsert([
        {
          id_usuario: authUserId,
          nombre: normalizedNombre,
          email: normalizedEmail,
          rol: 'VENDEDOR',
          estado: 'ACTIVO',
        },
      ], {
        onConflict: 'id_usuario',
      })
      .select();

    if (errorInsercion) {
      const isDuplicateEmail =
        errorInsercion.code === '23505' ||
        errorInsercion.message.includes('usuarios_email_key');

      try {
        await this.supabaseService.deleteUserByAdmin(authUserId);
      } catch {
        throw new InternalServerErrorException(
          `Error insertando en usuarios: ${errorInsercion.message}. Ademas, no se pudo revertir el usuario en Auth.`,
        );
      }

      if (isDuplicateEmail) {
        throw new BadRequestException('Ya existe un usuario con ese email');
      }

      throw new InternalServerErrorException(
        `Error insertando en usuarios: ${errorInsercion.message}`,
      );
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

  async update(id: string, updateVendedorDto: UpdateVendedorDto) {
    const supabase = this.supabaseService.getClient();

    const { data: vendedorActual, error: errorBusqueda } = await supabase
      .from('usuarios')
      .select('id_usuario, nombre, email, rol, estado')
      .eq('id_usuario', id)
      .eq('rol', 'VENDEDOR')
      .maybeSingle();

    if (errorBusqueda) {
      throw new InternalServerErrorException(errorBusqueda.message);
    }

    if (!vendedorActual) {
      throw new NotFoundException('Vendedor no encontrado');
    }

    const updates: Record<string, unknown> = {};
    const authUpdates: {
      email?: string;
      password?: string;
      user_metadata?: Record<string, unknown>;
    } = {};

    if (typeof updateVendedorDto.nombre === 'string') {
      const nombre = updateVendedorDto.nombre.trim();
      if (!nombre) {
        throw new BadRequestException('El nombre no puede estar vacio');
      }
      updates.nombre = nombre;
      authUpdates.user_metadata = {
        ...(authUpdates.user_metadata ?? {}),
        nombre,
      };
    }

    if (typeof updateVendedorDto.email === 'string') {
      const email = updateVendedorDto.email.trim().toLowerCase();
      if (!email) {
        throw new BadRequestException('El email no puede estar vacio');
      }

      const { data: emailTomado, error: errorEmail } = await supabase
        .from('usuarios')
        .select('id_usuario')
        .ilike('email', email)
        .neq('id_usuario', id)
        .maybeSingle();

      if (errorEmail) {
        throw new InternalServerErrorException(errorEmail.message);
      }

      if (emailTomado) {
        throw new BadRequestException('Ya existe un usuario con ese email');
      }

      updates.email = email;
      authUpdates.email = email;
    }

    if (typeof updateVendedorDto.password === 'string') {
      const password = updateVendedorDto.password.trim();
      if (password && password.length < 4) {
        throw new BadRequestException(
          'La contraseña debe tener al menos 4 caracteres',
        );
      }

      if (password) {
        authUpdates.password = password;
      }
    }

    if (typeof updateVendedorDto.estado === 'string') {
      const estado = updateVendedorDto.estado.trim().toUpperCase();
      if (!estado) {
        throw new BadRequestException('El estado no puede estar vacio');
      }
      updates.estado = estado;
    }

    if (Object.keys(updates).length === 0 && Object.keys(authUpdates).length === 0) {
      throw new BadRequestException('No se enviaron datos para actualizar');
    }

    if (Object.keys(authUpdates).length > 0) {
      try {
        await this.supabaseService.updateUserByAdmin(id, authUpdates);
      } catch (error: unknown) {
        const message =
          error instanceof Error
            ? error.message
            : 'No se pudo actualizar el usuario en Supabase Auth';
        throw new BadRequestException(message);
      }
    }

    if (Object.keys(updates).length === 0) {
      return {
        ...vendedorActual,
        ...updates,
      };
    }

    const { data: vendedorActualizado, error: errorUpdate } = await supabase
      .from('usuarios')
      .update(updates)
      .eq('id_usuario', id)
      .eq('rol', 'VENDEDOR')
      .select('id_usuario, nombre, email, rol, estado, created_at')
      .single();

    if (errorUpdate) {
      throw new InternalServerErrorException(errorUpdate.message);
    }

    return vendedorActualizado;
  }

  async remove(id: string) {
    const supabase = this.supabaseService.getClient();

    const { data: vendedorActual, error: errorBusqueda } = await supabase
      .from('usuarios')
      .select('id_usuario, rol')
      .eq('id_usuario', id)
      .eq('rol', 'VENDEDOR')
      .maybeSingle();

    if (errorBusqueda) {
      throw new InternalServerErrorException(errorBusqueda.message);
    }

    if (!vendedorActual) {
      throw new NotFoundException('Vendedor no encontrado');
    }

    try {
      await this.supabaseService.deleteUserByAdmin(id);
    } catch (error: unknown) {
      const message =
        error instanceof Error
          ? error.message
          : 'No se pudo eliminar el usuario de Supabase Auth';
      throw new BadRequestException(message);
    }

    const { error: errorDelete } = await supabase
      .from('usuarios')
      .delete()
      .eq('id_usuario', id)
      .eq('rol', 'VENDEDOR');

    if (errorDelete) {
      throw new InternalServerErrorException(errorDelete.message);
    }

    return { message: 'Vendedor eliminado correctamente' };
  }
}
