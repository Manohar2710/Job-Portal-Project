import { Component } from '@angular/core';
import { LoginCard } from 'auth-feature';
@Component({
  selector: 'app-login',
  templateUrl: './login.html',
  styleUrl: './login.scss',
  imports: [LoginCard]
})
export class LoginComponent {}
