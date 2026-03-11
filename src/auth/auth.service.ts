import { Injectable } from '@nestjs/common';
import { SupabaseService } from '../supabase/supabase.service';
import { SignUpDto } from './dto/auth.dto';

@Injectable()
export class AuthService {
  constructor(private supabaseService: SupabaseService) {}

  async signUp(signUpDto: SignUpDto) {
    return await this.supabaseService.signUp(
      signUpDto.email,
      signUpDto.password,
      signUpDto.nombre,
      signUpDto.rol || 'VENDEDOR'
    );
  }

  async signIn(email: string, password: string) {
    return await this.supabaseService.signIn(email, password);
  }

  async signOut(userId: string) {
    return { message: 'Sesión cerrada exitosamente' };
  }

  async validateUser(token: string) {
    return await this.supabaseService.verifyToken(token);
  }

  async getUserProfile(userId: string) {
    const supabase = this.supabaseService.getClient();
    const { data, error } = await supabase
      .from('usuarios')
      .select('id_usuario, nombre, email, rol, estado, created_at')
      .eq('id_usuario', userId)
      .single();

    if (error) throw error;
    return data;
  }
}
