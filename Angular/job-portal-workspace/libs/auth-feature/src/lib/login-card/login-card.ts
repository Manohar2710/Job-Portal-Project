import { Component, inject } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { AuthCard, FormField } from 'shared-ui';

@Component({
  selector: 'auth-login-card',
  imports: [AuthCard, FormField, ReactiveFormsModule],
  templateUrl: './login-card.html',
  styleUrl: './login-card.css',
})
export class LoginCard {
    private fb = inject(FormBuilder);

  form = this.fb.group({
    email:    ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]],
  });
}
