import { Body, Controller, Delete, Get, Param, Post, Put } from '@nestjs/common';
import { VendedoresService } from './vendedores.service';
import { CreateVendedorDto } from './dto/create-vendedor.dto';
import { UpdateVendedorDto } from './dto/update-vendedor.dto';

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

  @Put(':id')
  async update(
    @Param('id') id: string,
    @Body() updateVendedorDto: UpdateVendedorDto,
  ) {
    return await this.vendedoresService.update(id, updateVendedorDto);
  }

  @Delete(':id')
  async remove(@Param('id') id: string) {
    return await this.vendedoresService.remove(id);
  }
}