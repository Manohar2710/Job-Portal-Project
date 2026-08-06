import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AbstractControl, ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

@Component({
  selector: 'ui-form-field',
  imports: [CommonModule, ReactiveFormsModule, MatFormFieldModule, MatInputModule],
  templateUrl: './form-field.html',
  styleUrl: './form-field.css',
})
export class FormField {
  @Input() label: string = '';
  @Input() type: string = '';       // text | email | password
  @Input() placeholder: string = '';
  @Input() control!: AbstractControl;

  get hasError(): boolean {
    return !!this.control?.invalid && !!this.control?.touched;
  }

  get errorMessage(): string {
    const errors = this.control?.errors;
    if (!errors) return '';
    if (errors['required'])  return `${this.label} is required.`;
    if (errors['email'])     return 'Please enter a valid email address.';
    if (errors['minlength']) {
      const min = errors['minlength'].requiredLength as number;
      return `Must be at least ${min} characters.`;
    }
    return 'Invalid value.';
  }
}
