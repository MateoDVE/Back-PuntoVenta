export class SignUpDto {
  email: string;
  password: string;
  nombre: string;
  rol?: string;
}

export class SignInDto {
  email: string;
  password: string;
  nombre?: string;
  rol?: string;
}

export class UpdateUserProfileDto {
  nombre?: string;
  rol?: string;
  estado?: string;
}
