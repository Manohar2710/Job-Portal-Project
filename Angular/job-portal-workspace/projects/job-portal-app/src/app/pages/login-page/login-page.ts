import { Component } from '@angular/core';
import { LoginCard } from 'auth-feature';
@Component({
  selector: 'app-login-page',
  templateUrl: './login-page.html',
  styleUrl: './login-page.scss',
  imports: [LoginCard]
})
export class LoginPage {}
