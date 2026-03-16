import { Controller, Get, Post, Body } from '@nestjs/common';
import { ProductosService } from './productos.service';

@Controller('productos')
export class ProductosController {
  constructor(private readonly productosService: ProductosService) {}

  @Get()
  async findAll() {
    return await this.productosService.findAll();
  }

  @Post()
  async create(@Body() producto: any) {
    return await this.productosService.create(producto);
  }
}