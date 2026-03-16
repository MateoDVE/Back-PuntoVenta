import {
  Controller,
  Get,
  Post,
  Body,
  Put,
  Delete,
  Param,
  UseInterceptors,
  UploadedFile,
  BadRequestException,
} from '@nestjs/common';
import { FileInterceptor } from '@nestjs/platform-express';
import { ProductosService } from './productos.service';
import { CreateProductoDto, UpdateProductoDto } from './dto';

@Controller('productos')
export class ProductosController {
  constructor(private readonly productosService: ProductosService) {}

  @Get()
  async findAll() {
    return await this.productosService.findAll();
  }

  @Get(':id')
  async findOne(@Param('id') id: string) {
    return await this.productosService.findOne(id);
  }

  @Post()
  async create(@Body() createProductoDto: CreateProductoDto) {
    return await this.productosService.create(createProductoDto);
  }

  @Post('upload')
  @UseInterceptors(FileInterceptor('image'))
  async uploadImage(
    @UploadedFile() file: any,
  ) {
    if (!file) {
      throw new BadRequestException('No se ha proporcionado ningún archivo');
    }

    return await this.productosService.uploadProductImage(file.buffer, file.originalname);
  }

  @Post('upload/:id')
  @UseInterceptors(FileInterceptor('image'))
  async uploadImageWithId(
    @UploadedFile() file: any,
    @Param('id') productId: string,
  ) {
    if (!file) {
      throw new BadRequestException('No se ha proporcionado ningún archivo');
    }

    const id = parseInt(productId, 10);
    return await this.productosService.uploadProductImage(file.buffer, file.originalname, id);
  }

  @Post('delete-image')
  async deleteImage(@Body() body: { imageUrl: string }) {
    const { imageUrl } = body;
    if (!imageUrl) {
      throw new BadRequestException('URL de imagen no proporcionada');
    }
    await this.productosService.deleteProductImage(imageUrl);
    return { message: 'Imagen eliminada correctamente' };
  }

  @Put(':id')
  async update(@Param('id') id: string, @Body() updateProductoDto: UpdateProductoDto) {
    return await this.productosService.update(id, updateProductoDto);
  }

  @Delete(':id')
  async delete(@Param('id') id: string) {
    return await this.productosService.delete(id);
  }
}