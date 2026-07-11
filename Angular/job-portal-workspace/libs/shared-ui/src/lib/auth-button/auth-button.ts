import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'ui-auth-button',
  imports: [CommonModule],
  templateUrl: './auth-button.html',
  styleUrl: './auth-button.css',
})
export class AuthButton {
  @Input() label: string = 'Submit';
  @Input() loading: boolean = false;
  @Input() disabled: boolean = false;

  get isDisabled(): boolean {
    return this.disabled || this.loading;
  }
}
