import { Component, inject } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthCard, AuthButton, FormField } from 'shared-ui';
import { AuthService } from '../auth.service';

@Component({
  selector: 'auth-login-card',
  imports: [AuthCard, FormField, AuthButton, ReactiveFormsModule],
  templateUrl: './login-card.html',
  styleUrl: './login-card.css',
})
export class LoginCard {
  private fb          = inject(FormBuilder);
  private authService = inject(AuthService);
  private router      = inject(Router);

  isLoading = false;
  serverError: string | null = null;

  form = this.fb.group({
    email:    ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]],
  });

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    this.serverError = null;

    const { email, password } = this.form.value;

    this.authService.login({ email: email!, password: password! }).subscribe({
      next: () => {
        this.isLoading = false;
        this.router.navigate(['/home']);
      },
      error: () => {
        this.isLoading = false;
        this.serverError = 'Invalid email or password. Please try again.';
      },
    });
  }
}
