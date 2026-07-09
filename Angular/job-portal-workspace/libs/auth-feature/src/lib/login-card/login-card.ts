import { Component } from '@angular/core';
import { AuthCard } from 'shared-ui';

@Component({
  selector: 'auth-login-card',
  imports: [AuthCard],
  templateUrl: './login-card.html',
  styleUrl: './login-card.css',
})
export class LoginCard {}
