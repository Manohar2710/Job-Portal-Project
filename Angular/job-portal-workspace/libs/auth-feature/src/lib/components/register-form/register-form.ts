import { Component, ElementRef, inject, Input, QueryList, ViewChild, ViewChildren, ViewContainerRef, ViewEncapsulation } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthCard, FormField, AuthButton } from 'shared-ui';

@Component({
  selector: 'auth-register-form',
  imports: [AuthCard, ReactiveFormsModule, FormField, AuthButton],
  templateUrl: './register-form.html',
  styleUrl: './register-form.css',
  encapsulation: ViewEncapsulation.Emulated
})
export class RegisterForm {
  private fb: FormBuilder = inject(FormBuilder);

  @ViewChild('email', { static : true }) emailField! : ElementRef<HTMLInputElement>; // static true mean it can accessed in ngOninit
    // if static : false it will be rendered conditionally should be accessed in ngOnViewInit()


  @ViewChildren(FormField, {read: ViewContainerRef}) formFields! : ViewContainerRef;

  form = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    firstName: ['', [Validators.required, Validators.minLength(1)]],
    lastName: ['', [Validators.required, Validators.minLength(1)]],
    password: ['', [Validators.required, Validators.minLength(8)]],
    phone: ['', Validators.required, Validators.minLength(10)],

  })

  onSubmit() {
    
  }
}
