import { Injectable, BadRequestException, InternalServerErrorException } from '@nestjs/common';
import { SupabaseService } from './supabase.service';

@Injectable()
export class SupabaseStorageService {
  private readonly bucket = 'productos';
  private readonly maxFileSize = 5 * 1024 * 1024; // 5MB
  private readonly allowedMimeTypes = ['image/jpeg', 'image/png', 'image/webp', 'image/gif'];

  constructor(private readonly supabaseService: SupabaseService) {}

  /**
   * Sube una imagen de producto a Supabase Storage
   * @param file - Buffer del archivo
   * @param fileName - Nombre del archivo original
   * @param productId - ID del producto (opcional)
   * @returns URL pública del archivo subido
   */
  async uploadProductImage(
    file: Buffer,
    fileName: string,
    productId?: number,
  ): Promise<string> {
    // Validar archivo
    this.validateFile(file, fileName);

    // Generar nombre único
    const uniqueFileName = this.generateFileName(fileName, productId);

    try {
      const supabase = this.supabaseService.getClient();

      const { data, error } = await supabase.storage
        .from(this.bucket)
        .upload(uniqueFileName, file, {
          contentType: this.getMimeType(fileName),
          upsert: false,
        });

      if (error) {
        throw new InternalServerErrorException(
          `Error al subir imagen: ${error.message}`,
        );
      }

      // Obtener URL pública
      const {
        data: { publicUrl },
      } = supabase.storage.from(this.bucket).getPublicUrl(data.path);

      return publicUrl;
    } catch (error) {
      if (error instanceof BadRequestException) throw error;
      throw new InternalServerErrorException('Error al procesar la imagen');
    }
  }

  /**
   * Elimina una imagen de Supabase Storage
   * @param imageUrl - URL pública de la imagen
   */
  async deleteProductImage(imageUrl: string): Promise<void> {
    try {
      const filePath = this.extractPathFromUrl(imageUrl);

      if (!filePath) {
        throw new BadRequestException('URL de imagen inválida');
      }

      const supabase = this.supabaseService.getClient();
      const { error } = await supabase.storage
        .from(this.bucket)
        .remove([filePath]);

      if (error && error.message !== 'Not found') {
        throw new InternalServerErrorException(
          `Error al eliminar imagen: ${error.message}`,
        );
      }
    } catch (error) {
      if (error instanceof BadRequestException) throw error;
      throw new InternalServerErrorException('Error al eliminar la imagen');
    }
  }

  /**
   * Valida el archivo antes de subir
   */
  private validateFile(file: Buffer, fileName: string): void {
    // Validar tamaño
    if (file.length > this.maxFileSize) {
      throw new BadRequestException(
        `El archivo excede el tamaño máximo de 5MB (actual: ${(file.length / 1024 / 1024).toFixed(2)}MB)`,
      );
    }

    // Validar tipo MIME
    const mimeType = this.getMimeType(fileName);
    if (!this.allowedMimeTypes.includes(mimeType)) {
      throw new BadRequestException(
        'Tipo de archivo no permitido. Solo se aceptan JPEG, PNG, WebP y GIF',
      );
    }

    if (file.length === 0) {
      throw new BadRequestException('El archivo está vacío');
    }
  }

  /**
   * Genera un nombre único para el archivo
   */
  private generateFileName(fileName: string, productId?: number): string {
    const timestamp = Date.now();
    const randomId = Math.random().toString(36).substring(2, 9);
    const extension = this.getFileExtension(fileName);
    
    if (productId) {
      return `productos/${productId}/${randomId}-${timestamp}.${extension}`;
    }
    
    return `productos/${randomId}-${timestamp}.${extension}`;
  }

  /**
   * Extrae la extensión del archivo
   */
  private getFileExtension(fileName: string): string {
    const parts = fileName.split('.');
    return parts.length > 1 ? parts[parts.length - 1].toLowerCase() : 'jpg';
  }

  /**
   * Obtiene el tipo MIME basado en la extensión
   */
  private getMimeType(fileName: string): string {
    const extension = this.getFileExtension(fileName).toLowerCase();
    const mimeTypes: Record<string, string> = {
      jpg: 'image/jpeg',
      jpeg: 'image/jpeg',
      png: 'image/png',
      webp: 'image/webp',
      gif: 'image/gif',
    };
    return mimeTypes[extension] || 'image/jpeg';
  }

  /**
   * Extrae el path del archivo desde la URL pública
   */
  private extractPathFromUrl(imageUrl: string): string | null {
    // URL formato: https://xxxx.supabase.co/storage/v1/object/public/productos/path/to/file.jpg
    const match = imageUrl.match(/\/storage\/v1\/object\/public\/(.+)$/);
    return match ? match[1] : null;
  }
}
