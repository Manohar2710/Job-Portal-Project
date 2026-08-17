import { Component, inject } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthCard, AuthButton, FormField, HighlightOnHover, Truncate, Theme } from 'shared-ui';
import { AuthService } from '../../auth.service';
import { map, Observable, take, timer } from 'rxjs';
import { AsyncPipe, CommonModule } from '@angular/common';

@Component({
  selector: 'auth-login-card',
  imports: [AuthCard, 
    FormField, 
    AuthButton, 
    ReactiveFormsModule, 
    HighlightOnHover, 
    AsyncPipe, 
    CommonModule,
    Truncate
  ],
  templateUrl: './login-card.html',
  styleUrl: './login-card.css',
})
export class LoginCard {
  private fb          = inject(FormBuilder);
  private authService = inject(AuthService);
  private router      = inject(Router);

  // class Constructor injection example
  private theme: Theme;
  constructor() {
    this.theme = inject(Theme);
  }


  isLoading = false;
  serverError: string | null = null;

  stringForTruncatePipe = 'Angular custom pipes allow you to format template values easily.';
  form = this.fb.group({
    email:    ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]],
  });

  // async pipe HOT example
  readonly countDown$: Observable<number> = timer(0, 1000).pipe(
    map(i => 25 - i),
    take(26)
  )

  ngOnInit() {
    console.log(this.theme.color); // prints Light Mode
  }

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

  navigateToRegisterScreen(){
    this.router.navigate(['./register'])
  }
}
