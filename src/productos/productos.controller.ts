import { Controller, Get, Post, Body, Put, Delete, Param } from '@nestjs/common';
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

  @Put(':id')
  async update(@Param('id') id: string, @Body() updateProductoDto: UpdateProductoDto) {
    return await this.productosService.update(id, updateProductoDto);
  }

  @Delete(':id')
  async delete(@Param('id') id: string) {
    return await this.productosService.delete(id);
  }
}