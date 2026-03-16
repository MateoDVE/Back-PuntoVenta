import { Body, Controller, Get, Post } from '@nestjs/common';
import { VendedoresService } from './vendedores.service';
import { CreateVendedorDto } from './dto/create-vendedor.dto';

@Controller('vendedores')
export class VendedoresController {
  constructor(private readonly vendedoresService: VendedoresService) {}

  @Post()
  async create(@Body() createVendedorDto: CreateVendedorDto) {
    return await this.vendedoresService.create(createVendedorDto);
  }

  @Get()
  async findAll() {
    return await this.vendedoresService.findAll();
  }
}